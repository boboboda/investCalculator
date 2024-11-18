package com.bobodroid.myapplication.util

import android.app.Application
import com.bobodroid.myapplication.billing.BillingClientLifecycle
import com.bobodroid.myapplication.models.datamodels.repository.UserRepository
import com.bobodroid.myapplication.models.datamodels.useCases.LocalExistCheckUseCase
import com.bobodroid.myapplication.models.datamodels.websocket.WebSocketClient
import com.google.android.gms.ads.MobileAds
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltAndroidApp
class InvestApplication: Application() {



    @Inject lateinit var localExistCheckUseCase: LocalExistCheckUseCase

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

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

        initializeApp()

        instance = this
        this.billingClientLifecycle = BillingClientLifecycle.getInstance(this)

    }

    private fun initializeApp() {
        applicationScope.launch {
            // 로컬 유저 체크 및 데이터 초기화
            localExistCheckUseCase()
        }
    }
}