package com.bobodroid.myapplication.models.viewmodels


import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bobodroid.myapplication.MainActivity.Companion.TAG
import com.bobodroid.myapplication.models.datamodels.roomDb.ExchangeRate
import com.bobodroid.myapplication.models.datamodels.repository.InvestRepository
import com.bobodroid.myapplication.models.datamodels.roomDb.WonBuyRecord
import com.bobodroid.myapplication.models.datamodels.roomDb.WonSellRecord
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
class WonViewModel @Inject constructor(private val investRepository: InvestRepository): ViewModel() {



    private val _buyRecordFlow = MutableStateFlow<List<WonBuyRecord>>(emptyList())
    val buyRecordFlow = _buyRecordFlow.asStateFlow()

    private val _sellRecordFlow = MutableStateFlow<List<WonSellRecord>>(emptyList())
    val sellRecordFlow = _sellRecordFlow.asStateFlow()

    private val _filterBuyRecordFlow = MutableStateFlow<List<WonBuyRecord>>(emptyList())
    val filterBuyRecordFlow = _filterBuyRecordFlow.asStateFlow()

    private val _filterSellRecordFlow = MutableStateFlow<List<WonSellRecord>>(emptyList())
    val filterSellRecordFlow = _filterSellRecordFlow.asStateFlow()

    private val _groupBuyRecordFlow = MutableStateFlow<Map<String, List<WonBuyRecord>>>(emptyMap())
    val groupBuyRecordFlow = _groupBuyRecordFlow.asStateFlow()

    init{
        viewModelScope.launch(Dispatchers.IO) {

            val buyRecordFlow = investRepository.getAllWonBuyRecords().distinctUntilChanged()
            val sellRecordFlow = investRepository.getAllWonSellRecords().distinctUntilChanged()

            val combinedFlow = buyRecordFlow.combine(sellRecordFlow) { buyRecord, sellRecord ->
                combineBuyAndSell(sellRecord, buyRecord)

                if (buyRecord.isNullOrEmpty()) {
                    Log.d(TAG("WonViewModel", "init"), "Won Empty Buy list")
                    _buyRecordFlow.value = emptyList()
                    _filterBuyRecordFlow.value = emptyList()
                    _groupBuyRecordFlow.value = setGroup(emptyList())
                } else {

                    groupList.emit(buyRecord.map { it.buyWonCategoryName!! }.distinct())

                    _buyRecordFlow.value = buyRecord
                    _filterBuyRecordFlow.value = buyRecord
                    _groupBuyRecordFlow.value = setGroup(buyRecord)
                }


                if (sellRecord.isNullOrEmpty()) {
                    Log.d(TAG("WonViewModel", "init"), "Won Empty Sell list")
                    _sellRecordFlow.value = emptyList()
                    _filterSellRecordFlow.value = emptyList()
                } else {
                    _sellRecordFlow.value = sellRecord
                    _filterSellRecordFlow.value = sellRecord
                }

            }

            combinedFlow.collect {
                Log.d(TAG("WonViewModel", "init"), "init 완료")
            }

        }
    }

    val groupList = MutableStateFlow<List<String>>(emptyList())

    fun setGroup(recordList: List<WonBuyRecord>): Map<String, List<WonBuyRecord>> {

        val makeGroup = recordList.sortedBy { it.date }.groupBy { it.buyWonCategoryName!! }

        return makeGroup
    }

    fun groupAdd(newGroupName: String) {

        val updateGroupList = groupList.value.toMutableList().apply {
            add(newGroupName)
        }.toList()

        viewModelScope.launch {
            groupList.emit(updateGroupList)
        }

    }

    fun updateRecordGroup(wonBuyrecord: WonBuyRecord, groupName: String) {
        viewModelScope.launch {

            val updateData = wonBuyrecord.copy(buyWonCategoryName = groupName)

            investRepository.updateWonBuyRecord(updateData)
        }
    }

