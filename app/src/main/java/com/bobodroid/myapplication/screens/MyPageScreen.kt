package com.bobodroid.myapplication.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.bobodroid.myapplication.billing.BillingClientLifecycle
import com.bobodroid.myapplication.components.Dialogs.OnboardingTooltipDialog
import com.bobodroid.myapplication.models.datamodels.roomDb.LocalUserData
import com.bobodroid.myapplication.models.viewmodels.*
import com.bobodroid.myapplication.routes.MyPageRoute
import com.bobodroid.myapplication.routes.RouteAction
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.temporal.ChronoUnit

@Composable
fun MyPageScreen() {
    val coroutineScope = rememberCoroutineScope()
    val myPageViewModel: MyPageViewModel = hiltViewModel()
    val uiState by myPageViewModel.myPageUiState.collectAsState()
    val mainScreenSnackBarHostState = remember { SnackbarHostState() }
    val navController = rememberNavController()

    val myPageRouteAction = remember {
        RouteAction<MyPageRoute>(navController, MyPageRoute.SelectView.routeName)
    }

    var showOnboarding by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.BottomCenter
    ) {
        NavHost(
            navController = navController,
            startDestination = MyPageRoute.SelectView.routeName ?: "",
        ) {
            composable(MyPageRoute.SelectView.routeName!!) {
                ImprovedMyPageView(
                    myPageRouteAction = myPageRouteAction,
                    localUser = uiState.localUser,
                    investmentStats = uiState.investmentStats,
                    recentActivities = uiState.recentActivities,
                    monthlyGoal = uiState.monthlyGoal,
                    onSetGoal = { amount -> myPageViewModel.setMonthlyGoal(amount) },
                    showOnboarding = { showOnboarding = true },
                    badges = uiState.badges,
                    isPremium = uiState.localUser.isPremium
                )
            }

            composable(MyPageRoute.CreateUser.routeName!!) {
                AccountManageView(
                    routeAction = myPageRouteAction,
                    localUser = uiState.localUser,
                    onGoogleLogin = { activity ->
                        myPageViewModel.loginWithGoogle(activity) { resultMessage ->
                            coroutineScope.launch {
                                mainScreenSnackBarHostState.showSnackbar(
                                    resultMessage,
                                    actionLabel = "닫기",
                                    duration = SnackbarDuration.Short
                                )
                            }
                        }
                    },
                    onKakaoLogin = { activity ->
                        myPageViewModel.loginWithKakao(activity) { resultMessage ->
                            coroutineScope.launch {
                                mainScreenSnackBarHostState.showSnackbar(
                                    resultMessage,
                                    actionLabel = "닫기",
                                    duration = SnackbarDuration.Short
                                )
                            }
                        }
                    },
                    onLogout = {
                        myPageViewModel.logout { resultMessage ->
                            coroutineScope.launch {
                                mainScreenSnackBarHostState.showSnackbar(
                                    resultMessage,
                                    actionLabel = "닫기",
                                    duration = SnackbarDuration.Short
                                )
                            }
                        }
                    },
                    onUnlinkSocial = {
                        myPageViewModel.unlinkSocial { resultMessage ->
                            coroutineScope.launch {
                                mainScreenSnackBarHostState.showSnackbar(
                                    resultMessage,
                                    actionLabel = "닫기",
                                    duration = SnackbarDuration.Short
                                )
                            }
                        }
                    }
                )
            }

            composable(MyPageRoute.CloudService.routeName!!) {
                CloudView(
                    routeAction = myPageRouteAction,
                    localUser = uiState.localUser
                )
            }

            composable(MyPageRoute.WidgetSettings.routeName!!) {
                WidgetSettingsScreen(
                    onBackClick = { myPageRouteAction.goBack() },
                    onPremiumClick = { myPageRouteAction.navTo(MyPageRoute.Premium) }
                )
            }

            composable(MyPageRoute.Premium.routeName!!) {
                PremiumScreen(
                    onBackClick = { myPageRouteAction.goBack() }
                )
            }

            composable(MyPageRoute.CustomerServiceCenter.routeName!!) {
                CustomerView(myPageRouteAction)
            }
        }

        SnackbarHost(
            hostState = mainScreenSnackBarHostState,
            modifier = Modifier.padding(bottom = 20.dp)
        )

        if (showOnboarding) {
            OnboardingTooltipDialog(
                onDismiss = { showOnboarding = false }
            )
        }
    }
}

