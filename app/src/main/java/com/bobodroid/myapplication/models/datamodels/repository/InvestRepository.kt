package com.bobodroid.myapplication.models.datamodels.repository

import com.bobodroid.myapplication.models.datamodels.roomDb.DrBuyRecord
import com.bobodroid.myapplication.models.datamodels.roomDb.YenBuyRecord
import kotlinx.coroutines.flow.Flow
import java.util.UUID
import javax.inject.Inject

class InvestRepository @Inject constructor(
    private val dollarRepository: DollarRepository,
    private val yenRepository: YenRepository,
) {
    // Dollar Buy 관련 메서드들
    suspend fun addDollarBuyRecord(record: DrBuyRecord) = dollarRepository.addRecord(record)
    suspend fun addDollarBuyRecords(records: List<DrBuyRecord>) = dollarRepository.drBuyAddListRecord(records)
    suspend fun updateDollarBuyRecord(record: DrBuyRecord) = dollarRepository.updateRecord(record)
    suspend fun deleteDollarBuyRecord(record: DrBuyRecord) = dollarRepository.deleteRecord(record)
    suspend fun deleteAllDollarBuyRecords() = dollarRepository.deleteAllRecord(DrBuyRecord())
    suspend fun getDollarBuyRecordById(id: UUID): DrBuyRecord = dollarRepository.getRecordId(id)
    fun getAllDollarBuyRecords(): Flow<List<DrBuyRecord>> = dollarRepository.getAllBuyRecords()

    // Yen Buy 관련 메서드들
    suspend fun addYenBuyRecord(record: YenBuyRecord) = yenRepository.addRecord(record)
    suspend fun addYenBuyRecords(records: List<YenBuyRecord>) = yenRepository.yenBuyAddListRecord(records)
    suspend fun updateYenBuyRecord(record: YenBuyRecord) = yenRepository.updateRecord(record)
    suspend fun deleteYenBuyRecord(record: YenBuyRecord) = yenRepository.deleteRecord(record)
    suspend fun deleteAllYenBuyRecords() = yenRepository.deleteAllRecord(YenBuyRecord())
    suspend fun getYenBuyRecordById(id: UUID): YenBuyRecord = yenRepository.getYenRecordId(id)
    fun getAllYenBuyRecords(): Flow<List<YenBuyRecord>> = yenRepository.getAllYenBuyRecords()
}

