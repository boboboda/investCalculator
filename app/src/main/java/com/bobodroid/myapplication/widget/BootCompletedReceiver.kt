package com.bobodroid.myapplication.widget

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.bobodroid.myapplication.domain.repository.IUserRepository
import com.bobodroid.myapplication.util.PreferenceUtil
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 부팅 완료 리시버
 * - 디바이스 재부팅 후 프리미엄 사용자의 위젯 서비스 자동 시작
 */
@AndroidEntryPoint
class BootCompletedReceiver : BroadcastReceiver() {

    @Inject
    lateinit var userRepository: IUserRepository

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    companion object {
        private const val TAG = "BootCompletedReceiver"
    }

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_BOOT_COMPLETED,
            "android.intent.action.QUICKBOOT_POWERON" -> {
                Log.d(TAG, "━━━━━━━━━━━━━━━━━━━━━━━━━━")
                Log.d(TAG, "📱 부팅 완료 감지!")
                Log.d(TAG, "━━━━━━━━━━━━━━━━━━━━━━━━━━")

                handleBootCompleted(context)
            }
        }
    }

    private fun handleBootCompleted(context: Context) {
        scope.launch {
            try {
                // 1. 이전 서비스 실행 상태 확인
                val preferenceUtil = PreferenceUtil(context)
                val wasServiceRunning = preferenceUtil.getData("widget_service_running", "false") == "true"

                if (!wasServiceRunning) {
                    Log.d(TAG, "이전에 서비스가 실행 중이지 않았음")
                    return@launch
                }

                // 2. 프리미엄 사용자 확인
                val userData = userRepository.userData.firstOrNull()
                val isPremium = userData?.localUserData?.isPremium ?: false

                Log.d(TAG, "프리미엄 상태: $isPremium")

                if (!isPremium) {
                    Log.w(TAG, "프리미엄 사용자가 아님 - 서비스 시작 안 함")
                    return@launch
                }

                // 3. 서비스 자동 시작
                Log.d(TAG, "✅ 위젯 업데이트 서비스 자동 시작")
                WidgetUpdateService.startService(context)

                // 4. 위젯 즉시 업데이트
                WidgetUpdateHelper.updateAllWidgets(context)

            } catch (e: Exception) {
                Log.e(TAG, "부팅 후 서비스 시작 실패", e)
            }
        }
    }
}