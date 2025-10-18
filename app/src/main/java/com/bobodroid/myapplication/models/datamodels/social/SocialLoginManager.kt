package com.bobodroid.myapplication.models.datamodels.social

import android.app.Activity
import android.content.Context
import android.content.Intent
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
import kotlin.coroutines.Continuation
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

    // ✅ Google Sign-In 결과를 처리할 Continuation 저장
    private var googleSignInContinuation: Continuation<SocialLoginResult>? = null

    private fun initGoogleSignInClient() {
        if (googleSignInClient == null) {
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestProfile()
                .build()

            googleSignInClient = GoogleSignIn.getClient(context, gso)
        }
    }

    /**
     * ✅ Google 로그인 (Kakao 방식과 동일하게 Activity 사용)
     */
    suspend fun loginWithGoogle(activity: Activity): SocialLoginResult = suspendCancellableCoroutine { continuation ->
        initGoogleSignInClient()

        Log.d(TAG("SocialLoginManager", "loginWithGoogle"), "Google 로그인 시작")

        // ✅ Continuation 저장
        googleSignInContinuation = continuation

        // ✅ Google Sign-In Intent 시작
        val signInIntent = googleSignInClient!!.signInIntent

        try {
            activity.startActivityForResult(signInIntent, GOOGLE_SIGN_IN_REQUEST_CODE)
            Log.d(TAG("SocialLoginManager", "loginWithGoogle"), "Google Sign-In Intent 시작됨")
        } catch (e: Exception) {
            Log.e(TAG("SocialLoginManager", "loginWithGoogle"), "Intent 시작 실패", e)
            googleSignInContinuation = null
            continuation.resumeWithException(e)
        }

        // ✅ 취소 처리
        continuation.invokeOnCancellation {
            Log.d(TAG("SocialLoginManager", "loginWithGoogle"), "Google 로그인 취소됨")
            googleSignInContinuation = null
        }
    }

    /**
     * ✅ Google Sign-In 결과 처리 (Activity의 onActivityResult에서 호출)
     */
    fun handleGoogleSignInResult(requestCode: Int, resultCode: Int, data: Intent?) {
        Log.d(TAG("SocialLoginManager", "handleGoogleSignInResult"), "requestCode=$requestCode, resultCode=$resultCode, data=$data")

        if (requestCode != GOOGLE_SIGN_IN_REQUEST_CODE) {
            Log.d(TAG("SocialLoginManager", "handleGoogleSignInResult"), "다른 요청 코드: $requestCode")
            return
        }

        val continuation = googleSignInContinuation ?: run {
            Log.e(TAG("SocialLoginManager", "handleGoogleSignInResult"), "❌ Continuation이 null입니다")
            return
        }

        googleSignInContinuation = null

        if (resultCode == Activity.RESULT_OK && data != null) {
            try {
                val task = GoogleSignIn.getSignedInAccountFromIntent(data)
                val account = task.getResult(ApiException::class.java)

                Log.d(TAG("SocialLoginManager", "handleGoogleSignInResult"), "✅ Google 로그인 성공")
                Log.d(TAG("SocialLoginManager", "handleGoogleSignInResult"), "   - Email: ${account.email}")
                Log.d(TAG("SocialLoginManager", "handleGoogleSignInResult"), "   - Nickname: ${account.displayName}")

                val result = SocialLoginResult(
                    socialId = account.id ?: "",
                    socialType = SocialType.GOOGLE,
                    email = account.email,
                    nickname = account.displayName,
                    profileUrl = account.photoUrl?.toString()
                )

                continuation.resume(result)

            } catch (e: ApiException) {
                Log.e(TAG("SocialLoginManager", "handleGoogleSignInResult"), "❌ Google 로그인 실패: ${e.statusCode}", e)
                continuation.resumeWithException(e)
            }
        } else {
            Log.e(TAG("SocialLoginManager", "handleGoogleSignInResult"), "❌ 로그인 취소 또는 실패 (resultCode: $resultCode)")
            continuation.resumeWithException(Exception("Google 로그인이 취소되었습니다"))
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

        if (UserApiClient.instance.isKakaoTalkLoginAvailable(activity)) {
            UserApiClient.instance.loginWithKakaoTalk(activity) { token, error ->
                if (error != null) {
                    Log.e(TAG("SocialLoginManager", "loginWithKakao"), "카카오톡 로그인 실패", error)

                    if (error is ClientError && error.reason == ClientErrorCause.Cancelled) {
                        continuation.resumeWithException(error)
                        return@loginWithKakaoTalk
                    }

                    UserApiClient.instance.loginWithKakaoAccount(activity, callback = callback)
                } else {
                    callback(token, null)
                }
            }
        } else {
            UserApiClient.instance.loginWithKakaoAccount(activity, callback = callback)
        }
    }

    fun isGoogleLoggedIn(): Boolean {
        return try {
            val account = GoogleSignIn.getLastSignedInAccount(context)
            val isLoggedIn = account != null
            Log.d(TAG("SocialLoginManager", "isGoogleLoggedIn"), "Google 로그인 상태: $isLoggedIn")
            isLoggedIn
        } catch (e: Exception) {
            Log.e(TAG("SocialLoginManager", "isGoogleLoggedIn"), "상태 확인 실패", e)
            false
        }
    }

    fun isKakaoLoggedIn(): Boolean {
        return try {
            val hasToken = UserApiClient.instance.isKakaoTalkLoginAvailable(context)
            Log.d(TAG("SocialLoginManager", "isKakaoLoggedIn"), "Kakao 토큰 존재: $hasToken")
            hasToken
        } catch (e: Exception) {
            Log.e(TAG("SocialLoginManager", "isKakaoLoggedIn"), "상태 확인 실패", e)
            false
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
                Log.e(TAG("SocialLoginManager", "logoutKakao"), "Kakao 로그아웃 API 호출 실패", error)

                if (error is ClientError && error.reason == ClientErrorCause.TokenNotFound) {
                    Log.d(TAG("SocialLoginManager", "logoutKakao"), "✅ 이미 로그아웃된 상태 (토큰 없음) - 성공 처리")
                    continuation.resume(Result.success(Unit))
                } else {
                    Log.w(TAG("SocialLoginManager", "logoutKakao"), "⚠️ 기타 에러 발생하지만 계속 진행")
                    continuation.resume(Result.success(Unit))
                }
            } else {
                Log.d(TAG("SocialLoginManager", "logoutKakao"), "✅ Kakao 로그아웃 성공")
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

    companion object {
        const val GOOGLE_SIGN_IN_REQUEST_CODE = 9001
    }
}