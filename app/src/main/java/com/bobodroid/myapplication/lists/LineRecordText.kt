@file:OptIn(ExperimentalMaterial3Api::class)

package com.bobodroid.myapplication.lists

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
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
import com.bobodroid.myapplication.models.datamodels.*
import com.bobodroid.myapplication.models.viewmodels.DollarViewModel
import com.bobodroid.myapplication.models.viewmodels.WonViewModel
import com.bobodroid.myapplication.models.viewmodels.YenViewModel
import com.bobodroid.myapplication.ui.theme.SelectedColor
import androidx.compose.material.SnackbarHostState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.ui.draw.scale
import androidx.compose.ui.unit.Dp
import com.bobodroid.myapplication.components.Dialogs.DeleteDialog
import com.bobodroid.myapplication.components.Dialogs.SellDialog
import com.bobodroid.myapplication.components.Dialogs.WonSellDialog
import com.bobodroid.myapplication.components.Dialogs.YenSellDialog
import com.bobodroid.myapplication.ui.theme.DeleteColor
import androidx.compose.material.SwipeToDismiss
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.rememberDismissState
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color.Companion.Black
import androidx.compose.ui.graphics.Color.Companion.Yellow
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.modifier.modifierLocalMapOf
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.sp
import com.bobodroid.myapplication.MainActivity.Companion.TAG
import com.bobodroid.myapplication.components.RecordTextView
import com.bobodroid.myapplication.ui.theme.Green
import com.bobodroid.myapplication.ui.theme.TopButtonColor
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode
import java.util.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun LineDrRecordText(
    data: DrBuyRecord,
    sellAction: Boolean = data.recordColor!!,
    sellActed: (DrBuyRecord) -> Unit,
    onClicked: ((DrBuyRecord)-> Unit)?,
    dollarViewModel: DollarViewModel,
    snackBarHostState: SnackbarHostState) {

    val mathContext = MathContext(28, RoundingMode.HALF_UP)

    var openDialog by remember { mutableStateOf(false) }

    var itemRowVisible by remember { mutableStateOf(false) }

    val deleteAskDialog = remember { mutableStateOf(false) }

    val dismissState = rememberDismissState()

    var dropdownExpanded by remember { mutableStateOf(false) }

    var memoTextInput by remember { mutableStateOf("") }

    val coroutineScope = rememberCoroutineScope()

    val focusManager = LocalFocusManager.current

    var isTextFieldFocused = false

    val focusRequester by remember { mutableStateOf(FocusRequester()) }



    val profitColor = if(data.profit == "") {
        Color.Black
    } else {
        if(BigDecimal(data.profit, mathContext).signum() == -1) { Color.Blue} else {Color.Red}
    }

    val profit = if(data.profit == "") {
        "0"
    } else {
        data.profit
    }

    LaunchedEffect(key1 = data.buyDrMemo, block = {
        memoTextInput = data.buyDrMemo
    })

    if(dismissState.isDismissed(DismissDirection.StartToEnd))
        LaunchedEffect(key1 = Unit, block = {
            Log.d(TAG, "스와이프 이벤트")
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
                colors = CardDefaults.cardColors(containerColor = if(sellAction) SelectedColor else Color.White , contentColor = Color.Black),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            focusManager.clearFocus()
                            if (itemRowVisible == false) itemRowVisible = true else itemRowVisible =
                                false
                        }
                        .padding(top = 7.dp, bottom = 7.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center) {
                    RecordTextView(
                        recordText = "${data.date}",
                        TextHeight = 40.dp,
                        13,
                        2.5f,
                        bottonPpaing = 0.dp,
                        color = Color.Black)

                    Spacer(modifier = Modifier.width(1.dp))

                    RecordTextView(
                        recordText = "${BigDecimal(data.exchangeMoney, mathContext).toBigDecimalUs()!!}\n (${BigDecimal(data.money, mathContext).toBigDecimalWon()})",
                        TextHeight = 40.dp,
                        13,
                        2.5f,
                        bottonPpaing = 0.dp,
                        color = Color.Black)

                    Spacer(modifier = Modifier.width(1.dp))

                    RecordTextView(
                        recordText = "${data.rate}",
                        TextHeight = 40.dp,
                        13,
                        2.5f,
                        bottonPpaing = 0.dp,
                        color = Color.Black)

                    Spacer(modifier = Modifier.width(1.dp))

                    RecordTextView(
                        recordText = "${BigDecimal(profit, mathContext).toBigDecimalWon()}",
                        TextHeight = 40.dp,
                        13,
                        2.5f,
                        bottonPpaing = 0.dp,
                        color = profitColor)
                }

                AnimatedVisibility(visible = itemRowVisible) {
                    Column(
                       modifier = Modifier
                           .fillMaxWidth()
                           .padding(start = 32.dp, end = 30.dp)
                           .wrapContentHeight()) {
                        Row(modifier = Modifier
                            .wrapContentSize()) {
                            Card(modifier = Modifier
                                .padding(bottom = 10.dp),
                                colors = CardDefaults.cardColors( TopButtonColor ),
                                elevation = CardDefaults.cardElevation(8.dp),
                                shape = RoundedCornerShape(2.dp)) {
                                Text(
                                    text = "메모",
                                    modifier = Modifier
                                        .padding(all = 5.dp)
                                        .padding(horizontal = 3.dp))
                            }

                            Spacer(modifier = Modifier.weight(1f))


                            Box(
                                modifier = Modifier
                                    .wrapContentSize(Alignment.TopEnd)
                            ) {
                                Icon(
                                    modifier = Modifier
                                        .clickable {
                                            if(!dropdownExpanded) dropdownExpanded = true else dropdownExpanded = false
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
                                        Box(modifier = Modifier
                                            .fillMaxWidth(),
                                            contentAlignment = Alignment.TopStart) {
                                            Text(
                                                text = "매수 수정",
                                                fontSize = 13.sp)
                                        }
                                    }, onClick = {

                                    })

                                    DropdownMenuItem(
                                        modifier = Modifier
                                            .fillMaxWidth(),
                                        text = {
                                            Box(modifier = Modifier
                                                .fillMaxWidth(),
                                                contentAlignment = Alignment.TopStart) {
                                                Text(
                                                    text = "매도",
                                                    fontSize = 13.sp)
                                            }
                                        }, onClick = {
                                            if(!data.recordColor) {
                                                onClicked?.invoke(data)
                                                if(!openDialog) openDialog = true else openDialog = false
                                            } else {
                                                if(snackBarHostState.currentSnackbarData == null) {
                                                    coroutineScope.launch {
                                                        snackBarHostState.showSnackbar(
                                                            "매도한 기록입니다.",
                                                            actionLabel = "닫기", SnackbarDuration.Short
                                                        )
                                                    }
                                                }
                                                dropdownExpanded = false
                                            }
                                        })
                                }
                            }


                            
                        }

                        OutlinedTextField(
                            modifier = Modifier
                                .fillMaxWidth()
                                .wrapContentHeight()
                                .padding(bottom = 15.dp)
                                .focusRequester(focusRequester = focusRequester)
                                .onFocusChanged {
                                    isTextFieldFocused = it.isFocused
                                },
                            placeholder = {
                                          Text(text = "메모를 입력해주세요")
                            },
                            value = memoTextInput,
                            onValueChange = {
                                memoTextInput = it
                            },
                            textStyle = TextStyle(
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                textAlign = TextAlign.Start),
                            colors = TextFieldDefaults.outlinedTextFieldColors(
                                focusedBorderColor = Black,
                                unfocusedBorderColor = Black)
                            )

                        Row(modifier = Modifier
                            .wrapContentSize(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {

                            Spacer(modifier = Modifier.weight(1f))

                            Card(modifier = Modifier
                                .padding(bottom = 10.dp),
                                colors = CardDefaults.cardColors( TopButtonColor ),
                                elevation = CardDefaults.cardElevation(8.dp),
                                shape = RoundedCornerShape(2.dp),
                                onClick = {
                                    val updateData = data.copy(buyDrMemo = memoTextInput)
                                    dollarViewModel.buyDrMemoUpdate(updateData)
                                }) {
                                Text(
                                    text = "저장",
                                    modifier = Modifier
                                        .padding(all = 5.dp)
                                        .padding(horizontal = 3.dp))
                            }

                            Card(modifier = Modifier
                                .padding(bottom = 10.dp),
                                colors = CardDefaults.cardColors( TopButtonColor ),
                                elevation = CardDefaults.cardElevation(8.dp),
                                shape = RoundedCornerShape(2.dp),
                                onClick = {
                                    itemRowVisible = false
                                }) {
                                Text(
                                    text = "닫기",
                                    modifier = Modifier
                                        .padding(all = 5.dp)
                                        .padding(horizontal = 3.dp))
                            }
                        }
                    }
                }



            }
            if (openDialog) {
                SellDialog(
                    sellAction = {
                        sellActed(data)

                                 },
                    onDismissRequest = { openDialog = it},
                    onClicked = {openDialog = it},
                    dollarViewModel = dollarViewModel)
            }

            if(deleteAskDialog.value) {

                Log.d(TAG, "다이로그 오픈")

                DeleteDialog(onDismissRequest ={
                    deleteAskDialog.value = it
                }) {
                    dollarViewModel.removeBuyRecord(data)
                }

            }

        }
    )

}

