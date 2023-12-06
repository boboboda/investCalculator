package com.bobodroid.myapplication.lists

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Divider
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.bobodroid.myapplication.components.*
import com.bobodroid.myapplication.models.datamodels.*
import com.bobodroid.myapplication.models.viewmodels.*
import com.bobodroid.myapplication.ui.theme.DeleteColor
import java.util.UUID


@ExperimentalMaterialApi
@OptIn(ExperimentalMaterialApi::class)
@SuppressLint("StateFlowValueCalledInComposition")
@Composable
fun BuyRecordBox(dollarViewModel: DollarViewModel,
                 snackbarHostState: SnackbarHostState) {

    val dateChangeIn = dollarViewModel.changeDateAction.collectAsState()

    val buyRecordHistory : State<List<DrBuyRecord>> =
        when(dateChangeIn.value) {
            1 -> { dollarViewModel.buyDayFilteredRecordFlow.collectAsState(initial = emptyList())}
            2 -> { dollarViewModel.buyRecordFlow.collectAsState()}
            3 -> { dollarViewModel.buyMonthFilterRecordFlow.collectAsState(initial = emptyList())}
            4 -> { dollarViewModel.buyYearFilterRecordFlow.collectAsState(initial = emptyList())}

            else -> { dollarViewModel.buyRecordFlow.collectAsState()}
        }


    val buySortRecord = buyRecordHistory.value.sortedBy { it.date }

    var selectedId by remember { mutableStateOf(UUID.randomUUID()) }




    Row(modifier = Modifier
        .fillMaxWidth()
        .height(55.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {

        RecordTextView(recordText = "날짜", 45.dp, 16, 2.5f, 0.dp, color = Color.Black)
        Spacer(modifier = Modifier.width(1.dp))
        RecordTextView(recordText = "매수달러\n" + "(매수금)", 45.dp, 16, 2.5f,  0.dp, color = Color.Black)
        Spacer(modifier = Modifier.width(1.dp))
        RecordTextView(recordText = "매수환율", 45.dp, 16, 2.5f,  0.dp, color = Color.Black)
        Spacer(modifier = Modifier.width(1.dp))
        RecordTextView(recordText = "예상수익", 45.dp, 16, 2.5f,  0.dp ,color = Color.Black)

    }

    Column {

        Spacer(modifier = Modifier
            .fillMaxWidth()
            .background(Color.Gray)
            .height(2.dp))



        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            state = rememberLazyListState()) {

            //  Buy -> 라인리코드텍스트에 넣지 말고 바로 데이터 전달 -> 리팩토리
            items(buySortRecord, {item -> item.id}) { Buy ->

                val dismissState = rememberDismissState()

                if (dismissState.isDismissed(DismissDirection.EndToStart)) {
                    dollarViewModel.removeBuyRecord(Buy)

                }
                SwipeToDismiss(
                    state = dismissState,
                    modifier = Modifier
                        .padding(vertical = Dp(1f)),
                    directions = setOf(
                        DismissDirection.EndToStart),
                    dismissThresholds = { FractionalThreshold(0.25f)},
                    background = {
                        val color by animateColorAsState(
                            when (dismissState.targetValue) {
                                DismissValue.Default -> Color.White
                                else -> DeleteColor
                            }
                        )
                        val alignment = Alignment.CenterEnd
                        val icon = Icons.Default.Delete

                        val scale by animateFloatAsState(
                            if (dismissState.targetValue == DismissValue.Default) 0.75f else 1f
                        )

                        Box(
                            Modifier
                                .fillMaxSize()
                                .background(color)
                                .padding(horizontal = Dp(20f)),
                            contentAlignment = alignment
                        ) {
                            Image(
                                icon,
                                contentDescription = "Delete Icon",
                                modifier = Modifier.scale(scale)
                            )
                        }
                    },
                    dismissContent = {


                        Card(
                            elevation = animateDpAsState(
                                if (dismissState.dismissDirection != null) 4.dp else 0.dp).value,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(Dp(50f))
                                .padding(0.dp)
                        ) {
                            LineRecordText(
                                Buy,
                                sellAction = Buy.recordColor!!
                                ,
                                sellActed = { buyRecord ->
                                    selectedId = buyRecord.id

                                    dollarViewModel.updateBuyRecord(buyRecord)

                                },
                                onClicked = { recordbox ->
                                    selectedId = recordbox.id
                                    dollarViewModel.dateFlow.value = recordbox.date!!
                                    dollarViewModel.haveMoneyDollar.value = recordbox.exchangeMoney!!
                                    dollarViewModel.recordInputMoney.value = recordbox.money!!.toInt() },
                                 dollarViewModel,
                                snackbarHostState = snackbarHostState)

                        }
                    }
                )
                Divider()
            }
        }


    }
}


@OptIn(ExperimentalMaterialApi::class)
@Composable
fun SellRecordBox(dollarViewModel: DollarViewModel) {

    val dateChangeIn = dollarViewModel.changeDateAction.collectAsState()

    val sellRecordHistory : State<List<DrSellRecord>> =
        when(dateChangeIn.value) {
            1 -> { dollarViewModel.sellDayFilteredRecordFlow.collectAsState(initial = emptyList())}
            2 -> { dollarViewModel.sellRecordFlow.collectAsState() }
            3 -> { dollarViewModel.sellMonthFilterRecordFlow.collectAsState(initial = emptyList())}
            4 -> { dollarViewModel.sellYearFilterRecordFlow.collectAsState(initial = emptyList())}

            else -> { dollarViewModel.sellRecordFlow.collectAsState()}
        }

    val sellSortRecord = sellRecordHistory.value.sortedBy { it.date }

    Row(modifier = Modifier
        .background(Color.White)
        .fillMaxWidth()
        .height(55.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {

        RecordTextView(recordText = "날짜", 45.dp, 16, 2.5f, 0.dp, color = Color.Black)
        Spacer(modifier = Modifier.width(1.dp))
        RecordTextView(recordText = "매도달러", 45.dp, 16, 2.5f,  0.dp, color = Color.Black)
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



        LazyColumn(modifier = Modifier) {

            items(sellSortRecord, {item -> item.id}) { Sell ->

                val dismissState = rememberDismissState()

                if (dismissState.isDismissed(DismissDirection.EndToStart)) {
                    dollarViewModel.removeSellRecord(Sell)
                }
                SwipeToDismiss(
                    state = dismissState,
                    modifier = Modifier
                        .padding(vertical = Dp(1f)),
                    dismissThresholds = { FractionalThreshold(0.25f)},
                    directions = setOf(
                        DismissDirection.EndToStart
                    ),
                    background = {
                        val color by animateColorAsState(
                            when (dismissState.targetValue) {
                                DismissValue.Default -> Color.White
                                else -> DeleteColor
                            }
                        )
                        val alignment = Alignment.CenterEnd
                        val icon = Icons.Default.Delete

                        val scale by animateFloatAsState(
                            if (dismissState.targetValue == DismissValue.Default) 0.75f else 1f
                        )

                        Box(
                            Modifier
                                .fillMaxSize()
                                .background(color)
                                .padding(horizontal = Dp(20f)),
                            contentAlignment = alignment
                        ) {
                            Image(
                                icon,
                                contentDescription = "Delete Icon",
                                modifier = Modifier.scale(scale)
                            )
                        }
                    },
                    dismissContent = {
                        Card(
                            elevation = animateDpAsState(
                                if (dismissState.dismissDirection != null) 4.dp else 0.dp
                            ).value,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(Dp(50f))
                                .align(alignment = Alignment.CenterVertically)
                        ) {
                            SellLineRecordText(Sell,
                                onClicked = { recordBox ->
                                    dollarViewModel.exchangeMoney.value = recordBox.exchangeMoney
                                    dollarViewModel.sellRateFlow.value = recordBox.rate
                                    dollarViewModel.sellDollarFlow.value = recordBox.money
                                }, dollarViewModel)

                        }
                    }
                )
                Divider()
            }
        }
    }
}
