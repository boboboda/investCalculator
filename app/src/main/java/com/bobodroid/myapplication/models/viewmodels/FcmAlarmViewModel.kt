// app/src/main/java/com/bobodroid/myapplication/models/viewmodels/FcmAlarmViewModel.kt

package com.bobodroid.myapplication.models.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bobodroid.myapplication.MainActivity.Companion.TAG
import com.bobodroid.myapplication.models.datamodels.notification.*
import com.bobodroid.myapplication.models.datamodels.repository.LatestRateRepository
import com.bobodroid.myapplication.models.datamodels.repository.UserRepository
import com.bobodroid.myapplication.models.datamodels.roomDb.*
import com.bobodroid.myapplication.models.datamodels.service.UserApi.Rate
import com.bobodroid.myapplication.models.datamodels.useCases.FcmUseCases
import com.bobodroid.myapplication.models.repository.SettingsRepository
import com.bobodroid.myapplication.util.result.onError
import com.bobodroid.myapplication.util.result.onSuccess
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FcmAlarmViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val fcmUseCases: FcmUseCases,
    private val latestRateRepository: LatestRateRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    // ==================== 공통 State ====================

    private val deviceId = MutableStateFlow("")

    // ✅ 초기화 완료 여부 플래그 (중복 실행 방지)
    private var isAlarmDataInitialized = false

    val isPremium = userRepository.userData
        .map { it?.localUserData?.isPremium ?: false }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )

    val selectedCurrency = settingsRepository.selectedCurrency.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = CurrencyType.USD
    )

    // ==================== 목표환율 State ====================

    private val _targetRate = MutableStateFlow(
        TargetRates(
            dollarHighRates = emptyList(),
            dollarLowRates = emptyList(),
            yenHighRates = emptyList(),
            yenLowRates = emptyList()
        )
    )
    val targetRateFlow = _targetRate.asStateFlow()

    // ==================== 알림 설정 State ====================

    private val _notificationSettings = MutableStateFlow<NotificationSettings?>(null)
    val notificationSettings = _notificationSettings.asStateFlow()

    // ==================== 알림 히스토리 State ====================

    private val _notificationHistory = MutableStateFlow<List<NotificationHistoryItem>>(emptyList())
    val notificationHistory = _notificationHistory.asStateFlow()

    // ==================== 알림 통계 State ====================

    private val _notificationStats = MutableStateFlow<NotificationStats?>(null)
    val notificationStats = _notificationStats.asStateFlow()

    // ==================== UI State ====================

    private val _alarmUiState = MutableStateFlow(AlarmUiState())
    val alarmUiState = _alarmUiState.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    // ==================== 초기화 ====================

    init {
        // ✅ 최신 환율 구독
        viewModelScope.launch {
            receivedLatestRate()
        }

        // ✅ userData Flow 구독 (자동 초기화)
        viewModelScope.launch {
            userRepository.userData
                .filterNotNull()
                .collect { userData ->
                    Log.d(TAG("FcmAlarmViewModel", "init"), "UserData 수신: ${userData.localUserData.id}")

                    // deviceId 설정
                    deviceId.emit(userData.localUserData.id.toString())

                    // 목표환율 초기화
                    userData.exchangeRates?.let {
                        initTarRates(it)
                    }

                    // ✅ 한 번만 실행 (중복 방지)
                    if (!isAlarmDataInitialized && deviceId.value.isNotEmpty()) {
                        isAlarmDataInitialized = true

                        Log.d(TAG("FcmAlarmViewModel", "init"), "알림 데이터 초기화 시작 (deviceId: ${deviceId.value})")

                        // 알림 설정/히스토리/통계 로드
                        loadNotificationSettings()
                        loadNotificationHistory()
                        loadNotificationStats()

                        // 목표환율 실시간 업데이트 구독
                        fcmUseCases.targetRateUpdateUseCase(
                            onUpdate = {
                                viewModelScope.launch {
                                    _targetRate.emit(it)
                                }
                            }
                        )
                    }
                }
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

    fun updateCurrentForeignCurrency(currency: CurrencyType): Boolean {
        return settingsRepository.setSelectedCurrency(currency)
    }

    // ==================== 목표환율 관리 ====================

    fun addTargetRate(
        addRate: Rate,
        type: RateType
    ) {
        viewModelScope.launch {
            fcmUseCases.targetRateAddUseCase(
                deviceId = deviceId.value,
                targetRates = targetRateFlow.value,
                type = type,
                newRate = addRate
            ).onSuccess { targetRate, _ ->
                Log.d(TAG("FcmAlarmViewModel", "addTargetRate"), "Success: $targetRate")
                _targetRate.emit(targetRate)
            }.onError { error ->
                Log.e(TAG("FcmAlarmViewModel", "addTargetRate"), "Error", error.exception)
                _error.value = error.message
            }
        }
    }

    fun deleteTargetRate(
        deleteRate: Rate,
        type: RateType
    ) {
        viewModelScope.launch {
            fcmUseCases.targetRateDeleteUseCase(
                deviceId = deviceId.value,
                targetRates = targetRateFlow.value,
                type = type,
                deleteRate = deleteRate
            ).onSuccess { updateTargetRate, _ ->
                Log.d(TAG("FcmAlarmViewModel", "deleteTargetRate"), "Success: $updateTargetRate")
                _targetRate.emit(updateTargetRate)
            }.onError { error ->
                Log.e(TAG("FcmAlarmViewModel", "deleteTargetRate"), "Error", error.exception)
                _error.value = error.message
            }
        }
    }

    // ==================== 알림 설정 관리 ====================

    fun loadNotificationSettings() {
        viewModelScope.launch {
            _isLoading.value = true
            fcmUseCases.getNotificationSettingsUseCase(deviceId.value)
                .onSuccess { settings, _ ->
                    Log.d(TAG("FcmAlarmViewModel", "loadSettings"), "Success")
                    _notificationSettings.value = settings
                }
                .onError { error ->
                    Log.e(TAG("FcmAlarmViewModel", "loadSettings"), "Error", error.exception)
                    _error.value = "설정을 불러올 수 없습니다"
                }
            _isLoading.value = false
        }
    }

    fun toggleGlobalNotification(enabled: Boolean) {
        updateSettings(
            UpdateNotificationSettingsRequest(globalEnabled = enabled)
        )
    }

    fun toggleRateAlert(enabled: Boolean) {
        val current = _notificationSettings.value?.rateAlert ?: ChannelSettings()
        updateSettings(
            UpdateNotificationSettingsRequest(
                rateAlert = current.copy(enabled = enabled)
            )
        )
    }

    fun toggleProfitAlert(enabled: Boolean) {
        if (!isPremium.value) {
            _error.value = "프리미엄 기능입니다"
            return
        }

        val current = _notificationSettings.value?.recordAlert ?: ChannelSettings()
        updateSettings(
            UpdateNotificationSettingsRequest(
                recordAlert = current.copy(enabled = enabled)
            )
        )
    }

    fun updateRecordAgeTime(time: String) {
        val current = _notificationSettings.value?.conditions ?: NotificationConditions()
        updateSettings(
            UpdateNotificationSettingsRequest(
                conditions = current.copy(
                    recordAgeAlert = current.recordAgeAlert.copy(alertTime = time)
                )
            )
        )
    }

    fun updateRecordAgeDays(days: Int) {
        val current = _notificationSettings.value?.conditions ?: NotificationConditions()
        updateSettings(
            UpdateNotificationSettingsRequest(
                conditions = current.copy(
                    recordAgeAlert = current.recordAgeAlert.copy(alertDays = days)
                )
            )
        )
    }

    fun updateDailySummaryTime(time: String) {
        val current = _notificationSettings.value?.conditions ?: NotificationConditions()
        updateSettings(
            UpdateNotificationSettingsRequest(
                conditions = current.copy(
                    dailySummary = current.dailySummary.copy(summaryTime = time)
                )
            )
        )
    }

    fun updateQuietHours(quietHours: QuietHours) {
        updateSettings(
            UpdateNotificationSettingsRequest(quietHours = quietHours)
        )
    }

    fun updateMinProfitPercent(percent: Double) {
        val current = _notificationSettings.value?.conditions ?: NotificationConditions()
        updateSettings(
            UpdateNotificationSettingsRequest(
                conditions = current.copy(minProfitPercent = percent)
            )
        )
    }

    private fun updateSettings(request: UpdateNotificationSettingsRequest) {
        viewModelScope.launch {
            _isLoading.value = true
            fcmUseCases.updateNotificationSettingsUseCase(deviceId.value, request)
                .onSuccess { settings, _ ->
                    Log.d(TAG("FcmAlarmViewModel", "updateSettings"), "Success")
                    _notificationSettings.value = settings
                }
                .onError { error ->
                    Log.e(TAG("FcmAlarmViewModel", "updateSettings"), "Error", error.exception)
                    _error.value = "설정 업데이트 실패"
                }
            _isLoading.value = false
        }
    }

    // ==================== 알림 히스토리 관리 ====================

    fun loadNotificationHistory(limit: Int = 50) {
        viewModelScope.launch {
            fcmUseCases.getNotificationHistoryUseCase(deviceId.value, limit)
                .onSuccess { history, _ ->
                    Log.d(TAG("FcmAlarmViewModel", "loadHistory"), "Success: ${history.size}개")
                    _notificationHistory.value = history
                }
                .onError { error ->
                    Log.e(TAG("FcmAlarmViewModel", "loadHistory"), "Error", error.exception)
                }
        }
    }

    fun markAsRead(notificationId: String) {
        viewModelScope.launch {
            fcmUseCases.markAsReadUseCase(notificationId)
                .onSuccess { _, _ ->
                    Log.d(TAG("FcmAlarmViewModel", "markAsRead"), "Success")
                    // 로컬 상태 업데이트
                    _notificationHistory.value = _notificationHistory.value.map {
                        if (it.id == notificationId) {
                            it.copy(status = "READ")
                        } else it
                    }
                }
                .onError { error ->
                    Log.e(TAG("FcmAlarmViewModel", "markAsRead"), "Error", error.exception)
                }
        }
    }

    // ==================== 알림 통계 관리 ====================

    fun loadNotificationStats() {
        viewModelScope.launch {
            fcmUseCases.getNotificationStatsUseCase(deviceId.value)
                .onSuccess { stats, _ ->
                    Log.d(TAG("FcmAlarmViewModel", "loadStats"), "Success")
                    _notificationStats.value = stats
                }
                .onError { error ->
                    Log.e(TAG("FcmAlarmViewModel", "loadStats"), "Error", error.exception)
                }
        }
    }

    // ==================== 테스트 ====================

    fun sendTestNotification() {
        viewModelScope.launch {
            fcmUseCases.sendTestNotificationUseCase(deviceId.value)
                .onSuccess { _, _ ->
                    Log.d(TAG("FcmAlarmViewModel", "sendTest"), "Success")
                }
                .onError { error ->
                    Log.e(TAG("FcmAlarmViewModel", "sendTest"), "Error", error.exception)
                    _error.value = "테스트 알림 전송 실패"
                }
        }
    }

    // ==================== 에러 처리 ====================

    fun clearError() {
        _error.value = null
    }
}

data class AlarmUiState(
    val recentRate: ExchangeRate = ExchangeRate()
)