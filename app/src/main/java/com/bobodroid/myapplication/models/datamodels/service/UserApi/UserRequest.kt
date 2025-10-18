package com.bobodroid.myapplication.models.datamodels.service.UserApi

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class UserRequest(
    val deviceId: String,
    val socialId: String? = null,
    val socialType: String? = null,  // "GOOGLE", "KAKAO", "NONE"
    val email: String? = null,
    val nickname: String? = null,
    val profileUrl: String? = null,
    val fcmToken: String? = null,
    // 목표 환율
    val usdHighRates: List<Rate>? = null,
    val usdLowRates: List<Rate>? = null,
    val jpyHighRates: List<Rate>? = null,
    val jpyLowRates: List<Rate>? = null
)

@JsonClass(generateAdapter = true)
data class AuthUser(
    val customId: String,
    val pin: String
)