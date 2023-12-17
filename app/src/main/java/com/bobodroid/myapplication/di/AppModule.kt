package com.bobodroid.myapplication.di

import com.bobodroid.myapplication.models.datamodels.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Singleton
    @Provides
    fun provideRecordRepository(
        dollarBuyDatabaseDao: DollarBuyDatabaseDao,
        dollarSellDatabaseDao: DollarSellDatabaseDao,
        yenBuyDatabaseDao: YenBuyDatabaseDao,
        yenSellDatabaseDao: YenSellDatabaseDao,
        wonBuyDatabaseDao: WonBuyDatabaseDao,
        wonSellDatabaseDao: WonSellDatabaseDao,
        localUserDatabaseDao: LocalUserDatabaseDao): InvestRepository {
        return InvestRepository(
            dollarBuyDatabaseDao,
            dollarSellDatabaseDao,
            yenBuyDatabaseDao,
            yenSellDatabaseDao,
            wonBuyDatabaseDao,
            wonSellDatabaseDao,
            localUserDatabaseDao)
    }
}