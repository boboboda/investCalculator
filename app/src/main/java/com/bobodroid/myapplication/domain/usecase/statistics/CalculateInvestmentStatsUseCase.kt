// 파일 생성: domain/usecase/statistics/CalculateInvestmentStatsUseCase.kt

package com.bobodroid.myapplication.domain.usecase.statistics

import com.bobodroid.myapplication.domain.entity.InvestmentStatsEntity
import com.bobodroid.myapplication.domain.entity.RecordEntity
import com.bobodroid.myapplication.models.datamodels.roomDb.CurrencyRecord
import java.math.BigDecimal
import java.math.RoundingMode
import javax.inject.Inject

/**
 * 투자 통계 계산 UseCase
 *
 * [기존 위치]
 * MyPageViewModel.calculateInvestmentStats()
 *
 * [변경 사항]
 * - ViewModel에서 분리
 * - 12개 통화 모두 지원 (기존: USD, JPY만)
 */
class CalculateInvestmentStatsUseCase @Inject constructor() {

    /**
     * 전체 투자 통계 계산
     *
     * @param allRecords 모든 통화의 기록 (12개 통화)
     * @return 투자 통계
     */
    fun execute(allRecords: List<RecordEntity>): InvestmentStatsEntity {

        // ━━━━━━━━━━━━━━━━━━━━━━━━━━
        // 1. 총 투자금 계산
        // ━━━━━━━━━━━━━━━━━━━━━━━━━━
        val totalInvestment = allRecords.sumOf {
            it.money?.replace(",", "")?.toBigDecimalOrNull() ?: BigDecimal.ZERO
        }

        // ━━━━━━━━━━━━━━━━━━━━━━━━━━
        // 2. 예상 수익 계산 (보유중인 것만)
        // ━━━━━━━━━━━━━━━━━━━━━━━━━━
        val expectedProfit = allRecords
            .filter { it.recordColor == false }  // 보유 중
            .sumOf {
                it.expectProfit?.replace(",", "")?.toBigDecimalOrNull() ?: BigDecimal.ZERO
            }

        // ━━━━━━━━━━━━━━━━━━━━━━━━━━
        // 3. 수익률 계산
        // ━━━━━━━━━━━━━━━━━━━━━━━━━━
        val profitRate = if (totalInvestment > BigDecimal.ZERO) {
            (expectedProfit.divide(totalInvestment, 4, RoundingMode.HALF_UP) * BigDecimal(100))
                .setScale(1, RoundingMode.HALF_UP)
        } else {
            BigDecimal.ZERO
        }

        // ━━━━━━━━━━━━━━━━━━━━━━━━━━
        // 4. 거래 횟수 계산
        // ━━━━━━━━━━━━━━━━━━━━━━━━━━
        val totalTrades = allRecords.size
        val buyCount = allRecords.count { it.recordColor == false }   // 보유 중
        val sellCount = allRecords.count { it.recordColor == true }   // 매도 완료

        return InvestmentStatsEntity(
            totalInvestment = FormatUtils.formatCurrency(totalInvestment),
            expectedProfit = FormatUtils.formatCurrency(expectedProfit),
            profitRate = FormatUtils.formatProfitRate(profitRate),
            totalTrades = totalTrades,
            buyCount = buyCount,
            sellCount = sellCount
        )
    }
}