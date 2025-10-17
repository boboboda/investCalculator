package com.bobodroid.myapplication.models.datamodels.service.noticeApi

import com.squareup.moshi.Json


data class NoticeResponse (
    val message: String,
    @Json(name = "data")
    val data: Notice
)

data class Notice (
    val writer: String,
    val title: String,
    val content: String,
    @Json(name = "created_at")
    val createdAt: String
)
