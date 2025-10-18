package com.bobodroid.myapplication.models.datamodels.social

import android.app.Activity
import android.content.Context
import android.util.Log
import com.bobodroid.myapplication.MainActivity.Companion.TAG
import com.bobodroid.myapplication.models.datamodels.roomDb.SocialType
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.common.model.ClientError
import com.kakao.sdk.common.model.ClientErrorCause
import com.kakao.sdk.user.UserApiClient
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

data class SocialLoginResult(
    val socialId: String,
    val socialType: SocialType,
    val email: String?,
    val nickname: String?,
    val profileUrl: String?
)

@Singleton
class SocialLoginManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private var googleSignInClient: GoogleSignInClient? = null

    private fun initGoogleSignInClient() {
        if (googleSignInClient == null) {
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestProfile()
                .build()

            googleSignInClient = GoogleSignIn.getClient(context, gso)
        }
    }

    suspend fun loginWithGoogle(activity: Activity): SocialLoginResult = suspendCancellableCoroutine { continuation ->
        initGoogleSignInClient()

        val lastSignedInAccount = GoogleSignIn.getLastSignedInAccount(context)

        if (lastSignedInAccount != null) {
            Log.d(TAG("SocialLoginManager", "loginWithGoogle"), "이미 로그인된 계정 사용")
            val result = SocialLoginResult(
                socialId = lastSignedInAccount.id ?: "",
                socialType = SocialType.GOOGLE,
                email = lastSignedInAccount.email,
                nickname = lastSignedInAccount.displayName,
                profileUrl = lastSignedInAccount.photoUrl?.toString()
            )
            continuation.resume(result)
        } else {
            Log.d(TAG("SocialLoginManager", "loginWithGoogle"), "새 로그인 시작")
            continuation.resumeWithException(
                IllegalStateException("Google 로그인은 Activity에서 처리해야 합니다")
            )
        }
    }

    fun handleGoogleSignInResult(task: Task<GoogleSignInAccount>): SocialLoginResult {
        return try {
            val account = task.getResult(ApiException::class.java)
            SocialLoginResult(
                socialId = account.id ?: "",
                socialType = SocialType.GOOGLE,
                email = account.email,
                nickname = account.displayName,
                profileUrl = account.photoUrl?.toString()
            )
        } catch (e: ApiException) {
            Log.e(TAG("SocialLoginManager", "handleGoogleSignInResult"), "Google 로그인 실패: ${e.statusCode}")
            throw e
        }
    }

    /**
     * ✅ Kakao 로그인 (Activity Context 사용)
     */
    suspend fun loginWithKakao(activity: Activity): SocialLoginResult = suspendCancellableCoroutine { continuation ->

        val callback: (OAuthToken?, Throwable?) -> Unit = { token, error ->
            if (error != null) {
                Log.e(TAG("SocialLoginManager", "loginWithKakao"), "❌ Kakao 로그인 실패", error)
                continuation.resumeWithException(error)
            } else if (token != null) {
                Log.d(TAG("SocialLoginManager", "loginWithKakao"), "✅ Kakao 토큰 발급 성공")
                Log.d(TAG("SocialLoginManager", "loginWithKakao"), "→ 사용자 정보 요청 중...")

                // ✅ 사용자 정보 요청
                UserApiClient.instance.me { user, userError ->
                    Log.d(TAG("SocialLoginManager", "loginWithKakao"), "→ 사용자 정보 응답 받음")

                    if (userError != null) {
                        Log.e(TAG("SocialLoginManager", "loginWithKakao"), "❌ 사용자 정보 요청 실패", userError)
                        continuation.resumeWithException(userError)
                    } else if (user != null) {
                        Log.d(TAG("SocialLoginManager", "loginWithKakao"), "✅ 사용자 정보 조회 성공")
                        Log.d(TAG("SocialLoginManager", "loginWithKakao"), "   - ID: ${user.id}")
                        Log.d(TAG("SocialLoginManager", "loginWithKakao"), "   - Email: ${user.kakaoAccount?.email}")
                        Log.d(TAG("SocialLoginManager", "loginWithKakao"), "   - Nickname: ${user.kakaoAccount?.profile?.nickname}")
                        Log.d(TAG("SocialLoginManager", "loginWithKakao"), "   - ProfileUrl: ${user.kakaoAccount?.profile?.profileImageUrl}")

                        val result = SocialLoginResult(
                            socialId = user.id.toString(),
                            socialType = SocialType.KAKAO,
                            email = user.kakaoAccount?.email,
                            nickname = user.kakaoAccount?.profile?.nickname,
                            profileUrl = user.kakaoAccount?.profile?.profileImageUrl
                        )

                        Log.d(TAG("SocialLoginManager", "loginWithKakao"), "✅ SocialLoginResult 생성 완료")
                        continuation.resume(result)
                    } else {
                        Log.e(TAG("SocialLoginManager", "loginWithKakao"), "❌ user 객체가 null입니다")
                        continuation.resumeWithException(Exception("사용자 정보를 가져올 수 없습니다"))
                    }
                }
            } else {
                Log.e(TAG("SocialLoginManager", "loginWithKakao"), "❌ token이 null입니다")
                continuation.resumeWithException(Exception("토큰을 가져올 수 없습니다"))
            }
        }

        // ✅ Activity Context 사용
        if (UserApiClient.instance.isKakaoTalkLoginAvailable(activity)) {
            // 카카오톡으로 로그인
            UserApiClient.instance.loginWithKakaoTalk(activity) { token, error ->
                if (error != null) {
                    Log.e(TAG("SocialLoginManager", "loginWithKakao"), "카카오톡 로그인 실패", error)

                    if (error is ClientError && error.reason == ClientErrorCause.Cancelled) {
                        continuation.resumeWithException(error)
                        return@loginWithKakaoTalk
                    }

                    // 카카오톡 로그인 실패 시 카카오 계정으로 로그인 시도
                    UserApiClient.instance.loginWithKakaoAccount(activity, callback = callback)
                } else {
                    callback(token, null)
                }
            }
        } else {
            // 카카오 계정으로 로그인
            UserApiClient.instance.loginWithKakaoAccount(activity, callback = callback)
        }
    }

    suspend fun logoutGoogle(): Result<Unit> = suspendCancellableCoroutine { continuation ->
        initGoogleSignInClient()
        googleSignInClient?.signOut()?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Log.d(TAG("SocialLoginManager", "logoutGoogle"), "Google 로그아웃 성공")
                continuation.resume(Result.success(Unit))
            } else {
                Log.e(TAG("SocialLoginManager", "logoutGoogle"), "Google 로그아웃 실패")
                continuation.resume(Result.failure(task.exception ?: Exception("로그아웃 실패")))
            }
        }
    }

    suspend fun logoutKakao(): Result<Unit> = suspendCancellableCoroutine { continuation ->
        UserApiClient.instance.logout { error ->
            if (error != null) {
                Log.e(TAG("SocialLoginManager", "logoutKakao"), "Kakao 로그아웃 실패", error)
                continuation.resume(Result.failure(error))
            } else {
                Log.d(TAG("SocialLoginManager", "logoutKakao"), "Kakao 로그아웃 성공")
                continuation.resume(Result.success(Unit))
            }
        }
    }

    suspend fun logout(socialType: SocialType): Result<Unit> {
        return when (socialType) {
            SocialType.GOOGLE -> logoutGoogle()
            SocialType.KAKAO -> logoutKakao()
            SocialType.NONE -> Result.success(Unit)
        }
    }
}