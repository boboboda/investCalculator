package com.bobodroid.myapplication.models.viewmodels


import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.toMutableStateList
import androidx.core.util.rangeTo
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bobodroid.myapplication.MainActivity
import com.bobodroid.myapplication.MainActivity.Companion.TAG
import com.bobodroid.myapplication.extensions.toDecUs
import com.bobodroid.myapplication.extensions.toUs
import com.bobodroid.myapplication.extensions.toWon
import com.bobodroid.myapplication.extensions.toYen
import com.bobodroid.myapplication.models.datamodels.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.lang.System.out
import java.math.BigDecimal
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.*
import javax.inject.Inject
import kotlin.math.roundToInt
import kotlin.time.days

@HiltViewModel
class WonViewModel @Inject constructor(private val investRepository: InvestRepository): ViewModel() {



    private val _buyRecordFlow = MutableStateFlow<List<WonBuyRecord>>(emptyList())
    val buyRecordFlow = _buyRecordFlow.asStateFlow()

    private val _sellRecordFlow = MutableStateFlow<List<WonSellRecord>>(emptyList())
    val sellRecordFlow = _sellRecordFlow.asStateFlow()

    private val _filterBuyRecordFlow = MutableStateFlow<List<WonBuyRecord>>(emptyList())
    val filterBuyRecordFlow = _filterBuyRecordFlow.asStateFlow()

    private val _filterSellRecordFlow = MutableStateFlow<List<WonSellRecord>>(emptyList())
    val filterSellRecordFlow = _filterSellRecordFlow.asStateFlow()

    init{
        viewModelScope.launch(Dispatchers.IO) {
            investRepository.getAllWonBuyRecords().distinctUntilChanged()
                .collect {listOfRecord ->
                    if(listOfRecord.isNullOrEmpty()) {
                        Log.d(MainActivity.TAG, "Empty buy list")
                    } else {
                        _buyRecordFlow.value = listOfRecord
                        _filterBuyRecordFlow.value = listOfRecord
                    }
                }
        }
        viewModelScope.launch(Dispatchers.IO) {
            investRepository.getAllWonSellRecords().distinctUntilChanged()
                .collect {listOfRecord ->
                    if(listOfRecord.isNullOrEmpty()) {
                        Log.d(MainActivity.TAG, "Empty sell list")
                    } else {
                        _sellRecordFlow.value = listOfRecord
                        _filterSellRecordFlow.value = listOfRecord
                    }
                }
        }
    }



    //사용자 기록
    val moneyInputFlow = MutableStateFlow("")

    val rateInputFlow = MutableStateFlow("")

    val exchangeMoney = MutableStateFlow("")

    val selectedCheckBoxId = MutableStateFlow(1)

    val moneyCgBtnSelected = MutableStateFlow(1)

    // 날짜관련

    val time = Calendar.getInstance().time

    val formatter = SimpleDateFormat("yyyy-MM-dd")

    val today = formatter.format(time)


    // 선택된 날짜
    val dateFlow = MutableStateFlow("${LocalDate.now()}")

    val sellDateFlow = MutableStateFlow("${LocalDate.now()}")


    // 날짜 범위


    enum class WonAction {
        Buy, Sell
    }

    fun dateRangeInvoke(
        action: WonAction = WonAction.Buy,
        startDate: String,
        endDate: String) {


        when(action) {
            WonAction.Buy -> {
                val startFilterBuyRecord= buyRecordFlow.value.filter { it.date >= startDate}

                var endFilterBuyRecord = startFilterBuyRecord.filter { it.date <= endDate}


                viewModelScope.launch {
                    _filterBuyRecordFlow.emit(endFilterBuyRecord)
                }
            }

            WonAction.Sell -> {

                val startFilterSellRecord= sellRecordFlow.value.filter { it.date >= startDate}

                var endFilterSellRecord = startFilterSellRecord.filter { it.date <= endDate}


                viewModelScope.launch {
                    _filterSellRecordFlow.emit(endFilterSellRecord)
                }
            }
        }

    }



//    val dollarCgValue = dollarsellGetMoney.filterNot { it.isEmpty() }.map { list ->
//        val result = list
//            .map{ it.exchangeMoney }
//            .reduce{first, end ->
//                first + end
//            }
//        return@map result
//    }
//
//    val dollartotal = dollarCgValue.map { it.toDecUs() }





//    val yensellGetMoney =  endFilterRecordFlow.map { list ->
//        val result = list.filterNot { it.moneyType == 1 }
//        return@map result
//    }
//
//
//    val yenCgValue = yensellGetMoney.filterNot { it.isEmpty() }.map { list ->
//        val result = list
//            .map{ it.exchangeMoney }
//            .reduce{first, end ->
//                first + end
//            }.apply { this.toYen() }
//        return@map result
//    }
//
//    val yentotal = yenCgValue.map { it.toYen() }





