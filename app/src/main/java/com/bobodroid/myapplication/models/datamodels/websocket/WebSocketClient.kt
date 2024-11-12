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
        connect()
    }

    private fun connect() {
        socket = IO.socket(BuildConfig.WEBSOCKET_URL) // 서버 주소로 변경
        socket.connect()
        socket.on(Socket.EVENT_CONNECT) {
            Log.d(TAG, "WebSocket Connected")
            requestInitialData()  // 연결 즉시 초기 데이터 요청
        }

    }

//    fun recentRateWebReceiveData(
//        onInsert: (String) -> Unit,
//        onInitialData: (String) -> Unit
//    ) {
//        // 최신 환율 데이터 수신
//        socket.on("latestRate") { args ->
//            if (args.isNotEmpty() && args[0] is JSONObject) {
//                val exchangeRates = args[0] as JSONObject
//                Log.d(TAG, "환율 오브젝트: $exchangeRates")
//
//                // "exchangeRates" 필드가 존재하는지 확인
//                val rateString = exchangeRates.optString("exchangeRates", null)
//                if (rateString != null) {
//                    Log.d(TAG, "환율: $rateString")
//                    onInsert(rateString)  // 콜백 호출
//                } else {
//                    Log.e(TAG, "'exchangeRates' 필드가 없습니다.")
//                }
//            } else {
//                Log.e(TAG, "유효하지 않은 데이터 형식입니다.")
//            }
//        }
//
//        // 초기 데이터 수신
//        socket.on("initialData") { args ->
//            if (args.isNotEmpty() && args[0] is JSONObject) {
//                val initialData = args[0] as JSONObject
//                Log.d(TAG, "초기 데이터: $initialData")
//                val dataString = initialData.toString()
//                onInitialData(dataString)  // 초기 데이터를 위한 콜백 호출
//            } else {
//                Log.e(TAG, "유효하지 않은 초기 데이터 형식입니다.")
//            }
//        }
//
//        // 최신 데이터 수신
//        socket.on("getLatestData") { args ->
//            if (args.isNotEmpty() && args[0] is JSONObject) {
//                val latestData = args[0] as JSONObject
//                Log.d(TAG, "최신 데이터: $latestData")
//                val dataString = latestData.toString()
//                onInitialData(dataString)  // 초기 데이터를 위한 콜백 호출
//            } else {
//                Log.e(TAG, "유효하지 않은 최신 데이터 형식입니다.")
//            }
//        }
//    }

    fun recentRateWebReceiveData(
        onInsert: (String) -> Unit,
        onInitialData: (String) -> Unit
    ) {
        socket.on("latestRate") { args ->
            val exchangeRatesObject = args[0] as JSONObject
            Log.d(TAG, "환율 오브젝트 $exchangeRatesObject")

            // 전체 JSON 객체를 그대로 전달
            onInsert(exchangeRatesObject.toString())
        }

        socket.on("initialData") { args ->
            val initialDataObject = args[0] as JSONObject
            Log.d(TAG, "초기 데이터 $initialDataObject")

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
