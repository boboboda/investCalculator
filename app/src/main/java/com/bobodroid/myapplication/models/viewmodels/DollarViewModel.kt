package com.bobodroid.myapplication.models.viewmodels


import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bobodroid.myapplication.MainActivity.Companion.TAG
import com.bobodroid.myapplication.extensions.toWon
import com.bobodroid.myapplication.models.datamodels.DrBuyRecord
import com.bobodroid.myapplication.models.datamodels.DrSellRecord
import com.bobodroid.myapplication.models.datamodels.ExchangeRate
import com.bobodroid.myapplication.models.datamodels.InvestRepository
import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.firestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.*
import javax.inject.Inject
import kotlin.math.roundToInt


@HiltViewModel
class DollarViewModel @Inject constructor(private val investRepository: InvestRepository): ViewModel() {

    private val _buyRecordFlow = MutableStateFlow<List<DrBuyRecord>>(emptyList())
    val buyRecordFlow = _buyRecordFlow.asStateFlow()

    private val _filterBuyRecordFlow = MutableStateFlow<List<DrBuyRecord>>(emptyList())
    val filterBuyRecordFlow = _filterBuyRecordFlow.asStateFlow()

    private val _sellRecordFlow = MutableStateFlow<List<DrSellRecord>>(emptyList())
    val sellRecordFlow = _sellRecordFlow.asStateFlow()


    private val _filterSellRecordFlow = MutableStateFlow<List<DrSellRecord>>(emptyList())
    val filterSellRecordFlow = _filterSellRecordFlow.asStateFlow()

