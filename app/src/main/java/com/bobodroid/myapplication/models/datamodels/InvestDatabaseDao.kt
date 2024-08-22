package com.bobodroid.myapplication.models.datamodels

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import okhttp3.internal.connection.Exchange
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
interface DollarSellDatabaseDao {

    @Query("SELECT * from sellDollar_table")
    fun getRecords(): Flow<List<DrSellRecord>>

    @Query("SELECT * from sellDollar_table where id=:id")
    suspend fun getRecordById(id: UUID): DrSellRecord

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(drSellRecord: DrSellRecord)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(drSellRecord: List<DrSellRecord>)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun update(drSellRecord: DrSellRecord): Int

    @Query("DELETE from sellDollar_table")
    suspend fun deleteAll()

    @Delete
    suspend fun deleteNote(drSellRecord: DrSellRecord)

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
interface YenSellDatabaseDao {

    @Query("SELECT * from sellYen_table")
    fun getRecords(): Flow<List<YenSellRecord>>

    @Query("SELECT * from sellYen_table where id=:id")
    suspend fun getRecordById(id: UUID): YenSellRecord

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(yenSellRecord: YenSellRecord)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(yenSellRecord: List<YenSellRecord>)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun update(yenSellRecord: YenSellRecord): Int

    @Query("DELETE from sellYen_table")
    suspend fun deleteAll()

    @Delete
    suspend fun deleteNote(yenSellRecord: YenSellRecord)

}


@Dao
interface WonBuyDatabaseDao {

    @Query("SELECT * from buyWon_table")
    fun getRecords(): Flow<List<WonBuyRecord>>

    @Query("SELECT * from buyWon_table where id=:id")
    suspend fun getRecordById(id: UUID): WonBuyRecord

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(wonBuyRecord: WonBuyRecord)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(wonBuyRecord: List<WonBuyRecord>)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun update(wonBuyRecord: WonBuyRecord): Int

    @Query("DELETE from buyWon_table")
    suspend fun deleteAll()

    @Delete
    suspend fun deleteNote(wonBuyRecord: WonBuyRecord)

}

@Dao
interface WonSellDatabaseDao {

    @Query("SELECT * from sellWon_table")
    fun getRecords(): Flow<List<WonSellRecord>>

    @Query("SELECT * from sellWon_table where id=:id")
    suspend fun getRecordById(id: UUID): WonSellRecord

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(wonSellRecord: WonSellRecord)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(wonSellRecord: List<WonSellRecord>)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun update(wonSellRecord: WonSellRecord): Int

    @Query("DELETE from sellWon_table")
    suspend fun deleteAll()

    @Delete
    suspend fun deleteNote(wonSellRecord: WonSellRecord)

}


@Dao
interface LocalUserDatabaseDao {

    @Query("SELECT * from LocalUserData_table")
    fun getUserData(): Flow<LocalUserData>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(localUserData: LocalUserData)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun update(localUserData: LocalUserData)

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


