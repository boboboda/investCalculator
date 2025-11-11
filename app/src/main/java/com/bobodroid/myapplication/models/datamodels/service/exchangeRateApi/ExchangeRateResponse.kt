package com.bobodroid.myapplication.models.datamodels.service.exchangeRateApi

import com.squareup.moshi.Json

// ✅ 12개 통화 지원 - 신규 통화는 Optional (기본값 "0")
data class ExchangeRates(
    @Json(name = "USD") val usd: String,
    @Json(name = "JPY") val jpy: String,
    @Json(name = "EUR") val eur: String = "0",
    @Json(name = "GBP") val gbp: String = "0",
    @Json(name = "CNY") val cny: String = "0",
    @Json(name = "AUD") val aud: String = "0",
    @Json(name = "CAD") val cad: String = "0",
    @Json(name = "CHF") val chf: String = "0",
    @Json(name = "HKD") val hkd: String = "0",
    @Json(name = "SGD") val sgd: String = "0",
    @Json(name = "NZD") val nzd: String = "0",
    @Json(name = "THB") val thb: String = "0"
) {
    override fun toString(): String {
        return "(USD=$usd, JPY=$jpy, EUR=$eur, GBP=$gbp, CNY=$cny, AUD=$aud, CAD=$cad, CHF=$chf, HKD=$hkd, SGD=$sgd, NZD=$nzd, THB=$thb)"
    }

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

data class CurrencyChange(
    @Json(name = "USD") val usd: String,
    @Json(name = "JPY") val jpy: String,
    @Json(name = "EUR") val eur: String = "0",
    @Json(name = "GBP") val gbp: String = "0",
    @Json(name = "CNY") val cny: String = "0",
    @Json(name = "AUD") val aud: String = "0",
    @Json(name = "CAD") val cad: String = "0",
    @Json(name = "CHF") val chf: String = "0",
    @Json(name = "HKD") val hkd: String = "0",
    @Json(name = "SGD") val sgd: String = "0",
    @Json(name = "NZD") val nzd: String = "0",
    @Json(name = "THB") val thb: String = "0"
) {
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