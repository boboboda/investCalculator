// app/src/main/java/com/bobodroid/myapplication/models/datamodels/useCases/FcmUseCases.kt

package com.bobodroid.myapplication.models.datamodels.useCases

import android.util.Log
import com.bobodroid.myapplication.MainActivity.Companion.TAG
import com.bobodroid.myapplication.models.datamodels.notification.*
import com.bobodroid.myapplication.models.datamodels.roomDb.RateDirection
import com.bobodroid.myapplication.models.datamodels.roomDb.RateType
import com.bobodroid.myapplication.models.datamodels.roomDb.TargetRates
import com.bobodroid.myapplication.models.datamodels.service.UserApi.Rate
import com.bobodroid.myapplication.models.datamodels.service.UserApi.UserApi
import com.bobodroid.myapplication.models.datamodels.service.UserApi.UserRatesUpdateRequest
import com.bobodroid.myapplication.models.datamodels.service.notificationApi.NotificationApi
import com.bobodroid.myapplication.models.datamodels.websocket.WebSocketClient
import com.bobodroid.myapplication.util.result.Result
import javax.inject.Inject

// ==================== FCM 유즈케이스 컨테이너 ====================

class FcmUseCases @Inject constructor(
    val targetRateAddUseCase: TargetRateAddUseCase,
    val targetRateUpdateUseCase: TargetRateUpdateUseCase,
    val targetRateDeleteUseCase: TargetRateDeleteUseCase,
    val getNotificationSettingsUseCase: GetNotificationSettingsUseCase,
    val updateNotificationSettingsUseCase: UpdateNotificationSettingsUseCase,
    val getNotificationHistoryUseCase: GetNotificationHistoryUseCase,
    val markAsReadUseCase: MarkNotificationAsReadUseCase,
    val markAsClickedUseCase: MarkNotificationAsClickedUseCase,
    val getNotificationStatsUseCase: GetNotificationStatsUseCase,
    val sendTestNotificationUseCase: SendTestNotificationUseCase
)

// ==================== 목표환율 UseCase ====================

class TargetRateAddUseCase @Inject constructor() {
    suspend operator fun invoke(
        deviceId: String,
        targetRates: TargetRates,
        type: RateType,
        newRate: Rate
    ): Result<TargetRates> {
        return try {
            Log.d(TAG("TargetRateAdd", "Input"), "currency: ${type.currency.code}, direction: ${type.direction}, rate: ${newRate.rate}")

            // 현재 목표환율 리스트 가져오기
            val currentList = targetRates.getRates(type).toMutableList()

            // 새 목표환율 추가
            currentList.add(newRate)

            // 정렬
            val sortedList = sortList(currentList, type.direction)

            // 업데이트된 TargetRates 생성
            val updatedRates = targetRates.setRates(type, sortedList)

            // 서버에 업데이트 요청
            val updateRequest = UserRatesUpdateRequest.forCurrency(
                currency = type.currency,
                high = if (type.direction == RateDirection.HIGH) sortedList else null,
                low = if (type.direction == RateDirection.LOW) sortedList else null
            )

            UserApi.userService.updateUserRates(deviceId = deviceId, updateRequest)

            Result.Success(
                data = updatedRates,
                message = "${type.currency.koreanName} 목표환율이 추가되었습니다"
            )
        } catch (e: Exception) {
            Log.e(TAG("TargetRateAdd", "Error"), "Error", e)
            Result.Error(
                message = "목표환율 추가 중 오류가 발생했습니다",
                exception = e
            )
        }
    }

    private fun sortList(list: List<Rate>, direction: RateDirection): List<Rate> {
        val sorted = when (direction) {
            RateDirection.HIGH -> list.sortedBy { it.rate }
            RateDirection.LOW -> list.sortedByDescending { it.rate }
        }
        return sorted.mapIndexed { index, rate ->
            rate.copy(number = index + 1)
        }
    }
}

class TargetRateDeleteUseCase @Inject constructor() {
    suspend operator fun invoke(
        deviceId: String,
        targetRates: TargetRates,
        type: RateType,
        deleteRate: Rate
    ): Result<TargetRates> {
        return try {
            Log.d(TAG("TargetRateDelete", "Input"), "currency: ${type.currency.code}, direction: ${type.direction}, rate: ${deleteRate.rate}")

            // 현재 목표환율 리스트 가져오기
            val currentList = targetRates.getRates(type).toMutableList()

            // 목표환율 삭제
            currentList.remove(deleteRate)

            // 정렬
            val sortedList = sortList(currentList, type.direction)

            // 업데이트된 TargetRates 생성
            val updatedRates = targetRates.setRates(type, sortedList)

            // 서버에 업데이트 요청
            val updateRequest = UserRatesUpdateRequest.forCurrency(
                currency = type.currency,
                high = if (type.direction == RateDirection.HIGH) sortedList else null,
                low = if (type.direction == RateDirection.LOW) sortedList else null
            )

            UserApi.userService.updateUserRates(deviceId = deviceId, updateRequest)

            Result.Success(
                data = updatedRates,
                message = "${type.currency.koreanName} 목표환율이 삭제되었습니다"
            )
        } catch (e: Exception) {
            Log.e(TAG("TargetRateDelete", "Error"), "Error", e)
            Result.Error(
                message = "목표환율 삭제 중 오류가 발생했습니다",
                exception = e
            )
        }
    }

