// app/src/main/java/com/bobodroid/myapplication/worker/BackupWorker.kt
package com.bobodroid.myapplication.worker

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.bobodroid.myapplication.models.datamodels.service.BackupApi.BackupOutcome
import com.bobodroid.myapplication.models.datamodels.service.BackupApi.BackupSyncManager
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

/**
 * 자동 백업 Worker (소셜 로그인 연동 사용자 전체)
 * - 기록 추가/수정/삭제 후 1분 뒤 실행 (BackupScheduler)
 * - 실제 전송은 BackupSyncManager가 담당
 */
@HiltWorker
class BackupWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val backupSyncManager: BackupSyncManager
) : CoroutineWorker(appContext, workerParams) {

    companion object {
        const val TAG = "BackupWorker"
        const val WORK_NAME = "auto_backup_work"
        private const val MAX_RETRY = 5
    }

    override suspend fun doWork(): Result {
        return when (val outcome = backupSyncManager.backup()) {
            is BackupOutcome.Success -> {
                Log.d(TAG, "자동 백업 완료: ${outcome.recordCount}개")
                Result.success()
            }

            // 연동 전이거나 백업할 기록이 없는 경우
            is BackupOutcome.Skipped -> {
                Log.d(TAG, "자동 백업 건너뜀: ${outcome.reason}")
                Result.success()
            }

            // 서버 기록과 달라 보류된 경우: 재시도해도 같은 결과이므로 종료하고 화면에서 안내
            is BackupOutcome.Held -> {
                Log.w(TAG, "자동 백업 보류: 서버 기록 ${outcome.serverRecordCount}개")
                Result.success()
            }

            is BackupOutcome.Failed -> {
                Log.e(TAG, "자동 백업 실패(${runAttemptCount + 1}회째): ${outcome.message}")
                if (runAttemptCount < MAX_RETRY) Result.retry() else Result.failure()
            }
        }
    }
}
