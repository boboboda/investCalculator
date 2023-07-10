package com.bobodroid.myapplication

import android.app.Application
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import dagger.hilt.android.HiltAndroidApp


@HiltAndroidApp
class InvestApplication: Application() {
    override fun onCreate() {
        MobileAds.initialize(this)
        super.onCreate()

    }
}