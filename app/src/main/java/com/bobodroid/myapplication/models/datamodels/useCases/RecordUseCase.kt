package com.bobodroid.myapplication.models.datamodels.useCases

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.bobodroid.myapplication.MainActivity.Companion.TAG
import com.bobodroid.myapplication.models.datamodels.repository.InvestRepository
import com.bobodroid.myapplication.models.datamodels.roomDb.CurrencyType
import com.bobodroid.myapplication.models.datamodels.roomDb.DrBuyRecord
import com.bobodroid.myapplication.models.datamodels.roomDb.ForeignCurrencyRecord
import com.bobodroid.myapplication.models.datamodels.roomDb.YenBuyRecord
import com.bobodroid.myapplication.models.viewmodels.CurrencyRecordState
import com.bobodroid.myapplication.models.viewmodels.ForeignCurrencyRecordList
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.UUID
import javax.inject.Inject

class RecordUseCase @Inject constructor(
    private val investRepository: InvestRepository
) {
    fun getRecord(): Flow<ForeignCurrencyRecordList> = combine(
        investRepository.getAllDollarBuyRecords(),
        investRepository.getAllYenBuyRecords()
    ) { dollarRecords, yenRecords ->
        ForeignCurrencyRecordList(
            dollarState = CurrencyRecordState(
                records = dollarRecords,
                groupedRecords = setGroup(dollarRecords) { it.categoryName},
                groups = dollarRecords.map { it.categoryName ?: "미지정" }.distinct()
            ),
            yenState = CurrencyRecordState(
                records = yenRecords,
                groupedRecords = setGroup(yenRecords) { it.categoryName },
                groups = yenRecords.map { it.categoryName ?: "미지정" }.distinct()
            )
        )
    }

    suspend fun  addCurrencyRecord(request: CurrencyRecordRequest) {
        val exchangeMoney = calculateExchangeMoney(request.money, request.inputRate)
        val expectedProfit = calculateExpectedProfit(exchangeMoney, request.money, request.latestRate)

        when (request.type) {
            CurrencyType.USD -> {
                investRepository.addDollarBuyRecord(
                    DrBuyRecord(
                        date = request.date,
                        money = request.money,
                        rate = request.inputRate,
                        buyRate = request.inputRate,
                        exchangeMoney = exchangeMoney,
                        profit = expectedProfit,
                        expectProfit = expectedProfit,
                        categoryName = request.groupName,
                        memo = "",
                        // 기본값 설정
                        sellRate = "",
                        sellProfit = "",
                        recordColor = false
                    )
                )
            }
            CurrencyType.JPY -> {
                investRepository.addYenBuyRecord(
                    YenBuyRecord(
                        date = request.date,
                        money = request.money,
                        rate = request.inputRate,
                        buyRate = request.inputRate,
                        exchangeMoney = exchangeMoney,
                        profit = expectedProfit,
                        expectProfit = expectedProfit,
                        categoryName = request.groupName,
                        memo = "",
                        // 기본값 설정
                        sellRate = "",
                        sellProfit = "",
                        recordColor = false
                    )
                )
            }
        }

    }

    suspend fun cancelSellRecord(id: UUID, currencyType: CurrencyType): Boolean {
        val searchBuyRecord = when(currencyType) {
            CurrencyType.USD -> investRepository.getDollarBuyRecordById(id)
            CurrencyType.JPY -> investRepository.getYenBuyRecordById(id)
        }

        val updateData = when(searchBuyRecord) {
            is DrBuyRecord -> searchBuyRecord.copy(recordColor = false)
            is YenBuyRecord -> searchBuyRecord.copy(recordColor = false)
            else -> return false
        }

        when(currencyType) {
            CurrencyType.USD -> {
                val dollarRecord = updateData as DrBuyRecord
                investRepository.updateDollarBuyRecord(dollarRecord)
            }
            CurrencyType.JPY -> {
                val yenRecord = updateData as YenBuyRecord
                investRepository.updateYenBuyRecord(yenRecord)
            }
        }

        return true
    }

   private fun calculateExpectedProfit(exchangeMoney: String, inputMoney: String, latestRate: String): String {


        val profit = ((BigDecimal(exchangeMoney).times(BigDecimal(latestRate)))
            .setScale(20, RoundingMode.HALF_UP)
                ).minus(BigDecimal(inputMoney))
        Log.d(TAG("DollarViewModel", "expectSellValue"), "개별 profit 결과 값 ${profit}")

        return profit.toString()
    }


    private fun calculateExchangeMoney(money: String, rate: String) = BigDecimal(money).divide(
        BigDecimal(rate),
        20,
        RoundingMode.HALF_UP
    ).toString()


    private fun <T> setGroup(recordList: List<T>, categorySelector: (T) -> String?): Map<String, List<T>> {
        return recordList.sortedBy {
            when(it) {
                is DrBuyRecord -> it.date
                is YenBuyRecord -> it.date
                else -> null
            }
        }.groupBy {
            categorySelector(it) ?: "미지정"
        }
    }



}

data class CurrencyRecordRequest(
    val latestRate: String,
    val money: String,
    val inputRate: String,
    val groupName: String,
    val date: String,
    val type: CurrencyType  // USD, JPY 등
)