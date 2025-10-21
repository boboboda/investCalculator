package com.bobodroid.myapplication.screens

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.bobodroid.myapplication.BuildConfig
import com.bobodroid.myapplication.models.viewmodels.PremiumViewModel
import com.bobodroid.myapplication.util.BatteryOptimizationHelper

/**
 * 위젯 설정 화면
 * - 실시간 업데이트 ON/OFF (프리미엄 전용)
 * - 배터리 최적화 설정
 * - 수동 새로고침 안내
 * - 프리미엄 구매 유도
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WidgetSettingsScreen(
    onBackClick: () -> Unit,
    onPremiumClick: () -> Unit, // 프리미엄 화면으로 이동
    viewModel: PremiumViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val isPremium by viewModel.isPremium.collectAsState()
    val isServiceRunning by viewModel.isServiceRunning.collectAsState()

    var isBatteryOptimized by remember {
        mutableStateOf(!BatteryOptimizationHelper.isIgnoringBatteryOptimizations(context))
    }

    // 화면 포커스 시 배터리 최적화 상태 재확인
    LaunchedEffect(Unit) {
        isBatteryOptimized = !BatteryOptimizationHelper.isIgnoringBatteryOptimizations(context)
    }

    // 화면 재진입 시 배터리 상태 업데이트
    DisposableEffect(Unit) {
        onDispose {
            isBatteryOptimized = !BatteryOptimizationHelper.isIgnoringBatteryOptimizations(context)
        }
    }

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
            // 위젯 상태 카드
            WidgetStatusCard(isPremium = isPremium)

            if (isPremium) {
                // 배터리 최적화 경고 (프리미엄 + 서비스 실행 중일 때만)
                if (isBatteryOptimized && isServiceRunning) {
                    BatteryOptimizationWarningCard(
                        onOptimizeClick = {
                            BatteryOptimizationHelper.requestBatteryOptimizationException(context)
                            // 설정 변경 후 토스트 메시지
                            Toast.makeText(
                                context,
                                "설정 화면으로 이동합니다. '제한 없음'을 선택해주세요.",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    )
                }

                // 실시간 업데이트 설정 (프리미엄 전용)
                RealtimeUpdateCard(
                    isServiceRunning = isServiceRunning,
                    onToggle = { enabled ->
                        viewModel.toggleRealtimeUpdate(enabled)

                        // 서비스 시작 시 배터리 최적화 안내
                        if (enabled && isBatteryOptimized) {
                            Toast.makeText(
                                context,
                                "배터리 최적화를 해제하면 더 안정적으로 작동합니다",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                )

                // 서비스 상태 표시
                if (isServiceRunning) {
                    ServiceStatusCard()
                }

                // 기능 안내
                FeatureInfoCard()
            } else {
                // 수동 새로고침 안내
                ManualRefreshInfoCard()

                // 프리미엄 구매 유도
                WidgetPremiumPromotionCard(
                    onUpgradeClick = onPremiumClick
                )
            }

            // 디버그 빌드에만 테스트 도구 표시
            if (BuildConfig.DEBUG) {
                TestControlCard(
                    isPremium = isPremium,
                    onTogglePremium = { newStatus ->  // ✅ 파라미터로 받기

                        Log.d("Premium","프리미엄 상태: ${isPremium}, ${newStatus}")

                        viewModel.setTestPremiumStatus(newStatus)

                        if (newStatus) {
                            viewModel.toggleRealtimeUpdate(true)
                        } else {
                            viewModel.toggleRealtimeUpdate(false)
                        }
                    },
                    onRefreshStatus = { viewModel.refreshPremiumStatus() }
                )
            }
        }
    }
}

/**
 * 배터리 최적화 경고 카드
 */
