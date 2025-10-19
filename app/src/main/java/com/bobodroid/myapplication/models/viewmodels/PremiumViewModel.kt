package com.bobodroid.myapplication.models.viewmodels

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bobodroid.myapplication.MainActivity.Companion.TAG
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
 * 프리미엄 ViewModel (단순화)
 * - User DB의 isPremium만 참조
 * - 서비스 제어 로직만 담당
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

    // 서비스 실행 상태 (SharedPreferences에서 관리)
    private val _isServiceRunning = MutableStateFlow(getServiceState())
    val isServiceRunning = _isServiceRunning.asStateFlow()

    /**
     * 실시간 업데이트 토글
     */
    fun toggleRealtimeUpdate(enabled: Boolean) {
        viewModelScope.launch {
            val user = userRepository.userData.value?.localUserData

            if (user?.isPremium != true) {
                Log.w(TAG("PremiumViewModel", "toggleRealtimeUpdate"), "프리미엄 사용자가 아닙니다")
                return@launch
            }

            if (enabled) {
                startRealtimeUpdate()
            } else {
                stopRealtimeUpdate()
            }
        }
    }

    /**
     * 실시간 업데이트 시작
     */
    private fun startRealtimeUpdate() {
        WidgetUpdateService.startService(context)
        _isServiceRunning.value = true
        saveServiceState(true)
        Log.d(TAG("PremiumViewModel", "startRealtimeUpdate"), "✅ 서비스 시작")
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
     * 프리미엄 상태 수동 새로고침
     */
    fun refreshPremiumStatus() {
        premiumManager.refreshPremiumStatus()
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

    fun setTestPremiumStatus(isPremium: Boolean) {
        premiumManager.setTestPremiumStatus(isPremium)
    }
}