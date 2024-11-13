package com.bobodroid.myapplication.lists.dollorList

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Divider
import androidx.compose.material.SnackbarHostState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.bobodroid.myapplication.components.*
import com.bobodroid.myapplication.models.datamodels.roomDb.DrSellRecord
import com.bobodroid.myapplication.models.viewmodels.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun SellRecordBox(
    dollarViewModel: DollarViewModel,
    snackBarHostState: SnackbarHostState
) {

    val sellRecordHistory = dollarViewModel.filterSellRecordFlow.collectAsState()

    val sellSortRecord = sellRecordHistory.value.sortedBy { it.date }

    var lazyScrollState = rememberLazyListState()

    val coroutineScope = rememberCoroutineScope()

    val deleteAskDialog = remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .background(Color.White)
            .fillMaxWidth()
            .height(55.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {

        RecordTextView(recordText = "날짜", 45.dp, 16, 2.5f, 0.dp, color = Color.Black)
        Spacer(modifier = Modifier.width(1.dp))
        RecordTextView(recordText = "매도달러", 45.dp, 16, 2.5f, 0.dp, color = Color.Black)
        Spacer(modifier = Modifier.width(1.dp))
        RecordTextView(recordText = "매도환율", 45.dp, 16, 2.5f, 0.dp, color = Color.Black)
        Spacer(modifier = Modifier.width(1.dp))
        RecordTextView(recordText = "수익", 45.dp, 16, 2.5f, 0.dp, color = Color.Black)

    }

    Column(
        modifier = Modifier
    ) {

        Spacer(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.Gray)
                .height(2.dp)
        )



        LazyColumn(modifier = Modifier, state = lazyScrollState) {

            val listSize = sellSortRecord.size

            itemsIndexed(
                items = sellSortRecord,
                key = { index: Int, item: DrSellRecord -> item.id }) { index, Sell ->

                SellLineDrRecordText(
                    Sell,
                    index = index,
                    listSize = listSize,
                    dollarViewModel = dollarViewModel,
                    snackbarHostState = snackBarHostState
                ) {
                    coroutineScope.launch {
                        if (index <= listSize - 7) {
                            delay(300)
                            lazyScrollState.animateScrollToItem(index)
                        } else {
                            delay(300)
                            lazyScrollState.animateScrollToItem(index, 0)
                        }

                    }
                }

                Divider()
            }
        }


    }
}
