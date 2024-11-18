package com.bobodroid.myapplication.models.datamodels.websocket
import com.bobodroid.myapplication.models.datamodels.repository.UserRepository
import com.bobodroid.myapplication.models.datamodels.websocket.WebSocketClient
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object WebSocketModule {

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
