package com.bobodroid.myapplication.models.datamodels.repository

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.bobodroid.myapplication.MainActivity.Companion.TAG
import com.bobodroid.myapplication.models.datamodels.roomDb.ExchangeRate
import com.bobodroid.myapplication.models.datamodels.roomDb.LocalUserData
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
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _latestRate = MutableStateFlow(ExchangeRate())

    val latestRateFlow = _latestRate.asStateFlow()


    suspend fun subscribeToExchangeRateUpdates() {
        webSocketClient.recentRateWebReceiveData(
            onInsert = { latestRate ->
                Log.d(TAG("AllViewModel", "subscribeToExchangeRateUpdates"), "웹소켓 환율 최신 데이터: $latestRate")
                onRateUpdate(latestRate)

            },
            onInitialData = { initialRate ->
                Log.d(TAG("AllViewModel", "subscribeToExchangeRateUpdates"), "웹소켓 환율 초기화 데이터: $initialRate")
                onRateUpdate(initialRate)
            }
        )
    }

    private suspend fun onRateUpdate(rateString: String) {
        try {

            val exchangeRate = ExchangeRate.fromCustomJson(rateString) ?: return

            _latestRate.emit(exchangeRate)


            Log.d(TAG("AllViewModel", "onRateUpdate"), "환율 업데이트 파싱 완료: $exchangeRate")

            // 데이터가 잘 파싱되었는지 확인하는 로그
            Log.d(TAG("AllViewModel", "onRateUpdate"), "USD 환율: ${exchangeRate.usd}, JPY 환율: ${exchangeRate.jpy}")

        } catch (e: Exception) {
            Log.e(TAG("AllViewModel", "onRateUpdate"), "환율 업데이트 파싱 실패: $rateString", e)
        }
    }

}