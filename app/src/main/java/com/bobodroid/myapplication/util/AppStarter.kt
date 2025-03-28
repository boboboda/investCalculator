package com.bobodroid.myapplication.util

import android.content.Context
import com.bobodroid.myapplication.models.datamodels.repository.LatestRateRepository
import com.bobodroid.myapplication.models.datamodels.repository.NoticeRepository
import com.bobodroid.myapplication.models.datamodels.repository.UserRepository
import com.bobodroid.myapplication.models.datamodels.useCases.LocalExistCheckUseCase
import com.bobodroid.myapplication.util.AdMob.AdManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class AppStarter @Inject constructor(
    private val localExistCheckUseCase: LocalExistCheckUseCase,
    private val rateRepository: LatestRateRepository,
    private val noticeRepository: NoticeRepository,
    private val adManager: AdManager
) {
    fun startApp(context: Context) {
        CoroutineScope(Dispatchers.IO).launch {
            localExistCheckUseCase.invoke()
            rateRepository.subscribeToExchangeRateUpdates()
            noticeRepository.loadNotice()

            withContext(Dispatchers.Main) {
                adManager.loadBannerAd(context)
                adManager.loadRewardedAd(context)
            }
        }
    }
}