    // 리스트 매도 값

    val recordInputMoney = MutableStateFlow(0)

    var haveMoney = MutableStateFlow("")

    val sellRateFlow = MutableStateFlow("")

    val sellDollarFlow = MutableStateFlow("")

    val getPercentFlow = MutableStateFlow(0f)

    val moneyType = MutableStateFlow(1)

    private val sellRecordActionFlow = MutableStateFlow(false)


    fun buyAddRecord() {
        viewModelScope.launch {
            when(moneyCgBtnSelected.value) {
                1 -> {moneyType.emit(1)}
                2 -> {moneyType.emit(2)}
            }
            when(moneyCgBtnSelected.value) {
                1-> {
                    val dollarCg = dollarlastValue()
                    Log.d(TAG,"값 ${dollarCg}")
                    exchangeMoney.emit("${dollarCg}")
                }
                2-> {
                    val yenCg = yenlastValue()
                    exchangeMoney.emit("${yenCg}")
                }
                else -> {null}}
            investRepository
                .addRecord(
                    WonBuyRecord(
                        date = dateFlow.value,
                        money = moneyInputFlow.value,
                        rate = rateInputFlow.value,
                        exchangeMoney = "${exchangeMoney.value}",
                        recordColor = sellRecordActionFlow.value,
                        moneyType = moneyType.value
                    )
                )

            // 데이터 값 초기화
            buyRecordFlow.collectLatest {
                moneyInputFlow.emit("")
                rateInputFlow.emit("")
            }
        }
    }



    fun sellCalculation() {
        viewModelScope.launch {
            when(moneyType.value) {
                1 -> {
                    sellDollarFlow.emit(dollarsellValue().toString())
                    getPercentFlow.emit(sellPercent())}
                2 -> {
                    sellDollarFlow.emit(yensellValue().toString())
                    getPercentFlow.emit(sellPercent())
                }

            }


        }
    }

    fun removeBuyRecord(wonBuyRecord: WonBuyRecord) {
        viewModelScope.launch {
            investRepository.deleteRecord(wonBuyRecord)

            val buyRecordState = _buyRecordFlow.value
            val items = buyRecordState.toMutableList().apply{
                remove(wonBuyRecord)
            }.toList()
            _buyRecordFlow.value = items

        }
    }


    fun removeSellRecord(wonSellRecord: WonSellRecord) {
        viewModelScope.launch {
            investRepository.deleteRecord(wonSellRecord)

            val sellRecordState = _sellRecordFlow.value
            val items = sellRecordState.toMutableList().apply{
                remove(wonSellRecord)
            }.toList()
            _sellRecordFlow.value = items
        }
    }



    fun updateBuyRecord(wonBuyRecord: WonBuyRecord) {
        viewModelScope.launch {
            investRepository.updateRecord(
                WonBuyRecord(
                    wonBuyRecord.id,
                    wonBuyRecord.date,
                    wonBuyRecord.money,
                    wonBuyRecord.rate,
                    wonBuyRecord.profit,
                    wonBuyRecord.exchangeMoney,
                    true,
                    wonBuyRecord.moneyType)
            )
        }
    }


    fun sellRecordValue() {
        viewModelScope.launch {
            investRepository
                .addRecord(
                    WonSellRecord(
                        date = sellDateFlow.value,
                        money = haveMoney.value.toString(),
                        rate = sellRateFlow.value,
                        exchangeMoney = sellDollarFlow.value,
                        moneyType = moneyType.value)
                )
        }
    }



    fun resetValue() {
        viewModelScope.launch {
            sellRateFlow.emit("")
        }
    }


    private fun dollarlastValue() = BigDecimal(moneyInputFlow.value).times(BigDecimal(rateInputFlow.value))

    private fun yenlastValue() = (BigDecimal(moneyInputFlow.value).times(BigDecimal(rateInputFlow.value)))




    private fun dollarsellValue() = (BigDecimal(haveMoney.value).divide(BigDecimal(sellRateFlow.value))).minus(BigDecimal(recordInputMoney.value))

    private fun yensellValue() =((BigDecimal(haveMoney.value).divide(BigDecimal(sellRateFlow.value))).times(
        BigDecimal("100"))).minus(BigDecimal(recordInputMoney.value))


    private fun sellPercent(): Float = (sellDollarFlow.value.toFloat() / recordInputMoney.value.toFloat()) * 100f

}


