package com.bobodroid.myapplication.util.AdMob

import android.content.Context
import android.util.Log
import com.bobodroid.myapplication.MainActivity.Companion.TAG
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AdManager @Inject constructor() {

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━
    // 광고 준비 상태
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━

    private val _isRewardAdReady = MutableStateFlow(false)
    val isRewardAdReady = _isRewardAdReady.asStateFlow()

    private val _isRewardTargetAdReady = MutableStateFlow(false)
    val isRewardTargetAdReady = _isRewardTargetAdReady.asStateFlow()

    private val _isInterstitialAdReady = MutableStateFlow(false)
    val isInterstitialAdReady = _isInterstitialAdReady.asStateFlow()

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━
    // 광고 로드
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━

    /**
     * 리워드 광고 로드
     */
    fun loadRewardedAd(context: Context) {
        loadRewardedAdvertisement(context, onReadyAd = {
            _isRewardAdReady.value = it
            Log.d(TAG("AdManager", "loadRewardedAd"), "리워드 광고 준비: $it")
        })

        loadTargetRewardedAdvertisement(context, onReadyAd = {
            _isRewardTargetAdReady.value = it
            Log.d(TAG("AdManager", "loadRewardedAd"), "타겟 리워드 광고 준비: $it")
        })
    }

    /**
     * 전면 광고 로드
     */
    fun loadInterstitialAd(context: Context) {
        loadInterstitial(context, onReady = {
            _isInterstitialAdReady.value = it
            Log.d(TAG("AdManager", "loadInterstitialAd"), "전면 광고 준비: $it")
        })
    }

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━
    // 광고 표시
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━

    /**
     * 전면 광고 표시
     * @param onAdShown 광고가 닫힌 후 호출되는 콜백
     */
    fun showInterstitialAd(
        context: Context,
        onAdShown: () -> Unit = {},
        onAdFailed: () -> Unit = {}
    ) {
        if (!_isInterstitialAdReady.value) {
            Log.w(TAG("AdManager", "showInterstitialAd"), "전면 광고가 준비되지 않음")
            onAdFailed()
            // 광고 준비 안 되어도 다음 번을 위해 로드
            loadInterstitialAd(context)
            return
        }

        Log.d(TAG("AdManager", "showInterstitialAd"), "전면 광고 표시 시작")

        showInterstitial(context) {
            // 광고가 닫힌 후
            _isInterstitialAdReady.value = false

            Log.d(TAG("AdManager", "showInterstitialAd"), "전면 광고 닫힘")

            // 다음 광고 미리 로드
            loadInterstitialAd(context)

            // 콜백 호출
            onAdShown()
        }
    }

    /**
     * 리워드 광고 표시
     * @param onRewarded 광고 시청 완료 후 보상 지급 시 호출
     * @param onAdClosed 광고가 닫힌 후 호출 (보상 여부 무관)
     */
    fun showRewardAd(
        context: Context,
        onRewarded: () -> Unit = {},
        onAdClosed: () -> Unit = {},
        onAdFailed: () -> Unit = {}
    ) {
        if (!_isRewardAdReady.value) {
            Log.w(TAG("AdManager", "showRewardAd"), "리워드 광고가 준비되지 않음")
            onAdFailed()
            // 광고 준비 안 되어도 다음 번을 위해 로드
            loadRewardedAd(context)
            return
        }

        Log.d(TAG("AdManager", "showRewardAd"), "리워드 광고 표시 시작")

        showRewardedAdvertisement(context) {
            // 광고가 닫힌 후
            _isRewardAdReady.value = false

            Log.d(TAG("AdManager", "showRewardAd"), "리워드 광고 닫힘")

            // 다음 광고 미리 로드
            loadRewardedAd(context)

            // 보상 지급
            onRewarded()

            // 닫힘 콜백
            onAdClosed()
        }
    }

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━
    // 유틸리티
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━

    /**
     * 앱 시작 시 모든 광고 미리 로드
     */
    fun preloadAllAds(context: Context) {
        loadInterstitialAd(context)
        loadRewardedAd(context)
        Log.d(TAG("AdManager", "preloadAllAds"), "모든 광고 로드 시작")
    }
}