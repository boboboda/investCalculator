// 파일 생성: domain/entity/InvestmentStatsEntity.kt

package com.bobodroid.myapplication.domain.entity

/**
 * 투자 통계 Entity
 *
 * [기존 위치]
 * MyPageViewModel.InvestmentStats (내부 데이터 클래스)
 *
 * [변경 사항]
 * - domain 패키지로 이동
 * - 플랫폼 독립적인 데이터 구조
 */
data class InvestmentStatsEntity(
    val totalInvestment: String = "₩0",      // 총 투자금
    val expectedProfit: String = "₩0",        // 예상 수익
    val profitRate: String = "0.0%",          // 수익률
    val totalTrades: Int = 0,                 // 총 거래 횟수
    val buyCount: Int = 0,                    // 보유 중인 거래
    val sellCount: Int = 0                    // 매도 완료 거래
)

/**
 * 월간 목표 Entity
 */
data class MonthlyGoalEntity(
    val goalAmount: Long = 0L,                // 목표 금액
    val currentAmount: Long = 0L,             // 현재 달성 금액
    val progress: Float = 0f,                 // 달성률 (0.0 ~ 1.0)
    val isSet: Boolean = false                // 목표 설정 여부
)

/**
 * 뱃지 정보 Entity
 */
data class BadgeEntity(
    val type: BadgeType,
    val icon: String,                         // 이모지
    val title: String,
    val description: String,
    val isUnlocked: Boolean = false,
    val progress: Int = 0,                    // 0 ~ 100
    val currentValue: Int = 0,
    val targetValue: Int = 0
)

/**
 * 뱃지 타입
 */
enum class BadgeType {
    FIRST_TRADE,        // 첫 거래
    TRADER_50,          // 50회 거래
    TRADER_100,         // 100회 거래
    INVESTMENT_1M,      // 100만원 투자
    INVESTMENT_10M      // 1000만원 투자
}