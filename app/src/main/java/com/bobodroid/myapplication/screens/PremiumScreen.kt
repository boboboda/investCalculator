package com.bobodroid.myapplication.screens

import android.app.Activity
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
import com.bobodroid.myapplication.billing.BillingClientLifecycle
import com.bobodroid.myapplication.models.viewmodels.PremiumViewModel

/**
 * 프리미엄 전용 화면
 * - 프리미엄 구매
 * - 구독 관리
 * - 혜택 상세 안내
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PremiumScreen(
    onBackClick: () -> Unit,
    viewModel: PremiumViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val isPremium by viewModel.isPremium.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("프리미엄") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "뒤로가기"
                        )
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
                .background(Color(0xFFF9FAFB))
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (isPremium) {
                // 프리미엄 사용자
                PremiumActiveCard()

                SubscriptionManagementCard()

                PremiumFeaturesDetailCard()
            } else {
                // 일반 사용자
                PremiumHeroCard(
                    onPurchaseClick = {
                        activity?.let { act ->
                            val billingClient = BillingClientLifecycle.getInstance(context)
                            viewModel.startPurchaseFlow(act, billingClient)
                        }
                    }
                )

                PremiumBenefitsCard()

                PremiumFeaturesDetailCard()

                PricingCard()
            }
        }
    }
}

/**
 * 프리미엄 활성 카드
 */
@Composable
fun PremiumActiveCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFFEF3C7)
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
                tint = Color(0xFFF59E0B)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "프리미엄 사용 중",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF92400E)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "모든 프리미엄 기능을 이용하고 계십니다",
                fontSize = 14.sp,
                color = Color(0xFFB45309),
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * 구독 관리 카드
 */
@Composable
fun SubscriptionManagementCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = "구독 관리",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1F2937)
            )

            Spacer(modifier = Modifier.height(16.dp))

            ManagementItem(
                icon = Icons.Rounded.Receipt,
                title = "구독 정보",
                description = "Google Play에서 관리"
            )

            Spacer(modifier = Modifier.height(12.dp))

            ManagementItem(
                icon = Icons.Rounded.Autorenew,
                title = "자동 갱신",
                description = "활성화됨"
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedButton(
                onClick = { /* Google Play 구독 관리로 이동 */ },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Settings,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Google Play에서 관리하기")
            }
        }
    }
}

/**
 * 관리 항목
 */
@Composable
fun ManagementItem(
    icon: ImageVector,
    title: String,
    description: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = Color(0xFF6366F1)
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF1F2937)
            )

            Text(
                text = description,
                fontSize = 12.sp,
                color = Color(0xFF6B7280)
            )
        }
    }
}

/**
 * 프리미엄 히어로 카드
 */
@Composable
fun PremiumHeroCard(onPurchaseClick: () -> Unit) {
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
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Rounded.Star,
                contentDescription = null,
                modifier = Modifier.size(72.dp),
                tint = Color(0xFFFBBF24)
            )

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "프리미엄으로\n업그레이드하세요",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "광고 없이 모든 기능을 무제한으로 사용하세요",
                fontSize = 15.sp,
                color = Color.White.copy(alpha = 0.9f),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(28.dp))

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
                    modifier = Modifier.padding(vertical = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Star,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "지금 시작하기",
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp
                    )
                }
            }
        }
    }
}

/**
 * 프리미엄 혜택 요약 카드
 */
@Composable
fun PremiumBenefitsCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
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

            BenefitItem(
                icon = Icons.Rounded.Block,
                title = "모든 광고 제거",
                description = "방해 없이 쾌적하게 사용하세요"
            )

            BenefitItem(
                icon = Icons.Rounded.Language,
                title = "12개 모든 통화 사용",
                description = "USD, JPY 외 10개 추가 통화 기록 가능"
            )

            BenefitItem(
                icon = Icons.Rounded.FlashOn,
                title = "위젯 실시간 업데이트",
                description = "앱을 열지 않아도 자동으로 최신 환율 반영"
            )

            BenefitItem(
                icon = Icons.Rounded.CloudDone,
                title = "클라우드 자동 백업",
                description = "실시간 자동 백업으로 데이터 안전 보장"
            )
        }
    }
}

/**
 * 혜택 항목
 */
@Composable
fun BenefitItem(
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

        Column(modifier = Modifier.weight(1f)) {
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

/**
 * 프리미엄 기능 상세 카드
 */
@Composable
fun PremiumFeaturesDetailCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
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
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1F2937)
            )

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
                title = "모든 통화 무제한 사용",
                items = listOf(
                    "USD, JPY 포함 총 12개 통화",
                    "EUR, GBP, CNY, AUD, CAD 추가",
                    "CHF, HKD, SGD, NZD, THB 사용 가능"
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

            HorizontalDivider(color = Color(0xFFE5E7EB))

            DetailFeatureItem(
                title = "클라우드 자동 백업",
                items = listOf(
                    "수동 백업 → 자동 백업",
                    "실시간 데이터 동기화",
                    "언제 어디서나 데이터 복구"
                )
            )
        }
    }
}

/**
 * 상세 기능 항목
 */
@Composable
fun DetailFeatureItem(
    title: String,
    items: List<String>
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = title,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1F2937)
        )

        items.forEach { item ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = "•",
                    fontSize = 14.sp,
                    color = Color(0xFF6366F1),
                    modifier = Modifier.padding(end = 8.dp)
                )

                Text(
                    text = item,
                    fontSize = 14.sp,
                    color = Color(0xFF6B7280),
                    lineHeight = 20.sp
                )
            }
        }
    }
}

/**
 * 가격 카드
 */
@Composable
fun PricingCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F9FF)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = "가격 안내",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0C4A6E)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "월간 구독",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF0C4A6E)
                    )

                    Text(
                        text = "언제든지 취소 가능",
                        fontSize = 12.sp,
                        color = Color(0xFF075985)
                    )
                }

                Text(
                    text = "₩4,900 / 월",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0284C7)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            HorizontalDivider(color = Color(0xFFBAE6FD))

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "• 첫 달 무료 체험 (신규 가입자에 한함)\n• Google Play를 통한 안전한 결제\n• 자동 갱신, 언제든지 해지 가능",
                fontSize = 12.sp,
                color = Color(0xFF075985),
                lineHeight = 18.sp
            )
        }
    }
}