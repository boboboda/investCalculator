package com.bobodroid.myapplication.models.datamodels.repository

import com.bobodroid.myapplication.models.datamodels.backtest.BacktestAvailability
import com.bobodroid.myapplication.models.datamodels.backtest.BacktestHistoryItem
import com.bobodroid.myapplication.models.datamodels.backtest.BacktestRequest
import com.bobodroid.myapplication.models.datamodels.backtest.BacktestResponse
import com.bobodroid.myapplication.models.datamodels.roomDb.BacktestHistoryDao
import com.bobodroid.myapplication.models.datamodels.roomDb.BacktestHistoryEntity
import com.bobodroid.myapplication.models.datamodels.service.backtestApi.BacktestApi
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject

// ExchangeRateRepository 등 기존 리포지토리와 동일한 패턴:
// @Inject constructor + AppModule.kt의 @Provides(@Singleton)를 함께 사용 (di/AppModule.kt 패치, f20 참고)
class BacktestRepository @Inject constructor(
    private val backtestHistoryDao: BacktestHistoryDao
) {
    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    private val responseAdapter = moshi.adapter(BacktestResponse::class.java)

    suspend fun runBacktest(request: BacktestRequest): BacktestResponse {
        return BacktestApi.service.runBacktest(request)
    }

    // ✅ 선택 기간(통화 + 개월수) 고점/저점 조회 — 입력 화면의 "선택 기간 고점·저점" 카드용
    // periodMonths를 넘기면 서버가 그 기간만의 highRate/lowRate를 계산해서 돌려줌
    suspend fun getAvailability(currencyType: String, periodMonths: Int?): BacktestAvailability {
        return BacktestApi.service.getBacktestAvailability(
            currencyType = currencyType,
            periodMonths = periodMonths?.toString()
        )
    }

    suspend fun saveHistory(response: BacktestResponse) {
        val entity = BacktestHistoryEntity(
            currencyType = response.currencyType,
            periodMonths = response.periodMonths,
            investStrategy = response.investStrategy,
            requestedStartRate = response.requestedStartRate,
            principal = response.principal,
            realizedProfitWon = response.realizedProfitWon,
            profitRatePercent = response.profitRatePercent,
            buyCount = response.buyCount,
            sellCount = response.sellCount,
            responseJson = responseAdapter.toJson(response)
        )
        backtestHistoryDao.insert(entity)
    }

    fun getHistoryList(): Flow<List<BacktestHistoryItem>> {
        return backtestHistoryDao.getAllHistory().map { list ->
            list.map { entity ->
                BacktestHistoryItem(
                    id = entity.id,
                    createdAt = entity.createdAt,
                    currencyType = entity.currencyType,
                    periodMonths = entity.periodMonths,
                    investStrategy = entity.investStrategy,
                    profitRatePercent = entity.profitRatePercent,
                    realizedProfitWon = entity.realizedProfitWon
                )
            }
        }
    }

    suspend fun getHistoryDetail(id: UUID): BacktestResponse? {
        val entity = backtestHistoryDao.getHistoryById(id) ?: return null
        return responseAdapter.fromJson(entity.responseJson)
    }

    suspend fun deleteHistory(id: UUID) {
        backtestHistoryDao.delete(id)
    }
}
