package com.bobodroid.myapplication.util.AdMob

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.bobodroid.myapplication.MainActivity.Companion.TAG
import com.bobodroid.myapplication.models.datamodels.roomDb.LocalUserData
import com.bobodroid.myapplication.models.datamodels.useCases.LocalIdAddUseCase
import com.bobodroid.myapplication.models.datamodels.useCases.UserUseCases
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.GregorianCalendar
import javax.inject.Inject


class AdUseCase @Inject constructor(
  private val userUseCases: UserUseCases
) {

    // 단순히 비즈니스 로직만 처리 -> 리워드
    suspend fun processRewardAdState(user: LocalUserData, todayDate: String): Boolean {

        if (!user.rewardAdShowingDate.isNullOrEmpty()) {
            if (todayDate > user.rewardAdShowingDate!!) {
                return true  // 광고 표시 가능
            } else {
                return false
            }
        } else {
            return true  // 날짜 값이 없으면 광고 표시 가능
        }
    }

    suspend fun delayRewardAd(user: LocalUserData, todayDate: String) {
        val updatedUser = user.copy(
            rewardAdShowingDate = delayDate(inputDate = todayDate, delayDay = 1)
        )
        userUseCases.localUserUpdate(updatedUser)
    }

    fun bannerAdState(user: LocalUserData, todayDate: String): Boolean {
        Log.d(TAG("AdUseCase", "bannerAdState"), "배너 광고 제거 연기날짜: ${user.userResetDate} 오늘날짜: ${todayDate}")

        return user.userResetDate?.let { resetDate ->
            val isBeforeResetDate = todayDate <= resetDate
            Log.d(TAG("AdUseCase", "bannerAdState"),
                if (isBeforeResetDate) "연기된 날짜가 더 큽니다."
                else "오늘 날짜가 더 큽니다."
            )
            isBeforeResetDate
        } ?: run {
            Log.d(TAG("AdUseCase", "bannerAdState"), "날짜 값이 없습니다.")
            false
        }
    }

    // 배너 광고 제거 딜레이
    suspend fun deleteBannerDelayDate(user: LocalUserData, todayDate: String): Boolean {
        Log.d(TAG("AdUseCase", "deleteBannerDelayDate"), "날짜 연기 신청")

        val updateUserData = user.copy(
            userResetDate = delayDate(inputDate = todayDate, delayDay = 1)
        )
        userUseCases.localUserUpdate(updateUserData)

        Log.d(TAG("AllViewModel", "deleteBannerDelayDate"), "배너광고 삭제 딜레이리워드날짜: ${updateUserData.userResetDate}")

        return true
    }

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
}