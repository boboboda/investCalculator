// app/src/main/java/com/bobodroid/myapplication/screens/FcmAlarmScreen.kt

package com.bobodroid.myapplication.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import com.bobodroid.myapplication.components.Dialogs.ImprovedTargetRateDialog
import com.bobodroid.myapplication.components.common.CurrencyDropdown
import com.bobodroid.myapplication.models.datamodels.notification.RecordWithAlert
import com.bobodroid.myapplication.models.datamodels.roomDb.*
import com.bobodroid.myapplication.models.datamodels.service.UserApi.Rate
import com.bobodroid.myapplication.models.viewmodels.AlarmUiState
import com.bobodroid.myapplication.models.viewmodels.FcmAlarmViewModel
import java.text.NumberFormat
import java.util.Locale

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
    var showPremiumDialog by remember { mutableStateOf(false) }

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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF9FAFB))
    ) {
        // ✅ 컴팩트 헤더
        CompactRateHeader(
            currency = selectedCurrency,
            latestRate = alarmUiState.recentRate.getRateByCode(selectedCurrency.code) ?: "0.00",
            isPremium = isPremium,
            onCurrencyChange = { currency ->
                val success = viewModel.updateCurrentForeignCurrency(currency)
                if (!success) {
                    showPremiumDialog = true
                }
                success
            },
            onPremiumRequired = { showPremiumDialog = true },
            onSettingsClick = onNavigateToSettings
        )

        // 탭 선택
        ScrollableTabRow(
            selectedTabIndex = availableTabs.indexOf(selectedTab),
            containerColor = Color.White,
            edgePadding = 0.dp
        ) {
            availableTabs.forEach { tab ->
                Tab(
                    selected = selectedTab == tab,
                    onClick = { selectedTab = tab },
                    text = {
                        Text(
                            text = "${tab.icon} ${tab.title}",
                            fontWeight = if (selectedTab == tab) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                )
            }
        }

        // 탭 내용
        when (selectedTab) {
            AlarmTab.RATE_ALERT -> TargetRateTab(
                targetRateData = targetRateData,
                selectedCurrency = selectedCurrency,
                currentRate = alarmUiState.recentRate.getRateByCode(selectedCurrency.code) ?: "0.00",
                onDeleteRate = viewModel::deleteTargetRate,
                onAddRate = viewModel::addTargetRate
            )

            AlarmTab.PROFIT_ALERT -> {
                if (isPremium) {
                    ProfitAlertTabNew(viewModel = viewModel)
                } else {
                    PremiumLockScreen()
                }
            }

            AlarmTab.RECORD_AGE -> {
                if (isPremium) {
                    RecordAgeTab(
                        alertDays = notificationSettings?.conditions?.recordAgeAlert?.alertDays ?: 7,
                        alertTime = notificationSettings?.conditions?.recordAgeAlert?.alertTime ?: "09:00",
                        history = history.filter { it.type == "RECORD_AGE" },
                        onDaysChange = { viewModel.updateRecordAgeDays(it) },
                        onTimeChange = { viewModel.updateRecordAgeTime(it) },
                        onMarkAsRead = { viewModel.markAsRead(it) }
                    )
                } else {
                    PremiumLockScreen()
                }
            }

            AlarmTab.HISTORY -> NotificationHistoryTab(
                history = history,
                onMarkAsRead = { viewModel.markAsRead(it) }
            )

            AlarmTab.STATS -> {
                if (isPremium) {
                    NotificationStatsTab(stats = stats)
                } else {
                    PremiumLockScreen()
                }
            }
        }
    }

    // 프리미엄 안내 다이얼로그
    if (showPremiumDialog) {
        AlertDialog(
            onDismissRequest = { showPremiumDialog = false },
            title = { Text("🔒 프리미엄 기능") },
            text = { Text("이 기능은 프리미엄 사용자만 이용할 수 있습니다.") },
            confirmButton = {
                TextButton(onClick = { showPremiumDialog = false }) {
                    Text("확인")
                }
            }
        )
    }
}

// ✅ 컴팩트 헤더
@Composable
fun CompactRateHeader(
    currency: CurrencyType,
    latestRate: String,
    isPremium: Boolean,
    onCurrencyChange: (CurrencyType) -> Boolean,
    onPremiumRequired: () -> Unit,
    onSettingsClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.horizontalGradient(
                    colors = listOf(
                        Color(0xFF6366F1),
                        Color(0xFF8B5CF6)
                    )
                )
            )
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 왼쪽: 통화 선택 + 환율
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                CurrencyDropdown(
                    selectedCurrency = currency,
                    updateCurrentForeignCurrency = onCurrencyChange,
                    isPremium = isPremium,
                    onPremiumRequired = onPremiumRequired,
                    backgroundColor = Color.White.copy(alpha = 0.2f),
                    contentColor = Color.White
                )

                Column {
                    Text(
                        text = "$latestRate 원",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "현재 환율",
                        fontSize = 11.sp,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                }
            }

            // 오른쪽: 설정 버튼
            IconButton(
                onClick = onSettingsClick,
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        color = Color.White.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(20.dp)
                    )
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

// ==================== 1. 목표환율 탭 ====================
@Composable
fun TargetRateTab(
    targetRateData: TargetRates,
    selectedCurrency: CurrencyType,
    currentRate: String,
    onDeleteRate: (Rate, RateType) -> Unit,
    onAddRate: (Rate, RateType) -> Unit
) {
    var selectedDirection by remember { mutableStateOf(RateDirection.HIGH) }
    var showAddDialog by remember { mutableStateOf(false) }

    val rates = targetRateData.getRates(selectedCurrency, selectedDirection)

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // ✅ 고점/저점 선택 + 추가 버튼
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 고점/저점 토글
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(
                        RateDirection.HIGH to "고점 알림",
                        RateDirection.LOW to "저점 알림"
                    ).forEach { (direction, label) ->
                        val isSelected = selectedDirection == direction
                        val isHigh = direction == RateDirection.HIGH

                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedDirection = direction },
                            label = {
                                Text(
                                    text = label,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = if (isHigh)
                                        Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = Color.White,
                                selectedContainerColor = if (isHigh)
                                    Color(0xFFEF4444) else Color(0xFF3B82F6),
                                labelColor = if (isHigh)
                                    Color(0xFFEF4444) else Color(0xFF3B82F6),
                                selectedLabelColor = Color.White,
                                iconColor = if (isHigh)
                                    Color(0xFFEF4444) else Color(0xFF3B82F6),
                                selectedLeadingIconColor = Color.White
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isSelected,
                                borderColor = if (isHigh)
                                    Color(0xFFEF4444) else Color(0xFF3B82F6),
                                selectedBorderColor = Color.Transparent,
                                borderWidth = 1.5.dp
                            )
                        )
                    }
                }

                // ✅ 추가 버튼
                Button(
                    onClick = { showAddDialog = true },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent
                    ),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
                    modifier = Modifier
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    Color(0xFF6366F1),
                                    Color(0xFF8B5CF6)
                                )
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = Color.White
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = "추가",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }

        // ✅ 목표환율 리스트
        if (rates.isEmpty()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF9FAFB)),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(0.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(40.dp),
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
                                text = "설정된 목표환율이 없습니다",
                                fontSize = 16.sp,
                                color = Color(0xFF6B7280)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = { showAddDialog = true },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.Transparent
                                ),
                                modifier = Modifier
                                    .background(
                                        brush = Brush.horizontalGradient(
                                            colors = listOf(
                                                Color(0xFF6366F1),
                                                Color(0xFF8B5CF6)
                                            )
                                        ),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp),
                                    tint = Color.White
                                )
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    text = "목표환율 추가하기",
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }
        } else {
            items(rates) { rate ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
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
                                        text = rate.number.toString(),
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF667eea)
                                    )
                                }
                            }

                            Column {
                                Text(
                                    text = "${rate.rate}원",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1a1a1a)
                                )
                                Text(
                                    text = "${selectedCurrency.emoji} ${selectedCurrency.koreanName} ${if (selectedDirection == RateDirection.HIGH) "고점" else "저점"}",
                                    fontSize = 13.sp,
                                    color = Color(0xFF6B7280)
                                )
                            }
                        }

                        IconButton(
                            onClick = {
                                onDeleteRate(
                                    rate,
                                    RateType(selectedCurrency, selectedDirection)
                                )
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "삭제",
                                tint = Color(0xFFEF4444)
                            )
                        }
                    }
                }
            }
        }
    }

    // ✅ 목표환율 추가 다이얼로그
    if (showAddDialog) {
        val lastRate = rates.lastOrNull()

        ImprovedTargetRateDialog(
            currency = selectedCurrency,
            direction = selectedDirection,
            currentRate = currentRate,
            lastRate = lastRate,
            onDismiss = { showAddDialog = false },
            onConfirm = { newRate ->
                onAddRate(newRate, RateType(selectedCurrency, selectedDirection))
                showAddDialog = false
            }
        )
    }
}

