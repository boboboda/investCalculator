package com.bobodroid.myapplication.models.datamodels.roomDb

import androidx.room.Database
import androidx.room.DeleteTable
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [
    DrBuyRecord::class,
    DrSellRecord::class,
    YenBuyRecord::class,
    YenSellRecord::class,
    WonBuyRecord::class,
    WonSellRecord::class,
    LocalUserData::class,
    ExchangeRate::class,
    CurrencyRecord::class  // 새로운 통합 테이블 추가
],
    version = 28,  // 버전 27 -> 28로 증가
    exportSchema = false)
@DeleteTable.Entries(
    DeleteTable(tableName = "sellWon_table"),
    DeleteTable(tableName = "buyWon_table"),
    DeleteTable(tableName = "sellYen_table"),
    DeleteTable(tableName = "sellDollar_table")
)
@TypeConverters(Converters::class)  // UUID 변환용 TypeConverter 추가
abstract class InvestDatabase: RoomDatabase() {
    abstract fun DrBuyDao() : DollarBuyDatabaseDao
    abstract fun YenBuyDao() : YenBuyDatabaseDao
    abstract fun LocalUserDao() : LocalUserDatabaseDao
    abstract fun exchangeRateDao() : ExchangeRateDataBaseDao
    abstract fun currencyRecordDao() : CurrencyRecordDao  // 새로운 DAO 추가