    init{
        viewModelScope.launch(Dispatchers.IO) {
            investRepository.getAllBuyRecords().distinctUntilChanged()
                .collect {listOfRecord ->
                    if(listOfRecord.isNullOrEmpty()) {
                    Log.d(TAG, "Empty buy list")
                    } else {
                        _buyRecordFlow.value = listOfRecord
                        _filterBuyRecordFlow.value = listOfRecord

                    }
                }
        }
        viewModelScope.launch(Dispatchers.IO) {
            investRepository.getAllSellRecords().distinctUntilChanged()
                .collect {listOfRecord ->
                    if(listOfRecord.isNullOrEmpty()) {
                        Log.d(TAG, "Empty sell list")
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

    val exchangeMoney = MutableStateFlow(0f)

    val selectedCheckBoxId = MutableStateFlow(1)

    // 날짜 관련
    val time = Calendar.getInstance().time

    val formatter = SimpleDateFormat("yyyy-MM-dd")

    val today = formatter.format(time)

    val onWeek = dateWeek(1)

    val oneMonth = dateMonth(1)


    fun dateWeek(week: Int): String? {
        val c: Calendar = GregorianCalendar()
        c.add(Calendar.DAY_OF_WEEK, - week)
        val sdfr = SimpleDateFormat("yyy-MM-dd")
        return sdfr.format(c.time).toString()
    }
    fun dateMonth(month: Int): String? {
        val c: Calendar = GregorianCalendar()
        c.add(Calendar.MONTH, - month)
        val sdfr = SimpleDateFormat("yyy-MM-dd")
        return sdfr.format(c.time).toString()
    }

    val oneWeekFlow = MutableStateFlow("$onWeek")

    val oneMonthFlow = MutableStateFlow("$oneMonth")


    // 선택된 날짜
    val dateFlow = MutableStateFlow("${LocalDate.now()}")

    val sellDateFlow = MutableStateFlow("${LocalDate.now()}")

    val changeDateAction = MutableStateFlow(2)




    enum class Action {
        Buy, Sell
    }

    fun buyDateRangeInvoke(
        action: Action = Action.Buy,
        startDate: String,
        endDate: String) {


        when(action) {
            Action.Buy -> {
                val startFilterBuyRecord= buyRecordFlow.value.filter { it.date >= startDate}

                var endFilterBuyRecord = startFilterBuyRecord.filter { it.date <= endDate}


                viewModelScope.launch {
                    _filterBuyRecordFlow.emit(endFilterBuyRecord)
                }
            }

            Action.Sell -> {

                val startFilterSellRecord= sellRecordFlow.value.filter { it.date >= startDate}

                var endFilterSellRecord = startFilterSellRecord.filter { it.date <= endDate}


                viewModelScope.launch {
                    _filterSellRecordFlow.emit(endFilterSellRecord)
                }
            }
        }

    }

    val sellStartDateFlow = MutableStateFlow("${LocalDate.now()}")

    val sellEndDateFlow = MutableStateFlow("${LocalDate.now()}")

    val startFilterRecordFlow = sellRecordFlow.combine(sellStartDateFlow.filterNot { it.isEmpty() }) { sellRecordList, startDate ->
        sellRecordList.filter { it.date >= startDate } }

    val endFilterRecordFlow = startFilterRecordFlow.combine(sellEndDateFlow.filterNot { it.isEmpty() }) { sellRecordList, endDate ->
        sellRecordList.filter { it.date <= endDate }
    }


    //특정값만 인출
    val sellGetMoney =  endFilterRecordFlow.filterNot {it.isEmpty()}.map { list ->
        val result = list
            .map{
                it.exchangeMoney }
            .reduce{first, end ->
                first + end
            }
        return@map result
    }

    val total = sellGetMoney.map { it.toWon() }



    var changeMoney = MutableStateFlow(1)

    // 리스트 매도 값

    val recordInputMoney = MutableStateFlow(0)

    var haveMoneyDollar = MutableStateFlow(0f)

    val sellRateFlow = MutableStateFlow("")

    val sellDollarFlow = MutableStateFlow("")

    val getPercentFlow = MutableStateFlow(0f)

    val sellRecordActionFlow = MutableStateFlow(false)



    fun buyDollarAdd() {
        viewModelScope.launch {
            exchangeMoney.emit(lastValue().toFloat())
            investRepository
                .addRecord(DrBuyRecord(
                    date = dateFlow.value,
                    money = moneyInputFlow.value,
                    rate = rateInputFlow.value,
                    exchangeMoney = exchangeMoney.value,
                    recordColor = sellRecordActionFlow.value
                ))

            // 데이터 값 초기화
            buyRecordFlow.collectLatest {
                moneyInputFlow.emit("")
                rateInputFlow.emit("")
            }

        }
    }

    fun removeBuyRecord(drBuyrecord: DrBuyRecord) {
        viewModelScope.launch {
            investRepository.deleteRecord(drBuyrecord)

            val buyRecordState = _buyRecordFlow.value
            val items = buyRecordState.toMutableList().apply{
                remove(drBuyrecord)
            }.toList()
            _buyRecordFlow.value = items

        }
    }

    fun updateBuyRecord(drBuyrecord: DrBuyRecord) {
        viewModelScope.launch {
            investRepository.updateRecord(
                DrBuyRecord(
                    drBuyrecord.id,
                    drBuyrecord.date,
                    drBuyrecord.money,
                    drBuyrecord.rate,
//                    drBuyrecord.profit,
                    drBuyrecord.exchangeMoney,
                    true))
        }
    }


    fun sellRecordValue() {
        viewModelScope.launch {
            investRepository
                .addRecord(
                    DrSellRecord(
                date = sellDateFlow.value,
                money = haveMoneyDollar.value.toString(),
                rate = sellRateFlow.value,
                exchangeMoney = sellDollarFlow.value.toFloat()
            )
                )
        }
    }

    fun removeSellRecord(drSellRecord: DrSellRecord) {
        viewModelScope.launch {
            investRepository.deleteRecord(drSellRecord)

            val sellRecordState = _sellRecordFlow.value
            val items = sellRecordState.toMutableList().apply{
                remove(drSellRecord)
            }.toList()
            _sellRecordFlow.value = items


        }
    }


    fun sellCalculation() {
        viewModelScope.launch {
            sellDollarFlow.emit(sellValue().toString())
            getPercentFlow.emit(sellPercent().toFloat())

        }
    }


    fun resetValue() {
        viewModelScope.launch {
            sellRateFlow.emit("")
        }
    }

    private fun lastValue(): Float = (moneyInputFlow.value.toFloat() / rateInputFlow.value.toFloat())

    private fun sellValue(): Float = (haveMoneyDollar.value * sellRateFlow.value.toFloat()) - (recordInputMoney.value)

    private fun sellPercent(): Float = (sellDollarFlow.value.toFloat() / recordInputMoney.value.toFloat()) * 100f

}


