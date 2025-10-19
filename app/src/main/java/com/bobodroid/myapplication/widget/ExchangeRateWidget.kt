package com.bobodroid.myapplication.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.View
import android.widget.RemoteViews
import com.bobodroid.myapplication.R
import com.bobodroid.myapplication.MainActivity
import com.bobodroid.myapplication.models.datamodels.repository.InvestRepository
import com.bobodroid.myapplication.models.datamodels.repository.LatestRateRepository
import com.bobodroid.myapplication.models.datamodels.repository.UserRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.math.RoundingMode
import javax.inject.Inject

@AndroidEntryPoint
class ExchangeRateWidget : AppWidgetProvider() {

    @Inject
    lateinit var latestRateRepository: LatestRateRepository

    @Inject
    lateinit var investRepository: InvestRepository

    @Inject
    lateinit var userRepository: UserRepository

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onEnabled(context: Context) {
        Log.d("ExchangeRateWidget", "위젯 활성화됨")
    }

    override fun onDisabled(context: Context) {
        Log.d("ExchangeRateWidget", "위젯 비활성화됨")
        scope.cancel()
    }

    private fun updateAppWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int
    ) {
        val views = RemoteViews(context.packageName, R.layout.widget_exchange_rate)

        // ✅ 위젯 전체 클릭 시 앱 열기
        val mainIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            action = Intent.ACTION_MAIN
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        val mainPendingIntent = PendingIntent.getActivity(
            context,
            0,
            mainIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.widget_container, mainPendingIntent)

        // ✅ 새로고침 버튼 클릭 이벤트
        val refreshIntent = Intent(context, WidgetRefreshReceiver::class.java).apply {
            action = WidgetRefreshReceiver.ACTION_REFRESH
        }
        val refreshPendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            refreshIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.widget_refresh_button, refreshPendingIntent)

        // ✅ 비동기 작업 후 단 한 번만 업데이트
        scope.launch {
            try {
                // User DB에서 프리미엄 상태 확인
                val user = userRepository.userData.firstOrNull()?.localUserData
                val isPremium = user?.isPremium ?: false

                // 프리미엄 사용자면 새로고침 버튼 숨김
                views.setViewVisibility(
                    R.id.widget_refresh_button,
                    if (isPremium) View.GONE else View.VISIBLE
                )

                val latestRate = latestRateRepository.latestRateFlow.firstOrNull()

                if (latestRate != null) {
                    // 달러 환율
                    val usdRate = latestRate.usd?.let {
                        try {
                            val rate = BigDecimal(it)
                            val formattedRate = rate.setScale(2, RoundingMode.HALF_UP)
                            "${formattedRate.toPlainString()}원"
                        } catch (e: Exception) {
                            "---"
                        }
                    } ?: "---"
                    views.setTextViewText(R.id.widget_usd_rate, usdRate)

                    // 엔화 환율
                    val jpyRate = latestRate.jpy?.let {
                        try {
                            val rate = BigDecimal(it)
                            val formattedRate = rate.setScale(2, RoundingMode.HALF_UP)
                            "${formattedRate.toPlainString()}원"
                        } catch (e: Exception) {
                            "---"
                        }
                    } ?: "---"
                    views.setTextViewText(R.id.widget_jpy_rate, jpyRate)

                    // 업데이트 시간
                    val updateTime = latestRate.createAt ?: "정보 없음"
                    views.setTextViewText(R.id.widget_update_time, "업데이트: $updateTime")

                    // 총 수익 계산
                    val dollarRecords = investRepository.getAllDollarBuyRecords().firstOrNull() ?: emptyList()
                    val yenRecords = investRepository.getAllYenBuyRecords().firstOrNull() ?: emptyList()

                    val totalDollarProfit = dollarRecords.sumOf {
                        it.expectProfit?.replace(",", "")?.toBigDecimalOrNull() ?: BigDecimal.ZERO
                    }
                    val totalYenProfit = yenRecords.sumOf {
                        it.expectProfit?.replace(",", "")?.toBigDecimalOrNull() ?: BigDecimal.ZERO
                    }
                    val totalProfit = totalDollarProfit + totalYenProfit

                    // 총 수익 포맷팅
                    val formattedProfit = when {
                        totalProfit > BigDecimal.ZERO -> {
                            val formatted = "%,d".format(totalProfit.toLong())
                            "+₩$formatted"
                        }
                        totalProfit < BigDecimal.ZERO -> {
                            val formatted = "%,d".format(totalProfit.abs().toLong())
                            "-₩$formatted"
                        }
                        else -> "₩0"
                    }

                    // 총 수익 색상
                    val profitColor = when {
                        totalProfit > BigDecimal.ZERO -> android.graphics.Color.parseColor("#10B981")
                        totalProfit < BigDecimal.ZERO -> android.graphics.Color.parseColor("#EF4444")
                        else -> android.graphics.Color.parseColor("#6B7280")
                    }

                    views.setTextViewText(R.id.widget_total_profit, formattedProfit)
                    views.setTextColor(R.id.widget_total_profit, profitColor)

                    Log.d("ExchangeRateWidget", "위젯 업데이트: USD=$usdRate, JPY=$jpyRate, 수익=$formattedProfit, 프리미엄=$isPremium")
                } else {
                    views.setTextViewText(R.id.widget_usd_rate, "데이터 없음")
                    views.setTextViewText(R.id.widget_jpy_rate, "데이터 없음")
                    views.setTextViewText(R.id.widget_update_time, "환율 정보를 불러올 수 없습니다")
                    views.setTextViewText(R.id.widget_total_profit, "₩0")

                    Log.w("ExchangeRateWidget", "환율 데이터가 없습니다")
                }
            } catch (e: Exception) {
                Log.e("ExchangeRateWidget", "위젯 업데이트 실패", e)
                views.setTextViewText(R.id.widget_usd_rate, "오류")
                views.setTextViewText(R.id.widget_jpy_rate, "오류")
                views.setTextViewText(R.id.widget_update_time, "오류 발생")
                views.setTextViewText(R.id.widget_total_profit, "오류")
            }

            // ✅ 모든 작업 완료 후 단 한 번만 업데이트
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}