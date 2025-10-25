package com.bobodroid.myapplication.widget

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.util.Log
import androidx.core.app.NotificationCompat
import com.bobodroid.myapplication.MainActivity
import com.bobodroid.myapplication.R
import com.bobodroid.myapplication.domain.repository.IRecordRepository
import com.bobodroid.myapplication.domain.repository.IUserRepository
import com.bobodroid.myapplication.models.datamodels.repository.LatestRateRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.firstOrNull
import java.math.BigDecimal
import javax.inject.Inject

/**
 * 위젯 실시간 업데이트 서비스 (Foreground Service)
 * - 프리미엄 사용자 전용
 * - 백그라운드에서 웹소켓 연결 유지
 * - 실시간 환율 데이터로 위젯 자동 업데이트
 */
@AndroidEntryPoint
class WidgetUpdateService : Service() {

    @Inject
    lateinit var latestRateRepository: LatestRateRepository

    @Inject
    lateinit var recordRepository: IRecordRepository

    @Inject
    lateinit var userRepository: IUserRepository

    // Service 전용 CoroutineScope (SupervisorJob 사용)
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    // WakeLock 관리
    private var wakeLock: PowerManager.WakeLock? = null

    // Flow 구독 Job
    private var rateCollectionJob: Job? = null



    companion object {
        private const val TAG = "WidgetUpdateService"
        private const val NOTIFICATION_ID = 1001
        private const val CHANNEL_ID = "widget_update_channel"
        private const val CHANNEL_NAME = "위젯 실시간 업데이트"
        private const val WAKE_LOCK_TAG = "MyApp:WidgetUpdateWakeLock"

        // 서비스 제어
        fun startService(context: Context) {
            val intent = Intent(context, WidgetUpdateService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
            Log.d(TAG, "서비스 시작 요청")
        }

        fun stopService(context: Context) {
            val intent = Intent(context, WidgetUpdateService::class.java)
            context.stopService(intent)
            Log.d(TAG, "서비스 종료 요청")
        }
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "━━━━━━━━━━━━━━━━━━━━━━━━━━")
        Log.d(TAG, "🚀 서비스 생성됨")
        Log.d(TAG, "━━━━━━━━━━━━━━━━━━━━━━━━━━")

        // WakeLock 획득
        acquireWakeLock()

        // Notification 채널 생성
        createNotificationChannel()

        // Foreground Service 즉시 시작 - 개선된 초기 메시지
        startForeground(NOTIFICATION_ID, createInitialNotification())
        Log.d(TAG, "✅ Foreground 알림 시작 완료")
    }

    override fun onBind(intent: Intent?): IBinder? = null

    /**
     * 프리미엄 상태 확인 후 Flow 구독 시작
     */
    private suspend fun checkPremiumAndStartCollection() {
        try {
            Log.d(TAG, "🔍 프리미엄 상태 확인 시작...")

            // User DB에서 프리미엄 체크
            val userData = userRepository.userData.firstOrNull()
            val user = userData?.localUserData
            val isPremium = user?.isPremium ?: false

            Log.d(TAG, "💎 isPremium: $isPremium")

            if (!isPremium) {
                Log.w(TAG, "━━━━━━━━━━━━━━━━━━━━━━━━━━")
                Log.w(TAG, "⚠️ 프리미엄 사용자가 아닙니다.")
                Log.w(TAG, "🛑 서비스를 종료합니다.")
                Log.w(TAG, "━━━━━━━━━━━━━━━━━━━━━━━━━━")
                stopSelf()
                return
            }

            Log.d(TAG, "━━━━━━━━━━━━━━━━━━━━━━━━━━")
            Log.d(TAG, "✅ 프리미엄 사용자 확인 완료!")
            Log.d(TAG, "🚀 실시간 환율 구독 시작...")
            Log.d(TAG, "━━━━━━━━━━━━━━━━━━━━━━━━━━")

            // 알림 업데이트 - 연결 중 상태
            updateConnectingNotification()

            // 실시간 환율 구독 시작
            subscribeToRateUpdates()

        } catch (e: Exception) {
            Log.e(TAG, "❌ 프리미엄 확인 중 오류", e)
            stopSelf()
        }
    }

    /**
     * 실시간 환율 데이터 구독
     */
    private suspend fun subscribeToRateUpdates() {
        try {
            Log.d(TAG, "📡 latestRateFlow 구독 시작")

            // Flow 수집 (무한 루프)
            latestRateRepository.latestRateFlow
                .catch { e ->
                    Log.e(TAG, "❌ Flow 수집 중 오류", e)
                    // 에러 발생 시 재구독 시도
                    delay(5000)
                    subscribeToRateUpdates()
                }
                .collect { latestRate ->
                    try {
                        Log.d(TAG, "━━━━━━━━━━━━━━━━━━━━━━━━━━")
                        Log.d(TAG, "💰 환율 업데이트 수신!")
                        Log.d(TAG, "USD: ${latestRate.usd}")
                        Log.d(TAG, "JPY: ${latestRate.jpy}")
                        Log.d(TAG, "━━━━━━━━━━━━━━━━━━━━━━━━━━")

                        // 1. 위젯 업데이트
                        Log.d(TAG, "🔄 위젯 업데이트 시작...")
                        WidgetUpdateHelper.updateAllWidgets(applicationContext)
                        Log.d(TAG, "✅ 위젯 업데이트 완료")

                        // 2. 알림 업데이트 (실행 중 상태만 표시)
                        updateRunningNotification()

                    } catch (e: Exception) {
                        Log.e(TAG, "❌ 업데이트 처리 중 오류", e)
                    }
                }

        } catch (e: Exception) {
            Log.e(TAG, "❌ 환율 구독 중 치명적 오류", e)
            // 5초 후 재시도
            delay(5000)
            subscribeToRateUpdates()
        }
    }