    fun combineBuyAndSell(sellRecord: List<WonSellRecord>, buyRecord: List<WonBuyRecord>) {


        if (!sellRecord.isNullOrEmpty() && !buyRecord.isNullOrEmpty()) {
            buyRecord.forEach { buy ->
                if (buy.buyWonCategoryName.isNullOrEmpty()) buy.buyWonCategoryName =
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
            Log.d(TAG("WonViewModel", "combineBuyAndSell"), "여기가 실행됨")
        }

    }



    //사용자 기록
    val moneyInputFlow = MutableStateFlow("")

    val rateInputFlow = MutableStateFlow("")

    val exchangeMoney = MutableStateFlow("")

    val selectedBoxId = MutableStateFlow(1)

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
        startDate: String,
        endDate: String) {
        viewModelScope.launch {
            if (startDate == "" && endDate == "") {

                _filterBuyRecordFlow.emit(_buyRecordFlow.value)
                _filterSellRecordFlow.emit(_sellRecordFlow.value)
                _groupBuyRecordFlow.emit(_buyRecordFlow.value.groupBy { it.buyWonCategoryName!! })

//                val totalProfit = sumProfit(
//                    action = YenViewModel.YenAction.Sell,
//                    buyList = null, sellList = _sellRecordFlow.value
//                )
//
//                totalSellProfit.emit(totalProfit)

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

//                val totalProfit = sumProfit(
//                    action = YenViewModel.YenAction.Sell,
//                    buyList = null, sellList = endFilterSellRecord
//                )
//
//                totalSellProfit.emit(totalProfit)
            }
        }

    }


    // 리스트 매도 값

    val recordInputMoney = MutableStateFlow(0)

    var haveMoney = MutableStateFlow("")

    val sellRateFlow = MutableStateFlow("")

    val sellWonFlow = MutableStateFlow("")

    val getPercentFlow = MutableStateFlow(0f)

    val moneyType = MutableStateFlow(1)

    private val sellRecordActionFlow = MutableStateFlow(false)


    fun buyAddRecord(groupName: String) {
        viewModelScope.launch {
            when(moneyCgBtnSelected.value) {
                1 -> {moneyType.emit(1)}
                2 -> {moneyType.emit(2)}
            }
            when(moneyCgBtnSelected.value) {
                1-> {
                    val dollarCg = dollarLastValue(moneyInputFlow.value, rateInputFlow.value)
                    Log.d(TAG("WonViewModel", "buyAddRecord"),"값 ${dollarCg}")
                    exchangeMoney.emit("${dollarCg}")
                }
                2-> {
                    val yenCg = yenLastValue(moneyInputFlow.value, rateInputFlow.value)
                    exchangeMoney.emit("${yenCg}")
                }
                else -> {null}}
            investRepository
                .addWonBuyRecord(
                    WonBuyRecord(
                        date = dateFlow.value,
                        money = moneyInputFlow.value,
                        rate = rateInputFlow.value,
                        buyRate = rateInputFlow.value,
                        sellRate = "",
                        exchangeMoney = exchangeMoney.value,
                        recordColor = sellRecordActionFlow.value,
                        moneyType = moneyType.value,
                        profit = expectSellValue(),
                        sellProfit = "",
                        expectProfit = expectSellValue(),
                        buyWonCategoryName = groupName,
                        buyWonMemo = ""
                    )
                )

            // 데이터 값 초기화
            buyRecordFlow.collectLatest {
                moneyInputFlow.emit("")
                rateInputFlow.emit("")
            }
        }
    }


