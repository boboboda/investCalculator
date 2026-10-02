package com.bobodroid.myapplication.util.AdMob

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import com.bobodroid.myapplication.BuildConfig
import com.bobodroid.myapplication.util.analytics.AdFormat
import com.bobodroid.myapplication.util.analytics.trackAdClick
import com.bobodroid.myapplication.util.analytics.trackAdImpression
import com.bobodroid.myapplication.util.analytics.trackAdLoad
import com.bobodroid.myapplication.util.analytics.trackAdLoadFailed
import com.bobodroid.myapplication.util.analytics.trackAdPaid
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.OnPaidEventListener
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback

var mInterstitialAd: InterstitialAd? = null


fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

fun loadInterstitial(context: Context, onReady:((Boolean) -> Unit)? = null) {
    InterstitialAd.load(
        context,
        BuildConfig.FONT_AD_KEY, //Change this with your own AdUnitID!
        AdRequest.Builder().build(),
        object : InterstitialAdLoadCallback() {
            override fun onAdFailedToLoad(adError: LoadAdError) {
                mInterstitialAd = null
                trackAdLoadFailed(AdFormat.INTERSTITIAL, adError)
            }

            override fun onAdLoaded(interstitialAd: InterstitialAd) {
                mInterstitialAd = interstitialAd
                interstitialAd.onPaidEventListener = OnPaidEventListener { value ->
                    trackAdPaid(AdFormat.INTERSTITIAL, value)
                }
                trackAdLoad(AdFormat.INTERSTITIAL)
                onReady?.invoke(true)
            }
        }
    )
}

fun showInterstitial(context: Context, onAdDismissed: () -> Unit) {
    val activity = context.findActivity()

    if (mInterstitialAd != null && activity != null) {
        mInterstitialAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdFailedToShowFullScreenContent(e: AdError) {
                mInterstitialAd = null
            }

            override fun onAdImpression() {
                trackAdImpression(AdFormat.INTERSTITIAL)
            }

            override fun onAdClicked() {
                trackAdClick(AdFormat.INTERSTITIAL)
            }

            override fun onAdDismissedFullScreenContent() {
                mInterstitialAd = null

                loadInterstitial(context)
                onAdDismissed()
            }
        }
        mInterstitialAd?.show(activity)
    }
}

fun removeInterstitial() {
    mInterstitialAd?.fullScreenContentCallback = null
    mInterstitialAd = null
}