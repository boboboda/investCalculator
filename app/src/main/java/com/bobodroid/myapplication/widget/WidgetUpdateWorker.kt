package com.bobodroid.myapplication.widget

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.bobodroid.myapplication.models.datamodels.repository.LatestRateRepository
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent

/**
 * 위젯 자동 업데이트 Worker
 * - 일반 사용자용 5분 주기 업데이트
 * - REST API로 최신 환율 가져오기
 * - 위젯 자동 업데이트
 */
class WidgetUpdateWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface WidgetUpdateWorkerEntryPoint {
        fun latestRateRepository(): LatestRateRepository
    }

    companion object {
        const val TAG = "WidgetUpdateWorker"
        const val WORK_NAME = "widget_update_work"
    }

    override suspend fun doWork(): Result {
        return try {
            Log.d(TAG, "━━━━━━━━━━━━━━━━━━━━━━━━━━")
            Log.d(TAG, "⏰ 5분 주기 위젯 업데이트 시작")

            // Hilt EntryPoint로 Repository 가져오기
            val appContext = applicationContext.applicationContext
            val hiltEntryPoint = EntryPointAccessors.fromApplication(
                appContext,
                WidgetUpdateWorkerEntryPoint::class.java
            )
            val latestRateRepository = hiltEntryPoint.latestRateRepository()

            // REST API로 최신 환율 가져오기
            Log.d(TAG, "REST API 호출 중...")
            latestRateRepository.fetchInitialLatestRate()
            Log.d(TAG, "✅ 최신 환율 가져오기 완료")

            // 위젯 즉시 업데이트
            WidgetUpdateHelper.updateAllWidgets(appContext)
            Log.d(TAG, "✅ 위젯 업데이트 완료")

            Log.d(TAG, "━━━━━━━━━━━━━━━━━━━━━━━━━━")

            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "❌ 위젯 업데이트 실패", e)
            Result.retry()
        }
    }
}