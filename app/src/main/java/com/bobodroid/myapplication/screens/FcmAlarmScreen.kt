// app/src/main/java/com/bobodroid/myapplication/screens/FcmAlarmScreen.kt

package com.bobodroid.myapplication.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.bobodroid.myapplication.models.datamodels.roomDb.CurrencyType
import com.bobodroid.myapplication.models.datamodels.roomDb.RateType
import com.bobodroid.myapplication.models.datamodels.service.UserApi.Rate
import com.bobodroid.myapplication.models.viewmodels.FcmAlarmViewModel

enum class AlarmTab(val title: String) {
    RATE_ALERT("환율 알림"),
    PROFIT_ALERT("수익률 알림"),
    RECORD_AGE("매수 경과"),
    HISTORY("히스토리"),
    STATS("통계")
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

    // 활성화된 탭 계산
    val availableTabs = remember(notificationSettings, isPremium) {
        buildList {
            add(AlarmTab.RATE_ALERT)

            if (isPremium && notificationSettings?.recordAlert?.enabled == true) {
                add(AlarmTab.PROFIT_ALERT)
                add(AlarmTab.RECORD_AGE)
            }

            add(AlarmTab.HISTORY)

            if (isPremium) {
                add(AlarmTab.STATS)
            }
        }
    }

    var selectedTabIndex by remember { mutableStateOf(0) }

    LaunchedEffect(availableTabs) {
        if (selectedTabIndex >= availableTabs.size) {
            selectedTabIndex = 0
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("알림 관리") },
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "알림 설정",
                            tint = Color(0xFF6366F1)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        }
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // 탭바
            if (availableTabs.isNotEmpty()) {
                ScrollableTabRow(
                    selectedTabIndex = selectedTabIndex,
                    containerColor = Color.White,
                    contentColor = Color(0xFF6366F1)
                ) {
                    availableTabs.forEachIndexed { index, tab ->
                        Tab(
                            selected = selectedTabIndex == index,
                            onClick = { selectedTabIndex = index },
                            text = {
                                Text(
                                    text = tab.title,
                                    fontWeight = if (selectedTabIndex == index)
                                        FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        )
                    }
                }
            }

            // 탭 컨텐츠
            when (availableTabs.getOrNull(selectedTabIndex)) {
                AlarmTab.RATE_ALERT -> {
                    RateAlertContent(
                        targetRateData = targetRateData,
                        onAddRate = { rate, type -> viewModel.addTargetRate(rate, type) },
                        onDeleteRate = { rate, type -> viewModel.deleteTargetRate(rate, type) }
                    )
                }

                AlarmTab.PROFIT_ALERT -> {
                    ProfitAlertContent(
                        minPercent = notificationSettings?.conditions?.minProfitPercent ?: 5.0,
                        onPercentChange = { viewModel.updateMinProfitPercent(it) }
                    )
                }

                AlarmTab.RECORD_AGE -> {
                    RecordAgeContent(
                        alertDays = notificationSettings?.conditions?.recordAgeAlert?.alertDays ?: 7,
                        alertTime = notificationSettings?.conditions?.recordAgeAlert?.alertTime ?: "09:00",
                        onDaysChange = { viewModel.updateRecordAgeDays(it) },
                        onTimeChange = { viewModel.updateRecordAgeTime(it) }
                    )
                }

                AlarmTab.HISTORY -> {
                    HistoryContent(
                        history = history,
                        onMarkAsRead = { viewModel.markAsRead(it) }
                    )
                }

                AlarmTab.STATS -> {
                    StatsContent(stats = stats)
                }

                null -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("탭을 선택해주세요")
                    }
                }
            }
        }
    }
}

// ==================== 환율 알림 컨텐츠 ====================

