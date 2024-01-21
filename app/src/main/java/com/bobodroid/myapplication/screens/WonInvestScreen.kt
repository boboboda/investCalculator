@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3Api::class)

package com.bobodroid.myapplication.screens

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.SnackbarHostState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bobodroid.myapplication.components.*
import com.bobodroid.myapplication.components.Caldenders.WonMyDatePickerDialog
import com.bobodroid.myapplication.components.Dialogs.RateNumberField
import com.bobodroid.myapplication.components.Dialogs.TextFieldDialog
import com.bobodroid.myapplication.components.Dialogs.WonNumberField
import com.bobodroid.myapplication.components.admobs.BuyBannerAd
import com.bobodroid.myapplication.models.viewmodels.AllViewModel
import com.bobodroid.myapplication.models.viewmodels.WonViewModel
import com.bobodroid.myapplication.routes.*
import com.bobodroid.myapplication.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@SuppressLint("RememberReturnType")
@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)

@Composable
fun WonInvestScreen(
    wonViewModel: WonViewModel,
    routeAction: InvestRouteAction,
    routeName: String,
    allViewModel: AllViewModel,
) {



    var rateInput = wonViewModel.rateInputFlow.collectAsState()
    var moneyInput = wonViewModel.moneyInputFlow.collectAsState()
    val time = Calendar.getInstance().time
    val formatter = SimpleDateFormat("yyyy-MM-dd")
    val today = formatter.format(time)
    val isBtnActive = rateInput.value.isNotEmpty() && moneyInput.value.isNotEmpty()

    val isDialogOpen = remember { mutableStateOf(false) }

    val dateRecord = wonViewModel.dateFlow.collectAsState()

    var date = if(today == "") "$today" else {dateRecord.value}

    val moneyCgBtnSelected = wonViewModel.moneyCgBtnSelected.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    var group = remember { mutableStateOf("미지정") }

    var dropdownExpanded by remember { mutableStateOf(false) }

    val groupList = wonViewModel.groupList.collectAsState()

    var groupAddDialog by remember { mutableStateOf(false) }


    Column(
        modifier = Modifier
            .fillMaxSize()) {

        Spacer(modifier = Modifier
            .fillMaxWidth()
            .height(8.dp))


        Row(modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically) {

            CardButton(
                label = routeName,
                onClicked = {
                    routeAction.navTo(InvestRoute.DOLLAR_BUY)
                },
                fontSize = 15,
                modifier = Modifier
                    .padding(10.dp)
                    .height(50.dp)
                    .width(80.dp),
                fontColor = Color.Black,
                buttonColor = Green
            )

            Spacer(modifier = Modifier.weight(1f))

            Card(
                modifier = Modifier
                    .width(160.dp)
                    .padding(end = 10.dp)
                    .height(40.dp)
                    .background(Color.White),
                border = BorderStroke(1.dp, Color.Black),
                colors = CardDefaults.cardColors(
                    contentColor = Color.Black,
                    containerColor = Color.White
                ),
                onClick = {
                    isDialogOpen.value = !isDialogOpen.value
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
        }

        Spacer(modifier = Modifier
            .fillMaxWidth()
            .height(5.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 15.dp, bottom = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Card(
                colors = CardDefaults.cardColors(WelcomeScreenBackgroundColor),
                elevation = CardDefaults.cardElevation(8.dp),
                shape = RoundedCornerShape(1.dp),
                modifier = Modifier
                    .height(40.dp)
                    .wrapContentWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxHeight()
                        .wrapContentWidth()
                        .padding(horizontal = 10.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = group.value)
                }
            }


            Box(
                modifier = Modifier
                    .wrapContentSize(Alignment.TopEnd)
            ) {
                Card(
                    colors = CardDefaults.cardColors(WelcomeScreenBackgroundColor),
                    elevation = CardDefaults.cardElevation(8.dp),
                    shape = RoundedCornerShape(1.dp),
                    modifier = Modifier
                        .height(40.dp)
                        .wrapContentWidth(),
                    onClick = {
                        if (!dropdownExpanded) dropdownExpanded = true else dropdownExpanded = false
                    }) {
                    Row(
                        modifier = Modifier
                            .fillMaxHeight()
                            .wrapContentWidth()
                            .padding(horizontal = 10.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "그룹지정")

                        Icon(imageVector = Icons.Rounded.KeyboardArrowDown, contentDescription = "")
                    }
                }
                DropdownMenu(
                    scrollState = rememberScrollState(),
                    modifier = Modifier
                        .wrapContentHeight()
                        .heightIn(max = 200.dp)
                        .width(200.dp),
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

                    groupList.value.forEach { groupName ->
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
                                group.value = groupName
                                dropdownExpanded = false
                            })
                    }


                }
            }


        }


        Row(
            modifier = Modifier.width(180.dp),
            horizontalArrangement = Arrangement.Center) {
            MoneyChButtonView(
                mainText = "달러",
                id = 1,
                selectedId = moneyCgBtnSelected.value ,
                selectAction = {wonViewModel.moneyCgBtnSelected.value = it})

            Spacer(modifier = Modifier.width(20.dp))

            MoneyChButtonView(
                mainText = "엔화",
                id = 2,
                selectedId = moneyCgBtnSelected.value ,
                selectAction = {wonViewModel.moneyCgBtnSelected.value = it})
        }

        Spacer(modifier = Modifier
            .fillMaxWidth()
            .height(5.dp))


        WonNumberField("매수금(엔화,달러)을 입력해주세요", onClicked = {

             wonViewModel.moneyInputFlow.value = it


        },wonViewModel,
            snackbarHostState = snackbarHostState)

        Spacer(modifier = Modifier
            .fillMaxWidth()
            .height(10.dp))

        RateNumberField("매수환율을 입력해주세요",
            Modifier.padding(horizontal = 10.dp),
            onClicked = {
            wonViewModel.rateInputFlow.value = it
        })

        if(isDialogOpen.value) {
            WonMyDatePickerDialog(onDateSelected = null,
                onDismissRequest = {
                isDialogOpen.value = false
            }, id = 1,
                wonViewModel)
        }

        if (groupAddDialog) {
            TextFieldDialog(
                onDismissRequest = {
                    groupAddDialog = it },
                placeholder = "새 그룹명을 작성해주세요",
                onClickedLabel = "추가",
                closeButtonLabel = "닫기",
                onClicked = { name ->
                    wonViewModel.groupAdd(name)
                    groupAddDialog = false
                })
        }


        Spacer(modifier = Modifier
            .fillMaxWidth()
            .height(50.dp))

        Row(modifier = Modifier
            .fillMaxWidth()
            .height(70.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center) {
            Buttons("매수",
                enabled = isBtnActive,
                onClicked = {
                    wonViewModel.buyAddRecord(groupName = group.value)
                    wonViewModel.selectedBoxId.value = 1
                    routeAction.navTo(InvestRoute.MAIN)
                    allViewModel.changeMoney.value = 3
                    Log.d(TAG, "${wonViewModel.exchangeMoney.value}")
                    group.value = "미지정"

                }
                , color = BuyColor
                , fontColor = Color.Black
                , modifier = Modifier
                    .height(60.dp)
                    .width(120.dp)
                , fontSize = 25)

            Spacer(modifier = Modifier.width(60.dp))


            Buttons( "닫기", onClicked = {
                wonViewModel.selectedBoxId.value = 1
                routeAction.navTo(InvestRoute.MAIN)
                wonViewModel.moneyInputFlow.value = ""
                wonViewModel.rateInputFlow.value = ""
                allViewModel.changeMoney.value = 3 },
                color = BuyColor,
                fontColor = Color.Black,
                modifier = Modifier
                    .height(60.dp)
                    .width(120.dp),
                fontSize = 25)


        }

        Spacer(modifier = Modifier.weight(1f))

        Column(
            modifier = Modifier
                .fillMaxWidth(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally) {
            BuyBannerAd()
        }

    }
}




















