package com.bobodroid.myapplication.data.repository

import android.util.Log
import com.bobodroid.myapplication.MainActivity.Companion.TAG
import com.bobodroid.myapplication.domain.repository.IUserRepository
import com.bobodroid.myapplication.models.datamodels.roomDb.LocalUserData
import com.bobodroid.myapplication.models.datamodels.roomDb.LocalUserDatabaseDao
import com.bobodroid.myapplication.models.datamodels.useCases.UserData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * User Repository 구현체
 *
 * [변경 사항]
 * 기존: UserRepository
 * 신규: UserRepositoryImpl
 *
 * Data Layer의 실제 데이터 접근 구현
 * - Room Database와 직접 통신
 * - Domain의 IUserRepository 인터페이스 구현
 */
@Singleton
class UserRepositoryImpl @Inject constructor(
    private val localUserDatabaseDao: LocalUserDatabaseDao
) : IUserRepository {

    private val _userData = MutableStateFlow<UserData?>(null)
    override val userData: StateFlow<UserData?> = _userData.asStateFlow()

    override suspend fun localUserUpdate(localUserData: LocalUserData) = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG("UserRepositoryImpl", "localUserUpdate"), "━━━━━━━━━━━━━━━━━━━━━━━━━━")
            Log.d(TAG("UserRepositoryImpl", "localUserUpdate"), "🔥 localUserUpdate 호출됨!")
            Log.d(TAG("UserRepositoryImpl", "localUserUpdate"), "요청 데이터:")
            Log.d(TAG("UserRepositoryImpl", "localUserUpdate"), "  - id: ${localUserData.id}")
            Log.d(TAG("UserRepositoryImpl", "localUserUpdate"), "  - id type: ${localUserData.id::class.java}")
            Log.d(TAG("UserRepositoryImpl", "localUserUpdate"), "  - isPremium: ${localUserData.isPremium}")
            Log.d(TAG("UserRepositoryImpl", "localUserUpdate"), "  - socialId: ${localUserData.socialId}")
            Log.d(TAG("UserRepositoryImpl", "localUserUpdate"), "  - email: ${localUserData.email}")

            val updatedUser = localUserDatabaseDao.updateAndGetUser(localUserData)

            Log.d(TAG("UserRepositoryImpl", "localUserUpdate"), "━━━━━━━━━━━━━━━━━━━━━━━━━━")
            Log.d(TAG("UserRepositoryImpl", "localUserUpdate"), "updatedUser 결과: $updatedUser")

            if (updatedUser != null) {
                val currentUserData = _userData.value
                _userData.value = UserData(
                    localUserData = updatedUser,
                    exchangeRates = currentUserData?.exchangeRates
                )
                Log.d(TAG("UserRepositoryImpl", "localUserUpdate"), "✅ 유저 업데이트 성공!")
                Log.d(TAG("UserRepositoryImpl", "localUserUpdate"), "  - 최종 isPremium: ${updatedUser.isPremium}")
            } else {
                Log.e(TAG("UserRepositoryImpl", "localUserUpdate"), "❌ updatedUser가 null입니다!")
                Log.e(TAG("UserRepositoryImpl", "localUserUpdate"), "❌ 원인: update 또는 getUserById 실패")
            }
            Log.d(TAG("UserRepositoryImpl", "localUserUpdate"), "━━━━━━━━━━━━━━━━━━━━━━━━━━")
        } catch (e: Exception) {
            Log.e(TAG("UserRepositoryImpl", "localUserUpdate"), "❌ 유저 업데이트 실패", e)
            Log.e(TAG("UserRepositoryImpl", "localUserUpdate"), "  - Exception: ${e.message}")
            Log.e(TAG("UserRepositoryImpl", "localUserUpdate"), "  - Stack trace:", e)
        }
    }

    override suspend fun localUserAdd(localUserData: LocalUserData) = withContext(Dispatchers.IO) {
        try {
            localUserDatabaseDao.insert(localUserData)

            _userData.value = UserData(
                localUserData = localUserData,
                exchangeRates = null
            )
        } catch (e: Exception) {
            Log.e(TAG("UserRepositoryImpl", "localUserAdd"), "유저 생성 실패", e)
        }
    }

    override suspend fun localUserDataDelete() = withContext(Dispatchers.IO) {
        try {
            localUserDatabaseDao.deleteAll()
            _userData.value = null
        } catch (e: Exception) {
            Log.e(TAG("UserRepositoryImpl", "localUserDataDelete"), "유저 삭제 실패", e)
        }
    }

    override fun localUserDataGet(): Flow<LocalUserData?> =
        localUserDatabaseDao.getUserData()

    override fun updateUserData(data: UserData) {
        _userData.value = data
    }

    override suspend fun waitForUserData(): UserData {
        return userData.filterNotNull().first()
    }
}