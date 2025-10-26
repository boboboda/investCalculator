package com.bobodroid.myapplication.feature.exchange.presentation

/**
 * Exchange Event
 *
 * 환율 화면에서 발생하는 이벤트
 */
sealed class ExchangeEvent {
    /**
     * 화면 초기화
     */
    object Initialize : ExchangeEvent()

    /**
     * 환율 새로고침
     */
    object Refresh : ExchangeEvent()

    /**
     * WebSocket 연결 시작
     */
    object ConnectWebSocket : ExchangeEvent()

    /**
     * WebSocket 연결 해제
     */
    object DisconnectWebSocket : ExchangeEvent()

    /**
     * 에러 메시지 클리어
     */
    object ClearError : ExchangeEvent()
}