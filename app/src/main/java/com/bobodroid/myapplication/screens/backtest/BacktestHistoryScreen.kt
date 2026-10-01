package com.bobodroid.myapplication.screens.backtest

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bobodroid.myapplication.models.datamodels.backtest.BacktestHistoryItem
import com.bobodroid.myapplication.models.viewmodels.BacktestViewModel
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

@Composable
fun BacktestHistoryScreen(
    viewModel: BacktestViewModel,
    onBackClick: () -> Unit,
    onSelectHistory: (UUID) -> Unit
) {
    val historyList by viewModel.historyList.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FA))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = "뒤로가기",
                    tint = Color(0xFF1F2937)
                )
            }
            Text(text = "백테스트 실행 기록", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1F2937))
        }

        if (historyList.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().weight(1f), contentAlignment = Alignment.Center) {
                Text(text = "아직 실행한 백테스트가 없습니다", color = Color.Gray)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(historyList) { item -> HistoryRow(item, onClick = { onSelectHistory(item.id) }) }
            }
        }
    }
}

@Composable
private fun HistoryRow(item: BacktestHistoryItem, onClick: () -> Unit) {
    val isProfit = item.realizedProfitWon >= 0
    val color = if (isProfit) Color(0xFFD92D20) else Color(0xFF2563EB)
    val dateText = remember(item.createdAt) {
        SimpleDateFormat("yyyy.MM.dd HH:mm", Locale.KOREA).format(Date(item.createdAt))
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    text = "${item.currencyType} · ${item.periodMonths}개월 · ${item.investStrategy}",
                    fontSize = 12.sp,
                    color = Color(0xFF8A93A3)
                )
                Text(text = dateText, fontSize = 11.sp, color = Color(0xFFA6A6B5))
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "${if (isProfit) "+" else ""}${"%.2f".format(item.profitRatePercent)}%",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = color
                )
                Text(
                    text = "${if (isProfit) "+" else ""}${NumberFormat.getNumberInstance(Locale.KOREA).format(item.realizedProfitWon.toLong())}원",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = color
                )
            }
        }
    }
}
