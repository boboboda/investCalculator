package com.bobodroid.myapplication.models.datamodels.repository

import android.util.Log
import com.bobodroid.myapplication.MainActivity.Companion.TAG
import com.bobodroid.myapplication.models.datamodels.roomDb.LocalUserData
import com.bobodroid.myapplication.models.datamodels.roomDb.LocalUserDatabaseDao
import com.bobodroid.myapplication.models.datamodels.useCases.UserData  // ⭐ 변경
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(
    private val localUserDatabaseDao: LocalUserDatabaseDao
){
    private val _userData = MutableStateFlow<UserData?>(null)  // ⭐ 변경
    val userData = _userData.asStateFlow()

    suspend fun localUserUpdate(localUserData: LocalUserData) = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG("UserRepository","localUserUpdate"), "━━━━━━━━━━━━━━━━━━━━━━━━━━")
            Log.d(TAG("UserRepository","localUserUpdate"), "🔥 localUserUpdate 호출됨!")
            Log.d(TAG("UserRepository","localUserUpdate"), "요청 데이터:")
            Log.d(TAG("UserRepository","localUserUpdate"), "  - id: ${localUserData.id}")
            Log.d(TAG("UserRepository","localUserUpdate"), "  - id type: ${localUserData.id::class.java}")
            Log.d(TAG("UserRepository","localUserUpdate"), "  - isPremium: ${localUserData.isPremium}")
            Log.d(TAG("UserRepository","localUserUpdate"), "  - socialId: ${localUserData.socialId}")
            Log.d(TAG("UserRepository","localUserUpdate"), "  - email: ${localUserData.email}")

            val updatedUser = localUserDatabaseDao.updateAndGetUser(localUserData)

            Log.d(TAG("UserRepository","localUserUpdate"), "━━━━━━━━━━━━━━━━━━━━━━━━━━")
            Log.d(TAG("UserRepository","localUserUpdate"), "updatedUser 결과: $updatedUser")

            if (updatedUser != null) {
                val currentUserData = _userData.value
                _userData.value = UserData(
                    localUserData = updatedUser,
                    exchangeRates = currentUserData?.exchangeRates
                )
                Log.d(TAG("UserRepository","localUserUpdate"), "✅ 유저 업데이트 성공!")
                Log.d(TAG("UserRepository","localUserUpdate"), "  - 최종 isPremium: ${updatedUser.isPremium}")
            } else {
                Log.e(TAG("UserRepository","localUserUpdate"), "❌ updatedUser가 null입니다!")
                Log.e(TAG("UserRepository","localUserUpdate"), "❌ 원인: update 또는 getUserById 실패")
            }
            Log.d(TAG("UserRepository","localUserUpdate"), "━━━━━━━━━━━━━━━━━━━━━━━━━━")
        } catch (e: Exception) {
            Log.e(TAG("UserRepository","localUserUpdate"), "❌ 유저 업데이트 실패", e)
            Log.e(TAG("UserRepository","localUserUpdate"), "  - Exception: ${e.message}")
            Log.e(TAG("UserRepository","localUserUpdate"), "  - Stack trace:", e)
        }
    }

    suspend fun localUserAdd(localUserData: LocalUserData) = withContext(Dispatchers.IO) {
        try {
            localUserDatabaseDao.insert(localUserData)

            _userData.value = UserData(  // ⭐ 변경
                localUserData = localUserData,
                exchangeRates = null
            )
        } catch (e: Exception) {
            Log.e(TAG("UserRepository",""), "유저 생성 실패", e)
        }
    }

    suspend fun localUserDataDelete() = withContext(Dispatchers.IO) {
        try {
            localUserDatabaseDao.deleteAll()
            _userData.value = null
        } catch (e: Exception) {
            Log.e(TAG("UserRepository", ""), "유저 삭제 실패", e)
        }
    }

    suspend fun localUserDataGet(): Flow<LocalUserData?> = localUserDatabaseDao.getUserData()

    fun updateUserData(data: UserData) {  // ⭐ 변경
        _userData.value = data
    }

    suspend fun waitForUserData(): UserData {  // ⭐ 변경
        return userData.filterNotNull().first()
    }
}