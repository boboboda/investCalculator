package com.bobodroid.myapplication.models.datamodels.repository

import android.content.Context
import android.content.SharedPreferences
import com.bobodroid.myapplication.models.datamodels.repository.UserRepository
import com.bobodroid.myapplication.models.datamodels.roomDb.Currencies
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
 * - 통화별 매수/매도 스프레드
 * - 기타 사용자 설정
 */
@Singleton
class SettingsRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val userRepository: UserRepository
) {
    companion object {
        private const val PREFS_NAME = "app_settings"
        private const val KEY_SELECTED_CURRENCY = "selected_currency"
        private const val DEFAULT_CURRENCY = "USD"

        // ✅ 스프레드 설정 키 prefix
        private const val KEY_SPREAD_BUY_PREFIX = "spread_buy_"
        private const val KEY_SPREAD_SELL_PREFIX = "spread_sell_"

        // ✅ 서버 스키마 기본값과 동일 (50/50 분할 기준)
        private const val DEFAULT_BUY_SPREAD_PERCENT = 0.75
        private const val DEFAULT_SELL_SPREAD_PERCENT = 0.75
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
    fun setSelectedCurrency(currency: CurrencyType): Boolean {
        val currencyObj = Currencies.fromCurrencyType(currency)

        // ✅ 프리미엄 통화인데 프리미엄 유저가 아니면
        if (currencyObj.isPremium) {
            val isPremium = userRepository.userData.value?.localUserData?.isPremium ?: false
            if (!isPremium) {
                return false // 변경 실패
            }
        }

        // 변경 가능
        prefs.edit()
            .putString(KEY_SELECTED_CURRENCY, currency.name)
            .apply()
        _selectedCurrency.value = currency
        return true // 변경 성공
    }

    /**
     * 통화 선택 초기화
     */
    fun resetCurrency() {
        setSelectedCurrency(CurrencyType.USD)
    }

    // ==================== 통화별 매수/매도 스프레드 설정 ====================

    /**
     * 매수(살 때) 스프레드 % 조회
     */
    fun getBuySpreadPercent(currency: CurrencyType): Double {
        return prefs.getFloat(
            KEY_SPREAD_BUY_PREFIX + currency.name,
            DEFAULT_BUY_SPREAD_PERCENT.toFloat()
        ).toDouble()
    }

    /**
     * 매도(팔 때) 스프레드 % 조회
     */
    fun getSellSpreadPercent(currency: CurrencyType): Double {
        return prefs.getFloat(
            KEY_SPREAD_SELL_PREFIX + currency.name,
            DEFAULT_SELL_SPREAD_PERCENT.toFloat()
        ).toDouble()
    }

    /**
     * 매수(살 때) 스프레드 % 저장
     * @return 저장 성공 여부
     */
    fun setBuySpreadPercent(currency: CurrencyType, percent: Double): Boolean {
        if (percent < 0) return false

        prefs.edit()
            .putFloat(KEY_SPREAD_BUY_PREFIX + currency.name, percent.toFloat())
            .apply()
        return true
    }

    /**
     * 매도(팔 때) 스프레드 % 저장
     * @return 저장 성공 여부
     */
    fun setSellSpreadPercent(currency: CurrencyType, percent: Double): Boolean {
        if (percent < 0) return false

        prefs.edit()
            .putFloat(KEY_SPREAD_SELL_PREFIX + currency.name, percent.toFloat())
            .apply()
        return true
    }

    /**
     * 통화별 스프레드 설정 초기화 (기본값 50/50 복원)
     */
    fun resetSpread(currency: CurrencyType) {
        prefs.edit()
            .remove(KEY_SPREAD_BUY_PREFIX + currency.name)
            .remove(KEY_SPREAD_SELL_PREFIX + currency.name)
            .apply()
    }

    /**
     * 해당 통화에 스프레드가 커스텀 설정되어 있는지 여부
     * (마이페이지 배지의 "적용됨" 상태 표시용)
     */
    fun hasCustomSpread(currency: CurrencyType): Boolean {
        return prefs.contains(KEY_SPREAD_BUY_PREFIX + currency.name) ||
                prefs.contains(KEY_SPREAD_SELL_PREFIX + currency.name)
    }
}