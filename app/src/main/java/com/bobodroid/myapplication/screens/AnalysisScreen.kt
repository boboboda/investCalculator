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
import com.bobodroid.myapplication.components.mainComponents.AnimatedNewsChip
import com.bobodroid.myapplication.models.datamodels.roomDb.Currencies
import com.bobodroid.myapplication.models.datamodels.roomDb.CurrencyType
import com.bobodroid.myapplication.models.datamodels.roomDb.emoji
import com.bobodroid.myapplication.models.viewmodels.AnalysisUiState
import com.bobodroid.myapplication.models.viewmodels.AnalysisViewModel
import com.bobodroid.myapplication.models.viewmodels.DailyChangeDistribution
import com.bobodroid.myapplication.models.viewmodels.HighLowDistance
import com.bobodroid.myapplication.models.viewmodels.LoadingState
import com.bobodroid.myapplication.models.viewmodels.LongTermPosition
import com.bobodroid.myapplication.models.viewmodels.PeriodAverage
import com.bobodroid.myapplication.models.viewmodels.RateRangeCurrency
import com.bobodroid.myapplication.models.viewmodels.SharedViewModel
import com.bobodroid.myapplication.models.viewmodels.SpreadRates
import com.bobodroid.myapplication.models.viewmodels.TargetRateInfo
import com.bobodroid.myapplication.models.viewmodels.YearlyAverageStreak
import com.bobodroid.myapplication.ui.theme.primaryColor

