package com.bobodroid.myapplication.components.Dialogs

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.SnackbarHost
import androidx.compose.material.SnackbarHostState
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.bobodroid.myapplication.components.Buttons
import com.bobodroid.myapplication.components.Caldenders.BasicCalenderDialog
import com.bobodroid.myapplication.models.datamodels.roomDb.DrBuyRecord
import com.bobodroid.myapplication.models.datamodels.roomDb.WonBuyRecord
import com.bobodroid.myapplication.models.datamodels.roomDb.YenBuyRecord
import com.bobodroid.myapplication.ui.theme.SellButtonColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InsertDialog(
    data: Any,
    moneyTitle: String,
    rateTitle: String,
    onDismissRequest: (Boolean) -> Unit,
    onClicked:(String, String, String) -> Unit,
) {

    when (data) {
        is DrBuyRecord -> {
            var userMoneyInput by remember { mutableStateOf(data.money) }
            var userRateInput by remember { mutableStateOf(data.rate) }
            var date by remember { mutableStateOf("${data.date}") }
            // DrBuyRecord에 대한 처리 로직

            val snackbarHostState = remember{ SnackbarHostState() }

            var showCalenderDialog by remember { mutableStateOf(false) }

            Dialog(
                onDismissRequest = { onDismissRequest(false) },
                properties = DialogProperties()
            ) {
                Box(
                    Modifier
                        .wrapContentSize(),
                    contentAlignment = Alignment.BottomCenter) {
                    Column(
                        modifier = Modifier
                            .wrapContentSize()
                            .background(
                                color = Color.White,
                                shape = RoundedCornerShape(16.dp)
                            )
                            .padding(top = 30.dp, bottom = 30.dp)
                            .padding(horizontal = 20.dp),
                        horizontalAlignment = Alignment.Start
                    ) {

                        Card(
                            modifier = Modifier
                                .width(160.dp)
                                .padding(start = 10.dp, end = 25.dp)
                                .height(40.dp)
                                .background(Color.White),
                            border = BorderStroke(1.dp, Color.Black),
                            colors = CardDefaults.cardColors(
                                contentColor = Color.Black,
                                containerColor = Color.White
                            ),
                            onClick = {
                                if(!showCalenderDialog) showCalenderDialog = true
                            }) {

                            Row(modifier = Modifier
                                .fillMaxSize(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center) {
                                Text(
                                    text = "$date",
                                    color = Color.Black,
                                    fontSize = 18.sp,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier
                                )
                            }

                        }

                        //머니
                        NumberField(
                            title = moneyTitle,
                            haveValue = data.money,
                            onClicked = {
                                userMoneyInput = it
                            })

                        Spacer(modifier = Modifier.height(10.dp))

                        //환율
                        RateNumberField(
                            title = rateTitle,
                            haveValue = data.rate,
                            modifier = Modifier.padding(horizontal = 10.dp),
                            onClicked = {
                                userRateInput = it
                            })

                        Spacer(modifier = Modifier.height(30.dp))

                        Row(
                            modifier =
                            Modifier
                                .wrapContentSize()
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Buttons(
                                onClicked = {
                                    onClicked.invoke(date, userMoneyInput ?: "0", userRateInput ?: "0")
                                },

                                color = SellButtonColor,
                                fontColor = Color.White,
                                modifier = Modifier
                                    .height(40.dp)
                                    .width(80.dp),
                            ) {
                                Text(text = "수정", fontSize = 15.sp)
                            }
                            Spacer(modifier = Modifier.width(25.dp))

                            Buttons(
                                onClicked = { onDismissRequest(false) },
                                color = SellButtonColor,
                                fontColor = Color.White,
                                modifier = Modifier
                                    .height(40.dp)
                                    .width(80.dp)
                            ) {
                                Text(text = "닫기", fontSize = 15.sp)
                            }
                        }

                    }

                    if(showCalenderDialog) {
                        BasicCalenderDialog(
                            initialDate = date,
                            onDateSelected = {
                                date = it.toString()
                                showCalenderDialog = false
                            }, onDismissRequest = {
                                showCalenderDialog = false
                            })
                    }

                    // 스낵바
                    Row(modifier = Modifier
                        .wrapContentSize()
                        .padding(bottom = 20.dp)
                    ) {
                        SnackbarHost(
                            hostState = snackbarHostState, modifier = Modifier,
                            snackbar = { snackBarData ->


                                Card(
                                    shape = RoundedCornerShape(8.dp),
                                    border = BorderStroke(1.5.dp, Color.Black),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(all = 10.dp),
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(start = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Start
                                    ) {

                                        Text(
                                            text = snackBarData.message,
                                            fontSize = 15.sp,
                                            lineHeight = 20.sp,
                                            fontWeight = FontWeight.Medium
                                        )

                                        Spacer(modifier = Modifier.weight(1f))

                                        Text(
                                            modifier = Modifier
                                                .padding(8.dp)
                                                .clickable {
                                                    snackbarHostState.currentSnackbarData?.dismiss()
                                                },
                                            text = "닫기",
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            })
                    }
                }

            }
        }
        is YenBuyRecord -> {
            var userMoneyInput by remember { mutableStateOf(data.money) }
            var userRateInput by remember { mutableStateOf(data.rate) }
            var date by remember { mutableStateOf("${data.date}") }
            // YenBuyRecord에 대한 처리 로직

            val snackbarHostState = remember{ SnackbarHostState() }

            var showCalenderDialog by remember { mutableStateOf(false) }

            Dialog(
                onDismissRequest = { onDismissRequest(false) },
                properties = DialogProperties()
            ) {
                Box(
                    Modifier
                        .wrapContentSize(),
                    contentAlignment = Alignment.BottomCenter) {
                    Column(
                        modifier = Modifier
                            .wrapContentSize()
                            .background(
                                color = Color.White,
                                shape = RoundedCornerShape(16.dp)
                            )
                            .padding(top = 30.dp, bottom = 30.dp)
                            .padding(horizontal = 20.dp),
                        horizontalAlignment = Alignment.Start
                    ) {

                        Card(
                            modifier = Modifier
                                .width(160.dp)
                                .padding(start = 10.dp, end = 25.dp)
                                .height(40.dp)
                                .background(Color.White),
                            border = BorderStroke(1.dp, Color.Black),
                            colors = CardDefaults.cardColors(
                                contentColor = Color.Black,
                                containerColor = Color.White
                            ),
                            onClick = {
                                if(!showCalenderDialog) showCalenderDialog = true
                            }) {

                            Row(modifier = Modifier
                                .fillMaxSize(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center) {
                                Text(
                                    text = "$date",
                                    color = Color.Black,
                                    fontSize = 18.sp,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier
                                )
                            }

                        }

                        //머니
                        NumberField(
                            title = moneyTitle,
                            haveValue = data.money,
                            onClicked = {
                                userMoneyInput = it
                            })

                        Spacer(modifier = Modifier.height(10.dp))

                        //환율
                        RateNumberField(
                            title = rateTitle,
                            haveValue = data.rate,
                            modifier = Modifier.padding(horizontal = 10.dp),
                            onClicked = {
                                userRateInput = it
                            })

                        Spacer(modifier = Modifier.height(30.dp))

                        Row(
                            modifier =
                            Modifier
                                .wrapContentSize()
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Buttons(
                                onClicked = {
                                    onClicked.invoke(date, userMoneyInput ?: "0", userRateInput ?: "0")
                                },

                                color = SellButtonColor,
                                fontColor = Color.White,
                                modifier = Modifier
                                    .height(40.dp)
                                    .width(80.dp)
                            ) {
                                Text(text = "수정", fontSize = 15.sp)
                            }

                            Spacer(modifier = Modifier.width(25.dp))

                            Buttons(
                                onClicked = { onDismissRequest(false) },
                                color = SellButtonColor,
                                fontColor = Color.White,
                                modifier = Modifier
                                    .height(40.dp)
                                    .width(80.dp)
                            ) {
                                Text(text = "닫기", fontSize = 15.sp)
                            }
                        }

                    }

                    if(showCalenderDialog) {
                        BasicCalenderDialog(
                            initialDate = date,
                            onDateSelected = {
                                date = it.toString()
                                showCalenderDialog = false
                            }, onDismissRequest = {
                                showCalenderDialog = false
                            })
                    }

                    // 스낵바
                    Row(modifier = Modifier
                        .wrapContentSize()
                        .padding(bottom = 20.dp)
                    ) {
                        SnackbarHost(
                            hostState = snackbarHostState, modifier = Modifier,
                            snackbar = { snackBarData ->


                                Card(
                                    shape = RoundedCornerShape(8.dp),
                                    border = BorderStroke(1.5.dp, Color.Black),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(all = 10.dp),
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(start = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Start
                                    ) {

                                        Text(
                                            text = snackBarData.message,
                                            fontSize = 15.sp,
                                            lineHeight = 20.sp,
                                            fontWeight = FontWeight.Medium
                                        )

                                        Spacer(modifier = Modifier.weight(1f))

                                        Text(
                                            modifier = Modifier
                                                .padding(8.dp)
                                                .clickable {
                                                    snackbarHostState.currentSnackbarData?.dismiss()
                                                },
                                            text = "닫기",
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            })
                    }
                }

            }
        }
        is WonBuyRecord -> {
            var userMoneyInput by remember { mutableStateOf(data.money) }
            var userRateInput by remember { mutableStateOf(data.rate) }
            var date by remember { mutableStateOf("${data.date}") }
            // WonBuyRecord에 대한 처리 로직

            val snackbarHostState = remember{ SnackbarHostState() }

            var showCalenderDialog by remember { mutableStateOf(false) }

            Dialog(
                onDismissRequest = { onDismissRequest(false) },
                properties = DialogProperties()
            ) {
                Box(
                    Modifier
                        .wrapContentSize(),
                    contentAlignment = Alignment.BottomCenter) {
                    Column(
                        modifier = Modifier
                            .wrapContentSize()
                            .background(
                                color = Color.White,
                                shape = RoundedCornerShape(16.dp)
                            )
                            .padding(top = 30.dp, bottom = 30.dp)
                            .padding(horizontal = 20.dp),
                        horizontalAlignment = Alignment.Start
                    ) {

                        Card(
                            modifier = Modifier
                                .width(160.dp)
                                .padding(start = 10.dp, end = 25.dp)
                                .height(40.dp)
                                .background(Color.White),
                            border = BorderStroke(1.dp, Color.Black),
                            colors = CardDefaults.cardColors(
                                contentColor = Color.Black,
                                containerColor = Color.White
                            ),
                            onClick = {
                                if(!showCalenderDialog) showCalenderDialog = true
                            }) {

                            Row(modifier = Modifier
                                .fillMaxSize(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center) {
                                Text(
                                    text = "$date",
                                    color = Color.Black,
                                    fontSize = 18.sp,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier
                                )
                            }

                        }

                        //머니
                        NumberField(
                            title = moneyTitle,
                            haveValue = data.money,
                            onClicked = {
                                userMoneyInput = it
                            })

                        Spacer(modifier = Modifier.height(10.dp))

                        //환율
                        RateNumberField(
                            title = rateTitle,
                            haveValue = data.rate,
                            modifier = Modifier.padding(horizontal = 10.dp),
                            onClicked = {
                                userRateInput = it
                            })

                        Spacer(modifier = Modifier.height(30.dp))

                        Row(
                            modifier =
                            Modifier
                                .wrapContentSize()
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Buttons(
                                onClicked = {
                                    onClicked.invoke(date, userMoneyInput ?: "0", userRateInput ?: "0")
                                },

                                color = SellButtonColor,
                                fontColor = Color.White,
                                modifier = Modifier
                                    .height(40.dp)
                                    .width(80.dp),
                            ) {
                                Text(text = "수정", fontSize = 15.sp)
                            }
                            Spacer(modifier = Modifier.width(25.dp))

                            Buttons(
                                onClicked = { onDismissRequest(false) },
                                color = SellButtonColor,
                                fontColor = Color.White,
                                modifier = Modifier
                                    .height(40.dp)
                                    .width(80.dp)
                            ) {
                                Text(text = "닫기", fontSize = 15.sp)
                            }
                        }

                    }

                    if(showCalenderDialog) {
                        BasicCalenderDialog(
                            initialDate = date,
                            onDateSelected = {
                                date = it.toString()
                                showCalenderDialog = false
                            }, onDismissRequest = {
                                showCalenderDialog = false
                            })
                    }

                    // 스낵바
                    Row(modifier = Modifier
                        .wrapContentSize()
                        .padding(bottom = 20.dp)
                    ) {
                        SnackbarHost(
                            hostState = snackbarHostState, modifier = Modifier,
                            snackbar = { snackBarData ->


                                Card(
                                    shape = RoundedCornerShape(8.dp),
                                    border = BorderStroke(1.5.dp, Color.Black),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(all = 10.dp),
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(start = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Start
                                    ) {

                                        Text(
                                            text = snackBarData.message,
                                            fontSize = 15.sp,
                                            lineHeight = 20.sp,
                                            fontWeight = FontWeight.Medium
                                        )

                                        Spacer(modifier = Modifier.weight(1f))

                                        Text(
                                            modifier = Modifier
                                                .padding(8.dp)
                                                .clickable {
                                                    snackbarHostState.currentSnackbarData?.dismiss()
                                                },
                                            text = "닫기",
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            })
                    }
                }

            }
        }
        else -> {
            // data가 위의 어느 타입에도 해당하지 않는 경우
            return
        }
    }


}