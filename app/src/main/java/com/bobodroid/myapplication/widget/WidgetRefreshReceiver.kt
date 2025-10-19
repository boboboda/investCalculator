package com.bobodroid.myapplication.widget

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.bobodroid.myapplication.models.datamodels.repository.LatestRateRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 위젯 수동 새로고침 리시버
 * - 일반 사용자용 새로고침 버튼 클릭 처리
 * - REST API로 최신 환율 가져오기
 * - 위젯 즉시 업데이트
 */
@AndroidEntryPoint
class WidgetRefreshReceiver : BroadcastReceiver() {

    @Inject
    lateinit var latestRateRepository: LatestRateRepository

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    companion object {
        const val ACTION_REFRESH = "com.bobodroid.myapplication.ACTION_WIDGET_REFRESH"
        private const val TAG = "WidgetRefreshReceiver"
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == ACTION_REFRESH) {
            Log.d(TAG, "새로고침 버튼 클릭됨")

            // 비동기로 최신 환율 가져오기
            scope.launch {
                try {
                    Log.d(TAG, "REST API로 최신 환율 요청 중...")

                    // REST API로 최신 환율 가져오기
                    latestRateRepository.fetchInitialLatestRate()

                    Log.d(TAG, "최신 환율 가져오기 완료")

                    // 위젯 즉시 업데이트
                    WidgetUpdateHelper.updateAllWidgets(context)

                    Log.d(TAG, "위젯 새로고침 완료")
                } catch (e: Exception) {
                    Log.e(TAG, "새로고침 실패", e)
                }
            }
        }
    }
}