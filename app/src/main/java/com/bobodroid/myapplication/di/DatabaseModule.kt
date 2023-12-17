package com.bobodroid.myapplication.di

import android.content.Context
import androidx.room.Room
import com.bobodroid.myapplication.models.datamodels.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@InstallIn(SingletonComponent:: class)
@Module
private object DatabaseModule {

    @Provides
    fun provideBuyDollarRecordDao(investDatabase: InvestDatabase) : DollarBuyDatabaseDao {
        return investDatabase.DrBuyDao()
    }

    @Provides
    fun provideSellDollarRecordDao(investDatabase: InvestDatabase) : DollarSellDatabaseDao {
        return investDatabase.DrSellDao()
    }

    @Provides
    fun provideBuyYenRecordDao(investDatabase: InvestDatabase) : YenBuyDatabaseDao {
        return investDatabase.YenBuyDao()
    }

    @Provides
    fun provideSellYenRecordDao(investDatabase: InvestDatabase) : YenSellDatabaseDao {
        return investDatabase.YenSellDao()
    }

    @Provides
    fun provideBuyWonRecordDao(investDatabase: InvestDatabase) : WonBuyDatabaseDao {
        return investDatabase.WonBuyDao()
    }

    @Provides
    fun provideSellWonRecordDao(investDatabase: InvestDatabase) : WonSellDatabaseDao {
        return investDatabase.WonSellDao()
    }

    @Provides
    fun provideLocalUserDao(investDatabase: InvestDatabase) : LocalUserDatabaseDao {
        return investDatabase.LocalUserDao()
    }


    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context) : InvestDatabase {
        return Room.databaseBuilder(
            context.applicationContext,
            InvestDatabase::class.java,
            "Invests_database"
        )
            .fallbackToDestructiveMigration()
            .build()
    }
}