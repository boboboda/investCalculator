package com.bobodroid.myapplication.components.admobs

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.bobodroid.myapplication.BuildConfig
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView

@SuppressLint("ResourceType")
@Composable
fun BannerAd() {

    Box(
        modifier = Modifier
            .wrapContentSize()
            .padding(start = 10.dp, end = 10.dp)
    ) {
//        val adId = stringResource(id = R.string.admob_sample_app_id)
        val adRequest = com.google.android.gms.ads.AdRequest.Builder().build()
        AndroidView(
            modifier = Modifier.fillMaxWidth(),
            factory = {context ->
                AdView(context).apply {
                    setAdSize(AdSize.SMART_BANNER)
                    adUnitId = BuildConfig.BANNER_AD_KEY
                    loadAd(adRequest)

                }
            },
            update = {adView ->
                adView.loadAd(adRequest)
            }
        )

    }
}


@SuppressLint("ResourceType")
@Composable
fun BuyBannerAd() {

    Box(
        modifier = Modifier
            .wrapContentSize()
            .padding(start = 10.dp, end = 10.dp)
    ) {
//        val adId = stringResource(id = R.string.admob_sample_app_id)
        val adRequest = com.google.android.gms.ads.AdRequest.Builder().build()
        AndroidView(
            modifier = Modifier.fillMaxWidth(),
            factory = {context ->
                AdView(context).apply {
                    setAdSize(AdSize.SMART_BANNER)
                    adUnitId = BuildConfig.BUY_BANNER_AD_KEY
                    loadAd(adRequest)

                }
            },
            update = {adView ->
                adView.loadAd(adRequest)
            }
        )

    }
}