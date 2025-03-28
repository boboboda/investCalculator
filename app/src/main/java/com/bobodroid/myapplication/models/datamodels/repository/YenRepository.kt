package com.bobodroid.myapplication.models.datamodels.repository

import com.bobodroid.myapplication.models.datamodels.roomDb.ForeignCurrencyRecord
import com.bobodroid.myapplication.models.datamodels.roomDb.YenBuyDatabaseDao
import com.bobodroid.myapplication.models.datamodels.roomDb.YenBuyRecord
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.flowOn
import java.util.UUID
import javax.inject.Inject

class YenRepository @Inject constructor(
    private val yenBuyDatabaseDao: YenBuyDatabaseDao,
) {
    suspend fun addRecord(yenBuyRecord: YenBuyRecord) = yenBuyDatabaseDao.insert(yenBuyRecord)
    suspend fun yenBuyAddListRecord(yenBuyRecord: List<YenBuyRecord>) = yenBuyDatabaseDao.insertAll(yenBuyRecord)
    suspend fun getYenRecordId(yenBuyId: UUID): YenBuyRecord = yenBuyDatabaseDao.getRecordById(yenBuyId)
    suspend fun updateRecord(yenBuyRecord: YenBuyRecord) = yenBuyDatabaseDao.update(yenBuyRecord)
    suspend fun deleteRecord(yenBuyRecord: YenBuyRecord) = yenBuyDatabaseDao.deleteNote(yenBuyRecord)
    suspend fun deleteAllRecord(yenBuyRecord: YenBuyRecord) = yenBuyDatabaseDao.deleteAll()
    fun getAllYenBuyRecords(): Flow<List<YenBuyRecord>> = yenBuyDatabaseDao.getRecords().flowOn(
        Dispatchers.IO).conflate()
}