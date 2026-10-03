// app/src/main/java/com/bobodroid/myapplication/models/viewmodels/FcmAlarmViewModel.kt

package com.bobodroid.myapplication.models.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bobodroid.myapplication.MainActivity.Companion.TAG
import com.bobodroid.myapplication.models.datamodels.repository.InvestRepository
import com.bobodroid.myapplication.models.datamodels.repository.LatestRateRepository
import com.bobodroid.myapplication.models.datamodels.repository.SettingsRepository
import com.bobodroid.myapplication.models.datamodels.repository.UserRepository
import com.bobodroid.myapplication.models.datamodels.roomDb.*
import com.bobodroid.myapplication.models.datamodels.service.BackupApi.BackupOutcome
import com.bobodroid.myapplication.models.datamodels.service.BackupApi.BackupSyncManager
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
import com.bobodroid.myapplication.models.datamodels.useCases.TargetRateRemoveByRecordUseCase
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
    private val targetRateRemoveByRecordUseCase: TargetRateRemoveByRecordUseCase,
    private val latestRateRepository: LatestRateRepository,
    private val settingsRepository: SettingsRepository,
    private val investRepository: InvestRepository,
    private val backupSyncManager: BackupSyncManager
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

    // ==================== 스프레드 반영 기준 환율 ====================
    // ⚠️ 반드시 _alarmUiState 선언보다 아래에 둘 것 (초기화 순서)

    private val spreadRefresh = MutableStateFlow(0)

    val spreadRateInfo: StateFlow<SpreadRateInfo> = combine(
        selectedCurrency,
        _alarmUiState,
        spreadRefresh
    ) { currency, uiState, _ ->
        val midRate = uiState.recentRate
            .getRateByCode(currency.code)
            ?.replace(",", "")
            ?.toDoubleOrNull()

        val buy = settingsRepository.getBuySpreadWon(currency)
        val sell = settingsRepository.getSellSpreadWon(currency)

        SpreadRateInfo(
            buySpreadWon = buy,
            sellSpreadWon = sell,
            buyBasisRate = midRate?.plus(buy),
            sellBasisRate = midRate?.minus(sell)
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SpreadRateInfo()
    )

    // 마이페이지에서 스프레드를 바꾸고 돌아왔을 때 다시 읽기
    fun refreshSpread() {
        spreadRefresh.value += 1
    }

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

    // ==================== 기록 카드 → 알람 등록 ====================

    private val _alarmToast = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val alarmToast: SharedFlow<String> = _alarmToast.asSharedFlow()

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
                                    publishTargetRates(it)
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

    /**
     * 목표환율 변경을 모든 화면에 반영한다.
     * - 메인 화면과 알람 화면은 각자 다른 ViewModel 인스턴스를 쓰므로,
     *   공용 저장소(UserRepository)에도 같이 써야 서로 갱신된다.
     * - 각 ViewModel의 init 이 userData 를 구독하고 있어 initTarRates 로 자동 반영된다.
     */
    private suspend fun publishTargetRates(targetRates: TargetRates) {
        _targetRate.emit(targetRates)
        userRepository.userData.value?.let { current ->
            userRepository.updateUserData(current.copy(exchangeRates = targetRates))
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
                publishTargetRates(targetRate)
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
                publishTargetRates(updateTargetRate)
            }.onError { error ->
                Log.e(TAG("FcmAlarmViewModel", "deleteTargetRate"), "Error", error.exception)
                _error.value = error.message
            }
        }
    }

    /** 통화별 현재 기준환율 (스프레드 적용 전) */
    fun midRateOf(currency: CurrencyType): Double? =
        _alarmUiState.value.recentRate
            .getRateByCode(currency.code)
            ?.replace(",", "")
            ?.toDoubleOrNull()

    /** 예상수익 계산용 매도 스프레드(원) */
    fun sellSpreadWonOf(currency: CurrencyType): Double =
        settingsRepository.getSellSpreadWon(currency)

    /**
     * 기록 카드에서 기록 알람 등록
     * - 알람에 recordId를 같이 저장해서 그 기록의 카드에만 표시된다.
     * - 같은 기록에 같은 통화·방향·값이 이미 있으면 등록하지 않는다. (다른 기록이면 같은 값도 허용)
     * - 알람 화면의 목록에도 그대로 나타난다. (같은 targetRates 목록)
     */
    fun addTargetRateFromRecord(type: RateType, rateWon: Int, recordId: String) {
        if (deviceId.value.isEmpty()) {
            _alarmToast.tryEmit("사용자 정보를 불러오는 중이에요. 잠시 후 다시 시도해 주세요")
            return
        }

        val exists = targetRateFlow.value
            .getRecordRates(type.currency, type.direction, recordId)
            .any { it.rate == rateWon }
        if (exists) {
            _alarmToast.tryEmit("이 기록에 이미 같은 ${type.toDisplayString()} 알람이 있어요")
            return
        }

        viewModelScope.launch {
            fcmUseCases.targetRateAddUseCase(
                deviceId = deviceId.value,
                targetRates = targetRateFlow.value,
                type = type,
                newRate = Rate(number = 0, rate = rateWon, recordId = recordId)
            ).onSuccess { targetRate, _ ->
                publishTargetRates(targetRate)
                _alarmToast.tryEmit("${type.toDisplayString()} 알람을 등록했어요 (${"%,d".format(rateWon)}원)")
            }.onError { error ->
                Log.e(TAG("FcmAlarmViewModel", "addTargetRateFromRecord"), "Error", error.exception)
                _alarmToast.tryEmit(error.message ?: "알람 등록에 실패했어요")
            }
        }
    }

    /**
     * 기록을 매도하거나 삭제할 때, 그 기록에 연결된 알람(고점·저점)을 모두 지운다.
     */
    fun removeAlarmsOfRecord(currency: CurrencyType, recordId: String) {
        if (deviceId.value.isEmpty()) return

        val hasAlarm = listOf(RateDirection.HIGH, RateDirection.LOW).any { direction ->
            targetRateFlow.value.getRecordRates(currency, direction, recordId).isNotEmpty()
        }
        if (!hasAlarm) return

        viewModelScope.launch {
            targetRateRemoveByRecordUseCase(
                deviceId = deviceId.value,
                targetRates = targetRateFlow.value,
                currency = currency,
                recordId = recordId
            ).onSuccess { updated, _ ->
                publishTargetRates(updated)
                _alarmToast.tryEmit("이 기록의 알람을 함께 삭제했어요")
            }.onError { error ->
                Log.e(TAG("FcmAlarmViewModel", "removeAlarmsOfRecord"), "Error", error.exception)
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

                val unsoldRecords = investRepository.getUnsoldRecords().first()

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

                Log.d(TAG("FcmAlarmViewModel", "saveRecordAlerts"), "1단계: 백업 시작")

                val backupSuccess = triggerBackup(deviceId.value)

                if (!backupSuccess) {
                    _profitAlertMessage.value = "백업에 실패했습니다. 알림 설정을 저장할 수 없습니다."
                    return@launch
                }

                Log.d(TAG("FcmAlarmViewModel", "saveRecordAlerts"), "백업 완료")
                Log.d(TAG("FcmAlarmViewModel", "saveRecordAlerts"), "2단계: 알림 설정 저장 시작")

                // ✅ enabled=true이고 profitPercent가 null이 아닌 것만 필터링
                val enabledRecords = _recordsWithAlerts.value.filter {
                    it.enabled && it.profitPercent != null
                }

                val recordAlerts = enabledRecords.map { record ->
                    RecordProfitAlert(
                        recordId = record.recordId,
                        alertPercent = record.profitPercent!!,
                        alerted = false,
                        lastAlertedAt = null
                    )
                }

                val request = BatchUpdateRecordAlertsRequest(
                    recordProfitAlerts = recordAlerts
                )

                val response = NotificationApi.service.batchUpdateRecordAlerts(
                    deviceId = deviceId.value,
                    request = request
                )

                if (response.success) {
                    _saveSuccess.value = true
                    _profitAlertMessage.value = "알림 설정이 저장되었습니다 (${enabledRecords.size}개)"
                    Log.d(TAG("FcmAlarmViewModel", "saveRecordAlerts"), "저장 성공: ${recordAlerts.size}개")
                } else {
                    _profitAlertMessage.value = "알림 설정 저장에 실패했습니다: ${response.message}"
                }

            } catch (e: Exception) {
                Log.e(TAG("FcmAlarmViewModel", "saveRecordAlerts"), "저장 실패", e)
                _profitAlertMessage.value = "저장 중 오류가 발생했습니다: ${e.message}"
            } finally {
                _profitAlertLoading.value = false
            }
        }
    }

    /**
     * 수익률 알림 저장 전에 서버 기록을 최신으로 맞춘다.
     * 전송은 BackupSyncManager를 거치므로 서버 기록을 덮어쓰는 위험이 없다.
     * (소셜 로그인 없이도 deviceId 기준으로 백업)
     */
    private suspend fun triggerBackup(deviceId: String): Boolean {
        return when (val outcome = backupSyncManager.backup(requireSocialLogin = false)) {
            is BackupOutcome.Success -> {
                Log.d(TAG("FcmAlarmViewModel", "triggerBackup"), "백업 성공: ${outcome.recordCount}개")
                true
            }

            // 기록이 없어 보낼 것이 없는 경우
            is BackupOutcome.Skipped -> {
                Log.d(TAG("FcmAlarmViewModel", "triggerBackup"), "백업 건너뜀: ${outcome.reason}")
                true
            }

            // 서버 기록과 달라 보류된 경우 (클라우드 백업 화면에서 복구/덮어쓰기 선택 필요)
            is BackupOutcome.Held -> {
                Log.w(TAG("FcmAlarmViewModel", "triggerBackup"), "백업 보류: 서버 기록 ${outcome.serverRecordCount}개")
                false
            }

            is BackupOutcome.Failed -> {
                Log.e(TAG("FcmAlarmViewModel", "triggerBackup"), "백업 실패: ${outcome.message}", outcome.exception)
                false
            }
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
    val recentRate: ExchangeRate = ExchangeRate()
)

data class SpreadRateInfo(
    val buySpreadWon: Double = 0.0,
    val sellSpreadWon: Double = 0.0,
    val buyBasisRate: Double? = null,   // 살 때 기준 = 현재 환율 + 매수 스프레드
    val sellBasisRate: Double? = null   // 팔 때 기준 = 현재 환율 - 매도 스프레드
) {
    val hasSpread: Boolean
        get() = buySpreadWon > 0.0 || sellSpreadWon > 0.0
}
