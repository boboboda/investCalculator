package com.bobodroid.myapplication.util.AdMob

import android.content.Context
import android.util.Log
import com.bobodroid.myapplication.BuildConfig
import com.bobodroid.myapplication.MainActivity.Companion.TAG
import com.bobodroid.myapplication.util.analytics.AdFormat
import com.bobodroid.myapplication.util.analytics.trackAdClick
import com.bobodroid.myapplication.util.analytics.trackAdImpression
import com.bobodroid.myapplication.util.analytics.trackAdLoad
import com.bobodroid.myapplication.util.analytics.trackAdLoadFailed
import com.bobodroid.myapplication.util.analytics.trackAdPaid
import com.bobodroid.myapplication.util.analytics.trackAdRewardEarned
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.OnPaidEventListener
import com.google.android.gms.ads.OnUserEarnedRewardListener
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.google.android.gms.ads.rewarded.ServerSideVerificationOptions


private var rewardedAd: RewardedAd? = null

private var targetRewardedAd: RewardedAd? = null

/**
 * ✅ 프리미엄 리워드 광고 로드
 * @param deviceId SSV 콜백에서 "누구에게 지급할지" 판단하는 데 쓰임 (customData로 전달)
 */
fun loadRewardedAdvertisement(context: Context, deviceId: String, onReadyAd: (Boolean) -> Unit) {
    val adRequest = AdRequest.Builder().build()

    RewardedAd.load(
        context,
        BuildConfig.REWARD_FRONT_AD_KEY,
        adRequest,
        object : RewardedAdLoadCallback() {
            override fun onAdFailedToLoad(adError: LoadAdError) {
                Log.e(TAG("loadRewardedAdvertisement", ""), "$adError")
                rewardedAd = null
                trackAdLoadFailed(AdFormat.REWARDED, adError)
            }

            override fun onAdLoaded(ad: RewardedAd) {
                Log.d(TAG("loadRewardedAdvertisement", ""), "front Ad was loaded.")

                // ✅ SSV 옵션 설정 — customData에 deviceId를 실어서 서버가 식별할 수 있게 함
                val options = ServerSideVerificationOptions.Builder()
                    .setCustomData(deviceId)
                    .build()
                ad.setServerSideVerificationOptions(options)

                ad.onPaidEventListener = OnPaidEventListener { value ->
                    trackAdPaid(AdFormat.REWARDED, value)
                }

                rewardedAd = ad
                trackAdLoad(AdFormat.REWARDED)
                onReadyAd(true)
            }
        })
}

//
/**
 * ✅ 보상 획득(onUserEarnedReward)과 광고 닫힘(onAdDismissed)을 분리해서 전달
 * - 기존 코드는 두 이벤트가 하나로 묶여 있어서, 광고를 끝까지 안 봐도 보상이 나가는 문제가 있었음
 * - onUserEarnedReward: "유저가 보상 조건을 채웠다"는 클라이언트 측 신호 (실제 지급 확정은 서버 SSV)
 * - onAdDismissed: 광고 창이 닫힌 시점 (끝까지 봤든 중간에 껐든 항상 호출됨)
 */
fun showRewardedAdvertisement(
    context: Context,
    onUserEarnedReward: () -> Unit,
    onAdDismissed: () -> Unit
) {
    val activity = context.findActivity()

    if (rewardedAd != null && activity != null) {
        rewardedAd?.fullScreenContentCallback = object : FullScreenContentCallback() {

            override fun onAdClicked() {
                Log.d(TAG("loadRewardedAdvertisement", ""), "Ad was clicked.")
                trackAdClick(AdFormat.REWARDED)
            }

            override fun onAdImpression() {
                trackAdImpression(AdFormat.REWARDED)
            }

            override fun onAdFailedToShowFullScreenContent(p0: AdError) {
                Log.e(TAG("loadRewardedAdvertisement", ""), "Ad failed to show fullscreen content.")
                rewardedAd = null
                // ✅ 여기서 아무 콜백도 안 부르면 상위(AdUseCase)의 대기가 영원히 안 끝남
                onAdDismissed()
            }

            override fun onAdDismissedFullScreenContent() {
                rewardedAd = null
                onAdDismissed()
            }
        }
        rewardedAd?.let { ad ->
            ad.show(activity, OnUserEarnedRewardListener { rewardItem ->
                // ✅ 실제로 보상 조건을 채웠을 때만 호출되는 콜백 — 여기서 상위로 신호를 전달
                Log.d(TAG("loadRewardedAdvertisement", ""), "User earned the reward.")
                trackAdRewardEarned(AdFormat.REWARDED)
                onUserEarnedReward()
            })
        } ?: run {
            Log.d(TAG("loadRewardedAdvertisement", ""), "The rewarded ad wasn't ready yet.")
            onAdDismissed()
        }
    } else {
        onAdDismissed()
    }
}


fun loadTargetRewardedAdvertisement(context: Context, onReadyAd: ((Boolean) -> Unit)? = null) {
    val adRequest = AdRequest.Builder().build()

    RewardedAd.load(
        context,
        BuildConfig.REWARD_TARGET_FONT_AD_KEY,
        adRequest,
        object : RewardedAdLoadCallback() {
            override fun onAdFailedToLoad(adError: LoadAdError) {
                Log.e(TAG("loadRewardedAdvertisement", ""), "adError")
                targetRewardedAd = null
                trackAdLoadFailed(AdFormat.REWARDED_TARGET, adError)
            }

            override fun onAdLoaded(ad: RewardedAd) {
                Log.d(TAG("loadRewardedAdvertisement", ""), "Target Ad was loaded.")

                ad.onPaidEventListener = OnPaidEventListener { value ->
                    trackAdPaid(AdFormat.REWARDED_TARGET, value)
                }

                targetRewardedAd = ad
                trackAdLoad(AdFormat.REWARDED_TARGET)
                onReadyAd?.invoke(true)
            }
        })
}

//
fun showTargetRewardedAdvertisement(context: Context, onAdDismissed: () -> Unit) {
    val activity = context.findActivity()

    if (targetRewardedAd != null && activity != null) {
        targetRewardedAd?.fullScreenContentCallback = object : FullScreenContentCallback() {

            override fun onAdClicked() {
                Log.d(TAG("loadRewardedAdvertisement", ""), "Ad was clicked.")
                trackAdClick(AdFormat.REWARDED_TARGET)
            }

            override fun onAdImpression() {
                trackAdImpression(AdFormat.REWARDED_TARGET)
            }

            override fun onAdFailedToShowFullScreenContent(p0: AdError) {
                Log.e(TAG("loadRewardedAdvertisement", ""), "Ad failed to show fullscreen content.")
                targetRewardedAd = null
            }

            override fun onAdDismissedFullScreenContent() {
                targetRewardedAd = null
                loadTargetRewardedAdvertisement(context)
                onAdDismissed()
            }
        }
        targetRewardedAd?.let { ad ->
            ad.show(activity, OnUserEarnedRewardListener { rewardItem ->
                val rewardAmount = rewardItem.amount
                val rewardType = rewardItem.type
                Log.d(TAG("loadRewardedAdvertisement", ""), "User earned the reward.")
                trackAdRewardEarned(AdFormat.REWARDED_TARGET)
            })
        } ?: run {
            Log.d(TAG("loadRewardedAdvertisement", ""), "The rewarded ad wasn't ready yet.")
        }
    }
}