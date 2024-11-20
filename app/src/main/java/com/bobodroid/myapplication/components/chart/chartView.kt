package com.bobodroid.myapplication.components.chart

import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import com.bobodroid.myapplication.models.datamodels.service.exchangeRateApi.ExchangeRates
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberBottom
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberStart
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberColumnCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLine
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoZoomState
import com.patrykandpatrick.vico.compose.common.component.rememberLineComponent
import com.patrykandpatrick.vico.compose.common.fill
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
import com.patrykandpatrick.vico.core.common.component.LineComponent
import com.patrykandpatrick.vico.core.common.component.ShapeComponent
import com.patrykandpatrick.vico.core.common.component.TextComponent
import com.patrykandpatrick.vico.core.common.shape.CorneredShape
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale


@Composable
fun ExchangeRateChart(
    modelProducer: CartesianChartModelProducer,
    chartPeriod: ChartPeriod,
    modifier: Modifier
) {

    CartesianChartHost(
        chart = rememberCartesianChart(
            rememberColumnCartesianLayer(
                ColumnCartesianLayer.ColumnProvider.series(
                    rememberLineComponent(
                        color = Color(0xffff5500),
                        thickness = 16.dp,
                        shape = CorneredShape.rounded(allPercent = 40)
                    )
                ),
                rangeProvider = remember {
                    CartesianLayerRangeProvider.fixed(
                        maxY = 1400.0,
                        minY = 1200.0)
                }
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
            bottomAxis = HorizontalAxis.rememberBottom(
                line = rememberLineComponent(
                    color = Color.Black,
                    thickness = 1.dp
                ),
                label = rememberTextComponent(Color.Black),
                valueFormatter = when(chartPeriod) {
                    ChartPeriod.MINUTE -> minuteAxisFormatter
                    ChartPeriod.DAY -> dayAxisFormatter
                    ChartPeriod.MONTH -> monthAxisFormatter
                },
                itemPlacer = remember {
                    HorizontalAxis.ItemPlacer.aligned(
                        spacing = 3,
                        addExtremeLabelPadding = true
                    )
                }
            ),
            marker = rememberMarker()
        ),
        modelProducer = modelProducer,
        modifier = modifier
    )
}

// 텍스트 컴포넌트 헬퍼 함수
@Composable
private fun rememberTextComponent(color: Color) = remember {
    TextComponent(
        color = color.toArgb()
    )
}


// 축 포맷터들
private val minuteAxisFormatter = CartesianValueFormatter { _, x, _ ->
    SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(x.toLong()))
}

private val dayAxisFormatter = CartesianValueFormatter { _, x, _ ->
    SimpleDateFormat("MM/dd", Locale.getDefault()).format(Date(x.toLong()))
}

private val monthAxisFormatter = CartesianValueFormatter { _, x, _ ->
    val date = Calendar.getInstance().apply { timeInMillis = x.toLong() }
    "${date.get(Calendar.YEAR)}/${date.get(Calendar.MONTH) + 1}"
}




enum class ChartPeriod(val displayName: String) {
    MINUTE("분"),
    DAY("일"),
    MONTH("월")
}
