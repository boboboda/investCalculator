package com.bobodroid.myapplication.lists.dollorList

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.DismissDirection
import androidx.compose.material.DismissValue
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.FractionalThreshold
import androidx.compose.material.SnackbarDuration
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.bobodroid.myapplication.extensions.*
import com.bobodroid.myapplication.models.viewmodels.DollarViewModel
import androidx.compose.material.SnackbarHostState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.ui.draw.scale
import androidx.compose.ui.unit.Dp
import com.bobodroid.myapplication.ui.theme.DeleteColor
import androidx.compose.material.SwipeToDismiss
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.rememberDismissState
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color.Companion.Black
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.sp
import com.bobodroid.myapplication.MainActivity.Companion.TAG
import com.bobodroid.myapplication.components.Dialogs.AskTriggerDialog
import com.bobodroid.myapplication.components.RecordTextView
import com.bobodroid.myapplication.models.datamodels.roomDb.DrSellRecord
import com.bobodroid.myapplication.ui.theme.TopButtonColor
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode




@OptIn(ExperimentalMaterialApi::class, ExperimentalMaterial3Api::class)
@Composable
fun SellLineDrRecordText(
    data: DrSellRecord,
    listSize: Int,
    index: Int,
    dollarViewModel: DollarViewModel,
    snackbarHostState: SnackbarHostState,
    recordSelected: () -> Unit
) {

    val deleteAskDialog = remember { mutableStateOf(false) }

    val dismissState = rememberDismissState()

    val mathContext = MathContext(28, RoundingMode.HALF_UP)

    val profitColor = if (BigDecimal(data.exchangeMoney, mathContext).signum() == -1) {
        Color.Blue
    } else {
        Color.Red
    }

    var itemRowVisible by remember { mutableStateOf(false) }

    var itemRowPosition by remember { mutableStateOf(false) }

    if (index <= listSize - 7) {
        itemRowPosition = true
    } else {
        itemRowPosition = false
    }

    val focusManager = LocalFocusManager.current

    var dropdownExpanded by remember { mutableStateOf(false) }

    var memoTextInput by remember { mutableStateOf("") }

    val coroutineScope = rememberCoroutineScope()

    var isTextFieldFocused = false

    LaunchedEffect(key1 = data.sellDrMemo, block = {
        memoTextInput = data.sellDrMemo!!
    })

    val focusRequester by remember { mutableStateOf(FocusRequester()) }

    if (dismissState.isDismissed(DismissDirection.StartToEnd))
        LaunchedEffect(key1 = Unit, block = {
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
                    .wrapContentHeight()
                    .focusRequester(focusRequester = focusRequester),
                shape = RoundedCornerShape(0.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White,
                    contentColor = Color.Black
                ),
            ) {

                if (!itemRowPosition) {

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
                                                        text = "매도 취소",
                                                        fontSize = 13.sp
                                                    )
                                                }
                                            }, onClick = {
                                                coroutineScope.launch {
                                                    val result =
                                                        dollarViewModel.cancelSellRecord(data.id)
                                                    if (result.first) {
                                                        dropdownExpanded = false
                                                        if (snackbarHostState.currentSnackbarData == null) {
                                                            coroutineScope.launch {
                                                                snackbarHostState.showSnackbar(
                                                                    "매도가 취소되었습니다.",
                                                                    actionLabel = "닫기",
                                                                    SnackbarDuration.Short
                                                                )
                                                                dollarViewModel.removeSellRecord(data)
                                                            }
                                                        }

                                                    } else {
                                                        dropdownExpanded = false
                                                        if (snackbarHostState.currentSnackbarData == null) {
                                                            coroutineScope.launch {
                                                                snackbarHostState.showSnackbar(
                                                                    "일치하는 매수기록이 없습니다.",
                                                                    actionLabel = "닫기",
                                                                    SnackbarDuration.Short
                                                                )
                                                            }
                                                        }
                                                    }
                                                }
                                            })
                                    }
                                }


                            }

                            OutlinedTextField(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .focusRequester(focusRequester = focusRequester)
//                                .onFocusChanged {
//                                    isTextFieldFocused = it.isFocused
//                                }
                                ,
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
                                        if (snackbarHostState.currentSnackbarData == null) {
                                            coroutineScope.launch {
                                                snackbarHostState.showSnackbar(
                                                    "100자 이하로 작성해주세요",
                                                    actionLabel = "닫기", SnackbarDuration.Short
                                                )
                                            }
                                        }
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
                                    focusedBorderColor = Black,
                                    unfocusedBorderColor = Black
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
                                        val updateData = data.copy(sellDrMemo = memoTextInput)
                                        focusManager.clearFocus()
                                        dollarViewModel.sellDrMemoUpdate(updateData) { result ->
                                            if (result) {
                                                if (snackbarHostState.currentSnackbarData == null) {
                                                    coroutineScope.launch {
                                                        snackbarHostState.showSnackbar(
                                                            "성공적으로 저장되었습니다.",
                                                            actionLabel = "닫기",
                                                            SnackbarDuration.Short
                                                        )
                                                    }
                                                }

                                            } else {
                                                if (snackbarHostState.currentSnackbarData == null) {
                                                    coroutineScope.launch {
                                                        snackbarHostState.showSnackbar(
                                                            "저장에 실패하였습니다.",
                                                            actionLabel = "닫기",
                                                            SnackbarDuration.Short
                                                        )
                                                    }
                                                }
                                            }
                                        }
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
//                                        memoTextInput = data.buyDrMemo
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

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                if (itemRowVisible == false) {
                                    itemRowVisible = true
                                    recordSelected.invoke()
                                } else {
                                    focusManager.clearFocus()
                                }
                            }
                            .padding(top = 7.dp, bottom = 7.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        RecordTextView(
                            recordText = "${data.date}",
                            TextHeight = 40.dp,
                            13,
                            2.5f,
                            bottonPpaing = 0.dp,
                            color = Color.Black
                        )
                        Spacer(modifier = Modifier.width(1.dp))
                        RecordTextView(
                            recordText = "${BigDecimal(data.money, mathContext).toBigDecimalUs()}",
                            TextHeight = 40.dp,
                            13,
                            2.5f,
                            bottonPpaing = 0.dp,
                            color = Color.Black
                        )
                        Spacer(modifier = Modifier.width(1.dp))
                        RecordTextView(
                            recordText = "${data.rate}",
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
                                ).toBigDecimalWon()
                            }",
                            TextHeight = 40.dp,
                            13,
                            2.5f,
                            bottonPpaing = 0.dp,
                            color = profitColor
                        )
                    }
                } else {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                if (itemRowVisible == false) {
                                    itemRowVisible = true
                                    recordSelected.invoke()
                                } else {
                                    focusManager.clearFocus()
                                }
                            }
                            .padding(top = 7.dp, bottom = 7.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        RecordTextView(
                            recordText = "${data.date}",
                            TextHeight = 40.dp,
                            13,
                            2.5f,
                            bottonPpaing = 0.dp,
                            color = Color.Black
                        )
                        Spacer(modifier = Modifier.width(1.dp))
                        RecordTextView(
                            recordText = "${BigDecimal(data.money, mathContext).toBigDecimalUs()}",
                            TextHeight = 40.dp,
                            13,
                            2.5f,
                            bottonPpaing = 0.dp,
                            color = Color.Black
                        )
                        Spacer(modifier = Modifier.width(1.dp))
                        RecordTextView(
                            recordText = "${data.rate}",
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
                                ).toBigDecimalWon()
                            }",
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
                                                        text = "매도 취소",
                                                        fontSize = 13.sp
                                                    )
                                                }
                                            }, onClick = {
                                                coroutineScope.launch {
                                                    val result =
                                                        dollarViewModel.cancelSellRecord(data.id)
                                                    if (result.first) {
                                                        dropdownExpanded = false
                                                        if (snackbarHostState.currentSnackbarData == null) {
                                                            coroutineScope.launch {
                                                                snackbarHostState.showSnackbar(
                                                                    "매도가 취소되었습니다.",
                                                                    actionLabel = "닫기",
                                                                    SnackbarDuration.Short
                                                                )
                                                                dollarViewModel.removeSellRecord(data)
                                                            }
                                                        }

                                                    } else {
                                                        dropdownExpanded = false
                                                        if (snackbarHostState.currentSnackbarData == null) {
                                                            coroutineScope.launch {
                                                                snackbarHostState.showSnackbar(
                                                                    "일치하는 매수기록이 없습니다.",
                                                                    actionLabel = "닫기",
                                                                    SnackbarDuration.Short
                                                                )
                                                            }
                                                        }
                                                    }
                                                }
                                            })
                                    }
                                }


                            }

                            OutlinedTextField(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .focusRequester(focusRequester = focusRequester)
                                ,
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
                                        if (snackbarHostState.currentSnackbarData == null) {
                                            coroutineScope.launch {
                                                snackbarHostState.showSnackbar(
                                                    "100자 이하로 작성해주세요",
                                                    actionLabel = "닫기", SnackbarDuration.Short
                                                )
                                            }
                                        }
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
                                    focusedBorderColor = Black,
                                    unfocusedBorderColor = Black
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
                                        val updateData = data.copy(sellDrMemo = memoTextInput)
                                        focusManager.clearFocus()
                                        dollarViewModel.sellDrMemoUpdate(updateData) { result ->
                                            if (result) {
                                                if (snackbarHostState.currentSnackbarData == null) {
                                                    coroutineScope.launch {
                                                        snackbarHostState.showSnackbar(
                                                            "성공적으로 저장되었습니다.",
                                                            actionLabel = "닫기",
                                                            SnackbarDuration.Short
                                                        )
                                                    }
                                                }

                                            } else {
                                                if (snackbarHostState.currentSnackbarData == null) {
                                                    coroutineScope.launch {
                                                        snackbarHostState.showSnackbar(
                                                            "저장에 실패하였습니다.",
                                                            actionLabel = "닫기",
                                                            SnackbarDuration.Short
                                                        )
                                                    }
                                                }
                                            }
                                        }
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
//                                        memoTextInput = data.buyDrMemo
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


            }
            if (deleteAskDialog.value) {


                AskTriggerDialog(
                    title = "삭제하시겠습니까?",
                    onClickedLabel = "예",
                    onDismissRequest = {
                        deleteAskDialog.value = it
                    }) {
                    dollarViewModel.removeSellRecord(data)
                }

            }

        }
    )

}

