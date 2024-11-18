package com.bobodroid.myapplication.models.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bobodroid.myapplication.MainActivity.Companion.TAG
import com.bobodroid.myapplication.billing.BillingClientLifecycle.Companion.TAG
import com.bobodroid.myapplication.models.datamodels.useCases.UserUseCases
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FcmAlarmViewModel@Inject constructor(
    userUseCases: UserUseCases
): ViewModel()  {


    init {
        viewModelScope.launch {
            // 유저 데이터가 준비될 때까지 대기
            val userData = userUseCases.localExistCheck.waitForUserData()
            // 이후 초기화 작업 진행
            Log.d(TAG("FcmAlarmViewModel", "init") , "${userData}")
        }
    }

}