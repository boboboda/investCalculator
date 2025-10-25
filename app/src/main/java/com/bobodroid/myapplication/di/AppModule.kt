package com.bobodroid.myapplication.di

import com.bobodroid.myapplication.domain.repository.IUserRepository
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
    fun provideLatestRateRepository(webSocketClient: WebSocketClient): LatestRateRepository {
        return LatestRateRepository(webSocketClient)
    }

    @Provides
    @Singleton
    fun provideWebSocketClient(
        userRepository: IUserRepository
    ): WebSocketClient {
        return WebSocketClient(
            userRepository
        )
    }

}