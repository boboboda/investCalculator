package com.bobodroid.myapplication.screens.backtest

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bobodroid.myapplication.models.datamodels.backtest.BacktestOpenPosition
import com.bobodroid.myapplication.models.datamodels.backtest.BacktestTrade
import com.bobodroid.myapplication.models.viewmodels.BacktestViewModel
import java.text.NumberFormat
import java.util.Locale

@Composable
fun BacktestResultScreen(
    viewModel: BacktestViewModel,
    onBackClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val result = uiState.result

    // ⚠️ 수정: 이 화면이 사라질 때(뒤로가기 화살표든 시스템 뒤로가기/제스처든 어떤 경로로
    // 나가든) uiState.result를 비워줍니다.
    // BacktestViewModel은 Input/Result/History 화면이 공유하는 하나의 인스턴스라서,
    // 여기서 result를 비워주지 않으면 뒤로가기로 Input 화면에 돌아갔을 때 그 화면의
    // LaunchedEffect(uiState.result)가 "아직도 result가 있네?"라고 판단해 onSubmit()을
    // 다시 호출해버려서, Input이 보이자마자 곧바로 이 Result 화면으로 다시 튕겨나가는
    // 문제(겉보기엔 "뒤로가기가 안 먹힌다")가 있었습니다.
    DisposableEffect(Unit) {
        onDispose {
            viewModel.clearResult()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF6F5F2))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF16233A))
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = "뒤로가기",
                    tint = Color.White
                )
            }
            Column {
                if (result != null) {
                    Text(
                        text = "${result.currencyType} · ${result.periodMonths}개월 · ${result.investStrategy}",
                        fontSize = 12.sp,
                        color = Color(0xFF9FB3CC)
                    )
                }
                Text(text = "백테스트 결과", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
        }

        if (result == null) {
            Box(modifier = Modifier.fillMaxSize().weight(1f), contentAlignment = Alignment.Center) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(color = Color(0xFF6366F1))
                } else {
                    Text(text = uiState.errorMessage ?: "결과가 없습니다.", color = Color(0xFF6B7280))
                }
            }
        } else {
            val isProfit = result.realizedProfitWon >= 0
            val profitColor = if (isProfit) Color(0xFFD92D20) else Color(0xFF2563EB)

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                            Column {
                                Text(text = "실현 수익률", fontSize = 12.sp, color = Color(0xFF8A93A3))
                                Text(
                                    text = "${if (isProfit) "+" else ""}${"%.2f".format(result.profitRatePercent)}%",
                                    fontSize = 30.sp,
                                    fontWeight = FontWeight.Black,
                                    color = profitColor
                                )
                                Text(
                                    text = "${if (isProfit) "+" else ""}${formatWon(result.realizedProfitWon)}원 실현",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = profitColor
                                )
                            }
                            HorizontalDivider(color = Color(0xFFEFEDE7))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                StatCol("원금", formatWonShort(result.principal))
                                StatCol("매수", "${result.buyCount}회")
                                StatCol("매도", "${result.sellCount}회")
                                StatCol("보유중", "${result.openPositionCount}건")
                            }
                        }
                    }
                }

                if (result.warnings.isNotEmpty()) {
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF3C7)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                result.warnings.forEach { w ->
                                    Text(text = "· $w", fontSize = 12.sp, color = Color(0xFF92400E))
                                }
                            }
                        }
                    }
                }

                if (result.openPositions.isNotEmpty()) {
                    item {
                        Text(text = "보유 포지션 (${result.openPositions.size})", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF5B6472))
                    }
                    items(result.openPositions) { pos -> OpenPositionRow(pos) }
                }

                if (result.trades.isNotEmpty()) {
                    item {
                        Text(text = "매매 내역 (${result.trades.size})", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF5B6472))
                    }
                    items(result.trades) { trade -> TradeRow(trade) }
                }
            }
        }
    }
}

@Composable
private fun StatCol(label: String, value: String) {
    Column {
        Text(text = label, fontSize = 10.sp, color = Color(0xFF8A93A3))
        Text(text = value, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1F2937))
    }
}

@Composable
private fun OpenPositionRow(pos: BacktestOpenPosition) {
    val isProfit = pos.unrealizedProfitWon >= 0
    val color = if (isProfit) Color(0xFFD92D20) else Color(0xFF2563EB)
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(text = "${pos.round}회차 · 매입 ${"%.2f".format(pos.buyRate)}원", fontSize = 12.sp, color = Color(0xFF8A93A3))
                Text(text = "${formatWon(pos.amountWon)}원", fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(text = "평가손익", fontSize = 11.sp, color = Color(0xFF8A93A3))
                Text(
                    text = "${if (isProfit) "+" else ""}${formatWon(pos.unrealizedProfitWon)}원",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = color
                )
            }
        }
    }
}

@Composable
private fun TradeRow(trade: BacktestTrade) {
    val isBuy = trade.type == "BUY"
    val tagColor = if (isBuy) Color(0xFF16233A) else Color(0xFFFDECEA)
    val tagTextColor = if (isBuy) Color.White else Color(0xFFD92D20)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .width(40.dp)
                    .height(24.dp)
                    .background(tagColor, RoundedCornerShape(6.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = if (isBuy) "매수" else "매도", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = tagTextColor)
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(text = "${trade.round}회차 · ${trade.timestamp.take(10)}", fontSize = 12.sp, color = Color(0xFF8A93A3))
                Text(text = "${"%.2f".format(trade.rate)}원", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(text = "${formatWon(trade.amountWon)}원", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                trade.profitWon?.let { profit ->
                    val isProfit = profit >= 0
                    Text(
                        text = "${if (isProfit) "+" else ""}${formatWon(profit)}원",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isProfit) Color(0xFFD92D20) else Color(0xFF2563EB)
                    )
                }
            }
        }
    }
}

private fun formatWon(value: Double): String {
    return NumberFormat.getNumberInstance(Locale.KOREA).format(value.toLong())
}

private fun formatWonShort(value: Double): String {
    return when {
        value >= 100_000_000 -> "%.1f억".format(value / 100_000_000)
        value >= 10_000 -> "%,d만".format((value / 10_000).toLong())
        else -> formatWon(value)
    }
}