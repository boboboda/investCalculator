package com.bobodroid.myapplication.models.datamodels.service.BackupApi

import android.util.Log
import com.bobodroid.myapplication.models.datamodels.repository.InvestRepository
import com.bobodroid.myapplication.models.datamodels.repository.UserRepository
import com.bobodroid.myapplication.models.datamodels.roomDb.LocalUserData
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 백업 실행 결과
 */
sealed interface BackupOutcome {
    /** 서버 저장 완료 */
    data class Success(val recordCount: Int, val syncedAt: String) : BackupOutcome

    /** 백업할 필요가 없거나 조건이 맞지 않아 실행하지 않음 */
    data class Skipped(val reason: String) : BackupOutcome

    /** 서버에 이 기기에 없는 기록이 있어 보류함 (복구 또는 덮어쓰기 선택 필요) */
    data class Held(val serverRecordCount: Int?) : BackupOutcome

    /** 네트워크 오류 등으로 실패 (다시 시도할 수 있음) */
    data class Failed(val message: String, val exception: Exception? = null) : BackupOutcome
}

/**
 * 서버 백업 전송을 담당하는 단일 진입점
 *
 * 자동 백업(BackupWorker), 수동 백업(SyncToServerUseCase),
 * 수익률 알림 저장 전 백업(FcmAlarmViewModel)이 모두 이 클래스를 사용한다.
 */
@Singleton
class BackupSyncManager @Inject constructor(
    private val userRepository: UserRepository,
    private val investRepository: InvestRepository,
    private val syncPrefs: BackupSyncPrefs
) {
    // 자동/수동 백업이 동시에 실행되어 서로 기준값을 덮어쓰지 않도록 한 번에 하나만 실행
    private val mutex = Mutex()

    /**
     * @param force 사용자가 "이 기기 기록으로 덮어쓰기"를 선택한 경우 true
     * @param requireSocialLogin false이면 소셜 로그인 없이도 deviceId 기준으로 백업
     */
    suspend fun backup(
        force: Boolean = false,
        requireSocialLogin: Boolean = true
    ): BackupOutcome {
        return mutex.withLock {
            try {
                performBackup(force, requireSocialLogin)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "백업 중 오류", e)
                BackupOutcome.Failed("백업에 실패했습니다", e)
            }
        }
    }

    private suspend fun performBackup(force: Boolean, requireSocialLogin: Boolean): BackupOutcome {
        val localUser = userRepository.userData.first()?.localUserData
            ?: return BackupOutcome.Failed("사용자 정보를 찾을 수 없습니다")

        if (requireSocialLogin && !isLinked(localUser)) {
            return BackupOutcome.Skipped("소셜 로그인이 필요합니다")
        }

        val records = investRepository.getAllCurrencyRecords().first()
        val syncBaseAt = syncPrefs.getSyncBaseAt()

        // 한 번도 동기화한 적 없는 기기의 빈 목록은 서버에 보낼 이유가 없음
        // (기록을 전부 삭제한 경우는 기준값이 있으므로 정상적으로 전송됨)
        if (records.isEmpty() && syncBaseAt == null && !force) {
            return BackupOutcome.Skipped("백업할 기록이 없습니다")
        }

        val request = BackupRequest(
            deviceId = localUser.id.toString(),
            socialId = localUser.socialId,
            socialType = localUser.socialType,
            currencyRecords = BackupMapper.toDtoList(records),
            syncBaseAt = syncBaseAt,
            force = force
        )

        Log.d(TAG, "백업 전송: ${records.size}개 (force=$force, 기준값=${syncBaseAt != null})")
        val response = BackupApi.backupService.createBackup(request)

        if (response.success) {
            response.data?.lastBackupAt?.let { syncPrefs.setSyncBaseAt(it) }
            syncPrefs.setHeld(false)

            val syncedAt = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                .format(Date())

            val latestUser = userRepository.userData.first()?.localUserData ?: localUser
            userRepository.localUserUpdate(
                latestUser.copy(isSynced = true, lastSyncAt = syncedAt)
            )

            val count = response.data?.recordCount ?: records.size
            Log.d(TAG, "백업 완료: ${count}개")
            return BackupOutcome.Success(count, syncedAt)
        }

        if (response.code == CODE_SYNC_REQUIRED) {
            syncPrefs.setHeld(true)
            Log.w(TAG, "백업 보류: 서버 기록 ${response.data?.recordCount}개가 이 기기와 다름")
            return BackupOutcome.Held(response.data?.recordCount)
        }

        Log.e(TAG, "백업 실패: ${response.message}")
        return BackupOutcome.Failed(response.message)
    }

    private fun isLinked(user: LocalUserData): Boolean {
        return !user.socialId.isNullOrEmpty() && user.socialType != "NONE"
    }

    private companion object {
        const val TAG = "BackupSyncManager"
        const val CODE_SYNC_REQUIRED = "SYNC_REQUIRED"
    }
}
