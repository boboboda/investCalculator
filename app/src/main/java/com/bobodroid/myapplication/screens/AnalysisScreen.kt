package com.bobodroid.myapplication.screens

import android.util.Log
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bobodroid.myapplication.MainActivity.Companion.TAG
import com.bobodroid.myapplication.components.chart.ExchangeRateChart
import com.bobodroid.myapplication.models.datamodels.roomDb.Currencies
import com.bobodroid.myapplication.models.datamodels.roomDb.CurrencyType
import com.bobodroid.myapplication.models.datamodels.roomDb.emoji
import com.bobodroid.myapplication.models.viewmodels.AnalysisViewModel
import com.bobodroid.myapplication.models.viewmodels.RateRangeCurrency

@Composable
fun AnalysisScreen(
    analysisViewModel: AnalysisViewModel
) {

    LaunchedEffect(Unit) {
        analysisViewModel.refreshData()
    }

    PremiumChartScreen(analysisViewModel = analysisViewModel)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PremiumChartScreen(
    analysisViewModel: AnalysisViewModel
) {
    val analysisUiState by analysisViewModel.analysisUiState.collectAsState()
    // ✅ ViewModel에서 통화 상태 가져오기 (remember 제거)
    val targetCurrency by analysisViewModel.selectedCurrency.collectAsState()
    val scrollState = rememberScrollState()

    // 데이터 계산
    val statistics = remember(analysisUiState.selectedRates, targetCurrency) {
        analysisViewModel.calculateStatistics(targetCurrency)
    }

    val trendAnalysis = remember(analysisUiState.selectedRates, targetCurrency) {
        analysisViewModel.calculateTrendAnalysis(targetCurrency)
    }

    val periodComparison = remember(analysisUiState.selectedRates, targetCurrency) {
        analysisViewModel.calculatePeriodComparison(targetCurrency)
    }

    val rangeRateMapCurrencyType = analysisUiState.selectedRates.mapNotNull { rate ->
        val rawValue = rate.getRate(targetCurrency.code).toFloatOrNull() ?: return@mapNotNull null
        // ✅ ExchangeRate에 이미 needsMultiply 처리됨 - 추가 처리 불필요
        val truncated = kotlin.math.floor(rawValue * 100) / 100f  // 소수점 2자리 버림
        RateRangeCurrency(truncated, rate.createAt)
    }

    val latestRate = run {
        val rawValue = when(targetCurrency) {
            CurrencyType.USD -> analysisUiState.latestRate.usd
            CurrencyType.JPY -> analysisUiState.latestRate.jpy
            else -> "0"
        }.toFloatOrNull() ?: 0f

        // ✅ ExchangeRate에 이미 needsMultiply 처리됨 - 추가 처리 불필요
        String.format("%.2f", rawValue)
    }

    val changeRate = run {
        val rawValue = when(targetCurrency) {
            CurrencyType.USD -> analysisUiState.change.usd
            CurrencyType.JPY -> analysisUiState.change.jpy
            else -> "0"
        }.toFloatOrNull() ?: 0f

        // ✅ ExchangeRate에 이미 needsMultiply 처리됨 - 추가 처리 불필요
        String.format("%.2f", rawValue)
    }

    val changeIcon = when(targetCurrency) {
        CurrencyType.USD -> analysisUiState.usdChangeIcon
        CurrencyType.JPY -> analysisUiState.jpyChangeIcon
        else -> '-'
    }

    val changeColor = when(targetCurrency) {
        CurrencyType.USD -> analysisUiState.usdChangeColor
        CurrencyType.JPY -> analysisUiState.jpyChangeColor
        else -> Color.Gray
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF9FAFB))
            .verticalScroll(scrollState)
    ) {
        // 🎨 현재 환율 헤더
        CurrentRateHeader(
            currency = targetCurrency,
            latestRate = latestRate,
            changeRate = changeRate,
            changeIcon = changeIcon,
            changeColor = changeColor,
            onCurrencyChange = { analysisViewModel.updateSelectedCurrency(it) }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 📊 통계 카드 섹션
        StatisticsCardsSection(
            statistics = statistics,
            currency = targetCurrency
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 📈 차트 섹션
        ChartSection(
            data = rangeRateMapCurrencyType,
            selectedTabIndex = analysisUiState.selectedTabIndex,
            onTabSelected = { analysisViewModel.onTabSelected(it) }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 🔍 인사이트 섹션
        InsightsSection(
            trendAnalysis = trendAnalysis,
            statistics = statistics
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 📊 기간별 비교 섹션
        PeriodComparisonSection(
            periodComparison = periodComparison,
            latestRate = latestRate
        )

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun CurrentRateHeader(
    currency: CurrencyType,
    latestRate: String,
    changeRate: String,
    changeIcon: Char,
    changeColor: Color,
    onCurrencyChange: (CurrencyType) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }



    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF6366F1),
                        Color(0xFF8B5CF6)
                    )
                )
            )
            .padding(24.dp)
    ) {
        Column {
            // 통화 선택 버튼
            Box {
                OutlinedButton(
                    onClick = { expanded = true },
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Color.White.copy(alpha = 0.2f)
                    ),
                    border = null
                ) {
                    Text(
                        text = "${currency.emoji} ${currency.code}",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Icon(
                        Icons.Filled.ArrowDropDown,
                        contentDescription = null,
                        tint = Color.White
                    )
                }

                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    CurrencyType.values().forEach { currencyType ->
                        DropdownMenuItem(
                            text = {
                                Text("${currencyType.emoji} ${currencyType.koreanName} (${currencyType.code})")
                            },
                            onClick = {
                                onCurrencyChange(currencyType)
                                expanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 현재 환율
            Row(
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = latestRate,
                    fontSize = 42.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = " 원",
                    fontSize = 24.sp,
                    color = Color.White.copy(alpha = 0.8f),
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 변화량
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "$changeIcon ",
                    fontSize = 18.sp,
                    color = changeColor
                )
                Text(
                    text = changeRate,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = changeColor
                )
                Text(
                    text = " (전일 대비)",
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.7f),
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }
    }
}

@Composable
fun StatisticsCardsSection(
    statistics: com.bobodroid.myapplication.models.viewmodels.RateStatistics,
    currency: CurrencyType
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Text(
            text = "📊 통계 분석",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1F2937),
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatCard(
                modifier = Modifier.weight(1f),
                title = "최고",
                value = String.format("%.2f", statistics.max),
                icon = Icons.Rounded.TrendingUp,
                gradient = listOf(Color(0xFFEF4444), Color(0xFFF87171))
            )

            StatCard(
                modifier = Modifier.weight(1f),
                title = "최저",
                value = String.format("%.2f", statistics.min),
                icon = Icons.Rounded.TrendingDown,
                gradient = listOf(Color(0xFF3B82F6), Color(0xFF60A5FA))
            )

            StatCard(
                modifier = Modifier.weight(1f),
                title = "평균",
                value = String.format("%.2f", statistics.average),
                icon = Icons.Rounded.ShowChart,
                gradient = listOf(Color(0xFF10B981), Color(0xFF34D399))
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 변동폭 카드
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Rounded.SwapVert,
                        contentDescription = null,
                        tint = Color(0xFF8B5CF6),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "변동폭",
                            fontSize = 14.sp,
                            color = Color(0xFF6B7280)
                        )
                        Text(
                            text = String.format("%.2f원", statistics.range),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1F2937)
                        )
                    }
                }

                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = "변동성",
                        fontSize = 14.sp,
                        color = Color(0xFF6B7280)
                    )
                    Text(
                        text = String.format("±%.2f", statistics.volatility),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF8B5CF6)
                    )
                }
            }
        }
    }
}

