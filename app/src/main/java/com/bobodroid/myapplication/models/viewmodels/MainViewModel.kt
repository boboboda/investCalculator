package com.bobodroid.myapplication.models.viewmodels

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bobodroid.myapplication.MainActivity.Companion.TAG
import com.bobodroid.myapplication.data.mapper.RecordMapper.toDto
import com.bobodroid.myapplication.domain.entity.ExchangeRateEntity
import com.bobodroid.myapplication.domain.entity.HoldingStats
import com.bobodroid.myapplication.domain.entity.PremiumType
import com.bobodroid.myapplication.domain.entity.RecordEntity
import com.bobodroid.myapplication.domain.entity.RecordFilterCriteria
import com.bobodroid.myapplication.domain.entity.UserEntity
import com.bobodroid.myapplication.domain.repository.IExchangeRateRepository
import com.bobodroid.myapplication.domain.repository.IUserRepository
import com.bobodroid.myapplication.domain.usecase.exchange.CalculateExchangeUseCase
import com.bobodroid.myapplication.domain.usecase.record.CalculateHoldingStatsUseCase
import com.bobodroid.myapplication.domain.usecase.record.FilterRecordsUseCase
import com.bobodroid.myapplication.domain.usecase.record.GroupRecordsByCategoryUseCase
import com.bobodroid.myapplication.models.datamodels.repository.LatestRateRepository
import com.bobodroid.myapplication.models.datamodels.repository.Notice
import com.bobodroid.myapplication.models.datamodels.repository.NoticeRepository
import com.bobodroid.myapplication.models.datamodels.roomDb.*
import com.bobodroid.myapplication.models.datamodels.useCases.RecordUseCase
import com.bobodroid.myapplication.models.datamodels.useCases.UserUseCases
import com.bobodroid.myapplication.models.repository.SettingsRepository
import com.bobodroid.myapplication.premium.PremiumManager
import com.bobodroid.myapplication.screens.MainEvent
import com.bobodroid.myapplication.screens.PopupEvent
import com.bobodroid.myapplication.screens.RecordListEvent
import com.bobodroid.myapplication.util.AdMob.AdManager
import com.bobodroid.myapplication.widget.WidgetUpdateHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val userUseCases: UserUseCases,
    private val userRepository: IUserRepository,
