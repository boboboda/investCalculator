package com.bobodroid.myapplication.util.analytics

import com.google.android.gms.ads.AdValue
import com.google.android.gms.ads.LoadAdError

/** 광고 이벤트의 format 값. 대시보드에서 이 값으로 광고 종류를 나눈다. */
object AdFormat {
    const val BANNER = "banner"
    const val INTERSTITIAL = "interstitial"
    const val REWARDED = "rewarded"
    const val REWARDED_TARGET = "rewarded_target"
}

fun trackAdLoad(format: String) {
    trackEvent("ad_load", mapOf("format" to format))
}

fun trackAdLoadFailed(format: String, error: LoadAdError) {
    trackEvent(
        "ad_load_failed",
        mapOf(
            "format" to format,
            "code" to error.code,
            "message" to error.message.take(100),
        ),
    )
}

fun trackAdImpression(format: String) {
    trackEvent("ad_impression", mapOf("format" to format))
}

fun trackAdClick(format: String) {
    trackEvent("ad_click", mapOf("format" to format))
}

fun trackAdRewardEarned(format: String) {
    trackEvent("ad_reward_earned", mapOf("format" to format))
}

/** AdMob 이 알려주는 광고 1회 예상 수익 (valueMicros 는 100만분의 1 통화 단위) */
fun trackAdPaid(format: String, value: AdValue) {
    trackEvent(
        "ad_paid",
        mapOf(
            "format" to format,
            "valueMicros" to value.valueMicros,
            "currency" to value.currencyCode,
            "precision" to value.precisionType,
        ),
    )
}