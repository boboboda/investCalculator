// 파일 생성: domain/usecase/analysis/CalculatePeriodComparisonUseCase.kt

package com.bobodroid.myapplication.domain.usecase.analysis

import com.bobodroid.myapplication.domain.entity.PeriodComparisonEntity
import javax.inject.Inject

/**
 * 기간별 환율 비교 UseCase
 *
 * [기존 위치]
 * AnalysisViewModel.calculatePeriodComparison()
 *
 * [변경 사항]
 * - ViewModel에서 분리
 * - 변화율 계산 로직만 포함
 */
class CalculatePeriodComparisonUseCase @Inject constructor() {

    /**
     * 기간별 변화율 계산
     *
     * @param currentRate 현재 환율
     * @param previousDayRate 전일 환율 (null이면 계산 안함)
     * @param weekAgoRate 1주일 전 환율 (null이면 계산 안함)
     * @param monthAgoRate 1개월 전 환율 (null이면 계산 안함)
     * @return 기간별 변화율
     */
    fun execute(
        currentRate: Float,
        previousDayRate: Float?,
        weekAgoRate: Float?,
        monthAgoRate: Float?
    ): PeriodComparisonEntity {

        return PeriodComparisonEntity(
            previousDay = calculateChangeRate(currentRate, previousDayRate),
            weekAgo = calculateChangeRate(currentRate, weekAgoRate),
            monthAgo = calculateChangeRate(currentRate, monthAgoRate)
        )
    }

    /**
     * 변화율 계산
     *
     * 공식: ((현재값 - 과거값) / 과거값) × 100
     *
     * @param current 현재 값
     * @param past 과거 값 (null이면 "0.00%" 반환)
     * @return 변화율 (예: "+2.35%", "-1.20%")
     */
    private fun calculateChangeRate(current: Float, past: Float?): String {
        if (past == null || past == 0f) {
            return "0.00%"
        }

        // 변화율 계산
        val changeRate = ((current - past) / past) * 100

        // 포맷팅
        return when {
            changeRate > 0 -> "+%.2f%%".format(changeRate)
            changeRate < 0 -> "%.2f%%".format(changeRate)
            else -> "0.00%"
        }
    }
}