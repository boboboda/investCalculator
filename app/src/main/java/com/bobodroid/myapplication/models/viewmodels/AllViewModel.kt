package com.bobodroid.myapplication.models.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bobodroid.myapplication.MainActivity.Companion.TAG
import com.bobodroid.myapplication.models.datamodels.roomDb.CloudUserData
import com.bobodroid.myapplication.models.datamodels.roomDb.DrBuyRecord
import com.bobodroid.myapplication.models.datamodels.roomDb.DrSellRecord
import com.bobodroid.myapplication.models.datamodels.roomDb.ExchangeRate
import com.bobodroid.myapplication.models.datamodels.repository.ExchangeRateRepository
import com.bobodroid.myapplication.models.datamodels.repository.LatestRateRepository
import com.bobodroid.myapplication.models.datamodels.repository.UserRepository
import com.bobodroid.myapplication.models.datamodels.roomDb.LocalUserData
import com.bobodroid.myapplication.models.datamodels.roomDb.RateType
import com.bobodroid.myapplication.models.datamodels.roomDb.TargetRates
import com.bobodroid.myapplication.models.datamodels.roomDb.WonBuyRecord
import com.bobodroid.myapplication.models.datamodels.roomDb.WonSellRecord
import com.bobodroid.myapplication.models.datamodels.roomDb.YenBuyRecord
import com.bobodroid.myapplication.models.datamodels.roomDb.YenSellRecord
import com.bobodroid.myapplication.models.datamodels.service.UserApi.Rate
import com.bobodroid.myapplication.models.datamodels.service.noticeApi.NoticeApi
import com.bobodroid.myapplication.models.datamodels.useCases.TargetRateUseCases
import com.bobodroid.myapplication.models.datamodels.useCases.UserUseCases
import com.bobodroid.myapplication.util.InvestApplication
import com.bobodroid.myapplication.models.datamodels.websocket.WebSocketClient
import com.bobodroid.myapplication.util.result.UiState
import com.bobodroid.myapplication.util.result.Result
import com.bobodroid.myapplication.util.result.onError
import com.bobodroid.myapplication.util.result.onSuccess
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.IOException
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
class AllViewModel @Inject constructor(
    private val userUseCases: UserUseCases,
    private val userRepository: UserRepository,
    private val latestRateRepository: LatestRateRepository
) : ViewModel() {

    private val _allUiState = MutableStateFlow(MainViewUiState())
    val allUiState = _allUiState.asStateFlow()


    private val _localUser = MutableStateFlow(LocalUserData())

    val localUserFlow = _localUser.asStateFlow()

    val noticeShowDialog = MutableStateFlow(false)

    val rewardShowDialog = MutableStateFlow(false)

    private val todayDateFlow = MutableStateFlow("${LocalDate.now()}")

    val openAppNoticeDateState = MutableStateFlow(true)

    private val noticeDateFlow = MutableStateFlow("")

    val noticeContent = MutableStateFlow("")

    val alarmPermissionState = MutableStateFlow(false)

    val onReadyRewardAd = MutableStateFlow(false)

    val deleteBannerStateFlow = MutableStateFlow(false)

    val rewardIsReadyStateFlow = MutableStateFlow(false)

    // 현재 날짜와 시각
    val nowDateTimeFlow = MutableStateFlow(LocalDateTime.now().toString())

    // 오늘 자정의 시각
    val midnightDateTimeFlow = MutableStateFlow(LocalDateTime.now().with(LocalTime.MIDNIGHT).toString())



    init {
        viewModelScope.launch {

            localUserExistCheck()

            recentRateHotListener()

            onReadyRewardAd.collect { isReady ->
                if (isReady) {
                    val uiState = _allUiState.value.copy(rewardIsReadyState = true)

                    _allUiState.emit(uiState)

                    rewardAdState()
                    bannerAdState()
                }
            }
        }
    }

    // 리워드 다이로그 상태 관리
    private fun rewardAdState() {

        Log.d(TAG("AllViewModel", "rewardAdState"), "연기날짜: ${_allUiState.value.localUser.rewardAdShowingDate} 오늘날짜: ${todayDateFlow.value}")

        // 닫기 후 호출 방지
        if (!_allUiState.value.localUser.rewardAdShowingDate.isNullOrEmpty()) {
            if (todayDateFlow.value > _allUiState.value.localUser.rewardAdShowingDate!!) {
                Log.d(TAG("AllViewModel", "rewardAdState"), "오늘 날짜가 더 큽니다.")
                rewardShowDialog.value = true

            } else {
                Log.d(TAG("AllViewModel", "rewardAdState"), "연기된 날짜가 더 큽니다.")

            }
        } else {
            Log.d(TAG("AllViewModel", "rewardAdState"), "날짜 값이 없습니다.")
            rewardShowDialog.value = true
        }
    }

    // 리워드 다이로그 연기 설정
    fun rewardDelayDate() {
        viewModelScope.launch {

            Log.d(TAG("AllViewModel", "rewardDelayDate"), "날짜 연기 신청")

            val updateUserData = _allUiState.value.localUser.copy(
                rewardAdShowingDate = delayDate(inputDate = todayDateFlow.value, delayDay = 1)
            )

            userUseCases.localUserUpdate(updateUserData)

            Log.d(TAG("AllViewModel", "rewardDelayDate"), "딜레이리워드날짜: ${updateUserData.rewardAdShowingDate}")
        }
    }

    //배너 삭제
    private fun bannerAdState() {

        Log.d(TAG("AllViewModel", "bannerAdState"), "배너 광고 제거 연기날짜: ${_allUiState.value.localUser.userResetDate} 오늘날짜: ${todayDateFlow.value}")

        // 닫기 후 호출 방지

        _allUiState.value.localUser.userResetDate?.let {
            if (todayDateFlow.value > _allUiState.value.localUser.userResetDate!!) {
                Log.d(TAG("AllViewModel", "bannerAdState"), "오늘 날짜가 더 큽니다.")
            } else {
                Log.d(TAG("AllViewModel", "bannerAdState"), "연기된 날짜가 더 큽니다.")
                deleteBannerStateFlow.value = true
            }
        } ?: run {
            Log.d(TAG("AllViewModel", "bannerAdState"), "날짜 값이 없습니다.")
        }
    }

    // 배너 광고 제거 딜레이
    fun deleteBannerDelayDate() {
        viewModelScope.launch {

            Log.d(TAG("AllViewModel", "deleteBannerDelayDate"), "날짜 연기 신청")

            val updateUserData = _allUiState.value.localUser.copy(
                userResetDate = delayDate(inputDate = todayDateFlow.value, delayDay = 1)
            )

            userUseCases.localUserUpdate(updateUserData)

            Log.d(TAG("AllViewModel", "deleteBannerDelayDate"), "배너광고 삭제 딜레이리워드날짜: ${updateUserData.userResetDate}")

            deleteBannerStateFlow.value = true

        }
    }

    // 공지사항 로드
    private fun noticeApi(noticeDate: (String) -> Unit) {
        viewModelScope.launch {

            try {
                val noticeResponse = NoticeApi.noticeService.noticeRequest()
                val res = noticeResponse.data.first()

                noticeContent.emit(res.content)
                noticeDate.invoke(res.createdAt)
                noticeDateFlow.emit(res.createdAt)
            } catch (error: IOException) {
                Log.e(TAG("AllViewModel", "noticeApi"), "$error")
                return@launch
            } catch (error: Exception) {
                Log.e(TAG("AllViewModel", "noticeApi"), "$error")
                return@launch
            }
        }
    }

    // 공지사항 상태 관리
    private fun noticeDialogState(localUserDate: LocalUserData, noticeDate: String) {

        // 저장한 날짜와 같으면 실행
        Log.d(
            TAG("AllViewModel", "noticeDialogState"),
            "공지사항 날짜: ${noticeDate},  연기날짜: ${localUserDate.userShowNoticeDate} 오늘날짜 ${
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
            }"
        )

        // 닫기 후 호출 방지
        if (openAppNoticeDateState.value) {
            if (!localUserDate.userShowNoticeDate.isNullOrEmpty()) {
                if (noticeDate > localUserDate.userShowNoticeDate!!) {
                    Log.d(TAG("AllViewModel", "noticeDialogState"), "공지사항 날짜가 더 큽니다.")
                    noticeShowDialog.value = true
                } else {
                    Log.d(TAG("AllViewModel", "noticeDialogState"), "로컬에 저장된 날짜가 더 큽니다.")
                }
            } else {
                Log.d(TAG("AllViewModel", "noticeDialogState"), "날짜 값이 없습니다.")
                noticeShowDialog.value = true
            }
        } else {
            return
        }
    }

    // 공지사항 연기 설정
    fun selectDelayDate(localUser: LocalUserData) {
        viewModelScope.launch {

            Log.d(TAG("AllViewModel", "selectDelayDate"), "날짜 연기 신청")

            val updateUserData = localUser.copy(
                userShowNoticeDate = noticeDateFlow.value
            )

            userUseCases.localUserUpdate(updateUserData)

            Log.d(TAG("AllViewModel", "selectDelayDate"), "날짜 수정 실행, ${noticeDateFlow.value}")
        }
    }