@Composable
fun AnalysisScreen(
    analysisViewModel: AnalysisViewModel = hiltViewModel(),
    sharedViewModel: SharedViewModel,
    onNavigateToPremium: () -> Unit = {},
    onNavigateToNews: () -> Unit = {}
) {

    val isPremium by sharedViewModel.isPremium.collectAsState()

    val context = LocalContext.current

    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(Unit) {
        sharedViewModel.snackbarEvent.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    LaunchedEffect(Unit) {
        if (!isPremium) {
            sharedViewModel.showInterstitialAdIfNeeded(context)
        }
    }

    var showPremiumDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        analysisViewModel.refreshData()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        PremiumChartScreen(
            analysisViewModel = analysisViewModel,
            onPremiumRequired = { showPremiumDialog = true },
            onNavigateToPremium = onNavigateToPremium,
            onNavigateToNews = onNavigateToNews
        )

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 20.dp)
        )

        if (showPremiumDialog) {
            PremiumRequiredDialog(
                onDismiss = { showPremiumDialog = false },
                onPurchaseClick = {
                    showPremiumDialog = false
                    onNavigateToPremium()
                }
            )
        }

    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PremiumChartScreen(
    analysisViewModel: AnalysisViewModel,
    onPremiumRequired: () -> Unit,
    onNavigateToPremium: () -> Unit = {},
    onNavigateToNews: () -> Unit = {}
) {
    val analysisUiState by analysisViewModel.analysisUiState.collectAsState()
    val targetCurrency by analysisViewModel.selectedCurrency.collectAsState()
    val scrollState = rememberScrollState()

    when (val loadingState = analysisUiState.loadingState) {
        is LoadingState.Loading -> {
            AnalysisLoadingScreen()
        }

        is LoadingState.Error -> {
            AnalysisErrorScreen(
                errorMessage = loadingState.message,
                onRetry = { analysisViewModel.refreshData() }
            )
        }

        is LoadingState.Success -> {
            SuccessContent(
                analysisUiState = analysisUiState,
                targetCurrency = targetCurrency,
                scrollState = scrollState,
                analysisViewModel = analysisViewModel,
                onPremiumRequired = onPremiumRequired,
                onNavigateToPremium = onNavigateToPremium,
                onNavigateToNews = onNavigateToNews
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
    onPremiumRequired: () -> Unit,
    onNavigateToPremium: () -> Unit = {},
    onNavigateToNews: () -> Unit = {}
) {
    val statistics = remember(analysisUiState.selectedRates, targetCurrency) {
        analysisViewModel.calculateStatistics(targetCurrency)
    }

    val periodComparison = remember(analysisUiState.selectedRates, targetCurrency) {
        analysisViewModel.calculatePeriodComparison(targetCurrency)
    }

    val longTermPosition = remember(analysisUiState.latestRate, targetCurrency) {
        analysisViewModel.calculateLongTermPosition(targetCurrency)
    }

    val highLowDistance = remember(analysisUiState.latestRate, targetCurrency) {
        analysisViewModel.calculateHighLowDistance(targetCurrency)
    }

    val periodAverages = remember(analysisUiState.latestRate, targetCurrency) {
        analysisViewModel.calculatePeriodAverages(targetCurrency)
    }

    val yearlyStreak = remember(targetCurrency) {
        analysisViewModel.calculateYearlyAverageStreak(targetCurrency)
    }

    val dailyDistribution = remember(targetCurrency) {
        analysisViewModel.calculateDailyChangeDistribution(targetCurrency)
    }

    val spreadRates = remember(analysisUiState.latestRate, targetCurrency) {
        analysisViewModel.calculateSpreadRates(targetCurrency)
    }

    val targetRateInfo = remember(analysisUiState.latestRate, targetCurrency) {
        analysisViewModel.calculateTargetRateInfo(targetCurrency)
    }

    val rangeRateMapCurrencyType = analysisUiState.selectedRates.mapNotNull { rate ->
        val rawValue = rate.getRate(targetCurrency.code).toFloatOrNull() ?: return@mapNotNull null
        val truncated = kotlin.math.floor(rawValue * 100) / 100f
        RateRangeCurrency(truncated, rate.createAt)
    }

    val latestRate = run {
        val rawValue = analysisUiState.latestRate.getRate(targetCurrency.code).toFloatOrNull() ?: 0f
        String.format("%.2f", rawValue)
    }

    val changeRate = run {
        val rawValue = analysisUiState.change.getChange(targetCurrency.code).toFloatOrNull() ?: 0f
        String.format("%.2f", rawValue)
    }

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
    val latestNews by analysisViewModel.latestNews.collectAsState()

    var showPremiumDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF9FAFB))
            .verticalScroll(scrollState)
    ) {
        CurrentRateHeader(
            currency = targetCurrency,
            latestRate = latestRate,
            changeRate = changeRate,
            changeIcon = changeIcon,
            changeColor = changeColor,
            isPremium = isPremium,
            onCurrencyChange = { analysisViewModel.updateSelectedCurrency(it) },
            onPremiumRequired = onPremiumRequired,
            spreadRates = spreadRates
        )

        AnimatedNewsChip(
            newsTitles = latestNews.map { it.title },
            onClick = onNavigateToNews
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 📊 통계 분석 (최고/최저/평균 → 최고·최저가 대비 → 평균 대비 위치 → 변동폭 → 연속일수)
        StatisticsCardsSection(
            statistics = statistics,
            currency = targetCurrency,
            highLowDistance = highLowDistance,
            periodAverages = periodAverages,
            yearlyStreak = yearlyStreak
        )

        Spacer(modifier = Modifier.height(16.dp))

        ChartSection(
            data = rangeRateMapCurrencyType,
            selectedTabIndex = analysisUiState.selectedTabIndex,
            onTabSelected = { analysisViewModel.onTabSelected(it) }
        )

        Spacer(modifier = Modifier.height(16.dp))

        LongTermPositionSection(longTermPosition = longTermPosition)

        Spacer(modifier = Modifier.height(16.dp))

        TargetRateSection(targetRateInfo = targetRateInfo)

        Spacer(modifier = Modifier.height(16.dp))

        DailyDistributionSection(distribution = dailyDistribution)

        Spacer(modifier = Modifier.height(16.dp))

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
                    onNavigateToPremium()
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
    isPremium: Boolean,
    onCurrencyChange: (CurrencyType) -> Boolean,
    onPremiumRequired: () -> Unit,
    spreadRates: SpreadRates = SpreadRates()
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
            CurrencyDropdown(
                selectedCurrency = currency,
                updateCurrentForeignCurrency = onCurrencyChange,
                isPremium = isPremium,
                onPremiumRequired = onPremiumRequired,
                backgroundColor = Color.White.copy(alpha = 0.2f),
                contentColor = Color.White
            )

            Spacer(modifier = Modifier.height(16.dp))

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

            if (spreadRates.applied) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color.White.copy(alpha = 0.15f))
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(text = "매수", fontSize = 12.sp, color = Color.White.copy(alpha = 0.7f))
                        Text(
                            text = "${spreadRates.buyRate}원",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(text = "매도", fontSize = 12.sp, color = Color.White.copy(alpha = 0.7f))
                        Text(
                            text = "${spreadRates.sellRate}원",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StatisticsCardsSection(
    statistics: com.bobodroid.myapplication.models.viewmodels.RateStatistics,
    currency: CurrencyType,
    highLowDistance: HighLowDistance,
    periodAverages: List<PeriodAverage>,
    yearlyStreak: YearlyAverageStreak
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

        // 1. 최고 / 최저 / 평균
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

        // 2. 최고가 대비 / 최저가 대비 (1년 고정)
        HighLowDistanceSection(highLowDistance)

        Spacer(modifier = Modifier.height(12.dp))

        // 3. 평균 대비 현재 위치 (1일/7일/3개월/1년)
        PeriodAverageSection(periodAverages)

        Spacer(modifier = Modifier.height(12.dp))

        // 4. 변동폭 카드
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

        Spacer(modifier = Modifier.height(12.dp))

        // 5. 1년 평균 대비 연속일수
        YearlyStreakSection(yearlyStreak)
    }
}

@Composable
private fun HighLowDistanceSection(highLowDistance: HighLowDistance) {
    if (!highLowDistance.hasData) return

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        DistanceCard(
            modifier = Modifier.weight(1f),
            title = "최고가 대비",
            subLabel = "1년 최고 ${String.format("%.2f", highLowDistance.highRate)}원",
            diff = highLowDistance.diffFromHigh,
            diffPercent = highLowDistance.diffFromHighPercent,
            color = Color(0xFF3B82F6)
        )
        DistanceCard(
            modifier = Modifier.weight(1f),
            title = "최저가 대비",
            subLabel = "1년 최저 ${String.format("%.2f", highLowDistance.lowRate)}원",
            diff = highLowDistance.diffFromLow,
            diffPercent = highLowDistance.diffFromLowPercent,
            color = Color(0xFFEF4444)
        )
    }
}

@Composable
private fun DistanceCard(
    modifier: Modifier = Modifier,
    title: String,
    subLabel: String,
    diff: Float,
    diffPercent: Float,
    color: Color
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(text = title, fontSize = 13.sp, color = Color(0xFF6B7280))
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${if (diff >= 0) "+" else ""}${String.format("%.2f", diff)}원",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = "${if (diffPercent >= 0) "+" else ""}${String.format("%.2f", diffPercent)}%",
                fontSize = 13.sp,
                color = color
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(text = subLabel, fontSize = 11.sp, color = Color(0xFF9CA3AF))
        }
    }
}

