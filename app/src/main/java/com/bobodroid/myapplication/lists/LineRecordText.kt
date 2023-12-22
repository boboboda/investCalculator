@file:OptIn(ExperimentalMaterial3Api::class)

package com.bobodroid.myapplication.lists

import android.util.Log
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.DismissDirection
import androidx.compose.material.DismissValue
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.FractionalThreshold
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
import androidx.compose.material.rememberDismissState
import com.bobodroid.myapplication.MainActivity.Companion.TAG
import com.bobodroid.myapplication.components.RecordTextView
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

    val openDialog = remember { mutableStateOf(false) }

    val deleteAskDialog = remember { mutableStateOf(false) }

    val dismissState = rememberDismissState()

    val profitColor = if(BigDecimal(data.profit, mathContext).signum() == -1) { Color.Blue} else {Color.Red}

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
                        recordText = "${BigDecimal(data.exchangeMoney, mathContext).toBigDecimalUs()!!}\n (${BigDecimal(data.money, mathContext).toBigDecimalWon()})",
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
                        recordText = "${BigDecimal(data.profit, mathContext).toBigDecimalWon()}",
                        TextHeight = 50.dp,
                        13,
                        2.5f,
                        bottonPpaing = 0.dp,
                        color = profitColor)
                }

            }
            if (openDialog.value) {
                SellDialog(
                    sellAction = { sellActed(data)},
                    onDismissRequest = { openDialog.value = it},
                    onClicked = {openDialog.value = it},
                    dollarViewModel = dollarViewModel,
                    snackbarHostState = snackBarHostState)
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

    val profitColor = if(BigDecimal(data.profit, mathContext).signum() == -1) { Color.Blue} else {Color.Red}

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
                        recordText = "${BigDecimal(data.profit, mathContext).toBigDecimalWon()}",
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

    val profitMoneyCg = when(data.moneyType) {
        1 -> {BigDecimal(data.profit, mathContext).toBigDecimalUs() }
        2 -> {BigDecimal(data.profit, mathContext).toBigDecimalYen() }
        else -> null
    }



    val openDialog = remember { mutableStateOf(false) }

    val deleteAskDialog = remember { mutableStateOf(false) }

    val dismissState = rememberDismissState()

    val profitColor = if(BigDecimal(data.profit, mathContext).signum() == -1) { Color.Blue} else {Color.Red}

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

