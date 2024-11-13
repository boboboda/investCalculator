package com.bobodroid.myapplication.models.datamodels.service.UserApi

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Rate(
    val number: Int,
    val rate: Int
)

// 전체 응답 클래스
@JsonClass(generateAdapter = true)
data class UserResponse(
    val customId: String,
    val deviceId: String,
    val pin: String,
    val createAt: String,
    val fcmToken: String,
    val usdHighRates: List<Rate>,
    val usdLowRates: List<Rate>,
    val jpyHighRates: List<Rate>,
    val jpyLowRates: List<Rate>,
    @Json(name = "_id") val id: String,
    @Json(name = "__v") val version: Int
)


// 업데이트 요청 데이터 클래스
@JsonClass(generateAdapter = true)
data class UserRatesUpdateRequest(
    val usdHighRates: List<Rate>,
    val usdLowRates: List<Rate>,
    val jpyHighRates: List<Rate>,
    val jpyLowRates: List<Rate>
)