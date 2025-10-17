package com.bobodroid.myapplication.models.datamodels.repository

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.bobodroid.myapplication.MainActivity.Companion.TAG
import com.bobodroid.myapplication.models.datamodels.roomDb.ExchangeRate
import com.bobodroid.myapplication.models.datamodels.roomDb.LocalUserData
import com.bobodroid.myapplication.models.datamodels.service.exchangeRateApi.RateApi
import com.bobodroid.myapplication.models.datamodels.websocket.WebSocketClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject



class LatestRateRepository @Inject constructor(
    private val webSocketClient: WebSocketClient
) {

    private val _latestRate = MutableStateFlow(ExchangeRate())

    val latestRateFlow = _latestRate.asStateFlow()



    suspend fun fetchInitialLatestRate() {
        try {
            Log.d(TAG("LatestRateRepository", "fetchInitialLatestRate"), "REST API로 최신 환율 요청")

            val response = RateApi.rateService.getLatestRate()

            if (response.isSuccessful) {
                response.body()?.let { data ->
                    val exchangeRate = ExchangeRate(
                        id = data.id,
                        createAt = data.createAt,
                        usd = data.exchangeRates.usd,
                        jpy = data.exchangeRates.jpy
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

}