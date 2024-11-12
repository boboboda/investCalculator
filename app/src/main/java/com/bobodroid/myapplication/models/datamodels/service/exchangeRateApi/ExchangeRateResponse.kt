package com.bobodroid.myapplication.models.datamodels.service.exchangeRateApi

import com.squareup.moshi.Json

// 공통 환율 데이터
data class ExchangeRates(
    @Json(name = "USD") val usd: String,
    @Json(name = "JPY") val jpy: String
)

// 공통 Rates 데이터 클래스 (평균, 최대, 최소값을 위해 사용)
data class Rates(
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

// 일별 통계 응답 클래스
data class DailyStatsResponse(
    @Json(name = "_id") val id: String,
    @Json(name = "date") val date: String,
    @Json(name = "type") val type: String,
    @Json(name = "averageRates") val averageRates: Rates,
    @Json(name = "dataPoints") val dataPoints: Int,
    @Json(name = "maxRates") val maxRates: Rates,
    @Json(name = "minRates") val minRates: Rates,
    @Json(name = "month") val month: String,
    @Json(name = "updatedAt") val updatedAt: String
)

// 월별 통계 응답 클래스
data class MonthlyStatsResponse(
    @Json(name = "_id") val id: String,
    @Json(name = "date") val date: String,
    @Json(name = "type") val type: String,
    @Json(name = "averageRates") val averageRates: Rates,
    @Json(name = "dataPoints") val dataPoints: Int,
    @Json(name = "maxRates") val maxRates: Rates,
    @Json(name = "minRates") val minRates: Rates,
    @Json(name = "month") val month: String,
    @Json(name = "updatedAt") val updatedAt: String
)
