package com.bobodroid.myapplication.models.datamodels.roomDb

import java.math.BigDecimal
import java.math.RoundingMode

/**
 * 통화 설정 데이터 클래스
 * @param code 통화 코드 (USD, JPY, EUR 등)
 * @param koreanName 한글 이름
 * @param symbol 통화 심볼 ($, ¥, € 등)
 * @param scale 소수점 자리수
 * @param needsMultiply 서버 데이터에 100을 곱해야 하는지 여부 (JPY, THB 등)
 * @param isPremium 프리미엄 전용 통화 여부
 */
data class Currency(
    val code: String,
    val koreanName: String,
    val symbol: String,
    val scale: Int,
    val needsMultiply: Boolean,
    val isPremium: Boolean
) {
    /**
     * 환율 포맷팅 (소수점 처리)
     */
    fun formatRate(rate: String?): String {
        if (rate.isNullOrEmpty() || rate == "0") return "0"
        val value = rate.toFloatOrNull() ?: 0f
        return String.format("%.${scale}f", value)
    }

    /**
     * 환전 금액 계산
     * @param money 원화 금액
     * @param rate 환율
     * @return 외화 금액
     */
    fun calculateExchangeMoney(money: String, rate: String): BigDecimal {
        return (BigDecimal(money) / BigDecimal(rate))
            .setScale(20, RoundingMode.HALF_UP)
    }

    /**
     * 매도 수익 계산
     * @param exchangeMoney 외화 금액
     * @param sellRate 매도 환율
     * @param krMoney 투자 원화
     * @return 수익금
     */
    fun calculateSellProfit(
        exchangeMoney: String,
        sellRate: String,
        krMoney: String
    ): BigDecimal {
        return ((BigDecimal(exchangeMoney) * BigDecimal(sellRate))
            .setScale(20, RoundingMode.HALF_UP)) - BigDecimal(krMoney)
    }

    /**
     * 예상 수익 계산
     * @param exchangeMoney 외화 금액
     * @param money 투자 원화
     * @param latestRate 현재 환율
     * @return 예상 수익금
     */
    fun calculateExpectedProfit(
        exchangeMoney: String,
        money: String,
        latestRate: String
    ): String {
        val profit = (BigDecimal(exchangeMoney) * BigDecimal(latestRate)) - BigDecimal(money)
        return profit.setScale(0, RoundingMode.DOWN).toString()
    }
}

/**
 * 전체 통화 관리 객체
 * ✅ 여기만 수정하면 전체 앱에 적용됨!
 */
object Currencies {
    // 기본 통화 (무료)
    val USD = Currency(
        code = "USD",
        koreanName = "달러",
        symbol = "$",
        scale = 2,
        needsMultiply = false,
        isPremium = false
    )

    val JPY = Currency(
        code = "JPY",
        koreanName = "엔화",
        symbol = "¥",
        scale = 2,
        needsMultiply = true,  // ✅ 100 곱하기
        isPremium = false
    )

    // 프리미엄 통화
    val EUR = Currency(
        code = "EUR",
        koreanName = "유로",
        symbol = "€",
        scale = 2,
        needsMultiply = false,
        isPremium = true
    )

    val GBP = Currency(
        code = "GBP",
        koreanName = "파운드",
        symbol = "£",
        scale = 2,
        needsMultiply = false,
        isPremium = true
    )

    val CNY = Currency(
        code = "CNY",
        koreanName = "위안",
        symbol = "¥",
        scale = 2,
        needsMultiply = false,
        isPremium = true
    )

    val AUD = Currency(
        code = "AUD",
        koreanName = "호주달러",
        symbol = "A$",
        scale = 2,
        needsMultiply = false,
        isPremium = true
    )

    val CAD = Currency(
        code = "CAD",
        koreanName = "캐나다달러",
        symbol = "C$",
        scale = 2,
        needsMultiply = false,
        isPremium = true
    )

    val CHF = Currency(
        code = "CHF",
        koreanName = "스위스프랑",
        symbol = "CHF",
        scale = 2,
        needsMultiply = false,
        isPremium = true
    )

    val HKD = Currency(
        code = "HKD",
        koreanName = "홍콩달러",
        symbol = "HK$",
        scale = 2,
        needsMultiply = false,
        isPremium = true
    )

    val SGD = Currency(
        code = "SGD",
        koreanName = "싱가포르달러",
        symbol = "S$",
        scale = 2,
        needsMultiply = false,
        isPremium = true
    )

    val NZD = Currency(
        code = "NZD",
        koreanName = "뉴질랜드달러",
        symbol = "NZ$",
        scale = 2,
        needsMultiply = false,
        isPremium = true
    )

    val THB = Currency(
        code = "THB",
        koreanName = "바트",
        symbol = "฿",
        scale = 2,
        needsMultiply = true,  // ✅ 100 곱하기
        isPremium = true
    )

    // ✅ 전체 통화 리스트 (순서대로 표시됨)
    val all = listOf(USD, JPY, EUR, GBP, CNY, AUD, CAD, CHF, HKD, SGD, NZD, THB)

    // 무료 통화만
    val free = all.filter { !it.isPremium }

    // 프리미엄 통화만
    val premium = all.filter { it.isPremium }

    /**
     * 코드로 통화 찾기
     */
    fun findByCode(code: String): Currency? = all.find { it.code == code }

    /**
     * 기존 CurrencyType Enum을 Currency로 변환 (하위 호환)
     */
    fun fromCurrencyType(type: CurrencyType): Currency {
        return when (type) {
            CurrencyType.USD -> USD
            CurrencyType.JPY -> JPY
        }
    }
}