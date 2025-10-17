package com.bobodroid.myapplication.lists.foreignCurrencyList

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bobodroid.myapplication.extensions.toBigDecimalUs
import com.bobodroid.myapplication.extensions.toBigDecimalWon
import com.bobodroid.myapplication.extensions.toBigDecimalYen
import com.bobodroid.myapplication.models.datamodels.roomDb.CurrencyType
import com.bobodroid.myapplication.models.datamodels.roomDb.ForeignCurrencyRecord
import com.bobodroid.myapplication.screens.RecordListEvent
import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordListRowView(
    currencyType: CurrencyType,
    data: ForeignCurrencyRecord,
    sellState: Boolean = data.recordColor!!,
    groupList: List<String>,
    onEvent: (RecordListEvent) -> Unit,
    scrollEvent: () -> Unit
) {
    val mathContext = MathContext(28, RoundingMode.HALF_UP)
    var dropdownExpanded by remember { mutableStateOf(false) }

    // 통화 금액 포맷팅
    val foreignCurrencyMoney = when(currencyType) {
        CurrencyType.USD -> {
            BigDecimal(data.exchangeMoney, mathContext)
                .setScale(0, RoundingMode.DOWN)
                .toBigDecimalUs()
        }
        CurrencyType.JPY -> {
            BigDecimal(data.exchangeMoney, mathContext)
                .setScale(0, RoundingMode.DOWN)
                .toBigDecimalYen()
        }
    }

    // 수익 계산
    val profit = if (!data.recordColor!!) {
        if (data.profit.isNullOrEmpty()) "0" else data.profit
    } else {
        if (data.sellProfit.isNullOrEmpty()) "0" else data.sellProfit
    }

    val profitValue = BigDecimal(profit, mathContext)
    val isProfit = profitValue.signum() >= 0

    // 수익률 계산
    val profitPercent = if (data.rate != null && data.rate != "0") {
        val rate = BigDecimal(data.rate!!)
        val rateValue = BigDecimal(data.money!!)
        val totalBuyMoney = rate.multiply(rateValue)
        if (totalBuyMoney.signum() != 0) {
            val percent = profitValue.divide(totalBuyMoney, mathContext)
                .multiply(BigDecimal("100"))
                .setScale(1, RoundingMode.HALF_UP)
            "${if (isProfit) "+" else ""}$percent%"
        } else {
            "0.0%"
        }
    } else {
        "0.0%"
    }

    // 날짜
    val displayDate = if (data.recordColor!!) {
        data.sellDate ?: data.date
    } else {
        data.date
    }

    // 통화 심볼
    val currencySymbol = when(currencyType) {
        CurrencyType.USD -> "$"
        CurrencyType.JPY -> "¥"
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        onClick = { /* 카드 클릭 동작 */ }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // 🎨 헤더: 날짜 + 메뉴
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.CalendarToday,
                        contentDescription = null,
                        tint = Color(0xFF6B7280),
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = displayDate ?: "",
                        fontSize = 13.sp,
                        color = Color(0xFF6B7280),
                        fontWeight = FontWeight.Medium
                    )
                }

                IconButton(
                    onClick = { dropdownExpanded = true },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.MoreVert,
                        contentDescription = "메뉴",
                        tint = Color(0xFF9CA3AF)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 🎨 메인 컨텐츠: 금액 정보 + 수익 정보
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 좌측: 매수 정보
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "$currencySymbol$foreignCurrencyMoney",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF6366F1)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "환율: ${data.rate}원",
                        fontSize = 14.sp,
                        color = Color(0xFF6B7280)
                    )

                    // 그룹 태그
                    if (!data.categoryName.isNullOrEmpty() && data.categoryName != "미지정") {
                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFDBEAFE)
                        ) {
                            Text(
                                text = data.categoryName!!,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF3B82F6)
                            )
                        }
                    } else {
                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFF3F4F6)
                        ) {
                            Text(
                                text = "미지정",
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF6B7280)
                            )
                        }
                    }
                }

                // 우측: 수익 정보
                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    // 수익률 배지
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = if (isProfit) Color(0xFFD1FAE5) else Color(0xFFFEE2E2)
                    ) {
                        Text(
                            text = profitPercent,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            color = if (isProfit) Color(0xFF10B981) else Color(0xFFEF4444),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // 수익 금액
                    Text(
                        text = "${if (isProfit) "+" else ""}${profitValue.toBigDecimalWon()}원",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isProfit) Color(0xFF10B981) else Color(0xFFEF4444),
                        textAlign = TextAlign.End
                    )
                }
            }

            // 🎨 메모 영역 (있을 경우만)
            if (!data.memo.isNullOrEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Divider(color = Color(0xFFF3F4F6))
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Edit,
                        contentDescription = null,
                        tint = Color(0xFF6B7280),
                        modifier = Modifier.size(14.dp)
                    )
                    Column {
                        Text(
                            text = "메모",
                            fontSize = 12.sp,
                            color = Color(0xFF6B7280),
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = data.memo!!,
                            fontSize = 13.sp,
                            color = Color(0xFF4B5563),
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        }
    }

    // 드롭다운 메뉴
    DropdownMenu(
        expanded = dropdownExpanded,
        onDismissRequest = { dropdownExpanded = false }
    ) {
        DropdownMenuItem(
            text = { Text("수정") },
            onClick = {
                onEvent(RecordListEvent.ShowEditBottomSheet(data))
                dropdownExpanded = false
            }
        )
        DropdownMenuItem(
            text = { Text("삭제", color = Color(0xFFEF4444)) },
            onClick = {
                onEvent(RecordListEvent.RemoveRecord(data))
                dropdownExpanded = false
            }
        )
        DropdownMenuItem(
            text = { Text("그룹 변경") },
            onClick = {
                // 그룹 변경 다이얼로그
                dropdownExpanded = false
            }
        )
    }
}