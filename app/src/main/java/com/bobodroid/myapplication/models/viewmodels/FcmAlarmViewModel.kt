// app/src/main/java/com/bobodroid/myapplication/models/viewmodels/FcmAlarmViewModel.kt

package com.bobodroid.myapplication.models.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bobodroid.myapplication.MainActivity.Companion.TAG
import com.bobodroid.myapplication.data.local.entity.CurrencyRecordDto
import com.bobodroid.myapplication.domain.entity.ExchangeRateEntity
import com.bobodroid.myapplication.domain.entity.RecordAlertEntity
import com.bobodroid.myapplication.domain.repository.IRecordRepository
import com.bobodroid.myapplication.domain.repository.IUserRepository
import com.bobodroid.myapplication.domain.usecase.notification.CalculateRecordAgeUseCase
import com.bobodroid.myapplication.domain.usecase.notification.ValidateAlertSettingsUseCase
import com.bobodroid.myapplication.models.datamodels.repository.LatestRateRepository
import com.bobodroid.myapplication.models.datamodels.roomDb.*
import com.bobodroid.myapplication.models.datamodels.service.BackupApi.BackupApi
import com.bobodroid.myapplication.models.datamodels.service.BackupApi.CreateBackupDto
import com.bobodroid.myapplication.models.datamodels.service.BackupApi.CurrencyRecordBackUpDto
import com.bobodroid.myapplication.models.datamodels.service.UserApi.Rate
import com.bobodroid.myapplication.models.datamodels.service.notificationApi.BatchUpdateRecordAlertsRequest
import com.bobodroid.myapplication.models.datamodels.service.notificationApi.ChannelSettings
import com.bobodroid.myapplication.models.datamodels.service.notificationApi.NotificationApi
import com.bobodroid.myapplication.models.datamodels.service.notificationApi.NotificationConditions
import com.bobodroid.myapplication.models.datamodels.service.notificationApi.NotificationHistoryItem
import com.bobodroid.myapplication.models.datamodels.service.notificationApi.NotificationSettings
import com.bobodroid.myapplication.models.datamodels.service.notificationApi.NotificationStats
import com.bobodroid.myapplication.models.datamodels.service.notificationApi.QuietHours
import com.bobodroid.myapplication.models.datamodels.service.notificationApi.RecordProfitAlert
import com.bobodroid.myapplication.models.datamodels.service.notificationApi.RecordWithAlert
import com.bobodroid.myapplication.models.datamodels.service.notificationApi.UpdateNotificationSettingsRequest
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
    private val userRepository: IUserRepository,
    private val fcmUseCases: FcmUseCases,
    private val latestRateRepository: LatestRateRepository,
    private val settingsRepository: SettingsRepository,
    private val iRecordRepository: IRecordRepository,
    private val validateAlertSettingsUseCase: ValidateAlertSettingsUseCase,
    private val calculateRecordAgeUseCase: CalculateRecordAgeUseCase
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

    // ==================== 목표환율 State (12개 통화 지원) ====================

    private val _targetRate = MutableStateFlow(TargetRates.empty())
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

    // ==================== 🆕 수익률 알림 State ====================

    private val _recordsWithAlerts = MutableStateFlow<List<RecordWithAlert>>(emptyList())
    val recordsWithAlerts: StateFlow<List<RecordWithAlert>> = _recordsWithAlerts.asStateFlow()

    private val _profitAlertLoading = MutableStateFlow(false)
    val profitAlertLoading: StateFlow<Boolean> = _profitAlertLoading.asStateFlow()

    private val _profitAlertMessage = MutableStateFlow<String?>(null)
    val profitAlertMessage: StateFlow<String?> = _profitAlertMessage.asStateFlow()

    private val _saveSuccess = MutableStateFlow(false)
    val saveSuccess: StateFlow<Boolean> = _saveSuccess.asStateFlow()

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

                    // ✅ 목표환율 초기화 (12개 통화 지원)
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
                        loadRecordsWithAlerts()

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
        Log.d(TAG("FcmAlarmViewModel", "initTarRates"),
            "목표환율 초기화 완료 - 통화 ${targetRates.getAllCurrencies().size}개, 총 ${targetRates.getTotalCount()}개 목표환율")
    }

    fun updateCurrentForeignCurrency(currency: CurrencyType): Boolean {
        return settingsRepository.setSelectedCurrency(currency)
    }

    // ==================== 목표환율 관리 (12개 통화 지원) ====================

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
                Log.d(TAG("FcmAlarmViewModel", "addTargetRate"),
                    "Success: ${type.currency.koreanName} ${type.direction} - ${addRate.rate}")
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
                Log.d(TAG("FcmAlarmViewModel", "deleteTargetRate"),
                    "Success: ${type.currency.koreanName} ${type.direction} - ${deleteRate.rate}")
                _targetRate.emit(updateTargetRate)
            }.onError { error ->
                Log.e(TAG("FcmAlarmViewModel", "deleteTargetRate"), "Error", error.exception)
                _error.value = error.message
            }
        }
    }

    fun getTargetRates(currency: CurrencyType, direction: RateDirection): List<Rate> {
        return targetRateFlow.value.getRates(currency, direction)
    }

    fun hasCurrencyTargetRates(currency: CurrencyType): Boolean {
        return targetRateFlow.value.hasCurrency(currency)
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
            fcmUseCases.updateNotificationSettingsUseCase(deviceId.value, request)
                .onSuccess { settings, _ ->
                    Log.d(TAG("FcmAlarmViewModel", "updateSettings"), "Success")
                    _notificationSettings.value = settings
                }
                .onError { error ->
                    Log.e(TAG("FcmAlarmViewModel", "updateSettings"), "Error", error.exception)
                    _error.value = "설정 업데이트 실패"
                }
        }
    }

    // ==================== 알림 히스토리 관리 ====================

    fun loadNotificationHistory() {
        viewModelScope.launch {
            fcmUseCases.getNotificationHistoryUseCase(deviceId.value)
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

    // ==================== 🆕 수익률 알림 관리 ====================

    // app/src/main/java/com/bobodroid/myapplication/models/viewmodels/FcmAlarmViewModel.kt

    fun loadRecordsWithAlerts() {
        viewModelScope.launch {
            try {
                _profitAlertLoading.value = true

                val unsoldRecords = iRecordRepository.getUnsoldRecords().first()

                Log.d(TAG("FcmAlarmViewModel", "loadRecordsWithAlerts"), "보유중 기록: ${unsoldRecords.size}개")

                if (deviceId.value.isEmpty()) {
                    _profitAlertMessage.value = "사용자 정보를 불러오는 중입니다"
                    return@launch
                }

                val settingsResponse = NotificationApi.service.getNotificationSettings(deviceId.value)

                val recordsWithAlerts = unsoldRecords.map { record ->
                    val existingAlert = settingsResponse.data?.conditions?.recordProfitAlerts
                        ?.find { it.recordId == record.id.toString() }

                    RecordWithAlert(
                        recordId = record.id.toString(),
                        currencyCode = record.currencyCode,
                        categoryName = record.categoryName ?: "",
                        date = record.date ?: "",
                        money = record.money ?: "0",
                        exchangeMoney = record.exchangeMoney ?: "0",
                        buyRate = record.buyRate ?: "0",
                        profitPercent = existingAlert?.alertPercent,  // 기존 설정값 또는 null// 기존 설정 있으면 true
                    )
                }

                _recordsWithAlerts.value = recordsWithAlerts
                Log.d(TAG("FcmAlarmViewModel", "loadRecordsWithAlerts"), "설정 완료: ${recordsWithAlerts.size}개")

            } catch (e: Exception) {
                Log.e(TAG("FcmAlarmViewModel", "loadRecordsWithAlerts"), "로드 실패", e)
                _profitAlertMessage.value = "기록을 불러오는데 실패했습니다: ${e.message}"
            } finally {
                _profitAlertLoading.value = false
            }
        }
    }

    // 알림 토글 함수 추가
    fun toggleRecordAlert(recordId: String, enabled: Boolean) {
        _recordsWithAlerts.value = _recordsWithAlerts.value.map { record ->
            if (record.recordId == recordId) {
                record.copy(
                    enabled = enabled,
                    profitPercent = if (enabled && record.profitPercent == null) 0.4f else record.profitPercent
                )
            } else {
                record
            }
        }
    }

    fun updateRecordProfitPercent(recordId: String, percent: Float) {
        _recordsWithAlerts.value = _recordsWithAlerts.value.map { record ->
            if (record.recordId == recordId) {
                record.copy(profitPercent = percent)
            } else {
                record
            }
        }
    }

    fun saveRecordAlerts() {
        viewModelScope.launch {
            try {
                _profitAlertLoading.value = true
                _saveSuccess.value = false

                if (deviceId.value.isEmpty()) {
                    _profitAlertMessage.value = "사용자 정보가 없습니다"
                    return@launch
                }

                // ⭐ 1. UseCase로 검증
                val recordAlerts = _recordsWithAlerts.value.map { record ->
                    RecordAlertEntity(
                        recordId = record.recordId,
                        currencyCode = record.currencyCode,
                        categoryName = record.categoryName,
                        date = record.date,
                        money = record.money,
                        exchangeMoney = record.exchangeMoney,
                        buyRate = record.buyRate,
                        profitPercent = record.profitPercent,
                        enabled = record.enabled
                    )
                }

                val validation = validateAlertSettingsUseCase.validateRecordAlerts(recordAlerts)

                if (!validation.isValid) {
                    _profitAlertMessage.value = validation.errorMessage ?: "설정이 올바르지 않습니다"
                    return@launch
                }

                // ⭐ 2. 백업 (기존 로직)
                Log.d(TAG("FcmAlarmViewModel", "saveRecordAlerts"), "1단계: 백업 시작")
                val backupSuccess = triggerBackup(deviceId.value)

                if (!backupSuccess) {
                    _profitAlertMessage.value = "백업에 실패했습니다"
                    return@launch
                }

                Log.d(TAG("FcmAlarmViewModel", "saveRecordAlerts"), "2단계: 알림 설정 저장 시작")

                // ⭐ 3. 활성화된 알림만 서버로 전송
                val enabledRecords = _recordsWithAlerts.value.filter {
                    it.enabled && it.profitPercent != null
                }

                val recordAlertDtos = enabledRecords.map { record ->
                    RecordProfitAlert(
                        recordId = record.recordId,
                        alertPercent = record.profitPercent!!.toFloat()
                    )
                }

                // ⭐ 4. 서버로 전송
                val request = BatchUpdateRecordAlertsRequest(
                    recordProfitAlerts = recordAlertDtos
                )

                val response = NotificationApi.service.batchUpdateRecordAlerts(
                    deviceId = deviceId.value,
                    request = request
                )

                if (response.success) {
                    _saveSuccess.value = true
                    _profitAlertMessage.value = "알림 설정이 저장되었습니다"
                    Log.d(TAG("FcmAlarmViewModel", "saveRecordAlerts"), "저장 완료: ${enabledRecords.size}개")
                } else {
                    _profitAlertMessage.value = response.message ?: "저장 실패"
                }

            } catch (e: Exception) {
                Log.e(TAG("FcmAlarmViewModel", "saveRecordAlerts"), "저장 실패", e)
                _profitAlertMessage.value = "저장에 실패했습니다: ${e.message}"
            } finally {
                _profitAlertLoading.value = false
            }
        }
    }

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━
    // ✅ 새로운 함수: 경과 일수 표시
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━
    fun getRecordAge(buyDate: String): String {
        val days = calculateRecordAgeUseCase.execute(buyDate)
        return calculateRecordAgeUseCase.formatAge(days)
    }

    private suspend fun triggerBackup(deviceId: String): Boolean {
        return try {
            val currentUserData = userRepository.userData.filterNotNull().first()
            val localUser = currentUserData.localUserData ?: run {
                Log.e(TAG("FcmAlarmViewModel", "triggerBackup"), "사용자 정보 없음")
                return false
            }

            val allRecords = iRecordRepository.getAllRecords().first()

            Log.d(TAG("FcmAlarmViewModel", "triggerBackup"), "백업 대상 기록: ${allRecords.size}개")

            val currencyRecords = allRecords.map { record ->
                CurrencyRecordBackUpDto(
                    id = record.id.toString(),
                    currencyCode = record.currencyCode,
                    date = record.date ?: "",
                    money = record.money ?: "0",
                    rate = record.rate ?: "0",
                    buyRate = record.buyRate ?: "0",
                    exchangeMoney = record.exchangeMoney ?: "0",
                    profit = record.profit ?: "0",
                    expectProfit = record.expectProfit ?: "0",
                    categoryName = record.categoryName ?: "",
                    memo = record.memo ?: "",
                    sellRate = record.sellRate,
                    sellProfit = record.sellProfit,
                    sellDate = record.sellDate,
                    recordColor = record.recordColor ?: false
                )
            }

            val backupDto = CreateBackupDto(
                deviceId = deviceId,
                socialId = localUser.socialId,
                socialType = localUser.socialType?.name,
                currencyRecords = currencyRecords
            )

            val backupResponse = BackupApi.backupService.createBackupWithDto(backupDto)

            if (backupResponse.success) {
                Log.d(TAG("FcmAlarmViewModel", "triggerBackup"), "백업 성공: ${allRecords.size}개")
                true
            } else {
                Log.e(TAG("FcmAlarmViewModel", "triggerBackup"), "백업 실패: ${backupResponse.message}")
                false
            }

        } catch (e: Exception) {
            Log.e(TAG("FcmAlarmViewModel", "triggerBackup"), "백업 에러", e)
            false
        }
    }

    fun clearProfitAlertMessage() {
        _profitAlertMessage.value = null
    }

    fun refreshRecordAlerts() {
        loadRecordsWithAlerts()
    }

    // ==================== 에러 처리 ====================

    fun clearError() {
        _error.value = null
    }


    // ==================== 알림 히스토리 관리 - 삭제 기능 추가 ====================

    /**
     * 개별 알림 삭제
     */
    fun deleteNotification(notificationId: String) {
        viewModelScope.launch {
            fcmUseCases.deleteNotificationUseCase(notificationId)
                .onSuccess { _, message ->
                    Log.d(TAG("FcmAlarmViewModel", "deleteNotification"), "Success: $message")

                    // 로컬 리스트에서 제거
                    _notificationHistory.value = _notificationHistory.value.filter {
                        it.id != notificationId
                    }
                }
                .onError { error ->
                    Log.e(TAG("FcmAlarmViewModel", "deleteNotification"), "Error", error.exception)
                    _error.value = "알림 삭제 실패"
                }
        }
    }

    /**
     * 모든 알림 삭제
     */
    fun deleteAllNotifications() {
        viewModelScope.launch {
            fcmUseCases.deleteAllNotificationsUseCase(deviceId.value)
                .onSuccess { count, message ->
                    Log.d(TAG("FcmAlarmViewModel", "deleteAll"), "Success: $count 개 삭제")

                    // 로컬 리스트 비우기
                    _notificationHistory.value = emptyList()

                    // 통계 새로고침
                    loadNotificationStats()
                }
                .onError { error ->
                    Log.e(TAG("FcmAlarmViewModel", "deleteAll"), "Error", error.exception)
                    _error.value = "전체 삭제 실패"
                }
        }
    }

    /**
     * 읽은 알림만 삭제
     */
    fun deleteReadNotifications() {
        viewModelScope.launch {
            fcmUseCases.deleteReadNotificationsUseCase(deviceId.value)
                .onSuccess { count, message ->
                    Log.d(TAG("FcmAlarmViewModel", "deleteRead"), "Success: $count 개 삭제")

                    // 로컬 리스트에서 읽은 알림 제거
                    _notificationHistory.value = _notificationHistory.value.filter {
                        it.status == "sent" // 읽지 않은 것만 남김
                    }

                    // 통계 새로고침
                    loadNotificationStats()
                }
                .onError { error ->
                    Log.e(TAG("FcmAlarmViewModel", "deleteRead"), "Error", error.exception)
                    _error.value = "읽은 알림 삭제 실패"
                }
        }
    }

    /**
     * 오래된 알림 삭제 (기본 30일)
     */
    fun deleteOldNotifications(days: Int = 30) {
        viewModelScope.launch {
            fcmUseCases.deleteOldNotificationsUseCase(deviceId.value, days)
                .onSuccess { count, message ->
                    Log.d(TAG("FcmAlarmViewModel", "deleteOld"), "Success: $count 개 삭제")

                    // 히스토리 새로고침
                    loadNotificationHistory()

                    // 통계 새로고침
                    loadNotificationStats()
                }
                .onError { error ->
                    Log.e(TAG("FcmAlarmViewModel", "deleteOld"), "Error", error.exception)
                    _error.value = "오래된 알림 삭제 실패"
                }
        }
    }
}

data class AlarmUiState(
    val recentRate: ExchangeRateEntity = ExchangeRateEntity()
)