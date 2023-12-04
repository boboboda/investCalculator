package com.bobodroid.myapplication.components

import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.bobodroid.myapplication.components.Caldenders.WonSellDatePickerDialog
import com.bobodroid.myapplication.components.Caldenders.YenSellDatePickerDialog
import com.bobodroid.myapplication.models.viewmodels.DollarViewModel
import com.bobodroid.myapplication.models.viewmodels.WonViewModel
import com.bobodroid.myapplication.models.viewmodels.YenViewModel
import com.bobodroid.myapplication.screens.TAG
import com.bobodroid.myapplication.ui.theme.SellButtonColor
import com.bobodroid.myapplication.ui.theme.SellPopColor
import java.time.LocalDate
import androidx.compose.material.SnackbarHostState
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SellDialog(
    onDismissRequest: (Boolean) -> Unit,
    onClicked: ((Boolean) -> Unit)?,
    sellAction: () -> Unit,
    dollarViewModel: DollarViewModel,
    snackbarHostState: SnackbarHostState) {

    val isDialogOpen = remember { mutableStateOf(false) }

    var sellRate = dollarViewModel.sellRateFlow.collectAsState()

    var openDialog = remember { mutableStateOf(false) }

    val isBtnActive = sellRate.value.isNotEmpty()

    val sellDate = dollarViewModel.sellDateFlow.collectAsState()

    var InputDate = if(sellDate.value == null) "날짜를 선택해주세요" else {sellDate.value}



    Dialog(
        onDismissRequest = { onDismissRequest(false) },
        properties = DialogProperties()) {
        Column(
            modifier = Modifier
                .wrapContentSize()
                .background(
                    color = Color.White,
                    shape = RoundedCornerShape(16.dp)
                )
        ) {
            Box(
                modifier = Modifier
                    .width(250.dp)
                    .height(200.dp)
                    .background(color = SellPopColor)) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(top = 10.dp)) {

                    Card(
                        modifier = Modifier
                            .width(220.dp)
                            .height(45.dp),
                        border = BorderStroke(1.dp, Color.Black),
                        colors = CardDefaults.cardColors(contentColor = Color.Black, containerColor = Color.White),
                        onClick = { isDialogOpen.value = !isDialogOpen.value

                        }) {
                        Text(text = InputDate, color = Color.Black, fontSize = 18.sp , textAlign = TextAlign.Center, modifier = Modifier
                            .width(160.dp)
                            .height(30.dp)
                            .padding(start = 52.dp, top = 8.dp))

                        if(isDialogOpen.value) {
                            DrSellDatePickerDialog(
                                onDateSelected = null,
                                onDismissRequest = {
                                    isDialogOpen.value = false
                                }, id = 1,
                                dollarViewModel
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(5.dp))

                    RateNumberField(title = "매도환율을 입력해주세요", onClicked = {
                        dollarViewModel.sellRateFlow.value = it
                    })

                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier =
                        Modifier
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Buttons(label = "매도",
                            onClicked = {
                                if(openDialog.value == false) openDialog.value = !openDialog.value else null
                                dollarViewModel.sellCalculation()

                                Log.d(TAG, "팔 때 환율  ${dollarViewModel.sellRateFlow.value}")
                                Log.d(TAG, "수익  ${dollarViewModel.sellDollarFlow.value}")
                                Log.d(TAG, "현재 돈  ${dollarViewModel.recordInputMoney.value}")
                                Log.d(TAG, "수익률  ${dollarViewModel.getPercentFlow.value}")
                            },

                            color = SellButtonColor,
                            fontColor = Color.White,
                            modifier = Modifier
                                .height(40.dp)
                                .width(80.dp),
                            enabled = isBtnActive,
                            fontSize = 15)
                        Spacer(modifier = Modifier.width(25.dp))

                        Buttons(label = "닫기",
                            onClicked = {onDismissRequest(false)},
                            color = SellButtonColor,
                            fontColor = Color.White,
                            modifier = Modifier
                                .height(40.dp)
                                .width(80.dp)
                            , fontSize = 15)
                    }
                }
            }

            if (openDialog.value) {
                SellResultDialog(
                    sellAction = sellAction,
                    onDismissRequest = {
                        openDialog.value = it
                        onDismissRequest
                        dollarViewModel.resetValue() },
                    onClicked = { onClicked?.invoke(false) }
                    ,dollarViewModel = dollarViewModel)

            }


        }
    }
}



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun YenSellDialog(
    onDismissRequest: (Boolean) -> Unit,
    onClicked: ((Boolean) -> Unit)?,
    sellAction: () -> Unit,
    yenViewModel: YenViewModel,
    snackbarHostState: SnackbarHostState) {

    val isDialogOpen = remember { mutableStateOf(false) }

    var sellRate = yenViewModel.sellRateFlow.collectAsState()

    var openDialog = remember { mutableStateOf(false) }

    val isBtnActive = sellRate.value.isNotEmpty()

    val sellDate = yenViewModel.sellDateFlow.collectAsState()

    var InputDate = if(sellDate.value == null) "날짜를 선택해주세요" else {sellDate.value}



    Dialog(
        onDismissRequest = { onDismissRequest(false) },
        properties = DialogProperties()) {
        Column(
            modifier = Modifier
                .wrapContentSize()
                .background(
                    color = Color.White,
                    shape = RoundedCornerShape(16.dp)
                )
        ) {
            Box(
                modifier = Modifier
                    .width(250.dp)
                    .height(200.dp)
                    .background(color = SellPopColor)) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(top = 10.dp)) {

                    Card(
                        modifier = Modifier
                            .width(220.dp)
                            .height(45.dp),
                        border = BorderStroke(1.dp, Color.Black),
                        colors = CardDefaults.cardColors(contentColor = Color.Black, containerColor = Color.White),
                        onClick = { isDialogOpen.value = !isDialogOpen.value

                        }) {
                        Text(text = InputDate, color = Color.Black, fontSize = 18.sp , textAlign = TextAlign.Center, modifier = Modifier
                            .width(160.dp)
                            .height(30.dp)
                            .padding(start = 52.dp, top = 8.dp))

                        if(isDialogOpen.value) {
                            YenSellDatePickerDialog(
                                onDateSelected = null,
                                onDismissRequest = {
                                    isDialogOpen.value = false
                                }, id = 1,
                                yenViewModel
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(5.dp))

                    RateNumberField(title = "매도환율을 입력해주세요", onClicked = {
                        yenViewModel.sellRateFlow.value = it
                    })

                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier =
                        Modifier
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Buttons(label = "매도",
                            onClicked = {
                                if(openDialog.value == false) openDialog.value = !openDialog.value else null
                                yenViewModel.sellCalculation()
                            },

                            color = SellButtonColor,
                            fontColor = Color.White,
                            modifier = Modifier
                                .height(40.dp)
                                .width(80.dp),
                            enabled = isBtnActive,
                            fontSize = 15)
                        Spacer(modifier = Modifier.width(25.dp))

                        Buttons(label = "닫기",
                            onClicked = {onDismissRequest(false)},
                            color = SellButtonColor,
                            fontColor = Color.White,
                            modifier = Modifier
                                .height(40.dp)
                                .width(80.dp)
                            , fontSize = 15)
                    }
                }
            }

            if (openDialog.value) {
                YenSellResultDialog(
                    sellAction = sellAction,
                    onDismissRequest = {
                        openDialog.value = it
                        onDismissRequest
                        yenViewModel.resetValue() },
                    onClicked = { onClicked?.invoke(false) }
                    , yenViewModel = yenViewModel)

            }


        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WonSellDialog(
    onDismissRequest: (Boolean) -> Unit,
    onClicked: ((Boolean) -> Unit)?,
    sellAction: () -> Unit,
    wonViewModel: WonViewModel,
    snackbarHostState: SnackbarHostState) {

    val isDialogOpen = remember { mutableStateOf(false) }

    val sellDate = wonViewModel.sellDateFlow.collectAsState()

    var InputDate = if(sellDate.value == null) "날짜를 선택해주세요" else {sellDate.value}

    val selectedDate = remember { mutableStateOf(LocalDate.now()) }

    var sellRate = wonViewModel.sellRateFlow.collectAsState()

    var openDialog = remember { mutableStateOf(false) }

    val isBtnActive = sellRate.value.isNotEmpty()

    Dialog(
        onDismissRequest = { onDismissRequest(false) },
        properties = DialogProperties()) {
        Column(
            modifier = Modifier
                .wrapContentSize()
                .background(
                    color = Color.White,
                    shape = RoundedCornerShape(16.dp)
                )
        ) {
            Box(
                modifier = Modifier
                    .width(250.dp)
                    .height(200.dp)
                    .background(color = SellPopColor)) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(top = 10.dp)) {

                    Card(
                        modifier = Modifier
                            .width(220.dp)
                            .height(45.dp),
                        border = BorderStroke(1.dp, Color.Black),
                        colors = CardDefaults.cardColors(contentColor = Color.Black, containerColor = Color.White),
                        onClick = { isDialogOpen.value = !isDialogOpen.value

                        }) {
                        Text(text = InputDate, color = Color.Black, fontSize = 18.sp , textAlign = TextAlign.Center, modifier = Modifier
                            .width(160.dp)
                            .height(30.dp)
                            .padding(start = 52.dp, top = 8.dp))

                        if(isDialogOpen.value) {
                            WonSellDatePickerDialog(
                                onDateSelected = null,
                                onDismissRequest = {
                                    isDialogOpen.value = false
                                }, id = 1,
                                wonViewModel
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(5.dp))

                    RateNumberField(title = "매도환율을 입력해주세요", onClicked = {
                        wonViewModel.sellRateFlow.value = it
                    })

                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier =
                        Modifier
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Buttons(label = "매도",
                            onClicked = {
                                if(openDialog.value == false) openDialog.value = !openDialog.value else null
                                wonViewModel.sellCalculation()

                            },

                            color = SellButtonColor,
                            fontColor = Color.White,
                            modifier = Modifier
                                .height(40.dp)
                                .width(80.dp),
                            enabled = isBtnActive,
                            fontSize = 15)
                        Spacer(modifier = Modifier.width(25.dp))

                        Buttons(label = "닫기",
                            onClicked = {onDismissRequest(false)},
                            color = SellButtonColor,
                            fontColor = Color.White,
                            modifier = Modifier
                                .height(40.dp)
                                .width(80.dp)
                            , fontSize = 15)
                    }
                }
            }

            if (openDialog.value) {
                WonSellResultDialog(
                    onDismissRequest = {
                        openDialog.value = it
                        onDismissRequest
                        wonViewModel.resetValue() },
                    sellAction =  sellAction,
                    onClicked = { onClicked?.invoke(false) }
                    , wonViewModel = wonViewModel)

            }


        }
    }
}
