package com.bobodroid.myapplication.models.datamodels.repository

import com.bobodroid.myapplication.models.datamodels.roomDb.DollarBuyDatabaseDao
import com.bobodroid.myapplication.models.datamodels.roomDb.DrBuyRecord
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject

class DollarRepository @Inject constructor(
    private val dollarBuyDatabaseDao: DollarBuyDatabaseDao,
) {
    // ✅ IO 스레드에서 실행
    suspend fun addRecord(drbuyrecord: DrBuyRecord) = withContext(Dispatchers.IO) {
        dollarBuyDatabaseDao.insert(drbuyrecord)
    }

    suspend fun drBuyAddListRecord(drbuyrecord: List<DrBuyRecord>) = withContext(Dispatchers.IO) {
        dollarBuyDatabaseDao.insertAll(drbuyrecord)
    }

    suspend fun getRecordId(drBuyId: UUID): DrBuyRecord = withContext(Dispatchers.IO) {
        dollarBuyDatabaseDao.getRecordById(drBuyId)
    }

    suspend fun updateRecord(drbuyrecord: DrBuyRecord) = withContext(Dispatchers.IO) {
        dollarBuyDatabaseDao.update(drbuyrecord)
    }

    suspend fun deleteRecord(drbuyrecord: DrBuyRecord) = withContext(Dispatchers.IO) {
        dollarBuyDatabaseDao.deleteNote(drbuyrecord)
    }

    suspend fun deleteAllRecord(drBuyRecord: DrBuyRecord) = withContext(Dispatchers.IO) {
        dollarBuyDatabaseDao.deleteAll()
    }

    // ✅ Flow는 이미 flowOn으로 IO 스레드 지정됨
    fun getAllBuyRecords(): Flow<List<DrBuyRecord>> =
        dollarBuyDatabaseDao.getRecords()
            .flowOn(Dispatchers.IO)
            .conflate()
}