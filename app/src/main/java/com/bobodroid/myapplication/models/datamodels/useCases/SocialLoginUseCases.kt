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
    val unlinkSocial: UnlinkSocialUseCase,  // ✅ 추가
    val syncToServer: SyncToServerUseCase,
    val restoreFromServer: RestoreFromServerUseCase
)

/**
 * 동기화 결과 sealed class
 */
sealed class SyncResult {
    object Success : SyncResult()
    data class AlreadyLinked(
        val currentSocialType: String,
        val currentEmail: String?,
        val currentNickname: String?
    ) : SyncResult()
    data class Error(val exception: Exception) : SyncResult()
}

/**
 * 소셜 연동 충돌 Exception
 */
class AlreadyLinkedException(
    val currentSocialType: String,
    val currentEmail: String?,
    val currentNickname: String?,
    message: String = "이미 다른 소셜 계정이 연동되어 있습니다"
) : Exception(message)

/**
 * Google 로그인 UseCase (연동 차단 처리)
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

            // ⚠️ 서버 동기화 시도 (연동 충돌 체크)
            val syncResult = syncWithServer(updatedUser)

            // ⚠️ 연동 충돌 체크
            if (syncResult is SyncResult.AlreadyLinked) {
                return Result.Error(
                    message = "이미 ${syncResult.currentSocialType}로 연동되어 있습니다",
                    exception = AlreadyLinkedException(
                        currentSocialType = syncResult.currentSocialType,
                        currentEmail = syncResult.currentEmail,
                        currentNickname = syncResult.currentNickname
                    )
                )
            }

            userRepository.localUserUpdate(updatedUser)

            Log.d(TAG("GoogleLoginUseCase", "invoke"), "로컬 DB 업데이트 완료")

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

    private suspend fun syncWithServer(user: LocalUserData): SyncResult {
        return try {
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

            // ⚠️ 연동 충돌 에러 체크
            if (response.code == "ALREADY_LINKED") {
                Log.w(TAG("GoogleLoginUseCase", "syncWithServer"), "이미 다른 소셜 계정이 연동되어 있음")

                // 🔍 디버깅: 서버 응답 전체 로깅
                Log.w(TAG("GoogleLoginUseCase", "syncWithServer"), "서버 응답 전체: $response")
                Log.w(TAG("GoogleLoginUseCase", "syncWithServer"), "response.data: ${response.data}")
                Log.w(TAG("GoogleLoginUseCase", "syncWithServer"), "response.data is null: ${response.data == null}")

                if (response.data != null) {
                    Log.w(TAG("GoogleLoginUseCase", "syncWithServer"), "socialType 원본: ${response.data?.socialType}")
                    Log.w(TAG("GoogleLoginUseCase", "syncWithServer"), "email: ${response.data?.email}")
                    Log.w(TAG("GoogleLoginUseCase", "syncWithServer"), "nickname: ${response.data?.nickname}")
                }

                // ✅ 서버에서 보낸 소셜 타입을 한글로 변환
                val rawSocialType = response.data?.socialType

                Log.w(TAG("GoogleLoginUseCase", "syncWithServer"), "rawSocialType 추출 결과: '$rawSocialType'")

                val socialTypeDisplay = when (rawSocialType?.uppercase()) {
                    "GOOGLE" -> "Google"
                    "KAKAO" -> "Kakao"
                    "NAVER" -> "Naver"
                    "APPLE" -> "Apple"
                    null -> {
                        Log.e(TAG("GoogleLoginUseCase", "syncWithServer"), "⚠️ socialType이 null입니다!")
                        "알 수 없는 소셜"
                    }
                    else -> {
                        Log.e(TAG("GoogleLoginUseCase", "syncWithServer"), "⚠️ 예상하지 못한 socialType: '$rawSocialType'")
                        "알 수 없는 소셜($rawSocialType)"
                    }
                }

                Log.w(TAG("GoogleLoginUseCase", "syncWithServer"), "최종 표시 타입: '$socialTypeDisplay'")

                return SyncResult.AlreadyLinked(
                    currentSocialType = socialTypeDisplay,
                    currentEmail = response.data?.email,
                    currentNickname = response.data?.nickname
                )
            }

            Log.d(TAG("GoogleLoginUseCase", "syncWithServer"), "서버 동기화 성공: ${response.message}")

            val syncedUser = user.copy(isSynced = true)
            userRepository.localUserUpdate(syncedUser)

            SyncResult.Success

        } catch (e: Exception) {
            Log.e(TAG("GoogleLoginUseCase", "syncWithServer"), "서버 동기화 실패", e)
            SyncResult.Error(e)
        }
    }
}

/**
 * Kakao 로그인 UseCase (연동 차단 처리)
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

            // ⚠️ 서버 동기화 시도 (연동 충돌 체크)
            val syncResult = syncWithServer(updatedUser)

            // ⚠️ 연동 충돌 체크
            if (syncResult is SyncResult.AlreadyLinked) {
                return Result.Error(
                    message = "이미 ${syncResult.currentSocialType}로 연동되어 있습니다",
                    exception = AlreadyLinkedException(
                        currentSocialType = syncResult.currentSocialType,
                        currentEmail = syncResult.currentEmail,
                        currentNickname = syncResult.currentNickname
                    )
                )
            }

            userRepository.localUserUpdate(updatedUser)

            Log.d(TAG("KakaoLoginUseCase", "invoke"), "로컬 DB 업데이트 완료")

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


    private suspend fun syncWithServer(user: LocalUserData): SyncResult {
        return try {
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

            // ⚠️ 연동 충돌 에러 체크
            if (response.code == "ALREADY_LINKED") {
                Log.w(TAG("GoogleLoginUseCase", "syncWithServer"), "이미 다른 소셜 계정이 연동되어 있음")

                // 🔍 디버깅: 서버 응답 전체 로깅
                Log.w(TAG("GoogleLoginUseCase", "syncWithServer"), "서버 응답 전체: $response")
                Log.w(TAG("GoogleLoginUseCase", "syncWithServer"), "response.data: ${response.data}")
                Log.w(TAG("GoogleLoginUseCase", "syncWithServer"), "response.data is null: ${response.data == null}")

                if (response.data != null) {
                    Log.w(TAG("GoogleLoginUseCase", "syncWithServer"), "socialType 원본: ${response.data?.socialType}")
                    Log.w(TAG("GoogleLoginUseCase", "syncWithServer"), "email: ${response.data?.email}")
                    Log.w(TAG("GoogleLoginUseCase", "syncWithServer"), "nickname: ${response.data?.nickname}")
                }

                // ✅ 서버에서 보낸 소셜 타입을 한글로 변환
                val rawSocialType = response.data?.socialType

                Log.w(TAG("GoogleLoginUseCase", "syncWithServer"), "rawSocialType 추출 결과: '$rawSocialType'")

                val socialTypeDisplay = when (rawSocialType?.uppercase()) {
                    "GOOGLE" -> "Google"
                    "KAKAO" -> "Kakao"
                    "NAVER" -> "Naver"
                    "APPLE" -> "Apple"
                    null -> {
                        Log.e(TAG("GoogleLoginUseCase", "syncWithServer"), "⚠️ socialType이 null입니다!")
                        "알 수 없는 소셜"
                    }
                    else -> {
                        Log.e(TAG("GoogleLoginUseCase", "syncWithServer"), "⚠️ 예상하지 못한 socialType: '$rawSocialType'")
                        "알 수 없는 소셜($rawSocialType)"
                    }
                }

                Log.w(TAG("GoogleLoginUseCase", "syncWithServer"), "최종 표시 타입: '$socialTypeDisplay'")

                return SyncResult.AlreadyLinked(
                    currentSocialType = socialTypeDisplay,
                    currentEmail = response.data?.email,
                    currentNickname = response.data?.nickname
                )
            }

            Log.d(TAG("GoogleLoginUseCase", "syncWithServer"), "서버 동기화 성공: ${response.message}")

            val syncedUser = user.copy(isSynced = true)
            userRepository.localUserUpdate(syncedUser)

            SyncResult.Success

        } catch (e: Exception) {
            Log.e(TAG("GoogleLoginUseCase", "syncWithServer"), "서버 동기화 실패", e)
            SyncResult.Error(e)
        }
    }
}

/**
 * 소셜 로그아웃 UseCase
 */
