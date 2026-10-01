package com.bobodroid.myapplication.models.datamodels.roomDb

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import java.util.UUID

@Dao
interface BacktestHistoryDao {

    @Query("SELECT * FROM backtest_history ORDER BY created_at DESC")
    fun getAllHistory(): Flow<List<BacktestHistoryEntity>>

    @Query("SELECT * FROM backtest_history WHERE id = :id")
    suspend fun getHistoryById(id: UUID): BacktestHistoryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: BacktestHistoryEntity)

    @Query("DELETE FROM backtest_history WHERE id = :id")
    suspend fun delete(id: UUID)

    @Query("DELETE FROM backtest_history")
    suspend fun deleteAll()
}
