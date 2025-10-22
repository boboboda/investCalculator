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

    private val TAG = "FirebaseService"

    /** Token 생성 메서드 */
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        InvestApplication.prefs.setData("fcm_token", token)
        FCMTokenEvent.updateToken(token)
    }

    /** 메시지 수신 메서드 */
    @OptIn(ExperimentalFoundationApi::class)
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Log.d(TAG, "From: ${remoteMessage.from}")

        // notification 필드가 있는지 확인
        remoteMessage.notification?.let {
            val title = it.title ?: "기본 제목"
            val body = it.body ?: "기본 내용"

            Log.d(TAG, "Notification Title: $title")
            Log.d(TAG, "Notification Body: $body")

            // ✅ data 필드에서 정보 추출
            val notificationId = remoteMessage.data["notificationId"]
            val notificationType = remoteMessage.data["type"]
            val recordId = remoteMessage.data["recordId"]
            val currency = remoteMessage.data["currency"]

            Log.d(TAG, "Notification Data - ID: $notificationId, Type: $notificationType")

            // 알림 생성
            sendNotification(title, body, notificationId, notificationType, recordId, currency)
        } ?: Log.e(TAG, "Notification field is empty.")
    }

    /** 알림 생성 메서드 */
    @ExperimentalFoundationApi
    @SuppressLint("ServiceCast")
    private fun sendNotification(
        title: String,
        body: String,
        notificationId: String? = null,
        notificationType: String? = null,
        recordId: String? = null,
        currency: String? = null
    ) {
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

        // ✅ MainActivity로 이동하는 Intent 생성 (데이터 포함)
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            // ✅ 알림 관련 데이터 전달
            putExtra("FROM_NOTIFICATION", true)
            putExtra("NOTIFICATION_ID", notificationId)
            putExtra("NOTIFICATION_TYPE", notificationType)
            putExtra("RECORD_ID", recordId)
            putExtra("CURRENCY", currency)
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            System.currentTimeMillis().toInt(), // 고유 ID
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.mipmap.ic_main_round) // 알림 아이콘
            .setContentTitle(title) // 제목
            .setContentText(body) // 내용
            .setAutoCancel(true) // 클릭 시 알림 제거
            .setPriority(NotificationCompat.PRIORITY_DEFAULT) // 우선순위 설정
            .setContentIntent(pendingIntent) // ✅ 클릭 시 실행할 Intent

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