    /**
     * 알림 채널 생성
     */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW // 소리 없이 조용히 실행
            ).apply {
                description = "위젯의 실시간 환율 업데이트를 위한 백그라운드 서비스입니다"
                setShowBadge(false)
                setSound(null, null) // 무음 설정
            }

            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager?.createNotificationChannel(channel)
        }
    }

    /**
     * 초기 알림 생성
     */
    private fun createInitialNotification(): Notification {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_icon)
            .setContentTitle("위젯 실시간 업데이트 시작")
            .setContentText("백그라운드에서 환율 정보를 가져오는 중...")
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true) // 스와이프로 제거 불가
            .setSilent(true) // 무음
            .setAutoCancel(false)
            .build()
    }

    /**
     * 연결 중 알림 업데이트
     */
    private fun updateConnectingNotification() {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_icon)
            .setContentTitle("위젯 실시간 업데이트 연결 중")
            .setContentText("실시간 환율 서버와 연결하는 중...")
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .setSilent(true)
            .setAutoCancel(false)
            .build()

        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager?.notify(NOTIFICATION_ID, notification)
    }

    /**
     * 실행 중 알림 업데이트 (간결한 메시지)
     */
    private fun updateRunningNotification() {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // 스타일 적용 - 확장 가능한 알림
        val bigTextStyle = NotificationCompat.BigTextStyle()
            .bigText(
                """
                백그라운드에서 실시간 환율 정보를 받아
                위젯을 자동으로 업데이트하고 있습니다.
                
                ⚠️ 이 알림을 종료하면 실시간 업데이트가 중지됩니다.
                """.trimIndent()
            )
            .setBigContentTitle("위젯 실시간 업데이트")

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_icon)
            .setContentTitle("위젯 실시간 업데이트")
            .setContentText("백그라운드 실행 중")
            .setStyle(bigTextStyle)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .setSilent(true)
            .setAutoCancel(false)
            .setColor(0xFF6366F1.toInt()) // 프리미엄 색상
            .addAction(
                android.R.drawable.ic_menu_close_clear_cancel,  // 시스템 기본 아이콘 사용
                "서비스 종료",
                getStopServicePendingIntent()
            )
            .build()

        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager?.notify(NOTIFICATION_ID, notification)
    }

    /**
     * 서비스 종료 PendingIntent
     */
    private fun getStopServicePendingIntent(): PendingIntent {
        val stopIntent = Intent(this, WidgetUpdateService::class.java).apply {
            action = "STOP_SERVICE"
        }

        return PendingIntent.getService(
            this,
            1,
            stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "━━━━━━━━━━━━━━━━━━━━━━━━━━")
        Log.d(TAG, "🎯 onStartCommand() 호출")
        Log.d(TAG, "━━━━━━━━━━━━━━━━━━━━━━━━━━")

        // 서비스 종료 액션 처리
        if (intent?.action == "STOP_SERVICE") {
            Log.d(TAG, "🛑 서비스 종료 요청 (알림에서)")
            stopForeground(true)
            stopSelf()
            return START_NOT_STICKY
        }

        // 이미 실행 중인 Job이 있으면 취소
        rateCollectionJob?.cancel()

        // 새로운 Job 시작
        rateCollectionJob = serviceScope.launch {
            checkPremiumAndStartCollection()
        }

        // START_STICKY: 시스템이 서비스를 종료시켜도 자동으로 재시작
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "━━━━━━━━━━━━━━━━━━━━━━━━━━")
        Log.d(TAG, "💀 서비스 종료됨")
        Log.d(TAG, "━━━━━━━━━━━━━━━━━━━━━━━━━━")

        // Job 취소
        rateCollectionJob?.cancel()

        // Scope 취소
        serviceScope.cancel()

        // WakeLock 해제
        releaseWakeLock()
    }

    /**
     * WakeLock 획득 (CPU 깨어있음 유지)
     */
    private fun acquireWakeLock() {
        try {
            val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
            wakeLock = powerManager.newWakeLock(
                PowerManager.PARTIAL_WAKE_LOCK,
                WAKE_LOCK_TAG
            ).apply {
                acquire(10 * 60 * 1000L) // 최대 10분
            }
            Log.d(TAG, "✅ WakeLock 획득")
        } catch (e: Exception) {
            Log.e(TAG, "❌ WakeLock 획득 실패", e)
        }
    }

    /**
     * WakeLock 해제
     */
    private fun releaseWakeLock() {
        try {
            wakeLock?.let {
                if (it.isHeld) {
                    it.release()
                    Log.d(TAG, "✅ WakeLock 해제")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ WakeLock 해제 실패", e)
        }
    }
}