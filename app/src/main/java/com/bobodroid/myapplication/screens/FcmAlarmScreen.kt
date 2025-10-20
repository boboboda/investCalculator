package com.bobodroid.myapplication.screens

import android.util.Log
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.with
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.SnackbarDuration
import androidx.compose.material.SnackbarHost
import androidx.compose.material.SnackbarHostState
import androidx.compose.material.Surface
import androidx.compose.material.Tab
import androidx.compose.material.TabRow
import androidx.compose.material.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.sharp.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import com.bobodroid.myapplication.MainActivity.Companion.TAG
import com.bobodroid.myapplication.R
import com.bobodroid.myapplication.components.AutoSizeText
import com.bobodroid.myapplication.components.Dialogs.GuideDialog
import com.bobodroid.myapplication.components.Dialogs.TargetRateDialog
import com.bobodroid.myapplication.components.Dialogs.TextFieldDialog
import com.bobodroid.myapplication.components.RateView
import com.bobodroid.myapplication.components.addFocusCleaner
import com.bobodroid.myapplication.components.shadowCustom
import com.bobodroid.myapplication.models.datamodels.roomDb.Currencies
import com.bobodroid.myapplication.models.datamodels.roomDb.CurrencyType
import com.bobodroid.myapplication.models.datamodels.roomDb.RateDirection
import com.bobodroid.myapplication.models.datamodels.roomDb.RateType
import com.bobodroid.myapplication.models.datamodels.roomDb.emoji
import com.bobodroid.myapplication.models.datamodels.service.UserApi.Rate
import com.bobodroid.myapplication.models.viewmodels.AnalysisViewModel
import com.bobodroid.myapplication.models.viewmodels.FcmAlarmViewModel
import com.bobodroid.myapplication.ui.theme.DialogBackgroundColor
import com.bobodroid.myapplication.ui.theme.HighRateColor
import com.bobodroid.myapplication.ui.theme.LowRateColor
import com.bobodroid.myapplication.ui.theme.WelcomeScreenBackgroundColor
import com.bobodroid.myapplication.ui.theme.primaryColor
import com.bobodroid.myapplication.ui.theme.surfaceColor
import kotlinx.coroutines.launch
import okhttp3.internal.wait
import java.math.BigDecimal
import java.text.NumberFormat
import java.util.Locale
import javax.annotation.meta.When
import kotlin.math.abs
import kotlin.math.absoluteValue
import kotlin.math.roundToInt


