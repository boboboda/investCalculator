package com.bobodroid.myapplication.models.viewmodels

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.ProductDetails
import com.bobodroid.myapplication.MainActivity.Companion.TAG
import com.bobodroid.myapplication.billing.BillingClientLifecycle
import com.bobodroid.myapplication.models.datamodels.repository.UserRepository
import com.bobodroid.myapplication.models.datamodels.roomDb.PremiumType
import com.bobodroid.myapplication.premium.PremiumManager
import com.bobodroid.myapplication.util.PreferenceUtil
import com.bobodroid.myapplication.widget.WidgetUpdateService
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 프리미엄 ViewModel - 구독 결제 및 검증 포함
 * - 구매 플로우 관리
 * - 서버 검증 처리
 * - 서비스 제어
 * - 디버그 기능
 */
@HiltViewModel
class PremiumViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val userRepository: UserRepository,
    private val premiumManager: PremiumManager
) : ViewModel() {

    private val preferenceUtil = PreferenceUtil(context)
    private val billingClient = BillingClientLifecycle.getInstance(context)

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━
    // UI 상태
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━

    data class PremiumUiState(
        val isPremium: Boolean = false,
        val premiumType: PremiumType = PremiumType.NONE,
        val expiryDate: String? = null,
        val daysRemaining: Int = 0,
        val products: List<ProductDetails> = emptyList(),
        val isLoading: Boolean = false,
        val error: String? = null
    )

    private val _uiState = MutableStateFlow(PremiumUiState())
    val uiState = _uiState.asStateFlow()

    // 이벤트 채널
    private val _events = Channel<PremiumEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    sealed class PremiumEvent {
        data class ShowMessage(val message: String) : PremiumEvent()
        data class PurchaseSuccess(val productId: String) : PremiumEvent()
        object PurchaseFailed : PremiumEvent()
        object PurchaseCanceled : PremiumEvent()
    }

    // User DB에서 프리미엄 상태 가져오기
    val isPremium = userRepository.userData
        .map { it?.localUserData?.isPremium ?: false }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )

    // 서비스 실행 상태
    private val _isServiceRunning = MutableStateFlow(getServiceState())
    val isServiceRunning = _isServiceRunning.asStateFlow()

    init {
        Log.d(TAG("PremiumViewModel", "init"), "ViewModel 초기화")
        observeUserData()
        observeProducts()
    }

    /**
     * ✅ 사용자 데이터 구독
     */
    private fun observeUserData() {
        viewModelScope.launch {
            userRepository.userData.collect { userData ->
                val user = userData?.localUserData ?: return@collect

                val premiumType = premiumManager.checkPremiumStatus(user)
                val daysRemaining = calculateDaysRemaining(user.premiumExpiryDate)

                _uiState.value = _uiState.value.copy(
                    isPremium = user.isPremium,
                    premiumType = premiumType,
                    expiryDate = user.premiumExpiryDate,
                    daysRemaining = daysRemaining
                )

                Log.d(TAG("PremiumViewModel", "observeUserData"),
                    "상태 업데이트: isPremium=${user.isPremium}, type=$premiumType")
            }
        }
    }

    /**
     * ✅ 상품 목록 구독
     */
    private fun observeProducts() {
        viewModelScope.launch {
            billingClient.fetchedProductList.collect { products ->
                Log.d(TAG("PremiumViewModel", "observeProducts"), "상품 목록: ${products.size}개")
                _uiState.value = _uiState.value.copy(products = products)
            }
        }
    }

    /**
     * ✅ 구매 시작
     */
    fun startPurchase(activity: Activity, productDetails: ProductDetails, basePlanId: String) {
        Log.d(TAG("PremiumViewModel", "startPurchase"), "구매 시작: ${productDetails.productId}")

        _uiState.value = _uiState.value.copy(isLoading = true)

        val responseCode = billingClient.startBillingFlow(activity, productDetails, basePlanId)

        when (responseCode) {
            BillingClient.BillingResponseCode.OK -> {
                Log.d(TAG("PremiumViewModel", "startPurchase"), "구매 플로우 시작 성공")
            }
            BillingClient.BillingResponseCode.USER_CANCELED -> {
                viewModelScope.launch {
                    _events.send(PremiumEvent.PurchaseCanceled)
                }
            }
            else -> {
                viewModelScope.launch {
                    _events.send(PremiumEvent.ShowMessage("구매를 시작할 수 없습니다"))
                    _events.send(PremiumEvent.PurchaseFailed)
                }
            }
        }

        _uiState.value = _uiState.value.copy(isLoading = false)
    }

    /**
     * ✅ 구독 복원
     */
    fun restorePurchases() {
        Log.d(TAG("PremiumViewModel", "restorePurchases"), "구독 복원 시작")

        _uiState.value = _uiState.value.copy(isLoading = true)

        viewModelScope.launch {
            billingClient.queryActivePurchases(BillingClientLifecycle.PRODUCT_ID) { hasPremium ->
                if (hasPremium) {
                    Log.d(TAG("PremiumViewModel", "restorePurchases"), "구독 복원 성공")
                    viewModelScope.launch {
                        _events.send(PremiumEvent.ShowMessage("구독이 복원되었습니다"))
                        premiumManager.syncPremiumStatus()
                    }
                } else {
                    Log.d(TAG("PremiumViewModel", "restorePurchases"), "복원할 구독 없음")
                    viewModelScope.launch {
                        _events.send(PremiumEvent.ShowMessage("복원할 구독이 없습니다"))
                    }
                }

                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }

    /**
     * ✅ 구독 상태 새로고침
     */
    fun refreshSubscriptionStatus() {
        Log.d(TAG("PremiumViewModel", "refreshSubscriptionStatus"), "구독 상태 새로고침")

        _uiState.value = _uiState.value.copy(isLoading = true)

        viewModelScope.launch {
            try {
                premiumManager.syncPremiumStatus()
                _events.send(PremiumEvent.ShowMessage("구독 상태가 업데이트되었습니다"))
            } catch (e: Exception) {
                Log.e(TAG("PremiumViewModel", "refreshSubscriptionStatus"), "새로고침 실패", e)
                _events.send(PremiumEvent.ShowMessage("업데이트에 실패했습니다"))
            } finally {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }

    /**
     * ✅ 프리미엄 상태 수동 새로고침
     */
    fun refreshPremiumStatus() {
        Log.d(TAG("PremiumViewModel", "refreshPremiumStatus"), "프리미엄 상태 새로고침")
        viewModelScope.launch {
            premiumManager.refreshPremiumStatus()
        }
    }

    /**
     * ✅ 실시간 업데이트 토글
     */
    fun toggleRealtimeUpdate(enabled: Boolean) {
        Log.d(TAG("PremiumViewModel", "toggleRealtimeUpdate"), "━━━━━━━━━━━━━━━━━━━━━━━━━━")
        Log.d(TAG("PremiumViewModel", "toggleRealtimeUpdate"), "실시간 업데이트: $enabled")

        viewModelScope.launch {
            val user = userRepository.userData.value?.localUserData

            if (user?.isPremium != true) {
                Log.w(TAG("PremiumViewModel", "toggleRealtimeUpdate"), "프리미엄 사용자가 아닙니다")
                _events.send(PremiumEvent.ShowMessage("프리미엄 기능입니다"))
                return@launch
            }

            if (enabled) {
                startRealtimeUpdate()
            } else {
                stopRealtimeUpdate()
            }
        }
    }

    private fun startRealtimeUpdate() {
        Log.d(TAG("PremiumViewModel", "startRealtimeUpdate"), "서비스 시작")
        saveServiceState(true)
        _isServiceRunning.value = true
        WidgetUpdateService.startService(context)
    }

    private fun stopRealtimeUpdate() {
        Log.d(TAG("PremiumViewModel", "stopRealtimeUpdate"), "서비스 중지")
        saveServiceState(false)
        _isServiceRunning.value = false
        WidgetUpdateService.stopService(context)
    }

    /**
     * ✅ 테스트용: 프리미엄 상태 강제 설정 (디버그)
     */
    suspend fun setTestPremiumStatus(isPremium: Boolean) {
        Log.d(TAG("PremiumViewModel", "setTestPremiumStatus"), "테스트 프리미엄: $isPremium")
        premiumManager.setTestPremiumStatus(isPremium)
    }

    /**
     * SharedPreferences 헬퍼
     */
    private fun getServiceState(): Boolean {
        return try {
            preferenceUtil.getServiceRunning()
        } catch (e: Exception) {
            val sharedPrefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
            sharedPrefs.getBoolean("service_running", false)
        }
    }

    private fun saveServiceState(isRunning: Boolean) {
        try {
            preferenceUtil.saveServiceRunning(isRunning)
        } catch (e: Exception) {
            val sharedPrefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
            sharedPrefs.edit().putBoolean("service_running", isRunning).apply()
        }
    }

    /**
     * 남은 일수 계산
     */
    private fun calculateDaysRemaining(expiryDate: String?): Int {
        if (expiryDate.isNullOrEmpty()) return 0

        return try {
            val expiry = java.time.Instant.parse(expiryDate)
            val now = java.time.Instant.now()
            val days = java.time.Duration.between(now, expiry).toDays()
            days.toInt().coerceAtLeast(0)
        } catch (e: Exception) {
            0
        }
    }
}