package com.bobodroid.myapplication.models.datamodels.repository

import com.bobodroid.myapplication.models.datamodels.roomDb.ExchangeRate
import com.bobodroid.myapplication.models.datamodels.roomDb.ExchangeRateDataBaseDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

class ExchangeRateRepository @Inject constructor(
    private val exchangeRateDataBaseDao: ExchangeRateDataBaseDao
) {

    suspend fun exchangeSave(dataRate: ExchangeRate) = exchangeRateDataBaseDao.insert(dataRate)
    suspend fun exchangeUpdate(dataRate: ExchangeRate) = exchangeRateDataBaseDao.update(dataRate)
    suspend fun exchangeRateDataDelete() = exchangeRateDataBaseDao.deleteAll()
    fun exchangeRateDataGet(): Flow<List<ExchangeRate>> = exchangeRateDataBaseDao.getRateData().flowOn(
        Dispatchers.IO).conflate()

    suspend fun saveAllExchangeRates(exchangeRates: List<ExchangeRate>) = exchangeRateDataBaseDao.insertAll(exchangeRates = exchangeRates)

}