@Composable
fun RateAlertContent(
    targetRateData: com.bobodroid.myapplication.models.datamodels.roomDb.TargetRates,
    onAddRate: (Rate, RateType) -> Unit,
    onDeleteRate: (Rate, RateType) -> Unit
) {
    var selectedCurrency by remember { mutableStateOf(CurrencyType.USD) }
    var selectedDirection by remember { mutableStateOf("HIGH") }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 통화 선택
        item {
            Card(colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = selectedCurrency == CurrencyType.USD,
                        onClick = { selectedCurrency = CurrencyType.USD },
                        label = { Text("💵 달러") },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = selectedCurrency == CurrencyType.JPY,
                        onClick = { selectedCurrency = CurrencyType.JPY },
                        label = { Text("💴 엔화") },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // 고점/저점 선택
        item {
            Card(colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = selectedDirection == "HIGH",
                        onClick = { selectedDirection = "HIGH" },
                        label = { Text("📈 고점") },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = selectedDirection == "LOW",
                        onClick = { selectedDirection = "LOW" },
                        label = { Text("📉 저점") },
                        modifier = Modifier.weight(1f)
                    )
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

        if (rates.isNullOrEmpty()) {
            item {
                Card(colors = CardDefaults.cardColors(containerColor = Color(0xFFF3F4F6))) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "설정된 목표환율이 없습니다",
                            color = Color(0xFF9CA3AF)
                        )
                    }
                }
            }
        } else {
            items(rates) { rate ->
                RateItemCard(
                    rate = rate,
                    currency = selectedCurrency,
                    direction = selectedDirection,
                    onDelete = {
                        val type = RateType.from(selectedCurrency,
                            if (selectedDirection == "HIGH")
                                com.bobodroid.myapplication.models.datamodels.roomDb.RateDirection.HIGH
                            else
                                com.bobodroid.myapplication.models.datamodels.roomDb.RateDirection.LOW
                        )
                        onDeleteRate(rate, type)
                    }
                )
            }
        }
    }
}

@Composable
fun RateItemCard(
    rate: Rate,
    currency: CurrencyType,
    direction: String,
    onDelete: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (direction == "HIGH") Color(0xFFFEF3C7) else Color(0xFFDCFCE7)
        )
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
                    text = "${rate.rate}원",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (direction == "HIGH") "이상일 때 알림" else "이하일 때 알림",
                    fontSize = 12.sp,
                    color = Color(0xFF6B7280)
                )
            }

            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "삭제",
                    tint = Color(0xFFEF4444)
                )
            }
        }
    }
}

// ==================== 수익률 알림 컨텐츠 ====================

@Composable
fun ProfitAlertContent(
    minPercent: Double,
    onPercentChange: (Double) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(colors = CardDefaults.cardColors(containerColor = Color.White)) {
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

// ==================== 매수 경과 컨텐츠 ====================

@Composable
fun RecordAgeContent(
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
            Card(colors = CardDefaults.cardColors(containerColor = Color.White)) {
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
                            FilterChip(
                                selected = alertDays == days,
                                onClick = { onDaysChange(days) },
                                label = { Text("${days}일") },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }

        item {
            Card(colors = CardDefaults.cardColors(containerColor = Color.White)) {
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

// ==================== 히스토리 컨텐츠 ====================

@Composable
fun HistoryContent(
    history: List<com.bobodroid.myapplication.models.datamodels.notification.NotificationHistoryItem>,
    onMarkAsRead: (String) -> Unit
) {
    if (history.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "받은 알림이 없습니다",
                color = Color(0xFF9CA3AF)
            )
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(history) { item ->
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (item.status == "READ")
                            Color(0xFFF3F4F6) else Color.White
                    ),
                    onClick = { onMarkAsRead(item.id) }
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = item.title,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(Modifier.height(4.dp))
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

// ==================== 통계 컨텐츠 ====================

@Composable
fun StatsContent(
    stats: com.bobodroid.myapplication.models.datamodels.notification.NotificationStats?
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (stats != null) {
            item {
                Card(colors = CardDefaults.cardColors(containerColor = Color.White)) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        Text(
                            text = "알림 통계",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(Modifier.height(20.dp))

                        StatRow("총 알림", "${stats.total}개")
                        StatRow("확인한 알림", "${stats.read}개")
                        StatRow("클릭한 알림", "${stats.clicked}개")
                        StatRow("확인율", "${stats.readRate.toInt()}%")
                        StatRow("클릭율", "${stats.clickRate.toInt()}%")
                    }
                }
            }
        }
    }
}

@Composable
fun StatRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            color = Color(0xFF6B7280)
        )
        Text(
            text = value,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium
        )
    }
}