package com.bobodroid.myapplication.models.datamodels.backtest

import com.squareup.moshi.JsonClass

// ==================== 요청 (서버 BacktestRequestDto와 1:1 매칭) ====================

enum class BacktestCurrencyType {
    USD, JPY
}

enum class BacktestInvestStrategy {
    EQUAL, PROGRESSIVE
}

@JsonClass(generateAdapter = true)
data class BacktestRequest(
    val currencyType: BacktestCurrencyType,
    val budget: Double,                 // 예산 (원)
    val startRate: Double,               // 1회차 매입 희망가
    val minInvestPerRound: Double,       // 1회 최소 투자금 (원)
    val maxInvestPerRound: Double,       // 1회 최대 투자금 (원) — PROGRESSIVE에서는 서버가 무시
    val minSplitCount: Int,              // 최소분할매수횟수
    val dropGapWon: Double,              // 추가매수 하락폭 (원)
    val riseGapWon: Double,              // 매도 목표 상승폭 (원)
    val periodMonths: Int,               // 3, 6, 12
    val buySpreadWon: Double,            // 매수 스프레드 (원, 예: 7 = 시장가보다 7원 비싸게 매수 체결)
    val sellSpreadWon: Double,           // 매도 스프레드 (원, 예: 7 = 시장가보다 7원 싸게 매도 체결)
    val investStrategy: BacktestInvestStrategy = BacktestInvestStrategy.EQUAL,
    val progressiveIncreasePercent: Double? = null
)

@JsonClass(generateAdapter = true)
data class BacktestAvailability(
    val currencyType: String,
    val earliestValidDate: String? = null,
    val availablePeriods: List<Int> = emptyList(),
    val highRate: Double? = null,        // periodMonths 전달 시 해당 기간 최고가 (참고용)
    val lowRate: Double? = null          // periodMonths 전달 시 해당 기간 최저가 (참고용)
)

// ==================== 응답 (서버 runBacktest() 반환값과 1:1 매칭) ====================

@JsonClass(generateAdapter = true)
data class BacktestTrade(
    val round: Int,
    val type: String,                       // "BUY" | "SELL"
    val timestamp: String,
    val rate: Double,
    val marketRate: Double,
    val amountWon: Double,
    val remainingFundAfter: Double? = null, // BUY일 때만 존재
    val positionStatus: String? = null,     // BUY일 때만 ("OPEN" | "CLOSED")
    val profitWon: Double? = null           // SELL일 때만 존재
)

@JsonClass(generateAdapter = true)
data class BacktestOpenPosition(
    val round: Int,
    val buyRate: Double,
    val evaluationRate: Double,
    val amountWon: Double,
    val currentValueWon: Double,
    val unrealizedProfitWon: Double
)

@JsonClass(generateAdapter = true)
data class BacktestResponse(
    val currencyType: String,
    val periodMonths: Int,
    val startDate: String,
    val endDate: String,
    val investStrategy: String,
    val requestedStartRate: Double,
    val baselineRate: Double? = null,
    val finalRate: Double,
    val principal: Double,
    val realizedProfitWon: Double,
    val profitRatePercent: Double,
    val buyCount: Int,
    val sellCount: Int,
    val openPositionCount: Int,
    val openPositions: List<BacktestOpenPosition> = emptyList(),
    val pendingRecycledFund: Double,
    val warnings: List<String> = emptyList(),
    val trades: List<BacktestTrade> = emptyList()
)
