package com.bobodroid.myapplication.models.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bobodroid.myapplication.MainActivity.Companion.TAG
import com.bobodroid.myapplication.models.datamodels.service.exchangeRateApi.RateStatsApi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.io.IOException
import javax.inject.Inject

@HiltViewModel
class AnalysisViewModel@Inject constructor(): ViewModel() {


    companion object {
        val statsTAG = "분석 뷰모델"
    }

    init {
        rateStatsApi()
    }

    // 환율 통계 요청 Api
    private fun rateStatsApi() {
        viewModelScope.launch {

            try {
                val minuteRateStats = RateStatsApi.rateStatsService.getMinuteRates("2024-11-12")
                val dailyRateStats = RateStatsApi.rateStatsService.getDailyStats("2024-11-12")
                val monthRateStats = RateStatsApi.rateStatsService.getMonthlyStats(year = "2024", month = "11")


                Log.d(statsTAG, "분단위:${minuteRateStats.last().exchangeRates}")

                Log.d(statsTAG, "하루 단위:${dailyRateStats}")

                Log.d(statsTAG, "월 단위:${monthRateStats}")


            } catch (error: IOException) {
                Log.e(TAG, "$error")
                return@launch
            } catch (error: Exception) {
                Log.e(TAG, "$error")
                return@launch
            }
        }
    }
}