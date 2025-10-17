package com.bobodroid.myapplication.models.datamodels.repository

import com.bobodroid.myapplication.models.datamodels.roomDb.ExchangeRate
import com.bobodroid.myapplication.models.datamodels.roomDb.ExchangeRateDataBaseDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import javax.inject.Inject

class ExchangeRateRepository @Inject constructor(
    private val exchangeRateDataBaseDao: ExchangeRateDataBaseDao
) {
    // ✅ IO 스레드에서 실행
    suspend fun exchangeSave(dataRate: ExchangeRate) = withContext(Dispatchers.IO) {
        exchangeRateDataBaseDao.insert(dataRate)
    }

    suspend fun exchangeUpdate(dataRate: ExchangeRate) = withContext(Dispatchers.IO) {
        exchangeRateDataBaseDao.update(dataRate)
    }

    suspend fun exchangeRateDataDelete() = withContext(Dispatchers.IO) {
        exchangeRateDataBaseDao.deleteAll()
    }

    suspend fun saveAllExchangeRates(exchangeRates: List<ExchangeRate>) = withContext(Dispatchers.IO) {
        exchangeRateDataBaseDao.insertAll(exchangeRates)
    }

    // ✅ Flow는 이미 flowOn으로 IO 스레드 지정됨
    fun exchangeRateDataGet(): Flow<List<ExchangeRate>> =
        exchangeRateDataBaseDao.getRateData()
            .flowOn(Dispatchers.IO)
            .conflate()
}