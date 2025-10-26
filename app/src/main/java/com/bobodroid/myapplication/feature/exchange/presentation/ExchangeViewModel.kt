package com.bobodroid.myapplication.feature.exchange.presentation

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bobodroid.myapplication.MainActivity.Companion.TAG
import com.bobodroid.myapplication.domain.entity.ExchangeRateEntity
import com.bobodroid.myapplication.domain.repository.IExchangeRateRepository
import com.bobodroid.myapplication.models.datamodels.service.exchangeRateApi.RateApi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Exchange ViewModel
 *
 * 환율 조회 및 표시를 담당
 * - 최신 환율 조회
 * - 실시간 환율 구독 (WebSocket)
 * - 전일 대비 변동률 조회
 */
@HiltViewModel
class ExchangeViewModel @Inject constructor(
    private val exchangeRateRepository: IExchangeRateRepository
) : ViewModel() {

    // ===== UI State =====

    private val _uiState = MutableStateFlow(ExchangeUiState())
    val uiState: StateFlow<ExchangeUiState> = _uiState.asStateFlow()

    // ===== 초기화 =====

    init {
        Log.d(TAG("ExchangeViewModel", "init"), "ViewModel 초기화")
        initialize()
    }

    // ===== Event 처리 =====

    fun onEvent(event: ExchangeEvent) {
        when (event) {
            is ExchangeEvent.Initialize -> initialize()
            is ExchangeEvent.Refresh -> refreshRate()
            is ExchangeEvent.ConnectWebSocket -> connectWebSocket()
            is ExchangeEvent.DisconnectWebSocket -> disconnectWebSocket()
            is ExchangeEvent.ClearError -> clearError()
        }
    }

    // ===== Public Methods =====

    /**
     * 초기화
     * - 최신 환율 로드
     * - WebSocket 연결
     * - 전일 대비 변동률 로드
     */
    private fun initialize() {
        viewModelScope.launch {
            Log.d(TAG("ExchangeViewModel", "initialize"), "초기화 시작")

            _uiState.update { it.copy(isLoading = true) }

            try {
                // 1. 최신 환율 로드
                fetchInitialRate()

                // 2. WebSocket 연결
                connectWebSocket()

                // 3. 전일 대비 변동률 로드
                fetchDailyChanges()

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = null
                    )
                }

                Log.d(TAG("ExchangeViewModel", "initialize"), "초기화 완료")

            } catch (e: Exception) {
                Log.e(TAG("ExchangeViewModel", "initialize"), "초기화 실패: ${e.message}", e)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "환율 정보를 불러오는데 실패했습니다: ${e.message}"
                    )
                }
            }
        }
    }

    /**
     * 환율 새로고침
     */
    private fun refreshRate() {
        viewModelScope.launch {
            Log.d(TAG("ExchangeViewModel", "refreshRate"), "환율 새로고침 시작")

            try {
                fetchInitialRate()
                fetchDailyChanges()

                Log.d(TAG("ExchangeViewModel", "refreshRate"), "환율 새로고침 완료")

            } catch (e: Exception) {
                Log.e(TAG("ExchangeViewModel", "refreshRate"), "새로고침 실패: ${e.message}", e)
                _uiState.update {
                    it.copy(errorMessage = "새로고침 실패: ${e.message}")
                }
            }
        }
    }

    // ===== Private Methods =====

    /**
     * 최신 환율 조회 (HTTP API)
     */
    private suspend fun fetchInitialRate() {
        try {
            Log.d(TAG("ExchangeViewModel", "fetchInitialRate"), "최신 환율 조회 시작")

            // Repository의 latestRate Flow 구독
            exchangeRateRepository.latestRate
                .collect { rate ->
                    if (rate != null) {
                        _uiState.update {
                            it.copy(
                                latestRate = rate,
                                lastUpdateTime = rate.createAt
                            )
                        }
                        Log.d(TAG("ExchangeViewModel", "fetchInitialRate"),
                            "최신 환율 업데이트: $rate")
                    }
                }

        } catch (e: Exception) {
            Log.e(TAG("ExchangeViewModel", "fetchInitialRate"),
                "최신 환율 조회 실패: ${e.message}", e)
            throw e
        }
    }

    /**
     * 전일 대비 변동률 조회
     */
    /**
     * 전일 대비 변동률 조회
     */
    private suspend fun fetchDailyChanges() {
        try {
            Log.d(TAG("ExchangeViewModel", "fetchDailyChanges"), "변동률 조회 시작")

            val response = RateApi.rateService.getDailyChange()

            val changes = mutableMapOf<String, String>()

            // ⭐ CurrencyChange 객체에서 각 통화 변동률 추출
            changes["USD"] = response.change.usd
            changes["JPY"] = response.change.jpy
            changes["EUR"] = response.change.eur
            changes["GBP"] = response.change.gbp
            changes["CNY"] = response.change.cny
            changes["AUD"] = response.change.aud
            changes["CAD"] = response.change.cad
            changes["CHF"] = response.change.chf
            changes["HKD"] = response.change.hkd
            changes["SGD"] = response.change.sgd
            changes["NZD"] = response.change.nzd
            changes["THB"] = response.change.thb

            _uiState.update { it.copy(dailyChanges = changes) }

            Log.d(TAG("ExchangeViewModel", "fetchDailyChanges"),
                "변동률 조회 완료: $changes")

        } catch (e: Exception) {
            Log.e(TAG("ExchangeViewModel", "fetchDailyChanges"),
                "변동률 조회 실패: ${e.message}", e)
            // 변동률은 필수가 아니므로 에러를 던지지 않음
        }
    }

    /**
     * WebSocket 연결
     */
    private fun connectWebSocket() {
        viewModelScope.launch {
            try {
                Log.d(TAG("ExchangeViewModel", "connectWebSocket"), "WebSocket 연결 시작")

                exchangeRateRepository.subscribeToRateUpdates()

                // Repository의 latestRate Flow 구독 (실시간 업데이트)
                exchangeRateRepository.latestRate
                    .collect { rate ->
                        if (rate != null) {
                            _uiState.update {
                                it.copy(
                                    latestRate = rate,
                                    lastUpdateTime = rate.createAt,
                                    isConnected = true
                                )
                            }
                            Log.d(TAG("ExchangeViewModel", "connectWebSocket"),
                                "실시간 환율 업데이트: $rate")
                        }
                    }

            } catch (e: Exception) {
                Log.e(TAG("ExchangeViewModel", "connectWebSocket"),
                    "WebSocket 연결 실패: ${e.message}", e)
                _uiState.update { it.copy(isConnected = false) }
            }
        }
    }

    /**
     * WebSocket 연결 해제
     */
    private fun disconnectWebSocket() {
        try {
            Log.d(TAG("ExchangeViewModel", "disconnectWebSocket"), "WebSocket 연결 해제")

            exchangeRateRepository.disconnect()

            _uiState.update { it.copy(isConnected = false) }

        } catch (e: Exception) {
            Log.e(TAG("ExchangeViewModel", "disconnectWebSocket"),
                "연결 해제 실패: ${e.message}", e)
        }
    }

    /**
     * 에러 메시지 클리어
     */
    private fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    // ===== Lifecycle =====

    override fun onCleared() {
        super.onCleared()
        Log.d(TAG("ExchangeViewModel", "onCleared"), "ViewModel 정리")
        disconnectWebSocket()
    }
}