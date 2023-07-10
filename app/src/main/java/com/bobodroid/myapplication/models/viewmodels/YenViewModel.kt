package com.bobodroid.myapplication.models.viewmodels


import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.toMutableStateList
import androidx.core.util.rangeTo
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bobodroid.myapplication.MainActivity
import com.bobodroid.myapplication.extensions.toUs
import com.bobodroid.myapplication.extensions.toWon
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
class YenViewModel @Inject constructor(private val investRepository: InvestRepository): ViewModel() {


    private val _buyRecordFlow = MutableStateFlow<List<YenBuyRecord>>(emptyList())
    val buyRecordFlow = _buyRecordFlow.asStateFlow()



    private val _sellRecordFlow = MutableStateFlow<List<YenSellRecord>>(emptyList())
    val sellRecordFlow = _sellRecordFlow.asStateFlow()


    init{
        viewModelScope.launch(Dispatchers.IO) {
            investRepository.getAllYenBuyRecords().distinctUntilChanged()
                .collect {listOfRecord ->
                    if(listOfRecord.isNullOrEmpty()) {
                        Log.d(MainActivity.TAG, "Empty buy list")
                    } else {
                        _buyRecordFlow.value = listOfRecord
                    }
                }
        }
        viewModelScope.launch(Dispatchers.IO) {
            investRepository.getAllYenSellRecords().distinctUntilChanged()
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

    // 날짜 관련
    val time = Calendar.getInstance().time

    val formatter = SimpleDateFormat("yyyy-MM-dd")

    val today = formatter.format(time)

    val oneMonth = dateMonth(1)

    val onYear = dateYear(1)

    fun dateMonth(month: Int): String? {
        val c: Calendar = GregorianCalendar()
        c.add(Calendar.MONTH, - month)
        val sdfr = SimpleDateFormat("yyy-MM-dd")
        return sdfr.format(c.time).toString()
    }

    fun dateYear(year: Int): String? {
        val c = GregorianCalendar()
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
    val buyDayFilteredRecordFlow : Flow<List<YenBuyRecord>> = buyRecordFlow.combine(dateFlow.filterNot { it.isEmpty() }) { buyRecordList, selectedDate ->
        Log.d(MainActivity.TAG, "바인드 데이터 0: buyRecordList $buyRecordList, selectedDate: $selectedDate")
        buyRecordList.filter { it.date == selectedDate }
    }

    val buyMonthFilterRecordFlow : Flow<List<YenBuyRecord>> = buyRecordFlow.combine(oneMonthFlow.filterNot { it.isEmpty() }) { buyRecordList, selectedDate ->
        buyRecordList.filter {it.date >= selectedDate}
    }

    val buyYearFilterRecordFlow : Flow<List<YenBuyRecord>> = buyRecordFlow.combine(oneYearFlow.filterNot { it.isEmpty() }) { buyRecordList, selectedDate ->
        buyRecordList.filter {it.date >= selectedDate}
    }


    val sellDayFilteredRecordFlow : Flow<List<YenSellRecord>> = sellRecordFlow.combine(dateFlow.filterNot { it.isEmpty() }) { buyRecordList, selectedDate ->
        Log.d(MainActivity.TAG, "바인드 데이터 0: buyRecordList $buyRecordList, selectedDate: $selectedDate")
        buyRecordList.filter { it.date == selectedDate }
    }

    val sellMonthFilterRecordFlow : Flow<List<YenSellRecord>> = sellRecordFlow.combine(oneMonthFlow.filterNot { it.isEmpty() }) { buyRecordList, selectedDate ->
        buyRecordList.filter {it.date >= selectedDate}
    }

    val sellYearFilterRecordFlow : Flow<List<YenSellRecord>> = sellRecordFlow.combine(oneYearFlow.filterNot { it.isEmpty() }) { buyRecordList, selectedDate ->
        buyRecordList.filter {it.date >= selectedDate}
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



    // 캘린더에서 날짜 선택 +
    // 선택된 날짜의 리스트만 가져와야 한다
//    val buyDateRecord: Flow<List<RecordBox>> = buyrecordFlow.map { Item ->
//        Item.filter { dateFlow.value == it.date } }
    // buyDateRecord에서 제가 원하는 날짜의 리스트만 추출해서 뿌려주고 싶습니다.

    var changeMoney = MutableStateFlow(1)

    // 리스트 매도 값

    val recordInputMoney = MutableStateFlow(0)

    var haveMoney = MutableStateFlow(0)

    val sellRateFlow = MutableStateFlow("")

    val sellDollarFlow = MutableStateFlow("")

    val getPercentFlow = MutableStateFlow(0f)

    private val sellRecordActionFlow = MutableStateFlow(false)



    fun buyAddRecord() {
        viewModelScope.launch {
            exchangeMoney.emit(lastValue().toFloat())
            investRepository
                .addRecord(
                    YenBuyRecord(
                    date = dateFlow.value,
                    money = moneyInputFlow.value,
                    rate = rateInputFlow.value,
                    exchangeMoney = exchangeMoney.value,
                    recordColor = sellRecordActionFlow.value
                )
                )

            // 데이터 값 초기화
            buyRecordFlow.collectLatest {
                moneyInputFlow.emit("")
                rateInputFlow.emit("")
            }

        }
    }

    fun removeBuyRecord(yenBuyRecord: YenBuyRecord) {
        viewModelScope.launch {
            investRepository.deleteRecord(yenBuyRecord)

            val buyRecordState = _buyRecordFlow.value
            val items = buyRecordState.toMutableList().apply{
                remove(yenBuyRecord)
            }.toList()
            _buyRecordFlow.value = items

        }
    }

    fun updateBuyRecord(yenBuyRecord: YenBuyRecord) {
        viewModelScope.launch {
            investRepository.updateRecord(
                YenBuyRecord(
                    yenBuyRecord.id,
                    yenBuyRecord.date,
                    yenBuyRecord.money,
                    yenBuyRecord.rate,
                    yenBuyRecord.exchangeMoney,
                    true)
            )
        }
    }


    fun sellRecordValue() {
        viewModelScope.launch {
            investRepository
                .addRecord(
                    YenSellRecord(
                        date = sellDateFlow.value,
                        money = haveMoney.value.toString(),
                        rate = sellRateFlow.value,
                        exchangeMoney = sellDollarFlow.value.toFloat()
                    )
                )
        }
    }

    fun removeSellRecord(yenSellRecord: YenSellRecord) {
        viewModelScope.launch {
            investRepository.deleteRecord(yenSellRecord)

            val sellRecordState = _sellRecordFlow.value
            val items = sellRecordState.toMutableList().apply{
                remove(yenSellRecord)
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

    private fun lastValue(): Int = ((moneyInputFlow.value.toFloat() / rateInputFlow.value.toFloat()) * 100f).roundToInt()

    private fun sellValue(): Int = ((haveMoney.value.toFloat() * sellRateFlow.value.toFloat()) / 100f).toInt() - (recordInputMoney.value.toInt())

    private fun sellPercent(): Float = ((sellDollarFlow.value.toFloat() / recordInputMoney.value.toFloat()) * 100f)

}