@Composable
fun StatCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    icon: ImageVector,
    gradient: List<Color>
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = gradient.map { it.copy(alpha = 0.1f) }
                    )
                )
                .padding(16.dp)
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = gradient[0],
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                fontSize = 12.sp,
                color = Color(0xFF6B7280)
            )
            Text(
                text = value,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = gradient[0]
            )
        }
    }
}

@Composable
fun ChartSection(
    data: List<RateRangeCurrency>,
    selectedTabIndex: Int,
    onTabSelected: (Int) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "📈 환율 차트",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1F2937),
                modifier = Modifier.padding(bottom = 12.dp)
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
            ) {
                ExchangeRateChart(data = data)
            }

            Spacer(modifier = Modifier.height(16.dp))

            PeriodTabRow(
                selectedTabIndex = selectedTabIndex,
                onTabSelected = onTabSelected
            )
        }
    }
}

@Composable
fun PeriodTabRow(
    selectedTabIndex: Int,
    onTabSelected: (Int) -> Unit
) {
    val tabTitles = listOf("1일", "1주", "3개월", "1년")

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFFF3F4F6))
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        tabTitles.forEachIndexed { index, title ->
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        if (selectedTabIndex == index)
                            Color(0xFF6366F1)
                        else Color.Transparent
                    )
                    .clickable { onTabSelected(index) }
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = title,
                    fontSize = 14.sp,
                    fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal,
                    color = if (selectedTabIndex == index) Color.White else Color(0xFF6B7280)
                )
            }
        }
    }
}

