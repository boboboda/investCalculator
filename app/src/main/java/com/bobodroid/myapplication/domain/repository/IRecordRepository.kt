package com.bobodroid.myapplication.domain.repository

import com.bobodroid.myapplication.domain.entity.RecordEntity
import kotlinx.coroutines.flow.Flow
import java.util.UUID

/**
 * Record Repository 인터페이스 - Entity 버전
 *
 * [변경 사항]
 * 기존: CurrencyRecord 반환
 * 신규: RecordEntity 반환
 *
 * Domain Layer의 추상화된 데이터 접근 인터페이스
 * - Platform 독립적인 비즈니스 로직 정의
 * - 구현체는 Data Layer에 위치
 */
interface IRecordRepository {

    // ===== 조회 =====

    /**
     * 특정 통화의 모든 기록 조회
     * @return RecordEntity Flow
     */
    fun getRecordsByCurrency(currencyCode: String): Flow<List<RecordEntity>>

    /**
     * 모든 기록 조회
     * @return RecordEntity Flow
     */
    fun getAllRecords(): Flow<List<RecordEntity>>

    /**
     * 통화 + 카테고리로 기록 조회
     * @return RecordEntity Flow
     */
    fun getRecordsByCurrencyAndCategory(
        currencyCode: String,
        categoryName: String
    ): Flow<List<RecordEntity>>

    /**
     * 매도된 기록만 조회
     * @return RecordEntity Flow
     */
    fun getSoldRecords(): Flow<List<RecordEntity>>

    /**
     * 미매도 기록만 조회
     * @return RecordEntity Flow
     */
    fun getUnsoldRecords(): Flow<List<RecordEntity>>

    /**
     * ID로 단일 기록 조회
     * @return RecordEntity 또는 null
     */
    suspend fun getRecordById(id: UUID): RecordEntity?

    /**
     * 통화별 카테고리 목록 조회
     * @return 카테고리명 리스트
     */
    suspend fun getCategoriesByCurrency(currencyCode: String): List<String>

    // ===== 추가/수정/삭제 =====

    /**
     * 기록 추가
     * @param record RecordEntity
     */
    suspend fun addRecord(record: RecordEntity)

    /**
     * 여러 기록 추가
     * @param records RecordEntity 리스트
     */
    suspend fun addRecords(records: List<RecordEntity>)

    /**
     * 기록 수정
     * @param record RecordEntity
     */
    suspend fun updateRecord(record: RecordEntity)

    /**
     * 기록 삭제
     * @param record RecordEntity
     */
    suspend fun deleteRecord(record: RecordEntity)

    /**
     * ID로 기록 삭제
     */
    suspend fun deleteRecordById(id: UUID)

    /**
     * 특정 통화의 모든 기록 삭제
     */
    suspend fun deleteRecordsByCurrency(currencyCode: String)

    /**
     * 모든 기록 삭제
     */
    suspend fun deleteAllRecords()

    // ===== 부분 업데이트 =====

    /**
     * 수익 업데이트
     */
    suspend fun updateProfit(id: UUID, profit: String?, expectProfit: String?)

    /**
     * 매도 정보 업데이트
     */
    suspend fun updateSellInfo(id: UUID, sellRate: String, sellDate: String, profit: String)

    /**
     * 메모 업데이트
     */
    suspend fun updateMemo(id: UUID, memo: String)
}