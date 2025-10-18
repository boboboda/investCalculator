package com.bobodroid.myapplication.models.datamodels.useCases

import android.util.Log
import com.bobodroid.myapplication.MainActivity.Companion.TAG
import com.bobodroid.myapplication.fcm.FCMTokenEvent
import com.bobodroid.myapplication.models.datamodels.repository.UserRepository
import com.bobodroid.myapplication.models.datamodels.roomDb.LocalUserData
import com.bobodroid.myapplication.models.datamodels.roomDb.SocialType
import com.bobodroid.myapplication.models.datamodels.roomDb.TargetRates
import com.bobodroid.myapplication.models.datamodels.service.UserApi.UserApi
import com.bobodroid.myapplication.models.datamodels.service.UserApi.UserRequest
import com.bobodroid.myapplication.models.datamodels.service.UserApi.UserResponse
import com.bobodroid.myapplication.util.InvestApplication
import com.bobodroid.myapplication.util.result.Result
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject

/**
 * 사용자 관련 UseCase 모음 (소셜 로그인 버전)
 */
class UserUseCases(
    val deleteUser: DeleteUserUseCase,
    val localUserUpdate: LocalUserUpdate,
    val socialLoginUseCases: SocialLoginUseCases
)

/**
 * 로컬 사용자 생성 UseCase
 */
class LocalIdAddUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(): LocalUserData {
        val fcmToken = FCMTokenEvent.tokenFlow.filterNotNull().first()

        val createLocalUser = LocalUserData(
            userResetDate = "",
            rateAdCount = 0,
            rateResetCount = 3,
            fcmToken = fcmToken,
            socialType = SocialType.NONE.name  // ✅ String으로 저장
        )

        userRepository.localUserAdd(createLocalUser)
        return createLocalUser
    }
}

/**
 * 로컬 사용자 업데이트 UseCase
 */
class LocalUserUpdate @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(localUserData: LocalUserData) {
        userRepository.localUserUpdate(localUserData)
    }
}

/**
 * 사용자 삭제 UseCase
 */
class DeleteUserUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke() {
        userRepository.localUserDataDelete()
    }
}

/**
 * 로컬 사용자 존재 확인 및 초기화 UseCase
 */
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

            // ✅ socialType이 String이므로 "NONE"과 비교
            if (user.socialId != null && user.socialType != "NONE") {
                Log.d(TAG("LocalExistCheckUseCase", "invoke"), "소셜 로그인 사용자 - 서버 동기화 시작")
                val serverUser = syncWithServer(user)
                Log.d(TAG("LocalExistCheckUseCase", "invoke"), "서버 동기화 완료: $serverUser")

                val userDataType = UserData(
                    localUserData = user,
                    exchangeRates = serverUser?.data?.let { serverData ->
                        TargetRates(
                            dollarHighRates = serverData.usdHighRates ?: emptyList(),
                            dollarLowRates = serverData.usdLowRates ?: emptyList(),
                            yenHighRates = serverData.jpyHighRates ?: emptyList(),
                            yenLowRates = serverData.jpyLowRates ?: emptyList()
                        )
                    }
                )
                userRepository.updateUserData(userDataType)
            } else {
                Log.d(TAG("LocalExistCheckUseCase", "invoke"), "로컬 전용 사용자")
                val userDataType = UserData(
                    localUserData = user,
                    exchangeRates = null
                )
                userRepository.updateUserData(userDataType)
            }

            Log.d(TAG("LocalExistCheckUseCase", "invoke"), "UserRepository 업데이트 완료")
        } catch (e: Exception) {
            Log.e(TAG("LocalExistCheckUseCase", "invoke"), "사용자 확인 중 오류", e)
        }
    }

    private suspend fun syncWithServer(user: LocalUserData): UserResponse? {
        return try {
            val fcmToken = InvestApplication.prefs.getData("fcm_token", "")
            Log.d(TAG("LocalExistCheckUseCase", "syncWithServer"), "서버 동기화 FCM 토큰: $fcmToken")

            val searchId = user.socialId ?: user.id.toString()
            Log.d(TAG("LocalExistCheckUseCase", "syncWithServer"), "조회 ID: $searchId")

            if (user.id != null) {
                val serverUser = try {
                    Log.d(TAG("LocalExistCheckUseCase", "syncWithServer"), "서버 조회 시작...")
                    val response = UserApi.userService.getUserRequest(searchId)
                    Log.d(TAG("LocalExistCheckUseCase", "syncWithServer"), "서버 조회 성공: ${response.message}")
                    response
                } catch (e: retrofit2.HttpException) {
                    if (e.code() == 404) {
                        Log.d(TAG("LocalExistCheckUseCase", "syncWithServer"), "서버에 유저 없음 (404)")
                        null
                    } else {
                        throw e
                    }
                } catch (e: Exception) {
                    Log.e(TAG("LocalExistCheckUseCase", "syncWithServer"), "일반 에러: ${e.message}")
                    throw e
                }

                if (serverUser?.data == null) {
                    val createServerUser = UserRequest(
                        deviceId = user.id.toString(),
                        socialId = user.socialId,
                        socialType = user.socialType,  // ✅ 이미 String
                        email = user.email,
                        nickname = user.nickname,
                        profileUrl = user.profileUrl,
                        fcmToken = user.fcmToken ?: fcmToken
                    )

                    Log.d(TAG("LocalExistCheckUseCase", "syncWithServer"), "새 서버 유저 생성 시작")
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
                    socialId = null,
                    socialType = SocialType.NONE.name,  // ✅ String으로 저장
                    fcmToken = user.fcmToken ?: fcmToken
                )

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

/**
 * UserDataType 데이터 클래스
 */
data class UserData(
    val localUserData: LocalUserData,
    val exchangeRates: TargetRates? = null
)