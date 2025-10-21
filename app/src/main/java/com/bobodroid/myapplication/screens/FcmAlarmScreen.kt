package com.bobodroid.myapplication.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.bobodroid.myapplication.components.common.CurrencyDropdown
import com.bobodroid.myapplication.models.datamodels.roomDb.CurrencyType
import com.bobodroid.myapplication.models.datamodels.roomDb.RateType
import com.bobodroid.myapplication.models.datamodels.service.UserApi.Rate
import com.bobodroid.myapplication.models.viewmodels.FcmAlarmViewModel
import java.text.SimpleDateFormat
import java.util.*

// 탭 정의
enum class AlarmTab(val title: String, val icon: String) {
    RATE_ALERT("목표환율", "🎯"),
    PROFIT_ALERT("수익률 알림", "💰"),
    RECORD_AGE("매수경과", "⏰"),
    HISTORY("알림함", "📜"),
    STATS("통계", "📊")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FcmAlarmScreen(
    viewModel: FcmAlarmViewModel = hiltViewModel(),
    onNavigateToSettings: () -> Unit = {}
) {
    val targetRateData by viewModel.targetRateFlow.collectAsState()
    val notificationSettings by viewModel.notificationSettings.collectAsState()
    val isPremium by viewModel.isPremium.collectAsState()
    val history by viewModel.notificationHistory.collectAsState()
    val stats by viewModel.notificationStats.collectAsState()
    val alarmUiState by viewModel.alarmUiState.collectAsState()
    val selectedCurrency by viewModel.selectedCurrency.collectAsState()

    var selectedTab by remember { mutableStateOf(AlarmTab.RATE_ALERT) }
    var showAddDialog by remember { mutableStateOf(false) }

    // 사용 가능한 탭 계산
    val availableTabs = remember(isPremium) {
        listOf(
            AlarmTab.RATE_ALERT,
            AlarmTab.HISTORY,
            if (isPremium) AlarmTab.PROFIT_ALERT else null,
            if (isPremium) AlarmTab.RECORD_AGE else null,
            if (isPremium) AlarmTab.STATS else null
        ).filterNotNull()
    }

    Scaffold(
        topBar = {
            // 헤더
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.Transparent
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    Color(0xFF667eea),
                                    Color(0xFF764ba2)
                                )
                            )
                        )
                        .padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = "🔔",
                                fontSize = 24.sp
                            )
                            Text(
                                text = "알림 관리",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        Surface(
                            onClick = onNavigateToSettings,
                            shape = CircleShape,
                            color = Color.White.copy(alpha = 0.2f),
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.fillMaxSize()
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Settings,
                                    contentDescription = "설정",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        },
        floatingActionButton = {
            // FAB은 목표환율 탭에서만 표시
            if (selectedTab == AlarmTab.RATE_ALERT) {
                FloatingActionButton(
                    onClick = { showAddDialog = true },
                    containerColor = Color(0xFF667eea),
                    contentColor = Color.White,
                    shape = CircleShape
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "목표환율 추가",
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        },
        containerColor = Color(0xFFF5F5F5)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // 탭 네비게이션
            ScrollableTabRow(
                selectedTabIndex = availableTabs.indexOf(selectedTab),
                containerColor = Color.White,
                contentColor = Color(0xFF667eea),
                edgePadding = 8.dp,
                indicator = { },
                divider = { }
            ) {
                availableTabs.forEach { tab ->
                    val isSelected = selectedTab == tab
                    val isPremiumTab = tab in listOf(
                        AlarmTab.PROFIT_ALERT,
                        AlarmTab.RECORD_AGE,
                        AlarmTab.STATS
                    )

                    Tab(
                        selected = isSelected,
                        onClick = { selectedTab = tab },
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 12.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) Color(0xFF667eea) else Color.Transparent,
                            modifier = Modifier.padding(4.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(
                                    horizontal = 16.dp,
                                    vertical = 10.dp
                                ),
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = tab.icon,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = tab.title,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (isSelected) Color.White else Color(0xFF6B7280)
                                )
                                if (isPremiumTab && !isPremium) {
                                    Text(
                                        text = "👑",
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 탭 컨텐츠
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFF5F5F5))
            ) {
                when (selectedTab) {
                    AlarmTab.RATE_ALERT -> {
                        TargetRateTab(
                            targetRateData = targetRateData,
                            alarmUiState = alarmUiState,
                            selectedCurrency = selectedCurrency,
                            isPremium = isPremium,
                            onCurrencyChange = { viewModel.updateCurrentForeignCurrency(it) },
                            onPremiumRequired = { /* 프리미엄 안내 */ },
                            onDeleteRate = { rate, type ->
                                viewModel.deleteTargetRate(rate, type)
                            }
                        )
                    }

                    AlarmTab.PROFIT_ALERT -> {
                        if (isPremium) {
                            ProfitAlertTab(
                                minPercent = notificationSettings?.conditions?.minProfitPercent ?: 5.0,
                                onPercentChange = { viewModel.updateMinProfitPercent(it) }
                            )
                        } else {
                            PremiumLockScreen()
                        }
                    }

                    AlarmTab.RECORD_AGE -> {
                        if (isPremium) {
                            RecordAgeTab(
                                alertDays = notificationSettings?.conditions?.recordAgeAlert?.alertDays ?: 7,
                                alertTime = notificationSettings?.conditions?.recordAgeAlert?.alertTime ?: "09:00",
                                onDaysChange = { viewModel.updateRecordAgeDays(it) },
                                onTimeChange = { viewModel.updateRecordAgeTime(it) }
                            )
                        } else {
                            PremiumLockScreen()
                        }
                    }

                    AlarmTab.HISTORY -> {
                        NotificationHistoryTab(
                            history = history,
                            onMarkAsRead = { viewModel.markAsRead(it) }
                        )
                    }

                    AlarmTab.STATS -> {
                        if (isPremium) {
                            NotificationStatsTab(stats = stats)
                        } else {
                            PremiumLockScreen()
                        }
                    }
                }
            }
        }
    }
}

// ==================== 1. 목표환율 탭 ====================
@Composable
fun TargetRateTab(
    targetRateData: com.bobodroid.myapplication.models.datamodels.roomDb.TargetRates,
    alarmUiState: com.bobodroid.myapplication.models.viewmodels.AlarmUiState,
    selectedCurrency: CurrencyType,
    isPremium: Boolean,
    onCurrencyChange: (CurrencyType) -> Boolean,
    onPremiumRequired: () -> Unit,
    onDeleteRate: (Rate, RateType) -> Unit
) {
    var selectedDirection by remember { mutableStateOf("HIGH") }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 통화 선택
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Box(modifier = Modifier.padding(12.dp)) {
                    CurrencyDropdown(
                        selectedCurrency = selectedCurrency,
                        updateCurrentForeignCurrency = onCurrencyChange,
                        isPremium = isPremium,
                        onPremiumRequired = onPremiumRequired,
                        backgroundColor = Color(0xFFF8F9FA),
                        contentColor = Color(0xFF1a1a1a)
                    )
                }
            }
        }

        // 현재 환율 카드
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    Color(0xFF667eea),
                                    Color(0xFF764ba2)
                                )
                            )
                        )
                        .padding(24.dp)
                ) {
                    Column {
                        Text(
                            text = "💵 현재 환율",
                            fontSize = 14.sp,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = "${alarmUiState.recentRate.exchangeRate} 원",
                            fontSize = 40.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = "▲ 12.30 (+0.87%)",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFFFFE5E5)
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                                .format(Date()),
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                }
            }
        }

        // 고점/저점 탭
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("HIGH" to "고점 목표", "LOW" to "저점 목표").forEach { (type, label) ->
                        Surface(
                            onClick = { selectedDirection = type },
                            shape = RoundedCornerShape(12.dp),
                            color = if (selectedDirection == type) Color(0xFF667eea) else Color.Transparent,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier.padding(12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (selectedDirection == type) Color.White else Color(0xFF6B7280)
                                )
                            }
                        }
                    }
                }
            }
        }

        // 목표환율 리스트
        val rates = when {
            selectedCurrency == CurrencyType.USD && selectedDirection == "HIGH" ->
                targetRateData.dollarHighRates
            selectedCurrency == CurrencyType.USD && selectedDirection == "LOW" ->
                targetRateData.dollarLowRates
            selectedCurrency == CurrencyType.JPY && selectedDirection == "HIGH" ->
                targetRateData.yenHighRates
            else -> targetRateData.yenLowRates
        }

        items(rates) { rate ->
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFF3F4F6),
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.fillMaxSize()
                            ) {
                                Text(
                                    text = "${rate.id}",
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF667eea)
                                )
                            }
                        }

                        Text(
                            text = "${rate.rate}원",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1a1a1a)
                        )
                    }

                    Surface(
                        onClick = {
                            val rateType = if (selectedDirection == "HIGH") {
                                if (selectedCurrency == CurrencyType.USD)
                                    RateType.DOLLAR_HIGH_RATE
                                else RateType.YEN_HIGH_RATE
                            } else {
                                if (selectedCurrency == CurrencyType.USD)
                                    RateType.DOLLAR_LOW_RATE
                                else RateType.YEN_LOW_RATE
                            }
                            onDeleteRate(rate, rateType)
                        },
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFFFEE2E2),
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "삭제",
                                tint = Color(0xFFDC2626),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

