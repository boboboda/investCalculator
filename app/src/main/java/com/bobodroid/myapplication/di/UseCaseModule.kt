package com.bobodroid.myapplication.di

import android.content.Context
import com.bobodroid.myapplication.models.datamodels.repository.InvestRepository
import com.bobodroid.myapplication.models.datamodels.repository.UserRepository
import com.bobodroid.myapplication.models.datamodels.social.SocialLoginManager
import com.bobodroid.myapplication.models.datamodels.useCases.AccountSwitchUseCase
import com.bobodroid.myapplication.models.datamodels.useCases.DeleteAllNotificationsUseCase
import com.bobodroid.myapplication.models.datamodels.useCases.DeleteNotificationUseCase
import com.bobodroid.myapplication.models.datamodels.useCases.DeleteOldNotificationsUseCase
import com.bobodroid.myapplication.models.datamodels.useCases.DeleteReadNotificationsUseCase
import com.bobodroid.myapplication.models.datamodels.useCases.DeleteUserUseCase
import com.bobodroid.myapplication.models.datamodels.useCases.FcmUseCases
import com.bobodroid.myapplication.models.datamodels.useCases.GetNotificationHistoryUseCase
import com.bobodroid.myapplication.models.datamodels.useCases.GetNotificationSettingsUseCase
import com.bobodroid.myapplication.models.datamodels.useCases.GetNotificationStatsUseCase
import com.bobodroid.myapplication.models.datamodels.useCases.GoogleLoginUseCase
import com.bobodroid.myapplication.models.datamodels.useCases.KakaoLoginUseCase
import com.bobodroid.myapplication.models.datamodels.useCases.LocalExistCheckUseCase
import com.bobodroid.myapplication.models.datamodels.useCases.LocalIdAddUseCase
import com.bobodroid.myapplication.models.datamodels.useCases.LocalUserUpdate
import com.bobodroid.myapplication.models.datamodels.useCases.MarkNotificationAsClickedUseCase
import com.bobodroid.myapplication.models.datamodels.useCases.MarkNotificationAsReadUseCase
import com.bobodroid.myapplication.models.datamodels.useCases.RestoreFromServerUseCase
import com.bobodroid.myapplication.models.datamodels.useCases.SendTestNotificationUseCase
import com.bobodroid.myapplication.models.datamodels.useCases.SocialLoginUseCases
import com.bobodroid.myapplication.models.datamodels.useCases.SocialLogoutUseCase
import com.bobodroid.myapplication.models.datamodels.useCases.SyncToServerUseCase
import com.bobodroid.myapplication.models.datamodels.useCases.TargetRateAddUseCase
import com.bobodroid.myapplication.models.datamodels.useCases.TargetRateDeleteUseCase
import com.bobodroid.myapplication.models.datamodels.useCases.TargetRateUpdateUseCase
import com.bobodroid.myapplication.models.datamodels.useCases.UnlinkSocialUseCase
import com.bobodroid.myapplication.models.datamodels.useCases.UpdateNotificationSettingsUseCase
import com.bobodroid.myapplication.models.datamodels.useCases.UserUseCases
import com.bobodroid.myapplication.models.datamodels.websocket.WebSocketClient
import com.bobodroid.myapplication.premium.PremiumManager
import com.bobodroid.myapplication.util.AdMob.AdManager
import com.bobodroid.myapplication.util.AdMob.AdUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
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

    // ==================== FCM UseCases ====================

    // ==================== FCM UseCases ====================

    @Provides
    @Singleton
    fun provideFcmUseCases(
        targetRateAddUseCase: TargetRateAddUseCase,
        targetRateUpdateUseCase: TargetRateUpdateUseCase,
        targetRateDeleteUseCase: TargetRateDeleteUseCase,
        getNotificationSettingsUseCase: GetNotificationSettingsUseCase,
        updateNotificationSettingsUseCase: UpdateNotificationSettingsUseCase,
        getNotificationHistoryUseCase: GetNotificationHistoryUseCase,
        markAsReadUseCase: MarkNotificationAsReadUseCase,
        markAsClickedUseCase: MarkNotificationAsClickedUseCase,
        getNotificationStatsUseCase: GetNotificationStatsUseCase,
        sendTestNotificationUseCase: SendTestNotificationUseCase,
        // 🆕 삭제 UseCases
        deleteNotificationUseCase: DeleteNotificationUseCase,
        deleteAllNotificationsUseCase: DeleteAllNotificationsUseCase,
        deleteReadNotificationsUseCase: DeleteReadNotificationsUseCase,
        deleteOldNotificationsUseCase: DeleteOldNotificationsUseCase
    ): FcmUseCases {
        return FcmUseCases(
            targetRateAddUseCase = targetRateAddUseCase,
            targetRateUpdateUseCase = targetRateUpdateUseCase,
            targetRateDeleteUseCase = targetRateDeleteUseCase,
            getNotificationSettingsUseCase = getNotificationSettingsUseCase,
            updateNotificationSettingsUseCase = updateNotificationSettingsUseCase,
            getNotificationHistoryUseCase = getNotificationHistoryUseCase,
            markAsReadUseCase = markAsReadUseCase,
            markAsClickedUseCase = markAsClickedUseCase,
            getNotificationStatsUseCase = getNotificationStatsUseCase,
            sendTestNotificationUseCase = sendTestNotificationUseCase,
            // 🆕 삭제 UseCases
            deleteNotificationUseCase = deleteNotificationUseCase,
            deleteAllNotificationsUseCase = deleteAllNotificationsUseCase,
            deleteReadNotificationsUseCase = deleteReadNotificationsUseCase,
            deleteOldNotificationsUseCase = deleteOldNotificationsUseCase
        )
    }

    // ==================== 기존 Providers ====================

    @Provides
    fun provideGetNotificationSettingsUseCase(): GetNotificationSettingsUseCase {
        return GetNotificationSettingsUseCase()
    }

    @Provides
    fun provideUpdateNotificationSettingsUseCase(): UpdateNotificationSettingsUseCase {
        return UpdateNotificationSettingsUseCase()
    }

    @Provides
    fun provideGetNotificationHistoryUseCase(): GetNotificationHistoryUseCase {
        return GetNotificationHistoryUseCase()
    }

    @Provides
    fun provideMarkNotificationAsReadUseCase(): MarkNotificationAsReadUseCase {
        return MarkNotificationAsReadUseCase()
    }

    @Provides
    fun provideMarkNotificationAsClickedUseCase(): MarkNotificationAsClickedUseCase {
        return MarkNotificationAsClickedUseCase()
    }

    @Provides
    fun provideGetNotificationStatsUseCase(): GetNotificationStatsUseCase {
        return GetNotificationStatsUseCase()
    }

    @Provides
    fun provideSendTestNotificationUseCase(): SendTestNotificationUseCase {
        return SendTestNotificationUseCase()
    }

    // ==================== 🆕 삭제 UseCase Providers ====================

    @Provides
    fun provideDeleteNotificationUseCase(): DeleteNotificationUseCase {
        return DeleteNotificationUseCase()
    }

    @Provides
    fun provideDeleteAllNotificationsUseCase(): DeleteAllNotificationsUseCase {
        return DeleteAllNotificationsUseCase()
    }

    @Provides
    fun provideDeleteReadNotificationsUseCase(): DeleteReadNotificationsUseCase {
        return DeleteReadNotificationsUseCase()
    }

    @Provides
    fun provideDeleteOldNotificationsUseCase(): DeleteOldNotificationsUseCase {
        return DeleteOldNotificationsUseCase()
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
        userRepository: UserRepository,
        investRepository: InvestRepository
    ): SyncToServerUseCase = SyncToServerUseCase(userRepository, investRepository)

    @Provides
    fun provideRestoreFromServerUseCase(
        userRepository: UserRepository,
        investRepository: InvestRepository
    ): RestoreFromServerUseCase = RestoreFromServerUseCase(userRepository, investRepository)


    @Provides
    fun provideAccountSwitchUseCase(
        userRepository: UserRepository,
    ): AccountSwitchUseCase = AccountSwitchUseCase(
        userRepository,
    )

    // 🆕 PremiumManager 추가
    @Provides
    @Singleton
    fun providePremiumManager(
        userUseCases: UserUseCases,
        userRepository: UserRepository,
        @ApplicationContext context: Context
    ): PremiumManager {
        return PremiumManager(context, userRepository, userUseCases)
    }

    // 🆕 AdUseCase 수정 (PremiumManager 주입)
    @Provides
    @Singleton
    fun provideAdUseCase(
        userUseCases: UserUseCases,
        premiumManager: PremiumManager,
        adManager: AdManager
    ): AdUseCase {
        return AdUseCase(userUseCases, premiumManager, adManager)
    }
}