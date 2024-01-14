package com.bobodroid.myapplication.lists.wonList

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.bobodroid.myapplication.models.datamodels.*
import com.bobodroid.myapplication.models.viewmodels.*
import com.bobodroid.myapplication.screens.TAG
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.UUID


@ExperimentalMaterialApi
@OptIn(ExperimentalMaterialApi::class)
@SuppressLint("StateFlowValueCalledInComposition")
@Composable
fun BuyWonRecordBox(wonViewModel: WonViewModel, snackbarHostState: SnackbarHostState) {

    val buyRecordHistory : State<List<WonBuyRecord>> = wonViewModel.filterBuyRecordFlow.collectAsState()

    val buySortRecord = buyRecordHistory.value.sortedBy { it.date }

    var lazyScrollState = rememberLazyListState()

    val coroutineScope = rememberCoroutineScope()


    var selectedId by remember { mutableStateOf(UUID.randomUUID()) }




    Row(modifier = Modifier
        .background(Color.White)
        .fillMaxWidth()
        .height(55.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {

        RecordTextView(recordText = "날짜", 45.dp, 16, 2.5f, 0.dp, color = Color.Black)
        Spacer(modifier = Modifier.width(1.dp))
        RecordTextView(recordText = "매수원화\n" + "(매수금)", 45.dp, 16, 2.5f,  0.dp, color = Color.Black)
        Spacer(modifier = Modifier.width(1.dp))
        RecordTextView(recordText = "매수환율", 45.dp, 16, 2.5f,  0.dp, color = Color.Black)
        Spacer(modifier = Modifier.width(1.dp))
        RecordTextView(recordText = "예상수익", 45.dp, 16, 2.5f,  0.dp ,color = Color.Black)

    }

    Column(
        modifier = Modifier) {

        Spacer(modifier = Modifier
            .fillMaxWidth()
            .background(Color.Gray)
            .height(2.dp))



        LazyColumn(modifier = Modifier, state = lazyScrollState) {

            itemsIndexed(
                items = buySortRecord,
                key = { index: Int, item: WonBuyRecord -> item.id}) {index, Buy ->

                WonLineRecordText(
                    Buy,
                    sellAction = Buy.recordColor!!
                    ,
                    sellActed = { buyRecord ->
                        selectedId = buyRecord.id

                        wonViewModel.updateBuyRecord(buyRecord)

                    },
                    onClicked = { recordbox ->
                        selectedId = recordbox.id
                        wonViewModel.dateFlow.value = recordbox.date!!
                        wonViewModel.recordInputMoney.value = recordbox.money!!.toInt()
                        wonViewModel.moneyType.value = recordbox.moneyType!!
                        wonViewModel.haveMoney.value = recordbox.exchangeMoney!!

                        Log.d(TAG, " ${recordbox.money}, ${recordbox.exchangeMoney}")

                    }
                    ,
                    wonViewModel,
                    snackbarHostState = snackbarHostState)

                Divider()
            }
        }


    }
}

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
        RecordTextView(recordText = "예상수익", 45.dp, 16, 2.5f,  0.dp ,color = Color.Black)

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