//    private val latestRateRepository: LatestRateRepository,
    private val exchangeRateRepository: IExchangeRateRepository,
    private val noticeRepository: NoticeRepository,
    private val settingsRepository: SettingsRepository,
    private val recordUseCase: RecordUseCase,
    private val premiumManager: PremiumManager,
    private val calculateHoldingStatsUseCase: CalculateHoldingStatsUseCase,
    private val calculateExchangeUseCase: CalculateExchangeUseCase,
    @ApplicationContext private val context: Context
) : ViewModel() {



    private val _mainUiState = MutableStateFlow(MainUiState())
    val mainUiState = _mainUiState.asStateFlow()

    private val _noticeUiState = MutableStateFlow(NoticeUiState())
    val noticeUiState = _noticeUiState.asStateFlow()


    private val _recordListUiState = MutableStateFlow(RecordListUiState())
    val recordListUiState = _recordListUiState.asStateFlow()

    private val _mainSnackBarState = Channel<String>()
    val mainSnackBarState = _mainSnackBarState.receiveAsFlow()

    private val _sheetSnackBarState = Channel<String>()
    val sheetSnackBarState = _sheetSnackBarState.receiveAsFlow()

    private val todayDate = MutableStateFlow("${LocalDate.now()}")

    val alarmPermissionState = MutableStateFlow(false)


    val selectedCurrency = settingsRepository.selectedCurrency.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = CurrencyType.USD
    )


    // 뷰모델 초기화

    init {
        Log.e(TAG("MainViewModel", "init"), "━━━━━━━━━━━━━━━━━━━━━━━━━━")
        Log.e(TAG("MainViewModel", "init"), "🔥 MainViewModel 초기화 시작!")
        Log.e(TAG("MainViewModel", "init"), "ViewModel 인스턴스: ${this.hashCode()}")
        Log.e(TAG("MainViewModel", "init"), "━━━━━━━━━━━━━━━━━━━━━━━━━━")

        startInitialData()

        viewModelScope.launch {
            settingsRepository.selectedCurrency.collect { currency ->
                _mainUiState.update { it.copy(selectedCurrencyType = currency) }
            }
        }
    }

    // 프리미엄 체크
    suspend fun checkPremiumStatus(): Boolean {
        val user = userRepository.userData.value?.localUserData ?: return false
        return premiumManager.checkPremiumStatus(user) != PremiumType.NONE
    }


    // ✅ 초기화 메서드
    private fun startInitialData() {
        Log.d(TAG("MainViewModel", "startInitialData"), "━━━━━━━━━━━━━━━━━━━━━━━━━━")
        Log.d(TAG("MainViewModel", "startInitialData"), "📋 초기화 작업 시작")
        Log.d(TAG("MainViewModel", "startInitialData"), "━━━━━━━━━━━━━━━━━━━━━━━━━━")

        viewModelScope.launch {
            Log.d(TAG("MainViewModel", "startInitialData"), "🌐 WebSocket 구독 시작")
            exchangeRateRepository.subscribeToRateUpdates()
        }

        viewModelScope.launch {
            receivedLatestRate()
        }

        viewModelScope.launch {
            getRecords()
            Log.d(TAG("MainViewModel", "init"), "기록불러오기 확인완료")
        }

        viewModelScope.launch {
            calculateHoldingStats()
            Log.d(TAG("MainViewModel", "init"), "보유 통계 계산 시작")
        }
    }

    // ✅ 웹소켓 실시간 데이터 구독
    private suspend fun receivedLatestRate() {
        Log.d(TAG("MainViewModel", "receivedLatestRate"), "🔄 환율 Flow 구독 시작")

        exchangeRateRepository.latestRate.collect { latestRate ->
            Log.d(TAG("MainViewModel", "receivedLatestRate"), "실시간 데이터 수신: $latestRate")

            val uiState = _mainUiState.value.copy(recentRate = latestRate)
            _mainUiState.emit(uiState)

            reFreshProfit()
            Log.d(TAG("MainViewModel", "receivedLatestRate"), "환율 업데이트 → 수익 자동 재계산 완료")

            WidgetUpdateHelper.updateAllWidgets(context)
            Log.d(TAG("MainViewModel", "receivedLatestRate"),
                "위젯 업데이트 완료: USD=${latestRate.usd}, JPY=${latestRate.jpy}")
        }
    }

    private suspend fun localUserExistCheck() {
        Log.d(TAG("MainViewModel", "localUserExistCheck"), "━━━━━━━━━━━━━━━━━━━━━━━━━━")
        Log.d(TAG("MainViewModel", "localUserExistCheck"), "👤 로컬 유저 체크 시작")

        val initUserdata = userRepository.waitForUserData()

        Log.d(TAG("MainViewModel", "localUserExistCheck"), "📦 가져온 유저 데이터:")
        Log.d(TAG("MainViewModel", "localUserExistCheck"), "  - ID: ${initUserdata.localUserData.id}")
        Log.d(TAG("MainViewModel", "localUserExistCheck"), "  - SocialType: ${initUserdata.localUserData.socialType}")

        val uiState = _mainUiState.value.copy(localUser = initUserdata.localUserData)
        _mainUiState.emit(uiState)

        Log.d(TAG("MainViewModel", "localUserExistCheck"), "✅ UI 상태 업데이트 완료")
        Log.d(TAG("MainViewModel", "localUserExistCheck"), "━━━━━━━━━━━━━━━━━━━━━━━━━━")
    }

    private suspend fun noticeDialogState() {
        Log.d(TAG("MainViewModel", "noticeDialogState"), "━━━━━━━━━━━━━━━━━━━━━━━━━━")
        Log.d(TAG("MainViewModel", "noticeDialogState"), "📢 공지사항 다이얼로그 상태 체크")

        val noticeDate = _noticeUiState.value.notice.date
        val noticeContent = _noticeUiState.value.notice.content
        val userShowNoticeDate = _mainUiState.value.localUser.userShowNoticeDate

        if(noticeContent == null) {
            Log.d(TAG("MainViewModel", "noticeDialogState"), "❌ 공지가 없습니다 - 다이얼로그 표시 안함")
            val uiState = _noticeUiState.value.copy(showNoticeDialog = false, noticeState = false)
            _noticeUiState.emit(uiState)
            return
        }

        if(userShowNoticeDate == null) {
            Log.d(TAG("MainViewModel", "noticeDialogState"), "⚠️ 사용자 연기 날짜가 없음 → 다이얼로그 표시")
            val uiState = _noticeUiState.value.copy(showNoticeDialog = true)
            _noticeUiState.emit(uiState)
            return
        }

        val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        val noticeDateFormat = LocalDateTime.parse(noticeDate, dateTimeFormatter)
        val userShowNoticeDateFormat = LocalDateTime.parse(userShowNoticeDate, dateTimeFormatter)

        val showDialog = noticeDateFormat > userShowNoticeDateFormat

        val uiState = _noticeUiState.value.copy(showNoticeDialog = showDialog)
        _noticeUiState.emit(uiState)

        Log.d(TAG("MainViewModel", "noticeDialogState"), "최종 showNoticeDialog: ${_noticeUiState.value.showNoticeDialog}")
        Log.d(TAG("MainViewModel", "noticeDialogState"), "━━━━━━━━━━━━━━━━━━━━━━━━━━")
    }


    private suspend fun noticeExistCheck() {
        val noticeData = noticeRepository.waitForNoticeData()

        if (noticeData == null) {
            Log.d(TAG("MainViewModel", "noticeExistCheck"), "공지사항이 없습니다.")
            return
        }

        val uiState = _noticeUiState.value.copy(notice = noticeData)
        _noticeUiState.emit(uiState)
    }

    // ✅ 수익 재계산 - 새로운 구조
    private suspend fun reFreshProfit() {
        val recentRate = _mainUiState.value.recentRate
        val allRates = recentRate.rates

        recordUseCase.refreshAllCurrencyProfits(allRates)
    }

    // ✅ 기록 불러오기 - 새로운 구조
    private fun getRecords() {
        viewModelScope.launch {
            // ⭐ RecordUseCase가 이미 그룹화까지 완료한 Map을 반환
            recordUseCase.getAllCurrencyRecords().collect { records ->
                Log.d(TAG("MainViewModel", "getRecords"), "기록불러오기: $records")

                // ⭐ 그대로 사용하면 됨 (추가 처리 불필요)
                _recordListUiState.update {
                    it.copy(currencyRecords = records)
                }
            }
        }
    }


    // ✅ 보유중인 외화 통계 계산 - 새로운 구조
    private suspend fun calculateHoldingStats() {
        combine(
            recordListUiState,
            mainUiState
        ) { recordState, mainState ->

            // ⭐ Step 1: 통화별 기록 준비
            val recordsByType = mutableMapOf<CurrencyType, List<RecordEntity>>()
            CurrencyType.values().forEach { currencyType ->
                // 보유중인 기록만 필터링 (매도 안된 것)
                val records = recordState.getRecordsByType(currencyType).records
                    .filter { it.recordColor == false }
                recordsByType[currencyType] = records
            }

            // ⭐ Step 2: 현재 환율 준비
            val currentRates = mutableMapOf<String, String>()
            CurrencyType.values().forEach { currencyType ->
                currentRates[currencyType.name] =
                    mainState.recentRate.getRateByCode(currencyType.name) ?: "0"
            }

            // ⭐ Step 3: UseCase 호출
            calculateHoldingStatsUseCase.execute(recordsByType, currentRates)

        }.collect { stats ->
            // ⭐ Step 4: UI 상태 업데이트
            _mainUiState.update { it.copy(holdingStats = stats) }
        }
    }





    private fun formatCurrency(amount: BigDecimal): String {
        val absAmount = amount.abs()
        val formatted = "%,.0f".format(absAmount)
        return when {
            amount > BigDecimal.ZERO -> "+₩$formatted"
            amount < BigDecimal.ZERO -> "-₩$formatted"
            else -> "₩$formatted"
        }
    }





    // ✅ 현재 통화 타입별 기록 가져오기 - 새로운 구조
    fun getCurrentRecordsFlow(): Flow<CurrencyRecordState<RecordEntity>> =
        combine(recordListUiState, mainUiState) { recordState, mainState ->
            recordState.getCurrentRecords(mainState.selectedCurrencyType)
        }

    // 프리미엄 상태
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

    // ✅ 매도 계산 - 새로운 구조
    private suspend fun sellCalculate(sellRate: String) {
        val selectedRecord = _recordListUiState.value.selectedRecord as? RecordEntity ?: return
        val exchangeMoney = selectedRecord.exchangeMoney ?: return
        val krMoney = selectedRecord.money ?: return

        val currency = selectedRecord.getCurrency() ?: return
        val sellProfit = calculateExchangeUseCase.calculateSellProfit(currency,exchangeMoney, sellRate, krMoney).toString()
        val sellPercent = recordUseCase.sellPercent(sellProfit, krMoney).toString()

        val recordUiUpdateState = _recordListUiState.value.copy(
            sellProfit = sellProfit,
            sellPercent = sellPercent
        )

        _recordListUiState.emit(recordUiUpdateState)
    }

    // ✅ 메인 이벤트 처리 - 새로운 구조
    fun handleMainEvent(event: MainEvent) {
        when(event) {
            is MainEvent.GroupAdd -> {
                viewModelScope.launch {
                    val currencyCode = _mainUiState.value.selectedCurrencyType.name
                    val currentState = _recordListUiState.value.getRecordsByCode(currencyCode)
                    val newGroups = recordUseCase.addCurrencyGroup(
                        currencyCode = currencyCode,
                        currentGroups = currentState.groups,
                        newGroupName = event.groupName
                    )

                    val updatedRecords = _recordListUiState.value.currencyRecords.toMutableMap()
                    updatedRecords[currencyCode] = currentState.copy(groups = newGroups)

                    _recordListUiState.update { it.copy(currencyRecords = updatedRecords) }
                }
            }
            is MainEvent.ShowEditBottomSheet -> {
                _mainUiState.update { it.copy(showEditBottomSheet = true) }
                _recordListUiState.update { it.copy(selectedRecord = event.record) }
            }
            is MainEvent.ShowRateBottomSheet -> {
                _mainUiState.update { it.copy(showRateBottomSheet = true) }
                _recordListUiState.update { it.copy(selectedRecord = event.record) }
            }
            MainEvent.HideSellResultDialog -> {
                _mainUiState.update {
                    it.copy(selectedDate = today, showSellResultDialog = false)
                }
            }
            is MainEvent.SellRecord -> {
                viewModelScope.launch {
                    val sellRecord = _recordListUiState.value.selectedRecord as? RecordEntity ?: return@launch
                    recordUseCase.sellCurrencyRecord(
                        record = sellRecord,
                        sellDate = _mainUiState.value.selectedDate,
                        sellRate = _recordListUiState.value.sellRate
                    )
                    _mainUiState.update { it.copy(showSellResultDialog = false) }
                }
            }
            is MainEvent.SelectedDate -> {
                _mainUiState.update { it.copy(selectedDate = event.date) }
            }
            MainEvent.ShowAddBottomSheet -> {
                _mainUiState.update { it.copy(showAddBottomSheet = true) }
            }
            is MainEvent.SnackBarEvent -> {
                viewModelScope.launch {
                    _mainSnackBarState.send(event.message)
                }
            }
            is MainEvent.BottomSheetEvent.DismissSheet -> {
                _mainUiState.update { it.copy(showAddBottomSheet = false) }
            }
            is MainEvent.BottomSheetEvent.OnRecordAdd -> {
                // ✅ 하드코딩 제거: CurrencyType.name으로 동적 처리
                val currencyCode = mainUiState.value.selectedCurrencyType.name
                val latestRate = _mainUiState.value.recentRate.getRateByCode(currencyCode)

                if(latestRate == null || latestRate.isEmpty()) return

                viewModelScope.launch {
                    // ✅ 통합 메서드 사용 (currencyCode 기반)
                    recordUseCase.addCurrencyRecord(
                        currencyCode = currencyCode,
                        money = event.money,
                        inputRate = event.rate,
                        latestRate = latestRate,
                        groupName = event.group,
                        date = _mainUiState.value.selectedDate
                    )
                }
            }
            is MainEvent.BottomSheetEvent.OnGroupSelect -> {
                _mainUiState.update { it.copy(showGroupAddDialog = true) }
            }
            is MainEvent.BottomSheetEvent.OnDateSelect -> {
                _mainUiState.update { it.copy(showDatePickerDialog = true) }
            }
            is MainEvent.BottomSheetEvent.OnCurrencyTypeChange -> {
                _mainUiState.update { it.copy(selectedCurrencyType = event.currencyType) }
            }
            MainEvent.RateBottomSheetEvent.DismissRequest -> {
                _mainUiState.update { it.copy(showRateBottomSheet = false) }
            }
            is MainEvent.RateBottomSheetEvent.SellClicked -> {
                _mainUiState.update { it.copy(showRateBottomSheet = false) }
                viewModelScope.launch {
                    sellCalculate(sellRate = event.sellRate)
                    _mainUiState.update { it.copy(showSellResultDialog = true) }
                    _recordListUiState.update { it.copy(sellRate = event.sellRate) }
                }
            }
            MainEvent.RateBottomSheetEvent.ShowDatePickerDialog -> {
                _mainUiState.update { it.copy(showDatePickerDialog = true) }
            }
            MainEvent.EditBottomSheetEvent.DismissRequest -> {
                _mainUiState.update { it.copy(showEditBottomSheet = false) }
            }
            is MainEvent.EditBottomSheetEvent.EditSelected -> {
                viewModelScope.launch {
                    val currencyRecord = event.record as? RecordEntity ?: return@launch
                    recordUseCase.editCurrencyRecord(
                        record = currencyRecord,
                        editDate = _mainUiState.value.selectedDate,
                        editMoney = event.editMoney,
                        editRate = event.editRate
                    )
                }
            }
            is MainEvent.EditBottomSheetEvent.ShowDatePickerDialog -> {
                _mainUiState.update { it.copy(showDatePickerDialog = true) }
            }
            is MainEvent.GroupChangeBottomSheetEvent -> {
                when(event) {
                    MainEvent.GroupChangeBottomSheetEvent.DismissRequest -> {
                        _mainUiState.update { it.copy(showGroupChangeBottomSheet = false) }
                    }
                    is MainEvent.GroupChangeBottomSheetEvent.GroupChanged -> {
                        viewModelScope.launch {
                            val currencyRecord = event.record as? RecordEntity ?: return@launch
                            recordUseCase.updateCurrencyRecordCategory(currencyRecord, event.groupName)
                            _mainUiState.update { it.copy(showGroupChangeBottomSheet = false) }
                        }
                    }
                    MainEvent.GroupChangeBottomSheetEvent.OnGroupSelect -> {
                        _mainUiState.update { it.copy(showGroupAddDialog = true) }
                    }
                }
            }
            is MainEvent.HideGroupChangeBottomSheet -> {
                _mainUiState.update { it.copy(showGroupChangeBottomSheet = false) }
            }
            MainEvent.HideDatePickerDialog -> {
                _mainUiState.update { it.copy(showDatePickerDialog = false) }
            }
            MainEvent.HideGroupAddDialog -> {
                _mainUiState.update { it.copy(showGroupAddDialog = false) }
            }
            MainEvent.ShowDatePickerDialog -> {
                _mainUiState.update { it.copy(showDatePickerDialog = true) }
            }
            is MainEvent.BottomSheetEvent.Popup -> {
                when(event.popupEvent) {
                    is PopupEvent.SnackBarEvent -> {
                        viewModelScope.launch {
                            _sheetSnackBarState.send(event.popupEvent.message)
                        }
                    }
                    else -> return
                }
            }
            is MainEvent.EditBottomSheetEvent.Popup -> {
                when(event.event) {
                    is PopupEvent.SnackBarEvent -> {
                        viewModelScope.launch {
                            _sheetSnackBarState.send(event.event.message)
                        }
                    }
                    else -> return
                }
            }
            is MainEvent.RateBottomSheetEvent.Popup -> {
                when(event.event) {
                    is PopupEvent.SnackBarEvent -> {
                        viewModelScope.launch {
                            _sheetSnackBarState.send(event.event.message)
                        }
                    }
                    else -> return
                }
            }
            MainEvent.HideDateRangeDialog -> {
                _mainUiState.update { it.copy(showDateRangeDialog = false) }
            }
            MainEvent.ShowDateRangeDialog -> {
                _mainUiState.update { it.copy(showDateRangeDialog = true) }
            }
        }
    }

    // ✅ 기록 이벤트 처리 - 새로운 구조
    fun handleRecordEvent(event: RecordListEvent) {
        val uiState = _recordListUiState.value
        viewModelScope.launch {
            when (event) {
                is RecordListEvent.MemoUpdate -> {
                    val currencyRecord = event.record as? RecordEntity ?: return@launch
                    recordUseCase.updateCurrencyRecordMemo(currencyRecord, event.updateMemo)
                    _mainSnackBarState.send("메모가 저장되었습니다.")
                }
                is RecordListEvent.RemoveRecord -> {
                    val currencyRecord = event.data as? RecordEntity ?: return@launch
                    recordUseCase.removeCurrencyRecord(currencyRecord)
                }
                is RecordListEvent.CancelSellRecord -> {
                    val allRecords = uiState.currencyRecords.values.flatMap { it.records }
                    val record = allRecords.find { it.id == event.id } ?: return@launch
                    recordUseCase.cancelSellCurrencyRecord(record)
                    _mainSnackBarState.send("매도가 취소되었습니다.")
                }
                is RecordListEvent.UpdateRecordCategory -> {
                    val currencyRecord = event.record as? RecordEntity ?: return@launch
                    recordUseCase.updateCurrencyRecordCategory(currencyRecord, event.groupName)
                    _mainUiState.update { it.copy(showGroupChangeBottomSheet = false) }
                }
                is RecordListEvent.AddGroup -> {
                    val currencyCode = _mainUiState.value.selectedCurrencyType.name
                    val currentState = uiState.getRecordsByCode(currencyCode)
                    val newGroups = recordUseCase.addCurrencyGroup(
                        currencyCode = currencyCode,
                        currentGroups = currentState.groups,
                        newGroupName = event.groupName
                    )

                    val updatedRecords = uiState.currencyRecords.toMutableMap()
                    updatedRecords[currencyCode] = currentState.copy(groups = newGroups)

                    _recordListUiState.update { it.copy(currencyRecords = updatedRecords) }
                }
                is RecordListEvent.ShowGroupChangeBottomSheet -> {
                    _recordListUiState.update { it.copy(selectedRecord = event.data) }
                    _mainUiState.update { it.copy(showGroupChangeBottomSheet = true) }
                }
                is RecordListEvent.ShowEditBottomSheet -> {
                    _mainUiState.update { it.copy(showEditBottomSheet = true) }
                    _recordListUiState.update { it.copy(selectedRecord = event.data) }
                }
                is RecordListEvent.SnackBarEvent -> {
                    _mainSnackBarState.send(event.message)
                }
                is RecordListEvent.TotalSumProfit -> {
                    _recordListUiState.update {
                        it.copy(
                            totalProfitRangeDate = TotalProfitRangeDate(
                                startDate = event.startDate,
                                endDate = event.endDate
                            )
                        )
                    }

                    if(event.startDate.isEmpty() || event.endDate.isEmpty()) return@launch

                    val currentRecords = uiState.getCurrentRecords(_mainUiState.value.selectedCurrencyType)
                    val dateRangeFilterRecord = currentRecords.records
                        .filter { it.date!! in event.startDate..event.endDate }

                    val totalProfit = recordUseCase.sumProfit(record = dateRangeFilterRecord)

                    val currencyCode = _mainUiState.value.selectedCurrencyType.name
                    val updatedRecords = uiState.currencyRecords.toMutableMap()
                    updatedRecords[currencyCode] = currentRecords.copy(totalProfit = totalProfit)

                    _recordListUiState.update { it.copy(currencyRecords = updatedRecords) }
                }
                else -> return@launch
            }
        }
    }

    fun selectDelayDate() {
        viewModelScope.launch {
            Log.d(TAG("MainViewModel", "selectDelayDate"), "날짜 연기 신청")

            val updateUserData = _mainUiState.value.localUser.copy(
                userShowNoticeDate = _noticeUiState.value.notice.date
            )

            userUseCases.localUserUpdate(updateUserData)

            Log.d(TAG("MainViewModel", "selectDelayDate"),
                "날짜 수정 실행, ${_noticeUiState.value.notice.date}")
        }
    }

    fun closeNotice() {
        val uiState = _noticeUiState.value.copy(showNoticeDialog = false, noticeState = false)
        _noticeUiState.value = uiState
    }

    override fun onCleared() {
        super.onCleared()
        Log.e(TAG("MainViewModel", "onCleared"), "━━━━━━━━━━━━━━━━━━━━━━━━━━")
        Log.e(TAG("MainViewModel", "onCleared"), "💀 ViewModel이 소멸됩니다!")
        Log.e(TAG("MainViewModel", "onCleared"), "ViewModel 인스턴스: ${this.hashCode()}")
        Log.e(TAG("MainViewModel", "onCleared"), "━━━━━━━━━━━━━━━━━━━━━━━━━━")
    }
}