    companion object {
        // 21 -> 22 수동 마이그레이션
        val MIGRATION_21_22 = object : Migration(21, 22) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // 1. 새 테이블 생성 (버전 22 스키마)
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS `LocalUserData_table_new` (
                        `id` BLOB NOT NULL,
                        `social_id` TEXT DEFAULT '',
                        `social_type` TEXT NOT NULL DEFAULT 'NONE',
                        `email` TEXT DEFAULT '',
                        `nickname` TEXT DEFAULT '',
                        `profile_url` TEXT DEFAULT '',
                        `is_synced` INTEGER NOT NULL DEFAULT 0,
                        `fcm_Token` TEXT DEFAULT '',
                        `rate_Reset_Count` INTEGER,
                        `reFresh_CreateAt` TEXT DEFAULT '',
                        `rate_Ad_Count` INTEGER,
                        `reward_ad_Showing_date` TEXT DEFAULT '',
                        `user_Reset_Date` TEXT DEFAULT '',
                        `user_Show_Notice_Date` TEXT DEFAULT '',
                        `dr_Buy_Spread` INTEGER,
                        `dr_Sell_Spread` INTEGER,
                        `yen_Buy_Spread` INTEGER,
                        `yen_Sell_Spread` INTEGER,
                        `monthly_profit_goal` INTEGER NOT NULL DEFAULT 0,
                        `goal_set_month` TEXT DEFAULT '',
                        `is_premium` INTEGER NOT NULL DEFAULT 0,
                        PRIMARY KEY(`id`)
                    )
                """.trimIndent())

                // 2. 기존 데이터를 복사하지 않고 진행
                // 3. 기존 테이블 삭제
                database.execSQL("DROP TABLE `LocalUserData_table`")

                // 4. 새 테이블 이름 변경
                database.execSQL("ALTER TABLE `LocalUserData_table_new` RENAME TO `LocalUserData_table`")
            }
        }

        // 22 -> 26 마이그레이션 (is_premium 확인 및 추가)
        val MIGRATION_22_26 = object : Migration(22, 26) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // is_premium 컬럼이 없으면 추가
                try {
                    database.execSQL("ALTER TABLE LocalUserData_table ADD COLUMN is_premium INTEGER NOT NULL DEFAULT 0")
                } catch (e: Exception) {
                    // 이미 존재하면 무시
                }
            }
        }

        // 23 -> 26 마이그레이션 (is_premium 확인 및 추가)
        val MIGRATION_23_26 = object : Migration(23, 26) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // is_premium 컬럼이 없으면 추가
                try {
                    database.execSQL("ALTER TABLE LocalUserData_table ADD COLUMN is_premium INTEGER NOT NULL DEFAULT 0")
                } catch (e: Exception) {
                    // 이미 존재하면 무시
                }
            }
        }

        // 24 -> 26 마이그레이션 (is_premium 확인 및 추가)
        val MIGRATION_24_26 = object : Migration(24, 26) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // is_premium 컬럼이 없으면 추가
                try {
                    database.execSQL("ALTER TABLE LocalUserData_table ADD COLUMN is_premium INTEGER NOT NULL DEFAULT 0")
                } catch (e: Exception) {
                    // 이미 존재하면 무시
                }
            }
        }

        // 25 -> 26 마이그레이션 (is_premium 확인 및 추가)
        val MIGRATION_25_26 = object : Migration(25, 26) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // is_premium 컬럼이 없으면 추가
                try {
                    database.execSQL("ALTER TABLE LocalUserData_table ADD COLUMN is_premium INTEGER NOT NULL DEFAULT 0")
                } catch (e: Exception) {
                    // 이미 존재하면 무시
                }
            }
        }

        // 26 -> 27 마이그레이션: ExchangeRate 테이블 완전 교체
        val MIGRATION_26_27 = object : Migration(26, 27) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // 기존 테이블 삭제 (환율 데이터는 실시간이라 기존 데이터 필요 없음)
                database.execSQL("DROP TABLE IF EXISTS exchangeRate_table")

                // 새로운 구조로 테이블 생성
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS `exchangeRate_table` (
                        `id` TEXT NOT NULL,
                        `createAt` TEXT NOT NULL DEFAULT 'N/A',
                        `rates` TEXT NOT NULL DEFAULT '{}',
                        PRIMARY KEY(`id`)
                    )
                """.trimIndent())
            }
        }

        // ✅ 27 -> 28 마이그레이션: CurrencyRecord 테이블 추가 (12개 통화 지원)
        val MIGRATION_27_28 = object : Migration(27, 28) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // 새로운 통합 테이블 생성 (EUR, GBP 등 새로운 통화용)
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS `currency_records` (
                        `id` TEXT NOT NULL,
                        `currency_code` TEXT NOT NULL,
                        `date` TEXT,
                        `sell_date` TEXT,
                        `money` TEXT,
                        `rate` TEXT,
                        `buy_rate` TEXT,
                        `sell_rate` TEXT,
                        `profit` TEXT,
                        `sell_profit` TEXT,
                        `expect_profit` TEXT,
                        `exchange_money` TEXT,
                        `record_color` INTEGER,
                        `category_name` TEXT,
                        `memo` TEXT,
                        PRIMARY KEY(`id`)
                    )
                """.trimIndent())

                // 인덱스 생성 (성능 최적화)
                database.execSQL("""
                    CREATE INDEX IF NOT EXISTS `index_currency_records_currency_code` 
                    ON `currency_records` (`currency_code`)
                """.trimIndent())

                database.execSQL("""
                    CREATE INDEX IF NOT EXISTS `index_currency_records_date` 
                    ON `currency_records` (`date`)
                """.trimIndent())

                database.execSQL("""
                    CREATE INDEX IF NOT EXISTS `index_currency_records_category_name` 
                    ON `currency_records` (`category_name`)
                """.trimIndent())

                // 기존 USD, JPY 데이터는 기존 테이블에 유지
                // 새로운 통화(EUR, GBP 등)만 새 테이블에 저장
                // 향후 필요시 데이터 마이그레이션 가능
            }
        }
    }
}

/**
 * Room TypeConverters - UUID 변환용
 */
class Converters {
    @androidx.room.TypeConverter
    fun fromUUID(uuid: java.util.UUID?): String? {
        return uuid?.toString()
    }

    @androidx.room.TypeConverter
    fun toUUID(uuidString: String?): java.util.UUID? {
        return uuidString?.let {
            try {
                java.util.UUID.fromString(it)
            } catch (e: IllegalArgumentException) {
                null
            }
        }
    }
}