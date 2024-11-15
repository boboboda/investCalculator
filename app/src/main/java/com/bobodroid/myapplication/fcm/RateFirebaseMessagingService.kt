package com.bobodroid.myapplication.fcm

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.bobodroid.myapplication.MainActivity
import com.bobodroid.myapplication.R
import com.bobodroid.myapplication.util.InvestApplication
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage


class RateFirebaseMessagingService: FirebaseMessagingService() {
    /** 푸시 알림으로 보낼 수 있는 메세지는 2가지
     * 1. Notification: 앱이 실행중(포그라운드)일 떄만 푸시 알림이 옴
     * 2. Data: 실행중이거나 백그라운드(앱이 실행중이지 않을때) 알림이 옴 -> TODO: 대부분 사용하는 방식 */

    private val TAG = "FirebaseService"

    /** Token 생성 메서드(FirebaseInstanceIdService 사라짐) */
//    override fun onNewToken(token: String) {
//        Log.d(TAG, "new Token: $token")
//
//        // 토큰 값을 따로 저장
//        InvestApplication.prefs.setData("fcm_token", token)
//        Log.i(TAG, "성공적으로 토큰을 저장함")
//    }
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        InvestApplication.prefs.setData("fcm_token", token)
        FCMTokenEvent.updateToken(token)
    }

    /** 메시지 수신 메서드(포그라운드) */
//    @OptIn(ExperimentalFoundationApi::class)
//    override fun onMessageReceived(remoteMessage: RemoteMessage) {
//        Log.d(TAG, "From: " + remoteMessage!!.from)
//
//        // Notification 메시지를 수신할 경우
//        // remoteMessage.notification?.body!! 여기에 내용이 저장되있음
////         Log.d(TAG, "Notification Message Body: " + remoteMessage.notification?.body!!)
//
//        //받은 remoteMessage의 값 출력해보기. 데이터메세지 / 알림메세지
//        Log.d(TAG, "Message data : ${remoteMessage.data}")
//        Log.d(TAG, "Message noti : ${remoteMessage.notification}")
//
//        if (remoteMessage.data.isNotEmpty()) {
//            //알림생성
//            sendNotification(remoteMessage)
//            Log.d(TAG, remoteMessage.data["title"].toString())
//            Log.d(TAG, remoteMessage.data["body"].toString())
//        } else {
//            Log.e(TAG, "data가 비어있습니다. 메시지를 수신하지 못했습니다.")
//        }
//    }


    @OptIn(ExperimentalFoundationApi::class)
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Log.d(TAG, "From: ${remoteMessage.from}")

        // notification 필드가 있는지 확인
        remoteMessage.notification?.let {
            val title = it.title ?: "기본 제목"
            val body = it.body ?: "기본 내용"

            Log.d(TAG, "Notification Title: $title")
            Log.d(TAG, "Notification Body: $body")

            // 알림 생성
            sendNotification(title, body)
        } ?: Log.e(TAG, "Notification field is empty.")
    }

    /** 알림 생성 메서드 */
    @OptIn(ExperimentalMaterial3Api::class)
//    @ExperimentalFoundationApi
//    @SuppressLint("ServiceCast")
//    private fun sendNotification(remoteMessage: RemoteMessage) {
//        // RequestCode, Id를 고유값으로 지정하여 알림이 개별 표시
//        val uniId: Int = (System.currentTimeMillis() / 7).toInt()
//
//        // 일회용 PendingIntent : Intent 의 실행 권한을 외부의 어플리케이션에게 위임
//        val intent = Intent(this, MainActivity::class.java)
//        //각 key, value 추가
//        for (key in remoteMessage.data.keys) {
//            intent.putExtra(key, remoteMessage.data.getValue(key))
//        }
//        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP) // Activity Stack 을 경로만 남김(A-B-C-D-B => A-B)
//
//        //23.05.22 Android 최신버전 대응 (FLAG_MUTABLE, FLAG_IMMUTABLE)
//        //PendingIntent.FLAG_MUTABLE은 PendingIntent의 내용을 변경할 수 있도록 허용, PendingIntent.FLAG_IMMUTABLE은 PendingIntent의 내용을 변경할 수 없음
//        //val pendingIntent = PendingIntent.getActivity(this, uniId, intent, PendingIntent.FLAG_ONE_SHOT)
//        val pendingIntent = PendingIntent.getActivity(
//            this,
//            uniId,
//            intent,
//            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_MUTABLE
//        )
//
//        // 알림 채널 이름
//        val channelId = "my_channel"
//        // 알림 소리
//        val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
//
//
//        // 알림에 대한 UI 정보, 작업
//        val notificationBuilder = NotificationCompat.Builder(this, channelId)
//            .setSmallIcon(R.mipmap.ic_main_round)
//            .setContentTitle(remoteMessage.data["title"].toString()) // 제목
//            .setContentText(remoteMessage.data["body"].toString()) // 메시지 내용
//            .setAutoCancel(true)
//            .setSound(soundUri)
//            .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
//            .setContentIntent(pendingIntent)
//            .setColor(ContextCompat.getColor(this, R.color.purple_200))
//
//        val notificationManager =
//            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
//
//        // 오레오 버전 이후에는 채널이 필요
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            val channel =
//                NotificationChannel(channelId, "Notice", NotificationManager.IMPORTANCE_DEFAULT)
//            notificationManager.createNotificationChannel(channel)
//        }
//
//        // 알림 생성
//        notificationManager.notify(uniId, notificationBuilder.build())
//    }


    @ExperimentalFoundationApi
    @SuppressLint("ServiceCast")
    private fun sendNotification(title: String, body: String) {
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "default_channel_id"

        // 알림 채널 생성 (Android 8.0 이상 필요)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "기본 채널",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }

        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.mipmap.ic_main_round) // 알림 아이콘
            .setContentTitle(title) // 제목
            .setContentText(body) // 내용
            .setAutoCancel(true) // 클릭 시 알림 제거
            .setPriority(NotificationCompat.PRIORITY_DEFAULT) // 우선순위 설정

        notificationManager.notify(System.currentTimeMillis().toInt(), notificationBuilder.build())
    }
    /** Token 가져오기 */
    fun getFirebaseToken(getToken:(String) -> Unit) {
        //비동기 방식
        FirebaseMessaging.getInstance().token.addOnSuccessListener {
            Log.d(TAG, "token=${it}")

            getToken.invoke(it)

        }
    }

    fun deleteFirebaseToken() {
        FirebaseMessaging.getInstance().deleteToken()
    }
}