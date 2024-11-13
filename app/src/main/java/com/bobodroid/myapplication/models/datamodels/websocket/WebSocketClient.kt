package com.bobodroid.myapplication.models.datamodels.websocket

import android.util.Log
import com.bobodroid.myapplication.BuildConfig
import com.bobodroid.myapplication.MainActivity.Companion.TAG
import io.socket.client.IO
import io.socket.client.Socket
import org.json.JSONObject
import javax.inject.Inject

class WebSocketClient @Inject constructor() {
    private lateinit var socket: Socket

    init {
        Log.d(TAG("WebSocketClient","init"), "웹소켓: ${BuildConfig.WEBSOCKET_URL}")
        connect()
    }

    private fun connect() {
        socket = IO.socket(BuildConfig.WEBSOCKET_URL) // 서버 주소로 변경
        socket.connect()
        socket.on(Socket.EVENT_CONNECT) {
            Log.d(TAG("WebSocketClient",""), "WebSocket Connected")
            requestInitialData()  // 연결 즉시 초기 데이터 요청
        }

    }

    fun recentRateWebReceiveData(
        onInsert: (String) -> Unit,
        onInitialData: (String) -> Unit
    ) {
        socket.on("latestRate") { args ->
            val exchangeRatesObject = args[0] as JSONObject
            Log.d(TAG("WebSocketClient",""), "환율 오브젝트 $exchangeRatesObject")

            // 전체 JSON 객체를 그대로 전달
            onInsert(exchangeRatesObject.toString())
        }

        socket.on("initialData") { args ->
            val initialDataObject = args[0] as JSONObject
            Log.d(TAG("WebSocketClient",""), "초기 데이터 $initialDataObject")

            // 전체 JSON 객체를 그대로 전달
            onInitialData(initialDataObject.toString())
        }
    }


    private fun requestInitialData() {
        socket.emit("getInitialData")  // 서버에 초기 데이터 요청
    }


    fun requestLatestData() {
        socket.emit("getLatestData")  // 최신 데이터 요청
    }



    fun disconnect() {
        socket.disconnect()
    }
}