    fun insertBuyRecord(existingWonBuyRecord: WonBuyRecord,
                        insertDate: String,
                        insertMoney: String,
                        insertRate: String) {

        val moneyType = existingWonBuyRecord.moneyType

        when(moneyType) {
            1 -> {
                viewModelScope.launch {

                    val insertDate = existingWonBuyRecord.copy(
                        date = insertDate,
                        money = insertMoney,
                        rate = insertRate,
                        buyRate = insertRate,
                        profit = "0",
                        expectProfit = "0",
                        exchangeMoney = dollarLastValue(insertMoney, insertRate).toString())

                    investRepository.updateWonBuyRecord(insertDate)
                }
            }
            2 -> {
                viewModelScope.launch {

                    val insertDate = existingWonBuyRecord.copy(
                        date = insertDate,
                        money = insertMoney,
                        rate = insertRate,
                        buyRate = insertRate,
                        profit = "0",
                        expectProfit = "0",
                        exchangeMoney = yenLastValue(insertMoney, insertRate).toString())

                    investRepository.updateWonBuyRecord(insertDate)
                }
            }
        }



    }




    fun sellCalculation() {
        viewModelScope.launch {
            when(moneyType.value) {
                1 -> {
                    sellWonFlow.emit(dollarSellValue().toString())
                    getPercentFlow.emit(sellPercent())}
                2 -> {
                    sellWonFlow.emit(yenSellValue().toString())
                    getPercentFlow.emit(sellPercent())
                }

            }


        }
    }

    fun removeBuyRecord(wonBuyRecord: WonBuyRecord) {
        viewModelScope.launch {
            investRepository.deleteWonBuyRecord(wonBuyRecord)
        }
    }


    fun removeSellRecord(wonSellRecord: WonSellRecord) {
        viewModelScope.launch {
            investRepository.deleteWonSellRecord(wonSellRecord)

            val sellRecordState = _sellRecordFlow.value

            val filterSellRecord = _filterSellRecordFlow.value

            val sellItems = sellRecordState.toMutableList().apply{
                remove(wonSellRecord)
            }.toList()

            val filterSellItems = filterSellRecord.toMutableList().apply{
                remove(wonSellRecord)
            }.toList()

            _sellRecordFlow.value = sellItems

            _filterSellRecordFlow.value = filterSellItems
        }
    }



    fun updateBuyRecord(wonBuyRecord: WonBuyRecord) {
        viewModelScope.launch {
            investRepository.updateWonBuyRecord(
                WonBuyRecord(
                    id = wonBuyRecord.id,
                    date = wonBuyRecord.date,
                    sellDate = sellDateFlow.value,
                    rate = wonBuyRecord.rate,
                    moneyType = wonBuyRecord.moneyType,
                    buyRate = wonBuyRecord.buyRate,
                    sellRate = sellRateFlow.value,
                    money = wonBuyRecord.money,
                    exchangeMoney = wonBuyRecord.exchangeMoney,
                    profit = wonBuyRecord.profit,
                    expectProfit = wonBuyRecord.profit,
                    sellProfit = sellWonFlow.value,
                    recordColor = true,
                    buyWonMemo = wonBuyRecord.buyWonMemo,
                    buyWonCategoryName = wonBuyRecord.buyWonCategoryName
                )
            )
        }
    }


    fun sellRecordValue(buyRecord: WonBuyRecord) {

        val buyRecordId = buyRecord.id

        viewModelScope.launch {
            investRepository
                .addWonSellRecord(
                    WonSellRecord(
                        id = buyRecordId,
                        date = sellDateFlow.value,
                        money = haveMoney.value.toString(),
                        rate = sellRateFlow.value,
                        exchangeMoney = sellWonFlow.value,
                        moneyType = moneyType.value,
                        sellWonMemo = "",
                        sellWonCategoryName = "")
                )
        }
    }



    fun resetValue() {
        viewModelScope.launch {
            sellRateFlow.emit("")
        }
    }

