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


