package com.bobodroid.myapplication.domain.entity

import com.bobodroid.myapplication.models.datamodels.roomDb.Currency

/**
 * ExchangeRate Entity (Domain Layer)
 *
 * 플랫폼 독립적인 환율 모델
 * - Room, Firebase 등 구체적 기술과 무관
 * - 비즈니스 로직에만 집중
 * - iOS 이식 가능
 *
 * [변경 사항]
 * 기존: ExchangeRate (Room @Entity, JSON String 저장)
 * 신규: ExchangeRateEntity (Pure Kotlin, Map 사용)
 */
data class ExchangeRateEntity(
    /**
     * 고유 ID
     */
    val id: String? = null,

    /**
     * 생성 시간 (yyyy-MM-dd HH:mm:ss)
     */
    val createAt: String? = null,

    /**
     * 환율 데이터 (통화 코드 → 환율)
     *
     * 예시:
     * {
     *   "USD" -> "1300.50",
     *   "JPY" -> "944.00",
     *   "EUR" -> "1400.25"
     * }
     */
    val rates: Map<String, String> = mapOf(
        "USD" to "0",
        "JPY" to "0",
        "EUR" to "0"
    )
) {

    /**
     * 특정 통화의 환율 가져오기
     *
     * @param code 통화 코드 (USD, JPY 등)
     * @return 환율 문자열 또는 null
     */
    fun getRateByCode(code: String): String? {
        return rates[code]
    }

    /**
     * Currency 객체로 환율 가져오기
     *
     * @param currency Currency 객체
     * @return 환율 문자열 (없으면 "0")
     */
    fun getRate(currency: Currency): String {
        return rates[currency.code] ?: "0"
    }

    /**
     * 하위 호환성: USD 환율
     */
    val usd: String?
        get() = getRateByCode("USD")

    /**
     * 하위 호환성: JPY 환율
     */
    val jpy: String?
        get() = getRateByCode("JPY")

    /**
     * 하위 호환성: EUR 환율
     */
    val eur: String?
        get() = getRateByCode("EUR")

    /**
     * 모든 통화 코드 목록
     */
    fun getCurrencyCodes(): Set<String> {
        return rates.keys
    }

    /**
     * 특정 통화가 존재하는지 확인
     */
    fun hasRate(code: String): Boolean {
        return rates.containsKey(code)
    }

    /**
     * 환율 업데이트 (불변 객체이므로 새 인스턴스 반환)
     */
    fun updateRate(code: String, rate: String): ExchangeRateEntity {
        val newRates = rates.toMutableMap()
        newRates[code] = rate
        return this.copy(rates = newRates)
    }

    /**
     * 여러 환율 업데이트
     */
    fun updateRates(newRates: Map<String, String>): ExchangeRateEntity {
        val mergedRates = rates.toMutableMap()
        mergedRates.putAll(newRates)
        return this.copy(rates = mergedRates)
    }

    companion object {
        /**
         * 빈 ExchangeRateEntity 생성
         */
        fun empty(): ExchangeRateEntity {
            return ExchangeRateEntity(
                id = "",
                createAt = "N/A",
                rates = emptyMap()
            )
        }

        /**
         * 12개 통화 기본값으로 생성
         */
        fun withDefaultRates(id: String = "", createAt: String = "N/A"): ExchangeRateEntity {
            val defaultRates = mapOf(
                "USD" to "0",
                "JPY" to "0",
                "EUR" to "0",
                "GBP" to "0",
                "CNY" to "0",
                "AUD" to "0",
                "CAD" to "0",
                "CHF" to "0",
                "HKD" to "0",
                "SGD" to "0",
                "NZD" to "0",
                "THB" to "0"
            )
            return ExchangeRateEntity(
                id = id,
                createAt = createAt,
                rates = defaultRates
            )
        }
    }
}