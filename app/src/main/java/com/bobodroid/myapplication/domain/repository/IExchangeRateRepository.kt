package com.bobodroid.myapplication.domain.repository

import com.bobodroid.myapplication.models.datamodels.roomDb.ExchangeRate
import kotlinx.coroutines.flow.Flow

/**
 * ExchangeRate Repository 인터페이스
 *
 * Domain Layer의 추상화된 환율 데이터 접근 인터페이스
 * - Platform 독립적인 비즈니스 로직 정의
 * - 구현체는 Data Layer에 위치
 */
interface IExchangeRateRepository {

    /**
     * 최신 환율 데이터 Flow
     */
    val latestRateFlow: Flow<ExchangeRate>

    /**
     * REST API로 초기 환율 데이터 가져오기 (12개 통화)
     */
    suspend fun fetchInitialRate(): Unit

    /**
     * 웹소켓으로 실시간 환율 업데이트 구독
     */
    suspend fun subscribeToRateUpdates(): Unit

    /**
     * 웹소켓 연결 해제
     */
    fun disconnect(): Unit
}