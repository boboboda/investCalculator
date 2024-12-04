package com.bobodroid.myapplication.models.viewmodels

import android.util.Log
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetState
import androidx.compose.runtime.internal.illegalDecoyCallException
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bobodroid.myapplication.MainActivity.Companion.TAG
import com.bobodroid.myapplication.components.Dialogs.SellDialogEvent
import com.bobodroid.myapplication.lists.dollorList.RecordListEvent
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
import com.bobodroid.myapplication.models.datamodels.useCases.CurrencyRecordRequest
import com.bobodroid.myapplication.models.datamodels.useCases.RecordUseCase
import com.bobodroid.myapplication.models.datamodels.useCases.UserUseCases
import com.bobodroid.myapplication.screens.MainEvent
import com.bobodroid.myapplication.screens.PopupEvent
import com.bobodroid.myapplication.util.AdMob.AdManager
import com.bobodroid.myapplication.util.AdMob.AdUseCase
import com.bobodroid.myapplication.util.result.onError
import com.bobodroid.myapplication.util.result.onSuccess
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
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
) : ViewModel() {

    private val _mainUiState = MutableStateFlow(MainUiState())
    val mainUiState = _mainUiState.asStateFlow()

    private val _dateRangeUiState = MutableStateFlow(DateRangeUiState())
    val dateRangeUiState = _dateRangeUiState.asStateFlow()

    private val _noticeUiState = MutableStateFlow(NoticeUiState())
    val noticeUiState = _noticeUiState.asStateFlow()

    private val _adUiState = MutableStateFlow(AdUiState())
    val adUiState = _adUiState.asStateFlow()

    private val _recordListUiState = MutableStateFlow(RecordListUiState())
    val recordListUiState = _recordListUiState.asStateFlow()

    private val _mainSnackBarState = Channel<String>()
    val mainSnackBarState = _mainSnackBarState.receiveAsFlow()

    private val todayDate = MutableStateFlow("${LocalDate.now()}")

    val alarmPermissionState = MutableStateFlow(false)

    init {
        Log.d(TAG("MainViewModel", "init"), "MainViewModel 초기화 시작")
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
        viewModelScope.launch {
            localUserExistCheck()
            Log.d(TAG("MainViewModel", "init"), "로컬유저 확인완료")
            noticeExistCheck()
            noticeDialogState()
            Log.d(TAG("MainViewModel", "init"), "공지사항 확인완료")
            adDialogState()
            Log.d(TAG("MainViewModel", "init"), "광고 확인완료")
            receivedLatestRate()
            Log.d(TAG("MainViewModel", "init"), "최신환율 확인완료")
        }

        viewModelScope.launch {
            getRecords()
            Log.d(TAG("MainViewModel", "init"), "기록불러오기 확인완료")
        }
    }

    // 웹소켓 실시간 데이터 구독 -> 완료
    private suspend fun receivedLatestRate() {
        latestRateRepository.latestRateFlow.collect { latestRate ->
            Log.d(TAG("MainViewModel", "receivedLatestRate"), "실시간 데이터 수신: $latestRate")
            val uiState = _mainUiState.value.copy(
                recentRate = latestRate
            )
            _mainUiState.emit(uiState)
        }
    }


    // 공지사항 상태 초기화 -> 완료
    private suspend fun noticeDialogState() {
        // 저장한 날짜와 같으면 실행
        Log.d(
            TAG("MainViewModel", "noticeDialogState"),
            "공지사항 날짜: ${_noticeUiState.value.notice.date},  연기날짜: ${_mainUiState.value.localUser.userShowNoticeDate} 오늘날짜 ${
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
            }"
        )
        val noticeDate = _noticeUiState.value.notice.date
        val noticeContent = _noticeUiState.value.notice.content
        val userShowNoticeDate = _mainUiState.value.localUser.userShowNoticeDate

        if(noticeContent == null) {
            Log.d(TAG("MainViewModel", "noticeDialogState"), "공지가 없습니다.")
            return
        }

        // 닫기 후 호출 방지
        if (_noticeUiState.value.noticeState) {
            val uiState = _noticeUiState.value.copy(showNoticeDialog = true)
            if (userShowNoticeDate.isNullOrEmpty()) {
                Log.d(TAG("MainViewModel", "noticeDialogState"), "날짜 값이 없습니다.")
                _noticeUiState.emit(uiState)
            } else {
                // 안전한 널 검사 및 비교
                if (noticeDate != null && noticeDate > userShowNoticeDate) {
                    Log.d(TAG("MainViewModel", "noticeDialogState"), "공지사항 날짜가 더 큽니다.")
                    _noticeUiState.emit(uiState)
                } else {
                    Log.d(TAG("MainViewModel", "noticeDialogState"), "로컬에 저장된 날짜가 더 큽니다.")
                }
            }
        } else {
            return
        }
    }

    // 로컬유저 체크 -> 완료
    private fun localUserExistCheck() {
        viewModelScope.launch {
            val initUserdata = userRepository.waitForUserData()

            val uiState = _mainUiState.value.copy(localUser = initUserdata.localUserData)

            _mainUiState.emit(uiState)

        }

    }

    // 광고 다이얼로그 상태 -> 완료
    private suspend fun adDialogState() {
        val isReady = adManager.isRewardAdReady.first()

        if(isReady) {
            val shouldShowAd = adUseCase.processRewardAdState(
                _mainUiState.value.localUser,
                todayDate.value
            )
            if(shouldShowAd) {
                val uiStateUpdate = _adUiState.value.copy(rewardShowDialog = true)
                _adUiState.emit(uiStateUpdate)
            }
        }
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


    // 총수익 조회 범위 시작 날짜 선택 -> 완료
    fun inputStartDate(startDate: String){
       val uiState = _dateRangeUiState.value.copy(startDate = startDate)

        _dateRangeUiState.value = uiState
    }

    // 총수익 조회 범위 마지막 날짜 선택 -> 완료
    fun inputEndDate(endDate: String){
        val uiState = _dateRangeUiState.value.copy(endDate = endDate)

        _dateRangeUiState.value = uiState
    }

    // -> 완료
    fun reFreshProfit(reFreshClicked: (ExchangeRate) -> Unit) {
        val resentRate = _mainUiState.value.recentRate

        reFreshClicked.invoke(resentRate)
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
       val sellPercent = recordUseCase.sellPercent(exchangeMoney, krMoney).toString()

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

            MainEvent.ShowMainBottomSheet -> {
                _mainUiState.update {
                    it.copy(
                        showMainBottomSheet = true
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
                        showMainBottomSheet = false
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
                when(event.event) {
                    is PopupEvent.SnackBarEvent -> {
                        viewModelScope.launch {
                            _mainSnackBarState.send(event.event.message)
                        }
                    }
                    else -> return
                }

            }
            is MainEvent.EditBottomSheetEvent.Popup -> {
                when(event.event) {
                    is PopupEvent.SnackBarEvent -> {
                        viewModelScope.launch {
                            _mainSnackBarState.send(event.event.message)
                        }
                    }
                    else -> return
                }
            }
            is MainEvent.RateBottomSheetEvent.Popup -> {
                when(event.event) {
                    is PopupEvent.SnackBarEvent -> {
                        viewModelScope.launch {
                            _mainSnackBarState.send(event.event.message)
                        }
                    }
                    else -> return
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

                else -> return@launch
            }
        }
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
        val showMainBottomSheet: Boolean = false,
        val showGroupAddDialog: Boolean = false,
        val showDatePickerDialog: Boolean = false
)

// 날짜 검색 관련 상태
data class DateRangeUiState(
    val dateRangeDialog: Boolean = false,
    val startDate: String = "",
    val endDate: String = ""
)

// 알림 관련 상태
data class NoticeUiState(
    val showNoticeDialog: Boolean = false,
    val notice: Notice = Notice(),
    val noticeState: Boolean = false
)

// 광고 관련 상태
data class AdUiState(
    val rewardShowDialog: Boolean = false,
    val rewardAdState: Boolean = false,
    val bannerAdState: Boolean = false
)

// 거래 기록 관련 상태
data class RecordListUiState(
    val foreignCurrencyRecord: ForeignCurrencyRecordList = ForeignCurrencyRecordList(),
    val selectedRecord: ForeignCurrencyRecord? = null,
    val sellRate: String = "",
    val sellProfit: String = "",
    val sellPercent: String = ""
)

data class ForeignCurrencyRecordList(
    val dollarState: CurrencyRecordState<ForeignCurrencyRecord> = CurrencyRecordState(),
    val yenState: CurrencyRecordState<ForeignCurrencyRecord> = CurrencyRecordState()
)

data class CurrencyRecordState<T: ForeignCurrencyRecord>(  // BuyRecord 인터페이스로 제한
    val records: List<T> = emptyList(),
    val groupedRecords: Map<String, List<T>> = emptyMap(),
    val groups: List<String> = emptyList()
)


