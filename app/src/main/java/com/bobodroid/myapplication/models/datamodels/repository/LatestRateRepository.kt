package com.bobodroid.myapplication.models.datamodels.repository

import android.util.Log
import com.bobodroid.myapplication.MainActivity.Companion.TAG
import com.bobodroid.myapplication.models.datamodels.roomDb.ExchangeRate
import com.bobodroid.myapplication.models.datamodels.service.exchangeRateApi.RateApi
import com.bobodroid.myapplication.models.datamodels.websocket.WebSocketClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.math.BigDecimal
import java.math.RoundingMode
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LatestRateRepository @Inject constructor(
    private val webSocketClient: WebSocketClient
) {

    private val _latestRate = MutableStateFlow(ExchangeRate())
    val latestRateFlow = _latestRate.asStateFlow()

    // ✅ 웹소켓 구독 시작 여부 추적
    private var isWebSocketSubscribed = false

    /**
     * ✅ REST API로 초기 환율 데이터 가져오기
     * - 앱 시작 시 한 번만 호출
     */
    suspend fun fetchInitialLatestRate() {
        try {
            Log.d(TAG("LatestRateRepository", "fetchInitialLatestRate"), "REST API로 최신 환율 요청")

            val response = RateApi.rateService.getLatestRate()

            if (response.isSuccessful) {
                response.body()?.let { data ->
                    // ✅ JPY를 100 곱해서 저장 (웹소켓과 통일)
                    val jpyRate = data.exchangeRates.jpy?.let {
                        BigDecimal(it)
                            .multiply(BigDecimal("100"))
                            .setScale(2, RoundingMode.DOWN)
                            .toString()
                    }

                    val exchangeRate = ExchangeRate(
                        id = data.id,
                        createAt = data.createAt,
                        usd = data.exchangeRates.usd,
                        jpy = jpyRate
                    )

                    _latestRate.emit(exchangeRate)

                    Log.d(TAG("LatestRateRepository", "fetchInitialLatestRate"),
                        "초기 최신 환율 로드 성공: USD=${exchangeRate.usd}, JPY=${exchangeRate.jpy}")
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
                Log.d(TAG("LatestRateRepository", "subscribeToExchangeRateUpdates"), "웹소켓 환율 최신 데이터: $latestRate")
                onRateUpdate(latestRate)

            },
            onInitialData = { initialRate ->
                Log.d(TAG("LatestRateRepository", "subscribeToExchangeRateUpdates"), "웹소켓 환율 초기화 데이터: $initialRate")
                onRateUpdate(initialRate)
            }
        )
    }

    private suspend fun onRateUpdate(rateString: String) {
        try {

            val exchangeRate = ExchangeRate.fromCustomJson(rateString) ?: return

            _latestRate.emit(exchangeRate)


            Log.d(TAG("LatestRateRepository", "onRateUpdate"), "환율 업데이트 파싱 완료: $exchangeRate")

            // 데이터가 잘 파싱되었는지 확인하는 로그
            Log.d(TAG("LatestRateRepository", "onRateUpdate"), "USD 환율: ${exchangeRate.usd}, JPY 환율: ${exchangeRate.jpy}")

        } catch (e: Exception) {
            Log.e(TAG("LatestRateRepository", "onRateUpdate"), "환율 업데이트 파싱 실패: $rateString", e)
        }
    }

    /**
     * ✅ 웹소켓 연결 해제
     * - ViewModel onCleared에서 호출 가능
     */
    fun disconnect() {
        if (isWebSocketSubscribed) {
            Log.d(TAG("LatestRateRepository", "disconnect"), "웹소켓 연결 해제")
            isWebSocketSubscribed = false
            // webSocketClient.disconnect() // 필요시 구현
        }
    }
}