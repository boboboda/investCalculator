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

    // ===== 레거시 메서드 (하위 호환성 유지 - USD, JPY만) =====

    /**
     * USD/JPY만 가져옴 (레거시)
     */
//    fun getRecord(): Flow<ForeignCurrencyRecordList> = combine(
//        investRepository.getAllDollarBuyRecords().distinctUntilChanged(),
//        investRepository.getAllYenBuyRecords().distinctUntilChanged()
//    ) { dollarRecords, yenRecords ->
//        Log.d(TAG("RecordUseCase", "getRecord"), "dollarRecords: $dollarRecords")
//        Log.d(TAG("RecordUseCase", "getRecord"), "yenRecords: $yenRecords")
//        ForeignCurrencyRecordList(
//            dollarState = CurrencyRecordState(
//                records = dollarRecords as List<ForeignCurrencyRecord>,
//                groupedRecords = setGroup(dollarRecords) { it.categoryName},
//                groups = dollarRecords.map { it.categoryName ?: "미지정" }.distinct()
//            ),
//            yenState = CurrencyRecordState(
//                records = yenRecords as List<ForeignCurrencyRecord>,
//                groupedRecords = setGroup(yenRecords) { it.categoryName },
//                groups = yenRecords.map { it.categoryName ?: "미지정" }.distinct()
//            )
//        )
//    }

    /**
     * 레거시 기록 추가 (CurrencyRecordRequest 사용)
     */
//    suspend fun addCurrencyRecord(request: CurrencyRecordRequest) {
//        val currency = Currencies.fromCurrencyType(request.type)
//        val exchangeMoney = currency.calculateExchangeMoney(request.money, request.inputRate).toString()
//        val expectedProfit = currency.calculateExpectedProfit(exchangeMoney, request.money, request.latestRate)
//
//        when (request.type) {
//            CurrencyType.USD -> {
//                investRepository.addDollarBuyRecord(
//                    DrBuyRecord(
//                        date = request.date,
//                        money = request.money,
//                        rate = request.inputRate,
//                        buyRate = request.inputRate,
//                        exchangeMoney = exchangeMoney,
//                        profit = expectedProfit,
//                        expectProfit = expectedProfit,
//                        categoryName = request.groupName,
//                        memo = "",
//                        sellRate = "",
//                        sellProfit = "",
//                        recordColor = false
//                    )
//                )
//            }
//            CurrencyType.JPY -> {
//                investRepository.addYenBuyRecord(
//                    YenBuyRecord(
//                        date = request.date,
//                        money = request.money,
//                        rate = request.inputRate,
//                        buyRate = request.inputRate,
//                        exchangeMoney = exchangeMoney,
//                        profit = expectedProfit,
//                        expectProfit = expectedProfit,
//                        categoryName = request.groupName,
//                        memo = "",
//                        sellRate = "",
//                        sellProfit = "",
//                        recordColor = false
//                    )
//                )
//            }
//        }
//    }

    /**
     * 레거시 기록 수정
     */
//    suspend fun editRecord(
//        record: ForeignCurrencyRecord,
//        editDate: String,
//        editMoney: String,
//        editRate: String,
//        type: CurrencyType
//    ) {
//        // CurrencyRecord로 변환해서 통합 메서드 사용
//        if (record is CurrencyRecord) {
//            editCurrencyRecord(record, editDate, editMoney, editRate)
//            return
//        }
//
//        // 레거시 처리
//        val currency = Currencies.fromCurrencyType(type)
//        val exchangeMoney = currency.calculateExchangeMoney(editMoney, editRate).toString()
//
//        when(type) {
//            CurrencyType.USD -> {
//                val drBuyRecord = record as DrBuyRecord
//                val editData = drBuyRecord.copy(
//                    date = editDate,
//                    money = editMoney,
//                    rate = editRate,
//                    buyRate = editRate,
//                    profit = "0",
//                    expectProfit = "0",
//                    exchangeMoney = exchangeMoney
//                )
//                investRepository.updateDollarBuyRecord(editData)
//            }
//            CurrencyType.JPY -> {
//                val yenBuyRecord = record as YenBuyRecord
//                val editData = yenBuyRecord.copy(
//                    date = editDate,
//                    money = editMoney,
//                    rate = editRate,
//                    buyRate = editRate,
//                    profit = "0",
//                    expectProfit = "0",
//                    exchangeMoney = exchangeMoney
//                )
//                investRepository.updateYenBuyRecord(editData)
//            }
//        }
//    }

    /**
     * 레거시 매도 처리
     */
