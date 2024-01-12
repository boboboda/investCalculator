package com.bobodroid.myapplication.lists.yenList

import android.annotation.SuppressLint
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.UUID

@ExperimentalMaterialApi
@SuppressLint("StateFlowValueCalledInComposition")
@Composable
fun EditTypeYenRecordBox(yenViewModel: YenViewModel,
                    snackbarHostState: SnackbarHostState
                    ,hideSellRecordState: Boolean) {

    val buyRecordHistory : State<List<YenBuyRecord>> = yenViewModel.filterBuyRecordFlow.collectAsState()

    val filterRecord  = if(hideSellRecordState)
    {
        buyRecordHistory.value.filter { it.recordColor == false }
    } else {
        buyRecordHistory.value
    }

    var selectedId by remember { mutableStateOf(UUID.randomUUID()) }

    var lazyScrollState = rememberLazyListState()

    val coroutineScope = rememberCoroutineScope()


    Row(modifier = Modifier
        .fillMaxWidth()
        .height(55.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {

        RecordTextView(recordText = "매수날짜\n " + "(매도날짜)", 45.dp, 16, 2.5f, 0.dp, color = Color.Black)
        Spacer(modifier = Modifier.width(1.dp))
        RecordTextView(recordText = "매수앤화\n" + "(매수금)", 45.dp, 16, 2.5f,  0.dp, color = Color.Black)
        Spacer(modifier = Modifier.width(1.dp))
        RecordTextView(recordText = "매수환율\n" + "(매도환율)", 45.dp, 16, 2.5f,  0.dp, color = Color.Black)
        Spacer(modifier = Modifier.width(1.dp))
        RecordTextView(recordText = "예상수익\n " + "(확정수익)", 45.dp, 16, 2.5f,  0.dp ,color = Color.Black)

    }

    Column(
        modifier = Modifier) {

        Spacer(modifier = Modifier
            .fillMaxWidth()
            .background(Color.Gray)
            .height(2.dp))



        LazyColumn(
            modifier = Modifier,
            state = lazyScrollState) {

            //  Buy -> 라인리코드텍스트에 넣지 말고 바로 데이터 전달 -> 리팩토리

            val listSize = filterRecord.size


            itemsIndexed(
                items = filterRecord,
                key = { index: Int, item: YenBuyRecord -> item.id}) { index, Buy ->



                EditTypeLineYenRecordText(
                    Buy,
                    listSize = listSize,
                    index = index,
                    sellAction = Buy.recordColor!!
                    ,
                    sellActed = { buyRecord ->
                        // 매도시 컬러 변경
                        selectedId = buyRecord.id

                        yenViewModel.updateBuyRecord(buyRecord)

                    },
                    onClicked = { recordBox ->
                        // 매도시 값 인계
                        selectedId = recordBox .id
                        yenViewModel.dateFlow.value = recordBox.date!!
                        yenViewModel.haveMoney.value = recordBox.exchangeMoney!!
                        yenViewModel.recordInputMoney.value = recordBox.money!!},
                    yenViewModel,
                    snackbarHostState = snackbarHostState,
                    recordSelected =  {
                        coroutineScope.launch {
                            if(index <= listSize - 7) {
                                delay(300)
                                lazyScrollState.animateScrollToItem(index)
                            } else {
                                delay(300)
                                lazyScrollState.animateScrollToItem(index, 0)
                            }

                        }
                    })
                Divider()
            }
        }


    }
}
