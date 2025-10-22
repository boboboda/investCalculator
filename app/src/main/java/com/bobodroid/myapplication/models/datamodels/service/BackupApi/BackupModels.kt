// app/src/main/java/com/bobodroid/myapplication/models/datamodels/service/BackupApi/BackupModels.kt
package com.bobodroid.myapplication.models.datamodels.service.BackupApi

import com.squareup.moshi.JsonClass

/**
 * 백업할 투자 기록 DTO
 */
@JsonClass(generateAdapter = true)
data class CurrencyRecordDto(
    val id: String,
    val currencyCode: String,
    val date: String,
    val money: String,
    val rate: String,
    val buyRate: String,
    val exchangeMoney: String,
    val profit: String,
    val expectProfit: String,
    val categoryName: String,
    val memo: String,
    val sellRate: String? = null,
    val sellProfit: String? = null,
    val sellDate: String? = null,
    val recordColor: Boolean
)

/**
 * 백업 요청 DTO
 */
@JsonClass(generateAdapter = true)
data class BackupRequest(
    val deviceId: String,
    val socialId: String? = null,
    val socialType: String? = null,
    val currencyRecords: List<CurrencyRecordDto>
)

/**
 * 백업 응답 데이터
 */
@JsonClass(generateAdapter = true)
data class BackupData(
    val deviceId: String,
    val recordCount: Int,
    val lastBackupAt: String
)

/**
 * 백업 응답 DTO
 */
@JsonClass(generateAdapter = true)
data class BackupResponse(
    val success: Boolean,
    val message: String,
    val data: BackupData? = null,
    val error: String? = null
)

/**
 * 복구 응답 데이터
 */
@JsonClass(generateAdapter = true)
data class RestoreData(
    val deviceId: String,
    val socialId: String? = null,
    val socialType: String? = null,
    val currencyRecords: List<CurrencyRecordDto>,
    val lastBackupAt: String,
    val recordCount: Int
)

/**
 * 복구 응답 DTO
 */
@JsonClass(generateAdapter = true)
data class RestoreResponse(
    val success: Boolean,
    val message: String,
    val data: RestoreData? = null,
    val error: String? = null
)

/**
 * 백업 통계 데이터
 */
@JsonClass(generateAdapter = true)
data class BackupStatsData(
    val exists: Boolean,
    val recordCount: Int,
    val lastBackupAt: String? = null,
    val createdAt: String? = null
)

/**
 * 백업 통계 응답 DTO
 */
@JsonClass(generateAdapter = true)
data class BackupStatsResponse(
    val success: Boolean,
    val data: BackupStatsData? = null,
    val error: String? = null
)