package com.bobodroid.myapplication.lists.dollorList

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.SnackbarHostState
import androidx.compose.material3.Divider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.bobodroid.myapplication.components.RecordTextView
import com.bobodroid.myapplication.models.datamodels.DrBuyRecord
import com.bobodroid.myapplication.models.viewmodels.DollarViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.UUID

@Composable
fun EditTypeRecordBox(
    dollarViewModel: DollarViewModel,
    snackBarHostState: SnackbarHostState,
    hideSellRecordState: Boolean
) {

    val buyRecordHistory : State<List<DrBuyRecord>> = dollarViewModel.filterBuyRecordFlow.collectAsState()

    val filterRecord  = if(hideSellRecordState)
    {
        buyRecordHistory.value.filter { it.recordColor == false }
    } else {
        buyRecordHistory.value
    }

    var selectedId by remember { mutableStateOf(UUID.randomUUID()) }

    var lazyScrollState = rememberLazyListState()

    val columnScrollState = rememberScrollState()

    val coroutineScope = rememberCoroutineScope()

    Row(modifier = Modifier
        .fillMaxWidth()
        .height(55.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        RecordTextView(recordText = "매수날짜\n " + "(매도날짜)", 45.dp, 16, 2.5f, 0.dp, color = Color.Black)
        Spacer(modifier = Modifier.width(1.dp))
        RecordTextView(recordText = "매수달러\n" + "(매수금)", 45.dp, 16, 2.5f,  0.dp, color = Color.Black)
        Spacer(modifier = Modifier.width(1.dp))
        RecordTextView(recordText = "매수환율\n" + "(매도환율)", 45.dp, 16, 2.5f,  0.dp, color = Color.Black)
        Spacer(modifier = Modifier.width(1.dp))
        RecordTextView(recordText = "예상수익\n " + "(확정수익)", 45.dp, 16, 2.5f,  0.dp ,color = Color.Black)
    }


    //리스트 아이템

    Column(
    ) {

        Spacer(modifier = Modifier
            .fillMaxWidth()
            .background(Color.Gray)
            .height(2.dp))

        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            state = lazyScrollState) {

            //  Buy -> 라인리코드텍스트에 넣지 말고 바로 데이터 전달 -> 리팩토리
            val listSize = filterRecord.size
            itemsIndexed(
                items = filterRecord,
                key = { index: Int, item: DrBuyRecord -> item.id!! }
            ) { index, Buy ->

                EditLineDrRecord(
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