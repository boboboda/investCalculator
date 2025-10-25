package com.bobodroid.myapplication.data.repository

import com.bobodroid.myapplication.data.mapper.RecordMapper
import com.bobodroid.myapplication.data.mapper.RecordMapper.toDto
import com.bobodroid.myapplication.data.mapper.RecordMapper.toDtoList
import com.bobodroid.myapplication.data.mapper.RecordMapper.toEntity
import com.bobodroid.myapplication.data.mapper.RecordMapper.toEntityList
import com.bobodroid.myapplication.domain.entity.RecordEntity
import com.bobodroid.myapplication.domain.repository.IRecordRepository
import com.bobodroid.myapplication.models.datamodels.roomDb.CurrencyRecordDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Record Repository 구현체 - Entity 버전
 *
 * [변경 사항]
 * - DTO ↔ Entity 변환 추가
 * - 모든 함수가 Entity 반환
 * - Mapper를 통한 변환 처리
 */
@Singleton
class RecordRepositoryImpl @Inject constructor(
    private val currencyRecordDao: CurrencyRecordDao
) : IRecordRepository {

    // ===== 조회 =====

    /**
     * 특정 통화의 모든 기록 조회
     * ⭐ DTO → Entity 변환
     */
    override fun getRecordsByCurrency(currencyCode: String): Flow<List<RecordEntity>> {
        return currencyRecordDao.getRecordsByCurrency(currencyCode)
            .map { dtoList -> dtoList.toEntityList() }
    }

    /**
     * 모든 기록 조회
     * ⭐ DTO → Entity 변환
     */
    override fun getAllRecords(): Flow<List<RecordEntity>> {
        return currencyRecordDao.getAllRecords()
            .map { dtoList -> dtoList.toEntityList() }
    }

    /**
     * 통화 + 카테고리로 기록 조회
     * ⭐ DTO → Entity 변환
     */
    override fun getRecordsByCurrencyAndCategory(
        currencyCode: String,
        categoryName: String
    ): Flow<List<RecordEntity>> {
        return currencyRecordDao.getRecordsByCurrencyAndCategory(currencyCode, categoryName)
            .map { dtoList -> dtoList.toEntityList() }
    }

    /**
     * 매도된 기록만 조회
     * ⭐ DTO → Entity 변환
     */
    override fun getSoldRecords(): Flow<List<RecordEntity>> {
        return currencyRecordDao.getSoldRecords()
            .map { dtoList -> dtoList.toEntityList() }
    }

    /**
     * 미매도 기록만 조회
     * ⭐ DTO → Entity 변환
     */
    override fun getUnsoldRecords(): Flow<List<RecordEntity>> {
        return currencyRecordDao.getUnsoldRecords()
            .map { dtoList -> dtoList.toEntityList() }
    }

    /**
     * ID로 단일 기록 조회
     * ⭐ DTO → Entity 변환
     */
    override suspend fun getRecordById(id: UUID): RecordEntity? {
        val dto = currencyRecordDao.getRecordById(id)
        return dto?.toEntity()
    }

    /**
     * 통화별 카테고리 목록 조회
     */
    override suspend fun getCategoriesByCurrency(currencyCode: String): List<String> {
        return currencyRecordDao.getCategoriesByCurrency(currencyCode)
    }

    // ===== 추가/수정/삭제 =====

    /**
     * 기록 추가
     * ⭐ Entity → DTO 변환
     */
    override suspend fun addRecord(record: RecordEntity) {
        val dto = record.toDto()
        currencyRecordDao.insertRecord(dto)
    }

    /**
     * 여러 기록 추가
     * ⭐ Entity → DTO 변환
     */
    override suspend fun addRecords(records: List<RecordEntity>) {
        val dtoList = records.toDtoList()
        currencyRecordDao.insertRecords(dtoList)
    }

    /**
     * 기록 수정
     * ⭐ Entity → DTO 변환
     */
    override suspend fun updateRecord(record: RecordEntity) {
        val dto = record.toDto()
        currencyRecordDao.updateRecord(dto)
    }

    /**
     * 기록 삭제
     * ⭐ Entity → DTO 변환
     */
    override suspend fun deleteRecord(record: RecordEntity) {
        val dto = record.toDto()
        currencyRecordDao.deleteRecord(dto)
    }

    /**
     * ID로 기록 삭제
     */
    override suspend fun deleteRecordById(id: UUID) {
        currencyRecordDao.deleteRecordById(id)
    }

    /**
     * 특정 통화의 모든 기록 삭제
     */
    override suspend fun deleteRecordsByCurrency(currencyCode: String) {
        currencyRecordDao.deleteRecordsByCurrency(currencyCode)
    }

    /**
     * 모든 기록 삭제
     */
    override suspend fun deleteAllRecords() {
        currencyRecordDao.deleteAllRecords()
    }

    // ===== 부분 업데이트 =====

    /**
     * 수익 업데이트
     */
    override suspend fun updateProfit(id: UUID, profit: String?, expectProfit: String?) {
        currencyRecordDao.updateProfit(id, profit, expectProfit)
    }

    /**
     * 매도 정보 업데이트
     */
    override suspend fun updateSellInfo(id: UUID, sellRate: String, sellDate: String, profit: String) {
        currencyRecordDao.updateSellInfo(id, sellRate, sellDate, profit)
    }

    /**
     * 메모 업데이트
     */
    override suspend fun updateMemo(id: UUID, memo: String) {
        currencyRecordDao.updateMemo(id, memo)
    }
}