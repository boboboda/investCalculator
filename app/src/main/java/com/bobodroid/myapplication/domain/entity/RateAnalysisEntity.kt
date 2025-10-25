// 파일 생성: domain/entity/RateAnalysisEntity.kt

package com.bobodroid.myapplication.domain.entity

/**
 * 환율 통계 Entity
 *
 * [기존 위치]
 * AnalysisViewModel.RateStatistics
 *
 * [변경 사항]
 * - domain 패키지로 이동
 */
data class RateStatisticsEntity(
    val max: Float = 0f,           // 최고가
    val min: Float = 0f,           // 최저가
    val average: Float = 0f,       // 평균
    val volatility: Float = 0f,    // 변동성 (표준편차)
    val range: Float = 0f          // 가격 범위 (max - min)
)

/**
 * 추세 분석 Entity
 *
 * [기존 위치]
 * AnalysisViewModel.TrendAnalysis
 */
data class TrendAnalysisEntity(
    val trend: String = "횡보",      // 상승/하락/횡보
    val upDays: Int = 0,            // 상승 일수
    val downDays: Int = 0,          // 하락 일수
    val trendStrength: Int = 0      // 추세 강도 (0~100)
)

/**
 * 기간별 비교 Entity
 *
 * [기존 위치]
 * AnalysisViewModel.PeriodComparison
 */
data class PeriodComparisonEntity(
    val previousDay: String = "0.00%",   // 전일 대비
    val weekAgo: String = "0.00%",       // 1주일 전 대비
    val monthAgo: String = "0.00%"       // 1개월 전 대비
)