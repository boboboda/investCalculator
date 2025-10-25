// 파일 생성: app/src/main/java/com/bobodroid/myapplication/domain/entity/HoldingStatsEntity.kt

package com.bobodroid.myapplication.domain.entity

import com.bobodroid.myapplication.models.datamodels.roomDb.CurrencyType



/**
 * 개별 통화 보유 정보
 *
 * @property averageRate 평균 매수 환율 (가중평균)
 * @property currentRate 현재 환율
 * @property totalInvestment 총 투자금 (원화)
 * @property expectedProfit 예상 수익/손실 (원화)
 * @property profitRate 수익률 (%)
 * @property holdingAmount 보유 외화 수량
 * @property hasData 데이터 존재 여부 (false면 나머지 필드 무시)
 */
data class CurrencyHoldingInfo(
    val averageRate: String = "0.00",
    val currentRate: String = "0.00",
    val totalInvestment: String = "₩0",
    val expectedProfit: String = "₩0",
    val profitRate: String = "0.0%",
    val holdingAmount: String = "$0.00",
    val hasData: Boolean = false
)


data class HoldingStats(
    // Map으로 모든 통화 관리
    val currencyStats: Map<String, CurrencyHoldingInfo> = emptyMap()
) {
    // 편의 함수: 특정 통화 통계 가져오기
    fun getStatsByCode(currencyCode: String): CurrencyHoldingInfo {
        return currencyStats[currencyCode] ?: CurrencyHoldingInfo(hasData = false)
    }

    fun getStatsByType(type: CurrencyType): CurrencyHoldingInfo {
        return getStatsByCode(type.name)
    }

    // 레거시 호환 (기존 코드가 사용)
    val dollarStats: CurrencyHoldingInfo
        get() = getStatsByCode("USD")

    val yenStats: CurrencyHoldingInfo
        get() = getStatsByCode("JPY")
}