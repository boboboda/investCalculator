package com.bobodroid.myapplication.models.datamodels.repository

import android.util.Log
import com.bobodroid.myapplication.MainActivity.Companion.TAG
import com.bobodroid.myapplication.models.datamodels.roomDb.LocalUserData
import com.bobodroid.myapplication.models.datamodels.roomDb.LocalUserDatabaseDao
import com.bobodroid.myapplication.models.datamodels.useCases.UserDataType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject
import com.bobodroid.myapplication.util.result.Result
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(
    private val localUserDatabaseDao: LocalUserDatabaseDao
){
    private val _userData = MutableStateFlow<UserDataType?>(null)
    val userData = _userData.asStateFlow()

    private val _localUserData = MutableStateFlow<LocalUserData?>(null)
    val localUserData = _localUserData.asStateFlow()

    suspend fun localUserUpdate(localUserData: LocalUserData) {
        try {

            val updatedUser = localUserDatabaseDao.updateAndGetUser(localUserData)
            _localUserData.value = updatedUser
        } catch (e: Exception) {
            Log.e(TAG("UserRepository",""), "유저 업데이트 실패", e)
        }
    }

    suspend fun localUserAdd(localUserData: LocalUserData) {
        try {
            localUserDatabaseDao.insert(localUserData)
            _localUserData.value = localUserData
        } catch (e: Exception) {
            Log.e(TAG("UserRepository",""), "유저 생성 실패", e)
        }
    }

    suspend fun localUserDataDelete() {
        try {
            localUserDatabaseDao.deleteAll()
            _localUserData.value = null
            _userData.value = null
        } catch (e: Exception) {
            Log.e(TAG("UserRepository", ""), "유저 삭제 실패", e)
        }
    }

    suspend fun localUserDataGet(): Flow<LocalUserData?> = localUserDatabaseDao.getUserData()

    fun updateUserData(data: UserDataType) {
        _userData.value = data
    }

    suspend fun waitForUserData(): UserDataType {
        return userData.filterNotNull().first()
    }
}