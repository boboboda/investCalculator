package com.bobodroid.myapplication.util

import com.bobodroid.myapplication.models.datamodels.repository.LatestRateRepository
import com.bobodroid.myapplication.models.datamodels.repository.NoticeRepository
import com.bobodroid.myapplication.models.datamodels.repository.UserRepository
import com.bobodroid.myapplication.models.datamodels.useCases.LocalExistCheckUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

class AppStarter @Inject constructor(
    private val localExistCheckUseCase: LocalExistCheckUseCase,
    private val rateRepository: LatestRateRepository,
    private val noticeRepository: NoticeRepository
) {
    fun startApp() {
        CoroutineScope(Dispatchers.IO).launch {
            localExistCheckUseCase.invoke()
            rateRepository.subscribeToExchangeRateUpdates()
            noticeRepository.loadNotice()
        }
    }
}