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

    private val _groupBuyRecordFlow = MutableStateFlow<Map<String, List<YenBuyRecord>>>(emptyMap())
    val groupBuyRecordFlow = _groupBuyRecordFlow.asStateFlow()

    private val _filterSellRecordFlow = MutableStateFlow<List<YenSellRecord>>(emptyList())
    val filterSellRecordFlow = _filterSellRecordFlow.asStateFlow()

    val groupList = MutableStateFlow<List<String>>(emptyList())


    init {

        viewModelScope.launch(Dispatchers.IO) {

            val buyRecordFlow = investRepository.getAllYenBuyRecords().distinctUntilChanged()
            val sellRecordFlow = investRepository.getAllYenSellRecords().distinctUntilChanged()

            val combinedFlow = buyRecordFlow.combine(sellRecordFlow) { buyRecord, sellRecord ->
                combineBuyAndSell(sellRecord, buyRecord)

                if (buyRecord.isNullOrEmpty()) {
                    Log.d(TAG, "Yen Empty Buy list")
                    _buyRecordFlow.value = emptyList()
                    _filterBuyRecordFlow.value = emptyList()
                    _groupBuyRecordFlow.value = setGroup(emptyList())
                } else {

                    groupList.emit(buyRecord.map { it.buyYenCategoryName!! }.distinct())

                    _buyRecordFlow.value = buyRecord
                    _filterBuyRecordFlow.value = buyRecord
                    _groupBuyRecordFlow.value = setGroup(buyRecord)
                }


                if (sellRecord.isNullOrEmpty()) {
                    Log.d(TAG, "Yen Empty Sell list")
                    _sellRecordFlow.value = emptyList()
                    _filterSellRecordFlow.value = emptyList()
                } else {
                    _sellRecordFlow.value = sellRecord
                    _filterSellRecordFlow.value = sellRecord
                }

            }

            combinedFlow.collect {
                Log.d(TAG, "init 완료")
            }

        }
    }

    //사용자 기록


    val moneyInputFlow = MutableStateFlow("")

    val rateInputFlow = MutableStateFlow("")

    val exchangeMoney = MutableStateFlow("")

    val selectedBoxId = MutableStateFlow(1)

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

    fun setGroup(recordList: List<YenBuyRecord>): Map<String, List<YenBuyRecord>> {

        val makeGroup = recordList.sortedBy { it.date }.groupBy { it.buyYenCategoryName!! }

        return makeGroup
    }

    fun combineBuyAndSell(sellRecord: List<YenSellRecord>, buyRecord: List<YenBuyRecord>) {


        if (!sellRecord.isNullOrEmpty() && !buyRecord.isNullOrEmpty()) {
            Log.d(TAG, "매도 리스트 ${sellRecord}, ${buyRecord}")
            buyRecord.forEach { buy ->
                if (buy.buyYenCategoryName.isNullOrEmpty()) buy.buyYenCategoryName =
                    "미지정"
                buy.buyRate = buy.rate
                buy.sellDate =
                    sellRecord.filter { it.id == buy.id }.map { it.date }
                        .firstOrNull() ?: "값없음"
                buy.sellProfit =  sellRecord.filter { it.id == buy.id }
                    .map { it.exchangeMoney }.firstOrNull() ?: ""
                buy.sellRate =
                    sellRecord.filter { it.id == buy.id }.map { it.rate }
                        .firstOrNull() ?: "값없음"

                _buyRecordFlow.value = buyRecord
                _filterBuyRecordFlow.value = buyRecord
                _groupBuyRecordFlow.value = setGroup(buyRecord)

            }
        } else {
            Log.d(TAG, "여기가 실행됨")
        }

    }

    fun dateRangeInvoke(
        startDate: String,
        endDate: String
    ) {
        viewModelScope.launch {
            if (startDate == "" && endDate == "") {

                _filterBuyRecordFlow.emit(_buyRecordFlow.value)
                _filterSellRecordFlow.emit(_sellRecordFlow.value)
                _groupBuyRecordFlow.emit(_buyRecordFlow.value.groupBy { it.buyYenCategoryName!! })

                val totalProfit = sumProfit(
                    action = YenAction.Sell,
                    buyList = null, sellList = _sellRecordFlow.value
                )

                totalSellProfit.emit(totalProfit)

            } else {
                val startFilterSellRecord =
                    sellRecordFlow.value.filter { it.date!! >= startDate }

                val endFilterSellRecord =
                    startFilterSellRecord.filter { it.date!! <= endDate }

                val startFilterBuyRecord =
                    buyRecordFlow.value.filter { it.date!! >= startDate }

                var endFilterBuyRecord =
                    startFilterBuyRecord.filter { it.date!! <= endDate }


                _filterSellRecordFlow.emit(endFilterSellRecord)

                _groupBuyRecordFlow.emit(setGroup(endFilterBuyRecord))
                _filterBuyRecordFlow.emit(endFilterBuyRecord)

                val totalProfit = sumProfit(
                    action = YenAction.Sell,
                    buyList = null, sellList = endFilterSellRecord
                )

                totalSellProfit.emit(totalProfit)
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

                val result = buyList?.filter { it.profit != "" }?.map { BigDecimal(it.profit) }

                if(result.isNullOrEmpty()) {
                    return ""
                } else {
                    if(result.size > 1) {
                        return result.reduce {first, end ->
                            first + end }.toBigDecimalWon()
                    } else {
                        return "${result.first().toBigDecimalWon()}"
                    }
                }

            }

            YenAction.Sell -> {
                val result = sellList?.map { BigDecimal(it.exchangeMoney) }

                Log.d(TAG, "엔화 ${result}")

                if(result.isNullOrEmpty()) {
                    return ""
                } else {
                    if(result.size > 1) {
                        return result.reduce { first, end ->
                            first + end
                        }.toBigDecimalWon()

                    } else {
                        return "${result.first().toBigDecimalWon()}"
                    }

                }
            }
        }
    }


    // 리스트 매도 값

    val recordInputMoney = MutableStateFlow("")

    var haveMoney = MutableStateFlow("")

    val sellRateFlow = MutableStateFlow("")

    val sellYenFlow = MutableStateFlow("")

    val getPercentFlow = MutableStateFlow(0f)

    private val sellRecordActionFlow = MutableStateFlow(false)




    fun buyAddRecord(groupName: String) {
        viewModelScope.launch {
            exchangeMoney.emit("${lastValue(moneyInputFlow.value, rateInputFlow.value)}")
            investRepository
                .addRecord(
                    YenBuyRecord(
                        date = dateFlow.value,
                        money = moneyInputFlow.value,
                        rate = rateInputFlow.value,
                        buyRate = rateInputFlow.value,
                        sellRate = "",
                        sellProfit = "",
                        exchangeMoney = "${exchangeMoney.value}",
                        recordColor = sellRecordActionFlow.value,
                        profit = expectSellValue(),
                        expectProfit = expectSellValue(),
                        buyYenMemo = "",
                        buyYenCategoryName = groupName
                    )
                )

            // 데이터 값 초기화
            buyRecordFlow.collectLatest {
                moneyInputFlow.emit("")
                rateInputFlow.emit("")
            }

        }
    }


    fun insertBuyRecord(existingYenBuyRecord: YenBuyRecord,
                        insertDate: String,
                        insertMoney: String,
                        insertRate: String) {

        viewModelScope.launch {

            val insertDate = existingYenBuyRecord.copy(
                date = insertDate,
                money = insertMoney,
                rate = insertRate,
                buyRate = insertRate,
                profit = "0",
                expectProfit = "0",
                exchangeMoney = lastValue(insertMoney, insertRate).toString())

            investRepository.updateRecord(insertDate)
        }

    }


    fun removeBuyRecord(yenBuyRecord: YenBuyRecord) {
        viewModelScope.launch {
            investRepository.deleteRecord(yenBuyRecord)
        }
    }

    fun updateBuyRecord(yenBuyRecord: YenBuyRecord) {
        viewModelScope.launch {
            investRepository.updateRecord(
                YenBuyRecord(
                    id = yenBuyRecord.id,
                    date = yenBuyRecord.date,
                    sellDate = sellDateFlow.value,
                    rate = yenBuyRecord.rate,
                    buyRate = yenBuyRecord.buyRate,
                    sellRate = sellRateFlow.value,
                    money = yenBuyRecord.money,
                    exchangeMoney = yenBuyRecord.exchangeMoney,
                    profit = yenBuyRecord.profit,
                    expectProfit = yenBuyRecord.profit,
                    sellProfit = sellYenFlow.value,
                    recordColor = true,
                    buyYenMemo = yenBuyRecord.buyYenMemo,
                    buyYenCategoryName = yenBuyRecord.buyYenCategoryName
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
                        exchangeMoney = sellYenFlow.value,
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

    fun groupAdd(newGroupName: String) {

        val updateGroupList = groupList.value.toMutableList().apply {
            add(newGroupName)
        }.toList()

        viewModelScope.launch {
            groupList.emit(updateGroupList)
        }

    }

    fun updateRecordGroup(yenBuyrecord: YenBuyRecord, groupName: String) {
        viewModelScope.launch {

            val updateData = yenBuyrecord.copy(buyYenCategoryName = groupName)

            investRepository.updateRecord(updateData)
        }
    }

    fun sellCalculation() {
        viewModelScope.launch {
            sellYenFlow.emit(sellValue().toString())
            getPercentFlow.emit(sellPercent().toFloat())

        }
    }


    fun calculateProfit(exchangeRate: ExchangeRate) {

        val buyRecordProfit = buyRecordFlow.value.map { it.profit }

        Log.d(TAG, "yenBuyList 불러온 profit 값 : ${buyRecordProfit}")

        _buyRecordFlow.value.forEach { yenBuyRecord ->

            if(yenBuyRecord.recordColor == true) {
                val updateDate = yenBuyRecord.copy(profit = "")

                viewModelScope.launch {
                    investRepository.updateRecord(updateDate)
                }
            } else {
                if (yenBuyRecord.profit == null) {

                    Log.d(TAG, "기존 데이터 profit 추가 실행")

                    val resentRate = exchangeRate.exchangeRates?.jpy

                    if (resentRate.isNullOrEmpty()) {
                        Log.d(TAG, "calculateProfit 최신 값 받아오기 실패")

                    } else {
                        val exChangeMoney = yenBuyRecord.exchangeMoney

                        val koreaMoney = yenBuyRecord.money

                        Log.d(MainActivity.TAG, "값을 받아왔니? ${resentRate}")

                        val profit = (((BigDecimal(exChangeMoney).times(BigDecimal(resentRate).times(BigDecimal("100"))))
                            .setScale(20, RoundingMode.HALF_UP)) - BigDecimal(koreaMoney)).toString()

                        Log.d(TAG, "예상 수익 ${profit}")

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

    suspend fun cancelSellRecord(id: UUID): Pair<Boolean, YenSellRecord> {

        val searchBuyRecord = investRepository.getYenRecordId(id)

        val searchSellRecord = investRepository.getYenSellRecordId(id)

        Log.d(TAG, "cancelBuyRecord: ${searchBuyRecord}, sellId: ${id}")

        Log.d(TAG, "cancelSellRecord: ${searchSellRecord}, sellId: ${id}")

        if (searchBuyRecord == null && searchSellRecord == null) {
            return Pair(false, YenSellRecord())
        } else {
            val updateData = searchBuyRecord.copy(recordColor = false)

            investRepository.updateRecord(updateData)

            return Pair(true, YenSellRecord())
        }
    }

    fun yenCloudLoad(buyRecord: List<YenBuyRecord>, sellRecord: List<YenSellRecord>) {

        viewModelScope.launch {

                investRepository.yenBuyAddListRecord(buyRecord)

                investRepository.yenSellAddListRecord(sellRecord)

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

    private fun lastValue(money:String, rate:String) = BigDecimal(money).divide(BigDecimal(rate), 20, RoundingMode.HALF_UP) * BigDecimal("100")

    private fun sellValue() = (
            (BigDecimal(haveMoney.value).times(BigDecimal(sellRateFlow.value)))
                .setScale(20, RoundingMode.HALF_UP)
            ) / BigDecimal("100") - BigDecimal(recordInputMoney.value)

    private fun sellPercent(): Float =
        ((sellYenFlow.value.toFloat() / recordInputMoney.value.toFloat()) * 100f)

}
