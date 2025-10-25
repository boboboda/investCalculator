package com.bobodroid.myapplication.models.datamodels.roomDb

import android.util.Log
import androidx.room.*
import com.bobodroid.myapplication.MainActivity.Companion.TAG
import kotlinx.coroutines.flow.Flow
import java.util.UUID

/**
 * 사용자 데이터 DAO (UUID)
 */
//@Dao
//interface LocalUserDatabaseDao {
//
//    @Query("SELECT * from LocalUserData_table")
//    fun getUserData(): Flow<LocalUserData>
//
//    @Insert(onConflict = OnConflictStrategy.REPLACE)
//    suspend fun insert(localUserData: LocalUserData)
//
//    @Update(onConflict = OnConflictStrategy.REPLACE)
//    suspend fun update(localUserData: LocalUserData): Int
//
//    @Transaction
//    suspend fun updateAndGetUser(user: LocalUserData): LocalUserData? {
//        val updateCount = update(user)
//
//        Log.d(TAG("LocalUserDao", "updateAndGetUser"), "업데이트 결과: $updateCount")
//
//        return if (updateCount > 0) {
//            getUserById(user.id)
//        } else {
//            null
//        }
//    }
//
//    @Query("SELECT * from LocalUserData_table WHERE id = :id")
//    suspend fun getUserById(id: UUID): LocalUserData?
//
//    @Query("DELETE from LocalUserData_table")
//    suspend fun deleteAll()
//
//    @Delete
//    suspend fun deleteNote(localUserData: LocalUserData)
//}

/**
 * 환율 데이터 DAO (UUID)
 */
@Dao
interface ExchangeRateDataBaseDao {

    @Query("SELECT * from exchangeRate_table")
    fun getRateData(): Flow<List<ExchangeRate>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(rateData: ExchangeRate)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(exchangeRates: List<ExchangeRate>)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun update(rateData: ExchangeRate)

    @Query("DELETE from exchangeRate_table")
    suspend fun deleteAll()

    @Delete
    suspend fun deleteRate(rateData: ExchangeRate)
}

