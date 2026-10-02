package com.bobodroid.myapplication.models.viewmodels

import android.app.Activity
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bobodroid.myapplication.models.datamodels.repository.InvestRepository
import com.bobodroid.myapplication.models.datamodels.repository.UserRepository
import com.bobodroid.myapplication.models.datamodels.roomDb.CurrencyRecord
import com.bobodroid.myapplication.models.datamodels.roomDb.LocalUserData
import com.bobodroid.myapplication.models.datamodels.service.BackupApi.BackupNoticeManager
import com.bobodroid.myapplication.models.datamodels.service.BackupApi.BackupNoticeStatus
import com.bobodroid.myapplication.models.datamodels.service.BackupApi.BackupSyncPrefs
import com.bobodroid.myapplication.models.datamodels.useCases.AccountFoundException
import com.bobodroid.myapplication.models.datamodels.useCases.AccountSwitchUseCase
import com.bobodroid.myapplication.models.datamodels.useCases.DeleteUserUseCase
import com.bobodroid.myapplication.models.datamodels.useCases.SocialLoginUseCases
import com.bobodroid.myapplication.models.datamodels.useCases.UserUseCases
import com.bobodroid.myapplication.util.result.onError
import com.bobodroid.myapplication.util.result.onSuccess
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.math.RoundingMode
import javax.inject.Inject

