// app/src/main/java/com/bobodroid/myapplication/screens/NotificationSettingsScreen.kt

package com.bobodroid.myapplication.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.bobodroid.myapplication.models.viewmodels.FcmAlarmViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationSettingsScreen(
    onBackClick: () -> Unit,
    onPremiumClick: () -> Unit = {},
    viewModel: FcmAlarmViewModel = hiltViewModel()
) {
    val settings by viewModel.notificationSettings.collectAsState()
    val isPremium by viewModel.isPremium.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    var showPremiumDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("알림 설정") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, "뒤로가기")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        }
    ) { paddingValues ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFFF5F5F5)),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            // 로딩/에러
            if (isLoading) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }

            error?.let { errorMsg ->
                item {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFFFEBEE)
                        )
                    ) {
                        Text(
                            text = errorMsg,
                            modifier = Modifier.padding(16.dp),
                            color = Color(0xFFC62828)
                        )
                    }
                }
            }

            // 전체 알림 토글
            item {
                GlobalNotificationCard(
                    enabled = settings?.globalEnabled ?: true,
                    onToggle = { viewModel.toggleGlobalNotification(it) }
                )
            }

            // 실시간 알림 섹션
            item {
                SectionHeader(
                    title = "⚡ 실시간 알림",
                    description = "조건 달성 시 즉시 알림"
                )
            }

            // 환율 알림 (무료)
            item {
                NotificationCard(
                    icon = "💵",
                    title = "환율 알림",
                    description = "목표 환율 도달 시 즉시 알림",
                    hint = "상세 설정은 알림 > 목표환율 탭에서",
                    enabled = settings?.rateAlert?.enabled ?: true,
                    isPremium = false,
                    onToggle = { viewModel.toggleRateAlert(it) },
                    onLockClick = null
                )
            }

            // 수익률 알림 (프리미엄)
            item {
                NotificationCard(
                    icon = "📊",
                    title = "수익률 알림",
                    description = "수익률 목표 달성 시 즉시 알림",
                    hint = "상세 설정은 알림 > 수익률 알림 탭에서",
                    enabled = settings?.recordAlert?.enabled ?: false,
                    isPremium = isPremium,
                    onToggle = {
                        if (isPremium) {
                            viewModel.toggleProfitAlert(it)
                        } else {
                            showPremiumDialog = true
                        }
                    },
                    onLockClick = { showPremiumDialog = true }
                )
            }

            // 스케줄 알림 섹션
            item {
                Spacer(Modifier.height(12.dp))
                SectionHeader(
                    title = "⏰ 예약 알림",
                    description = "원하는 시간에 알림"
                )
            }

            // 매수 경과 알림 (프리미엄)
            item {
                NotificationCard(
                    icon = "⏰",
                    title = "매수 경과 알림",
                    description = "매수 후 일정 기간 경과 시 알림",
                    hint = "상세 설정은 알림 > 매수경과 탭에서",
                    enabled = settings?.recordAlert?.enabled ?: false,
                    isPremium = isPremium,
                    onToggle = {
                        if (isPremium) {
                            viewModel.toggleProfitAlert(it)
                        } else {
                            showPremiumDialog = true
                        }
                    },
                    onLockClick = { showPremiumDialog = true }
                )
            }

            // 일일 요약 (프리미엄) - 추후 구현
            item {
                NotificationCard(
                    icon = "📊",
                    title = "일일 요약",
                    description = "하루 한 번 요약 리포트",
                    hint = "추후 제공 예정",
                    enabled = settings?.systemAlert?.enabled ?: false,
                    isPremium = isPremium,
                    onToggle = {
                        if (isPremium) {
                            // TODO: systemAlert 토글
                        } else {
                            showPremiumDialog = true
                        }
                    },
                    onLockClick = { showPremiumDialog = true }
                )
            }

            // 방해금지 시간 섹션
            item {
                Spacer(Modifier.height(12.dp))
                SectionHeader(
                    title = "🌙 방해금지 시간",
                    description = "알림을 받지 않을 시간 설정"
                )
            }

            item {
                QuietHoursCard(
                    enabled = settings?.quietHours?.enabled ?: false,
                    startTime = settings?.quietHours?.startTime ?: "22:00",
                    endTime = settings?.quietHours?.endTime ?: "08:00",
                    onToggle = { enabled ->
                        // ✅ 간단하게 토글만
                        settings?.quietHours?.let { current ->
                            viewModel.updateQuietHours(
                                current.copy(enabled = enabled)
                            )
                        }
                    }
                )
            }

            // 일일 알림 제한
            item {
                Spacer(Modifier.height(12.dp))
                SectionHeader(
                    title = "🔔 일일 알림 제한",
                    description = "하루 최대 알림 개수"
                )
            }

            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "최대 알림 개수",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "하루 ${settings?.maxDailyNotifications ?: 20}개까지",
                                fontSize = 13.sp,
                                color = Color(0xFF6B7280)
                            )
                        }

                        Text(
                            text = "${settings?.maxDailyNotifications ?: 20}개",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF667eea)
                        )
                    }
                }
            }

            // 테스트 알림
