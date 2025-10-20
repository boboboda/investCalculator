package com.bobodroid.myapplication.di

import android.content.Context
import android.util.Log
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.bobodroid.myapplication.models.datamodels.roomDb.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object DatabaseModule {

    val nullIdCheckCallback = object : RoomDatabase.Callback() {
        override fun onOpen(db: SupportSQLiteDatabase) {
            super.onOpen(db)

            // 데이터베이스가 열릴 때마다 NULL ID 체크
            db.execSQL("""
            UPDATE buyDollar_table 
            SET id = lower(
                hex(randomblob(4)) || '-' || 
                hex(randomblob(2)) || '-4' || 
                substr(hex(randomblob(2)), 2) || '-' ||
                substr('89ab', abs(random()) % 4 + 1, 1) ||
                substr(hex(randomblob(2)), 2) || '-' ||
                hex(randomblob(6))
            )
            WHERE id IS NULL OR id = '' OR length(trim(id)) = 0
        """)

            db.execSQL("""
            UPDATE buyYen_table 
            SET id = lower(
                hex(randomblob(4)) || '-' || 
                hex(randomblob(2)) || '-4' || 
                substr(hex(randomblob(2)), 2) || '-' ||
                substr('89ab', abs(random()) % 4 + 1, 1) ||
                substr(hex(randomblob(2)), 2) || '-' ||
                hex(randomblob(6))
            )
            WHERE id IS NULL OR id = '' OR length(trim(id)) = 0
        """)

            Log.d("RoomCallback", "NULL ID check completed")
        }
    }

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): InvestDatabase {
        return Room.databaseBuilder(
            context.applicationContext,
            InvestDatabase::class.java,
            "Invests_database"
        )
            .addMigrations(
                InvestDatabase.MIGRATION_21_22,
                InvestDatabase.MIGRATION_22_26,
                InvestDatabase.MIGRATION_23_26,
                InvestDatabase.MIGRATION_24_26,
                InvestDatabase.MIGRATION_25_26,
                InvestDatabase.MIGRATION_26_27,
                InvestDatabase.MIGRATION_27_28  // ✅ CurrencyRecord 테이블 추가 마이그레이션
            )
//            .fallbackToDestructiveMigrationOnDowngrade() // 검증만 우회
//            .allowMainThreadQueries()
            .addCallback(object : RoomDatabase.Callback() {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    super.onCreate(db)
                    fixAllNullIds(db, "onCreate")
                }

                override fun onOpen(db: SupportSQLiteDatabase) {
                    super.onOpen(db)
                    fixAllNullIds(db, "onOpen")
                }

                private fun fixAllNullIds(db: SupportSQLiteDatabase, phase: String) {
                    Log.d("RoomCallback", "━━━━━━━━━━━━━━━━━━━━━━━━━━")
                    Log.d("RoomCallback", "🚨 NULL ID 긴급 수정 시작 ($phase)")

                    db.beginTransaction()
                    try {
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
                                val cursor = db.query("SELECT name FROM sqlite_master WHERE type='table' AND name='$tableName'")
                                if (cursor.count == 0) {
                                    cursor.close()
                                    continue
                                }
                                cursor.close()

                                // NULL ID 개수 확인
                                val nullCheckCursor = db.query(
                                    "SELECT COUNT(*) FROM $tableName WHERE id IS NULL OR id = '' OR length(trim(id)) = 0"
                                )
                                var nullCount = 0
                                if (nullCheckCursor.moveToFirst()) {
                                    nullCount = nullCheckCursor.getInt(0)
                                }
                                nullCheckCursor.close()

                                if (nullCount > 0) {
                                    Log.e("RoomCallback", "❌ $tableName: $nullCount 개의 NULL ID 발견!")

                                    // UUID 생성 함수로 모든 NULL ID 수정
                                    val updateQuery = """
                                    UPDATE $tableName 
                                    SET id = (
                                        SELECT lower(
                                            hex(randomblob(4)) || '-' || 
                                            hex(randomblob(2)) || '-4' || 
                                            substr(hex(randomblob(2)), 2) || '-' ||
                                            substr('89ab', abs(random()) % 4 + 1, 1) ||
                                            substr(hex(randomblob(2)), 2) || '-' ||
                                            hex(randomblob(6))
                                        )
                                    )
                                    WHERE id IS NULL 
                                       OR id = '' 
                                       OR length(trim(id)) = 0
                                """
                                    db.execSQL(updateQuery)

                                    // 수정 후 재확인
                                    val reCheckCursor = db.query(
                                        "SELECT COUNT(*) FROM $tableName WHERE id IS NULL OR id = ''"
                                    )
                                    if (reCheckCursor.moveToFirst()) {
                                        val remainingNull = reCheckCursor.getInt(0)
                                        if (remainingNull == 0) {
                                            Log.d("RoomCallback", "✅ $tableName: NULL ID 모두 수정 완료!")
                                        } else {
                                            Log.e("RoomCallback", "⚠️ $tableName: 여전히 $remainingNull 개 NULL 존재")
                                        }
                                    }
                                    reCheckCursor.close()
                                } else {
                                    Log.d("RoomCallback", "✅ $tableName: NULL ID 없음 (정상)")
                                }
                            } catch (e: Exception) {
                                Log.e("RoomCallback", "$tableName 처리 중 오류: ${e.message}", e)
                            }
                        }

                        db.setTransactionSuccessful()
                        Log.d("RoomCallback", "✅ NULL ID 수정 완료 ($phase)")
                        Log.d("RoomCallback", "━━━━━━━━━━━━━━━━━━━━━━━━━━")
                    } catch (e: Exception) {
                        Log.e("RoomCallback", "NULL ID 수정 실패", e)
                    } finally {
                        db.endTransaction()
                    }
                }
            })
            .build()
    }

    @Provides
    fun provideBuyDollarRecordDao(investDatabase: InvestDatabase): DollarBuyDatabaseDao {
        return investDatabase.DrBuyDao()
    }

    @Provides
    fun provideBuyYenRecordDao(investDatabase: InvestDatabase): YenBuyDatabaseDao {
        return investDatabase.YenBuyDao()
    }

    @Provides
    fun provideLocalUserDao(investDatabase: InvestDatabase): LocalUserDatabaseDao {
        return investDatabase.LocalUserDao()
    }

    @Provides
    fun provideExchagerateDao(investDatabase: InvestDatabase): ExchangeRateDataBaseDao {
        return investDatabase.exchangeRateDao()
    }

    // ✅ 새로운 CurrencyRecordDao Provider 추가
    @Provides
    fun provideCurrencyRecordDao(investDatabase: InvestDatabase): CurrencyRecordDao {
        return investDatabase.currencyRecordDao()
    }
}