    fun calculateProfit(exchangeRate: ExchangeRate) {


        val buyRecordProfit = buyRecordFlow.value.map { it.profit }

        Log.d(TAG("WonViewModel", "calculateProfit"), "wonBuyList 불러온 profit 값 : ${buyRecordProfit}")

        _buyRecordFlow.value.forEach { wonBuyRecord->

            if(wonBuyRecord.recordColor == true) {
                val updateDate = wonBuyRecord.copy(profit = "")

                viewModelScope.launch {
                    investRepository.updateWonBuyRecord(updateDate)
                }
            } else {
                if(wonBuyRecord.profit == null) {
                    // 기존 데이터가 비어있을 때
                    Log.d(TAG("WonViewModel", "calculateProfit"), "프로핏 데이터가 없는경우 profit 실행")

                    val resentRateUs = exchangeRate.usd
                    val resentRateYen = exchangeRate.jpy

                    if(resentRateUs.isNullOrEmpty()) {
                        Log.d(TAG("WonViewModel", "calculateProfit"), "calculateProfit 최신 값 받아오기 실패")

                    } else {

                        //원화
                        val exChangeMoney = wonBuyRecord.exchangeMoney

                        //외화
                        val foreignCurrencyMoney = wonBuyRecord.money

                        Log.d(TAG("WonViewModel", "calculateProfit"), "값을 받아왔니? us: ${resentRateUs} jpy:${resentRateYen}")

                        Log.d(TAG("WonViewModel", "calculateProfit"), "계산해보자 원화: ${exChangeMoney} 외화:${foreignCurrencyMoney}")

                        val profit = when(wonBuyRecord.moneyType) {
                            1-> {  foreignCurrencyMoney!!.toBigDecimal() -(exChangeMoney!!.toBigDecimal() / (resentRateUs.toBigDecimal()))   }

                            2-> {  foreignCurrencyMoney!!.toBigDecimal() - (exChangeMoney!!.toBigDecimal() / (resentRateYen!!.toBigDecimal())) }

                            else -> { "" }
                        }


                        Log.d(TAG("WonViewModel", "calculateProfit"), "예상 수익 ${profit}")

                        val updateDate = wonBuyRecord.copy(profit = profit.toString())

                        viewModelScope.launch {
                            investRepository.updateWonBuyRecord(updateDate)
                        }
                    }
                } else {

                    val resentRateUs = exchangeRate.usd
                    val resentRateYen = exchangeRate.jpy

                    if(resentRateUs.isNullOrEmpty()) {
                        Log.d(TAG("WonViewModel", "calculateProfit"), "calculateProfit 최신 값 받아오기 실패")

                    } else {
                        val exChangeMoney = wonBuyRecord.exchangeMoney

                        val foreignCurrencyMoney = wonBuyRecord.money

                        Log.d(TAG("WonViewModel", "calculateProfit"), "값을 받아왔니? us: ${resentRateUs} jpy:${resentRateYen}")

                        Log.d(TAG("WonViewModel", "calculateProfit"), "계산해보자 원화: ${exChangeMoney} 외화:${foreignCurrencyMoney}")

                        val profit = when(wonBuyRecord.moneyType) {
                            1-> {  foreignCurrencyMoney!!.toBigDecimal() -(exChangeMoney!!.toBigDecimal() / (resentRateUs.toBigDecimal()))   }

                            2-> {  foreignCurrencyMoney!!.toBigDecimal() - (exChangeMoney!!.toBigDecimal() / (resentRateYen!!.toBigDecimal())) }

                            else -> { "" }
                        }

                        Log.d(TAG("WonViewModel", "calculateProfit"), "예상 수익 ${profit}")

                        val updateDate = wonBuyRecord.copy(profit = profit.toString())

                        viewModelScope.launch {
                            investRepository.updateWonBuyRecord(updateDate)
                        }
                    }
                }
            }



        }
    }



    val wonResentRateStateFlow = MutableStateFlow<ExchangeRate>(ExchangeRate())


    fun requestRate(exchangeRate: ExchangeRate) {
        viewModelScope.launch {
            wonResentRateStateFlow.emit(exchangeRate)
        }
    }

