package com.bobodroid.myapplication.models.datamodels.roomDb

import android.util.Log
import androidx.room.*
import com.bobodroid.myapplication.MainActivity.Companion.TAG
import kotlinx.coroutines.flow.Flow
import java.util.UUID

/**
 * 사용자 데이터 DAO (UUID)
 */
@Dao
interface LocalUserDatabaseDao {

    @Query("SELECT * from LocalUserData_table")
    fun getUserData(): Flow<LocalUserData>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(localUserData: LocalUserData)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun update(localUserData: LocalUserData): Int

    @Transaction
    suspend fun updateAndGetUser(user: LocalUserData): LocalUserData? {
        val updateCount = update(user)

        Log.d(TAG("LocalUserDao", "updateAndGetUser"), "업데이트 결과: $updateCount")

        return if (updateCount > 0) {
            getUserById(user.id)
        } else {
            null
        }
    }

    @Query("SELECT * from LocalUserData_table WHERE id = :id")
    suspend fun getUserById(id: UUID): LocalUserData?

    @Query("DELETE from LocalUserData_table")
    suspend fun deleteAll()

    @Delete
    suspend fun deleteNote(localUserData: LocalUserData)
}

/**
 * 환율 데이터 DAO (UUID)
 */
@Dao
interface ExchangeRateDataBaseDao {

    @Query("SELECT * from exchangeRate_table")
    fun getRateData(): Flow<List<ExchangeRate>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(rateData: ExchangeRate)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(exchangeRates: List<ExchangeRate>)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun update(rateData: ExchangeRate)

    @Query("DELETE from exchangeRate_table")
    suspend fun deleteAll()

    @Delete
    suspend fun deleteRate(rateData: ExchangeRate)
}

/**
 * 통합 외화 기록 DAO (UUID)
 */
@Dao
interface CurrencyRecordDao {

    // 조회
    @Query("SELECT * FROM currency_records WHERE currency_code = :currencyCode ORDER BY date DESC")
    fun getRecordsByCurrency(currencyCode: String): Flow<List<CurrencyRecord>>

    @Query("SELECT * FROM currency_records ORDER BY date DESC")
    fun getAllRecords(): Flow<List<CurrencyRecord>>

    @Query("SELECT * FROM currency_records WHERE id = :id")
    suspend fun getRecordById(id: UUID): CurrencyRecord?

    @Query("SELECT * FROM currency_records WHERE currency_code = :currencyCode AND category_name = :categoryName ORDER BY date DESC")
    fun getRecordsByCurrencyAndCategory(currencyCode: String, categoryName: String): Flow<List<CurrencyRecord>>

    @Query("SELECT * FROM currency_records WHERE record_color = 1 ORDER BY sell_date DESC")
    fun getSoldRecords(): Flow<List<CurrencyRecord>>

    @Query("SELECT * FROM currency_records WHERE record_color = 0 ORDER BY date DESC")
    fun getUnsoldRecords(): Flow<List<CurrencyRecord>>

    @Query("SELECT DISTINCT category_name FROM currency_records WHERE currency_code = :currencyCode")
    suspend fun getCategoriesByCurrency(currencyCode: String): List<String>

    // 추가
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: CurrencyRecord)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecords(records: List<CurrencyRecord>)

    // 수정
    @Update
    suspend fun updateRecord(record: CurrencyRecord)

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

    // 삭제
    @Delete
    suspend fun deleteRecord(record: CurrencyRecord)

    @Query("DELETE FROM currency_records WHERE id = :id")
    suspend fun deleteRecordById(id: UUID)

    @Query("DELETE FROM currency_records WHERE currency_code = :currencyCode")
    suspend fun deleteRecordsByCurrency(currencyCode: String)

    @Query("DELETE FROM currency_records")
    suspend fun deleteAllRecords()

    // 통계
    @Query("SELECT COUNT(*) FROM currency_records WHERE currency_code = :currencyCode")
    suspend fun getRecordCount(currencyCode: String): Int

    @Query("SELECT SUM(CAST(money AS REAL)) FROM currency_records WHERE currency_code = :currencyCode AND record_color = 0")
    suspend fun getTotalInvestment(currencyCode: String): Float?

    @Query("SELECT SUM(CAST(sell_profit AS REAL)) FROM currency_records WHERE currency_code = :currencyCode AND record_color = 1")
    suspend fun getTotalProfit(currencyCode: String): Float?
}

/**
 * 여행모드 - 여행(Trip) & 지출(TripExpense) DAO
 */
@Dao
interface TripDao {

    // ===== Trip =====
    @Query("SELECT * FROM trips ORDER BY start_date DESC")
    fun getAllTrips(): Flow<List<Trip>>

    @Query("SELECT * FROM trips WHERE is_completed = 0 ORDER BY start_date DESC")
    fun getOngoingTrips(): Flow<List<Trip>>

    @Query("SELECT * FROM trips WHERE is_completed = 1 ORDER BY start_date DESC")
    fun getCompletedTrips(): Flow<List<Trip>>

    @Query("SELECT * FROM trips WHERE id = :id")
    suspend fun getTripById(id: UUID): Trip?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrip(trip: Trip)

    @Update
    suspend fun updateTrip(trip: Trip)

    @Query("UPDATE trips SET is_completed = :isCompleted WHERE id = :id")
    suspend fun updateTripCompleted(id: UUID, isCompleted: Boolean)

    @Delete
    suspend fun deleteTrip(trip: Trip)

    @Query("DELETE FROM trips WHERE id = :id")
    suspend fun deleteTripById(id: UUID)

    // ===== TripExpense =====
    @Query("SELECT * FROM trip_expenses WHERE trip_id = :tripId ORDER BY date DESC")
    fun getExpensesByTrip(tripId: UUID): Flow<List<TripExpense>>

    @Query("SELECT * FROM trip_expenses WHERE id = :id")
    suspend fun getExpenseById(id: UUID): TripExpense?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: TripExpense)

    @Update
    suspend fun updateExpense(expense: TripExpense)

    @Delete
    suspend fun deleteExpense(expense: TripExpense)

    @Query("DELETE FROM trip_expenses WHERE id = :id")
    suspend fun deleteExpenseById(id: UUID)

    @Query("DELETE FROM trip_expenses WHERE trip_id = :tripId")
    suspend fun deleteExpensesByTrip(tripId: UUID)

    // ===== 분석(통계) =====
    @Query("SELECT SUM(CAST(krw_amount AS REAL)) FROM trip_expenses WHERE trip_id = :tripId")
    suspend fun getTotalSpentWon(tripId: UUID): Float?

    @Query("SELECT COUNT(*) FROM trip_expenses WHERE trip_id = :tripId")
    suspend fun getExpenseCount(tripId: UUID): Int

    @Query("""
        SELECT category_name as categoryName, SUM(CAST(krw_amount AS REAL)) as totalWon
        FROM trip_expenses
        WHERE trip_id = :tripId
        GROUP BY category_name
    """)
    suspend fun getCategoryBreakdown(tripId: UUID): List<CategoryBreakdown>
}

/**
 * 분석탭 카테고리별 지출 비중 집계용 결과 매핑
 */
data class CategoryBreakdown(
    val categoryName: String,
    val totalWon: Float
)
