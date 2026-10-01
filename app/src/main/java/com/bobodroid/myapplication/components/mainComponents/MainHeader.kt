package com.bobodroid.myapplication.components.mainComponents

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import com.bobodroid.myapplication.components.common.CurrencyDropdown
import com.bobodroid.myapplication.models.datamodels.roomDb.CurrencyType
import com.bobodroid.myapplication.models.viewmodels.AdUiState
import com.bobodroid.myapplication.models.viewmodels.MainUiState
import com.bobodroid.myapplication.models.viewmodels.CurrencyHoldingInfo

/**
 * 상단 헤더 (접힘 형태 고정, 환율 전용)
 * - 매도기록 표시 여부는 여기서 완전히 제거됨 (RecordListView 상단바로 이전)
 */
@Composable
fun MainHeader(
    mainUiState: MainUiState,
    adUiState: AdUiState,
    onExpandClick: () -> Unit,
    onSpreadBadgeClick: () -> Unit = {},
    onNavigateToBacktest: () -> Unit = {},   // ✅ 신규 추가
) {
    val rate = mainUiState.recentRate.getRateByCode(mainUiState.selectedCurrencyType.name) ?: "0"

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color.White,
            shadowElevation = 8.dp,
            tonalElevation = 0.dp,
            onClick = onExpandClick
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

                    Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = rate,
                                fontSize = 18.sp,
                                lineHeight = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1F2937)
                            )

                            SpreadStatusPill(
                                text = mainUiState.spreadBadgeText,
                                applied = mainUiState.spreadBadgeApplied,
                                onClick = onSpreadBadgeClick
                            )
                        }
                        Text(
                            text = mainUiState.recentRate.createAt,
                            fontSize = 10.sp,
                            lineHeight = 10.sp,
                            color = Color(0xFF9CA3AF)
                        )
                    }
                }

                IconButton(
                    onClick = onExpandClick,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.ExpandMore,
                        contentDescription = "환율 상세 보기",
                        tint = Color(0xFF6366F1)
                    )
                }
            }
        }

        HorizontalDivider(
            thickness = 1.dp,
            color = Color(0xFFE5E7EB)
        )

        // ✅ 환테크 시뮬레이터 배너 ↔ 광고를 같은 자리에서 교대로 노출
        // (진입 시 시뮬레이터 배너 먼저 5초 → 광고로 전환 25초 → 이후 반복, 오른쪽 아래 버튼으로 수동 전환 가능)
        RotatingBannerAdSlot(
            onNavigateToBacktest = onNavigateToBacktest,
            adVisible = adUiState.bannerAdState
        )
    }
}

