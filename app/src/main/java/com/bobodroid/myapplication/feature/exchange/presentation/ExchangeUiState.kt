package com.bobodroid.myapplication.feature.exchange.presentation

import com.bobodroid.myapplication.domain.entity.ExchangeRateEntity
import com.bobodroid.myapplication.models.datamodels.roomDb.CurrencyType

/**
 * Exchange UI State
 *
 * 환율 화면의 UI 상태 관리
 */
data class ExchangeUiState(
    /**
     * 현재 표시 중인 최신 환율
     */
    val latestRate: ExchangeRateEntity = ExchangeRateEntity.empty(),

    /**
     * 전일 대비 변동률 (통화별)
     * 예: {"USD": "+0.5%", "JPY": "-0.3%"}
     */
    val dailyChanges: Map<String, String> = emptyMap(),

    /**
     * 로딩 상태
     */
    val isLoading: Boolean = false,

    /**
     * 에러 메시지
     */
    val errorMessage: String? = null,

    /**
     * 마지막 업데이트 시간
     */
    val lastUpdateTime: String? = null,

    /**
     * WebSocket 연결 상태
     */
    val isConnected: Boolean = false
) {
    /**
     * 특정 통화의 환율 가져오기
     */
    fun getRateByCurrency(currencyType: CurrencyType): String {
        return latestRate.getRateByCode(currencyType.name) ?: "0"
    }

    /**
     * 특정 통화의 변동률 가져오기
     */
    fun getChangeByCurrency(currencyType: CurrencyType): String {
        return dailyChanges[currencyType.name] ?: "0%"
    }

    /**
     * 에러 상태인지 확인
     */
    fun hasError(): Boolean = errorMessage != null
}