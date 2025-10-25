// 파일 생성: domain/usecase/notification/ValidateAlertSettingsUseCase.kt

package com.bobodroid.myapplication.domain.usecase.notification

import com.bobodroid.myapplication.domain.entity.AlertValidationEntity
import com.bobodroid.myapplication.domain.entity.RecordAlertEntity
import javax.inject.Inject

/**
 * 알림 설정 검증 UseCase
 *
 * [기존 위치]
 * FcmAlarmViewModel 내부 검증 로직
 *
 * [변경 사항]
 * - 검증 로직을 UseCase로 분리
 * - 앱에서 먼저 검증 후 서버로 전송
 */
class ValidateAlertSettingsUseCase @Inject constructor() {

    /**
     * 수익률 알림 설정 검증
     *
     * @param recordAlerts 설정할 알림 리스트
     * @return 검증 결과
     */
    fun validateRecordAlerts(recordAlerts: List<RecordAlertEntity>): AlertValidationEntity {

        // ━━━━━━━━━━━━━━━━━━━━━━━━━━
        // 1. 활성화된 알림이 있는지 확인
        // ━━━━━━━━━━━━━━━━━━━━━━━━━━
        val enabledAlerts = recordAlerts.filter { it.enabled && it.profitPercent != null }

        if (enabledAlerts.isEmpty()) {
            return AlertValidationEntity(
                isValid = false,
                errorMessage = "활성화된 알림이 없습니다"
            )
        }

        // ━━━━━━━━━━━━━━━━━━━━━━━━━━
        // 2. 수익률 범위 검증 (-100% ~ +1000%)
        // ━━━━━━━━━━━━━━━━━━━━━━━━━━
        val invalidPercents = enabledAlerts.filter { record ->
            val percent = record.profitPercent ?: return@filter true
            percent < -100f || percent > 1000f
        }

        if (invalidPercents.isNotEmpty()) {
            return AlertValidationEntity(
                isValid = false,
                errorMessage = "수익률은 -100% ~ +1000% 범위로 설정해주세요"
            )
        }

        // ━━━━━━━━━━━━━━━━━━━━━━━━━━
        // 3. 중복 기록 확인
        // ━━━━━━━━━━━━━━━━━━━━━━━━━━
        val recordIds = enabledAlerts.map { it.recordId }
        val uniqueRecordIds = recordIds.distinct()

        if (recordIds.size != uniqueRecordIds.size) {
            return AlertValidationEntity(
                isValid = false,
                errorMessage = "중복된 기록이 있습니다"
            )
        }

        // ━━━━━━━━━━━━━━━━━━━━━━━━━━
        // 4. 모든 검증 통과
        // ━━━━━━━━━━━━━━━━━━━━━━━━━━
        return AlertValidationEntity(isValid = true)
    }

    /**
     * 목표환율 검증
     *
     * @param targetRate 목표환율
     * @param currentRate 현재환율
     * @return 검증 결과
     */
    fun validateTargetRate(targetRate: Float, currentRate: Float): AlertValidationEntity {

        // ━━━━━━━━━━━━━━━━━━━━━━━━━━
        // 1. 환율 범위 검증 (0보다 커야 함)
        // ━━━━━━━━━━━━━━━━━━━━━━━━━━
        if (targetRate <= 0f) {
            return AlertValidationEntity(
                isValid = false,
                errorMessage = "환율은 0보다 커야 합니다"
            )
        }

        // ━━━━━━━━━━━━━━━━━━━━━━━━━━
        // 2. 현재환율과 너무 가까운지 확인 (1% 이내)
        // ━━━━━━━━━━━━━━━━━━━━━━━━━━
        val difference = kotlin.math.abs(targetRate - currentRate)
        val percentDiff = (difference / currentRate) * 100

        if (percentDiff < 1f) {
            return AlertValidationEntity(
                isValid = false,
                errorMessage = "현재환율과 1% 이상 차이나는 값을 설정해주세요"
            )
        }

        return AlertValidationEntity(isValid = true)
    }
}