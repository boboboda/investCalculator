package com.bobodroid.myapplication.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.RemoteViews
import com.bobodroid.myapplication.R
import com.bobodroid.myapplication.MainActivity
import com.bobodroid.myapplication.models.datamodels.repository.InvestRepository
import com.bobodroid.myapplication.models.datamodels.repository.LatestRateRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
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

    // ✅ 위젯이 화면에 보일 때마다 호출 (삼성 런처 대응)
    override fun onAppWidgetOptionsChanged(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        newOptions: Bundle?
    ) {
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions)

        Log.d("ExchangeRateWidget", "옵션 변경됨 - 위젯 리셋")

        // 위젯 강제 리프레시
        scope.launch {
            delay(100) // 짧은 딜레이 후 리프레시
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    private fun updateAppWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int
    ) {
        val views = RemoteViews(context.packageName, R.layout.widget_exchange_rate)

        // ✅ 위젯 클릭 시 앱 열기
        val mainIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_NEW_TASK
        }

        val mainPendingIntent = PendingIntent.getActivity(
            context,
            appWidgetId,
            mainIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // ✅ 버튼에 클릭 리스너 설정 (TextView 대신)
        views.setOnClickPendingIntent(R.id.widget_button, mainPendingIntent)

        scope.launch {
            try {
                val latestRate = latestRateRepository.latestRateFlow.firstOrNull()

                if (latestRate != null) {
                    // ✅ 달러 환율
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
                    views.setTextViewText(R.id.widget_usd_change, "")

                    // ✅ 엔화 환율
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
                    views.setTextViewText(R.id.widget_jpy_change, "")

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

                    Log.d("ExchangeRateWidget", "위젯 업데이트: USD=$usdRate, JPY=$jpyRate, 수익=$formattedProfit")
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

            // ✅ 위젯 업데이트 적용
            appWidgetManager.updateAppWidget(appWidgetId, views)

            // ✅ 삼성 런처 대응: 즉시 다시 한 번 업데이트하여 스케일 리셋
            delay(50)
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}