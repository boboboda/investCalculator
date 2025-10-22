// app/src/main/java/com/bobodroid/myapplication/worker/BackupWorker.kt
package com.bobodroid.myapplication.worker

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.bobodroid.myapplication.models.datamodels.repository.InvestRepository
import com.bobodroid.myapplication.models.datamodels.repository.UserRepository
import com.bobodroid.myapplication.models.datamodels.service.BackupApi.BackupApi
import com.bobodroid.myapplication.models.datamodels.service.BackupApi.BackupMapper
import com.bobodroid.myapplication.models.datamodels.service.BackupApi.BackupRequest
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first

/**
 * 자동 백업 Worker (프리미엄 전용)
 * - 기록 추가/수정/삭제 후 1분 뒤 자동 실행
 * - 네트워크 연결 확인
 * - 소셜 로그인 체크
 */
@HiltWorker
class BackupWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val userRepository: UserRepository,
    private val investRepository: InvestRepository
) : CoroutineWorker(appContext, workerParams) {

    companion object {
        const val TAG = "BackupWorker"
        const val WORK_NAME = "auto_backup_work"
    }

    override suspend fun doWork(): Result {
        return try {
            Log.d(TAG, "━━━━━━━━━━━━━━━━━━━━")
            Log.d(TAG, "🔄 자동 백업 시작")
            Log.d(TAG, "━━━━━━━━━━━━━━━━━━━━")

            // ✅ 1. 사용자 정보 확인
            val userData = userRepository.userData.first()
            val localUser = userData?.localUserData

            if (localUser == null) {
                Log.e(TAG, "❌ 사용자 정보 없음")
                return Result.failure()
            }

            Log.d(TAG, "👤 사용자: ${localUser.email}")

            // ✅ 2. 프리미엄 체크
            if (!localUser.isPremium) {
                Log.d(TAG, "⚠️ 프리미엄 사용자 아님 - 백업 취소")
                return Result.success()
            }

            Log.d(TAG, "⭐ 프리미엄 사용자 확인")

            // ✅ 3. 소셜 로그인 체크
            if (localUser.socialId.isNullOrEmpty()) {
                Log.e(TAG, "❌ 소셜 로그인 필요")
                return Result.failure()
            }

            Log.d(TAG, "🔐 소셜 로그인 확인 (${localUser.socialType})")

            // ✅ 4. 모든 투자 기록 가져오기
            val allRecords = investRepository.getAllCurrencyRecords().first()
            Log.d(TAG, "📊 백업할 기록: ${allRecords.size}개")

            if (allRecords.isEmpty()) {
                Log.d(TAG, "⚠️ 백업할 기록이 없음")
                return Result.success()
            }

            // ✅ 5. DTO 변환
            val recordDtos = BackupMapper.toDtoList(allRecords)

            // ✅ 6. 백업 요청 생성
            val backupRequest = BackupRequest(
                deviceId = localUser.id.toString(),
                socialId = localUser.socialId,
                socialType = localUser.socialType,
                currencyRecords = recordDtos
            )

            // ✅ 7. 서버로 백업 전송
            Log.d(TAG, "🌐 서버 백업 시작...")
            val response = BackupApi.backupService.createBackup(backupRequest)

            if (!response.success) {
                Log.e(TAG, "❌ 백업 실패: ${response.message}")
                return Result.retry()
            }

            Log.d(TAG, "✅ 서버 백업 성공")

            // ✅ 8. lastSyncAt 업데이트
            val currentTime = java.text.SimpleDateFormat(
                "yyyy-MM-dd'T'HH:mm:ss",
                java.util.Locale.getDefault()
            ).format(java.util.Date())

            val updatedUser = localUser.copy(
                isSynced = true,
                lastSyncAt = currentTime
            )

            userRepository.localUserUpdate(updatedUser)

            Log.d(TAG, "━━━━━━━━━━━━━━━━━━━━")
            Log.d(TAG, "✅ 자동 백업 완료: ${allRecords.size}개")
            Log.d(TAG, "⏰ 백업 시간: $currentTime")
            Log.d(TAG, "━━━━━━━━━━━━━━━━━━━━")

            Result.success()

        } catch (e: Exception) {
            Log.e(TAG, "❌ 자동 백업 오류", e)
            Result.retry()
        }
    }
}