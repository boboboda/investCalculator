package com.bobodroid.myapplication.models.datamodels.repository

import com.bobodroid.myapplication.models.datamodels.roomDb.DollarBuyDatabaseDao
import com.bobodroid.myapplication.models.datamodels.roomDb.DrBuyRecord
import com.bobodroid.myapplication.models.datamodels.roomDb.ForeignCurrencyRecord
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.flowOn
import java.util.UUID
import javax.inject.Inject

class DollarRepository @Inject constructor(
    private val dollarBuyDatabaseDao: DollarBuyDatabaseDao,
) {
    suspend fun addRecord(drbuyrecord: DrBuyRecord) = dollarBuyDatabaseDao.insert(drbuyrecord)
    suspend fun drBuyAddListRecord(drbuyrecord: List<DrBuyRecord>) = dollarBuyDatabaseDao.insertAll(drbuyrecord)
    suspend fun getRecordId(drBuyId: UUID): DrBuyRecord = dollarBuyDatabaseDao.getRecordById(drBuyId)
    suspend fun updateRecord(drbuyrecord: DrBuyRecord) = dollarBuyDatabaseDao.update(drbuyrecord)
    suspend fun deleteRecord(drbuyrecord: DrBuyRecord) = dollarBuyDatabaseDao.deleteNote(drbuyrecord)
    suspend fun deleteAllRecord(drBuyRecord: DrBuyRecord) = dollarBuyDatabaseDao.deleteAll()
    fun getAllBuyRecords(): Flow<List<DrBuyRecord>> = dollarBuyDatabaseDao.getRecords().flowOn(
        Dispatchers.IO).conflate()

}