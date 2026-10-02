// app/src/main/java/com/bobodroid/myapplication/screens/PremiumScreen.kt
package com.bobodroid.myapplication.screens

import android.app.Activity
import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.android.billingclient.api.ProductDetails
import com.bobodroid.myapplication.BuildConfig
import com.bobodroid.myapplication.billing.BillingClientLifecycle
import com.bobodroid.myapplication.components.SocialLoginWarningBanner
import com.bobodroid.myapplication.models.datamodels.roomDb.LocalUserData
import com.bobodroid.myapplication.models.datamodels.roomDb.PremiumType
import com.bobodroid.myapplication.models.viewmodels.PremiumViewModel
import com.bobodroid.myapplication.models.viewmodels.SharedViewModel
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.Instant

/**
 * 프리미엄 화면
 *
 * ⚠️ 신규 구독 결제 진입점(SubscriptionPlansCard)은 제거됨 — 사업자 이슈로 신규 구독 판매 중단.
 * 기존 구독자는 서버에서 자연 만료될 때까지 그대로 유지되고, "구독 복원" 카드만 남겨둠
 * (다른 기기에서 예전에 구매한 구독을 이 기기로 복원하는 용도).
 * 신규 유저는 리워드 광고로만 프리미엄을 얻을 수 있음.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PremiumScreen(
    onBackClick: () -> Unit,
    onAccountManageClick: () -> Unit = {},
    viewModel: PremiumViewModel = hiltViewModel(),
    sharedViewModel: SharedViewModel = hiltViewModel() // ✅ 전역 상태
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val uiState by viewModel.uiState.collectAsState()
    val user by sharedViewModel.user.collectAsState()
    val isPremium by sharedViewModel.isPremium.collectAsState()
    val premiumType by sharedViewModel.premiumType.collectAsState()
    val premiumExpiryDate by sharedViewModel.premiumExpiryDate.collectAsState()
    val adUiState by sharedViewModel.adUiState.collectAsState()
    val rewardAdInfo by sharedViewModel.rewardAdInfo.collectAsState()

    val isLoading = uiState.isLoading

    // ✅ 화면 진입 시 서버 기준 최신 프리미엄 상태 재조회 (구독 + 리워드 통합)
    LaunchedEffect(Unit) {
        sharedViewModel.refreshPremiumStatus()
    }

    LaunchedEffect(Unit) {
        sharedViewModel.snackbarEvent.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    Scaffold(
        // ✅ 상태바 여백은 AppScreen 에서 이미 적용됨 → 이중 적용 방지
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                windowInsets = WindowInsets(0, 0, 0, 0),
                title = { Text("프리미엄") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, "뒤로가기")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF6366F1),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (!user.socialId.isNullOrEmpty()) {
                val isSocialLinked = user.socialType != "NONE" && !user.socialId.isNullOrEmpty()
                if (!isSocialLinked) {
                    SocialLoginWarningBanner(
                        isSocialLinked = isSocialLinked,
                        onLinkClick = onAccountManageClick
                    )
                }
            }

            if (isPremium) {
                // ✅ 프리미엄 사용자 - 활성 상태 표시
                PremiumActiveCard(
                    premiumType = premiumType,
                    premiumExpiryDate = premiumExpiryDate
                )

                // ✅ 리워드 프리미엄인 경우, 추가 시청으로 더 연장할 수 있게 카드 유지
                if (premiumType == PremiumType.REWARD_AD) {
                    RewardAdCard(
                        canWatchToday = adUiState.rewardAdState,
                        todayCount = rewardAdInfo?.todayRewardCount,
                        dailyCap = rewardAdInfo?.dailyRewardCap,
                        onWatchAd = {
                            // ✅ 바로 광고 호출 대신 확인 다이얼로그부터 (AnalysisScreen.kt와 동일 패턴)
                            sharedViewModel.showRewardAdDialog()
                        }
                    )
                }
            } else {
                // ✅ 일반 사용자
                PremiumHeroCard()

                // ✅ 리워드 광고로 프리미엄 받기 (신규 구독 결제 대신 메인 경로)
                RewardAdCard(
                    canWatchToday = adUiState.rewardAdState,
                    todayCount = rewardAdInfo?.todayRewardCount,
                    dailyCap = rewardAdInfo?.dailyRewardCap,
                    onWatchAd = {
                        sharedViewModel.showRewardAdDialog()
                    }
                )

                // ⚠️ 구독 복원 카드 제거됨 — 실제 구독자가 없어 복원할 대상 자체가 없음
                // ⚠️ 신규 구독 결제 카드(SubscriptionPlansCard)는 제거됨 — 더 이상 신규 판매 안 함

                PremiumBenefitsCard()
            }


            if (BuildConfig.DEBUG) {
                TestControlCard(
                    isPremium = isPremium,
                    user = user,
                    rewardCount = rewardAdInfo?.todayRewardCount,
                    rewardCap = rewardAdInfo?.dailyRewardCap,
                    onResetPremium = { sharedViewModel.debugResetPremium() },
                    onResetDailyAdCount = { sharedViewModel.debugResetDailyAdCount() },
                    onGrantPremium = { sharedViewModel.debugGrantPremium(1) },
                    onRefreshStatus = { sharedViewModel.refreshPremiumStatus() }
                )
            }
        }
    }
}

/**
 * ✅ 신규: 리워드 광고로 프리미엄 받기/연장하기 카드
 */
