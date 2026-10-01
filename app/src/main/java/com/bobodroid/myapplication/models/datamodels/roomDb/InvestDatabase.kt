package com.bobodroid.myapplication.models.datamodels.roomDb

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase


@Database(
    entities = [
        LocalUserData::class,
        ExchangeRate::class,
        CurrencyRecord::class,
        BacktestHistoryEntity::class
    ],
    version = 35,
    exportSchema = true,
    autoMigrations = [
        AutoMigration(from = 31, to = 32),
        AutoMigration(from = 32, to = 33),
        AutoMigration(from = 33, to = 34),
        AutoMigration(from = 34, to = 35)
    ]
)
abstract class InvestDatabase : RoomDatabase() {
    abstract fun localUserDao(): LocalUserDatabaseDao
    abstract fun exchangeRateDao(): ExchangeRateDataBaseDao
    abstract fun currencyRecordDao(): CurrencyRecordDao
    abstract fun backtestHistoryDao(): BacktestHistoryDao
}