package com.bobodroid.myapplication.models.datamodels.useCases

import android.util.Log
import com.bobodroid.myapplication.MainActivity.Companion.TAG
import com.bobodroid.myapplication.models.datamodels.repository.UserRepository
import com.bobodroid.myapplication.models.datamodels.roomDb.LocalUserData
import com.bobodroid.myapplication.models.datamodels.service.BackupApi.BackupApi
import com.bobodroid.myapplication.models.datamodels.service.UserApi.UserApi
import com.bobodroid.myapplication.util.result.Result
import java.util.UUID
import javax.inject.Inject

/**
 * 계정 전환 결과
 */
data class AccountSwitchResult(
    val switchedUser: LocalUserData,
    val hasBackupData: Boolean,
    val backupRecordCount: Int = 0,
    val lastBackupAt: String? = null
)

/**
 * 계정 전환 UseCase
 *
 * 소셜 로그인 시 기존 서버 계정을 발견했을 때
 * 로컬 계정을 서버 계정으로 전환하는 역할
 */
class AccountSwitchUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(
        serverDeviceId: String,
        localUser: LocalUserData
    ): Result<AccountSwitchResult> {
        return try {
            Log.d(TAG("AccountSwitchUseCase", "invoke"), "━━━━━━━━━━━━━━━━━━━━━━━━━━")
            Log.d(TAG("AccountSwitchUseCase", "invoke"), "계정 전환 시작")
            Log.d(TAG("AccountSwitchUseCase", "invoke"), "  - 현재 로컬 디바이스ID: ${localUser.id}")
            Log.d(TAG("AccountSwitchUseCase", "invoke"), "  - 서버 디바이스ID: $serverDeviceId")

            // 1. 서버에서 기존 계정 정보 가져오기
            val serverUserResponse = UserApi.userService.getUserByDeviceId(serverDeviceId)

            if (!serverUserResponse.success || serverUserResponse.data == null) {
                Log.e(TAG("AccountSwitchUseCase", "invoke"), "서버 계정 조회 실패")
                return Result.Error(
                    message = "서버 계정을 찾을 수 없습니다",
                    exception = Exception("Server account not found")
                )
            }

            val serverData = serverUserResponse.data
            Log.d(TAG("AccountSwitchUseCase", "invoke"), "✅ 서버 계정 조회 성공")

            // 2. 백업 데이터 존재 여부 확인
            var hasBackupData = false
            var backupRecordCount = 0
            var lastBackupAt: String? = null

            try {
                Log.d(TAG("AccountSwitchUseCase", "invoke"), "백업 데이터 확인 중...")
                val backupResponse = BackupApi.backupService.restoreByDeviceId(serverDeviceId)

                if (backupResponse.success && backupResponse.data != null) {
                    hasBackupData = true
                    backupRecordCount = backupResponse.data.recordCount
                    lastBackupAt = backupResponse.data.lastBackupAt

                    Log.d(TAG("AccountSwitchUseCase", "invoke"), "✅ 백업 데이터 발견: ${backupRecordCount}개")
                    Log.d(TAG("AccountSwitchUseCase", "invoke"), "  - 마지막 백업: $lastBackupAt")
                } else {
                    Log.d(TAG("AccountSwitchUseCase", "invoke"), "백업 데이터 없음")
                }
            } catch (e: Exception) {
                Log.w(TAG("AccountSwitchUseCase", "invoke"), "백업 데이터 확인 실패 (계속 진행)", e)
            }

            // 3. 로컬 디바이스ID를 서버 디바이스ID로 변경
            val newDeviceId = try {
                UUID.fromString(serverDeviceId)
            } catch (e: Exception) {
                Log.e(TAG("AccountSwitchUseCase", "invoke"), "디바이스ID 변환 실패", e)
                return Result.Error(
                    message = "잘못된 디바이스 ID 형식입니다",
                    exception = e
                )
            }

            // 4. 로컬 DB 삭제 (기존 로컬 계정)
            userRepository.localUserDataDelete()
            Log.d(TAG("AccountSwitchUseCase", "invoke"), "기존 로컬 계정 삭제 완료")

            // 5. 새로운 로컬 계정 생성 (서버 디바이스ID 사용)
            val switchedUser = localUser.copy(
                id = newDeviceId,
                socialId = serverData.socialId ?: localUser.socialId,
                socialType = serverData.socialType ?: localUser.socialType,
                email = serverData.email ?: localUser.email,
                nickname = serverData.nickname ?: localUser.nickname,
                profileUrl = serverData.profileUrl ?: localUser.profileUrl,
                isSynced = true
            )

            userRepository.localUserAdd(switchedUser)
            Log.d(TAG("AccountSwitchUseCase", "invoke"), "✅ 새 로컬 계정 생성 완료 (ID: ${switchedUser.id})")

            Log.d(TAG("AccountSwitchUseCase", "invoke"), "✅ 계정 전환 완료")
            Log.d(TAG("AccountSwitchUseCase", "invoke"), "━━━━━━━━━━━━━━━━━━━━━━━━━━")

            val result = AccountSwitchResult(
                switchedUser = switchedUser,
                hasBackupData = hasBackupData,
                backupRecordCount = backupRecordCount,
                lastBackupAt = lastBackupAt
            )

            Result.Success(
                data = result,
                message = "기존 계정으로 전환되었습니다"
            )

        } catch (e: Exception) {
            Log.e(TAG("AccountSwitchUseCase", "invoke"), "계정 전환 실패", e)
            Result.Error(
                message = "계정 전환 중 오류가 발생했습니다: ${e.message}",
                exception = e
            )
        }
    }
}

/**
 * 서버 계정 정보 (UI 표시용)
 */
data class ServerAccountInfo(
    val deviceId: String,
    val email: String?,
    val nickname: String?,
    val lastSyncAt: String?,
    val hasBackupData: Boolean,
    val backupRecordCount: Int = 0
)