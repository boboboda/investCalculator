package com.bobodroid.myapplication.models.viewmodels

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bobodroid.myapplication.MainActivity.Companion.TAG
import com.bobodroid.myapplication.extensions.toDate
import com.bobodroid.myapplication.extensions.toDateTime
import com.bobodroid.myapplication.models.datamodels.CloudUserData
import com.bobodroid.myapplication.models.datamodels.DrBuyRecord
import com.bobodroid.myapplication.models.datamodels.DrSellRecord
import com.bobodroid.myapplication.models.datamodels.ExchangeRate
import com.bobodroid.myapplication.models.datamodels.InvestRepository
import com.bobodroid.myapplication.models.datamodels.LocalUserData
import com.bobodroid.myapplication.models.datamodels.TargetRate
import com.bobodroid.myapplication.models.datamodels.TargetRateList
import com.bobodroid.myapplication.models.datamodels.WonBuyRecord
import com.bobodroid.myapplication.models.datamodels.WonSellRecord
import com.bobodroid.myapplication.models.datamodels.YenBuyRecord
import com.bobodroid.myapplication.models.datamodels.YenSellRecord
import com.bobodroid.myapplication.models.datamodels.service.NoticeApi
import com.bobodroid.myapplication.util.InvestApplication
import com.example.app.data.WebSocketClient
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.launch
import java.io.IOException
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
class AllViewModel @Inject constructor(
    private val investRepository: InvestRepository
) : ViewModel() {


    private val webSocketClient = WebSocketClient()

    var changeMoney = MutableStateFlow(1)

    val nowBottomCardValue = MutableStateFlow(1)

    val db = Firebase.firestore

    val localUserData = MutableStateFlow(LocalUserData())

    val noticeShowDialog = MutableStateFlow(false)

    val rewardShowDialog = MutableStateFlow(false)

    val todayDateFlow = MutableStateFlow("${LocalDate.now()}")

    val openAppNoticeDateState = MutableStateFlow(true)

    private val noticeDateFlow = MutableStateFlow("")

    val noticeContent = MutableStateFlow("")

    val alarmPermissionState = MutableStateFlow(false)

    val _targetRate = MutableStateFlow(TargetRateList())

    val targetRateFlow = _targetRate.asStateFlow()

    val onReadyRewardAd = MutableStateFlow(false)

    val deleteBannerStateFlow = MutableStateFlow(false)



    init{


        viewModelScope.launch(Dispatchers.IO) {
            webSocketClient.recentRateWebReceiveData(
                onInsert = { rateString ->
                    // 환율 데이터 처리 로직
                    Log.d(TAG, "Received exchange rate: $rateString")
                },
                onInitialData = { initialData ->
                    // 초기 데이터 처리 로직
                    Log.d(TAG, "Received initial data: $initialData")
                }
            )
        }



        viewModelScope.launch() {
            onReadyRewardAd.collect {isReady ->
                if(isReady) {

                    rewardIsReadyStateFlow.value = true

                    rewardAdState(localUserData.value)

                    bannerAdState(localUserData.value)
                }
            }
        }

        loadLocalRate()
    }

    val rewardIsReadyStateFlow = MutableStateFlow(false)

    // 목표 환율 추가
    fun targetRateAdd(
        drHighRate: TargetRate? = null,
        drLowRate: TargetRate? = null,
        yenHighRate: TargetRate? = null,
        yenLowRate: TargetRate? = null
    ) {
        if(drHighRate != null) {

            val updateDollarRateList = targetRateFlow.value.dollarHighRateList?.toMutableList()?.apply {
                add(drHighRate)
            }?.toList()

            val addRateData = targetRateFlow.value.copy(
                customId = localUserData.value.customId,
                fcmToken = localUserData.value.fcmToken,
                dollarHighRateList = updateDollarRateList?.let { sortTargetRateList(type="달러고점", it) } as List<TargetRate>
            )
            db.collection("userTargetRate").document(localUserData.value.customId!!)
                .set(addRateData.asHasMap())
            viewModelScope.launch {
                _targetRate.emit(addRateData)
            }
        }

        if(drLowRate != null) {
            val updateDollarRateList = targetRateFlow.value.dollarLowRateList?.toMutableList()?.apply {
                add(drLowRate)
            }?.toList()

            val addRateData = targetRateFlow.value.copy(
                customId = localUserData.value.customId,
                fcmToken = localUserData.value.fcmToken,
                dollarLowRateList = updateDollarRateList?.let { sortTargetRateList(type="달러저점", it) } as List<TargetRate>
            )
            db.collection("userTargetRate").document(localUserData.value.customId!!)
                .set(addRateData.asHasMap())
            viewModelScope.launch {
                _targetRate.emit(addRateData)
            }
        }

        if(yenHighRate != null) {
            val updateYenRateList = targetRateFlow.value.yenHighRateList?.toMutableList()?.apply {
                add(yenHighRate)
            }?.toList()

            val addRateData = targetRateFlow.value.copy(
                customId = localUserData.value.customId,
                fcmToken = localUserData.value.fcmToken,
                yenHighRateList = updateYenRateList?.let { sortTargetRateList(type="엔화고점", it) } as List<TargetRate>
            )
            db.collection("userTargetRate").document(localUserData.value.customId!!)
                .set(addRateData.asHasMap())
            viewModelScope.launch {
                _targetRate.emit(addRateData)
            }
        }

        if(yenLowRate != null) {
            val updateYenRateList = targetRateFlow.value.yenLowRateList?.toMutableList()?.apply {
                add(yenLowRate)
            }?.toList()

            val addRateData = targetRateFlow.value.copy(
                customId = localUserData.value.customId,
                fcmToken = localUserData.value.fcmToken,
                yenLowRateList = updateYenRateList?.let { sortTargetRateList(type="엔화저점", it) } as List<TargetRate>
            )
            db.collection("userTargetRate").document(localUserData.value.customId!!)
                .set(addRateData.asHasMap())
            viewModelScope.launch {
                _targetRate.emit(addRateData)
            }
        }

    }

    // 목표 환율 삭제
    fun targetRateRemove(
        drHighRate: TargetRate? = null,
        drLowRate: TargetRate? = null,
        yenHighRate: TargetRate? = null,
        yenLowRate: TargetRate? = null
    ) {
        if(drHighRate != null) {

            val updateDollarRateList = targetRateFlow.value.dollarHighRateList?.toMutableList()?.apply {
                remove(drHighRate)
            }?.toList()

            val removeRateData = targetRateFlow.value.copy(
                customId = localUserData.value.customId,
                fcmToken = localUserData.value.fcmToken,
                dollarHighRateList = updateDollarRateList?.let { sortTargetRateList(type="달러고점", it) } as List<TargetRate>
            )
            db.collection("userTargetRate").document(localUserData.value.customId!!)
                .set(removeRateData.asHasMap())
            viewModelScope.launch {
                _targetRate.emit(removeRateData)
            }
        }

        if(drLowRate != null) {
            val updateDollarRateList = targetRateFlow.value.dollarLowRateList?.toMutableList()?.apply {
                remove(drLowRate)
            }?.toList()

            val removeRateData = targetRateFlow.value.copy(
                customId = localUserData.value.customId,
                fcmToken = localUserData.value.fcmToken,
                dollarLowRateList = updateDollarRateList?.let { sortTargetRateList(type="달러저점", it) } as List<TargetRate>
            )
            db.collection("userTargetRate").document(localUserData.value.customId!!)
                .set(removeRateData.asHasMap())
            viewModelScope.launch {
                _targetRate.emit(removeRateData)
            }
        }

        if(yenHighRate != null) {
            val updateYenRateList = targetRateFlow.value.yenHighRateList?.toMutableList()?.apply {
                remove(yenHighRate)
            }?.toList()

            val removeRateData = targetRateFlow.value.copy(
                customId = localUserData.value.customId,
                fcmToken = localUserData.value.fcmToken,
                yenHighRateList = updateYenRateList?.let { sortTargetRateList(type="엔화고점", it) } as List<TargetRate>
            )
            db.collection("userTargetRate").document(localUserData.value.customId!!)
                .set(removeRateData.asHasMap())
            viewModelScope.launch {
                _targetRate.emit(removeRateData)
            }
        }

        if(yenLowRate != null) {
            val updateYenRateList = targetRateFlow.value.yenLowRateList?.toMutableList()?.apply {
                remove(yenLowRate)
            }?.toList()

            val removeRateData = targetRateFlow.value.copy(
                customId = localUserData.value.customId,
                fcmToken = localUserData.value.fcmToken,
                yenLowRateList = updateYenRateList?.let { sortTargetRateList(type="엔화저점", it) } as List<TargetRate>
            )
            db.collection("userTargetRate").document(localUserData.value.customId!!)
                .set(removeRateData.asHasMap())
            viewModelScope.launch {
                _targetRate.emit(removeRateData)
            }
        }

    }


    // 목표 환율 정렬
    fun sortTargetRateList(type: String, rateList: List<Any>): List<Any>  {

        when(type) {
            "달러고점" -> {
                rateList as List<TargetRate>

                var sortList = rateList.sortedByDescending { it.rate }

                sortList = sortList.mapIndexed { index, element ->
                    element.copy(number = "${index + 1}") }.toMutableList()

                return sortList
            }
            "달러저점" -> {
                rateList as List<TargetRate>

                var sortList = rateList.sortedByDescending { it.rate }

                sortList = sortList.mapIndexed { index, element ->
                    element.copy(number = "${index + 1}") }.toMutableList()

                return sortList
            }
            "엔화고점" -> {
                rateList as List<TargetRate>

                var sortList = rateList.sortedByDescending { it.rate }

                sortList = sortList.mapIndexed { index, element ->
                    element.copy(number = "${index + 1}") }.toMutableList()

                return sortList
            }
            "엔화저점" -> {
                rateList as List<TargetRate>

                var sortList = rateList.sortedByDescending { it.rate }

                sortList = sortList.mapIndexed { index, element ->
                    element.copy(number = "${index + 1}") }.toMutableList()

                return sortList
            }


            else -> return emptyList()
        }
    }


    // 목표 환율 로딩
    fun targetRateLoad(customId: String, targetRateData: (TargetRateList, resultMessage: String) -> Unit) {
        db.collection("userTargetRate")
            .whereEqualTo("customId", customId)
            .addSnapshotListener { querySnapShot, e ->

                if(querySnapShot != null) {
                    val data = TargetRateList(querySnapShot)

                    targetRateData.invoke(data, "알람설정을 성공적으로 불러왔습니다.")

                    Log.d(TAG, "목표환율 변경 수신${data}")
                } else {
                    Log.d(TAG, "목표환율 null")
                }

                if (e != null) {
                    Log.w(TAG, "Listen failed.", e)
                    return@addSnapshotListener
                }

            }
    }


    // 리워드 다이로그 상태 관리
    private fun rewardAdState(localUserDate: LocalUserData) {

        Log.d(TAG, "연기날짜: ${localUserDate.rewardAdShowingDate} 오늘날짜: ${todayDateFlow.value}")

        // 닫기 후 호출 방지
        if (!localUserDate.rewardAdShowingDate.isNullOrEmpty()) {
            if (todayDateFlow.value > localUserDate.rewardAdShowingDate!!) {
                Log.d(TAG, "오늘 날짜가 더 큽니다.")
                rewardShowDialog.value = true

            } else {
                Log.d(TAG, "연기된 날짜가 더 큽니다.")

            }
        } else {
            Log.d(TAG, "날짜 값이 없습니다.")
            rewardShowDialog.value = true
        }
    }

    // 리워드 다이로그 연기 설정
    fun rewardDelayDate(localUser: LocalUserData) {
        viewModelScope.launch {

            Log.d(TAG, "날짜 연기 신청")

            val updateUserData = localUser.copy(
                rewardAdShowingDate = delayDate(inputDate = todayDateFlow.value, delayDay = 1)
            )

            investRepository.localUserUpdate(updateUserData)

            Log.d(TAG, "딜레이리워드날짜: ${localUserData.value.rewardAdShowingDate}")
        }
    }


    //배너 삭제
    private fun bannerAdState(localUserDate: LocalUserData) {

        Log.d(TAG, "배너 광고 제거 연기날짜: ${localUserDate.userResetDate} 오늘날짜: ${todayDateFlow.value}")

        // 닫기 후 호출 방지

        localUserDate.userResetDate?.let {
            if (todayDateFlow.value > localUserDate.userResetDate!!) {
                Log.d(TAG, "오늘 날짜가 더 큽니다.")
            } else {
                Log.d(TAG, "연기된 날짜가 더 큽니다.")
                deleteBannerStateFlow.value = true
            }
        } ?: run {
            Log.d(TAG, "날짜 값이 없습니다.")
        }
    }


    // 배너 광고 제거 딜레이
    fun deleteBannerDelayDate(localUser: LocalUserData) {
        viewModelScope.launch {

            Log.d(TAG, "날짜 연기 신청")

            val updateUserData = localUser.copy(
                userResetDate = delayDate(inputDate = todayDateFlow.value, delayDay = 1)
            )

            investRepository.localUserUpdate(updateUserData)

            Log.d(TAG, "배너광고 삭제 딜레이리워드날짜: ${localUserData.value.userResetDate}")

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
                Log.e(TAG, "$error")
                return@launch
            } catch (error: Exception) {
                Log.e(TAG, "$error")
                return@launch
            }
        }
    }


    // 공지사항 상태 관리
    fun noticeDialogState(localUserDate: LocalUserData, noticeDate: String) {

        // 저장한 날짜와 같으면 실행
        Log.d(
            TAG,
            "공지사항 날짜: ${noticeDate},  연기날짜: ${localUserDate.userShowNoticeDate} 오늘날짜 ${
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
            }"
        )

        // 닫기 후 호출 방지
        if (openAppNoticeDateState.value) {
            if (!localUserDate.userShowNoticeDate.isNullOrEmpty()) {
                if (noticeDate > localUserDate.userShowNoticeDate!!) {
                    Log.d(TAG, "공지사항 날짜가 더 큽니다.")
                    noticeShowDialog.value = true
                } else {
                    Log.d(TAG, "로컬에 저장된 날짜가 더 큽니다.")
                }
            } else {
                Log.d(TAG, "날짜 값이 없습니다.")
                noticeShowDialog.value = true
            }
        } else {
            return
        }
    }


    // 공지사항 연기 설정
    fun selectDelayDate(localUser: LocalUserData) {
        viewModelScope.launch {

            Log.d(TAG, "날짜 연기 신청")

            val updateUserData = localUser.copy(
                userShowNoticeDate = noticeDateFlow.value
            )

            investRepository.localUserUpdate(updateUserData)

            Log.d(TAG, "날짜 수정 실행, ${noticeDateFlow.value}")
        }
    }


    // 로컬 아이디 생성
    fun localIdAdd(localUser: (LocalUserData) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            Log.d(TAG, "로컬 유저 생성 실행")

            val createLocalUser = LocalUserData(
                userResetDate = "",
                rateAdCount = 0,
                rateResetCount = 3
            )
            investRepository.localUserAdd(createLocalUser)

            investRepository.localUserDataGet().distinctUntilChanged()
                .collect { localUser ->
                    if (localUser.userResetDate.isNullOrEmpty()) {
                        Log.d(TAG, "LocalUserData null")
                    } else {
                        localUser(localUser)
                    }
                }


        }
    }



    fun dateReset() {
        viewModelScope.launch {
            Log.d(TAG, "날짜 초기화")

            val user = localUserData.value

            val updateUserData = user.copy(
                userShowNoticeDate = "",
                userResetDate = "",
                rewardAdShowingDate = ""


            )
            investRepository.localUserUpdate(updateUserData)
        }
    }

    // 로컬 아이디 삭제
    fun deleteLocalUser() {
        viewModelScope.launch {
            investRepository.localUserDataDelete()
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




    // 환율 관련
    // 항상 최신 값 가지고 있음
    val recentExChangeRateFlow = MutableStateFlow(ExchangeRate())

    val totalExchangeRate = MutableStateFlow<List<ExchangeRate>>(emptyList())

    // 현재 날짜와 시각
    val nowDateTimeFlow = MutableStateFlow(LocalDateTime.now().toString())

    // 오늘 자정의 시각
    val midnightDateTimeFlow = MutableStateFlow(LocalDateTime.now().with(LocalTime.MIDNIGHT).toString())


    // 최신 환율 불러오기
    fun recentRateHotListener(response: (ExchangeRate, LocalUserData) -> Unit) {
//        dateUpdate()

        viewModelScope.launch {

            localExistCheck { localData ->

                localUserData.value = localData

                Log.d(TAG, "localData = ${localUserData.value}")

//                resetChance(localData)

                noticeApi { noticeDate ->
                    noticeDialogState(localData, noticeDate)

                }

                fcmTokenUpdate(localData)

                viewModelScope.launch {
                    refreshDateFlow.emit(localData.reFreshCreateAt ?: "새로고침 정보가 없습니다.")
                }

                if(!localData.customId.isNullOrEmpty()) {
                    targetRateLoad(localData.customId!!) { data, message->
                        viewModelScope.launch {
                            _targetRate.emit(data)
                            Log.w(TAG, "목표환율 불러오기 성공")
                        }
                    }
                }

            }

            db.collection("exchangeRates")
                .orderBy("createAt", Query.Direction.DESCENDING)
                .limit(1)
                .addSnapshotListener { snapshot, e ->
                    if (e != null) {
                        Log.w(TAG, "Listen failed.", e)
                        return@addSnapshotListener
                    }

                    if (snapshot != null) {

                        val data = ExchangeRate.fromQuerySnapshot(snapshot)

                        Log.d(TAG, "서버에서 들어온 값 ${data}")

                        data?.let {
                            response(it, localUserData.value)

                            viewModelScope.launch {
                                recentExChangeRateFlow.emit(it)
                            }
                        }

                    } else {
                        Log.w(TAG, "Current data: null")
                        return@addSnapshotListener

                    }
                }

        }
    }


    fun deleteTotalRates() {
        viewModelScope.launch {
            investRepository.exchangeRateDataDelete()
        }
    }
    private fun fireStoreNowRateLoad(startOfDay: String, endOfDay: String, result:(List<ExchangeRate>) -> Unit) {

        viewModelScope.launch(Dispatchers.IO) {
            db.collection("exchangeRates")
                .whereGreaterThanOrEqualTo("createAt", startOfDay)
                .whereLessThan("createAt", endOfDay)
                .orderBy("createAt", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener { snapshot  ->
                    if (snapshot != null && !snapshot.isEmpty) {
                        val data = snapshot.documents.mapNotNull { doc ->
                            ExchangeRate.fromDocumentSnapshot(doc)
                        }

                        result(data)

                    } else {
                        Log.w(TAG, "Current data: null")
                    }
                }
                .addOnFailureListener { e ->
                    Log.w(TAG, "Data load failed: ", e)
                }
        }
    }
    fun firebaseRateLoad(loadFinished:(String) -> Unit) {

        // 토탈 데이터 마지막 날짜 시간, 오늘날짜 시간 비교
        // 오늘 날짜가 더 클 경우 오늘 자정부터, 현재시간 데이터 요청

        val totalRateLastDay = totalExchangeRate.value.lastOrNull()?.createAt

        val todayTime = nowDateTimeFlow.value.toDateTime()


        if(totalExchangeRate.value.isEmpty()) {
            totalRateLoad {
                viewModelScope.launch {
                    totalExchangeRate.emit(it)

                    investRepository.saveAllExchangeRates(it)

                    loadFinished("성공적으로 데이터를 불러왔습니다.")

                }
            }

        } else {

            totalRateLastDay?.let {
                if(todayTime > totalRateLastDay) {
                    fireStoreNowRateLoad(
                        startOfDay = totalRateLastDay,
                        endOfDay = todayTime) {
                        viewModelScope.launch {
                            investRepository.saveAllExchangeRates(it)
                            loadFinished("성공적으로 데이터를 불러왔습니다.")
                        }
                    }

                } else {
                    loadFinished("데이터가 최신값입니다.")
                }
            }
        }
    }

    private fun totalRateLoad(loadFinished:(List<ExchangeRate>) -> Unit){

        viewModelScope.launch(Dispatchers.IO) {
            db.collection("exchangeRates")
                .get()
                .addOnSuccessListener { querySnapShot ->
                    if (querySnapShot != null) {

                        Log.d(TAG, "서버에서 모든 데이터 불러오기 실행")

                        val exchangeRates = querySnapShot.map { ExchangeRate.fromQuerySnapshot(querySnapShot) }

                        Log.d(TAG, "서버에서 들어온 토탈 환율 값 갯수: ${exchangeRates.count()}")

                        loadFinished(exchangeRates.filterNotNull())
                    } else {
                        Log.w(TAG, "Current data: null")
                        return@addOnSuccessListener
                    }
                }
                .addOnFailureListener { e ->
                    Log.w(TAG, "Data load failed: ", e)
                }
        }


    }
    private fun loadLocalRate() {
        viewModelScope.launch(Dispatchers.IO) {
            investRepository.exchangeRateDataGet()
                .distinctUntilChanged()  // 중복 데이터 필터링
                .collect { localRateData ->  // collect로 데이터 처리
                    totalExchangeRate.emit(localRateData) // 변화된 데이터 emit
                    processRates()
                }
        }
    }

    private fun processRates() {
        val dateToRatesMap = mutableMapOf<String, MutableList<Float>>()

        // 데이터를 날짜별로 그룹화
        for (exchangeRate in totalExchangeRate.value) {
            val date = exchangeRate.createAt.toDate() ?: continue
            val rate = exchangeRate.jpy?.toFloat() ?: continue  // Rate 객체의 값을 얻는 부분은 Rate 클래스에 맞게 수정

            dateToRatesMap.getOrPut(date) { mutableListOf() }.add(rate)
        }

        // 날짜별로 최대값, 최소값, 날짜를 구하여 트리플 리스트로 변환
        val processedExchangeRates = dateToRatesMap.map { (date, rates) ->
            Triple(date, rates.minOrNull() ?: 0f, rates.maxOrNull() ?: 0f)
        }

        Log.d(TAG, "서버에서 들어온 가공된 값 $processedExchangeRates")
    }


    fun dateFormat(localDateCreateAt: String, serverDateCreateAt: String): Boolean {


        val serverDate = serverDateCreateAt!!.split(" ")[0]
        val serverTime = serverDateCreateAt!!.split(" ")[1]
        val localDate = localDateCreateAt!!.split(" ")[0]
        val localTime = localDateCreateAt!!.split(" ")[1]

        val serverDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        val serverDateObj = serverDateFormat.parse(serverDate + " " + serverTime)
        val localDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        val localDateObj = localDateFormat.parse(localDate + " " + localTime)

        val difference = serverDateObj.time - localDateObj.time

        // 시간 차이가 1시간 이상이면 true를 반환합니다.
        val diffHours = difference / (60 * 60 * 1000)

        return diffHours >= 1
    }

    private fun localExistCheck(localData: (LocalUserData) -> Unit) {

        viewModelScope.launch(Dispatchers.IO) {
            investRepository.localUserDataGet().distinctUntilChanged()
                .collect() { userData ->
                    Log.d(TAG, "로컬아이디 체크 실행")
                    if (userData == null) {
                        localIdAdd {
                            localData(it)
                        }

                        Log.d(TAG, "로컬 유저아이디 없음 ${userData}")
                    } else {
                        Log.d(TAG, "가져온 유저 데이터 ${userData}")
                        localData(userData)

                        localUserData.emit(userData)
                    }
                }
        }
    }


    val startDateFlow = MutableStateFlow("")

    val endDateFlow = MutableStateFlow("")

    val dateStringFlow = MutableStateFlow("모두")

    val refreshDateFlow = MutableStateFlow("")


    fun reFreshProfit(reFreshClicked: (ExchangeRate) -> Unit) {
        val resentRate = recentExChangeRateFlow.value

        reFreshClicked.invoke(resentRate)
    }



//    fun chargeChance() {
//        val localUser = localUserData.value
//
//        val payChance = localUser.rateAdCount
//
//        viewModelScope.launch {
//            val chargeChance = payChance!! + 1
//
//            val updateDate = localUser.copy(
//                rateAdCount = chargeChance
//            )
//            investRepository.localUserUpdate(updateDate)
//        }
//    }

//    private fun resetChance(localUser: LocalUserData) {
//        val lastResetDate = localUser.userResetDate!!
//
//        if (todayDateFlow.value > lastResetDate) {
//            Log.w(TAG, "무료기회 리셋 진행")
//            viewModelScope.launch {
//                val updateData = localUser.copy(
//                    userResetDate = todayDateFlow.value,
//                    rateResetCount = 3 // 사용자에게 다시 리셋 기회 제공
//                )
//                investRepository.localUserUpdate(updateData)
//            }
//        } else if (todayDateFlow.value == lastResetDate) {
//            Log.w(TAG, "오늘 이미 무료기회를 리셋 받았습니다.")
//        } else {
//            Log.w(TAG, "리셋 날짜가 미래로 설정되어 있습니다. 설정 오류가 있는지 확인하세요.")
//        }
//    }

    fun idCustom(customId: String, pin: String, resultMessage: (String) -> Unit) {
        // 파이어스토어 id 생성
        // 중복 생성 방지
        val userDocument = db.collection("user").document(customId)
        val checkUserdata = userDocument.get()

        checkUserdata.addOnSuccessListener { docSnapShot ->

            if (docSnapShot.exists()) {
                resultMessage.invoke("이미 생성된 아이디 입니다.")
            } else {
                //파이어 베이스에 생성
                val makeCustomId = LocalUserData(
                    customId = customId,
                    pin = pin
                )

                userDocument.set(makeCustomId)

                // 로컬에 생성
                val localUser = localUserData.value

                viewModelScope.launch {
                    Log.d(TAG, "localId custom 실행")

                    val updateDate = localUser.copy(
                        customId = customId
                    )

                    investRepository.localUserUpdate(updateDate)

                    resultMessage.invoke("서버에 아이디가 생성되었습니다.")
                }
            }
        }
    }

    fun logIn(id: String, pin: String, successFind: (message: String) -> Unit) {

        val localUser = localUserData.value

        val userDocument = db.collection("user").document(id)
        val checkUserdata = userDocument.get()

        checkUserdata.addOnSuccessListener { docSnapShot ->

            val userData = LocalUserData(docSnapShot)

            Log.w(TAG, "${userData}")

            if (docSnapShot.exists()) {
                if (userData.pin != pin) {
                    successFind.invoke("핀이 틀렸습니다.")
                } else {

                    viewModelScope.launch {
                        Log.d(TAG, "localId custom 실행")

                        val updateDate = localUser.copy(
                            customId = id
                        )
                        investRepository.localUserUpdate(updateDate)

                        successFind.invoke("로그인 되었습니다.")
                    }

                }
            } else {
                successFind.invoke("아이디가 없습니다.")
            }

        }

    }



    fun logout(result: (message: String) -> Unit) {

        val localUser = localUserData.value

        viewModelScope.launch {
            Log.d(TAG, "localId custom 실행")

            val updateDate = localUser.copy(
                customId = ""
            )
            investRepository.localUserUpdate(updateDate)

            result.invoke("로그아웃되었습니다.")
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
        val localUser = localUserData.value

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

    private fun fcmTokenUpdate(localUser: LocalUserData) {

        val updateToken = InvestApplication.prefs.getData("fcm_token", "")

        Log.d(TAG, "토큰값${updateToken}")

        val updateLocalUserData = localUser.copy(fcmToken = updateToken)

        viewModelScope.launch {
            investRepository.localUserUpdate(updateLocalUserData)
        }



    }
}