// ==================== 2. 수익률 알림 탭 ====================
@Composable
fun ProfitAlertTab(
    minPercent: Double,
    onPercentChange: (Double) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Text(
                        text = "최소 수익률 설정",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(Modifier.height(8.dp))

                    Text(
                        text = "수익률이 ${minPercent.toInt()}% 이상일 때 알림을 받습니다",
                        fontSize = 14.sp,
                        color = Color(0xFF6B7280)
                    )

                    Spacer(Modifier.height(20.dp))

                    Text(
                        text = "${minPercent.toInt()}%",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF16A34A)
                    )

                    Slider(
                        value = minPercent.toFloat(),
                        onValueChange = { onPercentChange(it.toDouble()) },
                        valueRange = 1f..20f,
                        steps = 18,
                        colors = SliderDefaults.colors(
                            thumbColor = Color(0xFF667eea),
                            activeTrackColor = Color(0xFF667eea)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("1%", fontSize = 12.sp, color = Color(0xFF9CA3AF))
                        Text("20%", fontSize = 12.sp, color = Color(0xFF9CA3AF))
                    }
                }
            }
        }
    }
}

// ==================== 3. 매수경과 탭 ====================
@Composable
fun RecordAgeTab(
    alertDays: Int,
    alertTime: String,
    onDaysChange: (Int) -> Unit,
    onTimeChange: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Text(
                        text = "경과일 설정",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(7, 14, 30).forEach { days ->
                            Surface(
                                onClick = { onDaysChange(days) },
                                shape = RoundedCornerShape(12.dp),
                                color = if (alertDays == days) Color(0xFF667eea) else Color(0xFFF3F4F6),
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier.padding(vertical = 12.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "${days}일",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (alertDays == days) Color.White else Color(0xFF6B7280)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Text(
                        text = "알림 시간",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(Modifier.height(16.dp))

                    Text(
                        text = alertTime,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF6366F1)
                    )
                }
            }
        }
    }
}

