package com.bobodroid.myapplication.components.mainComponents

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

    // 수익 여부 판단
    val profitValue = totalProfit.replace(",", "").toDoubleOrNull() ?: 0.0
    val isProfit = profitValue >= 0

    // 값이 없을 때 처리
    val displayProfit = if (totalProfit.isEmpty() || totalProfit == "0") "기록 없음" else "₩$totalProfit"
    val hasData = totalProfit.isNotEmpty() && totalProfit != "0"

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        // 🎨 헤더 섹션 (보라색 배경)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF6366F1))
                .padding(horizontal = 16.dp)
                .padding(bottom = 16.dp)
        ) {
            // 대시보드 카드
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(4.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                ) {
                    // 카드 헤더
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "내 환율 대시보드",
                            color = Color(0xFF1F2937),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )

                        // 통화 선택 버튼
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFF6366F1).copy(alpha = 0.1f),
                            onClick = { dropdownExpanded = true }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = when(mainUiState.selectedCurrencyType) {
                                        CurrencyType.USD -> "USD"
                                        CurrencyType.JPY -> "JPY"
                                    },
                                    color = Color(0xFF6366F1),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = null,
                                    tint = Color(0xFF6366F1),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // 카드 컨텐츠
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // 현재 환율
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "현재 환율",
                                color = Color(0xFF6B7280),
                                fontSize = 13.sp
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            when(mainUiState.selectedCurrencyType) {
                                CurrencyType.USD -> {
                                    Text(
                                        text = "${mainUiState.recentRate.usd}원",
                                        color = Color(0xFF1F2937),
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                CurrencyType.JPY -> {
                                    Text(
                                        text = "${BigDecimal(mainUiState.recentRate.jpy).times(BigDecimal("100"))}원",
                                        color = Color(0xFF1F2937),
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = mainUiState.recentRate.createAt,
                                color = Color(0xFF9CA3AF),
                                fontSize = 11.sp
                            )
                        }

                        // 구분선
                        Spacer(modifier = Modifier.width(24.dp))
                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(60.dp)
                                .background(Color(0xFFE5E7EB))
                        )
                        Spacer(modifier = Modifier.width(24.dp))

                        // 총 수익
                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.End
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.End
                            ) {
                                if (hasData) {
                                    Icon(
                                        imageVector = Icons.Rounded.TrendingUp,
                                        contentDescription = null,
                                        tint = if (isProfit) Color(0xFF10B981) else Color(0xFFEF4444),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                }
                                Text(
                                    text = "총 예상수익",
                                    color = Color(0xFF6B7280),
                                    fontSize = 13.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = displayProfit,
                                color = if (hasData) {
                                    if (isProfit) Color(0xFF10B981) else Color(0xFFEF4444)
                                } else {
                                    Color(0xFF9CA3AF)
                                },
                                fontSize = if (hasData) 24.sp else 18.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.End
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            // 날짜 범위 표시
                            if (hasData &&
                                recordUiState.totalProfitRangeDate.startDate.isNotEmpty() &&
                                recordUiState.totalProfitRangeDate.endDate.isNotEmpty()) {
                                Text(
                                    text = "${recordUiState.totalProfitRangeDate.startDate} ~ ${recordUiState.totalProfitRangeDate.endDate}",
                                    color = Color(0xFF9CA3AF),
                                    fontSize = 11.sp,
                                    textAlign = TextAlign.End
                                )
                            }
                        }
                    }
                }
            }
        }

        // 🎨 컨트롤 섹션 (보라색 배경)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF6366F1))
                .padding(horizontal = 16.dp)
                .padding(bottom = 12.dp)
        ) {
            // 매도기록 토글 버튼
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = Color.White,
                shadowElevation = 2.dp,
                onClick = { onHide(!hideSellRecordState) }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = if (hideSellRecordState)
                            Icons.Rounded.Visibility else Icons.Rounded.VisibilityOff,
                        contentDescription = null,
                        tint = Color(0xFF6366F1),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (hideSellRecordState) "매도기록 보기" else "매도기록 숨기기",
                        fontSize = 13.sp,
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

        // 새로고침 시간 안내
        if (recordUiState.refreshDate.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "💡 새로고침: ${recordUiState.refreshDate}",
                    fontSize = 11.sp,
                    color = Color(0xFF6B7280)
                )
            }
        }

        // 광고 배치 (조건부)
        if(!adUiState.bannerAdState) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(top = 8.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                BannerAd()
            }
        }
    }
}