@Composable
fun ImprovedMyPageView(
    myPageRouteAction: RouteAction<MyPageRoute>,
    localUser: LocalUserData,
    investmentStats: InvestmentStats = InvestmentStats(),
    recentActivities: List<RecentActivity> = emptyList(),
    monthlyGoal: MonthlyGoal = MonthlyGoal(),
    onSetGoal: (Long) -> Unit = {},
    showOnboarding: () -> Unit,
    badges: List<BadgeInfo>,
    isPremium: Boolean = false
) {
    val context = LocalContext.current

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FA))
    ) {
        // 프로필 헤더
        item {
            ProfileHeader(
                localUser = localUser,
                onProfileClick = { myPageRouteAction.navTo(MyPageRoute.CreateUser) }
            )
        }

        // 프리미엄 구매/상태 카드
        item {
            PremiumPurchaseCard(
                isPremium = isPremium,
                onPurchaseClick = {
                    myPageRouteAction.navTo(MyPageRoute.Premium)
                },
                onSettingsClick = {
                    myPageRouteAction.navTo(MyPageRoute.Premium)
                }
            )
        }

        // 투자 현황 대시보드
        item {
            InvestmentDashboard(stats = investmentStats)
        }

        // 이번 달 목표
        item {
            MonthlyGoalSection(
                goal = monthlyGoal,
                onSetGoal = onSetGoal
            )
        }

        // 최근 활동
        item {
            RecentActivitySection(activities = recentActivities)
        }

        // 나의 뱃지
        item {
            BadgeSection(badges = badges)
        }

        item {
            Spacer(modifier = Modifier.height(32.dp))
        }

        // 설정 메뉴
        item {
            SettingSection(
                onAccountManageClick = { myPageRouteAction.navTo(MyPageRoute.CreateUser) },
                onCloudServiceClick = { myPageRouteAction.navTo(MyPageRoute.CloudService) },
                onCustomerServiceClick = { myPageRouteAction.navTo(MyPageRoute.CustomerServiceCenter) },
                onWidgetSettingsClick = { myPageRouteAction.navTo(MyPageRoute.WidgetSettings) }
            )
        }

        item {
            Spacer(modifier = Modifier.height(32.dp))
        }

        // 일반 사용자에게만 프리미엄 혜택 상세 표시
        if (!isPremium) {
            item {
                PremiumBenefitsDetailCard()
            }
        }

        item {
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

/**
 * 프리미엄 구매/상태 카드
 */
@Composable
fun PremiumPurchaseCard(
    isPremium: Boolean,
    onPurchaseClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isPremium) Color(0xFFFEF3C7) else Color(0xFF6366F1)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        if (isPremium) {
            PremiumActiveContent(onSettingsClick = onSettingsClick)
        } else {
            PremiumPromotionContent(onPurchaseClick = onPurchaseClick)
        }
    }
}

/**
 * 프리미엄 활성 상태
 */
@Composable
fun PremiumActiveContent(onSettingsClick: () -> Unit) {
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
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Star,
                    contentDescription = null,
                    modifier = Modifier.size(32.dp),
                    tint = Color(0xFFF59E0B)
                )

                Column {
                    Text(
                        text = "프리미엄 사용 중",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF92400E)
                    )

                    Text(
                        text = "모든 기능 이용 가능",
                        fontSize = 13.sp,
                        color = Color(0xFFB45309)
                    )
                }
            }

            IconButton(
                onClick = onSettingsClick,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Settings,
                    contentDescription = "설정",
                    tint = Color(0xFF92400E)
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        PremiumBenefitsSummary(isCompact = true, textColor = Color(0xFF92400E))
    }
}

/**
 * 프리미엄 구매 프로모션
 */
@Composable
fun PremiumPromotionContent(onPurchaseClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Rounded.Star,
            contentDescription = null,
            modifier = Modifier.size(56.dp),
            tint = Color(0xFFFBBF24)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "프리미엄으로 업그레이드",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "광고 없이 모든 기능을 사용하세요",
            fontSize = 14.sp,
            color = Color.White.copy(alpha = 0.9f),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(20.dp))

        PremiumBenefitsSummary(isCompact = true, textColor = Color.White)

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = onPurchaseClick,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.White,
                contentColor = Color(0xFF6366F1)
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Star,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "자세히 보기",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        }
    }
}

/**
 * 프리미엄 혜택 요약
 */
