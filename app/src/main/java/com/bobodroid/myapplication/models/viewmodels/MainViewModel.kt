package com.bobodroid.myapplication.models.viewmodels

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetState
import androidx.compose.runtime.internal.illegalDecoyCallException
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bobodroid.myapplication.MainActivity.Companion.TAG
import com.bobodroid.myapplication.models.datamodels.repository.InvestRepository
import com.bobodroid.myapplication.models.datamodels.roomDb.ExchangeRate
import com.bobodroid.myapplication.models.datamodels.repository.LatestRateRepository
import com.bobodroid.myapplication.models.datamodels.repository.Notice
import com.bobodroid.myapplication.models.datamodels.repository.NoticeRepository
import com.bobodroid.myapplication.models.datamodels.repository.UserRepository
import com.bobodroid.myapplication.models.datamodels.roomDb.CurrencyType
import com.bobodroid.myapplication.models.datamodels.roomDb.DrBuyRecord
import com.bobodroid.myapplication.models.datamodels.roomDb.ForeignCurrencyRecord
import com.bobodroid.myapplication.models.datamodels.roomDb.LocalUserData
import com.bobodroid.myapplication.models.datamodels.roomDb.YenBuyRecord
import com.bobodroid.myapplication.models.datamodels.social.SocialLoginResult
import com.bobodroid.myapplication.models.datamodels.useCases.CurrencyRecordRequest
import com.bobodroid.myapplication.models.datamodels.useCases.RecordUseCase
import com.bobodroid.myapplication.models.datamodels.useCases.UserUseCases
import com.bobodroid.myapplication.screens.MainEvent
import com.bobodroid.myapplication.screens.PopupEvent
import com.bobodroid.myapplication.screens.RecordListEvent
import com.bobodroid.myapplication.util.AdMob.AdManager
import com.bobodroid.myapplication.util.AdMob.AdUseCase
import com.bobodroid.myapplication.util.result.onError
import com.bobodroid.myapplication.util.result.onSuccess
import com.bobodroid.myapplication.widget.WidgetUpdateHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Date
import java.util.GregorianCalendar
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val userUseCases: UserUseCases,
    private val userRepository: UserRepository,
    private val latestRateRepository: LatestRateRepository,
    private val noticeRepository: NoticeRepository,
    private val adManager: AdManager,
    private val adUseCase: AdUseCase,
    private val recordUseCase: RecordUseCase,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _mainUiState = MutableStateFlow(MainUiState())
    val mainUiState = _mainUiState.asStateFlow()

    private val _noticeUiState = MutableStateFlow(NoticeUiState())
    val noticeUiState = _noticeUiState.asStateFlow()

    private val _adUiState = MutableStateFlow(AdUiState())
    val adUiState = _adUiState.asStateFlow()

    private val _recordListUiState = MutableStateFlow(RecordListUiState())
    val recordListUiState = _recordListUiState.asStateFlow()

    private val _mainSnackBarState = Channel<String>()
    val mainSnackBarState = _mainSnackBarState.receiveAsFlow()

    private val _sheetSnackBarState = Channel<String>()
    val sheetSnackBarState = _sheetSnackBarState.receiveAsFlow()

    private val todayDate = MutableStateFlow("${LocalDate.now()}")

    val alarmPermissionState = MutableStateFlow(false)



    init {
        Log.e(TAG("MainViewModel", "init"), "━━━━━━━━━━━━━━━━━━━━━━━━━━")
        Log.e(TAG("MainViewModel", "init"), "🔥 MainViewModel 초기화 시작!")
        Log.e(TAG("MainViewModel", "init"), "ViewModel 인스턴스: ${this.hashCode()}")
        Log.e(TAG("MainViewModel", "init"), "━━━━━━━━━━━━━━━━━━━━━━━━━━")
        startInitialData()
    }

    // 리워드 다이로그 연기 설정
   fun rewardDelayDate() {
       viewModelScope.launch {
           adUseCase.delayRewardAd(_mainUiState.value.localUser, todayDate.value)
       }
    }

    fun closeRewardDialog() {
        val uiState = _adUiState.value.copy(rewardShowDialog = false)
        _adUiState.value = uiState
    }


    // 공지사항 연기 설정
    fun selectDelayDate() {
        viewModelScope.launch {

            Log.d(TAG("MainViewModel", "selectDelayDate"), "날짜 연기 신청")

            val updateUserData = _mainUiState.value.localUser.copy(
                userShowNoticeDate = _noticeUiState.value.notice.date
            )

            userUseCases.localUserUpdate(updateUserData)

            Log.d(TAG("MainViewModel", "selectDelayDate"), "날짜 수정 실행, ${_noticeUiState.value.notice.date}")
        }
    }

    // 공지사항 닫기
    fun closeNotice() {
        val uiState = _noticeUiState.value.copy(showNoticeDialog = false, noticeState = false)

        _noticeUiState.value = uiState
    }

    // 초기화 매소드
    private fun startInitialData() {
        Log.d(TAG("MainViewModel", "startInitialData"), "━━━━━━━━━━━━━━━━━━━━━━━━━━")
        Log.d(TAG("MainViewModel", "startInitialData"), "📋 초기화 작업 시작")
        Log.d(TAG("MainViewModel", "startInitialData"), "━━━━━━━━━━━━━━━━━━━━━━━━━━")

        // ✅ Flow collect는 별도 코루틴으로 분리 (무한 루프이기 때문)
        viewModelScope.launch {
            receivedLatestRate()
        }

        // ✅ 기록 수집도 별도 코루틴으로
        viewModelScope.launch {
            getRecords()
            Log.d(TAG("MainViewModel", "init"), "기록불러오기 확인완료")
        }

        // ✅ 초기화 작업들은 순차적으로 실행
        viewModelScope.launch {
            Log.d(TAG("MainViewModel", "startInitialData"), "📌 Step 1: localUserExistCheck 시작")
            localUserExistCheck()
            Log.d(TAG("MainViewModel", "init"), "✅ 로컬유저 확인완료")

            Log.d(TAG("MainViewModel", "startInitialData"), "📌 Step 2: noticeExistCheck 시작")
            noticeExistCheck()
            Log.d(TAG("MainViewModel", "init"), "✅ 공지사항 확인완료")

            Log.d(TAG("MainViewModel", "startInitialData"), "📌 Step 3: noticeDialogState 시작")
            noticeDialogState()
            Log.d(TAG("MainViewModel", "init"), "✅ 공지사항 다이얼로그 확인완료")

            Log.d(TAG("MainViewModel", "startInitialData"), "📌 Step 4: adDialogState 시작")
            adDialogState()
            Log.d(TAG("MainViewModel", "init"), "✅ 광고 확인완료")

            Log.d(TAG("MainViewModel", "startInitialData"), "📌 Step 5: fetchInitialLatestRate 시작")
            latestRateRepository.fetchInitialLatestRate()
            Log.d(TAG("MainViewModel", "init"), "✅ 초기 최신환율 확인완료")

            Log.d(TAG("MainViewModel", "startInitialData"), "━━━━━━━━━━━━━━━━━━━━━━━━━━")
            Log.d(TAG("MainViewModel", "startInitialData"), "✨ 모든 초기화 작업 완료!")
            Log.d(TAG("MainViewModel", "startInitialData"), "━━━━━━━━━━━━━━━━━━━━━━━━━━")
        }
    }



    // 웹소켓 실시간 데이터 구독 -> 완료
    private suspend fun receivedLatestRate() {
        latestRateRepository.latestRateFlow.collect { latestRate ->
            Log.d(TAG("MainViewModel", "receivedLatestRate"), "실시간 데이터 수신: $latestRate")

            // 1. UI 상태 업데이트
            val uiState = _mainUiState.value.copy(
                recentRate = latestRate
            )
            _mainUiState.emit(uiState)

            // 2. ✅ 자동으로 수익 재계산
            reFreshProfit()
            Log.d(TAG("MainViewModel", "receivedLatestRate"), "환율 업데이트 → 수익 자동 재계산 완료")

            WidgetUpdateHelper.updateAllWidgets(context)
            Log.d(TAG("MainViewModel", "receivedLatestRate"),
                "위젯 업데이트 완료: USD=${latestRate.usd}, JPY=${latestRate.jpy}")
        }
    }


    // 공지사항 상태 초기화 -> 완료
    private suspend fun noticeDialogState() {
        Log.d(TAG("MainViewModel", "noticeDialogState"), "━━━━━━━━━━━━━━━━━━━━━━━━━━")
        Log.d(TAG("MainViewModel", "noticeDialogState"), "📢 공지사항 다이얼로그 상태 체크")

        val noticeDate = _noticeUiState.value.notice.date
        val noticeContent = _noticeUiState.value.notice.content
        val userShowNoticeDate = _mainUiState.value.localUser.userShowNoticeDate
        val today = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))

        Log.d(TAG("MainViewModel", "noticeDialogState"), "📌 현재 상태:")
        Log.d(TAG("MainViewModel", "noticeDialogState"), "  - 공지사항 날짜: $noticeDate")
        Log.d(TAG("MainViewModel", "noticeDialogState"), "  - 사용자 연기 날짜: $userShowNoticeDate")
        Log.d(TAG("MainViewModel", "noticeDialogState"), "  - 오늘 날짜: $today")
        Log.d(TAG("MainViewModel", "noticeDialogState"), "  - 공지내용 존재: ${noticeContent != null}")
        Log.d(TAG("MainViewModel", "noticeDialogState"), "  - noticeState: ${_noticeUiState.value.noticeState}")

        if(noticeContent == null) {
            Log.d(TAG("MainViewModel", "noticeDialogState"), "❌ 공지가 없습니다 - 다이얼로그 표시 안함")
            Log.d(TAG("MainViewModel", "noticeDialogState"), "━━━━━━━━━━━━━━━━━━━━━━━━━━")
            return
        }

        // 닫기 후 호출 방지
        if (_noticeUiState.value.noticeState) {
            if (userShowNoticeDate.isNullOrEmpty()) {
                Log.d(TAG("MainViewModel", "noticeDialogState"), "⚠️ 사용자 연기 날짜가 없음 → 다이얼로그 표시")
                _noticeUiState.update {
                    _noticeUiState.value.copy(showNoticeDialog = true, noticeState = true)
                }
            } else {
                if (noticeDate != null && noticeDate > userShowNoticeDate) {
                    Log.d(TAG("MainViewModel", "noticeDialogState"), "✅ 공지사항 날짜 > 연기 날짜 → 다이얼로그 표시")
                    Log.d(TAG("MainViewModel", "noticeDialogState"), "   ($noticeDate > $userShowNoticeDate)")
                    _noticeUiState.update {
                        _noticeUiState.value.copy(showNoticeDialog = true, noticeState = true)
                    }
                } else {
                    Log.d(TAG("MainViewModel", "noticeDialogState"), "❌ 로컬 저장 날짜가 더 큼 → 다이얼로그 표시 안함")
                    Log.d(TAG("MainViewModel", "noticeDialogState"), "   ($noticeDate <= $userShowNoticeDate)")
                    _noticeUiState.value.copy(showNoticeDialog = false, noticeState = false)
                }
            }
        } else {
            Log.d(TAG("MainViewModel", "noticeDialogState"), "❌ noticeState가 false → 다이얼로그 표시 안함")
        }

        Log.d(TAG("MainViewModel", "noticeDialogState"), "최종 showNoticeDialog: ${_noticeUiState.value.showNoticeDialog}")
        Log.d(TAG("MainViewModel", "noticeDialogState"), "━━━━━━━━━━━━━━━━━━━━━━━━━━")
    }



    // 로컬유저 체크 -> 완료
    private fun localUserExistCheck() {
        viewModelScope.launch {
            Log.d(TAG("MainViewModel", "localUserExistCheck"), "━━━━━━━━━━━━━━━━━━━━━━━━━━")
            Log.d(TAG("MainViewModel", "localUserExistCheck"), "👤 로컬 유저 체크 시작")

            val initUserdata = userRepository.waitForUserData()

            Log.d(TAG("MainViewModel", "localUserExistCheck"), "📦 가져온 유저 데이터:")
            Log.d(TAG("MainViewModel", "localUserExistCheck"), "  - ID: ${initUserdata.localUserData.id}")
            Log.d(TAG("MainViewModel", "localUserExistCheck"), "  - SocialType: ${initUserdata.localUserData.socialType}")
            Log.d(TAG("MainViewModel", "localUserExistCheck"), "  - Email: ${initUserdata.localUserData.email}")
            Log.d(TAG("MainViewModel", "localUserExistCheck"), "  - userShowNoticeDate: ${initUserdata.localUserData.userShowNoticeDate}")
            Log.d(TAG("MainViewModel", "localUserExistCheck"), "  - rewardAdShowingDate: ${initUserdata.localUserData.rewardAdShowingDate}")

            val uiState = _mainUiState.value.copy(localUser = initUserdata.localUserData)
            _mainUiState.emit(uiState)

            Log.d(TAG("MainViewModel", "localUserExistCheck"), "✅ UI 상태 업데이트 완료")
            Log.d(TAG("MainViewModel", "localUserExistCheck"), "━━━━━━━━━━━━━━━━━━━━━━━━━━")
        }
    }

    // 광고 다이얼로그 상태 -> 완료
    private suspend fun adDialogState() {
        Log.d(TAG("MainViewModel", "adDialogState"), "━━━━━━━━━━━━━━━━━━━━━━━━━━")
        Log.d(TAG("MainViewModel", "adDialogState"), "📺 광고 다이얼로그 상태 체크")

        val isReady = adManager.isRewardAdReady.first()
        Log.d(TAG("MainViewModel", "adDialogState"), "  - 광고 준비 상태: $isReady")

        if(isReady) {
            val shouldShowAd = adUseCase.processRewardAdState(
                _mainUiState.value.localUser,
                todayDate.value
            )

            Log.d(TAG("MainViewModel", "adDialogState"), "  - 광고 표시 여부: $shouldShowAd")
            Log.d(TAG("MainViewModel", "adDialogState"), "  - 오늘 날짜: ${todayDate.value}")
            Log.d(TAG("MainViewModel", "adDialogState"), "  - 사용자 rewardAdShowingDate: ${_mainUiState.value.localUser.rewardAdShowingDate}")

            if(shouldShowAd) {
                Log.d(TAG("MainViewModel", "adDialogState"), "✅ 광고 다이얼로그 표시")
                val uiStateUpdate = _adUiState.value.copy(rewardShowDialog = true)
                _adUiState.emit(uiStateUpdate)
            } else {
                Log.d(TAG("MainViewModel", "adDialogState"), "❌ 광고 다이얼로그 표시 안함")
            }
        } else {
            Log.d(TAG("MainViewModel", "adDialogState"), "❌ 광고가 준비되지 않음")
        }

        Log.d(TAG("MainViewModel", "adDialogState"), "━━━━━━━━━━━━━━━━━━━━━━━━━━")
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







    // -> 완료
    suspend fun reFreshProfit() {
        val resentRate = _mainUiState.value.recentRate

        recordUseCase.reFreshProfit(resentRate, _recordListUiState.value.foreignCurrencyRecord)
    }


    private val formatTodayFlow = MutableStateFlow(
        LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
    )


//    fun cloudSave(
//        drBuyRecord: List<DrBuyRecord>,
//        drSellRecord: List<DrSellRecord>,
//        yenBuyRecord: List<YenBuyRecord>,
//        yenSellRecord: List<YenSellRecord>,
//        wonBuyRecord: List<WonBuyRecord>,
//        wonSellRecord: List<WonSellRecord>
//    ) {
//        val localUser = _localUser.value
//
//        val uploadCloudData = CloudUserData(
//            id = localUser.id.toString(),
//            customId = localUser.customId,
//            createAt = formatTodayFlow.value,
//            drBuyRecord = drBuyRecord,
//            drSellRecord = drSellRecord,
//            yenBuyRecord = yenBuyRecord,
//            yenSellRecord = yenSellRecord,
//            wonBuyRecord = wonBuyRecord,
//            wonSellRecord = wonSellRecord
//
//        )
//
//        db.collection("userCloud").document(localUser.customId!!)
//            .set(uploadCloudData.asHasMap())
//    }
//
//
//    fun cloudLoad(cloudId: String, cloudData: (CloudUserData, resultMessage: String) -> Unit) {
//
//        db.collection("userCloud")
//            .whereEqualTo("customId", cloudId)
//            .get()
//            .addOnSuccessListener { querySnapShot ->
//                val data = CloudUserData(querySnapShot)
//
//                cloudData.invoke(data, "클라우드 불러오기가 성공하였습니다.")
//            }
//    }


    // 기록 부분

    private fun getRecords() {
        viewModelScope.launch {
            recordUseCase.getRecord().collect { record ->
                Log.d(TAG("MainViewModel", "getRecords"), "기록불러오기: $record")
                _recordListUiState.update { it.copy(foreignCurrencyRecord = record) }
            }
        }
    }


    fun getCurrentRecordsFlow(): Flow<CurrencyRecordState<ForeignCurrencyRecord>> =
        recordListUiState.map { recordState ->
            when(_mainUiState.value.selectedCurrencyType) {
                CurrencyType.USD -> recordState.foreignCurrencyRecord.dollarState
                CurrencyType.JPY -> recordState.foreignCurrencyRecord.yenState
            }
        }



    fun updateCurrentForeignCurrency(currencyType: CurrencyType) {
        val updateUiState = _mainUiState.value.copy(selectedCurrencyType = currencyType)

        _mainUiState.value = updateUiState
    }


   private suspend fun sellCalculate(
        sellRate: String) {

       val exchangeMoney = _recordListUiState.value.selectedRecord?.exchangeMoney ?: return

       val krMoney = _recordListUiState.value.selectedRecord?.money ?: return

       val sellProfit = recordUseCase.sellProfit(exchangeMoney, sellRate, krMoney, _mainUiState.value.selectedCurrencyType).toString()
       val sellPercent = recordUseCase.sellPercent(sellProfit, krMoney).toString()

       val recordUiUpdateState = _recordListUiState.value.copy(sellProfit = sellProfit, sellPercent = sellPercent)

       _recordListUiState.emit(recordUiUpdateState)

    }

    fun handleMainEvent(event: MainEvent) {
        when(event) {
            is MainEvent.GroupAdd -> {
                viewModelScope.launch {
                    recordUseCase.groupAdd(_recordListUiState.value, event.groupName, _mainUiState.value.selectedCurrencyType) { updatedState ->
                        _recordListUiState.value = updatedState
                    }
                    MainEvent.HideGroupAddDialog
                }
            }
            is MainEvent.ShowEditBottomSheet -> {
                _mainUiState.update {
                    it.copy(showEditBottomSheet = true)
                }
                _recordListUiState.update {
                    it.copy(selectedRecord = event.record)
                }
            }
            is MainEvent.ShowRateBottomSheet -> {
                _mainUiState.update {
                    it.copy(
                        showRateBottomSheet = true
                    )
                }
                _recordListUiState.update {
                    it.copy(
                        selectedRecord = event.record
                    )
                }
            }
            MainEvent.HideSellResultDialog -> {
                _mainUiState.update {
                    it.copy(
                        selectedDate = today,
                        showSellResultDialog = false
                    )
                }
            }
            is MainEvent.SellRecord -> {
                viewModelScope.launch {
                    val sellRecord = _recordListUiState.value.selectedRecord ?: return@launch
                    recordUseCase.onSellRecord(
                        sellRecord,
                        _mainUiState.value.selectedDate,
                        _recordListUiState.value.sellRate,
                        _mainUiState.value.selectedCurrencyType
                    )

                    _mainUiState.update {
                        it.copy(
                            showSellResultDialog = false
                        )
                    }
                }
            }

            is MainEvent.SelectedDate -> {
                _mainUiState.update {
                    it.copy(
                        selectedDate = event.date
                    )
                }
            }

            MainEvent.ShowAddBottomSheet -> {
                _mainUiState.update {
                    it.copy(
                        showAddBottomSheet = true
                    )
                }
            }

            is MainEvent.SnackBarEvent -> {
                viewModelScope.launch {
                    _mainSnackBarState.send(event.message)
                }
            }

            is MainEvent.BottomSheetEvent.DismissSheet -> {
                _mainUiState.update {
                    it.copy(
                        showAddBottomSheet = false
                    )
                }
            }
            is MainEvent.BottomSheetEvent.OnRecordAdd -> {
                val latestRate = when(mainUiState.value.selectedCurrencyType) {
                    CurrencyType.USD -> _mainUiState.value.recentRate.usd
                    CurrencyType.JPY -> _mainUiState.value.recentRate.jpy
                }

                if(latestRate == null) return

                val addRequest = CurrencyRecordRequest(
                    latestRate = latestRate,
                    money = event.money,
                    inputRate = event.rate,
                    groupName = event.group,
                    date = _mainUiState.value.selectedDate,
                    type = mainUiState.value.selectedCurrencyType,
                )
                viewModelScope.launch {
                    recordUseCase.addCurrencyRecord(addRequest)
                    MainEvent.BottomSheetEvent.DismissSheet
                }
            }
            is MainEvent.BottomSheetEvent.OnGroupSelect -> {
                _mainUiState.update {
                    it.copy(
                        showGroupAddDialog = true
                    )
                }
            }
            is MainEvent.BottomSheetEvent.OnDateSelect -> {
                _mainUiState.update {
                    it.copy(
                        showDatePickerDialog = true
                    )
                }
            }
            is MainEvent.BottomSheetEvent.OnCurrencyTypeChange -> {
                _mainUiState.update {
                    it.copy(
                        selectedCurrencyType = event.currencyType
                    )
                }
            }

            MainEvent.RateBottomSheetEvent.DismissRequest -> {
                _mainUiState.update {
                    it.copy(
                        showRateBottomSheet = false
                    )
                }
            }
            is MainEvent.RateBottomSheetEvent.SellClicked -> {
                _mainUiState.update {
                    it.copy(
                        showRateBottomSheet = false
                    )
                }
                viewModelScope.launch {
                    sellCalculate(sellRate = event.sellRate)

                    _mainUiState.update {
                        it.copy(
                            showSellResultDialog = true
                        )
                    }

                    _recordListUiState.update {
                        it.copy(
                            sellRate = event.sellRate
                        )
                    }
                }
            }
            MainEvent.RateBottomSheetEvent.ShowDatePickerDialog -> {
                _mainUiState.update {
                    it.copy(
                        showDatePickerDialog = true
                    )
                }
            }

            MainEvent.EditBottomSheetEvent.DismissRequest -> {
                _mainUiState.update {
                    it.copy(
                        showEditBottomSheet = false
                    )
                }
            }
            is MainEvent.EditBottomSheetEvent.EditSelected -> {
                viewModelScope.launch {
                    recordUseCase.editRecord(
                        record = event.record,
                        _mainUiState.value.selectedDate,
                        editMoney = event.editMoney,
                        editRate = event.editRate,
                        type = _mainUiState.value.selectedCurrencyType
                    )
                }
            }
            is MainEvent.EditBottomSheetEvent.ShowDatePickerDialog -> {
                _mainUiState.update {
                    it.copy(
                        showDatePickerDialog = true
                    )
                }
            }

            // ✅ 추가: GroupChangeBottomSheetEvent 처리
            is MainEvent.GroupChangeBottomSheetEvent -> {
                when(event) {
                    MainEvent.GroupChangeBottomSheetEvent.DismissRequest -> {
                        _mainUiState.update {
                            it.copy(showGroupChangeBottomSheet = false)
                        }
                    }
                    is MainEvent.GroupChangeBottomSheetEvent.GroupChanged -> {
                        viewModelScope.launch {
                            recordUseCase.updateRecordCategory(
                                event.record,
                                event.groupName,
                                _mainUiState.value.selectedCurrencyType
                            )
                            _mainUiState.update {
                                it.copy(showGroupChangeBottomSheet = false)
                            }
                        }
                    }
                    MainEvent.GroupChangeBottomSheetEvent.OnGroupSelect -> {
                        _mainUiState.update {
                            it.copy(showGroupAddDialog = true)
                        }
                    }
                }
            }

            is MainEvent.HideGroupChangeBottomSheet -> {
                viewModelScope.launch {
                    _mainUiState.update {
                        it.copy(showGroupChangeBottomSheet = false)
                    }
                }
            }

            MainEvent.HideDatePickerDialog -> {
                _mainUiState.update {
                    it.copy(
                        showDatePickerDialog = false
                    )
                }
            }

            MainEvent.HideGroupAddDialog -> {
                _mainUiState.update {
                    it.copy(
                        showGroupAddDialog = false
                    )
                }
            }
            MainEvent.ShowDatePickerDialog -> {
                _mainUiState.update {
                    it.copy(
                        showDatePickerDialog = true
                    )
                }
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
                _mainUiState.update {
                    it.copy(
                        showDateRangeDialog = false
                    )
                }
            }

            MainEvent.ShowDateRangeDialog -> {
                _mainUiState.update {
                    it.copy(
                        showDateRangeDialog = true
                    )
                }
            }
        }
    }

    fun handleRecordEvent(event: RecordListEvent) {
        val uiState = _recordListUiState.value
        viewModelScope.launch {
            when (event) {
                is RecordListEvent.MemoUpdate -> {
                    recordUseCase.updateRecordMemo(event.record, event.updateMemo, _mainUiState.value.selectedCurrencyType)
                    _mainSnackBarState.send("메모가 저장되었습니다.")
                }
                    is RecordListEvent.RemoveRecord -> {
                        recordUseCase.removeRecord(event.data, _mainUiState.value.selectedCurrencyType)
                    }
                is RecordListEvent.CancelSellRecord -> {
                    recordUseCase.cancelSellRecord(event.id, _mainUiState.value.selectedCurrencyType)
                    _mainSnackBarState.send("매도가 취소되었습니다.")
                }
                is RecordListEvent.UpdateRecordCategory -> {
                    recordUseCase.updateRecordCategory(event.record, event.groupName, _mainUiState.value.selectedCurrencyType)
                }
                is RecordListEvent.AddGroup -> {
                   recordUseCase.groupAdd(uiState, event.groupName, _mainUiState.value.selectedCurrencyType) { updatedState ->
                       viewModelScope.launch {
                           _recordListUiState.emit(updatedState)
                       }
                   }
                }

                is RecordListEvent.ShowGroupChangeBottomSheet -> {
                    viewModelScope.launch {
                        _recordListUiState.update {
                            it.copy(selectedRecord = event.data)
                        }
                        _mainUiState.update {
                            it.copy(showGroupChangeBottomSheet = true)
                        }
                    }
                }

                is RecordListEvent.UpdateRecordCategory -> {
                    viewModelScope.launch {
                        // 기록 업데이트 로직 (기존에 있을 것으로 예상)
                        recordUseCase.updateRecordCategory(event.record, event.groupName, _mainUiState.value.selectedCurrencyType)

                        // 바텀시트 닫기
                        _mainUiState.update {
                            it.copy(showGroupChangeBottomSheet = false)
                        }
                    }
                }

                is RecordListEvent.ShowEditBottomSheet -> {
                    _mainUiState.update {
                        it.copy(
                            showEditBottomSheet = true
                        )
                    }
                    _recordListUiState.update {
                        it.copy(
                            selectedRecord = event.data
                        )
                    }
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

                    Log.d(TAG("MainViewModel", "handleRecordEvent"), "startDate: ${event.startDate}, endDate: ${event.endDate}")

                    viewModelScope.launch {
                        when(_mainUiState.value.selectedCurrencyType) {
                            CurrencyType.USD -> {
                                val dateRangeFilterRecord = _recordListUiState.value.foreignCurrencyRecord.dollarState.records.filter { it.date!! in event.startDate..event.endDate  }

                                Log.d(TAG("MainViewModel", "handleRecordEvent"), "USD: $dateRangeFilterRecord")

                                val totalProfit = recordUseCase.sumProfit(
                                    record = dateRangeFilterRecord,
                                )

                                Log.d(TAG("MainViewModel", "handleRecordEvent"), "USD: $totalProfit")

                                _recordListUiState.update { currentState ->
                                    currentState.copy(
                                        foreignCurrencyRecord = currentState.foreignCurrencyRecord.copy(
                                            dollarState = currentState.foreignCurrencyRecord.dollarState.copy(
                                                totalProfit = totalProfit
                                            )
                                        )
                                    )
                                }
                            }
                            CurrencyType.JPY -> {

                                val dateRangeFilterRecord = _recordListUiState.value.foreignCurrencyRecord.yenState.records.filter { it.date!! in event.startDate..event.endDate  }

                                val totalProfit = recordUseCase.sumProfit(
                                    record = dateRangeFilterRecord,
                                )

                                _recordListUiState.update { currentState ->
                                    currentState.copy(
                                        foreignCurrencyRecord = currentState.foreignCurrencyRecord.copy(
                                            dollarState = currentState.foreignCurrencyRecord.yenState.copy(
                                                totalProfit = totalProfit
                                            )
                                        )
                                    )
                                }
                            }
                        }
                    }
                }

                else -> return@launch
            }
        }
    }


    // MainViewModel.kt의 init 블록 내부에 추가 (getRecords() 호출 후)

    init {
        viewModelScope.launch {
            launch { receivedLatestRate() }
            launch { getRecords() }
            launch { calculateHoldingStats() } // ✅ 추가

            localUserExistCheck()
            noticeExistCheck()
            noticeDialogState()
            adDialogState()
            latestRateRepository.fetchInitialLatestRate()
        }
    }

// MainViewModel.kt 클래스 내부에 추가할 함수들

    /**
     * 보유중인 외화 통계 계산 및 업데이트
     */
    private suspend fun calculateHoldingStats() {
        combine(
            recordListUiState,
            mainUiState
        ) { recordState, mainState ->
            val dollarRecords = recordState.foreignCurrencyRecord.dollarState.records
                .filter { it.recordColor == false } // 보유중인 것만

            val yenRecords = recordState.foreignCurrencyRecord.yenState.records
                .filter { it.recordColor == false } // 보유중인 것만

            val currentUsdRate = mainState.recentRate.usd ?: "0"
            val currentJpyRate = mainState.recentRate.jpy ?: "0"

            HoldingStats(
                dollarStats = calculateCurrencyHolding(
                    records = dollarRecords,
                    currentRate = currentUsdRate,
                    currencyType = CurrencyType.USD
                ),
                yenStats = calculateCurrencyHolding(
                    records = yenRecords,
                    currentRate = currentJpyRate,
                    currencyType = CurrencyType.JPY
                )
            )
        }.collect { stats ->
            _mainUiState.update { it.copy(holdingStats = stats) }
        }
    }

    /**
     * 개별 통화의 보유 통계 계산
     */
    private fun calculateCurrencyHolding(
        records: List<ForeignCurrencyRecord>,
        currentRate: String,
        currencyType: CurrencyType
    ): CurrencyHoldingInfo {
        if (records.isEmpty() || currentRate == "0" || currentRate.isEmpty()) {
            return CurrencyHoldingInfo(hasData = false)
        }

        try {
            // 1. 총 투자금 계산
            val totalInvestment = records.sumOf {
                it.money?.replace(",", "")?.toBigDecimalOrNull() ?: BigDecimal.ZERO
            }

            // 2. 총 보유 외화량 계산
            val totalHoldingAmount = records.sumOf {
                it.exchangeMoney?.replace(",", "")?.toBigDecimalOrNull() ?: BigDecimal.ZERO
            }

            // 3. 가중 평균 매수가 계산
            var totalWeightedRate = BigDecimal.ZERO
            var totalWeight = BigDecimal.ZERO

            records.forEach { record ->
                val exchangeMoney = record.exchangeMoney?.replace(",", "")?.toBigDecimalOrNull() ?: BigDecimal.ZERO
                val buyRate = record.buyRate?.replace(",", "")?.toBigDecimalOrNull() ?: BigDecimal.ZERO

                if (exchangeMoney > BigDecimal.ZERO && buyRate > BigDecimal.ZERO) {
                    totalWeightedRate += buyRate.multiply(exchangeMoney)
                    totalWeight += exchangeMoney
                }
            }

            val averageRate = if (totalWeight > BigDecimal.ZERO) {
                totalWeightedRate.divide(totalWeight, 2, RoundingMode.HALF_UP)
            } else {
                BigDecimal.ZERO
            }

            // 4. 현재 환율로 예상 수익 계산
            val currentRateBD = currentRate.replace(",", "").toBigDecimalOrNull() ?: BigDecimal.ZERO

            val expectedProfit = when (currencyType) {
                CurrencyType.USD -> {
                    // (보유달러 × 현재환율) - 투자금
                    (totalHoldingAmount.multiply(currentRateBD)).minus(totalInvestment)
                }
                CurrencyType.JPY -> {
                    // (보유엔화 × 현재환율 ÷ 100) - 투자금
                    (totalHoldingAmount.multiply(currentRateBD).divide(BigDecimal(100), 2, RoundingMode.HALF_UP)).minus(totalInvestment)
                }
            }

            // 5. 수익률 계산
            val profitRate = if (totalInvestment > BigDecimal.ZERO) {
                (expectedProfit.divide(totalInvestment, 4, RoundingMode.HALF_UP).multiply(BigDecimal(100)))
                    .setScale(1, RoundingMode.HALF_UP)
            } else {
                BigDecimal.ZERO
            }

            return CurrencyHoldingInfo(
                averageRate = formatRate(averageRate),
                currentRate = formatRate(currentRateBD),
                totalInvestment = formatCurrency(totalInvestment),
                expectedProfit = formatCurrency(expectedProfit),
                profitRate = formatProfitRate(profitRate),
                holdingAmount = formatAmount(totalHoldingAmount, currencyType),
                hasData = true
            )

        } catch (e: Exception) {
            Log.e("MainViewModel", "보유 통계 계산 오류: ${e.message}", e)
            return CurrencyHoldingInfo(hasData = false)
        }
    }

    // 포맷팅 헬퍼 함수들
    private fun formatRate(rate: BigDecimal): String {
        return "%,.2f".format(rate)
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

    private fun formatProfitRate(rate: BigDecimal): String {
        return when {
            rate > BigDecimal.ZERO -> "+${rate}%"
            rate < BigDecimal.ZERO -> "${rate}%"
            else -> "0.0%"
        }
    }

    private fun formatAmount(amount: BigDecimal, type: CurrencyType): String {
        val formatted = "%,.2f".format(amount)
        return when (type) {
            CurrencyType.USD -> "$$formatted"
            CurrencyType.JPY -> "¥$formatted"
        }
    }

    override fun onCleared() {
        super.onCleared()
        Log.e(TAG("MainViewModel", "onCleared"), "━━━━━━━━━━━━━━━━━━━━━━━━━━")
        Log.e(TAG("MainViewModel", "onCleared"), "💀 ViewModel이 소멸됩니다!")
        Log.e(TAG("MainViewModel", "onCleared"), "ViewModel 인스턴스: ${this.hashCode()}")
        Log.e(TAG("MainViewModel", "onCleared"), "━━━━━━━━━━━━━━━━━━━━━━━━━━")
    }


}

val time = Calendar.getInstance().time

val formatter = SimpleDateFormat("yyyy-MM-dd")

val today = formatter.format(time)


    // 메인 화면의 핵심 상태
data class MainUiState (
        val selectedCurrencyType: CurrencyType = CurrencyType.USD,
        val selectedDate: String = today,
        val recentRate: ExchangeRate = ExchangeRate(),
        val localUser: LocalUserData = LocalUserData(),
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


// 알림 관련 상태
data class NoticeUiState(
    val showNoticeDialog: Boolean = false,
    val notice: Notice = Notice(),
    val noticeState: Boolean = true
)

// 광고 관련 상태
data class AdUiState(
    val rewardShowDialog: Boolean = false,
    val rewardAdState: Boolean = false,
    val bannerAdState: Boolean = false
)

data class TotalProfitRangeDate(
    val startDate: String = "",
    val endDate: String = ""
)

// 거래 기록 관련 상태
data class RecordListUiState(
    val foreignCurrencyRecord: ForeignCurrencyRecordList = ForeignCurrencyRecordList(),
    val selectedRecord: ForeignCurrencyRecord? = null,
    val sellRate: String = "",
    val sellProfit: String = "",
    val sellPercent: String = "",
    val refreshDate: String = "",
    val totalProfitRangeDate: TotalProfitRangeDate = TotalProfitRangeDate()
)

data class ForeignCurrencyRecordList(
    val dollarState: CurrencyRecordState<ForeignCurrencyRecord> = CurrencyRecordState(),
    val yenState: CurrencyRecordState<ForeignCurrencyRecord> = CurrencyRecordState()
)

data class CurrencyRecordState<T: ForeignCurrencyRecord>(  // BuyRecord 인터페이스로 제한
    val records: List<T> = emptyList(),
    val groupedRecords: Map<String, List<T>> = emptyMap(),
    val groups: List<String> = emptyList(),
    val totalProfit: String = "",
)

data class HoldingStats(
    val dollarStats: CurrencyHoldingInfo = CurrencyHoldingInfo(),
    val yenStats: CurrencyHoldingInfo = CurrencyHoldingInfo()
)

/**
 * 통화별 보유 정보
 */
data class CurrencyHoldingInfo(
    val averageRate: String = "0", // 평균 매수가
    val currentRate: String = "0", // 현재 환율
    val totalInvestment: String = "₩0", // 총 투자금
    val expectedProfit: String = "₩0", // 예상 수익
    val profitRate: String = "0.0%", // 수익률
    val holdingAmount: String = "0", // 보유 외화량
    val hasData: Boolean = false // 데이터 존재 여부
)

