package com.bobodroid.myapplication.di

import com.bobodroid.myapplication.models.datamodels.repository.DollarRepository
import com.bobodroid.myapplication.models.datamodels.repository.ExchangeRateRepository
import com.bobodroid.myapplication.models.datamodels.repository.InvestRepository
import com.bobodroid.myapplication.models.datamodels.repository.UserRepository
import com.bobodroid.myapplication.models.datamodels.repository.WonRepository
import com.bobodroid.myapplication.models.datamodels.repository.YenRepository
import com.bobodroid.myapplication.models.datamodels.roomDb.DollarBuyDatabaseDao
import com.bobodroid.myapplication.models.datamodels.roomDb.DollarSellDatabaseDao
import com.bobodroid.myapplication.models.datamodels.roomDb.ExchangeRateDataBaseDao
import com.bobodroid.myapplication.models.datamodels.roomDb.LocalUserDatabaseDao
import com.bobodroid.myapplication.models.datamodels.roomDb.WonBuyDatabaseDao
import com.bobodroid.myapplication.models.datamodels.roomDb.WonSellDatabaseDao
import com.bobodroid.myapplication.models.datamodels.roomDb.YenBuyDatabaseDao
import com.bobodroid.myapplication.models.datamodels.roomDb.YenSellDatabaseDao
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
    fun provideInvestRepository(
        dollarRepository: DollarRepository,
        yenRepository: YenRepository,
        wonRepository: WonRepository
    ): InvestRepository {
        return InvestRepository(dollarRepository, yenRepository, wonRepository)
    }

    @Singleton
    @Provides
    fun provideUserRepository(localUserDatabaseDao: LocalUserDatabaseDao): UserRepository {
        return UserRepository(localUserDatabaseDao)
    }

    // ExchangeRateRepository
    @Singleton
    @Provides
    fun provideExchangeRateRepository(exchangeRateDataBaseDao: ExchangeRateDataBaseDao): ExchangeRateRepository {
        return ExchangeRateRepository(exchangeRateDataBaseDao)
    }

}