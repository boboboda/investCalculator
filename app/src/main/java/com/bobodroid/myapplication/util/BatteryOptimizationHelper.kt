package com.bobodroid.myapplication.util

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import android.util.Log
import androidx.activity.result.ActivityResultLauncher

object BatteryOptimizationHelper {

    private const val TAG = "BatteryOptimization"

    /**
     * 배터리 최적화 예외 확인
     */
    fun isIgnoringBatteryOptimizations(context: Context): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
            return powerManager.isIgnoringBatteryOptimizations(context.packageName)
        }
        return true // Android 6.0 미만은 제한 없음
    }

    /**
     * 배터리 최적화 예외 설정 화면 열기
     */
    @SuppressLint("BatteryLife")
    fun requestBatteryOptimizationException(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                val intent = Intent().apply {
                    action = Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
                    data = Uri.parse("package:${context.packageName}")
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(intent)
                Log.d(TAG, "배터리 최적화 예외 요청 화면 열기")
            } catch (e: Exception) {
                Log.e(TAG, "배터리 최적화 설정 실패", e)
                // 대체: 설정 화면으로 이동
                openBatterySettings(context)
            }
        }
    }

    /**
     * 배터리 설정 화면 열기 (대체 방법)
     */
    private fun openBatterySettings(context: Context) {
        try {
            val intent = Intent().apply {
                action = Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Log.e(TAG, "배터리 설정 화면 열기 실패", e)
        }
    }
}