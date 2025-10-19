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
import android.util.Log
import androidx.core.app.NotificationCompat
import com.bobodroid.myapplication.MainActivity
import com.bobodroid.myapplication.R
import com.bobodroid.myapplication.models.datamodels.repository.InvestRepository
import com.bobodroid.myapplication.models.datamodels.repository.LatestRateRepository
import com.bobodroid.myapplication.models.datamodels.repository.UserRepository
import com.bobodroid.myapplication.premium.PremiumManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
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
    lateinit var userRepository: UserRepository // ✅ 변경

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    companion object {
        private const val TAG = "WidgetUpdateService"
        private const val NOTIFICATION_ID = 1001
        private const val CHANNEL_ID = "widget_update_channel"
        private const val CHANNEL_NAME = "실시간 환율 업데이트"

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
        Log.d(TAG, "서비스 생성됨")

        // ✅ User DB에서 프리미엄 체크
        scope.launch {
            val user = userRepository.userData.firstOrNull()?.localUserData
            val isPremium = user?.isPremium ?: false

            if (!isPremium) {
                Log.w(TAG, "⚠️ 프리미엄 사용자가 아닙니다. 서비스를 종료합니다.")
                stopSelf()
                return@launch
            }

            // Foreground Service 시작
            createNotificationChannel()
            startForeground(NOTIFICATION_ID, createNotification("환율 정보를 불러오는 중...", "₩0"))

            // 실시간 환율 구독
            subscribeToRateUpdates()
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "onStartCommand 호출")
        return START_STICKY // 시스템에 의해 종료되어도 자동 재시작
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "서비스 종료됨")
        scope.cancel()
    }

    /**
     * 실시간 환율 데이터 구독
     */
    private fun subscribeToRateUpdates() {
        scope.launch {
            try {
                Log.d(TAG, "실시간 환율 구독 시작")

                latestRateRepository.latestRateFlow.collect { latestRate ->
                    Log.d(TAG, "환율 업데이트 수신: USD=${latestRate.usd}, JPY=${latestRate.jpy}")

                    // 1. 위젯 업데이트
                    WidgetUpdateHelper.updateAllWidgets(applicationContext)

                    // 2. 알림 업데이트 (현재 환율 + 수익 표시)
                    val totalProfit = calculateTotalProfit()
                    updateNotification(
                        "USD $${latestRate.usd} | JPY ¥${latestRate.jpy}",
                        totalProfit
                    )

                    Log.d(TAG, "위젯 & 알림 업데이트 완료")
                }
            } catch (e: Exception) {
                Log.e(TAG, "환율 구독 중 오류 발생", e)
                stopSelf()
            }
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

            when {
                totalProfit > BigDecimal.ZERO -> {
                    val formatted = "%,d".format(totalProfit.toLong())
                    "수익 +₩$formatted"
                }
                totalProfit < BigDecimal.ZERO -> {
                    val formatted = "%,d".format(totalProfit.abs().toLong())
                    "손실 -₩$formatted"
                }
                else -> "수익 ₩0"
            }
        } catch (e: Exception) {
            Log.e(TAG, "수익 계산 실패", e)
            "수익 계산 중..."
        }
    }

    /**
     * 알림 채널 생성 (Android 8.0 이상)
     */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW // 소리 없이 조용히
            ).apply {
                description = "실시간 환율 정보를 위젯에 업데이트합니다"
                setShowBadge(false)
            }

            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
            Log.d(TAG, "알림 채널 생성 완료")
        }
    }

    /**
     * 알림 생성
     */
    private fun createNotification(title: String, content: String): Notification {
        val notificationIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            notificationIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("💱 $title")
            .setContentText(content)
            .setSmallIcon(R.drawable.ic_icon) // 앱 아이콘으로 변경
            .setContentIntent(pendingIntent)
            .setOngoing(true) // 스와이프로 지울 수 없음
            .setPriority(NotificationCompat.PRIORITY_LOW) // 조용한 알림
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .build()
    }

    /**
     * 알림 업데이트
     */
    private fun updateNotification(title: String, content: String) {
        val notification = createNotification(title, content)
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, notification)
    }
}