package com.bobodroid.myapplication.models.datamodels

import androidx.room.*
import kotlinx.coroutines.flow.Flow


@Dao
interface DollarBuyDatabaseDao {

    @Query("SELECT * from buyDollar_table")
    fun getRecords(): Flow<List<DrBuyRecord>>

    @Query("SELECT * from buyDollar_table where id=:id")
    suspend fun getRecordById(id: String): DrBuyRecord

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(drBuyRecord: DrBuyRecord)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun update(drBuyRecord: DrBuyRecord)

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
    suspend fun getRecordById(id: String): DrSellRecord

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(drSellRecord: DrSellRecord)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun update(drSellRecord: DrSellRecord)

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
    suspend fun getRecordById(id: String): YenBuyRecord

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(yenBuyRecord: YenBuyRecord)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun update(yenBuyRecord: YenBuyRecord)

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
    suspend fun getRecordById(id: String): YenSellRecord

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(yenSellRecord: YenSellRecord)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun update(yenSellRecord: YenSellRecord)

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
    suspend fun getRecordById(id: String): WonBuyRecord

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(wonBuyRecord: WonBuyRecord)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun update(wonBuyRecord: WonBuyRecord)

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
    suspend fun getRecordById(id: String): WonSellRecord

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(wonSellRecord: WonSellRecord)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun update(wonSellRecord: WonSellRecord)

    @Query("DELETE from sellWon_table")
    suspend fun deleteAll()

    @Delete
    suspend fun deleteNote(wonSellRecord: WonSellRecord)

}



