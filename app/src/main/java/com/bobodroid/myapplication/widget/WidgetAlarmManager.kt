package com.bobodroid.myapplication.widget

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.SystemClock
import android.util.Log
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.bobodroid.myapplication.models.datamodels.repository.LatestRateRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * 위젯 AlarmManager + WorkManager 관리
 * - 일반 사용자용 5분 주기 자동 업데이트
 */
object WidgetAlarmManager {
    private const val TAG = "WidgetAlarmManager"
    private const val ALARM_REQUEST_CODE = 1001
    private const val UPDATE_INTERVAL = 5 * 60 * 1000L // 5분 (밀리초)

    /**
     * 5분 주기 자동 업데이트 시작 (일반 사용자)
     */
    fun startPeriodicUpdate(context: Context) {
        Log.d(TAG, "━━━━━━━━━━━━━━━━━━━━━━━━━━")
        Log.d(TAG, "🔄 5분 주기 자동 업데이트 시작")

        // WorkManager로 5분 주기 작업 등록
        val workRequest = PeriodicWorkRequestBuilder<WidgetUpdateWorker>(
            5, TimeUnit.MINUTES
        ).build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            WidgetUpdateWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP, // 이미 실행 중이면 유지
            workRequest
        )

        Log.d(TAG, "✅ WorkManager 등록 완료 (5분 주기)")
        Log.d(TAG, "━━━━━━━━━━━━━━━━━━━━━━━━━━")
    }

    /**
     * 자동 업데이트 중지
     */
    fun stopPeriodicUpdate(context: Context) {
        Log.d(TAG, "━━━━━━━━━━━━━━━━━━━━━━━━━━")
        Log.d(TAG, "⏹️ 5분 주기 자동 업데이트 중지")

        // WorkManager 취소
        WorkManager.getInstance(context).cancelUniqueWork(WidgetUpdateWorker.WORK_NAME)

        Log.d(TAG, "✅ WorkManager 취소 완료")
        Log.d(TAG, "━━━━━━━━━━━━━━━━━━━━━━━━━━")
    }

    /**
     * 현재 실행 중인지 확인
     */
    fun isRunning(context: Context): Boolean {
        val workManager = WorkManager.getInstance(context)
        val workInfos = workManager.getWorkInfosForUniqueWork(WidgetUpdateWorker.WORK_NAME).get()

        return workInfos.any { !it.state.isFinished }
    }
}