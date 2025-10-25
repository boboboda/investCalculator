package com.bobodroid.myapplication.models.datamodels.roomDb

import androidx.room.*
import com.bobodroid.myapplication.data.local.entity.ExchangeRateDto
import kotlinx.coroutines.flow.Flow

/**
 * 환율 데이터 DAO (DTO 버전)
 *
 * [변경 사항]
 * 기존: ExchangeRate 사용
 * 신규: ExchangeRateDto 사용
 */
@Dao
interface ExchangeRateDataBaseDao {

    @Query("SELECT * from exchangeRate_table")
    fun getRateData(): Flow<List<ExchangeRateDto>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(rateData: ExchangeRateDto)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(exchangeRates: List<ExchangeRateDto>)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun update(rateData: ExchangeRateDto)

    @Query("DELETE from exchangeRate_table")
    suspend fun deleteAll()

    @Delete
    suspend fun deleteRate(rateData: ExchangeRateDto)
}