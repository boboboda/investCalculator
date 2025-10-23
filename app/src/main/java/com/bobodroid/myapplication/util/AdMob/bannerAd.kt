package com.bobodroid.myapplication.util.AdMob

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.bobodroid.myapplication.BuildConfig
import com.bobodroid.myapplication.MainActivity.Companion.TAG
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError

@SuppressLint("ResourceType")
@Composable
fun BannerAd() {
    var isAdLoaded by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .wrapContentSize()
            .padding(start = 10.dp, end = 10.dp)
    ) {
        val adRequest = AdRequest.Builder().build()

        AndroidView(
            modifier = Modifier.fillMaxWidth(),
            factory = { context ->
                AdView(context).apply {
                    setAdSize(AdSize.SMART_BANNER)
                    adUnitId = BuildConfig.BANNER_AD_KEY
                    loadAd(adRequest)
                    adListener = object : AdListener() {
                        override fun onAdClicked() {
                            // Code to be executed when the user clicks on an ad.
                        }

                        override fun onAdClosed() {
                            // Code to be executed when the user is about to return
                            // to the app after tapping on an ad.
                        }

                        override fun onAdFailedToLoad(adError: LoadAdError) {
                            // Code to be executed when an ad request fails.
                            Log.d(TAG("bannerAd",""), "배너 광고 로드 실패 ${adError}")
                            isAdLoaded = false
                        }

                        override fun onAdImpression() {
                            // Code to be executed when an impression is recorded
                            // for an ad.
                        }

                        override fun onAdLoaded() {
                            // Code to be executed when an ad finishes loading.
                            isAdLoaded = true
                        }

                        override fun onAdOpened() {
                            // Code to be executed when an ad opens an overlay that
                            // covers the screen.
                        }
                    }
                }
            },
            update = { adView ->
                if (!isAdLoaded) {
                    adView.loadAd(adRequest)
                }
            }
        )
        if (!isAdLoaded) {
            Column(modifier = Modifier
                .height(50.dp).fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center) {
                Text(text = "광고가 준비중입니다")
            }
        }
    }
}