/**
 * 바텀시트에 들어가는 상세 대시보드 콘텐츠
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainDashboardBottomSheetContent(
    mainUiState: MainUiState,
    updateCurrentForeignCurrency: (CurrencyType) -> Boolean,
    isPremium: Boolean,
    onPremiumRequired: () -> Unit,
    onSpreadBadgeClick: () -> Unit,
) {
    val rate = mainUiState.recentRate.getRateByCode(mainUiState.selectedCurrencyType.name) ?: "0"

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
            .navigationBarsPadding()
            .padding(bottom = 16.dp)
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
                    .padding(18.dp)
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

                    CurrencyDropdown(
                        selectedCurrency = mainUiState.selectedCurrencyType,
                        updateCurrentForeignCurrency = updateCurrentForeignCurrency,
                        isPremium = isPremium,
                        onPremiumRequired = onPremiumRequired,
                        backgroundColor = Color.White.copy(alpha = 0.2f),
                        contentColor = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

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
                        Spacer(modifier = Modifier.height(4.dp))

                        if (mainUiState.recentRate.usd == null || mainUiState.recentRate.jpy == null) {
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
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = mainUiState.recentRate.createAt,
                                color = Color.White.copy(alpha = 0.7f),
                                fontSize = 11.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            SpreadStatusPill(
                                text = mainUiState.spreadBadgeText,
                                applied = mainUiState.spreadBadgeApplied,
                                onClick = onSpreadBadgeClick,
                                onDark = true
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(16.dp))
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(56.dp)
                            .background(Color.White.copy(alpha = 0.3f))
                    )
                    Spacer(modifier = Modifier.width(16.dp))

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
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "₩${currentStats.averageRate}",
                                color = Color.White,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.End
                            )
                            Spacer(modifier = Modifier.height(2.dp))
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

                if (mainUiState.spreadBadgeApplied) {
                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = Color.White.copy(alpha = 0.22f))
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "매수(살 때)",
                                color = Color.White.copy(alpha = 0.7f),
                                fontSize = 11.sp
                            )
                            Text(
                                text = "+%.2f원".format(mainUiState.spreadBuyWon),
                                color = Color.White.copy(alpha = 0.9f),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Text(
                            text = "₩${mainUiState.spreadBuyRate}",
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "매도(팔 때)",
                                color = Color.White.copy(alpha = 0.7f),
                                fontSize = 11.sp
                            )
                            Text(
                                text = "-%.2f원".format(mainUiState.spreadSellWon),
                                color = Color.White.copy(alpha = 0.9f),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Text(
                            text = "₩${mainUiState.spreadSellRate}",
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp)),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = Color.Transparent,
                            onClick = onSpreadBadgeClick
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                modifier = Modifier.padding(vertical = 4.dp)
                            ) {
                                Text(
                                    text = "스프레드 설정으로 이동",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Icon(
                                    imageVector = Icons.Rounded.ChevronRight,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        val currentStats = mainUiState.holdingStats.getStatsByCode(mainUiState.selectedCurrencyType.code)

        if (currentStats.hasData) {
            HoldingStatsCard(
                stats = currentStats,
                currencyType = mainUiState.selectedCurrencyType
            )
        }
    }
}

@Composable
private fun SpreadStatusPill(
    text: String,
    applied: Boolean,
    onClick: () -> Unit,
    onDark: Boolean = false
) {
    val bg = if (onDark) {
        Color.White.copy(alpha = 0.18f)
    } else if (applied) {
        Color(0xFFEDEAFB)
    } else {
        Color(0xFFF1F0F1)
    }
    val fg = if (onDark) {
        Color.White
    } else if (applied) {
        Color(0xFF6152D9)
    } else {
        Color(0xFF9CA3AF)
    }

    Surface(
        shape = RoundedCornerShape(50),
        color = bg,
        onClick = onClick
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(2.dp),
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        ) {
            Text(
                text = text,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = fg
            )
            Icon(
                imageVector = Icons.Rounded.ChevronRight,
                contentDescription = "스프레드 설정으로 이동",
                tint = fg,
                modifier = Modifier.size(9.dp)
            )
        }
    }
}

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
                .padding(14.dp)
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

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatItem(label = "평균 매수가", value = "₩${stats.averageRate}", color = Color(0xFF6B7280))
                StatItem(label = "매도환율(팔 때)", value = "₩${stats.sellRate}", color = Color(0xFF1F2937))
            }

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = Color(0xFFE5E7EB))
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatItem(label = "보유량", value = stats.holdingAmount, color = Color(0xFF6B7280))
                StatItem(label = "투자금", value = stats.totalInvestment, color = Color(0xFF6B7280))
            }

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = Color(0xFFE5E7EB))
            Spacer(modifier = Modifier.height(8.dp))

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
                    text = "매도환율 기준 예상 수익",
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
                            tint = if (isProfit) Color(0xFF10B981) else Color(0xFFEF4444)
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

@Composable
private fun StatItem(label: String, value: String, color: Color) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(text = label, fontSize = 12.sp, color = Color(0xFF9CA3AF))
        Text(text = value, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = color)
    }
}
