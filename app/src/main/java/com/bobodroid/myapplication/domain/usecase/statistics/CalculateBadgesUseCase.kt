// 파일 생성: domain/usecase/statistics/CalculateBadgesUseCase.kt

package com.bobodroid.myapplication.domain.usecase.statistics

import com.bobodroid.myapplication.domain.entity.BadgeEntity
import com.bobodroid.myapplication.domain.entity.BadgeType
import com.bobodroid.myapplication.models.datamodels.roomDb.CurrencyRecord
import javax.inject.Inject

/**
 * 뱃지 계산 UseCase
 *
 * [기존 위치]
 * MyPageViewModel.calculateBadges()
 *
 * [변경 사항]
 * - ViewModel에서 분리
 * - 12개 통화 모두 지원
 */
class CalculateBadgesUseCase @Inject constructor() {

    /**
     * 모든 뱃지 계산
     *
     * @param allRecords 모든 통화의 기록
     * @return 뱃지 리스트
     */
    fun execute(allRecords: List<CurrencyRecord>): List<BadgeEntity> {

        // ━━━━━━━━━━━━━━━━━━━━━━━━━━
        // 1. 기본 통계 계산
        // ━━━━━━━━━━━━━━━━━━━━━━━━━━
        val totalTrades = allRecords.size
        val sellCount = allRecords.count { it.recordColor == true }

        val totalInvestment = allRecords.sumOf {
            it.money?.replace(",", "")?.toLongOrNull() ?: 0L
        }

        // ━━━━━━━━━━━━━━━━━━━━━━━━━━
        // 2. 각 뱃지 생성
        // ━━━━━━━━━━━━━━━━━━━━━━━━━━
        return listOf(
            // 첫 거래
            BadgeEntity(
                type = BadgeType.FIRST_TRADE,
                icon = "🎯",
                title = "첫 거래",
                description = "첫 환전 기록을 생성했습니다",
                isUnlocked = totalTrades >= 1,
                progress = if (totalTrades >= 1) 100 else 0,
                currentValue = totalTrades,
                targetValue = 1
            ),

            // 50회 거래
            BadgeEntity(
                type = BadgeType.TRADER_50,
                icon = "📈",
                title = "트레이더",
                description = "총 50회 거래를 달성했습니다",
                isUnlocked = totalTrades >= 50,
                progress = ((totalTrades.toFloat() / 50f) * 100).toInt().coerceIn(0, 100),
                currentValue = totalTrades,
                targetValue = 50
            ),

            // 100회 거래
            BadgeEntity(
                type = BadgeType.TRADER_100,
                icon = "🏆",
                title = "마스터 트레이더",
                description = "총 100회 거래를 달성했습니다",
                isUnlocked = totalTrades >= 100,
                progress = ((totalTrades.toFloat() / 100f) * 100).toInt().coerceIn(0, 100),
                currentValue = totalTrades,
                targetValue = 100
            ),

            // 100만원 투자
            BadgeEntity(
                type = BadgeType.INVESTMENT_1M,
                icon = "💰",
                title = "백만장자",
                description = "총 투자금 100만원을 달성했습니다",
                isUnlocked = totalInvestment >= 1_000_000L,
                progress = ((totalInvestment.toFloat() / 1_000_000f) * 100).toInt().coerceIn(0, 100),
                currentValue = (totalInvestment / 10000).toInt(),
                targetValue = 100
            ),

            // 1000만원 투자
            BadgeEntity(
                type = BadgeType.INVESTMENT_10M,
                icon = "💎",
                title = "천만장자",
                description = "총 투자금 1,000만원을 달성했습니다",
                isUnlocked = totalInvestment >= 10_000_000L,
                progress = ((totalInvestment.toFloat() / 10_000_000f) * 100).toInt().coerceIn(0, 100),
                currentValue = (totalInvestment / 10000).toInt(),
                targetValue = 1000
            )
        )
    }
}