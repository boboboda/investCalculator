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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.android.billingclient.api.ProductDetails
import com.bobodroid.myapplication.BuildConfig
import com.bobodroid.myapplication.billing.BillingClientLifecycle
import com.bobodroid.myapplication.components.SocialLoginWarningBanner
import com.bobodroid.myapplication.domain.entity.PremiumType
import com.bobodroid.myapplication.domain.entity.SocialType
import com.bobodroid.myapplication.domain.entity.UserEntity
import com.bobodroid.myapplication.models.viewmodels.PremiumViewModel
import com.bobodroid.myapplication.models.viewmodels.SharedViewModel
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.Instant

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


    // ViewModel 상태
    val uiState by viewModel.uiState.collectAsState()
    val user by sharedViewModel.user.collectAsState()
    val isPremium by sharedViewModel.isPremium.collectAsState()
    val premiumType by sharedViewModel.premiumType.collectAsState()
    val premiumExpiryDate by sharedViewModel.premiumExpiryDate.collectAsState()

    val products = uiState.products
    val isLoading = uiState.isLoading

    // ✅ 이벤트 처리
    LaunchedEffect(Unit) {
        sharedViewModel.snackbarEvent.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
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
            // ✅ 소셜 로그인 경고 배너 (필요 시)
            if (!user.socialId.isNullOrEmpty()) {
                // ✅ 소셜 로그인 안내 (미연동 시에만)
                val isSocialLinked = user.socialType != SocialType.NONE && !user.socialId.isNullOrEmpty()
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
            } else {
                // ✅ 일반 사용자
                PremiumHeroCard()

                // ✅ 구독 복원 버튼 (일반 사용자용)
                RestorePurchaseCard(
                    onRestoreClick = {
                        coroutineScope.launch {
                            viewModel.restorePurchases()
                        }
                    }
                )

                // 구독 플랜 표시
                if (products.isNotEmpty()) {
                    SubscriptionPlansCard(
                        products = products,
                        onPlanClick = { product, basePlanId ->
                            activity?.let { act ->
                                viewModel.startPurchase(act, product, basePlanId)
                            }
                        }
                    )
                }

                PremiumBenefitsCard()
            }


            // ✅ 디버그 모드일 때만 테스트 컨트롤 표시
            if (BuildConfig.DEBUG) {
                TestControlCard(
                    isPremium = isPremium,
                    user = user,
                    onTogglePremium = { enabled ->
                        viewModel.setTestPremiumStatus(enabled)
                    },
                    onRefreshStatus = {
                        viewModel.refreshPremiumStatus()
                    },
                    onGrantTestPremium = { minutes ->
                        sharedViewModel.grantTestPremium(minutes)
                    },
                    onResetAdCounts = {
                        sharedViewModel.resetAdCounts()
                    }
                )
            }
        }
    }
}

/**
 * ✅ 구독 복원 카드 (일반 사용자용)
 */