@Composable
fun InsightsSection(
    trendAnalysis: com.bobodroid.myapplication.models.viewmodels.TrendAnalysis,
    statistics: com.bobodroid.myapplication.models.viewmodels.RateStatistics
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Text(
            text = "🔍 분석 인사이트 (최근 1년 기준)",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1F2937),
            modifier = Modifier.padding(bottom = 12.dp)
        )

        // 추세 분석 카드
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 12.dp)
                ) {
                    Icon(
                        when(trendAnalysis.trend) {
                            "상승" -> Icons.Rounded.TrendingUp
                            "하락" -> Icons.Rounded.TrendingDown
                            else -> Icons.Rounded.TrendingFlat
                        },
                        contentDescription = null,
                        tint = when(trendAnalysis.trend) {
                            "상승" -> Color(0xFFEF4444)
                            "하락" -> Color(0xFF3B82F6)
                            else -> Color(0xFF6B7280)
                        },
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "현재 추세: ${trendAnalysis.trend}",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1F2937)
                    )
                }

                Divider(color = Color(0xFFE5E7EB))

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    InsightItem(
                        label = "상승일",
                        value = "${trendAnalysis.upDays}일",
                        color = Color(0xFFEF4444)
                    )
                    InsightItem(
                        label = "하락일",
                        value = "${trendAnalysis.downDays}일",
                        color = Color(0xFF3B82F6)
                    )
                    InsightItem(
                        label = "추세 강도",
                        value = "${trendAnalysis.trendStrength}%",
                        color = Color(0xFF8B5CF6)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 변동성 분석 카드
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "💡 변동성 평가",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1F2937),
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    Text(
                        text = when {
                            statistics.volatility < 5 -> "안정적인 구간입니다"
                            statistics.volatility < 15 -> "보통 변동성입니다"
                            else -> "높은 변동성 주의"
                        },
                        fontSize = 14.sp,
                        color = Color(0xFF6B7280)
                    )
                }

                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(RoundedCornerShape(30.dp))
                        .background(
                            when {
                                statistics.volatility < 5 -> Color(0xFF10B981)
                                statistics.volatility < 15 -> Color(0xFFF59E0B)
                                else -> Color(0xFFEF4444)
                            }.copy(alpha = 0.2f)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = when {
                            statistics.volatility < 5 -> "🟢"
                            statistics.volatility < 15 -> "🟡"
                            else -> "🔴"
                        },
                        fontSize = 28.sp
                    )
                }
            }
        }
    }
}

@Composable
fun InsightItem(
    label: String,
    value: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            fontSize = 12.sp,
            color = Color(0xFF6B7280)
        )
    }
}

@Composable
fun PeriodComparisonSection(
    periodComparison: com.bobodroid.myapplication.models.viewmodels.PeriodComparison,
    latestRate: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Text(
            text = "📊 기간별 비교(최신 환율 기준)",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1F2937),
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                ComparisonItem(
                    period = "전일 대비",
                    change = periodComparison.previousDay,
                    icon = Icons.Rounded.CalendarToday
                )
                Divider(modifier = Modifier.padding(vertical = 12.dp))
                ComparisonItem(
                    period = "1주 전 대비",
                    change = periodComparison.weekAgo,
                    icon = Icons.Rounded.DateRange
                )
                Divider(modifier = Modifier.padding(vertical = 12.dp))
                ComparisonItem(
                    period = "1개월 전 대비",
                    change = periodComparison.monthAgo,
                    icon = Icons.Rounded.CalendarMonth
                )
            }
        }
    }
}

@Composable
fun ComparisonItem(
    period: String,
    change: String,
    icon: ImageVector
) {
    val isPositive = !change.startsWith("-")
    val changeColor = if (isPositive) Color(0xFFEF4444) else Color(0xFF3B82F6)

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = Color(0xFF6B7280),
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = period,
                fontSize = 15.sp,
                color = Color(0xFF1F2937)
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                if (isPositive) Icons.Rounded.ArrowUpward else Icons.Rounded.ArrowDownward,
                contentDescription = null,
                tint = changeColor,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = change,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = changeColor
            )
        }
    }
}