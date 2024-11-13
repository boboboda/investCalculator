package com.bobodroid.myapplication.models.datamodels.repository

import com.bobodroid.myapplication.models.datamodels.roomDb.WonBuyDatabaseDao
import com.bobodroid.myapplication.models.datamodels.roomDb.WonBuyRecord
import com.bobodroid.myapplication.models.datamodels.roomDb.WonSellDatabaseDao
import com.bobodroid.myapplication.models.datamodels.roomDb.WonSellRecord
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.flowOn
import java.util.UUID
import javax.inject.Inject

class WonRepository @Inject constructor(
    private val wonBuyDatabaseDao: WonBuyDatabaseDao,
    private val wonSellDatabaseDao: WonSellDatabaseDao
) {

    suspend fun addRecord(wonBuyRecord: WonBuyRecord) = wonBuyDatabaseDao.insert(wonBuyRecord)

    suspend fun wonBuyAddListRecord(wonBuyRecord: List<WonBuyRecord>) = wonBuyDatabaseDao.insertAll(wonBuyRecord)

    suspend fun getWonRecordId(yenBuyId: UUID): WonBuyRecord = wonBuyDatabaseDao.getRecordById(yenBuyId)

    suspend fun getWonSellRecordId(yenSellId: UUID): WonSellRecord = wonSellDatabaseDao.getRecordById(yenSellId)
    suspend fun updateRecord(wonBuyRecord: WonBuyRecord) = wonBuyDatabaseDao.update(wonBuyRecord)
    suspend fun deleteRecord(wonBuyRecord: WonBuyRecord) = wonBuyDatabaseDao.deleteNote(wonBuyRecord)
    suspend fun deleteAllRecord(wonBuyRecord: WonBuyRecord) = wonBuyDatabaseDao.deleteAll()
    fun getAllWonBuyRecords(): Flow<List<WonBuyRecord>> = wonBuyDatabaseDao.getRecords().flowOn(
        Dispatchers.IO).conflate()

    suspend fun addRecord(wonSellRecord: WonSellRecord) = wonSellDatabaseDao.insert(wonSellRecord)



    suspend fun wonSellAddListRecord(wonSellRecord: List<WonSellRecord>) = wonSellDatabaseDao.insertAll(wonSellRecord)
    suspend fun updateRecord(wonSellRecord: WonSellRecord) = wonSellDatabaseDao.update(wonSellRecord)
    suspend fun deleteRecord(wonSellRecord: WonSellRecord) = wonSellDatabaseDao.deleteNote(wonSellRecord)
    suspend fun deleteAllRecord(wonSellRecord: WonSellRecord) = wonSellDatabaseDao.deleteAll()
    fun getAllWonSellRecords(): Flow<List<WonSellRecord>> = wonSellDatabaseDao.getRecords().flowOn(
        Dispatchers.IO).conflate()
}