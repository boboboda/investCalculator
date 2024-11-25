package com.bobodroid.myapplication.models.datamodels.websocket

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.bobodroid.myapplication.BuildConfig
import com.bobodroid.myapplication.MainActivity.Companion.TAG
import com.bobodroid.myapplication.models.datamodels.repository.UserRepository
import com.bobodroid.myapplication.models.datamodels.roomDb.ExchangeRate
import com.bobodroid.myapplication.models.datamodels.roomDb.TargetRates
import com.bobodroid.myapplication.models.datamodels.service.UserApi.UserData
import com.bobodroid.myapplication.models.datamodels.service.exchangeRateApi.ExchangeRates
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
    private val userRepository: UserRepository
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
                        query = "deviceId=$deviceId"
                    }
                    connect(options)
                    initializationComplete.complete(Unit)
                }
            }
        }
    }

    private fun connect(options: IO.Options) {
        socket = IO.socket(BuildConfig.WEBSOCKET_URL, options)
        socket?.connect()
        socket?.on(Socket.EVENT_CONNECT) {
            Log.d(TAG("WebSocketClient", ""), "WebSocket Connected")
            requestInitialData()
        }
    }

    suspend fun recentRateWebReceiveData(
        onInsert: suspend (String) -> Unit,
        onInitialData: suspend (String) -> Unit
    ) {
        initializationComplete.await()
        socket?.let { socket ->


            socket.on("latestRate") { args ->
                scope.launch {
                    val exchangeRatesObject = args[0] as JSONObject
                    Log.d(TAG("WebSocketClient",""), "환율 오브젝트 $exchangeRatesObject")
                    onInsert(exchangeRatesObject.toString())
                }

            }

            socket.on("initialData") { args ->
                scope.launch {
                    val initialDataObject = args[0] as JSONObject
                    Log.d(TAG("WebSocketClient",""), "초기 데이터 $initialDataObject")
                    onInitialData(initialDataObject.toString())
                }
            }
        }
    }


    suspend fun targetRateUpdateReceiveData(
        onUpdate: (String) -> Unit
    ) {
        socket?.let { socket ->
            socket.on("targetUpdate") { args->
                val targetUpdateObject = args[0] as JSONObject
                Log.d(TAG("WebSocketClient","targetRateUpdate"), "목표환율 업데이트 ${targetUpdateObject}")

                onUpdate(targetUpdateObject.toString())

            }

        }
    }


    private fun requestInitialData() {
        socket?.emit("getInitialData")  // 서버에 초기 데이터 요청
    }


    fun requestLatestData() {
        socket?.emit("getLatestData")  // 최신 데이터 요청
    }



    fun disconnect() {
        socket?.disconnect()
    }
}
