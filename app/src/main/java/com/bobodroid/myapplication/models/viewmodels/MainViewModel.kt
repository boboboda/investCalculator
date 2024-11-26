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
import com.bobodroid.myapplication.models.datamodels.roomDb.LocalUserData
import com.bobodroid.myapplication.models.datamodels.useCases.UserUseCases
import com.bobodroid.myapplication.util.result.onError
import com.bobodroid.myapplication.util.result.onSuccess
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
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
    private val noticeRepository: NoticeRepository
) : ViewModel() {

    private val _allUiState = MutableStateFlow(MainViewUiState())
    val allUiState = _allUiState.asStateFlow()


    val rewardShowDialog = MutableStateFlow(false)

    private val todayDateFlow = MutableStateFlow("${LocalDate.now()}")

    val alarmPermissionState = MutableStateFlow(false)

    val onReadyRewardAd = MutableStateFlow(false)

    private val deleteBannerStateFlow = MutableStateFlow(false)


    init {
        viewModelScope.launch {

            startInitialData()

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



    // 공지사항 연기 설정
    fun selectDelayDate() {
        viewModelScope.launch {

            Log.d(TAG("AllViewModel", "selectDelayDate"), "날짜 연기 신청")

            val updateUserData = _allUiState.value.localUser.copy(
                userShowNoticeDate = _allUiState.value.notice.date
            )

            userUseCases.localUserUpdate(updateUserData)

            Log.d(TAG("AllViewModel", "selectDelayDate"), "날짜 수정 실행, ${_allUiState.value.notice.date}")
        }
    }

    fun closeNotice() {
        val uiState = _allUiState.value.copy(showNoticeDialog = false, noticeState = false)

        _allUiState.value = uiState
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




    private fun startInitialData() {
        viewModelScope.launch {
           localUserExistCheck()
              noticeExistCheck()
            receivedLatestRate()
            noticeDialogState()
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


    // 공지사항 상태 초기화 -> 완료
    private suspend fun noticeDialogState() {
        // 저장한 날짜와 같으면 실행
        Log.d(
            TAG("AllViewModel", "noticeDialogState"),
            "공지사항 날짜: ${_allUiState.value.notice.date},  연기날짜: ${_allUiState.value.localUser.userShowNoticeDate} 오늘날짜 ${
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
            }"
        )
        val noticeDate = _allUiState.value.notice.date
        val noticeContent = _allUiState.value.notice.content
        val userShowNoticeDate = _allUiState.value.localUser.userShowNoticeDate

        if(noticeContent == null) {
            Log.d(TAG("AllViewModel", "noticeDialogState"), "공지가 없습니다.")
            return
        }

        // 닫기 후 호출 방지
        if (_allUiState.value.noticeState) {
            val uiState = _allUiState.value.copy(showNoticeDialog = true)
            if (userShowNoticeDate.isNullOrEmpty()) {
                Log.d(TAG("AllViewModel", "noticeDialogState"), "날짜 값이 없습니다.")
                _allUiState.emit(uiState)
            } else {
                // 안전한 널 검사 및 비교
                if (noticeDate != null && noticeDate > userShowNoticeDate) {
                    Log.d(TAG("AllViewModel", "noticeDialogState"), "공지사항 날짜가 더 큽니다.")
                    _allUiState.emit(uiState)
                } else {
                    Log.d(TAG("AllViewModel", "noticeDialogState"), "로컬에 저장된 날짜가 더 큽니다.")
                }
            }
        } else {
            return
        }
    }

    // -> 완료
    private fun localUserExistCheck() {
        viewModelScope.launch {
            val initUserdata = userRepository.waitForUserData()

            val uiState = _allUiState.value.copy(localUser = initUserdata.localUserData)

            _allUiState.emit(uiState)

        }

    }

    private fun noticeExistCheck() {
        viewModelScope.launch {
            val noticeData = noticeRepository.waitForNoticeData()

            val uiState = _allUiState.value.copy(notice = noticeData)

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
    val notice: Notice = Notice(),
    val noticeState: Boolean = false,
    val localUser: LocalUserData = LocalUserData(),
    val recentRate: ExchangeRate = ExchangeRate()
)