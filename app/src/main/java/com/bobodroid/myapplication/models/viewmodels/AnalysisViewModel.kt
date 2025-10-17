package com.bobodroid.myapplication.models.viewmodels

import android.util.Log
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bobodroid.myapplication.MainActivity.Companion.TAG
import com.bobodroid.myapplication.models.datamodels.service.exchangeRateApi.CurrencyChange
import com.bobodroid.myapplication.models.datamodels.service.exchangeRateApi.ExchangeRateResponse
import com.bobodroid.myapplication.models.datamodels.service.exchangeRateApi.ExchangeRates
import com.bobodroid.myapplication.models.datamodels.service.exchangeRateApi.RateApi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class AnalysisViewModel @Inject constructor(): ViewModel() {

    private val _dailyRates = MutableStateFlow<List<RateRange>>(emptyList())
    private val _weeklyRates = MutableStateFlow<List<RateRange>>(emptyList())
    private val _monthlyRates = MutableStateFlow<List<RateRange>>(emptyList())
    private val _yearlyRates = MutableStateFlow<List<RateRange>>(emptyList())

    private val _analysisUiState = MutableStateFlow<AnalysisUiState>(AnalysisUiState())
    val analysisUiState = _analysisUiState.asStateFlow()

    init {
        // ✅ 초기 데이터 로드는 IO 스레드에서
        viewModelScope.launch(Dispatchers.IO) {
            loadAllRangeData()
            loadDailyRates()
            loadDailyCharge()

            // ✅ UI 업데이트는 Main 스레드로 전환
            withContext(Dispatchers.Main) {
                _analysisUiState.update { currentUiState ->
                    currentUiState.copy(
                        selectedRates = _dailyRates.value
                    )
                }
            }

            Log.d(TAG("AnalysisViewModel", "init"), "Initial daily data set: ${_dailyRates.value}")
            Log.d(TAG("AnalysisViewModel", "init"), "Initial selectedRates data set: ${_analysisUiState.value.selectedRates}")
        }

        // ✅ collect는 별도 코루틴으로
        viewModelScope.launch {
            _analysisUiState.collect { uiState ->
                Log.d(TAG("AnalysisViewModel", "collectTabIndex"),
                    "Selected tab index: ${uiState.selectedTabIndex}, Current selectedRates: ${uiState.selectedRates}")

                val newSelectedRates = when(uiState.selectedTabIndex) {
                    0 -> _dailyRates.value
                    1 -> _weeklyRates.value
                    2 -> _monthlyRates.value
                    3 -> _yearlyRates.value
                    else -> emptyList()
                }

                if (newSelectedRates != uiState.selectedRates) {
                    _analysisUiState.update { current ->
                        current.copy(selectedRates = newSelectedRates)
                    }
                }
            }
        }
    }

    // ✅ 모든 네트워크 작업은 IO 스레드에서 실행
    private suspend fun loadAllRangeData() = withContext(Dispatchers.IO) {
        try {
            loadDailyRates()
            loadWeeklyRates()
            loadMonthlyRates()
            loadYearlyRates()
        } catch (error: Exception) {
            Log.e(TAG("AnalysisViewModel", "loadAllRangeData"), "$error")
        }
    }

    private suspend fun loadDailyRates() = withContext(Dispatchers.IO) {
        try {
            val (startDate, endDate) = rangeDateFromTab(0)
            val rates = RateApi.rateService.getRatesByPeriod("day", startDate, endDate)
            Log.d(TAG("AnalysisViewModel", "loadDailyRates"), "Received data: $rates")
            _dailyRates.value = filterAndMapRates(rates)
        } catch (error: Exception) {
            Log.e(TAG("AnalysisViewModel", "loadDailyRates"), "$error")
        }
    }

    private suspend fun loadWeeklyRates() = withContext(Dispatchers.IO) {
        try {
            val (startDate, endDate) = rangeDateFromTab(1)
            val rates = RateApi.rateService.getRatesByPeriod("week", startDate, endDate)
            Log.d(TAG("AnalysisViewModel", "loadWeeklyRates"), "Received data: $rates")
            _weeklyRates.value = filterAndMapRates(rates)
        } catch (error: Exception) {
            Log.e(TAG("AnalysisViewModel", "loadWeeklyRates"), "$error")
        }
    }

    private suspend fun loadMonthlyRates() = withContext(Dispatchers.IO) {
        try {
            val (startDate, endDate) = rangeDateFromTab(2)
            val rates = RateApi.rateService.getRatesByPeriod("month", startDate, endDate)
            Log.d(TAG("AnalysisViewModel", "loadMonthlyRates"), "Received data: $rates")
            _monthlyRates.value = filterAndMapRates(rates)
        } catch (error: Exception) {
            Log.e(TAG("AnalysisViewModel", "loadMonthlyRates"), "$error")
        }
    }

    private suspend fun loadYearlyRates() = withContext(Dispatchers.IO) {
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

        return rates.map {
            RateRange(
                jpy = it.exchangeRates.jpy,
                usd = it.exchangeRates.usd,
                createAt = it.createAt
            )
        }
    }

    fun onTabSelected(index: Int) {
        _analysisUiState.update { uiState ->
            uiState.copy(selectedTabIndex = index)
        }
    }

    fun refreshData() {
        // ✅ IO 스레드에서 실행
        viewModelScope.launch(Dispatchers.IO) {
            loadAllRangeData()
        }
    }

    private suspend fun loadDailyCharge() = withContext(Dispatchers.IO) {
        try {
            val changeAndLatestRate = RateApi.rateService.getDailyChange()
            Log.d(TAG("AnalysisViewModel", "loadDailyCharge"), "Received daily change data: $changeAndLatestRate")

            val (jpyIcon, jpyColor) = getChangeIndicator(changeAndLatestRate.change.jpy)
            val (usdIcon, usdColor) = getChangeIndicator(changeAndLatestRate.change.usd)

            // ✅ UI 업데이트는 Main 스레드로
            withContext(Dispatchers.Main) {
                _analysisUiState.update { uiState ->
                    uiState.copy(
                        latestRate = changeAndLatestRate.latestRate,
                        change = changeAndLatestRate.change,
                        jpyChangeIcon = jpyIcon,
                        jpyChangeColor = jpyColor,
                        usdChangeIcon = usdIcon,
                        usdChangeColor = usdColor
                    )
                }
            }

            Log.d(TAG("AnalysisViewModel", "loadDailyCharge"), "AnalysisUiState updated with daily change data.")
        } catch (error: Exception) {
            Log.e(TAG("AnalysisViewModel", "loadDailyCharge"), "Error loading daily change data: $error")
        }
    }

    private fun getChangeIndicator(changeValue: String): Pair<Char, Color> {
        return try {
            val change = changeValue.toDouble()
            when {
                change > 0 -> Pair('▲', Color.Red)
                change < 0 -> Pair('▼', Color.Blue)
                else -> Pair('-', Color.Gray)
            }
        } catch (e: NumberFormatException) {
            Log.e(TAG("AnalysisViewModel", "getChangeIndicator"),
                "NumberFormatException for changeValue: $changeValue, Error: $e")
            Pair('-', Color.Gray)
        }
    }
}

