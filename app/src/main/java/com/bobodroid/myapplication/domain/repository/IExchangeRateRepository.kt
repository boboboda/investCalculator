package com.bobodroid.myapplication.domain.repository

import com.bobodroid.myapplication.domain.entity.ExchangeRateEntity
import kotlinx.coroutines.flow.Flow

/**
 * ExchangeRate Repository 인터페이스 - Entity 버전
 */
interface IExchangeRateRepository {

    /**
     * 최신 환율 Flow
     */
    val latestRate: Flow<ExchangeRateEntity>

    /**
     * 웹소켓으로 실시간 환율 업데이트 구독
     * onInitialData가 초기 환율 제공
     */
    suspend fun subscribeToRateUpdates()

    /**
     * 웹소켓 연결 해제
     */
    fun disconnect()
}