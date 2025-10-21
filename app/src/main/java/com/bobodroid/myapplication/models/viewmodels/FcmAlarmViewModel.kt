package com.bobodroid.myapplication.models.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bobodroid.myapplication.MainActivity.Companion.TAG
import com.bobodroid.myapplication.billing.BillingClientLifecycle.Companion.TAG
import com.bobodroid.myapplication.models.datamodels.repository.LatestRateRepository
import com.bobodroid.myapplication.models.datamodels.repository.UserRepository
import com.bobodroid.myapplication.models.datamodels.roomDb.CurrencyType
import com.bobodroid.myapplication.models.datamodels.roomDb.ExchangeRate
import com.bobodroid.myapplication.models.datamodels.roomDb.RateDirection
import com.bobodroid.myapplication.models.datamodels.roomDb.RateType
import com.bobodroid.myapplication.models.datamodels.roomDb.TargetRates
import com.bobodroid.myapplication.models.datamodels.service.UserApi.Rate
import com.bobodroid.myapplication.models.datamodels.useCases.TargetRateUseCases
import com.bobodroid.myapplication.models.datamodels.useCases.UserUseCases
import com.bobodroid.myapplication.models.datamodels.websocket.WebSocketClient
import com.bobodroid.myapplication.models.repository.SettingsRepository
import com.bobodroid.myapplication.util.result.onError
import com.bobodroid.myapplication.util.result.onSuccess
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FcmAlarmViewModel@Inject constructor(
   private val userRepository: UserRepository,
   private val fcmUseCases: TargetRateUseCases,
    private val latestRateRepository: LatestRateRepository,
   private val settingsRepository: SettingsRepository
): ViewModel()  {

    val selectedCurrency = settingsRepository.selectedCurrency.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = CurrencyType.USD
    )


    val isPremium = userRepository.userData
        .map { it?.localUserData?.isPremium ?: false }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )


    fun updateCurrentForeignCurrency(currency: CurrencyType): Boolean {
        return settingsRepository.setSelectedCurrency(currency)
    }




    private val _alarmUiState = MutableStateFlow(AlarmUiState())
    val alarmUiState = _alarmUiState.asStateFlow()

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

    private val deviceId = MutableStateFlow("")


    init {
        // receivedLatestRate는 별도 코루틴
        viewModelScope.launch {
            receivedLatestRate()
        }

        // initAlarmData는 별도 코루틴
        viewModelScope.launch {
            initAlarmData()
        }

    }



    fun initAlarmData() {
        viewModelScope.launch {
            // 유저 데이터가 준비될 때까지 대기
            val userData = userRepository.waitForUserData()

            if(userData.exchangeRates != null) {
                initTarRates(userData.exchangeRates)
                deviceId.emit(userData.localUserData.id.toString())
            }

            // 이후 초기화 작업 진행
            Log.d(TAG("FcmAlarmViewModel", "init") , "${userData}")


            fcmUseCases.targetRateUpdateUseCase(
                onUpdate = {
                    viewModelScope.launch {
                        _targetRate.emit(it)
                    }
                }
            )
        }
    }

    private suspend fun receivedLatestRate() {
        latestRateRepository.latestRateFlow.collect { latestRate ->
            val uiState = _alarmUiState.value.copy(
                recentRate = latestRate
            )
            _alarmUiState.emit(uiState)
        }
    }


    fun initTarRates(targetRates: TargetRates) {
        _targetRate.value = targetRates
    }



    fun addTargetRate(
        addRate: Rate,
        type: RateType) {
        viewModelScope.launch {
            fcmUseCases.targetRateAddUseCase(
                deviceId = deviceId.value,
                targetRates = targetRateFlow.value,
                type = type,
                newRate = addRate
            ).onSuccess { targetRate, _ ->
                Log.d(TAG("FcmAlarmViewModel", "Success"), "Result: $targetRate")
                _targetRate.emit(targetRate)
                Log.d(TAG("FcmAlarmViewModel", "Emit"), "Emitted to _targetRate")
            }.onError { error ->
                Log.e(TAG("FcmAlarmViewModel", "Error"), "Error occurred", error.exception)
            }
        }
    }

    fun deleteTargetRate(
        deleteRate: Rate,
        type: RateType) {
        viewModelScope.launch {
            fcmUseCases.targetRateDeleteUseCase(
                deviceId = deviceId.value,
                targetRates = targetRateFlow.value,
                type = type,
                deleteRate = deleteRate
            ).onSuccess { updateTargetRate, _  ->
                Log.d(TAG("FcmAlarmViewModel", "Success"), "Result: $updateTargetRate")
                _targetRate.emit(updateTargetRate)
                Log.d(TAG("FcmAlarmViewModel", "Emit"), "Emitted to _targetRate")
            }.onError { error ->
                Log.e(TAG("FcmAlarmViewModel", "Error"), "Error occurred", error.exception)
            }
        }
    }


}

data class AlarmUiState(
    val recentRate: ExchangeRate = ExchangeRate()
)