@Composable
fun PremiumBenefitsSummary(
    isCompact: Boolean = true,
    textColor: Color = Color.White
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(if (isCompact) 8.dp else 12.dp)
    ) {
        PremiumBenefitItem(
            icon = Icons.Rounded.Block,
            text = "모든 광고 제거",
            textColor = textColor
        )
        PremiumBenefitItem(
            icon = Icons.Rounded.Language,
            text = "12개 모든 통화 기록 사용",
            textColor = textColor
        )
        PremiumBenefitItem(
            icon = Icons.Rounded.FlashOn,
            text = "위젯 실시간 업데이트",
            textColor = textColor
        )
        PremiumBenefitItem(
            icon = Icons.Rounded.CloudDone,
            text = "클라우드 자동 백업",
            textColor = textColor
        )
    }
}

/**
 * 프리미엄 혜택 항목
 */
@Composable
fun PremiumBenefitItem(
    icon: ImageVector,
    text: String,
    textColor: Color
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = textColor.copy(alpha = 0.9f)
        )

        Text(
            text = text,
            fontSize = 14.sp,
            color = textColor.copy(alpha = 0.9f),
            fontWeight = FontWeight.Medium
        )
    }
}

/**
 * 프리미엄 혜택 상세 카드
 */
@Composable
fun PremiumBenefitsDetailCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "프리미엄 혜택",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1F2937)
            )

            PremiumBenefitDetailItem(
                icon = Icons.Rounded.Block,
                title = "모든 광고 제거",
                description = "분석 화면, 목표 환율 설정, 배너 광고 완전 제거"
            )

            HorizontalDivider(color = Color(0xFFE5E7EB))

            PremiumBenefitDetailItem(
                icon = Icons.Rounded.Language,
                title = "12개 모든 통화 사용",
                description = "USD, JPY 외 10개 추가 통화 기록 가능"
            )

            HorizontalDivider(color = Color(0xFFE5E7EB))

            PremiumBenefitDetailItem(
                icon = Icons.Rounded.FlashOn,
                title = "위젯 실시간 업데이트",
                description = "WebSocket 실시간 연결로 즉시 환율 반영"
            )

            HorizontalDivider(color = Color(0xFFE5E7EB))

            PremiumBenefitDetailItem(
                icon = Icons.Rounded.CloudDone,
                title = "클라우드 자동 백업",
                description = "수동 백업 → 실시간 자동 백업으로 데이터 안전 보장"
            )
        }
    }
}

/**
 * 프리미엄 혜택 상세 항목
 */
@Composable
fun PremiumBenefitDetailItem(
    icon: ImageVector,
    title: String,
    description: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = Color(0xFFEEF2FF)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier
                    .padding(12.dp)
                    .size(24.dp),
                tint = Color(0xFF6366F1)
            )
        }

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1F2937)
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = description,
                fontSize = 13.sp,
                color = Color(0xFF6B7280),
                lineHeight = 18.sp
            )
        }
    }
}

