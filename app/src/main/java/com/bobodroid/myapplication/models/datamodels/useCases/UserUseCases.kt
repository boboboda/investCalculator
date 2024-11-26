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
            val updateLocalData = localUserData.copy(
                customId = customId,
                pin = pin
            )

            userRepository.localUserUpdate(updateLocalData)

            val userRequest = UserRequest(
                customId = customId,
                deviceId = updateLocalData.id.toString(),
                pin = updateLocalData.pin,
                fcmToken = updateLocalData.fcmToken ?: ""
            )

           val fetchUserData = UserApi.userService.userUpdate(
                deviceId = updateLocalData.id.toString(),
                userRequest = userRequest)

            Result.Success(
                data = updateLocalData,
                message = fetchUserData.message
            )

        } catch (e: Exception) {
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

            val userDataType = UserDataType(
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

    private suspend fun syncWithServer(user: LocalUserData): UserResponse? {
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
}

data class UserDataType (
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