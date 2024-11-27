package com.bobodroid.myapplication.models.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bobodroid.myapplication.MainActivity.Companion.TAG
import com.bobodroid.myapplication.models.datamodels.roomDb.ExchangeRate
import com.bobodroid.myapplication.models.datamodels.repository.LatestRateRepository
import com.bobodroid.myapplication.models.datamodels.repository.Notice
import com.bobodroid.myapplication.models.datamodels.repository.NoticeRepository
import com.bobodroid.myapplication.models.datamodels.repository.UserRepository
import com.bobodroid.myapplication.models.datamodels.roomDb.CurrencyType
import com.bobodroid.myapplication.models.datamodels.roomDb.DrBuyRecord
import com.bobodroid.myapplication.models.datamodels.roomDb.DrSellRecord
import com.bobodroid.myapplication.models.datamodels.roomDb.LocalUserData
import com.bobodroid.myapplication.models.datamodels.roomDb.YenBuyRecord
import com.bobodroid.myapplication.models.datamodels.roomDb.YenSellRecord
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
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val userUseCases: UserUseCases,
    private val userRepository: UserRepository,
    private val latestRateRepository: LatestRateRepository,
    private val noticeRepository: NoticeRepository,
    private val adManager: AdManager,
    private val adUseCase: AdUseCase,
    private val recordUseCase: RecordUseCase
) : ViewModel() {

    private val _mainUiState = MutableStateFlow(MainViewUiState())
    val mainUiState = _mainUiState.asStateFlow()

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
        val uiState = _mainUiState.value.copy(rewardShowDialog = false)
        _mainUiState.value = uiState
    }


    // 공지사항 연기 설정
    fun selectDelayDate() {
        viewModelScope.launch {

            Log.d(TAG("AllViewModel", "selectDelayDate"), "날짜 연기 신청")

            val updateUserData = _mainUiState.value.localUser.copy(
                userShowNoticeDate = _mainUiState.value.notice.date
            )

            userUseCases.localUserUpdate(updateUserData)

            Log.d(TAG("AllViewModel", "selectDelayDate"), "날짜 수정 실행, ${_mainUiState.value.notice.date}")
        }
    }

    // 공지사항 닫기
    fun closeNotice() {
        val uiState = _mainUiState.value.copy(showNoticeDialog = false, noticeState = false)

        _mainUiState.value = uiState
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
            "공지사항 날짜: ${_mainUiState.value.notice.date},  연기날짜: ${_mainUiState.value.localUser.userShowNoticeDate} 오늘날짜 ${
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
            }"
        )
        val noticeDate = _mainUiState.value.notice.date
        val noticeContent = _mainUiState.value.notice.content
        val userShowNoticeDate = _mainUiState.value.localUser.userShowNoticeDate

        if(noticeContent == null) {
            Log.d(TAG("AllViewModel", "noticeDialogState"), "공지가 없습니다.")
            return
        }

        // 닫기 후 호출 방지
        if (_mainUiState.value.noticeState) {
            val uiState = _mainUiState.value.copy(showNoticeDialog = true)
            if (userShowNoticeDate.isNullOrEmpty()) {
                Log.d(TAG("AllViewModel", "noticeDialogState"), "날짜 값이 없습니다.")
                _mainUiState.emit(uiState)
            } else {
                // 안전한 널 검사 및 비교
                if (noticeDate != null && noticeDate > userShowNoticeDate) {
                    Log.d(TAG("AllViewModel", "noticeDialogState"), "공지사항 날짜가 더 큽니다.")
                    _mainUiState.emit(uiState)
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
                    val uiStateUpdate = _mainUiState.value.copy(rewardShowDialog = true)
                    _mainUiState.emit(uiStateUpdate)
                }
            }
        }
    }

    private suspend fun noticeExistCheck() {
        val noticeData = noticeRepository.waitForNoticeData()

        val uiState = _mainUiState.value.copy(notice = noticeData)

        _mainUiState.emit(uiState)
    }


    // 총수익 조회 범위 시작 날짜 선택 -> 완료
    fun inputStartDate(startDate: String){
       val uiState = _mainUiState.value.copy(startDate = startDate)

        _mainUiState.value = uiState
    }

    // 총수익 조회 범위 마지막 날짜 선택 -> 완료
    fun inputEndDate(endDate: String){
        val uiState = _mainUiState.value.copy(endDate = endDate)

        _mainUiState.value = uiState
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

    // 매도기록 선택된 날짜
    private val _selectedDate = MutableStateFlow("${LocalDate.now()}")

    private fun getRecords() {
        viewModelScope.launch {
            recordUseCase.getRecord().collect { record ->
                val uiState = _mainUiState.value.copy(foreignCurrencyRecord = record)
                _mainUiState.emit(uiState)
            }
        }
    }

    fun selectRecordDate(date: String) {
        _selectedDate.value = date
    }

    fun sellCalculate(
        exchangeMoney: String,
        sellRate: String,
        krMoney: String,
        currencyType: CurrencyType,
        sellResult:(sellProfit: String, sellPercent: String) -> Unit) {
        viewModelScope.launch {

           val currencyExchangeMoney = when(currencyType) {
                CurrencyType.USD -> {
                     exchangeMoney
                }
                CurrencyType.JPY -> {
                     exchangeMoney
                }
              }

           val sellProfit = sellProfit(currencyExchangeMoney, sellRate, krMoney, currencyType)
            val sellPercent = sellPercent(currencyExchangeMoney, krMoney)

            sellResult.invoke(sellProfit.toString(), sellPercent.toString())
        }
    }

    private fun sellProfit(
        exchangeMoney: String,
        sellRate: String,
        krMoney: String,
        currencyType: CurrencyType
    ): BigDecimal {
        return when(currencyType) {
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







}

data class MainViewUiState(
    val startDate: String = "",
    val endDate: String = "",
    val showNoticeDialog: Boolean = false,
    val rewardShowDialog: Boolean = false,
    val rewardAdState: Boolean = false,
    val bannerAdState: Boolean = false,
    val notice: Notice = Notice(),
    val noticeState: Boolean = false,
    val localUser: LocalUserData = LocalUserData(),
    val recentRate: ExchangeRate = ExchangeRate(),
    val foreignCurrencyRecord: ForeignCurrencyRecord = ForeignCurrencyRecord()
)

data class CurrencyRecordState<T>(
    val records: List<T> = emptyList(),
    val groupedRecords: Map<String, List<T>> = emptyMap(),
    val groups: List<String> = emptyList()
)

data class ForeignCurrencyRecord(
    val dollarState: CurrencyRecordState<DrBuyRecord> = CurrencyRecordState(),
    val yenState: CurrencyRecordState<YenBuyRecord> = CurrencyRecordState()
)