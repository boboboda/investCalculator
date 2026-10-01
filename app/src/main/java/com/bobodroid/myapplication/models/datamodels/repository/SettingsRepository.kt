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
import kotlin.math.roundToLong

/**
 * 앱 전역 설정 관리
 * - 선택된 통화
 * - 통화별 매수/매도 스프레드 (원 단위)
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

        // ✅ 스프레드(원) 설정 키 prefix
        //   기존 % 값(spread_buy_, spread_sell_)과 키를 분리해서 옛 값이 원 단위로 읽히지 않게 함
        private const val KEY_SPREAD_BUY_PREFIX = "spread_won_buy_"
        private const val KEY_SPREAD_SELL_PREFIX = "spread_won_sell_"

        // ✅ 서버 스키마 기본값과 동일 (0원 = 스프레드 미반영)
        private const val DEFAULT_BUY_SPREAD_WON = 0.0
        private const val DEFAULT_SELL_SPREAD_WON = 0.0
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

    // ==================== 통화별 매수/매도 스프레드 설정 (원 단위) ====================
    //   JPY, THB는 앱에 표시되는 100단위 기준 환율에 더해지는 원 값
    //   Float 오차(0.1 → 0.10000000149)를 피하려고 문자열로 저장

    /**
     * 매수(살 때) 스프레드(원) 조회
     */
    fun getBuySpreadWon(currency: CurrencyType): Double {
        return prefs.getString(KEY_SPREAD_BUY_PREFIX + currency.name, null)
            ?.toDoubleOrNull() ?: DEFAULT_BUY_SPREAD_WON
    }

    /**
     * 매도(팔 때) 스프레드(원) 조회
     */
    fun getSellSpreadWon(currency: CurrencyType): Double {
        return prefs.getString(KEY_SPREAD_SELL_PREFIX + currency.name, null)
            ?.toDoubleOrNull() ?: DEFAULT_SELL_SPREAD_WON
    }

    /**
     * 매수(살 때) 스프레드(원) 저장 (소수 둘째 자리까지)
     * @return 저장 성공 여부
     */
    fun setBuySpreadWon(currency: CurrencyType, won: Double): Boolean {
        if (won < 0) return false

        prefs.edit()
            .putString(KEY_SPREAD_BUY_PREFIX + currency.name, won.roundTo2().toString())
            .apply()
        return true
    }

    /**
     * 매도(팔 때) 스프레드(원) 저장 (소수 둘째 자리까지)
     * @return 저장 성공 여부
     */
    fun setSellSpreadWon(currency: CurrencyType, won: Double): Boolean {
        if (won < 0) return false

        prefs.edit()
            .putString(KEY_SPREAD_SELL_PREFIX + currency.name, won.roundTo2().toString())
            .apply()
        return true
    }

    /**
     * 통화별 스프레드 설정 초기화 (0원 = 미반영 복원)
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

    private fun Double.roundTo2(): Double = (this * 100).roundToLong() / 100.0
}
