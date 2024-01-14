package com.bobodroid.myapplication.lists.wonList

import android.annotation.SuppressLint
import android.util.Log
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.ExperimentalMaterialApi
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
import com.bobodroid.myapplication.components.RecordHeader
import com.bobodroid.myapplication.components.RecordTextView
import com.bobodroid.myapplication.models.datamodels.WonBuyRecord
import com.bobodroid.myapplication.models.datamodels.YenBuyRecord
import com.bobodroid.myapplication.models.viewmodels.WonViewModel
import com.bobodroid.myapplication.screens.TAG
import java.util.UUID

@OptIn(ExperimentalFoundationApi::class)
@ExperimentalMaterialApi
@SuppressLint("StateFlowValueCalledInComposition")
@Composable
fun TotalWonRecordBox(wonViewModel: WonViewModel,
                      snackbarHostState: SnackbarHostState,
                      hideSellRecordState: Boolean) {

    val buyRecordHistory : State<Map<String, List<WonBuyRecord>>> = wonViewModel.groupBuyRecordFlow.collectAsState()

    val filterRecord  = if(hideSellRecordState)
    {
        buyRecordHistory.value.mapValues { it.value.filter { it.recordColor == false } }
    } else {
        buyRecordHistory.value
    }

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

        RecordTextView(recordText = "매수날짜\n " + "(매도날짜)", 45.dp, 16, 2.5f, 0.dp, color = Color.Black)
        Spacer(modifier = Modifier.width(1.dp))
        RecordTextView(recordText = "매수원화\n" + "(매수금)", 45.dp, 16, 2.5f,  0.dp, color = Color.Black)
        Spacer(modifier = Modifier.width(1.dp))
        RecordTextView(recordText = "매수환율\n" + "(매도환율)", 45.dp, 16, 2.5f,  0.dp, color = Color.Black)
        Spacer(modifier = Modifier.width(1.dp))
        RecordTextView(recordText = "예상수익\n " + "(확정수익)", 45.dp, 16, 2.5f,  0.dp ,color = Color.Black)

    }

    Column(
        modifier = Modifier
    ) {

        Spacer(modifier = Modifier
            .fillMaxWidth()
            .background(Color.Gray)
            .height(2.dp))



        LazyColumn(
            modifier = Modifier,
            state = lazyScrollState) {

            filterRecord.forEach { key, items ->

                stickyHeader {
                    RecordHeader(key = key)
                }

                itemsIndexed(
                    items = items,
                    key = { index: Int, item: WonBuyRecord -> item.id}
                ) {index, Buy ->

                    TotalLineWonRecordText(
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
}