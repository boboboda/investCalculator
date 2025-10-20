package com.bobodroid.myapplication.models.datamodels.repository

import com.bobodroid.myapplication.models.datamodels.roomDb.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InvestRepository @Inject constructor(
    private val dollarRepository: DollarRepository,
    private val yenRepository: YenRepository,
    private val currencyRecordDao: CurrencyRecordDao  // 새로운 통화용 DAO
) {
    // ===== 기존 Dollar Buy 메서드들 (완전 유지) =====
    suspend fun addDollarBuyRecord(record: DrBuyRecord) = dollarRepository.addRecord(record)
    suspend fun addDollarBuyRecords(records: List<DrBuyRecord>) = dollarRepository.drBuyAddListRecord(records)
    suspend fun updateDollarBuyRecord(record: DrBuyRecord) = dollarRepository.updateRecord(record)
    suspend fun deleteDollarBuyRecord(record: DrBuyRecord) = dollarRepository.deleteRecord(record)
    suspend fun deleteAllDollarBuyRecords() = dollarRepository.deleteAllRecord(DrBuyRecord())
    suspend fun getDollarBuyRecordById(id: UUID): DrBuyRecord = dollarRepository.getRecordId(id)
    fun getAllDollarBuyRecords(): Flow<List<DrBuyRecord>> = dollarRepository.getAllBuyRecords()

    // ===== 기존 Yen Buy 메서드들 (완전 유지) =====
    suspend fun addYenBuyRecord(record: YenBuyRecord) = yenRepository.addRecord(record)
    suspend fun addYenBuyRecords(records: List<YenBuyRecord>) = yenRepository.yenBuyAddListRecord(records)
    suspend fun updateYenBuyRecord(record: YenBuyRecord) = yenRepository.updateRecord(record)
    suspend fun deleteYenBuyRecord(record: YenBuyRecord) = yenRepository.deleteRecord(record)
    suspend fun deleteAllYenBuyRecords() = yenRepository.deleteAllRecord(YenBuyRecord())
    suspend fun getYenBuyRecordById(id: UUID): YenBuyRecord = yenRepository.getYenRecordId(id)
    fun getAllYenBuyRecords(): Flow<List<YenBuyRecord>> = yenRepository.getAllYenBuyRecords()

    // ===== 새로운 통합 메서드들 (12개 통화 지원) =====

    /**
     * 통화별 기록 조회
     * USD, JPY: 기존 Repository 사용
     * 나머지: 새 DAO 사용
     */
    fun getRecordsByCurrency(currencyCode: String): Flow<List<CurrencyRecord>> {
        return when(currencyCode) {
            "USD" -> getAllDollarBuyRecords().map { records ->
                records.map { it.toCurrencyRecord() }
            }
            "JPY" -> getAllYenBuyRecords().map { records ->
                records.map { it.toCurrencyRecord() }
            }
            else -> currencyRecordDao.getRecordsByCurrency(currencyCode)
        }
    }

    /**
     * 모든 통화의 기록 조회
     */
    fun getAllCurrencyRecords(): Flow<List<CurrencyRecord>> {
        return combine(
            getAllDollarBuyRecords(),
            getAllYenBuyRecords(),
            currencyRecordDao.getAllRecords()
        ) { dollarRecords, yenRecords, newRecords ->
            val usdRecords = dollarRecords.map { it.toCurrencyRecord() }
            val jpyRecords = yenRecords.map { it.toCurrencyRecord() }
            // 새 테이블에서 USD, JPY 제외한 나머지만 가져옴 (중복 방지)
            val otherRecords = newRecords.filter {
                it.currencyCode != "USD" && it.currencyCode != "JPY"
            }
            usdRecords + jpyRecords + otherRecords
        }
    }

    /**
     * 통합 기록 추가
     * USD, JPY: 기존 Repository 사용
     * 나머지: 새 DAO 사용
     */
    suspend fun addCurrencyRecord(record: CurrencyRecord) {
        when(record.currencyCode) {
            "USD" -> addDollarBuyRecord(record.toLegacyDollarRecord())
            "JPY" -> addYenBuyRecord(record.toLegacyYenRecord())
            else -> currencyRecordDao.insertRecord(record)
        }
    }

    /**
     * 통합 기록 수정
     */
    suspend fun updateCurrencyRecord(record: CurrencyRecord) {
        when(record.currencyCode) {
            "USD" -> updateDollarBuyRecord(record.toLegacyDollarRecord())
            "JPY" -> updateYenBuyRecord(record.toLegacyYenRecord())
            else -> currencyRecordDao.updateRecord(record)
        }
    }

    /**
     * 통합 기록 삭제
     */
    suspend fun deleteCurrencyRecord(record: CurrencyRecord) {
        when(record.currencyCode) {
            "USD" -> deleteDollarBuyRecord(record.toLegacyDollarRecord())
            "JPY" -> deleteYenBuyRecord(record.toLegacyYenRecord())
            else -> currencyRecordDao.deleteRecord(record)
        }
    }

    /**
     * ID로 통합 기록 조회
     */
    suspend fun getCurrencyRecordById(id: UUID): CurrencyRecord? {
        // 먼저 새 테이블에서 찾기
        currencyRecordDao.getRecordById(id)?.let { return it }

        // 없으면 기존 테이블에서 찾기
        try {
            getDollarBuyRecordById(id).let {
                return it.toCurrencyRecord()
            }
        } catch (e: Exception) {
            // 달러에 없으면 엔에서 찾기
        }

        try {
            getYenBuyRecordById(id).let {
                return it.toCurrencyRecord()
            }
        } catch (e: Exception) {
            // 엔에도 없으면 null 반환
        }

        return null
    }

    /**
     * 메모 업데이트 (통합)
     */
    suspend fun updateCurrencyRecordMemo(id: UUID, memo: String) {
        val record = getCurrencyRecordById(id) ?: return

        when(record.currencyCode) {
            "USD" -> {
                try {
                    val dollarRecord = getDollarBuyRecordById(id)
                    updateDollarBuyRecord(dollarRecord.copy(memo = memo))
                } catch (e: Exception) {
                    // 에러 처리
                }
            }
            "JPY" -> {
                try {
                    val yenRecord = getYenBuyRecordById(id)
                    updateYenBuyRecord(yenRecord.copy(memo = memo))
                } catch (e: Exception) {
                    // 에러 처리
                }
            }
            else -> {
                currencyRecordDao.updateMemo(id, memo)
            }
        }
    }

    /**
     * 카테고리 업데이트 (통합)
     */
    suspend fun updateCurrencyRecordCategory(id: UUID, categoryName: String) {
        val record = getCurrencyRecordById(id) ?: return

        when(record.currencyCode) {
            "USD" -> {
                try {
                    val dollarRecord = getDollarBuyRecordById(id)
                    updateDollarBuyRecord(dollarRecord.copy(categoryName = categoryName))
                } catch (e: Exception) {
                    // 에러 처리
                }
            }
            "JPY" -> {
                try {
                    val yenRecord = getYenBuyRecordById(id)
                    updateYenBuyRecord(yenRecord.copy(categoryName = categoryName))
                } catch (e: Exception) {
                    // 에러 처리
                }
            }
            else -> {
                currencyRecordDao.updateCategory(id, categoryName)
            }
        }
    }

    /**
     * 매도 정보 업데이트 (통합)
     */
    suspend fun updateSellInfo(id: UUID, sellDate: String, sellRate: String, sellProfit: String) {
        val record = getCurrencyRecordById(id) ?: return

        when(record.currencyCode) {
            "USD" -> {
                try {
                    val dollarRecord = getDollarBuyRecordById(id)
                    updateDollarBuyRecord(
                        dollarRecord.copy(
                            sellDate = sellDate,
                            sellRate = sellRate,
                            sellProfit = sellProfit,
                            expectProfit = sellProfit,
                            recordColor = true
                        )
                    )
                } catch (e: Exception) {
                    // 에러 처리
                }
            }
            "JPY" -> {
                try {
                    val yenRecord = getYenBuyRecordById(id)
                    updateYenBuyRecord(
                        yenRecord.copy(
                            sellDate = sellDate,
                            sellRate = sellRate,
                            sellProfit = sellProfit,
                            expectProfit = sellProfit,
                            recordColor = true
                        )
                    )
                } catch (e: Exception) {
                    // 에러 처리
                }
            }
            else -> {
                currencyRecordDao.updateSellInfo(id, sellDate, sellRate, sellProfit)
            }
        }
    }

    /**
     * 매도 취소 (통합)
     */
    suspend fun cancelSell(id: UUID) {
        val record = getCurrencyRecordById(id) ?: return

        when(record.currencyCode) {
            "USD" -> {
                try {
                    val dollarRecord = getDollarBuyRecordById(id)
                    updateDollarBuyRecord(dollarRecord.copy(recordColor = false))
                } catch (e: Exception) {
                    // 에러 처리
                }
            }
            "JPY" -> {
                try {
                    val yenRecord = getYenBuyRecordById(id)
                    updateYenBuyRecord(yenRecord.copy(recordColor = false))
                } catch (e: Exception) {
                    // 에러 처리
                }
            }
            else -> {
                currencyRecordDao.cancelSell(id)
            }
        }
    }

    /**
     * 수익 업데이트 (통합)
     */
    suspend fun updateProfit(id: UUID, profit: String?, expectProfit: String?) {
        val record = getCurrencyRecordById(id) ?: return

        when(record.currencyCode) {
            "USD" -> {
                try {
                    val dollarRecord = getDollarBuyRecordById(id)
                    updateDollarBuyRecord(
                        dollarRecord.copy(profit = profit, expectProfit = expectProfit)
                    )
                } catch (e: Exception) {
                    // 에러 처리
                }
            }
            "JPY" -> {
                try {
                    val yenRecord = getYenBuyRecordById(id)
                    updateYenBuyRecord(
                        yenRecord.copy(profit = profit, expectProfit = expectProfit)
                    )
                } catch (e: Exception) {
                    // 에러 처리
                }
            }
            else -> {
                currencyRecordDao.updateProfit(id, profit, expectProfit)
            }
        }
    }
}