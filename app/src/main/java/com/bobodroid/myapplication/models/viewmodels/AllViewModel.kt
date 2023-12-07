package com.bobodroid.myapplication.models.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bobodroid.myapplication.MainActivity
import com.bobodroid.myapplication.models.datamodels.ExchangeRate
import com.google.firebase.Firebase
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate

class AllViewModel: ViewModel() {

    var changeMoney = MutableStateFlow(1)

    val db = Firebase.firestore

    init {

        // 최신 환율 데이터 받아오기
        resentGetExchangeRate()
    }

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