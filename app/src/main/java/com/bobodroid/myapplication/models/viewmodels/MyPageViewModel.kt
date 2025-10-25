package com.bobodroid.myapplication.models.viewmodels

import FormatUtils.formatCurrency
import android.app.Activity
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bobodroid.myapplication.data.mapper.RecordMapper.toLegacyRecordList
import com.bobodroid.myapplication.domain.entity.BadgeEntity
import com.bobodroid.myapplication.domain.entity.InvestmentStatsEntity
import com.bobodroid.myapplication.domain.entity.MonthlyGoalEntity
import com.bobodroid.myapplication.domain.repository.IRecordRepository
import com.bobodroid.myapplication.domain.repository.IUserRepository
import com.bobodroid.myapplication.domain.usecase.statistics.CalculateBadgesUseCase
import com.bobodroid.myapplication.domain.usecase.statistics.CalculateInvestmentStatsUseCase
import com.bobodroid.myapplication.domain.usecase.statistics.CalculateMonthlyGoalUseCase
import com.bobodroid.myapplication.models.datamodels.roomDb.Currencies
import com.bobodroid.myapplication.models.datamodels.roomDb.CurrencyRecord
import com.bobodroid.myapplication.models.datamodels.roomDb.LocalUserData
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
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.math.RoundingMode
import javax.inject.Inject

@HiltViewModel
class MyPageViewModel @Inject constructor(
    private val userRepository: IUserRepository,
    private val recordRepository: IRecordRepository,
    private val socialLoginUseCases: SocialLoginUseCases,
    private val userUseCases: UserUseCases,
    private val accountSwitchUseCase: AccountSwitchUseCase,
    private val calculateInvestmentStatsUseCase: CalculateInvestmentStatsUseCase,
    private val calculateMonthlyGoalUseCase: CalculateMonthlyGoalUseCase,
    private val calculateBadgesUseCase: CalculateBadgesUseCase
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


    fun syncToServer(result: (String) -> Unit) {
        viewModelScope.launch {
            val localUser = _myPageUiState.value.localUser

            socialLoginUseCases.syncToServer(localUser)
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
        // ⭐ 12개 통화 모두 가져오기
        recordRepository.getAllRecords()
            .map { records ->
                // ⭐ UseCase 호출
                calculateInvestmentStatsUseCase.execute(records.toLegacyRecordList())
            }
            .flowOn(Dispatchers.Default)
            .collectLatest { stats ->
                _myPageUiState.update {
                    it.copy(investmentStats = stats)
                }
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
        recordRepository.getAllRecords()  // ⭐ 12개 통화 모두 가져오기
            .map { records ->
                // ⭐ 모든 기록을 RecentActivity로 변환
                records.map { record ->
                    RecentActivity(
                        date = record.date ?: "",
                        currencyType = record.currencyCode,  // ⭐ USD, JPY, EUR 등 동적 처리
                        isBuy = record.recordColor != true,
                        amount = formatActivityAmount(record.money, record.rate, record.currencyCode),
                        profit = if (record.recordColor == true) formatProfit(record.sellProfit) else null
                    )
                }
                    .sortedByDescending { parseDate(it.date) }  // 날짜순 정렬
                    .take(5)  // 최근 5개만
            }
            .flowOn(Dispatchers.Default)
            .collectLatest { activities ->
                _myPageUiState.update {
                    it.copy(recentActivities = activities)
                }
            }
    }



    /**
     * ✅ 활동 금액 포맷팅 (12개 통화 지원)
     *
     * @param money 외화 금액
     * @param rate 환율
     * @param currencyCode 통화 코드 (USD, JPY, EUR 등)
     */
    private fun formatActivityAmount(money: String?, rate: String?, currencyCode: String): String {
        val moneyValue = money?.toFloatOrNull() ?: 0f
        val rateValue = rate?.toFloatOrNull() ?: 0f

        // ⭐ Currency 정보 가져오기
        val currency = Currencies.findByCode(currencyCode)
        val symbol = currency?.symbol ?: currencyCode

        return when {
            moneyValue == 0f -> "$symbol 0"
            rateValue == 0f -> "$symbol ${formatCurrency(moneyValue.toBigDecimal())}"
            else -> {
                val krwAmount = moneyValue * rateValue
                "$symbol ${formatCurrency(moneyValue.toBigDecimal())} (₩${formatCurrency(krwAmount.toBigDecimal())})"
            }
        }
    }

    /**
     * ✅ 수익 포맷팅
     */
    private fun formatProfit(profit: String?): String {
        val profitValue = profit?.toFloatOrNull() ?: return "₩0"
        val formatted = formatCurrency(profitValue.toBigDecimal())
        return when {
            profitValue > 0 -> "+₩$formatted"
            profitValue < 0 -> "-₩${formatted.removePrefix("-")}"
            else -> "₩0"
        }
    }

    /**
     * ✅ 날짜 파싱 (정렬용)
     */
    private fun parseDate(dateStr: String): Long {
        return try {
            // 날짜 형식에 맞게 파싱 (예: "2024.01.15")
            val parts = dateStr.split(".")
            if (parts.size == 3) {
                val year = parts[0].toInt()
                val month = parts[1].toInt()
                val day = parts[2].toInt()
                year * 10000L + month * 100L + day
            } else {
                0L
            }
        } catch (e: Exception) {
            0L
        }
    }









    private suspend fun calculateGoalProgress() {
        combine(
            recordRepository.getAllRecords(),  // ⭐ 12개 통화
            userRepository.userData
        ) { allRecords, userData ->
            val localUser = userData?.localUserData
            val goalAmount = localUser?.monthlyProfitGoal ?: 0L
            val goalMonth = localUser?.goalSetMonth
            val currentMonth = getCurrentYearMonth()

            // ⭐ UseCase 호출
            calculateMonthlyGoalUseCase.execute(
                allRecords = allRecords.toLegacyRecordList(),
                goalAmount = goalAmount,
                goalMonth = goalMonth,
                currentMonth = currentMonth
            )
        }
            .flowOn(Dispatchers.Default)
            .collectLatest { goal ->
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
       recordRepository.getAllRecords()  // ⭐ 12개 통화
            .map { records ->
                // ⭐ UseCase 호출
                calculateBadgesUseCase.execute(records.toLegacyRecordList())
            }
            .flowOn(Dispatchers.Default)
            .collectLatest { badges ->
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



// 최근 활동 데이터 클래스
data class RecentActivity(
    val date: String,
    val currencyType: String,  // "USD" or "JPY"
    val isBuy: Boolean,        // true = 매수, false = 매도
    val amount: String,        // "$1,000 @ 1,320원"
    val profit: String? = null // 매도인 경우만
)



// UiState
data class MyPageUiState(
    val localUser: LocalUserData = LocalUserData(),
    val investmentStats: InvestmentStatsEntity = InvestmentStatsEntity(),
    val recentActivities: List<RecentActivity> = emptyList(),
    val monthlyGoal: MonthlyGoalEntity = MonthlyGoalEntity(),
    val badges: List<BadgeEntity> = emptyList(),

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