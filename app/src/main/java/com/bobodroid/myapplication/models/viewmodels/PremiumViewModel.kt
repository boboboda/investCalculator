package com.bobodroid.myapplication.models.viewmodels

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bobodroid.myapplication.MainActivity.Companion.TAG
import com.bobodroid.myapplication.billing.BillingClientLifecycle
import com.bobodroid.myapplication.models.datamodels.repository.UserRepository
import com.bobodroid.myapplication.premium.PremiumManager
import com.bobodroid.myapplication.util.PreferenceUtil
import com.bobodroid.myapplication.widget.WidgetUpdateService
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 프리미엄 ViewModel
 * - User DB의 isPremium 참조
 * - 서비스 제어 로직
 * - 구매 플로우 시작
 */
@HiltViewModel
class PremiumViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val userRepository: UserRepository,
    private val premiumManager: PremiumManager
) : ViewModel() {

    private val preferenceUtil = PreferenceUtil(context)

    // ✅ User DB에서 프리미엄 상태 가져오기
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

    /**
     * ✅ 실시간 업데이트 토글 (Context 제거)
     */
    fun toggleRealtimeUpdate(enabled: Boolean) {
        Log.d(TAG("PremiumViewModel", "toggleRealtimeUpdate"), "━━━━━━━━━━━━━━━━━━━━━━━━━━")
        Log.d(TAG("PremiumViewModel", "toggleRealtimeUpdate"), "📞 toggleRealtimeUpdate 호출됨")
        Log.d(TAG("PremiumViewModel", "toggleRealtimeUpdate"), "enabled: $enabled")

        viewModelScope.launch {
            try {
                val user = userRepository.userData.value?.localUserData
                Log.d(TAG("PremiumViewModel", "toggleRealtimeUpdate"), "👤 user: $user")
                Log.d(TAG("PremiumViewModel", "toggleRealtimeUpdate"), "💎 isPremium: ${user?.isPremium}")

                if (user?.isPremium != true) {
                    Log.w(TAG("PremiumViewModel", "toggleRealtimeUpdate"), "⚠️ 프리미엄 사용자가 아닙니다")
                    return@launch
                }

                Log.d(TAG("PremiumViewModel", "toggleRealtimeUpdate"), "✅ 프리미엄 확인 완료")

                if (enabled) {
                    Log.d(TAG("PremiumViewModel", "toggleRealtimeUpdate"), "🚀 서비스 시작 시도...")
                    startRealtimeUpdate()
                } else {
                    Log.d(TAG("PremiumViewModel", "toggleRealtimeUpdate"), "🛑 서비스 종료 시도...")
                    stopRealtimeUpdate()
                }

                Log.d(TAG("PremiumViewModel", "toggleRealtimeUpdate"), "━━━━━━━━━━━━━━━━━━━━━━━━━━")
            } catch (e: Exception) {
                Log.e(TAG("PremiumViewModel", "toggleRealtimeUpdate"), "❌ 에러 발생", e)
            }
        }
    }

    /**
     * 실시간 업데이트 시작
     */
    private fun startRealtimeUpdate() {
        Log.d(TAG("PremiumViewModel", "startRealtimeUpdate"), "━━━━━━━━━━━━━━━━━━━━━━━━━━")
        Log.d(TAG("PremiumViewModel", "startRealtimeUpdate"), "📡 startRealtimeUpdate 시작")
        Log.d(TAG("PremiumViewModel", "startRealtimeUpdate"), "Context: $context")

        try {
            WidgetUpdateService.startService(context)
            Log.d(TAG("PremiumViewModel", "startRealtimeUpdate"), "✅ startService 호출 완료")

            _isServiceRunning.value = true
            saveServiceState(true)
            Log.d(TAG("PremiumViewModel", "startRealtimeUpdate"), "✅ 상태 저장 완료")
        } catch (e: Exception) {
            Log.e(TAG("PremiumViewModel", "startRealtimeUpdate"), "❌ 서비스 시작 실패", e)
        }

        Log.d(TAG("PremiumViewModel", "startRealtimeUpdate"), "━━━━━━━━━━━━━━━━━━━━━━━━━━")
    }

    /**
     * 실시간 업데이트 종료
     */
    private fun stopRealtimeUpdate() {
        WidgetUpdateService.stopService(context)
        _isServiceRunning.value = false
        saveServiceState(false)
        Log.d(TAG("PremiumViewModel", "stopRealtimeUpdate"), "⏹ 서비스 종료")
    }

    /**
     * ✅ 구매 플로우 시작
     */
    fun startPurchaseFlow(activity: Activity, billingClient: BillingClientLifecycle) {
        viewModelScope.launch {
            // fetchedProductList에서 상품 가져오기
            billingClient.fetchedProductList.collect { products ->
                if (products.isNotEmpty()) {
                    val productDetails = products.first()
                    val responseCode = billingClient.startBillingFlow(activity, productDetails)
                    Log.d(TAG("PremiumViewModel", "startPurchaseFlow"),
                        "결제 시작: responseCode=$responseCode")
                } else {
                    Log.e(TAG("PremiumViewModel", "startPurchaseFlow"),
                        "구매 가능한 상품이 없습니다")
                }
            }
        }
    }

    /**
     * 프리미엄 상태 수동 새로고침
     */
    fun refreshPremiumStatus() {
        premiumManager.refreshPremiumStatus()
    }

    /**
     * 테스트용: 프리미엄 상태 강제 설정
     */
    suspend fun setTestPremiumStatus(isPremium: Boolean) {
        premiumManager.setTestPremiumStatus(isPremium)
    }

    /**
     * 서비스 상태 저장
     */
    private fun saveServiceState(isRunning: Boolean) {
        preferenceUtil.setData("widget_service_running", isRunning.toString())
    }

    /**
     * 서비스 상태 불러오기
     */
    private fun getServiceState(): Boolean {
        return preferenceUtil.getData("widget_service_running", "false") == "true"
    }
}