@Composable
fun ProfileHeader(
    localUser: LocalUserData,
    onProfileClick: () -> Unit
) {
    val isSocialLinked = localUser.socialType != "NONE" && localUser.socialId != null
    val displayName = when {
        !localUser.nickname.isNullOrEmpty() -> localUser.nickname!!
        isSocialLinked -> localUser.email?.substringBefore("@") ?: "사용자"
        else -> "게스트 사용자"
    }
    val shortId = localUser.id.toString().take(8)
    val gradientColors = listOf(Color(0xFF667EEA), Color(0xFF764BA2))

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clickable { onProfileClick() },
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.horizontalGradient(gradientColors))
                .padding(24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .background(
                                color = Color.White.copy(alpha = 0.3f),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Person,
                            contentDescription = "Profile",
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = displayName,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )

                            if (!isSocialLinked) {
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = Color.White.copy(alpha = 0.3f)
                                ) {
                                    Text(
                                        text = "게스트",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Color.White,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }

                        Text(
                            text = "ID: $shortId...",
                            fontSize = 13.sp,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                }

                Icon(
                    imageVector = Icons.Rounded.ChevronRight,
                    contentDescription = "Navigate",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
fun InvestmentDashboard(stats: InvestmentStats) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Text(
            text = "나의 투자 현황",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1F2937),
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatCard(
                modifier = Modifier.weight(1f),
                title = "총 투자금",
                value = stats.totalInvestment,
                icon = Icons.Rounded.AccountBalance,
                color = Color(0xFF6366F1)
            )

            StatCard(
                modifier = Modifier.weight(1f),
                title = "예상 수익",
                value = stats.expectedProfit,
                subtitle = stats.profitRate,
                icon = Icons.Rounded.TrendingUp,
                color = if (stats.expectedProfit.contains("-")) Color(0xFFEF4444) else Color(0xFF10B981)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        TradeSummaryCard(
            totalTrades = stats.totalTrades,
            buyCount = stats.buyCount,
            sellCount = stats.sellCount
        )
    }
}

@Composable
fun StatCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    subtitle: String? = null,
    icon: ImageVector,
    color: Color
) {
    Card(
        modifier = modifier.height(140.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(color.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = color,
                    modifier = Modifier.size(24.dp)
                )
            }

            Column {
                Text(
                    text = title,
                    fontSize = 12.sp,
                    color = Color.Gray
                )
                Text(
                    text = value,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1F2937)
                )
                subtitle?.let {
                    Text(
                        text = it,
                        fontSize = 12.sp,
                        color = color,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
fun TradeSummaryCard(
    totalTrades: Int,
    buyCount: Int,
    sellCount: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            TradeStatItem(
                label = "총 거래",
                value = totalTrades.toString(),
                color = Color(0xFF6366F1)
            )
            Divider(
                modifier = Modifier
                    .height(40.dp)
                    .width(1.dp),
                color = Color.LightGray
            )
            TradeStatItem(
                label = "매수",
                value = buyCount.toString(),
                color = Color(0xFFEF4444)
            )
            Divider(
                modifier = Modifier
                    .height(40.dp)
                    .width(1.dp),
                color = Color.LightGray
            )
            TradeStatItem(
                label = "매도",
                value = sellCount.toString(),
                color = Color(0xFF3B82F6)
            )
        }
    }
}

@Composable
fun TradeStatItem(
    label: String,
    value: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            fontSize = 12.sp,
            color = Color.Gray
        )
    }
}

@Composable
fun MonthlyGoalSection(
    goal: MonthlyGoal,
    onSetGoal: (Long) -> Unit
) {
    var showGoalDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(top = 24.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Flag,
                    contentDescription = "Goal",
                    tint = Color(0xFFEF4444),
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = "이번 달 목표",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1F2937)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (!goal.isSet || goal.goalAmount == 0L) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "이번 달 목표를 설정해보세요!",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = { showGoalDialog = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF6366F1)
                        )
                    ) {
                        Text("목표 설정하기")
                    }
                }
            } else {
                Text(
                    text = "₩%,d".format(goal.goalAmount),
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF6366F1)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "달성률: ${(goal.progress * 100).toInt()}%",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                        Text(
                            text = "₩%,d / ₩%,d".format(goal.currentAmount, goal.goalAmount),
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    LinearProgressIndicator(
                        progress = goal.progress,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = Color(0xFF10B981),
                        trackColor = Color(0xFFE5E7EB)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                TextButton(
                    onClick = { showGoalDialog = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("목표 수정하기", color = Color(0xFF6366F1))
                }
            }
        }
    }

    if (showGoalDialog) {
        GoalSettingDialog(
            currentGoal = goal.goalAmount,
            onDismiss = { showGoalDialog = false },
            onConfirm = { newGoal ->
                onSetGoal(newGoal)
                showGoalDialog = false
            }
        )
    }
}