@Composable
fun RewardAdCard(
    canWatchToday: Boolean,
    onWatchAd: () -> Unit,
    todayCount: Int? = null,
    dailyCap: Int? = null
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF7ED)),
        border = BorderStroke(1.dp, Color(0xFFFDBA74))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.PlayCircle,
                    contentDescription = null,
                    tint = Color(0xFFF97316),
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "광고 보고 프리미엄 받기",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF9A3412)
                )
            }

            Text(
                text = "짧은 광고 한 편이면 프리미엄이 연장됩니다.",
                fontSize = 14.sp,
                color = Color(0xFF9A3412),
                lineHeight = 20.sp
            )

            if (todayCount != null && dailyCap != null) {
                Text(
                    text = "오늘 시청 $todayCount / $dailyCap 회",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFFC2410C)
                )
            }

            Button(
                onClick = onWatchAd,
                enabled = canWatchToday,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFF97316)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    Icons.Rounded.PlayCircle,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    if (canWatchToday) "광고 보고 프리미엄 받기" else "오늘 시청 횟수를 다 채웠어요",
                    fontSize = 15.sp
                )
            }
        }
    }
}

/**
 * 프리미엄 활성 상태 카드
 */
@Composable
fun PremiumActiveCard(
    premiumType: PremiumType,
    premiumExpiryDate: String?
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF10B981)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Rounded.CheckCircle,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = when (premiumType) {
                    // ⚠️ SUBSCRIPTION 분기 제거 — 실제 구독자가 없어 도달할 일 없음
                    PremiumType.REWARD_AD -> Color(0xFFF97316)
                    PremiumType.LIFETIME -> Color(0xFF8B5CF6)
                    else -> Color(0xFF6B7280)
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = when (premiumType) {
                    PremiumType.REWARD_AD -> "리워드 프리미엄 이용 중"
                    PremiumType.LIFETIME -> "평생 이용권"
                    else -> "프리미엄 활성"
                },
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (premiumExpiryDate != null && premiumType == PremiumType.REWARD_AD) {
                val daysRemaining = calculateDaysRemaining(premiumExpiryDate)
                Text(
                    text = "남은 기간: ${daysRemaining}일",
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.9f)
                )
            }
        }
    }
}

/**
 * 프리미엄 히어로 카드
 */
@Composable
fun PremiumHeroCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF6366F1)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Rounded.Star,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = Color(0xFFFCD34D)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "프리미엄으로 업그레이드",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "광고 없는 쾌적한 환경과\n모든 기능을 자유롭게",
                fontSize = 14.sp,
                color = Color.White.copy(alpha = 0.9f),
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * 프리미엄 혜택 카드 - 전체 혜택 표시
 */
