package com.bobodroid.myapplication.components.mainComponents

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bobodroid.myapplication.extensions.toBigDecimalWon
import com.bobodroid.myapplication.models.datamodels.roomDb.CurrencyRecord
import com.bobodroid.myapplication.models.datamodels.roomDb.CurrencyType
import com.bobodroid.myapplication.models.datamodels.roomDb.RateDirection
import com.bobodroid.myapplication.models.datamodels.roomDb.TargetRates
import java.math.BigDecimal
import kotlin.math.abs
import kotlin.math.roundToInt

private val HighColor = Color(0xFFEF4444)
private val LowColor = Color(0xFF2563EB)
private val AccentColor = Color(0xFF6366F1)
private val AmberText = Color(0xFFB45309)
private val AmberBg = Color(0xFFFFF1D6)

/**
 * 기록 카드에 표시할 알람 요약 (기록 단위)
 * - 알람 리스트(통화/방향별)에서 recordId가 이 기록과 같은 알람만 집계한다.
 * - 알람 화면에서 등록한 통화 알람(recordId 없음)은 카드에 표시하지 않는다.
 */
data class RecordAlarmSummary(
    val count: Int = 0,
    val nextHigh: Int? = null,   // 가장 먼저 울릴 고점 (가장 낮은 고점)
    val nextLow: Int? = null     // 가장 먼저 울릴 저점 (가장 높은 저점)
) {
    val hasAlarm: Boolean get() = count > 0

    companion object {
        fun of(targetRates: TargetRates, currency: CurrencyType, recordId: String): RecordAlarmSummary {
            val high = targetRates.getRecordRates(currency, RateDirection.HIGH, recordId)
            val low = targetRates.getRecordRates(currency, RateDirection.LOW, recordId)
            return RecordAlarmSummary(
                count = high.size + low.size,
                nextHigh = high.minOfOrNull { it.rate },
                nextLow = low.maxOfOrNull { it.rate }
            )
        }
    }
}

