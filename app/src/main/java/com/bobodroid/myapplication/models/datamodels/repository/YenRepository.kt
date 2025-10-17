package com.bobodroid.myapplication.models.datamodels.repository

import com.bobodroid.myapplication.models.datamodels.roomDb.YenBuyDatabaseDao
import com.bobodroid.myapplication.models.datamodels.roomDb.YenBuyRecord
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject

class YenRepository @Inject constructor(
    private val yenBuyDatabaseDao: YenBuyDatabaseDao,
) {
    // ✅ IO 스레드에서 실행
    suspend fun addRecord(yenBuyRecord: YenBuyRecord) = withContext(Dispatchers.IO) {
        yenBuyDatabaseDao.insert(yenBuyRecord)
    }

    suspend fun yenBuyAddListRecord(yenBuyRecord: List<YenBuyRecord>) = withContext(Dispatchers.IO) {
        yenBuyDatabaseDao.insertAll(yenBuyRecord)
    }

    suspend fun getYenRecordId(yenBuyId: UUID): YenBuyRecord = withContext(Dispatchers.IO) {
        yenBuyDatabaseDao.getRecordById(yenBuyId)
    }

    suspend fun updateRecord(yenBuyRecord: YenBuyRecord) = withContext(Dispatchers.IO) {
        yenBuyDatabaseDao.update(yenBuyRecord)
    }

    suspend fun deleteRecord(yenBuyRecord: YenBuyRecord) = withContext(Dispatchers.IO) {
        yenBuyDatabaseDao.deleteNote(yenBuyRecord)
    }

    suspend fun deleteAllRecord(yenBuyRecord: YenBuyRecord) = withContext(Dispatchers.IO) {
        yenBuyDatabaseDao.deleteAll()
    }

    // ✅ Flow는 이미 flowOn으로 IO 스레드 지정됨
    fun getAllYenBuyRecords(): Flow<List<YenBuyRecord>> =
        yenBuyDatabaseDao.getRecords()
            .flowOn(Dispatchers.IO)
            .conflate()
}