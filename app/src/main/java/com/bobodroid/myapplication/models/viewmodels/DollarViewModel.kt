package com.bobodroid.myapplication.models.viewmodels


import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bobodroid.myapplication.MainActivity.Companion.TAG
import com.bobodroid.myapplication.extensions.toBigDecimalWon
import com.bobodroid.myapplication.models.datamodels.roomDb.DrBuyRecord
import com.bobodroid.myapplication.models.datamodels.roomDb.ExchangeRate
import com.bobodroid.myapplication.models.datamodels.repository.InvestRepository
import com.bobodroid.myapplication.models.datamodels.repository.Notice
import com.bobodroid.myapplication.models.datamodels.repository.UserRepository
import com.bobodroid.myapplication.models.datamodels.roomDb.LocalUserData
import com.bobodroid.myapplication.util.AdMob.AdManager
import com.bobodroid.myapplication.util.AdMob.AdUseCase
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

data class RecordViewUiState(
    val localUser: LocalUserData = LocalUserData(),
    val bannerAdVisibleState: Boolean = false
)

@HiltViewModel
class DollarViewModel @Inject constructor(
    private val investRepository: InvestRepository):
    ViewModel() {

    private val _buyRecordFlow = MutableStateFlow<List<DrBuyRecord>>(emptyList())

    private val _groupBuyRecordFlow = MutableStateFlow<Map<String, List<DrBuyRecord>>>(emptyMap())
    val groupBuyRecordFlow = _groupBuyRecordFlow.asStateFlow()

//    val groupList = MutableStateFlow<List<String>>(emptyList())

    private val todayDate = MutableStateFlow("${LocalDate.now()}")

    init {


    }






    //사용자 기록
    val moneyInputFlow = MutableStateFlow("")

    val rateInputFlow = MutableStateFlow("")

    val exchangeMoney = MutableStateFlow("")


    val sellDateFlow = MutableStateFlow("${LocalDate.now()}")


    enum class DrAction {
        Buy, Sell
    }

    // 전체 매도기록 수익 조회

//    fun dateRangeInvoke(
//        startDate: String,
//        endDate: String
//    ) {
//        Log.d(TAG("DollarViewModel", "dateRangeInvoke"), "전체 데이터 : ${_buyRecordFlow.value}")
//
//        viewModelScope.launch {
//            if (startDate == "" && endDate == "") {
//                _groupBuyRecordFlow.emit(_buyRecordFlow.value.groupBy { it.buyDrCategoryName!! })
//
//                val totalProfit = sumProfit(
//                    action = DrAction.Sell,
//                    buyList = null,
//                    sellList = _sellRecordFlow.value
//                )
//
//                totalSellProfit.emit(totalProfit)
//
//            } else {
//                val startFilterSellRecord =
//                    sellRecordFlow.value.filter { it.date!! >= startDate }
//
//                val endFilterSellRecord =
//                    startFilterSellRecord.filter { it.date!! <= endDate }
//
//                val startFilterBuyRecord =
//                    buyRecordFlow.value.filter { it.date!! >= startDate }
//
//                var endFilterBuyRecord =
//                    startFilterBuyRecord.filter { it.date!! <= endDate }
//
//
//                _filterSellRecordFlow.emit(endFilterSellRecord)
//
//                _groupBuyRecordFlow.emit(setGroup(endFilterBuyRecord))
////                _filterBuyRecordFlow.emit(endFilterBuyRecord)
//
//                val totalProfit = sumProfit(
//                    action = DrAction.Sell,
//                    buyList = null, sellList = endFilterSellRecord
//                )
//
//                totalSellProfit.emit(totalProfit)
//            }
//        }
//
//    }

    //특정값만 인출

    val totalSellProfit = MutableStateFlow("")

    val totalExpectProfit = MutableStateFlow("")


//    fun sumProfit(
//        action: DrAction = DrAction.Buy,
//        buyList: List<DrBuyRecord>?,
//        sellList: List<DrSellRecord>?
//    ): String {
//
//        when (action) {
//            DrAction.Buy -> {
//                val result = buyList?.filter { it.profit != "" }?.map { BigDecimal(it.profit) }
//
//                if (result.isNullOrEmpty()) {
//                    return ""
//                } else {
//                    if (result.size > 1) {
//                        return result.reduce { first, end ->
//                            first!! + end!!
//                        }!!.toBigDecimalWon()
//                    } else {
//                        return "${result.first()!!.toBigDecimalWon()}"
//                    }
//                }
//
//            }
//
//            DrAction.Sell -> {
//                val result = sellList?.map { BigDecimal(it.exchangeMoney) }
//
//                Log.d(TAG("DollarViewModel", "sumProfit"), "달러 ${result}")
//
//                if (result.isNullOrEmpty()) {
//                    return ""
//                } else {
//                    if (result.size > 1) {
//                        return result.reduce { first, end ->
//                            first + end
//                        }.toBigDecimalWon()
//
//                    } else {
//                        return "${result.first().toBigDecimalWon()}"
//                    }
//
//                }
//            }
//        }
//    }


    // 리스트 매도 값
    val recordInputMoney = MutableStateFlow("")

    var haveMoneyDollar = MutableStateFlow("")

    val sellRateFlow = MutableStateFlow("")

    val sellDollarFlow = MutableStateFlow("")

    val getPercentFlow = MutableStateFlow(0f)

    val sellRecordActionFlow = MutableStateFlow(false)






    fun removeBuyRecord(drBuyrecord: DrBuyRecord) {
        viewModelScope.launch {

            investRepository.deleteDollarBuyRecord(drBuyrecord)

        }
    }


    //매도 시 컬러색 변경 예상 수익 초기화






//    fun sellCalculation() {
//        viewModelScope.launch {
//            sellDollarFlow.emit(sellValue().toString())
//            getPercentFlow.emit(sellPercent())
//        }
//    }

    fun resetValue() {
        viewModelScope.launch {
            sellRateFlow.emit("")
        }
    }


    // 기존 데이터 저장 -> 새로고침
    fun calculateProfit(exchangeRate: ExchangeRate) {

        val buyRecordProfit = _buyRecordFlow.value.map { it.profit }

        Log.d(TAG("DollarViewModel", "calculateProfit"), "dollarBuyList 불러온 profit 값 : ${buyRecordProfit}")

        _buyRecordFlow.value.forEach { drBuyRecord ->

            if (drBuyRecord.recordColor == true) {
                val updateDate = drBuyRecord.copy(profit = "")

                viewModelScope.launch {
                    investRepository.updateDollarBuyRecord(updateDate)
                }
            } else {
                Log.d(TAG("DollarViewModel", "calculateProfit"), "기존 데이터 profit 추가 실행")

                val resentRate = exchangeRate.usd

                if (resentRate.isNullOrEmpty()) {
                    Log.d(TAG("DollarViewModel", "calculateProfit"), "calculateProfit 최신 값 받아오기 실패")
                } else {
                    val exChangeMoney = drBuyRecord.exchangeMoney

                    val koreaMoney = drBuyRecord.money

                    Log.d(TAG("DollarViewModel", "calculateProfit"), "값을 받아왔니? ${resentRate}")

                    val profit = (((BigDecimal(exChangeMoney).times(BigDecimal(resentRate)))
                        .setScale(
                            20,
                            RoundingMode.HALF_UP
                        )) - BigDecimal(koreaMoney)).toString()

                    Log.d(TAG("DollarViewModel", "calculateProfit"), "예상 수익 ${profit}")

                    val updateDate = drBuyRecord.copy(profit = profit)

                    viewModelScope.launch {
                        investRepository.updateDollarBuyRecord(updateDate)
                    }
                }
                }
            }

    }



//    fun drCloudLoad(buyRecord: List<DrBuyRecord>, sellRecord: List<DrSellRecord>) {
//
//        viewModelScope.launch {
//
//            investRepository.addDollarBuyRecords(buyRecord)
//
//            investRepository.addDollarSellRecords(sellRecord)
//        }
//
//    }






//    private fun lastValue() = (moneyInputFlow.value.toBigDecimal() / rateInputFlow.value.toBigDecimal()).setScale(2)

    private fun lastValue(money: String, rate: String) = BigDecimal(money).divide(
        BigDecimal(rate),
        20,
        RoundingMode.HALF_UP
    )



}