private fun rangeDateFromTab(tabIndex: Int): Pair<String, String> {
    val today = LocalDate.now()
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    val todayDate = today.format(formatter)

    val weekAgo = today.minusWeeks(1)
    val rangeWeek = weekAgo.format(formatter)

    val threeMonthsAgo = today.minusMonths(3)
    val rangeThreeMonths = threeMonthsAgo.format(formatter)

    val yearAgo = today.minusYears(1)
    val rangeYear = yearAgo.format(formatter)

    val endDate = todayDate
    val startDate = when(tabIndex) {
        0 -> todayDate
        1 -> rangeWeek
        2 -> rangeThreeMonths
        3 -> rangeYear
        else -> todayDate
    }

    return Pair(startDate, endDate)
}

data class RateRange(
    val jpy: String = "",
    val usd: String = "",
    val createAt: String = ""
)

data class RateRangeCurrency(
    val rate: Float,
    val createAt: String
)

data class AnalysisUiState(
    val selectedRates: List<RateRange> = emptyList(),
    val selectedTabIndex: Int = 0,
    val latestRate: ExchangeRates = ExchangeRates(
        usd = "0",
        jpy = "0"
    ),
    val change: CurrencyChange = CurrencyChange(
        usd = "0",
        jpy = "0"
    ),
    val jpyChangeIcon: Char = '-',
    val jpyChangeColor: Color = Color.Gray,
    val usdChangeIcon: Char = '-',
    val usdChangeColor: Color = Color.Gray
)