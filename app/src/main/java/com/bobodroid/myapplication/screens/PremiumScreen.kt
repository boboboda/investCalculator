package com.bobodroid.myapplication.screens

import android.app.Activity
import android.util.Log
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
import com.bobodroid.myapplication.models.viewmodels.PremiumViewModel
import com.bobodroid.myapplication.models.viewmodels.SharedViewModel
import kotlinx.coroutines.launch

/**
 * 프리미엄 구독 화면
 *
 * ViewModel 통합 구조:
 * - SharedViewModel: 전역 프리미엄 상태, 유저 정보, 디버그 기능
 * - PremiumViewModel: 구매 플로우, 서비스 토글
 * - BillingClient: Google Play 구독 관리
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

    // ✅ SharedViewModel에서 전역 상태 가져오기
    val user by sharedViewModel.user.collectAsState()
    val isPremium by sharedViewModel.isPremium.collectAsState()
    val premiumType by sharedViewModel.premiumType.collectAsState()
    val premiumExpiryDate by sharedViewModel.premiumExpiryDate.collectAsState()

    // ✅ PremiumViewModel에서 화면 전용 상태 가져오기
    val isServiceRunning by viewModel.isServiceRunning.collectAsState()

    // BillingClient 인스턴스
    val billingClient = remember { BillingClientLifecycle.getInstance(context) }
    val products by billingClient.fetchedProductList.collectAsState()

    // ✅ 스낵바 이벤트 구독
    LaunchedEffect(Unit) {
        sharedViewModel.snackbarEvent.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("프리미엄") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, "뒤로가기")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.refreshPremiumStatus() }
                    ) {
                        Icon(Icons.Rounded.Refresh, "새로고침")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF6366F1),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFFF9FAFB))
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ✅ 소셜 로그인 안내 (미연동 시에만)
            val isSocialLinked = user.socialType != "NONE" && !user.socialId.isNullOrEmpty()
            if (!isSocialLinked) {
                SocialLoginWarningBanner(
                    isSocialLinked = isSocialLinked,
                    onLinkClick = onAccountManageClick
                )
            }

            // ✅ 디버그 모드 (기존 코드 유지)
            if (BuildConfig.DEBUG) {
                TestControlCard(
                    isPremium = isPremium,
                    user = user,
                    onTogglePremium = { newStatus ->
                        coroutineScope.launch {
                            Log.d("PremiumScreen", "프리미엄 상태 변경: $isPremium → $newStatus")
                            viewModel.setTestPremiumStatus(newStatus)

                            if (newStatus) {
                                viewModel.toggleRealtimeUpdate(true)
                            } else {
                                viewModel.toggleRealtimeUpdate(false)
                            }
                        }
                    },
                    onRefreshStatus = { viewModel.refreshPremiumStatus() },
                    onGrantTestPremium = { minutes ->
                        sharedViewModel.grantTestPremium(minutes)
                    },
                    onResetAdCounts = {
                        sharedViewModel.resetAdCounts()
                    }
                )
            }

            // ✅ 프리미엄 활성 상태
            if (isPremium) {
                PremiumActiveCard(
                    premiumType = premiumType,
                    premiumExpiryDate = premiumExpiryDate
                )

                // 실시간 업데이트 토글
//                RealtimeUpdateCard(
//                    isEnabled = isServiceRunning,
//                    onToggle = { enabled ->
//                        viewModel.toggleRealtimeUpdate(enabled)
//                    }
//                )

                SubscriptionManagementCard(
                    onRestoreClick = {
                        // TODO: 구독 복원 로직
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar("구독 복원 기능 준비 중")
                        }
                    }
                )
            } else {
                // ✅ 일반 사용자 - 구독 유도
                PremiumHeroCard(
                    onPurchaseClick = {
                        activity?.let { act ->
                            if (products.isNotEmpty()) {
                                billingClient.startBillingFlow(act, products.first())
                            } else {
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("상품 정보를 불러오는 중입니다")
                                }
                            }
                        }
                    }
                )

                // 구독 플랜 표시
                if (products.isNotEmpty()) {
                    SubscriptionPlansCard(
                        products = products,
                        onProductClick = { product ->
                            activity?.let { act ->
                                billingClient.startBillingFlow(act, product)
                            }
                        }
                    )
                }

                PremiumBenefitsCard()
            }

            PremiumFeaturesDetailCard()
        }
    }
}

/**
 * ✅ 디버그 컨트롤 카드 (기존 로직 유지)
 */