//            item {
//                Spacer(Modifier.height(8.dp))
//                OutlinedButton(
//                    onClick = { viewModel.sendTestNotification() },
//                    modifier = Modifier.fillMaxWidth()
//                ) {
//                    Icon(Icons.Default.Notifications, null)
//                    Spacer(Modifier.width(8.dp))
//                    Text("테스트 알림 보내기")
//                }
//            }
        }
    }

    // 프리미엄 다이얼로그
    if (showPremiumDialog) {
        AlertDialog(
            onDismissRequest = { showPremiumDialog = false },
            title = { Text("🔒 프리미엄 기능") },
            text = { Text("이 기능은 프리미엄 사용자만 이용할 수 있습니다.") },
            confirmButton = {
                TextButton(onClick = {
                    showPremiumDialog = false
                    onPremiumClick()
                }) {
                    Text("프리미엄 구독")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPremiumDialog = false }) {
                    Text("취소")
                }
            }
        )
    }
}

// ==================== 공통 컴포넌트 ====================

@Composable
fun SectionHeader(
    title: String,
    description: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Text(
            text = title,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1F2937)
        )
        Text(
            text = description,
            fontSize = 14.sp,
            color = Color(0xFF6B7280)
        )
    }
}

@Composable
fun GlobalNotificationCard(
    enabled: Boolean,
    onToggle: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (enabled) Color(0xFFDCFCE7) else Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = if (enabled) Icons.Default.Notifications
                    else Icons.Default.NotificationsOff,
                    contentDescription = null,
                    tint = if (enabled) Color(0xFF16A34A) else Color(0xFF9CA3AF),
                    modifier = Modifier.size(32.dp)
                )

                Spacer(Modifier.width(16.dp))

                Column {
                    Text(
                        text = "전체 알림",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1F2937)
                    )
                    Text(
                        text = if (enabled) "모든 알림 활성화" else "모든 알림 비활성화",
                        fontSize = 14.sp,
                        color = Color(0xFF6B7280)
                    )
                }
            }

            Switch(
                checked = enabled,
                onCheckedChange = onToggle,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = Color(0xFF16A34A)
                )
            )
        }
    }
}

// ✅ 간소화된 알림 카드 (ON/OFF만)
@Composable
fun NotificationCard(
    icon: String,
    title: String,
    description: String,
    hint: String,
    enabled: Boolean,
    isPremium: Boolean,
    onToggle: (Boolean) -> Unit,
    onLockClick: (() -> Unit)?
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(icon, fontSize = 28.sp)

                Spacer(Modifier.width(12.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = title,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )

                        if (!isPremium && onLockClick != null) {
                            Spacer(Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "프리미엄",
                                tint = Color(0xFFF59E0B),
                                modifier = Modifier
                                    .size(16.dp)
                                    .clickable { onLockClick() }
                            )
                        }
                    }

                    Text(
                        text = description,
                        fontSize = 13.sp,
                        color = Color(0xFF6B7280)
                    )

                    // ✅ 상세 설정 힌트
                    Text(
                        text = hint,
                        fontSize = 11.sp,
                        color = Color(0xFF9CA3AF),
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            Switch(
                checked = enabled,
                onCheckedChange = onToggle,
                enabled = isPremium || onLockClick == null
            )
        }
    }
}

// ✅ 방해금지 시간 카드
@Composable
fun QuietHoursCard(
    enabled: Boolean,
    startTime: String,
    endTime: String,
    onToggle: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("🌙", fontSize = 28.sp)
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "방해금지 모드",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = if (enabled) "$startTime ~ $endTime" else "설정 안 함",
                            fontSize = 13.sp,
                            color = Color(0xFF6B7280)
                        )
                    }
                }

                Switch(
                    checked = enabled,
                    onCheckedChange = onToggle
                )
            }

            if (enabled) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp)
                ) {
                    Divider()

                    Spacer(Modifier.height(12.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFF3F4F6))
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "시작",
                                fontSize = 12.sp,
                                color = Color(0xFF6B7280)
                            )
                            Text(
                                text = startTime,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        Icon(
                            imageVector = Icons.Default.ArrowForward,
                            contentDescription = null,
                            tint = Color(0xFF6B7280)
                        )

                        Column {
                            Text(
                                text = "종료",
                                fontSize = 12.sp,
                                color = Color(0xFF6B7280)
                            )
                            Text(
                                text = endTime,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }
}