package com.bobodroid.myapplication.models.viewmodels


import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bobodroid.myapplication.MainActivity
import com.bobodroid.myapplication.MainActivity.Companion.TAG
import com.bobodroid.myapplication.extensions.toBigDecimalWon
import com.bobodroid.myapplication.models.datamodels.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.*
import javax.inject.Inject


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

                viewModelScope.launch {
                    if(startDate == "" && endDate == "") {

                        _filterBuyRecordFlow.emit(_buyRecordFlow.value)

                        val totalProfit = sumProfit(
                            action = YenAction.Buy,
                            buyList = _buyRecordFlow.value, sellList = null)

                        totalExpectProfit.emit(totalProfit)

                    } else {
                        val startFilterBuyRecord= buyRecordFlow.value.filter { it.date >= startDate}

                        var endFilterBuyRecord = startFilterBuyRecord.filter { it.date <= endDate}



                        Log.d(TAG, "데이터 : ${endFilterBuyRecord}")

                        _filterBuyRecordFlow.emit(endFilterBuyRecord)

                        val totalProfit = sumProfit(
                            action = YenAction.Buy,
                            buyList = endFilterBuyRecord, sellList = null)

                        totalExpectProfit.emit(totalProfit)

                    }
                }

            }

            YenAction.Sell -> {

                viewModelScope.launch {
                    if(startDate == "" && endDate == "") {

                        _filterSellRecordFlow.emit(_sellRecordFlow.value)

                        val totalProfit = sumProfit(
                            action = YenAction.Sell,
                            buyList = null, sellList = _sellRecordFlow.value)

                        totalSellProfit.emit(totalProfit)

                    } else {
                        val startFilterSellRecord= sellRecordFlow.value.filter { it.date >= startDate}

                        val endFilterSellRecord = startFilterSellRecord.filter { it.date <= endDate}


                        _filterSellRecordFlow.emit(endFilterSellRecord)

                        val totalProfit = sumProfit(
                            action = YenAction.Sell,
                            buyList = null, sellList = endFilterSellRecord)

                        totalSellProfit.emit(totalProfit)


                    }
                }
            }
        }

    }


    // 날짜 선택이 되어도 발생
    // 리스트 변경이 되어도 발생


    //특정값만 인출
    val totalSellProfit = MutableStateFlow("")

    val totalExpectProfit = MutableStateFlow("")



    fun sumProfit(
        action: YenAction = YenAction.Buy,
        buyList: List<YenBuyRecord>?,
        sellList: List<YenSellRecord>?
    ) : String {

        when(action) {
            YenAction.Buy ->  {
                val result = buyList?.map { BigDecimal(it.profit) }

                if(result.isNullOrEmpty()) {
                    return ""
                } else {
                    return result.reduce {first, end ->
                        first + end }.toBigDecimalWon()
                }
            }

            YenAction.Sell -> {
                val result = sellList?.map { BigDecimal(it.exchangeMoney) }

                if(result.isNullOrEmpty()) {
                    return ""
                } else {
                    result.reduceOrNull { first, end ->
                        first!! + end!!
                    }
                    return result.toString()
                }
            }
        }
    }


    // 리스트 매도 값

    val recordInputMoney = MutableStateFlow("")

    var haveMoney = MutableStateFlow("")

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
                        profit = expectSellValue(),
                        buyYenMemo = "",
                        buyYenCategoryName = ""
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

            val filterBuyRecord = _filterBuyRecordFlow.value

            val buyItems = buyRecordState.toMutableList().apply {
                remove(yenBuyRecord)
            }.toList()

            val filterBuyItems = filterBuyRecord.toMutableList().apply {
                remove(yenBuyRecord)
            }.toList()

            _buyRecordFlow.value = buyItems

            _filterBuyRecordFlow.value = filterBuyItems
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
                    true,
                    "",
                    ""
                )
            )
        }
    }


    fun sellRecordValue(buyRecord: YenBuyRecord) {
        viewModelScope.launch {

            val buyRecordId = buyRecord.id

            investRepository
                .addRecord(
                    YenSellRecord(
                        id = buyRecordId,
                        date = sellDateFlow.value,
                        money = haveMoney.value,
                        rate = sellRateFlow.value,
                        exchangeMoney = sellDollarFlow.value,
                        sellYenMemo = "",
                        sellYenCategoryName = "",
                    )
                )
        }
    }

    fun removeSellRecord(yenSellRecord: YenSellRecord) {
        viewModelScope.launch {
            investRepository.deleteRecord(yenSellRecord)

            val sellRecordState = _sellRecordFlow.value

            val filterSellRecord = _filterSellRecordFlow.value

            val sellItems = sellRecordState.toMutableList().apply {
                remove(yenSellRecord)
            }.toList()

            val filterSellItems = filterSellRecord.toMutableList().apply {
                remove(yenSellRecord)
            }.toList()

            _sellRecordFlow.value = sellItems

            _filterSellRecordFlow.value = filterSellItems


        }
    }


    fun sellCalculation() {
        viewModelScope.launch {
            sellDollarFlow.emit(sellValue().toString())
            getPercentFlow.emit(sellPercent().toFloat())

        }
    }


    fun calculateProfit(exchangeRate: ExchangeRate) {

        val buyRecordProfit = buyRecordFlow.value.map { it.profit }

        Log.d(TAG, "yenBuyList 불러온 profit 값 : ${buyRecordProfit}")

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

                    val profit = (((BigDecimal(exChangeMoney).times(BigDecimal(resentRate)))
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

    fun buyYenMemoUpdate(updateData: YenBuyRecord, result:(Boolean) -> Unit){

        var success: Boolean = false
        viewModelScope.launch {
            val successValue = investRepository.updateRecord(updateData)

            Log.d(TAG, "업데이트 성공 ${successValue}")
            success = if(successValue > 0) true else false
            result(success)

        }
    }

    fun sellYenMemoUpdate(updateData: YenSellRecord, result:(Boolean) -> Unit){

        var success: Boolean = false
        viewModelScope.launch {
            val successValue = investRepository.updateRecord(updateData)

            Log.d(TAG, "업데이트 성공 ${successValue}")
            success = if(successValue > 0) true else false
            result(success)

        }
    }

    suspend fun cancelSellRecord(id: UUID): Boolean {

        val searchBuyRecord = investRepository.getYenRecordId(id)

        Log.d(TAG, "cancelSellRecord: ${searchBuyRecord}, sellId: ${id}")

        if(searchBuyRecord == null) {
            return false
        } else {
            val updateData = searchBuyRecord.copy(recordColor = false)

            investRepository.updateRecord(updateData)

            return true}
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

    private fun lastValue() = BigDecimal(moneyInputFlow.value).divide(BigDecimal(rateInputFlow.value), 20, RoundingMode.HALF_UP) * BigDecimal("100")


    private fun sellValue() = (
            (BigDecimal(haveMoney.value).times(BigDecimal(sellRateFlow.value)))
                .setScale(20, RoundingMode.HALF_UP)
            ) / BigDecimal("100") - BigDecimal(recordInputMoney.value)

    private fun sellPercent(): Float =
        ((sellDollarFlow.value.toFloat() / recordInputMoney.value.toFloat()) * 100f)

}
