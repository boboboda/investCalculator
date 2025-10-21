// app/src/main/java/com/bobodroid/myapplication/models/datamodels/roomDb/TargetRates.kt

package com.bobodroid.myapplication.models.datamodels.roomDb

import com.bobodroid.myapplication.models.datamodels.service.UserApi.Rate

// ✅ 통화별 목표환율 구조
data class CurrencyTargetRates(
    val high: List<Rate> = emptyList(),
    val low: List<Rate> = emptyList()
)

// ✅ 12개 통화 지원 (레거시 제거)
data class TargetRates(
    val rates: Map<CurrencyType, CurrencyTargetRates> = emptyMap()
) {
    companion object {
        fun empty(): TargetRates = TargetRates(rates = emptyMap())
    }

    // 특정 통화/방향의 목표환율 가져오기
    fun getRates(currency: CurrencyType, direction: RateDirection): List<Rate> {
        return when (direction) {
            RateDirection.HIGH -> rates[currency]?.high ?: emptyList()
            RateDirection.LOW -> rates[currency]?.low ?: emptyList()
        }
    }

    // 특정 통화/방향의 목표환율 설정하기
    fun setRates(currency: CurrencyType, direction: RateDirection, newRates: List<Rate>): TargetRates {
        val updatedRatesMap = rates.toMutableMap()
        val currentCurrencyRates = updatedRatesMap[currency] ?: CurrencyTargetRates()

        updatedRatesMap[currency] = when (direction) {
            RateDirection.HIGH -> currentCurrencyRates.copy(high = newRates)
            RateDirection.LOW -> currentCurrencyRates.copy(low = newRates)
        }

        return copy(rates = updatedRatesMap)
    }

    // RateType으로 목표환율 가져오기
    fun getRates(rateType: RateType): List<Rate> {
        return getRates(rateType.currency, rateType.direction)
    }

    // RateType으로 목표환율 설정하기
    fun setRates(rateType: RateType, newRates: List<Rate>): TargetRates {
        return setRates(rateType.currency, rateType.direction, newRates)
    }

    // 모든 통화의 목표환율 개수
    fun getTotalCount(): Int {
        return rates.values.sumOf { it.high.size + it.low.size }
    }

    // 특정 통화의 목표환율이 있는지 확인
    fun hasCurrency(currency: CurrencyType): Boolean {
        val currencyRates = rates[currency] ?: return false
        return currencyRates.high.isNotEmpty() || currencyRates.low.isNotEmpty()
    }

    // 모든 통화 목록
    fun getAllCurrencies(): List<CurrencyType> {
        return rates.keys.toList()
    }
}