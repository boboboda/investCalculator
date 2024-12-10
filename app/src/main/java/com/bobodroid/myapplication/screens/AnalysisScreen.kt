package com.bobodroid.myapplication.screens


import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import com.bobodroid.myapplication.R
import com.bobodroid.myapplication.components.Buttons
import com.bobodroid.myapplication.components.chart.ExchangeRateChart
import com.bobodroid.myapplication.components.shadowCustom
import com.bobodroid.myapplication.models.datamodels.roomDb.CurrencyType
import com.bobodroid.myapplication.models.datamodels.service.exchangeRateApi.ExchangeRates
import com.bobodroid.myapplication.models.viewmodels.AnalysisViewModel
import com.bobodroid.myapplication.models.viewmodels.FcmAlarmViewModel
import com.bobodroid.myapplication.models.viewmodels.RateRange
import com.bobodroid.myapplication.models.viewmodels.RateRangeCurrency
import com.bobodroid.myapplication.routes.MainRoute
import com.bobodroid.myapplication.ui.theme.primaryColor
import com.github.mikephil.charting.data.BarEntry
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.random.Random
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.bobodroid.myapplication.MainActivity.Companion.TAG


@Composable
fun AnalysisScreen(
    analysisViewModel: AnalysisViewModel
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "analysis_main"
    ) {
        composable("analysis_main") {
            AnalysisMainContent(
                viewModel = analysisViewModel,
                onNavigateToChart = {
                    navController.navigate("analysis_chart")
                }
            )
        }

        composable("analysis_chart") {
            ChartScreen(
                analysisViewModel = analysisViewModel,
                onBackPress = {
                    navController.navigateUp()
                }
            )
        }
    }
}


@Composable
fun AnalysisMainContent(
    viewModel: AnalysisViewModel,
    onNavigateToChart: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 15.dp)
        ) {
            Buttons(
                onClicked = {

                    onNavigateToChart()
                },
                color = Color.White,
                fontColor = Color.Black,
                modifier = Modifier
                    .height(70.dp)
                    .fillMaxWidth()
                    .padding(top = 10.dp)
                    .shadowCustom(
                        color = Color.LightGray,
                        offsetX = 5.dp,
                        offsetY = 5.dp,
                        blurRadius = 10.dp
                    ),
            ) {
                Text(
                    text = "그래프 보기",
                    fontSize = 15.sp,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(0.dp))
            }
        }

    }
}


@Composable
fun ChartScreen(
    analysisViewModel: AnalysisViewModel,
    onBackPress: () -> Unit
) {

    val selectedTabIndex by analysisViewModel.selectedTabIndex.collectAsState()
    val rates by analysisViewModel.selectedRates.collectAsState()

    Log.d(TAG("ChartScreen", "rates"), rates.toString())

    var currencyExpanded by remember { mutableStateOf(false) }

    var targetRateMoneyType by remember {
        mutableStateOf(CurrencyType.USD)
    }

    val rangeRateMapCurrencyType = rates.map {
        when (targetRateMoneyType) {
            CurrencyType.USD -> RateRangeCurrency(it.usd.toFloat(), it.createAt)
            CurrencyType.JPY -> RateRangeCurrency(it.jpy.toFloat(), it.createAt)
        }
    }


    Log.d(TAG("ChartScreen", "rangeRateMapCurrencyType"), rangeRateMapCurrencyType.toString())

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {

        Row(modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(top = 20.dp)) {
            Column(
                modifier = Modifier.weight(0.5f)
            ) {
                Text(text = "실시간 환율", fontSize = 12.sp, color = Color.Gray)
                Text(text ="1,300원", fontSize = 24.sp, fontWeight = FontWeight.Bold)

                Spacer(modifier = Modifier.height(10.dp))
                Row(modifier = Modifier.fillMaxWidth()) {
                    Text(text = "전일대비", fontSize = 12.sp, color = Color.Gray)
                    Text(text = "▲ 5.75", fontSize = 12.sp, color = Color.Red)

                }
            }
            Column(
                modifier = Modifier.weight(0.5f),
                horizontalAlignment = Alignment.End
            ) {
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
                                CurrencyType.USD -> "달러(USD)"
                                CurrencyType.JPY -> "엔화(JPY)"
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
                                targetRateMoneyType = CurrencyType.USD
                                currencyExpanded = false
                            },
                            text = { Text("달러(USD)") }
                        )
                        DropdownMenuItem(
                            onClick = {
                                targetRateMoneyType = CurrencyType.JPY
                                currencyExpanded = false
                            },
                            text = { Text("엔화(JPY)") }
                        )
                    }
                }
            }
        }


        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
//                .background(Color.White)  // 배경색 추가
        ) {

            ExchangeRateChart(
                data = rangeRateMapCurrencyType,
            )



        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp)
        ) {
            BackgroundEmphasisTabRow(
                selectedTabIndex = selectedTabIndex,
                onTabSelected = { analysisViewModel.onTabSelected(it) }
            )
        }
    }
}


@Composable
fun BackgroundEmphasisTabRow(
    selectedTabIndex: Int,
    onTabSelected: (Int) -> Unit
) {
    val tabTitles = listOf("1일", "1주일", "3달", "1년")

    TabRow(
        selectedTabIndex = selectedTabIndex,
        containerColor = Color.Transparent,
        modifier = Modifier
            .background(color = Color.LightGray, shape = RoundedCornerShape(8.dp))
            .height(50.dp),
        indicator = { },  // 인디케이터 제거
        divider = { }     // 디바이더 제거
    ) {
        tabTitles.forEachIndexed { index, title ->
            Tab(
                selected = selectedTabIndex == index,
                onClick = { onTabSelected(index) },
                modifier = Modifier
                    .height(50.dp)
                    .padding(5.dp)
                    .background(
                        color = if (selectedTabIndex == index) {
                            Color.White
                        } else {
                            Color.Transparent
                        },
                        shape = RoundedCornerShape(8.dp)
                    ),
                text = {
                    Text(
                        text = title,
                        color = if (selectedTabIndex == index) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        }
                    )
                }
            )
        }
    }
}