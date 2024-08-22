package com.bobodroid.myapplication.screens


import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.bobodroid.myapplication.components.Buttons
import com.bobodroid.myapplication.components.Dialogs.LoadingDialog
import com.bobodroid.myapplication.components.chart.CandleChartView
import com.bobodroid.myapplication.components.chart.CombinedChartView
import com.bobodroid.myapplication.models.viewmodels.AllViewModel
import com.github.mikephil.charting.data.BarEntry
import kotlin.random.Random


@Composable
fun AnalysisScreen(allViewModel: AllViewModel) {

    var loadDialog by remember { mutableStateOf(false) }
    var loadState by remember { mutableStateOf(false) }
    var loadResultText by remember { mutableStateOf("") }


    Column(modifier = Modifier.fillMaxSize()) {

        Buttons(
            onClicked = {
               allViewModel.firebaseRateLoad {
                   loadResultText = it
                   loadState = true
               }
                loadDialog = true
                        },
            color = Color.White,
            fontColor = Color.Black,
            modifier = Modifier) {
            Text(text = "환율 불러오기")
        }

        Buttons(
            onClicked = {
                allViewModel.deleteTotalRates()
            },
            color = Color.White,
            fontColor = Color.Black,
            modifier = Modifier) {
            Text(text = "삭제")
        }
    }

    if(loadDialog) {
        LoadingDialog(onDismissRequest = {
                                         loadDialog = it
        }, loadState = loadState,
            guideText = loadResultText)
    }


}

@Composable
fun ChartView(allViewModel: AllViewModel) {
    var chartType by remember {
        mutableStateOf(1)
    }

    val dataEntries = List(30) {
        val min = (700..800).random().toFloat()
        val max = (min + (10..50).random()).toFloat()
        min to max
    }

    val rateData = allViewModel.totalExchangeRate


    val sampleData = listOf(
        CandleEntryData(1f, 700f, 750f, 800f, 650f, 50f),
        CandleEntryData(2f, 750f, 770f, 820f, 740f, 20f),
        CandleEntryData(3f, 770f, 760f, 790f, 740f, -10f),
        CandleEntryData(4f, 760f, 780f, 800f, 750f, 20f),
        CandleEntryData(5f, 780f, 790f, 810f, 770f, 10f),
        CandleEntryData(6f, 790f, 780f, 820f, 760f, -10f),
        CandleEntryData(7f, 780f, 770f, 800f, 750f, -10f),
        CandleEntryData(8f, 770f, 760f, 790f, 740f, -10f),
        CandleEntryData(9f, 760f, 750f, 780f, 730f, -10f),
        CandleEntryData(10f, 750f, 740f, 770f, 720f, -10f)
        // 더 많은 데이터를 추가할 수 있습니다.
    )


    Column(modifier = Modifier.fillMaxSize()) {

        Row(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { chartType = 1 }) {
                Text("컴바인드그래프")
            }
            Button(onClick = { chartType = 2 }) {
                Text("캔들그래프")
            }

        }

        when(chartType) {
            1-> { CombinedChartView(dataEntries, 30)}
            2-> { CandleChartView(dataEntries = sampleData, visibleRange = 30)}
        }

    }
}


fun generateDataEntries(): List<BarEntry> {
    val entries = mutableListOf<BarEntry>()
    val random = java.util.Random()

    for (i in 1..30) {
        val min = 700f + random.nextInt(50)
        val max = min + random.nextInt(50)
        val range = max - min
        entries.add(BarEntry(i.toFloat(), floatArrayOf(min, range, max)))
    }

    return entries
}

data class CandleEntryData(
    val x: Float,
    val open: Float,
    val close: Float,
    val high: Float,
    val low: Float,
    val change: Float // 전날 대비 변화율
)