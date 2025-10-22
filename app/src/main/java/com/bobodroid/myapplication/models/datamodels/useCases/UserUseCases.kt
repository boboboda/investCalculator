// app/src/main/java/com/bobodroid/myapplication/models/datamodels/useCases/UserUseCases.kt

package com.bobodroid.myapplication.models.datamodels.useCases

import android.util.Log
import com.bobodroid.myapplication.MainActivity.Companion.TAG
import com.bobodroid.myapplication.fcm.FCMTokenEvent
import com.bobodroid.myapplication.models.datamodels.repository.UserRepository
import com.bobodroid.myapplication.models.datamodels.roomDb.LocalUserData
import com.bobodroid.myapplication.models.datamodels.roomDb.SocialType
import com.bobodroid.myapplication.models.datamodels.roomDb.TargetRates
import com.bobodroid.myapplication.models.datamodels.roomDb.CurrencyTargetRates
import com.bobodroid.myapplication.models.datamodels.roomDb.CurrencyType
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
import retrofit2.HttpException
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
            socialType = SocialType.NONE.name
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
    private val localIdAddUseCase: LocalIdAddUseCase,
    private val socialLoginManager: SocialLoginManager
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
                validateSocialLoginState(existingUser)
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

            // ✅ 모든 사용자에 대해 서버 동기화 (목표환율 기능을 위해 필수)
            Log.d(TAG("LocalExistCheckUseCase", "invoke"), "서버 동기화 시작 (모든 사용자)")
            val serverUser = syncWithServer(user)
            Log.d(TAG("LocalExistCheckUseCase", "invoke"), "서버 동기화 완료: ${serverUser?.success}")

            // ✅ 12개 통화 지원하는 새로운 구조로 변환
            val userDataType = UserData(
                localUserData = user,
                exchangeRates = serverUser?.data?.targetRates?.let { targetRatesMap ->
                    val ratesMap = mutableMapOf<CurrencyType, CurrencyTargetRates>()

                    targetRatesMap.forEach { (currencyCode, ratesJson) ->
                        // 통화 코드로 CurrencyType 찾기
                        val currencyType = CurrencyType.values().find { it.code == currencyCode }

                        if (currencyType != null) {
                            ratesMap[currencyType] = CurrencyTargetRates(
                                high = ratesJson.high ?: emptyList(),
                                low = ratesJson.low ?: emptyList()
                            )
                        }
                    }

                    TargetRates(rates = ratesMap)
                }
            )
            userRepository.updateUserData(userDataType)

            Log.d(TAG("LocalExistCheckUseCase", "invoke"), "UserRepository 업데이트 완료")
        } catch (e: Exception) {
            Log.e(TAG("LocalExistCheckUseCase", "invoke"), "사용자 확인 중 오류", e)
        }
    }

    /**
     * 소셜 로그인 상태 검증
     */
    private suspend fun validateSocialLoginState(user: LocalUserData): LocalUserData {
        if (user.socialType == "NONE" || user.socialId == null) {
            return user
        }

        Log.d(TAG("LocalExistCheckUseCase", "validateSocialLoginState"), "소셜 로그인 상태 검증: ${user.socialType}")

        val isActuallyLoggedIn = when (user.socialType) {
            "GOOGLE" -> socialLoginManager.isGoogleLoggedIn()
            "KAKAO" -> socialLoginManager.isKakaoLoggedIn()
            else -> false
        }

        return if (!isActuallyLoggedIn) {
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

            userRepository.localUserUpdate(loggedOutUser)
            loggedOutUser
        } else {
            Log.d(TAG("LocalExistCheckUseCase", "validateSocialLoginState"),
                "✅ ${user.socialType} 로그인 상태 정상")
            user
        }
    }

    /**
     * ✅ 서버 동기화 (모든 사용자 대상)
     */
    private suspend fun syncWithServer(user: LocalUserData): UserResponse? {
        return try {
            val fcmToken = InvestApplication.prefs.getData("fcm_token", "")
            Log.d(TAG("LocalExistCheckUseCase", "syncWithServer"), "━━━━━━━━━━━━━━━━━━━━━━━━━━")
            Log.d(TAG("LocalExistCheckUseCase", "syncWithServer"), "서버 동기화 시작")
            Log.d(TAG("LocalExistCheckUseCase", "syncWithServer"), "DeviceId: ${user.id}")
            Log.d(TAG("LocalExistCheckUseCase", "syncWithServer"), "SocialType: ${user.socialType}")
            Log.d(TAG("LocalExistCheckUseCase", "syncWithServer"), "FCM 토큰: $fcmToken")

            val deviceId = user.id.toString()

            // ✅ 1단계: 서버 사용자 조회
            val serverUser = try {
                Log.d(TAG("LocalExistCheckUseCase", "syncWithServer"), "서버 조회 시작...")
                val response = UserApi.userService.getUserByDeviceId(deviceId)
                Log.d(TAG("LocalExistCheckUseCase", "syncWithServer"), "✅ 서버 조회 성공: ${response.message}")

                if (response.success && response.data != null) {
                    response
                } else {
                    Log.w(TAG("LocalExistCheckUseCase", "syncWithServer"), "⚠️ 서버 응답은 있지만 데이터가 없음")
                    null
                }
            } catch (e: HttpException) {
                if (e.code() == 404) {
                    Log.d(TAG("LocalExistCheckUseCase", "syncWithServer"), "❌ 서버에 유저 없음 (404)")
                    null
                } else {
                    Log.e(TAG("LocalExistCheckUseCase", "syncWithServer"), "HTTP 에러: ${e.code()}")
                    throw e
                }
            } catch (e: Exception) {
                Log.e(TAG("LocalExistCheckUseCase", "syncWithServer"), "일반 에러: ${e.message}", e)
                throw e
            }

            // ✅ 2단계: 서버에 없으면 새로 생성 (모든 사용자)
            if (serverUser == null) {
                Log.d(TAG("LocalExistCheckUseCase", "syncWithServer"), "🆕 새 서버 유저 생성 시작")

                val createServerUser = UserRequest(
                    deviceId = deviceId,
                    socialId = user.socialId,
                    socialType = user.socialType,
                    email = user.email,
                    nickname = user.nickname,
                    profileUrl = user.profileUrl,
                    fcmToken = user.fcmToken ?: fcmToken
                )

                Log.d(TAG("LocalExistCheckUseCase", "syncWithServer"), "요청 데이터:")
                Log.d(TAG("LocalExistCheckUseCase", "syncWithServer"), "  - deviceId: $deviceId")
                Log.d(TAG("LocalExistCheckUseCase", "syncWithServer"), "  - socialType: ${user.socialType}")
                Log.d(TAG("LocalExistCheckUseCase", "syncWithServer"), "  - fcmToken: ${user.fcmToken ?: fcmToken}")

                try {
                    val createResponse = UserApi.userService.userAddRequest(createServerUser)

                    Log.d(TAG("LocalExistCheckUseCase", "syncWithServer"), "생성 응답:")
                    Log.d(TAG("LocalExistCheckUseCase", "syncWithServer"), "  - success: ${createResponse.success}")
                    Log.d(TAG("LocalExistCheckUseCase", "syncWithServer"), "  - message: ${createResponse.message}")

                    if (createResponse.success) {
                        Log.d(TAG("LocalExistCheckUseCase", "syncWithServer"), "✅ 새 서버 유저 생성 성공!")
                        Log.d(TAG("LocalExistCheckUseCase", "syncWithServer"), "━━━━━━━━━━━━━━━━━━━━━━━━━━")
                        createResponse
                    } else {
                        Log.e(TAG("LocalExistCheckUseCase", "syncWithServer"), "❌ 생성 실패: ${createResponse.message}")
                        Log.d(TAG("LocalExistCheckUseCase", "syncWithServer"), "━━━━━━━━━━━━━━━━━━━━━━━━━━")
                        null
                    }
                } catch (e: Exception) {
                    Log.e(TAG("LocalExistCheckUseCase", "syncWithServer"), "❌ 생성 요청 중 에러: ${e.message}", e)
                    Log.d(TAG("LocalExistCheckUseCase", "syncWithServer"), "━━━━━━━━━━━━━━━━━━━━━━━━━━")
                    null
                }
            } else {
                Log.d(TAG("LocalExistCheckUseCase", "syncWithServer"), "✅ 기존 서버 유저 사용")
                Log.d(TAG("LocalExistCheckUseCase", "syncWithServer"), "━━━━━━━━━━━━━━━━━━━━━━━━━━")
                serverUser
            }
        } catch (e: Exception) {
            Log.e(TAG("LocalExistCheckUseCase", "syncWithServer"), "서버 동기화 실패: ${e.message}", e)
            Log.d(TAG("LocalExistCheckUseCase", "syncWithServer"), "━━━━━━━━━━━━━━━━━━━━━━━━━━")
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