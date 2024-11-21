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
import com.bobodroid.myapplication.components.chart.ExchangeRateChart
import com.bobodroid.myapplication.models.datamodels.roomDb.ExchangeRate
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.CartesianValueFormatter
import com.patrykandpatrick.vico.core.cartesian.data.columnSeries
import com.patrykandpatrick.vico.core.cartesian.data.lineSeries
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
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

    val modelProducer = remember { CartesianChartModelProducer() }
    // 더미 데이터

    val testDummyData = remember {
        listOf(
            // 20초 단위
            ExchangeRate(createAt = "2024-11-21 09:00:00", usd = "1380.50"),
            ExchangeRate(createAt = "2024-11-21 09:00:20", usd = "1385.75"),
            ExchangeRate(createAt = "2024-11-21 09:00:40", usd = "1379.25"),
            ExchangeRate(createAt = "2024-11-21 09:01:00", usd = "1382.00"),
            ExchangeRate(createAt = "2024-11-21 09:01:20", usd = "1388.50"),
            ExchangeRate(createAt = "2024-11-21 09:01:40", usd = "1384.25"),

            // 3분 단위로 전환
            ExchangeRate(createAt = "2024-11-21 09:03:00", usd = "1379.75"),
            ExchangeRate(createAt = "2024-11-21 09:06:00", usd = "1386.00"),
            ExchangeRate(createAt = "2024-11-21 09:09:00", usd = "1390.25"),
            ExchangeRate(createAt = "2024-11-21 09:12:00", usd = "1383.50"),
            ExchangeRate(createAt = "2024-11-21 09:15:00", usd = "1387.75"),

            // 다시 20초 단위
            ExchangeRate(createAt = "2024-11-21 09:15:20", usd = "1381.25"),
            ExchangeRate(createAt = "2024-11-21 09:15:40", usd = "1385.00"),
            ExchangeRate(createAt = "2024-11-21 09:16:00", usd = "1389.50"),
            ExchangeRate(createAt = "2024-11-21 09:16:20", usd = "1384.75"),
            ExchangeRate(createAt = "2024-11-21 09:16:40", usd = "1380.25"),

            // 다시 3분 단위
            ExchangeRate(createAt = "2024-11-21 09:18:00", usd = "1386.50"),
            ExchangeRate(createAt = "2024-11-21 09:21:00", usd = "1391.75"),
            ExchangeRate(createAt = "2024-11-21 09:24:00", usd = "1385.25"),
            ExchangeRate(createAt = "2024-11-21 09:27:00", usd = "1388.00"),
            ExchangeRate(createAt = "2024-11-21 09:30:00", usd = "1382.50")
        )
    }

    val dateList = remember(testDummyData) {
        testDummyData.map { it.createAt }
    }

    // 데이터 프로듀서 생성
    LaunchedEffect(testDummyData) {
        modelProducer.runTransaction {
//            columnSeries {
//                series(  )
//            }

            lineSeries {
                series(
                    testDummyData.map { it.usd?.toFloat()?.toInt() ?: 0 }
                )
            }
        }
    }

    Column {
        // 기간 선택 탭


        // 차트
        ExchangeRateChart(
            modelProducer = modelProducer,
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .padding(16.dp)
        )
    }
}
