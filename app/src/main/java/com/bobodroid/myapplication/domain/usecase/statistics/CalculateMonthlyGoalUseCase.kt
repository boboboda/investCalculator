// 파일 생성: domain/usecase/statistics/CalculateMonthlyGoalUseCase.kt

package com.bobodroid.myapplication.domain.usecase.statistics

import com.bobodroid.myapplication.domain.entity.MonthlyGoalEntity
import com.bobodroid.myapplication.domain.entity.RecordEntity
import com.bobodroid.myapplication.models.datamodels.roomDb.CurrencyRecord
import java.math.BigDecimal
import java.math.RoundingMode
import javax.inject.Inject

/**
 * 월간 목표 달성률 계산 UseCase
 *
 * [기존 위치]
 * MyPageViewModel.calculateGoalProgress()
 * MyPageViewModel.calculateMonthlyProfit()
 *
 * [변경 사항]
 * - ViewModel에서 분리
 * - 12개 통화 모두 지원
 */
class CalculateMonthlyGoalUseCase @Inject constructor() {

    /**
     * 월간 목표 달성률 계산
     *
     * @param allRecords 모든 통화의 기록
     * @param goalAmount 목표 금액 (원화)
     * @param goalMonth 목표 설정 월 ("2025-01")
     * @param currentMonth 현재 월 ("2025-01")
     * @return 월간 목표 정보
     */
    fun execute(
        allRecords: List<RecordEntity>,
        goalAmount: Long,
        goalMonth: String?,
        currentMonth: String
    ): MonthlyGoalEntity {

        // ━━━━━━━━━━━━━━━━━━━━━━━━━━
        // 1. 목표 설정 여부 확인
        // ━━━━━━━━━━━━━━━━━━━━━━━━━━
        if (goalAmount <= 0L) {
            return MonthlyGoalEntity(
                goalAmount = 0L,
                currentAmount = 0L,
                progress = 0f,
                isSet = false
            )
        }

        // ━━━━━━━━━━━━━━━━━━━━━━━━━━
        // 2. 월 변경 확인 (새 달이면 리셋)
        // ━━━━━━━━━━━━━━━━━━━━━━━━━━
        if (goalMonth != currentMonth) {
            return MonthlyGoalEntity(
                goalAmount = goalAmount,
                currentAmount = 0L,
                progress = 0f,
                isSet = true
            )
        }

        // ━━━━━━━━━━━━━━━━━━━━━━━━━━
        // 3. 이번 달 매도 수익 계산
        // ━━━━━━━━━━━━━━━━━━━━━━━━━━
        val monthlyProfit = calculateMonthlyProfit(allRecords, currentMonth)

        // ━━━━━━━━━━━━━━━━━━━━━━━━━━
        // 4. 달성률 계산
        // ━━━━━━━━━━━━━━━━━━━━━━━━━━
        val progress = if (goalAmount > 0) {
            (monthlyProfit.toFloat() / goalAmount.toFloat()).coerceIn(0f, 1f)
        } else {
            0f
        }

        return MonthlyGoalEntity(
            goalAmount = goalAmount,
            currentAmount = monthlyProfit,
            progress = progress,
            isSet = true
        )
    }

    /**
     * 이번 달 매도 수익 계산
     *
     * @param records 기록 리스트
     * @param targetMonth 대상 월 ("2025-01")
     * @return 매도 수익 합계
     */
    private fun calculateMonthlyProfit(
        records: List<RecordEntity>,
        targetMonth: String
    ): Long {
        val totalProfit = records
            .filter {
                // 매도 완료 && 이번 달에 매도
                it.recordColor == true &&
                        it.sellDate?.startsWith(targetMonth) == true
            }
            .sumOf {
                it.sellProfit?.replace(",", "")?.toBigDecimalOrNull() ?: BigDecimal.ZERO
            }

        return totalProfit.setScale(0, RoundingMode.HALF_UP).toLong()
    }
}