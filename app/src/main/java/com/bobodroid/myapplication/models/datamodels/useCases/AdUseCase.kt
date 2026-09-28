package com.bobodroid.myapplication.util.AdMob

import android.content.Context
import android.util.Log
import com.bobodroid.myapplication.MainActivity.Companion.TAG
import com.bobodroid.myapplication.models.datamodels.roomDb.LocalUserData
import com.bobodroid.myapplication.models.datamodels.roomDb.PremiumType
import com.bobodroid.myapplication.models.datamodels.useCases.UserUseCases
import com.bobodroid.myapplication.premium.PremiumManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.suspendCancellableCoroutine
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.GregorianCalendar
import javax.inject.Inject
import kotlin.coroutines.resume

class AdUseCase @Inject constructor(
    private val userUseCases: UserUseCases,
    private val premiumManager: PremiumManager,
    private val adManager: AdManager  // ← AdManager 주입
) {

    companion object {
        // SSV 콜백이 서버에 도착하는 데 걸리는 시간에 대한 여유. 필요시 조정
        private const val SSV_SYNC_DELAY_MS = 3000L
    }

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━
    // 전면 광고 (통합 로직) — 변경 없음
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━

    /**
     * 전면 광고 표시 (모든 로직 통합)
     * - 프리미엄 체크
     * - 3회마다 광고 표시
     * - 카운트 증가
     * - 10회 이상일 때 프리미엄 유도 팝업 여부 반환
     *
     * @return 프리미엄 유도 팝업을 표시해야 하면 true
     */
    suspend fun showInterstitialAdIfNeeded(
        context: Context,
        user: LocalUserData,
        onPremiumPromptNeeded: () -> Unit = {}
    ): Boolean {
        // 프리미엄 사용자는 광고 스킵
        if (premiumManager.checkPremiumStatus(user) != PremiumType.NONE) {
            Log.d(TAG("AdUseCase", "showInterstitialAdIfNeeded"), "프리미엄 사용자 - 광고 스킵")
            return false
        }

        // 카운트 먼저 증가
        val updatedUser = incrementInterstitialAdCount(user)

        Log.d(TAG("AdUseCase", "showInterstitialAdIfNeeded"),
            "광고 카운트: ${updatedUser.interstitialAdCount}")

        // 5의 배수일 때만 광고 표시
        if (updatedUser.interstitialAdCount % 5 != 0) {
            Log.d(TAG("AdUseCase", "showInterstitialAdIfNeeded"),
                "5의 배수 아님 - 광고 스킵")
            return false
        }

        Log.d(TAG("AdUseCase", "showInterstitialAdIfNeeded"),
            "5의 배수 - 전면 광고 표시 시도")

        // 광고 표시
        adManager.showInterstitialAd(
            context = context,
            onAdShown = {
                Log.d(TAG("AdUseCase", "showInterstitialAdIfNeeded"), "광고 표시 완료")
            },
            onAdFailed = {
                Log.d(TAG("AdUseCase", "showInterstitialAdIfNeeded"), "광고 실패")
            }
        )

        // 10회 이상일 때 프리미엄 유도 팝업
        if (shouldShowPremiumPrompt(updatedUser)) {
            onPremiumPromptNeeded()
            return true
        }

        return false
    }

    /**
     * 전면 광고 표시 여부 확인
     */
    fun shouldShowInterstitialAd(user: LocalUserData): Boolean {
        if (premiumManager.checkPremiumStatus(user) != PremiumType.NONE) {
            return false
        }
        return true
    }

    /**
     * 전면 광고 노출 카운트 증가
     */
    suspend fun incrementInterstitialAdCount(user: LocalUserData): LocalUserData {
        val updatedUser = user.copy(
            interstitialAdCount = user.interstitialAdCount + 1
        )
        userUseCases.localUserUpdate(updatedUser)

        Log.d(
            TAG("AdUseCase", "incrementInterstitialAdCount"),
            "전면 광고 카운트: ${updatedUser.interstitialAdCount}"
        )

        return updatedUser
    }

    /**
     * 프리미엄 유도 팝업 표시 조건 (10회 이상)
     */
    fun shouldShowPremiumPrompt(user: LocalUserData): Boolean {
        if (premiumManager.checkPremiumStatus(user) != PremiumType.NONE) {
            return false
        }
        return user.interstitialAdCount >= 10
    }

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━
    // 리워드 광고 (통합 로직) — ✅ 여기가 이번에 바뀐 핵심 함수
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━

    /**
     * 리워드 광고 표시 (모든 로직 통합)
     * - 하루 캡 1차 체크(로컬, UX용 — 최종 판단은 서버 SSV)
     * - 광고가 "실제로 끝까지 재생됐는지" 콜백이 올 때까지 대기
     * - ⚠️ 로컬 즉시 지급 없음 — SSV가 서버에 도착할 시간을 기다린 뒤 서버 상태 재조회
     *
     * @return 성공 여부
     */
    suspend fun showRewardAdAndGrantPremium(
        context: Context,
        user: LocalUserData,
        onSuccess: () -> Unit = {},
        onAlreadyUsed: () -> Unit = {},
        onAdFailed: () -> Unit = {}
    ): Boolean {
        // 1차 체크 (로컬, 버튼 비활성화 등 UX 용도)
        if (!premiumManager.canUseRewardAdToday(user)) {
            Log.d(TAG("AdUseCase", "showRewardAdAndGrantPremium"), "오늘 이미 리워드 사용함")
            onAlreadyUsed()
            return false
        }

        Log.d(TAG("AdUseCase", "showRewardAdAndGrantPremium"), "리워드 광고 표시 시도")

        // ✅ 광고를 "실제로 끝까지 봤는지" 콜백이 올 때까지 기다림
        val earnedReward = suspendCancellableCoroutine<Boolean> { cont ->
            var rewarded = false

            adManager.showRewardAd(
                context = context,
                deviceId = user.id.toString(),
                onRewarded = {
                    rewarded = true
                },
                onAdClosed = {
                    if (cont.isActive) cont.resume(rewarded)
                },
                onAdFailed = {
                    if (cont.isActive) cont.resume(false)
                }
            )
        }

        if (!earnedReward) {
            Log.d(TAG("AdUseCase", "showRewardAdAndGrantPremium"), "광고 미완료 - 지급 없음")
            onAdFailed()
            return false
        }

        Log.d(TAG("AdUseCase", "showRewardAdAndGrantPremium"), "광고 시청 완료 - SSV 도착 대기 후 서버 재조회")

        // SSV 콜백이 Google → 우리 서버로 도착할 시간을 잠깐 기다림
        delay(SSV_SYNC_DELAY_MS)

        // ⚠️ 로컬에서 직접 프리미엄을 지급하지 않고, 서버의 최종 판정을 그대로 반영
        premiumManager.refreshUnifiedPremiumStatus()

        onSuccess()
        return true
    }

    /**
     * 리워드 광고 표시 여부 확인
     */
    suspend fun processRewardAdState(user: LocalUserData): Boolean {
        if (premiumManager.checkPremiumStatus(user) != PremiumType.NONE) {
            return false
        } else {
            return true
        }
    }

    /**
     * 리워드 광고 연기 (다음 날까지)
     */
    suspend fun delayRewardAd(user: LocalUserData, todayDate: String) {
        val updatedUser = user.copy(
            rewardAdShowingDate = delayDate(inputDate = todayDate, delayDay = 1)
        )
        userUseCases.localUserUpdate(updatedUser)
    }

    /**
     * ⚠️⚠️ 확인 필요: 리워드 광고 시청 완료 → 프리미엄 지급 (레거시)
     * 이 함수가 여전히 어딘가에서 호출되고 있다면, showRewardAdAndGrantPremium과 달리
     * 로컬에서 직접 grantRewardPremium()을 호출하고 있어서 새 구조(서버 SSV 기준)와
     * 어긋납니다. 호출하는 곳이 있는지 확인해주세요 — 없으면 삭제 대상입니다.
     */
    @Deprecated("서버 SSV 기반 흐름과 어긋남 - showRewardAdAndGrantPremium 사용 권장")
    suspend fun onRewardAdWatched(user: LocalUserData): Boolean {
        Log.d(TAG("AdUseCase", "onRewardAdWatched"), "리워드 광고 시청 완료 (레거시 경로)")

        if (!premiumManager.canUseRewardAdToday(user)) {
            Log.d(TAG("AdUseCase", "onRewardAdWatched"), "오늘 이미 리워드 사용함")
            return false
        }

        return premiumManager.grantRewardPremium(user)
    }

    /**
     * 30회 리워드 광고 시청 시 특별 혜택 제공 조건 — 변경 없음
     */
    fun shouldOfferSpecialDiscount(user: LocalUserData): Boolean {
        return user.totalRewardCount >= 30 &&
                premiumManager.checkPremiumStatus(user) == PremiumType.NONE
    }

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━
    // 배너 광고 — 변경 없음
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━

    fun bannerAdState(user: LocalUserData): Boolean {
        if (premiumManager.checkPremiumStatus(user) != PremiumType.NONE) {
            return false
        } else {
            return true
        }
    }

    suspend fun deleteBannerDelayDate(user: LocalUserData, todayDate: String): Boolean {
        Log.d(TAG("AdUseCase", "deleteBannerDelayDate"), "날짜 연기 신청")

        val updateUserData = user.copy(
            userResetDate = delayDate(inputDate = todayDate, delayDay = 1)
        )
        userUseCases.localUserUpdate(updateUserData)

        Log.d(
            TAG("AdUseCase", "deleteBannerDelayDate"),
            "배너광고 삭제 딜레이 날짜: ${updateUserData.userResetDate}"
        )

        return true
    }

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━
    // 유틸리티 — 변경 없음
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━

    private fun delayDate(inputDate: String, delayDay: Int): String? {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd")
        val date: Date? = dateFormat.parse(inputDate)

        date?.let {
            val calendar = GregorianCalendar().apply {
                time = date
                add(Calendar.DAY_OF_MONTH, delayDay)
            }

            return dateFormat.format(calendar.time)
        }

        return null
    }
}