@Composable
fun BatteryOptimizationWarningCard(onOptimizeClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onOptimizeClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFFFF3E0)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Rounded.Warning,
                contentDescription = "경고",
                tint = Color(0xFFF57C00),
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "배터리 최적화 활성화됨",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFE65100)
                )
                Text(
                    text = "백그라운드 업데이트가 제한될 수 있습니다.\n탭하여 예외 설정하기",
                    fontSize = 12.sp,
                    color = Color(0xFF795548),
                    modifier = Modifier.padding(top = 4.dp),
                    lineHeight = 16.sp
                )
            }

            Icon(
                imageVector = Icons.Rounded.ChevronRight,
                contentDescription = "이동",
                tint = Color(0xFF795548),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

/**
 * 서비스 상태 카드
 */
@Composable
fun ServiceStatusCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFE8F5E9)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Rounded.CheckCircle,
                contentDescription = "실행 중",
                tint = Color(0xFF4CAF50),
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "실시간 업데이트 실행 중",
                fontSize = 14.sp,
                color = Color(0xFF2E7D32)
            )
        }
    }
}

/**
 * 위젯 상태 카드
 */
@Composable
fun WidgetStatusCard(isPremium: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isPremium) Color(0xFFFEF3C7) else Color(0xFFF3F4F6)
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
                imageVector = if (isPremium) Icons.Rounded.Star else Icons.Rounded.Widgets,
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = if (isPremium) Color(0xFFF59E0B) else Color(0xFF6B7280)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(
                    text = if (isPremium) "프리미엄 사용 중" else "기본 위젯",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isPremium) Color(0xFF92400E) else Color(0xFF1F2937)
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
                            .padding(12.dp),
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
                            text = "위젯이 자동으로 최신 환율을 업데이트합니다",
                            fontSize = 12.sp,
                            color = Color(0xFF6366F1)
                        )
                    }
                }
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
                text = "실시간 업데이트 기능",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1F2937)
            )

            FeatureItem(
                icon = Icons.Rounded.Update,
                title = "자동 업데이트",
                description = "앱을 열지 않아도 최신 환율이 자동으로 업데이트됩니다"
            )

            FeatureItem(
                icon = Icons.Rounded.PhoneAndroid,
                title = "백그라운드 실행",
                description = "앱이 종료되어도 위젯이 계속 업데이트됩니다"
            )

            FeatureItem(
                icon = Icons.Rounded.Notifications,
                title = "알림에서 확인",
                description = "알림창에서 현재 환율과 수익을 바로 확인할 수 있습니다"
            )

            // 배터리 최적화 팁 추가
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFF0F9FF)
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Lightbulb,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = Color(0xFF0284C7)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "팁: 배터리 최적화를 해제하면 더 안정적으로 작동합니다",
                        fontSize = 11.sp,
                        color = Color(0xFF075985),
                        lineHeight = 15.sp
                    )
                }
            }
        }
    }
}

/**
 * 기능 항목 (아이콘 추가)
 */
@Composable
fun FeatureItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier
                .size(20.dp)
                .padding(top = 2.dp),
            tint = Color(0xFF10B981)
        )

        Spacer(modifier = Modifier.width(12.dp))

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
 * 위젯용 프리미엄 구매 유도 카드
 */
@Composable
fun WidgetPremiumPromotionCard(onUpgradeClick: () -> Unit) {
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
                text = "프리미엄으로\n위젯 자동 업데이트 사용",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "앱을 열지 않아도 최신 환율이 자동으로 업데이트됩니다",
                fontSize = 13.sp,
                color = Color.White.copy(alpha = 0.9f),
                textAlign = TextAlign.Center
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
                        text = "프리미엄 자세히 보기",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
            }
        }
    }
}

/**
 * 테스트 컨트롤 카드 (디버그 빌드 전용)
 */
@Composable
fun TestControlCard(
    isPremium: Boolean,
    onTogglePremium: (Boolean) -> Unit,  // ✅ Boolean 파라미터
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
                    onClick = {
                        onTogglePremium(!isPremium)  // ✅ 계산된 값 전달!
                    },
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