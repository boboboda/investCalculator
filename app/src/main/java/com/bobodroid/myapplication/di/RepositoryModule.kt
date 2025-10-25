package com.bobodroid.myapplication.di

import com.bobodroid.myapplication.data.repository.ExchangeRateRepositoryImpl
import com.bobodroid.myapplication.data.repository.RecordRepositoryImpl
import com.bobodroid.myapplication.data.repository.UserRepositoryImpl
import com.bobodroid.myapplication.domain.repository.IExchangeRateRepository
import com.bobodroid.myapplication.domain.repository.IRecordRepository
import com.bobodroid.myapplication.domain.repository.IUserRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Repository DI 모듈
 *
 * [Phase 2: Repository 분리]
 * - 인터페이스와 구현체를 바인딩
 * - 기존 InvestRepository는 점진적으로 제거 예정
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    /**
     * Record Repository 바인딩
     *
     * IRecordRepository 요청 시 → RecordRepositoryImpl 제공
     */
    @Binds
    @Singleton
    abstract fun bindRecordRepository(
        impl: RecordRepositoryImpl
    ): IRecordRepository

    @Binds
    @Singleton
    abstract fun bindUserRepository(
        impl: UserRepositoryImpl
    ): IUserRepository

    @Binds
    @Singleton
    abstract fun bindExchangeRateRepository(
        impl: ExchangeRateRepositoryImpl
    ): IExchangeRateRepository
}