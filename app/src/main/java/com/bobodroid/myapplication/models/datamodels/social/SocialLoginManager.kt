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

/**
 * 소셜 로그인 결과 데이터
 */
data class SocialLoginResult(
    val socialId: String,
    val socialType: SocialType,
    val email: String?,
    val nickname: String?,
    val profileUrl: String?
)

/**
 * 소셜 로그인 통합 매니저
 */
@Singleton
class SocialLoginManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private var googleSignInClient: GoogleSignInClient? = null

    /**
     * Google 로그인 클라이언트 초기화
     */
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
     * Kakao SDK 초기화
     * Application의 onCreate에서 한 번만 호출
     */
    fun initKakaoSdk() {
        // TODO: AndroidManifest.xml에 네이티브 앱 키 추가 필요
        // TODO: build.gradle에 kakao_app_key 추가 필요
        // com.kakao.sdk.common.KakaoSdk.init(context, "YOUR_KAKAO_APP_KEY")
        Log.d(TAG("SocialLoginManager", "initKakaoSdk"), "Kakao SDK 초기화 필요")
    }

    /**
     * Google 로그인
     */
    suspend fun loginWithGoogle(activity: Activity): SocialLoginResult = suspendCancellableCoroutine { continuation ->
        initGoogleSignInClient()

        val lastSignedInAccount = GoogleSignIn.getLastSignedInAccount(context)

        if (lastSignedInAccount != null) {
            // 이미 로그인되어 있음
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
            // 새로운 로그인 필요
            Log.d(TAG("SocialLoginManager", "loginWithGoogle"), "새 로그인 시작")

            // Activity Result API를 사용하는 방식으로 변경 권장
            // 여기서는 기존 방식 사용
            val signInIntent = googleSignInClient?.signInIntent

            // TODO: Activity에서 startActivityForResult 처리 필요
            // 임시로 에러 반환
            continuation.resumeWithException(
                IllegalStateException("Google 로그인은 Activity에서 처리해야 합니다")
            )
        }
    }

    /**
     * Google 로그인 결과 처리
     * Activity의 onActivityResult에서 호출
     */
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
     * Kakao 로그인
     */
    suspend fun loginWithKakao(activity: Activity): SocialLoginResult = suspendCancellableCoroutine { continuation ->

        val callback: (OAuthToken?, Throwable?) -> Unit = { token, error ->
            if (error != null) {
                Log.e(TAG("SocialLoginManager", "loginWithKakao"), "Kakao 로그인 실패", error)
                continuation.resumeWithException(error)
            } else if (token != null) {
                Log.d(TAG("SocialLoginManager", "loginWithKakao"), "Kakao 로그인 성공")

                // 사용자 정보 가져오기
                UserApiClient.instance.me { user, userError ->
                    if (userError != null) {
                        Log.e(TAG("SocialLoginManager", "loginWithKakao"), "사용자 정보 요청 실패", userError)
                        continuation.resumeWithException(userError)
                    } else if (user != null) {
                        val result = SocialLoginResult(
                            socialId = user.id.toString(),
                            socialType = SocialType.KAKAO,
                            email = user.kakaoAccount?.email,
                            nickname = user.kakaoAccount?.profile?.nickname,
                            profileUrl = user.kakaoAccount?.profile?.profileImageUrl
                        )
                        continuation.resume(result)
                    }
                }
            }
        }

        // 카카오톡 설치 여부 확인
        if (UserApiClient.instance.isKakaoTalkLoginAvailable(context)) {
            // 카카오톡으로 로그인
            UserApiClient.instance.loginWithKakaoTalk(context) { token, error ->
                if (error != null) {
                    Log.e(TAG("SocialLoginManager", "loginWithKakao"), "카카오톡 로그인 실패", error)

                    // 사용자가 취소한 경우
                    if (error is ClientError && error.reason == ClientErrorCause.Cancelled) {
                        continuation.resumeWithException(error)
                        return@loginWithKakaoTalk
                    }

                    // 카카오톡 로그인 실패 시 카카오 계정으로 로그인 시도
                    UserApiClient.instance.loginWithKakaoAccount(context, callback = callback)
                } else {
                    callback(token, null)
                }
            }
        } else {
            // 카카오 계정으로 로그인
            UserApiClient.instance.loginWithKakaoAccount(context, callback = callback)
        }
    }

    /**
     * Google 로그아웃
     */
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

    /**
     * Kakao 로그아웃
     */
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

    /**
     * 통합 로그아웃
     */
    suspend fun logout(socialType: SocialType): Result<Unit> {
        return when (socialType) {
            SocialType.GOOGLE -> logoutGoogle()
            SocialType.KAKAO -> logoutKakao()
            SocialType.NONE -> Result.success(Unit)
        }
    }
}