@Composable
private fun PeriodAverageSection(periodAverages: List<PeriodAverage>) {
    if (periodAverages.isEmpty()) return

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "평균 대비 현재 위치",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1F2937),
                modifier = Modifier.padding(bottom = 10.dp)
            )
            periodAverages.forEachIndexed { index, item ->
                PeriodAverageRow(item)
                if (index != periodAverages.lastIndex) {
                    Spacer(modifier = Modifier.height(10.dp))
                }
            }
        }
    }
}

@Composable
private fun PeriodAverageRow(item: PeriodAverage) {
    val color = if (item.diff >= 0) Color(0xFFEF4444) else Color(0xFF3B82F6)

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(text = "${item.label} 평균", fontSize = 13.sp, color = Color(0xFF6B7280))
            Text(
                text = "${String.format("%.2f", item.averageRate)}원",
                fontSize = 14.sp,
                color = Color(0xFF1F2937)
            )
        }
        Text(
            text = "${if (item.diff >= 0) "+" else ""}${String.format("%.2f", item.diff)}원 (${if (item.diffPercent >= 0) "+" else ""}${String.format("%.2f", item.diffPercent)}%)",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

@Composable
private fun YearlyStreakSection(streak: YearlyAverageStreak) {
    if (!streak.hasData || streak.streakDays == 0) return

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                if (streak.streakDays > 0) Icons.Rounded.TrendingUp else Icons.Rounded.TrendingDown,
                contentDescription = null,
                tint = if (streak.streakDays > 0) Color(0xFFEF4444) else Color(0xFF3B82F6),
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = if (streak.streakDays > 0)
                    "1년 평균 대비 ${streak.streakDays}일 연속 상회 중"
                else
                    "1년 평균 대비 ${-streak.streakDays}일 연속 하회 중",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1F2937)
            )
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
fun LongTermPositionSection(longTermPosition: LongTermPosition) {
    if (longTermPosition.threeMonthPercentile == null && longTermPosition.oneYearPercentile == null) return

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Text(
            text = "📐 장기 포지션",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1F2937),
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                longTermPosition.threeMonthPercentile?.let {
                    PositionBar(label = "3개월", percentile = it)
                    if (longTermPosition.oneYearPercentile != null) {
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
                longTermPosition.oneYearPercentile?.let {
                    PositionBar(label = "1년", percentile = it)
                }
            }
        }
    }
}

