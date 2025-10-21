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

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): InvestDatabase {
        return Room.databaseBuilder(
            context.applicationContext,
            InvestDatabase::class.java,
            "Invests_database"
        )
            .fallbackToDestructiveMigration()  // ✅ 버전 불일치 시 DB 초기화
            .addCallback(object : RoomDatabase.Callback() {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    super.onCreate(db)
                    Log.d("RoomCallback", "✅ 새 데이터베이스 생성됨")
                }

                override fun onOpen(db: SupportSQLiteDatabase) {
                    super.onOpen(db)
                    Log.d("RoomCallback", "✅ 데이터베이스 열림")
                }
            })
            .build()
    }


    @Provides
    @Singleton
    fun provideLocalUserDao(database: InvestDatabase): LocalUserDatabaseDao {
        return database.localUserDao()
    }

    @Provides
    @Singleton
    fun provideExchangeRateDao(database: InvestDatabase): ExchangeRateDataBaseDao {
        return database.exchangeRateDao()
    }

    @Provides
    @Singleton
    fun provideCurrencyRecordDao(database: InvestDatabase): CurrencyRecordDao {
        return database.currencyRecordDao()
    }
}