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
import com.bobodroid.myapplication.models.datamodels.social.SocialLoginManager
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
/**
 * 로컬 사용자 존재 확인 및 초기화 UseCase
 */
/**
 * 로컬 사용자 존재 확인 및 초기화 UseCase
 */
class LocalExistCheckUseCase @Inject constructor(
    private val userRepository: UserRepository,
    private val localIdAddUseCase: LocalIdAddUseCase,
    private val socialLoginManager: SocialLoginManager  // ✅ 추가
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

                // ⚠️ 소셜 로그인 상태 검증
                val validatedUser = validateSocialLoginState(existingUser)
                validatedUser
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

    /**
     * ⚠️ 소셜 로그인 상태 검증
     * DB에 저장된 소셜 타입과 실제 SDK 토큰 상태를 비교
     */
    private suspend fun validateSocialLoginState(user: LocalUserData): LocalUserData {
        if (user.socialType == "NONE" || user.socialId == null) {
            // 소셜 로그인 사용 안 함
            return user
        }

        Log.d(TAG("LocalExistCheckUseCase", "validateSocialLoginState"), "소셜 로그인 상태 검증: ${user.socialType}")

        val isActuallyLoggedIn = when (user.socialType) {
            "GOOGLE" -> {
                // Google 로그인 상태 확인
                socialLoginManager.isGoogleLoggedIn()
            }
            "KAKAO" -> {
                // Kakao 로그인 상태 확인
                socialLoginManager.isKakaoLoggedIn()
            }
            else -> false
        }

        return if (!isActuallyLoggedIn) {
            // SDK에 토큰이 없음 = 실제로는 로그아웃됨
            Log.w(TAG("LocalExistCheckUseCase", "validateSocialLoginState"),
                "⚠️ DB에는 ${user.socialType}로 저장되어 있지만, 실제로는 로그아웃 상태입니다. DB를 초기화합니다.")

            val loggedOutUser = user.copy(
                socialId = null,
                socialType = "NONE",
                email = null,
                nickname = null,
                profileUrl = null,
                isSynced = false
            )

            // DB 업데이트
            userRepository.localUserUpdate(loggedOutUser)
            loggedOutUser
        } else {
            Log.d(TAG("LocalExistCheckUseCase", "validateSocialLoginState"),
                "✅ ${user.socialType} 로그인 상태 정상")
            user
        }
    }

    private suspend fun syncWithServer(user: LocalUserData): UserResponse? {
        return try {
            val fcmToken = InvestApplication.prefs.getData("fcm_token", "")
            Log.d(TAG("LocalExistCheckUseCase", "syncWithServer"), "서버 동기화 FCM 토큰: $fcmToken")

            val deviceId = user.id.toString()
            Log.d(TAG("LocalExistCheckUseCase", "syncWithServer"), "DeviceId: $deviceId")

            // ✅ deviceId로 서버 사용자 조회
            val serverUser = try {
                Log.d(TAG("LocalExistCheckUseCase", "syncWithServer"), "서버 조회 시작...")
                val response = UserApi.userService.getUserByDeviceId(deviceId)
                Log.d(TAG("LocalExistCheckUseCase", "syncWithServer"), "서버 조회 성공: ${response.message}")

                if (response.success && response.data != null) {
                    response
                } else {
                    null
                }
            } catch (e: retrofit2.HttpException) {
                if (e.code() == 404) {
                    Log.d(TAG("LocalExistCheckUseCase", "syncWithServer"), "서버에 유저 없음 (404)")
                    null
                } else {
                    Log.e(TAG("LocalExistCheckUseCase", "syncWithServer"), "HTTP 에러: ${e.code()}")
                    throw e
                }
            } catch (e: Exception) {
                Log.e(TAG("LocalExistCheckUseCase", "syncWithServer"), "일반 에러: ${e.message}")
                throw e
            }

            if (serverUser == null) {
                // 서버에 없으면 새로 생성
                val createServerUser = UserRequest(
                    deviceId = deviceId,
                    socialId = user.socialId,
                    socialType = user.socialType,
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