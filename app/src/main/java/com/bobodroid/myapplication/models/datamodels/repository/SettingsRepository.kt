package com.bobodroid.myapplication.models.repository

import android.content.Context
import android.content.SharedPreferences
import com.bobodroid.myapplication.models.datamodels.roomDb.CurrencyType
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 앱 전역 설정 관리
 * - 선택된 통화
 * - 기타 사용자 설정
 */
@Singleton
class SettingsRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val PREFS_NAME = "app_settings"
        private const val KEY_SELECTED_CURRENCY = "selected_currency"
        private const val DEFAULT_CURRENCY = "USD"
    }

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    // 선택된 통화 StateFlow
    private val _selectedCurrency = MutableStateFlow(getCurrentCurrency())
    val selectedCurrency: StateFlow<CurrencyType> = _selectedCurrency.asStateFlow()

    /**
     * 현재 선택된 통화 가져오기
     */
    private fun getCurrentCurrency(): CurrencyType {
        val currencyCode = prefs.getString(KEY_SELECTED_CURRENCY, DEFAULT_CURRENCY) ?: DEFAULT_CURRENCY
        return try {
            CurrencyType.valueOf(currencyCode)
        } catch (e: IllegalArgumentException) {
            CurrencyType.USD  // 잘못된 값이면 기본값
        }
    }

    /**
     * 통화 선택 변경
     */
    fun setSelectedCurrency(currency: CurrencyType) {
        prefs.edit()
            .putString(KEY_SELECTED_CURRENCY, currency.name)
            .apply()

        _selectedCurrency.value = currency
    }

    /**
     * 통화 선택 초기화
     */
    fun resetCurrency() {
        setSelectedCurrency(CurrencyType.USD)
    }
}