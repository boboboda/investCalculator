package com.bobodroid.myapplication.components.chart

import android.text.Layout
import android.text.TextUtils
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import com.bobodroid.myapplication.models.datamodels.service.exchangeRateApi.ExchangeRates
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.VicoScrollState
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberBottom
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberStart
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberColumnCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLine
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoScrollState
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoZoomState
import com.patrykandpatrick.vico.compose.common.component.rememberLineComponent
import com.patrykandpatrick.vico.compose.common.component.shapeComponent
import com.patrykandpatrick.vico.compose.common.dimensions
import com.patrykandpatrick.vico.compose.common.fill
import com.patrykandpatrick.vico.core.cartesian.Scroll
import com.patrykandpatrick.vico.core.cartesian.axis.BaseAxis
import com.patrykandpatrick.vico.core.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.core.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.CartesianLayerRangeProvider
import com.patrykandpatrick.vico.core.cartesian.data.CartesianValueFormatter
import com.patrykandpatrick.vico.core.cartesian.decoration.HorizontalLine
import com.patrykandpatrick.vico.core.cartesian.layer.ColumnCartesianLayer
import com.patrykandpatrick.vico.core.cartesian.layer.LineCartesianLayer
import com.patrykandpatrick.vico.core.common.Dimensions
import com.patrykandpatrick.vico.core.common.HorizontalPosition
import com.patrykandpatrick.vico.core.common.component.LineComponent
import com.patrykandpatrick.vico.core.common.component.ShapeComponent
import com.patrykandpatrick.vico.core.common.component.TextComponent
import com.patrykandpatrick.vico.core.common.data.CartesianLayerDrawingModelInterpolator
import com.patrykandpatrick.vico.core.common.shape.CorneredShape
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale


@Composable
fun ExchangeRateChart(
    modelProducer: CartesianChartModelProducer,
    modifier: Modifier
) {

    CartesianChartHost(
        chart = rememberCartesianChart(
//            rememberColumnCartesianLayer(
//                ColumnCartesianLayer.ColumnProvider.series(
//                    rememberLineComponent(
//                        color = Color(0xffff5500),
//                        thickness = 16.dp,
//                        shape = CorneredShape.rounded(allPercent = 40)
//                    )
//                ),
//                rangeProvider = remember {
//                    CartesianLayerRangeProvider.fixed(
//                        maxY = 1400.0,
//                        minY = 1200.0)
//                },
//                columnCollectionSpacing = 5.dp,
//
//            ),
            rememberLineCartesianLayer(
                LineCartesianLayer.LineProvider.series(
                    LineCartesianLayer.rememberLine(
                        fill = remember { LineCartesianLayer.LineFill.single(fill(Color(0xfffdc8c4))) },
                        pointConnector = remember { LineCartesianLayer.PointConnector.cubic(curvature = 0f) },
                    )
                ),
                rangeProvider = remember {
                    CartesianLayerRangeProvider.fixed(
                        maxY = 1400.0,
                        minY = 1370.0)
                },
                pointSpacing = 1.dp,
                drawingModelInterpolator = remember { CartesianLayerDrawingModelInterpolator.default() }
            ),
            startAxis = VerticalAxis.rememberStart(
                line = rememberLineComponent(
                    color = Color.Black,
                    thickness = 1.dp
                ),
                label = rememberTextComponent(Color.Black),
                itemPlacer = remember {
                    VerticalAxis. ItemPlacer. step(
                        step = { 5.0 },
                        shiftTopLines = true
                    )
                },
                tickLength = 10.dp,
                valueFormatter = remember {
                    CartesianValueFormatter.decimal()
                }

            ),
            marker = rememberMarker(),
//            decorations = listOf(rememberComposeHorizontalLine()),
        ),
        modelProducer = modelProducer,
        modifier = modifier,
        scrollState = rememberVicoScrollState(
            initialScroll = Scroll.Absolute.End
        ),
//        zoomState = rememberVicoZoomState(),

    )
}

// 텍스트 컴포넌트 헬퍼 함수
@Composable
private fun rememberTextComponent(color: Color) = remember {
    TextComponent(
        color = color.toArgb()
    )
}





@Composable
private fun rememberComposeHorizontalLine(): HorizontalLine {
    val color = Color.Black
    val line = rememberLineComponent(color, HORIZONTAL_LINE_THICKNESS_DP.dp)
    val labelComponent =
        com.patrykandpatrick.vico.compose.common.component.rememberTextComponent(
            margins = dimensions(HORIZONTAL_LINE_LABEL_MARGIN_DP.dp),
            padding =
            dimensions(
                HORIZONTAL_LINE_LABEL_HORIZONTAL_PADDING_DP.dp,
                HORIZONTAL_LINE_LABEL_VERTICAL_PADDING_DP.dp,
            ),
            background = shapeComponent(Color.White, CorneredShape.Pill),
        )
    val horizontalLabelPosition = HorizontalPosition.End
    return remember { HorizontalLine(
        y = { HORIZONTAL_LINE_Y },
        line,
        labelComponent,
        horizontalLabelPosition =  horizontalLabelPosition
        )


    }
}

private const val HORIZONTAL_LINE_Y = 1347.39
private val HORIZONTAL_LINE_COLOR = Color.Black.toArgb()
private const val HORIZONTAL_LINE_THICKNESS_DP = 2f
private const val HORIZONTAL_LINE_LABEL_HORIZONTAL_PADDING_DP = 8f
private const val HORIZONTAL_LINE_LABEL_VERTICAL_PADDING_DP = 2f
private const val HORIZONTAL_LINE_LABEL_MARGIN_DP = 4f