package com.bobodroid.myapplication.models.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bobodroid.myapplication.MainActivity.Companion.TAG
import com.bobodroid.myapplication.models.datamodels.ExchangeRate
import com.bobodroid.myapplication.models.datamodels.InvestRepository
import com.bobodroid.myapplication.models.datamodels.LocalUserData
import com.bobodroid.myapplication.models.datamodels.service.NoticeApi
import com.google.firebase.Firebase
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.firestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.GregorianCalendar
import javax.inject.Inject

@HiltViewModel
class AllViewModel @Inject constructor(
    private val investRepository: InvestRepository
) : ViewModel() {

    var changeMoney = MutableStateFlow(1)

    val db = Firebase.firestore

    val localUserData = MutableStateFlow(LocalUserData())

    val noticeShowDialog = MutableStateFlow(false)

    val todayDateFlow = MutableStateFlow("${LocalDate.now()}")

    private val noticeDateFlow = MutableStateFlow("")

    val noticeContent = MutableStateFlow("")
    private fun noticeApi( noticeDate:(String) -> Unit) {
        viewModelScope.launch {
            val noticeResponse = NoticeApi.noticeService.noticeRequest()

            val res = noticeResponse.data.first()

            noticeContent.emit(res.content)
            noticeDate.invoke(res.createdAt)
            noticeDateFlow.emit(res.createdAt)
        }
    }

    fun noticeDialogState(localUserDate: LocalUserData, noticeDate: String) {


        // 저장한 날짜와 같으면 실행
        Log.d(TAG, "공지사항 날짜: ${noticeDate},  연기날짜: ${localUserDate.userShowNoticeDate} 오늘날짜 ${LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))}"
        )

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

    }


    // 임시 더미 로컬 유저데이터 수정 필요
    fun localIdAdd(localUser: (LocalUserData) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            Log.d(TAG, "로컬 유저 생성 실행")

            val createLocalUser = LocalUserData(
                userResetDate = todayDateFlow.value,
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
                userResetState = ""


            )
            investRepository.localUserUpdate(updateUserData)
        }
    }

    fun selectDelayDate(localUser: LocalUserData) {
        viewModelScope.launch {
            if (localUser != null) {

                Log.d(TAG, "날짜 연기 신청")

                val updateUserData = localUser.copy(
                    userShowNoticeDate = noticeDateFlow.value
                )

                investRepository.localUserUpdate(updateUserData)

                Log.d(TAG, "날짜 수정 실행, ${noticeDateFlow.value}")
            }
        }
    }




    fun dateWeek(week: Int): String? {
        val c = GregorianCalendar()
        c.add(Calendar.DAY_OF_WEEK, +week)
        val sdfr = SimpleDateFormat("yyyy-MM-dd")
        return sdfr.format(c.time).toString()
    }

    val delayDay = dateWeek(7)

    val delayDayFlow = MutableStateFlow("${delayDay}")

    fun deleteLocalUser() {
        viewModelScope.launch {
            investRepository.localUserDataDelete()
        }
    }




    // 항상 최신 값 가지고 있음
    val recentExChangeRateFlow = MutableStateFlow(ExchangeRate())


    fun recentRateHotListener(response: (ExchangeRate, LocalUserData) -> Unit) {
//        dateUpdate()

        viewModelScope.launch {

            localExistCheck { localData ->

                localUserData.value = localData

                Log.d(TAG, "localData ${localUserData.value}")

                resetChance(localData)

                noticeApi { noticeDate ->
                    noticeDialogState(localData, noticeDate)
                }



                viewModelScope.launch {
                    refreshDateFlow.emit(localData.reFreshCreateAt ?: "새로고침 정보가 없습니다.")
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

                        val data = ExchangeRate(snapshot)

                        Log.d(TAG, "서버에서 들어온 값 ${data}")

                        response(data, localUserData.value)

                        viewModelScope.launch {
                            recentExChangeRateFlow.emit(data)
                        }

                    } else {
                        Log.d(TAG, "Current data: null")
                    }
                }

        }
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

    val mainDateState = MutableStateFlow(true)

    val refreshDateFlow = MutableStateFlow("")


    fun useItem(
        useChance: (Boolean, ExchangeRate) -> Unit
    ) {

        val resentRate = recentExChangeRateFlow.value

        val localUser = localUserData.value

        val freeChance = localUser.rateResetCount
        val payChance = localUser.rateAdCount

        // 리셋되어 차감된 데이터 가져옴
        Log.e(TAG, "데이터 체크${localUser}")

        // 기회 모두 소진한 경우
        if (freeChance == 0 && payChance == 0) {

            viewModelScope.launch {
                Log.d(TAG, "useItem 기회 모두 소진한 경우")

                val updateDate = localUser.copy(
                    reFreshCreateAt = resentRate.createAt
                )

                refreshDateFlow.emit(resentRate.createAt!!)

                investRepository.localUserUpdate(updateDate)

                useChance.invoke(true, resentRate)
            }
        } else {
            // 무료 기회만 소진한 경우
            if (freeChance == 0) {
                //유료 기회 사용 로직

                viewModelScope.launch {

                    val usePayChange = payChance!! - 1

                    Log.d(TAG, "useItem 무료기회만 소진한 경우 ${usePayChange}")

                    val updateDate = localUser.copy(
                        rateAdCount = usePayChange,
                        reFreshCreateAt = resentRate.createAt
                    )

                    refreshDateFlow.emit(resentRate.createAt!!)

                    investRepository.localUserUpdate(updateDate)

                    useChance.invoke(false, resentRate)
                }

            } else {

                viewModelScope.launch {

                    val useFreeChance = freeChance!! - 1

                    Log.d(TAG, "useItem 무료기회 차감 로직 ${useFreeChance}")

                    val updateDate = localUser.copy(
                        rateResetCount = useFreeChance,
                        reFreshCreateAt = resentRate.createAt
                    )

                    refreshDateFlow.emit(resentRate.createAt!!)

                    investRepository.localUserUpdate(updateDate)


                    useChance.invoke(false, resentRate)
                }

            }
        }

    }

    fun chargeChance() {
        val localUser = localUserData.value

        val payChance = localUser.rateAdCount

        viewModelScope.launch {
            val chargeChance = payChance!! + 1

            val updateDate = localUser.copy(
                rateAdCount = chargeChance
            )
            investRepository.localUserUpdate(updateDate)
        }
    }

    private fun resetChance(localUser: LocalUserData) {

        val resetDate = localUser.userResetDate

        val resetState = localUser.userResetState


        if (todayDateFlow.value >= resetDate!!) {
            Log.w(TAG, "리셋 받을 날짜가 오늘이랑 같거나 큰 경우")
            if (todayDateFlow.value == resetState) {
                Log.w(TAG, "무료기회가 리셋된 기기입니다.")
            } else {

                Log.w(TAG, "무료기회 리셋 진행")
                viewModelScope.launch {
                    val updateData = localUser.copy(
                        userResetDate = todayDateFlow.value,
                        userResetState = todayDateFlow.value,
                        rateResetCount = 3
                    )

                    investRepository.localUserUpdate(updateData)
                }
            }
        } else {
            Log.w(TAG, "리셋 에러")
            return
        }
    }

    fun localIdCustom(customId: String) {

        val localUser = localUserData.value

        viewModelScope.launch {
            Log.d(TAG, "localId custom 실행")

            val updateDate = localUser.copy(
                customId = customId
            )

            investRepository.localUserUpdate(updateDate)
        }

    }




}