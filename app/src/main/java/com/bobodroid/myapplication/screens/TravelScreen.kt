package com.bobodroid.myapplication.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Flight
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.bobodroid.myapplication.components.mainComponents.BottomSheet
import com.bobodroid.myapplication.components.travelComponents.ExpenseAddBottomSheet
import com.bobodroid.myapplication.components.travelComponents.TripCreateBottomSheet
import com.bobodroid.myapplication.extensions.toLongWon
import com.bobodroid.myapplication.models.datamodels.roomDb.Trip
import com.bobodroid.myapplication.models.datamodels.roomDb.TripExpense
import com.bobodroid.myapplication.models.viewmodels.TravelEvent
import com.bobodroid.myapplication.models.viewmodels.TravelViewModel
import com.bobodroid.myapplication.ui.theme.primaryColor
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.temporal.ChronoUnit

/**
 * 여행모드 - 기록탭 여행 카드 리스트 화면
 * 진행중/완료 여행 카드 + 카드 펼침(지출 내역) + 새 여행 만들기 FAB
 *
 * ✅ 5단계 수정: Scaffold 중첩 제거 — 바깥(InvestAppScreen)에 이미 bottomBar용 Scaffold가 있어서
 * MainScreen과 동일하게 Box + 수동 FAB/스낵바 배치 방식으로 통일.
 * ✅ 5단계 추가: 상단에 "✈️ 여행모드" 헤더 바 추가 (백지 화면 방지).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TravelScreen(
    travelViewModel: TravelViewModel = hiltViewModel()
) {
    val uiState by travelViewModel.uiState.collectAsState()
    val ongoingTrips by travelViewModel.ongoingTrips.collectAsState()
    val completedTrips by travelViewModel.completedTrips.collectAsState()

    val snackBarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    val createSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val expenseSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    LaunchedEffect(uiState.snackBarMessage) {
        val message = uiState.snackBarMessage
        if (message != null) {
            snackBarHostState.showSnackbar(message)
            travelViewModel.consumeSnackBar()
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.BottomCenter   // ✅ MainScreen과 동일: FAB가 이 설정 덕에 하단에 붙음
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // ✅ 헤더: 여행모드임을 알려주는 상단 바
            TravelHeaderBar(ongoingCount = ongoingTrips.size)

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(ongoingTrips, key = { it.id }) { trip ->
                    TripCard(
                        trip = trip,
                        isCompleted = false,
                        isExpanded = uiState.selectedTrip?.id == trip.id,
                        expenses = if (uiState.selectedTrip?.id == trip.id) uiState.expenses else emptyList(),
                        onToggle = { travelViewModel.handleTravelEvent(TravelEvent.SelectTrip(trip.id)) },
                        onAddExpense = { travelViewModel.handleTravelEvent(TravelEvent.ShowAddExpenseSheet(trip.id)) }
                    )
                }

                items(completedTrips, key = { it.id }) { trip ->
                    TripCard(
                        trip = trip,
                        isCompleted = true,
                        isExpanded = uiState.selectedTrip?.id == trip.id,
                        expenses = if (uiState.selectedTrip?.id == trip.id) uiState.expenses else emptyList(),
                        onToggle = { travelViewModel.handleTravelEvent(TravelEvent.SelectTrip(trip.id)) },
                        onAddExpense = { travelViewModel.handleTravelEvent(TravelEvent.ShowAddExpenseSheet(trip.id)) }
                    )
                }

                item { Spacer(modifier = Modifier.height(80.dp)) }
            }
        }

        // ✅ FAB: MainScreen의 ContentIcon과 동일한 패턴으로 수동 배치 (Scaffold 미사용)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(end = 20.dp, bottom = 20.dp),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.Bottom
        ) {
            FloatingActionButton(
                onClick = { travelViewModel.handleTravelEvent(TravelEvent.ShowCreateTripSheet) },
                containerColor = primaryColor,
                contentColor = Color.White,
                elevation = FloatingActionButtonDefaults.elevation(
                    defaultElevation = 4.dp,
                    pressedElevation = 8.dp
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Rounded.Add, contentDescription = "새 여행 만들기", modifier = Modifier.size(28.dp))
            }
        }

        Box {
            SnackbarHost(snackBarHostState)
        }
    }

    if (uiState.createStep > 0) {
        BottomSheet(
            sheetState = createSheetState,
            snackBarHostState = snackBarHostState,
            onDismissRequest = {
                coroutineScope.launch {
                    createSheetState.hide()
                    travelViewModel.handleTravelEvent(TravelEvent.DismissCreateTripSheet)
                }
            }
        ) {
            TripCreateBottomSheet(
                step = uiState.createStep,
                draft = uiState.createDraft,
                onEvent = { event ->
                    travelViewModel.handleTravelEvent(event)
                    if (event is TravelEvent.CreateStep2Event.StartTripClicked) {
                        coroutineScope.launch { createSheetState.hide() }
                    }
                }
            )
        }
    }

    if (uiState.showAddExpenseSheet) {
        val targetTrip = uiState.selectedTrip
        if (targetTrip != null) {
            BottomSheet(
                sheetState = expenseSheetState,
                snackBarHostState = snackBarHostState,
                onDismissRequest = {
                    coroutineScope.launch {
                        expenseSheetState.hide()
                        travelViewModel.handleTravelEvent(TravelEvent.DismissExpenseSheet)
                    }
                }
            ) {
                ExpenseAddBottomSheet(
                    trip = targetTrip,
                    draft = uiState.expenseDraft,
                    isEditing = uiState.editingExpenseId != null,
                    onEvent = { event ->
                        travelViewModel.handleTravelEvent(event)
                        if (event is TravelEvent.ExpenseDraftEvent.ConfirmClicked) {
                            coroutineScope.launch { expenseSheetState.hide() }
                        }
                    },
                    onCancel = {
                        coroutineScope.launch {
                            expenseSheetState.hide()
                            travelViewModel.handleTravelEvent(TravelEvent.DismissExpenseSheet)
                        }
                    }
                )
            }
        }
    }
}

/**
 * ✅ 신규: 여행모드 상단 헤더 바
 * 기존 MainTopBar(40dp, 흰 배경)와 비슷한 톤으로, "지금 여행모드를 보고 있다"는 표시만 담당.
 */
