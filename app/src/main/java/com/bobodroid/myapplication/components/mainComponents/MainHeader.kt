package com.bobodroid.myapplication.components.mainComponents

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.bobodroid.myapplication.models.viewmodels.CurrencyHoldingInfo
import java.math.BigDecimal

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainHeader(
    mainUiState: MainUiState,
    adUiState: AdUiState,
    recordUiState: RecordListUiState,
    updateCurrentForeignCurrency: (CurrencyType) -> Unit,
    hideSellRecordState: Boolean,
    onHide:(Boolean) -> Unit,
    isCollapsed: Boolean = false,
    onToggleClick: () -> Unit
) {
    var dropdownExpanded by remember { mutableStateOf(false) }

    val totalProfit = when(mainUiState.selectedCurrencyType) {
        CurrencyType.USD -> recordUiState.foreignCurrencyRecord.dollarState.totalProfit
        CurrencyType.JPY -> recordUiState.foreignCurrencyRecord.yenState.totalProfit
    }

    val profitValue = totalProfit.replace(",", "").toDoubleOrNull() ?: 0.0
    val isProfit = profitValue >= 0
    val displayProfit = if (totalProfit.isEmpty() || totalProfit == "0") "기록 없음" else "₩$totalProfit"
    val hasData = totalProfit.isNotEmpty() && totalProfit != "0"

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        AnimatedContent(
            targetState = isCollapsed,
            transitionSpec = {
                fadeIn(animationSpec = tween(300)) +
                        slideInVertically(
                            animationSpec = tween(300),
                            initialOffsetY = { -it / 2 }
                        ) togetherWith
                        fadeOut(animationSpec = tween(300)) +
                        slideOutVertically(
                            animationSpec = tween(300),
                            targetOffsetY = { -it / 2 }
                        )
            },
            label = "header_animation"
        ) { collapsed ->
            if (collapsed) {
                // 🎯 축소 뷰 - 현재 환율만 표시
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color.White,
                    shadowElevation = 8.dp,
                    tonalElevation = 0.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // 왼쪽: 통화 + 환율
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // 통화 뱃지
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFF6366F1).copy(alpha = 0.1f),
                                modifier = Modifier.clip(RoundedCornerShape(8.dp))
                            ) {
                                Text(
                                    text = when(mainUiState.selectedCurrencyType) {
                                        CurrencyType.USD -> "USD"
                                        CurrencyType.JPY -> "JPY"
                                    },
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF6366F1)
                                )
                            }

                            // 환율
                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(
                                    text = when(mainUiState.selectedCurrencyType) {
                                        CurrencyType.USD -> "${mainUiState.recentRate.usd}원"
                                        CurrencyType.JPY -> "${BigDecimal(mainUiState.recentRate.jpy).times(BigDecimal("100"))}원"
                                    },
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1F2937)
                                )
                                Text(
                                    text = mainUiState.recentRate.createAt,
                                    fontSize = 10.sp,
                                    color = Color(0xFF9CA3AF)
                                )
                            }
                        }

                        // 우측 토글 버튼
                        IconButton(
                            onClick = onToggleClick,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.ExpandMore,
                                contentDescription = "헤더 펼치기",
                                tint = Color(0xFF6366F1)
                            )
                        }
                    }
                }
            } else {
                // 🎨 확장 뷰 - 기존 대시보드 + 선택된 통화 상세
                Column(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White)
                            .padding(horizontal = 16.dp)
                            .padding(top = 16.dp, bottom = 16.dp)
                    ) {
                        // 기존 대시보드 카드
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            elevation = CardDefaults.cardElevation(4.dp),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF6366F1))
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp)
                            ) {
                                // 헤더
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "내 환율 대시보드",
                                        color = Color.White,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold
                                    )

                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        // 통화 선택 드롭다운
                                        Box {
                                            Surface(
                                                shape = RoundedCornerShape(12.dp),
                                                color = Color.White.copy(alpha = 0.2f),
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

                                            DropdownMenu(
                                                expanded = dropdownExpanded,
                                                onDismissRequest = { dropdownExpanded = false }
                                            ) {
                                                DropdownMenuItem(
                                                    text = { Text("USD") },
                                                    onClick = {
                                                        updateCurrentForeignCurrency(CurrencyType.USD)
                                                        dropdownExpanded = false
                                                    }
                                                )
                                                DropdownMenuItem(
                                                    text = { Text("JPY") },
                                                    onClick = {
                                                        updateCurrentForeignCurrency(CurrencyType.JPY)
                                                        dropdownExpanded = false
                                                    }
                                                )
                                            }
                                        }

                                        // 축소 버튼
                                        IconButton(
                                            onClick = onToggleClick,
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Rounded.ExpandLess,
                                                contentDescription = "헤더 접기",
                                                tint = Color.White
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(20.dp))

                                // 현재 환율 & 총 예상수익
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
                                        Spacer(modifier = Modifier.height(6.dp))

                                        if(mainUiState.recentRate.usd == null || mainUiState.recentRate.jpy == null) {
                                            Text(
                                                text = "불러오는 중",
                                                color = Color.White,
                                                fontSize = 20.sp,
                                                fontWeight = FontWeight.Medium
                                            )
                                        } else {
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
                                                        text = "${BigDecimal(mainUiState.recentRate.jpy)}원",
                                                        color = Color.White,
                                                        fontSize = 24.sp,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                            }
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = mainUiState.recentRate.createAt,
                                                color = Color.White.copy(alpha = 0.7f),
                                                fontSize = 11.sp
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(24.dp))
                                    Box(
                                        modifier = Modifier
                                            .width(1.dp)
                                            .height(60.dp)
                                            .background(Color.White.copy(alpha = 0.3f))
                                    )
                                    Spacer(modifier = Modifier.width(24.dp))

                                    // 보유 통화 통계
                                    val currentStats = when(mainUiState.selectedCurrencyType) {
                                        CurrencyType.USD -> mainUiState.holdingStats.dollarStats
                                        CurrencyType.JPY -> mainUiState.holdingStats.yenStats
                                    }

                                    Column(
                                        modifier = Modifier.weight(1f),
                                        horizontalAlignment = Alignment.End
                                    ) {
                                        if (currentStats.hasData) {
                                            Text(
                                                text = "평균 매수가",
                                                color = Color.White.copy(alpha = 0.8f),
                                                fontSize = 13.sp
                                            )
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Text(
                                                text = "₩${currentStats.averageRate}",
                                                color = Color.White,
                                                fontSize = 24.sp,
                                                fontWeight = FontWeight.Bold,
                                                textAlign = TextAlign.End
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Row(
                                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                val profitValue = currentStats.expectedProfit
                                                    .replace("+₩", "").replace("-₩", "").replace("₩", "").replace(",", "")
                                                    .toDoubleOrNull() ?: 0.0
                                                val isProfit = profitValue >= 0

                                                Icon(
                                                    imageVector = if (isProfit) Icons.Rounded.TrendingUp else Icons.Rounded.TrendingDown,
                                                    contentDescription = null,
                                                    tint = Color.White,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                                Text(
                                                    text = currentStats.profitRate,
                                                    color = Color.White.copy(alpha = 0.9f),
                                                    fontSize = 14.sp,
                                                    fontWeight = FontWeight.SemiBold
                                                )
                                            }
                                        } else {
                                            Text(
                                                text = "보유 없음",
                                                color = Color.White.copy(alpha = 0.8f),
                                                fontSize = 18.sp,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // ✅ 추가: 보유 통화 상세 통계
                        Spacer(modifier = Modifier.height(16.dp))

                        val currentStats = when(mainUiState.selectedCurrencyType) {
                            CurrencyType.USD -> mainUiState.holdingStats.dollarStats
                            CurrencyType.JPY -> mainUiState.holdingStats.yenStats
                        }

                        if (currentStats.hasData) {
                            HoldingStatsCard(
                                stats = currentStats,
                                currencyType = mainUiState.selectedCurrencyType
                            )
                        }
                    }

                    // 컨트롤 섹션 삭제 (AnimatedContent 밖으로 이동)
                }
            }
        }

        // 매도기록 보기/숨김 버튼 (항상 표시)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(horizontal = 16.dp)
                .padding(bottom = 12.dp)
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFFF5F5F5),
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
                            Icons.Rounded.VisibilityOff
                        else
                            Icons.Rounded.Visibility,
                        contentDescription = null,
                        tint = Color(0xFF6366F1),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (hideSellRecordState) "매도기록 숨김" else "매도기록 보기",
                        color = Color(0xFF1F2937),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        HorizontalDivider(
            thickness = 1.dp,
            color = Color(0xFFE5E7EB)
        )

        // 광고
        AnimatedVisibility(
            visible = !adUiState.bannerAdState,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(vertical = 8.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                BannerAd()
            }
        }
    }
}

