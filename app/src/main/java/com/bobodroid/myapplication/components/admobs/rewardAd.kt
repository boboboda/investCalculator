package com.bobodroid.myapplication.components.admobs

import android.content.Context
import android.util.Log
import com.bobodroid.myapplication.BuildConfig
import com.bobodroid.myapplication.MainActivity.Companion.TAG
import com.bobodroid.myapplication.models.viewmodels.AllViewModel
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.OnUserEarnedRewardListener
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback


private var rewardedAd: RewardedAd? = null

private var targetRewardedAd: RewardedAd? = null

fun loadRewardedAdvertisement(context: Context, allViewModel: AllViewModel) {
    val adRequest = AdRequest.Builder().build()

    RewardedAd.load(
        context,
        BuildConfig.REWARD_FRONT_AD_KEY,
        adRequest,
        object : RewardedAdLoadCallback() {
            override fun onAdFailedToLoad(adError: LoadAdError) {
                Log.e(TAG, "$adError")
                rewardedAd = null
            }


            override fun onAdLoaded(ad: RewardedAd) {
                Log.d(TAG, "Ad was loaded.")
                rewardedAd = ad
                allViewModel.onReadyRewardAd.value = true
            }
        })
}

//
fun showRewardedAdvertisement(context: Context, onAdDismissed: () -> Unit) {
    val activity = context.findActivity()

    if (rewardedAd != null && activity != null) {
        rewardedAd?.fullScreenContentCallback = object : FullScreenContentCallback() {

            override fun onAdClicked() {
                // Called when a click is recorded for an ad.
                Log.d(TAG, "Ad was clicked.")
            }

            override fun onAdFailedToShowFullScreenContent(p0: AdError) {
                // Called when ad fails to show.
                Log.e(TAG, "Ad failed to show fullscreen content.")
                rewardedAd = null
            }


            override fun onAdDismissedFullScreenContent() {
                rewardedAd = null

                onAdDismissed()
            }
        }
        rewardedAd?.let { ad ->
            ad.show(activity, OnUserEarnedRewardListener { rewardItem ->
                // Handle the reward.
                val rewardAmount = rewardItem.amount
                val rewardType = rewardItem.type

                Log.d(TAG, "User earned the reward.")

            })
        } ?: run {
            Log.d(TAG, "The rewarded ad wasn't ready yet.")
        }
    }
}


fun loadTargetRewardedAdvertisement(context: Context) {
    val adRequest = AdRequest.Builder().build()

    RewardedAd.load(
        context,
        BuildConfig.REWARD_TARGET_FONT_AD_KEY,
        adRequest,
        object : RewardedAdLoadCallback() {
            override fun onAdFailedToLoad(adError: LoadAdError) {
                Log.e(TAG, "adError")
                targetRewardedAd = null
            }


            override fun onAdLoaded(ad: RewardedAd) {
                Log.d(TAG, "Ad was loaded.")
                targetRewardedAd = ad
            }
        })
}

//
fun showTargetRewardedAdvertisement(context: Context, onAdDismissed: () -> Unit) {
    val activity = context.findActivity()

    if (targetRewardedAd != null && activity != null) {
        targetRewardedAd?.fullScreenContentCallback = object : FullScreenContentCallback() {

            override fun onAdClicked() {
                // Called when a click is recorded for an ad.
                Log.d(TAG, "Ad was clicked.")
            }

            override fun onAdFailedToShowFullScreenContent(p0: AdError) {
                // Called when ad fails to show.
                Log.e(TAG, "Ad failed to show fullscreen content.")
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
                // Handle the reward.
                val rewardAmount = rewardItem.amount
                val rewardType = rewardItem.type

                Log.d(TAG, "User earned the reward.")

            })
        } ?: run {
            Log.d(TAG, "The rewarded ad wasn't ready yet.")
        }
    }
}