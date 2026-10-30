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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bobodroid.myapplication.extensions.toLongWon
import com.bobodroid.myapplication.models.datamodels.roomDb.Trip
import com.bobodroid.myapplication.models.viewmodels.ExpenseDraft
import com.bobodroid.myapplication.models.viewmodels.TravelEvent
import com.bobodroid.myapplication.ui.theme.primaryColor

val TRAVEL_EXPENSE_CATEGORIES = listOf("식비", "교통", "쇼핑", "기타")

/**
 * 여행모드 - 지출 추가/수정 바텀시트
 * 순서: 카테고리 칩 -> 메모 -> 금액(숫자패드) -> 날짜 -> 취소/저장
 * (메모를 금액/키패드보다 앞에 두는 건 확정된 배치 - 입력 흐름이 숫자패드로 끝나도록)
 */
@Composable
fun ExpenseAddBottomSheet(
    trip: Trip,
    draft: ExpenseDraft,
    isEditing: Boolean,
    onEvent: (TravelEvent) -> Unit,
    onCancel: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(horizontal = 20.dp)
            .padding(bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Box(
            modifier = Modifier
                .padding(top = 6.dp)
                .size(width = 38.dp, height = 4.dp)
                .background(Color(0xFFECEAF5), RoundedCornerShape(2.dp))
        )

        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                if (isEditing) "지출 수정" else "지출 추가",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1F2430)
            )
            Text(
                trip.tripName,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = primaryColor,
                modifier = Modifier
                    .background(Color(0xFFF1EEFE), RoundedCornerShape(8.dp))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            TRAVEL_EXPENSE_CATEGORIES.forEach { category ->
                val selected = draft.categoryName == category
                Text(
                    text = category,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (selected) Color.White else Color(0xFF6B7280),
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .weight(1f)
                        .background(
                            if (selected) primaryColor else Color(0xFFF7F6FE),
                            RoundedCornerShape(11.dp)
                        )
                        .clickable { onEvent(TravelEvent.ExpenseDraftEvent.CategorySelected(category)) }
                        .padding(vertical = 10.dp)
                )
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("메모", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF8B93A3))
            OutlinedTextField(
                value = draft.memo,
                onValueChange = { onEvent(TravelEvent.ExpenseDraftEvent.MemoChanged(it)) },
                placeholder = { Text("예: 스시 오마카세") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                "금액 (" + trip.currencyCode + ")",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF8B93A3)
            )
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.5.dp, primaryColor, RoundedCornerShape(12.dp))
                    .background(Color(0xFFFAFAFF), RoundedCornerShape(12.dp))
                    .padding(horizontal = 14.dp, vertical = 14.dp)
            ) {
                Text(
                    draft.amount.ifBlank { "0" },
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1F2430)
                )
                Text(trip.currencyCode, fontSize = 13.sp, color = Color(0xFF8B93A3))
            }

            val estimatedWon = draft.amount.toDoubleOrNull()?.let {
                trip.convertToWon(draft.amount).toDoubleOrNull()?.toLong()
            }

            if (estimatedWon != null) {
                Text(
                    "≈ " + estimatedWon.toLongWon() + "원 (내 평균 환전환율 기준)",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = primaryColor,
                    textAlign = TextAlign.End,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        TravelNumberPad(
            decimalKey = true,
            onKeyPress = { key -> onEvent(TravelEvent.ExpenseDraftEvent.AmountChanged(draft.amount.appendNumberKey(key))) },
            onBackspace = { onEvent(TravelEvent.ExpenseDraftEvent.AmountChanged(draft.amount.removeLastDigit())) }
        )

        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 2.dp, vertical = 4.dp)
        ) {
            Text("날짜", fontSize = 13.sp, color = Color(0xFF8B93A3))
            Text(
                draft.date,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF1F2430)
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            OutlinedButton(
                onClick = onCancel,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp)
            ) {
                Text("취소", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF6B7280))
            }
            Button(
                onClick = { onEvent(TravelEvent.ExpenseDraftEvent.ConfirmClicked) },
                enabled = draft.isValid,
                colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .weight(2f)
                    .height(50.dp)
            ) {
                Text("저장", fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
