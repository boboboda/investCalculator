package com.bobodroid.myapplication.models.datamodels.useCases

import android.util.Log
import com.bobodroid.myapplication.MainActivity.Companion.TAG
import com.bobodroid.myapplication.billing.BillingClientLifecycle.Companion.TAG
import com.bobodroid.myapplication.fcm.FCMTokenEvent
import com.bobodroid.myapplication.models.datamodels.repository.UserRepository
import com.bobodroid.myapplication.models.datamodels.roomDb.LocalUserData
import com.bobodroid.myapplication.models.datamodels.roomDb.TargetRates
import com.bobodroid.myapplication.models.datamodels.service.UserApi.AuthUser
import com.bobodroid.myapplication.models.datamodels.service.UserApi.UserApi
import com.bobodroid.myapplication.models.datamodels.service.UserApi.UserData
import com.bobodroid.myapplication.models.datamodels.service.UserApi.UserRequest
import com.bobodroid.myapplication.models.datamodels.service.UserApi.UserResponse
import com.bobodroid.myapplication.util.InvestApplication
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject
import com.bobodroid.myapplication.util.result.Result
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class UserUseCases(
    val logIn: LogInUseCase,
    val logout: LogoutUseCase,
    val deleteUser: DeleteUserUseCase,
    val localUserUpdate: LocalUserUpdate,
    val customIdCreateUser: CustomIdCreateUser
)


class LocalIdAddUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(): LocalUserData {
        val fcmToken = FCMTokenEvent.tokenFlow.filterNotNull().first()

        val createLocalUser = LocalUserData(
            userResetDate = "",
            rateAdCount = 0,
            rateResetCount = 3,
            fcmToken = fcmToken
        )

        userRepository.localUserAdd(createLocalUser)
        return createLocalUser
    }
}

class LocalUserUpdate @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(localUserData: LocalUserData) {
        userRepository.localUserUpdate(localUserData)
    }

}

class CustomIdCreateUser @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(localUserData: LocalUserData, customId: String, pin: String): Result<LocalUserData> {

        return try {
            Log.d("CustomIdCreateUser", "===== 사용자 업데이트 시작 =====")
            Log.d("CustomIdCreateUser", "Input - customId: $customId, pin: $pin")
            Log.d("CustomIdCreateUser", "기존 localUserData: $localUserData")

            val updateLocalData = localUserData.copy(
                customId = customId,
                pin = pin
            )
            Log.d("CustomIdCreateUser", "업데이트할 데이터: $updateLocalData")

            Log.d("CustomIdCreateUser", "로컬 DB 업데이트 시작...")
            userRepository.localUserUpdate(updateLocalData)
            Log.d("CustomIdCreateUser", "로컬 DB 업데이트 완료")

            val userRequest = UserRequest(
                customId = customId,
                deviceId = updateLocalData.id.toString(),
                pin = updateLocalData.pin,
                fcmToken = updateLocalData.fcmToken ?: ""
            )
            Log.d("CustomIdCreateUser", "API 요청 데이터: $userRequest")

            Log.d("CustomIdCreateUser", "API 호출 시작 - deviceId: ${updateLocalData.id}")
            val fetchUserData = UserApi.userService.userUpdate(
                deviceId = updateLocalData.id.toString(),
                userRequest = userRequest
            )
            Log.d("CustomIdCreateUser", "API 호출 성공 - response: $fetchUserData")

            val result = Result.Success(
                data = updateLocalData,
                message = fetchUserData.message
            )
            Log.d("CustomIdCreateUser", "===== 사용자 업데이트 성공 =====")

            result

        } catch (e: Exception) {
            Log.e("CustomIdCreateUser", "===== 사용자 업데이트 실패 =====")
            Log.e("CustomIdCreateUser", "에러 타입: ${e::class.java.simpleName}")
            Log.e("CustomIdCreateUser", "에러 메시지: ${e.message}")
            Log.e("CustomIdCreateUser", "스택 트레이스:", e)

            Result.Error(
                message = "업데이트를 실패하였습니다.",
                exception = e
            )
        }
    }
}

class LogInUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(localUserData: LocalUserData, customId: String, pin: String): Result<LocalUserData> {
        return try {
            val resData = UserApi.userService.login(
                AuthUser(
                    customId,
                    pin
                )
            )
            Log.d(TAG("LogInUseCase",""), resData.message)
            Log.d(TAG("LogInUseCase",""), resData.data.toString())

            val userData = resData.data

            if(userData != null) {
                val updateLocalUser = localUserData.copy(customId = customId)

                userRepository.localUserUpdate(updateLocalUser)
                Result.Success(
                    message = "로그인 되었습니다.",
                    data = updateLocalUser
                )
            } else {
                Result.Error(
                    message = "비밀번호가 틀렸습니다."
                )
            }

        } catch (e: Exception) {
            Result.Error(
                message = "로그인이 실패하였습니다.",
                exception = e
            )
        }


    }
}

class LogoutUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(localUserData: LocalUserData): Result<LocalUserData> {
       return try {
           val updatedUser = localUserData.copy(customId = null)
           userRepository.localUserUpdate(updatedUser)
           Result.Success(
               message = "로그아웃되었습니다.",
               data = updatedUser
           )
        } catch (e:Exception) {
            Result.Error(
                message = "로그아웃이 실패되었습니다."
            )
        }
    }
}


class DeleteUserUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke() {
        userRepository.localUserDataDelete()
    }


}

class LocalExistCheckUseCase @Inject constructor(
    private val userRepository: UserRepository,
    private val localIdAddUseCase: LocalIdAddUseCase
) {
    suspend operator fun invoke() {
        try {
            val fcmToken = FCMTokenEvent.tokenFlow.firstOrNull()
                ?: InvestApplication.prefs.getData("fcm_token", "")
            Log.d(TAG("LocalExistCheckUseCase", "invoke"), "FCM 토큰: $fcmToken")

            val existingUser = userRepository.localUserDataGet()
                .distinctUntilChanged()
                .firstOrNull()
            Log.d(TAG("LocalExistCheckUseCase", "invoke"), "기존 유저 데이터: $existingUser")

            val user = if (existingUser != null) {
                Log.d(TAG("LocalExistCheckUseCase", "invoke"), "기존 유저 사용")
                existingUser
            } else {
                Log.d(TAG("LocalExistCheckUseCase", "invoke"), "새 유저 생성 시작")
                if (fcmToken.isNotEmpty()) {
                    localIdAddUseCase()
                } else {
                    FCMTokenEvent.tokenFlow
                        .filterNotNull()
                        .first()
                    localIdAddUseCase()
                }.also {
                    Log.d(TAG("LocalExistCheckUseCase", "invoke"), "새 유저 생성 완료: $it")
                }
            }

            Log.d(TAG("LocalExistCheckUseCase", "invoke"), "서버 동기화 시작: ${user.id}")
            val serverUser = syncWithServer(user)
            Log.d(TAG("LocalExistCheckUseCase", "invoke"), "서버 동기화 완료: $serverUser")

            val userDataType = UserData(
                localUserData = user,
                exchangeRates = serverUser?.data?.let { serverData ->
                    TargetRates(
                        dollarHighRates = serverData.usdHighRates,
                        dollarLowRates = serverData.usdLowRates,
                        yenHighRates = serverData.jpyHighRates,
                        yenLowRates = serverData.jpyLowRates
                    )
                }
            )
            Log.d(TAG("LocalExistCheckUseCase", "invoke"), "최종 UserDataType 생성: $userDataType")
            userRepository.updateUserData(userDataType)
            Log.d(TAG("LocalExistCheckUseCase", "invoke"), "UserRepository 업데이트 완료")
        } catch (e: Exception) {
            Log.e(TAG("LocalExistCheckUseCase", "invoke"), "사용자 확인 중 오류", e)
        }
    }

    // UserUseCases.kt - syncWithServer

    private suspend fun syncWithServer(user: LocalUserData): UserResponse? {
        return try {
            val fcmToken = InvestApplication.prefs.getData("fcm_token", "")
            Log.d(TAG("LocalExistCheckUseCase", "syncWithServer"), "서버 동기화 FCM 토큰: $fcmToken")

            // ✅ customId 우선, 없으면 deviceId
            val searchId = if (!user.customId.isNullOrEmpty()) {
                user.customId!!
            } else {
                user.id.toString()
            }

            Log.d(TAG("LocalExistCheckUseCase", "syncWithServer"), "조회 ID: $searchId")

            if (user.id != null) {
                // ✅ 404 처리 추가
                val serverUser = try {
                    Log.d(TAG("LocalExistCheckUseCase", "syncWithServer"), "서버 조회 시작...")
                    val response = UserApi.userService.getUserRequest(searchId)
                    Log.d(TAG("LocalExistCheckUseCase", "syncWithServer"), "서버 조회 성공: ${response.message}")
                    response
                } catch (e: retrofit2.HttpException) {
                    Log.e(TAG("LocalExistCheckUseCase", "syncWithServer"), "HTTP 에러: ${e.code()}")
                    Log.e(TAG("LocalExistCheckUseCase", "syncWithServer"), "HTTP 메시지: ${e.message()}")
                    Log.e(TAG("LocalExistCheckUseCase", "syncWithServer"), "HTTP 응답: ${e.response()?.errorBody()?.string()}")

                    if (e.code() == 404) {
                        Log.d(TAG("LocalExistCheckUseCase", "syncWithServer"), "서버에 유저 없음 (404)")
                        null
                    } else {
                        throw e
                    }
                } catch (e: Exception) {
                    Log.e(TAG("LocalExistCheckUseCase", "syncWithServer"), "일반 에러: ${e.javaClass.simpleName}")
                    Log.e(TAG("LocalExistCheckUseCase", "syncWithServer"), "일반 에러 메시지: ${e.message}")
                    Log.e(TAG("LocalExistCheckUseCase", "syncWithServer"), "스택트레이스:", e)
                    throw e
                }

                Log.d(TAG("LocalExistCheckUseCase", "syncWithServer"), "서버 응답: $serverUser")

                if (serverUser?.data == null) {
                    val createServerUser = UserRequest(
                        deviceId = user.id.toString(),
                        customId = user.customId ?: "",
                        pin = user.pin ?: "",
                        fcmToken = user.fcmToken ?: fcmToken
                    )

                    Log.d(TAG("LocalExistCheckUseCase", "syncWithServer"), "새 서버 유저 생성 시작: $createServerUser")
                    UserApi.userService.userAddRequest(createServerUser).also {
                        Log.d(TAG("LocalExistCheckUseCase", "syncWithServer"), "새 서버 유저 생성 완료: $it")
                    }
                } else {
                    Log.d(TAG("LocalExistCheckUseCase", "syncWithServer"), "기존 서버 유저 사용")
                    serverUser
                }
            } else {
                val createServerUser = UserRequest(
                    deviceId = user.id.toString(),
                    customId = "",
                    pin = "",
                    fcmToken = user.fcmToken ?: fcmToken
                )

                UserApi.userService.userAddRequest(createServerUser).also {
                    Log.d(TAG("LocalExistCheckUseCase", "syncWithServer"), "ID 없는 새 서버 유저 생성: $it")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG("LocalExistCheckUseCase", "syncWithServer"), "=== 서버 동기화 실패 상세 ===")
            Log.e(TAG("LocalExistCheckUseCase", "syncWithServer"), "에러 타입: ${e.javaClass.name}")
            Log.e(TAG("LocalExistCheckUseCase", "syncWithServer"), "에러 메시지: ${e.message}")
            Log.e(TAG("LocalExistCheckUseCase", "syncWithServer"), "원인: ${e.cause}")
            e.printStackTrace()
            null
        }
    }
}

data class UserData(
    val localUserData: LocalUserData,
    val exchangeRates: TargetRates? = null
)

private suspend fun serverUpdateUser(user: LocalUserData): UserResponse? {
    return try {
        val fcmToken = InvestApplication.prefs.getData("fcm_token", "")
        Log.d(TAG("LocalExistCheckUseCase", "syncWithServer"), "서버 동기화 FCM 토큰: $fcmToken")

        val createServerUser = UserRequest(
            deviceId = user.id.toString(),
            customId = "",
            pin = "",
            fcmToken = user.fcmToken ?: fcmToken
        )
        Log.d(TAG("LocalExistCheckUseCase", "syncWithServer"), "서버 요청 데이터: $createServerUser")

        if (user.id != null) {
            val serverUser = UserApi.userService.getUserRequest(user.id.toString())
            Log.d(TAG("LocalExistCheckUseCase", "syncWithServer"), "서버 응답: $serverUser")
            if (serverUser.data == null) {
                UserApi.userService.userAddRequest(createServerUser).also {
                    Log.d(TAG("LocalExistCheckUseCase", "syncWithServer"), "새 서버 유저 생성: $it")
                }
            } else {
                serverUser.also {
                    Log.d(TAG("LocalExistCheckUseCase", "syncWithServer"), "기존 서버 유저 사용: $it")
                }
            }
        } else {
            UserApi.userService.userAddRequest(createServerUser).also {
                Log.d(TAG("LocalExistCheckUseCase", "syncWithServer"), "ID 없는 새 서버 유저 생성: $it")
            }
        }
    } catch (e: Exception) {
        Log.e(TAG("LocalExistCheckUseCase", "syncWithServer"), "서버 동기화 실패", e)
        null
    }
}