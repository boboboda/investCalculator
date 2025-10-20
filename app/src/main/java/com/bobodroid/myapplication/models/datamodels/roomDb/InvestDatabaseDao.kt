package com.bobodroid.myapplication.models.datamodels.roomDb

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import java.util.UUID


@Dao
interface DollarBuyDatabaseDao {

    @Query("SELECT * from buyDollar_table")
    fun getRecords(): Flow<List<DrBuyRecord>>

    @Query("SELECT * from buyDollar_table where id=:id")
    suspend fun getRecordById(id: UUID): DrBuyRecord

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(drBuyRecord: DrBuyRecord)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(drBuyRecord: List<DrBuyRecord>)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun update(drBuyRecord: DrBuyRecord) : Int

    @Query("DELETE from buyDollar_table")
    suspend fun deleteAll()

    @Delete
    suspend fun deleteNote(drbuyrecord: DrBuyRecord)

}




@Dao
interface YenBuyDatabaseDao {

    @Query("SELECT * from buyYen_table")
    fun getRecords(): Flow<List<YenBuyRecord>>

    @Query("SELECT * from buyYen_table where id=:id")
    suspend fun getRecordById(id: UUID): YenBuyRecord

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(yenBuyRecord: YenBuyRecord)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(yenBuyRecord: List<YenBuyRecord>)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun update(yenBuyRecord: YenBuyRecord): Int

    @Query("DELETE from buyYen_table")
    suspend fun deleteAll()

    @Delete
    suspend fun deleteNote(yenBuyRecord: YenBuyRecord)

}

@Dao
interface LocalUserDatabaseDao {

    @Query("SELECT * from LocalUserData_table")
    fun getUserData(): Flow<LocalUserData>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(localUserData: LocalUserData)

    @Transaction
    suspend fun updateAndGetUser(user: LocalUserData): LocalUserData? {
        val updateCount = update(user)
        return if (updateCount > 0) {
            getUserById(user.id)
        } else null
    }

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun update(localUserData: LocalUserData): Int

    @Query("SELECT * from LocalUserData_table where id=:id")
    suspend fun getUserById(id: UUID): LocalUserData

    @Query("DELETE from LocalUserData_table")
    suspend fun deleteAll()
    @Delete
    suspend fun deleteNote(localUserData: LocalUserData)
}

@Dao
interface  ExchangeRateDataBaseDao {

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

@Dao
interface CurrencyRecordDao {

    // ===== 조회 =====

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

    // ===== 추가 =====

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: CurrencyRecord)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecords(records: List<CurrencyRecord>)

    // ===== 수정 =====

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

    // ===== 삭제 =====

    @Delete
    suspend fun deleteRecord(record: CurrencyRecord)

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


