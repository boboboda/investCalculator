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
import com.bobodroid.myapplication.models.datamodels.repository.InvestRepository
import com.bobodroid.myapplication.models.datamodels.repository.LatestRateRepository
import com.bobodroid.myapplication.models.datamodels.repository.UserRepository
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
    lateinit var investRepository: InvestRepository

    @Inject
    lateinit var userRepository: UserRepository

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
        private const val CHANNEL_NAME = "실시간 환율 업데이트"
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

        // Foreground Service 즉시 시작 (중요!)
        startForeground(NOTIFICATION_ID, createNotification("서비스 시작 중...", ""))
        Log.d(TAG, "✅ Foreground 알림 시작 완료")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "━━━━━━━━━━━━━━━━━━━━━━━━━━")
        Log.d(TAG, "🎯 onStartCommand() 호출")
        Log.d(TAG, "━━━━━━━━━━━━━━━━━━━━━━━━━━")

        // 이미 실행 중인 Job이 있으면 취소
        rateCollectionJob?.cancel()

        // 새로운 Job 시작
        rateCollectionJob = serviceScope.launch {
            checkPremiumAndStartCollection()
        }

        // START_STICKY: 시스템이 서비스를 종료시켜도 자동으로 재시작
        return START_STICKY
    }

    /**
     * 프리미엄 상태 확인 후 Flow 구독 시작
     */
    private suspend fun checkPremiumAndStartCollection() {
        try {
            Log.d(TAG, "🔍 프리미엄 상태 확인 시작...")

            // User DB에서 프리미엄 체크
            val userData = userRepository.userData.firstOrNull()
            Log.d(TAG, "📦 UserData: $userData")

            val user = userData?.localUserData
            Log.d(TAG, "👤 LocalUser: $user")

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

            // 알림 업데이트
            updateNotification("환율 정보를 불러오는 중...", "₩0")

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

                        // 2. 알림 업데이트 (환율 + 수익)
                        withContext(Dispatchers.IO) {
                            val totalProfit = calculateTotalProfit()
                            withContext(Dispatchers.Main) {
                                updateNotification(
                                    "USD $${latestRate.usd} | JPY ¥${latestRate.jpy}",
                                    totalProfit
                                )
                                Log.d(TAG, "💵 총 수익: $totalProfit")
                                Log.d(TAG, "✅ 알림 업데이트 완료")
                            }
                        }

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
     * 총 수익 계산
     */
    private suspend fun calculateTotalProfit(): String {
        return try {
            val dollarRecords = investRepository.getAllDollarBuyRecords().firstOrNull() ?: emptyList()
            val yenRecords = investRepository.getAllYenBuyRecords().firstOrNull() ?: emptyList()

            val totalDollarProfit = dollarRecords.sumOf {
                it.expectProfit?.replace(",", "")?.toBigDecimalOrNull() ?: BigDecimal.ZERO
            }
            val totalYenProfit = yenRecords.sumOf {
                it.expectProfit?.replace(",", "")?.toBigDecimalOrNull() ?: BigDecimal.ZERO
            }

            val totalProfit = totalDollarProfit + totalYenProfit
            val formattedProfit = String.format("%,.0f", totalProfit)

            if (totalProfit > BigDecimal.ZERO) "+₩$formattedProfit" else "₩$formattedProfit"
        } catch (e: Exception) {
            Log.e(TAG, "수익 계산 실패", e)
            "₩0"
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
                description = "실시간 환율 정보를 백그라운드에서 업데이트합니다"
                setShowBadge(false)
            }

            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager?.createNotificationChannel(channel)
        }
    }

    /**
     * 알림 생성
     */
    private fun createNotification(title: String, content: String): Notification {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_icon)
            .setContentTitle(title)
            .setContentText(content)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true) // 스와이프로 제거 불가
            .setAutoCancel(false)
            .build()
    }

    /**
     * 알림 업데이트
     */
    private fun updateNotification(rates: String, profit: String) {
        val title = "실시간 환율"
        val content = "$rates | 수익: $profit"

        val notification = createNotification(title, content)
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager?.notify(NOTIFICATION_ID, notification)
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
                acquire(10*60*1000L) // 10분 타임아웃 (안전장치)
                Log.d(TAG, "🔋 WakeLock 획득 완료")
            }
        } catch (e: Exception) {
            Log.e(TAG, "WakeLock 획득 실패", e)
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
                    Log.d(TAG, "🔋 WakeLock 해제 완료")
                }
            }
            wakeLock = null
        } catch (e: Exception) {
            Log.e(TAG, "WakeLock 해제 실패", e)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "━━━━━━━━━━━━━━━━━━━━━━━━━━")
        Log.d(TAG, "🛑 서비스 종료됨")
        Log.d(TAG, "━━━━━━━━━━━━━━━━━━━━━━━━━━")

        // Job 취소
        rateCollectionJob?.cancel()

        // Scope 취소
        serviceScope.cancel()

        // WakeLock 해제
        releaseWakeLock()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}