@Composable
fun TestControlCard(
    isPremium: Boolean,
    user: LocalUserData,
    onTogglePremium: (Boolean) -> Unit,
    onRefreshStatus: () -> Unit,
    onGrantTestPremium: (Int) -> Unit,
    onResetAdCounts: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F2)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Rounded.BugReport,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = Color(0xFFDC2626)
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = "🔧 테스트 도구 (개발자 전용)",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF991B1B)
                )
            }

            HorizontalDivider(color = Color(0xFFFECACA))

            // 현재 상태 표시
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "현재 상태: ${if (isPremium) "✅ 프리미엄" else "❌ 일반"}",
                    fontSize = 14.sp,
                    color = Color(0xFF7F1D1D),
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "타입: ${user.premiumType}",
                    fontSize = 12.sp,
                    color = Color(0xFF991B1B)
                )
                user.premiumExpiryDate?.let {
                    Text(
                        text = "만료: $it",
                        fontSize = 12.sp,
                        color = Color(0xFF991B1B)
                    )
                }
            }

            HorizontalDivider(color = Color(0xFFFECACA))

            // 버튼들
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { onTogglePremium(!isPremium) },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isPremium) Color(0xFFEF4444) else Color(0xFF10B981)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = if (isPremium) "일반으로" else "프리미엄으로",
                        fontSize = 13.sp
                    )
                }

                OutlinedButton(
                    onClick = onRefreshStatus,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "새로고침",
                        fontSize = 13.sp,
                        color = Color(0xFF991B1B)
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { onGrantTestPremium(1) },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFF59E0B)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("1분 프리미엄", fontSize = 12.sp)
                }

                Button(
                    onClick = { onResetAdCounts() },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF6366F1)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("광고 초기화", fontSize = 12.sp)
                }
            }
        }
    }
}

/**
 * 프리미엄 활성 카드
 */
@Composable
fun PremiumActiveCard(
    premiumType: com.bobodroid.myapplication.models.datamodels.roomDb.PremiumType,
    premiumExpiryDate: String?
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = when (premiumType) {
                com.bobodroid.myapplication.models.datamodels.roomDb.PremiumType.SUBSCRIPTION -> Color(0xFFFEF3C7)
                com.bobodroid.myapplication.models.datamodels.roomDb.PremiumType.REWARD_AD -> Color(0xFFFFEDD5)
                com.bobodroid.myapplication.models.datamodels.roomDb.PremiumType.LIFETIME -> Color(0xFFDDD6FE)
                else -> Color(0xFFF3F4F6)
            }
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
                imageVector = when (premiumType) {
                    com.bobodroid.myapplication.models.datamodels.roomDb.PremiumType.SUBSCRIPTION -> Icons.Rounded.Star
                    com.bobodroid.myapplication.models.datamodels.roomDb.PremiumType.REWARD_AD -> Icons.Rounded.PlayCircle
                    com.bobodroid.myapplication.models.datamodels.roomDb.PremiumType.LIFETIME -> Icons.Rounded.AllInclusive
                    else -> Icons.Rounded.CheckCircle
                },
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = when (premiumType) {
                    com.bobodroid.myapplication.models.datamodels.roomDb.PremiumType.SUBSCRIPTION -> Color(0xFFF59E0B)
                    com.bobodroid.myapplication.models.datamodels.roomDb.PremiumType.REWARD_AD -> Color(0xFFF97316)
                    com.bobodroid.myapplication.models.datamodels.roomDb.PremiumType.LIFETIME -> Color(0xFF8B5CF6)
                    else -> Color(0xFF6B7280)
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = when (premiumType) {
                    com.bobodroid.myapplication.models.datamodels.roomDb.PremiumType.SUBSCRIPTION -> "정기 구독 활성"
                    com.bobodroid.myapplication.models.datamodels.roomDb.PremiumType.REWARD_AD -> "24시간 무료 체험"
                    com.bobodroid.myapplication.models.datamodels.roomDb.PremiumType.LIFETIME -> "평생 이용권"
                    else -> "프리미엄 활성"
                },
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1F2937)
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (premiumExpiryDate != null && premiumType != com.bobodroid.myapplication.models.datamodels.roomDb.PremiumType.LIFETIME) {
                val daysRemaining = calculateDaysRemaining(premiumExpiryDate)
                Text(
                    text = "남은 기간: ${daysRemaining}일",
                    fontSize = 14.sp,
                    color = Color(0xFF6B7280)
                )
            }
        }
    }
}