@Composable
fun GoalSettingDialog(
    currentGoal: Long,
    onDismiss: () -> Unit,
    onConfirm: (Long) -> Unit
) {
    var goalText by remember { mutableStateOf(if (currentGoal > 0) currentGoal.toString() else "") }
    var selectedQuickAmount by remember { mutableStateOf<Long?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "이번 달 수익 목표",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1F2937)
                        )
                        Text(
                            text = "달성하고 싶은 금액을 설정하세요",
                            fontSize = 13.sp,
                            color = Color.Gray,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = "닫기",
                            tint = Color.Gray
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "빠른 선택",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF6B7280),
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(
                        100000L to "10만원",
                        300000L to "30만원"
                    ).forEach { (amount, label) ->
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (selectedQuickAmount == amount)
                                    Color(0xFF6366F1) else Color(0xFFF3F4F6)
                            ),
                            onClick = {
                                selectedQuickAmount = amount
                                goalText = amount.toString()
                            }
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = if (selectedQuickAmount == amount)
                                        Color.White else Color(0xFF374151)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(
                        500000L to "50만원",
                        1000000L to "100만원"
                    ).forEach { (amount, label) ->
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (selectedQuickAmount == amount)
                                    Color(0xFF6366F1) else Color(0xFFF3F4F6)
                            ),
                            onClick = {
                                selectedQuickAmount = amount
                                goalText = amount.toString()
                            }
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = if (selectedQuickAmount == amount)
                                        Color.White else Color(0xFF374151)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    HorizontalDivider(modifier = Modifier.weight(1f), color = Color(0xFFE5E7EB))
                    Text(
                        text = "또는",
                        fontSize = 12.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )
                    HorizontalDivider(modifier = Modifier.weight(1f), color = Color(0xFFE5E7EB))
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "직접 입력",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF6B7280),
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                OutlinedTextField(
                    value = goalText,
                    onValueChange = {
                        if (it.isEmpty() || it.all { char -> char.isDigit() }) {
                            goalText = it
                            selectedQuickAmount = null
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = {
                        Text(
                            "원하는 금액 입력",
                            color = Color(0xFF9CA3AF)
                        )
                    },
                    prefix = {
                        Text(
                            "₩",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF6366F1)
                        )
                    },
                    suffix = {
                        Text(
                            "원",
                            color = Color.Gray
                        )
                    },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF6366F1),
                        unfocusedBorderColor = Color(0xFFE5E7EB),
                        focusedContainerColor = Color(0xFFF9FAFB),
                        unfocusedContainerColor = Color(0xFFF9FAFB)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    textStyle = TextStyle(
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium
                    )
                )

                if (goalText.isNotEmpty() && goalText.toLongOrNull() != null) {
                    val amount = goalText.toLong()
                    Spacer(modifier = Modifier.height(12.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFEEF2FF)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "설정될 목표",
                                fontSize = 13.sp,
                                color = Color(0xFF6366F1)
                            )
                            Text(
                                text = "₩%,d".format(amount),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF6366F1)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Color(0xFFE5E7EB))
                    ) {
                        Text(
                            "취소",
                            fontSize = 15.sp,
                            color = Color(0xFF6B7280)
                        )
                    }

                    Button(
                        onClick = {
                            val amount = goalText.toLongOrNull() ?: 0L
                            if (amount > 0) {
                                onConfirm(amount)
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp),
                        enabled = goalText.isNotEmpty() &&
                                goalText.toLongOrNull() != null &&
                                goalText.toLong() > 0,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF6366F1),
                            disabledContainerColor = Color(0xFFE5E7EB)
                        )
                    ) {
                        Text(
                            "설정하기",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun RecentActivitySection(activities: List<RecentActivity>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(top = 24.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.History,
                    contentDescription = "Recent",
                    tint = Color(0xFF6366F1),
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = "최근 활동",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1F2937)
                )
            }

            TextButton(onClick = { /* 전체 보기 */ }) {
                Text("전체보기", color = Color(0xFF6366F1), fontSize = 14.sp)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            if (activities.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "아직 거래 내역이 없습니다",
                        color = Color.Gray,
                        fontSize = 14.sp
                    )
                }
            } else {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    activities.forEachIndexed { index, activity ->
                        ActivityTimelineItem(activity)
                        if (index < activities.size - 1) {
                            HorizontalDivider(color = Color(0xFFE5E7EB))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ActivityTimelineItem(activity: RecentActivity) {
    val displayDate = formatActivityDate(activity.date)
    val currencyIcon = if (activity.currencyType == "USD") "💵" else "💴"
    val typeText = "${activity.currencyType} ${if (activity.isBuy) "매수" else "매도"}"

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        if (activity.isBuy) Color(0xFFEF4444).copy(alpha = 0.1f)
                        else Color(0xFF3B82F6).copy(alpha = 0.1f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (activity.isBuy) Icons.Rounded.ArrowUpward
                    else Icons.Rounded.ArrowDownward,
                    contentDescription = typeText,
                    tint = if (activity.isBuy) Color(0xFFEF4444) else Color(0xFF3B82F6),
                    modifier = Modifier.size(20.dp)
                )
            }

            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(text = currencyIcon, fontSize = 14.sp)
                    Text(
                        text = typeText,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF1F2937)
                    )
                }
                Text(
                    text = activity.amount,
                    fontSize = 12.sp,
                    color = Color.Gray
                )
                activity.profit?.let { profit ->
                    Text(
                        text = "수익: $profit",
                        fontSize = 11.sp,
                        color = if (profit.contains("-")) Color(0xFFEF4444) else Color(0xFF10B981),
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = displayDate,
                fontSize = 12.sp,
                color = Color.Gray
            )
            Text(
                text = activity.date,
                fontSize = 11.sp,
                color = Color.LightGray
            )
        }
    }
}

