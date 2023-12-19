package com.bobodroid.myapplication.models.viewmodels


import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.toMutableStateList
import androidx.core.util.rangeTo
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bobodroid.myapplication.MainActivity
import com.bobodroid.myapplication.MainActivity.Companion.TAG
import com.bobodroid.myapplication.extensions.toUs
import com.bobodroid.myapplication.extensions.toWon
import com.bobodroid.myapplication.models.datamodels.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.lang.System.out
import java.math.BigDecimal
import java.math.BigInteger
import java.math.RoundingMode
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.*
import javax.inject.Inject
import kotlin.math.roundToInt
import kotlin.time.days


@HiltViewModel
class YenViewModel @Inject constructor(private val investRepository: InvestRepository) :
    ViewModel() {


    private val _buyRecordFlow = MutableStateFlow<List<YenBuyRecord>>(emptyList())
    val buyRecordFlow = _buyRecordFlow.asStateFlow()


    private val _sellRecordFlow = MutableStateFlow<List<YenSellRecord>>(emptyList())
    val sellRecordFlow = _sellRecordFlow.asStateFlow()


    private val _filterBuyRecordFlow = MutableStateFlow<List<YenBuyRecord>>(emptyList())
    val filterBuyRecordFlow = _filterBuyRecordFlow.asStateFlow()

    private val _filterSellRecordFlow = MutableStateFlow<List<YenSellRecord>>(emptyList())
    val filterSellRecordFlow = _filterSellRecordFlow.asStateFlow()


    init {
        viewModelScope.launch(Dispatchers.IO) {
            investRepository.getAllYenBuyRecords().distinctUntilChanged()
                .collect { listOfRecord ->
                    if (listOfRecord.isNullOrEmpty()) {
                        Log.d(MainActivity.TAG, "Empty buy list")
                    } else {
                        _buyRecordFlow.value = listOfRecord
                        _filterBuyRecordFlow.value = listOfRecord
                    }
                }
        }
        viewModelScope.launch(Dispatchers.IO) {
            investRepository.getAllYenSellRecords().distinctUntilChanged()
                .collect { listOfRecord ->
                    if (listOfRecord.isNullOrEmpty()) {
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

    // 날짜 관련
    val time = Calendar.getInstance().time

    val formatter = SimpleDateFormat("yyyy-MM-dd")

    val today = formatter.format(time)


    // 선택된 날짜
    val dateFlow = MutableStateFlow("${LocalDate.now()}")

    val sellDateFlow = MutableStateFlow("${LocalDate.now()}")


    enum class YenAction {
        Buy, Sell
    }

    fun dateRangeInvoke(
        action: YenAction = YenAction.Buy,
        startDate: String,
        endDate: String
    ) {


        when (action) {
            YenAction.Buy -> {
                val startFilterBuyRecord = buyRecordFlow.value.filter { it.date >= startDate }

                var endFilterBuyRecord = startFilterBuyRecord.filter { it.date <= endDate }


                viewModelScope.launch {
                    _filterBuyRecordFlow.emit(endFilterBuyRecord)
                }
            }

            YenAction.Sell -> {

                val startFilterSellRecord = sellRecordFlow.value.filter { it.date >= startDate }

                var endFilterSellRecord = startFilterSellRecord.filter { it.date <= endDate }


                viewModelScope.launch {
                    _filterSellRecordFlow.emit(endFilterSellRecord)
                }
            }
        }

    }


    // 날짜 선택이 되어도 발생
    // 리스트 변경이 되어도 발생


    //특정값만 인출
//    val sellGetMoney =  endFilterRecordFlow.filterNot {it.isEmpty()}.map { list ->
//        val result = list
//            .map{
//                it.exchangeMoney }
//            .reduce{first, end ->
//                first + end
//            }
//        return@map result
//    }
//
//    val total = sellGetMoney.map { it.toWon() }


    // 리스트 매도 값

    val recordInputMoney = MutableStateFlow(0)

    var haveMoney = MutableStateFlow(0)

    val sellRateFlow = MutableStateFlow("")

    val sellDollarFlow = MutableStateFlow("")

    val getPercentFlow = MutableStateFlow(0f)

    private val sellRecordActionFlow = MutableStateFlow(false)


    fun buyAddRecord() {
        viewModelScope.launch {
            exchangeMoney.emit("${lastValue()}")
            investRepository
                .addRecord(
                    YenBuyRecord(
                        date = dateFlow.value,
                        money = moneyInputFlow.value,
                        rate = rateInputFlow.value,
                        exchangeMoney = "${exchangeMoney.value}",
                        recordColor = sellRecordActionFlow.value,
                        profit = expectSellValue()
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
            val items = buyRecordState.toMutableList().apply {
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
                    yenBuyRecord.profit,
                    yenBuyRecord.exchangeMoney,
                    true
                )
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
                        exchangeMoney = sellDollarFlow.value
                    )
                )
        }
    }

    fun removeSellRecord(yenSellRecord: YenSellRecord) {
        viewModelScope.launch {
            investRepository.deleteRecord(yenSellRecord)

            val sellRecordState = _sellRecordFlow.value
            val items = sellRecordState.toMutableList().apply {
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


    fun beforeCalculateProfit(exchangeRate: ExchangeRate) {

        val buyRecordProfit = buyRecordFlow.value.map { it.profit }

        Log.d(MainActivity.TAG, "불러온 profit 값 : ${buyRecordProfit}")

        _buyRecordFlow.value.forEach { yenBuyRecord ->

            if (yenBuyRecord.profit == null) {

                Log.d(MainActivity.TAG, "기존 데이터 profit 추가 실행")

                val resentRate = exchangeRate.exchangeRates?.jpy

                if (resentRate.isNullOrEmpty()) {
                    Log.d(MainActivity.TAG, "calculateProfit 최신 값 받아오기 실패")

                } else {
                    val exChangeMoney = yenBuyRecord.exchangeMoney

                    val koreaMoney = yenBuyRecord.money

                    Log.d(MainActivity.TAG, "값을 받아왔니? ${resentRate}")

                    val profit = (((BigDecimal(exChangeMoney).times(BigDecimal(resentRate).times(BigDecimal("100"))))
                        .setScale(20, RoundingMode.HALF_UP)) - BigDecimal(koreaMoney)).toString()

                    Log.d(MainActivity.TAG, "예상 수익 ${profit}")

                    val updateDate = yenBuyRecord.copy(profit = profit)

                    viewModelScope.launch {
                        investRepository.updateRecord(updateDate)
                    }
                }
            } else {

                Log.d(MainActivity.TAG, "업데이트 데이터 profit 실행")

                val resentRate = exchangeRate.exchangeRates?.jpy

                if (resentRate.isNullOrEmpty()) {
                    Log.d(MainActivity.TAG, "calculateProfit 최신 값 받아오기 실패")

                } else {
                    val exChangeMoney = yenBuyRecord.exchangeMoney

                    val koreaMoney = yenBuyRecord.money

                    Log.d(MainActivity.TAG, "값을 받아왔니? ${resentRate}")

                    val profit = (((BigDecimal(exChangeMoney).times(BigDecimal(resentRate).times(BigDecimal("100"))))
                        .setScale(20, RoundingMode.HALF_UP)) - BigDecimal(koreaMoney)).toString()

                    Log.d(MainActivity.TAG, "예상 수익 ${profit}")

                    val updateDate = yenBuyRecord.copy(profit = profit)

                    viewModelScope.launch {
                        investRepository.updateRecord(updateDate)
                    }
                }


            }

        }
    }


    val yenResentRateStateFlow = MutableStateFlow<ExchangeRate>(ExchangeRate())


    fun requestRate(exchangeRate: ExchangeRate) {
        viewModelScope.launch {
            yenResentRateStateFlow.emit(exchangeRate)
        }
    }


    fun resetValue() {
        viewModelScope.launch {
            sellRateFlow.emit("")
        }
    }

    fun expectSellValue(): String {

        val resentUsRate = yenResentRateStateFlow.value.exchangeRates?.jpy

        Log.d(
            TAG,
            "개별 profit 실행 exchangeMoney:${exchangeMoney.value} 최신환율: ${resentUsRate} 원화: ${moneyInputFlow.value}"
        )

        val profit = ((BigDecimal(exchangeMoney.value).times(BigDecimal(resentUsRate)))
            .setScale(20, RoundingMode.HALF_UP)
                ).minus(BigDecimal(moneyInputFlow.value))
        Log.d(TAG, "개별 profit 결과 값 ${profit}")

        return profit.toString()
    }

    private fun lastValue() = (moneyInputFlow.value.toBigDecimal() / rateInputFlow.value.toBigDecimal())


    private fun sellValue() = (
            (BigDecimal(haveMoney.value).times(BigDecimal(sellRateFlow.value)))
                .setScale(20, RoundingMode.HALF_UP)
            ) - BigDecimal(recordInputMoney.value)

    private fun sellPercent(): Float =
        ((sellDollarFlow.value.toFloat() / recordInputMoney.value.toFloat()) * 100f)

}
