package com.bobodroid.myapplication.lists.dollorList

import android.annotation.SuppressLint
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
import com.bobodroid.myapplication.models.datamodels.*
import com.bobodroid.myapplication.models.viewmodels.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.UUID


@ExperimentalMaterialApi
@OptIn(ExperimentalMaterialApi::class)
@SuppressLint("StateFlowValueCalledInComposition")
@Composable
fun BuyRecordBox(dollarViewModel: DollarViewModel,
                 snackBarHostState: SnackbarHostState) {

    val buyRecordHistory : State<List<DrBuyRecord>> = dollarViewModel.filterBuyRecordFlow.collectAsState()

    val buySortRecord = buyRecordHistory.value.sortedBy { it.date }

    var selectedId by remember { mutableStateOf(UUID.randomUUID()) }

    var lazyScrollState = rememberLazyListState()

    val coroutineScope = rememberCoroutineScope()

    // 리스트 헤더
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

    //리스트 아이템
    Column {

        Spacer(modifier = Modifier
            .fillMaxWidth()
            .background(Color.Gray)
            .height(2.dp))

        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            state = lazyScrollState) {

            //  Buy -> 라인리코드텍스트에 넣지 말고 바로 데이터 전달 -> 리팩토리
            val listSize = buySortRecord.size

            itemsIndexed(
                items = buySortRecord,
                key = { index: Int, item: DrBuyRecord -> item.id}) { index, Buy ->

                LineDrRecordText(
                    Buy,
                    index = index,
                    listSize = listSize,
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
                        dollarViewModel.recordInputMoney.value = recordbox.money!! },
                    dollarViewModel = dollarViewModel,
                    snackBarHostState = snackBarHostState) {
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


@OptIn(ExperimentalMaterialApi::class)
@Composable
fun SellRecordBox(dollarViewModel: DollarViewModel, snackBarHostState: SnackbarHostState) {

    val sellRecordHistory = dollarViewModel.filterSellRecordFlow.collectAsState()

    val sellSortRecord = sellRecordHistory.value.sortedBy { it.date }

    var lazyScrollState = rememberLazyListState()

    val coroutineScope = rememberCoroutineScope()

    val deleteAskDialog = remember { mutableStateOf(false) }

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



        LazyColumn(modifier = Modifier, state = lazyScrollState) {

            val listSize = sellSortRecord.size

            itemsIndexed(
                items = sellSortRecord,
                key = { index: Int, item: DrSellRecord -> item.id}) { index, Sell ->

                SellLineDrRecordText(
                    Sell,
                    index = index,
                    listSize = listSize,
                    dollarViewModel = dollarViewModel,
                    snackbarHostState = snackBarHostState) {
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