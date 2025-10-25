package com.bobodroid.myapplication.models.datamodels.roomDb

import androidx.room.*
import com.bobodroid.myapplication.data.local.entity.CurrencyRecordDto
import kotlinx.coroutines.flow.Flow
import java.util.UUID

/**
 * 통합 외화 기록 DAO (DTO 버전)
 *
 * [변경 사항]
 * 기존: CurrencyRecord 사용
 * 신규: CurrencyRecordDto 사용
 */
@Dao
interface CurrencyRecordDao {

    // ===== 조회 =====

    @Query("SELECT * FROM currency_records WHERE currency_code = :currencyCode ORDER BY date DESC")
    fun getRecordsByCurrency(currencyCode: String): Flow<List<CurrencyRecordDto>>

    @Query("SELECT * FROM currency_records ORDER BY date DESC")
    fun getAllRecords(): Flow<List<CurrencyRecordDto>>

    @Query("SELECT * FROM currency_records WHERE id = :id")
    suspend fun getRecordById(id: UUID): CurrencyRecordDto?

    @Query("SELECT * FROM currency_records WHERE currency_code = :currencyCode AND category_name = :categoryName ORDER BY date DESC")
    fun getRecordsByCurrencyAndCategory(currencyCode: String, categoryName: String): Flow<List<CurrencyRecordDto>>

    @Query("SELECT * FROM currency_records WHERE record_color = 1 ORDER BY sell_date DESC")
    fun getSoldRecords(): Flow<List<CurrencyRecordDto>>

    @Query("SELECT * FROM currency_records WHERE record_color = 0 ORDER BY date DESC")
    fun getUnsoldRecords(): Flow<List<CurrencyRecordDto>>

    @Query("SELECT DISTINCT category_name FROM currency_records WHERE currency_code = :currencyCode")
    suspend fun getCategoriesByCurrency(currencyCode: String): List<String>

    // ===== 추가 =====

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: CurrencyRecordDto)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecords(records: List<CurrencyRecordDto>)

    // ===== 수정 =====

    @Update
    suspend fun updateRecord(record: CurrencyRecordDto)

    @Query("UPDATE currency_records SET profit = :profit, expect_profit = :expectProfit WHERE id = :id")
    suspend fun updateProfit(id: UUID, profit: String?, expectProfit: String?)

    @Query("UPDATE currency_records SET memo = :memo WHERE id = :id")
    suspend fun updateMemo(id: UUID, memo: String)

    @Query("UPDATE currency_records SET category_name = :categoryName WHERE id = :id")
    suspend fun updateCategory(id: UUID, categoryName: String)

    @Query("""
        UPDATE currency_records 
        SET sell_date = :sellDate, 
            sell_rate = :sellRate, 
            sell_profit = :sellProfit, 
            expect_profit = :sellProfit,
            record_color = 1 
        WHERE id = :id
    """)
    suspend fun updateSellInfo(
        id: UUID,
        sellDate: String,
        sellRate: String,
        sellProfit: String
    )

    @Query("UPDATE currency_records SET record_color = 0, sell_date = NULL, sell_rate = NULL, sell_profit = NULL WHERE id = :id")
    suspend fun cancelSell(id: UUID)

    // ===== 삭제 =====

    @Delete
    suspend fun deleteRecord(record: CurrencyRecordDto)

    @Query("DELETE FROM currency_records WHERE id = :id")
    suspend fun deleteRecordById(id: UUID)

    @Query("DELETE FROM currency_records WHERE currency_code = :currencyCode")
    suspend fun deleteRecordsByCurrency(currencyCode: String)

    @Query("DELETE FROM currency_records")
    suspend fun deleteAllRecords()

    // ===== 통계 =====

    @Query("SELECT COUNT(*) FROM currency_records WHERE currency_code = :currencyCode")
    suspend fun getRecordCount(currencyCode: String): Int

    @Query("SELECT SUM(CAST(money AS REAL)) FROM currency_records WHERE currency_code = :currencyCode AND record_color = 0")
    suspend fun getTotalInvestment(currencyCode: String): Float?

    @Query("SELECT SUM(CAST(sell_profit AS REAL)) FROM currency_records WHERE currency_code = :currencyCode AND record_color = 1")
    suspend fun getTotalProfit(currencyCode: String): Float?
}