private fun formatActivityDate(dateString: String): String {
    return try {
        val today = LocalDate.now()
        val activityDate = LocalDate.parse(dateString)
        val daysBetween = ChronoUnit.DAYS.between(activityDate, today).toInt()

        when (daysBetween) {
            0 -> "오늘"
            1 -> "어제"
            in 2..7 -> "${daysBetween}일 전"
            else -> dateString
        }
    } catch (e: Exception) {
        dateString
    }
}

@Composable
fun BadgeSection(badges: List<BadgeInfo>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(top = 24.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.EmojiEvents,
                    contentDescription = "Badges",
                    tint = Color(0xFFF59E0B),
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = "나의 뱃지",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1F2937)
                )
            }

            val unlockedCount = badges.count { it.isUnlocked }
            Text(
                text = "$unlockedCount/${badges.size}",
                fontSize = 14.sp,
                color = Color(0xFF6366F1),
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            if (badges.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "뱃지를 수집해보세요!",
                        color = Color.Gray
                    )
                }
            } else {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    badges.forEach { badge ->
                        BadgeItemNew(badge)
                    }
                }
            }
        }
    }
}

@Composable
fun BadgeItemNew(badge: BadgeInfo) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = badge.icon,
                fontSize = 32.sp,
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        if (badge.isUnlocked) Color(0xFFFEF3C7) else Color(0xFFF3F4F6),
                        RoundedCornerShape(12.dp)
                    )
                    .wrapContentSize(Alignment.Center)
            )

            Column {
                Text(
                    text = badge.title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (badge.isUnlocked) Color(0xFF1F2937) else Color.Gray
                )
                Text(
                    text = badge.description,
                    fontSize = 12.sp,
                    color = Color.Gray
                )
                if (!badge.isUnlocked && badge.progress > 0) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        LinearProgressIndicator(
                            progress = badge.progress / 100f,
                            modifier = Modifier
                                .width(100.dp)
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp)),
                            color = Color(0xFF6366F1),
                            trackColor = Color(0xFFE5E7EB)
                        )
                        Text(
                            text = "${badge.currentValue}/${badge.targetValue}",
                            fontSize = 11.sp,
                            color = Color(0xFF6366F1),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        if (badge.isUnlocked) {
            Icon(
                imageVector = Icons.Rounded.CheckCircle,
                contentDescription = "Unlocked",
                tint = Color(0xFF10B981),
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
fun SettingSection(
    onAccountManageClick: () -> Unit,
    onCloudServiceClick: () -> Unit,
    onCustomerServiceClick: () -> Unit,
    onWidgetSettingsClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            SettingItem(
                icon = Icons.Rounded.Widgets,
                title = "위젯 설정",
                subtitle = "실시간 업데이트 설정",
                onClick = onWidgetSettingsClick
            )
            HorizontalDivider(color = Color(0xFFE5E7EB))
            SettingItem(
                icon = Icons.Rounded.Person,
                title = "계정 관리",
                subtitle = "소셜 로그인 연동",
                onClick = onAccountManageClick
            )
            HorizontalDivider(color = Color(0xFFE5E7EB))
            SettingItem(
                icon = Icons.Rounded.Cloud,
                title = "클라우드 백업",
                subtitle = "데이터 동기화",
                onClick = onCloudServiceClick
            )
            HorizontalDivider(color = Color(0xFFE5E7EB))
            SettingItem(
                icon = Icons.Rounded.Help,
                title = "고객센터",
                subtitle = "문의하기",
                onClick = onCustomerServiceClick
            )
        }
    }
}

@Composable
fun SettingItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = Color(0xFF6366F1),
                modifier = Modifier.size(24.dp)
            )
            Column {
                Text(
                    text = title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF1F2937)
                )
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
        }

        Icon(
            imageVector = Icons.Rounded.ChevronRight,
            contentDescription = "Navigate",
            tint = Color.Gray,
            modifier = Modifier.size(20.dp)
        )
    }
}