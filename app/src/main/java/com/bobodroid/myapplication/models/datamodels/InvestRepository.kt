package com.bobodroid.myapplication.models.datamodels

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.flowOn
import java.util.UUID
import javax.inject.Inject

class InvestRepository @Inject constructor(
    private val dollarBuyDatabaseDao: DollarBuyDatabaseDao,
    private val dollarSellDatabaseDao: DollarSellDatabaseDao,
    private val yenBuyDatabaseDao: YenBuyDatabaseDao,
    private val yenSellDatabaseDao: YenSellDatabaseDao,
    private val wonBuyDatabaseDao: WonBuyDatabaseDao,
    private val wonSellDatabaseDao: WonSellDatabaseDao,
    private val localUserDatabaseDao: LocalUserDatabaseDao) {


    // 달러
    suspend fun addRecord(drbuyrecord: DrBuyRecord) = dollarBuyDatabaseDao.insert(drbuyrecord)
    suspend fun drBuyAddListRecord(drbuyrecord: List<DrBuyRecord>) = dollarBuyDatabaseDao.insertAll(drbuyrecord)
    suspend fun getRecordId(drBuyId: UUID): DrBuyRecord = dollarBuyDatabaseDao.getRecordById(drBuyId)
    suspend fun updateRecord(drbuyrecord: DrBuyRecord) = dollarBuyDatabaseDao.update(drbuyrecord)
    suspend fun deleteRecord(drbuyrecord: DrBuyRecord) = dollarBuyDatabaseDao.deleteNote(drbuyrecord)
    suspend fun deleteAllRecord(drbuyrecord: DrBuyRecord) = dollarBuyDatabaseDao.deleteAll()
    fun getAllBuyRecords(): Flow<List<DrBuyRecord>> = dollarBuyDatabaseDao.getRecords().flowOn(Dispatchers.IO).conflate()

    suspend fun addRecord(drSellRecord: DrSellRecord) = dollarSellDatabaseDao.insert(drSellRecord)
    suspend fun drSellAddListRecord(drSellRecord: List<DrSellRecord>) = dollarSellDatabaseDao.insertAll(drSellRecord)

    suspend fun getSellRecordId(drSellId: UUID): DrSellRecord = dollarSellDatabaseDao.getRecordById(drSellId)
    suspend fun updateRecord(drSellRecord: DrSellRecord) = dollarSellDatabaseDao.update(drSellRecord)
    suspend fun deleteRecord(drSellRecord: DrSellRecord) = dollarSellDatabaseDao.deleteNote(drSellRecord)
    suspend fun deleteAllRecord(drSellRecord: DrSellRecord) = dollarSellDatabaseDao.deleteAll()
    fun getAllSellRecords(): Flow<List<DrSellRecord>> = dollarSellDatabaseDao.getRecords().flowOn(Dispatchers.IO).conflate()

    //엔화
    suspend fun addRecord(yenBuyRecord: YenBuyRecord) = yenBuyDatabaseDao.insert(yenBuyRecord)
    suspend fun yenBuyAddListRecord(yenBuyRecord: List<YenBuyRecord>) = yenBuyDatabaseDao.insertAll(yenBuyRecord)
    suspend fun getYenRecordId(yenBuyId: UUID): YenBuyRecord = yenBuyDatabaseDao.getRecordById(yenBuyId)
    suspend fun updateRecord(yenBuyRecord: YenBuyRecord) = yenBuyDatabaseDao.update(yenBuyRecord)
    suspend fun deleteRecord(yenBuyRecord: YenBuyRecord) = yenBuyDatabaseDao.deleteNote(yenBuyRecord)
    suspend fun deleteAllRecord(yenBuyRecord: YenBuyRecord) = yenBuyDatabaseDao.deleteAll()
    fun getAllYenBuyRecords(): Flow<List<YenBuyRecord>> = yenBuyDatabaseDao.getRecords().flowOn(Dispatchers.IO).conflate()

    suspend fun addRecord(yenSellRecord: YenSellRecord) = yenSellDatabaseDao.insert(yenSellRecord)

    suspend fun getYenSellRecordId(yenSellId: UUID): YenSellRecord = yenSellDatabaseDao.getRecordById(yenSellId)
    suspend fun yenSellAddListRecord(yenSellRecord: List<YenSellRecord>) = yenSellDatabaseDao.insertAll(yenSellRecord)
    suspend fun updateRecord(yenSellRecord: YenSellRecord) = yenSellDatabaseDao.update(yenSellRecord)
    suspend fun deleteRecord(yenSellRecord: YenSellRecord) = yenSellDatabaseDao.deleteNote(yenSellRecord)
    suspend fun deleteAllRecord(yenSellRecord: YenSellRecord) = yenSellDatabaseDao.deleteAll()
    fun getAllYenSellRecords(): Flow<List<YenSellRecord>> = yenSellDatabaseDao.getRecords().flowOn(Dispatchers.IO).conflate()


    //원화
    suspend fun addRecord(wonBuyRecord: WonBuyRecord) = wonBuyDatabaseDao.insert(wonBuyRecord)

    suspend fun wonBuyAddListRecord(wonBuyRecord: List<WonBuyRecord>) = wonBuyDatabaseDao.insertAll(wonBuyRecord)

    suspend fun getWonRecordId(yenBuyId: UUID): WonBuyRecord = wonBuyDatabaseDao.getRecordById(yenBuyId)

    suspend fun getWonSellRecordId(yenSellId: UUID): WonSellRecord = wonSellDatabaseDao.getRecordById(yenSellId)
    suspend fun updateRecord(wonBuyRecord: WonBuyRecord) = wonBuyDatabaseDao.update(wonBuyRecord)
    suspend fun deleteRecord(wonBuyRecord: WonBuyRecord) = wonBuyDatabaseDao.deleteNote(wonBuyRecord)
    suspend fun deleteAllRecord(wonBuyRecord: WonBuyRecord) = wonBuyDatabaseDao.deleteAll()
    fun getAllWonBuyRecords(): Flow<List<WonBuyRecord>> = wonBuyDatabaseDao.getRecords().flowOn(Dispatchers.IO).conflate()

    suspend fun addRecord(wonSellRecord: WonSellRecord) = wonSellDatabaseDao.insert(wonSellRecord)



    suspend fun wonSellAddListRecord(wonSellRecord: List<WonSellRecord>) = wonSellDatabaseDao.insertAll(wonSellRecord)
    suspend fun updateRecord(wonSellRecord: WonSellRecord) = wonSellDatabaseDao.update(wonSellRecord)
    suspend fun deleteRecord(wonSellRecord: WonSellRecord) = wonSellDatabaseDao.deleteNote(wonSellRecord)
    suspend fun deleteAllRecord(wonSellRecord: WonSellRecord) = wonSellDatabaseDao.deleteAll()
    fun getAllWonSellRecords(): Flow<List<WonSellRecord>> = wonSellDatabaseDao.getRecords().flowOn(Dispatchers.IO).conflate()


    //로컬 유저 생성
    suspend fun localUserAdd(localUserData: LocalUserData) = localUserDatabaseDao.insert(localUserData)
    suspend fun localUserUpdate(localUserData: LocalUserData) = localUserDatabaseDao.update(localUserData)
    suspend fun localUserDataDelete() = localUserDatabaseDao.deleteAll()
    fun localUserDataGet(): Flow<LocalUserData> = localUserDatabaseDao.getUserData().flowOn(Dispatchers.IO).conflate()
}