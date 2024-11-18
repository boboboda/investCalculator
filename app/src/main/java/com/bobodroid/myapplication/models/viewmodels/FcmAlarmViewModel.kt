package com.bobodroid.myapplication.models.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bobodroid.myapplication.MainActivity.Companion.TAG
import com.bobodroid.myapplication.billing.BillingClientLifecycle.Companion.TAG
import com.bobodroid.myapplication.models.datamodels.repository.UserRepository
import com.bobodroid.myapplication.models.datamodels.roomDb.TargetRates
import com.bobodroid.myapplication.models.datamodels.service.UserApi.Rate
import com.bobodroid.myapplication.models.datamodels.service.UserApi.UserData
import com.bobodroid.myapplication.models.datamodels.useCases.TargetRateUseCases
import com.bobodroid.myapplication.models.datamodels.useCases.UserDataType
import com.bobodroid.myapplication.models.datamodels.useCases.UserUseCases
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FcmAlarmViewModel@Inject constructor(
   private val userRepository: UserRepository,
   private val fcmUseCases: TargetRateUseCases
): ViewModel()  {

    val sampleDollarHighRates = listOf(
        Rate(number = 1, rate = 1320),
        Rate(number = 2, rate = 1350),
        Rate(number = 3, rate = 1380),
        Rate(number = 4, rate = 1400),
        Rate(number = 5, rate = 1420)
    )

    val sampleDollarLowRates = listOf(
        Rate(number = 1, rate = 1250),
        Rate(number = 2, rate = 1270),
        Rate(number = 3, rate = 1290)
    )

    val sampleYenHighRates = listOf(
        Rate(number = 1, rate = 890),
        Rate(number = 2, rate = 920),
        Rate(number = 3, rate = 950),
        Rate(number = 4, rate = 980)
    )

    val sampleYenLowRates = listOf(
        Rate(number = 1, rate = 850),
        Rate(number = 2, rate = 870),
        Rate(number = 3, rate = 890)
    )

    private val _targetRate = MutableStateFlow(
        TargetRates(
            dollarHighRates = sampleDollarHighRates,
            dollarLowRates = sampleDollarLowRates,
            yenHighRates = sampleYenHighRates,
            yenLowRates = sampleYenLowRates
        )
    )

    val targetRateFlow = _targetRate.asStateFlow()


    init {
        viewModelScope.launch {
            // 유저 데이터가 준비될 때까지 대기
            val userData = userRepository.waitForUserData()

            if(userData.exchangeRates != null) {
                initTarRates(userData.exchangeRates)
            }

            // 이후 초기화 작업 진행
            Log.d(TAG("FcmAlarmViewModel", "init") , "${userData}")
        }
    }


    fun initTarRates(targetRates: TargetRates) {
        _targetRate.value = targetRates
    }


//    fun addTargetRate() {
//        viewModelScope.launch {
//            fcmUseCases.targetRateAddUseCase(
//
//            )
//        }
//
//    }


}