// app/src/main/java/com/bobodroid/myapplication/models/datamodels/service/BackupApi/BackupNoticeManager.kt
package com.bobodroid.myapplication.models.datamodels.service.BackupApi

import android.content.Context
import android.util.Log
import com.bobodroid.myapplication.models.datamodels.repository.InvestRepository
import com.bobodroid.myapplication.models.datamodels.repository.UserRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 백업이 되고 있지 않은 상태
 * - NONE: 안내할 것 없음 (정상 백업 중이거나 기록이 없음)
 * - NEEDS_LOGIN: 기록은 있는데 소셜 로그인 미연동 → 백업되지 않음
 * - HELD: 서버 기록과 달라 자동 백업이 보류됨
 */
enum class BackupNoticeStatus { NONE, NEEDS_LOGIN, HELD }

/**
 * 백업 안내 관리
 * - status: 하단 탭 점, 마이페이지 클라우드 백업 배지 표시용
 * - promptEvents: 기록 저장 직후 안내 배너 표시용 (첫 기록 후 1회, 이후 기록 5개마다 1회, 최대 3회)
 */
@Singleton
class BackupNoticeManager @Inject constructor(
    @ApplicationContext context: Context,
    private val userRepository: UserRepository,
    investRepository: InvestRepository,
    backupSyncPrefs: BackupSyncPrefs
) {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    val status: StateFlow<BackupNoticeStatus> = combine(
        userRepository.userData,
        investRepository.getAllCurrencyRecords(),
        backupSyncPrefs.held
    ) { userData, records, held ->
        val localUser = userData?.localUserData
        Log.d(TAG, "[백업안내] 상태 계산: user=${localUser != null}, socialId=${localUser?.socialId}, socialType=${localUser?.socialType}, records=${records.size}, held=$held")
        when {
            localUser == null -> BackupNoticeStatus.NONE
            isLinked(localUser.socialId, localUser.socialType) ->
                if (held) BackupNoticeStatus.HELD else BackupNoticeStatus.NONE
            records.isNotEmpty() -> BackupNoticeStatus.NEEDS_LOGIN
            else -> BackupNoticeStatus.NONE
        }
    }.stateIn(scope, SharingStarted.Eagerly, BackupNoticeStatus.NONE)

    private val _promptEvents = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val promptEvents: SharedFlow<Unit> = _promptEvents

    /** 새 기록이 저장된 직후 호출. 소셜 미연동이면 조건에 맞을 때 안내 이벤트를 보낸다. */
    fun onRecordAdded() {
        val localUser = userRepository.userData.value?.localUserData
        if (localUser == null) {
            Log.d(TAG, "[백업안내] 안내 건너뜀: 사용자 정보 없음")
            return
        }
        if (isLinked(localUser.socialId, localUser.socialType)) {
            Log.d(TAG, "[백업안내] 안내 건너뜀: 이미 소셜 연동됨")
            return
        }

        val shown = prefs.getInt(KEY_SHOWN_COUNT, 0)
        if (shown >= MAX_PROMPTS) {
            Log.d(TAG, "[백업안내] 안내 건너뜀: 최대 횟수($MAX_PROMPTS)에 도달")
            return
        }

        val sinceLast = prefs.getInt(KEY_SINCE_LAST, 0) + 1
        val shouldShow = shown == 0 || sinceLast >= RECORDS_PER_PROMPT

        if (shouldShow) {
            prefs.edit()
                .putInt(KEY_SHOWN_COUNT, shown + 1)
                .putInt(KEY_SINCE_LAST, 0)
                .apply()
            val emitted = _promptEvents.tryEmit(Unit)
            Log.d(TAG, "[백업안내] 안내 이벤트 발행: shown=${shown + 1}, emitted=$emitted")
        } else {
            prefs.edit().putInt(KEY_SINCE_LAST, sinceLast).apply()
            Log.d(TAG, "[백업안내] 안내 건너뜀: 다음 안내까지 ${RECORDS_PER_PROMPT - sinceLast}개 남음 (shown=$shown)")
        }
    }

    private fun isLinked(socialId: String?, socialType: String?): Boolean =
        !socialId.isNullOrEmpty() && socialType != "NONE"

    companion object {
        private const val TAG = "로그"
        private const val PREFS_NAME = "backup_notice_prefs"
        private const val KEY_SHOWN_COUNT = "shown_count"
        private const val KEY_SINCE_LAST = "since_last"
        private const val MAX_PROMPTS = 3
        private const val RECORDS_PER_PROMPT = 5
    }
}
