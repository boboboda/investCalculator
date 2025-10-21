package com.bobodroid.myapplication.models.datamodels.repository

import com.bobodroid.myapplication.models.datamodels.roomDb.*
import kotlinx.coroutines.flow.Flow
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 투자 기록 Repository
 * - CurrencyRecord: 통합 외화 기록 (모든 통화)
 * - 구버전 테이블 제거 완료
 */
@Singleton
class InvestRepository @Inject constructor(
    private val currencyRecordDao: CurrencyRecordDao
) {

    // ===== 통화별 기록 조회 =====

    /**
     * 특정 통화의 모든 기록 조회
     */
    fun getRecordsByCurrency(currencyCode: String): Flow<List<CurrencyRecord>> {
        return currencyRecordDao.getRecordsByCurrency(currencyCode)
    }

    /**
     * 모든 통화의 기록 조회
     */
    fun getAllCurrencyRecords(): Flow<List<CurrencyRecord>> {
        return currencyRecordDao.getAllRecords()
    }

    /**
     * 통화별, 카테고리별 기록 조회
     */
    fun getRecordsByCurrencyAndCategory(
        currencyCode: String,
        categoryName: String
    ): Flow<List<CurrencyRecord>> {
        return currencyRecordDao.getRecordsByCurrencyAndCategory(currencyCode, categoryName)
    }

    /**
     * 매도된 기록만 조회
     */
    fun getSoldRecords(): Flow<List<CurrencyRecord>> {
        return currencyRecordDao.getSoldRecords()
    }

    /**
     * 매도되지 않은 기록만 조회
     */
    fun getUnsoldRecords(): Flow<List<CurrencyRecord>> {
        return currencyRecordDao.getUnsoldRecords()
    }

    /**
     * ID로 기록 조회
     */
    suspend fun getCurrencyRecordById(id: UUID): CurrencyRecord? {
        return currencyRecordDao.getRecordById(id)
    }

    /**
     * 통화별 카테고리 목록 조회
     */
    suspend fun getCategoriesByCurrency(currencyCode: String): List<String> {
        return currencyRecordDao.getCategoriesByCurrency(currencyCode)
    }

    // ===== 기록 추가/수정/삭제 =====

    /**
     * 기록 추가
     */
    suspend fun addCurrencyRecord(record: CurrencyRecord) {
        currencyRecordDao.insertRecord(record)
    }

    /**
     * 여러 기록 추가
     */
    suspend fun addCurrencyRecords(records: List<CurrencyRecord>) {
        currencyRecordDao.insertRecords(records)
    }

    /**
     * 기록 수정
     */
    suspend fun updateCurrencyRecord(record: CurrencyRecord) {
        currencyRecordDao.updateRecord(record)
    }

    /**
     * 기록 삭제
     */
    suspend fun deleteCurrencyRecord(record: CurrencyRecord) {
        currencyRecordDao.deleteRecord(record)
    }

    /**
     * ID로 기록 삭제
     */
    suspend fun deleteCurrencyRecordById(id: UUID) {
        currencyRecordDao.deleteRecordById(id)
    }

    /**
     * 특정 통화의 모든 기록 삭제
     */
    suspend fun deleteRecordsByCurrency(currencyCode: String) {
        currencyRecordDao.deleteRecordsByCurrency(currencyCode)
    }

    /**
     * 모든 기록 삭제
     */
    suspend fun deleteAllRecords() {
        currencyRecordDao.deleteAllRecords()
    }

    // ===== 부분 업데이트 =====

    /**
     * 수익 업데이트
     */
    suspend fun updateProfit(id: UUID, profit: String?, expectProfit: String?) {
        currencyRecordDao.updateProfit(id, profit, expectProfit)
    }

    /**
     * 메모 업데이트
     */
    suspend fun updateMemo(id: UUID, memo: String) {
        currencyRecordDao.updateMemo(id, memo)
    }

    /**
     * 카테고리 업데이트
     */
    suspend fun updateCategory(id: UUID, categoryName: String) {
        currencyRecordDao.updateCategory(id, categoryName)
    }

    /**
     * 매도 정보 업데이트
     */
    suspend fun updateSellInfo(
        id: UUID,
        sellDate: String,
        sellRate: String,
        sellProfit: String
    ) {
        currencyRecordDao.updateSellInfo(id, sellDate, sellRate, sellProfit)
    }

    /**
     * 매도 취소
     */
    suspend fun cancelSell(id: UUID) {
        currencyRecordDao.cancelSell(id)
    }

    // ===== 통계 =====

    /**
     * 특정 통화의 기록 개수
     */
    suspend fun getRecordCount(currencyCode: String): Int {
        return currencyRecordDao.getRecordCount(currencyCode)
    }

    /**
     * 특정 통화의 총 투자금액
     */
    suspend fun getTotalInvestment(currencyCode: String): Float? {
        return currencyRecordDao.getTotalInvestment(currencyCode)
    }

    /**
     * 특정 통화의 총 수익
     */
    suspend fun getTotalProfit(currencyCode: String): Float? {
        return currencyRecordDao.getTotalProfit(currencyCode)
    }

    // ===== 하위 호환성 메서드 (USD, JPY) =====

    /**
     * USD 기록 조회 (하위 호환)
     */
    fun getAllDollarBuyRecords(): Flow<List<CurrencyRecord>> {
        return getRecordsByCurrency("USD")
    }

    /**
     * JPY 기록 조회 (하위 호환)
     */
    fun getAllYenBuyRecords(): Flow<List<CurrencyRecord>> {
        return getRecordsByCurrency("JPY")
    }
}