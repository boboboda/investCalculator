package com.bobodroid.myapplication.models.datamodels.useCases

import android.app.Activity
import android.util.Log
import com.bobodroid.myapplication.MainActivity.Companion.TAG
import com.bobodroid.myapplication.models.datamodels.repository.UserRepository
import com.bobodroid.myapplication.models.datamodels.roomDb.LocalUserData
import com.bobodroid.myapplication.models.datamodels.roomDb.SocialType
import com.bobodroid.myapplication.models.datamodels.service.UserApi.UserApi
import com.bobodroid.myapplication.models.datamodels.service.UserApi.UserRequest
import com.bobodroid.myapplication.models.datamodels.social.SocialLoginManager
import com.bobodroid.myapplication.util.result.Result
import javax.inject.Inject

/**
 * 소셜 로그인 관련 UseCase 모음
 */
class SocialLoginUseCases(
    val googleLogin: GoogleLoginUseCase,
    val kakaoLogin: KakaoLoginUseCase,
    val socialLogout: SocialLogoutUseCase,
    val syncToServer: SyncToServerUseCase,
    val restoreFromServer: RestoreFromServerUseCase
)

/**
 * Google 로그인 UseCase
 */
class GoogleLoginUseCase @Inject constructor(
    private val userRepository: UserRepository,
    private val socialLoginManager: SocialLoginManager
) {
    suspend operator fun invoke(
        activity: Activity,
        localUserData: LocalUserData
    ): Result<LocalUserData> {
        return try {
            Log.d(TAG("GoogleLoginUseCase", "invoke"), "Google 로그인 시작")

            val socialResult = socialLoginManager.loginWithGoogle(activity)

            Log.d(TAG("GoogleLoginUseCase", "invoke"), "Google 로그인 성공: ${socialResult.email}")

            val updatedUser = localUserData.copy(
                socialId = socialResult.socialId,
                socialType = SocialType.GOOGLE.name,
                email = socialResult.email,
                nickname = socialResult.nickname,
                profileUrl = socialResult.profileUrl,
                isSynced = false
            )

            userRepository.localUserUpdate(updatedUser)

            Log.d(TAG("GoogleLoginUseCase", "invoke"), "로컬 DB 업데이트 완료")

            syncWithServer(updatedUser)

            Result.Success(
                data = updatedUser,
                message = "Google 로그인 성공"
            )

        } catch (e: Exception) {
            Log.e(TAG("GoogleLoginUseCase", "invoke"), "Google 로그인 실패", e)
            Result.Error(
                message = "Google 로그인에 실패했습니다: ${e.message}",
                exception = e
            )
        }
    }

    private suspend fun syncWithServer(user: LocalUserData) {
        try {
            val userRequest = UserRequest(
                deviceId = user.id.toString(),
                socialId = user.socialId,
                socialType = user.socialType,
                email = user.email,
                nickname = user.nickname,
                profileUrl = user.profileUrl,
                fcmToken = user.fcmToken ?: ""
            )

            val response = UserApi.userService.userUpdate(
                deviceId = user.id.toString(),
                userRequest = userRequest
            )

            Log.d(TAG("GoogleLoginUseCase", "syncWithServer"), "서버 동기화 성공: ${response.message}")

            val syncedUser = user.copy(isSynced = true)
            userRepository.localUserUpdate(syncedUser)

        } catch (e: Exception) {
            Log.e(TAG("GoogleLoginUseCase", "syncWithServer"), "서버 동기화 실패", e)
        }
    }
}

/**
 * Kakao 로그인 UseCase
 */
class KakaoLoginUseCase @Inject constructor(
    private val userRepository: UserRepository,
    private val socialLoginManager: SocialLoginManager
) {
    suspend operator fun invoke(
        activity: Activity,
        localUserData: LocalUserData
    ): Result<LocalUserData> {
        return try {
            Log.d(TAG("KakaoLoginUseCase", "invoke"), "Kakao 로그인 시작")

            val socialResult = socialLoginManager.loginWithKakao(activity)

            Log.d(TAG("KakaoLoginUseCase", "invoke"), "Kakao 로그인 성공: ${socialResult.email}")

            val updatedUser = localUserData.copy(
                socialId = socialResult.socialId,
                socialType = SocialType.KAKAO.name,
                email = socialResult.email,
                nickname = socialResult.nickname,
                profileUrl = socialResult.profileUrl,
                isSynced = false
            )

            userRepository.localUserUpdate(updatedUser)

            Log.d(TAG("KakaoLoginUseCase", "invoke"), "로컬 DB 업데이트 완료")

            syncWithServer(updatedUser)

            Result.Success(
                data = updatedUser,
                message = "Kakao 로그인 성공"
            )

        } catch (e: Exception) {
            Log.e(TAG("KakaoLoginUseCase", "invoke"), "Kakao 로그인 실패", e)
            Result.Error(
                message = "Kakao 로그인에 실패했습니다: ${e.message}",
                exception = e
            )
        }
    }

    private suspend fun syncWithServer(user: LocalUserData) {
        try {
            val userRequest = UserRequest(
                deviceId = user.id.toString(),
                socialId = user.socialId,
                socialType = user.socialType,
                email = user.email,
                nickname = user.nickname,
                profileUrl = user.profileUrl,
                fcmToken = user.fcmToken ?: ""
            )

            val response = UserApi.userService.userUpdate(
                deviceId = user.id.toString(),
                userRequest = userRequest
            )

            Log.d(TAG("KakaoLoginUseCase", "syncWithServer"), "서버 동기화 성공: ${response.message}")

            val syncedUser = user.copy(isSynced = true)
            userRepository.localUserUpdate(syncedUser)

        } catch (e: Exception) {
            Log.e(TAG("KakaoLoginUseCase", "syncWithServer"), "서버 동기화 실패", e)
        }
    }
}

