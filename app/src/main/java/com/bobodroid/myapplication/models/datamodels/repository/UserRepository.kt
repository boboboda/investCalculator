package com.bobodroid.myapplication.models.datamodels.repository

import android.util.Log
import com.bobodroid.myapplication.MainActivity.Companion.TAG
import com.bobodroid.myapplication.models.datamodels.roomDb.LocalUserData
import com.bobodroid.myapplication.models.datamodels.roomDb.LocalUserDatabaseDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject
import com.bobodroid.myapplication.util.result.Result
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow


class UserRepository @Inject constructor(
    private val localUserDatabaseDao: LocalUserDatabaseDao
) {
    suspend fun localUserUpdate(localUserData: LocalUserData): Result<LocalUserData> {
        return try {
            val updatedUser = localUserDatabaseDao.updateAndGetUser(localUserData)
            if (updatedUser != null) {
                Result.Success(
                    data = updatedUser,
                    message = "유저 정보가 업데이트되었습니다."
                )
            } else {
                Result.Error("업데이트할 유저를 찾을 수 없습니다.")
            }
        } catch (e: Exception) {
            Result.Error("유저 업데이트 실패", e)
        }
    }

    suspend fun localUserAdd(localUserData: LocalUserData): Result<LocalUserData> {
        return try {
            localUserDatabaseDao.insert(localUserData)
            Result.Success(
                data = localUserData,
                message = "새로운 유저가 생성되었습니다."
            )
        } catch (e: Exception) {
            Result.Error("유저 생성 실패", e)
        }
    }

    suspend fun localUserDataDelete(): Result<Unit> {
        return try {
            localUserDatabaseDao.deleteAll()
            Result.Success(
                data = Unit,
                message = "유저 데이터가 삭제되었습니다."
            )
        } catch (e: Exception) {
            Result.Error("유저 삭제 실패", e)
        }
    }

   suspend fun localUserDataGet(): Flow<Result<LocalUserData?>> = flow {
       val userData = localUserDatabaseDao.getUserData()
           .firstOrNull() // 최초의 데이터를 가져옴 (nullable)

       emit(Result.Success(
           data = userData,
           message = if (userData == null) "데이터가 없습니다." else "유저 데이터를 불러왔습니다."
       ))
    }
}