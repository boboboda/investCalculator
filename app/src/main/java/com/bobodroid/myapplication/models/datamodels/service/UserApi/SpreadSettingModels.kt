package com.bobodroid.myapplication.models.datamodels.service.UserApi

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class SpreadSettingRequest(
    val currency: String,
    val buySpreadPercent: Double,
    val sellSpreadPercent: Double
)

@JsonClass(generateAdapter = true)
data class SpreadSettingResponse(
    val success: Boolean,
    val message: String? = null,
    val data: SpreadSettingResponseData? = null,
    val error: String? = null
)

@JsonClass(generateAdapter = true)
data class SpreadSettingResponseData(
    val deviceId: String? = null,
    val currency: String? = null,
    val spreadSetting: SpreadSettingJson? = null
)

@JsonClass(generateAdapter = true)
data class SpreadSettingJson(
    val buySpreadPercent: Double? = null,
    val sellSpreadPercent: Double? = null
)

@JsonClass(generateAdapter = true)
data class SpreadSettingsListResponse(
    val success: Boolean,
    val message: String? = null,
    val data: SpreadSettingsListData? = null,
    val error: String? = null
)

@JsonClass(generateAdapter = true)
data class SpreadSettingsListData(
    val spreadSettings: Map<String, SpreadSettingJson>? = null
)