@Composable
fun FcmAlarmScreen() {

    val coroutineScope = rememberCoroutineScope()
    val fcmAlarmViewModel: FcmAlarmViewModel = hiltViewModel()
    val targetRateData = fcmAlarmViewModel.targetRateFlow.collectAsState()

    val fcmUiState = fcmAlarmViewModel.alarmUiState.collectAsState()

    var selectedTabIndex by remember { mutableStateOf(0) }
    var currencyExpanded by remember { mutableStateOf(false) }
    var addTargetDialog by remember { mutableStateOf(false) }



    var targetRateState by remember {
        mutableStateOf(RateDirection.HIGH)
    }


    val targetRateMoneyType by fcmAlarmViewModel.selectedCurrency.collectAsState()

    val currency = remember(targetRateMoneyType) {
        Currencies.fromCurrencyType(targetRateMoneyType)
    }

    val rates = when(selectedTabIndex) {
        0 -> when(targetRateMoneyType) {
            CurrencyType.USD -> targetRateData.value.dollarHighRates
            CurrencyType.JPY -> targetRateData.value.yenHighRates
            else -> null
        }
        else -> when(targetRateMoneyType) {
            CurrencyType.USD -> targetRateData.value.dollarLowRates
            CurrencyType.JPY -> targetRateData.value.yenLowRates
            else -> null
        }
    }

    val fcmAlarmScreenSnackBarHostState = remember { SnackbarHostState() }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter)
    {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // 상단 헤더
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.alarm),
                        contentDescription = "",
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text(
                        text = "목표 환율 알람 설정",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                }

                // 통화 선택 드롭다운
                Box {
                    TextButton(
                        onClick = { currencyExpanded = true },
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = primaryColor
                        )
                    ) {
                        Text("${currency.emoji} ${currency.koreanName}(${targetRateMoneyType.code})")
                        Icon(Icons.Filled.ArrowDropDown, null)
                    }

                    DropdownMenu(
                        expanded = currencyExpanded,
                        onDismissRequest = { currencyExpanded = false }
                    ) {
                        CurrencyType.values().forEach { currencyType ->
                            val currencyObj = Currencies.fromCurrencyType(currencyType)
                            DropdownMenuItem(
                                onClick = {
                                    fcmAlarmViewModel.updateSelectedCurrency(currencyType)
                                    currencyExpanded = false
                                },
                                text = {
                                    Text("${currencyType.emoji} ${currencyObj.koreanName}(${currencyType.code})")
                                }
                            )
                        }
                    }
                }
            }

            // 현재 환율 표시
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                // ExchangeRate의 getRateByCode()로 가져오기
                val recentRateValue = fcmUiState.value.recentRate.getRateByCode(targetRateMoneyType.code) ?: "0"

                // RateView에 그대로 전달 (ExchangeRate.fromDocumentSnapshot에서 이미 needsMultiply 처리됨)
                RateView(
                    title = targetRateMoneyType.code,
                    recentRate = recentRateValue,
                    createAt = fcmUiState.value.recentRate.createAt
                )
            }

            // 탭과 리스트
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(vertical = 8.dp),
                elevation = CardDefaults.elevatedCardElevation(4.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                ),
            ) {
                Column {
                    TabRow(
                        selectedTabIndex = selectedTabIndex,
                        modifier = Modifier.fillMaxWidth(),
                        backgroundColor = surfaceColor,
                        indicator = { tabPositions ->
                            Box(
                                Modifier
                                    .tabIndicatorOffset(tabPositions[selectedTabIndex])
                                    .height(4.dp)
                                    .padding(horizontal = 24.dp)
                                    .background(
                                        color = when (selectedTabIndex) {
                                            0 -> Color(0xFFD32F2F)
                                            else -> Color(0xFF1976D2)
                                        },
                                        shape = RoundedCornerShape(topStart = 3.dp, topEnd = 3.dp)
                                    )
                            )
                        }
                    ) {
                        Tab(
                            selected = selectedTabIndex == 0,
                            onClick = { selectedTabIndex = 0 },
                            text = {
                                Text(
                                    "고점 목표 환율",
                                    color = if(selectedTabIndex == 0) Color(0xFFD32F2F) else Color.Gray,
                                    fontWeight = if(selectedTabIndex == 0) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        )
                        Tab(
                            selected = selectedTabIndex == 1,
                            onClick = { selectedTabIndex = 1 },
                            text = {
                                Text(
                                    "저점 목표 환율",
                                    color = if(selectedTabIndex == 1) Color(0xFF1976D2) else Color.Gray,
                                    fontWeight = if(selectedTabIndex == 1) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        )
                    }

                    Log.d(TAG("FcmAlarmScreen", "ListItem"), rates.toString())
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if(rates?.isEmpty() != false) {
                            item {
                                Row {
                                    Text("목표환율이 없습니다. 추가해주세요")
                                }
                            }
                        } else {
                            items(rates) { rate ->
                                RateItem(
                                    rate = rate,
                                    backgroundColor = if(selectedTabIndex == 0) HighRateColor else LowRateColor,
                                    onDelete = { deleteRate ->
                                        val rateType = RateType.from(targetRateMoneyType, targetRateState)
                                        fcmAlarmViewModel.deleteTargetRate(
                                            deleteRate = deleteRate,
                                            type = rateType
                                        )
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // Add FAB
            FloatingActionButton(
                onClick = {
                    // USD, JPY가 아닌 경우 경고
                    if(targetRateMoneyType != CurrencyType.USD && targetRateMoneyType != CurrencyType.JPY) {
                        coroutineScope.launch {
                            fcmAlarmScreenSnackBarHostState.showSnackbar(
                                "현재 USD와 JPY만 지원됩니다.",
                                actionLabel = "닫기",
                                SnackbarDuration.Short
                            )
                        }
                        return@FloatingActionButton
                    }

                    if((rates?.size ?: 0) < 5) {
                        targetRateState = if(selectedTabIndex == 0) RateDirection.HIGH else RateDirection.LOW
                        addTargetDialog = true
                    } else {
                        if (fcmAlarmScreenSnackBarHostState.currentSnackbarData == null) {
                            coroutineScope.launch {
                                fcmAlarmScreenSnackBarHostState.showSnackbar(
                                    "목표환율은 최대 5개까지 생성가능합니다.",
                                    actionLabel = "닫기",
                                    SnackbarDuration.Short
                                )
                            }
                        }
                    }
                },
                modifier = Modifier
                    .align(Alignment.End)
                    .padding(16.dp),
                containerColor = primaryColor
            ) {
                Icon(
                    Icons.Filled.Add,
                    contentDescription = "Add target rate",
                    tint = Color.White
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
        ) {
            SnackbarHost(
                hostState = fcmAlarmScreenSnackBarHostState,
                modifier = Modifier,
                snackbar = { snackBarData ->
                    Card(
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.5.dp, Color.Black),
                        modifier = Modifier
                            .padding(10.dp)
                            .fillMaxWidth(),
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp)
                                .padding(start = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Start
                        ) {
                            Text(
                                text = snackBarData.message,
                                fontSize = 15.sp,
                                lineHeight = 20.sp,
                                fontWeight = FontWeight.Medium
                            )

                            Spacer(modifier = Modifier.weight(1f))

                            Text(
                                modifier = Modifier
                                    .padding(8.dp)
                                    .clickable {
                                        fcmAlarmScreenSnackBarHostState.currentSnackbarData?.dismiss()
                                    },
                                text = "닫기",
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            )
        }
    }

    if(addTargetDialog) {
        val lastRate = rates?.lastOrNull()

        TargetRateDialog(
            onDismissRequest = {
                addTargetDialog = it
            },
            lastRate = lastRate,
            selected = { addTargetRate ->
                val rateType = RateType.from(targetRateMoneyType, targetRateState)
                fcmAlarmViewModel.addTargetRate(
                    addRate = addTargetRate,
                    type = rateType
                )
                addTargetDialog = false
            }
        )
    }
}


@Composable
fun RateItem(
    rate: Rate,
    backgroundColor: Color,
    onDelete: (Rate) -> Unit
) {
    var offsetX by remember { mutableStateOf(0f) }
    val buttonWidth = 100.dp
    val buttonWidthPx = with(LocalDensity.current) { buttonWidth.toPx() }

    val offsetXAnimated by animateFloatAsState(
        targetValue = offsetX.coerceIn(-buttonWidthPx, buttonWidthPx),
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        )
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        elevation = CardDefaults.elevatedCardElevation(5.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxSize()
                .background(Color.White)
        ) {
            // 삭제 버튼 (오른쪽)
            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .fillMaxHeight()
                    .width(buttonWidth)
                    .background(Color.Red)
                    .clickable {
                        onDelete(rate)
                        offsetX = 0f
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = Color.White
                )
            }

            // 메인 컨텐츠
            Box(
                modifier = Modifier
                    .offset { IntOffset(offsetXAnimated.roundToInt(), 0) }
                    .fillMaxWidth()
                    .background(backgroundColor)
                    .pointerInput(Unit) {
                        detectHorizontalDragGestures(
                            onDragEnd = {
                                offsetX = when {
                                    abs(offsetX) < buttonWidthPx / 2 -> 0f
                                    offsetX < 0 -> -buttonWidthPx
                                    else -> 0f
                                }
                            }
                        ) { _, dragAmount ->
                            val newOffset = offsetX + dragAmount
                            offsetX = newOffset.coerceIn(-buttonWidthPx, 0f)
                        }
                    }
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxSize()
                        .background(backgroundColor)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "목표 환율 ${rate.number}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Color.Black
                        )

                        Text(
                            text = "${rate.rate} 원",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1976D2)
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                }
            }
        }
    }
}


enum class TargetRateMoneyType {
    Dollar,
    Yen
}

enum class TargetRateState {
    High,
    Low
}