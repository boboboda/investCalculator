package com.bobodroid.myapplication.models.datamodels.backtest

import java.util.UUID

/**
 * 기록 리스트 화면(History)에서 카드 하나에 필요한 요약 정보만 담은 모델
 */
data class BacktestHistoryItem(
    val id: UUID,
    val createdAt: Long,
    val currencyType: String,
    val periodMonths: Int,
    val investStrategy: String,
    val profitRatePercent: Double,
    val realizedProfitWon: Double
)
