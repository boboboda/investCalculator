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
    // 목표환율 관련
    val targetRateAddUseCase: TargetRateAddUseCase,
    val targetRateUpdateUseCase: TargetRateUpdateUseCase,
    val targetRateDeleteUseCase: TargetRateDeleteUseCase,

    // 알림 설정 관련
    val getNotificationSettingsUseCase: GetNotificationSettingsUseCase,
    val updateNotificationSettingsUseCase: UpdateNotificationSettingsUseCase,

    // 알림 히스토리 관련
    val getNotificationHistoryUseCase: GetNotificationHistoryUseCase,
    val markAsReadUseCase: MarkNotificationAsReadUseCase,

    // 알림 통계 관련
    val getNotificationStatsUseCase: GetNotificationStatsUseCase,

    // 테스트
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
            Log.d(TAG("TargetRateAddUseCase", "Input"),
                "deviceId: $deviceId, targetRates: $targetRates, type: $type, newRate: $newRate")

            val currentList = when (type) {
                is RateType.USD_HIGH -> targetRates.dollarHighRates
                is RateType.USD_LOW -> targetRates.dollarLowRates
                is RateType.JPY_HIGH -> targetRates.yenHighRates
                is RateType.JPY_LOW -> targetRates.yenLowRates
            }?.toMutableList() ?: mutableListOf()

            currentList.add(newRate)
            val sortedList = sortList(currentList, type.direction)

            val updatedRates = targetRates.copy().apply {
                when (type) {
                    is RateType.USD_HIGH -> dollarHighRates = sortedList
                    is RateType.USD_LOW -> dollarLowRates = sortedList
                    is RateType.JPY_HIGH -> yenHighRates = sortedList
                    is RateType.JPY_LOW -> yenLowRates = sortedList
                }
            }

            val updateRequest = when(type) {
                is RateType.USD_HIGH -> UserRatesUpdateRequest(usdHighRates = sortedList)
                is RateType.USD_LOW -> UserRatesUpdateRequest(usdLowRates = sortedList)
                is RateType.JPY_HIGH -> UserRatesUpdateRequest(jpyHighRates = sortedList)
                is RateType.JPY_LOW -> UserRatesUpdateRequest(jpyLowRates = sortedList)
            }

            UserApi.userService.updateUserRates(deviceId = deviceId, updateRequest)

            Result.Success(
                data = updatedRates,
                message = "목표환율이 추가되었습니다"
            )
        } catch (e: Exception) {
            Log.e(TAG("TargetRateAddUseCase", "Error"), "Error occurred", e)
            Result.Error(
                message = "목표환율 추가 중 오류가 발생했습니다",
                exception = e
            )
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
            val currentList = when (type) {
                is RateType.USD_HIGH -> targetRates.dollarHighRates
                is RateType.USD_LOW -> targetRates.dollarLowRates
                is RateType.JPY_HIGH -> targetRates.yenHighRates
                is RateType.JPY_LOW -> targetRates.yenLowRates
            }?.toMutableList() ?: mutableListOf()

            currentList.remove(deleteRate)
            val sortedList = sortList(currentList, type.direction)

            val updatedRates = targetRates.copy().apply {
                when (type) {
                    is RateType.USD_HIGH -> dollarHighRates = sortedList
                    is RateType.USD_LOW -> dollarLowRates = sortedList
                    is RateType.JPY_HIGH -> yenHighRates = sortedList
                    is RateType.JPY_LOW -> yenLowRates = sortedList
                }
            }

            val updateRequest = when(type) {
                is RateType.USD_HIGH -> UserRatesUpdateRequest(usdHighRates = sortedList)
                is RateType.USD_LOW -> UserRatesUpdateRequest(usdLowRates = sortedList)
                is RateType.JPY_HIGH -> UserRatesUpdateRequest(jpyHighRates = sortedList)
                is RateType.JPY_LOW -> UserRatesUpdateRequest(jpyLowRates = sortedList)
            }

            UserApi.userService.updateUserRates(deviceId = deviceId, updateRequest)

            Result.Success(
                data = updatedRates,
                message = "목표환율이 삭제되었습니다"
            )
        } catch (e: Exception) {
            Log.e(TAG("TargetRateDeleteUseCase", "Error"), "Error occurred", e)
            Result.Error(
                message = "목표환율 삭제 중 오류가 발생했습니다",
                exception = e
            )
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
            Log.d(TAG("GetNotificationSettings", "invoke"), "deviceId: $deviceId")

            val response = NotificationApi.service.getNotificationSettings(deviceId)

            if (response.success && response.data != null) {
                Log.d(TAG("GetNotificationSettings", "success"), "설정 조회 성공")
                Result.Success(
                    data = response.data,
                    message = "설정 조회 성공"
                )
            } else {
                Log.e(TAG("GetNotificationSettings", "error"), response.message ?: "Unknown error")
                Result.Error(
                    message = response.message ?: "설정 조회 실패",
                    exception = Exception(response.message)
                )
            }
        } catch (e: Exception) {
            Log.e(TAG("GetNotificationSettings", "exception"), "Exception", e)
            Result.Error(
                message = "네트워크 오류: ${e.message}",
                exception = e
            )
        }
    }
}

