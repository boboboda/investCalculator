package com.bobodroid.myapplication.models.viewmodels

import android.util.Log
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
import com.bobodroid.myapplication.util.AdMob.AdManager
import com.bobodroid.myapplication.util.AdMob.AdUseCase
import com.bobodroid.myapplication.util.result.onError
import com.bobodroid.myapplication.util.result.onSuccess
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
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

    private val todayDate = MutableStateFlow("${LocalDate.now()}")

    val alarmPermissionState = MutableStateFlow(false)

    init {
        viewModelScope.launch {

            startInitialData()
        }
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

            Log.d(TAG("AllViewModel", "selectDelayDate"), "날짜 연기 신청")

            val updateUserData = _mainUiState.value.localUser.copy(
                userShowNoticeDate = _noticeUiState.value.notice.date
            )

            userUseCases.localUserUpdate(updateUserData)

            Log.d(TAG("AllViewModel", "selectDelayDate"), "날짜 수정 실행, ${_noticeUiState.value.notice.date}")
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
              noticeExistCheck()
            receivedLatestRate()
            noticeDialogState()
            adDialogState()

            getRecords()
        }
    }

    // 웹소켓 실시간 데이터 구독 -> 완료
    private suspend fun receivedLatestRate() {
        latestRateRepository.latestRateFlow.collect { latestRate ->
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
            TAG("AllViewModel", "noticeDialogState"),
            "공지사항 날짜: ${_noticeUiState.value.notice.date},  연기날짜: ${_mainUiState.value.localUser.userShowNoticeDate} 오늘날짜 ${
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
            }"
        )
        val noticeDate = _noticeUiState.value.notice.date
        val noticeContent = _noticeUiState.value.notice.content
        val userShowNoticeDate = _mainUiState.value.localUser.userShowNoticeDate

        if(noticeContent == null) {
            Log.d(TAG("AllViewModel", "noticeDialogState"), "공지가 없습니다.")
            return
        }

        // 닫기 후 호출 방지
        if (_noticeUiState.value.noticeState) {
            val uiState = _noticeUiState.value.copy(showNoticeDialog = true)
            if (userShowNoticeDate.isNullOrEmpty()) {
                Log.d(TAG("AllViewModel", "noticeDialogState"), "날짜 값이 없습니다.")
                _noticeUiState.emit(uiState)
            } else {
                // 안전한 널 검사 및 비교
                if (noticeDate != null && noticeDate > userShowNoticeDate) {
                    Log.d(TAG("AllViewModel", "noticeDialogState"), "공지사항 날짜가 더 큽니다.")
                    _noticeUiState.emit(uiState)
                } else {
                    Log.d(TAG("AllViewModel", "noticeDialogState"), "로컬에 저장된 날짜가 더 큽니다.")
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
        adManager.isRewardAdReady.collect { isReady ->
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
    }

    private suspend fun noticeExistCheck() {
        val noticeData = noticeRepository.waitForNoticeData()

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
                val uiState = _recordListUiState.value.copy(foreignCurrencyRecord = record)
                _recordListUiState.emit(uiState)
            }
        }
    }


    fun getCurrentRecords(): CurrencyRecordState<ForeignCurrencyRecord> {
        val currentState = _mainUiState.value
        val recordState = _recordListUiState.value
        return when(currentState.selectedCurrencyType) {
            CurrencyType.USD -> recordState.foreignCurrencyRecord.dollarState
            CurrencyType.JPY -> recordState.foreignCurrencyRecord.yenState }
    }

    fun addRecord(
        money: String,
        inputRate: String,
        groupName: String
    ) {

        val latestRate = when(mainUiState.value.selectedCurrencyType) {
            CurrencyType.USD -> _mainUiState.value.recentRate.usd
            CurrencyType.JPY -> _mainUiState.value.recentRate.jpy
        }

        if(latestRate == null) return

        val addRequest = CurrencyRecordRequest(
            latestRate = latestRate,
            money = money,
            inputRate = inputRate,
            groupName = groupName,
            date = _mainUiState.value.selectedDate,
            type = mainUiState.value.selectedCurrencyType,
        )
        viewModelScope.launch {
            recordUseCase.addCurrencyRecord(addRequest)
        }
    }


    fun updateCurrentForeignCurrency(currencyType: CurrencyType) {
        val updateUiState = _mainUiState.value.copy(selectedCurrencyType = currencyType)

        _mainUiState.value = updateUiState
    }

    fun selectRecordDate(date: String) {

        val uiState = _mainUiState.value.copy(selectedDate = date)

        _mainUiState.value = uiState
    }

   suspend fun sellCalculate(
        exchangeMoney: String,
        sellRate: String,
        krMoney: String) {

       val sellProfit = sellProfit(exchangeMoney, sellRate, krMoney).toString()
       val sellPercent = sellPercent(exchangeMoney, krMoney).toString()

       val recordUiUpdateState = _recordListUiState.value.copy(sellProfit = sellProfit, sellPercent = sellPercent)

       _recordListUiState.emit(recordUiUpdateState)

    }

    private fun sellProfit(
        exchangeMoney: String,
        sellRate: String,
        krMoney: String,
    ): BigDecimal {
        return when(_mainUiState.value.selectedCurrencyType) {
            CurrencyType.USD -> {
                ((BigDecimal(exchangeMoney).times(BigDecimal(sellRate))).setScale(20, RoundingMode.HALF_UP)) - BigDecimal(krMoney)
            }
            CurrencyType.JPY -> {
                ((BigDecimal(exchangeMoney).times(BigDecimal(sellRate))).setScale(20, RoundingMode.HALF_UP))/ BigDecimal("100") - BigDecimal(krMoney)
            }
        }
    }

    private fun sellPercent(
        exchangeMoney: String,
        krMoney: String
    ): Float =
        (exchangeMoney.toFloat() / krMoney.toFloat()) * 100f



    fun groupAdd(newGroupName: String) {
        val currentState = _recordListUiState.value

        val updatedState = when(mainUiState.value.selectedCurrencyType) {
            CurrencyType.USD -> {
                currentState.copy(
                    foreignCurrencyRecord = currentState.foreignCurrencyRecord.copy(
                        dollarState = currentState.foreignCurrencyRecord.dollarState.copy(
                            groups = currentState.foreignCurrencyRecord.dollarState.groups + newGroupName
                        )
                    )
                )
            }
            CurrencyType.JPY -> {
                currentState.copy(
                    foreignCurrencyRecord = currentState.foreignCurrencyRecord.copy(
                        yenState = currentState.foreignCurrencyRecord.yenState.copy(
                            groups = currentState.foreignCurrencyRecord.yenState.groups + newGroupName
                        )
                    )
                )
            }
        }

        viewModelScope.launch {
            _recordListUiState.emit(updatedState)
        }
    }


    suspend fun cancelSellRecord(id: UUID): Boolean {
       return recordUseCase.cancelSellRecord(id, mainUiState.value.selectedCurrencyType)
    }

    fun handleRecordEvent(event: RecordListEvent) {
        val uiState = _recordListUiState.value
        viewModelScope.launch {
            when (event) {
                is RecordListEvent.OnSellDialog -> {
                    when(event.event) {
                        is SellDialogEvent.SellRecord -> {

                        }
                        is SellDialogEvent.SellCalculate -> {

                            val exchangeMoney = event.record.exchangeMoney ?: return@launch
                            val money = event.record.money ?: return@launch

                            sellCalculate(
                                exchangeMoney = exchangeMoney,
                                sellRate = event.event.sellRate,
                                krMoney = money
                            )
                        }
                    }
                }
                is RecordListEvent.EditRecord -> {
                    recordUseCase.editRecord(
                        record = event.data,
                        editDate = event.date,
                        editMoney = event.money,
                        editRate = event.rate,
                        type = _mainUiState.value.selectedCurrencyType
                    )
                }
                is RecordListEvent.MemoUpdate -> {}
                    is RecordListEvent.RemoveRecord -> {}
                is RecordListEvent.CancelSellRecord -> {}
                is RecordListEvent.UpdateRecordCategory -> {}
                is RecordListEvent.AddGroup -> {}
            }
        }
    }


}


    // 메인 화면의 핵심 상태
data class MainUiState(
    val selectedCurrencyType: CurrencyType = CurrencyType.USD,
    val selectedDate: String = "",
    val recentRate: ExchangeRate = ExchangeRate(),
    val localUser: LocalUserData = LocalUserData()
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


