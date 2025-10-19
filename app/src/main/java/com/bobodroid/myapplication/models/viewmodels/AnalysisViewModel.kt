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
import kotlin.math.pow
import kotlin.math.sqrt

@HiltViewModel
class AnalysisViewModel @Inject constructor(): ViewModel() {

    private val _dailyRates = MutableStateFlow<List<RateRange>>(emptyList())
    private val _weeklyRates = MutableStateFlow<List<RateRange>>(emptyList())
    private val _monthlyRates = MutableStateFlow<List<RateRange>>(emptyList())
    private val _yearlyRates = MutableStateFlow<List<RateRange>>(emptyList())

    private val _analysisUiState = MutableStateFlow<AnalysisUiState>(AnalysisUiState())
    val analysisUiState = _analysisUiState.asStateFlow()

    init {
        viewModelScope.launch(Dispatchers.IO) {
            loadAllRangeData()
            loadDailyRates()
            loadDailyCharge()

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

    // ✨ 통계 계산 - 선택된 탭의 데이터 기준
    fun calculateStatistics(currencyType: com.bobodroid.myapplication.models.datamodels.roomDb.CurrencyType): RateStatistics {
        val rates = _analysisUiState.value.selectedRates
        if (rates.isEmpty()) return RateStatistics()

        val values = rates.map {
            when(currencyType) {
                com.bobodroid.myapplication.models.datamodels.roomDb.CurrencyType.USD ->
                    it.usd.toFloatOrNull() ?: 0f
                com.bobodroid.myapplication.models.datamodels.roomDb.CurrencyType.JPY ->
                    (it.jpy.toFloatOrNull() ?: 0f) * 100f  // ✅ JPY는 100배
            }
        }

        val max = values.maxOrNull() ?: 0f
        val min = values.minOrNull() ?: 0f
        val average = if (values.isNotEmpty()) values.average().toFloat() else 0f
        val volatility = calculateVolatility(values)
        val range = max - min

        return RateStatistics(
            max = max,
            min = min,
            average = average,
            volatility = volatility,
            range = range
        )
    }

    private fun calculateVolatility(values: List<Float>): Float {
        if (values.size < 2) return 0f

        val mean = values.average()
        val variance = values.map { (it - mean).pow(2) }.average()
        return sqrt(variance).toFloat()
    }

    // ✅ 추세 분석 - 항상 전체 데이터(1년) 기준
    fun calculateTrendAnalysis(currencyType: com.bobodroid.myapplication.models.datamodels.roomDb.CurrencyType): TrendAnalysis {
        // ✅ 항상 1년 데이터 사용 (탭과 무관)
        val rates = _yearlyRates.value
        if (rates.size < 2) return TrendAnalysis()

        val values = rates.map {
            when(currencyType) {
                com.bobodroid.myapplication.models.datamodels.roomDb.CurrencyType.USD ->
                    it.usd.toFloatOrNull() ?: 0f
                com.bobodroid.myapplication.models.datamodels.roomDb.CurrencyType.JPY ->
                    (it.jpy.toFloatOrNull() ?: 0f) * 100f  // ✅ JPY는 100배
            }
        }

        var upDays = 0
        var downDays = 0

        for (i in 1 until values.size) {
            when {
                values[i] > values[i-1] -> upDays++
                values[i] < values[i-1] -> downDays++
            }
        }

        val trend = when {
            upDays > downDays -> "상승"
            downDays > upDays -> "하락"
            else -> "횡보"
        }

        val trendStrength = if (values.size > 1) {
            val maxChange = (upDays + downDays).toFloat()
            if (maxChange > 0) ((kotlin.math.abs(upDays - downDays) / maxChange) * 100).toInt() else 0
        } else 0

        return TrendAnalysis(
            trend = trend,
            upDays = upDays,
            downDays = downDays,
            trendStrength = trendStrength
        )
    }

    // ✅ 기간별 비교 - 항상 최신 환율 기준 (탭과 무관)
    fun calculatePeriodComparison(currencyType: com.bobodroid.myapplication.models.datamodels.roomDb.CurrencyType): PeriodComparison {
        // ✅ 최신 환율 사용
        val currentRate = when(currencyType) {
            com.bobodroid.myapplication.models.datamodels.roomDb.CurrencyType.USD ->
                _analysisUiState.value.latestRate.usd.toFloatOrNull() ?: 0f
            com.bobodroid.myapplication.models.datamodels.roomDb.CurrencyType.JPY ->
                (_analysisUiState.value.latestRate.jpy.toFloatOrNull() ?: 0f)
        }

        // ✅ 항상 전체 데이터(1년)에서 비교
        val allRates = _yearlyRates.value
        if (allRates.isEmpty()) return PeriodComparison()

        val values = allRates.map {
            when(currencyType) {
                com.bobodroid.myapplication.models.datamodels.roomDb.CurrencyType.USD ->
                    it.usd.toFloatOrNull() ?: 0f
                com.bobodroid.myapplication.models.datamodels.roomDb.CurrencyType.JPY ->
                    (it.jpy.toFloatOrNull() ?: 0f) * 100f  // ✅ JPY는 100배
            }
        }

        fun calculateChange(oldValue: Float): String {
            if (oldValue == 0f) return "0.00%"
            val change = ((currentRate - oldValue) / oldValue) * 100
            return String.format("%.2f%%", change)
        }

        // ✅ 항상 고정된 기간으로 비교
        val previousDay = if (values.size > 1) values[values.size - 2] else currentRate
        val weekAgo = if (values.size >= 7) values[values.size - 7] else currentRate
        val monthAgo = if (values.size >= 30) values[values.size - 30] else currentRate

        return PeriodComparison(
            previousDay = calculateChange(previousDay),
            weekAgo = calculateChange(weekAgo),
            monthAgo = calculateChange(monthAgo)
        )
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

// ✨ 데이터 클래스들
data class RateStatistics(
    val max: Float = 0f,
    val min: Float = 0f,
    val average: Float = 0f,
    val volatility: Float = 0f,
    val range: Float = 0f
)

data class TrendAnalysis(
    val trend: String = "횡보",
    val upDays: Int = 0,
    val downDays: Int = 0,
    val trendStrength: Int = 0
)

data class PeriodComparison(
    val previousDay: String = "0.00%",
    val weekAgo: String = "0.00%",
    val monthAgo: String = "0.00%"
)