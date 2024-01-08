package com.bobodroid.myapplication.util

import android.app.Application
import com.google.android.gms.ads.MobileAds
import dagger.hilt.android.HiltAndroidApp


@HiltAndroidApp
class InvestApplication: Application() {

    companion object {
        lateinit var prefs: PreferenceUtil
    }
    override fun onCreate() {

        prefs = PreferenceUtil(applicationContext)
        MobileAds.initialize(this)
        super.onCreate()

    }
}