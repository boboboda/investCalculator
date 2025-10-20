package com.bobodroid.myapplication.models.datamodels.roomDb

import androidx.room.Database
import androidx.room.DeleteTable
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import java.util.UUID

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
@TypeConverters(Converters::class)  // ✅ TypeConverter 활성화!
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


        // ✅ 27 -> 28 마이그레이션: NULL ID 수정 포함 전체 코드
        val MIGRATION_27_28 = object : Migration(27, 28) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.beginTransaction()
                try {
                    // ✅ 1단계: 모든 테이블의 NULL ID를 먼저 수정
                    fixAllNullIds(database)

                    // 2단계: 테이블 마이그레이션
                    migrateLocalUserDataTable(database)
                    migrateBuyDollarTable(database)
                    migrateBuyYenTable(database)
                    migrateSellDollarTable(database)
                    migrateSellYenTable(database)
                    migrateWonTables(database)

                    // 3단계: 새로운 currency_records 테이블 생성 (인덱스 없이!)
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

                    database.setTransactionSuccessful()
                } catch (e: Exception) {
                    android.util.Log.e("Migration", "Migration 27->28 failed: ${e.message}", e)
                    throw e
                } finally {
                    database.endTransaction()
                }
            }

            // ✅ 새로 추가: NULL ID를 UUID로 변환하는 메서드
            private fun fixAllNullIds(database: SupportSQLiteDatabase) {
                val tables = listOf(
                    "LocalUserData_table",
                    "buyDollar_table",
                    "buyYen_table",
                    "sellDollar_table",
                    "sellYen_table",
                    "buyWon_table",
                    "sellWon_table"
                )

                for (tableName in tables) {
                    try {
                        // 테이블 존재 확인
                        val cursor = database.query("SELECT name FROM sqlite_master WHERE type='table' AND name='$tableName'")
                        if (cursor.count == 0) {
                            cursor.close()
                            continue
                        }
                        cursor.close()

                        // NULL이나 빈 ID를 UUID로 업데이트
                        database.execSQL("""
                    UPDATE $tableName 
                    SET id = lower(
                        hex(randomblob(4)) || '-' || 
                        hex(randomblob(2)) || '-4' || 
                        substr(hex(randomblob(2)), 2) || '-' ||
                        substr('89ab', abs(random()) % 4 + 1, 1) ||
                        substr(hex(randomblob(2)), 2) || '-' ||
                        hex(randomblob(6))
                    )
                    WHERE id IS NULL 
                       OR id = '' 
                       OR length(trim(id)) = 0
                """)

                        android.util.Log.d("Migration", "✅ $tableName: NULL ID 수정 완료")
                    } catch (e: Exception) {
                        android.util.Log.e("Migration", "Error fixing NULL IDs in $tableName: ${e.message}")
                    }
                }
            }

            private fun migrateLocalUserDataTable(database: SupportSQLiteDatabase) {
                val cursor = database.query("SELECT name FROM sqlite_master WHERE type='table' AND name='LocalUserData_table'")
                if (cursor.count == 0) {
                    cursor.close()
                    return
                }
                cursor.close()

                database.execSQL("""
            CREATE TABLE IF NOT EXISTS `LocalUserData_table_temp` (
                `id` TEXT NOT NULL,
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

                // ✅ UUID 생성 함수 - NULL 처리 강화
                database.execSQL("""
            INSERT OR REPLACE INTO `LocalUserData_table_temp` 
            SELECT 
                CASE 
                    WHEN id IS NULL OR length(trim(CAST(id AS TEXT))) = 0 THEN 
                        lower(
                            hex(randomblob(4)) || '-' || 
                            hex(randomblob(2)) || '-4' || 
                            substr(hex(randomblob(2)), 2) || '-' ||
                            substr('89ab', abs(random()) % 4 + 1, 1) ||
                            substr(hex(randomblob(2)), 2) || '-' ||
                            hex(randomblob(6))
                        )
                    WHEN typeof(id) = 'blob' AND length(id) > 0 THEN hex(id)
                    WHEN typeof(id) = 'text' AND length(id) > 0 THEN id
                    ELSE lower(
                        hex(randomblob(4)) || '-' || 
                        hex(randomblob(2)) || '-4' || 
                        substr(hex(randomblob(2)), 2) || '-' ||
                        substr('89ab', abs(random()) % 4 + 1, 1) ||
                        substr(hex(randomblob(2)), 2) || '-' ||
                        hex(randomblob(6))
                    )
                END,
                social_id, social_type, email, nickname, profile_url,
                is_synced, fcm_Token, rate_Reset_Count, reFresh_CreateAt,
                rate_Ad_Count, reward_ad_Showing_date, user_Reset_Date,
                user_Show_Notice_Date, dr_Buy_Spread, dr_Sell_Spread,
                yen_Buy_Spread, yen_Sell_Spread, monthly_profit_goal,
                goal_set_month, is_premium
            FROM `LocalUserData_table`
        """.trimIndent())

                database.execSQL("DROP TABLE `LocalUserData_table`")
                database.execSQL("ALTER TABLE `LocalUserData_table_temp` RENAME TO `LocalUserData_table`")
            }

            private fun migrateBuyDollarTable(database: SupportSQLiteDatabase) {
                val cursor = database.query("SELECT name FROM sqlite_master WHERE type='table' AND name='buyDollar_table'")
                if (cursor.count == 0) {
                    cursor.close()
                    return
                }
                cursor.close()

                database.execSQL("""
            CREATE TABLE IF NOT EXISTS `buyDollar_table_temp` (
                `id` TEXT NOT NULL,
                `date` TEXT DEFAULT '',
                `sell_Date` TEXT DEFAULT '',
                `money` TEXT DEFAULT '',
                `rate` TEXT DEFAULT '',
                `buy_rate` TEXT DEFAULT '',
                `sell_rate` TEXT DEFAULT '',
                `profit` TEXT DEFAULT '',
                `sell_profit` TEXT DEFAULT '',
                `expect_profit` TEXT DEFAULT '',
                `exchangeMoney` TEXT DEFAULT '',
                `usingRecord` INTEGER,
                `buyDrMemo` TEXT DEFAULT '',
                `buyDrCategoryName` TEXT DEFAULT '',
                PRIMARY KEY(`id`)
            )
        """.trimIndent())

                // ✅ NULL 처리 강화
                database.execSQL("""
            INSERT OR REPLACE INTO `buyDollar_table_temp` 
            SELECT 
                CASE 
                    WHEN id IS NULL OR length(trim(CAST(id AS TEXT))) = 0 THEN 
                        lower(
                            hex(randomblob(4)) || '-' || 
                            hex(randomblob(2)) || '-4' || 
                            substr(hex(randomblob(2)), 2) || '-' ||
                            substr('89ab', abs(random()) % 4 + 1, 1) ||
                            substr(hex(randomblob(2)), 2) || '-' ||
                            hex(randomblob(6))
                        )
                    WHEN typeof(id) = 'blob' AND length(id) > 0 THEN hex(id)
                    WHEN typeof(id) = 'text' AND length(id) > 0 THEN id
                    ELSE lower(
                        hex(randomblob(4)) || '-' || 
                        hex(randomblob(2)) || '-4' || 
                        substr(hex(randomblob(2)), 2) || '-' ||
                        substr('89ab', abs(random()) % 4 + 1, 1) ||
                        substr(hex(randomblob(2)), 2) || '-' ||
                        hex(randomblob(6))
                    )
                END,
                date, sell_Date, money, rate, buy_rate, sell_rate, 
                profit, sell_profit, expect_profit, exchangeMoney, 
                usingRecord, buyDrMemo, buyDrCategoryName
            FROM `buyDollar_table`
        """.trimIndent())

                database.execSQL("DROP TABLE `buyDollar_table`")
                database.execSQL("ALTER TABLE `buyDollar_table_temp` RENAME TO `buyDollar_table`")
            }

            private fun migrateBuyYenTable(database: SupportSQLiteDatabase) {
                val cursor = database.query("SELECT name FROM sqlite_master WHERE type='table' AND name='buyYen_table'")
                if (cursor.count == 0) {
                    cursor.close()
                    return
                }
                cursor.close()

                database.execSQL("""
            CREATE TABLE IF NOT EXISTS `buyYen_table_temp` (
                `id` TEXT NOT NULL,
                `date` TEXT DEFAULT '',
                `sell_Date` TEXT DEFAULT '',
                `money` TEXT DEFAULT '',
                `rate` TEXT DEFAULT '',
                `buy_rate` TEXT DEFAULT '',
                `sell_rate` TEXT DEFAULT '',
                `profit` TEXT DEFAULT '',
                `sell_profit` TEXT DEFAULT '',
                `expect_profit` TEXT DEFAULT '',
                `exchangeMoney` TEXT DEFAULT '',
                `usingRecord` INTEGER,
                `buyYenMemo` TEXT DEFAULT '',
                `buyYenCategoryName` TEXT DEFAULT '',
                PRIMARY KEY(`id`)
            )
        """.trimIndent())

                // ✅ NULL 처리 강화
                database.execSQL("""
            INSERT OR REPLACE INTO `buyYen_table_temp` 
            SELECT 
                CASE 
                    WHEN id IS NULL OR length(trim(CAST(id AS TEXT))) = 0 THEN 
                        lower(
                            hex(randomblob(4)) || '-' || 
                            hex(randomblob(2)) || '-4' || 
                            substr(hex(randomblob(2)), 2) || '-' ||
                            substr('89ab', abs(random()) % 4 + 1, 1) ||
                            substr(hex(randomblob(2)), 2) || '-' ||
                            hex(randomblob(6))
                        )
                    WHEN typeof(id) = 'blob' AND length(id) > 0 THEN hex(id)
                    WHEN typeof(id) = 'text' AND length(id) > 0 THEN id
                    ELSE lower(
                        hex(randomblob(4)) || '-' || 
                        hex(randomblob(2)) || '-4' || 
                        substr(hex(randomblob(2)), 2) || '-' ||
                        substr('89ab', abs(random()) % 4 + 1, 1) ||
                        substr(hex(randomblob(2)), 2) || '-' ||
                        hex(randomblob(6))
                    )
                END,
                date, sell_Date, money, rate, buy_rate, sell_rate, 
                profit, sell_profit, expect_profit, exchangeMoney, 
                usingRecord, buyYenMemo, buyYenCategoryName
            FROM `buyYen_table`
        """.trimIndent())

                database.execSQL("DROP TABLE `buyYen_table`")
                database.execSQL("ALTER TABLE `buyYen_table_temp` RENAME TO `buyYen_table`")
            }

            private fun migrateSellDollarTable(database: SupportSQLiteDatabase) {
                val cursor = database.query("SELECT name FROM sqlite_master WHERE type='table' AND name='sellDollar_table'")
                if (cursor.count == 0) {
                    cursor.close()
                    return
                }
                cursor.close()

                database.execSQL("""
            CREATE TABLE IF NOT EXISTS `sellDollar_table_temp` (
                `id` TEXT NOT NULL,
                `date` TEXT DEFAULT '',
                `money` TEXT DEFAULT '',
                `rate` TEXT DEFAULT '',
                `exchangeMoney` TEXT DEFAULT '',
                `sellDrMemo` TEXT DEFAULT '',
                `sellDrCategoryName` TEXT DEFAULT '',
                PRIMARY KEY(`id`)
            )
        """.trimIndent())

                // ✅ NULL 처리 강화
                database.execSQL("""
            INSERT OR REPLACE INTO `sellDollar_table_temp` 
            SELECT 
                CASE 
                    WHEN id IS NULL OR length(trim(CAST(id AS TEXT))) = 0 THEN 
                        lower(
                            hex(randomblob(4)) || '-' || 
                            hex(randomblob(2)) || '-4' || 
                            substr(hex(randomblob(2)), 2) || '-' ||
                            substr('89ab', abs(random()) % 4 + 1, 1) ||
                            substr(hex(randomblob(2)), 2) || '-' ||
                            hex(randomblob(6))
                        )
                    WHEN typeof(id) = 'blob' AND length(id) > 0 THEN hex(id)
                    WHEN typeof(id) = 'text' AND length(id) > 0 THEN id
                    ELSE lower(
                        hex(randomblob(4)) || '-' || 
                        hex(randomblob(2)) || '-4' || 
                        substr(hex(randomblob(2)), 2) || '-' ||
                        substr('89ab', abs(random()) % 4 + 1, 1) ||
                        substr(hex(randomblob(2)), 2) || '-' ||
                        hex(randomblob(6))
                    )
                END,
                date, money, rate, exchangeMoney, 
                sellDrMemo, sellDrCategoryName
            FROM `sellDollar_table`
        """.trimIndent())

                database.execSQL("DROP TABLE `sellDollar_table`")
                database.execSQL("ALTER TABLE `sellDollar_table_temp` RENAME TO `sellDollar_table`")
            }

            private fun migrateSellYenTable(database: SupportSQLiteDatabase) {
                val cursor = database.query("SELECT name FROM sqlite_master WHERE type='table' AND name='sellYen_table'")
                if (cursor.count == 0) {
                    cursor.close()
                    return
                }
                cursor.close()

                database.execSQL("""
            CREATE TABLE IF NOT EXISTS `sellYen_table_temp` (
                `id` TEXT NOT NULL,
                `date` TEXT DEFAULT '',
                `money` TEXT DEFAULT '',
                `rate` TEXT DEFAULT '',
                `exchangeMoney` TEXT DEFAULT '',
                `sellYenMemo` TEXT DEFAULT '',
                `sellYenCategoryName` TEXT DEFAULT '',
                PRIMARY KEY(`id`)
            )
        """.trimIndent())

                // ✅ NULL 처리 강화
                database.execSQL("""
            INSERT OR REPLACE INTO `sellYen_table_temp` 
            SELECT 
                CASE 
                    WHEN id IS NULL OR length(trim(CAST(id AS TEXT))) = 0 THEN 
                        lower(
                            hex(randomblob(4)) || '-' || 
                            hex(randomblob(2)) || '-4' || 
                            substr(hex(randomblob(2)), 2) || '-' ||
                            substr('89ab', abs(random()) % 4 + 1, 1) ||
                            substr(hex(randomblob(2)), 2) || '-' ||
                            hex(randomblob(6))
                        )
                    WHEN typeof(id) = 'blob' AND length(id) > 0 THEN hex(id)
                    WHEN typeof(id) = 'text' AND length(id) > 0 THEN id
                    ELSE lower(
                        hex(randomblob(4)) || '-' || 
                        hex(randomblob(2)) || '-4' || 
                        substr(hex(randomblob(2)), 2) || '-' ||
                        substr('89ab', abs(random()) % 4 + 1, 1) ||
                        substr(hex(randomblob(2)), 2) || '-' ||
                        hex(randomblob(6))
                    )
                END,
                date, money, rate, exchangeMoney, 
                sellYenMemo, sellYenCategoryName
            FROM `sellYen_table`
        """.trimIndent())

                database.execSQL("DROP TABLE `sellYen_table`")
                database.execSQL("ALTER TABLE `sellYen_table_temp` RENAME TO `sellYen_table`")
            }

            private fun migrateWonTables(database: SupportSQLiteDatabase) {
                // buyWon_table 처리
                val buyWonCursor = database.query("SELECT name FROM sqlite_master WHERE type='table' AND name='buyWon_table'")
                if (buyWonCursor.count > 0) {
                    buyWonCursor.close()

                    database.execSQL("""
                CREATE TABLE IF NOT EXISTS `buyWon_table_temp` (
                    `id` TEXT NOT NULL,
                    `date` TEXT DEFAULT '',
                    `sell_Date` TEXT DEFAULT '',
                    `money` TEXT DEFAULT '',
                    `rate` TEXT DEFAULT '',
                    `buy_rate` TEXT DEFAULT '',
                    `sell_rate` TEXT DEFAULT '',
                    `profit` TEXT DEFAULT '',
                    `sell_profit` TEXT DEFAULT '',
                    `expect_profit` TEXT DEFAULT '',
                    `exchangeMoney` TEXT DEFAULT '',
                    `usingRecord` INTEGER,
                    `moneyType` INTEGER,
                    `buyWonMemo` TEXT DEFAULT '',
                    `buyWonCategoryName` TEXT DEFAULT '',
                    PRIMARY KEY(`id`)
                )
            """.trimIndent())

                    try {
                        // ✅ NULL 처리 강화
                        database.execSQL("""
                    INSERT OR REPLACE INTO `buyWon_table_temp` 
                    SELECT 
                        CASE 
                            WHEN id IS NULL OR length(trim(CAST(id AS TEXT))) = 0 THEN 
                                lower(
                                    hex(randomblob(4)) || '-' || 
                                    hex(randomblob(2)) || '-4' || 
                                    substr(hex(randomblob(2)), 2) || '-' ||
                                    substr('89ab', abs(random()) % 4 + 1, 1) ||
                                    substr(hex(randomblob(2)), 2) || '-' ||
                                    hex(randomblob(6))
                                )
                            WHEN typeof(id) = 'blob' AND length(id) > 0 THEN hex(id)
                            WHEN typeof(id) = 'text' AND length(id) > 0 THEN id
                            ELSE lower(
                                hex(randomblob(4)) || '-' || 
                                hex(randomblob(2)) || '-4' || 
                                substr(hex(randomblob(2)), 2) || '-' ||
                                substr('89ab', abs(random()) % 4 + 1, 1) ||
                                substr(hex(randomblob(2)), 2) || '-' ||
                                hex(randomblob(6))
                            )
                        END,
                        date, sell_Date, money, rate, buy_rate, sell_rate, 
                        profit, sell_profit, expect_profit, exchangeMoney, 
                        usingRecord, moneyType, buyWonMemo, buyWonCategoryName
                    FROM `buyWon_table`
                """.trimIndent())
                    } catch (e: Exception) {
                        android.util.Log.w("Migration", "Error migrating buyWon_table data: ${e.message}")
                    }

                    database.execSQL("DROP TABLE IF EXISTS `buyWon_table`")
                    database.execSQL("ALTER TABLE `buyWon_table_temp` RENAME TO `buyWon_table`")
                } else {
                    buyWonCursor.close()
                    // 테이블이 없으면 새로 생성 (DeleteTable에 있지만 Room이 체크함)
                    database.execSQL("""
                CREATE TABLE IF NOT EXISTS `buyWon_table` (
                    `id` TEXT NOT NULL,
                    `date` TEXT DEFAULT '',
                    `sell_Date` TEXT DEFAULT '',
                    `money` TEXT DEFAULT '',
                    `rate` TEXT DEFAULT '',
                    `buy_rate` TEXT DEFAULT '',
                    `sell_rate` TEXT DEFAULT '',
                    `profit` TEXT DEFAULT '',
                    `sell_profit` TEXT DEFAULT '',
                    `expect_profit` TEXT DEFAULT '',
                    `exchangeMoney` TEXT DEFAULT '',
                    `usingRecord` INTEGER,
                    `moneyType` INTEGER,
                    `buyWonMemo` TEXT DEFAULT '',
                    `buyWonCategoryName` TEXT DEFAULT '',
                    PRIMARY KEY(`id`)
                )
            """.trimIndent())
                }

                // sellWon_table 처리
                val sellWonCursor = database.query("SELECT name FROM sqlite_master WHERE type='table' AND name='sellWon_table'")
                if (sellWonCursor.count > 0) {
                    sellWonCursor.close()

                    database.execSQL("""
                CREATE TABLE IF NOT EXISTS `sellWon_table_temp` (
                    `id` TEXT NOT NULL,
                    `date` TEXT DEFAULT '',
                    `money` TEXT DEFAULT '',
                    `rate` TEXT DEFAULT '',
                    `exchangeMoney` TEXT DEFAULT '',
                    `moneyType` INTEGER,
                    `sellWonMemo` TEXT DEFAULT '',
                    `sellWonCategoryName` TEXT DEFAULT '',
                    PRIMARY KEY(`id`)
                )
            """.trimIndent())

                    try {
                        // ✅ NULL 처리 강화
                        database.execSQL("""
                    INSERT OR REPLACE INTO `sellWon_table_temp` 
                    SELECT 
                        CASE 
                            WHEN id IS NULL OR length(trim(CAST(id AS TEXT))) = 0 THEN 
                                lower(
                                    hex(randomblob(4)) || '-' || 
                                    hex(randomblob(2)) || '-4' || 
                                    substr(hex(randomblob(2)), 2) || '-' ||
                                    substr('89ab', abs(random()) % 4 + 1, 1) ||
                                    substr(hex(randomblob(2)), 2) || '-' ||
                                    hex(randomblob(6))
                                )
                            WHEN typeof(id) = 'blob' AND length(id) > 0 THEN hex(id)
                            WHEN typeof(id) = 'text' AND length(id) > 0 THEN id
                            ELSE lower(
                                hex(randomblob(4)) || '-' || 
                                hex(randomblob(2)) || '-4' || 
                                substr(hex(randomblob(2)), 2) || '-' ||
                                substr('89ab', abs(random()) % 4 + 1, 1) ||
                                substr(hex(randomblob(2)), 2) || '-' ||
                                hex(randomblob(6))
                            )
                        END,
                        date, money, rate, exchangeMoney, 
                        moneyType, sellWonMemo, sellWonCategoryName
                    FROM `sellWon_table`
                """.trimIndent())
                    } catch (e: Exception) {
                        android.util.Log.w("Migration", "Error migrating sellWon_table data: ${e.message}")
                    }

                    database.execSQL("DROP TABLE IF EXISTS `sellWon_table`")
                    database.execSQL("ALTER TABLE `sellWon_table_temp` RENAME TO `sellWon_table`")
                } else {
                    sellWonCursor.close()
                    // 테이블이 없으면 새로 생성
                    database.execSQL("""
                CREATE TABLE IF NOT EXISTS `sellWon_table` (
                    `id` TEXT NOT NULL,
                    `date` TEXT DEFAULT '',
                    `money` TEXT DEFAULT '',
                    `rate` TEXT DEFAULT '',
                    `exchangeMoney` TEXT DEFAULT '',
                    `moneyType` INTEGER,
                    `sellWonMemo` TEXT DEFAULT '',
                    `sellWonCategoryName` TEXT DEFAULT '',
                    PRIMARY KEY(`id`)
                )
            """.trimIndent())
                }
            }
        }

   }
}

/**
 * Room TypeConverters - UUID를 TEXT로 변환
 */
class Converters {
    @TypeConverter
    fun fromUUID(uuid: UUID?): String? {
        return uuid?.toString()
    }

    @TypeConverter
    fun toUUID(uuidString: String?): UUID? {
        if (uuidString == null) return null

        return try {
            // 1. 정상적인 UUID 문자열 (8-4-4-4-12 형식)
            if (uuidString.contains("-")) {
                UUID.fromString(uuidString)
            }
            // 2. hex 문자열 (32자리)
            else if (uuidString.length == 32) {
                val formatted = StringBuilder(uuidString)
                    .insert(8, "-")
                    .insert(13, "-")
                    .insert(18, "-")
                    .insert(23, "-")
                    .toString()
                UUID.fromString(formatted)
            }
            // 3. 대문자 hex 문자열
            else if (uuidString.length > 30) {
                val lower = uuidString.toLowerCase()
                if (lower.length == 32) {
                    val formatted = StringBuilder(lower)
                        .insert(8, "-")
                        .insert(13, "-")
                        .insert(18, "-")
                        .insert(23, "-")
                        .toString()
                    UUID.fromString(formatted)
                } else {
                    // 기본 UUID 생성
                    UUID.randomUUID()
                }
            }
            // 4. 빈 문자열이나 이상한 값
            else {
                UUID.randomUUID()
            }
        } catch (e: Exception) {
            // 변환 실패 시 새 UUID 생성
            android.util.Log.e("Converters", "UUID 변환 실패: $uuidString", e)
            UUID.randomUUID()
        }
    }
}