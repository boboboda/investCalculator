// 파일 생성: domain/usecase/notification/CalculateRecordAgeUseCase.kt

package com.bobodroid.myapplication.domain.usecase.notification

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import javax.inject.Inject

/**
 * 기록 경과 일수 계산 UseCase
 *
 * [용도]
 * - "N일째 보유 중" 표시
 * - 매수 경과 알림 조건 체크
 */
class CalculateRecordAgeUseCase @Inject constructor() {

    /**
     * 매수일로부터 경과 일수 계산
     *
     * @param buyDate 매수일 ("2025-01-15")
     * @return 경과 일수
     */
    fun execute(buyDate: String): Int {
        return try {
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
            val buy = LocalDate.parse(buyDate, formatter)
            val today = LocalDate.now()

            ChronoUnit.DAYS.between(buy, today).toInt()
        } catch (e: Exception) {
            0  // 날짜 파싱 실패 시 0 반환
        }
    }

    /**
     * 경과 일수를 텍스트로 변환
     *
     * @param days 경과 일수
     * @return 텍스트 (예: "7일째 보유 중", "3개월째 보유 중")
     */
    fun formatAge(days: Int): String {
        return when {
            days == 0 -> "오늘 매수"
            days < 30 -> "${days}일째 보유 중"
            days < 365 -> "${days / 30}개월째 보유 중"
            else -> "${days / 365}년 ${(days % 365) / 30}개월째 보유 중"
        }
    }
}