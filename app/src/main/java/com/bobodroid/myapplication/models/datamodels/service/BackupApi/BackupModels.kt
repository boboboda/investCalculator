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
 * 백업할 여행모드 - 여행 중 지출 DTO
 */
@JsonClass(generateAdapter = true)
data class TripExpenseDto(
    val id: String,
    val tripId: String,
    val amount: String,
    val krwAmount: String,
    val categoryName: String,
    val memo: String,
    val date: String
)

/**
 * 백업할 여행모드 - 여행(트립) DTO
 */
@JsonClass(generateAdapter = true)
data class TripDto(
    val id: String,
    val tripName: String,
    val currencyCode: String,
    val startDate: String,
    val endDate: String,
    val budgetWon: String? = null,
    val averageExchangeRate: String,
    val isCompleted: Boolean,
    val createdAt: String,
    val expenses: List<TripExpenseDto>
)

/**
 * 백업 요청 DTO
 */
@JsonClass(generateAdapter = true)
data class BackupRequest(
    val deviceId: String,
    val socialId: String? = null,
    val socialType: String? = null,
    val currencyRecords: List<CurrencyRecordDto>,
    // 여행모드 데이터 (2026-10 추가)
    val trips: List<TripDto> = emptyList(),
    // 이 기기가 마지막으로 동기화했을 때 서버가 알려준 lastBackupAt (서버가 덮어쓰기 안전 여부를 판단)
    val syncBaseAt: String? = null,
    // 사용자가 "이 기기 기록으로 덮어쓰기"를 선택한 경우에만 true
    val force: Boolean = false
)

/**
 * 백업 응답 데이터
 */
@JsonClass(generateAdapter = true)
data class BackupData(
    val deviceId: String,
    val recordCount: Int,
    val tripCount: Int = 0,
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
    val error: String? = null,
    // 보류 사유 코드 (서버 기록이 이 기기와 달라 저장하지 않은 경우 "SYNC_REQUIRED")
    val code: String? = null
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
    val trips: List<TripDto> = emptyList(),
    val lastBackupAt: String,
    val recordCount: Int,
    val tripCount: Int = 0
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
    val tripCount: Int = 0,
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
