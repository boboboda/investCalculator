package com.bobodroid.myapplication.extensions

import android.icu.util.LocaleData
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.*




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

fun BigDecimal.toBigDecimalUs(): String {
    val us = NumberFormat.getCurrencyInstance(Locale.US)
    us.maximumFractionDigits = 2
    return us.format(this)
}

fun BigDecimal.toBigDecimalYen(): String {
    val won = NumberFormat.getCurrencyInstance(Locale.JAPAN)
    won.maximumFractionDigits = 2
    return won.format(this)
}

fun Float.toWon(): String {
    val won = NumberFormat.getInstance(Locale.KOREA)
    won.maximumFractionDigits = 0
    return won.format(this)
}


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

fun Float.toPer(): String {
//    val format: NumberFormat = NumberFormat.getCurrencyInstance()
    val format: NumberFormat = NumberFormat.getInstance()
    format.maximumFractionDigits = 2
    return format.format(this)
}

fun Float.toRate(): String {
    val rate: NumberFormat = NumberFormat.getInstance()
    rate.maximumFractionDigits = 2
    return rate.format(this)
}

//fun String.toDate(): LocalDate {
//    val c: LocalDate = LocalDate.
//    return c
//}

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

