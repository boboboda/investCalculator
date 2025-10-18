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
            val updatedUser = localUserDatabaseDao.updateAndGetUser(localUserData)

            if (updatedUser != null) {
                val currentUserData = _userData.value
                _userData.value = UserData(  // ⭐ 변경
                    localUserData = updatedUser,
                    exchangeRates = currentUserData?.exchangeRates
                )
                Log.d(TAG("UserRepository",""), "유저 업데이트 성공 : $updatedUser")
            }
        } catch (e: Exception) {
            Log.e(TAG("UserRepository",""), "유저 업데이트 실패", e)
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