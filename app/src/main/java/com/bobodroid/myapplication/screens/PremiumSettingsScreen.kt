package com.bobodroid.myapplication.screens

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.BugReport
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.bobodroid.myapplication.BuildConfig
import com.bobodroid.myapplication.billing.BillingClientLifecycle
import com.bobodroid.myapplication.models.viewmodels.PremiumViewModel
import com.bobodroid.myapplication.premium.PremiumManager
import com.bobodroid.myapplication.widget.WidgetUpdateService

/**
 * 프리미엄 설정 화면
 * - 실시간 업데이트 ON/OFF
 * - 프리미엄 구매 안내
 * - 서비스 상태 표시
 * - 테스트 버튼 (디버그 빌드에만 표시)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PremiumSettingsScreen(
    onBackClick: () -> Unit,
    viewModel: PremiumViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val isPremium by viewModel.isPremium.collectAsState()
    val isServiceRunning by viewModel.isServiceRunning.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("위젯 설정") },
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
            // 프리미엄 상태 카드
            PremiumStatusCard(isPremium = isPremium)

            if (isPremium) {
                // 실시간 업데이트 설정
                RealtimeUpdateCard(
                    isServiceRunning = isServiceRunning,
                    onToggle = { enabled ->
                        viewModel.toggleRealtimeUpdate(enabled)
                    }
                )
            } else {
                // 프리미엄 안내 카드
                PremiumPromotionCard(
                    onUpgradeClick = {
                        // 결제 화면으로 이동 또는 결제 플로우 시작
                        val billingClient = BillingClientLifecycle.getInstance(context)
                        // TODO: 구매 화면 열기
                    }
                )
            }

            // 기능 안내 카드
            FeatureInfoCard()

            // 수동 새로고침 안내
            ManualRefreshInfoCard()

            if (BuildConfig.DEBUG) {
                TestControlCard(
                    isPremium = isPremium,
                    onTogglePremium = {
                        // PremiumViewModel에 테스트 함수 추가 필요
                        viewModel.setTestPremiumStatus(!isPremium)
                    },
                    onRefreshStatus = {
                        viewModel.refreshPremiumStatus()
                    }
                )
            }
        }
    }
}

/**
 * 프리미엄 상태 표시 카드
 */
@Composable
fun PremiumStatusCard(isPremium: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isPremium) Color(0xFFFEF3C7) else Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Rounded.Star,
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = if (isPremium) Color(0xFFF59E0B) else Color(0xFF9CA3AF)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (isPremium) "프리미엄 사용자" else "일반 사용자",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isPremium) Color(0xFFB45309) else Color(0xFF1F2937)
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = if (isPremium) {
                        "실시간 위젯 업데이트를 사용할 수 있습니다"
                    } else {
                        "수동 새로고침만 가능합니다"
                    },
                    fontSize = 13.sp,
                    color = if (isPremium) Color(0xFFB45309) else Color(0xFF6B7280)
                )
            }
        }
    }
}

/**
 * 실시간 업데이트 설정 카드 (프리미엄 전용)
 */
@Composable
fun RealtimeUpdateCard(
    isServiceRunning: Boolean,
    onToggle: (Boolean) -> Unit
) {
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "실시간 위젯 업데이트",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1F2937)
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = if (isServiceRunning) "백그라운드에서 실행 중" else "꺼져 있음",
                        fontSize = 13.sp,
                        color = Color(0xFF6B7280)
                    )
                }

                Switch(
                    checked = isServiceRunning,
                    onCheckedChange = onToggle,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = Color(0xFF6366F1),
                        uncheckedThumbColor = Color.White,
                        uncheckedTrackColor = Color(0xFFD1D5DB)
                    )
                )
            }

            if (isServiceRunning) {
                Spacer(modifier = Modifier.height(12.dp))

                HorizontalDivider(color = Color(0xFFE5E7EB))

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Info,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = Color(0xFF6366F1)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = "알림을 스와이프로 지우면 서비스가 종료됩니다",
                        fontSize = 12.sp,
                        color = Color(0xFF6B7280),
                        lineHeight = 18.sp
                    )
                }
            }
        }
    }
}

/**
 * 프리미엄 홍보 카드 (일반 사용자용)
 */
@Composable
fun PremiumPromotionCard(onUpgradeClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF6366F1)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
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
                modifier = Modifier.size(48.dp),
                tint = Color(0xFFFBBF24)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "프리미엄으로 업그레이드",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "실시간 위젯 자동 업데이트를 사용하세요",
                fontSize = 14.sp,
                color = Color.White.copy(alpha = 0.9f)
            )

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = onUpgradeClick,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = Color(0xFF6366F1)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "프리미엄 구매하기",
                    modifier = Modifier.padding(vertical = 4.dp),
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }
        }
    }
}

/**
 * 기능 안내 카드
 */
@Composable
fun FeatureInfoCard() {
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
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1F2937)
            )

            FeatureItem(
                title = "실시간 위젯 업데이트",
                description = "앱을 열지 않아도 최신 환율이 자동으로 업데이트됩니다"
            )

            FeatureItem(
                title = "백그라운드 실행",
                description = "앱이 종료되어도 위젯이 계속 업데이트됩니다"
            )

            FeatureItem(
                title = "알림에서 환율 확인",
                description = "알림창에서 현재 환율과 수익을 바로 확인할 수 있습니다"
            )
        }
    }
}

/**
 * 기능 항목
 */
@Composable
fun FeatureItem(title: String, description: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = "✓",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF10B981),
            modifier = Modifier.padding(end = 12.dp)
        )

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
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
 * 수동 새로고침 안내 카드
 */
@Composable
fun ManualRefreshInfoCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F9FF)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Rounded.Refresh,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = Color(0xFF0284C7)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "수동 새로고침",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF0C4A6E)
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "위젯의 새로고침 버튼을 눌러 최신 환율을 가져올 수 있습니다",
                    fontSize = 12.sp,
                    color = Color(0xFF075985),
                    lineHeight = 16.sp
                )
            }
        }
    }
}

/**
 * ✅ 테스트 컨트롤 카드 (디버그 빌드 전용)
 */
@Composable
fun TestControlCard(
    isPremium: Boolean,
    onTogglePremium: () -> Unit,
    onRefreshStatus: () -> Unit
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

            Text(
                text = "현재 프리미엄 상태: ${if (isPremium) "✅ 프리미엄" else "❌ 일반"}",
                fontSize = 14.sp,
                color = Color(0xFF7F1D1D)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onTogglePremium,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isPremium) Color(0xFFEF4444) else Color(0xFF10B981)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = if (isPremium) "일반으로 변경" else "프리미엄으로 변경",
                        fontSize = 13.sp
                    )
                }

                OutlinedButton(
                    onClick = onRefreshStatus,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "상태 새로고침",
                        fontSize = 13.sp,
                        color = Color(0xFF991B1B)
                    )
                }
            }
        }
    }
}

