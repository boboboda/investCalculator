package com.bobodroid.myapplication.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.bobodroid.myapplication.components.Dialogs.OnboardingTooltipDialog
import com.bobodroid.myapplication.models.datamodels.roomDb.LocalUserData
import com.bobodroid.myapplication.models.viewmodels.BadgeInfo
import com.bobodroid.myapplication.models.viewmodels.InvestmentStats
import com.bobodroid.myapplication.models.viewmodels.MonthlyGoal
import com.bobodroid.myapplication.models.viewmodels.MyPageViewModel
import com.bobodroid.myapplication.models.viewmodels.RecentActivity
import com.bobodroid.myapplication.routes.MyPageRoute
import com.bobodroid.myapplication.routes.RouteAction
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.util.*

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
                    investmentStats = uiState.investmentStats,  // ⭐ 통계 전달
                    recentActivities = uiState.recentActivities, // ⭐ 활동 전달
                    monthlyGoal = uiState.monthlyGoal,          // ⭐ 목표 전달
                    onSetGoal = { amount -> myPageViewModel.setMonthlyGoal(amount) },  // ⭐ 목표 설정
                    showOnboarding = {
                        showOnboarding = true
                    },
                    badges = uiState.badges
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
                    }
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

        // 온보딩 다이얼로그
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
    investmentStats: InvestmentStats = InvestmentStats(),  // ⭐ 통계 데이터 추가
    recentActivities: List<RecentActivity> = emptyList(),   // ⭐ 활동 데이터 추가
    monthlyGoal: MonthlyGoal = MonthlyGoal(),              // ⭐ 목표 데이터 추가
    onSetGoal: (Long) -> Unit = {}, // ⭐ 목표 설정 콜백
    showOnboarding: () -> Unit,
    badges: List<BadgeInfo>,
    ) {



    val gradientColors = listOf(
        Color(0xFF667EEA),
        Color(0xFF764BA2)
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FA))
    ) {
        // 프로필 헤더
        item {
            ProfileHeader(
                localUser = localUser,
                onProfileClick = {myPageRouteAction.navTo(MyPageRoute.CreateUser)}
            )
        }

        // 투자 현황 대시보드
        item {
            InvestmentDashboard(stats = investmentStats)  // ⭐ 통계 전달
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
            RecentActivitySection(activities = recentActivities)  // ⭐ 활동 전달
        }

        // 나의 뱃지
        item {
            BadgeSection(badges = badges)
        }

        // 설정 메뉴
        item {
            SettingsSection(
                onCustomerServiceClick = { /* 고객센터 */ },
                onShowOnboardingClick = {
                    showOnboarding()
                     }
            )
        }

        item {
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun ProfileHeader(
    localUser: LocalUserData,
    onProfileClick: () -> Unit  // AccountManageView로 이동하는 콜백
) {
    // 소셜 연동 여부 확인
    val isSocialLinked = localUser.socialType != "NONE" && localUser.socialId != null

    // 표시할 이름 결정
    val displayName = when {
        !localUser.nickname.isNullOrEmpty() -> localUser.nickname!!
        isSocialLinked -> localUser.email?.substringBefore("@") ?: "사용자"
        else -> "게스트 사용자"
    }

    // UUID 축약 (첫 8자리)
    val shortId = localUser.id.toString().take(8)

    val gradientColors = listOf(
        Color(0xFF667EEA),
        Color(0xFF764BA2)
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clickable { onProfileClick() },  // 카드 전체 클릭 가능
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
                // 왼쪽: 프로필 정보
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    // 프로필 아이콘
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

                    // 사용자 정보
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

                            // 게스트 뱃지
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

                        // ID 표시 (축약)
                        Text(
                            text = "ID: $shortId...",
                            fontSize = 13.sp,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                }

                // 오른쪽: 화살표 아이콘
                Icon(
                    imageVector = Icons.Rounded.ChevronRight,
                    contentDescription = "자세히 보기",
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}

@Composable
fun InvestmentDashboard(stats: InvestmentStats) {  // ⭐ 파라미터 추가
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
                value = stats.totalInvestment,  // ⭐ 실제 데이터
                icon = Icons.Rounded.AccountBalance,
                color = Color(0xFF6366F1)
            )

            StatCard(
                modifier = Modifier.weight(1f),
                title = "예상 수익",
                value = stats.expectedProfit,  // ⭐ 실제 데이터
                subtitle = stats.profitRate,   // ⭐ 실제 데이터
                icon = Icons.Rounded.TrendingUp,
                color = if (stats.expectedProfit.contains("-")) Color(0xFFEF4444) else Color(0xFF10B981)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        TradeSummaryCard(
            totalTrades = stats.totalTrades,  // ⭐ 실제 데이터
            buyCount = stats.buyCount,        // ⭐ 실제 데이터
            sellCount = stats.sellCount       // ⭐ 실제 데이터
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
                // 목표 미설정 상태
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
                // 목표 설정됨
                Text(
                    text = "₩%,d".format(goal.goalAmount),
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF6366F1)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // 진행률 바
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

    // 목표 설정 다이얼로그
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
                // 헤더
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

                // 빠른 선택 버튼들
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

                // 구분선
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

                // 직접 입력
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

                // 미리보기
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

                // 버튼
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
fun RecentActivitySection(activities: List<RecentActivity>) {  // ⭐ 파라미터 추가
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
                // 활동이 없을 때
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
fun ActivityTimelineItem(activity: RecentActivity) {  // ⭐ RecentActivity 사용
    // 날짜 포맷팅 (yyyy-MM-dd → 며칠 전)
    val displayDate = formatActivityDate(activity.date)

    // 통화 아이콘
    val currencyIcon = if (activity.currencyType == "USD") "💵" else "💴"

    // 거래 타입 텍스트
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
                // 매도인 경우 수익 표시
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

// 날짜를 "오늘", "어제", "3일 전" 형식으로 변환
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

// BadgeSection 함수 전체를 이것으로 교체:

@Composable
fun BadgeSection(badges: List<BadgeInfo>) {  // ⭐ 파라미터 추가
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

            // ⭐ 달성 개수
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
                        BadgeItemNew(badge)  // ⭐ 새 함수 사용
                    }
                }
            }
        }
    }
}

// ⭐ 새로운 BadgeItem
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
fun SettingsSection(
    onCustomerServiceClick: () -> Unit,
    onShowOnboardingClick: () -> Unit  // 🎯 새로 추가
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(top = 24.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.Settings,
                contentDescription = "Settings",
                tint = Color(0xFF6366F1),
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = "설정",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1F2937)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            Column {
                // 🎯 사용법 다시보기 추가
                SettingItem(
                    icon = Icons.Rounded.Help,
                    title = "사용법 다시보기",
                    subtitle = "스와이프 기능 안내",
                    onClick = onShowOnboardingClick
                )

                HorizontalDivider(color = Color(0xFFE5E7EB))

                SettingItem(
                    icon = Icons.Rounded.Palette,
                    title = "테마 변경",
                    subtitle = "라이트 / 다크",
                    onClick = { /* TODO */ }
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