package com.bobodroid.myapplication.components.mainComponents

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import com.bobodroid.myapplication.components.common.CurrencyDropdown
import com.bobodroid.myapplication.models.datamodels.roomDb.CurrencyType
import com.bobodroid.myapplication.models.viewmodels.AdUiState
import com.bobodroid.myapplication.models.viewmodels.MainUiState
import com.bobodroid.myapplication.models.viewmodels.CurrencyHoldingInfo
import java.math.BigDecimal

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainHeader(
    mainUiState: MainUiState,
    adUiState: AdUiState,
    updateCurrentForeignCurrency: (CurrencyType) -> Boolean,  // ✅ Boolean 반환으로 변경
    isPremium: Boolean,  // ✅ 추가
    onPremiumRequired: () -> Unit,  // ✅ 추가
    hideSellRecordState: Boolean,
    onHide:(Boolean) -> Unit,
    isCollapsed: Boolean = false,
    onToggleClick: () -> Unit
) {
    val rate = mainUiState.recentRate.getRateByCode(mainUiState.selectedCurrencyType.name) ?: "0"

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        AnimatedContent(
            targetState = isCollapsed,
            transitionSpec = {
                fadeIn(
                    animationSpec = tween(
                        durationMillis = 900,
                        easing = FastOutSlowInEasing
                    )
                ) + slideInVertically(
                    animationSpec = tween(
                        durationMillis = 900,
                        easing = FastOutSlowInEasing
                    ),
                    initialOffsetY = { -it / 2 }
                ) togetherWith
                        fadeOut(
                            animationSpec = tween(
                                durationMillis = 900,
                                easing = FastOutSlowInEasing
                            )
                        ) + slideOutVertically(
                    animationSpec = tween(
                        durationMillis = 900,
                        easing = FastOutSlowInEasing
                    ),
                    targetOffsetY = { -it / 2 }
                )
            },
            label = "header_animation"
        ) { collapsed ->
            if (collapsed) {
                // 축소 뷰
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
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFF6366F1).copy(alpha = 0.1f),
                                modifier = Modifier.clip(RoundedCornerShape(8.dp))
                            ) {
                                Text(
                                    text = mainUiState.selectedCurrencyType.name,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF6366F1)
                                )
                            }

                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(
                                    text = rate,
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
                // 확장 뷰
                Column(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White)
                            .padding(horizontal = 16.dp)
                            .padding(top = 16.dp, bottom = 16.dp)
                    ) {
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
                                        // ✅ CurrencyDropdown 사용
                                        CurrencyDropdown(
                                            selectedCurrency = mainUiState.selectedCurrencyType,
                                            updateCurrentForeignCurrency = updateCurrentForeignCurrency,
                                            isPremium = isPremium,
                                            onPremiumRequired = onPremiumRequired,
                                            backgroundColor = Color.White.copy(alpha = 0.2f),
                                            contentColor = Color.White
                                        )

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

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
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
                                            Text(
                                                text = "${rate}원",
                                                color = Color.White,
                                                fontSize = 24.sp,
                                                fontWeight = FontWeight.Bold
                                            )
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

                                    val currentStats = mainUiState.holdingStats.getStatsByCode(mainUiState.selectedCurrencyType.code)

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

                        Spacer(modifier = Modifier.height(16.dp))

                        val currentStats = mainUiState.holdingStats.getStatsByCode(mainUiState.selectedCurrencyType.code)

                        if (currentStats.hasData) {
                            HoldingStatsCard(
                                stats = currentStats,
                                currencyType = mainUiState.selectedCurrencyType
                            )
                        }
                    }
                }
            }
        }

        // 매도기록 보기/숨김 버튼
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
                    text = "보유 중인 ${currencyType.name}",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1F2937)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

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