package com.bobodroid.myapplication.models.viewmodels


import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bobodroid.myapplication.MainActivity.Companion.TAG
import com.bobodroid.myapplication.extensions.toBigDecimalWon
import com.bobodroid.myapplication.models.datamodels.DrBuyRecord
import com.bobodroid.myapplication.models.datamodels.DrSellRecord
import com.bobodroid.myapplication.models.datamodels.ExchangeRate
import com.bobodroid.myapplication.models.datamodels.InvestRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import java.lang.reflect.Array.set
import java.math.BigDecimal
import java.math.BigInteger
import java.math.MathContext
import java.math.RoundingMode
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.*
import javax.inject.Inject


@HiltViewModel
class DollarViewModel @Inject constructor(private val investRepository: InvestRepository) :
    ViewModel() {

    private val _buyRecordFlow = MutableStateFlow<List<DrBuyRecord>>(emptyList())
    val buyRecordFlow = _buyRecordFlow.asStateFlow()

    private val _filterBuyRecordFlow = MutableStateFlow<List<DrBuyRecord>>(emptyList())
    val filterBuyRecordFlow = _filterBuyRecordFlow.asStateFlow()

    private val _groupBuyRecordFlow = MutableStateFlow<Map<String, List<DrBuyRecord>>>(emptyMap())
    val groupBuyRecordFlow = _groupBuyRecordFlow.asStateFlow()

    private val _sellRecordFlow = MutableStateFlow<List<DrSellRecord>>(emptyList())
    val sellRecordFlow = _sellRecordFlow.asStateFlow()


    private val _filterSellRecordFlow = MutableStateFlow<List<DrSellRecord>>(emptyList())
    val filterSellRecordFlow = _filterSellRecordFlow.asStateFlow()


    val groupList = MutableStateFlow<List<String>>(emptyList())

    init {
        viewModelScope.launch(Dispatchers.IO) {

            val buyRecordFlow = investRepository.getAllBuyRecords().distinctUntilChanged()
            val sellRecordFlow = investRepository.getAllSellRecords().distinctUntilChanged()

            val combinedFlow = buyRecordFlow.combine(sellRecordFlow) { buyRecord, sellRecord ->
                combineBuyAndSell(sellRecord, buyRecord)

                if (buyRecord.isNullOrEmpty()) {
                    Log.d(TAG, "Empty Buy list")
                } else {

                    groupList.emit(buyRecord.map { it.buyDrCategoryName!! }.distinct())

                    _buyRecordFlow.value = buyRecord
                    _filterBuyRecordFlow.value = buyRecord
                    _groupBuyRecordFlow.value = setGroup(buyRecord)
                }


                if (sellRecord.isNullOrEmpty()) {
                    Log.d(TAG, "Empty Buy list")
                } else {
                    _sellRecordFlow.value = sellRecord
                    _filterSellRecordFlow.value = sellRecord
                }

            }

            combinedFlow.collect {
                Log.d(TAG, "init 완료")
            }




//            investRepository.getAllBuyRecords().distinctUntilChanged()
//                .collect { listOfRecord ->
//                    if (listOfRecord.isNullOrEmpty()) {
//                        Log.d(TAG, "Empty Buy list")
//                    } else {
//
//                        _buyRecordFlow.value = listOfRecord
//                        _filterBuyRecordFlow.value = setGroup(listOfRecord)
//
//                        val groupName = listOfRecord.map { it.buyDrCategoryName ?: "기본" }
//
//                        groupList.value = groupName.distinct()
//
//                        Log.w(TAG, "${_buyRecordFlow.value}")
//
//
//                    }
//                }
        }
//        viewModelScope.launch(Dispatchers.IO) {
//            investRepository.getAllSellRecords().distinctUntilChanged()
//                .collect { listOfRecord ->
//                    if (listOfRecord.isNullOrEmpty()) {
//                        Log.d(TAG, "Empty Sell list")
//                    } else {
//
//                        _sellRecordFlow.value = listOfRecord
//                        _filterSellRecordFlow.value = listOfRecord
//                    }
//                }
//        }


    }
    // 매수매도 통합 작업
    // 예외처리 대비 루프

    fun checkBuyRecordData() {

    }

    fun combineBuyAndSell(sellRecord: List<DrSellRecord>, buyRecord: List<DrBuyRecord>) {


        if (!sellRecord.isNullOrEmpty() && !buyRecord.isNullOrEmpty()) {
            Log.d(TAG, "매도 리스트 ${sellRecord}, ${buyRecord}")
            buyRecord.forEach { buy ->
                if (buy.buyDrCategoryName.isNullOrEmpty()) buy.buyDrCategoryName =
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

    fun groupAdd(newGroupName: String) {

        val updateGroupList = groupList.value.toMutableList().apply {
            add(newGroupName)
        }.toList()

        viewModelScope.launch {
            groupList.emit(updateGroupList)
        }

    }

    fun setGroup(recordList: List<DrBuyRecord>): Map<String, List<DrBuyRecord>> {

        val makeGroup = recordList.sortedBy { it.date }.groupBy { it.buyDrCategoryName!! }

        return makeGroup
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


    enum class DrAction {
        Buy, Sell
    }

    fun dateRangeInvoke(
        action: DrAction = DrAction.Buy,
        startDate: String,
        endDate: String
    ) {

        Log.d(TAG, "전체 데이터 : ${buyRecordFlow.value}")

        when (action) {
            DrAction.Buy -> {

                viewModelScope.launch {
                    if (startDate == "" && endDate == "") {

                        _filterBuyRecordFlow.emit(_buyRecordFlow.value)
                        _groupBuyRecordFlow.emit(_buyRecordFlow.value.groupBy { it.buyDrCategoryName!! })

                        val totalProfit = sumProfit(
                            action = DrAction.Buy,
                            buyList = _buyRecordFlow.value, sellList = null
                        )

                        totalExpectProfit.emit(totalProfit)

                    } else {
                        val startFilterBuyRecord =
                            buyRecordFlow.value.filter { it.date!! >= startDate }

                        var endFilterBuyRecord =
                            startFilterBuyRecord.filter { it.date!! <= endDate }

                        _groupBuyRecordFlow.emit(setGroup(endFilterBuyRecord))
                        _filterBuyRecordFlow.emit(endFilterBuyRecord)

                        val totalProfit = sumProfit(
                            action = DrAction.Buy,
                            buyList = endFilterBuyRecord, sellList = null
                        )

                        totalExpectProfit.emit(totalProfit)

                    }
                }

            }

            DrAction.Sell -> {

                viewModelScope.launch {
                    if (startDate == "" && endDate == "") {

                        _filterSellRecordFlow.emit(_sellRecordFlow.value)

                        val totalProfit = sumProfit(
                            action = DrAction.Sell,
                            buyList = null, sellList = _sellRecordFlow.value
                        )

                        totalSellProfit.emit(totalProfit)

                    } else {
                        val startFilterSellRecord =
                            sellRecordFlow.value.filter { it.date!! >= startDate }

                        val endFilterSellRecord =
                            startFilterSellRecord.filter { it.date!! <= endDate }


                        _filterSellRecordFlow.emit(endFilterSellRecord)

                        val totalProfit = sumProfit(
                            action = DrAction.Sell,
                            buyList = null, sellList = endFilterSellRecord
                        )

                        totalSellProfit.emit(totalProfit)
                    }
                }
            }
        }

    }

    //특정값만 인출

    val totalSellProfit = MutableStateFlow("")

    val totalExpectProfit = MutableStateFlow("")


    fun sumProfit(
        action: DrAction = DrAction.Buy,
        buyList: List<DrBuyRecord>?,
        sellList: List<DrSellRecord>?
    ): String {

        when (action) {
            DrAction.Buy -> {
                val result = buyList?.filter { it.profit != "" }?.map { BigDecimal(it.profit) }

                if (result.isNullOrEmpty()) {
                    return ""
                } else {
                    if (result.size > 1) {
                        return result.reduce { first, end ->
                            first!! + end!!
                        }!!.toBigDecimalWon()
                    } else {
                        return "${result.first()!!.toBigDecimalWon()}"
                    }
                }

            }

            DrAction.Sell -> {
                val result = sellList?.map { BigDecimal(it.exchangeMoney) }

                Log.d(TAG, "달러 ${result}")

                if (result.isNullOrEmpty()) {
                    return ""
                } else {
                    if (result.size > 1) {
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

    var haveMoneyDollar = MutableStateFlow("")

    val sellRateFlow = MutableStateFlow("")

    val sellDollarFlow = MutableStateFlow("")

    val getPercentFlow = MutableStateFlow(0f)

    val sellRecordActionFlow = MutableStateFlow(false)


    fun buyDollarAdd(groupName: String) {
        viewModelScope.launch {
            exchangeMoney.emit("${lastValue()}")
            Log.d(TAG, "매수 달러 ${exchangeMoney.value}")
            investRepository
                .addRecord(
                    DrBuyRecord(
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
                        buyDrCategoryName = groupName,
                        buyDrMemo = ""
                    )
                )

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
            val groupRecord = _groupBuyRecordFlow.value
            val filterRecord = _filterBuyRecordFlow.value
            val buyItems = buyRecordState.toMutableList().apply {
                remove(drBuyrecord)
            }.toList()

            val groupBuyItems = groupRecord.toMutableMap().apply {
                remove(drBuyrecord.buyDrCategoryName)
            }.toMap()

            val filterBuyItems = filterRecord.toMutableList().apply {
                remove(drBuyrecord)
            }.toList()


            _buyRecordFlow.value = buyItems

            _groupBuyRecordFlow.value = groupBuyItems

            _filterBuyRecordFlow.value = filterBuyItems

        }
    }


    //매도 시 컬러색 변경 예상 수익 초기화
    fun updateBuyRecord(drBuyrecord: DrBuyRecord) {
        viewModelScope.launch {
            investRepository.updateRecord(
                DrBuyRecord(
                    drBuyrecord.id,
                    drBuyrecord.date,
                    drBuyrecord.money,
                    rate = drBuyrecord.rate,
                    buyRate = drBuyrecord.buyRate,
                    sellRate = sellRateFlow.value,
                    money = drBuyrecord.money,
                    exchangeMoney = drBuyrecord.exchangeMoney,
                    profit = drBuyrecord.profit,
                    expectProfit = drBuyrecord.profit,
                    sellProfit = sellDollarFlow.value,
                    recordColor = true,
                    buyDrMemo = drBuyrecord.buyDrMemo,
                    buyDrCategoryName = drBuyrecord.buyDrCategoryName
                )
            )
        }
    }


    fun updateRecordGroup(drBuyrecord: DrBuyRecord, groupName: String) {
        viewModelScope.launch {

            val updateData = drBuyrecord.copy(buyDrCategoryName = groupName)

            investRepository.updateRecord(updateData)
        }
    }


    fun sellRecordValue(buyRecord: DrBuyRecord) {
        viewModelScope.launch {
            val buyRecordId = buyRecord.id

            Log.d(TAG, "매도아이디 ${buyRecordId}")
            investRepository
                .addRecord(
                    DrSellRecord(
                        id = buyRecordId,
                        date = sellDateFlow.value,
                        money = haveMoneyDollar.value,
                        rate = sellRateFlow.value,
                        exchangeMoney = sellDollarFlow.value,
                        sellDrMemo = "",
                        sellDrCategoryName = ""
                    )
                )
        }
    }

    fun removeSellRecord(drSellRecord: DrSellRecord) {

        Log.d(TAG, "${drSellRecord}")

        viewModelScope.launch {
            investRepository.deleteRecord(drSellRecord)

            val sellRecordState = _sellRecordFlow.value
            val filterSellRecord = _filterSellRecordFlow.value
            val sellItems = sellRecordState.toMutableList().apply {
                remove(drSellRecord)
            }.toList()

            val filterSellItems = filterSellRecord.toMutableList().apply {
                remove(drSellRecord)
            }.toList()

            _sellRecordFlow.value = sellItems

            _filterSellRecordFlow.value = filterSellItems


        }
    }

    fun sellCalculation() {
        viewModelScope.launch {
            sellDollarFlow.emit(sellValue().toString())
            getPercentFlow.emit(sellPercent())
        }
    }

    fun resetValue() {
        viewModelScope.launch {
            sellRateFlow.emit("")
        }
    }


    // resentRate
    val drResentRateStateFlow = MutableStateFlow<ExchangeRate>(ExchangeRate())


    fun requestRate(exchangeRate: ExchangeRate) {
        viewModelScope.launch {
            drResentRateStateFlow.emit(exchangeRate)
        }
    }

    // 기존 데이터 저장
    fun calculateProfit(exchangeRate: ExchangeRate) {

        val buyRecordProfit = buyRecordFlow.value.map { it.profit }

        Log.d(TAG, "dollarBuyList 불러온 profit 값 : ${buyRecordProfit}")

        _buyRecordFlow.value.forEach { drBuyRecord ->

            //매도 상태
            //수정 필요
            if (drBuyRecord.recordColor == true) {
                val updateDate = drBuyRecord.copy(profit = "")

                viewModelScope.launch {
                    investRepository.updateRecord(updateDate)
                }
            } else {
                if (drBuyRecord.profit == null) {

                    Log.d(TAG, "기존 데이터 profit 추가 실행")

                    val resentRate = exchangeRate.exchangeRates?.usd

                    if (resentRate.isNullOrEmpty()) {
                        Log.d(TAG, "calculateProfit 최신 값 받아오기 실패")

                    } else {
                        val exChangeMoney = drBuyRecord.exchangeMoney

                        val koreaMoney = drBuyRecord.money

                        Log.d(TAG, "값을 받아왔니? ${resentRate}")

                        val profit = (((BigDecimal(exChangeMoney).times(BigDecimal(resentRate)))
                            .setScale(
                                20,
                                RoundingMode.HALF_UP
                            )) - BigDecimal(koreaMoney)).toString()

                        Log.d(TAG, "예상 수익 ${profit}")

                        val updateDate = drBuyRecord.copy(profit = profit)

                        viewModelScope.launch {
                            investRepository.updateRecord(updateDate)
                        }
                    }
                } else {
                    Log.d(TAG, "업데이트 데이터 profit 실행")

                    val resentRate = exchangeRate.exchangeRates?.usd

                    if (resentRate.isNullOrEmpty()) {
                        Log.d(TAG, "calculateProfit 최신 값 받아오기 실패")

                    } else {
                        val exChangeMoney = drBuyRecord.exchangeMoney

                        val koreaMoney = drBuyRecord.money

                        Log.d(TAG, "값을 받아왔니? ${resentRate}")

                        val profit = (((BigDecimal(exChangeMoney).times(BigDecimal(resentRate)))
                            .setScale(
                                20,
                                RoundingMode.HALF_UP
                            )) - BigDecimal(koreaMoney)).toString()

                        Log.d(TAG, "예상 수익 ${profit}")

                        val updateDate = drBuyRecord.copy(profit = profit)

                        viewModelScope.launch {
                            investRepository.updateRecord(updateDate)
                        }
                    }
                }
            }


        }
    }

    fun buyDrMemoUpdate(updateData: DrBuyRecord, result: (Boolean) -> Unit) {

        var success: Boolean = false
        viewModelScope.launch {
            val successValue = investRepository.updateRecord(updateData)

            Log.d(TAG, "업데이트 성공 ${successValue}")
            success = if (successValue > 0) true else false
            result(success)

        }
    }

    fun sellDrMemoUpdate(updateData: DrSellRecord, result: (Boolean) -> Unit) {

        var success: Boolean = false
        viewModelScope.launch {
            val successValue = investRepository.updateRecord(updateData)

            Log.d(TAG, "업데이트 성공 ${successValue}")
            success = if (successValue > 0) true else false
            result(success)

        }
    }

    suspend fun cancelSellRecord(id: UUID): Pair<Boolean, DrSellRecord> {

        val searchBuyRecord = investRepository.getRecordId(id)

        val searchSellRecord = investRepository.getSellRecordId(id)

        Log.d(TAG, "cancelBuyRecord: ${searchBuyRecord}, sellId: ${id}")

        Log.d(TAG, "cancelSellRecord: ${searchSellRecord}, sellId: ${id}")

        if (searchBuyRecord == null && searchSellRecord == null) {
            return Pair(false, DrSellRecord())
        } else {
            val updateData = searchBuyRecord.copy(recordColor = false)

            investRepository.updateRecord(updateData)

            return Pair(true, searchSellRecord)
        }
    }

    fun drCloudLoad(buyRecord: List<DrBuyRecord>, sellRecord: List<DrSellRecord>) {

        viewModelScope.launch {

            investRepository.drBuyAddListRecord(buyRecord)

            investRepository.drSellAddListRecord(sellRecord)
        }

    }

    fun convertFirebaseUuidToKotlinUuid(firebaseUuid: List<Map<String, String>>): UUID {
        val mostSignificantBits = firebaseUuid[0]["mostSignificantBits"]
        val leastSignificantBits = firebaseUuid[0]["leastSignificantBits"]
        return UUID.fromString(
            String.format(
                "%016x-%016x",
                mostSignificantBits,
                leastSignificantBits
            )
        )
    }


    fun expectSellValue(): String {

        val resentUsRate = drResentRateStateFlow.value.exchangeRates?.usd

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


//    private fun lastValue() = (moneyInputFlow.value.toBigDecimal() / rateInputFlow.value.toBigDecimal()).setScale(2)

    private fun lastValue() = BigDecimal(moneyInputFlow.value).divide(
        BigDecimal(rateInputFlow.value),
        20,
        RoundingMode.HALF_UP
    )

    private fun sellValue() = (
            (BigDecimal(haveMoneyDollar.value).times(BigDecimal(sellRateFlow.value)))
                .setScale(20, RoundingMode.HALF_UP)
            ) - BigDecimal(recordInputMoney.value)

    private fun sellPercent(): Float =
        (sellDollarFlow.value.toFloat() / recordInputMoney.value.toFloat()) * 100f

}