package com.bobodroid.myapplication.models.datamodels.response

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * 뉴스 아이템
 */
@JsonClass(generateAdapter = true)
data class NewsItem(
    @Json(name = "newsId")
    val newsId: String,

    @Json(name = "title")
    val title: String,

    @Json(name = "summary")
    val summary: String,

    @Json(name = "source")
    val source: String,

    @Json(name = "publishedAt")
    val publishedAt: String,

    @Json(name = "url")
    val url: String,

    @Json(name = "imageUrl")
    val imageUrl: String? = null,

    @Json(name = "sentiment")
    val sentiment: Double = 0.0,

    @Json(name = "topics")
    val topics: List<String> = emptyList(),

    @Json(name = "tickers")
    val tickers: List<String> = emptyList()
)

/**
 * 최신 뉴스 응답
 */
@JsonClass(generateAdapter = true)
data class LatestNewsResponse(
    @Json(name = "success")
    val success: Boolean,

    @Json(name = "message")
    val message: String,

    @Json(name = "data")
    val data: List<NewsItem>
)