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
import com.bobodroid.myapplication.models.datamodels.useCases.TargetRateUseCases
import com.bobodroid.myapplication.models.datamodels.useCases.UserUseCases
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent


@Module
@InstallIn(SingletonComponent::class)
object UseCaseModule {

    @Provides
    fun provideUserUseCases(
        userRepository: UserRepository
    ): UserUseCases {
        return UserUseCases(
            customIdCreateUser = CustomIdCreateUser(userRepository),
            logIn = LogInUseCase(userRepository),
            logout = LogoutUseCase(userRepository),
            localExistCheck = LocalExistCheckUseCase(userRepository, LocalIdAddUseCase(userRepository)),
            deleteUser = DeleteUserUseCase(userRepository),
            localUserUpdate = LocalUserUpdate(userRepository)
        )
    }


    @Provides
    fun provideTargetRateUseCases(): TargetRateUseCases {
        return  TargetRateUseCases(
           targetRateAddUseCase =  TargetRateAddUseCase())
    }

}