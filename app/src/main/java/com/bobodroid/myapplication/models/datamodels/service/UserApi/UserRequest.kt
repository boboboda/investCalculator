package com.bobodroid.myapplication.models.datamodels.service.UserApi

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class UserRequest(
    val customId: String,
    val deviceId: String,
    val pin: String,
    val fcmToken: String
)