@Composable
fun RestorePurchaseCard(
    onRestoreClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F9FF)),
        border = BorderStroke(1.dp, Color(0xFF93C5FD))
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
                    imageVector = Icons.Rounded.Info,
                    contentDescription = null,
                    tint = Color(0xFF3B82F6),
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "이미 구독하셨나요?",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E40AF)
                )
            }

            Text(
                text = "다른 기기에서 구매한 구독을 복원할 수 있습니다.",
                fontSize = 14.sp,
                color = Color(0xFF1E40AF),
                lineHeight = 20.sp
            )

            Button(
                onClick = onRestoreClick,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF3B82F6)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    Icons.Rounded.Restore,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("구독 복원하기", fontSize = 15.sp)
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
                    PremiumType.SUBSCRIPTION -> Color(0xFFFCD34D)
                    PremiumType.REWARD_AD -> Color(0xFFF97316)
                    PremiumType.LIFETIME -> Color(0xFF8B5CF6)
                    else -> Color(0xFF6B7280)
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = when (premiumType) {
                    PremiumType.SUBSCRIPTION -> "정기 구독 활성"
                    PremiumType.REWARD_AD -> "24시간 무료 체험"
                    PremiumType.LIFETIME -> "평생 이용권"
                    else -> "프리미엄 활성"
                },
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (premiumExpiryDate != null && premiumType != PremiumType.LIFETIME) {
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
 * 구독 플랜 카드 - 하나의 상품에서 월간/연간 요금제 분리 표시
 */
@Composable
fun SubscriptionPlansCard(
    products: List<ProductDetails>,
    onPlanClick: (ProductDetails, String) -> Unit  // (product, basePlanId)
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

        // ✅ recordadvertisementremove 상품 찾기
        val product = products.find {
            it.productId == BillingClientLifecycle.PRODUCT_ID
        }

        product?.let { productDetails ->
            val offers = productDetails.subscriptionOfferDetails ?: emptyList()

            // ✅ 월간 요금제 찾기
            val monthlyOffer = offers.find {
                it.basePlanId == BillingClientLifecycle.BASE_PLAN_MONTHLY
            }

            // ✅ 연간 요금제 찾기
            val yearlyOffer = offers.find {
                it.basePlanId == BillingClientLifecycle.BASE_PLAN_YEARLY
            }

            // 월간 구독 카드
            monthlyOffer?.let { offer ->
                val pricingPhase = offer.pricingPhases.pricingPhaseList.firstOrNull()

                SubscriptionPlanItem(
                    planType = "월간",
                    planIcon = Icons.Rounded.CalendarMonth,
                    planColor = Color(0xFF6366F1),
                    price = pricingPhase?.formattedPrice ?: "",
                    renewalText = "매월 자동 갱신",
                    onPlanClick = {
                        onPlanClick(productDetails, BillingClientLifecycle.BASE_PLAN_MONTHLY)
                    }
                )
            }

            // 연간 구독 카드 (추천 배지 추가)
            yearlyOffer?.let { offer ->
                val pricingPhase = offer.pricingPhases.pricingPhaseList.firstOrNull()
                val yearlyPrice = pricingPhase?.priceAmountMicros?.div(1_000_000.0) ?: 0.0
                val monthlyPrice = yearlyPrice / 12
                val discount = if (monthlyOffer != null) {
                    val monthlyAmount = monthlyOffer.pricingPhases.pricingPhaseList.firstOrNull()
                        ?.priceAmountMicros?.div(1_000_000.0) ?: 0.0
                    if (monthlyAmount > 0) {
                        ((1 - (monthlyPrice / monthlyAmount)) * 100).toInt()
                    } else 0
                } else 0

                SubscriptionPlanItemYearly(
                    planType = "연간",
                    planIcon = Icons.Rounded.CalendarToday,
                    planColor = Color(0xFFF59E0B),
                    price = pricingPhase?.formattedPrice ?: "",
                    monthlyEquivalent = "월 ${String.format("%.0f", monthlyPrice)}원",
                    discount = if (discount > 0) "$discount% 할인" else null,
                    renewalText = "매년 자동 갱신",
                    onPlanClick = {
                        onPlanClick(productDetails, BillingClientLifecycle.BASE_PLAN_YEARLY)
                    }
                )
            }
        } ?: run {
            Text(
                text = "구독 상품을 불러오는 중...",
                color = Color.Gray,
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}

/**
 * 구독 플랜 아이템 (월간용)
 */
@Composable
private fun SubscriptionPlanItem(
    planType: String,
    planIcon: ImageVector,
    planColor: Color,
    price: String,
    renewalText: String,
    onPlanClick: () -> Unit
) {
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
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = planIcon,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = planColor
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = planType,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = price,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = planColor
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "/ 월",
                    fontSize = 16.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = renewalText,
                fontSize = 14.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onPlanClick,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = planColor
                )
            ) {
                Text("구독하기", fontSize = 16.sp)
            }
        }
    }
}

/**
 * 구독 플랜 아이템 (연간용 - 추천 배지 포함)
 */
@Composable
private fun SubscriptionPlanItemYearly(
    planType: String,
    planIcon: ImageVector,
    planColor: Color,
    price: String,
    monthlyEquivalent: String,
    discount: String?,
    renewalText: String,
    onPlanClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(2.dp, planColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = planIcon,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        tint = planColor
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = planType,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // 추천 배지
                if (discount != null) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = planColor
                    ) {
                        Text(
                            text = discount,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = price,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = planColor
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "/ 년",
                    fontSize = 16.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = monthlyEquivalent,
                fontSize = 14.sp,
                color = planColor,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = renewalText,
                fontSize = 14.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onPlanClick,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = planColor
                )
            ) {
                Text("구독하기", fontSize = 16.sp)
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
 * 프리미엄 혜택 카드
 */
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
                Triple(Icons.Rounded.Cloud, "자동 백업", "클라우드에 안전하게 데이터 보관"),
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
 * ✅ 디버그 컨트롤 카드
 */
@Composable
fun TestControlCard(
    isPremium: Boolean,
    user: UserEntity,
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
                    )
                ) {
                    Text(
                        text = if (isPremium) "비활성화" else "활성화",
                        fontSize = 13.sp
                    )
                }

                Button(
                    onClick = onRefreshStatus,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF3B82F6)
                    )
                ) {
                    Text("새로고침", fontSize = 13.sp)
                }
            }

            Button(
                onClick = { onGrantTestPremium(1) },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFF59E0B)
                )
            ) {
                Text("1분 후 만료 프리미엄 지급", fontSize = 13.sp)
            }

            Button(
                onClick = onResetAdCounts,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF8B5CF6)
                )
            ) {
                Text("광고 카운트 초기화", fontSize = 13.sp)
            }
        }
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

