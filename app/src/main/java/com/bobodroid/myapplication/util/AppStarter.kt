package com.bobodroid.myapplication.util

import android.content.Context
import android.util.Log
import com.bobodroid.myapplication.models.datamodels.repository.LatestRateRepository
import com.bobodroid.myapplication.models.datamodels.repository.NoticeRepository
import com.bobodroid.myapplication.models.datamodels.repository.UserRepository
import com.bobodroid.myapplication.models.datamodels.service.BackupApi.BackupSyncPrefs
import com.bobodroid.myapplication.models.datamodels.useCases.LocalExistCheckUseCase
import com.bobodroid.myapplication.util.AdMob.AdManager
import com.bobodroid.myapplication.worker.BackupScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class AppStarter @Inject constructor(
    private val localExistCheckUseCase: LocalExistCheckUseCase,
    private val noticeRepository: NoticeRepository,
    private val userRepository: UserRepository,
    private val backupSyncPrefs: BackupSyncPrefs,
    private val backupScheduler: BackupScheduler,
) {
    fun startApp(context: Context) {
        CoroutineScope(Dispatchers.IO).launch {
            localExistCheckUseCase.invoke()
            noticeRepository.loadNotice()
            scheduleCatchUpBackup()
        }
    }

    /**
     * 자동 백업 전환 이후 처음 실행하는 연동 사용자의 백업을 1회 예약한다.
     * - 기록을 더 수정하지 않는 기존 무료 사용자도 서버에 백업되도록 하기 위함
     * - 이 기기에서 한 번이라도 백업/복구에 성공하면 기준값이 저장되어 더 이상 예약하지 않음
     * - 서버 기록과 다르면 BackupSyncManager가 보류하므로 서버 데이터를 덮어쓰지 않음
     */
    private suspend fun scheduleCatchUpBackup() {
        try {
            if (backupSyncPrefs.getSyncBaseAt() != null) return

            val localUser = userRepository.userData.first()?.localUserData ?: return
            if (localUser.socialId.isNullOrEmpty() || localUser.socialType == "NONE") return

            backupScheduler.scheduleBackup()
            Log.d("AppStarter", "백업 보정 예약")
        } catch (e: Exception) {
            Log.e("AppStarter", "백업 보정 예약 실패", e)
        }
    }
}
