package com.bobodroid.myapplication.models.viewmodels

import android.util.Log
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bobodroid.myapplication.MainActivity.Companion.TAG
import com.bobodroid.myapplication.models.datamodels.repository.NewsRepository
import com.bobodroid.myapplication.models.datamodels.repository.UserRepository
import com.bobodroid.myapplication.models.datamodels.roomDb.Currencies
import com.bobodroid.myapplication.models.datamodels.roomDb.Currency
import com.bobodroid.myapplication.models.datamodels.roomDb.CurrencyType
import com.bobodroid.myapplication.models.datamodels.roomDb.TargetRates
import com.bobodroid.myapplication.models.datamodels.response.NewsItem
import com.bobodroid.myapplication.models.datamodels.service.exchangeRateApi.CurrencyChange
import com.bobodroid.myapplication.models.datamodels.service.exchangeRateApi.ExchangeRateResponse
import com.bobodroid.myapplication.models.datamodels.service.exchangeRateApi.ExchangeRates
import com.bobodroid.myapplication.models.datamodels.service.exchangeRateApi.RateApi
import com.bobodroid.myapplication.models.datamodels.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sqrt

@HiltViewModel
class AnalysisViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val settingsRepository: SettingsRepository,
    private val newsRepository: NewsRepository, // ✅ MainViewModel에서 이전
): ViewModel() {


    val isPremium = userRepository.userData
        .map { it?.localUserData?.isPremium ?: false }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )

    // ✅ 뉴스 (MainViewModel에서 이전)
    private val _latestNews = MutableStateFlow<List<NewsItem>>(emptyList())
    val latestNews = _latestNews.asStateFlow()

    // ✅ 목표환율 (FcmAlarmViewModel과 동일한 소스를 직접 구독)
    private val _targetRates = MutableStateFlow(TargetRates(rates = emptyMap()))

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
    private val _monthlyRates = MutableStateFlow<List<RateRange>>(emptyList()) // ⚠️ 실제로는 "3개월" 범위 데이터
    private val _yearlyRates = MutableStateFlow<List<RateRange>>(emptyList())

    private val _analysisUiState = MutableStateFlow<AnalysisUiState>(AnalysisUiState())
    val analysisUiState = _analysisUiState.asStateFlow()

    init {
        viewModelScope.launch(Dispatchers.IO) {
            _analysisUiState.update {
                it.copy(loadingState = LoadingState.Loading)
            }

            try {
                loadAllRangeData()
                loadDailyCharge()

                if (_dailyRates.value.isEmpty()) {
                    throw Exception("환율 데이터를 불러올 수 없습니다.\n인터넷 연결을 확인해주세요.")
                }

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

        // ✅ 뉴스 로드 (MainViewModel에서 이전)
        loadLatestNews()

        // ✅ 목표환율 구독 (FcmAlarmViewModel.initTarRates()와 동일한 소스)
        viewModelScope.launch {
            userRepository.userData.filterNotNull().collect { userData ->
                _targetRates.value = userData.exchangeRates ?: TargetRates(rates = emptyMap())
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

    // ✅ 뉴스 로드 (MainViewModel에서 이전)
    private fun loadLatestNews() {
        viewModelScope.launch {
            newsRepository.getLatestNews(5).collect { result ->
                result.onSuccess { news ->
                    Log.d(TAG("AnalysisViewModel", "loadLatestNews"), "✅ ${news.size}개 뉴스 로드")
                    _latestNews.value = news
                }.onFailure { error ->
                    Log.e(TAG("AnalysisViewModel", "loadLatestNews"), "❌ 뉴스 로드 실패", error)
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

    // ✅ Bug4 fix: USD/JPY 하드코딩 제거, 12개 통화 모두 지원
    fun calculatePeriodComparison(currencyType: CurrencyType): PeriodComparison {
        val currentRate = _analysisUiState.value.latestRate.getRate(currencyType.code).toFloatOrNull() ?: 0f

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

        val previousDay = if (values.size > 1) values[values.size - 2] else currentRate
        val weekAgo = if (values.size >= 7) values[values.size - 7] else currentRate
        val monthAgo = if (values.size >= 30) values[values.size - 30] else currentRate

        return PeriodComparison(
            previousDay = calculateChange(previousDay),
            weekAgo = calculateChange(weekAgo),
            monthAgo = calculateChange(monthAgo)
        )
    }

    // ✅ 신규: 장기 포지션 - 3개월/1년 구간에서 현재 환율의 위치(0~100%)
    fun calculateLongTermPosition(currencyType: CurrencyType): LongTermPosition {
        val currentRate = _analysisUiState.value.latestRate.getRate(currencyType.code).toFloatOrNull()
            ?: return LongTermPosition()

        fun percentileIn(rates: List<RateRange>): Float? {
            val values = rates.mapNotNull { it.getRate(currencyType.code).toFloatOrNull() }
            if (values.size < 2) return null
            val min = values.min()
            val max = values.max()
            if (max == min) return 50f
            return ((currentRate - min) / (max - min) * 100f).coerceIn(0f, 100f)
        }

        return LongTermPosition(
            threeMonthPercentile = percentileIn(_monthlyRates.value),
            oneYearPercentile = percentileIn(_yearlyRates.value)
        )
    }

    // ✅ 신규: 최고가/최저가 대비 (항상 1년 데이터 고정 기준)
    fun calculateHighLowDistance(currencyType: CurrencyType): HighLowDistance {
        val currentRate = _analysisUiState.value.latestRate.getRate(currencyType.code).toFloatOrNull()
            ?: return HighLowDistance()

        val values = _yearlyRates.value.mapNotNull { it.getRate(currencyType.code).toFloatOrNull() }
        if (values.isEmpty()) return HighLowDistance()

        val high = values.max()
        val low = values.min()

        val diffFromHigh = currentRate - high
        val diffFromHighPercent = if (high != 0f) (diffFromHigh / high) * 100f else 0f

        val diffFromLow = currentRate - low
        val diffFromLowPercent = if (low != 0f) (diffFromLow / low) * 100f else 0f

        return HighLowDistance(
            highRate = high,
            lowRate = low,
            diffFromHigh = diffFromHigh,
            diffFromHighPercent = diffFromHighPercent,
            diffFromLow = diffFromLow,
            diffFromLowPercent = diffFromLowPercent,
            hasData = true
        )
    }

    // ✅ 신규: 기간별(1일/7일/3개월/1년) 평균 대비 현재 위치
    fun calculatePeriodAverages(currencyType: CurrencyType): List<PeriodAverage> {
        val currentRate = _analysisUiState.value.latestRate.getRate(currencyType.code).toFloatOrNull()
            ?: return emptyList()

        fun averageOf(rates: List<RateRange>): Float? {
            val values = rates.mapNotNull { it.getRate(currencyType.code).toFloatOrNull() }
            if (values.isEmpty()) return null
            return values.average().toFloat()
        }

        val periods = listOf(
            "1일" to _dailyRates.value,
            "7일" to _weeklyRates.value,
            "3개월" to _monthlyRates.value,
            "1년" to _yearlyRates.value
        )

        return periods.mapNotNull { (label, rates) ->
            val avg = averageOf(rates) ?: return@mapNotNull null
            val diff = currentRate - avg
            val diffPercent = if (avg != 0f) (diff / avg) * 100f else 0f
            PeriodAverage(label = label, averageRate = avg, diff = diff, diffPercent = diffPercent)
        }
    }

    // ✅ 신규: 1년 평균 대비 연속일수 (최근 데이터부터 거꾸로 카운트)
    fun calculateYearlyAverageStreak(currencyType: CurrencyType): YearlyAverageStreak {
        val values = _yearlyRates.value.mapNotNull { it.getRate(currencyType.code).toFloatOrNull() }
        if (values.size < 2) return YearlyAverageStreak()

        val average = values.average().toFloat()
        if (average == 0f) return YearlyAverageStreak()

        var streak = 0
        for (i in values.size - 1 downTo 0) {
            val value = values[i]
            when {
                value > average -> {
                    if (streak >= 0) streak++ else break
                }
                value < average -> {
                    if (streak <= 0) streak-- else break
                }
                else -> break
            }
        }

        return YearlyAverageStreak(streakDays = streak, hasData = true)
    }

    // ✅ 신규: 일별 변동 분포 - 최근 1년, 원 단위 고정 구간(3원/5원)
    fun calculateDailyChangeDistribution(currencyType: CurrencyType): DailyChangeDistribution {
        val values = _yearlyRates.value.mapNotNull { it.getRate(currencyType.code).toFloatOrNull() }
        if (values.size < 2) return DailyChangeDistribution()

        var bigDown = 0
        var down = 0
        var flat = 0
        var up = 0
        var bigUp = 0

        for (i in 1 until values.size) {
            val change = values[i] - values[i - 1] // 전일 대비 변동폭 (원)
            when {
                change <= -5f -> bigDown++
                change <= -3f -> down++
                change < 3f -> flat++
                change < 5f -> up++
                else -> bigUp++
            }
        }

        val total = bigDown + down + flat + up + bigUp
        return DailyChangeDistribution(
            bigDownCount = bigDown,
            downCount = down,
            flatCount = flat,
            upCount = up,
            bigUpCount = bigUp,
            totalCount = total
        )
    }

    // ✅ 매수/매도 스프레드 적용 환율 (헤더 표시용)
    fun calculateSpreadRates(currencyType: CurrencyType): SpreadRates {
        if (!settingsRepository.hasCustomSpread(currencyType)) return SpreadRates()

        val currentRate = _analysisUiState.value.latestRate.getRate(currencyType.code).toFloatOrNull()
            ?: return SpreadRates()

        val buySpreadPercent = settingsRepository.getBuySpreadPercent(currencyType)
        val sellSpreadPercent = settingsRepository.getSellSpreadPercent(currencyType)

        val buyRate = currentRate * (1f + (buySpreadPercent / 100.0).toFloat())
        val sellRate = currentRate * (1f - (sellSpreadPercent / 100.0).toFloat())

        return SpreadRates(
            buyRate = String.format("%.2f", buyRate),
            sellRate = String.format("%.2f", sellRate),
            applied = true
        )
    }

    // ✅ 목표환율 대비 현재 환율 위치 (가장 가까운 목표 1개씩)
    fun calculateTargetRateInfo(currencyType: CurrencyType): TargetRateInfo {
        val currencyTargets = _targetRates.value.rates[currencyType] ?: return TargetRateInfo()
        if (currencyTargets.high.isEmpty() && currencyTargets.low.isEmpty()) return TargetRateInfo()

        val currentRate = _analysisUiState.value.latestRate.getRate(currencyType.code).toFloatOrNull()
            ?: return TargetRateInfo()

        val nearestHigh = currencyTargets.high.minByOrNull { abs(it.rate - currentRate) }
        val nearestLow = currencyTargets.low.minByOrNull { abs(it.rate - currentRate) }

        val highDiffPercent = nearestHigh?.let {
            if (currentRate == 0f) null else ((it.rate - currentRate) / currentRate) * 100f
        }
        val lowDiffPercent = nearestLow?.let {
            if (currentRate == 0f) null else ((it.rate - currentRate) / currentRate) * 100f
        }

        return TargetRateInfo(
            hasTargets = true,
            nearestHighRate = nearestHigh?.rate,
            nearestLowRate = nearestLow?.rate,
            highDiffPercent = highDiffPercent,
            lowDiffPercent = lowDiffPercent
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
    val rates: Map<String, String>,
    val createAt: String
) {
    fun getRate(currencyCode: String): String = rates[currencyCode] ?: "0"
    fun getRate(currency: Currency): String = getRate(currency.code)
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
        usd = "0", jpy = "0", eur = "0", gbp = "0", cny = "0", aud = "0",
        cad = "0", chf = "0", hkd = "0", sgd = "0", nzd = "0", thb = "0",
    ),
    val change: CurrencyChange = CurrencyChange(
        usd = "0", jpy = "0", eur = "0", gbp = "0", cny = "0", aud = "0",
        cad = "0", chf = "0", hkd = "0", sgd = "0", nzd = "0", thb = "0",
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

data class PeriodComparison(
    val previousDay: String = "0.00%",
    val weekAgo: String = "0.00%",
    val monthAgo: String = "0.00%"
)

// ✅ 장기 포지션 (구간 내 현재 환율의 위치, 0~100%)
data class LongTermPosition(
    val threeMonthPercentile: Float? = null,
    val oneYearPercentile: Float? = null
)

// ✅ 신규: 최고가/최저가 대비 (1년 고정 기준)
data class HighLowDistance(
    val highRate: Float = 0f,
    val lowRate: Float = 0f,
    val diffFromHigh: Float = 0f,
    val diffFromHighPercent: Float = 0f,
    val diffFromLow: Float = 0f,
    val diffFromLowPercent: Float = 0f,
    val hasData: Boolean = false
)

// ✅ 신규: 기간별 평균 대비 위치
data class PeriodAverage(
    val label: String,
    val averageRate: Float,
    val diff: Float,
    val diffPercent: Float
)

// ✅ 신규: 1년 평균 대비 연속일수 (양수=연속 상회, 음수=연속 하회)
data class YearlyAverageStreak(
    val streakDays: Int = 0,
    val hasData: Boolean = false
)

// ✅ 일별 변동 분포 (최근 1년, 원 단위 5단계)
data class DailyChangeDistribution(
    val bigDownCount: Int = 0,
    val downCount: Int = 0,
    val flatCount: Int = 0,
    val upCount: Int = 0,
    val bigUpCount: Int = 0,
    val totalCount: Int = 0
)

// ✅ 매수/매도 스프레드 적용 환율
data class SpreadRates(
    val buyRate: String = "-",
    val sellRate: String = "-",
    val applied: Boolean = false
)

// ✅ 목표환율 대비 정보
data class TargetRateInfo(
    val hasTargets: Boolean = false,
    val nearestHighRate: Int? = null,
    val nearestLowRate: Int? = null,
    val highDiffPercent: Float? = null,
    val lowDiffPercent: Float? = null
)

sealed class LoadingState {
    object Loading : LoadingState()
    object Success : LoadingState()
    data class Error(val message: String) : LoadingState()
}