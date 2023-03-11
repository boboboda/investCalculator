package com.bobodroid.myapplication.models.viewmodels


import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.toMutableStateList
import androidx.core.util.rangeTo
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bobodroid.myapplication.MainActivity
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

    init{
        viewModelScope.launch(Dispatchers.IO) {
            investRepository.getAllWonBuyRecords().distinctUntilChanged()
                .collect {listOfRecord ->
                    if(listOfRecord.isNullOrEmpty()) {
                        Log.d(MainActivity.TAG, "Empty buy list")
                    } else {
                        _buyRecordFlow.value = listOfRecord
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
                    }
                }
        }
    }



    //사용자 기록
    val moneyInputFlow = MutableStateFlow("")

    val rateInputFlow = MutableStateFlow("")

    val exchangeMoney = MutableStateFlow(0f)

    val selectedCheckBoxId = MutableStateFlow(1)

    val moneyCgBtnSelected = MutableStateFlow(1)

    // 날짜관련

    val time = Calendar.getInstance().time

    val formatter = SimpleDateFormat("yyyy-MM-dd")

    val today = formatter.format(time)

    val oneMonth = dateMonth(1)

    val onYear = dateYear(1)

    fun dateMonth(month: Int): String? {
        val c: Calendar = GregorianCalendar()
        c.add(Calendar.MONTH, - month)
        val sdfr = SimpleDateFormat("yyyy-MM-dd")
        return sdfr.format(c.time).toString()
    }

    fun dateYear(year: Int): String? {
        val c: Calendar = GregorianCalendar()
        c.add(Calendar.YEAR, - year)
        val sdfr = SimpleDateFormat("yyyy-MM-dd")
        return sdfr.format(c.time).toString()
    }



    val oneMonthFlow = MutableStateFlow("$oneMonth")

    val oneYearFlow = MutableStateFlow("$onYear")

    // 선택된 날짜
    val dateFlow = MutableStateFlow("${LocalDate.now()}")

    val sellDateFlow = MutableStateFlow("${LocalDate.now()}")

    val changeDateAction = MutableStateFlow(2)


    // 날짜 선택이 되어도 발생
    // 리스트 변경이 되어도 발생
    val buyDayFilteredRecordFlow : Flow<List<WonBuyRecord>> = buyRecordFlow.combine(dateFlow.filterNot { it.isEmpty() }) { buyRecordList, selectedDate ->
        buyRecordList.filter { it.date == selectedDate }
    }

    val buyMonthFilterRecordFlow : Flow<List<WonBuyRecord>> = buyRecordFlow.combine(oneMonthFlow.filterNot { it.isEmpty() }) { buyRecordList, selectedDate ->
        buyRecordList.filter {it.date >= selectedDate}
    }


    val buyYearFilterRecordFlow : Flow<List<WonBuyRecord>> = buyRecordFlow.combine(oneYearFlow.filterNot { it.isEmpty() }) { buyRecordList, selectedDate ->
        buyRecordList.filter {it.date >= selectedDate}
    }




    val sellDayFilteredRecordFlow : Flow<List<WonSellRecord>> = sellRecordFlow.combine(dateFlow.filterNot { it.isEmpty() }) { buyRecordList, selectedDate ->
        buyRecordList.filter { it.date == selectedDate }
    }


    val sellMonthFilterRecordFlow : Flow<List<WonSellRecord>> = sellRecordFlow.combine(oneMonthFlow.filterNot { it.isEmpty() }) { buyRecordList, selectedDate ->
        buyRecordList.filter {it.date >= selectedDate}
    }

    val sellYearFilterRecordFlow : Flow<List<WonSellRecord>> = sellRecordFlow.combine(oneYearFlow.filterNot { it.isEmpty() }) { buyRecordList, selectedDate ->
        buyRecordList.filter {it.date >= selectedDate}
    }


    val sellStartDateFlow = MutableStateFlow("${LocalDate.now()}")



    val sellEndDateFlow = MutableStateFlow("${LocalDate.now()}")

    val startFilterRecordFlow = sellRecordFlow.combine(sellStartDateFlow.filterNot { it.isEmpty() }) { sellRecordList, startDate ->
        sellRecordList.filter { it.date >= startDate } }

    val endFilterRecordFlow = startFilterRecordFlow.combine(sellEndDateFlow.filterNot { it.isEmpty() }) { sellRecordList, endDate ->
        sellRecordList.filter { it.date <= endDate }
    }

    val dollarsellGetMoney =  endFilterRecordFlow.map { list ->
        val result = list.filterNot { it.moneyType == 2 }
            return@map result
    }

    val dollarCgValue = dollarsellGetMoney.filterNot { it.isEmpty() }.map { list ->
        val result = list
            .map{ it.exchangeMoney }
            .reduce{first, end ->
                first + end
            }
        return@map result
    }

    val dollartotal = dollarCgValue.map { it.toDecUs() }





    val yensellGetMoney =  endFilterRecordFlow.map { list ->
        val result = list.filterNot { it.moneyType == 1 }
        return@map result
    }


    val yenCgValue = yensellGetMoney.filterNot { it.isEmpty() }.map { list ->
        val result = list
            .map{ it.exchangeMoney }
            .reduce{first, end ->
                first + end
            }.apply { this.toYen() }
        return@map result
    }

    val yentotal = yenCgValue.map { it.toYen() }




    // 캘린더에서 날짜 선택 +
    // 선택된 날짜의 리스트만 가져와야 한다
//    val buyDateRecord: Flow<List<RecordBox>> = buyrecordFlow.map { Item ->
//        Item.filter { dateFlow.value == it.date } }
    // buyDateRecord에서 제가 원하는 날짜의 리스트만 추출해서 뿌려주고 싶습니다.

    var changeMoney = MutableStateFlow(1)

    // 리스트 매도 값

    val recordInputMoney = MutableStateFlow(0)

    var haveMoney = MutableStateFlow(0.0f)

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
                1-> {dollarlastValue()}
                2-> {yenlastValue()}
                else -> {null} }?.let {
                exchangeMoney.emit(it.toFloat())

            }
            investRepository
                .addRecord(
                    WonBuyRecord(
                        date = dateFlow.value,
                        money = moneyInputFlow.value,
                        rate = rateInputFlow.value,
                        exchangeMoney = exchangeMoney.value,
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

    fun reSetValue() {
        viewModelScope.launch {  }
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
                        exchangeMoney = sellDollarFlow.value.toFloat(),
                        moneyType = moneyType.value)
                )
        }
    }



    fun resetValue() {
        viewModelScope.launch {
            sellRateFlow.emit("")
        }
    }


    private fun dollarlastValue(): Int = (moneyInputFlow.value.toFloat() * rateInputFlow.value.toFloat()).roundToInt()

    private fun yenlastValue(): Int = ((moneyInputFlow.value.toFloat() * rateInputFlow.value.toFloat()) / 100f).roundToInt()




    private fun dollarsellValue(): Float = (haveMoney.value / sellRateFlow.value.toFloat()) - recordInputMoney.value

    private fun yensellValue(): Float = ((haveMoney.value / sellRateFlow.value.toFloat()) * 100f) - recordInputMoney.value


    private fun sellPercent(): Float = (sellDollarFlow.value.toFloat() / recordInputMoney.value.toFloat()) * 100f

}


