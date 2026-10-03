package com.bobodroid.myapplication.lists.foreignCurrencyList

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bobodroid.myapplication.components.EmptyRecordView
import com.bobodroid.myapplication.components.RecordHeader
import com.bobodroid.myapplication.components.mainComponents.RecordAlarmSummary
import com.bobodroid.myapplication.models.datamodels.roomDb.CurrencyRecord
import com.bobodroid.myapplication.models.datamodels.roomDb.CurrencyType
import com.bobodroid.myapplication.models.datamodels.roomDb.TargetRates
import com.bobodroid.myapplication.models.viewmodels.CurrencyHoldingInfo
import com.bobodroid.myapplication.models.viewmodels.CurrencyRecordState
import com.bobodroid.myapplication.models.datamodels.roomDb.ForeignCurrencyRecord
import com.bobodroid.myapplication.screens.RecordListEvent
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@ExperimentalMaterialApi
@Composable
fun RecordListView(
    currencyType: CurrencyType,
    currencyRecordState: CurrencyRecordState<CurrencyRecord>,
    hideSellRecordState: Boolean,
    onHideSellRecordChange: (Boolean) -> Unit, // ✅ 추가: 매도 표시 토글
    sortAscending: Boolean,                    // ✅ 추가: true=오래된순, false=최신순
    onSortToggle: () -> Unit,                  // ✅ 추가: 정렬 토글
    holdingStats: CurrencyHoldingInfo,         // ✅ 추가: 상단바 요약 정보용
    targetRates: TargetRates = TargetRates.empty(),   // 기록 카드 알람 표시용
    scrollState: LazyListState = rememberLazyListState(),
    onEvent: (RecordListEvent) -> Unit
) {
    val buyRecordHistory = currencyRecordState.groupedRecords
    val groupList = currencyRecordState.groups

    val filterRecord = if (hideSellRecordState) {
        buyRecordHistory.mapValues { it.value.filter { it.recordColor == false } }
    } else {
        buyRecordHistory
    }

    // ✅ 정렬: 그룹별 항목 순서를 그대로 뒤집는 방식 (오래된순이 원본 순서라고 가정)
    val orderedRecord = if (sortAscending) {
        filterRecord
    } else {
        filterRecord.mapValues { it.value.reversed() }
    }

    val coroutineScope = rememberCoroutineScope()

    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp
    val bottomPadding = screenHeight * 0.6f

    val isEmpty = orderedRecord.values.all { it.isEmpty() }
    val totalCount = orderedRecord.values.sumOf { it.size }

    Column {
        // ✅ 리스트 상단바: 정렬 + 요약 정보 + 매도 표시
        RecordListTopBar(
            totalCount = totalCount,
            holdingStats = holdingStats,
            sortAscending = sortAscending,
            onSortToggle = onSortToggle,
            hideSellRecordState = hideSellRecordState,
            onHideSellRecordChange = onHideSellRecordChange
        )

        if (isEmpty) {
            EmptyRecordView(
                currencyName = currencyType.name,
                onAddClick = {
                    onEvent(RecordListEvent.ShowAddBottomSheet)
                }
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                state = scrollState,
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                orderedRecord.onEachIndexed { groupIndex: Int, (key, items) ->
                    stickyHeader {
                        RecordHeader(key = key)
                    }

                    items(
                        count = items.size,
                        key = { index -> items[index].id!! }
                    ) { index ->
                        val record = items[index]

                        var accumulatedCount = 1
                        (0..<groupIndex).forEach { foreachIndex ->
                            val currentKey = orderedRecord.keys.elementAt(foreachIndex)
                            val elements = orderedRecord.getValue(currentKey)
                            accumulatedCount += elements.count()
                        }

                        val finalIndex = index + accumulatedCount + groupIndex

                        RecordListRowView(
                            currencyType = currencyType,
                            data = record,
                            sellState = record.recordColor!!,
                            groupList = groupList,
                            alarmSummary = RecordAlarmSummary.of(
                                targetRates,
                                currencyType,
                                record.id.toString()
                            ),
                            onEvent = { event ->
                                onEvent(event)
                            },
                            scrollEvent = {
                                coroutineScope.launch {
                                    delay(300)
                                    scrollState.animateScrollToItem(finalIndex, -55)
                                }
                            }
                        )

                        Divider()
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(bottomPadding))
                }
            }
        }
    }
}

/**
 * ✅ 리스트 상단바
 * - 좌측: 정렬 토글 + 요약(총 건수 · 보유량 · 예상수익)
 * - 우측: 매도 표시 토글
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RecordListTopBar(
    totalCount: Int,
    holdingStats: CurrencyHoldingInfo,
    sortAscending: Boolean,
    onSortToggle: () -> Unit,
    hideSellRecordState: Boolean,
    onHideSellRecordChange: (Boolean) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(horizontal = 16.dp, vertical = 4.dp) // ✅ 8dp → 4dp
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.weight(1f)
            ) {
                // 정렬 토글
                Surface(
                    shape = RoundedCornerShape(50),
                    color = Color(0xFFF5F5F5),
                    onClick = onSortToggle
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp) // ✅ 5dp → 3dp
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.SwapVert,
                            contentDescription = "정렬",
                            tint = Color(0xFF6366F1),
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = if (sortAscending) "오래된순" else "최신순",
                            fontSize = 11.sp,
                            lineHeight = 11.sp, // ✅ 추가
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF1F2937)
                        )
                    }
                }

                // 요약 정보
                val summaryText = if (holdingStats.hasData) {
                    "총 ${totalCount}건 · ${holdingStats.holdingAmount} · ${holdingStats.expectedProfit}"
                } else {
                    "총 ${totalCount}건"
                }
                Text(
                    text = summaryText,
                    fontSize = 12.sp,
                    lineHeight = 12.sp, // ✅ 추가
                    color = Color(0xFF6B7280),
                    maxLines = 1,
                    modifier = Modifier.weight(1f, fill = false)
                )
            }

            // 매도 표시 토글
            Surface(
                shape = RoundedCornerShape(50),
                color = Color(0xFFF5F5F5),
                onClick = { onHideSellRecordChange(!hideSellRecordState) }
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(3.dp),
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp) // ✅ 5dp → 3dp
                ) {
                    Icon(
                        imageVector = if (hideSellRecordState)
                            Icons.Rounded.VisibilityOff
                        else
                            Icons.Rounded.Visibility,
                        contentDescription = null,
                        tint = Color(0xFF6366F1),
                        modifier = Modifier.size(13.dp)
                    )
                    Text(
                        text = if (hideSellRecordState) "매도 숨김" else "매도 표시",
                        color = Color(0xFF1F2937),
                        fontSize = 11.sp,
                        lineHeight = 11.sp, // ✅ 추가
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}