private fun Int.won(): String = "%,d".format(this)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordAlarmBottomSheet(
    sheetState: SheetState,
    record: CurrencyRecord,
    currencyType: CurrencyType,
    currentRate: Double?,          // 현재 기준환율 (스프레드 적용 전)
    sellSpreadWon: Double,         // 예상수익 계산용 매도 스프레드(원)
    existingRates: TargetRates,
    onRegister: (RateDirection, Int) -> Unit,
    onDismiss: () -> Unit
) {
    // 목표환율 초기값 = 이 기록의 매수환율
    val buyRate = remember(record.id) {
        (record.rate ?: record.buyRate)
            ?.replace(",", "")
            ?.toDoubleOrNull()
            ?.roundToInt() ?: 0
    }

    var direction by remember { mutableStateOf(RateDirection.HIGH) }
    var targetText by remember(record.id) {
        mutableStateOf(if (buyRate > 0) buyRate.toString() else "")
    }
    var showWarnDialog by remember { mutableStateOf(false) }

    val target = targetText.toIntOrNull()?.takeIf { it > 0 }
    val isHigh = direction == RateDirection.HIGH
    val directionColor = if (isHigh) HighColor else LowColor
    val directionLabel = if (isHigh) "고점" else "저점"

    val isDuplicate = target != null &&
            existingRates.getRecordRates(currencyType, direction, record.id.toString())
                .any { it.rate == target }

    // 고점: 목표 <= 현재 / 저점: 목표 >= 현재 → 등록 즉시 알림 조건 충족
    val willFireNow = target != null && currentRate != null &&
            (if (isHigh) target <= currentRate else target >= currentRate)

    val currentText = currentRate?.let { "%,.2f".format(it) }
    val gapText = if (target != null && currentRate != null) "%,.2f".format(abs(currentRate - target)) else null

    // 목표환율 도달 시 예상수익 (기록 카드와 같은 공식 + 매도 스프레드 반영)
    val expectedProfit: BigDecimal? = remember(target, sellSpreadWon, record.id) {
        runCatching {
            val currency = record.getCurrency()
            val money = record.money
            val exchangeMoney = record.exchangeMoney
            if (target == null || currency == null || money == null || exchangeMoney == null) {
                null
            } else {
                val calcRate = BigDecimal.valueOf(maxOf(target - sellSpreadWon, 0.0)).toPlainString()
                BigDecimal(currency.calculateExpectedProfit(exchangeMoney, money, calcRate))
            }
        }.getOrNull()
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.White,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 헤더
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "알람 설정",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1F2937)
                    )
                    Text(
                        text = "${currencyType.name} · 매수환율 ${buyRate.won()}원 기준",
                        fontSize = 14.sp,
                        color = Color(0xFF6B7280)
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = "닫기",
                        tint = Color(0xFF6B7280)
                    )
                }
            }

            // 고점 / 저점 선택
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF3F4F6), RoundedCornerShape(14.dp))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                listOf(RateDirection.HIGH to "고점 알람", RateDirection.LOW to "저점 알람")
                    .forEach { (dir, label) ->
                        val selected = dir == direction
                        Surface(
                            onClick = { direction = dir },
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp),
                            shape = RoundedCornerShape(10.dp),
                            color = if (selected) Color.White else Color.Transparent,
                            shadowElevation = if (selected) 2.dp else 0.dp
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = label,
                                    fontSize = 16.sp,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                    color = when {
                                        !selected -> Color(0xFF6B7280)
                                        dir == RateDirection.HIGH -> HighColor
                                        else -> LowColor
                                    }
                                )
                            }
                        }
                    }
            }

            // 목표 환율 입력
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = targetText,
                    onValueChange = { input ->
                        if (input.length <= 7 && input.all { it.isDigit() }) targetText = input
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("목표 환율") },
                    suffix = { Text("원", color = Color(0xFF6B7280)) },
                    textStyle = LocalTextStyle.current.copy(
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = directionColor,
                        focusedLabelColor = directionColor
                    )
                )

                when {
                    isDuplicate -> Text(
                        text = "이미 등록된 $directionLabel 알람이에요",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = HighColor
                    )
                    willFireNow -> Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Warning,
                            contentDescription = null,
                            tint = AmberText,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "현재 ${currentText}원보다 ${gapText}원 ${if (isHigh) "낮아" else "높아"} 바로 울릴 수 있어요",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = AmberText
                        )
                    }
                    currentText != null -> Text(
                        text = "현재 ${currentText}원 · ${if (isHigh) "이 값 이상" else "이 값 이하"}이 되면 알림",
                        fontSize = 13.sp,
                        color = Color(0xFF6B7280)
                    )
                }
            }

            // -5 / -1 / +1 / +5
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(-5, -1, 1, 5).forEach { step ->
                    OutlinedButton(
                        onClick = {
                            val base = target ?: buyRate
                            targetText = (base + step).coerceAtLeast(1).toString()
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.5.dp, Color(0xFFD1D5DB)),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text(
                            text = if (step > 0) "+${step}원" else "${step}원",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (step > 0) HighColor else LowColor
                        )
                    }
                }
            }

            // 도달 시 예상수익
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(AmberBg, RoundedCornerShape(12.dp))
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "이 가격에 도달하면 예상수익",
                    fontSize = 14.sp,
                    color = Color(0xFF92400E)
                )
                Text(
                    text = expectedProfit?.let {
                        (if (it.signum() > 0) "+" else "") + it.toBigDecimalWon()
                    } ?: "-",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF92400E)
                )
            }

            // 등록
            Button(
                onClick = {
                    val value = target ?: return@Button
                    if (willFireNow) showWarnDialog = true else onRegister(direction, value)
                },
                enabled = target != null && !isDuplicate,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AccentColor)
            ) {
                Text(text = "알람 등록", fontSize = 17.sp, fontWeight = FontWeight.Bold)
            }
        }
    }

    // 등록 시 한 번 더 경고
    if (showWarnDialog && target != null) {
        AlertDialog(
            onDismissRequest = { showWarnDialog = false },
            containerColor = Color.White,
            icon = {
                Icon(
                    imageVector = Icons.Rounded.Warning,
                    contentDescription = null,
                    tint = AmberText
                )
            },
            title = {
                Text("등록하면 바로 알림이 울려요", fontWeight = FontWeight.Bold)
            },
            text = {
                Text(
                    "$directionLabel 목표 ${target.won()}원이 현재 환율 ${currentText}원보다 " +
                            "${if (isHigh) "낮아요" else "높아요"}. 의도한 설정이 맞는지 확인해 주세요."
                )
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showWarnDialog = false },
                    border = BorderStroke(1.5.dp, AccentColor)
                ) {
                    Text("목표 수정", color = AccentColor, fontWeight = FontWeight.Bold)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showWarnDialog = false
                        onRegister(direction, target)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentColor)
                ) {
                    Text("그래도 등록", fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}