//    private fun dateWeek(week: Int): String? {
//        val c = GregorianCalendar()
//        c.add(Calendar.DAY_OF_WEEK, +week)
//        val sdfr = SimpleDateFormat("yyyy-MM-dd")
//        return sdfr.format(c.time).toString()
//    }
//
//
//    val delayDay = dateWeek(7)

    private fun delayDate(inputDate: String, delayDay: Int): String? {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd")
        val date: Date? = dateFormat.parse(inputDate)

        date?.let {
            val calendar = GregorianCalendar().apply {
                time = date
                add(Calendar.DAY_OF_MONTH, delayDay)
            }

            return dateFormat.format(calendar.time)
        }

        return null
    }


    private fun recentRateHotListener() {

        Log.d(TAG("AllViewModel", "recentRateHotListener"), "최신환율 실행")

        viewModelScope.launch {
            setupNotice(_allUiState.value.localUser)
            // 실시간 데이터 구독 시작
            receivedLatestRate()
        }
    }

    // 알림 관련 작업
    private fun setupNotice(localData: LocalUserData) {
        noticeApi { noticeDate ->
            noticeDialogState(localData, noticeDate)
        }
    }


    // 웹소켓 실시간 데이터 구독 -> 완료
    private suspend fun receivedLatestRate() {
        latestRateRepository.latestRateFlow.collect { latestRate ->
            val uiState = _allUiState.value.copy(
                recentRate = latestRate
            )
            _allUiState.emit(uiState)
        }
    }


    val dateStringFlow = MutableStateFlow("모두")

    val refreshDateFlow = MutableStateFlow("")

    // -> 완료
    fun inputStartDate(startDate: String){
       val uiState = _allUiState.value.copy(startDate = startDate)

        _allUiState.value = uiState
    }

    // -> 완료
    fun inputEndDate(endDate: String){
        val uiState = _allUiState.value.copy(endDate = endDate)

        _allUiState.value = uiState
    }

    // -> 완료
    fun reFreshProfit(reFreshClicked: (ExchangeRate) -> Unit) {
        val resentRate = _allUiState.value.recentRate

        reFreshClicked.invoke(resentRate)
    }

    // -> 완료
    fun createUser(customId: String, pin: String, resultMessage: (String) -> Unit) {
        viewModelScope.launch {
            userUseCases.customIdCreateUser(
                localUserData = _allUiState.value.localUser,
                customId = customId,
                pin = pin)
                .onSuccess { updateData, _ ->
                    val uiState = _allUiState.value.copy(localUser = updateData)

                    _allUiState.emit(uiState)
                    resultMessage("성공적으로 아이디가 생성되었습니다.")
                }
                .onError { _ ->
                    resultMessage("아이디 생성이 실패되었습니다.")
                }
        }
    }

    // -> 완료
    fun logIn(id: String, pin: String, successFind: (message: String) -> Unit) {
        viewModelScope.launch {
            userUseCases.logIn(_allUiState.value.localUser, id, pin)
                .onSuccess { localData, msg ->
                    val uiState = _allUiState.value.copy(localUser = localData)

                    _allUiState.emit(uiState)

                    successFind(msg ?: "")
                }
        }
    }

    // -> 완료
    fun logout(result: (message: String) -> Unit) {

        viewModelScope.launch {
            userUseCases.logout(_allUiState.value.localUser)
                .onSuccess { localData, msg ->

                    val uiState = _allUiState.value.copy(localUser = localData)

                    _allUiState.emit(uiState)

                    result(msg ?: "")
                }
        }
    }

    // -> 완료
    fun localUserExistCheck() {
        viewModelScope.launch {
          val initUserdata = userRepository.waitForUserData()

            val uiState = _allUiState.value.copy(localUser = initUserdata.localUserData)

            _allUiState.emit(uiState)

        }

    }

    // 로컬 아이디 삭제
    fun deleteLocalUser() {

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


}

data class MainViewUiState(
    val startDate: String = "",
    val endDate: String = "",
    val showNoticeDialog: Boolean = false,
    val rewardIsReadyState: Boolean = false,
    val noticeContent: String = "",
    val localUser: LocalUserData = LocalUserData(),
    val recentRate: ExchangeRate = ExchangeRate()
)