@Composable
fun PremiumBenefitsCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "프리미엄 혜택",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

            val benefits = listOf(
                Triple(Icons.Rounded.Block, "광고 없는 환경", "모든 광고 완전 제거"),
                Triple(Icons.Rounded.Public, "12개 통화 지원", "USD, JPY, EUR 등 주요 통화"),
                Triple(Icons.Rounded.Notifications, "실시간 환율 알림", "목표 환율 도달 시 즉시 알림"),
                Triple(Icons.Rounded.TrendingUp, "수익률 알림", "기록별 목표 수익률 달성 알림"),
                Triple(Icons.Rounded.CalendarToday, "일일 리포트", "매일 수익 현황 요약 알림"),
                Triple(Icons.Rounded.Update, "매수 경과 알림", "장기 보유 기록 리마인더"),
                Triple(Icons.Rounded.Sync, "다중 기기 동기화", "모든 기기에서 실시간 동기화"),
                Triple(Icons.Rounded.Analytics, "고급 분석", "상세한 수익률 및 통계"),
                Triple(Icons.Rounded.Widgets, "위젯 실시간 업데이트", "백그라운드에서 자동 환율 갱신")
            )

            benefits.forEach { (icon, title, desc) ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = Color(0xFF6366F1),
                        modifier = Modifier.size(24.dp)
                    )

                    Column {
                        Text(
                            text = title,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = desc,
                            fontSize = 13.sp,
                            color = Color.Gray
                        )
                    }
                }
            }
        }
    }
}

/**
 * ✅ 개발자 도구 카드 (디버그 빌드 전용)
 * - 모든 버튼은 서버 값을 직접 바꾼 뒤 앱에 반영함
 */
@Composable
fun TestControlCard(
    isPremium: Boolean,
    user: LocalUserData,
    rewardCount: Int?,
    rewardCap: Int?,
    onResetPremium: () -> Unit,
    onResetDailyAdCount: () -> Unit,
    onGrantPremium: () -> Unit,
    onRefreshStatus: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF111827))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 헤더
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.BugReport,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = Color(0xFFFBBF24)
                    )
                    Text(
                        text = "개발자 도구",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0xFF374151)
                ) {
                    Text(
                        text = "DEBUG · 서버 연동",
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        color = Color(0xFFFBBF24),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            // 현재 상태
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF1F2937), RoundedCornerShape(12.dp))
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DevStatusRow(
                    label = "상태",
                    value = if (isPremium) "프리미엄" else "일반",
                    valueColor = if (isPremium) Color(0xFF34D399) else Color(0xFF9CA3AF)
                )
                DevStatusRow(label = "타입", value = user.premiumType)
                DevStatusRow(label = "만료", value = user.premiumExpiryDate ?: "-")
                DevStatusRow(
                    label = "오늘 리워드",
                    value = if (rewardCount != null && rewardCap != null) "$rewardCount / $rewardCap" else "-"
                )
            }

            // 프리미엄 초기화
            Button(
                onClick = onResetPremium,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
            ) {
                Icon(Icons.Rounded.RestartAlt, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("프리미엄 초기화", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
            }

            // 하루 광고 횟수 초기화
            Button(
                onClick = onResetDailyAdCount,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6))
            ) {
                Icon(Icons.Rounded.Replay, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("하루 광고 횟수 초기화", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
            }

            // 테스트 프리미엄 지급
            OutlinedButton(
                onClick = onGrantPremium,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Color(0xFF4B5563))
            ) {
                Text("1분 프리미엄 지급", fontSize = 14.sp, color = Color(0xFFE5E7EB))
            }

            TextButton(
                onClick = onRefreshStatus,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("서버 상태 새로고침", fontSize = 13.sp, color = Color(0xFF9CA3AF))
            }

            Text(
                text = "하루 광고 횟수 초기화는 리워드 시청 횟수(서버)와 전면광고 카운트(앱)를 함께 0으로 되돌립니다.",
                fontSize = 12.sp,
                lineHeight = 17.sp,
                color = Color(0xFF6B7280)
            )
        }
    }
}

@Composable
private fun DevStatusRow(
    label: String,
    value: String,
    valueColor: Color = Color.White
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            color = Color(0xFF9CA3AF)
        )
        Text(
            text = value,
            fontSize = 13.sp,
            fontFamily = FontFamily.Monospace,
            color = valueColor,
            textAlign = TextAlign.End,
            modifier = Modifier.padding(start = 16.dp)
        )
    }
}

/**
 * 날짜 문자열에서 남은 일수 계산
 */
private fun calculateDaysRemaining(expiryDate: String): Int {
    return try {
        val expiry = Instant.parse(expiryDate)
        val now = Instant.now()
        val days = Duration.between(now, expiry).toDays()
        if (days > 0) days.toInt() else 0
    } catch (e: Exception) {
        0
    }
}
