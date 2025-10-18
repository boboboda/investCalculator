package com.bobodroid.myapplication.di

import com.bobodroid.myapplication.models.datamodels.repository.UserRepository
import com.bobodroid.myapplication.models.datamodels.social.SocialLoginManager
import com.bobodroid.myapplication.models.datamodels.useCases.DeleteUserUseCase
import com.bobodroid.myapplication.models.datamodels.useCases.GoogleLoginUseCase
import com.bobodroid.myapplication.models.datamodels.useCases.KakaoLoginUseCase
import com.bobodroid.myapplication.models.datamodels.useCases.LocalExistCheckUseCase
import com.bobodroid.myapplication.models.datamodels.useCases.LocalIdAddUseCase
import com.bobodroid.myapplication.models.datamodels.useCases.LocalUserUpdate
import com.bobodroid.myapplication.models.datamodels.useCases.RestoreFromServerUseCase
import com.bobodroid.myapplication.models.datamodels.useCases.SocialLoginUseCases
import com.bobodroid.myapplication.models.datamodels.useCases.SocialLogoutUseCase
import com.bobodroid.myapplication.models.datamodels.useCases.SyncToServerUseCase
import com.bobodroid.myapplication.models.datamodels.useCases.TargetRateAddUseCase
import com.bobodroid.myapplication.models.datamodels.useCases.TargetRateDeleteUseCase
import com.bobodroid.myapplication.models.datamodels.useCases.TargetRateUpdateUseCase
import com.bobodroid.myapplication.models.datamodels.useCases.TargetRateUseCases
import com.bobodroid.myapplication.models.datamodels.useCases.UnlinkSocialUseCase
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
        localIdAddUseCase: LocalIdAddUseCase,
        socialLoginManager: SocialLoginManager
    ): LocalExistCheckUseCase {
        return LocalExistCheckUseCase(userRepository, localIdAddUseCase, socialLoginManager)
    }

    @Provides
    fun provideLocalIdAddUseCase(
        userRepository: UserRepository
    ): LocalIdAddUseCase {
        return LocalIdAddUseCase(userRepository)
    }

    // user di
    @Provides
    @Singleton
    fun provideUserUseCases(
        deleteUser: DeleteUserUseCase,
        localUserUpdate: LocalUserUpdate,
        socialLoginUseCases: SocialLoginUseCases
    ): UserUseCases {
        return UserUseCases(
            deleteUser = deleteUser,
            localUserUpdate = localUserUpdate,
            socialLoginUseCases = socialLoginUseCases
        )
    }

    @Provides
    @Singleton
    fun provideSocialLoginUseCases(
        googleLogin: GoogleLoginUseCase,
        kakaoLogin: KakaoLoginUseCase,
        socialLogout: SocialLogoutUseCase,
        syncToServer: SyncToServerUseCase,
        restoreFromServer: RestoreFromServerUseCase,
        unlinkSocialUseCase: UnlinkSocialUseCase
    ): SocialLoginUseCases {
        return SocialLoginUseCases(
            googleLogin = googleLogin,
            kakaoLogin = kakaoLogin,
            socialLogout = socialLogout,
            syncToServer = syncToServer,
            restoreFromServer = restoreFromServer,
            unlinkSocial = unlinkSocialUseCase
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




    // ✅ 개별 UseCase 제공
    @Provides
    fun provideDeleteUserUseCase(
        userRepository: UserRepository
    ): DeleteUserUseCase = DeleteUserUseCase(userRepository)

    @Provides
    fun provideLocalUserUpdate(
        userRepository: UserRepository
    ): LocalUserUpdate = LocalUserUpdate(userRepository)


    // ✅ 소셜 로그인 개별 UseCase 제공
    @Provides
    fun provideGoogleLoginUseCase(
        userRepository: UserRepository,
        socialLoginManager: SocialLoginManager
    ): GoogleLoginUseCase = GoogleLoginUseCase(userRepository, socialLoginManager)

    @Provides
    fun provideKakaoLoginUseCase(
        userRepository: UserRepository,
        socialLoginManager: SocialLoginManager
    ): KakaoLoginUseCase = KakaoLoginUseCase(userRepository, socialLoginManager)

    @Provides
    fun provideSocialLogoutUseCase(
        userRepository: UserRepository,
        socialLoginManager: SocialLoginManager
    ): SocialLogoutUseCase = SocialLogoutUseCase(userRepository, socialLoginManager)

    @Provides
    fun provideSyncToServerUseCase(
        userRepository: UserRepository
    ): SyncToServerUseCase = SyncToServerUseCase(userRepository)

    @Provides
    fun provideRestoreFromServerUseCase(
        userRepository: UserRepository
    ): RestoreFromServerUseCase = RestoreFromServerUseCase(userRepository)

}