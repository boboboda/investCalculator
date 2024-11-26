package com.bobodroid.myapplication.di

import com.bobodroid.myapplication.models.datamodels.repository.UserRepository
import com.bobodroid.myapplication.models.datamodels.useCases.CustomIdCreateUser
import com.bobodroid.myapplication.models.datamodels.useCases.DeleteUserUseCase
import com.bobodroid.myapplication.models.datamodels.useCases.LocalExistCheckUseCase
import com.bobodroid.myapplication.models.datamodels.useCases.LocalIdAddUseCase
import com.bobodroid.myapplication.models.datamodels.useCases.LocalUserUpdate
import com.bobodroid.myapplication.models.datamodels.useCases.LogInUseCase
import com.bobodroid.myapplication.models.datamodels.useCases.LogoutUseCase
import com.bobodroid.myapplication.models.datamodels.useCases.TargetRateAddUseCase
import com.bobodroid.myapplication.models.datamodels.useCases.TargetRateDeleteUseCase
import com.bobodroid.myapplication.models.datamodels.useCases.TargetRateUpdateUseCase
import com.bobodroid.myapplication.models.datamodels.useCases.TargetRateUseCases
import com.bobodroid.myapplication.models.datamodels.useCases.UserUseCases
import com.bobodroid.myapplication.models.datamodels.websocket.WebSocketClient
import com.bobodroid.myapplication.util.AdMob.AdUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object UseCaseModule {

    @Singleton  // LocalExistCheckUseCase를 싱글톤으로 선언
    @Provides
    fun provideLocalExistCheckUseCase(
        userRepository: UserRepository,
        localIdAddUseCase: LocalIdAddUseCase
    ): LocalExistCheckUseCase {
        return LocalExistCheckUseCase(userRepository, localIdAddUseCase)
    }

    @Provides
    fun provideLocalIdAddUseCase(
        userRepository: UserRepository
    ): LocalIdAddUseCase {
        return LocalIdAddUseCase(userRepository)
    }

    // user di
    @Provides
    fun provideUserUseCases(
        userRepository: UserRepository,
    ): UserUseCases {
        return UserUseCases(
            customIdCreateUser = CustomIdCreateUser(userRepository),
            logIn = LogInUseCase(userRepository),
            logout = LogoutUseCase(userRepository), // 주입받은 인스턴스 사용
            deleteUser = DeleteUserUseCase(userRepository),
            localUserUpdate = LocalUserUpdate(userRepository)
        )
    }

    // 환율 di
    @Provides
    fun provideTargetRateUseCases(
        webSocketClient: WebSocketClient
    ): TargetRateUseCases {
        return TargetRateUseCases(
            targetRateAddUseCase = TargetRateAddUseCase(),
            targetRateUpdateUseCase = TargetRateUpdateUseCase(webSocketClient),
            targetRateDeleteUseCase = TargetRateDeleteUseCase()
        )
    }

}