package com.bobodroid.myapplication.data.repository

import android.util.Log
import com.bobodroid.myapplication.MainActivity.Companion.TAG
import com.bobodroid.myapplication.data.mapper.ExchangeRateMapper
import com.bobodroid.myapplication.domain.entity.ExchangeRateEntity
import com.bobodroid.myapplication.domain.repository.IExchangeRateRepository
import com.bobodroid.myapplication.models.datamodels.websocket.WebSocketClient
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * ExchangeRate Repository 구현체 - Entity 버전
 *
 * [변경 사항]
 * - 외부(Domain): ExchangeRateEntity 사용
 * - WebSocket만 사용 (REST API 제거)
 * - Mapper로 변환 처리
 */
@Singleton
class ExchangeRateRepositoryImpl @Inject constructor(
    private val webSocketClient: WebSocketClient
) : IExchangeRateRepository {

    // ⭐ ExchangeRateEntity 사용
    private val _latestRate = MutableStateFlow(ExchangeRateEntity.empty())
    override val latestRate: Flow<ExchangeRateEntity> = _latestRate.asStateFlow()

    private var isWebSocketSubscribed = false

    /**
     * 웹소켓으로 실시간 환율 업데이트 구독
     * onInitialData가 초기 환율도 제공하므로 REST API 불필요
     */
    override suspend fun subscribeToRateUpdates() {
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
        isWebSocketSubscribed = true
    }

    /**
     * 환율 업데이트 처리 (private)
     * ⭐ JSON String → Entity 변환 (Mapper 사용)
     */
    private suspend fun onRateUpdate(rateString: String) {
        try {
            // ⭐ Mapper가 소수점 처리 포함
            val exchangeRate = ExchangeRateMapper.fromServerJson(rateString) ?: return

            _latestRate.emit(exchangeRate)

            Log.d(TAG("ExchangeRateRepositoryImpl", "onRateUpdate"),
                "환율 업데이트 파싱 완료 (Entity): $exchangeRate")

        } catch (e: Exception) {
            Log.e(TAG("ExchangeRateRepositoryImpl", "onRateUpdate"),
                "환율 업데이트 파싱 실패: $rateString", e)
        }
    }

    /**
     * 웹소켓 연결 해제
     */
    override fun disconnect() {
        if (isWebSocketSubscribed) {
            Log.d(TAG("ExchangeRateRepositoryImpl", "disconnect"), "웹소켓 연결 해제")
            isWebSocketSubscribed = false
        }
    }
}