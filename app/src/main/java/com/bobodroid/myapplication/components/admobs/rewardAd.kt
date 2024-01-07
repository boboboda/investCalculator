package com.bobodroid.myapplication.components.admobs

import android.content.Context
import android.util.Log
import com.bobodroid.myapplication.BuildConfig
import com.bobodroid.myapplication.MainActivity.Companion.TAG
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.OnUserEarnedRewardListener
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback


private var rewardedAd: RewardedAd? = null

fun loadRewardedAdvertisement(context: Context) {
    var adRequest = AdRequest.Builder().build()
        RewardedAd.load(context, BuildConfig.REWARD_FRONT_AD_KEY, adRequest, object : RewardedAdLoadCallback() {
            override fun onAdFailedToLoad(adError: LoadAdError) {
                Log.d(TAG, "${adError?.toString()}")
                rewardedAd = null
            }


            override fun onAdLoaded(ad: RewardedAd) {
                Log.d(TAG, "Ad was loaded.")
                rewardedAd = ad
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

                loadRewardedAdvertisement(context)
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