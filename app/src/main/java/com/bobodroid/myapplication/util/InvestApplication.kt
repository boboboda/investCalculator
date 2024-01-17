package com.bobodroid.myapplication.util

import android.app.Application
import com.bobodroid.myapplication.billing.BillingClientLifecycle
import com.google.android.gms.ads.MobileAds
import dagger.hilt.android.HiltAndroidApp


@HiltAndroidApp
class InvestApplication: Application() {

    companion object {
        lateinit var prefs: PreferenceUtil

        lateinit var instance: InvestApplication
            private set
    }

    lateinit var billingClientLifecycle: BillingClientLifecycle
        private set
    override fun onCreate() {

        prefs = PreferenceUtil(applicationContext)
        MobileAds.initialize(this)
        super.onCreate()

        instance = this
        this.billingClientLifecycle = BillingClientLifecycle.getInstance(this)

    }
}