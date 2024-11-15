package com.bobodroid.myapplication.models.datamodels.useCases

import android.util.Log
import com.bobodroid.myapplication.MainActivity.Companion.TAG
import com.bobodroid.myapplication.billing.BillingClientLifecycle.Companion.TAG
import com.bobodroid.myapplication.fcm.FCMTokenEvent
import com.bobodroid.myapplication.models.datamodels.repository.UserRepository
import com.bobodroid.myapplication.models.datamodels.roomDb.LocalUserData
import com.bobodroid.myapplication.models.datamodels.service.UserApi.UserApi
import com.bobodroid.myapplication.models.datamodels.service.UserApi.UserRequest
import com.bobodroid.myapplication.util.InvestApplication
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject
import com.bobodroid.myapplication.util.result.Result
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class UserUseCases(
    val logIn: LogInUseCase,
    val logout: LogoutUseCase,
    val localExistCheck: LocalExistCheckUseCase,
    val deleteUser: DeleteUserUseCase,
    val localUserUpdate: LocalUserUpdate,
    val customIdCreateUser: CustomIdCreateUser
)


class LocalIdAddUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(): Result<LocalUserData> {
        return try {
            val fcmToken = FCMTokenEvent.tokenFlow.filterNotNull().first()

            val createLocalUser = LocalUserData(
                userResetDate = "",
                rateAdCount = 0,
                rateResetCount = 3,
                fcmToken = fcmToken
            )

            userRepository.localUserAdd(createLocalUser).let { result ->
                when (result) {
                    is Result.Success -> {
                        Result.Success(
                            data = result.data,
                            message = "새로운 사용자가 생성되었습니다."
                        )
                    }
                    is Result.Error -> {
                        Result.Error(
                            message = "사용자 생성 실패: ${result.message}",
                            exception = result.exception
                        )
                    }
                    is Result.Loading -> Result.Loading
                }
            }
        } catch (e: Exception) {
            Result.Error(
                message = "사용자 생성 중 오류가 발생했습니다.",
                exception = e
            )
        }
    }
}

class LocalUserUpdate @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(localUserData: LocalUserData): Result<LocalUserData> {
        return userRepository.localUserUpdate(localUserData)
    }
}

class CustomIdCreateUser @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(localUserData: LocalUserData) {

    }
}

class LogInUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(id: String, pin: String, onSuccess: (String) -> Unit) {


    }
}

class LogoutUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(onLogout: (String) -> Unit) {
        val updatedUser = LocalUserData(customId = "")
        userRepository.localUserUpdate(updatedUser)
        onLogout("로그아웃되었습니다.")
    }
}


class DeleteUserUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(): Result<Unit> {
        return when (val deleteResult = userRepository.localUserDataDelete()) {
            is Result.Success -> Result.Success(
                data = Unit,
                message = "사용자 데이터가 삭제되었습니다."
            )
            is Result.Error -> Result.Error(
                message = deleteResult.message,
                exception = deleteResult.exception
            )
            is Result.Loading -> Result.Loading
        }
    }
}

class LocalExistCheckUseCase @Inject constructor(
    private val userRepository: UserRepository,
    private val localIdAddUseCase: LocalIdAddUseCase
) {
    suspend operator fun invoke(): Result<LocalUserData> {
        return try {

            val fcmToken = FCMTokenEvent.tokenFlow.firstOrNull()
                ?: InvestApplication.prefs.getData("fcm_token", "")
            Log.d(TAG("userUseCase", "LocalExistCheckUseCase"), "실행")

            // 기존 사용자 확인
            val existingUserResult = userRepository.localUserDataGet()
                .distinctUntilChanged()
                .firstOrNull()

            // 사용자 생성 또는 가져오기
            val userResult = when {
                existingUserResult is Result.Success && existingUserResult.data != null ->
                    Result.Success(existingUserResult.data)
                else -> {
                    if (fcmToken.isNotEmpty()) {
                        localIdAddUseCase()
                    } else {
                        // FCM 토큰이 없으면 대기
                        FCMTokenEvent.tokenFlow
                            .filterNotNull()
                            .first()
                        localIdAddUseCase()
                    }
                }
            }

            // userResult에 따라 서버 동기화 처리
            when (userResult) {
                is Result.Success -> {
                    val user = userResult.data
                    if (user != null) {
                        syncWithServer(user) // user가 null이 아닌 경우에만 동기화
                    }
                    userResult
                }
                is Result.Error -> userResult
                is Result.Loading -> Result.Loading
            }

        } catch (e: Exception) {
            Log.e(TAG("userUseCase", "LocalExistCheckUseCase"), "에러 발생: ${e.message}")
            Result.Error(
                message = "사용자 확인 중 오류가 발생했습니다.",
                exception = e
            )
        }
    }


    private suspend fun syncWithServer(user: LocalUserData): Result<LocalUserData> {
        return try {
            val fcmToken = InvestApplication.prefs.getData("fcm_token", "")

            Log.d(TAG("userUseCase", "LocalExistCheckUseCase"), "토큰: ${fcmToken}")

            val createServerUser = UserRequest(
                deviceId = user.id.toString(),
                customId = "",
                pin = "",
                fcmToken = user.fcmToken ?: fcmToken
            )

            Log.d(TAG("userUseCase", "LocalExistCheckUseCase"), "서버 동기화 시작: ${user}")

            if (user.id != null) {
                val serverUserCheckId = UserApi.userService.getUserRequest(user.id.toString())

                if (serverUserCheckId.data == null) {
                    Log.d(TAG("userUseCase", "LocalExistCheckUseCase"), "서버에 유저 없음, 신규 생성")
                    UserApi.userService.userAddRequest(createServerUser)
                    Result.Success(
                        data = user,
                        message = "로컬 및 서버에 새로운 사용자가 등록되었습니다."
                    )
                } else {
                    Log.d(TAG("userUseCase", "LocalExistCheckUseCase"), "서버에 유저 존재: ${user}")
                    Result.Success(
                        data = user,
                        message = "기존 사용자 정보로 동기화되었습니다."
                    )
                }
            } else {
                Log.d(TAG("userUseCase", "LocalExistCheckUseCase"), "ID 없음, 신규 유저 생성")
                UserApi.userService.userAddRequest(createServerUser)
                Result.Success(
                    data = user,
                    message = "새로운 사용자가 생성되었습니다."
                )
            }
        } catch (e: Exception) {
            Log.e(TAG("userUseCase", "LocalExistCheckUseCase"), "서버 동기화 실패: ${e.message}")
            Result.Error(
                message = "서버 동기화에 실패했습니다.",
                exception = e
            )
        }
    }
}

