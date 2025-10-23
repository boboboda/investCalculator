package com.bobodroid.myapplication.models.viewmodels

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bobodroid.myapplication.MainActivity.Companion.TAG
import com.bobodroid.myapplication.models.datamodels.repository.UserRepository
import com.bobodroid.myapplication.models.datamodels.roomDb.PremiumType
import com.bobodroid.myapplication.premium.PremiumManager
import com.bobodroid.myapplication.util.AdMob.AdUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
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
    private val adUseCase: AdUseCase,  // ← UseCase만 주입
    private val userRepository: UserRepository
) : ViewModel() {

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━
    // 상태 (Repository에서 구독)
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━

    /**
     * 프리미엄 상태
     */
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
    // 다이얼로그 상태
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━

    private val _showPremiumPrompt = MutableStateFlow(false)
    val showPremiumPrompt = _showPremiumPrompt.asStateFlow()

    private val _showRewardAdInfo = MutableStateFlow(false)
    val showRewardAdInfo = _showRewardAdInfo.asStateFlow()

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━
    // 광고 UI 상태
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━

    private val _adUiState = MutableStateFlow(AdUiState())
    val adUiState = _adUiState.asStateFlow()

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━
    // 이벤트 (스낵바용)
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━

    private val _snackbarEvent = Channel<String>(Channel.BUFFERED)
    val snackbarEvent = _snackbarEvent.receiveAsFlow()

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━
    // 초기화
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━

    init {
        observeAdStates()
    }

    /**
     * 광고 상태 관찰 및 자동 갱신
     */
    private fun observeAdStates() {
        viewModelScope.launch {
            userRepository.userData.collect { userData ->
                val user = userData?.localUserData ?: return@collect
                val todayDate = getCurrentDate()

                // 리워드 광고 표시 가능 여부
                val canShowRewardAd = adUseCase.processRewardAdState(user)

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

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━
    // 전면 광고 (UseCase 호출)
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━

    /**
     * 전면 광고 표시 (UseCase에 위임)
     */
    fun showInterstitialAdIfNeeded(context: Context) {
        viewModelScope.launch {
            val user = userRepository.userData.value?.localUserData ?: run {
                Log.w(TAG("SharedViewModel", "showInterstitialAdIfNeeded"), "사용자 데이터 없음")
                return@launch
            }

            // UseCase에 모든 로직 위임
            adUseCase.showInterstitialAdIfNeeded(
                context = context,
                user = user,
                onPremiumPromptNeeded = {
                    _showPremiumPrompt.value = true
                }
            )
        }
    }

    /**
     * 프리미엄 유도 팝업 닫기
     */
    fun closePremiumPrompt() {
        _showPremiumPrompt.value = false
        Log.d(TAG("SharedViewModel", "closePremiumPrompt"), "프리미엄 유도 팝업 닫기")
    }

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━
    // 리워드 광고 (UseCase 호출)
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━

    /**
     * 리워드 광고 표시 및 프리미엄 지급 (UseCase에 위임)
     */
    fun showRewardAdAndGrantPremium(context: Context) {
        viewModelScope.launch {
            val user = userRepository.userData.value?.localUserData ?: run {
                Log.w(TAG("SharedViewModel", "showRewardAdAndGrantPremium"), "사용자 데이터 없음")
                return@launch
            }

            // UseCase에 모든 로직 위임
            adUseCase.showRewardAdAndGrantPremium(
                context = context,
                user = user,
                onSuccess = {
                    _snackbarEvent.trySend("✨ 24시간 프리미엄이 활성화되었습니다!")
                },
                onAlreadyUsed = {
                    _snackbarEvent.trySend("오늘은 이미 리워드 광고를 시청하셨습니다")
                },
                onAdFailed = {
                    _snackbarEvent.trySend("광고를 불러올 수 없습니다. 잠시 후 다시 시도해주세요.")
                }
            )

            // 다이얼로그 닫기
            _showRewardAdInfo.value = false
        }
    }

    /**
     * 리워드 광고 다이얼로그 열기
     */
    fun showRewardAdDialog() {
        _showRewardAdInfo.value = true
        Log.d(TAG("SharedViewModel", "showRewardAdDialog"), "리워드 광고 다이얼로그 열기")
    }

    /**
     * 리워드 광고 다이얼로그 닫기
     */
    fun closeRewardAdDialog() {
        _showRewardAdInfo.value = false
        Log.d(TAG("SharedViewModel", "closeRewardAdDialog"), "리워드 광고 다이얼로그 닫기")
    }

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━
    // 배너 광고
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━

    /**
     * 배너 광고 제거 연기
     */


    // ━━━━━━━━━━━━━━━━━━━━━━━━━━
    // 프리미엄 만료 체크
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

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━
    // 유틸리티
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━

    private fun getCurrentDate(): String {
        val dateFormat = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
        return dateFormat.format(java.util.Date())
    }
}

/**
 * 광고 UI 상태
 */
data class AdUiState(
    val rewardAdState: Boolean = false,
    val bannerAdState: Boolean = false
)