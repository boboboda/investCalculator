package com.bobodroid.myapplication.components.travelComponents

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bobodroid.myapplication.components.MyDatePickerDialog
import com.bobodroid.myapplication.models.datamodels.roomDb.CurrencyType
import com.bobodroid.myapplication.models.viewmodels.TravelEvent
import com.bobodroid.myapplication.models.viewmodels.TripCreateDraft
import com.bobodroid.myapplication.ui.theme.primaryColor
import java.time.LocalDate

private enum class ActiveNumericField { BUDGET, RATE }

/**
 * 여행모드 - 새 여행 만들기 2단계 바텀시트
 * 1단계: 이름 / 통화 / 기간 -> "다음"
 * 2단계: 예산 / 내 평균 환전환율 -> "여행 시작"
 */
@Composable
fun TripCreateBottomSheet(
    step: Int,
    draft: TripCreateDraft,
    onEvent: (TravelEvent) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(horizontal = 20.dp)
            .padding(bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(
            modifier = Modifier
                .padding(top = 6.dp)
                .size(width = 38.dp, height = 4.dp)
                .background(Color(0xFFECEAF5), RoundedCornerShape(2.dp))
        )

        if (step == 1) {
            TripCreateStep1(draft = draft, onEvent = onEvent)
        } else {
            TripCreateStep2(draft = draft, onEvent = onEvent)
        }
    }
}

@Composable
private fun StepHeader(
    title: String = "새 여행 만들기",
    currentStep: Int,
    onBack: (() -> Unit)? = null
) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        if (onBack != null) {
            IconButton(onClick = onBack, modifier = Modifier.size(24.dp)) {
                Icon(
                    imageVector = Icons.Rounded.ArrowBack,
                    contentDescription = "이전",
                    tint = Color(0xFF1F2430)
                )
            }
        }
        Text(
            text = title,
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1F2430),
            modifier = Modifier.weight(1f)
        )
        Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
            repeat(2) { index ->
                Box(
                    modifier = Modifier
                        .size(width = 16.dp, height = 4.dp)
                        .background(
                            if (index == currentStep - 1) primaryColor else Color(0xFFECEAF5),
                            RoundedCornerShape(2.dp)
                        )
                )
            }
        }
    }
}

@Composable
private fun TripCreateStep1(
    draft: TripCreateDraft,
    onEvent: (TravelEvent) -> Unit
) {
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }

    StepHeader(currentStep = 1)

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text("여행 이름", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF8B93A3))
        OutlinedTextField(
            value = draft.tripName,
            onValueChange = { onEvent(TravelEvent.CreateStep1Event.NameChanged(it)) },
            placeholder = { Text("예: 도쿄 여행") },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        )
    }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("통화 선택", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF8B93A3))
        CurrencyChipRow(
            selected = draft.currencyCode,
            onSelect = { code -> onEvent(TravelEvent.CreateStep1Event.CurrencyChanged(code)) }
        )
        Text(
            "여행 중 통화 변경은 지원되지 않아요. 다른 통화가 필요하면 여행을 새로 만들어요.",
            fontSize = 11.sp,
            color = Color(0xFFB7BECB)
        )
    }

    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("시작일", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF8B93A3))
            DateBox(
                dateText = draft.startDate.ifBlank { "선택" },
                onClick = { showStartDatePicker = true }
            )
        }
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("종료일", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF8B93A3))
            DateBox(
                dateText = draft.endDate.ifBlank { "선택" },
                onClick = { showEndDatePicker = true }
            )
        }
    }

    Button(
        onClick = { onEvent(TravelEvent.CreateStep1Event.NextClicked) },
        enabled = draft.isStep1Valid,
        colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
    ) {
        Text("다음", fontSize = 15.sp, fontWeight = FontWeight.Bold)
    }

    if (showStartDatePicker) {
        MyDatePickerDialog(
            selectedDate = draft.startDate.toLocalDateOrToday(),
            onDateSelected = {
                onEvent(TravelEvent.CreateStep1Event.StartDateChanged(it.toString()))
                showStartDatePicker = false
            },
            onDismissRequest = { showStartDatePicker = false }
        )
    }

    if (showEndDatePicker) {
        MyDatePickerDialog(
            selectedDate = draft.endDate.toLocalDateOrToday(),
            onDateSelected = {
                onEvent(TravelEvent.CreateStep1Event.EndDateChanged(it.toString()))
                showEndDatePicker = false
            },
            onDismissRequest = { showEndDatePicker = false }
        )
    }
}

