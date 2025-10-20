package com.bobodroid.myapplication.extensions

import android.icu.util.LocaleData
import com.bobodroid.myapplication.models.datamodels.roomDb.Currency
import com.bobodroid.myapplication.models.datamodels.roomDb.Currencies
import com.bobodroid.myapplication.models.datamodels.roomDb.CurrencyType
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.*


// ==================== 원화 포맷팅 ====================

fun Long.toLongWon(): String {
    val won = NumberFormat.getInstance(Locale.KOREA)
    won.maximumFractionDigits = 0
    return won.format(this)
}

fun BigDecimal.toBigDecimalWon(): String {
    val won = NumberFormat.getInstance(Locale.KOREA)
    won.maximumFractionDigits = 0
    return won.format(this)
}

fun Float.toWon(): String {
    val won = NumberFormat.getInstance(Locale.KOREA)
    won.maximumFractionDigits = 0
    return won.format(this)
}

fun BigDecimal.toWon(): String {
    val formatter = DecimalFormat("#,##0")
    formatter.roundingMode = RoundingMode.DOWN
    return "₩${formatter.format(this)}"
}

fun String.toWon(): String {
    return (this.toBigDecimalOrNull() ?: BigDecimal.ZERO).toWon()
}


// ==================== 레거시 USD/JPY 포맷팅 (하위 호환) ====================

fun Int.toUs(): String {
    val us = NumberFormat.getCurrencyInstance(Locale.US)
    us.maximumFractionDigits = 0
    return us.format(this)
}

fun Float.toDecUs(): String {
    val us = NumberFormat.getCurrencyInstance(Locale.US)
    us.maximumFractionDigits = 3
    return us.format(this)
}

fun Long.toLongUs(): String {
    val us = NumberFormat.getCurrencyInstance(Locale.US)
    us.maximumFractionDigits = 0
    return us.format(this)
}

fun Float.toYen(): String {
    val yen = NumberFormat.getCurrencyInstance(Locale.JAPAN)
    yen.maximumFractionDigits = 2
    return yen.format(this)
}

@Deprecated("Use formatAsCurrency(Currencies.USD) instead", ReplaceWith("formatAsCurrency(Currencies.USD)"))
fun BigDecimal.toBigDecimalUs(): String {
    return this.formatAsCurrency(Currencies.USD)
}

@Deprecated("Use formatAsCurrency(Currencies.JPY) instead", ReplaceWith("formatAsCurrency(Currencies.JPY)"))
fun BigDecimal.toBigDecimalYen(): String {
    return this.formatAsCurrency(Currencies.JPY)
}


// ==================== 새로운 통합 통화 포맷팅 (12개 통화 지원) ====================

/**
 * Currency 객체 기반 포맷팅 (추천 ⭐)
 *
 * 사용 예시:
 * BigDecimal("1234.56").formatAsCurrency(Currencies.USD) → "$1,234.56"
 * BigDecimal("1234.56").formatAsCurrency(Currencies.JPY) → "¥1,234.56"
 * BigDecimal("1234.56").formatAsCurrency(Currencies.EUR) → "€1,234.56"
 */
fun BigDecimal.formatAsCurrency(currency: Currency): String {
    val pattern = if (currency.scale == 0) {
        "#,##0"  // 소수점 없음
    } else {
        "#,##0.${"0".repeat(currency.scale)}"  // 소수점 자리수만큼
    }

    val formatter = DecimalFormat(pattern)
    formatter.roundingMode = RoundingMode.DOWN

    val formattedNumber = formatter.format(this.setScale(currency.scale, RoundingMode.DOWN))
    return "${currency.symbol}$formattedNumber"
}

/**
 * CurrencyType 기반 포맷팅 (기존 코드 호환)
 *
 * 사용 예시:
 * BigDecimal("1234.56").formatWithCurrencyType(CurrencyType.USD) → "$1,234.56"
 */
fun BigDecimal.formatWithCurrencyType(currencyType: CurrencyType): String {
    val currency = Currencies.fromCurrencyType(currencyType)
    return this.formatAsCurrency(currency)
}

/**
 * String을 BigDecimal로 변환 후 포맷팅
 *
 * 사용 예시:
 * "1234.56".formatAsCurrency(Currencies.USD) → "$1,234.56"
 */
fun String.formatAsCurrency(currency: Currency): String {
    return (this.toBigDecimalOrNull() ?: BigDecimal.ZERO).formatAsCurrency(currency)
}

fun String.formatWithCurrencyType(currencyType: CurrencyType): String {
    val currency = Currencies.fromCurrencyType(currencyType)
    return this.formatAsCurrency(currency)
}

/**
 * 심볼 없이 숫자만 포맷팅
 *
 * 사용 예시:
 * BigDecimal("1234.56").formatNumber(2) → "1,234.56"
 */
fun BigDecimal.formatNumber(scale: Int): String {
    val pattern = if (scale == 0) {
        "#,##0"
    } else {
        "#,##0.${"0".repeat(scale)}"
    }

    val formatter = DecimalFormat(pattern)
    formatter.roundingMode = RoundingMode.DOWN

    return formatter.format(this.setScale(scale, RoundingMode.DOWN))
}


// ==================== 기타 포맷팅 ====================

fun Float.toPer(): String {
    val format: NumberFormat = NumberFormat.getInstance()
    format.maximumFractionDigits = 2
    return format.format(this)
}

fun Float.toRate(): String {
    val rate: NumberFormat = NumberFormat.getInstance()
    rate.maximumFractionDigits = 2
    return rate.format(this)
}


// ==================== 날짜 포맷팅 ====================

fun String.toDate(): String {
    val dateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    return dateFormatter.format(dateFormatter.parse(this))
}

fun String.toDateTime(): String {
    val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS", Locale.getDefault())
    val outputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    val date = inputFormat.parse(this) // 원본 날짜 파싱
    return outputFormat.format(date) // 변경된 포맷으로 출력
}

fun String.toLocalDate(): LocalDate {
    val date = LocalDate.parse(this)
    return LocalDate.of(date.year, date.month, date.dayOfMonth)
}



/**
 * String을 통화 형식으로 포맷팅 (심볼 없이 숫자만)
 *
 * 사용 예시:
 * "1541.213132131313123".formatCurrencyNumber(Currencies.USD) → "1,541.21"
 * "1541.213132131313123".formatCurrencyNumber(Currencies.JPY) → "1,541.21"
 */
fun String.formatCurrencyNumber(currency: Currency): String {
    val bigDecimal = this.toBigDecimalOrNull() ?: BigDecimal.ZERO
    return bigDecimal.formatNumber(currency.scale)
}

fun String.formatCurrencyNumber(currencyType: CurrencyType): String {
    val currency = Currencies.fromCurrencyType(currencyType)
    return this.formatCurrencyNumber(currency)
}