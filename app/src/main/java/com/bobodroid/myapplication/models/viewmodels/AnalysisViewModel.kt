package com.bobodroid.myapplication.models.viewmodels

import android.util.Log
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bobodroid.myapplication.MainActivity.Companion.TAG
import com.bobodroid.myapplication.models.datamodels.repository.UserRepository
import com.bobodroid.myapplication.models.datamodels.roomDb.Currencies
import com.bobodroid.myapplication.models.datamodels.roomDb.Currency
import com.bobodroid.myapplication.models.datamodels.roomDb.CurrencyType
import com.bobodroid.myapplication.models.datamodels.service.exchangeRateApi.CurrencyChange
import com.bobodroid.myapplication.models.datamodels.service.exchangeRateApi.ExchangeRateResponse
import com.bobodroid.myapplication.models.datamodels.service.exchangeRateApi.ExchangeRates
import com.bobodroid.myapplication.models.datamodels.service.exchangeRateApi.RateApi
import com.bobodroid.myapplication.models.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import kotlin.math.pow
import kotlin.math.sqrt

@HiltViewModel
class AnalysisViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val settingsRepository: SettingsRepository, // ✅ 추가
): ViewModel() {


    val isPremium = userRepository.userData
        .map { it?.localUserData?.isPremium ?: false }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )


    fun updateSelectedCurrency(currency: CurrencyType): Boolean {
        return settingsRepository.setSelectedCurrency(currency)
    }

    // ✅ SettingsRepository에서 통화 상태 가져오기
    val selectedCurrency = settingsRepository.selectedCurrency.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = CurrencyType.USD
    )

    private val _dailyRates = MutableStateFlow<List<RateRange>>(emptyList())
    private val _weeklyRates = MutableStateFlow<List<RateRange>>(emptyList())
    private val _monthlyRates = MutableStateFlow<List<RateRange>>(emptyList())
    private val _yearlyRates = MutableStateFlow<List<RateRange>>(emptyList())

    private val _analysisUiState = MutableStateFlow<AnalysisUiState>(AnalysisUiState())
    val analysisUiState = _analysisUiState.asStateFlow()

    init {
        viewModelScope.launch(Dispatchers.IO) {
            // ✅ 로딩 시작
            _analysisUiState.update {
                it.copy(loadingState = LoadingState.Loading)
            }

            try {
                // ✅ 모든 기간 데이터 로드
                loadAllRangeData()
                loadDailyCharge()

                // ✅ 데이터 검증 - 최소한 일간 데이터는 있어야 함
                if (_dailyRates.value.isEmpty()) {
                    throw Exception("환율 데이터를 불러올 수 없습니다.\n인터넷 연결을 확인해주세요.")
                }

                // ✅ 초기 선택된 탭 데이터 설정
                withContext(Dispatchers.Main) {
                    _analysisUiState.update { currentUiState ->
                        currentUiState.copy(
                            selectedRates = _dailyRates.value,
                            loadingState = LoadingState.Success
                        )
                    }
                }

                Log.d(TAG("AnalysisViewModel", "init"), "데이터 로드 완료")
                Log.d(TAG("AnalysisViewModel", "init"), "Daily: ${_dailyRates.value.size}개")
                Log.d(TAG("AnalysisViewModel", "init"), "Weekly: ${_weeklyRates.value.size}개")
                Log.d(TAG("AnalysisViewModel", "init"), "Monthly: ${_monthlyRates.value.size}개")
                Log.d(TAG("AnalysisViewModel", "init"), "Yearly: ${_yearlyRates.value.size}개")

            } catch (error: Exception) {
                Log.e(TAG("AnalysisViewModel", "init"), "데이터 로드 실패: ${error.message}", error)

                withContext(Dispatchers.Main) {
                    _analysisUiState.update {
                        it.copy(
                            loadingState = LoadingState.Error(
                                error.message ?: "알 수 없는 오류가 발생했습니다."
                            )
                        )
                    }
                }
            }
        }

        viewModelScope.launch {
            _analysisUiState.collect { uiState ->
                val newSelectedRates = when(uiState.selectedTabIndex) {
                    0 -> _dailyRates.value
                    1 -> _weeklyRates.value
                    2 -> _monthlyRates.value
                    3 -> _yearlyRates.value
                    else -> emptyList()
                }

                Log.d(TAG("AnalysisViewModel", "collectTabIndex"),
                    "탭 변경: ${uiState.selectedTabIndex}, 새 데이터: ${newSelectedRates.size}개")

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

        return filteredRates.map {
            // ✅ 12개 통화 모두 Map으로 변환
            val ratesMap = mapOf(
                "USD" to it.exchangeRates.usd,
                "JPY" to it.exchangeRates.jpy,
                "EUR" to it.exchangeRates.eur,
                "GBP" to it.exchangeRates.gbp,
                "CNY" to it.exchangeRates.cny,
                "AUD" to it.exchangeRates.aud,
                "CAD" to it.exchangeRates.cad,
                "CHF" to it.exchangeRates.chf,
                "HKD" to it.exchangeRates.hkd,
                "SGD" to it.exchangeRates.sgd,
                "NZD" to it.exchangeRates.nzd,
                "THB" to it.exchangeRates.thb
            )

            RateRange(
                rates = ratesMap,
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

            // ✅ USD, JPY만 처리하던 기존 로직 (하위 호환성 유지)
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
    fun calculateStatistics(currencyType: CurrencyType): RateStatistics {
        val rates = _analysisUiState.value.selectedRates
        if (rates.isEmpty()) return RateStatistics()

        // ✅ ExchangeRate에 이미 needsMultiply 처리된 값이 저장되어 있음
        val values = rates.mapNotNull { rate ->
            rate.getRate(currencyType.code).toFloatOrNull()
        }

        if (values.isEmpty()) return RateStatistics()

        val max = values.maxOrNull() ?: 0f
        val min = values.minOrNull() ?: 0f
        val average = values.average().toFloat()
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
    fun calculateTrendAnalysis(currencyType: CurrencyType): TrendAnalysis {
        val rates = _yearlyRates.value
        if (rates.size < 2) return TrendAnalysis()

        // ✅ ExchangeRate에 이미 needsMultiply 처리된 값이 저장되어 있음
        val values = rates.mapNotNull { rate ->
            rate.getRate(currencyType.code).toFloatOrNull()
        }

        if (values.size < 2) return TrendAnalysis()

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
    fun calculatePeriodComparison(currencyType: CurrencyType): PeriodComparison {
        // ✅ 최신 환율 사용 (ExchangeRate에 이미 needsMultiply 처리됨)
        val currentRateStr = when(currencyType) {
            CurrencyType.USD -> _analysisUiState.value.latestRate.usd
            CurrencyType.JPY -> _analysisUiState.value.latestRate.jpy
            else -> "0"
        }

        val currentRate = currentRateStr.toFloatOrNull() ?: 0f

        // ✅ 항상 전체 데이터(1년)에서 비교
        val allRates = _yearlyRates.value
        if (allRates.isEmpty()) return PeriodComparison()

        val values = allRates.mapNotNull { rate ->
            rate.getRate(currencyType.code).toFloatOrNull()
        }

        if (values.isEmpty()) return PeriodComparison()

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

// ✅ 파일 상단 data class 수정
data class RateRange(
    val rates: Map<String, String>, // 12개 통화 모두 저장
    val createAt: String
) {
    // ✅ 통화 코드로 환율 가져오기
    fun getRate(currencyCode: String): String = rates[currencyCode] ?: "0"

    // ✅ Currency 객체로 환율 가져오기
    fun getRate(currency: Currency): String = getRate(currency.code)

    // ✅ 하위 호환성 (레거시 코드 지원)
    val usd: String get() = getRate("USD")
    val jpy: String get() = getRate("JPY")
}

data class RateRangeCurrency(
    val rate: Float,
    val createAt: String
)

data class AnalysisUiState(
    val loadingState: LoadingState = LoadingState.Loading,
    val selectedRates: List<RateRange> = emptyList(),
    val selectedTabIndex: Int = 0,
    val latestRate: ExchangeRates = ExchangeRates(
        usd = "0",
        jpy = "0",
        eur = "0",
        gbp = "0",
        cny = "0",
        aud = "0",
        cad = "0",
        chf = "0",
        hkd = "0",
        sgd = "0",
        nzd = "0",
        thb = "0",
    ),
    val change: CurrencyChange = CurrencyChange(
        usd = "0",
        jpy = "0",
        eur = "0",
        gbp = "0",
        cny = "0",
        aud = "0",
        cad = "0",
        chf = "0",
        hkd = "0",
        sgd = "0",
        nzd = "0",
        thb = "0",
    ),
    val jpyChangeIcon: Char = '-',
    val jpyChangeColor: Color = Color.Gray,
    val usdChangeIcon: Char = '-',
    val usdChangeColor: Color = Color.Gray
)

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

sealed class LoadingState {
    object Loading : LoadingState()      // 로딩 중
    object Success : LoadingState()      // 성공
    data class Error(val message: String) : LoadingState()  // 실패
}