@Composable
private fun PositionBar(label: String, percentile: Float) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = label, fontSize = 14.sp, color = Color(0xFF6B7280))
            Text(
                text = "${percentile.toInt()}%",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF6366F1)
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
                .clip(RoundedCornerShape(5.dp))
                .background(Color(0xFFE5E7EB))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction = (percentile / 100f).coerceIn(0f, 1f))
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(5.dp))
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(Color(0xFF3B82F6), Color(0xFFEF4444))
                        )
                    )
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = when {
                percentile >= 80f -> "구간 내 상단권 (최고가에 근접)"
                percentile <= 20f -> "구간 내 하단권 (최저가에 근접)"
                else -> "구간 내 중간권"
            },
            fontSize = 12.sp,
            color = Color(0xFF9CA3AF)
        )
    }
}

@Composable
fun TargetRateSection(targetRateInfo: TargetRateInfo) {
    if (!targetRateInfo.hasTargets) return

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Text(
            text = "🎯 목표환율 대비",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1F2937),
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                targetRateInfo.nearestHighRate?.let { highRate ->
                    TargetRateRow(
                        label = "오름 목표",
                        targetRate = highRate,
                        diffPercent = targetRateInfo.highDiffPercent,
                        icon = Icons.Rounded.TrendingUp,
                        color = Color(0xFFEF4444)
                    )
                    if (targetRateInfo.nearestLowRate != null) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Divider(color = Color(0xFFE5E7EB))
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
                targetRateInfo.nearestLowRate?.let { lowRate ->
                    TargetRateRow(
                        label = "내림 목표",
                        targetRate = lowRate,
                        diffPercent = targetRateInfo.lowDiffPercent,
                        icon = Icons.Rounded.TrendingDown,
                        color = Color(0xFF3B82F6)
                    )
                }
            }
        }
    }
}

@Composable
private fun TargetRateRow(
    label: String,
    targetRate: Int,
    diffPercent: Float?,
    icon: ImageVector,
    color: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(text = label, fontSize = 13.sp, color = Color(0xFF6B7280))
                Text(
                    text = "${targetRate}원",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1F2937)
                )
            }
        }
        diffPercent?.let {
            Text(
                text = if (it >= 0) "+${String.format("%.2f", it)}%" else "${String.format("%.2f", it)}%",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}

@Composable
fun DailyDistributionSection(distribution: DailyChangeDistribution) {
    if (distribution.totalCount == 0) return

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Text(
            text = "📶 일별 변동 분포 (최근 1년)",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1F2937),
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                DistributionBarRow(label = "5원↑하락", count = distribution.bigDownCount, total = distribution.totalCount, color = Color(0xFF1D4ED8))
                DistributionBarRow(label = "3~5원 하락", count = distribution.downCount, total = distribution.totalCount, color = Color(0xFF60A5FA))
                DistributionBarRow(label = "보합(±3원)", count = distribution.flatCount, total = distribution.totalCount, color = Color(0xFF9CA3AF))
                DistributionBarRow(label = "3~5원 상승", count = distribution.upCount, total = distribution.totalCount, color = Color(0xFFF87171))
                DistributionBarRow(label = "5원↑상승", count = distribution.bigUpCount, total = distribution.totalCount, color = Color(0xFFB91C1C))
            }
        }
    }
}

@Composable
private fun DistributionBarRow(label: String, count: Int, total: Int, color: Color) {
    val fraction = if (total > 0) count.toFloat() / total.toFloat() else 0f

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            color = Color(0xFF6B7280),
            modifier = Modifier.width(72.dp)
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .height(14.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(Color(0xFFF3F4F6))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction = fraction.coerceIn(0f, 1f))
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(4.dp))
                    .background(color)
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "${count}일",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1F2937),
            modifier = Modifier.width(36.dp)
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
