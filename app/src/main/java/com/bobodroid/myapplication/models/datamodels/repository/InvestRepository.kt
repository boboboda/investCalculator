package com.bobodroid.myapplication.models.datamodels.repository

import com.bobodroid.myapplication.models.datamodels.firebase.ExchangeRate
import com.bobodroid.myapplication.models.datamodels.roomDb.DrBuyRecord
import com.bobodroid.myapplication.models.datamodels.roomDb.DrSellRecord
import com.bobodroid.myapplication.models.datamodels.roomDb.LocalUserData
import com.bobodroid.myapplication.models.datamodels.roomDb.WonBuyRecord
import com.bobodroid.myapplication.models.datamodels.roomDb.WonSellRecord
import com.bobodroid.myapplication.models.datamodels.roomDb.YenBuyRecord
import com.bobodroid.myapplication.models.datamodels.roomDb.YenSellRecord
import kotlinx.coroutines.flow.Flow
import java.util.UUID
import javax.inject.Inject

class InvestRepository @Inject constructor(
    private val dollarRepository: DollarRepository,
    private val yenRepository: YenRepository,
    private val wonRepository: WonRepository,
) {
    // Dollar Buy 관련 메서드들
    suspend fun addDollarBuyRecord(record: DrBuyRecord) = dollarRepository.addRecord(record)
    suspend fun addDollarBuyRecords(records: List<DrBuyRecord>) = dollarRepository.drBuyAddListRecord(records)
    suspend fun updateDollarBuyRecord(record: DrBuyRecord) = dollarRepository.updateRecord(record)
    suspend fun deleteDollarBuyRecord(record: DrBuyRecord) = dollarRepository.deleteRecord(record)
    suspend fun deleteAllDollarBuyRecords() = dollarRepository.deleteAllRecord(DrBuyRecord())
    suspend fun getDollarBuyRecordById(id: UUID): DrBuyRecord = dollarRepository.getRecordId(id)
    fun getAllDollarBuyRecords(): Flow<List<DrBuyRecord>> = dollarRepository.getAllBuyRecords()

    // Dollar Sell 관련 메서드들
    suspend fun addDollarSellRecord(record: DrSellRecord) = dollarRepository.addRecord(record)
    suspend fun addDollarSellRecords(records: List<DrSellRecord>) = dollarRepository.drSellAddListRecord(records)
    suspend fun updateDollarSellRecord(record: DrSellRecord) = dollarRepository.updateRecord(record)
    suspend fun deleteDollarSellRecord(record: DrSellRecord) = dollarRepository.deleteRecord(record)
    suspend fun deleteAllDollarSellRecords() = dollarRepository.deleteAllRecord(DrSellRecord())
    suspend fun getDollarSellRecordById(id: UUID): DrSellRecord = dollarRepository.getSellRecordId(id)
    fun getAllDollarSellRecords(): Flow<List<DrSellRecord>> = dollarRepository.getAllSellRecords()

    // Yen Buy 관련 메서드들
    suspend fun addYenBuyRecord(record: YenBuyRecord) = yenRepository.addRecord(record)
    suspend fun addYenBuyRecords(records: List<YenBuyRecord>) = yenRepository.yenBuyAddListRecord(records)
    suspend fun updateYenBuyRecord(record: YenBuyRecord) = yenRepository.updateRecord(record)
    suspend fun deleteYenBuyRecord(record: YenBuyRecord) = yenRepository.deleteRecord(record)
    suspend fun deleteAllYenBuyRecords() = yenRepository.deleteAllRecord(YenBuyRecord())
    suspend fun getYenBuyRecordById(id: UUID): YenBuyRecord = yenRepository.getYenRecordId(id)
    fun getAllYenBuyRecords(): Flow<List<YenBuyRecord>> = yenRepository.getAllYenBuyRecords()

    // Yen Sell 관련 메서드들
    suspend fun addYenSellRecord(record: YenSellRecord) = yenRepository.addRecord(record)
    suspend fun addYenSellRecords(records: List<YenSellRecord>) = yenRepository.yenSellAddListRecord(records)
    suspend fun updateYenSellRecord(record: YenSellRecord) = yenRepository.updateRecord(record)
    suspend fun deleteYenSellRecord(record: YenSellRecord) = yenRepository.deleteRecord(record)
    suspend fun deleteAllYenSellRecords() = yenRepository.deleteAllRecord(YenSellRecord())
    suspend fun getYenSellRecordById(id: UUID): YenSellRecord = yenRepository.getYenSellRecordId(id)
    fun getAllYenSellRecords(): Flow<List<YenSellRecord>> = yenRepository.getAllYenSellRecords()

    // Won Buy 관련 메서드들
    suspend fun addWonBuyRecord(record: WonBuyRecord) = wonRepository.addRecord(record)
    suspend fun addWonBuyRecords(records: List<WonBuyRecord>) = wonRepository.wonBuyAddListRecord(records)
    suspend fun updateWonBuyRecord(record: WonBuyRecord) = wonRepository.updateRecord(record)
    suspend fun deleteWonBuyRecord(record: WonBuyRecord) = wonRepository.deleteRecord(record)
    suspend fun deleteAllWonBuyRecords() = wonRepository.deleteAllRecord(WonBuyRecord())
    suspend fun getWonBuyRecordById(id: UUID): WonBuyRecord = wonRepository.getWonRecordId(id)
    fun getAllWonBuyRecords(): Flow<List<WonBuyRecord>> = wonRepository.getAllWonBuyRecords()

    // Won Sell 관련 메서드들
    suspend fun addWonSellRecord(record: WonSellRecord) = wonRepository.addRecord(record)
    suspend fun addWonSellRecords(records: List<WonSellRecord>) = wonRepository.wonSellAddListRecord(records)
    suspend fun updateWonSellRecord(record: WonSellRecord) = wonRepository.updateRecord(record)
    suspend fun deleteWonSellRecord(record: WonSellRecord) = wonRepository.deleteRecord(record)
    suspend fun deleteAllWonSellRecords() = wonRepository.deleteAllRecord(WonSellRecord())
    suspend fun getWonSellRecordById(id: UUID): WonSellRecord = wonRepository.getWonSellRecordId(id)
    fun getAllWonSellRecords(): Flow<List<WonSellRecord>> = wonRepository.getAllWonSellRecords()

}

