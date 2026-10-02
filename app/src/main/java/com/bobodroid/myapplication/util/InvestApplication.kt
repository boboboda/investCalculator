package com.bobodroid.myapplication.util

import android.app.Application
import android.content.pm.PackageManager
import android.util.Base64
import android.util.Log
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.bobodroid.myapplication.BuildConfig
import com.bobodroid.myapplication.billing.BillingClientLifecycle
import com.bobodroid.myapplication.util.analytics.AnalyticsTracker
import com.google.android.gms.ads.MobileAds
import com.kakao.sdk.common.KakaoSdk
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import java.security.MessageDigest
import javax.inject.Inject

@HiltAndroidApp
class InvestApplication: Application(), Configuration.Provider {

    @Inject
    lateinit var appStarter: AppStarter

    @Inject
    lateinit var analyticsTracker: AnalyticsTracker

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    companion object {
        lateinit var prefs: PreferenceUtil
        lateinit var instance: InvestApplication
            private set
    }

    lateinit var billingClientLifecycle: BillingClientLifecycle
        private set

    override fun onCreate() {
        super.onCreate()

        // ✅ 1. 키 해시 출력 (Debug & Release 모두)
        printKeyHash()

        // ✅ 2. Kakao 앱 키 확인
        Log.d("KakaoInit", "========================================")
        Log.d("KakaoInit", "Kakao 앱 키: ${BuildConfig.KAKAO_APP_KEY}")
        Log.d("KakaoInit", "앱 키 길이: ${BuildConfig.KAKAO_APP_KEY.length}")
        Log.d("KakaoInit", "앱 키 비어있음: ${BuildConfig.KAKAO_APP_KEY.isEmpty()}")
        Log.d("KakaoInit", "========================================")

        // ✅ 3. Kakao SDK 초기화
        try {
            KakaoSdk.init(this, BuildConfig.KAKAO_APP_KEY)
            Log.d("KakaoInit", "✅ Kakao SDK 초기화 성공!")
        } catch (e: Exception) {
            Log.e("KakaoInit", "❌ Kakao SDK 초기화 실패", e)
        }

        // 기존 초기화들
        MobileAds.initialize(this)
        prefs = PreferenceUtil(applicationContext)

        appStarter.startApp(this)

        analyticsTracker.start(this)

        instance = this
        this.billingClientLifecycle = BillingClientLifecycle.getInstance(this)
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    /**
     * ✅ 키 해시 자동 출력
     * Debug/Release 빌드에 따라 자동으로 키 해시 출력
     */
    private fun printKeyHash() {
        try {
            val buildType = if (BuildConfig.DEBUG) "DEBUG" else "RELEASE"

            // ✅ API 28 이상과 이하 분기 처리
            val signatures = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                // API 28 이상
                val signingInfo = packageManager.getPackageInfo(
                    packageName,
                    PackageManager.GET_SIGNING_CERTIFICATES
                ).signingInfo

                if (signingInfo?.hasMultipleSigners() == true) {
                    signingInfo.apkContentsSigners
                } else {
                    signingInfo?.signingCertificateHistory
                }
            } else {
                // API 28 미만
                @Suppress("DEPRECATION")
                packageManager.getPackageInfo(
                    packageName,
                    PackageManager.GET_SIGNATURES
                ).signatures
            }

            // ✅ null 체크
            signatures?.forEach { signature ->
                val md = MessageDigest.getInstance("SHA")
                md.update(signature.toByteArray())
                val keyHash = Base64.encodeToString(md.digest(), Base64.NO_WRAP)

                Log.e("KeyHash", "╔════════════════════════════════════════╗")
                Log.e("KeyHash", "║   🔑 Kakao Key Hash ($buildType)        ")
                Log.e("KeyHash", "╚════════════════════════════════════════╝")
                Log.e("KeyHash", "📱 패키지명: $packageName")
                Log.e("KeyHash", "🔐 키 해시: $keyHash")
                Log.e("KeyHash", "🏗️  빌드 타입: $buildType")

                if (BuildConfig.DEBUG) {
                    Log.e("KeyHash", "✅ 개발용 - Kakao Developers에 등록하세요!")
                } else {
                    Log.e("KeyHash", "🚀 배포용 - Play Store 출시 전 확인 필수!")
                }

                Log.e("KeyHash", "════════════════════════════════════════")
            } ?: run {
                Log.e("KeyHash", "❌ 서명 정보를 찾을 수 없습니다")
            }

        } catch (e: Exception) {
            Log.e("KeyHash", "❌ 키 해시 생성 실패", e)
        }
    }
}