/**
 * 구독 관리 카드
 */
@Composable
fun SubscriptionManagementCard(
    onRestoreClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "구독 관리",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            OutlinedButton(
                onClick = onRestoreClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Rounded.Restore, null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("구독 복원")
            }

            Text(
                text = "• 다른 기기에서 구매한 구독을 복원할 수 있습니다\n• 구독은 Google Play에서 취소할 수 있습니다",
                fontSize = 12.sp,
                color = Color.Gray,
                lineHeight = 16.sp
            )
        }
    }
}

/**
 * 구독 플랜 카드
 */
@Composable
fun SubscriptionPlansCard(
    products: List<ProductDetails>,
    onProductClick: (ProductDetails) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "구독 플랜",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )

        products.forEach { product ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Text(
                        text = product.name,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    val offer = product.subscriptionOfferDetails?.firstOrNull()
                    val price = offer?.pricingPhases?.pricingPhaseList?.firstOrNull()

                    if (price != null) {
                        Text(
                            text = price.formattedPrice,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF6366F1)
                        )

                        Text(
                            text = "매월 자동 갱신",
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = { onProductClick(product) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF6366F1)
                        )
                    ) {
                        Text("구독하기", fontSize = 16.sp)
                    }
                }
            }
        }
    }
}

/**
 * 프리미엄 히어로 카드 (기존 코드 유지)
 */
@Composable
fun PremiumHeroCard(onPurchaseClick: () -> Unit) {
    // 기존 구현 유지
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

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onPurchaseClick,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = Color(0xFF6366F1)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "지금 시작하기",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
        }
    }
}

/**
 * 프리미엄 혜택 카드 (기존 코드 유지)
 */
@Composable
fun PremiumBenefitsCard() {
    // 기존 구현 유지
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
                Triple(Icons.Rounded.Block, "광고 없는 환경", "모든 광고 제거"),
                Triple(Icons.Rounded.Notifications, "실시간 알림", "환율 변동 즉시 확인"),
                Triple(Icons.Rounded.Analytics, "고급 분석", "상세한 수익 분석"),
                Triple(Icons.Rounded.Cloud, "자동 백업", "데이터 안전 보관")
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
 * 프리미엄 기능 상세 카드 (기존 코드 유지)
 */
@Composable
fun PremiumFeaturesDetailCard() {
    // 기존 구현과 동일
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Text(
                text = "프리미엄 기능 상세",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            // 상세 기능 목록...
            DetailFeatureItem(
                title = "광고 없는 쾌적한 환경",
                items = listOf(
                    "분석 화면 전면 광고 제거",
                    "목표 환율 설정 광고 제거",
                    "하단 배너 광고 완전 제거"
                )
            )

            HorizontalDivider(color = Color(0xFFE5E7EB))

            DetailFeatureItem(
                title = "실시간 위젯 업데이트",
                items = listOf(
                    "WebSocket 실시간 환율 연결",
                    "백그라운드 자동 업데이트",
                    "알림을 통한 환율 확인"
                )
            )
        }
    }
}

@Composable
fun DetailFeatureItem(title: String, items: List<String>) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = title,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF1F2937)
        )

        items.forEach { item ->
            Row(
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.CheckCircle,
                    contentDescription = null,
                    tint = Color(0xFF10B981),
                    modifier = Modifier.size(18.dp)
                )

                Text(
                    text = item,
                    fontSize = 14.sp,
                    color = Color(0xFF4B5563)
                )
            }
        }
    }
}

/**
 * 남은 일수 계산 헬퍼
 */
private fun calculateDaysRemaining(expiryDate: String): Int {
    return try {
        val expiry = java.time.Instant.parse(expiryDate)
        val now = java.time.Instant.now()
        val days = java.time.Duration.between(now, expiry).toDays()
        days.toInt().coerceAtLeast(0)
    } catch (e: Exception) {
        0
    }
}