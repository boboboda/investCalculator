// 파일 생성: domain/usecase/analysis/CalculateRateStatisticsUseCase.kt

package com.bobodroid.myapplication.domain.usecase.analysis

import com.bobodroid.myapplication.domain.entity.RateStatisticsEntity
import javax.inject.Inject
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * 환율 통계 계산 UseCase
 *
 * [기존 위치]
 * AnalysisViewModel.calculateStatistics()
 * AnalysisViewModel.calculateVolatility()
 *
 * [변경 사항]
 * - ViewModel에서 분리
 * - 순수 계산 로직만 포함
 */
class CalculateRateStatisticsUseCase @Inject constructor() {

    /**
     * 환율 통계 계산
     *
     * @param rates 환율 리스트 (Float 값)
     * @return 통계 정보 (최대, 최소, 평균, 변동성, 범위)
     */
    fun execute(rates: List<Float>): RateStatisticsEntity {
        if (rates.isEmpty()) {
            return RateStatisticsEntity()
        }

        // ━━━━━━━━━━━━━━━━━━━━━━━━━━
        // 1. 기본 통계
        // ━━━━━━━━━━━━━━━━━━━━━━━━━━
        val max = rates.maxOrNull() ?: 0f
        val min = rates.minOrNull() ?: 0f
        val average = rates.average().toFloat()
        val range = max - min

        // ━━━━━━━━━━━━━━━━━━━━━━━━━━
        // 2. 변동성 (표준편차) 계산
        // ━━━━━━━━━━━━━━━━━━━━━━━━━━
        val volatility = calculateVolatility(rates)

        return RateStatisticsEntity(
            max = max,
            min = min,
            average = average,
            volatility = volatility,
            range = range
        )
    }

    /**
     * 변동성 계산 (표준편차)
     *
     * 공식: σ = √(Σ(x - μ)² / N)
     * - σ: 표준편차
     * - x: 각 값
     * - μ: 평균
     * - N: 데이터 개수
     *
     * @param values 데이터 리스트
     * @return 표준편차 (변동성)
     */
    private fun calculateVolatility(values: List<Float>): Float {
        if (values.size < 2) return 0f

        // 1. 평균 계산
        val mean = values.average()

        // 2. 분산 계산: Σ(x - μ)² / N
        val variance = values.map { (it - mean).pow(2) }.average()

        // 3. 표준편차 계산: √분산
        return sqrt(variance).toFloat()
    }
}