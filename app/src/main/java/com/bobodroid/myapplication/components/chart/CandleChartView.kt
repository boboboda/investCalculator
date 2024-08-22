package com.bobodroid.myapplication.components.chart

import android.graphics.Color
import android.graphics.Paint
import android.view.LayoutInflater
import androidx.compose.runtime.Composable
import androidx.compose.ui.viewinterop.AndroidView
import com.bobodroid.myapplication.R
import com.bobodroid.myapplication.screens.CandleEntryData
import com.github.mikephil.charting.charts.CandleStickChart
import com.github.mikephil.charting.data.CandleData
import com.github.mikephil.charting.data.CandleDataSet
import com.github.mikephil.charting.data.CandleEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter

@Composable
fun CandleChartView(dataEntries: List<CandleEntryData>, visibleRange: Int) {
    AndroidView(
        factory = { context ->
            val inflater = LayoutInflater.from(context)
            val view = inflater.inflate(R.layout.candlestick_chart_layout, null, false)
            val candleChart = view.findViewById<CandleStickChart>(R.id.candleStickChart)
            setupCandleChart(candleChart, dataEntries.take(visibleRange))
            view
        },
        update = { view ->
            val candleChart = view.findViewById<CandleStickChart>(R.id.candleStickChart)
            setupCandleChart(candleChart, dataEntries.take(visibleRange))
        }
    )
}

fun setupCandleChart(candleChart: CandleStickChart, entries: List<CandleEntryData>) {
    val candleEntries = entries.map { CandleEntry(it.x, it.high, it.low, it.open, it.close) }

    val candleDataSet = CandleDataSet(candleEntries, "환율 데이터").apply {
        shadowColor = Color.DKGRAY
        shadowWidth = 0.7f
        decreasingColor = Color.BLUE // 음봉 (감소) 색상
        decreasingPaintStyle = Paint.Style.FILL
        increasingColor = Color.RED // 양봉 (증가) 색상
        increasingPaintStyle = Paint.Style.FILL
        neutralColor = Color.GRAY
        setDrawValues(false)
    }

    val candleData = CandleData(candleDataSet)
    candleChart.data = candleData

    // Configure the X-axis
    val xAxis = candleChart.xAxis
    xAxis.valueFormatter = IndexAxisValueFormatter((1..entries.size).map { "$it 일" })
    xAxis.setDrawAxisLine(true)
    xAxis.position = com.github.mikephil.charting.components.XAxis.XAxisPosition.BOTTOM
    xAxis.setDrawGridLines(false)

    // Configure the Y-axis
    val yAxisLeft = candleChart.axisLeft
    yAxisLeft.setDrawGridLines(false)

    val yAxisRight = candleChart.axisRight
    yAxisRight.setDrawGridLines(false)
    yAxisRight.isEnabled = false

    candleChart.invalidate()
}