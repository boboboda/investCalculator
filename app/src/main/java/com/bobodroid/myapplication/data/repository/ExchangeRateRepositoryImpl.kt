package com.bobodroid.myapplication.data.repository

import android.util.Log
import com.bobodroid.myapplication.MainActivity.Companion.TAG
import com.bobodroid.myapplication.domain.repository.IExchangeRateRepository
import com.bobodroid.myapplication.models.datamodels.roomDb.ExchangeRate
import com.bobodroid.myapplication.models.datamodels.service.exchangeRateApi.RateApi
import com.bobodroid.myapplication.models.datamodels.websocket.WebSocketClient
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

/**
 * ExchangeRate Repository 구현체
 *
 * [변경 사항]
 * 기존: LatestRateRepository
 * 신규: ExchangeRateRepositoryImpl
 *
 * Data Layer의 실제 데이터 접근 구현
 * - REST API 및 WebSocket 통신
 * - Domain의 IExchangeRateRepository 인터페이스 구현
 */
@Singleton
class ExchangeRateRepositoryImpl @Inject constructor(
    private val webSocketClient: WebSocketClient
) : IExchangeRateRepository {

    private val _latestRate = MutableStateFlow(ExchangeRate())
    override val latestRateFlow: Flow<ExchangeRate> = _latestRate.asStateFlow()

    private var isWebSocketSubscribed = false

    /**
     * REST API로 초기 환율 데이터 가져오기 (12개 통화)
     */
    override suspend fun fetchInitialRate(): Unit {
        try {
            Log.d(TAG("ExchangeRateRepositoryImpl", "fetchInitialRate"), "REST API로 최신 환율 요청")

            val response = RateApi.rateService.getLatestRate()

            if (response.isSuccessful) {
                response.body()?.let { data ->
                    // ✅ 12개 통화 전체를 JSON으로 변환
                    val exchangeRatesJson = JSONObject().apply {
                        put("USD", data.exchangeRates.usd)
                        put("JPY", data.exchangeRates.jpy)
                    }

                    val jsonString = JSONObject().apply {
                        put("id", data.id)
                        put("createAt", data.createAt)
                        put("exchangeRates", exchangeRatesJson)
                    }.toString()

                    // ✅ fromCustomJson이 needsMultiply 처리 (JPY, THB 100배)
                    val exchangeRate = ExchangeRate.fromCustomJson(jsonString)

                    if (exchangeRate != null) {
                        _latestRate.emit(exchangeRate)
                        Log.d(TAG("ExchangeRateRepositoryImpl", "fetchInitialRate"),
                            "초기 최신 환율 로드 성공 (12개 통화): $exchangeRate")
                    }
                }
            } else {
                Log.e(TAG("ExchangeRateRepositoryImpl", "fetchInitialRate"),
                    "초기 최신 환율 로드 실패: ${response.code()}")
            }
        } catch (e: Exception) {
            Log.e(TAG("ExchangeRateRepositoryImpl", "fetchInitialRate"),
                "초기 최신 환율 로드 에러: ${e.message}", e)
        }
    }

    /**
     * 웹소켓으로 실시간 환율 업데이트 구독
     */
    override suspend fun subscribeToRateUpdates(): Unit {
        webSocketClient.recentRateWebReceiveData(
            onInsert = { latestRate ->
                Log.d(TAG("ExchangeRateRepositoryImpl", "subscribeToRateUpdates"),
                    "웹소켓 환율 최신 데이터 수신")
                onRateUpdate(latestRate)
            },
            onInitialData = { initialRate ->
                Log.d(TAG("ExchangeRateRepositoryImpl", "subscribeToRateUpdates"),
                    "웹소켓 환율 초기화 데이터 수신")
                onRateUpdate(initialRate)
            }
        )
    }

    /**
     * 환율 업데이트 처리 (private)
     */
    private suspend fun onRateUpdate(rateString: String) {
        try {
            val exchangeRate = ExchangeRate.fromCustomJson(rateString) ?: return

            _latestRate.emit(exchangeRate)

            Log.d(TAG("ExchangeRateRepositoryImpl", "onRateUpdate"),
                "환율 업데이트 파싱 완료: $exchangeRate")

        } catch (e: Exception) {
            Log.e(TAG("ExchangeRateRepositoryImpl", "onRateUpdate"),
                "환율 업데이트 파싱 실패: $rateString", e)
        }
    }

    /**
     * 웹소켓 연결 해제
     */
    override fun disconnect(): Unit {
        if (isWebSocketSubscribed) {
            Log.d(TAG("ExchangeRateRepositoryImpl", "disconnect"), "웹소켓 연결 해제")
            isWebSocketSubscribed = false
        }
    }
}