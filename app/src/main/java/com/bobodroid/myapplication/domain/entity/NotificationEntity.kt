// 파일 생성: domain/entity/NotificationEntity.kt

package com.bobodroid.myapplication.domain.entity

/**
 * 기록별 수익률 알림 설정 Entity
 *
 * [기존 위치]
 * FcmAlarmViewModel.RecordWithAlert
 *
 * [변경 사항]
 * - domain 패키지로 이동
 */
data class RecordAlertEntity(
    val recordId: String,
    val currencyCode: String,
    val categoryName: String,
    val date: String,
    val money: String,                  // 투자금
    val exchangeMoney: String,          // 보유량
    val buyRate: String,                // 매수 환율
    val profitPercent: Float?,          // 목표 수익률 (null이면 미설정)
    val enabled: Boolean = false        // 알림 활성화 여부
)

/**
 * 알림 검증 결과 Entity
 */
data class AlertValidationEntity(
    val isValid: Boolean,
    val errorMessage: String? = null
)