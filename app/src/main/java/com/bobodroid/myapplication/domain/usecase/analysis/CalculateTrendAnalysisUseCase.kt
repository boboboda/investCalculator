// 파일 생성: domain/usecase/analysis/CalculateTrendAnalysisUseCase.kt

package com.bobodroid.myapplication.domain.usecase.analysis

import com.bobodroid.myapplication.domain.entity.TrendAnalysisEntity
import javax.inject.Inject
import kotlin.math.abs

/**
 * 추세 분석 UseCase
 *
 * [기존 위치]
 * AnalysisViewModel.calculateTrendAnalysis()
 *
 * [변경 사항]
 * - ViewModel에서 분리
 * - 상승/하락 판단 로직만 포함
 */
class CalculateTrendAnalysisUseCase @Inject constructor() {

    /**
     * 추세 분석
     *
     * 전날 대비 상승/하락 일수를 세고 추세 방향 판단
     *
     * @param rates 환율 리스트 (시간 순서대로 정렬됨)
     * @return 추세 분석 결과
     */
    fun execute(rates: List<Float>): TrendAnalysisEntity {
        if (rates.size < 2) {
            return TrendAnalysisEntity()
        }

        // ━━━━━━━━━━━━━━━━━━━━━━━━━━
        // 1. 상승/하락 일수 계산
        // ━━━━━━━━━━━━━━━━━━━━━━━━━━
        var upDays = 0
        var downDays = 0

        for (i in 1 until rates.size) {
            when {
                rates[i] > rates[i - 1] -> upDays++
                rates[i] < rates[i - 1] -> downDays++
                // rates[i] == rates[i-1] → 변화 없음, 카운트 안함
            }
        }

        // ━━━━━━━━━━━━━━━━━━━━━━━━━━
        // 2. 추세 방향 판단
        // ━━━━━━━━━━━━━━━━━━━━━━━━━━
        val trend = when {
            upDays > downDays -> "상승"
            downDays > upDays -> "하락"
            else -> "횡보"
        }

        // ━━━━━━━━━━━━━━━━━━━━━━━━━━
        // 3. 추세 강도 계산 (0~100)
        // ━━━━━━━━━━━━━━━━━━━━━━━━━━
        // 공식: (|상승일수 - 하락일수| / 전체변화일수) × 100
        val trendStrength = if (rates.size > 1) {
            val totalChangeDays = (upDays + downDays).toFloat()
            if (totalChangeDays > 0) {
                ((abs(upDays - downDays) / totalChangeDays) * 100).toInt()
            } else {
                0
            }
        } else {
            0
        }

        return TrendAnalysisEntity(
            trend = trend,
            upDays = upDays,
            downDays = downDays,
            trendStrength = trendStrength
        )
    }
}