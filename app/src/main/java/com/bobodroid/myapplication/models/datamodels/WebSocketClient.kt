package com.example.app.data

import android.util.Log
import com.bobodroid.myapplication.MainActivity.Companion.TAG
import io.socket.client.IO
import io.socket.client.Socket
import org.json.JSONObject

class WebSocketClient {
    private lateinit var socket: Socket

    init {
        connect()
    }

    private fun connect() {
        socket = IO.socket("http://10.0.2.2:3000") // 서버 주소로 변경
        socket.connect()
        socket.on(Socket.EVENT_CONNECT) {
            Log.d(TAG, "WebSocket Connected")
            requestInitialData()  // 연결 즉시 초기 데이터 요청
        }

    }

    private fun requestInitialData() {
        socket.emit("getInitialData")  // 서버에 초기 데이터 요청
    }

    fun recentRateWebReceiveData(
        onInsert: (String) -> Unit,
        onInitialData: (String) -> Unit
    ) {
        socket.on("insert") { args ->
            val exchangeRates = args[0] as JSONObject
            Log.d(TAG, "환율 오브젝트 $exchangeRates")
            val rateString = exchangeRates.getString("exchangeRates")
            Log.d(TAG, "환율 $rateString")
            onInsert(rateString)  // 콜백 호출
        }

        socket.on("initialData") { args ->
            val initialData = args[0] as JSONObject
            Log.d(TAG, "초기 데이터 $initialData")
            val dataString = initialData.toString()
            onInitialData(dataString)  // 초기 데이터를 위한 콜백 호출
        }
    }
    fun requestLatestData() {
        socket.emit("getLatestData")  // 최신 데이터 요청
    }



    fun disconnect() {
        socket.disconnect()
    }
}
