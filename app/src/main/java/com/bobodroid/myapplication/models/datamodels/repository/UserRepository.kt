package com.bobodroid.myapplication.models.datamodels.repository

import com.bobodroid.myapplication.models.datamodels.firebase.ExchangeRate
import com.bobodroid.myapplication.models.datamodels.roomDb.LocalUserData
import com.bobodroid.myapplication.models.datamodels.roomDb.LocalUserDatabaseDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

class UserRepository @Inject constructor(
    private val localUserDatabaseDao: LocalUserDatabaseDao
) {

    //로컬 유저 생성
    suspend fun localUserAdd(localUserData: LocalUserData) : LocalUserData {
        localUserDatabaseDao.insert(localUserData)
        return localUserData
    }

    suspend fun localUserUpdate(localUserData: LocalUserData) = localUserDatabaseDao.update(localUserData)
    suspend fun localUserDataDelete() = localUserDatabaseDao.deleteAll()
    fun localUserDataGet(): Flow<LocalUserData> = localUserDatabaseDao.getUserData().flowOn(
        Dispatchers.IO).conflate()
}