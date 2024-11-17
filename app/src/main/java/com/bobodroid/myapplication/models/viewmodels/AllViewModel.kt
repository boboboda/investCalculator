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
    private val targetRateUseCases: TargetRateUseCases,
//    private val exchangeRateRepository: ExchangeRateRepository,
    private val webSocketClient: WebSocketClient
) : ViewModel() {


    var changeMoney = MutableStateFlow(1)

    val nowBottomCardValue = MutableStateFlow(1)

    val db = Firebase.firestore

    val _localUser = MutableStateFlow(LocalUserData())
    val localUserFlow = _localUser.asStateFlow()

    val noticeShowDialog = MutableStateFlow(false)

    val rewardShowDialog = MutableStateFlow(false)

    val todayDateFlow = MutableStateFlow("${LocalDate.now()}")

    val openAppNoticeDateState = MutableStateFlow(true)

    private val noticeDateFlow = MutableStateFlow("")

    val noticeContent = MutableStateFlow("")

    val alarmPermissionState = MutableStateFlow(false)

    val _targetRate = MutableStateFlow(TargetRates())

    val targetRateFlow = _targetRate.asStateFlow()

    val onReadyRewardAd = MutableStateFlow(false)

    val deleteBannerStateFlow = MutableStateFlow(false)

    val rewardIsReadyStateFlow = MutableStateFlow(false)

    // 환율 관련
    // 항상 최신 값 가지고 있음
    val _recentExchangeRateFlow = MutableStateFlow(ExchangeRate())

    val recentExchangeRateFlow = _recentExchangeRateFlow.asStateFlow()

    val totalExchangeRate = MutableStateFlow<List<ExchangeRate>>(emptyList())

    // 현재 날짜와 시각
    val nowDateTimeFlow = MutableStateFlow(LocalDateTime.now().toString())

    // 오늘 자정의 시각
    val midnightDateTimeFlow = MutableStateFlow(LocalDateTime.now().with(LocalTime.MIDNIGHT).toString())



    init {
        viewModelScope.launch {

//            deleteLocalUser()

//            delay(2000L)

            localUserExistCheck()

            recentRateHotListener()

            onReadyRewardAd.collect { isReady ->
                if (isReady) {
                    rewardIsReadyStateFlow.value = true
                    rewardAdState()
                    bannerAdState()
                }
            }


        }
    }





    // 목표 환율 추가
    fun addTargetRate(
        type: RateType,
        rate: Rate) {
        viewModelScope.launch {
           targetRateUseCases.targetRateAddUseCase(
               deviceId = localUserFlow.value.id.toString(),
               targetRates = targetRateFlow.value,
               type = type,
               newRate = rate
           ).onSuccess {

           }.onError {

           }
        }
    }

//    // 목표 환율 삭제
//    fun targetRateRemove(
//        drHighRate: Rate? = null,
//        drLowRate: Rate? = null,
//        yenHighRate: Rate? = null,
//        yenLowRate: Rate? = null
//    ) {
//        if(drHighRate != null) {
//
//            val updateDollarRateList = targetRateFlow.value.dollarHighRateList?.toMutableList()?.apply {
//                remove(drHighRate)
//            }?.toList()
//
//            val removeRateData = targetRateFlow.value.copy(
//                customId = _localUser.value.customId,
//                fcmToken = _localUser.value.fcmToken,
//                dollarHighRateList = updateDollarRateList?.let { sortTargetRateList(type="달러고점", it) } as List<TargetRate>
//            )
//
//            //목표환율 api 추가 로직 !!!!!!!
//
//            viewModelScope.launch {
//                _targetRate.emit(removeRateData)
//            }
//        }
//
//        if(drLowRate != null) {
//            val updateDollarRateList = targetRateFlow.value.dollarLowRateList?.toMutableList()?.apply {
//                remove(drLowRate)
//            }?.toList()
//
//            val removeRateData = targetRateFlow.value.copy(
//                customId = _localUser.value.customId,
//                fcmToken = _localUser.value.fcmToken,
//                dollarLowRateList = updateDollarRateList?.let { sortTargetRateList(type="달러저점", it) } as List<TargetRate>
//            )
//
//            //목표환율 api 추가 로직 !!!!!!!
//
//            viewModelScope.launch {
//                _targetRate.emit(removeRateData)
//            }
//        }
//
//        if(yenHighRate != null) {
//            val updateYenRateList = targetRateFlow.value.yenHighRateList?.toMutableList()?.apply {
//                remove(yenHighRate)
//            }?.toList()
//
//            val removeRateData = targetRateFlow.value.copy(
//                customId = _localUser.value.customId,
//                fcmToken = _localUser.value.fcmToken,
//                yenHighRateList = updateYenRateList?.let { sortTargetRateList(type="엔화고점", it) } as List<TargetRate>
//            )
//
//            //목표환율 api 추가 로직 !!!!!!!
//
//            viewModelScope.launch {
//                _targetRate.emit(removeRateData)
//            }
//        }
//
//        if(yenLowRate != null) {
//            val updateYenRateList = targetRateFlow.value.yenLowRateList?.toMutableList()?.apply {
//                remove(yenLowRate)
//            }?.toList()
//
//            val removeRateData = targetRateFlow.value.copy(
//                customId = _localUser.value.customId,
//                fcmToken = _localUser.value.fcmToken,
//                yenLowRateList = updateYenRateList?.let { sortTargetRateList(type="엔화저점", it) } as List<TargetRate>
//            )
//
//            //목표환율 api 추가 로직 !!!!!!!
//
//            viewModelScope.launch {
//                _targetRate.emit(removeRateData)
//            }
//        }
//
//    }
//
//
//    // 목표 환율 정렬
//    fun sortTargetRateList(type: String, rateList: List<Any>): List<Any>  {
//
//        when(type) {
//            "달러고점" -> {
//                rateList as List<TargetRate>
//
//                var sortList = rateList.sortedByDescending { it.rate }
//
//                sortList = sortList.mapIndexed { index, element ->
//                    element.copy(number = "${index + 1}") }.toMutableList()
//
//                return sortList
//            }
//            "달러저점" -> {
//                rateList as List<TargetRate>
//
//                var sortList = rateList.sortedByDescending { it.rate }
//
//                sortList = sortList.mapIndexed { index, element ->
//                    element.copy(number = "${index + 1}") }.toMutableList()
//
//                return sortList
//            }
//            "엔화고점" -> {
//                rateList as List<TargetRate>
//
//                var sortList = rateList.sortedByDescending { it.rate }
//
//                sortList = sortList.mapIndexed { index, element ->
//                    element.copy(number = "${index + 1}") }.toMutableList()
//
//                return sortList
//            }
//            "엔화저점" -> {
//                rateList as List<TargetRate>
//
//                var sortList = rateList.sortedByDescending { it.rate }
//
//                sortList = sortList.mapIndexed { index, element ->
//                    element.copy(number = "${index + 1}") }.toMutableList()
//
//                return sortList
//            }
//
//
//            else -> return emptyList()
//        }
//    }
//
//
//    // 목표 환율 로딩
//    fun targetRateLoad(customId: String, targetRateData: (TargetRateList, resultMessage: String) -> Unit) {
//        db.collection("userTargetRate")
//            .whereEqualTo("customId", customId)
//            .addSnapshotListener { querySnapShot, e ->
//
//                if(querySnapShot != null) {
//                    val data = TargetRateList(querySnapShot)
//
//                    targetRateData.invoke(data, "알람설정을 성공적으로 불러왔습니다.")
//
//                    Log.d(TAG("AllViewModel", "targetRateLoad"), "목표환율 변경 수신${data}")
//                } else {
//                    Log.d(TAG("AllViewModel", "targetRateLoad"), "목표환율 null")
//                }
//
//                if (e != null) {
//                    Log.w(TAG("AllViewModel", "targetRateLoad"), "Listen failed.", e)
//                    return@addSnapshotListener
//                }
//
//            }
//    }


    // 리워드 다이로그 상태 관리
    private fun rewardAdState() {

        Log.d(TAG("AllViewModel", "rewardAdState"), "연기날짜: ${_localUser.value.rewardAdShowingDate} 오늘날짜: ${todayDateFlow.value}")

        // 닫기 후 호출 방지
        if (!_localUser.value.rewardAdShowingDate.isNullOrEmpty()) {
            if (todayDateFlow.value > _localUser.value.rewardAdShowingDate!!) {
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

            val updateUserData = localUserFlow.value.copy(
                rewardAdShowingDate = delayDate(inputDate = todayDateFlow.value, delayDay = 1)
            )

            userUseCases.localUserUpdate(updateUserData)

            Log.d(TAG("AllViewModel", "rewardDelayDate"), "딜레이리워드날짜: ${updateUserData.rewardAdShowingDate}")
        }
    }


    //배너 삭제
    private fun bannerAdState() {

        Log.d(TAG("AllViewModel", "bannerAdState"), "배너 광고 제거 연기날짜: ${_localUser.value.userResetDate} 오늘날짜: ${todayDateFlow.value}")

        // 닫기 후 호출 방지

        _localUser.value.userResetDate?.let {
            if (todayDateFlow.value > _localUser.value.userResetDate!!) {
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

            val updateUserData = localUserFlow.value.copy(
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
    fun noticeDialogState(localUserDate: LocalUserData, noticeDate: String) {

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


    fun dateWeek(week: Int): String? {
        val c = GregorianCalendar()
        c.add(Calendar.DAY_OF_WEEK, +week)
        val sdfr = SimpleDateFormat("yyyy-MM-dd")
        return sdfr.format(c.time).toString()
    }


    val delayDay = dateWeek(7)

    fun delayDate(inputDate: String, delayDay: Int): String? {
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


    fun recentRateHotListener() {

        Log.d(TAG("AllViewModel", "recentRateHotListener"), "최신환율 실행")

        viewModelScope.launch {
            setupNotice(localUserFlow.value)

            // 실시간 데이터 구독 시작
            subscribeToExchangeRateUpdates()
        }
    }

    // 알림 관련 작업
    private fun setupNotice(localData: LocalUserData) {
        noticeApi { noticeDate ->
            noticeDialogState(localData, noticeDate)
        }
    }

    // FCM 토큰 업데이트
    private fun updateFcmToken(localData: LocalUserData) {
        fcmTokenUpdate(localData)
    }

//    // 목표 환율 로드
//    private fun loadTargetRate(customId: String?) {
//        if (!customId.isNullOrEmpty()) {
//            targetRateLoad(customId) { data, message ->
//                viewModelScope.launch {
//                    _targetRate.emit(data)
//                    Log.w(TAG("AllViewModel", "loadTargetRate"), "목표환율 불러오기 성공")
//                }
//            }
//        }
//    }
//
//    fun deleteTotalRates() {
//        viewModelScope.launch {
//            exchangeRateRepository.exchangeRateDataDelete()
//        }
//    }


    // 웹소켓 실시간 데이터 구독
    private fun subscribeToExchangeRateUpdates() {
        webSocketClient.recentRateWebReceiveData(
            onInsert = { rateString ->
                Log.d(TAG("AllViewModel", "subscribeToExchangeRateUpdates"), "웹소켓 환율 최신 데이터: $rateString")
                onRateUpdate(rateString)
            },
            onInitialData = { initialData ->
                Log.d(TAG("AllViewModel", "subscribeToExchangeRateUpdates"), "웹소켓 환율 초기화 데이터: $initialData")
                onInitialDataReceived(initialData)
            }
        )
    }

    private fun onRateUpdate(rateString: String) {
        try {
            // rateString을 JSON으로 변환하고 ExchangeRate 객체 생성
            val exchangeRate = ExchangeRate.fromCustomJson(rateString) ?: return

            // 필요한 로직 처리 (예: 로컬 DB에 저장하거나 UI에 업데이트)
            viewModelScope.launch {
                _recentExchangeRateFlow.emit(exchangeRate)
            }

            Log.d(TAG("AllViewModel", "onRateUpdate"), "환율 업데이트 파싱 완료: $exchangeRate")

            // 데이터가 잘 파싱되었는지 확인하는 로그
            Log.d(TAG("AllViewModel", "onRateUpdate"), "USD 환율: ${exchangeRate.usd}, JPY 환율: ${exchangeRate.jpy}")

        } catch (e: Exception) {
            Log.e(TAG("AllViewModel", "onRateUpdate"), "환율 업데이트 파싱 실패: $rateString", e)
        }
    }

    // 초기 데이터 처리
    private fun onInitialDataReceived(initialData: String) {
        try {
            // initialData를 JSON으로 변환하고 ExchangeRate 객체 생성
            val exchangeRate = ExchangeRate.fromCustomJson(initialData) ?: return


            viewModelScope.launch {
                _recentExchangeRateFlow.emit(exchangeRate)
            }

            Log.d(TAG("AllViewModel", "onInitialDataReceived"), "초기 데이터 파싱 완료: $exchangeRate")

            // 데이터가 잘 파싱되었는지 확인하는 로그
            Log.d(TAG("AllViewModel", "onInitialDataReceived"), "초기 USD 환율: ${exchangeRate.usd}, 초기 JPY 환율: ${exchangeRate.jpy}")

        } catch (e: Exception) {
            Log.e(TAG("AllViewModel", "onInitialDataReceived"), "초기 데이터 파싱 실패: $initialData", e)
        }
    }


    private fun fcmTokenUpdate(localUser: LocalUserData) {

        val updateToken = InvestApplication.prefs.getData("fcm_token", "")

        Log.d(TAG("AllViewModel", "fcmTokenUpdate"), "토큰값${updateToken}")

        val updateLocalUserData = localUser.copy(fcmToken = updateToken)

        viewModelScope.launch {
//            userUseCases.localUserUpdate(updateLocalUserData)
        }



    }




    val startDateFlow = MutableStateFlow("")

    val endDateFlow = MutableStateFlow("")

    val dateStringFlow = MutableStateFlow("모두")

    val refreshDateFlow = MutableStateFlow("")


    fun reFreshProfit(reFreshClicked: (ExchangeRate) -> Unit) {
        val resentRate = recentExchangeRateFlow.value

        reFreshClicked.invoke(resentRate)
    }


    fun createUser(customId: String, pin: String, resultMessage: (String) -> Unit) {

        viewModelScope.launch {

            val createUser = _localUser.value.copy(customId = customId, pin =  pin)

            userUseCases.customIdCreateUser(createUser)
        }
    }

    fun logIn(id: String, pin: String, successFind: (message: String) -> Unit) {

        viewModelScope.launch {
            userUseCases.logIn(id, pin, successFind)
        }

    }

    fun logout(result: (message: String) -> Unit) {

        viewModelScope.launch {
            userUseCases.logout(result)
        }
    }


    fun localUserExistCheck() {
        viewModelScope.launch {
            try {
                when (val result = userUseCases.localExistCheck()) {


                    is Result.Success -> {
                        _localUser.value = result.data.localUserData

                        if(result.data.exchangeRates != null) {
                            _targetRate.value = result.data.exchangeRates
                        }

                        Log.d(TAG("AllViewModel", "localUserExistCheck"), "${result.message}")
                        Log.d(TAG("AllViewModel", "localUserExistCheck"), "${targetRateFlow.value}")

                    }
                    is Result.Error -> {
                        Log.d(TAG("AllViewModel", "localUserExistCheck"), "${result.message}")
                    }
                    else -> Unit
                }
            } catch (e: Exception) {
                Log.d(TAG("AllViewModel", "localUserExistCheck"), "유저 데이터 체크 중 오류 발생")
            }
        }
    }

    // 로컬 아이디 삭제
    fun deleteLocalUser() {
        viewModelScope.launch {
            when (val result = userUseCases.deleteUser()) {
                is Result.Success -> {
                    Log.d(TAG("AllViewModel", "deleteLocalUser"), "${result.message}")
                }
                is Result.Error -> {
                    Log.d(TAG("AllViewModel", "deleteLocalUser"), "${result.message}")
                }
                is Result.Loading -> {
                    // 로딩 처리
                }
            }
        }
    }



    private val formatTodayFlow = MutableStateFlow(
        LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
    )


    fun cloudSave(
        drBuyRecord: List<DrBuyRecord>,
        drSellRecord: List<DrSellRecord>,
        yenBuyRecord: List<YenBuyRecord>,
        yenSellRecord: List<YenSellRecord>,
        wonBuyRecord: List<WonBuyRecord>,
        wonSellRecord: List<WonSellRecord>
    ) {
        val localUser = _localUser.value

        val uploadCloudData = CloudUserData(
            id = localUser.id.toString(),
            customId = localUser.customId,
            createAt = formatTodayFlow.value,
            drBuyRecord = drBuyRecord,
            drSellRecord = drSellRecord,
            yenBuyRecord = yenBuyRecord,
            yenSellRecord = yenSellRecord,
            wonBuyRecord = wonBuyRecord,
            wonSellRecord = wonSellRecord

        )

        db.collection("userCloud").document(localUser.customId!!)
            .set(uploadCloudData.asHasMap())
    }


    fun cloudLoad(cloudId: String, cloudData: (CloudUserData, resultMessage: String) -> Unit) {

        db.collection("userCloud")
            .whereEqualTo("customId", cloudId)
            .get()
            .addOnSuccessListener { querySnapShot ->
                val data = CloudUserData(querySnapShot)

                cloudData.invoke(data, "클라우드 불러오기가 성공하였습니다.")
            }
    }


}