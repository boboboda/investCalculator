package com.bobodroid.myapplication.di

import com.bobodroid.myapplication.models.datamodels.repository.*
import com.bobodroid.myapplication.models.datamodels.roomDb.*
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
        currencyRecordDao: CurrencyRecordDao  // 새로운 DAO 추가
    ): InvestRepository {
        return InvestRepository(currencyRecordDao)
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