package com.bobodroid.myapplication.util.AdMob

import android.content.Context
import android.util.Log
import com.bobodroid.myapplication.MainActivity.Companion.TAG
import com.bobodroid.myapplication.models.datamodels.roomDb.LocalUserData
import com.bobodroid.myapplication.models.datamodels.roomDb.PremiumType
import com.bobodroid.myapplication.models.datamodels.useCases.UserUseCases
import com.bobodroid.myapplication.premium.PremiumManager
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.GregorianCalendar
import javax.inject.Inject

class AdUseCase @Inject constructor(
    private val userUseCases: UserUseCases,
    private val premiumManager: PremiumManager,
    private val adManager: AdManager  // ← AdManager 주입
) {

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━
    // 전면 광고 (통합 로직)
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
        if (updatedUser.interstitialAdCount % 2 != 0) {
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
        // 프리미엄 사용자는 광고 안 보여줌
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
     * 프리미엄 유도 팝업 표시 조건 (10회 이상 & 10의 배수)
     */
    fun shouldShowPremiumPrompt(user: LocalUserData): Boolean {
        // 프리미엄 사용자는 팝업 안 보여줌
        if (premiumManager.checkPremiumStatus(user) != PremiumType.NONE) {
            return false
        }

        // 10회 이상이고 10의 배수일 때만 표시
//        return user.interstitialAdCount >= 10 && user.interstitialAdCount % 10 == 0
       return user.interstitialAdCount >= 1
    }

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━
    // 리워드 광고 (통합 로직)
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━

    /**
     * 리워드 광고 표시 (모든 로직 통합)
     * - 하루 1회 체크
     * - 광고 표시
     * - 24시간 프리미엄 지급
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
        // 오늘 이미 사용했는지 확인
        if (!premiumManager.canUseRewardAdToday(user)) {
            Log.d(TAG("AdUseCase", "showRewardAdAndGrantPremium"), "오늘 이미 리워드 사용함")
            onAlreadyUsed()
            return false
        }

        Log.d(TAG("AdUseCase", "showRewardAdAndGrantPremium"), "리워드 광고 표시 시도")

        // 광고 표시
        adManager.showRewardAd(
            context = context,
            onRewarded = {
                // 광고 시청 완료 → 24시간 프리미엄 지급
                Log.d(TAG("AdUseCase", "showRewardAdAndGrantPremium"), "광고 시청 완료 - 프리미엄 지급")
                onSuccess()
            },
            onAdFailed = {
                Log.d(TAG("AdUseCase", "showRewardAdAndGrantPremium"), "광고 로드 실패")
                onAdFailed()
            }
        )

        // 24시간 프리미엄 지급
        return premiumManager.grantRewardPremium(user)
    }

    /**
     * 리워드 광고 표시 여부 확인
     */
    suspend fun processRewardAdState(user: LocalUserData): Boolean {
        // 프리미엄 사용자는 광고 안 보여줌
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
     * 리워드 광고 시청 완료 → 24시간 프리미엄 지급 (레거시)
     */
    suspend fun onRewardAdWatched(user: LocalUserData): Boolean {
        Log.d(TAG("AdUseCase", "onRewardAdWatched"), "리워드 광고 시청 완료")

        // 오늘 이미 사용했는지 확인
        if (!premiumManager.canUseRewardAdToday(user)) {
            Log.d(TAG("AdUseCase", "onRewardAdWatched"), "오늘 이미 리워드 사용함")
            return false
        }

        // 24시간 프리미엄 지급
        return premiumManager.grantRewardPremium(user)
    }

    /**
     * 30회 리워드 광고 시청 시 특별 혜택 제공 조건
     */
    fun shouldOfferSpecialDiscount(user: LocalUserData): Boolean {
        return user.totalRewardCount >= 30 &&
                premiumManager.checkPremiumStatus(user) == PremiumType.NONE
    }

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━
    // 배너 광고
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━

    /**
     * 배너 광고 표시 상태 확인
     */
    fun bannerAdState(user: LocalUserData): Boolean {
        // 프리미엄 사용자는 광고 안 보여줌
        if (premiumManager.checkPremiumStatus(user) != PremiumType.NONE) {
            return false
        } else {
            return true
        }
    }

    /**
     * 배너 광고 제거 딜레이
     */
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
    // 유틸리티
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━

    /**
     * 날짜 연기 계산
     */
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