package com.bobodroid.myapplication.models.datamodels.websocket

import android.util.Log
import com.bobodroid.myapplication.BuildConfig
import com.bobodroid.myapplication.MainActivity.Companion.TAG
import com.bobodroid.myapplication.domain.repository.IUserRepository
import io.socket.client.IO
import io.socket.client.Socket
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.json.JSONObject
import javax.inject.Inject

class WebSocketClient @Inject constructor(
    private val userRepository: IUserRepository
) {
    private var socket: Socket? = null
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val initializationComplete = CompletableDeferred<Unit>()

    init {
        Log.d(TAG("WebSocketClient", "init"), "웹소켓: ${BuildConfig.WEBSOCKET_URL}")
        setupConnection()
    }

    private fun setupConnection() {
        scope.launch {
            userRepository.userData.filterNotNull().first().let { userData ->
                userData.localUserData.id?.toString()?.let { deviceId ->
                    Log.d(TAG("WebSocketClient", "setupConnection"), "디바이스 ID: $deviceId")
                    val options = IO.Options().apply {
                        // ✅ HTTPS 사용
                        secure = true

                        // ✅ 전송 방식
                        transports = arrayOf("polling", "websocket")

                        // ✅ 경로 명시 (중요!)
                        path = "/exchange-rate/socket.io/"

                        // ✅ 쿼리 파라미터
                        query = "deviceId=$deviceId"

                        // ✅ 재연결 설정
                        reconnection = true
                        reconnectionDelay = 1000
                        reconnectionAttempts = 5

                        // ✅ 타임아웃
                        timeout = 10000
                    }
                    connect(options)
                    initializationComplete.complete(Unit)
                }
            }
        }
    }

    private fun connect(options: IO.Options) {
        // ✅ 베이스 URL만 (경로 제외)
        socket = IO.socket(BuildConfig.WEBSOCKET_URL, options)

        socket?.on(Socket.EVENT_CONNECT) {
            Log.d(TAG("WebSocketClient", "connect"), "✅ WebSocket 연결 성공")
            requestInitialData()
        }

        socket?.on(Socket.EVENT_CONNECT_ERROR) { args ->
            Log.e(TAG("WebSocketClient", "connect"), "❌ 연결 실패: ${args.getOrNull(0)}")
        }

        socket?.on(Socket.EVENT_DISCONNECT) {
            Log.d(TAG("WebSocketClient", "connect"), "🔌 연결 종료")
        }

        socket?.connect()
        Log.d(TAG("WebSocketClient", "connect"), "🔌 연결 시도 중...")
    }

    suspend fun recentRateWebReceiveData(
        onInsert: suspend (String) -> Unit,
        onInitialData: suspend (String) -> Unit
    ) {
        Log.d(TAG("WebSocketClient", "recentRateWebReceiveData"), "━━━━━━━━━━━━━━━━━━━━━━━━━━")
        Log.d(TAG("WebSocketClient", "recentRateWebReceiveData"), "📡 환율 데이터 구독 시작")

        initializationComplete.await()
        socket?.let { socket ->

            socket.on("latestRate") { args ->
                scope.launch {
                    val exchangeRatesObject = args[0] as JSONObject
                    Log.d(TAG("WebSocketClient","latestRate"), "━━━━━━━━━━━━━━━━━━━━━━━━━━")
                    Log.d(TAG("WebSocketClient","latestRate"), "📊 최신 환율 수신 (latestRate 이벤트)")
                    Log.d(TAG("WebSocketClient","latestRate"), "수신 시간: ${System.currentTimeMillis()}")
                    Log.d(TAG("WebSocketClient","latestRate"), "데이터: $exchangeRatesObject")
                    Log.d(TAG("WebSocketClient","latestRate"), "━━━━━━━━━━━━━━━━━━━━━━━━━━")
                    onInsert(exchangeRatesObject.toString())
                }
            }

            socket.on("initialData") { args ->
                scope.launch {
                    val initialDataObject = args[0] as JSONObject
                    Log.d(TAG("WebSocketClient","initialData"), "━━━━━━━━━━━━━━━━━━━━━━━━━━")
                    Log.d(TAG("WebSocketClient","initialData"), "🎯 초기 환율 수신 (initialData 이벤트)")
                    Log.d(TAG("WebSocketClient","initialData"), "수신 시간: ${System.currentTimeMillis()}")
                    Log.d(TAG("WebSocketClient","initialData"), "데이터: $initialDataObject")
                    Log.d(TAG("WebSocketClient","initialData"), "━━━━━━━━━━━━━━━━━━━━━━━━━━")
                    onInitialData(initialDataObject.toString())
                }
            }

            Log.d(TAG("WebSocketClient", "recentRateWebReceiveData"), "✅ 이벤트 리스너 등록 완료")
        } ?: run {
            Log.e(TAG("WebSocketClient", "recentRateWebReceiveData"), "❌ 소켓이 null입니다")
        }
    }

    suspend fun targetRateUpdateReceiveData(
        onUpdate: (String) -> Unit
    ) {
        socket?.let { socket ->
            socket.on("targetUpdate") { args->
                val targetUpdateObject = args[0] as JSONObject
                Log.d(TAG("WebSocketClient","targetRateUpdate"), "목표환율 업데이트: $targetUpdateObject")
                onUpdate(targetUpdateObject.toString())
            }
        }
    }

    private fun requestInitialData() {
        socket?.emit("getInitialData")
        Log.d(TAG("WebSocketClient", "requestInitialData"), "📡 초기 데이터 요청")
    }

    fun requestLatestData() {
        socket?.emit("getLatestData")
        Log.d(TAG("WebSocketClient", "requestLatestData"), "📡 최신 데이터 요청")
    }

    fun disconnect() {
        socket?.disconnect()
        socket?.off()
        Log.d(TAG("WebSocketClient", "disconnect"), "🔌 연결 해제")
    }
}