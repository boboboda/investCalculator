package com.bobodroid.myapplication.models.viewmodels

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bobodroid.myapplication.BuildConfig
import com.bobodroid.myapplication.MainActivity.Companion.TAG
import com.bobodroid.myapplication.models.datamodels.repository.UserRepository
import com.bobodroid.myapplication.models.datamodels.roomDb.LocalUserData
import com.bobodroid.myapplication.models.datamodels.roomDb.PremiumType
import com.bobodroid.myapplication.premium.PremiumManager
import com.bobodroid.myapplication.util.AdMob.AdUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

/**
 * SharedViewModel
 * - 광고 & 프리미엄 관련 앱 전역 상태 관리
 * - 모든 화면에서 공유되는 ViewModel
 * - 비즈니스 로직은 AdUseCase에 위임
 */
@HiltViewModel
class SharedViewModel @Inject constructor(
    private val premiumManager: PremiumManager,
    private val adUseCase: AdUseCase,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _adUiState = MutableStateFlow(AdUiState())
    val adUiState = _adUiState.asStateFlow()

    // 서버 기준 오늘 리워드 시청 횟수 / 하루 캡
    val rewardAdInfo = premiumManager.rewardAdInfo

    private val _showPremiumPrompt = MutableStateFlow(false)
    val showPremiumPrompt = _showPremiumPrompt.asStateFlow()

    private val _showRewardAdInfo = MutableStateFlow(false)
    val showRewardAdInfo = _showRewardAdInfo.asStateFlow()

    // 광고 재생 중 여부 (중복 호출 방지)
    private var isAdFlowRunning = false

    // 리워드 광고 시청 시작 ~ 서버 반영 완료까지 (시청 버튼 비활성화 / 진행 표시용)
    private val _isRewardProcessing = MutableStateFlow(false)
    val isRewardProcessing = _isRewardProcessing.asStateFlow()

    private fun isAdDialogBusy(): Boolean =
        isAdFlowRunning || _showPremiumPrompt.value || _showRewardAdInfo.value

    private val _snackbarEvent = Channel<String>(Channel.BUFFERED)
    val snackbarEvent = _snackbarEvent.receiveAsFlow()

    // ✅ 추가: 마이페이지를 특정 하위 화면으로 바로 열기 위한 일회성 요청
    //    (예: 메인 스프레드 배지 → 마이페이지 목록을 거치지 않고 스프레드 설정으로 직행)
    private val _myPageStartRoute = MutableStateFlow<String?>(null)
    val myPageStartRoute = _myPageStartRoute.asStateFlow()

    fun requestMyPageRoute(routeName: String) {
        _myPageStartRoute.value = routeName
    }

    fun consumeMyPageRoute() {
        _myPageStartRoute.value = null
    }

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━
    // ✅ 이제 init 블록 (필드 초기화 후)
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━

    init {
        Log.d(TAG("SharedViewModel", "init"), "SharedViewModel 초기화")
        startPremiumExpiryMonitoring()
    }


    // 만료 체크

    // 만료 체크 (REWARD_AD, EVENT만 — 로컬 캐시 기준 즉시 UI 반영용, 서버 진실은 PremiumManager가 주기 동기화)
    private fun startPremiumExpiryMonitoring() {
        viewModelScope.launch {
            while (true) {
                delay(30_000)  // 30초마다 체크

                val user = userRepository.userData.value?.localUserData ?: continue

                // ✅ SUBSCRIPTION은 PremiumManager가 처리하므로 제외
                if (user.premiumType == "SUBSCRIPTION") {
                    continue
                }

                // 현재 프리미엄 상태 확인 (REWARD_AD, EVENT, LIFETIME만)
                val currentType = premiumManager.checkPremiumStatus(user)

                // DB에는 프리미엄인데 실제로는 만료됨
                if (currentType == PremiumType.NONE &&
                    user.premiumType != "NONE" &&
                    !user.premiumExpiryDate.isNullOrEmpty()) {

                    Log.d(TAG("SharedViewModel", "monitoring"),
                        "🔴 프리미엄 만료 감지 (${user.premiumType}) - 자동 초기화")

                    val expiredUser = user.copy(
                        premiumType = "NONE",
                        premiumExpiryDate = null,
                        isPremium = false
                    )

                    userRepository.localUserUpdate(expiredUser)

                    // ✅ 타입별 메시지 구분 (REWARD_AD: 더 이상 "24시간 고정"이 아니므로 문구 일반화)
                    val message = when (user.premiumType) {
                        "REWARD_AD" -> "⏰ 리워드 프리미엄이 만료되었습니다"
                        "EVENT" -> "⏰ 이벤트 프리미엄이 만료되었습니다"
                        else -> "⏰ 프리미엄이 만료되었습니다"
                    }
                    _snackbarEvent.trySend(message)
                }
            }
        }
    }
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━
    // 상태 (Repository에서 구독)
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━

    val user: StateFlow<LocalUserData> = userRepository.userData
        .mapNotNull { it?.localUserData }  // null이 아닌 경우만
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = LocalUserData()
        )

    val isPremium: StateFlow<Boolean> = userRepository.userData
        .map { userData ->
            userData?.localUserData?.let { user ->
                premiumManager.checkPremiumStatus(user) != PremiumType.NONE
            } ?: false
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = false
        )

    /**
     * 프리미엄 타입 (자동 만료 처리 포함)
     */
    val premiumType: StateFlow<PremiumType> = userRepository.userData
        .map { userData ->
            val user = userData?.localUserData ?: return@map PremiumType.NONE

            val currentType = premiumManager.checkPremiumStatus(user)

            // ✅ 만료 감지 시 자동 DB 업데이트
            if (currentType == PremiumType.NONE &&
                user.premiumType != "NONE" &&
                !user.premiumExpiryDate.isNullOrEmpty()) {

                viewModelScope.launch {
                    Log.d(TAG("SharedViewModel", "premiumType"), "🔴 만료 감지 - DB 초기화")

                    val expiredUser = user.copy(
                        premiumType = "NONE",
                        premiumExpiryDate = null,
                        isPremium = false
                    )
                    userRepository.localUserUpdate(expiredUser)
                }
            }

            currentType
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = PremiumType.NONE
        )

    /**
     * 프리미엄 만료 시간
     */
    val premiumExpiryDate: StateFlow<String?> = userRepository.userData
        .map { it?.localUserData?.premiumExpiryDate }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = null
        )



    // ━━━━━━━━━━━━━━━━━━━━━━━━━━
    // 초기화
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━

    init {
        Log.d(TAG("SharedViewModel", "init"), "━━━━━━━━━━━━━━━━━━━━━━━━━━")
        Log.d(TAG("SharedViewModel", "init"), "SharedViewModel 초기화")
        Log.d(TAG("SharedViewModel", "init"), "━━━━━━━━━━━━━━━━━━━━━━━━━━")

        observeAdStates()
        observeDialogStates()
    }

    /**
     * 광고 상태 관찰 및 자동 갱신
     */
    private fun observeAdStates() {
        viewModelScope.launch {
            // 사용자 데이터 또는 서버 시청 횟수 정보가 바뀔 때마다 갱신
            combine(userRepository.userData, premiumManager.rewardAdInfo) { userData, _ ->
                userData
            }.collect { userData ->
                val user = userData?.localUserData ?: return@collect
                val todayDate = getCurrentDate()

                // 리워드 광고 시청 가능 여부 (서버 기준 하루 캡 반영)
                val canShowRewardAd = premiumManager.canUseRewardAdToday(user)

                // 배너 광고 표시 여부
                val shouldShowBanner = adUseCase.bannerAdState(user)

                // UI 상태 업데이트
                _adUiState.update {
                    it.copy(
                        rewardAdState = canShowRewardAd,
                        bannerAdState = shouldShowBanner
                    )
                }

                Log.d(
                    TAG("SharedViewModel", "observeAdStates"),
                    "광고 상태 갱신 - 리워드: $canShowRewardAd, 배너: $shouldShowBanner"
                )
            }
        }
    }

    /**
     * 다이얼로그 상태 관찰 (디버깅용)
     */
    private fun observeDialogStates() {
        viewModelScope.launch {
            showPremiumPrompt.collect { isShowing ->
                Log.d(TAG("SharedViewModel", "observeDialogStates"),
                    "🔔 PremiumPrompt 상태 변경: $isShowing")
            }
        }

        viewModelScope.launch {
            showRewardAdInfo.collect { isShowing ->
                Log.d(TAG("SharedViewModel", "observeDialogStates"),
                    "🔔 RewardAdInfo 상태 변경: $isShowing")
            }
        }
    }

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━
    // 전면 광고 (UseCase 호출)
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━

    /**
     * 전면 광고 표시 (UseCase에 위임)
     * - 다이얼로그 또는 다른 광고가 진행 중이면 건너뜀
     */
    fun showInterstitialAdIfNeeded(context: Context) {
        if (isAdDialogBusy()) {
            Log.d(TAG("SharedViewModel", "showInterstitialAdIfNeeded"), "다이얼로그/광고 진행 중 - 건너뜀")
            return
        }

        viewModelScope.launch {
            val user = userRepository.userData.value?.localUserData ?: run {
                Log.w(TAG("SharedViewModel", "showInterstitialAdIfNeeded"), "❌ 사용자 데이터 없음")
                return@launch
            }

            isAdFlowRunning = true
            try {
                adUseCase.showInterstitialAdIfNeeded(
                    context = context,
                    user = user,
                    onPremiumPromptNeeded = {
                        Log.d(TAG("SharedViewModel", "showInterstitialAdIfNeeded"),
                            "🎯 프리미엄 유도 팝업 트리거됨")
                        _showPremiumPrompt.value = true
                    }
                )
            } finally {
                isAdFlowRunning = false
            }
        }
    }

    /**
     * 프리미엄 필요 화면(잠금 기능)에서 호출하는 단일 진입점
     * - 구독 화면으로 이동하지 않고 리워드 안내 다이얼로그만 연다
     */
    fun requestPremiumUnlock() {
        if (isAdDialogBusy()) return
        _showRewardAdInfo.value = true
    }

    /**
     * 프리미엄 유도 팝업 닫기 및 리워드 광고 다이얼로그 열기
     */
    fun closePremiumPromptAndShowRewardDialog() {
        _showPremiumPrompt.value = false
        _showRewardAdInfo.value = true
    }

    /**
     * 프리미엄 유도 팝업 닫기
     */
    fun closePremiumPrompt() {
        _showPremiumPrompt.value = false
    }

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━
    // 리워드 광고 (UseCase 호출)
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━

    /**
     * 리워드 광고 표시 및 프리미엄 지급 (UseCase에 위임)
     * - 다이얼로그를 먼저 닫고 광고 재생, 중복 호출 차단
     */
    fun showRewardAdAndGrantPremium(context: Context) {
        if (isAdFlowRunning) return

        isAdFlowRunning = true
        _isRewardProcessing.value = true
        _showRewardAdInfo.value = false
        _showPremiumPrompt.value = false

        viewModelScope.launch {
            try {
                val user = userRepository.userData.value?.localUserData ?: run {
                    Log.w(TAG("SharedViewModel", "showRewardAdAndGrantPremium"), "❌ 사용자 데이터 없음")
                    return@launch
                }

                adUseCase.showRewardAdAndGrantPremium(
                    context = context,
                    user = user,
                    onSuccess = {
                        // ⚠️ 실제 연장 일수는 캡에 따라 달라질 수 있어 고정 숫자를 안 박음
                        _snackbarEvent.trySend("✨ 프리미엄이 연장되었습니다!")
                    },
                    onAlreadyUsed = {
                        _snackbarEvent.trySend("오늘은 리워드 시청 횟수를 다 채우셨습니다")
                    },
                    onAdFailed = {
                        _snackbarEvent.trySend("현재 시청 가능한 광고가 없습니다. 잠시 후 다시 시도해주세요.")
                    }
                )
            } finally {
                isAdFlowRunning = false
                _isRewardProcessing.value = false
            }
        }
    }

    /**
     * 리워드 광고 다이얼로그 열기
     */
    fun showRewardAdDialog() {
        requestPremiumUnlock()
    }

    /**
     * 리워드 광고 다이얼로그 닫기
     */
    fun closeRewardAdDialog() {
        _showRewardAdInfo.value = false
    }

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━
    // 프리미엄 만료 체크 / 서버 재조회
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━

    /**
     * 프리미엄 만료 임박 체크
     */
    fun checkPremiumExpiry() {
        viewModelScope.launch {
            val user = userRepository.userData.value?.localUserData ?: return@launch

            if (premiumManager.isExpiringWithinHour(user)) {
                val remainingSeconds = premiumManager.getRemainingSeconds(user)
                val remainingMinutes = remainingSeconds / 60

                _snackbarEvent.send("⚠️ 프리미엄이 ${remainingMinutes}분 후 만료됩니다")
                Log.d(
                    TAG("SharedViewModel", "checkPremiumExpiry"),
                    "프리미엄 만료 임박: ${remainingMinutes}분 남음"
                )
            }
        }
    }

    /**
     * ✅ 신규: 유저 데이터가 로드될 때까지 기다렸다가 deviceId 반환
     * - MainActivity.onCreate()에서 광고 프리로드 시 deviceId가 필요해서 추가
     */
    suspend fun getDeviceId(): String {
        return userRepository.userData.filterNotNull().first().localUserData.id.toString()
    }

    /**
     * ✅ 신규: 서버 기준으로 프리미엄 상태 재조회 (구독 + 리워드 통합)
     * - 프리미엄 화면 진입 시 등, 최신 상태를 확실히 보여줘야 할 때 호출
     */
    fun refreshPremiumStatus() {
        viewModelScope.launch {
            premiumManager.refreshUnifiedPremiumStatus()
        }
    }

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━
    // 유틸리티
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━

    private fun getCurrentDate(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return dateFormat.format(Date())
    }

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━
    // 테스트 도구 (디버그 빌드 전용 — 서버와 동기화)
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━

    private fun runDebugAction(label: String, action: suspend () -> Pair<Boolean, String>) {
        if (!BuildConfig.DEBUG) return
        viewModelScope.launch {
            val (ok, message) = action()
            _snackbarEvent.trySend(if (ok) "🔧 $label 완료" else "🔧 $label 실패: $message")
        }
    }

    /** 서버의 리워드 프리미엄 초기화 */
    fun debugResetPremium() = runDebugAction("프리미엄 초기화") {
        premiumManager.debugResetPremium()
    }

    /** 서버의 하루 리워드 시청 횟수 초기화 (+ 로컬 전면광고 카운트 초기화) */
    fun debugResetDailyAdCount() = runDebugAction("하루 광고 횟수 초기화") {
        val result = premiumManager.debugResetDailyReward()

        // 서버 반영 후 최신 로컬 값 기준으로 전면광고 카운트와 1차 체크 필드 초기화
        val user = userRepository.userData.value?.localUserData
        if (user != null) {
            userRepository.localUserUpdate(
                user.copy(
                    interstitialAdCount = 0,
                    lastRewardDate = null,
                    dailyRewardUsed = false
                )
            )
        }
        result
    }

    /** 서버에 N분 후 만료 프리미엄 지급 */
    fun debugGrantPremium(minutes: Int = 1) = runDebugAction("프리미엄 ${minutes}분 지급") {
        premiumManager.debugGrantPremium(minutes)
    }
}

/**
 * 광고 UI 상태
 */
data class AdUiState(
    val rewardAdState: Boolean = false,
    val bannerAdState: Boolean = false
)