// ==================== 2. 수익률 알림 탭 ====================
@Composable
fun ProfitAlertTabNew(
    viewModel: FcmAlarmViewModel
) {
    val recordsWithAlerts by viewModel.recordsWithAlerts.collectAsState()
    val isLoading by viewModel.profitAlertLoading.collectAsState()
    val message by viewModel.profitAlertMessage.collectAsState()
    val saveSuccess by viewModel.saveSuccess.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    // 메시지 표시
    LaunchedEffect(message) {
        message?.let {
            snackbarHostState.showSnackbar(
                message = it,
                duration = SnackbarDuration.Short
            )
            viewModel.clearProfitAlertMessage()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Color.Transparent
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // 안내 텍스트
            Text(
                text = "보유 중인 각 기록마다 목표 수익률을 설정하세요",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            when {
                isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                recordsWithAlerts.isEmpty() -> {
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
                                text = "보유 중인 기록이 없습니다",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "외화를 매수한 후 알림을 설정할 수 있습니다",
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                else -> {
                    // 기록 리스트
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(
                            items = recordsWithAlerts,
                            key = { it.recordId }
                        ) { record ->
                            RecordAlertCard(
                                record = record,
                                onPercentChange = { newPercent ->
                                    viewModel.updateRecordProfitPercent(record.recordId, newPercent)
                                }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // 저장 버튼
                    Button(
                        onClick = { viewModel.saveRecordAlerts() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        enabled = !isLoading,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        if (isLoading) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    strokeWidth = 2.dp
                                )
                                Text(
                                    text = "저장 중...",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        } else {
                            Text(
                                text = "💾 저장",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun RecordAlertCard(
    record: RecordWithAlert,
    onPercentChange: (Float) -> Unit
) {
    val numberFormat = NumberFormat.getNumberInstance(Locale.KOREA)
    val money = try {
        numberFormat.format(record.money.toDouble().toInt())
    } catch (e: Exception) {
        record.money
    }

    // 환율 변화량 계산
    val rateChange = try {
        val buyRate = record.buyRate.toFloat()
        (buyRate * record.profitPercent / 100).toInt()
    } catch (e: Exception) {
        0
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // 기록 정보
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = record.currencyCode,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = record.categoryName,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${money}원 투자",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = record.date,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 슬라이더
            Column {
                // 현재 선택된 값
                Text(
                    text = "${String.format("%.1f", record.profitPercent)}% (+${numberFormat.format(rateChange)}원)",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // 슬라이더
                Slider(
                    value = record.profitPercent,
                    onValueChange = { newValue ->
                        // 0.1 단위로 반올림
                        val rounded = (newValue * 10).toInt() / 10f
                        onPercentChange(rounded)
                    },
                    valueRange = 0.1f..5.0f,
                    steps = 48,
                    modifier = Modifier.fillMaxWidth()
                )
                // 최소/최대 범위
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    val minChange = try {
                        (record.buyRate.toFloat() * 0.1 / 100).toInt()
                    } catch (e: Exception) {
                        0
                    }
                    val maxChange = try {
                        (record.buyRate.toFloat() * 5.0 / 100).toInt()
                    } catch (e: Exception) {
                        0
                    }

                    Text(
                        text = "0.1% (+${numberFormat.format(minChange)}원)",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "5.0% (+${numberFormat.format(maxChange)}원)",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
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
    history: List<com.bobodroid.myapplication.models.datamodels.notification.NotificationHistoryItem>,
    onDaysChange: (Int) -> Unit,
    onTimeChange: (String) -> Unit,
    onMarkAsRead: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // ✅ 상단: 경과일 설정
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

        // ✅ 상단: 알림 시간
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

        // ✅ 하단: 매수경과 알림 히스토리
        item {
            Text(
                text = "매수경과 알림 히스토리",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        if (history.isEmpty()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF9FAFB)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(text = "📭", fontSize = 48.sp)
                            Text(
                                text = "받은 매수경과 알림이 없습니다",
                                fontSize = 14.sp,
                                color = Color(0xFF9CA3AF)
                            )
                        }
                    }
                }
            }
        } else {
            items(history) { item ->
                NotificationHistoryCard(item, onMarkAsRead)
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
                Text(text = "📭", fontSize = 48.sp)
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
                NotificationHistoryCard(item, onMarkAsRead)
            }
        }
    }
}

// ✅ 알림 히스토리 카드 (공통 컴포넌트)
@Composable
fun NotificationHistoryCard(
    item: com.bobodroid.myapplication.models.datamodels.notification.NotificationHistoryItem,
    onMarkAsRead: (String) -> Unit
) {
    Card(
        onClick = { if (item.status != "READ") onMarkAsRead(item.id) },
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
            if (item.status != "READ") {
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
                    "읽은 알림" to "${stats?.read ?: 0}개",
                    "읽음률" to "${String.format("%.1f", stats?.readRate ?: 0.0)}%"
                ).forEach { (label, value) ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(2.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Text(
                                text = label,
                                fontSize = 12.sp,
                                color = Color(0xFF6B7280)
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = value,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1a1a1a)
                            )
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
                        text = "클릭률",
                        fontSize = 14.sp,
                        color = Color(0xFF6B7280)
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "${String.format("%.1f", stats?.clickRate ?: 0.0)}%",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF667eea)
                    )
                }
            }
        }
    }
}

// ==================== 프리미엄 잠금 화면 ====================
@Composable
fun PremiumLockScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF9FAFB)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(32.dp)
        ) {
            Text(
                text = "👑",
                fontSize = 64.sp
            )
            Text(
                text = "프리미엄 기능",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "이 기능은 프리미엄 회원만 사용할 수 있습니다",
                fontSize = 14.sp,
                color = Color(0xFF6B7280)
            )
        }
    }
}