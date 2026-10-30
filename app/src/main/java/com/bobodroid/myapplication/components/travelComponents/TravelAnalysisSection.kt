package com.bobodroid.myapplication.components.travelComponents

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.bobodroid.myapplication.extensions.toLongWon
import com.bobodroid.myapplication.models.datamodels.useCases.TripSummary
import com.bobodroid.myapplication.models.viewmodels.TravelEvent
import com.bobodroid.myapplication.models.viewmodels.TravelViewModel
import com.bobodroid.myapplication.ui.theme.primaryColor

private val categoryColors = listOf(
    primaryColor,
    Color(0xFF9C8BF5),
    Color(0xFFC3B8FA),
    Color(0xFFE7E2FD)
)

/**
 * 분석탭 - 여행모드 라우트.
 * 여행 목록을 구독해 선택된 여행(없으면 첫 번째 여행)의 TripSummary를 그려준다.
 * AnalysisScreen.kt에서 여행모드일 때 PremiumChartScreen 대신 이 컴포저블 하나만 호출하면 됨.
 */
@Composable
fun TravelAnalysisTab(
    travelViewModel: TravelViewModel = hiltViewModel()
) {
    val uiState by travelViewModel.uiState.collectAsState()
    val ongoingTrips by travelViewModel.ongoingTrips.collectAsState()
    val completedTrips by travelViewModel.completedTrips.collectAsState()
    val allTrips = ongoingTrips + completedTrips

    LaunchedEffect(allTrips.map { it.id }, uiState.selectedTrip?.id) {
        if (uiState.selectedTrip == null) {
            val first = allTrips.firstOrNull()
            if (first != null) {
                travelViewModel.handleTravelEvent(TravelEvent.SelectTrip(first.id))
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        if (allTrips.size > 1) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(bottom = 14.dp)
            ) {
                allTrips.forEach { trip ->
                    val selected = uiState.selectedTrip?.id == trip.id
                    Text(
                        trip.tripName,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (selected) Color.White else Color(0xFF6B7280),
                        modifier = Modifier
                            .background(
                                if (selected) primaryColor else Color(0xFFF7F6FE),
                                RoundedCornerShape(10.dp)
                            )
                            .clickable {
                                if (!selected) {
                                    travelViewModel.handleTravelEvent(TravelEvent.SelectTrip(trip.id))
                                }
                            }
                            .padding(horizontal = 12.dp, vertical = 7.dp)
                    )
                }
            }
        }

        val summary = uiState.tripSummary
        when {
            allTrips.isEmpty() -> Text("아직 생성된 여행이 없어요", fontSize = 14.sp, color = Color(0xFF8B93A3))
            summary == null -> Text("불러오는 중...", fontSize = 14.sp, color = Color(0xFF8B93A3))
            else -> TravelAnalysisSection(summary = summary)
        }
    }
}

/**
 * 분석탭 - 여행 요약 섹션 (TripSummary를 그대로 받아 그리는 독립 컴포저블)
 */
@Composable
fun TravelAnalysisSection(summary: TripSummary) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                summary.trip.tripName + " 요약",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1F2430)
            )
            Text(
                summary.trip.startDate + " ~ " + summary.trip.endDate + " · " + summary.tripDays + "일",
                fontSize = 12.sp,
                color = Color(0xFF8B93A3)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFF7F6FE), RoundedCornerShape(16.dp))
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text("총 지출 · 내 평균 환전환율 기준", fontSize = 12.sp, color = Color(0xFF8B93A3))
                Text(
                    summary.totalSpentWon.toLong().toLongWon() + "원",
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1F2430)
                )
            }

            val budgetWon = summary.trip.budgetWon?.toFloatOrNull()
            val usagePercent = summary.budgetUsagePercent
            if (budgetWon != null && budgetWon > 0f && usagePercent != null) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Text("예산 대비", fontSize = 12.sp, color = Color(0xFF8B93A3))
                        Text(
                            usagePercent.toString() + "% 사용 (" + summary.totalSpentWon.toLong().toLongWon() +
                                    " / " + budgetWon.toLong().toLongWon() + "원)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1F2430)
                        )
                    }
                    LinearProgressIndicator(
                        progress = (usagePercent / 100f).coerceIn(0f, 1f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(7.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = primaryColor,
                        trackColor = Color(0xFFE7E2FD)
                    )
                }
            }
        }

        if (summary.categoryBreakdown.isNotEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White, RoundedCornerShape(16.dp))
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("카테고리별 지출", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF8B93A3))
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    summary.categoryBreakdown.sortedByDescending { it.totalWon }.forEachIndexed { index, category ->
                        Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                Text(category.categoryName, fontSize = 13.sp, color = Color(0xFF1F2430))
                                Text(
                                    category.totalWon.toLong().toLongWon() + "원 · " + category.percent + "%",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1F2430)
                                )
                            }
                            LinearProgressIndicator(
                                progress = (category.percent / 100f).coerceIn(0f, 1f),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = categoryColors[index % categoryColors.size],
                                trackColor = Color(0xFFF1EEFE)
                            )
                        }
                    }
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White, RoundedCornerShape(16.dp))
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("일 평균 지출", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF8B93A3))
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text(summary.tripDays.toString() + "일 여행 기준", fontSize = 14.sp, color = Color(0xFF1F2430))
                Text(
                    summary.dailyAverageWon.toLong().toLongWon() + "원 / 일",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = primaryColor
                )
            }
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text("지출 건수", fontSize = 14.sp, color = Color(0xFF1F2430))
                Text(
                    summary.expenseCount.toString() + "건",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1F2430)
                )
            }
        }
    }
}
