// app/src/main/java/com/bobodroid/myapplication/models/viewmodels/PremiumViewModel.kt
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
import com.bobodroid.myapplication.models.datamodels.roomDb.LocalUserData
import com.bobodroid.myapplication.models.datamodels.roomDb.PremiumType
import com.bobodroid.myapplication.models.datamodels.service.subscriptionApi.RestoreSubscriptionRequest
import com.bobodroid.myapplication.models.datamodels.service.subscriptionApi.SubscriptionApi
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
import java.time.Instant
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
        val error: String? = null,
        // ✅ 소셜 로그인 상태 추가
        val isSocialLoggedIn: Boolean = false,
        val socialType: String? = null
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
        object RequireSocialLogin : PremiumEvent() // ✅ 소셜 로그인 필요 이벤트
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

                // ✅ 소셜 로그인 상태 체크
                val isSocialLoggedIn = !user.socialId.isNullOrEmpty() &&
                        !user.socialType.isNullOrEmpty()

                _uiState.value = _uiState.value.copy(
                    isPremium = user.isPremium,
                    premiumType = premiumType,
                    expiryDate = user.premiumExpiryDate,
                    daysRemaining = daysRemaining,
                    isSocialLoggedIn = isSocialLoggedIn,
                    socialType = user.socialType
                )

                Log.d(TAG("PremiumViewModel", "observeUserData"),
                    "상태 업데이트: isPremium=${user.isPremium}, type=$premiumType, social=$isSocialLoggedIn")
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
     * ✅ 구매 시작 (소셜 로그인 체크 추가)
     */
    fun startPurchase(activity: Activity, productDetails: ProductDetails, basePlanId: String) {
        Log.d(TAG("PremiumViewModel", "startPurchase"), "구매 시작: ${productDetails.productId}")

        // ✅ 소셜 로그인 필수 체크
        if (!_uiState.value.isSocialLoggedIn) {
            Log.w(TAG("PremiumViewModel", "startPurchase"), "소셜 로그인이 필요합니다")
            viewModelScope.launch {
                _events.send(PremiumEvent.RequireSocialLogin)
            }
            return
        }

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
     * ✅ 구독 복원 (소셜 계정 기반)
     */
    fun restorePurchases() {
        Log.d(TAG("PremiumViewModel", "restorePurchases"), "━━━━━━━━━━━━━━━━━━━━━━━━━━")
        Log.d(TAG("PremiumViewModel", "restorePurchases"), "구독 복원 시작 (소셜 기반)")

        _uiState.value = _uiState.value.copy(isLoading = true)

        viewModelScope.launch {
            try {
                // 1. 현재 사용자 정보 가져오기
                val user = userRepository.userData.value?.localUserData

                if (user == null) {
                    Log.e(TAG("PremiumViewModel", "restorePurchases"), "사용자 정보 없음")
                    _events.send(PremiumEvent.ShowMessage("사용자 정보를 찾을 수 없습니다"))
                    _uiState.value = _uiState.value.copy(isLoading = false)
                    return@launch
                }

                // 2. 소셜 로그인 정보 확인
                val socialId = user.socialId
                val socialType = user.socialType

                if (socialId.isNullOrEmpty() || socialType.isNullOrEmpty()) {
                    Log.w(TAG("PremiumViewModel", "restorePurchases"), "소셜 로그인 정보 없음 - 로컬 복원 시도")

                    // 소셜 로그인 안 했으면 기존 방식으로 복원 (디바이스 기반)
                    billingClient.queryActivePurchases(BillingClientLifecycle.PRODUCT_ID) { hasPremium ->
                        if (hasPremium) {
                            Log.d(TAG("PremiumViewModel", "restorePurchases"), "로컬 구독 복원 성공")
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
                    return@launch
                }

                // 3. 서버에 소셜 기반 복원 요청
                Log.d(TAG("PremiumViewModel", "restorePurchases"), "소셜 정보: $socialType - $socialId")

                val request = RestoreSubscriptionRequest(
                    socialId = socialId,
                    socialType = socialType
                )

                val response = SubscriptionApi.service.restoreSubscription(request)

                Log.d(TAG("PremiumViewModel", "restorePurchases"), "서버 응답: ${response.success}")
                Log.d(TAG("PremiumViewModel", "restorePurchases"), "메시지: ${response.message}")

                if (response.success && response.data != null) {
                    val data = response.data

                    if (data.isPremium) {
                        // 4. 프리미엄 구독 복원 성공
                        Log.d(TAG("PremiumViewModel", "restorePurchases"), "✅ 구독 복원 성공")
                        Log.d(TAG("PremiumViewModel", "restorePurchases"), "만료일: ${data.expiryTime}")

                        // 5. 로컬 DB 업데이트
                        val updatedUser = user.copy(
                            premiumType = data.premiumType,
                            premiumExpiryDate = data.expiryTime,
                            premiumGrantedBy = "subscription",
                            premiumGrantedAt = Instant.now().toString(),
                            isPremium = true
                        )

                        userRepository.localUserUpdate(updatedUser)

                        _events.send(PremiumEvent.ShowMessage("구독이 복원되었습니다"))
                        Log.d(TAG("PremiumViewModel", "restorePurchases"), "━━━━━━━━━━━━━━━━━━━━━━━━━━")
                    } else {
                        // 복원할 구독이 없음
                        Log.d(TAG("PremiumViewModel", "restorePurchases"), "복원할 구독 없음")
                        _events.send(PremiumEvent.ShowMessage("복원할 구독이 없습니다"))
                    }
                } else {
                    Log.e(TAG("PremiumViewModel", "restorePurchases"), "서버 응답 실패: ${response.message}")
                    _events.send(PremiumEvent.ShowMessage("구독 복원에 실패했습니다"))
                }

            } catch (e: Exception) {
                Log.e(TAG("PremiumViewModel", "restorePurchases"), "구독 복원 실패", e)
                _events.send(PremiumEvent.ShowMessage("구독 복원 중 오류가 발생했습니다"))
            } finally {
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


    fun refreshPremiumStatus() {
        Log.d(TAG("PremiumViewModel", "refreshPremiumStatus"), "프리미엄 상태 새로고침")
        viewModelScope.launch {
            premiumManager.refreshPremiumStatus()
        }
    }

    /**
     * ✅ 테스트용: 프리미엄 상태 강제 설정 (디버그)
     */
    fun setTestPremiumStatus(isPremium: Boolean) {
        Log.d(TAG("PremiumViewModel", "setTestPremiumStatus"), "테스트 프리미엄: $isPremium")
        viewModelScope.launch {
            premiumManager.updateUserPremiumStatus(isPremium)
        }
    }
}