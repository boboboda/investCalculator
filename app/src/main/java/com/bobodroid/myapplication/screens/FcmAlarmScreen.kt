// app/src/main/java/com/bobodroid/myapplication/screens/FcmAlarmScreen.kt

package com.bobodroid.myapplication.screens

import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.bobodroid.myapplication.components.Dialogs.ImprovedTargetRateDialog
import com.bobodroid.myapplication.components.Dialogs.PremiumPromptDialog
import com.bobodroid.myapplication.components.Dialogs.RewardAdInfoDialog
import com.bobodroid.myapplication.components.common.CurrencyDropdown
import com.bobodroid.myapplication.models.datamodels.service.notificationApi.RecordWithAlert
import com.bobodroid.myapplication.models.datamodels.roomDb.*
import com.bobodroid.myapplication.models.datamodels.service.UserApi.Rate
import com.bobodroid.myapplication.models.datamodels.service.notificationApi.NotificationHistoryItem
import com.bobodroid.myapplication.models.datamodels.service.notificationApi.NotificationStats
import com.bobodroid.myapplication.models.viewmodels.FcmAlarmViewModel
import com.bobodroid.myapplication.models.viewmodels.SharedViewModel
import com.bobodroid.myapplication.models.viewmodels.SpreadRateInfo
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

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
    sharedViewModel: SharedViewModel,
    onNavigateToSettings: () -> Unit = {}
) {

    val isPremium by sharedViewModel.isPremium.collectAsState()
    val showPremiumPrompt by sharedViewModel.showPremiumPrompt.collectAsState()
    val showRewardAdInfo by sharedViewModel.showRewardAdInfo.collectAsState()

    val targetRateData by viewModel.targetRateFlow.collectAsState()
    val notificationSettings by viewModel.notificationSettings.collectAsState()
    val history by viewModel.notificationHistory.collectAsState()
    val stats by viewModel.notificationStats.collectAsState()
    val alarmUiState by viewModel.alarmUiState.collectAsState()
    val selectedCurrency by viewModel.selectedCurrency.collectAsState()
    val spreadInfo by viewModel.spreadRateInfo.collectAsState()

    // 마이페이지에서 스프레드 변경 후 돌아왔을 때 최신 값 반영
    LaunchedEffect(Unit) {
        viewModel.refreshSpread()
    }

    var selectedTab by remember { mutableStateOf(AlarmTab.RATE_ALERT) }
    var showPremiumDialog by remember { mutableStateOf(false) }

    val context = LocalContext.current

    // ✅ 디버그 로그 추가
    LaunchedEffect(isPremium) {
        Log.d("FcmAlarmScreen", "━━━━━━━━━━━━━━━━━━━━━━━━━━")
        Log.d("FcmAlarmScreen", "isPremium 상태: $isPremium")
        Log.d("FcmAlarmScreen", "━━━━━━━━━━━━━━━━━━━━━━━━━━")
    }

    LaunchedEffect(Unit) {
        if (!isPremium) {
            // ✅ ViewModel이 UseCase 호출 → UseCase가 AdManager 호출
            sharedViewModel.showInterstitialAdIfNeeded(context)
        }
    }


    // ✅ 사용 가능한 탭 계산 (무료: 목표환율 + 알림함만)
    val availableTabs = remember(isPremium) {
        buildList {
            add(AlarmTab.RATE_ALERT)  // 무료
            add(AlarmTab.HISTORY)     // 무료

            // 프리미엄 전용
            if (isPremium) {
                add(AlarmTab.PROFIT_ALERT)
                add(AlarmTab.RECORD_AGE)
                add(AlarmTab.STATS)
            }
        }
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
                spreadInfo = spreadInfo,
                onDeleteRate = viewModel::deleteTargetRate,
                onAddRate = viewModel::addTargetRate,
                onSpreadSettingsClick = onNavigateToSettings
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
                viewModel = viewModel
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

    if (showPremiumPrompt) {
        PremiumPromptDialog(
            onWatchAd = {
                sharedViewModel.closePremiumPromptAndShowRewardDialog()
            },
            onDismiss = {
                sharedViewModel.closePremiumPrompt()
            }
        )
    }

    // 리워드 광고 안내 팝업
    if (showRewardAdInfo) {
        RewardAdInfoDialog(
            onConfirm = {
                sharedViewModel.showRewardAdAndGrantPremium(context)
            },
            onDismiss = {
                sharedViewModel.closeRewardAdDialog()
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
    spreadInfo: SpreadRateInfo,
    onDeleteRate: (Rate, RateType) -> Unit,
    onAddRate: (Rate, RateType) -> Unit,
    onSpreadSettingsClick: () -> Unit
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

        // ✅ 스프레드 반영 기준 환율
        item {
            SpreadBasisCard(
                direction = selectedDirection,
                spreadInfo = spreadInfo,
                onSpreadSettingsClick = onSpreadSettingsClick
            )
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
                                    text = "${selectedCurrency.emoji} ${selectedCurrency.koreanName} ${if (selectedDirection == RateDirection.HIGH) "고점" else "저점"}" +
                                            if (rate.recordId != null) " · 기록 알람" else "",
                                    fontSize = 13.sp,
                                    color = if (rate.recordId != null) Color(0xFFB45309) else Color(0xFF6B7280)
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


// ==================== 수익률 알림 탭 ====================
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
            Column(modifier = Modifier.padding(bottom = 16.dp)) {
                Text(
                    text = "보유 중인 각 기록마다 목표 수익률을 설정하세요",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "스위치를 켜서 알림을 받을 기록만 선택하세요",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

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
                                onToggle = { enabled ->
                                    viewModel.toggleRecordAlert(record.recordId, enabled)
                                },
                                onPercentChange = { newPercent ->
                                    viewModel.updateRecordProfitPercent(record.recordId, newPercent)
                                }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // 활성화된 기록 수 표시
                    val enabledCount = recordsWithAlerts.count { it.enabled }
                    if (enabledCount > 0) {
                        Text(
                            text = "💡 ${enabledCount}개 기록의 알림이 활성화됩니다",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }

                    // 저장 버튼
                    Button(
                        onClick = { viewModel.saveRecordAlerts() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        enabled = !isLoading && enabledCount > 0,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant
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
                                text = if (enabledCount > 0) "💾 저장 (${enabledCount}개)" else "💾 저장",
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
    onToggle: (Boolean) -> Unit,
    onPercentChange: (Float) -> Unit
) {
    val numberFormat = NumberFormat.getNumberInstance(Locale.KOREA)

    // 투자 금액
    val investMoney = try {
        record.money.toDouble()
    } catch (e: Exception) {
        0.0
    }

    val moneyFormatted = try {
        numberFormat.format(investMoney.toInt())
    } catch (e: Exception) {
        record.money
    }

    // 매수 환율
    val buyRate = try {
        record.buyRate.toFloat()
    } catch (e: Exception) {
        0f
    }

    // 외화량
    val exchangeMoney = try {
        record.exchangeMoney.toDouble()
    } catch (e: Exception) {
        0.0
    }

    // 목표 환율 계산
    val targetRate = if (record.enabled && record.profitPercent != null) {
        try {
            buyRate * (1 + record.profitPercent!! / 100)
        } catch (e: Exception) {
            0f
        }
    } else {
        0f
    }

    // ✅ 올바른 예상 수익금액 계산
    val expectedProfit = if (record.enabled && record.profitPercent != null) {
        try {
            // 예상 수익 = (외화량 × 목표환율) - 투자금
            (exchangeMoney * targetRate) - investMoney
        } catch (e: Exception) {
            0.0
        }
    } else {
        0.0
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (record.enabled) Color.White else Color(0xFFF5F5F5)
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (record.enabled) 4.dp else 1.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // 헤더: 기록 정보 + 토글
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
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
                            color = if (record.enabled)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = record.categoryName,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (record.enabled)
                                Color.Black
                            else
                                MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = record.date,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // 알림 ON/OFF 스위치
                Switch(
                    checked = record.enabled,
                    onCheckedChange = onToggle,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = MaterialTheme.colorScheme.primary,
                        uncheckedThumbColor = Color.White,
                        uncheckedTrackColor = Color(0xFFCCCCCC)
                    )
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 투자 정보
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                InfoItem(
                    label = "투자금액",
                    value = "${moneyFormatted}원",
                    enabled = record.enabled
                )
                InfoItem(
                    label = "매수환율",
                    value = "${numberFormat.format(buyRate.toInt())}원",
                    enabled = record.enabled
                )
                InfoItem(
                    label = "외화량",
                    value = "${String.format("%.2f", exchangeMoney)} ${record.currencyCode}",
                    enabled = record.enabled
                )
            }

            // 알림이 활성화된 경우에만 슬라이더 표시
            if (record.enabled) {
                Spacer(modifier = Modifier.height(16.dp))
                Divider(color = Color(0xFFE0E0E0))
                Spacer(modifier = Modifier.height(16.dp))

                // 목표 수익률 설정
                Column {
                    // 현재 설정값
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Text(
                            text = "목표 수익률",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${String.format("%.1f", record.profitPercent ?: 0.4f)}%",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // 목표 환율 & 예상 수익
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "목표 환율",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "${numberFormat.format(targetRate.toInt())}원",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF4CAF50)
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "예상 수익",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "${if (expectedProfit >= 0) "+" else ""}${numberFormat.format(expectedProfit.toInt())}원",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (expectedProfit >= 0) Color(0xFFFF9800) else Color(0xFFEF4444)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // 슬라이더
                    Slider(
                        value = record.profitPercent ?: 0.4f,
                        onValueChange = { newValue ->
                            // 0.1 단위로 반올림
                            val rounded = (newValue * 10).toInt() / 10f
                            onPercentChange(rounded)
                        },
                        valueRange = 0.1f..5.0f,
                        steps = 48,
                        modifier = Modifier.fillMaxWidth(),
                        colors = SliderDefaults.colors(
                            thumbColor = MaterialTheme.colorScheme.primary,
                            activeTrackColor = MaterialTheme.colorScheme.primary,
                            inactiveTrackColor = Color(0xFFE0E0E0)
                        )
                    )

                    // 최소/최대 범위
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        val minRate = buyRate * 1.001f
                        val maxRate = buyRate * 1.05f
                        val minProfit = (exchangeMoney * minRate) - investMoney
                        val maxProfit = (exchangeMoney * maxRate) - investMoney

                        Text(
                            text = "0.1% (${numberFormat.format(minRate.toInt())}원, +${numberFormat.format(minProfit.toInt())}원)",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "5.0% (${numberFormat.format(maxRate.toInt())}원, +${numberFormat.format(maxProfit.toInt())}원)",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoItem(
    label: String,
    value: String,
    enabled: Boolean
) {
    Column {
        Text(
            text = label,
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = if (enabled) Color.Black else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// ==================== 3. 매수경과 탭 ====================
@Composable
fun RecordAgeTab(
    alertDays: Int,
    alertTime: String,
    history: List<NotificationHistoryItem>,
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun NotificationHistoryTab(
    viewModel: FcmAlarmViewModel,
    history: List<NotificationHistoryItem>
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    var deleteMode by remember { mutableStateOf<DeleteMode>(DeleteMode.NONE) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // ✅ 읽지 않은 알림 개수 표시 (대문자 상태 확인)
        val unreadCount = history.count { it.status.uppercase() == "SENT" }
        val readCount = history.count {
            val status = it.status.uppercase()
            status == "READ" || status == "CLICKED"
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // 읽지 않은 알림
            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFEEF2FF)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.MarkEmailUnread,
                        contentDescription = null,
                        tint = Color(0xFF4F46E5),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "안읽음 $unreadCount",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF4F46E5)
                    )
                }
            }

            // 읽은 알림
            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFF9FAFB)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.DoneAll,
                        contentDescription = null,
                        tint = Color(0xFF9CA3AF),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "읽음 $readCount",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF6B7280)
                    )
                }
            }
        }

        // ✅ 삭제 버튼들
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // 전체 삭제
            OutlinedButton(
                onClick = {
                    deleteMode = DeleteMode.ALL
                    showDeleteDialog = true
                },
                modifier = Modifier.weight(1f),
                enabled = history.isNotEmpty()
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("전체 삭제", fontSize = 13.sp)
            }

            // 읽은 알림 삭제
            OutlinedButton(
                onClick = {
                    deleteMode = DeleteMode.READ
                    showDeleteDialog = true
                },
                modifier = Modifier.weight(1f),
                enabled = history.any {
                    val status = it.status.uppercase()
                    status == "READ" || status == "CLICKED"
                }
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("읽은 알림", fontSize = 13.sp)
            }
        }

        // ✅ 알림 리스트
        if (history.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "알림 내역이 없습니다",
                        color = Color.Gray,
                        fontSize = 16.sp
                    )
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(
                    items = history,
                    key = { it.id }
                ) { notification ->
                    DismissibleNotificationCard(
                        notification = notification,
                        onMarkAsRead = { viewModel.markAsRead(notification.id) },
                        onDelete = { viewModel.deleteNotification(notification.id) },
                        modifier = Modifier.animateItemPlacement()
                    )
                }
            }
        }
    }

    // ✅ 삭제 확인 다이얼로그
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = {
                Text(
                    text = when (deleteMode) {
                        DeleteMode.ALL -> "모든 알림 삭제"
                        DeleteMode.READ -> "읽은 알림 삭제"
                        else -> ""
                    }
                )
            },
            text = {
                Text(
                    text = when (deleteMode) {
                        DeleteMode.ALL -> "모든 알림을 삭제하시겠습니까?\n이 작업은 되돌릴 수 없습니다."
                        DeleteMode.READ -> "읽은 알림을 모두 삭제하시겠습니까?"
                        else -> ""
                    }
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        when (deleteMode) {
                            DeleteMode.ALL -> viewModel.deleteAllNotifications()
                            DeleteMode.READ -> viewModel.deleteReadNotifications()
                            else -> {}
                        }
                        showDeleteDialog = false
                    }
                ) {
                    Text("삭제", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("취소")
                }
            }
        )
    }
}

// ✅ 삭제 모드 Enum
enum class DeleteMode {
    NONE, ALL, READ
}

// ✅ 알림 카드 (읽음/안읽음 구분 + 삭제 기능)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DismissibleNotificationCard(
    notification: NotificationHistoryItem,
    onMarkAsRead: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isRead = notification.status.uppercase() in listOf("READ", "CLICKED")
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = {
            if (it == SwipeToDismissBoxValue.EndToStart) {
                onDelete()
                true
            } else {
                false
            }
        }
    )

    SwipeToDismissBox(
        state = dismissState,
        modifier = modifier, // ✅ modifier를 SwipeToDismissBox에 적용
        backgroundContent = {
            // ✅ 배경을 카드 크기에 정확히 맞춤
            Card(
                modifier = Modifier.fillMaxSize(),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFEF4444)
                ),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "삭제",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        },
        enableDismissFromStartToEnd = false,
        enableDismissFromEndToStart = true
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(), // ✅ modifier 제거
            colors = CardDefaults.cardColors(
                containerColor = if (isRead) Color(0xFFF9FAFB) else Color.White
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = if (isRead) 0.dp else 2.dp
            ),
            border = if (!isRead) BorderStroke(1.dp, Color(0xFFE5E7EB)) else null,
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { if (!isRead) onMarkAsRead() }
                    .padding(16.dp),
                verticalAlignment = Alignment.Top
            ) {
                // ✅ 읽음/안읽음 표시 - 더 명확하게
                Surface(
                    shape = CircleShape,
                    color = if (isRead) Color(0xFFE5E7EB) else Color(0xFF4F46E5),
                    modifier = Modifier.size(10.dp)
                ) {}

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    // 상태 라벨
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        if (!isRead) {
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = Color(0xFFEEF2FF)
                            ) {
                                Text(
                                    text = "NEW",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF4F46E5),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Text(
                            text = notification.title,
                            fontSize = 15.sp,
                            fontWeight = if (isRead) FontWeight.Normal else FontWeight.Bold,
                            color = if (isRead) Color(0xFF6B7280) else Color.Black
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = notification.body,
                        fontSize = 13.sp,
                        color = if (isRead) Color(0xFF9CA3AF) else Color(0xFF6B7280),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = null,
                            tint = Color(0xFF9CA3AF),
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = formatNotificationTime(notification.sentAt),
                            fontSize = 11.sp,
                            color = Color(0xFF9CA3AF)
                        )

                        // ✅ 읽은 시간 표시
                        if (isRead && notification.readAt != null) {
                            Text(
                                text = "•",
                                fontSize = 11.sp,
                                color = Color(0xFF9CA3AF)
                            )
                            Icon(
                                imageVector = Icons.Default.DoneAll,
                                contentDescription = null,
                                tint = Color(0xFF9CA3AF),
                                modifier = Modifier.size(12.dp)
                            )
                            Text(
                                text = formatReadTime(notification.readAt),
                                fontSize = 11.sp,
                                color = Color(0xFF9CA3AF)
                            )
                        }
                    }
                }

                // ✅ 타입 아이콘
                Surface(
                    shape = CircleShape,
                    color = if (isRead) Color(0xFFF3F4F6) else Color(0xFFEEF2FF),
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Icon(
                            imageVector = when (notification.type) {
                                "RATE_ALERT" -> Icons.Default.TrendingUp
                                "PROFIT_ALERT" -> Icons.Default.AttachMoney
                                "RECORD_AGE" -> Icons.Default.AccessTime
                                else -> Icons.Default.Notifications
                            },
                            contentDescription = null,
                            tint = if (isRead) Color(0xFF9CA3AF) else Color(0xFF4F46E5),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}


// ✅ 시간 포맷팅 헬퍼 함수들 (한국 시간 기준)
fun formatNotificationTime(isoTime: String): String {
    return try {
        // UTC 시간을 파싱
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.KOREA)
        inputFormat.timeZone = TimeZone.getTimeZone("UTC")
        val date = inputFormat.parse(isoTime) ?: return isoTime

        // 현재 시간 (시스템 시간 = 한국 시간)
        val now = System.currentTimeMillis()
        val diffMillis = now - date.time
        val diffMinutes = (diffMillis / (1000 * 60)).toInt()
        val diffHours = (diffMillis / (1000 * 60 * 60)).toInt()
        val diffDays = (diffMillis / (1000 * 60 * 60 * 24)).toInt()

        when {
            diffMinutes < 1 -> "방금 전"
            diffMinutes < 60 -> "${diffMinutes}분 전"
            diffHours < 24 -> "${diffHours}시간 전"
            diffDays < 7 -> "${diffDays}일 전"
            else -> {
                // 7일 이상: 한국 시간대로 날짜 표시
                val outputFormat = SimpleDateFormat("MM/dd HH:mm", Locale.KOREA)
                outputFormat.timeZone = TimeZone.getTimeZone("Asia/Seoul")
                outputFormat.format(date)
            }
        }
    } catch (e: Exception) {
        isoTime
    }
}

fun formatReadTime(isoTime: String): String {
    return try {
        // UTC 시간을 파싱
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.KOREA)
        inputFormat.timeZone = TimeZone.getTimeZone("UTC")
        val date = inputFormat.parse(isoTime) ?: return "읽음"

        // 현재 시간 (시스템 시간 = 한국 시간)
        val now = System.currentTimeMillis()
        val diffMillis = now - date.time
        val diffMinutes = (diffMillis / (1000 * 60)).toInt()
        val diffHours = (diffMillis / (1000 * 60 * 60)).toInt()

        when {
            diffMinutes < 1 -> "방금 읽음"
            diffMinutes < 60 -> "${diffMinutes}분 전 읽음"
            diffHours < 24 -> "${diffHours}시간 전 읽음"
            else -> {
                // 24시간 이상: 한국 시간대로 날짜 표시
                val outputFormat = SimpleDateFormat("MM/dd HH:mm", Locale.KOREA)
                outputFormat.timeZone = TimeZone.getTimeZone("Asia/Seoul")
                "${outputFormat.format(date)} 읽음"
            }
        }
    } catch (e: Exception) {
        "읽음"
    }
}




