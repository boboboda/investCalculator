package com.bobodroid.myapplication.models.datamodels.roomDb

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase
import java.util.UUID

/**
 * 투자 기록 데이터베이스
 * - LocalUserData: 사용자 정보 (UUID)
 * - ExchangeRate: 환율 정보 (UUID)
 * - CurrencyRecord: 통합 외화 기록 (UUID)
 *
 * Version 32: lastSyncAt 필드 추가
 */
@Database(
    entities = [
        LocalUserData::class,
        ExchangeRate::class,
        CurrencyRecord::class
    ],
    version = 32,
    exportSchema = true,
    autoMigrations = [
        AutoMigration(from = 31, to = 32)
    ]
)
abstract class InvestDatabase : RoomDatabase() {
    abstract fun localUserDao(): LocalUserDatabaseDao
    abstract fun exchangeRateDao(): ExchangeRateDataBaseDao
    abstract fun currencyRecordDao(): CurrencyRecordDao
}