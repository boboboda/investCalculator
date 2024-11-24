package com.bobodroid.myapplication.components.chart

import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import com.bobodroid.myapplication.MainActivity.Companion.TAG
import com.bobodroid.myapplication.models.viewmodels.RateRange
import com.bobodroid.myapplication.models.viewmodels.RateRangeCurrency
import kotlin.math.roundToInt

@Composable
fun ExchangeRateChart(
    data: List<RateRangeCurrency>,
) {
    var selectedPoint by remember { mutableStateOf<RateRangeCurrency?>(null) }
    var chartSize by remember { mutableStateOf(Size.Zero) }
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    val scrollState = rememberScrollState()

    val xAxisScale = 0.8f



    LaunchedEffect(data, xAxisScale) {
        // 스크롤을 최대값으로 설정
        selectedPoint = null
        scrollState.animateScrollTo(scrollState.maxValue)

        Log.d(TAG("ExchangeRateChart",""), "data: $data")
    }

    val leftMargin = 10f
    val rightMargin = 10f
    val topMargin = 10f
    val bottomMargin = 10f

    if (data.isEmpty()) {
        Log.d(TAG("ExchangeRateChart",""), "data is empty or only has one point")
        return
    }


    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(
                start = leftMargin.dp,
                end = rightMargin.dp,
                top = topMargin.dp,
                bottom = bottomMargin.dp
            )
            .clipToBounds()  // 부모 영역을 벗어나는 내용을 클리핑
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .horizontalScroll(scrollState)
                .padding(end = 30.dp)
        ) {
            Canvas(
                modifier = Modifier
                    .width(screenWidth * xAxisScale)
                    .fillMaxHeight(1f)
                    .onSizeChanged { size ->
                        chartSize = Size(size.width.toFloat(), size.height.toFloat())
                    }
                    .pointerInput(data, xAxisScale) {
                        detectDragGestures(
                            onDragStart = { offset ->
                                // 드래그 시작할 때
                                if (chartSize != Size.Zero) {
                                    selectedPoint = findClosestPoint(offset, data, chartSize, xAxisScale)
                                }
                            },
                            onDrag = { change, _ ->
                                // 드래그 중
                                if (chartSize != Size.Zero) {
                                    selectedPoint = findClosestPoint(change.position, data, chartSize, xAxisScale)
                                }
                            }
                        )
                    }
                    .pointerInput(data, xAxisScale) {  // Unit 대신 data와 xAxisScale 추가
                        detectTapGestures { offset ->
                            if (chartSize != Size.Zero) {
                                selectedPoint = findClosestPoint(offset, data, chartSize, xAxisScale)
                            }
                        }
                    }
            ) {
                val chartHeight = size.height

                val maxRate = data.maxOf { it.rate }
                val minRate = data.minOf { it.rate }
                val rateRange = maxRate - minRate
                val padding = if (rateRange == 0f) {
                    // 모든 값이 같을 때 적절한 패딩 설정
                    maxRate * 0.1f  // 최대값의 10%를 패딩으로 사용
                } else {
                    rateRange * 1f
                }

                fun calculatePointPosition(index: Int, rate: Float): Offset {
                    val normalizedRate = if (rateRange == 0f) {
                        0.5f  // 모든 값이 같을 때 중앙에 위치
                    } else {
                        (rate - minRate + padding) / (rateRange + 2 * padding)
                    }
                    val xOffset = 100f
                    return Offset(
                        x = xOffset + (index.toFloat() / (data.size - 1)) * ((size.width * xAxisScale) - xOffset),
                        y = chartHeight * (1f - normalizedRate)
                    )
                }

                // 데이터 라인과 포인트 그리기
                val path = Path()
                val points = data.mapIndexed { index, item ->
                    calculatePointPosition(index, item.rate)
                }

                points.forEachIndexed { index, point ->
                    if (index == 0) path.moveTo(point.x, point.y)
                    else path.lineTo(point.x, point.y)
                }

                drawPath(
                    path = path,
                    color = Color.Red,
                    style = Stroke(width = 2f)
                )

                drawCircle(
                    color = Color.Red,
                    radius = 10f,
                    center = points.last()
                )

                val maxPoint = data.withIndex().maxByOrNull { it.value.rate }!!
                val minPoint = data.withIndex().minByOrNull { it.value.rate }!!

                // 최대/최소값 포인트의 위치 계산
                val maxPointPosition = calculatePointPosition(maxPoint.index, maxPoint.value.rate)
                val minPointPosition = calculatePointPosition(minPoint.index, minPoint.value.rate)

                // 최대값 텍스트 그리기
                val maxMarkerPaint = android.graphics.Paint().apply {
                    color = android.graphics.Color.RED
                    textSize = 32f
                    textAlign = android.graphics.Paint.Align.CENTER
                }

                drawContext.canvas.nativeCanvas.apply {
                    // 최대값
                    drawText(
                        "최고 ${maxPoint.value.rate}" ,
                        maxPointPosition.x,
                        maxPointPosition.y - 30f,  // 포인트 위에 표시
                        maxMarkerPaint
                    )

                    // 최소값
                    drawText(
                        "최소 ${minPoint.value.rate}",
                        minPointPosition.x,
                        minPointPosition.y + 50f,  // 포인트 위에 표시
                        maxMarkerPaint
                    )
                }


                // Draw selected point marker
                selectedPoint?.let { selected ->
                    val index = data.indexOf(selected)
                    if (index != -1 && index < points.size) {
                        val point = calculatePointPosition(index, selected.rate)

                        drawCircle(
                            color = Color.Red,
                            radius = 8f,
                            center = point
                        )

                        // 마커 박스 그리기 (두 줄 텍스트용 높이 증가)
                        val dateText = selected.createAt
                        val rateText = selected.rate.toString()
                        val markerPaint = android.graphics.Paint().apply {
                            color = android.graphics.Color.BLACK
                            textSize = 32f
                            textAlign = android.graphics.Paint.Align.CENTER
                        }

                        val textY = 40f
                        val lineHeight = 35f  // 줄 간격
                        val maxTextWidth = maxOf(
                            markerPaint.measureText(dateText),
                            markerPaint.measureText(rateText)
                        )
                        val boxMargin = 20f

                        val boxRect = Rect(
                            point.x - maxTextWidth/2 - boxMargin,
                            textY - 40f,  // 박스 높이 증가
                            point.x + maxTextWidth/2 + boxMargin,
                            textY + lineHeight + 10f  // 박스 높이 증가
                        )

                        drawRect(
                            color = Color.White,
                            topLeft = Offset(boxRect.left, boxRect.top),
                            size = Size(boxRect.width, boxRect.height)
                        )

                        drawRect(
                            color = Color.Gray,
                            topLeft = Offset(boxRect.left, boxRect.top),
                            size = Size(boxRect.width, boxRect.height),
                            style = Stroke(width = 1f)
                        )

                        drawLine(
                            color = Color.Gray,
                            start = Offset(point.x, boxRect.bottom),
                            end = Offset(point.x, point.y),
                            strokeWidth = 1f,
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                        )

                        drawContext.canvas.nativeCanvas.apply {
                            // 날짜 텍스트
                            drawText(
                                dateText,
                                point.x,
                                textY,
                                markerPaint
                            )
                            // 환율 텍스트
                            drawText(
                                rateText,
                                point.x,
                                textY + lineHeight,  // 두 번째 줄
                                markerPaint
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun findClosestPoint(
    tapPosition: Offset,
    data: List<RateRangeCurrency>,
    size: Size,
    xAxisScale: Float
): RateRangeCurrency {
    var minDistance = Float.MAX_VALUE
    var closestPoint = data.first()
    val xOffset = 100f  // 시작 지점 오프셋

    data.forEachIndexed { index, item ->
        val pointPosition = Offset(
            x = xOffset + (index.toFloat() / (data.size - 1)) * ((size.width * xAxisScale) - xOffset),  // xAxisScale 적용
            y = 0f
        )

        val distance = kotlin.math.abs(tapPosition.x - pointPosition.x)
        if (distance < minDistance) {
            minDistance = distance
            closestPoint = item
        }
    }

    return closestPoint
}