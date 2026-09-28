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
     * ✅ deviceId 추가 — SSV customData로 실어서 서버가 누구에게 지급할지 식별
     */
    fun loadRewardedAd(context: Context, deviceId: String) {
        loadRewardedAdvertisement(context, deviceId, onReadyAd = {
            _isRewardAdReady.value = it
            Log.d(TAG("AdManager", "loadRewardedAd"), "리워드 광고 준비: $it")
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
     * ✅ deviceId 추가 — 시청 후 다음 광고 재로드 시에도 필요
     * ✅ onRewarded는 "실제로 끝까지 봐서 보상 조건을 채웠을 때"만 호출 (기존 버그 수정)
     * @param onAdClosed 광고가 닫힌 후 호출 (보상 여부 무관, 항상 호출됨)
     */
    fun showRewardAd(
        context: Context,
        deviceId: String,
        onRewarded: () -> Unit = {},
        onAdClosed: () -> Unit = {},
        onAdFailed: () -> Unit = {}
    ) {
        if (!_isRewardAdReady.value) {
            Log.w(TAG("AdManager", "showRewardAd"), "리워드 광고가 준비되지 않음")
            onAdFailed()
            // 광고 준비 안 되어도 다음 번을 위해 로드
            loadRewardedAd(context, deviceId)
            return
        }

        Log.d(TAG("AdManager", "showRewardAd"), "리워드 광고 표시 시작")

        showRewardedAdvertisement(
            context = context,
            onUserEarnedReward = {
                // ✅ 실제로 끝까지 봤을 때만 호출됨 — 진짜 "보상 획득" 신호
                Log.d(TAG("AdManager", "showRewardAd"), "✅ 보상 조건 충족")
                onRewarded()
            },
            onAdDismissed = {
                // 광고가 닫힌 후 (끝까지 봤든 중간에 껐든 항상 호출)
                _isRewardAdReady.value = false

                Log.d(TAG("AdManager", "showRewardAd"), "리워드 광고 닫힘")

                // 다음 광고 미리 로드
                loadRewardedAd(context, deviceId)

                // 닫힘 콜백
                onAdClosed()
            }
        )
    }

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━
    // 유틸리티
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━

    /**
     * 앱 시작 시 모든 광고 미리 로드
     * ✅ deviceId 추가
     */
    fun preloadAllAds(context: Context, deviceId: String) {
        loadInterstitialAd(context)
        loadRewardedAd(context, deviceId)
        Log.d(TAG("AdManager", "preloadAllAds"), "모든 광고 로드 시작")
    }
}