package com.bobodroid.myapplication.screens


import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.bobodroid.myapplication.components.Buttons
import com.bobodroid.myapplication.components.Dialogs.LoadingDialog
import com.bobodroid.myapplication.models.viewmodels.AllViewModel
import com.bobodroid.myapplication.models.viewmodels.AnalysisViewModel
import androidx.hilt.navigation.compose.hiltViewModel
import com.bobodroid.myapplication.components.chart.ChartPeriod
import com.bobodroid.myapplication.components.chart.ExchangeRateChart
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.columnSeries
import kotlin.random.Random

@Composable
fun AnalysisScreen() {


    Column {
        ExchangeRateScreen()
    }





}


// 사용 예시
@Composable
fun ExchangeRateScreen() {
    var selectedPeriod by remember { mutableStateOf(ChartPeriod.DAY) }

    val modelProducer = remember { CartesianChartModelProducer() }
    // 더미 데이터
    val dummyMinuteData = remember {
        List(30) { index ->
            ExchangeRate(
                value = 1300f + Random.nextFloat() * 50,
                timestamp = System.currentTimeMillis() + (index * 60 * 1000)
            )
        }
    }

    val dummyDayData = remember {
        List(30) { index ->
            ExchangeRate(
                value = 1300f + Random.nextFloat() * 50,
                timestamp = System.currentTimeMillis() + (index * 24 * 60 * 60 * 1000)
            )
        }
    }

    val dummyMonthData = remember {
        List(12) { index ->
            ExchangeRate(
                value = 1300f + Random.nextFloat() * 50,
                timestamp = System.currentTimeMillis() + (index * 30L * 24 * 60 * 60 * 1000)
            )
        }
    }

    // 데이터 프로듀서 생성
    LaunchedEffect(selectedPeriod) {
        modelProducer.runTransaction {
            columnSeries {
                series(
                    when(selectedPeriod) {
                        ChartPeriod.MINUTE -> dummyMinuteData
                        ChartPeriod.DAY -> dummyDayData
                        ChartPeriod.MONTH -> dummyMonthData
                    }.map { it.value }
                )
            }
        }
    }

    Column {
        // 기간 선택 탭
        TabRow(selectedTabIndex = selectedPeriod.ordinal) {
            ChartPeriod.values().forEach { period ->
                Tab(
                    selected = selectedPeriod == period,
                    onClick = { selectedPeriod = period }
                ) {
                    Text(period.displayName)
                }
            }
        }

        // 차트
        ExchangeRateChart(
            modelProducer = modelProducer,
            chartPeriod = selectedPeriod,
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .padding(16.dp)
        )
    }
}

// 더미 데이터를 위한 데이터 클래스
data class ExchangeRate(
    val value: Float,
    val timestamp: Long
)