@Composable
private fun TripCreateStep2(
    draft: TripCreateDraft,
    onEvent: (TravelEvent) -> Unit
) {
    var activeField by remember { mutableStateOf(ActiveNumericField.BUDGET) }

    StepHeader(currentStep = 2, onBack = { onEvent(TravelEvent.CreateStep2Event.BackClicked) })

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF7F6FE), RoundedCornerShape(12.dp))
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        CurrencyTag(code = draft.currencyCode)
        Text(
            draft.tripName,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1F2430),
            modifier = Modifier.weight(1f)
        )
        Text(
            draft.startDate + " ~ " + draft.endDate,
            fontSize = 12.sp,
            color = Color(0xFF8B93A3)
        )
    }

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text("예산 (원, 선택)", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF8B93A3))
        NumericFieldBox(
            value = draft.budgetWon,
            unit = "원",
            active = activeField == ActiveNumericField.BUDGET,
            onClick = { activeField = ActiveNumericField.BUDGET }
        )
    }

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            "내 평균 환전환율 (100" + draft.currencyCode + " 기준)",
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF8B93A3)
        )
        NumericFieldBox(
            value = draft.averageExchangeRate,
            unit = "원",
            active = activeField == ActiveNumericField.RATE,
            onClick = { activeField = ActiveNumericField.RATE }
        )
        Text(
            "실제 환전 영수증 기준으로 입력하면 지출이 정확한 원화로 계산돼요. (탭하면 숫자패드가 이 입력으로 전환)",
            fontSize = 11.sp,
            color = Color(0xFFB7BECB)
        )
    }

    TravelNumberPad(
        decimalKey = activeField == ActiveNumericField.RATE,
        onKeyPress = { key ->
            when (activeField) {
                ActiveNumericField.BUDGET -> onEvent(
                    TravelEvent.CreateStep2Event.BudgetChanged(draft.budgetWon.appendNumberKey(key))
                )
                ActiveNumericField.RATE -> onEvent(
                    TravelEvent.CreateStep2Event.AverageRateChanged(draft.averageExchangeRate.appendNumberKey(key))
                )
            }
        },
        onBackspace = {
            when (activeField) {
                ActiveNumericField.BUDGET -> onEvent(
                    TravelEvent.CreateStep2Event.BudgetChanged(draft.budgetWon.removeLastDigit())
                )
                ActiveNumericField.RATE -> onEvent(
                    TravelEvent.CreateStep2Event.AverageRateChanged(draft.averageExchangeRate.removeLastDigit())
                )
            }
        }
    )

    Button(
        onClick = { onEvent(TravelEvent.CreateStep2Event.StartTripClicked) },
        enabled = draft.isStep2Valid,
        colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
    ) {
        Text("여행 시작", fontSize = 15.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun DateBox(dateText: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFFECEAF5), RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 13.dp)
    ) {
        Text(dateText, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF1F2430))
    }
}

@Composable
private fun NumericFieldBox(value: String, unit: String, active: Boolean, onClick: () -> Unit) {
    Row(
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = if (active) 1.5.dp else 1.dp,
                color = if (active) primaryColor else Color(0xFFECEAF5),
                shape = RoundedCornerShape(12.dp)
            )
            .background(if (active) Color(0xFFFAFAFF) else Color.White, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 13.dp)
    ) {
        Text(
            value.ifBlank { "0" },
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1F2430)
        )
        Text(unit, fontSize = 12.sp, color = Color(0xFF8B93A3))
    }
}

@Composable
private fun CurrencyTag(code: String) {
    Text(
        code,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        color = primaryColor,
        modifier = Modifier
            .background(Color(0xFFE7E2FD), RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CurrencyChipRow(selected: String, onSelect: (String) -> Unit) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        CurrencyType.values().forEach { currency ->
            val isSelected = currency.code == selected
            Text(
                text = currency.code + " " + currency.koreanName,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) Color.White else Color(0xFF6B7280),
                modifier = Modifier
                    .background(
                        if (isSelected) primaryColor else Color(0xFFF7F6FE),
                        RoundedCornerShape(11.dp)
                    )
                    .clickable { onSelect(currency.code) }
                    .padding(horizontal = 14.dp, vertical = 9.dp)
            )
        }
    }
}

private fun String.toLocalDateOrToday(): LocalDate =
    try {
        LocalDate.parse(this)
    } catch (e: Exception) {
        LocalDate.now()
    }
