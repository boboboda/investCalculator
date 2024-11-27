package com.bobodroid.myapplication.components.Dialogs

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
import com.bobodroid.myapplication.models.viewmodels.YenViewModel
import com.bobodroid.myapplication.ui.theme.SellButtonColor
import java.time.LocalDate
import androidx.compose.material.SnackbarHostState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.bobodroid.myapplication.components.Buttons
import com.bobodroid.myapplication.components.DrSellDatePickerDialog
import com.bobodroid.myapplication.models.datamodels.roomDb.CurrencyType
import com.bobodroid.myapplication.models.datamodels.roomDb.DrBuyRecord
import com.bobodroid.myapplication.models.datamodels.roomDb.ForeignCurrencyRecord
import com.bobodroid.myapplication.models.datamodels.roomDb.YenBuyRecord
import com.bobodroid.myapplication.models.viewmodels.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SellDialog(
    buyRecord: ForeignCurrencyRecord,
    onDismissRequest: (Boolean) -> Unit,
    selectedRecord: (sellRate: String, sellDate: String, sellProfit: String) -> Unit,
    mainViewModel: MainViewModel,
    currencyType: CurrencyType) {

    val sellRate by remember {
        mutableStateOf("")
    }

    val sellDate by remember {
        mutableStateOf<String?>(null)
    }

    var sellProfit by remember {
        mutableStateOf("")
    }

    var sellPercent by remember {
        mutableStateOf("")
    }

    val isDialogOpen = remember { mutableStateOf(false) }

    var openDialog = remember { mutableStateOf(false) }

    val isBtnActive = if(sellRate == "") true else false



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
                .padding(top = 20.dp, bottom = 20.dp)
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {


                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(45.dp),
                        border = BorderStroke(1.dp, Color.Black),
                        colors = CardDefaults.cardColors(contentColor = Color.Black, containerColor = Color.White),
                        onClick = { isDialogOpen.value = !isDialogOpen.value

                        }) {
                        Text(text = sellDate ?: "날짜를 선택해주세요",
                            color = Color.Black,
                            fontSize = 18.sp ,
                            modifier = Modifier
                                .padding(top = 10.dp)
                                .fillMaxWidth(),
                            textAlign = TextAlign.Center,

                           )

                        if(isDialogOpen.value) {
                            DrSellDatePickerDialog(
                                onDateSelected = { selectedDate ->

                                },
                                onDismissRequest = {
                                    isDialogOpen.value = false
                                }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    RateNumberField(
                        title = "매도환율을 입력해주세요",
                        modifier = Modifier.fillMaxWidth(),
                        onClicked = { sellRate ->

                    })

                    Spacer(modifier = Modifier.height(20.dp))
                    Row(
                        modifier =
                        Modifier
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Buttons(
                            onClicked = {
                                if(openDialog.value == false) openDialog.value = !openDialog.value else null
                                mainViewModel.sellCalculate(
                                    buyRecord.exchangeMoney ?: "",
                                    sellRate,
                                    buyRecord.money ?: "",
                                    currencyType,
                                    sellResult = { sellProfitValue, sellPercentValue ->
                                    sellProfit = sellProfitValue
                                    sellPercent = sellPercentValue
                                } )
                            },

                            color = SellButtonColor,
                            fontColor = Color.White,
                            modifier = Modifier
                                .height(40.dp)
                                .width(80.dp),
                            enabled = isBtnActive) {
                            Text("매도", fontSize = 15.sp)
                        }


                        Spacer(modifier = Modifier.width(25.dp))

                        Buttons(
                            onClicked = {onDismissRequest(false)},
                            color = SellButtonColor,
                            fontColor = Color.White,
                            modifier = Modifier
                                .height(40.dp)
                                .width(80.dp)) {
                            Text("닫기", fontSize = 15.sp)
                        }
                    }



            if (openDialog.value) {
                SellResultDialog(
                    selectedRecord = {
                        selectedRecord(sellRate, sellDate ?: "", sellProfit)
                    },
                    onDismissRequest = {
                        openDialog.value = it
                        onDismissRequest.invoke(it) },
                    onClicked = {

                    },
                    percent = sellPercent,
                    sellProfit = sellProfit)

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
    buyRecord: YenBuyRecord
) {

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
                .padding(top = 20.dp, bottom = 20.dp)
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(45.dp),
                        border = BorderStroke(1.dp, Color.Black),
                        colors = CardDefaults.cardColors(contentColor = Color.Black, containerColor = Color.White),
                        onClick = { isDialogOpen.value = !isDialogOpen.value

                        }) {
                        Text(text = InputDate,
                            color = Color.Black,
                            fontSize = 18.sp ,
                            modifier = Modifier
                                .padding(top = 10.dp)
                                .fillMaxWidth(),
                            textAlign = TextAlign.Center,

                            )

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

            Spacer(modifier = Modifier.height(20.dp))

                    RateNumberField(
                        title = "매도환율을 입력해주세요",
                        modifier = Modifier ,
                        onClicked = {
                        yenViewModel.sellRateFlow.value = it
                    })

            Spacer(modifier = Modifier.height(20.dp))
                    Row(
                        modifier =
                        Modifier
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {

                        Buttons(
                            onClicked = {
                                if(openDialog.value == false) openDialog.value = !openDialog.value else null
                                yenViewModel.sellCalculation()
                            },

                            color = SellButtonColor,
                            fontColor = Color.White,
                            modifier = Modifier
                                .height(40.dp)
                                .width(80.dp),
                            enabled = isBtnActive) {
                            Text("매도", fontSize = 15.sp)
                        }


                        Spacer(modifier = Modifier.width(25.dp))


                        Buttons(
                            onClicked = {onDismissRequest(false)},
                            color = SellButtonColor,
                            fontColor = Color.White,
                            modifier = Modifier
                                .height(40.dp)
                                .width(80.dp)) {
                            Text("닫기", fontSize = 15.sp)
                        }
                    }



            if (openDialog.value) {
                YenSellResultDialog(
                    buyRecord = buyRecord,
                    sellAction = sellAction,
                    onDismissRequest = {
                        openDialog.value = it
                        onDismissRequest.invoke(it)
                        yenViewModel.resetValue()
                    },
                    onClicked = { onClicked?.invoke(it) },
                    yenViewModel = yenViewModel)

            }


        }
    }
}