@HiltViewModel
class MyPageViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val investRepository: InvestRepository,
    private val socialLoginUseCases: SocialLoginUseCases,
    private val userUseCases: UserUseCases,
    private val accountSwitchUseCase: AccountSwitchUseCase,
    private val backupSyncPrefs: BackupSyncPrefs,
    backupNoticeManager: BackupNoticeManager
) : ViewModel() {

    val _myPageUiState = MutableStateFlow(MyPageUiState())
    val myPageUiState = _myPageUiState.asStateFlow()

    // 서버에 이 기기에 없는 기록이 있어 자동 백업이 보류된 상태 (클라우드 백업 화면 안내용)
    val backupHeld: StateFlow<Boolean> = backupSyncPrefs.held

    // 백업이 되고 있지 않은 상태 (마이페이지 클라우드 백업 배지용)
    val backupNoticeStatus: StateFlow<BackupNoticeStatus> = backupNoticeManager.status

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

        viewModelScope.launch {
            calculateGoalProgress()
        }

        viewModelScope.launch {
            calculateBadges()
        }
    }


    // 3. Google 로그인 수정 (계정 발견 처리)
    fun loginWithGoogle(activity: Activity, onAccountFound: (AccountFoundInfo) -> Unit, onComplete: (String) -> Unit) {
        viewModelScope.launch {
            val localUser = _myPageUiState.value.localUser

            socialLoginUseCases.googleLogin(activity, localUser)
                .onSuccess { updatedUser, message ->
                    Log.d("MyPageViewModel", "Google 로그인 성공: ${updatedUser.email}")
                    onComplete(message ?: "Google 로그인 성공!")
                }
                .onError { error ->
                    Log.e("MyPageViewModel", "Google 로그인 결과: ${error.message}", error.exception)

                    // ✅ 계정 발견 체크
                    if (error.message == "ACCOUNT_FOUND" && error.exception is AccountFoundException) {
                        val foundException = error.exception as AccountFoundException
                        val accountInfo = AccountFoundInfo(
                            serverDeviceId = foundException.serverDeviceId,
                            email = foundException.email,
                            nickname = foundException.nickname,
                            lastSyncAt = foundException.lastSyncAt
                        )

                        // ✅ UI State 업데이트
                        _myPageUiState.update {
                            it.copy(
                                foundAccount = accountInfo,
                                showAccountFoundDialog = true
                            )
                        }

                        onAccountFound(accountInfo)
                    } else {
                        onComplete(error.message)
                    }
                }
        }
    }

    // 4. Kakao 로그인 수정 (계정 발견 처리)
    fun loginWithKakao(activity: Activity, onAccountFound: (AccountFoundInfo) -> Unit, onComplete: (String) -> Unit) {
        viewModelScope.launch {
            val localUser = _myPageUiState.value.localUser

            socialLoginUseCases.kakaoLogin(activity, localUser)
                .onSuccess { updatedUser, message ->
                    Log.d("MyPageViewModel", "Kakao 로그인 성공: ${updatedUser.email}")
                    onComplete(message ?: "Kakao 로그인 성공!")
                }
                .onError { error ->
                    Log.e("MyPageViewModel", "Kakao 로그인 결과: ${error.message}", error.exception)

                    // ✅ 계정 발견 체크
                    if (error.message == "ACCOUNT_FOUND" && error.exception is AccountFoundException) {
                        val foundException = error.exception as AccountFoundException
                        val accountInfo = AccountFoundInfo(
                            serverDeviceId = foundException.serverDeviceId,
                            email = foundException.email,
                            nickname = foundException.nickname,
                            lastSyncAt = foundException.lastSyncAt
                        )

                        _myPageUiState.update {
                            it.copy(
                                foundAccount = accountInfo,
                                showAccountFoundDialog = true
                            )
                        }

                        onAccountFound(accountInfo)
                    } else {
                        onComplete(error.message)
                    }
                }
        }
    }


    fun logout(result: (String) -> Unit) {
        viewModelScope.launch {
            val localUser = _myPageUiState.value.localUser

            socialLoginUseCases.socialLogout(localUser)
                .onSuccess { _, message ->
                    Log.d("MyPageViewModel", "로그아웃 성공")
                    result(message ?: "로그아웃되었습니다")
                }
                .onError { error ->  // ✅ Result.Error 객체로 받음
                    Log.e("MyPageViewModel", "로그아웃 실패: ${error.message}", error.exception)
                    result(error.message)
                }
        }
    }

    /**
     * 회원 탈퇴
     */
    fun deleteAccount(result: (String) -> Unit) {
        viewModelScope.launch {
            val localUser = _myPageUiState.value.localUser

            if (localUser.id.toString().isEmpty()) {
                result("사용자 정보를 찾을 수 없습니다")
                return@launch
            }

            userUseCases.deleteUser(localUser)
                .onSuccess { _, message ->
                    Log.d("MyPageViewModel", "✅ 회원 탈퇴 성공")

                    // UI 상태 초기화
                    _myPageUiState.update { currentState ->
                        currentState.copy(
                            localUser = LocalUserData()
                        )
                    }

                    result(message ?: "회원 탈퇴가 완료되었습니다")
                }
                .onError { error ->
                    Log.e("MyPageViewModel", "❌ 회원 탈퇴 실패: ${error.message}", error.exception)
                    result(error.message)
                }
        }
    }


    /**
     * @param force true이면 서버 기록이 이 기기와 달라도 이 기기 기록으로 덮어쓴다
     *              (사용자가 화면에서 덮어쓰기를 확인한 경우에만 사용)
     */
    fun syncToServer(force: Boolean = false, result: (String) -> Unit) {
        viewModelScope.launch {
            val localUser = _myPageUiState.value.localUser

            socialLoginUseCases.syncToServer(localUser, force)
                .onSuccess { updatedUser, message ->
                    Log.d("MyPageViewModel", "백업 성공")

                    // ✅ UI 상태 업데이트 (이미 lastSyncAt이 포함된 updatedUser)
                    _myPageUiState.update { currentState ->
                        currentState.copy(
                            localUser = updatedUser
                        )
                    }

                    result(message ?: "데이터가 백업되었습니다")
                }
                .onError { error ->
                    Log.e("MyPageViewModel", "백업 실패: ${error.message}", error.exception)
                    result(error.message)
                }
        }
    }

    // ✅ 복구 기능 추가
    fun restoreFromServer(result: (String) -> Unit) {
        viewModelScope.launch {
            val localUser = _myPageUiState.value.localUser

            socialLoginUseCases.restoreFromServer(
                deviceId = localUser.id.toString(),
                socialId = localUser.socialId,
                socialType = localUser.socialType
            )
                .onSuccess { recordCount, message ->
                    Log.d("MyPageViewModel", "복구 성공: ${recordCount}개")

                    // ❌ 삭제: 아래 4줄 제거
                    // calculateInvestmentStats()
                    // collectRecentActivities()
                    // calculateGoalProgress()
                    // calculateBadges()

                    // ✅ Flow가 자동으로 감지하므로 별도 호출 불필요
                    result(message ?: "데이터를 복구했습니다")
                }
                .onError { error ->
                    Log.e("MyPageViewModel", "복구 실패: ${error.message}", error.exception)
                    result(error.message)
                }
        }
    }


    fun unlinkSocial(result: (String) -> Unit) {
        viewModelScope.launch {
            val localUser = _myPageUiState.value.localUser

            socialLoginUseCases.unlinkSocial(localUser)
                .onSuccess { _, message ->
                    Log.d("MyPageViewModel", "소셜 연동 해제 성공")
                    result(message ?: "소셜 연동이 해제되었습니다")
                }
                .onError { error ->
                    Log.e("MyPageViewModel", "소셜 연동 해제 실패: ${error.message}", error.exception)
                    result(error.message)
                }
        }
    }



    private suspend fun calculateInvestmentStats() {
        combine(
            investRepository.getAllDollarBuyRecords(),
            investRepository.getAllYenBuyRecords()
        ) { dollarRecords, yenRecords ->

            // 기존 계산 로직 그대로 유지
            Log.d("MyPageViewModel", "달러 기록: ${dollarRecords.size}개, 엔화 기록: ${yenRecords.size}개")

            val totalDollarInvestment = dollarRecords.sumOf {
                it.money?.replace(",", "")?.toBigDecimalOrNull() ?: BigDecimal.ZERO
            }
            val totalYenInvestment = yenRecords.sumOf {
                it.money?.replace(",", "")?.toBigDecimalOrNull() ?: BigDecimal.ZERO
            }
            val totalInvestment = totalDollarInvestment + totalYenInvestment

            val totalDollarProfit = dollarRecords.sumOf {
                it.expectProfit?.replace(",", "")?.toBigDecimalOrNull() ?: BigDecimal.ZERO
            }
            val totalYenProfit = yenRecords.sumOf {
                it.expectProfit?.replace(",", "")?.toBigDecimalOrNull() ?: BigDecimal.ZERO
            }
            val totalProfit = totalDollarProfit + totalYenProfit

            val profitRate = if (totalInvestment > BigDecimal.ZERO) {
                (totalProfit.divide(totalInvestment, 4, RoundingMode.HALF_UP) * BigDecimal(100))
                    .setScale(1, RoundingMode.HALF_UP)
            } else {
                BigDecimal.ZERO
            }

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
            .flowOn(Dispatchers.Default)
            .collectLatest { stats ->  // ← collect를 collectLatest로 변경
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

            // 기존 로직 그대로 유지
            val dollarActivities = dollarRecords.map { record ->
                RecentActivity(
                    date = record.date ?: "",
                    currencyType = "USD",
                    isBuy = record.recordColor != true,
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

            (dollarActivities + yenActivities)
                .sortedByDescending { parseDate(it.date) }
                .take(5)
        }
            .flowOn(Dispatchers.Default)
            .collectLatest { activities ->  // ← collect를 collectLatest로 변경
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
            investRepository.getAllCurrencyRecords()  // ✅ 통합 메서드 사용
        ) { userData, allRecords ->

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

            // ✅ 이번 달 매도 수익 계산 (기존 함수 이름 사용)
            val monthlyProfit = calculateMonthlyProfit(allRecords, currentMonth)

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
        }
            .flowOn(Dispatchers.Default)
            .collectLatest { goal ->  // ← collect를 collectLatest로 변경
                _myPageUiState.update {
                    it.copy(monthlyGoal = goal)
                }
            }
    }

    /**
     * 이번 달 매도 수익 계산 (통합 버전)
     */
    private fun calculateMonthlyProfit(
        records: List<CurrencyRecord>,
        targetMonth: String
    ): Long {
        val totalProfit = records
            .filter {
                it.recordColor == true &&
                        it.sellDate?.startsWith(targetMonth) == true
            }
            .sumOf {
                it.sellProfit?.replace(",", "")?.toBigDecimalOrNull() ?: BigDecimal.ZERO
            }

        return totalProfit.setScale(0, RoundingMode.HALF_UP).toLong()
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
            Log.d("MyPageViewModel", "유저 데이터: ₩$updatedUser")
            Log.d("MyPageViewModel", "목표 설정 완료: ₩$goalAmount")
        }
    }


    private suspend fun calculateBadges() {
        combine(
            investRepository.getAllDollarBuyRecords(),
            investRepository.getAllYenBuyRecords()
        ) { dollarRecords, yenRecords ->

            // 기존 로직 그대로 유지
            val totalTrades = dollarRecords.size + yenRecords.size
            val sellCount = dollarRecords.count { it.recordColor == true } +
                    yenRecords.count { it.recordColor == true }

            val totalInvestment = (dollarRecords + yenRecords).sumOf {
                it.money?.replace(",", "")?.toBigDecimalOrNull()?.toLong() ?: 0L
            }

            val totalProfit = (dollarRecords + yenRecords).sumOf {
                it.expectProfit?.replace(",", "")?.toBigDecimalOrNull()?.toLong() ?: 0L
            }

            listOf(
                BadgeInfo(
                    type = BadgeType.FIRST_TRADE,
                    icon = "🎯",
                    title = "첫 거래",
                    description = "첫 환전 기록을 생성했습니다",
                    isUnlocked = totalTrades >= 1,
                    progress = if (totalTrades >= 1) 100 else 0,
                    currentValue = totalTrades,
                    targetValue = 1
                ),
                BadgeInfo(
                    type = BadgeType.TRADER_50,
                    icon = "📈",
                    title = "트레이더",
                    description = "총 50회 거래를 달성했습니다",
                    isUnlocked = totalTrades >= 50,
                    progress = ((totalTrades.toFloat() / 50f) * 100).toInt().coerceIn(0, 100),
                    currentValue = totalTrades,
                    targetValue = 50
                ),
                BadgeInfo(
                    type = BadgeType.TRADER_100,
                    icon = "🏆",
                    title = "마스터 트레이더",
                    description = "총 100회 거래를 달성했습니다",
                    isUnlocked = totalTrades >= 100,
                    progress = ((totalTrades.toFloat() / 100f) * 100).toInt().coerceIn(0, 100),
                    currentValue = totalTrades,
                    targetValue = 100
                ),
                BadgeInfo(
                    type = BadgeType.INVESTMENT_1M,
                    icon = "💰",
                    title = "백만장자",
                    description = "총 투자금 100만원을 달성했습니다",
                    isUnlocked = totalInvestment >= 1_000_000L,
                    progress = ((totalInvestment.toFloat() / 1_000_000f) * 100).toInt().coerceIn(0, 100),
                    currentValue = (totalInvestment / 10000).toInt(),
                    targetValue = 100
                ),
                BadgeInfo(
                    type = BadgeType.INVESTMENT_10M,
                    icon = "💎",
                    title = "천만장자",
                    description = "총 투자금 1,000만원을 달성했습니다",
                    isUnlocked = totalInvestment >= 10_000_000L,
                    progress = ((totalInvestment.toFloat() / 10_000_000f) * 100).toInt().coerceIn(0, 100),
                    currentValue = (totalInvestment / 10000).toInt(),
                    targetValue = 1000
                )
            )
        }
            .flowOn(Dispatchers.Default)
            .collectLatest { badges ->  // ← collect를 collectLatest로 변경
                _myPageUiState.update {
                    it.copy(badges = badges)
                }
            }
    }

    // 5. 기존 계정 사용 (계정 전환) - 수정됨
    fun useExistingAccount(onComplete: (String) -> Unit) {
        viewModelScope.launch {
            val foundAccount = _myPageUiState.value.foundAccount
            val localUser = _myPageUiState.value.localUser

            if (foundAccount == null) {
                onComplete("계정 정보를 찾을 수 없습니다")
                return@launch
            }

            // ✅ 계정 전환
            accountSwitchUseCase(
                serverDeviceId = foundAccount.serverDeviceId,
                localUser = localUser
            )
                .onSuccess { result, message ->
                    Log.d("MyPageViewModel", "계정 전환 성공")
                    Log.d("MyPageViewModel", "  - 백업 데이터 존재: ${result.hasBackupData}")
                    Log.d("MyPageViewModel", "  - 백업 기록 수: ${result.backupRecordCount}")

                    // ✅ 백업 데이터가 있는 경우에만 복원 다이얼로그 표시
                    if (result.hasBackupData && result.backupRecordCount > 0) {
                        Log.d("MyPageViewModel", "✅ 백업 데이터 있음 → 복원 다이얼로그 표시")

                        _myPageUiState.update {
                            it.copy(
                                localUser = result.switchedUser,
                                showAccountFoundDialog = false,
                                showDataRestoreDialog = true,  // ✅ 복원 다이얼로그 표시
                                backupInfo = BackupInfo(
                                    recordCount = result.backupRecordCount,
                                    lastBackupAt = result.lastBackupAt
                                )
                            )
                        }

                        onComplete("계정이 전환되었습니다.")
                    } else {
                        Log.d("MyPageViewModel", "ℹ️ 백업 데이터 없음 → 바로 완료")

                        // ✅ 백업 데이터가 없으면 바로 완료
                        _myPageUiState.update {
                            it.copy(
                                localUser = result.switchedUser,
                                showAccountFoundDialog = false,
                                showDataRestoreDialog = false,
                                foundAccount = null
                            )
                        }

                        onComplete("계정이 전환되었습니다 (백업 데이터 없음)")
                    }
                }
                .onError { error ->
                    Log.e("MyPageViewModel", "계정 전환 실패: ${error.message}", error.exception)

                    _myPageUiState.update {
                        it.copy(
                            showAccountFoundDialog = false,
                            foundAccount = null
                        )
                    }

                    onComplete(error.message)
                }
        }
    }



    // 6. 새 계정으로 시작
    fun createNewAccount(onComplete: (String) -> Unit) {
        viewModelScope.launch {
            Log.d("MyPageViewModel", "새 계정으로 시작 (서버 계정 무시)")

            // ✅ 다이얼로그 닫기
            _myPageUiState.update {
                it.copy(
                    showAccountFoundDialog = false,
                    foundAccount = null
                )
            }

            onComplete("새 계정으로 시작합니다")
        }
    }

    // 7. 백업 데이터 복원
    fun restoreBackupData(onComplete: (String) -> Unit) {
        viewModelScope.launch {
            val localUser = _myPageUiState.value.localUser

            socialLoginUseCases.restoreFromServer(localUser.id.toString())
                .onSuccess { _, message ->
                    Log.d("MyPageViewModel", "백업 데이터 복원 성공")

                    _myPageUiState.update {
                        it.copy(
                            showDataRestoreDialog = false,
                            foundAccount = null
                        )
                    }

                    onComplete(message ?: "데이터 복원 완료!")
                }
                .onError { error ->
                    Log.e("MyPageViewModel", "데이터 복원 실패: ${error.message}", error.exception)

                    _myPageUiState.update {
                        it.copy(showDataRestoreDialog = false)
                    }

                    onComplete("데이터 복원 실패: ${error.message}")
                }
        }
    }

    // 8. 로컬 데이터 사용
    fun useLocalData(onComplete: (String) -> Unit) {
        viewModelScope.launch {
            Log.d("MyPageViewModel", "로컬 데이터 사용 선택")

            _myPageUiState.update {
                it.copy(
                    showDataRestoreDialog = false,
                    foundAccount = null
                )
            }

            onComplete("로컬 데이터를 사용합니다")
        }
    }

    // 9. 다이얼로그 닫기
    fun dismissAccountFoundDialog() {
        _myPageUiState.update {
            it.copy(
                showAccountFoundDialog = false,
                foundAccount = null
            )
        }
    }

    fun dismissDataRestoreDialog() {
        _myPageUiState.update {
            it.copy(showDataRestoreDialog = false)
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

enum class BadgeType {
    FIRST_TRADE,
    TRADER_50,
    TRADER_100,
    FIRST_PROFIT,
    PROFIT_RATE_10,
    PROFIT_RATE_20,
    INVESTMENT_1M,
    INVESTMENT_10M,
    STREAK_7,
    STREAK_30
}

data class BadgeInfo(
    val type: BadgeType,
    val icon: String,
    val title: String,
    val description: String,
    val isUnlocked: Boolean,
    val progress: Int,
    val currentValue: Int,
    val targetValue: Int
)


// UiState
data class MyPageUiState(
    val localUser: LocalUserData = LocalUserData(),
    val investmentStats: InvestmentStats = InvestmentStats(),
    val recentActivities: List<RecentActivity> = emptyList(),
    val monthlyGoal: MonthlyGoal = MonthlyGoal(),
    val badges: List<BadgeInfo> = emptyList(),

    val foundAccount: AccountFoundInfo? = null,
    val showAccountFoundDialog: Boolean = false,
    val showDataRestoreDialog: Boolean = false,
    val backupInfo: BackupInfo? = null
)

// ✅ 계정 발견 정보 데이터 클래스
data class AccountFoundInfo(
    val serverDeviceId: String,
    val email: String?,
    val nickname: String?,
    val lastSyncAt: String?
)



// ✅ 백업 정보 데이터 클래스
data class BackupInfo(
    val recordCount: Int,
    val lastBackupAt: String?
)