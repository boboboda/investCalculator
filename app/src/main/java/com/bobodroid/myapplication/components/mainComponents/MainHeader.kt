package com.bobodroid.myapplication.components.mainComponents

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.rounded.TrendingUp
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bobodroid.myapplication.R
import com.bobodroid.myapplication.components.admobs.BannerAd
import com.bobodroid.myapplication.models.datamodels.roomDb.CurrencyType
import com.bobodroid.myapplication.models.viewmodels.AdUiState
import com.bobodroid.myapplication.models.viewmodels.MainUiState
import com.bobodroid.myapplication.models.viewmodels.RecordListUiState
import java.math.BigDecimal

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainHeader(
    mainUiState: MainUiState,
    adUiState: AdUiState,
    recordUiState: RecordListUiState,
    updateCurrentForeignCurrency: (CurrencyType) -> Unit,
    hideSellRecordState: Boolean,
    onHide:(Boolean) -> Unit
) {
    var dropdownExpanded by remember { mutableStateOf(false) }

    val totalProfit = when(mainUiState.selectedCurrencyType) {
        CurrencyType.USD -> recordUiState.foreignCurrencyRecord.dollarState.totalProfit
        CurrencyType.JPY -> recordUiState.foreignCurrencyRecord.yenState.totalProfit
    }

    val displayProfit = if (totalProfit.isEmpty() || totalProfit == "0") "기록 없음" else "₩$totalProfit"
    val hasData = totalProfit.isNotEmpty() && totalProfit != "0"

    // 수익 여부 판단
    val profitValue = totalProfit.replace(",", "").toDoubleOrNull() ?: 0.0
    val isProfit = profitValue >= 0

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF8F9FA))
    ) {
        // 🎨 1. 그라데이션 대시보드 카드
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            elevation = CardDefaults.cardElevation(8.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                Color(0xFF6366F1),
                                Color(0xFF8B5CF6)
                            )
                        )
                    )
                    .padding(20.dp)
            ) {
                Column {
                    // 상단: 통화 선택
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "내 환율 대시보드",
                            color = Color.White.copy(alpha = 0.9f),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )

                        // 통화 선택 버튼
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color.White.copy(alpha = 0.2f),
                            onClick = { dropdownExpanded = true }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = when(mainUiState.selectedCurrencyType) {
                                        CurrencyType.USD -> "USD"
                                        CurrencyType.JPY -> "JPY"
                                    },
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // 중앙: 현재 환율과 총 수익
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // 현재 환율
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "현재 환율",
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 13.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            when(mainUiState.selectedCurrencyType) {
                                CurrencyType.USD -> {
                                    Text(
                                        text = "${mainUiState.recentRate.usd}원",
                                        color = Color.White,
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                CurrencyType.JPY -> {
                                    Text(
                                        text = "${BigDecimal(mainUiState.recentRate.jpy).times(BigDecimal("100"))}원",
                                        color = Color.White,
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            Text(
                                text = mainUiState.recentRate.createAt,
                                color = Color.White.copy(alpha = 0.6f),
                                fontSize = 11.sp
                            )
                        }

                        // 구분선
                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(60.dp)
                                .background(Color.White.copy(alpha = 0.3f))
                        )

                        // 총 수익
                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.End
                        )
                        {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (hasData) {
                                    Icon(
                                        imageVector = Icons.Rounded.TrendingUp,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                }
                                Text(
                                    text = "총 예상수익",
                                    color = Color.White.copy(alpha = 0.8f),
                                    fontSize = 13.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = displayProfit,
                                color = Color.White,
                                fontSize = if (hasData) 24.sp else 18.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.End
                            )
                            // 날짜 범위 표시 (데이터가 있고 날짜가 비어있지 않을 때만)
                            if (hasData &&
                                recordUiState.totalProfitRangeDate.startDate.isNotEmpty() &&
                                recordUiState.totalProfitRangeDate.endDate.isNotEmpty()) {
                                Text(
                                    text = "${recordUiState.totalProfitRangeDate.startDate} ~ ${recordUiState.totalProfitRangeDate.endDate}",
                                    color = Color.White.copy(alpha = 0.6f),
                                    fontSize = 11.sp,
                                    textAlign = TextAlign.End
                                )
                            }

                    }
                }
            }
        }
        }

        // 🎨 2. 컨트롤 버튼들
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        )
        {
            // 매도기록 토글 버튼
            Surface(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                color = Color.White,
                shadowElevation = 2.dp,
                onClick = { onHide(!hideSellRecordState) }
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = if (hideSellRecordState)
                            Icons.Rounded.Visibility else Icons.Rounded.VisibilityOff,
                        contentDescription = null,
                        tint = Color(0xFF6366F1),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (hideSellRecordState) "매도기록 보기" else "매도기록 숨기기",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF1F2937)
                    )
                }
            }
        }

        // 드롭다운 메뉴
        DropdownMenu(
            expanded = dropdownExpanded,
            onDismissRequest = { dropdownExpanded = false }
        ) {
            DropdownMenuItem(
                text = { Text("달러(USD)") },
                onClick = {
                    updateCurrentForeignCurrency(CurrencyType.USD)
                    dropdownExpanded = false
                }
            )
            DropdownMenuItem(
                text = { Text("엔화(JPY)") },
                onClick = {
                    updateCurrentForeignCurrency(CurrencyType.JPY)
                    dropdownExpanded = false
                }
            )
        }

        // 🚫 광고는 하단으로 이동 (여기선 제거)

        // 새로고침 시간 안내
        if (recordUiState.refreshDate.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "💡 새로고침: ${recordUiState.refreshDate}",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
        }

        // 광고 배치 (조건부)
        if(!adUiState.bannerAdState) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                BannerAd()
            }
        }
    }
}