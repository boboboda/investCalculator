package com.bobodroid.myapplication.models.datamodels.service.exchangeRateApi

import com.squareup.moshi.Json

//// 공통 환율 데이터
//data class ExchangeRates(
//    @Json(name = "USD") val usd: String,
//    @Json(name = "JPY") val jpy: String
//)
//
//// 공통 Rates 데이터 클래스 (평균, 최대, 최소값을 위해 사용)
//data class Rates(
//    @Json(name = "USD") val usd: String,
//    @Json(name = "JPY") val jpy: String
//)

// 공통 환율 데이터
data class ExchangeRates(
    @Json(name = "USD") val usd: String,
    @Json(name = "JPY") val jpy: String
) {
    override fun toString(): String {
        return "(USD=$usd, JPY=$jpy)"
    }
}
data class CurrencyChange(
    @Json(name = "USD") val usd: String,
    @Json(name = "JPY") val jpy: String
)

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