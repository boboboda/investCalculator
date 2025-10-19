package com.bobodroid.myapplication.models.datamodels.roomDb

import androidx.room.Database
import androidx.room.DeleteTable
import androidx.room.RoomDatabase
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
    ExchangeRate::class],
    version = 26,
    exportSchema = false)

@DeleteTable.Entries(
    DeleteTable(tableName = "sellWon_table"),
    DeleteTable(tableName = "buyWon_table"),
    DeleteTable(tableName = "sellYen_table"),
    DeleteTable(tableName = "sellDollar_table")
)

abstract class InvestDatabase: RoomDatabase() {
    abstract fun DrBuyDao() : DollarBuyDatabaseDao
    abstract fun YenBuyDao() : YenBuyDatabaseDao
    abstract fun LocalUserDao() : LocalUserDatabaseDao
    abstract fun exchangeRateDao() : ExchangeRateDataBaseDao

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

                // 2. 기존 데이터 복사 (customId, pin 제외)
                database.execSQL("""
                    INSERT INTO `LocalUserData_table_new` 
                    (`id`, `social_id`, `social_type`, `email`, `nickname`, `profile_url`, 
                     `is_synced`, `fcm_Token`, `rate_Reset_Count`, `reFresh_CreateAt`, 
                     `rate_Ad_Count`, `reward_ad_Showing_date`, `user_Reset_Date`, 
                     `user_Show_Notice_Date`, `dr_Buy_Spread`, `dr_Sell_Spread`, 
                     `yen_Buy_Spread`, `yen_Sell_Spread`, `monthly_profit_goal`, 
                     `goal_set_month`, `is_premium`)
                    SELECT 
                        `id`, '' AS social_id, 'NONE' AS social_type, '' AS email, 
                        '' AS nickname, '' AS profile_url, 0 AS is_synced, `fcm_Token`, 
                        `rate_Reset_Count`, `reFresh_CreateAt`, `rate_Ad_Count`, 
                        `reward_ad_Showing_date`, `user_Reset_Date`, `user_Show_Notice_Date`, 
                        `dr_Buy_Spread`, `dr_Sell_Spread`, `yen_Buy_Spread`, `yen_Sell_Spread`, 
                        `monthly_profit_goal`, `goal_set_month`, 0 AS is_premium
                    FROM `LocalUserData_table`
                """.trimIndent())

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
    }
}