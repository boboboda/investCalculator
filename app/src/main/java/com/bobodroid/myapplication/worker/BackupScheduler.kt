// app/src/main/java/com/bobodroid/myapplication/worker/BackupScheduler.kt
package com.bobodroid.myapplication.worker

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.work.*
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 백업 스케줄러
 * - 기록 변경 시 1분 후 자동 백업 예약
 * - 프리미엄 사용자만 작동
 */
@Singleton
class BackupScheduler @Inject constructor(
    @ApplicationContext private val context: Context
) {

    companion object {
        private const val TAG = "BackupScheduler"
        private const val BACKUP_DELAY_MINUTES = 1L
    }

    /**
     * 자동 백업 예약 (1분 후 실행)
     */
    fun scheduleBackup() {
        Log.d(TAG, "백업 예약: ${BACKUP_DELAY_MINUTES}분 후 실행")

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)  // 네트워크 필수
            .build()

        val backupRequest = OneTimeWorkRequestBuilder<BackupWorker>()
            .setConstraints(constraints)
            .setInitialDelay(BACKUP_DELAY_MINUTES, TimeUnit.MINUTES)
            .addTag(BackupWorker.TAG)
            .build()

        WorkManager.getInstance(context)
            .enqueueUniqueWork(
                BackupWorker.WORK_NAME,
                ExistingWorkPolicy.REPLACE,  // 기존 작업 취소 후 새로 예약
                backupRequest
            )

        Log.d(TAG, "백업 예약 완료")
    }

    /**
     * 즉시 백업 (지연 없음)
     */
    fun scheduleImmediateBackup() {
        Log.d(TAG, "즉시 백업 예약")

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val backupRequest = OneTimeWorkRequestBuilder<BackupWorker>()
            .setConstraints(constraints)
            .addTag(BackupWorker.TAG)
            .build()

        WorkManager.getInstance(context)
            .enqueueUniqueWork(
                "${BackupWorker.WORK_NAME}_immediate",
                ExistingWorkPolicy.REPLACE,
                backupRequest
            )

        Log.d(TAG, "즉시 백업 예약 완료")
    }

    /**
     * 예약된 백업 취소
     */
    fun cancelBackup() {
        Log.d(TAG, "백업 예약 취소")
        WorkManager.getInstance(context).cancelUniqueWork(BackupWorker.WORK_NAME)
    }

    /**
     * 백업 상태 조회
     */
    fun getBackupStatus(): LiveData<WorkInfo?> {
        return WorkManager.getInstance(context)
            .getWorkInfosForUniqueWorkLiveData(BackupWorker.WORK_NAME)
            .map { workInfos -> workInfos.firstOrNull() }
    }
}

// ✅ LiveData 확장 함수
private fun <T, R> LiveData<T>.map(transform: (T) -> R): LiveData<R> {
    val result = androidx.lifecycle.MediatorLiveData<R>()
    result.addSource(this) { value ->
        result.value = transform(value)
    }
    return result
}