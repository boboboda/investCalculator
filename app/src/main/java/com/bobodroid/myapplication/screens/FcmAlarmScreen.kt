package com.bobodroid.myapplication.screens

import android.util.Log
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.with
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Tab
import androidx.compose.material.TabRow
import androidx.compose.material.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.sharp.Delete
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.bobodroid.myapplication.MainActivity.Companion.TAG
import com.bobodroid.myapplication.R
import com.bobodroid.myapplication.components.AutoSizeText
import com.bobodroid.myapplication.components.Dialogs.GuideDialog
import com.bobodroid.myapplication.components.Dialogs.TextFieldDialog
import com.bobodroid.myapplication.components.RateView
import com.bobodroid.myapplication.components.shadowCustom
import com.bobodroid.myapplication.models.datamodels.service.UserApi.Rate
import com.bobodroid.myapplication.models.viewmodels.AllViewModel
import com.bobodroid.myapplication.models.viewmodels.AnalysisViewModel
import com.bobodroid.myapplication.models.viewmodels.FcmAlarmViewModel
import com.bobodroid.myapplication.ui.theme.HighRateColor
import com.bobodroid.myapplication.ui.theme.LowRateColor
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.absoluteValue


@Composable
fun AlarmScreen(allViewModel: AllViewModel) {
    val fcmAlarmViewModel: FcmAlarmViewModel = hiltViewModel()
    val recentRate = allViewModel.recentExchangeRateFlow.collectAsState()
    val targetRateData = allViewModel.targetRateFlow.collectAsState()

    var selectedTabIndex by remember { mutableStateOf(0) }
    var currencyExpanded by remember { mutableStateOf(false) }
    var addTargetDialog by remember { mutableStateOf(false) }

    var targetRateMoneyType by remember {
        mutableStateOf(TargetRateMoneyType.Dollar)
    }

    var targetRateState by remember {
        mutableStateOf(TargetRateState.High)
    }

    val primaryColor = Color(0xFF2196F3)
    val surfaceColor = Color(0xFFF5F5F5)
    val highRateColor = Color(0xFFE3F2FD)
    val lowRateColor = Color(0xFFFFEBEE)

    // 디버그용 로그
    LaunchedEffect(targetRateData.value) {
        println("Target Rate Data: ${targetRateData.value}")
    }


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
                    text = "목표 환율 설정",
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
                    Text(
                        when(targetRateMoneyType) {
                            TargetRateMoneyType.Dollar -> "달러(USD)"
                            TargetRateMoneyType.Yen -> "엔화(JPY)"
                        }
                    )
                    Icon(Icons.Filled.ArrowDropDown, null)
                }

                DropdownMenu(
                    expanded = currencyExpanded,
                    onDismissRequest = { currencyExpanded = false }
                ) {
                    DropdownMenuItem(
                        onClick = {
                            targetRateMoneyType = TargetRateMoneyType.Dollar
                            currencyExpanded = false
                        },
                        text = { Text("달러(USD)") }
                    )
                    DropdownMenuItem(
                        onClick = {
                            targetRateMoneyType = TargetRateMoneyType.Yen
                            currencyExpanded = false
                        },
                        text = { Text("엔화(JPY)") }
                    )
                }
            }
        }

        // 현재 환율 표시
        RateView(
            title = "USD",
            recentRate = "${recentRate.value.usd}",
            subTitle = "JPY",
            subRecentRate = "${BigDecimal(recentRate.value.jpy).times(BigDecimal("100"))}",
            createAt = "${recentRate.value.createAt}"
        )

        // 탭과 리스트
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(vertical = 8.dp),
            elevation = 4.dp,
            backgroundColor = Color.White
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
                                    color = when(selectedTabIndex) {
                                        0 -> Color(0xFF1976D2)
                                        else -> Color(0xFFD32F2F)
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
                                color = if(selectedTabIndex == 0) Color(0xFF1976D2) else Color.Gray,
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
                                color = if(selectedTabIndex == 1) Color(0xFFD32F2F) else Color.Gray,
                                fontWeight = if(selectedTabIndex == 1) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    )
                }

                val rates = when(selectedTabIndex) {
                    0 -> when(targetRateMoneyType) {
                        TargetRateMoneyType.Dollar -> targetRateData.value.dollarHighRates
                        TargetRateMoneyType.Yen -> targetRateData.value.yenHighRates
                    }
                    else -> when(targetRateMoneyType) {
                        TargetRateMoneyType.Dollar -> targetRateData.value.dollarLowRates
                        TargetRateMoneyType.Yen -> targetRateData.value.yenLowRates
                    }
                }


                Log.d(TAG("FcmAlarmScreen", "ListItem"), rates.toString())
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if(rates == null) {
                        item() {
                            Row {
                                Text("데이터가 없다.")
                            }
                        }
                    } else {
                        items(rates) { rate ->
                            RateItem(
                                rate = rate,
                                targetRateMoneyType = targetRateMoneyType,
                                backgroundColor = if(selectedTabIndex == 0) highRateColor else lowRateColor
                            )
                        }
                    }

                }
            }
        }

        // Add FAB
        FloatingActionButton(
            onClick = {
                targetRateState = if(selectedTabIndex == 0) TargetRateState.High else TargetRateState.Low
                addTargetDialog = true
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

    if(addTargetDialog) {
        TextFieldDialog(
            onDismissRequest = { addTargetDialog = it },
            keyboardType = KeyboardType.Number,
            onClickedLabel = "확인",
            placeholder = "목표환율을 입력해주세요",
            onClicked = { rate ->
                // 목표환율 추가 로직
                addTargetDialog = false
            },
            closeButtonLabel = "닫기"
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 15.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if(targetRateState == TargetRateState.High) "고점 목표 환율" else "저점 목표 환율",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun RateItem(
    rate: Rate,
    targetRateMoneyType: TargetRateMoneyType,
    backgroundColor: Color
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        backgroundColor = backgroundColor,
        elevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "목표 ${
                        when(targetRateMoneyType) {
                            TargetRateMoneyType.Dollar -> "달러"
                            TargetRateMoneyType.Yen -> "엔화"
                        }
                    } #${rate.number}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color.Black
                )

                Text(
                    text = when(targetRateMoneyType) {
                        TargetRateMoneyType.Dollar -> "${rate.rate} USD"
                        TargetRateMoneyType.Yen -> "${rate.rate} JPY"
                    },
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1976D2)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Text(
                    text = when(targetRateMoneyType) {
                        TargetRateMoneyType.Dollar -> "₩${NumberFormat.getNumberInstance(Locale.US).format(rate.rate * 1300)}"
                        TargetRateMoneyType.Yen -> "₩${NumberFormat.getNumberInstance(Locale.US).format(rate.rate * 8.5)}"
                    },
                    color = Color.Gray,
                    fontSize = 14.sp
                )
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NumberGrid(number: String) {
    Card(
        colors = CardDefaults.cardColors(Color.White),
        elevation = CardDefaults.cardElevation(2.dp),
    ) {

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            AutoSizeText(
                value = number,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .padding(vertical = 20.dp)
                    .padding(horizontal = 1.dp),
                maxLines = 1,
                minFontSize = 8.sp,
                color = Color.Black
            )
        }

    }
}


