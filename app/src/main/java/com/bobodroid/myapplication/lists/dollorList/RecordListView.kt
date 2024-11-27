package com.bobodroid.myapplication.lists.dollorList

import android.annotation.SuppressLint
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.SnackbarHostState
import androidx.compose.material3.Divider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.bobodroid.myapplication.components.RecordHeader
import com.bobodroid.myapplication.components.RecordTextView
import com.bobodroid.myapplication.models.datamodels.roomDb.ForeignCurrencyRecord
import com.bobodroid.myapplication.models.viewmodels.CurrencyRecordState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.UUID

@ExperimentalMaterialApi
@OptIn(ExperimentalFoundationApi::class,
    ExperimentalStdlibApi::class
)
@SuppressLint("StateFlowValueCalledInComposition")
@Composable
fun RecordListView(
    snackBarHostState: SnackbarHostState,
    currencyRecordState: CurrencyRecordState<ForeignCurrencyRecord>,
    hideSellRecordState: Boolean
) {

    val buyRecordHistory = currencyRecordState.groupedRecords

    val groupList = currencyRecordState.groups

    val filterRecord = if (hideSellRecordState) {
        buyRecordHistory.mapValues { it.value.filter { it.recordColor == false } }
    } else {
        buyRecordHistory
    }

    var selectedId by remember { mutableStateOf(UUID.randomUUID()) }

    var lazyScrollState = rememberLazyListState()

    val coroutineScope = rememberCoroutineScope()



    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(55.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        RecordTextView(
            recordText = "매수날짜\n " + "(매도날짜)",
            45.dp,
            16,
            2.5f,
            0.dp,
            color = Color.Black
        )
        Spacer(modifier = Modifier.width(1.dp))
        RecordTextView(recordText = "매수달러\n" + "(매수금)", 45.dp, 16, 2.5f, 0.dp, color = Color.Black)
        Spacer(modifier = Modifier.width(1.dp))
        RecordTextView(recordText = "매수환율\n" + "(매도환율)", 45.dp, 16, 2.5f, 0.dp, color = Color.Black)
        Spacer(modifier = Modifier.width(1.dp))
        RecordTextView(
            recordText = "예상수익\n " + "(확정수익)",
            45.dp,
            16,
            2.5f,
            0.dp,
            color = Color.Black
        )
    }


    //리스트 아이템

    Column(
    ) {

        Spacer(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.Gray)
                .height(2.dp)
        )

        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            state = lazyScrollState
        ) {

            //  Buy -> 라인리코드텍스트에 넣지 말고 바로 데이터 전달 -> 리팩토링
            filterRecord.onEachIndexed { groupIndex: Int, (key, items) ->
                stickyHeader {
                    RecordHeader(key = key)
                }

                items(
                    items = items,
                    key = { it.id!! }
                ) { Buy ->

                    var accmulatedCount = 1

                    (0..<groupIndex).forEach { foreachIndex ->
                        val currentKey = filterRecord.keys.elementAt(foreachIndex)
                        val elements = filterRecord.getValue(currentKey)
                        accmulatedCount += elements.count()
                    }

                    val foundIndex = items.indexOfFirst { it.id === Buy.id }

                    val finalIndex = foundIndex + accmulatedCount + groupIndex

                    RecordListRowView(
                        Buy,
                        sellAction = Buy.recordColor!!,
                        sellSelected = {

                        },
                        insertSelected = { date, money, rate->
//                            dollarViewModel.insertBuyRecord(Buy, date, money, rate)
                        },
                        groupList = groupList,
                        snackBarHostState = snackBarHostState
                    ) {
                        coroutineScope.launch {
                            delay(300)
                            lazyScrollState.animateScrollToItem(finalIndex, -55)
                        }
                    }

                    Divider()
                }
            }


        }
    }
}