fun Modifier.addFocusCleaner(focusManager: FocusManager, doOnClear: () -> Unit = {}): Modifier {
    return this.pointerInput(Unit) {
        detectTapGestures(onTap = {
            doOnClear()
            focusManager.clearFocus()
        })
    }
}


@OptIn(ExperimentalMaterialApi::class)
@Composable
fun SellLineDrRecordText(data: DrSellRecord,
                       onClicked: ((DrSellRecord)-> Unit)?,
                       dollarViewModel: DollarViewModel) {

    val deleteAskDialog = remember { mutableStateOf(false) }

    val dismissState = rememberDismissState()

    val mathContext = MathContext(28, RoundingMode.HALF_UP)

    if(dismissState.isDismissed(DismissDirection.StartToEnd))


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
                    .height(50.dp),
                shape = RoundedCornerShape(0.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White, contentColor = Color.Black),
                onClick = {

                }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 7.dp, bottom = 7.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    RecordTextView(recordText = "${data.date}", TextHeight = 50.dp, 13, 2.5f, bottonPpaing = 0.dp, color = Color.Black)
                    Spacer(modifier = Modifier.width(1.dp))
                    RecordTextView(recordText = "${BigDecimal(data.money, mathContext).toBigDecimalUs()}", TextHeight = 50.dp, 13, 2.5f, bottonPpaing = 0.dp, color = Color.Black)
                    Spacer(modifier = Modifier.width(1.dp))
                    RecordTextView(recordText = "${data.rate}", TextHeight = 50.dp, 13, 2.5f, bottonPpaing = 0.dp, color = Color.Black)
                    Spacer(modifier = Modifier.width(1.dp))
                    RecordTextView(recordText = "${BigDecimal(data.exchangeMoney, mathContext).toBigDecimalWon()}", TextHeight = 50.dp, 13, 2.5f, bottonPpaing = 0.dp, color = Color.Red)
                }

            }
            if(deleteAskDialog.value) {

                Log.d(TAG, "다이로그 오픈")

                DeleteDialog(onDismissRequest ={
                    deleteAskDialog.value = it
                }) {
                    dollarViewModel.removeSellRecord(data)
                }

            }

        }
    )

}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun LineYenRecordText(
    data: YenBuyRecord,
    sellAction: Boolean = data.recordColor,
    sellActed: (YenBuyRecord) -> Unit,
    onClicked: ((YenBuyRecord)-> Unit)?,
    yenViewModel: YenViewModel,
    snackbarHostState: SnackbarHostState) {

    val mathContext = MathContext(28, RoundingMode.HALF_UP)

    val openDialog = remember { mutableStateOf(false) }

    val deleteAskDialog = remember { mutableStateOf(false) }

    val dismissState = rememberDismissState()

    val profitColor = if(data.profit == "") {
        Color.Black
    } else {
        if(BigDecimal(data.profit, mathContext).signum() == -1) { Color.Blue} else {Color.Red}
    }

    val profit = if(data.profit == "") {
        "0"
    } else {
        data.profit
    }



    if(dismissState.isDismissed(DismissDirection.StartToEnd)){
        LaunchedEffect(key1 = Unit, block = {
            dismissState.reset()
            deleteAskDialog.value = true
        })
    }

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
                    .height(50.dp),
                shape = RoundedCornerShape(0.dp),
                colors = CardDefaults.cardColors(containerColor = if(sellAction) SelectedColor else Color.White , contentColor = Color.Black),
                onClick = {
                    if(openDialog.value == false) openDialog.value = !openDialog.value else null
                    onClicked?.invoke(data)
                }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 7.dp, bottom = 7.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center) {
                    RecordTextView(
                        recordText = "${data.date}",
                        TextHeight = 50.dp,
                        13,
                        2.5f,
                        bottonPpaing = 0.dp,
                        color = Color.Black)

                    Spacer(modifier = Modifier.width(1.dp))

                    RecordTextView(
                        recordText = "${BigDecimal(data.exchangeMoney, mathContext).toBigDecimalYen()!!}\n (${BigDecimal(data.money, mathContext).toBigDecimalWon()!!})",
                        TextHeight = 50.dp,
                        13,
                        2.5f,
                        bottonPpaing = 0.dp,
                        color = Color.Black)

                    Spacer(modifier = Modifier.width(1.dp))

                    RecordTextView(
                        recordText = "${data.rate}",
                        TextHeight = 50.dp,
                        13,
                        2.5f,
                        bottonPpaing = 0.dp,
                        color = Color.Black)

                    Spacer(modifier = Modifier.width(1.dp))

                    RecordTextView(
                        recordText = "${BigDecimal(profit, mathContext).toBigDecimalWon()}",
                        TextHeight = 50.dp,
                        13,
                        2.5f,
                        bottonPpaing = 0.dp,
                        color = profitColor)
                }

            }
            if (openDialog.value) {
                YenSellDialog(
                    sellAction = { sellActed(data)},
                    onDismissRequest = { openDialog.value = it},
                    onClicked = {openDialog.value = it},
                    yenViewModel = yenViewModel,
                    snackbarHostState = snackbarHostState)
            }

            if(deleteAskDialog.value) {

                Log.d(TAG, "다이로그 오픈")

                DeleteDialog(onDismissRequest ={
                    deleteAskDialog.value = it
                }) {
                    yenViewModel.removeBuyRecord(data)
                }

            }

        }
    )
}


