package com.bobodroid.myapplication.models.viewmodels


import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bobodroid.myapplication.MainActivity.Companion.TAG
import com.bobodroid.myapplication.extensions.toBigDecimalWon
import com.bobodroid.myapplication.models.datamodels.DrBuyRecord
import com.bobodroid.myapplication.models.datamodels.DrSellRecord
import com.bobodroid.myapplication.models.datamodels.ExchangeRate
import com.bobodroid.myapplication.models.datamodels.InvestRepository
import com.bobodroid.myapplication.models.datamodels.LocalUserData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.math.BigInteger
import java.math.RoundingMode
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.*
import javax.inject.Inject


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
                    Log.d(TAG, "Empty Buy list")
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
                        Log.d(TAG, "Empty Sell list")
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




    enum class DrAction {
        Buy, Sell
    }

    fun dateRangeInvoke(
        action: DrAction = DrAction.Buy,
        startDate: String,
        endDate: String) {

        Log.d(TAG, "전체 데이터 : ${buyRecordFlow.value}")

        when(action) {
            DrAction.Buy -> {

                viewModelScope.launch {
                    if(startDate == "" && endDate == "") {

                        _filterBuyRecordFlow.emit(_buyRecordFlow.value)

                        val totalProfit = sumProfit(
                            action = DrAction.Buy,
                            buyList = _buyRecordFlow.value, sellList = null)

                        totalExpectProfit.emit(totalProfit)

                    } else {
                        val startFilterBuyRecord= buyRecordFlow.value.filter { it.date >= startDate}

                        var endFilterBuyRecord = startFilterBuyRecord.filter { it.date <= endDate}

                            _filterBuyRecordFlow.emit(endFilterBuyRecord)

                       val totalProfit = sumProfit(
                           action = DrAction.Buy,
                           buyList = endFilterBuyRecord, sellList = null)

                        totalExpectProfit.emit(totalProfit)

                    }
                }

            }

            DrAction.Sell -> {

                viewModelScope.launch {
                    if(startDate == "" && endDate == "") {

                        _filterSellRecordFlow.emit(_sellRecordFlow.value)

                        val totalProfit = sumProfit(
                            action = DrAction.Sell,
                            buyList = null, sellList = _sellRecordFlow.value)

                        totalSellProfit.emit(totalProfit)

                    } else {
                        val startFilterSellRecord= sellRecordFlow.value.filter { it.date >= startDate}

                        val endFilterSellRecord = startFilterSellRecord.filter { it.date <= endDate}


                        _filterSellRecordFlow.emit(endFilterSellRecord)

                        val totalProfit = sumProfit(
                            action = DrAction.Sell,
                            buyList = null, sellList = endFilterSellRecord)

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
    ) : String {

        when(action) {
            DrAction.Buy ->  {
                val result = buyList?.map { BigDecimal(it.profit) }

                if(result.isNullOrEmpty()) {
                    return ""
                } else {
                    return result.reduce {first, end ->
                        first + end }.toBigDecimalWon()
                    }
                }

            DrAction.Sell -> {
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

    var haveMoneyDollar = MutableStateFlow("")

    val sellRateFlow = MutableStateFlow("")

    val sellDollarFlow = MutableStateFlow("")

    val getPercentFlow = MutableStateFlow(0f)

    val sellRecordActionFlow = MutableStateFlow(false)



    fun buyDollarAdd() {
        viewModelScope.launch {
            exchangeMoney.emit("${lastValue()}")
            Log.d(TAG, "매수 달러 ${exchangeMoney.value}")
            investRepository
                .addRecord(DrBuyRecord(
                    date = dateFlow.value,
                    money = moneyInputFlow.value,
                    rate = rateInputFlow.value,
                    exchangeMoney = "${exchangeMoney.value}",
                    recordColor = sellRecordActionFlow.value,
                    profit = expectSellValue(),
                    buyDrCategoryName = "",
                    buyDrMemo = ""
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
            val filterRecord = _filterBuyRecordFlow.value
            val buyItems = buyRecordState.toMutableList().apply{
                remove(drBuyrecord)
            }.toList()

            val filterBuyItems = filterRecord.toMutableList().apply{
                remove(drBuyrecord)
            }.toList()


            _buyRecordFlow.value = buyItems

            _filterBuyRecordFlow.value = filterBuyItems

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
                    drBuyrecord.profit,
                    drBuyrecord.exchangeMoney,
                    true,
                    "",
                    ""
                    ))
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
        viewModelScope.launch {
            investRepository.deleteRecord(drSellRecord)

            val sellRecordState = _sellRecordFlow.value
            val filterSellRecord = _filterSellRecordFlow.value
            val sellItems = sellRecordState.toMutableList().apply{
                remove(drSellRecord)
            }.toList()

            val filterSellItems = filterSellRecord.toMutableList().apply{
                remove(drSellRecord)
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

            _buyRecordFlow.value.forEach {drBuyRecord->

                if(drBuyRecord.profit == null) {

                    Log.d(TAG, "기존 데이터 profit 추가 실행")

                    val resentRate = exchangeRate.exchangeRates?.usd

                    if(resentRate.isNullOrEmpty()) {
                        Log.d(TAG, "calculateProfit 최신 값 받아오기 실패")

                    } else {
                        val exChangeMoney = drBuyRecord.exchangeMoney

                        val koreaMoney = drBuyRecord.money

                        Log.d(TAG, "값을 받아왔니? ${resentRate}")

                        val profit = (((BigDecimal(exChangeMoney).times(BigDecimal(resentRate)))
                            .setScale(20, RoundingMode.HALF_UP)) - BigDecimal(koreaMoney)).toString()

                        Log.d(TAG, "예상 수익 ${profit}")

                        val updateDate = drBuyRecord.copy(profit = profit)

                        viewModelScope.launch {
                            investRepository.updateRecord(updateDate)
                        }
                    }
                } else {
                    Log.d(TAG, "업데이트 데이터 profit 실행")

                    val resentRate = exchangeRate.exchangeRates?.usd

                    if(resentRate.isNullOrEmpty()) {
                        Log.d(TAG, "calculateProfit 최신 값 받아오기 실패")

                    } else {
                        val exChangeMoney = drBuyRecord.exchangeMoney

                        val koreaMoney = drBuyRecord.money

                        Log.d(TAG, "값을 받아왔니? ${resentRate}")

                        val profit = (((BigDecimal(exChangeMoney).times(BigDecimal(resentRate)))
                            .setScale(20, RoundingMode.HALF_UP)) - BigDecimal(koreaMoney)).toString()

                        Log.d(TAG, "예상 수익 ${profit}")

                        val updateDate = drBuyRecord.copy(profit = profit)

                        viewModelScope.launch {
                            investRepository.updateRecord(updateDate)
                        }
                    }


                }

            }
    }

    fun buyDrMemoUpdate(updateData: DrBuyRecord, result:(Boolean) -> Unit){

        var success: Boolean = false
        viewModelScope.launch {
           val successValue = investRepository.updateRecord(updateData)

            Log.d(TAG, "업데이트 성공 ${successValue}")
            success = if(successValue > 0) true else false
            result(success)

        }
    }

    fun sellDrMemoUpdate(updateData: DrSellRecord, result:(Boolean) -> Unit){

        var success: Boolean = false
        viewModelScope.launch {
            val successValue = investRepository.updateRecord(updateData)

            Log.d(TAG, "업데이트 성공 ${successValue}")
            success = if(successValue > 0) true else false
            result(success)

        }
    }

   suspend fun cancelSellRecord(id: UUID): Boolean {

       val searchBuyRecord = investRepository.getRecordId(id)

       Log.d(TAG, "cancelSellRecord: ${searchBuyRecord}, sellId: ${id}")

       if(searchBuyRecord == null) {
           return false
       } else {
           val updateData = searchBuyRecord.copy(recordColor = false)

           investRepository.updateRecord(updateData)

           return true}
    }




    fun expectSellValue(): String {

        val resentUsRate = drResentRateStateFlow.value.exchangeRates?.usd

        Log.d(TAG, "개별 profit 실행 exchangeMoney:${exchangeMoney.value} 최신환율: ${resentUsRate} 원화: ${moneyInputFlow.value}")

        val profit = ((BigDecimal(exchangeMoney.value).times(BigDecimal(resentUsRate)))
            .setScale(20, RoundingMode.HALF_UP)
                ).minus(BigDecimal(moneyInputFlow.value))
        Log.d(TAG, "개별 profit 결과 값 ${profit}")

        return profit.toString()
    }



//    private fun lastValue() = (moneyInputFlow.value.toBigDecimal() / rateInputFlow.value.toBigDecimal()).setScale(2)

    private fun lastValue() = BigDecimal(moneyInputFlow.value).divide(BigDecimal(rateInputFlow.value), 20, RoundingMode.HALF_UP)
    private fun sellValue() = (
            (BigDecimal(haveMoneyDollar.value).times(BigDecimal(sellRateFlow.value)))
                .setScale(20, RoundingMode.HALF_UP)
            ) - BigDecimal(recordInputMoney.value)

    private fun sellPercent(): Float = (sellDollarFlow.value.toFloat() / recordInputMoney.value.toFloat()) * 100f

}



