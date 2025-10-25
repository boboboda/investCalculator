package com.bobodroid.myapplication.models.datamodels.roomDb

import android.util.Log
import androidx.room.*
import com.bobodroid.myapplication.MainActivity.Companion.TAG
import com.bobodroid.myapplication.data.local.entity.LocalUserDto
import kotlinx.coroutines.flow.Flow
import java.util.UUID

/**
 * 사용자 데이터 DAO (DTO 버전)
 *
 * [변경 사항]
 * 기존: LocalUserData 사용
 * 신규: LocalUserDto 사용
 */
@Dao
interface LocalUserDatabaseDao {

    @Query("SELECT * from LocalUserData_table")
    fun getUserData(): Flow<LocalUserDto>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(localUserDto: LocalUserDto)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun update(localUserDto: LocalUserDto): Int

    @Transaction
    suspend fun updateAndGetUser(user: LocalUserDto): LocalUserDto? {
        val updateCount = update(user)

        Log.d(TAG("LocalUserDao", "updateAndGetUser"), "업데이트 결과: $updateCount")

        return if (updateCount > 0) {
            getUserById(user.id)
        } else {
            null
        }
    }

    @Query("SELECT * from LocalUserData_table WHERE id = :id")
    suspend fun getUserById(id: UUID): LocalUserDto?

    @Query("DELETE from LocalUserData_table")
    suspend fun deleteAll()

    @Delete
    suspend fun deleteNote(localUserDto: LocalUserDto)
}