class UpdateNotificationSettingsUseCase @Inject constructor() {
    suspend operator fun invoke(
        deviceId: String,
        settings: UpdateNotificationSettingsRequest
    ): Result<NotificationSettings> {
        return try {
            Log.d(TAG("UpdateNotificationSettings", "invoke"),
                "deviceId: $deviceId, settings: $settings")

            val response = NotificationApi.service.updateNotificationSettings(
                deviceId,
                settings
            )

            if (response.success && response.data != null) {
                Log.d(TAG("UpdateNotificationSettings", "success"), "설정 업데이트 성공")
                Result.Success(
                    data = response.data,
                    message = "설정이 업데이트되었습니다"
                )
            } else {
                Log.e(TAG("UpdateNotificationSettings", "error"), response.message ?: "Unknown error")
                Result.Error(
                    message = response.message ?: "설정 업데이트 실패",
                    exception = Exception(response.message)
                )
            }
        } catch (e: Exception) {
            Log.e(TAG("UpdateNotificationSettings", "exception"), "Exception", e)
            Result.Error(
                message = "네트워크 오류: ${e.message}",
                exception = e
            )
        }
    }
}

// ==================== 알림 히스토리 UseCase ====================

class GetNotificationHistoryUseCase @Inject constructor() {
    suspend operator fun invoke(
        deviceId: String,
        limit: Int = 50
    ): Result<List<NotificationHistoryItem>> {
        return try {
            Log.d(TAG("GetNotificationHistory", "invoke"),
                "deviceId: $deviceId, limit: $limit")

            val response = NotificationApi.service.getNotificationHistory(deviceId, limit)

            if (response.success) {
                Log.d(TAG("GetNotificationHistory", "success"),
                    "히스토리 조회 성공: ${response.count}개")
                Result.Success(
                    data = response.data,
                    message = "히스토리 조회 성공"
                )
            } else {
                Log.e(TAG("GetNotificationHistory", "error"), "히스토리 조회 실패")
                Result.Error(
                    message = "히스토리 조회 실패",
                    exception = Exception("히스토리 조회 실패")
                )
            }
        } catch (e: Exception) {
            Log.e(TAG("GetNotificationHistory", "exception"), "Exception", e)
            Result.Error(
                message = "네트워크 오류: ${e.message}",
                exception = e
            )
        }
    }
}

class MarkNotificationAsReadUseCase @Inject constructor() {
    suspend operator fun invoke(notificationId: String): Result<Unit> {
        return try {
            Log.d(TAG("MarkNotificationAsRead", "invoke"), "notificationId: $notificationId")

            val response = NotificationApi.service.markAsRead(notificationId)

            if (response.success) {
                Log.d(TAG("MarkNotificationAsRead", "success"), "읽음 처리 성공")
                Result.Success(
                    data = Unit,
                    message = "읽음 처리 완료"
                )
            } else {
                Log.e(TAG("MarkNotificationAsRead", "error"), response.message)
                Result.Error(
                    message = response.message,
                    exception = Exception(response.message)
                )
            }
        } catch (e: Exception) {
            Log.e(TAG("MarkNotificationAsRead", "exception"), "Exception", e)
            Result.Error(
                message = "네트워크 오류: ${e.message}",
                exception = e
            )
        }
    }
}

// ==================== 알림 통계 UseCase ====================

class GetNotificationStatsUseCase @Inject constructor() {
    suspend operator fun invoke(deviceId: String): Result<NotificationStats> {
        return try {
            Log.d(TAG("GetNotificationStats", "invoke"), "deviceId: $deviceId")

            val response = NotificationApi.service.getNotificationStats(deviceId)

            if (response.success) {
                Log.d(TAG("GetNotificationStats", "success"), "통계 조회 성공")
                Result.Success(
                    data = response.data,
                    message = "통계 조회 성공"
                )
            } else {
                Log.e(TAG("GetNotificationStats", "error"), "통계 조회 실패")
                Result.Error(
                    message = "통계 조회 실패",
                    exception = Exception("통계 조회 실패")
                )
            }
        } catch (e: Exception) {
            Log.e(TAG("GetNotificationStats", "exception"), "Exception", e)
            Result.Error(
                message = "네트워크 오류: ${e.message}",
                exception = e
            )
        }
    }
}

// ==================== 테스트 알림 UseCase ====================

class SendTestNotificationUseCase @Inject constructor() {
    suspend operator fun invoke(deviceId: String): Result<Unit> {
        return try {
            Log.d(TAG("SendTestNotification", "invoke"), "deviceId: $deviceId")

            val response = NotificationApi.service.sendTestNotification(deviceId)

            if (response.success) {
                Log.d(TAG("SendTestNotification", "success"), "테스트 알림 전송 성공")
                Result.Success(
                    data = Unit,
                    message = "테스트 알림이 전송되었습니다"
                )
            } else {
                Log.e(TAG("SendTestNotification", "error"), response.message)
                Result.Error(
                    message = response.message,
                    exception = Exception(response.message)
                )
            }
        } catch (e: Exception) {
            Log.e(TAG("SendTestNotification", "exception"), "Exception", e)
            Result.Error(
                message = "네트워크 오류: ${e.message}",
                exception = e
            )
        }
    }
}

// ==================== Helper 함수 ====================

private fun sortList(list: List<Rate>, direction: RateDirection): List<Rate> {
    return list
        .sortedBy { it.rate }
        .let { sorted ->
            when (direction) {
                RateDirection.HIGH -> sorted
                RateDirection.LOW -> sorted.reversed()
            }
        }
        .mapIndexed { index, rate ->
            rate.copy(number = (index + 1))
        }
}