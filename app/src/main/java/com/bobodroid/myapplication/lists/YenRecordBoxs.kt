package com.bobodroid.myapplication.lists

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import java.util.UUID

@ExperimentalMaterialApi
@OptIn(ExperimentalMaterialApi::class)
@SuppressLint("StateFlowValueCalledInComposition")
@Composable
fun BuyYenRecordBox(yenViewModel: YenViewModel, snackbarHostState: SnackbarHostState) {

    val buyRecordHistory : State<List<YenBuyRecord>> = yenViewModel.filterBuyRecordFlow.collectAsState()
    val buySortRecord = buyRecordHistory.value.sortedBy { it.date }

    var selectedId by remember { mutableStateOf(UUID.randomUUID()) }


    Row(modifier = Modifier
        .fillMaxWidth()
        .height(80.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {

        RecordTextView(recordText = "날짜", 45.dp, 16, 2.5f, 0.dp, color = Color.Black)
        Spacer(modifier = Modifier.width(1.dp))
        RecordTextView(recordText = "매수엔화\n" + "(매수금)", 45.dp, 16, 2.5f,  0.dp, color = Color.Black)
        Spacer(modifier = Modifier.width(1.dp))
        RecordTextView(recordText = "매수환율", 45.dp, 16, 2.5f,  0.dp, color = Color.Black)
        Spacer(modifier = Modifier.width(1.dp))
        RecordTextView(recordText = "예상수익\n" + "(개발예정)", 45.dp, 16, 2.5f,  0.dp ,color = Color.Black)

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

                LineYenRecordText(
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
                Divider()
            }
        }


    }
}


@OptIn(ExperimentalMaterialApi::class)
@Composable
fun SellYenRecordBox(yenViewModel: YenViewModel) {

    val sellRecordHistory : State<List<YenSellRecord>> = yenViewModel.filterSellRecordFlow.collectAsState()

    val sellSortRecord = sellRecordHistory.value.sortedBy { it.date }

    Row(modifier = Modifier
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
                SellLineYenRecordText(Sell,
                    onClicked = { recordBox ->
                        yenViewModel.exchangeMoney.value = recordBox.exchangeMoney
                        yenViewModel.sellRateFlow.value = recordBox.rate
                        yenViewModel.sellDollarFlow.value = recordBox.money
                    },
                    yenViewModel)
                Divider()
            }
        }
    }
}