// ============================================================================
// Data Classes
// ============================================================================

val time = Calendar.getInstance().time
val formatter = SimpleDateFormat("yyyy-MM-dd")
val today = formatter.format(time)

data class MainUiState (
    val selectedCurrencyType: CurrencyType = CurrencyType.USD,
    val selectedDate: String = today,
    val recentRate: ExchangeRateEntity = ExchangeRateEntity(),
    val localUser: UserEntity = UserEntity(),
    val showRateBottomSheet: Boolean = false,
    val showEditBottomSheet: Boolean = false,
    val showSellResultDialog: Boolean = false,
    val showAddBottomSheet: Boolean = false,
    val showGroupAddDialog: Boolean = false,
    val showDatePickerDialog: Boolean = false,
    val showDateRangeDialog: Boolean = false,
    val showGroupChangeBottomSheet: Boolean = false,
    val holdingStats: HoldingStats = HoldingStats()
)

data class NoticeUiState(
    val showNoticeDialog: Boolean = false,
    val notice: Notice = Notice(),
    val noticeState: Boolean = true
)


data class TotalProfitRangeDate(
    val startDate: String = "",
    val endDate: String = ""
)

data class RecordListUiState(
    val currencyRecords: Map<String, CurrencyRecordState<RecordEntity>> = emptyMap(),
    val selectedRecord: ForeignCurrencyRecord? = null,
    val sellRate: String = "",
    val sellProfit: String = "",
    val sellPercent: String = "",
    val refreshDate: String = "",
    val totalProfitRangeDate: TotalProfitRangeDate = TotalProfitRangeDate()
) {
    fun getRecordsByCode(currencyCode: String): CurrencyRecordState<RecordEntity> {
        return currencyRecords[currencyCode] ?: CurrencyRecordState()
    }

    fun getRecordsByType(type: CurrencyType): CurrencyRecordState<RecordEntity> {
        return currencyRecords[type.name] ?: CurrencyRecordState()
    }

    fun getRecordsByCurrency(currency: Currency): CurrencyRecordState<RecordEntity> {
        return currencyRecords[currency.code] ?: CurrencyRecordState()
    }

    fun getCurrentRecords(selectedType: CurrencyType): CurrencyRecordState<RecordEntity> {
        return getRecordsByType(selectedType)
    }

    fun getUsdRecords(): CurrencyRecordState<RecordEntity> {
        return getRecordsByCode("USD")
    }

    fun getJpyRecords(): CurrencyRecordState<RecordEntity> {
        return getRecordsByCode("JPY")
    }
}

data class CurrencyRecordState<T: ForeignCurrencyRecord>(
    val records: List<T> = emptyList(),
    val groupedRecords: Map<String, List<RecordEntity>> = emptyMap(),
    val groups: List<String> = emptyList(),
    val totalProfit: String = "",
)



