package com.bobodroid.myapplication.models.datamodels.repository

import android.util.Log
import com.bobodroid.myapplication.MainActivity.Companion.TAG
import com.bobodroid.myapplication.models.datamodels.roomDb.ExchangeRate
import com.bobodroid.myapplication.models.datamodels.service.exchangeRateApi.RateApi
import com.bobodroid.myapplication.models.datamodels.websocket.WebSocketClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LatestRateRepository @Inject constructor(
    private val webSocketClient: WebSocketClient
) {

    private val _latestRate = MutableStateFlow(ExchangeRate())
    val latestRateFlow = _latestRate.asStateFlow()

    private var isWebSocketSubscribed = false

    /**
     * ✅ REST API로 초기 환율 데이터 가져오기
     * - 서버에서 받은 JSON을 ExchangeRate.fromCustomJson으로 변환 (JPY * 100 자동 처리됨)
     */
    suspend fun fetchInitialLatestRate() {
        try {
            Log.d(TAG("LatestRateRepository", "fetchInitialLatestRate"), "REST API로 최신 환율 요청")

            val response = RateApi.rateService.getLatestRate()

            if (response.isSuccessful) {
                response.body()?.let { data ->
                    // ✅ 서버 응답을 JSON 문자열로 변환
                    val jsonString = """
                        {
                            "id": "${data.id}",
                            "createAt": "${data.createAt}",
                            "exchangeRates": {
                                "USD": "${data.exchangeRates.usd}",
                                "JPY": "${data.exchangeRates.jpy}"
                            }
                        }
                    """.trimIndent()

                    // ✅ fromCustomJson이 JPY * 100 처리를 자동으로 수행
                    val exchangeRate = ExchangeRate.fromCustomJson(jsonString)

                    if (exchangeRate != null) {
                        _latestRate.emit(exchangeRate)
                        Log.d(TAG("LatestRateRepository", "fetchInitialLatestRate"),
                            "초기 최신 환율 로드 성공: USD=${exchangeRate.usd}, JPY=${exchangeRate.jpy}")
                    }
                }
            } else {
                Log.e(TAG("LatestRateRepository", "fetchInitialLatestRate"),
                    "초기 최신 환율 로드 실패: ${response.code()}")
            }
        } catch (e: Exception) {
            Log.e(TAG("LatestRateRepository", "fetchInitialLatestRate"),
                "초기 최신 환율 로드 에러: ${e.message}", e)
        }
    }

    suspend fun subscribeToExchangeRateUpdates() {
        webSocketClient.recentRateWebReceiveData(
            onInsert = { latestRate ->
                Log.d(TAG("LatestRateRepository", "subscribeToExchangeRateUpdates"),
                    "웹소켓 환율 최신 데이터: $latestRate")
                onRateUpdate(latestRate)
            },
            onInitialData = { initialRate ->
                Log.d(TAG("LatestRateRepository", "subscribeToExchangeRateUpdates"),
                    "웹소켓 환율 초기화 데이터: $initialRate")
                onRateUpdate(initialRate)
            }
        )
    }

    private suspend fun onRateUpdate(rateString: String) {
        try {
            // ✅ fromCustomJson이 JPY * 100 처리 포함
            val exchangeRate = ExchangeRate.fromCustomJson(rateString) ?: return

            _latestRate.emit(exchangeRate)

            Log.d(TAG("LatestRateRepository", "onRateUpdate"),
                "환율 업데이트 파싱 완료: $exchangeRate")
            Log.d(TAG("LatestRateRepository", "onRateUpdate"),
                "USD 환율: ${exchangeRate.usd}, JPY 환율: ${exchangeRate.jpy}")

        } catch (e: Exception) {
            Log.e(TAG("LatestRateRepository", "onRateUpdate"),
                "환율 업데이트 파싱 실패: $rateString", e)
        }
    }

    fun disconnect() {
        if (isWebSocketSubscribed) {
            Log.d(TAG("LatestRateRepository", "disconnect"), "웹소켓 연결 해제")
            isWebSocketSubscribed = false
        }
    }
}