@OptIn(ExperimentalMaterialApi::class)
@Composable
fun SellLineYenRecordText(data: YenSellRecord,
                       onClicked: ((YenSellRecord)-> Unit)?,
                          yenViewModel: YenViewModel) {

    val mathContext = MathContext(28, RoundingMode.HALF_UP)

    val deleteAskDialog = remember { mutableStateOf(false) }

    val dismissState = rememberDismissState()

    if(dismissState.isDismissed(DismissDirection.StartToEnd))


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
                    .height(50.dp),
                shape = RoundedCornerShape(0.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White, contentColor = Color.Black),
                onClick = {

                }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 7.dp, bottom = 7.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    RecordTextView(recordText = "${data.date}", TextHeight = 50.dp, 13, 2.5f, bottonPpaing = 0.dp, color = Color.Black)
                    Spacer(modifier = Modifier.width(1.dp))
                    RecordTextView(recordText = "${BigDecimal(data.money, mathContext).toBigDecimalYen()}", TextHeight = 50.dp, 13, 2.5f, bottonPpaing = 0.dp, color = Color.Black)
                    Spacer(modifier = Modifier.width(1.dp))
                    RecordTextView(recordText = "${data.rate}", TextHeight = 50.dp, 13, 2.5f, bottonPpaing = 0.dp, color = Color.Black)
                    Spacer(modifier = Modifier.width(1.dp))
                    RecordTextView(recordText = "${BigDecimal(data.exchangeMoney, mathContext).toBigDecimalWon()}", TextHeight = 50.dp, 13, 2.5f, bottonPpaing = 0.dp, color = Color.Red)
                }

            }
            if(deleteAskDialog.value) {

                Log.d(TAG, "다이로그 오픈")

                DeleteDialog(onDismissRequest ={
                    deleteAskDialog.value = it
                }) {
                    yenViewModel.removeSellRecord(data)
                }

            }

        }
    )

}