    fun buyWonMemoUpdate(updateData: WonBuyRecord, result:(Boolean) -> Unit){

        var success: Boolean = false
        viewModelScope.launch {
            val successValue = investRepository.updateWonBuyRecord(updateData)

            Log.d(TAG("WonViewModel", "buyWonMemoUpdate"), "업데이트 성공 ${successValue}")
            success = if(successValue > 0) true else false
            result(success)

        }
    }

    fun sellWonMemoUpdate(updateData: WonSellRecord, result:(Boolean) -> Unit){

        var success: Boolean = false
        viewModelScope.launch {
            val successValue = investRepository.updateWonSellRecord(updateData)

            Log.d(TAG("WonViewModel", "sellWonMemoUpdate"), "업데이트 성공 ${successValue}")
            success = if(successValue > 0) true else false
            result(success)

        }
    }

    suspend fun cancelSellRecord(id: UUID): Pair<Boolean, WonSellRecord> {

        val searchBuyRecord = investRepository.getWonBuyRecordById(id)

        val searchSellRecord = investRepository.getWonSellRecordById(id)

        Log.d(TAG("WonViewModel", "cancelSellRecord"), "cancelBuyRecord: ${searchBuyRecord}, sellId: ${id}")

        Log.d(TAG("WonViewModel", "cancelSellRecord"), "cancelSellRecord: ${searchSellRecord}, sellId: ${id}")

        if (searchBuyRecord == null && searchSellRecord == null) {
            return Pair(false, WonSellRecord())
        } else {
            val updateData = searchBuyRecord.copy(recordColor = false)

            investRepository.updateWonBuyRecord(updateData)

            return Pair(true, WonSellRecord())
        }
    }

    fun wonCloudLoad(buyRecord: List<WonBuyRecord>, sellRecord: List<WonSellRecord>) {

        viewModelScope.launch {
            viewModelScope.launch {

                investRepository.addWonBuyRecords(buyRecord)

                investRepository.addWonSellRecords(sellRecord)

            }
        }
    }




    fun expectSellValue(): String {

        Log.d(TAG("WonViewModel","expectSellValue"), "머니 타입 ${moneyType.value}")

        Log.d(TAG("WonViewModel","expectSellValue"), "원화 us: ${wonResentRateStateFlow.value.usd}")

        Log.d(TAG("WonViewModel","expectSellValue"), "원화 jpy: ${wonResentRateStateFlow.value.jpy}")

        val resentUsRate = when(moneyType.value) {
            1-> {wonResentRateStateFlow.value.usd}
            2-> {wonResentRateStateFlow.value.jpy}
            else -> { null }
        }

        Log.d(
            TAG("WonViewModel","expectSellValue"),
            "개별 profit 실행 exchangeMoney:${exchangeMoney.value} 최신환율: ${resentUsRate} 원화: ${moneyInputFlow.value}"
        )

        val profit = (BigDecimal(moneyInputFlow.value) - (BigDecimal(exchangeMoney.value) / BigDecimal(resentUsRate)) )
        Log.d(TAG("WonViewModel","expectSellValue"), "개별 profit 결과 값 ${profit}")

        return profit.toString()
    }



    private fun dollarLastValue(money: String, rate: String) = (BigDecimal(money).times(BigDecimal(rate))).setScale(20, RoundingMode.HALF_UP)

    private fun yenLastValue(money: String, rate: String) = (BigDecimal(money).times(BigDecimal(rate))).setScale(20, RoundingMode.HALF_UP).divide(
        BigDecimal("100")
    )


    private fun dollarSellValue() = (BigDecimal(haveMoney.value).divide(BigDecimal(sellRateFlow.value), 28, RoundingMode.HALF_UP)).minus(BigDecimal(recordInputMoney.value))

    private fun yenSellValue() =((BigDecimal(haveMoney.value).divide(BigDecimal(sellRateFlow.value),28, RoundingMode.HALF_UP)).times(
        BigDecimal("100"))).minus(BigDecimal(recordInputMoney.value))


    private fun sellPercent(): Float = (sellWonFlow.value.toFloat() / recordInputMoney.value.toFloat()) * 100f

}