/**
 * 축소 뷰 통화 카드
 */
@Composable
private fun CompactCurrencyCard(
    symbol: String,
    stats: CurrencyHoldingInfo,
    color: Color
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = color.copy(alpha = 0.1f)
        ) {
            Text(
                text = symbol,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = color,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }

        if (stats.hasData) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = stats.currentRate,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1F2937)
                )

                val profitValue = stats.expectedProfit
                    .replace("+₩", "").replace("-₩", "").replace("₩", "").replace(",", "")
                    .toDoubleOrNull() ?: 0.0
                val isProfit = profitValue >= 0

                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (isProfit) Icons.Rounded.TrendingUp else Icons.Rounded.TrendingDown,
                        contentDescription = null,
                        tint = if (isProfit) Color(0xFF10B981) else Color(0xFFEF4444),
                        modifier = Modifier.size(12.dp)
                    )
                    Text(
                        text = stats.profitRate,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isProfit) Color(0xFF10B981) else Color(0xFFEF4444)
                    )
                }
            }
        } else {
            Text(
                text = "보유 없음",
                fontSize = 12.sp,
                color = Color.Gray
            )
        }
    }
}

/**
 * 보유 통화 상세 통계 카드
 */
@Composable
private fun HoldingStatsCard(
    stats: CurrencyHoldingInfo,
    currencyType: CurrencyType
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F9FA)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // 타이틀
            val currencyName = when(currencyType) {
                CurrencyType.USD -> "달러"
                CurrencyType.JPY -> "엔화"
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.AccountBalance,
                    contentDescription = null,
                    tint = Color(0xFF6366F1),
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "보유 중인 $currencyName",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1F2937)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 평균 매수가 vs 현재 환율
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatItem(label = "평균 매수가", value = "₩${stats.averageRate}", color = Color(0xFF6B7280))
                StatItem(label = "현재 환율", value = "₩${stats.currentRate}", color = Color(0xFF1F2937))
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = Color(0xFFE5E7EB))
            Spacer(modifier = Modifier.height(12.dp))

            // 보유량 & 투자금
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatItem(label = "보유량", value = stats.holdingAmount, color = Color(0xFF6B7280))
                StatItem(label = "투자금", value = stats.totalInvestment, color = Color(0xFF6B7280))
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = Color(0xFFE5E7EB))
            Spacer(modifier = Modifier.height(12.dp))

            // 예상 수익 (강조)
            val profitValue = stats.expectedProfit
                .replace("+₩", "").replace("-₩", "").replace("₩", "").replace(",", "")
                .toDoubleOrNull() ?: 0.0
            val isProfit = profitValue >= 0

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "현재 환율 기준 수익",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF6B7280)
                )

                Column(horizontalAlignment = Alignment.End) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isProfit) Icons.Rounded.TrendingUp else Icons.Rounded.TrendingDown,
                            contentDescription = null,
                            tint = if (isProfit) Color(0xFF10B981) else Color(0xFFEF4444),
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = stats.expectedProfit,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isProfit) Color(0xFF10B981) else Color(0xFFEF4444)
                        )
                    }
                    Text(
                        text = stats.profitRate,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (isProfit) Color(0xFF10B981) else Color(0xFFEF4444)
                    )
                }
            }
        }
    }
}

/**
 * 통계 항목
 */
@Composable
private fun StatItem(label: String, value: String, color: Color) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(text = label, fontSize = 12.sp, color = Color(0xFF9CA3AF))
        Text(text = value, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = color)
    }
}