@OptIn(ExperimentalMaterialApi::class)
@Composable
fun WonLineRecordText(
    data: WonBuyRecord,
    sellAction: Boolean = data.recordColor,
    sellActed: (WonBuyRecord) -> Unit,
    onClicked: ((WonBuyRecord)-> Unit)?,
    wonViewModel: WonViewModel,
    snackbarHostState: SnackbarHostState) {

    val mathContext = MathContext(28, RoundingMode.HALF_UP)

    val moneyCg = when(data.moneyType) {
        1 -> {BigDecimal(data.money, mathContext).toBigDecimalUs() }
        2 -> {BigDecimal(data.money, mathContext).toBigDecimalYen() }
        else -> null
    }

    val profit = if(data.profit == "") {
        "0"
    } else {
        data.profit
    }

    val profitMoneyCg = when(data.moneyType) {
        1 -> {BigDecimal(profit, mathContext).toBigDecimalUs() }
        2 -> {BigDecimal(profit, mathContext).toBigDecimalYen() }
        else -> null
    }



    val openDialog = remember { mutableStateOf(false) }

    val deleteAskDialog = remember { mutableStateOf(false) }

    val dismissState = rememberDismissState()

    val profitColor = if(data.profit == "") {
        Color.Black
    } else {
        if(BigDecimal(data.profit, mathContext).signum() == -1) { Color.Blue} else {Color.Red}
    }

    if(dismissState.isDismissed(DismissDirection.StartToEnd))


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
                    .height(50.dp),
                shape = RoundedCornerShape(0.dp),
                colors = CardDefaults.cardColors(containerColor = if(sellAction) SelectedColor else Color.White , contentColor = Color.Black),
                onClick = {
                    if(openDialog.value == false) openDialog.value = !openDialog.value else null
                    onClicked?.invoke(data)
                }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 7.dp, bottom = 7.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center) {

                    RecordTextView(recordText = "${data.date}", TextHeight = 50.dp, 13, 2.5f, bottonPpaing = 0.dp, color = Color.Black)
                    Spacer(modifier = Modifier.width(1.dp))
                    RecordTextView(recordText = "${BigDecimal(data.exchangeMoney, mathContext).toBigDecimalWon()}\n (${moneyCg})", TextHeight = 50.dp, 13, 2.5f, bottonPpaing = 0.dp, color = Color.Black)
                    Spacer(modifier = Modifier.width(1.dp))
                    RecordTextView(recordText = "${data.rate}", TextHeight = 50.dp,13, 2.5f, bottonPpaing = 0.dp, color = Color.Black)
                    Spacer(modifier = Modifier.width(1.dp))
                    RecordTextView(recordText = "${profitMoneyCg}", TextHeight = 50.dp, 13, 2.5f, bottonPpaing = 0.dp, color = profitColor)



                }

            }

            if (openDialog.value) {
                WonSellDialog(
                    onDismissRequest = { openDialog.value = it},
                    onClicked = {openDialog.value = it},
                    wonViewModel = wonViewModel,
                    sellAction = { sellActed(data) },
                    snackbarHostState = snackbarHostState)
            }


            if(deleteAskDialog.value) {

                Log.d(TAG, "다이로그 오픈")

                DeleteDialog(onDismissRequest ={
                    deleteAskDialog.value = it
                }) {
                    wonViewModel.removeBuyRecord(data)
                }

            }

        }
    )
}