// ==================== 4. 알림함 탭 ====================
@Composable
fun NotificationHistoryTab(
    history: List<com.bobodroid.myapplication.models.datamodels.notification.NotificationHistoryItem>,
    onMarkAsRead: (String) -> Unit
) {
    if (history.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "📭",
                    fontSize = 48.sp
                )
                Text(
                    text = "받은 알림이 없습니다",
                    color = Color(0xFF9CA3AF),
                    fontSize = 14.sp
                )
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(history) { item ->
                Card(
                    onClick = { onMarkAsRead(item.id) },
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White
                    ),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // 읽지 않은 알림 표시
                        if (item.status == "UNREAD") {
                            Box(
                                modifier = Modifier
                                    .width(4.dp)
                                    .fillMaxHeight()
                                    .background(Color(0xFF667eea))
                            )
                        }

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Text(
                                text = item.title,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF1a1a1a)
                            )
                            Spacer(Modifier.height(6.dp))
                            Text(
                                text = item.body,
                                fontSize = 14.sp,
                                color = Color(0xFF6B7280)
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = item.sentAt,
                                fontSize = 12.sp,
                                color = Color(0xFF9CA3AF)
                            )
                        }
                    }
                }
            }
        }
    }
}

// ==================== 5. 통계 탭 ====================
@Composable
fun NotificationStatsTab(
    stats: com.bobodroid.myapplication.models.datamodels.notification.NotificationStats?
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Text(
                        text = "총 알림",
                        fontSize = 14.sp,
                        color = Color(0xFF6B7280)
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "${stats?.total ?: 0}개",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1a1a1a)
                    )
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                listOf(
                    "읽은 알림" to (stats?.read ?: 0),
                    "클릭률" to "${(stats?.clickRate ?: 0.0).toInt()}%"
                ).forEach { (label, value) ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F9FA)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Text(
                                text = label,
                                fontSize = 13.sp,
                                color = Color(0xFF6B7280)
                            )
                            Spacer(Modifier.height(6.dp))
                            Text(
                                text = value.toString(),
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF667eea)
                            )
                        }
                    }
                }
            }
        }
    }
}

// ==================== 프리미엄 잠금 화면 ====================
@Composable
fun PremiumLockScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(60.dp)
        ) {
            Text(
                text = "🔒",
                fontSize = 64.sp,
                color = Color(0xFF6B7280).copy(alpha = 0.5f)
            )
            Text(
                text = "프리미엄 기능",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1a1a1a)
            )
            Text(
                text = "상세한 알림 통계는\n프리미엄 플랜에서 확인하실 수 있습니다",
                fontSize = 14.sp,
                color = Color(0xFF6B7280),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Spacer(Modifier.height(8.dp))
            Button(
                onClick = { /* 프리미엄 구독 화면 이동 */ },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFCD34D)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "👑 프리미엄 구독하기",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )
            }
        }
    }
}