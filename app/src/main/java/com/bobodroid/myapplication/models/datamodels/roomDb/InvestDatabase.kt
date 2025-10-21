package com.bobodroid.myapplication.models.datamodels.roomDb

import androidx.room.Database
import androidx.room.RoomDatabase
import java.util.UUID

/**
 * 투자 기록 데이터베이스
 * - LocalUserData: 사용자 정보 (UUID)
 * - ExchangeRate: 환율 정보 (UUID)
 * - CurrencyRecord: 통합 외화 기록 (UUID)
 *
 * fallbackToDestructiveMigration 사용으로 마이그레이션 불필요
 */
@Database(
    entities = [
        LocalUserData::class,
        ExchangeRate::class,
        CurrencyRecord::class
    ],
    version = 31,
    exportSchema = true
)
abstract class InvestDatabase : RoomDatabase() {
    abstract fun localUserDao(): LocalUserDatabaseDao
    abstract fun exchangeRateDao(): ExchangeRateDataBaseDao
    abstract fun currencyRecordDao(): CurrencyRecordDao
}