// 파일: app/src/main/java/com/bobodroid/myapplication/domain/usecase/record/CalculateHoldingStatsUseCase.kt

package com.bobodroid.myapplication.domain.usecase.record

import android.util.Log
import com.bobodroid.myapplication.domain.entity.CurrencyHoldingInfo
import com.bobodroid.myapplication.domain.entity.HoldingStats
import com.bobodroid.myapplication.models.datamodels.roomDb.Currencies
import com.bobodroid.myapplication.models.datamodels.roomDb.CurrencyRecord
import com.bobodroid.myapplication.models.datamodels.roomDb.CurrencyType
import java.math.BigDecimal
import java.math.RoundingMode
import javax.inject.Inject

/**
 * 보유 중인 외화 통계 계산 UseCase
 *
 * 기능:
 * - 통화별 평균 매수 환율 계산 (가중평균)
 * - 현재 환율 기준 예상 수익/손실 계산
 * - 수익률 계산
 */
class CalculateHoldingStatsUseCase @Inject constructor() {

    /**
     * 모든 통화의 보유 통계 계산
     *
     * @param recordsByType 통화별 기록 맵 (매수 기록만 전달해야 함)
     * @param currentRates 통화별 현재 환율 맵 (Key: USD, JPY 등)
     * @return HoldingStatsEntity 계산된 통계
     */
    fun execute(
        recordsByType: Map<CurrencyType, List<CurrencyRecord>>,
        currentRates: Map<String, String>
    ): HoldingStats {

        val statsMap = mutableMapOf<String, CurrencyHoldingInfo>()

        // ⭐ 각 통화별로 통계 계산
        recordsByType.forEach { (currencyType, records) ->
            val currentRate = currentRates[currencyType.name] ?: "0"

            // ⭐ 기록이 있고 환율이 유효할 때만 계산
            if (records.isNotEmpty() && currentRate != "0") {
                val stats = calculateCurrencyHolding(
                    records = records,
                    currentRate = currentRate,
                    currencyType = currencyType
                )
                statsMap[currencyType.name] = stats
            }
        }

        return HoldingStats(currencyStats = statsMap)
    }

    /**
     * 개별 통화의 보유 통계 계산
     *
     * 계산 항목:
     * 1. 평균 매수 환율 (가중평균)
     * 2. 총 투자금 (원화)
     * 3. 예상 수익/손실
     * 4. 수익률
     */
    private fun calculateCurrencyHolding(
        records: List<CurrencyRecord>,
        currentRate: String,
        currencyType: CurrencyType
    ): CurrencyHoldingInfo {

        // ⭐ 데이터 검증
        if (records.isEmpty() || currentRate == "0" || currentRate.isEmpty()) {
            return CurrencyHoldingInfo(hasData = false)
        }

        try {
            // ━━━━━━━━━━━━━━━━━━━━━━━━━━
            // 1. 총 투자금 계산 (원화)
            // ━━━━━━━━━━━━━━━━━━━━━━━━━━
            val totalInvestment = records.sumOf {
                it.money?.replace(",", "")?.toBigDecimalOrNull() ?: BigDecimal.ZERO
            }

            // ━━━━━━━━━━━━━━━━━━━━━━━━━━
            // 2. 보유 외화 총량 계산
            // ━━━━━━━━━━━━━━━━━━━━━━━━━━
            val totalHoldingAmount = records.sumOf {
                it.exchangeMoney?.replace(",", "")?.toBigDecimalOrNull() ?: BigDecimal.ZERO
            }

            // ━━━━━━━━━━━━━━━━━━━━━━━━━━
            // 3. 가중평균 매수 환율 계산
            // ━━━━━━━━━━━━━━━━━━━━━━━━━━
            // 공식: (매수환율1 × 수량1 + 매수환율2 × 수량2 ...) / 총수량
            var totalWeightedRate = BigDecimal.ZERO
            var totalWeight = BigDecimal.ZERO

            records.forEach { record ->
                val exchangeMoney = record.exchangeMoney?.replace(",", "")?.toBigDecimalOrNull() ?: BigDecimal.ZERO
                val buyRate = record.buyRate?.replace(",", "")?.toBigDecimalOrNull() ?: BigDecimal.ZERO

                if (exchangeMoney > BigDecimal.ZERO && buyRate > BigDecimal.ZERO) {
                    totalWeightedRate += buyRate.multiply(exchangeMoney)
                    totalWeight += exchangeMoney
                }
            }

            val averageRate = if (totalWeight > BigDecimal.ZERO) {
                totalWeightedRate.divide(totalWeight, 2, RoundingMode.HALF_UP)
            } else {
                BigDecimal.ZERO
            }

            // ━━━━━━━━━━━━━━━━━━━━━━━━━━
            // 4. 예상 수익/손실 계산
            // ━━━━━━━━━━━━━━━━━━━━━━━━━━
            val currentRateBD = currentRate.replace(",", "").toBigDecimalOrNull() ?: BigDecimal.ZERO
            val currency = Currencies.fromCurrencyType(currencyType)

            // ⭐ 통화 특성에 따라 계산 방식 다름
            val expectedProfit = if (currency.needsMultiply) {
                // JPY, THB 등: 환율이 100단위당 가격이므로 100으로 나눔
                // 예: 100엔 = 900원 → 1엔 = 9원
                val currentValue = totalHoldingAmount
                    .multiply(currentRateBD)
                    .divide(BigDecimal(100), 2, RoundingMode.HALF_UP)
                currentValue.minus(totalInvestment)
            } else {
                // USD, EUR 등: 환율이 1단위당 가격
                // 예: 1달러 = 1300원
                val currentValue = totalHoldingAmount.multiply(currentRateBD)
                currentValue.minus(totalInvestment)
            }

            // ━━━━━━━━━━━━━━━━━━━━━━━━━━
            // 5. 수익률 계산
            // ━━━━━━━━━━━━━━━━━━━━━━━━━━
            // 공식: (예상수익 / 투자금) × 100
            val profitRate = if (totalInvestment > BigDecimal.ZERO) {
                expectedProfit
                    .divide(totalInvestment, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal(100))
                    .setScale(1, RoundingMode.HALF_UP)
            } else {
                BigDecimal.ZERO
            }

            // ━━━━━━━━━━━━━━━━━━━━━━━━━━
            // 6. 포맷팅 후 반환
            // ━━━━━━━━━━━━━━━━━━━━━━━━━━
            return CurrencyHoldingInfo(
                averageRate = FormatUtils.formatRate(averageRate),         // 예: "1,350.00"
                currentRate = FormatUtils.formatRate(currentRateBD),       // 예: "1,380.00"
                totalInvestment = FormatUtils.formatCurrency(totalInvestment),  // 예: "+₩1,350,000"
                expectedProfit = FormatUtils.formatCurrency(expectedProfit),    // 예: "+₩30,000"
                profitRate = FormatUtils.formatProfitRate(profitRate),          // 예: "+2.2%"
                holdingAmount = FormatUtils.formatAmount(totalHoldingAmount, currencyType),  // 예: "$1,000.00"
                hasData = true
            )

        } catch (e: Exception) {
            Log.e("CalculateHoldingStatsUseCase", "계산 오류: ${e.message}", e)
            return CurrencyHoldingInfo(hasData = false)
        }
    }
}