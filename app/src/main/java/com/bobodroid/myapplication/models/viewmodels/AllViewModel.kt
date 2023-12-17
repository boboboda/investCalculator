package com.bobodroid.myapplication.models.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bobodroid.myapplication.MainActivity
import com.bobodroid.myapplication.MainActivity.Companion.TAG
import com.bobodroid.myapplication.models.datamodels.ExchangeRate
import com.bobodroid.myapplication.models.datamodels.InvestRepository
import com.bobodroid.myapplication.models.datamodels.LocalUserData
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

    val localUserData = MutableStateFlow<LocalUserData>(LocalUserData())
    init {
        // 최신 환율 데이터 받아오기
        resentGetExchangeRate()

        //로컬 ID 생성
        viewModelScope.launch(Dispatchers.IO) {
            investRepository.localUserDataGet().distinctUntilChanged()
                .collect(){userData ->
                    if(userData == null) {
                        localIdAdd()
                        Log.d(TAG, "로컬 유저아이디 없음 ${userData}") }
                    else {
                        Log.d(TAG, "가져온 유저 데이터 ${userData}")
                        localUserData.emit(userData)
//                        dateUpdate()
                    }
                }
        }
    }

    // 임시 더미 로컬 유저데이터 수정 필요
    fun localIdAdd() {
        viewModelScope.launch {
                Log.d(TAG, "로컬 유저 생성 실행")
                investRepository.localUserAdd(LocalUserData(
                    userShowNoticeDate = dateFlow.value,
                    userResetDate = dateFlow.value,
                    rateAdCount = 0,
                    rateResetCount = 3
                ))
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


    val exchangeRateFlow = MutableStateFlow<ExchangeRate>(ExchangeRate())

    fun resentGetExchangeRate() {

        db.collection("exchangeRates")
            .orderBy("createAt", Query.Direction.DESCENDING)
            .limit(1)
            .get()
            .addOnSuccessListener {
                val data = ExchangeRate(it)
                Log.d(MainActivity.TAG, "데이터 받아오기 성공 ${data}")
                viewModelScope.launch {
                    exchangeRateFlow.emit(data)
                }
            }
            .addOnFailureListener {e->
                Log.d(MainActivity.TAG, "데이터 받아오기 실패 ${e}")
            }
    }

    val startDateFlow = MutableStateFlow("${LocalDate.now()}")

    val endDateFlow = MutableStateFlow("${LocalDate.now()}")

    val dateStringFlow = MutableStateFlow("모두")

}