@Composable
private fun TravelHeaderBar(ongoingCount: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(44.dp)
            .background(Color.White)
            .padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = Icons.Rounded.Flight,
            contentDescription = null,
            tint = primaryColor,
            modifier = Modifier.size(18.dp)
        )
        Text(
            text = "여행모드",
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1F2430)
        )
        Spacer(modifier = Modifier.weight(1f))
        if (ongoingCount > 0) {
            Text(
                text = "진행중 " + ongoingCount + "개",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = primaryColor,
                modifier = Modifier
                    .background(Color(0xFFF1EEFE), RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }
    }
}

@Composable
private fun TripCard(
    trip: Trip,
    isCompleted: Boolean,
    isExpanded: Boolean,
    expenses: List<TripExpense>,
    onToggle: () -> Unit,
    onAddExpense: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(16.dp))
            .border(1.dp, Color(0xFFECEAF5), RoundedCornerShape(16.dp))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onToggle)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    trip.currencyCode,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isCompleted) Color(0xFF8B93A3) else primaryColor,
                    modifier = Modifier
                        .background(
                            if (isCompleted) Color(0xFFF5F5F7) else Color(0xFFF1EEFE),
                            RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )
                Text(
                    trip.tripName,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1F2430),
                    modifier = Modifier.weight(1f)
                )
                if (isCompleted) {
                    Text("완료", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF9AA3AF))
                } else {
                    Text(
                        text = dDayLabel(trip.startDate, trip.endDate),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = primaryColor,
                        modifier = Modifier
                            .background(Color(0xFFE7E2FD), RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
                Icon(
                    imageVector = if (isExpanded) Icons.Rounded.KeyboardArrowUp else Icons.Rounded.KeyboardArrowDown,
                    contentDescription = null,
                    tint = Color(0xFF8B93A3)
                )
            }

            Text(
                trip.startDate + " ~ " + trip.endDate,
                fontSize = 12.sp,
                color = Color(0xFF8B93A3)
            )

            if (isExpanded) {
                val budget = trip.budgetWon?.toFloatOrNull()
                val spent = expenses.sumOf { it.krwAmount.toDoubleOrNull() ?: 0.0 }.toFloat()
                if (budget != null && budget > 0f) {
                    val percent = ((spent / budget) * 100).toInt().coerceAtLeast(0)
                    Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Text("예산 사용", fontSize = 12.sp, color = Color(0xFF8B93A3))
                            Text(
                                spent.toLong().toLongWon() + " / " + budget.toLong().toLongWon() + "원 (" + percent + "%)",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1F2430)
                            )
                        }
                        LinearProgressIndicator(
                            progress = (percent / 100f).coerceIn(0f, 1f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = primaryColor,
                            trackColor = Color(0xFFF1EEFE)
                        )
                    }
                }
            }
        }

        if (isExpanded) {
            HorizontalDivider(color = Color(0xFFECEAF5))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("지출 내역", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF8B93A3))
                    Text(
                        "+ 지출 추가",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = primaryColor,
                        modifier = Modifier
                            .background(Color(0xFFF1EEFE), RoundedCornerShape(8.dp))
                            .clickable(onClick = onAddExpense)
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }

                if (expenses.isEmpty()) {
                    Text("아직 지출 내역이 없어요", fontSize = 13.sp, color = Color(0xFFB7BECB))
                } else {
                    expenses.forEach { expense ->
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(
                                    expense.memo?.takeIf { it.isNotBlank() } ?: expense.categoryName,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF1F2430)
                                )
                                Text(
                                    expense.date + " · " + expense.categoryName,
                                    fontSize = 11.sp,
                                    color = Color(0xFF8B93A3)
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    expense.amount + trip.currencyCode,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1F2430)
                                )
                                val krwLong = expense.krwAmount.toDoubleOrNull()?.toLong()
                                Text(
                                    (if (krwLong != null) krwLong.toLongWon() else expense.krwAmount) + "원",
                                    fontSize = 11.sp,
                                    color = Color(0xFF8B93A3)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * 오늘 기준 D-day 표기
 * - 시작 전: 시작일까지 D-n
 * - 여행 중: 종료일까지 D-n
 * - 종료일 지남(아직 완료 처리 전): "여행중"으로 표기
 */
private fun dDayLabel(startDate: String, endDate: String): String {
    return try {
        val today = LocalDate.now()
        val start = LocalDate.parse(startDate)
        val end = LocalDate.parse(endDate)
        when {
            today.isBefore(start) -> "D-" + ChronoUnit.DAYS.between(today, start)
            !today.isAfter(end) -> "D-" + ChronoUnit.DAYS.between(today, end)
            else -> "여행중"
        }
    } catch (e: Exception) {
        ""
    }
}
