package com.bobodroid.myapplication.lists.dollorList

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.DismissDirection
import androidx.compose.material.DismissValue
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.FractionalThreshold
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.SnackbarDuration
import androidx.compose.material.SnackbarHostState
import androidx.compose.material.SwipeToDismiss
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.rememberDismissState
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bobodroid.myapplication.MainActivity.Companion.TAG
import com.bobodroid.myapplication.components.Dialogs.AskTriggerDialog
import com.bobodroid.myapplication.components.Dialogs.TextFieldDialog
import com.bobodroid.myapplication.components.RecordTextView
import com.bobodroid.myapplication.extensions.toBigDecimalUs
import com.bobodroid.myapplication.extensions.toBigDecimalWon
import com.bobodroid.myapplication.models.datamodels.roomDb.CurrencyType
import com.bobodroid.myapplication.models.datamodels.roomDb.DrBuyRecord
import com.bobodroid.myapplication.models.datamodels.roomDb.ForeignCurrencyRecord
import com.bobodroid.myapplication.models.datamodels.roomDb.YenBuyRecord
import com.bobodroid.myapplication.models.viewmodels.MainViewModel
import com.bobodroid.myapplication.screens.MainEvent
import com.bobodroid.myapplication.screens.RecordListEvent
import com.bobodroid.myapplication.ui.theme.DeleteColor
import com.bobodroid.myapplication.ui.theme.SelectedColor
import com.bobodroid.myapplication.ui.theme.TopButtonColor
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun RecordListRowView(
    data: ForeignCurrencyRecord,
    sellState: Boolean = data.recordColor!!,
    groupList: List<String>,
    onEvent: (RecordListEvent) -> Unit,
    scrollEvent: () -> Unit
) {


    val mathContext = MathContext(28, RoundingMode.HALF_UP)

//    var openDialog by remember { mutableStateOf(false) }

    var itemRowVisible by remember { mutableStateOf(false) }

    val focusManager = LocalFocusManager.current

    val deleteAskDialog = remember { mutableStateOf(false) }

    val dismissState = rememberDismissState()

    var dropdownExpanded by remember { mutableStateOf(false) }

    var groupDropdownExpanded by remember { mutableStateOf(false) }

    var memoTextInput by remember { mutableStateOf("") }

    val coroutineScope = rememberCoroutineScope()

    var groupAddDialog by remember { mutableStateOf(false) }

    val focusRequester by remember { mutableStateOf(FocusRequester()) }

    var insertDialog by remember { mutableStateOf(false) }


    val profit = if (!data.recordColor!!) {
        if (data.profit.isNullOrEmpty()) {
            "0"
        } else {
            data.profit
        }
    } else {
        if (data.sellProfit.isNullOrEmpty()) {
            "0"
        } else {
            data.sellProfit
        }

    }

    val profitColor = if (profit == "") {
        Color.Black
    } else {
        if (BigDecimal(profit, mathContext).signum() == -1) {
            Color.Blue
        } else {
            Color.Red
        }
    }

    val date = if (data.recordColor!!) {
        "${data.date}\n (${data.sellDate ?: "데이터없음"})"
    } else {
        data.date
    }

    val rate = if (data.recordColor!!) {
        "${data.rate}\n (${data.sellRate ?: "데이터없음"})"
    } else {
        data.rate
    }

    LaunchedEffect(key1 = data.memo, block = {
        memoTextInput = data.memo ?: ""
    })

    if (dismissState.isDismissed(DismissDirection.StartToEnd))
        LaunchedEffect(key1 = Unit, block = {
            Log.d(TAG("TotalLineDrRecord",""), "스와이프 이벤트")

            dismissState.reset()
            deleteAskDialog.value = true
        })


    SwipeToDismiss(
        state = dismissState,
        modifier = Modifier,
        directions = setOf(
            DismissDirection.StartToEnd
        ),
        dismissThresholds = { FractionalThreshold(0.40f) },
        background = {
            val color by animateColorAsState(
                when (dismissState.targetValue) {
                    DismissValue.Default -> Color.White
                    else -> DeleteColor
                }
            )
            val alignment = Alignment.CenterStart
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
                modifier = Modifier
                    .wrapContentHeight(),
                shape = RoundedCornerShape(0.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (sellState) SelectedColor else Color.White,
                    contentColor = Color.Black
                ),
            ) {


                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            if (itemRowVisible == false) {
                                coroutineScope.launch {
                                    itemRowVisible = true
                                    scrollEvent()
                                }
                            } else {
                                focusManager.clearFocus()
                            }
                        }
                        .padding(top = 7.dp, bottom = 7.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center) {
                    RecordTextView(
                        recordText = "${date}",
                        TextHeight = 40.dp,
                        13,
                        2.5f,
                        bottonPpaing = 0.dp,
                        color = Color.Black
                    )

                    Spacer(modifier = Modifier.width(1.dp))

                    RecordTextView(
                        recordText = "${
                            BigDecimal(
                                data.exchangeMoney,
                                mathContext
                            ).toBigDecimalUs()!!
                        }\n (${BigDecimal(data.money, mathContext).toBigDecimalWon()})",
                        TextHeight = 40.dp,
                        13,
                        2.5f,
                        bottonPpaing = 0.dp,
                        color = Color.Black
                    )

                    Spacer(modifier = Modifier.width(1.dp))

                    RecordTextView(
                        recordText = "${rate}",
                        TextHeight = 40.dp,
                        13,
                        2.5f,
                        bottonPpaing = 0.dp,
                        color = Color.Black
                    )

                    Spacer(modifier = Modifier.width(1.dp))

                    RecordTextView(
                        recordText = "${BigDecimal(profit, mathContext).toBigDecimalWon()}",
                        TextHeight = 40.dp,
                        13,
                        2.5f,
                        bottonPpaing = 0.dp,
                        color = profitColor
                    )
                }

                AnimatedVisibility(visible = itemRowVisible) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 32.dp, end = 30.dp)
                            .wrapContentHeight()
                    ) {
                        Row(
                            modifier = Modifier
                                .wrapContentSize()
                        ) {
                            Card(
                                modifier = Modifier
                                    .padding(bottom = 10.dp),
                                colors = CardDefaults.cardColors(TopButtonColor),
                                elevation = CardDefaults.cardElevation(8.dp),
                                shape = RoundedCornerShape(2.dp)
                            ) {
                                Text(
                                    text = "메모",
                                    modifier = Modifier
                                        .padding(all = 5.dp)
                                        .padding(horizontal = 3.dp)
                                )
                            }

                            Spacer(modifier = Modifier.weight(1f))


                            Box(
                                modifier = Modifier
                                    .wrapContentSize(Alignment.TopEnd)
                            ) {
                                Icon(
                                    modifier = Modifier
                                        .clickable {
                                            if (!dropdownExpanded) dropdownExpanded =
                                                true else dropdownExpanded = false
                                        },
                                    imageVector = Icons.Rounded.MoreVert,
                                    contentDescription = "메뉴",
                                    tint = Color.Black
                                )
                                DropdownMenu(
                                    offset = DpOffset(x = 0.dp, y = 10.dp),
                                    expanded = dropdownExpanded,
                                    onDismissRequest = {
                                        dropdownExpanded = false
                                    }
                                ) {

                                    DropdownMenuItem(
                                        modifier = Modifier
                                            .fillMaxWidth(),
                                        text = {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth(),
                                                contentAlignment = Alignment.TopStart
                                            ) {
                                                Text(
                                                    text = "매도",
                                                    fontSize = 13.sp
                                                )
                                            }
                                        }, onClick = {
                                            dropdownExpanded = false
                                            if (!data.recordColor!!) {
                                                onEvent(RecordListEvent.SellRecord(data))
                                            } else {
                                                onEvent(RecordListEvent.SnackBarEvent("매도한 기록입니다."))
                                            }
                                        })


                                    DropdownMenuItem(
                                        modifier = Modifier
                                            .fillMaxWidth(),
                                        text = {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth(),
                                                contentAlignment = Alignment.TopStart
                                            ) {
                                                Text(
                                                    text = "매수 수정",
                                                    fontSize = 13.sp
                                                )
                                            }
                                        },
                                        onClick = {
                                            dropdownExpanded = false
                                            onEvent(RecordListEvent.ShowEditBottomSheet(data))
                                        })


                                    DropdownMenuItem(
                                        modifier = Modifier
                                            .fillMaxWidth(),
                                        text = {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth(),
                                                contentAlignment = Alignment.TopStart
                                            ) {
                                                Text(
                                                    text = "매도 취소",
                                                    fontSize = 13.sp
                                                )
                                            }
                                        },
                                        onClick = {
                                            coroutineScope.launch {
                                                dropdownExpanded = false
                                                if (data.recordColor == false) {
                                                    onEvent(RecordListEvent.SnackBarEvent("매도한 기록이 없습니다."))
                                                } else {
                                                    onEvent(RecordListEvent.CancelSellRecord(data.id))
                                                }


                                            }
                                        })


                                    DropdownMenuItem(
                                        modifier = Modifier
                                            .fillMaxWidth(),
                                        text = {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth(),
                                                contentAlignment = Alignment.TopStart
                                            ) {
                                                Text(
                                                    text = "그룹 변경",
                                                    fontSize = 13.sp
                                                )
                                            }
                                        }, onClick = {
                                            groupDropdownExpanded = true
                                        })
                                }

                                DropdownMenu(
                                    scrollState = rememberScrollState(),
                                    modifier = Modifier
                                        .wrapContentHeight()
                                        .heightIn(max = 160.dp),
                                    offset = DpOffset(x = 115.dp, y = 10.dp),
                                    expanded = groupDropdownExpanded,
                                    onDismissRequest = {
                                        groupDropdownExpanded = false
                                    }
                                ) {

                                    DropdownMenuItem(
                                        modifier = Modifier
                                            .fillMaxWidth(),
                                        text = {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth(),
                                                contentAlignment = Alignment.TopStart
                                            ) {
                                                Text(
                                                    text = "새그룹",
                                                    color = Color.Blue,
                                                    fontSize = 13.sp
                                                )
                                            }
                                        }, onClick = {
                                            groupAddDialog = true
                                        })

                                    Divider(
                                        Modifier
                                            .fillMaxWidth(),
                                        color = Color.Gray,
                                        thickness = 2.dp
                                    )

                                    groupList.forEach { groupName ->
                                        DropdownMenuItem(
                                            modifier = Modifier
                                                .fillMaxWidth(),
                                            text = {
                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxWidth(),
                                                    contentAlignment = Alignment.TopStart
                                                ) {
                                                    Text(
                                                        text = groupName,
                                                        fontSize = 13.sp
                                                    )
                                                }
                                            }, onClick = {
                                                onEvent(RecordListEvent.UpdateRecordCategory(data, groupName))
                                                dropdownExpanded = false
                                                groupDropdownExpanded = false
                                            })
                                    }

                                }
                            }


                        }

                        OutlinedTextField(
                            modifier = Modifier
                                .fillMaxWidth()
                                .focusRequester(focusRequester = focusRequester)
                                .onFocusChanged {
                                },
                            placeholder = {
                                Text(
                                    text = "메모를 입력해주세요",
                                    fontSize = 15.sp
                                )
                            },
                            value = memoTextInput,
                            onValueChange = {

                                if (memoTextInput.length > 100) {
                                    focusManager.clearFocus()
                                    onEvent(RecordListEvent.SnackBarEvent("100자 이하로 작성해주세요"))
                                } else {
                                    memoTextInput = it
                                }

                            },
                            textStyle = TextStyle(
                                baselineShift = BaselineShift.None,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Medium,
                                textAlign = TextAlign.Start
                            ),
                            colors = TextFieldDefaults.outlinedTextFieldColors(
                                focusedBorderColor = Color.Black,
                                unfocusedBorderColor = Color.Black
                            )
                        )

                        Row(
                            modifier = Modifier
                                .wrapContentSize(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {

                            Text(text = "limit: ${memoTextInput.length}/100자")

                            Spacer(modifier = Modifier.weight(1f))

                            Card(modifier = Modifier
                                .padding(bottom = 10.dp),
                                colors = CardDefaults.cardColors(TopButtonColor),
                                elevation = CardDefaults.cardElevation(8.dp),
                                shape = RoundedCornerShape(2.dp),
                                onClick = {

                                    onEvent(RecordListEvent.MemoUpdate(data, memoTextInput))
                                    focusManager.clearFocus()
//                                    dollarViewModel.buyDrMemoUpdate(updateData) { result ->
//                                        if (result) {
//                                            if (snackBarHostState.currentSnackbarData == null) {
//                                                coroutineScope.launch {
//                                                    snackBarHostState.showSnackbar(
//                                                        "성공적으로 저장되었습니다.",
//                                                        actionLabel = "닫기", SnackbarDuration.Short
//                                                    )
//                                                }
//                                            }
//
//                                        } else {
//                                            if (snackBarHostState.currentSnackbarData == null) {
//                                                coroutineScope.launch {
//                                                    snackBarHostState.showSnackbar(
//                                                        "저장에 실패하였습니다.",
//                                                        actionLabel = "닫기", SnackbarDuration.Short
//                                                    )
//                                                }
//                                            }
//                                        }
//                                    }
                                }) {
                                Text(
                                    text = "저장",
                                    modifier = Modifier
                                        .padding(all = 5.dp)
                                        .padding(horizontal = 3.dp)
                                )
                            }

                            Card(modifier = Modifier
                                .padding(bottom = 10.dp),
                                colors = CardDefaults.cardColors(TopButtonColor),
                                elevation = CardDefaults.cardElevation(8.dp),
                                shape = RoundedCornerShape(2.dp),
                                onClick = {
                                    itemRowVisible = false
                                    coroutineScope.launch {
                                        delay(500)
                                        memoTextInput = data.memo!!
                                    }

                                }) {
                                Text(
                                    text = "닫기",
                                    modifier = Modifier
                                        .padding(all = 5.dp)
                                        .padding(horizontal = 3.dp)
                                )
                            }
                        }
                    }
                }


            }



            if (groupAddDialog) {
                TextFieldDialog(
                    onDismissRequest = {
                        groupAddDialog = it
                    },
                    placeholder = "새 그룹명을 작성해주세요",
                    onClickedLabel = "추가",
                    closeButtonLabel = "닫기",
                    onClicked = { name ->
                        onEvent(RecordListEvent.AddGroup(data, name))
                        groupAddDialog = false
                        groupDropdownExpanded = false
                        dropdownExpanded = false
                    })
            }

            if (deleteAskDialog.value) {
                AskTriggerDialog(
                    title = "삭제하시겠습니까?",
                    onClickedLabel = "예",
                    onDismissRequest = {
                        deleteAskDialog.value = it
                    }) {
                    onEvent(RecordListEvent.RemoveRecord(data))
                }

            }

        }
    )

}

