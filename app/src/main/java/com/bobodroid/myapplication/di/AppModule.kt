package com.bobodroid.myapplication.di

import com.bobodroid.myapplication.models.datamodels.repository.DollarRepository
import com.bobodroid.myapplication.models.datamodels.repository.ExchangeRateRepository
import com.bobodroid.myapplication.models.datamodels.repository.InvestRepository
import com.bobodroid.myapplication.models.datamodels.repository.LatestRateRepository
import com.bobodroid.myapplication.models.datamodels.repository.UserRepository
import com.bobodroid.myapplication.models.datamodels.repository.YenRepository
import com.bobodroid.myapplication.models.datamodels.roomDb.DollarBuyDatabaseDao
import com.bobodroid.myapplication.models.datamodels.roomDb.ExchangeRateDataBaseDao
import com.bobodroid.myapplication.models.datamodels.roomDb.LocalUserDatabaseDao
import com.bobodroid.myapplication.models.datamodels.roomDb.YenBuyDatabaseDao
import com.bobodroid.myapplication.models.datamodels.websocket.WebSocketClient
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
    ): InvestRepository {
        return InvestRepository(dollarRepository, yenRepository)
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

    @Singleton
    @Provides
    fun provideLatestRateRepository(webSocketClient: WebSocketClient): LatestRateRepository {
        return LatestRateRepository(webSocketClient)
    }

    @Provides
    @Singleton
    fun provideWebSocketClient(
        userRepository: UserRepository
    ): WebSocketClient {
        return WebSocketClient(
            userRepository
        )
    }

}