package com.bobodroid.myapplication.models.datamodels.useCases

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.bobodroid.myapplication.MainActivity.Companion.TAG
import com.bobodroid.myapplication.extensions.toBigDecimalWon
import com.bobodroid.myapplication.models.datamodels.repository.InvestRepository
import com.bobodroid.myapplication.models.datamodels.roomDb.CurrencyType
import com.bobodroid.myapplication.models.datamodels.roomDb.DrBuyRecord
import com.bobodroid.myapplication.models.datamodels.roomDb.ForeignCurrencyRecord
import com.bobodroid.myapplication.models.datamodels.roomDb.YenBuyRecord
import com.bobodroid.myapplication.models.viewmodels.CurrencyRecordState
import com.bobodroid.myapplication.models.viewmodels.ForeignCurrencyRecordList
import com.bobodroid.myapplication.models.viewmodels.RecordListUiState
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.UUID
import javax.inject.Inject

class RecordUseCase @Inject constructor(
    private val investRepository: InvestRepository
) {
    fun getRecord(): Flow<ForeignCurrencyRecordList> = combine(
        investRepository.getAllDollarBuyRecords().distinctUntilChanged(),
        investRepository.getAllYenBuyRecords().distinctUntilChanged()
    ) { dollarRecords, yenRecords ->
        Log.d(TAG("RecordUseCase", "getRecord"), "dollarRecords: $dollarRecords")
        Log.d(TAG("RecordUseCase", "getRecord"), "yenRecords: $yenRecords")
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

    suspend fun addCurrencyRecord(request: CurrencyRecordRequest) {
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

    suspend fun editRecord(record: ForeignCurrencyRecord,
                           editDate: String,
                           editMoney: String,
                           editRate: String,
                           type: CurrencyType) {

        when(type) {
            CurrencyType.USD -> {

                val drBuyRecord = record as DrBuyRecord

                val exchangeMoney = calculateExchangeMoney(editMoney, editRate)
                val editData = drBuyRecord.copy(
                    date = editDate,
                    money = editMoney,
                    rate = editRate,
                    buyRate = editRate,
                    profit = "0",
                    expectProfit = "0",
                    exchangeMoney = exchangeMoney)

                investRepository.updateDollarBuyRecord(editData)
            }
            CurrencyType.JPY -> {
                val drBuyRecord = record as YenBuyRecord

                val exchangeMoney = calculateExchangeMoney(editMoney, editRate)
                val editData = drBuyRecord.copy(
                    date = editDate,
                    money = editMoney,
                    rate = editRate,
                    buyRate = editRate,
                    profit = "0",
                    expectProfit = "0",
                    exchangeMoney = exchangeMoney)

                investRepository.updateYenBuyRecord(editData)
            }
        }




    }

    suspend fun onSellRecord(record: ForeignCurrencyRecord,
                           sellDate: String,
                           sellRate: String,
                           type: CurrencyType) {

        when(type) {
            CurrencyType.USD -> {

                val drBuyRecord = record as DrBuyRecord

                val exchangeMoney = drBuyRecord.exchangeMoney ?: return

                val money = drBuyRecord.money ?: return

                val sellProfit = sellProfit(exchangeMoney, sellRate, money, type)

                val editData = drBuyRecord.copy(
                    sellProfit = sellProfit.toString(),
                    sellDate = sellDate,
                    sellRate = sellRate,
                    expectProfit = sellProfit.toString(),
                    recordColor = true,
                )

                investRepository.updateDollarBuyRecord(editData)
            }
            CurrencyType.JPY -> {
                val yenBuyRecord = record as YenBuyRecord

                val exchangeMoney = yenBuyRecord.exchangeMoney ?: return

                val money = yenBuyRecord.money ?: return

                val sellProfit = sellProfit(exchangeMoney, sellRate, money, type)

                val editData = yenBuyRecord.copy(
                    sellProfit = sellProfit.toString(),
                    sellDate = sellDate,
                    sellRate = sellRate,
                    expectProfit = sellProfit.toString(),
                    recordColor = true)

                investRepository.updateYenBuyRecord(editData)
            }
        }

    }

    suspend fun updateRecordMemo(record: ForeignCurrencyRecord, memo: String, type: CurrencyType) {
        when(type) {
            CurrencyType.USD -> {
                val drBuyRecord = record as DrBuyRecord
                val editData = drBuyRecord.copy(memo = memo)
                investRepository.updateDollarBuyRecord(editData)
            }
            CurrencyType.JPY -> {
                val yenBuyRecord = record as YenBuyRecord
                val editData = yenBuyRecord.copy(memo = memo)
                investRepository.updateYenBuyRecord(editData)
            }
        }
    }

    suspend fun removeRecord(record: ForeignCurrencyRecord, type: CurrencyType) {
        when(type) {
            CurrencyType.USD -> {
                val drBuyRecord = record as DrBuyRecord
                investRepository.deleteDollarBuyRecord(drBuyRecord)
            }
            CurrencyType.JPY -> {
                val yenBuyRecord = record as YenBuyRecord
                investRepository.deleteYenBuyRecord(yenBuyRecord)
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


    suspend fun updateRecordCategory(record: ForeignCurrencyRecord, groupName: String, type: CurrencyType) {
        when(type) {
            CurrencyType.USD -> {
                val drBuyRecord = record as DrBuyRecord
                val updateData = drBuyRecord.copy(categoryName = groupName)
                investRepository.updateDollarBuyRecord(updateData)
            }
            CurrencyType.JPY -> {
                val yenBuyRecord = record as YenBuyRecord
                val updateData = yenBuyRecord.copy(categoryName = groupName)
                investRepository.updateYenBuyRecord(updateData)
            }
        }
    }

    suspend fun groupAdd(uiState:RecordListUiState, groupName: String, type: CurrencyType, result:(RecordListUiState)->Unit){
        when(type) {
            CurrencyType.USD -> {
                val updatedState = uiState.copy(
                    foreignCurrencyRecord = uiState.foreignCurrencyRecord.copy(
                        dollarState = uiState.foreignCurrencyRecord.dollarState.copy(
                            groups = uiState.foreignCurrencyRecord.dollarState.groups + groupName
                        )
                    )
                )

                result(updatedState)
            }
            CurrencyType.JPY -> {
                val updatedState = uiState.copy(
                    foreignCurrencyRecord = uiState.foreignCurrencyRecord.copy(
                        yenState = uiState.foreignCurrencyRecord.yenState.copy(
                            groups = uiState.foreignCurrencyRecord.yenState.groups + groupName
                        )
                    )
                )
                result(updatedState)
            }
        }
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

    fun sellProfit(
        exchangeMoney: String,
        sellRate: String,
        krMoney: String,
        type:CurrencyType
    ): BigDecimal {
        return when(type) {
            CurrencyType.USD -> {
                ((BigDecimal(exchangeMoney).times(BigDecimal(sellRate))).setScale(20, RoundingMode.HALF_UP)) - BigDecimal(krMoney)
            }
            CurrencyType.JPY -> {
                ((BigDecimal(exchangeMoney).times(BigDecimal(sellRate))).setScale(20, RoundingMode.HALF_UP))/ BigDecimal("100") - BigDecimal(krMoney)
            }
        }
    }

    fun sellPercent(
        exchangeMoney: String,
        krMoney: String
    ): Float =
        (exchangeMoney.toFloat() / krMoney.toFloat()) * 100f

    fun sumProfit(
        record: ForeignCurrencyRecordList,
        type: CurrencyType
    ): String {

        val currencyRecord = when(type) {
            CurrencyType.USD -> {
               record.dollarState.records
            }
            CurrencyType.JPY -> {
                record.yenState.records
            }
        }
        val mapProfitDecimal = currencyRecord.filter { it.profit != "" }.map { BigDecimal(it.profit) }

        if(mapProfitDecimal.isNotEmpty()) {
            return ""
        } else {
            if(mapProfitDecimal.size > 1) {
                return mapProfitDecimal.reduce {first, end ->
                    first + end }.toBigDecimalWon()
            } else {
                return mapProfitDecimal.first().toBigDecimalWon()
            }
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