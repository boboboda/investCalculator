package com.bobodroid.myapplication.models.viewmodels

import android.util.Log
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bobodroid.myapplication.MainActivity.Companion.TAG
import com.bobodroid.myapplication.models.datamodels.repository.AnalysisRateRepository
import com.bobodroid.myapplication.models.datamodels.repository.NewsRepository
import com.bobodroid.myapplication.models.datamodels.repository.UserRepository
import com.bobodroid.myapplication.models.datamodels.roomDb.Currencies
import com.bobodroid.myapplication.models.datamodels.roomDb.Currency
import com.bobodroid.myapplication.models.datamodels.roomDb.CurrencyType
import com.bobodroid.myapplication.models.datamodels.roomDb.TargetRates
import com.bobodroid.myapplication.models.datamodels.response.NewsItem
import com.bobodroid.myapplication.models.datamodels.service.exchangeRateApi.CurrencyChange
import com.bobodroid.myapplication.models.datamodels.service.exchangeRateApi.ExchangeRates
import com.bobodroid.myapplication.models.datamodels.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.util.TreeMap
import javax.inject.Inject
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sqrt

@HiltViewModel
class AnalysisViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val settingsRepository: SettingsRepository,
    private val newsRepository: NewsRepository, // ✅ MainViewModel에서 이전
    private val analysisRateRepository: AnalysisRateRepository, // ✅ 병렬 호출 + 앱 전체 공유 캐시
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
        // ✅ 분석 데이터 로드 (캐시가 있으면 스켈레톤 없이 즉시 표시, 없으면 병렬 호출)
        loadAnalysisData()

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
                val newSelectedRates = ratesForTab(uiState.selectedTabIndex)

                if (newSelectedRates != uiState.selectedRates) {
                    _analysisUiState.update { current ->
                        current.copy(selectedRates = newSelectedRates)
                    }
                }
            }
        }
    }

    private fun ratesForTab(tabIndex: Int): List<RateRange> {
        return when (tabIndex) {
            0 -> _dailyRates.value
            1 -> _weeklyRates.value
            2 -> _monthlyRates.value
            3 -> _yearlyRates.value
            else -> emptyList()
        }
    }

    /**
     * 분석 데이터 로드
     * - TTL 이내 캐시가 있으면 네트워크 호출 없이 바로 반영
     * - 이미 표시 중인 데이터가 있으면 스켈레톤 없이 백그라운드에서 갱신
     * - 처음 로드하는 경우에만 스켈레톤(Loading) → Success/Error 전환
     */
    private fun loadAnalysisData(force: Boolean = false) {
        viewModelScope.launch {
            if (!force) {
                analysisRateRepository.peekFresh()?.let { snapshot ->
                    applySnapshot(snapshot)
                    return@launch
                }
            }

            val hasData = _dailyRates.value.isNotEmpty()
            if (!hasData) {
                _analysisUiState.update { it.copy(loadingState = LoadingState.Loading) }
            }

            try {
                val snapshot = analysisRateRepository.load(force)
                applySnapshot(snapshot)
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                Log.e(TAG("AnalysisViewModel", "loadAnalysisData"), "데이터 로드 실패: ${error.message}", error)

                // 이미 표시 중인 데이터가 있으면 에러 화면으로 바꾸지 않고 기존 화면 유지
                if (!hasData) {
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
    }

    private fun applySnapshot(snapshot: AnalysisRateRepository.Snapshot) {
        _dailyRates.value = snapshot.daily
        _weeklyRates.value = snapshot.weekly
        _monthlyRates.value = snapshot.monthly
        _yearlyRates.value = snapshot.yearly

        _analysisUiState.update { current ->
            val updated = current.copy(
                selectedRates = ratesForTab(current.selectedTabIndex),
                loadingState = LoadingState.Success
            )

            val latest = snapshot.latest ?: return@update updated

            val (jpyIcon, jpyColor) = getChangeIndicator(latest.change.jpy)
            val (usdIcon, usdColor) = getChangeIndicator(latest.change.usd)

            updated.copy(
                latestRate = latest.latestRate,
                change = latest.change,
                jpyChangeIcon = jpyIcon,
                jpyChangeColor = jpyColor,
                usdChangeIcon = usdIcon,
                usdChangeColor = usdColor
            )
        }

        Log.d(
            TAG("AnalysisViewModel", "applySnapshot"),
            "Daily: ${snapshot.daily.size}, Weekly: ${snapshot.weekly.size}, " +
                    "Monthly: ${snapshot.monthly.size}, Yearly: ${snapshot.yearly.size}"
        )
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

    fun onTabSelected(index: Int) {
        _analysisUiState.update { uiState ->
            uiState.copy(selectedTabIndex = index)
        }
    }

    /**
     * 화면 진입/재시도 시 호출
     * - 캐시(60초)가 유효하면 네트워크 호출 없이 종료
     * - 만료됐으면 병렬로 다시 로드 (표시 중인 데이터가 있으면 스켈레톤 없이 갱신)
     */
    fun refreshData() {
        loadAnalysisData()
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

    // ✅ 일 단위(달력 기준 하루 1개) 값
    private data class DailyPoint(val date: LocalDate, val value: Float)

    private fun parseDate(createAt: String): LocalDate? {
        return try {
            LocalDate.parse(createAt.take(10))
        } catch (e: Exception) {
            null
        }
    }

    /**
     * 구간 데이터를 "달력 기준 하루 1개" 값으로 변환
     * - 같은 날짜가 여러 개면 마지막 값을 사용
     * - 값이 같아서 합쳐진 날(주말 등)은 합쳐진 구간의 값으로 채움
     *   (같은 값이 이어지면 구간의 마지막 기록만 남으므로, 기록 사이의 빈 날짜는 뒤쪽 기록과 같은 값)
     * - 서버가 일 단위로 내려주는 3개월/1년 구간에서 사용
     */
    private fun buildDailySeries(rates: List<RateRange>, currencyType: CurrencyType): List<DailyPoint> {
        val byDate = TreeMap<LocalDate, Float>()
        rates.forEach { rate ->
            val date = parseDate(rate.createAt) ?: return@forEach
            val value = rate.getRate(currencyType.code).toFloatOrNull() ?: return@forEach
            byDate[date] = value
        }
        if (byDate.isEmpty()) return emptyList()

        val first = byDate.firstKey()
        val last = byDate.lastKey()

        val result = ArrayList<DailyPoint>()
        var current = byDate.getValue(last)
        var date = last
        while (!date.isBefore(first)) {
            byDate[date]?.let { current = it }
            result.add(DailyPoint(date, current))
            date = date.minusDays(1)
        }
        result.reverse()
        return result
    }

    /** 특정 날짜의 값 (마지막 날짜 이후는 마지막 값, 첫 날짜 이전은 null) */
    private fun valueOnDate(series: List<DailyPoint>, target: LocalDate): Float? {
        if (series.isEmpty()) return null
        if (target.isBefore(series.first().date)) return null
        if (target.isAfter(series.last().date)) return series.last().value
        return series.firstOrNull { it.date == target }?.value
    }

    // ✅ 기간별 비교 - 날짜 기준 (전일 / 7일 전 / 1개월 전의 마지막 값과 비교), 12개 통화 모두 지원
    fun calculatePeriodComparison(currencyType: CurrencyType): PeriodComparison {
        val currentRate = _analysisUiState.value.latestRate.getRate(currencyType.code).toFloatOrNull() ?: 0f

        val series = buildDailySeries(_yearlyRates.value, currencyType)
        if (series.isEmpty()) return PeriodComparison()

        fun calculateChange(oldValue: Float?): String {
            if (oldValue == null || oldValue == 0f) return "0.00%"
            val change = ((currentRate - oldValue) / oldValue) * 100
            return String.format("%.2f%%", change)
        }

        val today = LocalDate.now()

        return PeriodComparison(
            previousDay = calculateChange(valueOnDate(series, today.minusDays(1))),
            weekAgo = calculateChange(valueOnDate(series, today.minusDays(7))),
            monthAgo = calculateChange(valueOnDate(series, today.minusMonths(1)))
        )
    }

    // ✅ 신규: 장기 포지션 - 3개월/1년 구간에서 현재 환율의 위치(0~100%), 일 단위 값 기준
    fun calculateLongTermPosition(currencyType: CurrencyType): LongTermPosition {
        val currentRate = _analysisUiState.value.latestRate.getRate(currencyType.code).toFloatOrNull()
            ?: return LongTermPosition()

        fun percentileIn(rates: List<RateRange>): Float? {
            val values = buildDailySeries(rates, currencyType).map { it.value }
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

    // ✅ 신규: 최고가/최저가 대비 (항상 1년 데이터 고정 기준, 일 단위 값)
    fun calculateHighLowDistance(currencyType: CurrencyType): HighLowDistance {
        val currentRate = _analysisUiState.value.latestRate.getRate(currencyType.code).toFloatOrNull()
            ?: return HighLowDistance()

        val values = buildDailySeries(_yearlyRates.value, currencyType).map { it.value }
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
    // 3개월/1년은 일 단위 값의 평균 (값이 같아 합쳐진 날도 평균에 포함)
    fun calculatePeriodAverages(currencyType: CurrencyType): List<PeriodAverage> {
        val currentRate = _analysisUiState.value.latestRate.getRate(currencyType.code).toFloatOrNull()
            ?: return emptyList()

        fun averageOf(rates: List<RateRange>, dailyBased: Boolean): Float? {
            val values = if (dailyBased) {
                buildDailySeries(rates, currencyType).map { it.value }
            } else {
                rates.mapNotNull { it.getRate(currencyType.code).toFloatOrNull() }
            }
            if (values.isEmpty()) return null
            return values.average().toFloat()
        }

        val periods = listOf(
            Triple("1일", _dailyRates.value, false),
            Triple("7일", _weeklyRates.value, false),
            Triple("3개월", _monthlyRates.value, true),
            Triple("1년", _yearlyRates.value, true)
        )

        return periods.mapNotNull { (label, rates, dailyBased) ->
            val avg = averageOf(rates, dailyBased) ?: return@mapNotNull null
            val diff = currentRate - avg
            val diffPercent = if (avg != 0f) (diff / avg) * 100f else 0f
            PeriodAverage(label = label, averageRate = avg, diff = diff, diffPercent = diffPercent)
        }
    }

    // ✅ 신규: 1년 평균 대비 연속일수 (달력 기준 하루씩, 최근 날짜부터 거꾸로 카운트)
    fun calculateYearlyAverageStreak(currencyType: CurrencyType): YearlyAverageStreak {
        val values = buildDailySeries(_yearlyRates.value, currencyType).map { it.value }
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

    // ✅ 신규: 일별 변동 분포 - 최근 1년, 달력 기준 하루씩(주말 포함), 원 단위 고정 구간(3원/5원)
    fun calculateDailyChangeDistribution(currencyType: CurrencyType): DailyChangeDistribution {
        val values = buildDailySeries(_yearlyRates.value, currencyType).map { it.value }
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

    // ✅ 매수/매도 스프레드 적용 환율 (헤더 표시용, 원 단위 가감)
    fun calculateSpreadRates(currencyType: CurrencyType): SpreadRates {
        if (!settingsRepository.hasCustomSpread(currencyType)) return SpreadRates()

        val currentRate = _analysisUiState.value.latestRate.getRate(currencyType.code).toDoubleOrNull()
            ?: return SpreadRates()

        val buySpreadWon = settingsRepository.getBuySpreadWon(currencyType)
        val sellSpreadWon = settingsRepository.getSellSpreadWon(currencyType)

        val buyRate = currentRate + buySpreadWon
        val sellRate = (currentRate - sellSpreadWon).coerceAtLeast(0.0)

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
