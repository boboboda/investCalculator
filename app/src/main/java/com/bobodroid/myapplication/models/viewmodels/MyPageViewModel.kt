package com.bobodroid.myapplication.models.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bobodroid.myapplication.models.datamodels.repository.InvestRepository
import com.bobodroid.myapplication.models.datamodels.repository.UserRepository
import com.bobodroid.myapplication.models.datamodels.roomDb.LocalUserData
import com.bobodroid.myapplication.models.datamodels.useCases.UserUseCases
import com.bobodroid.myapplication.util.result.onError
import com.bobodroid.myapplication.util.result.onSuccess
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.math.RoundingMode
import javax.inject.Inject

@HiltViewModel
class MyPageViewModel @Inject constructor(
    private val userUseCases: UserUseCases,
    private val userRepository: UserRepository,
    private val investRepository: InvestRepository
) : ViewModel() {

    val _myPageUiState = MutableStateFlow(MyPageUiState())
    val myPageUiState = _myPageUiState.asStateFlow()

    init {
        // ✅ userData collect는 별도 코루틴
        viewModelScope.launch {
            userRepository.userData.collect { userData ->
                _myPageUiState.update {
                    it.copy(localUser = userData?.localUserData ?: LocalUserData())
                }
            }
        }

        // ✅ 통계 계산 (별도 코루틴)
        viewModelScope.launch {
            calculateInvestmentStats()
        }

        // ✅ 최근 활동 수집 (별도 코루틴)
        viewModelScope.launch {
            collectRecentActivities()
        }
    }

    // ✅ DB Flow는 이미 flowOn으로 IO 처리되므로 추가 작업 불필요
    private suspend fun calculateInvestmentStats() {
        combine(
            investRepository.getAllDollarBuyRecords(),
            investRepository.getAllYenBuyRecords()
        ) { dollarRecords, yenRecords ->

            Log.d("MyPageViewModel", "달러 기록: ${dollarRecords.size}개, 엔화 기록: ${yenRecords.size}개")

            // 1. 총 투자금 계산 (환전한 금액 합계)
            val totalDollarInvestment = dollarRecords.sumOf {
                it.money?.replace(",", "")?.toBigDecimalOrNull() ?: BigDecimal.ZERO
            }
            val totalYenInvestment = yenRecords.sumOf {
                it.money?.replace(",", "")?.toBigDecimalOrNull() ?: BigDecimal.ZERO
            }
            val totalInvestment = totalDollarInvestment + totalYenInvestment

            // 2. 예상 수익 계산
            val totalDollarProfit = dollarRecords.sumOf {
                it.expectProfit?.replace(",", "")?.toBigDecimalOrNull() ?: BigDecimal.ZERO
            }
            val totalYenProfit = yenRecords.sumOf {
                it.expectProfit?.replace(",", "")?.toBigDecimalOrNull() ?: BigDecimal.ZERO
            }
            val totalProfit = totalDollarProfit + totalYenProfit

            // 3. 수익률 계산
            val profitRate = if (totalInvestment > BigDecimal.ZERO) {
                (totalProfit.divide(totalInvestment, 4, RoundingMode.HALF_UP) * BigDecimal(100))
                    .setScale(1, RoundingMode.HALF_UP)
            } else {
                BigDecimal.ZERO
            }

            // 4. 거래 횟수 (recordColor: false = 보유중, true = 매도완료)
            val totalTrades = dollarRecords.size + yenRecords.size
            val dollarBuyCount = dollarRecords.filter { it.recordColor == false }.size
            val yenBuyCount = yenRecords.filter { it.recordColor == false }.size
            val buyCount = dollarBuyCount + yenBuyCount

            val dollarSellCount = dollarRecords.filter { it.recordColor == true }.size
            val yenSellCount = yenRecords.filter { it.recordColor == true }.size
            val sellCount = dollarSellCount + yenSellCount

            Log.d("MyPageViewModel", "총 투자금: $totalInvestment, 예상 수익: $totalProfit, 수익률: $profitRate%")

            InvestmentStats(
                totalInvestment = formatCurrency(totalInvestment),
                expectedProfit = formatCurrency(totalProfit),
                profitRate = formatProfitRate(profitRate),
                totalTrades = totalTrades,
                buyCount = buyCount,
                sellCount = sellCount
            )
        }
            .flowOn(Dispatchers.Default)  // ✅ 계산 작업은 Default 스레드
            .collect { stats ->
                _myPageUiState.update {
                    it.copy(investmentStats = stats)
                }
            }
    }

    // 숫자 포맷팅 (₩10,500,000)
    private fun formatCurrency(amount: BigDecimal): String {
        val absAmount = amount.abs()
        val formatted = "%,.0f".format(absAmount)
        return when {
            amount > BigDecimal.ZERO -> "+₩$formatted"
            amount < BigDecimal.ZERO -> "-₩$formatted"
            else -> "₩$formatted"
        }
    }

    // 수익률 포맷팅 (+8.2% 또는 -3.5%)
    private fun formatProfitRate(rate: BigDecimal): String {
        return when {
            rate > BigDecimal.ZERO -> "+${rate}%"
            rate < BigDecimal.ZERO -> "${rate}%"
            else -> "0.0%"
        }
    }

    // ✅ 최근 활동 수집
    private suspend fun collectRecentActivities() {
        combine(
            investRepository.getAllDollarBuyRecords(),
            investRepository.getAllYenBuyRecords()
        ) { dollarRecords, yenRecords ->

            // 모든 거래를 RecentActivity로 변환
            val dollarActivities = dollarRecords.map { record ->
                RecentActivity(
                    date = record.date ?: "",
                    currencyType = "USD",
                    isBuy = record.recordColor != true,  // false = 매수, true = 매도
                    amount = formatActivityAmount(record.money, record.rate, "USD"),
                    profit = if (record.recordColor == true) formatProfit(record.sellProfit) else null
                )
            }

            val yenActivities = yenRecords.map { record ->
                RecentActivity(
                    date = record.date ?: "",
                    currencyType = "JPY",
                    isBuy = record.recordColor != true,
                    amount = formatActivityAmount(record.money, record.rate, "JPY"),
                    profit = if (record.recordColor == true) formatProfit(record.sellProfit) else null
                )
            }

            // 합치고 날짜순 정렬 후 최근 5건
            (dollarActivities + yenActivities)
                .sortedByDescending { parseDate(it.date) }
                .take(5)

        }
            .flowOn(Dispatchers.Default)  // ✅ 정렬/변환 작업은 Default 스레드
            .collect { activities ->
                _myPageUiState.update {
                    it.copy(recentActivities = activities)
                }
            }
    }

    // 활동 금액 포맷팅 (₩2,000,000 / 1,320원)
    private fun formatActivityAmount(money: String?, rate: String?, currencyType: String): String {
        val formattedMoney = money?.replace(",", "")?.toBigDecimalOrNull()?.let {
            "₩" + "%,d".format(it.toLong())
        } ?: "₩0"

        val formattedRate = rate?.replace(",", "")?.toBigDecimalOrNull()?.let {
            "%,d".format(it.toLong()) + "원"
        } ?: "0원"

        return "$formattedMoney / $formattedRate"
    }

    // 수익 포맷팅 (+₩12,346)
    private fun formatProfit(profit: String?): String? {
        return profit?.replace(",", "")?.toBigDecimalOrNull()?.let { amount ->
            val roundedAmount = amount.setScale(0, RoundingMode.HALF_UP)
            val formatted = "%,d".format(roundedAmount.toLong())
            when {
                roundedAmount > BigDecimal.ZERO -> "+₩$formatted"
                roundedAmount < BigDecimal.ZERO -> "-₩${formatted.replace("-", "")}"
                else -> "₩0"
            }
        }
    }

    // 날짜 파싱 (yyyy-MM-dd 형식)
    private fun parseDate(dateString: String): Long {
        return try {
            val parts = dateString.split("-")
            if (parts.size == 3) {
                val year = parts[0].toLong()
                val month = parts[1].toLong()
                val day = parts[2].toLong()
                year * 10000 + month * 100 + day
            } else 0L
        } catch (e: Exception) {
            0L
        }
    }


    private suspend fun calculateGoalProgress() {
        combine(
            userRepository.userData,
            investRepository.getAllDollarBuyRecords(),
            investRepository.getAllYenBuyRecords()
        ) { userData, dollarRecords, yenRecords ->

            val localUser = userData?.localUserData
            val goalAmount = localUser?.monthlyProfitGoal ?: 0L
            val goalMonth = localUser?.goalSetMonth

            if (goalAmount <= 0L) {
                // 목표가 설정되지 않음
                return@combine MonthlyGoal(
                    goalAmount = 0L,
                    currentAmount = 0L,
                    progress = 0f,
                    isSet = false
                )
            }

            // 현재 월
            val currentMonth = getCurrentYearMonth()

            // 목표 월과 현재 월이 다르면 초기화 필요
            if (goalMonth != currentMonth) {
                // 새 달이 시작되면 목표 리셋 (자동)
                return@combine MonthlyGoal(
                    goalAmount = goalAmount,
                    currentAmount = 0L,
                    progress = 0f,
                    isSet = true
                )
            }

            // 이번 달 매도 수익 계산
            val monthlyProfit = calculateMonthlyProfit(dollarRecords, yenRecords, currentMonth)

            // 달성률 계산
            val progress = if (goalAmount > 0) {
                (monthlyProfit.toFloat() / goalAmount.toFloat()).coerceIn(0f, 1f)
            } else 0f

            MonthlyGoal(
                goalAmount = goalAmount,
                currentAmount = monthlyProfit,
                progress = progress,
                isSet = true
            )

        }.collect { goal ->
            _myPageUiState.update {
                it.copy(monthlyGoal = goal)
            }
        }
    }

    // 이번 달 매도 수익 계산
    private fun calculateMonthlyProfit(
        dollarRecords: List<com.bobodroid.myapplication.models.datamodels.roomDb.DrBuyRecord>,
        yenRecords: List<com.bobodroid.myapplication.models.datamodels.roomDb.YenBuyRecord>,
        targetMonth: String
    ): Long {
        val dollarProfit = dollarRecords
            .filter { it.recordColor == true && it.sellDate?.startsWith(targetMonth) == true }
            .sumOf { it.sellProfit?.replace(",", "")?.toBigDecimalOrNull() ?: BigDecimal.ZERO }

        val yenProfit = yenRecords
            .filter { it.recordColor == true && it.sellDate?.startsWith(targetMonth) == true }
            .sumOf { it.sellProfit?.replace(",", "")?.toBigDecimalOrNull() ?: BigDecimal.ZERO }

        return (dollarProfit + yenProfit).setScale(0, RoundingMode.HALF_UP).toLong()
    }

    // 현재 년-월 (yyyy-MM 형식)
    private fun getCurrentYearMonth(): String {
        val calendar = java.util.Calendar.getInstance()
        val year = calendar.get(java.util.Calendar.YEAR)
        val month = calendar.get(java.util.Calendar.MONTH) + 1
        return "%04d-%02d".format(year, month)
    }

    // ⭐ 목표 설정
    fun setMonthlyGoal(goalAmount: Long) {
        viewModelScope.launch {
            val currentUser = _myPageUiState.value.localUser
            val updatedUser = currentUser.copy(
                monthlyProfitGoal = goalAmount,
                goalSetMonth = getCurrentYearMonth()
            )

            userRepository.localUserUpdate(updatedUser)

            Log.d("MyPageViewModel", "목표 설정 완료: ₩$goalAmount")
        }
    }

    // ========== 기존 함수들 ==========

    fun createUser(customId: String, pin: String, resultMessage: (String) -> Unit) {
        viewModelScope.launch {
            val currentLocalUser = _myPageUiState.value.localUser

            Log.d("MyPageViewModel", "createUser - deviceId: ${currentLocalUser.id}")

            userUseCases.customIdCreateUser(
                localUserData = currentLocalUser,
                customId = customId,
                pin = pin
            )
                .onSuccess { updateData, _ ->
                    _myPageUiState.update { it.copy(localUser = updateData) }
                    resultMessage("성공적으로 아이디가 생성되었습니다.")
                }
                .onError { _ ->
                    resultMessage("아이디 생성이 실패되었습니다.")
                }
        }
    }

    fun logIn(id: String, pin: String, successFind: (message: String) -> Unit) {
        viewModelScope.launch {
            userUseCases.logIn(_myPageUiState.value.localUser, id, pin)
                .onSuccess { localData, msg ->
                    _myPageUiState.update { it.copy(localUser = localData) }
                    successFind(msg ?: "로그인 성공")
                }
                .onError { error ->
                    successFind("로그인 실패")
                }
        }
    }

    fun logout(result: (message: String) -> Unit) {
        viewModelScope.launch {
            userUseCases.logout(_myPageUiState.value.localUser)
                .onSuccess { updateData, _ ->
                    _myPageUiState.update { it.copy(localUser = updateData) }
                    result("로그아웃 성공")
                }
                .onError { _ ->
                    result("로그아웃 실패")
                }
        }
    }
}

// 통계 데이터 클래스
data class InvestmentStats(
    val totalInvestment: String = "₩0",
    val expectedProfit: String = "₩0",
    val profitRate: String = "0.0%",
    val totalTrades: Int = 0,
    val buyCount: Int = 0,
    val sellCount: Int = 0
)

// 최근 활동 데이터 클래스
data class RecentActivity(
    val date: String,
    val currencyType: String,  // "USD" or "JPY"
    val isBuy: Boolean,        // true = 매수, false = 매도
    val amount: String,        // "$1,000 @ 1,320원"
    val profit: String? = null // 매도인 경우만
)


// ⭐ 이번 달 목표 데이터 클래스
data class MonthlyGoal(
    val goalAmount: Long = 0L,        // 목표 금액
    val currentAmount: Long = 0L,      // 현재 달성 금액
    val progress: Float = 0f,          // 달성률 (0.0 ~ 1.0)
    val isSet: Boolean = false         // 목표 설정 여부
)


// UiState
data class MyPageUiState(
    val localUser: LocalUserData = LocalUserData(),
    val investmentStats: InvestmentStats = InvestmentStats(),
    val recentActivities: List<RecentActivity> = emptyList(),
    val monthlyGoal: MonthlyGoal = MonthlyGoal()
)