/**
 * 소셜 로그아웃 UseCase (수정 완료)
 *
 * 로그아웃: 클라이언트(앱)에서만 로그아웃 처리
 * - SDK 로그아웃 (Kakao/Google)
 * - 로컬 DB 소셜 정보 초기화
 * - 서버 데이터는 유지 (목표환율 등 보존)
 */
class SocialLogoutUseCase @Inject constructor(
    private val userRepository: UserRepository,
    private val socialLoginManager: SocialLoginManager
) {
    suspend operator fun invoke(localUserData: LocalUserData): Result<LocalUserData> {
        return try {
            Log.d(TAG("SocialLogoutUseCase", "invoke"), "로그아웃 시작: ${localUserData.socialType}")

            // ✅ 1단계: 먼저 로컬 DB 업데이트 (소셜 정보 초기화)
            val updatedUser = localUserData.copy(
                socialId = "",
                socialType = SocialType.NONE.name,
                email = "",
                nickname = "",
                profileUrl = "",
                isSynced = false
            )

            userRepository.localUserUpdate(updatedUser)
            Log.d(TAG("SocialLogoutUseCase", "invoke"), "✅ 로컬 DB 업데이트 완료")

            // ✅ 2단계: SDK 로그아웃 시도 (실패해도 계속 진행)
            val socialTypeEnum = localUserData.getSocialTypeEnum()
            val logoutResult = socialLoginManager.logout(socialTypeEnum)

            if (logoutResult.isFailure) {
                val error = logoutResult.exceptionOrNull()
                Log.w(TAG("SocialLogoutUseCase", "invoke"), "SDK 로그아웃 실패했지만 계속 진행: ${error?.message}")
                // ⚠️ SDK 로그아웃 실패해도 DB는 이미 업데이트되었으므로 성공 처리
            } else {
                Log.d(TAG("SocialLogoutUseCase", "invoke"), "✅ SDK 로그아웃 성공")
            }

            // ⚠️ 주의: 로그아웃은 클라이언트만 처리 (서버 데이터 변경 없음)
            Log.d(TAG("SocialLogoutUseCase", "invoke"), "로그아웃 완료 (로컬만)")

            Result.Success(
                data = updatedUser,  // ✅ 업데이트된 사용자 데이터 반환
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
 * 소셜 연동 해제 UseCase (새로 추가)
 */
class UnlinkSocialUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(localUserData: LocalUserData): Result<Unit> {
        return try {
            Log.d(TAG("UnlinkSocialUseCase", "invoke"), "소셜 연동 해제 시작")

            // 서버에 연동 해제 요청
            val response = UserApi.userService.unlinkSocial(
                deviceId = localUserData.id.toString()
            )

            if (!response.success) {
                return Result.Error(message = response.message)
            }

            // 로컬 DB 업데이트 (소셜 정보만 초기화, 목표환율 등은 유지)
            val updatedUser = localUserData.copy(
                socialId = null,
                socialType = SocialType.NONE.name,
                email = null,
                nickname = null,
                profileUrl = null,
                isSynced = true
            )

            userRepository.localUserUpdate(updatedUser)

            Log.d(TAG("UnlinkSocialUseCase", "invoke"), "소셜 연동 해제 완료")

            Result.Success(
                data = Unit,
                message = "연동이 해제되었습니다"
            )

        } catch (e: Exception) {
            Log.e(TAG("UnlinkSocialUseCase", "invoke"), "연동 해제 실패", e)
            Result.Error(
                message = "연동 해제에 실패했습니다",
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

            if (!response.success) {
                return Result.Error(message = response.message)
            }

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
    suspend operator fun invoke(socialId: String, socialType: String): Result<LocalUserData> {
        return try {
            Log.d(TAG("RestoreFromServerUseCase", "invoke"), "데이터 복구 시작: $socialId ($socialType)")

            // ✅ find-by-social 엔드포인트 사용
            val response = UserApi.userService.findBySocial(
                socialId = socialId,
                socialType = socialType
            )

            if (!response.success || response.data == null) {
                return Result.Error(message = response.message ?: "서버에 저장된 데이터가 없습니다")
            }

            val serverData = response.data

            // TODO: 서버 데이터를 LocalUserData로 변환 후 로컬 DB에 저장
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