// app/src/main/java/com/bobodroid/myapplication/screens/NotificationSettingsScreen.kt

package com.bobodroid.myapplication.screens

import androidx.compose.animation.AnimatedVisibility
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
                    enabled = settings?.rateAlert?.enabled ?: true,
                    isPremium = false,
                    onToggle = { viewModel.toggleRateAlert(it) },
                    onLockClick = null
                )
            }

            // 수익률 알림 (프리미엄)
            item {
                ProfitAlertCard(
                    enabled = settings?.recordAlert?.enabled ?: false,
                    minPercent = settings?.conditions?.minProfitPercent ?: 5.0,
                    isPremium = isPremium,
                    onToggle = {
                        if (isPremium) {
                            viewModel.toggleProfitAlert(it)
                        } else {
                            showPremiumDialog = true
                        }
                    },
                    onPercentChange = { viewModel.updateMinProfitPercent(it) },
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
                RecordAgeAlertCard(
                    enabled = settings?.recordAlert?.enabled ?: false,
                    alertDays = settings?.conditions?.recordAgeAlert?.alertDays ?: 7,
                    alertTime = settings?.conditions?.recordAgeAlert?.alertTime ?: "09:00",
                    isPremium = isPremium,
                    onToggle = {
                        if (isPremium) {
                            viewModel.toggleProfitAlert(it)
                        } else {
                            showPremiumDialog = true
                        }
                    },
                    onDaysChange = { viewModel.updateRecordAgeDays(it) },
                    onTimeChange = { viewModel.updateRecordAgeTime(it) },
                    onLockClick = { showPremiumDialog = true }
                )
            }

            // 일일 요약 (프리미엄)
            item {
                DailySummaryCard(
                    enabled = settings?.systemAlert?.enabled ?: false,
                    summaryTime = settings?.conditions?.dailySummary?.summaryTime ?: "20:00",
                    isPremium = isPremium,
                    onToggle = {
                        if (isPremium) {
                            // TODO: systemAlert 토글
                        } else {
                            showPremiumDialog = true
                        }
                    },
                    onTimeChange = { viewModel.updateDailySummaryTime(it) },
                    onLockClick = { showPremiumDialog = true }
                )
            }

            // 테스트 알림
            item {
                OutlinedButton(
                    onClick = { viewModel.sendTestNotification() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Notifications, null)
                    Spacer(Modifier.width(8.dp))
                    Text("테스트 알림 보내기")
                }
            }
        }
    }

    // 프리미엄 다이얼로그
    if (showPremiumDialog) {
        AlertDialog(
            onDismissRequest = { showPremiumDialog = false },
            title = { Text("프리미엄 기능") },
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

@Composable
fun NotificationCard(
    icon: String,
    title: String,
    description: String,
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

@Composable
fun ProfitAlertCard(
    enabled: Boolean,
    minPercent: Double,
    isPremium: Boolean,
    onToggle: (Boolean) -> Unit,
    onPercentChange: (Double) -> Unit,
    onLockClick: () -> Unit
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
                    Text("📊", fontSize = 28.sp)
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "수익률 알림",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium
                            )
                            if (!isPremium) {
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
                            text = "수익률 목표 달성 시 즉시 알림",
                            fontSize = 13.sp,
                            color = Color(0xFF6B7280)
                        )
                    }
                }

                Switch(
                    checked = enabled,
                    onCheckedChange = onToggle,
                    enabled = isPremium
                )
            }

            AnimatedVisibility(visible = enabled && isPremium) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                ) {
                    Divider()
                    Spacer(Modifier.height(16.dp))

                    Text(
                        text = "최소 수익률: ${minPercent.toInt()}%",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF374151)
                    )

                    Slider(
                        value = minPercent.toFloat(),
                        onValueChange = { onPercentChange(it.toDouble()) },
                        valueRange = 1f..20f,
                        steps = 18,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text(
                        text = "${minPercent.toInt()}% 이상 수익 발생 시 알림",
                        fontSize = 12.sp,
                        color = Color(0xFF6B7280)
                    )
                }
            }
        }
    }
}

@Composable
fun RecordAgeAlertCard(
    enabled: Boolean,
    alertDays: Int,
    alertTime: String,
    isPremium: Boolean,
    onToggle: (Boolean) -> Unit,
    onDaysChange: (Int) -> Unit,
    onTimeChange: (String) -> Unit,
    onLockClick: () -> Unit
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
                    Text("⏰", fontSize = 28.sp)
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "매수 경과 알림",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium
                            )
                            if (!isPremium) {
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
                            text = "매수 후 일정 기간 경과 시 알림",
                            fontSize = 13.sp,
                            color = Color(0xFF6B7280)
                        )
                    }
                }

                Switch(
                    checked = enabled,
                    onCheckedChange = onToggle,
                    enabled = isPremium
                )
            }

            AnimatedVisibility(visible = enabled && isPremium) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                ) {
                    Divider()
                    Spacer(Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "경과일",
                            fontSize = 14.sp,
                            color = Color(0xFF374151)
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf(7, 14, 30).forEach { days ->
                                FilterChip(
                                    selected = alertDays == days,
                                    onClick = { onDaysChange(days) },
                                    label = { Text("${days}일") }
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFF3F4F6))
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "알림 시간",
                            fontSize = 14.sp,
                            color = Color(0xFF374151)
                        )

                        Text(
                            text = alertTime,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF1F2937)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DailySummaryCard(
    enabled: Boolean,
    summaryTime: String,
    isPremium: Boolean,
    onToggle: (Boolean) -> Unit,
    onTimeChange: (String) -> Unit,
    onLockClick: () -> Unit
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
                    Text("📊", fontSize = 28.sp)
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "일일 요약",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium
                            )
                            if (!isPremium) {
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
                            text = "하루 한 번 요약 리포트",
                            fontSize = 13.sp,
                            color = Color(0xFF6B7280)
                        )
                    }
                }

                Switch(
                    checked = enabled,
                    onCheckedChange = onToggle,
                    enabled = isPremium
                )
            }

            AnimatedVisibility(visible = enabled && isPremium) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                ) {
                    Divider()
                    Spacer(Modifier.height(16.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFF3F4F6))
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("요약 시간", fontSize = 14.sp)
                        Text(
                            text = summaryTime,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}