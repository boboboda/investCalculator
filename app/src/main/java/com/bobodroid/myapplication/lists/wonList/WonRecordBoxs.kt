package com.bobodroid.myapplication.lists.wonList

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.*
import androidx.compose.material3.Divider
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.bobodroid.myapplication.components.*
import com.bobodroid.myapplication.lists.dollorList.TotalLineDrRecord
import com.bobodroid.myapplication.models.datamodels.*
import com.bobodroid.myapplication.models.viewmodels.*
import com.bobodroid.myapplication.screens.TAG
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.UUID


@Composable
fun SellWonRecordBox(wonViewModel: WonViewModel, snackbarHostState: SnackbarHostState) {

    val sellRecordHistory : State<List<WonSellRecord>> = wonViewModel.filterSellRecordFlow.collectAsState()


    var lazyScrollState = rememberLazyListState()

    val coroutineScope = rememberCoroutineScope()

    val sellSortRecord = sellRecordHistory.value.sortedBy { it.date }


    Row(modifier = Modifier
        .background(Color.White)
        .fillMaxWidth()
        .height(80.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {

        RecordTextView(recordText = "날짜", 45.dp, 16, 2.5f, 0.dp, color = Color.Black)
        Spacer(modifier = Modifier.width(1.dp))
        RecordTextView(recordText = "매도원화", 45.dp, 16, 2.5f,  0.dp, color = Color.Black)
        Spacer(modifier = Modifier.width(1.dp))
        RecordTextView(recordText = "매도환율", 45.dp, 16, 2.5f,  0.dp, color = Color.Black)
        Spacer(modifier = Modifier.width(1.dp))
        RecordTextView(recordText = "수익", 45.dp, 16, 2.5f,  0.dp ,color = Color.Black)

    }

    Column(
        modifier = Modifier) {

        Spacer(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.Gray)
                .height(2.dp))



        LazyColumn(modifier = Modifier, state = lazyScrollState) {

            val listSize = sellSortRecord.size

            itemsIndexed(
                items = sellSortRecord,
                key = { index: Int, item: WonSellRecord -> item.id}) { index, Sell ->

                WonSellLineRecordText(
                    Sell,
                    index = index,
                    listSize = listSize,
                    wonViewModel = wonViewModel,
                    snackbarHostState = snackbarHostState) {
                    coroutineScope.launch {
                        if(index <= listSize - 7) {
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