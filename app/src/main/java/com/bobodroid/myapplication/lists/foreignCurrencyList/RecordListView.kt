package com.bobodroid.myapplication.lists.foreignCurrencyList

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.Divider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import com.bobodroid.myapplication.components.EmptyRecordView
import com.bobodroid.myapplication.components.RecordHeader
import com.bobodroid.myapplication.data.mapper.RecordMapper.toLegacyRecord
import com.bobodroid.myapplication.models.datamodels.roomDb.CurrencyRecord
import com.bobodroid.myapplication.models.datamodels.roomDb.CurrencyType
import com.bobodroid.myapplication.models.viewmodels.CurrencyRecordState
import com.bobodroid.myapplication.models.datamodels.roomDb.ForeignCurrencyRecord
import com.bobodroid.myapplication.screens.RecordListEvent
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@ExperimentalMaterialApi
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun RecordListView(
    currencyType: CurrencyType,
    currencyRecordState: CurrencyRecordState<CurrencyRecord>,
    hideSellRecordState: Boolean,
    scrollState: LazyListState = rememberLazyListState(),
    onEvent: (RecordListEvent) -> Unit
) {
    val groupList = currencyRecordState.groups

    val displayRecords = if (hideSellRecordState) {
        // UI에서 간단한 필터링만 수행 (복잡한 로직 아님)
        currencyRecordState.groupedRecords.mapValues { (_, records) ->
            records.filter { it.recordColor == false }
        }
    } else {
        currencyRecordState.groupedRecords
    }

    val coroutineScope = rememberCoroutineScope()

    // 🎯 화면 높이의 60%를 여백으로 추가 (항상 스크롤 가능하도록)
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp
    val bottomPadding = screenHeight * 0.6f  // 화면 높이의 60%


    // 🎯 기록이 없는지 확인
    val isEmpty = displayRecords.values.all { it.isEmpty() }

    Column {

        if (isEmpty) {
            // 🎯 빈 화면 표시
            EmptyRecordView(
                currencyName = currencyType.name,
                onAddClick = {
                    // 추가 버튼 클릭 이벤트
                    onEvent(RecordListEvent.ShowAddBottomSheet)
                }
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                state = scrollState,
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                displayRecords.onEachIndexed { groupIndex: Int, (key, items) ->
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
                            val currentKey = displayRecords.keys.elementAt(foreachIndex)
                            val elements = displayRecords.getValue(currentKey)
                            accumulatedCount += elements.count()
                        }

                        val finalIndex = index + accumulatedCount + groupIndex

                        RecordListRowView(
                            currencyType = currencyType,
                            data = record.toLegacyRecord(),
                            sellState = record.recordColor!!,
                            groupList = groupList,
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

                // 🎯 충분한 하단 여백 - 항상 스크롤 가능하도록
                item {
                    Spacer(modifier = Modifier.height(bottomPadding))
                }
            }
        }





    }
}