    private fun sortList(list: List<Rate>, direction: RateDirection): List<Rate> {
        val sorted = when (direction) {
            RateDirection.HIGH -> list.sortedBy { it.rate }
            RateDirection.LOW -> list.sortedByDescending { it.rate }
        }
        return sorted.mapIndexed { index, rate ->
            rate.copy(number = index + 1)
        }
    }
}

class TargetRateUpdateUseCase @Inject constructor(
    private val webSocketClient: WebSocketClient
) {
    suspend operator fun invoke(onUpdate: (TargetRates) -> Unit) {
        webSocketClient.targetRateUpdateReceiveData { userDataString ->
            val targetRates = com.bobodroid.myapplication.models.datamodels.service.UserApi.UserResponseData.fromTargetRateJson(userDataString)
            if(targetRates != null) {
                onUpdate(targetRates)
            }
        }
    }
}

// ==================== 알림 설정 UseCase ====================

class GetNotificationSettingsUseCase @Inject constructor() {
    suspend operator fun invoke(deviceId: String): Result<NotificationSettings> {
        return try {
            val response = NotificationApi.service.getNotificationSettings(deviceId)

            if (response.success && response.data != null) {
                Result.Success(data = response.data, message = "설정 조회 성공")
            } else {
                Result.Error(
                    message = response.message ?: "설정 조회 실패",
                    exception = Exception(response.message)
                )
            }
        } catch (e: Exception) {
            Result.Error(message = "설정 조회 중 오류가 발생했습니다", exception = e)
        }
    }
}

class UpdateNotificationSettingsUseCase @Inject constructor() {
    suspend operator fun invoke(
        deviceId: String,
        request: UpdateNotificationSettingsRequest
    ): Result<NotificationSettings> {
        return try {
            val response = NotificationApi.service.updateNotificationSettings(deviceId, request)

            if (response.success && response.data != null) {
                Result.Success(data = response.data, message = "설정 업데이트 성공")
            } else {
                Result.Error(
                    message = response.message ?: "설정 업데이트 실패",
                    exception = Exception(response.message)
                )
            }
        } catch (e: Exception) {
            Result.Error(message = "설정 업데이트 중 오류가 발생했습니다", exception = e)
        }
    }
}

// ==================== 알림 히스토리 UseCase ====================

class GetNotificationHistoryUseCase @Inject constructor() {
    suspend operator fun invoke(deviceId: String, limit: Int = 50): Result<List<NotificationHistoryItem>> {
        return try {
            val response = NotificationApi.service.getNotificationHistory(deviceId, limit)

            if (response.success) {
                Result.Success(data = response.data, message = "히스토리 조회 성공")
            } else {
                Result.Error(
                    message = "히스토리 조회 실패",
                    exception = Exception("히스토리 조회 실패")
                )
            }
        } catch (e: Exception) {
            Result.Error(message = "히스토리 조회 중 오류가 발생했습니다", exception = e)
        }
    }
}

class MarkNotificationAsReadUseCase @Inject constructor() {
    suspend operator fun invoke(notificationId: String): Result<Unit> {
        return try {
            val response = NotificationApi.service.markAsRead(notificationId)

            if (response.success) {
                Result.Success(data = Unit, message = "읽음 처리 성공")
            } else {
                Result.Error(
                    message = response.message ?: "읽음 처리 실패",
                    exception = Exception(response.message)
                )
            }
        } catch (e: Exception) {
            Result.Error(message = "읽음 처리 중 오류가 발생했습니다", exception = e)
        }
    }
}

// ✅ 알림 클릭 처리 UseCase (신규 추가)
class MarkNotificationAsClickedUseCase @Inject constructor() {
    suspend operator fun invoke(notificationId: String): Result<Unit> {
        return try {
            val response = NotificationApi.service.markAsClicked(notificationId)

            if (response.success) {
                Result.Success(data = Unit, message = "클릭 처리 성공")
            } else {
                Result.Error(
                    message = response.message ?: "클릭 처리 실패",
                    exception = Exception(response.message)
                )
            }
        } catch (e: Exception) {
            Result.Error(message = "클릭 처리 중 오류가 발생했습니다", exception = e)
        }
    }
}

// ==================== 알림 통계 UseCase ====================

class GetNotificationStatsUseCase @Inject constructor() {
    suspend operator fun invoke(deviceId: String): Result<NotificationStats> {
        return try {
            val response = NotificationApi.service.getNotificationStats(deviceId)

            if (response.success && response.data != null) {
                Result.Success(data = response.data, message = "통계 조회 성공")
            } else {
                Result.Error(
                    message = response.message ?: "통계 조회 실패",
                    exception = Exception(response.message)
                )
            }
        } catch (e: Exception) {
            Result.Error(message = "통계 조회 중 오류가 발생했습니다", exception = e)
        }
    }
}

// ==================== 테스트 UseCase ====================

class SendTestNotificationUseCase @Inject constructor() {
    suspend operator fun invoke(deviceId: String): Result<Unit> {
        return try {
            val response = NotificationApi.service.sendTestNotification(deviceId)

            if (response.success) {
                Result.Success(data = Unit, message = "테스트 알림이 전송되었습니다")
            } else {
                Result.Error(
                    message = response.message ?: "테스트 알림 전송 실패",
                    exception = Exception(response.message)
                )
            }
        } catch (e: Exception) {
            Result.Error(message = "테스트 알림 전송 중 오류가 발생했습니다", exception = e)
        }
    }
}