package com.bobodroid.myapplication.models.datamodels.service.UserApi

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class UserRequest(
    val deviceId: String,
    val fcmToken: String,

    // ✅ 소셜 로그인 관련 필드 추가
    val socialId: String? = null,
    val socialType: String? = null,  // "GOOGLE", "KAKAO", "NONE"
    val email: String? = null,
    val nickname: String? = null,
    val profileUrl: String? = null
)

@JsonClass(generateAdapter = true)
data class AuthUser(
    val customId: String,
    val pin: String
)