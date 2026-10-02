package com.bobodroid.myapplication.models.datamodels.service.BackupApi

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 백업 동기화 기준 정보 (이 기기 기준)
 *
 * - syncBaseAt: 마지막으로 백업/복구에 성공했을 때 서버가 알려준 lastBackupAt.
 *   서버는 이 값으로 "이 기기가 서버 최신 상태를 알고 있는지" 판단한다.
 * - held: 서버에 이 기기에 없는 기록이 있어 자동 백업이 보류된 상태 (화면 안내용)
 */
@Singleton
class BackupSyncPrefs @Inject constructor(
    @ApplicationContext context: Context
) {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _held = MutableStateFlow(prefs.getBoolean(KEY_HELD, false))
    val held: StateFlow<Boolean> = _held.asStateFlow()

    fun getSyncBaseAt(): String? = prefs.getString(KEY_SYNC_BASE_AT, null)

    fun setSyncBaseAt(value: String?) {
        prefs.edit().putString(KEY_SYNC_BASE_AT, value).apply()
    }

    fun setHeld(value: Boolean) {
        prefs.edit().putBoolean(KEY_HELD, value).apply()
        _held.value = value
    }

    /** 소셜 연동 해제 등으로 동기화 기준을 처음 상태로 되돌릴 때 */
    fun clear() {
        prefs.edit().clear().apply()
        _held.value = false
    }

    private companion object {
        const val PREFS_NAME = "backup_sync_prefs"
        const val KEY_SYNC_BASE_AT = "sync_base_at"
        const val KEY_HELD = "held"
    }
}
