package com.bobodroid.myapplication.models.datamodels.useCases

import android.util.Log
import com.bobodroid.myapplication.MainActivity.Companion.TAG
import com.bobodroid.myapplication.extensions.toBigDecimalWon
import com.bobodroid.myapplication.models.datamodels.repository.InvestRepository
import com.bobodroid.myapplication.models.datamodels.roomDb.*
import com.bobodroid.myapplication.models.viewmodels.CurrencyRecordState
import com.bobodroid.myapplication.models.viewmodels.RecordListUiState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.UUID
import javax.inject.Inject

class RecordUseCase @Inject constructor(
    private val investRepository: InvestRepository
) {

    // ===== 새로운 통합 메서드 (12개 통화 지원) =====

    /**
     * 모든 통화의 기록을 가져옴
     */
    fun getAllCurrencyRecords(): Flow<Map<String, CurrencyRecordState<CurrencyRecord>>> {
        return investRepository.getAllCurrencyRecords()
            .distinctUntilChanged()
            .map { records ->
                // 통화별로 그룹화
                val groupedByCurrency = records.groupBy { it.currencyCode }

                // 각 통화별로 CurrencyRecordState 생성
                groupedByCurrency.mapValues { (_, currencyRecords) ->
                    CurrencyRecordState<CurrencyRecord>(
                        records = currencyRecords,
                        groupedRecords = setGroup(currencyRecords) { it.categoryName },
                        groups = currencyRecords.map { it.categoryName ?: "미지정" }.distinct()
                    )
                }
            }
    }

    /**
     * 특정 통화의 기록만 가져옴
     */
    fun getRecordsByCurrency(currencyCode: String): Flow<CurrencyRecordState<CurrencyRecord>> {
        return investRepository.getRecordsByCurrency(currencyCode)
            .distinctUntilChanged()
            .map { records ->
                CurrencyRecordState<CurrencyRecord>(
                    records = records,
                    groupedRecords = setGroup(records) { it.categoryName },
                    groups = records.map { it.categoryName ?: "미지정" }.distinct()
                )
            }
    }

    /**
     * 통합 기록 추가 (currencyCode 사용)
     */
    suspend fun addCurrencyRecord(
        currencyCode: String,
        money: String,
        inputRate: String,
        latestRate: String,
        groupName: String,
        date: String
    ) {
        val currency = Currencies.findByCode(currencyCode)
            ?: throw IllegalArgumentException("Unknown currency: $currencyCode")

        val exchangeMoney = currency.calculateExchangeMoney(money, inputRate).toString()
        val expectedProfit = currency.calculateExpectedProfit(exchangeMoney, money, latestRate)

        val record = CurrencyRecord(
            currencyCode = currencyCode,
            date = date,
            money = money,
            rate = inputRate,
            buyRate = inputRate,
            exchangeMoney = exchangeMoney,
            profit = expectedProfit,
            expectProfit = expectedProfit,
            categoryName = groupName,
            memo = "",
            sellRate = null,
            sellProfit = null,
            sellDate = null,
            recordColor = false
        )

        investRepository.addCurrencyRecord(record)
    }

    /**
     * 통합 기록 수정 (CurrencyRecord 사용)
     */
    suspend fun editCurrencyRecord(
        record: CurrencyRecord,
        editDate: String,
        editMoney: String,
        editRate: String
    ) {
        val currency = record.getCurrency() ?: return
        val exchangeMoney = currency.calculateExchangeMoney(editMoney, editRate).toString()

        val editedRecord = record.copy(
            date = editDate,
            money = editMoney,
            rate = editRate,
            buyRate = editRate,
            profit = "0",
            expectProfit = "0",
            exchangeMoney = exchangeMoney
        )

        investRepository.updateCurrencyRecord(editedRecord)
    }

    /**
     * 통합 매도 처리 (CurrencyRecord 사용)
     */
    suspend fun sellCurrencyRecord(
        record: CurrencyRecord,
        sellDate: String,
        sellRate: String
    ) {
        val currency = record.getCurrency() ?: return
        val exchangeMoney = record.exchangeMoney ?: return
        val money = record.money ?: return

        val sellProfit = currency.calculateSellProfit(exchangeMoney, sellRate, money)

        val soldRecord = record.copy(
            sellDate = sellDate,
            sellRate = sellRate,
            sellProfit = sellProfit.toString(),
            expectProfit = sellProfit.toString(),
            recordColor = true
        )

        investRepository.updateCurrencyRecord(soldRecord)
    }

    /**
     * 통합 메모 업데이트 (CurrencyRecord 사용)
     */
    suspend fun updateCurrencyRecordMemo(record: CurrencyRecord, memo: String) {
        val updatedRecord = record.copy(memo = memo)
        investRepository.updateCurrencyRecord(updatedRecord)
    }

    /**
     * 통합 기록 삭제 (CurrencyRecord 사용)
     */
    suspend fun removeCurrencyRecord(record: CurrencyRecord) {
        investRepository.deleteCurrencyRecord(record)
    }

    /**
     * 통합 매도 취소 (CurrencyRecord 사용)
     */
    suspend fun cancelSellCurrencyRecord(record: CurrencyRecord) {
        val canceledRecord = record.copyForCancelSell()
        investRepository.updateCurrencyRecord(canceledRecord)
    }

    /**
     * 통합 카테고리 업데이트 (CurrencyRecord 사용)
     */
    suspend fun updateCurrencyRecordCategory(record: CurrencyRecord, groupName: String) {
        val updatedRecord = record.copy(categoryName = groupName)
        investRepository.updateCurrencyRecord(updatedRecord)
    }

    /**
     * 통합 그룹 추가 (통화별)
     */
    suspend fun addCurrencyGroup(
        currencyCode: String,
        currentGroups: List<String>,
        newGroupName: String
    ): List<String> {
        return currentGroups + newGroupName
    }

    /**
     * 통합 수익 갱신 (12개 통화 모두)
     */
    suspend fun refreshAllCurrencyProfits(latestRates: Map<String, String>) {
        val allRecords = investRepository.getAllCurrencyRecords().first()

        allRecords
            .filter { it.recordColor == false } // 보유중인 것만
            .forEach { record ->
                val currency = record.getCurrency() ?: return@forEach
                val rate = latestRates[record.currencyCode] ?: return@forEach

                val profit = record.money?.let { m ->
                    record.exchangeMoney?.let { e ->
                        currency.calculateExpectedProfit(e, m, rate)
                    }
                } ?: return@forEach

                val updatedRecord = record.copy(
                    profit = profit,
                    expectProfit = profit
                )

                investRepository.updateCurrencyRecord(updatedRecord)
            }
    }



    /**
     * 그룹 설정
     */
    private fun <T> setGroup(recordList: List<T>, categorySelector: (T) -> String?): Map<String, List<T>> {
        return recordList.sortedBy {
            when(it) {
                is CurrencyRecord -> it.date
                is DrBuyRecord -> it.date
                is YenBuyRecord -> it.date
                else -> null
            }
        }.groupBy {
            categorySelector(it) ?: "미지정"
        }
    }

    /**
     * 매도 퍼센트 계산
     */
    fun sellPercent(profit: String, krMoney: String): Float =
        (profit.toFloat() / krMoney.toFloat()) * 100f

    /**
     * 총 수익 계산
     */
    fun sumProfit(record: List<ForeignCurrencyRecord>): String {
        val mapProfitDecimal = record.filter { it.profit != "" }.map { BigDecimal(it.profit) }

        return if(mapProfitDecimal.isNotEmpty()) {
            if(mapProfitDecimal.size > 1) {
                mapProfitDecimal.reduce { first, end ->
                    first + end
                }.toBigDecimalWon()
            } else {
                mapProfitDecimal.first().toBigDecimalWon()
            }
        } else {
            ""
        }
    }

}

/**
 * 기록 요청 데이터 클래스 (레거시)
 */
data class CurrencyRecordRequest(
    val latestRate: String,
    val money: String,
    val inputRate: String,
    val groupName: String,
    val date: String,
    val type: CurrencyType  // USD, JPY
)