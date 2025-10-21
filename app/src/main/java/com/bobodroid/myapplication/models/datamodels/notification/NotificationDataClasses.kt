// app/src/main/java/com/bobodroid/myapplication/models/datamodels/notification/NotificationDataClasses.kt

package com.bobodroid.myapplication.models.datamodels.notification

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

// ==================== Response ====================

@JsonClass(generateAdapter = true)
data class NotificationSettingsResponse(
    val success: Boolean,
    val data: NotificationSettings?,
    val message: String?
)

@JsonClass(generateAdapter = true)
data class NotificationSettings(
    val deviceId: String,
    val globalEnabled: Boolean = true,

    // 채널별 설정
    val rateAlert: ChannelSettings = ChannelSettings(),
    val recordAlert: ChannelSettings = ChannelSettings(),
    val systemAlert: ChannelSettings = ChannelSettings(),

    // 조용한 시간대
    val quietHours: QuietHours = QuietHours(),

    // 조건 설정
    val conditions: NotificationConditions = NotificationConditions(),

    // 템플릿
    val template: NotificationTemplate = NotificationTemplate(),

    // 제한
    val maxDailyNotifications: Int = 20,
    val todayNotificationCount: Int = 0
)

@JsonClass(generateAdapter = true)
data class ChannelSettings(
    val enabled: Boolean = true,
    val sound: String = "default",
    val vibrate: Boolean = true,
    val showOnLockScreen: Boolean = true
)

@JsonClass(generateAdapter = true)
data class QuietHours(
    val enabled: Boolean = false,
    val startTime: String = "22:00",
    val endTime: String = "08:00",
    val quietDays: List<Int> = emptyList() // 0=일, 1=월, ..., 6=토
)

@JsonClass(generateAdapter = true)
data class NotificationConditions(
    val minRateChangePercent: Double = 0.0,
    val minProfitPercent: Double = 5.0,
    val recordAgeAlert: RecordAgeAlertSettings = RecordAgeAlertSettings(),
    val dailySummary: DailySummarySettings = DailySummarySettings(),
    val batchNotifications: Boolean = true,
    val batchIntervalMinutes: Int = 30
)

@JsonClass(generateAdapter = true)
data class RecordAgeAlertSettings(
    val alertDays: Int = 7,
    val alertTime: String = "09:00"
)

@JsonClass(generateAdapter = true)
data class DailySummarySettings(
    val summaryTime: String = "20:00"
)

@JsonClass(generateAdapter = true)
data class NotificationTemplate(
    val style: String = "detailed",
    val showEmoji: Boolean = true,
    val showChart: Boolean = false
)

// ==================== Request ====================

@JsonClass(generateAdapter = true)
data class UpdateNotificationSettingsRequest(
    val globalEnabled: Boolean? = null,
    val rateAlert: ChannelSettings? = null,
    val recordAlert: ChannelSettings? = null,
    val systemAlert: ChannelSettings? = null,
    val quietHours: QuietHours? = null,
    val conditions: NotificationConditions? = null,
    val template: NotificationTemplate? = null,
    val maxDailyNotifications: Int? = null
)

// ==================== History ====================

@JsonClass(generateAdapter = true)
data class NotificationHistoryResponse(
    val success: Boolean,
    val data: List<NotificationHistoryItem>,
    val count: Int
)

@JsonClass(generateAdapter = true)
data class NotificationHistoryItem(
    @Json(name = "_id") val id: String,
    val deviceId: String,
    val type: String,
    val priority: String,
    val title: String,
    val body: String,
    val data: Map<String, String>? = null,
    val status: String,
    val sentAt: String,
    val readAt: String? = null,
    val clickedAt: String? = null
)

// ==================== Stats ====================

@JsonClass(generateAdapter = true)
data class NotificationStatsResponse(
    val success: Boolean,
    val data: NotificationStats,
    val message: String? = null
)

@JsonClass(generateAdapter = true)
data class NotificationStats(
    val total: Int,
    val read: Int,
    val clicked: Int,
    val readRate: Double,
    val clickRate: Double
)

// ==================== Base ====================

@JsonClass(generateAdapter = true)
data class BaseResponse(
    val success: Boolean,
    val message: String
)

// ==================== Enums ====================

enum class NotificationType(val displayName: String) {
    RATE_ALERT("환율 알림"),
    PROFIT_ALERT("수익률 알림"),
    RECORD_AGE("매수 경과"),
    DAILY_SUMMARY("일일 요약"),
    WEEKLY_SUMMARY("주간 요약"),
    SYSTEM("시스템")
}

enum class NotificationStatus {
    SENT,
    READ,
    CLICKED,
    FAILED
}