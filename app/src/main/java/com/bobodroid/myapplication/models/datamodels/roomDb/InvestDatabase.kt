package com.bobodroid.myapplication.models.datamodels.roomDb

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase
import com.bobodroid.myapplication.data.local.entity.CurrencyRecordDto
import com.bobodroid.myapplication.data.local.entity.ExchangeRateDto
import com.bobodroid.myapplication.data.local.entity.LocalUserDto

/**
 * 투자 기록 데이터베이스
 * - LocalUserData: 사용자 정보 (UUID)
 * - ExchangeRate: 환율 정보 (UUID)
 * - CurrencyRecord: 통합 외화 기록 (UUID)
 *
 * Version 32: lastSyncAt 필드 추가
 * Version 33: interstitialAdCount 필드 추가 (전면 광고 카운트)
 * Version 34: 프리미엄 & 광고 확장 필드 추가
 *            - premiumType, premiumExpiryDate, premiumGrantedBy, premiumGrantedAt
 *            - dailyRewardUsed, lastRewardDate, totalRewardCount
 */
@Database(
    entities = [
        LocalUserDto::class,
        ExchangeRateDto::class,
        CurrencyRecordDto::class
    ],
    version = 37,
    exportSchema = true,
    autoMigrations = [
        AutoMigration(from = 31, to = 32),
        AutoMigration(from = 32, to = 33),
        AutoMigration(from = 33, to = 34),
        AutoMigration(from = 34, to = 35),
        AutoMigration(from = 35, to = 36, spec = Migration35To36Spec::class),
        AutoMigration(from = 36, to = 37),
    ]
)
abstract class InvestDatabase : RoomDatabase() {
    abstract fun localUserDao(): LocalUserDatabaseDao
    abstract fun exchangeRateDao(): ExchangeRateDataBaseDao
    abstract fun currencyRecordDao(): CurrencyRecordDao
}