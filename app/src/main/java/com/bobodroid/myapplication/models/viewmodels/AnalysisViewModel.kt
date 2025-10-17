package com.bobodroid.myapplication.models.viewmodels

import android.provider.Telephony.Mms.Rate
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
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.update
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
//    private val _selectedRates = MutableStateFlow<List<RateRange>>(emptyList())
//    val selectedRates = _selectedRates.asStateFlow()

    // 현재 선택된 탭 인덱스
//    private val _selectedTabIndex = MutableStateFlow(0)
//    val selectedTabIndex = _selectedTabIndex.asStateFlow()

    private val _analysisUiState = MutableStateFlow<AnalysisUiState>(AnalysisUiState())
    val analysisUiState = _analysisUiState.asStateFlow()

    init {
        // 초기 데이터 로드

        viewModelScope.launch {
            loadAllRangeData()

            loadDailyRates()

            loadDailyCharge()

            _analysisUiState.update { currentUiState ->
                currentUiState.copy(
                    selectedRates = _dailyRates.value // _dailyRates의 현재 값을 가져와 할당
                )
            }

            Log.d(TAG("AnalysisViewModel", "init"), "Initial daily data set: ${_dailyRates.value}")
            Log.d(TAG("AnalysisViewModel", "init"), "Initial selectedRates data set: ${_analysisUiState.value.selectedRates}")
        }

        // 탭 변경 시 해당하는 데이터로 selectedRates 업데이트

        viewModelScope.launch {
            // _analysisUiState의 selectedTabIndex 변화를 감지
            // collectAsState()는 Composable에서 사용하므로, ViewModel에서는 collect를 사용
            _analysisUiState.collect { uiState -> // uiState는 AnalysisUiState의 현재 값입니다.
                Log.d(TAG("AnalysisViewModel", "collectTabIndex"), "Selected tab index: ${uiState.selectedTabIndex}, Current selectedRates: ${uiState.selectedRates}")

                val newSelectedRates = when(uiState.selectedTabIndex) {
                    0 -> _dailyRates.value
                    1 -> _weeklyRates.value
                    2 -> _monthlyRates.value
                    3 -> _yearlyRates.value
                    else -> emptyList()
                }

                // 현재 selectedRates와 다를 경우에만 업데이트
                if (newSelectedRates != uiState.selectedRates) {
                    _analysisUiState.update { current ->
                        current.copy(selectedRates = newSelectedRates) // 새로운 AnalysisUiState 객체 생성 및 할당
                    }
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

        return rates.map {
            RateRange(
                jpy = it.exchangeRates.jpy,
                usd = it.exchangeRates.usd,
                createAt = it.createAt
            )
        }
    }

    // UI에서 탭 변경 시 호출
    fun onTabSelected(index: Int) {
        _analysisUiState.update { uiState ->
            uiState.copy(selectedTabIndex = index)
        }
    }

    // 데이터 새로고침이 필요한 경우
    fun refreshData() {
        viewModelScope.launch {
            loadAllRangeData()
        }

    }


    private suspend fun loadDailyCharge() { // 사용자님이 사용하시는 함수 이름 그대로 유지

        try {
            val changeAndLatestRate = RateApi.rateService.getDailyChange()
            Log.d(TAG("AnalysisViewModel", "loadDailyCharge"), "Received daily change data: $changeAndLatestRate") // 로그 메시지 수정

            _analysisUiState.update { uiState ->
                // uiState의 copy()를 사용하여 latestRate와 change 필드를 업데이트합니다.
                // 이렇게 하면 새로운 AnalysisUiState 객체가 생성되어 _analysisUiState에 할당됩니다.
                val (jpyIcon, jpyColor) = getChangeIndicator(changeAndLatestRate.change.jpy) // 아이콘/색상 로직 추가
                val (usdIcon, usdColor) = getChangeIndicator(changeAndLatestRate.change.usd) // 아이콘/색상 로직 추가

                uiState.copy(
                    latestRate = changeAndLatestRate.latestRate, // 최신 환율 업데이트
                    change = changeAndLatestRate.change,         // 변화량 업데이트
                    jpyChangeIcon = jpyIcon,                     // JPY 아이콘 업데이트
                    jpyChangeColor = jpyColor,                   // JPY 색상 업데이트
                    usdChangeIcon = usdIcon,                     // USD 아이콘 업데이트
                    usdChangeColor = usdColor                    // USD 색상 업데이트
                )
            }
            Log.d(TAG("AnalysisViewModel", "loadDailyCharge"), "AnalysisUiState updated with daily change data.") // 업데이트 완료 로그

        } catch (error: Exception) {
            // 오류 로그는 그대로 유지
            Log.e(TAG("AnalysisViewModel", "loadDailyCharge"), "Error loading daily change data: $error") // 로그 메시지 수정
            // TODO: UI에 에러를 표시하려면 AnalysisUiState에 errorMessage 필드를 추가하거나 다른 방법을 사용해야 합니다.
        }
    }

    private fun getChangeIndicator(changeValue: String): Pair<Char, Color> {
        return try {
            val change = changeValue.toDouble() // 문자열을 Double로 변환
            when {
                change > 0 -> Pair('▲', Color.Red) // 값이 0보다 크면 상승 (빨간색)
                change < 0 -> Pair('▼', Color.Blue) // 값이 0보다 작으면 하락 (파란색)
                else -> Pair('-', Color.Gray) // 값이 0이거나 N/A 등 파싱 불가 시 (회색)
            }
        } catch (e: NumberFormatException) {
            // changeValue가 "N/A" 또는 유효한 숫자가 아닐 경우
            Log.e(TAG("AnalysisViewModel", "getChangeIndicator"), "NumberFormatException for changeValue: $changeValue, Error: $e")
            Pair('-', Color.Gray) // 파싱 실패 시 기본값 (회색)
        }
    }

}




private fun rangeDateFromTab(tabIndex: Int): Pair<String, String> {


    val today = LocalDate.now()
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    // 오늘 날짜
    val todayDate = today.format(formatter)

//    val todayDate = "2024-11-23"

    // 일주일 범위 (오늘부터 7일 전)
    val weekAgo = today.minusWeeks(1)
    val rangeWeek = weekAgo.format(formatter)

    // 3달 범위 (오늘부터 3달 전)
    val threeMonthsAgo = today.minusMonths(3)
    val rangeThreeMonths = threeMonthsAgo.format(formatter)

    // 1년 범위 (오늘부터 1년 전)
    val yearAgo = today.minusYears(1)
    val rangeYear = yearAgo.format(formatter)

    var endDate = todayDate
    var startDate = ""

    when(tabIndex) {
        0 -> {
            startDate = todayDate
        }
        1 -> {
            startDate = rangeWeek
        }
        2 -> {
            startDate = rangeThreeMonths
        }
        3 -> {
            startDate = rangeYear
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


data class AnalysisUiState(
    val selectedRates: List<RateRange> = emptyList(),
    val selectedTabIndex: Int = 0,
    val latestRate: ExchangeRates = ExchangeRates(
        usd = "0",
        jpy = "0"
    ),
    val change: CurrencyChange  = CurrencyChange(
        usd = "0",
        jpy = "0"
    ),

    val jpyChangeIcon: Char = '-', // ▲, ▼, - 중 하나
    val jpyChangeColor: Color = Color.Gray, // 상승/하락/유지에 따른 색상
    val usdChangeIcon: Char = '-',
    val usdChangeColor: Color = Color.Gray
)

