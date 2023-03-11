package com.bobodroid.myapplication.components

import android.annotation.SuppressLint
import android.media.tv.AdRequest
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.bobodroid.myapplication.R
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView

@SuppressLint("ResourceType")
@Composable
fun BannerAd() {

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 10.dp, end = 10.dp)
    ) {
//        val adId = stringResource(id = R.string.admob_sample_app_id)
        val adRequest = com.google.android.gms.ads.AdRequest.Builder().build()
        AndroidView(
            modifier = Modifier.fillMaxWidth(),
            factory = {context ->
                AdView(context).apply {
                    setAdSize(AdSize.SMART_BANNER)
                    adUnitId = "ca-app-pub-8596470561558049/7511395566"
                    loadAd(adRequest)

                }
            },
            update = {adView ->
                adView.loadAd(adRequest)
            }
        )

    }
}