//    suspend fun onSellRecord(
//        record: ForeignCurrencyRecord,
//        sellDate: String,
//        sellRate: String,
//        type: CurrencyType
//    ) {
//        // CurrencyRecord로 변환해서 통합 메서드 사용
//        if (record is CurrencyRecord) {
//            sellCurrencyRecord(record, sellDate, sellRate)
//            return
//        }
//
//        // 레거시 처리
//        val currency = Currencies.fromCurrencyType(type)
//
//        when(type) {
//            CurrencyType.USD -> {
//                val drBuyRecord = record as DrBuyRecord
//                val exchangeMoney = drBuyRecord.exchangeMoney ?: return
//                val money = drBuyRecord.money ?: return
//
//                val sellProfit = currency.calculateSellProfit(exchangeMoney, sellRate, money)
//
//                val editData = drBuyRecord.copy(
//                    sellProfit = sellProfit.toString(),
//                    sellDate = sellDate,
//                    sellRate = sellRate,
//                    expectProfit = sellProfit.toString(),
//                    recordColor = true,
//                )
//
//                investRepository.updateDollarBuyRecord(editData)
//            }
//            CurrencyType.JPY -> {
//                val yenBuyRecord = record as YenBuyRecord
//                val exchangeMoney = yenBuyRecord.exchangeMoney ?: return
//                val money = yenBuyRecord.money ?: return
//
//                val sellProfit = currency.calculateSellProfit(exchangeMoney, sellRate, money)
//
//                val editData = yenBuyRecord.copy(
//                    sellProfit = sellProfit.toString(),
//                    sellDate = sellDate,
//                    sellRate = sellRate,
//                    expectProfit = sellProfit.toString(),
//                    recordColor = true
//                )
//
//                investRepository.updateYenBuyRecord(editData)
//            }
//        }
//    }

    /**
     * 레거시 메모 업데이트
     */
//    suspend fun updateRecordMemo(record: ForeignCurrencyRecord, memo: String, type: CurrencyType) {
//        // CurrencyRecord로 변환해서 통합 메서드 사용
//        if (record is CurrencyRecord) {
//            updateCurrencyRecordMemo(record, memo)
//            return
//        }
//
//        // 레거시 처리
//        when(type) {
//            CurrencyType.USD -> {
//                val drBuyRecord = record as DrBuyRecord
//                val editData = drBuyRecord.copy(memo = memo)
//                investRepository.updateDollarBuyRecord(editData)
//            }
//            CurrencyType.JPY -> {
//                val yenBuyRecord = record as YenBuyRecord
//                val editData = yenBuyRecord.copy(memo = memo)
//                investRepository.updateYenBuyRecord(editData)
//            }
//        }
//    }

    /**
     * 레거시 기록 삭제
     */
//    suspend fun removeRecord(record: ForeignCurrencyRecord, type: CurrencyType) {
//        // CurrencyRecord로 변환해서 통합 메서드 사용
//        if (record is CurrencyRecord) {
//            removeCurrencyRecord(record)
//            return
//        }
//
//        // 레거시 처리
//        when(type) {
//            CurrencyType.USD -> {
//                val drBuyRecord = record as DrBuyRecord
//                investRepository.deleteDollarBuyRecord(drBuyRecord)
//            }
//            CurrencyType.JPY -> {
//                val yenBuyRecord = record as YenBuyRecord
//                investRepository.deleteYenBuyRecord(yenBuyRecord)
//            }
//        }
//    }

    /**
     * 레거시 매도 취소
     */
