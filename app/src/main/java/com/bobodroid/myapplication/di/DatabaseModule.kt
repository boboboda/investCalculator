package com.bobodroid.myapplication.di

import android.content.Context
import androidx.room.Room
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
            .addMigrations(
                InvestDatabase.MIGRATION_21_22,
                InvestDatabase.MIGRATION_22_26,
                InvestDatabase.MIGRATION_23_26,
                InvestDatabase.MIGRATION_24_26,
                InvestDatabase.MIGRATION_25_26,
                InvestDatabase.MIGRATION_26_27,
                InvestDatabase.MIGRATION_27_28  // ✅ CurrencyRecord 테이블 추가 마이그레이션
            )
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