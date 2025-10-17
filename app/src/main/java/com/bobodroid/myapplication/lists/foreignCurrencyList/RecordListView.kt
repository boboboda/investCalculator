package com.bobodroid.myapplication.lists.foreignCurrencyList

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.Divider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bobodroid.myapplication.components.RecordHeader
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
    currencyRecordState: CurrencyRecordState<ForeignCurrencyRecord>,
    hideSellRecordState: Boolean,
    onEvent: (RecordListEvent) -> Unit
) {
    val buyRecordHistory = currencyRecordState.groupedRecords
    val groupList = currencyRecordState.groups

    val filterRecord = if (hideSellRecordState) {
        buyRecordHistory.mapValues { it.value.filter { it.recordColor == false } }
    } else {
        buyRecordHistory
    }

    val lazyScrollState = androidx.compose.foundation.lazy.rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    Column {
        // ❌ 헤더 제거! (RecordTextView 모두 삭제)

        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            state = lazyScrollState,
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            filterRecord.onEachIndexed { groupIndex: Int, (key, items) ->
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
                        val currentKey = filterRecord.keys.elementAt(foreachIndex)
                        val elements = filterRecord.getValue(currentKey)
                        accumulatedCount += elements.count()
                    }

                    val finalIndex = index + accumulatedCount + groupIndex

                    RecordListRowView(
                        currencyType = currencyType,
                        data = record,
                        sellState = record.recordColor!!,
                        groupList = groupList,
                        onEvent = { event ->
                            onEvent(event)
                        },
                        scrollEvent = {
                            coroutineScope.launch {
                                delay(300)
                                lazyScrollState.animateScrollToItem(finalIndex, -55)
                            }
                        }
                    )

                    Divider()
                }
            }

            item {
                Spacer(modifier = Modifier.height(100.dp))
            }
        }
    }
}