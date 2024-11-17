package com.bobodroid.myapplication.models.datamodels.service.UserApi

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Rate(
    val number: Int,
    val rate: Int
)

// 전체 응답 클래스 (메시지 포함)
@JsonClass(generateAdapter = true)
data class UserResponse(
    val message: String, // 메시지 필드 추가
    val data: UserData? = null// 데이터를 별도로 포함하는 구조로 변경
)

@JsonClass(generateAdapter = true)
data class UserData(
    val customId: String? = null,
    val deviceId: String? = null,
    val pin: String? = null,
    val createAt: String,
    val fcmToken: String,
    val usdHighRates: List<Rate>? = emptyList(),
    val usdLowRates: List<Rate>? = emptyList(),
    val jpyHighRates: List<Rate>? = emptyList(),
    val jpyLowRates: List<Rate>? = emptyList(),
    @Json(name = "_id") val id: String,
    @Json(name = "__v") val version: Int
)

// 업데이트 요청 데이터 클래스
@JsonClass(generateAdapter = true)
data class UserRatesUpdateRequest(
    val usdHighRates: List<Rate>? = null,
    val usdLowRates: List<Rate>? = null,
    val jpyHighRates: List<Rate>? = null,
    val jpyLowRates: List<Rate>? = null
)
