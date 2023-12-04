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
fun BuyRecordBox(yenViewModel: YenViewModel, snackbarHostState: SnackbarHostState) {

    val dateChangeIn = yenViewModel.changeDateAction.collectAsState()

    val buyRecordHistory : State<List<YenBuyRecord>> =
        when(dateChangeIn.value) {
            1 -> { yenViewModel.buyDayFilteredRecordFlow.collectAsState(initial = emptyList())}
            2 -> { yenViewModel.buyRecordFlow.collectAsState()}
            3 -> { yenViewModel.buyMonthFilterRecordFlow.collectAsState(initial = emptyList())}
            4 -> { yenViewModel.buyYearFilterRecordFlow.collectAsState(initial = emptyList())}

            else -> { yenViewModel.buyRecordFlow.collectAsState()}
        }

    val buySortRecord = buyRecordHistory.value.sortedBy { it.date }

    var selectedId by remember { mutableStateOf(UUID.randomUUID()) }


    Row(modifier = Modifier
        .background(Color.White)
        .fillMaxWidth()
        .height(80.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {

        RecordTextView(recordText = "날짜", 45.dp, 16, 2.5f, 0.dp, color = Color.Black)
        Spacer(modifier = Modifier.width(1.dp))
        RecordTextView(recordText = "매수금", 45.dp, 16, 2.5f,  0.dp, color = Color.Black)
        Spacer(modifier = Modifier.width(1.dp))
        RecordTextView(recordText = "매수환율", 45.dp, 16, 2.5f,  0.dp, color = Color.Black)
        Spacer(modifier = Modifier.width(1.dp))
        RecordTextView(recordText = "매수엔화", 45.dp, 16, 2.5f,  0.dp ,color = Color.Black)

    }

    Column(
        modifier = Modifier) {

        Spacer(modifier = Modifier
            .fillMaxWidth()
            .background(Color.Gray)
            .height(2.dp))



        LazyColumn(modifier = Modifier, state = rememberLazyListState()) {

            //  Buy -> 라인리코드텍스트에 넣지 말고 바로 데이터 전달 -> 리팩토리
            items(buySortRecord, {item -> item.id}) {Buy ->

                val dismissState = rememberDismissState()

                if (dismissState.isDismissed(DismissDirection.EndToStart)) {
                    yenViewModel.removeBuyRecord(Buy)

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
                                .align(alignment = Alignment.CenterVertically)
                        ) {
                            LineRecordText(
                                Buy,
                                sellAction = Buy.recordColor
                                ,
                                sellActed = { buyRecord ->
                                    selectedId = buyRecord.id

                                    yenViewModel.updateBuyRecord(buyRecord)

                                },
                                onClicked = { recordbox ->
                                    selectedId = recordbox.id
                                    yenViewModel.dateFlow.value = recordbox.date
                                    yenViewModel.haveMoney.value = recordbox.exchangeMoney.toInt()
                                    yenViewModel.recordInputMoney.value = recordbox.money.toInt() },
                                yenViewModel,
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
fun SellRecordBox(yenViewModel: YenViewModel) {

    val dateChangeIn = yenViewModel.changeDateAction.collectAsState()

    val sellRecordHistory : State<List<YenSellRecord>> =
        when(dateChangeIn.value) {
            1 -> { yenViewModel.sellDayFilteredRecordFlow.collectAsState(initial = emptyList())}
            2 -> { yenViewModel.sellRecordFlow.collectAsState() }
            3 -> { yenViewModel.sellMonthFilterRecordFlow.collectAsState(initial = emptyList())}
            4 -> { yenViewModel.sellYearFilterRecordFlow.collectAsState(initial = emptyList())}

            else -> { yenViewModel.sellRecordFlow.collectAsState()}
        }

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
        RecordTextView(recordText = "매도엔화", 45.dp, 16, 2.5f,  0.dp, color = Color.Black)
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
                    yenViewModel.removeSellRecord(Sell)
                }
                SwipeToDismiss(
                    state = dismissState,
                    modifier = Modifier
                        .padding(vertical = Dp(1f)),
                    directions = setOf(
                        DismissDirection.EndToStart
                    ),
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
                                if (dismissState.dismissDirection != null) 4.dp else 0.dp
                            ).value,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(Dp(50f))
                                .align(alignment = Alignment.CenterVertically)
                        ) {
                            SellLineRecordText(Sell,
                                onClicked = { recordBox ->
                                    yenViewModel.exchangeMoney.value = recordBox.exchangeMoney
                                    yenViewModel.sellRateFlow.value = recordBox.rate
                                    yenViewModel.sellDollarFlow.value = recordBox.money
                                }, yenViewModel)

                        }
                    }
                )
                Divider()
            }
        }
    }
}