// ✅ 알림 히스토리 카드 (공통 컴포넌트)
@Composable
fun NotificationHistoryCard(
    item: NotificationHistoryItem,
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
    stats: NotificationStats?
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

// ==================== 스프레드 반영 기준 환율 카드 ====================

private fun formatWon(value: Double): String =
    if (value % 1.0 == 0.0) String.format(Locale.KOREA, "%,.0f", value)
    else String.format(Locale.KOREA, "%,.2f", value)

@Composable
fun SpreadBasisCard(
    direction: RateDirection,
    spreadInfo: SpreadRateInfo,
    onSpreadSettingsClick: () -> Unit
) {
    val isHigh = direction == RateDirection.HIGH
    val accent = if (isHigh) Color(0xFFEF4444) else Color(0xFF3B82F6)
    val basisRate = if (isHigh) spreadInfo.sellBasisRate else spreadInfo.buyBasisRate
    val spreadWon = if (isHigh) spreadInfo.sellSpreadWon else spreadInfo.buySpreadWon

    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = if (isHigh) "팔 때 기준 환율" else "살 때 기준 환율",
                        fontSize = 13.sp,
                        color = Color(0xFF6B7280)
                    )
                    Text(
                        text = basisRate?.let { "${formatWon(it)}원" } ?: "-",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = accent
                    )
                }

                if (spreadInfo.hasSpread) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = accent.copy(alpha = 0.1f)
                    ) {
                        Text(
                            text = "스프레드 적용",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = accent,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            if (spreadInfo.hasSpread) {
                Text(
                    text = if (isHigh)
                        "현재 환율 − 매도 스프레드 ${formatWon(spreadWon)}원"
                    else
                        "현재 환율 + 매수 스프레드 ${formatWon(spreadWon)}원",
                    fontSize = 12.sp,
                    color = Color(0xFF6B7280)
                )
                Text(
                    text = "이 기준 환율이 목표환율에 닿으면 알림이 울립니다.",
                    fontSize = 12.sp,
                    color = Color(0xFF9CA3AF)
                )
            } else {
                Text(
                    text = "스프레드가 설정되지 않아 현재 환율 기준으로 알림이 울립니다.",
                    fontSize = 12.sp,
                    color = Color(0xFF6B7280)
                )
                TextButton(
                    onClick = onSpreadSettingsClick,
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text(
                        text = "스프레드 설정하기",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}