/**
 * 소셜 로그아웃 UseCase
 */
class SocialLogoutUseCase @Inject constructor(
    private val userRepository: UserRepository,
    private val socialLoginManager: SocialLoginManager
) {
    suspend operator fun invoke(localUserData: LocalUserData): Result<LocalUserData> {
        return try {
            Log.d(TAG("SocialLogoutUseCase", "invoke"), "로그아웃 시작: ${localUserData.socialType}")

            val socialTypeEnum = localUserData.getSocialTypeEnum()
            val logoutResult = socialLoginManager.logout(socialTypeEnum)

            if (logoutResult.isFailure) {
                val throwable = logoutResult.exceptionOrNull()
                // ✅ Throwable을 Exception으로 변환
                val exception = when (throwable) {
                    is Exception -> throwable
                    is Throwable -> Exception(throwable.message, throwable)
                    else -> null
                }

                return if (exception != null) {
                    Result.Error(
                        message = "로그아웃에 실패했습니다: ${exception.message}",
                        exception = exception
                    )
                } else {
                    Result.Error(message = "로그아웃에 실패했습니다")
                }
            }

            val updatedUser = localUserData.copy(
                socialId = null,
                socialType = SocialType.NONE.name,
                email = null,
                nickname = null,
                profileUrl = null,
                isSynced = false
            )

            userRepository.localUserUpdate(updatedUser)

            Log.d(TAG("SocialLogoutUseCase", "invoke"), "로그아웃 완료")

            Result.Success(
                data = updatedUser,
                message = "로그아웃되었습니다"
            )

        } catch (e: Exception) {
            Log.e(TAG("SocialLogoutUseCase", "invoke"), "로그아웃 실패", e)
            Result.Error(
                message = "로그아웃에 실패했습니다",
                exception = e
            )
        }
    }
}

/**
 * 서버 백업 UseCase
 */
class SyncToServerUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(localUserData: LocalUserData): Result<Unit> {
        return try {
            if (localUserData.socialId == null) {
                return Result.Error(message = "소셜 로그인이 필요합니다")
            }

            Log.d(TAG("SyncToServerUseCase", "invoke"), "서버 백업 시작")

            val userRequest = UserRequest(
                deviceId = localUserData.id.toString(),
                socialId = localUserData.socialId,
                socialType = localUserData.socialType,
                email = localUserData.email,
                nickname = localUserData.nickname,
                profileUrl = localUserData.profileUrl,
                fcmToken = localUserData.fcmToken ?: ""
            )

            val response = UserApi.userService.userUpdate(
                deviceId = localUserData.id.toString(),
                userRequest = userRequest
            )

            val syncedUser = localUserData.copy(isSynced = true)
            userRepository.localUserUpdate(syncedUser)

            Log.d(TAG("SyncToServerUseCase", "invoke"), "서버 백업 완료: ${response.message}")

            Result.Success(
                data = Unit,
                message = "데이터가 백업되었습니다"
            )

        } catch (e: Exception) {
            Log.e(TAG("SyncToServerUseCase", "invoke"), "서버 백업 실패", e)
            Result.Error(
                message = "백업에 실패했습니다",
                exception = e
            )
        }
    }
}

/**
 * 서버에서 데이터 복구 UseCase
 */
class RestoreFromServerUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(socialId: String): Result<LocalUserData> {
        return try {
            Log.d(TAG("RestoreFromServerUseCase", "invoke"), "데이터 복구 시작: $socialId")

            val response = UserApi.userService.getUserRequest(socialId)

            val serverData = response.data
            if (serverData == null) {
                return Result.Error(message = "서버에 저장된 데이터가 없습니다")
            }

            Log.d(TAG("RestoreFromServerUseCase", "invoke"), "데이터 복구 완료")

            Result.Error(message = "데이터 복구 기능은 추후 구현 예정입니다")

        } catch (e: Exception) {
            Log.e(TAG("RestoreFromServerUseCase", "invoke"), "데이터 복구 실패", e)
            Result.Error(
                message = "복구에 실패했습니다",
                exception = e
            )
        }
    }
}