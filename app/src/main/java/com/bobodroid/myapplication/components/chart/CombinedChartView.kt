package com.bobodroid.myapplication.components.chart

import android.view.LayoutInflater
import androidx.compose.runtime.Composable
import androidx.compose.ui.viewinterop.AndroidView
import com.bobodroid.myapplication.R
import com.github.mikephil.charting.charts.CombinedChart
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import android.graphics.Color.rgb
import android.view.MotionEvent
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.listener.ChartTouchListener
import com.github.mikephil.charting.listener.OnChartGestureListener

@Composable
fun CombinedChartView(dataEntries: List<Pair<Float, Float>>, visibleRange: Int) {
    AndroidView(
        factory = { context ->
            val inflater = LayoutInflater.from(context)
            val view = inflater.inflate(R.layout.combined_chart_layout, null, false)
            val combinedChart = view.findViewById<CombinedChart>(R.id.combinedChart)
            setupCombinedChart(combinedChart, dataEntries.take(visibleRange))
            view
        },
        update = { view ->
            val combinedChart = view.findViewById<CombinedChart>(R.id.combinedChart)
            setupCombinedChart(combinedChart, dataEntries.take(visibleRange))
        }
    )
}

fun setupCombinedChart(combinedChart: CombinedChart, entries: List<Pair<Float, Float>>) {
    // Create Bar Data
    val minEntries = entries.mapIndexed { index, pair ->
        BarEntry(index.toFloat(), pair.first)
    }
    val maxEntries = entries.mapIndexed { index, pair ->
        BarEntry(index.toFloat(), pair.second)
    }

    val minDataSet = BarDataSet(minEntries, "최소 환율가").apply {
        color = rgb(202, 230, 178)
        this.valueTextSize = 12f
    }
    val maxDataSet = BarDataSet(maxEntries, "최대 환율가").apply {
        color = rgb(160, 222, 255)
        this.valueTextSize = 12f
    }

    val barData = BarData(maxDataSet, minDataSet)
    barData.barWidth = 0.8f

    // Create Line Data
    val minLineEntries = entries.mapIndexed { index, pair ->
        Entry(index.toFloat(), pair.first)
    }
    val maxLineEntries = entries.mapIndexed { index, pair ->
        Entry(index.toFloat(), pair.second)
    }

    val minLineDataSet = LineDataSet(minLineEntries, "").apply {
        color = rgb(255, 127, 62)
        valueTextSize = 12f
        lineWidth = 2f  // Set line width
        this.setDrawIcons(false)
        this.setDrawValues(false)
        this.setForm(Legend.LegendForm.NONE)
    }
    val maxLineDataSet = LineDataSet(maxLineEntries, "").apply {
        color = rgb(228, 155, 255)
        valueTextSize = 12f
        lineWidth = 2f  // Set line width
        this.setDrawIcons(false)
        this.setDrawValues(false)
        this.setForm(Legend.LegendForm.NONE)
    }

    val lineData = LineData(minLineDataSet, maxLineDataSet)

    // Combine Data
    val combinedData = CombinedData()
    combinedData.setData(barData)
    combinedData.setData(lineData)

    combinedChart.data = combinedData
    combinedChart.description.isEnabled = false

    // Configure the X-axis
    val xAxis = combinedChart.xAxis
    xAxis.valueFormatter = IndexAxisValueFormatter((0..entries.size).map { "${it + 1} 일" })
    xAxis.setDrawAxisLine(true)
    xAxis.position = com.github.mikephil.charting.components.XAxis.XAxisPosition.BOTTOM
    xAxis.labelCount = entries.size
    xAxis.granularity = 1f
    xAxis.setDrawGridLines(false)
    xAxis.axisMinimum = barData.xMin - 0.5f
    xAxis.axisMaximum = barData.xMax + 0.5f

    // Configure the Y-axis
    val yAxisLeft = combinedChart.axisLeft
    val yAxisRight = combinedChart.axisRight
    yAxisLeft.axisMinimum = 650f
    yAxisLeft.axisMaximum = 1000f  // 최대값을 약간 늘려서 여유를 둠
    yAxisLeft.granularity = 50f  // Y축 레이블 간격을 100으로 설정
    yAxisLeft.setLabelCount(10, true)

    // Configure the legend
    val legend = combinedChart.legend
    legend.verticalAlignment = com.github.mikephil.charting.components.Legend.LegendVerticalAlignment.TOP
    legend.horizontalAlignment = com.github.mikephil.charting.components.Legend.LegendHorizontalAlignment.LEFT
    legend.orientation = com.github.mikephil.charting.components.Legend.LegendOrientation.HORIZONTAL
    legend.setDrawInside(false)
    legend.yOffset = 10f

//    combinedChart.setVisibleXRangeMaximum(10f) // 기본적으로 10개까지만 보이게 설정
//    combinedChart.setVisibleXRangeMinimum(10f) // 기본적으로 10개까지만 보이게 설정

    // Enable dragging, scaling, and pinch zoom
    combinedChart.setDragEnabled(true)
    combinedChart.setScaleEnabled(true)
    combinedChart.setPinchZoom(true)
    combinedChart.setScaleXEnabled(true)
    combinedChart.setScaleYEnabled(true)

    combinedChart.onChartGestureListener = object : OnChartGestureListener {
        override fun onChartGestureStart(me: MotionEvent?, lastPerformedGesture: ChartTouchListener.ChartGesture?) {}
        override fun onChartGestureEnd(me: MotionEvent?, lastPerformedGesture: ChartTouchListener.ChartGesture?) {}
        override fun onChartLongPressed(me: MotionEvent?) {}
        override fun onChartDoubleTapped(me: MotionEvent?) {}
        override fun onChartSingleTapped(me: MotionEvent?) {}
        override fun onChartFling(
            me1: MotionEvent?,
            me2: MotionEvent?,
            velocityX: Float,
            velocityY: Float
        ) {}
        override fun onChartScale(me: MotionEvent?, scaleX: Float, scaleY: Float) {
            updateXAxisLabels(combinedChart, entries)
        }
        override fun onChartTranslate(me: MotionEvent?, dX: Float, dY: Float) {}
    }

    // Refresh the chart
    combinedChart.invalidate()
}

fun updateXAxisLabels(combinedChart: CombinedChart, entries: List<Pair<Float, Float>>) {
    val xAxis = combinedChart.xAxis
    val scale = combinedChart.scaleX
    xAxis.valueFormatter = when {
        scale > 5 -> IndexAxisValueFormatter((0..entries.size).map { "${it + 1} 일" })
        scale > 2 -> IndexAxisValueFormatter((0..entries.size / 7).map { "${it + 1} 주" })
        scale > 1 -> IndexAxisValueFormatter((0..entries.size / 30).map { "${it + 1} 월" })
        else -> IndexAxisValueFormatter((0..entries.size / 365).map { "${it + 1} 년" })
    }
    combinedChart.invalidate()
}