//    suspend fun cancelSellRecord(id: UUID, currencyType: CurrencyType): Boolean {
//        // ID로 기록 조회 후 통합 메서드 사용
//        val record = investRepository.getCurrencyRecordById(id)
//        if (record != null) {
//            cancelSellCurrencyRecord(record)
//            return true
//        }
//
//        // 레거시 처리
//        val searchBuyRecord = when(currencyType) {
//            CurrencyType.USD -> investRepository.getDollarBuyRecordById(id)
//            CurrencyType.JPY -> investRepository.getYenBuyRecordById(id)
//        }
//
//        val updateData = when(searchBuyRecord) {
//            is DrBuyRecord -> searchBuyRecord.copy(recordColor = false)
//            is YenBuyRecord -> searchBuyRecord.copy(recordColor = false)
//            else -> return false
//        }
//
//        when(currencyType) {
//            CurrencyType.USD -> {
//                val dollarRecord = updateData as DrBuyRecord
//                investRepository.updateDollarBuyRecord(dollarRecord)
//            }
//            CurrencyType.JPY -> {
//                val yenRecord = updateData as YenBuyRecord
//                investRepository.updateYenBuyRecord(yenRecord)
//            }
//        }
//
//        return true
//    }

    /**
     * 레거시 카테고리 업데이트
     */
//    suspend fun updateRecordCategory(record: ForeignCurrencyRecord, groupName: String, type: CurrencyType) {
//        // CurrencyRecord로 변환해서 통합 메서드 사용
//        if (record is CurrencyRecord) {
//            updateCurrencyRecordCategory(record, groupName)
//            return
//        }
//
//        // 레거시 처리
//        when(type) {
//            CurrencyType.USD -> {
//                val drBuyRecord = record as DrBuyRecord
//                val updateData = drBuyRecord.copy(categoryName = groupName)
//                investRepository.updateDollarBuyRecord(updateData)
//            }
//            CurrencyType.JPY -> {
//                val yenBuyRecord = record as YenBuyRecord
//                val updateData = yenBuyRecord.copy(categoryName = groupName)
//                investRepository.updateYenBuyRecord(updateData)
//            }
//        }
//    }

    /**
     * 레거시 그룹 추가
     */
//    suspend fun groupAdd(uiState: RecordListUiState, groupName: String, type: CurrencyType, result: (RecordListUiState) -> Unit) {
//        when(type) {
//            CurrencyType.USD -> {
//                val updatedState = uiState.copy(
//                    foreignCurrencyRecord = uiState.foreignCurrencyRecord.copy(
//                        dollarState = uiState.foreignCurrencyRecord.dollarState.copy(
//                            groups = uiState.foreignCurrencyRecord.dollarState.groups + groupName
//                        )
//                    )
//                )
//                result(updatedState)
//            }
//            CurrencyType.JPY -> {
//                val updatedState = uiState.copy(
//                    foreignCurrencyRecord = uiState.foreignCurrencyRecord.copy(
//                        yenState = uiState.foreignCurrencyRecord.yenState.copy(
//                            groups = uiState.foreignCurrencyRecord.yenState.groups + groupName
//                        )
//                    )
//                )
//                result(updatedState)
//            }
//        }
//    }

    /**
     * 레거시 수익 갱신 (USD, JPY만)
     */
//    suspend fun reFreshProfit(
//        latestRate: ExchangeRate,
//        records: ForeignCurrencyRecordList
//    ) {
//        // USD 처리
//        records.dollarState.records
//            .filter { it.recordColor == false }
//            .forEach { record ->
//                val profit = record.money?.let { m ->
//                    record.exchangeMoney?.let { e ->
//                        latestRate.usd?.let { r ->
//                            Currencies.USD.calculateExpectedProfit(e, m, r)
//                        }
//                    }
//                }
//                profit?.let {
//                    investRepository.updateDollarBuyRecord(
//                        record.copyWithProfitAndExpectProfit(it) as DrBuyRecord
//                    )
//                }
//            }
//
//        // JPY 처리
//        records.yenState.records
//            .filter { it.recordColor == false }
//            .forEach { record ->
//                val profit = record.money?.let { m ->
//                    record.exchangeMoney?.let { e ->
//                        latestRate.jpy?.let { r ->
//                            Currencies.JPY.calculateExpectedProfit(e, m, r)
//                        }
//                    }
//                }
//                profit?.let {
//                    investRepository.updateYenBuyRecord(
//                        record.copyWithProfitAndExpectProfit(it) as YenBuyRecord
//                    )
//                }
//            }
//    }

    // ===== 헬퍼 메서드 =====

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

    /**
     * 매도 수익 계산 (레거시)
     */
//    fun sellProfit(exchangeMoney: String, sellRate: String, krMoney: String, type: CurrencyType): BigDecimal {
//        val currency = Currencies.fromCurrencyType(type)
//        return currency.calculateSellProfit(exchangeMoney, sellRate, krMoney)
//    }
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