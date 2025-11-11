package com.bobodroid.myapplication.screens

import android.util.Log
import androidx.compose.animation.core.*
import androidx.compose.foundation.ScrollState
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.bobodroid.myapplication.MainActivity.Companion.TAG
import com.bobodroid.myapplication.components.Dialogs.PremiumPromptDialog
import com.bobodroid.myapplication.components.Dialogs.PremiumRequiredDialog
import com.bobodroid.myapplication.components.Dialogs.RewardAdInfoDialog
import com.bobodroid.myapplication.components.chart.ExchangeRateChart
import com.bobodroid.myapplication.components.common.CurrencyDropdown
import com.bobodroid.myapplication.models.datamodels.roomDb.Currencies
import com.bobodroid.myapplication.models.datamodels.roomDb.CurrencyType
import com.bobodroid.myapplication.models.datamodels.roomDb.emoji
import com.bobodroid.myapplication.models.viewmodels.AnalysisUiState
import com.bobodroid.myapplication.models.viewmodels.AnalysisViewModel
import com.bobodroid.myapplication.models.viewmodels.LoadingState
import com.bobodroid.myapplication.models.viewmodels.RateRangeCurrency
import com.bobodroid.myapplication.models.viewmodels.SharedViewModel
import com.bobodroid.myapplication.ui.theme.primaryColor

@Composable
fun AnalysisScreen(
    analysisViewModel: AnalysisViewModel = hiltViewModel(),
    sharedViewModel: SharedViewModel,
) {


    val isPremium by sharedViewModel.isPremium.collectAsState()
    val showPremiumPrompt by sharedViewModel.showPremiumPrompt.collectAsState()
    val showRewardAdInfo by sharedViewModel.showRewardAdInfo.collectAsState()

    val context = LocalContext.current

    LaunchedEffect(Unit) {
        if (!isPremium) {
            // ✅ ViewModel이 UseCase 호출 → UseCase가 AdManager 호출
            sharedViewModel.showInterstitialAdIfNeeded(context)
        }
    }


    // ✅ 프리미엄 다이얼로그 상태 추가
    var showPremiumDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        analysisViewModel.refreshData()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        PremiumChartScreen(
            analysisViewModel = analysisViewModel,
            onPremiumRequired = { showPremiumDialog = true }
        )

        // ✅ 프리미엄 다이얼로그
        if (showPremiumDialog) {
            PremiumRequiredDialog(
                onDismiss = { showPremiumDialog = false },
                onPurchaseClick = {
                    showPremiumDialog = false
                    // 프리미엄 화면으로 이동 (구현 필요)
                }
            )
        }

        if (showPremiumPrompt) {
            PremiumPromptDialog(
                onWatchAd = {
                    sharedViewModel.closePremiumPromptAndShowRewardDialog()
                },
                onDismiss = {
                    sharedViewModel.closePremiumPrompt()
                }
            )
        }

        // 리워드 광고 안내 팝업
        if (showRewardAdInfo) {
            RewardAdInfoDialog(
                onConfirm = {
                    sharedViewModel.showRewardAdAndGrantPremium(context)
                },
                onDismiss = {
                    sharedViewModel.closeRewardAdDialog()
                }
            )
        }
    }
}

// ✅ PremiumChartScreen 수정 버전 (AnalysisScreen.kt에서 기존 함수 대체)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PremiumChartScreen(
    analysisViewModel: AnalysisViewModel,
    onPremiumRequired: () -> Unit
) {
    val analysisUiState by analysisViewModel.analysisUiState.collectAsState()
    val targetCurrency by analysisViewModel.selectedCurrency.collectAsState()
    val scrollState = rememberScrollState()

    // ✅ 로딩 상태에 따른 UI 분기
    when (val loadingState = analysisUiState.loadingState) {
        is LoadingState.Loading -> {
            // 로딩 중
            AnalysisLoadingScreen()
        }

        is LoadingState.Error -> {
            // 에러 발생
            AnalysisErrorScreen(
                errorMessage = loadingState.message,
                onRetry = { analysisViewModel.refreshData() }
            )
        }

        is LoadingState.Success -> {
            // 데이터 로드 성공 - 실제 컨텐츠 표시
            SuccessContent(
                analysisUiState = analysisUiState,
                targetCurrency = targetCurrency,
                scrollState = scrollState,
                analysisViewModel = analysisViewModel,
                onPremiumRequired = onPremiumRequired
            )
        }
    }
}

/**
 * 데이터 로드 성공 시 표시되는 실제 컨텐츠
 */
@Composable
private fun SuccessContent(
    analysisUiState: AnalysisUiState,
    targetCurrency: CurrencyType,
    scrollState: ScrollState,
    analysisViewModel: AnalysisViewModel,
    onPremiumRequired: () -> Unit
) {
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
        val truncated = kotlin.math.floor(rawValue * 100) / 100f
        RateRangeCurrency(truncated, rate.createAt)
    }

    // ✅ 선택된 통화의 최신 환율 가져오기
    val latestRate = run {
        val rawValue = analysisUiState.latestRate.getRate(targetCurrency.code).toFloatOrNull() ?: 0f
        String.format("%.2f", rawValue)
    }

    // ✅ 선택된 통화의 변화량 가져오기
    val changeRate = run {
        val rawValue = analysisUiState.change.getChange(targetCurrency.code).toFloatOrNull() ?: 0f
        String.format("%.2f", rawValue)
    }

    // ✅ 변화 아이콘/색상 동적 계산
    val (changeIcon, changeColor) = remember(changeRate) {
        try {
            val change = changeRate.toDouble()
            when {
                change > 0 -> Pair('▲', Color.Red)
                change < 0 -> Pair('▼', Color.Blue)
                else -> Pair('-', Color.Gray)
            }
        } catch (e: NumberFormatException) {
            Pair('-', Color.Gray)
        }
    }

    val isPremium by analysisViewModel.isPremium.collectAsState()

    var showPremiumDialog by remember { mutableStateOf(false) }

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
            isPremium = isPremium,
            onCurrencyChange = { analysisViewModel.updateSelectedCurrency(it) },
            onPremiumRequired = onPremiumRequired
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

        if (showPremiumDialog) {
            PremiumRequiredDialog(
                onDismiss = { showPremiumDialog = false },
                onPurchaseClick = {
                    showPremiumDialog = false
                    // 프리미엄 화면으로 이동 (구현 필요)
                }
            )
        }
    }
}

@Composable
fun CurrentRateHeader(
    currency: CurrencyType,
    latestRate: String,
    changeRate: String,
    changeIcon: Char,
    changeColor: Color,
    isPremium: Boolean,                              // ✅ 추가
    onCurrencyChange: (CurrencyType) -> Boolean,     // ✅ Boolean 반환으로 변경
    onPremiumRequired: () -> Unit                    // ✅ 추가
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
            CurrencyDropdown(
                selectedCurrency = currency,
                updateCurrentForeignCurrency = onCurrencyChange,
                isPremium = isPremium,
                onPremiumRequired = onPremiumRequired,
                backgroundColor = Color.White.copy(alpha = 0.2f),
                contentColor = Color.White
            )

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