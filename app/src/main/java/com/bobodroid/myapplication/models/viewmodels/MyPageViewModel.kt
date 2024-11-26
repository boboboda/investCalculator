package com.bobodroid.myapplication.models.viewmodels

import androidx.lifecycle.ViewModel
import com.bobodroid.myapplication.models.datamodels.repository.LatestRateRepository
import com.bobodroid.myapplication.models.datamodels.repository.UserRepository
import com.bobodroid.myapplication.models.datamodels.useCases.UserUseCases
import javax.inject.Inject

class MyPageViewModel @Inject constructor(
    private val userUseCases: UserUseCases,
    private val userRepository: UserRepository,
    private val latestRateRepository: LatestRateRepository
) : ViewModel() {

}

