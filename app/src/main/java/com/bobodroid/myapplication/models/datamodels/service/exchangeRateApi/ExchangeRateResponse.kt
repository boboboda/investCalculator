package com.bobodroid.myapplication.models.datamodels.service.exchangeRateApi

import com.squareup.moshi.Json

// ✅ 12개 통화 지원하도록 수정
data class ExchangeRates(
    @Json(name = "USD") val usd: String,
    @Json(name = "JPY") val jpy: String,
    @Json(name = "EUR") val eur: String,
    @Json(name = "GBP") val gbp: String,
    @Json(name = "CNY") val cny: String,
    @Json(name = "AUD") val aud: String,
    @Json(name = "CAD") val cad: String,
    @Json(name = "CHF") val chf: String,
    @Json(name = "HKD") val hkd: String,
    @Json(name = "SGD") val sgd: String,
    @Json(name = "NZD") val nzd: String,
    @Json(name = "THB") val thb: String
) {
    override fun toString(): String {
        return "(USD=$usd, JPY=$jpy, EUR=$eur, GBP=$gbp, CNY=$cny, AUD=$aud, CAD=$cad, CHF=$chf, HKD=$hkd, SGD=$sgd, NZD=$nzd, THB=$thb)"
    }

    // ✅ 통화 코드로 환율 가져오기
    fun getRate(currencyCode: String): String {
        return when (currencyCode) {
            "USD" -> usd
            "JPY" -> jpy
            "EUR" -> eur
            "GBP" -> gbp
            "CNY" -> cny
            "AUD" -> aud
            "CAD" -> cad
            "CHF" -> chf
            "HKD" -> hkd
            "SGD" -> sgd
            "NZD" -> nzd
            "THB" -> thb
            else -> "0"
        }
    }
}

// ✅ 12개 통화 지원하도록 수정
data class CurrencyChange(
    @Json(name = "USD") val usd: String,
    @Json(name = "JPY") val jpy: String,
    @Json(name = "EUR") val eur: String,
    @Json(name = "GBP") val gbp: String,
    @Json(name = "CNY") val cny: String,
    @Json(name = "AUD") val aud: String,
    @Json(name = "CAD") val cad: String,
    @Json(name = "CHF") val chf: String,
    @Json(name = "HKD") val hkd: String,
    @Json(name = "SGD") val sgd: String,
    @Json(name = "NZD") val nzd: String,
    @Json(name = "THB") val thb: String
) {
    // ✅ 통화 코드로 변화량 가져오기
    fun getChange(currencyCode: String): String {
        return when (currencyCode) {
            "USD" -> usd
            "JPY" -> jpy
            "EUR" -> eur
            "GBP" -> gbp
            "CNY" -> cny
            "AUD" -> aud
            "CAD" -> cad
            "CHF" -> chf
            "HKD" -> hkd
            "SGD" -> sgd
            "NZD" -> nzd
            "THB" -> thb
            else -> "0"
        }
    }
}

// 기본 응답 클래스: Exchange Rate 정보
data class ExchangeRateResponse(
    @Json(name = "exchangeRates") val exchangeRates: ExchangeRates,
    @Json(name = "_id") val id: String,
    @Json(name = "id") val uuid: String,
    @Json(name = "createAt") val createAt: String,
    @Json(name = "documentNumber") val documentNumber: Int,
    @Json(name = "__v") val version: Int
)

data class ExchangeRateDailyChange(
    @Json(name = "date") val date: String,
    @Json(name = "latestRate") val latestRate: ExchangeRates,
    @Json(name = "change") val change: CurrencyChange
)