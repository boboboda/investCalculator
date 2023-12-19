package com.bobodroid.myapplication.models.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bobodroid.myapplication.MainActivity
import com.bobodroid.myapplication.MainActivity.Companion.TAG
import com.bobodroid.myapplication.models.datamodels.ExchangeRate
import com.bobodroid.myapplication.models.datamodels.InvestRepository
import com.bobodroid.myapplication.models.datamodels.LocalUserData
import com.bobodroid.myapplication.models.datamodels.Rate
import com.google.firebase.Firebase
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.firestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.Calendar
import java.util.GregorianCalendar
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class AllViewModel @Inject constructor(
    private val investRepository: InvestRepository
) : ViewModel() {

    var changeMoney = MutableStateFlow(1)

    val db = Firebase.firestore

    val localUserData = MutableStateFlow(LocalUserData())


    // 임시 더미 로컬 유저데이터 수정 필요
    fun localIdAdd(localUser: (LocalUserData) -> Unit ) {
        viewModelScope.launch {
                Log.d(TAG, "로컬 유저 생성 실행")

            val createLocalUser = LocalUserData(
                userShowNoticeDate = dateFlow.value,
                userResetDate = dateFlow.value,
                rateAdCount = 0,
                rateResetCount = 3
            )
                investRepository.localUserAdd(createLocalUser)

            localUser(createLocalUser)
        }
    }

    fun dateUpdate() {
        viewModelScope.launch {
            Log.d(TAG,"날짜 초기화")

            val user = localUserData.value

            val updateUserData = user.copy(
                userShowNoticeDate = dateFlow.value
            )
            Log.d(TAG, "오늘 날짜 수정 실행, ${dateFlow.value}")

            investRepository.localUserUpdate(updateUserData)
        }
    }


    fun selectDelayDate() {
        viewModelScope.launch {
            if(localUserData.value != null) {

                Log.d(TAG,"날짜 연기 신청")

                val user = localUserData.value

                val updateUserData = user.copy(
                    userShowNoticeDate = delayDayFlow.value
                )

                investRepository.localUserUpdate(updateUserData)

                Log.d(TAG, "날짜 수정 실행, ${delayDayFlow.value}")
            }
        }
    }

    val dateFlow = MutableStateFlow("${LocalDate.now()}")

    val noticeState = MutableStateFlow( true)
    fun dateWeek(week: Int): String? {
        val c = GregorianCalendar()
        c.add(Calendar.DAY_OF_WEEK, +week)
        val sdfr = SimpleDateFormat("yyyy-MM-dd")
        return sdfr.format(c.time).toString()
    }

    val delayDay = dateWeek(7)

    val delayDayFlow = MutableStateFlow("${delayDay}")



    // 20개 범위 최신 환율 가지고 있음
    val exChangeRateListFlow = MutableStateFlow<List<ExchangeRate>>(emptyList())


    // 최신 값 마지막 값 변동성 있는 값
    val exChangeRateFlow = MutableStateFlow(ExchangeRate())

    // 항상 최신 값 가지고 있음
    val recentExChangeRateFlow = MutableStateFlow(ExchangeRate())

    fun dateHourTime(hour: Int): String? {
        val c: java.util.Calendar = GregorianCalendar()
        c.add(java.util.Calendar.HOUR, - hour)
        val sdfr = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        return sdfr.format(c.time).toString()
    }

    fun recentRateHotListener( response: (ExchangeRate) -> Unit ) {

            db.collection("exchangeRates")
                .orderBy("createAt", Query.Direction.DESCENDING)
                .limit(20)
                .addSnapshotListener { snapshot, e ->
                    if (e != null) {
                        Log.w(TAG, "Listen failed.", e)
                        return@addSnapshotListener
                    }

                    if (snapshot != null) {

                        val dataList = mutableListOf<ExchangeRate>()

                        snapshot.forEach { queruSnapshot->
                            val data = ExchangeRate(queruSnapshot)

                            dataList.add(data)
                        }

                        Log.w(TAG, "들어온 데이터 ${dataList}")


                        localExistCheck { localUserData->

                            Log.w(TAG, "들어온 유저데이터 ${localUserData}")

                            if(localUserData.recentUsRate == null) {

                                Log.d(TAG, "rate 데이터가 없어서 데이터 넣어줌")

                                viewModelScope.launch {

                                    exChangeRateListFlow.emit(dataList)

                                    // 서버 초기 1시간 값 이전 값 넣어주기
                                    exChangeRateFlow.emit(dataList.last())

                                    recentExChangeRateFlow.emit(dataList.first())

                                    response(dataList.last())

                                    val localUser = localUserData

                                    val serverRateData = dataList.last()

                                    val updateData = localUser.copy(
                                        recentRateCreateAt = serverRateData.createAt,
                                        recentUsRate = serverRateData.exchangeRates?.usd,
                                        recentYenRate = serverRateData.exchangeRates?.jpy
                                    )

                                    investRepository.localUserUpdate(updateData)
                                }
                            } else {

                                val delayDate = dateFormat(
                                    serverDateCreateAt = dataList.last().createAt!!,
                                    localDateCreateAt = localUserData.recentRateCreateAt!!
                                )
                                if(delayDate) {

                                    Log.d(TAG, "새로고침된 환율이 한시간 차이나서 업데이트 함")

                                    viewModelScope.launch {

                                        exChangeRateListFlow.emit(dataList)

                                        // 서버 초기 1시간 값 이전 값 넣어주기
                                        exChangeRateFlow.emit(dataList.last())

                                        recentExChangeRateFlow.emit(dataList.first())

                                        response(dataList.last())

                                        val localUser = localUserData

                                        val serverRateData = dataList.last()

                                        val updateData = localUser.copy(
                                            recentRateCreateAt = serverRateData.createAt,
                                            recentUsRate = serverRateData.exchangeRates?.usd,
                                            recentYenRate = serverRateData.exchangeRates?.jpy
                                        )

                                        investRepository.localUserUpdate(updateData)
                                    }
                                } else {
                                    viewModelScope.launch {

                                        exChangeRateListFlow.emit(dataList)

                                        // 로컬 초기 1시간 값 이전 값 넣어주기

                                        val localRecentRate = ExchangeRate(
                                            createAt = localUserData.recentRateCreateAt,
                                            exchangeRates = Rate(
                                                jpy = localUserData.recentYenRate,
                                                usd = localUserData.recentUsRate
                                            )
                                        )

                                        exChangeRateFlow.emit(localRecentRate)

                                        recentExChangeRateFlow.emit(dataList.first())

                                        response(localRecentRate)
                                    }
                                }

                            }
                        }

                        // 새로고침 값이 1시간 이전보다 최근 값 보존하기 위한 코드

                    } else {
                        Log.d(TAG, "Current data: null")
                    }
                }
        }



    fun resetRate(rate:(ExchangeRate) -> Unit) {

        viewModelScope.launch {
            val resentRateList = exChangeRateListFlow.value

            rate(resentRateList.first())

            val localUser = localUserData.value

            val serverRateData = resentRateList.first()

            exChangeRateFlow.emit(serverRateData)

            val updateData = localUser.copy(
                recentRateCreateAt = serverRateData.createAt,
                recentUsRate = serverRateData.exchangeRates?.usd,
                recentYenRate = serverRateData.exchangeRates?.jpy
            )

            investRepository.localUserUpdate(updateData)

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

    fun localExistCheck (localData: (LocalUserData) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            investRepository.localUserDataGet().distinctUntilChanged()
                .collect(){userData ->
                    if(userData == null) {
                        localIdAdd {
                            localData(it)
                        }
                        Log.d(TAG, "로컬 유저아이디 없음 ${userData}") }

                    else {
                        Log.d(TAG, "가져온 유저 데이터 ${userData}")
                        localData(userData)
                        localUserData.emit(userData)
//                       dateUpdate()
                    }
                }
        }
    }




    val startDateFlow = MutableStateFlow("${LocalDate.now()}")

    val endDateFlow = MutableStateFlow("${LocalDate.now()}")

    val dateStringFlow = MutableStateFlow("모두")

}