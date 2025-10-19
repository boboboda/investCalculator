package com.bobodroid.myapplication.widget

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent

/**
 * 위젯 업데이트 헬퍼
 * - ViewModel에서 호출하여 위젯 즉시 갱신
 */
object WidgetUpdateHelper {

    /**
     * 모든 위젯 즉시 업데이트
     */
    fun updateAllWidgets(context: Context) {
        val intent = Intent(context, ExchangeRateWidget::class.java)
        intent.action = AppWidgetManager.ACTION_APPWIDGET_UPDATE

        val appWidgetManager = AppWidgetManager.getInstance(context)
        val ids = appWidgetManager.getAppWidgetIds(
            ComponentName(context, ExchangeRateWidget::class.java)
        )

        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
        context.sendBroadcast(intent)
    }
}