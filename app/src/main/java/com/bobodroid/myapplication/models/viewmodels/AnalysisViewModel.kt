package com.bobodroid.myapplication.models.viewmodels

import android.provider.Telephony.Mms.Rate
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bobodroid.myapplication.MainActivity.Companion.TAG
import com.bobodroid.myapplication.models.datamodels.service.exchangeRateApi.ExchangeRateResponse
import com.bobodroid.myapplication.models.datamodels.service.exchangeRateApi.RateApi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class AnalysisViewModel @Inject constructor(): ViewModel() {

    private val _dailyRates = MutableStateFlow<List<RateRange>>(emptyList())
    private val _weeklyRates = MutableStateFlow<List<RateRange>>(emptyList())
    private val _monthlyRates = MutableStateFlow<List<RateRange>>(emptyList())
    private val _yearlyRates = MutableStateFlow<List<RateRange>>(emptyList())

    // UI에서 현재 선택된 탭의 데이터를 보여주기 위한 State
    private val _selectedRates = MutableStateFlow<List<RateRange>>(emptyList())
    val selectedRates = _selectedRates.asStateFlow()

    // 현재 선택된 탭 인덱스
    private val _selectedTabIndex = MutableStateFlow(0)
    val selectedTabIndex = _selectedTabIndex.asStateFlow()

    init {
        // 초기 데이터 로드

        viewModelScope.launch {
            loadAllRangeData()

            withContext(Dispatchers.Main) {
                _selectedRates.value = _dailyRates.value
                Log.d("AnalysisViewModel", "Initial daily data set: ${_dailyRates.value}")
            }
        }

        // 탭 변경 시 해당하는 데이터로 selectedRates 업데이트

        viewModelScope.launch {
            _selectedTabIndex.collect { tabIndex ->
                _selectedRates.value = when(tabIndex) {
                    0 -> _dailyRates.value
                    1 -> _weeklyRates.value
                    2 -> _monthlyRates.value
                    3 -> _yearlyRates.value
                    else -> emptyList()
                }
            }

        }



    }

    private suspend fun loadAllRangeData() {
        try {
            // 각 기간별 데이터 로드
            loadDailyRates()
            loadWeeklyRates()
            loadMonthlyRates()
            loadYearlyRates()
        } catch (error: Exception) {
            Log.e(TAG("AnalysisViewModel", "loadAllRangeData"), "$error")
        }
    }

    private suspend fun loadDailyRates() {
        try {
            val (startDate, endDate) = rangeDateFromTab(0)
            val rates = RateApi.rateService.getRatesByPeriod("day", startDate, endDate)
            Log.d(TAG("AnalysisViewModel", "loadDailyRates"), "Received data: $rates")
            _dailyRates.value = filterAndMapRates(rates)
        } catch (error: Exception) {
            Log.e(TAG("AnalysisViewModel", "loadDailyRates"), "$error")
        }
    }

    private suspend fun loadWeeklyRates() {
        try {
            val (startDate, endDate) = rangeDateFromTab(1)
            val rates = RateApi.rateService.getRatesByPeriod("week", startDate, endDate)
            Log.d(TAG("AnalysisViewModel", "loadWeeklyRates"), "Received data: $rates")
            _weeklyRates.value = filterAndMapRates(rates)
        } catch (error: Exception) {
            Log.e(TAG("AnalysisViewModel", "loadWeeklyRates"), "$error")
        }
    }

    private suspend fun loadMonthlyRates() {
        try {
            val (startDate, endDate) = rangeDateFromTab(2)
            val rates = RateApi.rateService.getRatesByPeriod("month", startDate, endDate)
            Log.d(TAG("AnalysisViewModel", "loadMonthlyRates"), "Received data: $rates")
            _monthlyRates.value = filterAndMapRates(rates)
        } catch (error: Exception) {
            Log.e(TAG("AnalysisViewModel", "loadMonthlyRates"), "$error")
        }
    }

    private suspend fun loadYearlyRates() {
        try {
            val (startDate, endDate) = rangeDateFromTab(3)
            val rates = RateApi.rateService.getRatesByPeriod("year", startDate, endDate)
            Log.d(TAG("AnalysisViewModel", "loadYearlyRates"), "Received data: $rates")
            _yearlyRates.value = filterAndMapRates(rates)
        } catch (error: Exception) {
            Log.e(TAG("AnalysisViewModel", "loadYearlyRates"), "$error")
        }
    }
    private fun filterAndMapRates(rates: List<ExchangeRateResponse>): List<RateRange> {
        val filteredRates = rates.fold(mutableListOf<ExchangeRateResponse>()) { acc, current ->
            if (acc.isEmpty() ||
                acc.last().exchangeRates.jpy != current.exchangeRates.jpy ||
                acc.last().exchangeRates.usd != current.exchangeRates.usd
            ) {
                acc.add(current)
            } else {
                acc[acc.lastIndex] = current
            }
            acc
        }

        return filteredRates.map {
            RateRange(
                jpy = it.exchangeRates.jpy,
                usd = it.exchangeRates.usd,
                createAt = it.createAt
            )
        }
    }

    // UI에서 탭 변경 시 호출
    fun onTabSelected(index: Int) {
        _selectedTabIndex.value = index
    }

    // 데이터 새로고침이 필요한 경우
    fun refreshData() {
        viewModelScope.launch {
            loadAllRangeData()
        }

    }
}

private fun rangeDateFromTab(tabIndex: Int): Pair<String, String> {


    val today = LocalDate.now()
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    // 오늘 날짜
//    val todayDate = today.format(formatter)

    val todayDate = "2024-11-23"

    // 일주일 범위 (오늘부터 7일 전)
    val weekAgo = today.minusWeeks(1)
    val rangeWeek = weekAgo.format(formatter)

    // 3달 범위 (오늘부터 3달 전)
    val threeMonthsAgo = today.minusMonths(3)
    val rangeThreeMonths = threeMonthsAgo.format(formatter)

    // 1년 범위 (오늘부터 1년 전)
    val yearAgo = today.minusYears(1)
    val rangeYear = yearAgo.format(formatter)

    var endDate = ""
    var startDate = todayDate

    when(tabIndex) {
        0 -> {
            endDate = todayDate
        }
        1 -> {
            endDate = rangeWeek
        }
        2 -> {
            endDate = rangeThreeMonths
        }
        3 -> {
            endDate = rangeYear
        }
    }

    return Pair(startDate, endDate)
}

data class RateRange(
    val jpy: String = "",
    val usd: String = "",
    val createAt: String = "")

data class RateRangeCurrency(
    val rate: Float,
    val createAt: String
)