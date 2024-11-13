package com.bobodroid.myapplication.models.datamodels.repository

import com.bobodroid.myapplication.models.datamodels.roomDb.DollarBuyDatabaseDao
import com.bobodroid.myapplication.models.datamodels.roomDb.DollarSellDatabaseDao
import com.bobodroid.myapplication.models.datamodels.roomDb.DrBuyRecord
import com.bobodroid.myapplication.models.datamodels.roomDb.DrSellRecord
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.flowOn
import java.util.UUID
import javax.inject.Inject

class DollarRepository @Inject constructor(
    private val dollarBuyDatabaseDao: DollarBuyDatabaseDao,
    private val dollarSellDatabaseDao: DollarSellDatabaseDao
) {

    suspend fun addRecord(drbuyrecord: DrBuyRecord) = dollarBuyDatabaseDao.insert(drbuyrecord)
    suspend fun drBuyAddListRecord(drbuyrecord: List<DrBuyRecord>) = dollarBuyDatabaseDao.insertAll(drbuyrecord)
    suspend fun getRecordId(drBuyId: UUID): DrBuyRecord = dollarBuyDatabaseDao.getRecordById(drBuyId)
    suspend fun updateRecord(drbuyrecord: DrBuyRecord) = dollarBuyDatabaseDao.update(drbuyrecord)
    suspend fun deleteRecord(drbuyrecord: DrBuyRecord) = dollarBuyDatabaseDao.deleteNote(drbuyrecord)
    suspend fun deleteAllRecord(drbuyrecord: DrBuyRecord) = dollarBuyDatabaseDao.deleteAll()
    fun getAllBuyRecords(): Flow<List<DrBuyRecord>> = dollarBuyDatabaseDao.getRecords().flowOn(
        Dispatchers.IO).conflate()


    suspend fun addRecord(drSellRecord: DrSellRecord) = dollarSellDatabaseDao.insert(drSellRecord)
    suspend fun drSellAddListRecord(drSellRecord: List<DrSellRecord>) = dollarSellDatabaseDao.insertAll(drSellRecord)

    suspend fun getSellRecordId(drSellId: UUID): DrSellRecord = dollarSellDatabaseDao.getRecordById(drSellId)
    suspend fun updateRecord(drSellRecord: DrSellRecord) = dollarSellDatabaseDao.update(drSellRecord)
    suspend fun deleteRecord(drSellRecord: DrSellRecord) = dollarSellDatabaseDao.deleteNote(drSellRecord)
    suspend fun deleteAllRecord(drSellRecord: DrSellRecord) = dollarSellDatabaseDao.deleteAll()
    fun getAllSellRecords(): Flow<List<DrSellRecord>> = dollarSellDatabaseDao.getRecords().flowOn(Dispatchers.IO).conflate()


}