@OptIn(ExperimentalMaterialApi::class)
@Composable
fun WonSellLineRecordText(
    data: WonSellRecord,
    onClicked: ((WonSellRecord)-> Unit)?,
    wonViewModel: WonViewModel) {

    val mathContext = MathContext(28, RoundingMode.HALF_UP)

    val moneyCg = when(data.moneyType) {
        1 -> {BigDecimal(data.exchangeMoney, mathContext).toBigDecimalUs() }
        2 -> {BigDecimal(data.exchangeMoney, mathContext).toBigDecimalYen() }
        else -> null
    }


    val deleteAskDialog = remember { mutableStateOf(false) }

    val dismissState = rememberDismissState()

    if(dismissState.isDismissed(DismissDirection.StartToEnd))


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
                    .height(50.dp),
                shape = RoundedCornerShape(0.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White, contentColor = Color.Black),
                onClick = {

                }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 7.dp, bottom = 7.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    RecordTextView(recordText = "${data.date}", TextHeight = 50.dp, 13, 2.5f, bottonPpaing = 0.dp, color = Color.Black)
                    Spacer(modifier = Modifier.width(1.dp))
                    RecordTextView(recordText = "${BigDecimal(data.money, mathContext).toBigDecimalWon()}", TextHeight = 50.dp, 13, 2.5f, bottonPpaing = 0.dp, color = Color.Black)
                    Spacer(modifier = Modifier.width(1.dp))
                    RecordTextView(recordText = "${data.rate}", TextHeight = 50.dp, 13, 2.5f, bottonPpaing = 0.dp, color = Color.Black)
                    Spacer(modifier = Modifier.width(1.dp))
                    RecordTextView(recordText = "${moneyCg}", TextHeight = 50.dp, 13, 2.5f, bottonPpaing = 0.dp, color = Color.Red)
                }

            }
            if(deleteAskDialog.value) {

                Log.d(TAG, "다이로그 오픈")

                DeleteDialog(onDismissRequest ={
                    deleteAskDialog.value = it
                }) {
                    wonViewModel.removeSellRecord(data)
                }

            }

        }
    )

}

