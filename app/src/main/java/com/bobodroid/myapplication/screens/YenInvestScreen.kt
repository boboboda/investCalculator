@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3Api::class)

package com.bobodroid.myapplication.screens

import android.util.Log
import android.widget.CalendarView
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.bobodroid.myapplication.components.*
import com.bobodroid.myapplication.models.viewmodels.YenViewModel
import com.bobodroid.myapplication.ui.theme.*
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*
import androidx.compose.material.SnackbarHostState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.ui.unit.DpOffset
import com.bobodroid.myapplication.components.Dialogs.NumberField
import com.bobodroid.myapplication.components.Dialogs.RateNumberField
import com.bobodroid.myapplication.components.Dialogs.TextFieldDialog
import com.bobodroid.myapplication.components.admobs.BuyBannerAd
import com.bobodroid.myapplication.models.viewmodels.AllViewModel
import com.bobodroid.myapplication.routes.InvestRoute
import com.bobodroid.myapplication.routes.InvestRouteAction

@OptIn(ExperimentalMaterial3Api::class)

@Composable
fun YenInvestScreen(yenViewModel: YenViewModel, routeAction: InvestRouteAction, allViewModel: AllViewModel) {



    var rateInput = yenViewModel.rateInputFlow.collectAsState()
    var moneyInput = yenViewModel.moneyInputFlow.collectAsState()
    val time = Calendar.getInstance().time
    val formatter = SimpleDateFormat("yyyy-MM-dd")
    val today = formatter.format(time)
    val isBtnActive = rateInput.value.isNotEmpty() && moneyInput.value.isNotEmpty()

    val isDialogOpen = remember { mutableStateOf(false) }

    val dateRecord = yenViewModel.dateFlow.collectAsState()

    var date = if(today == "") "$today" else {dateRecord.value}


    val snackBarHostState = remember { SnackbarHostState() }

    var dropdownExpanded by remember { mutableStateOf(false) }

    val groupList = yenViewModel.groupList.collectAsState()

    var groupAddDialog by remember { mutableStateOf(false) }

    var group = remember { mutableStateOf("미지정") }


    Column(
        modifier = Modifier
            .fillMaxSize()) {

        Spacer(modifier = Modifier
            .fillMaxWidth()
            .height(8.dp))

        Row(modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically) {
            Card(
                modifier = Modifier
                    .padding(10.dp)
                    .height(50.dp)
                    .width(80.dp),
                colors = CardDefaults.cardColors(containerColor = Green)
            ) {
                Text(
                    text = "엔화",
                    fontSize = 20.sp,
                    modifier = Modifier
                        .padding(10.dp)
                        .height(50.dp)
                        .width(80.dp),
                    textAlign = TextAlign.Center)
            }

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

        NumberField("매수금(원)을 입력해주세요", onClicked = {
             yenViewModel.moneyInputFlow.value = it
        },
            snackBarHostState)

        Spacer(modifier = Modifier
            .fillMaxWidth()
            .height(10.dp))

        RateNumberField("매수환율을 입력해주세요",
            Modifier.padding(horizontal = 10.dp),
            onClicked = {
            yenViewModel.rateInputFlow.value = it
        })

        if(isDialogOpen.value) {
            YenMyDatePickerDialog(onDateSelected = null,
                onDismissRequest = {
                isDialogOpen.value = false
            }, id = 1,
                yenViewModel)
        }

        if (groupAddDialog) {
            TextFieldDialog(
                onDismissRequest = {
                    groupAddDialog = it },
                placeholder = "새 그룹명을 작성해주세요",
                onClickedLabel = "추가",
                closeButtonLabel = "닫기",
                onClicked = { name ->
                    yenViewModel.groupAdd(name)
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
                    yenViewModel.buyAddRecord()
                    yenViewModel.selectedBoxId.value = 1
                    routeAction.navTo(InvestRoute.MAIN)
                    allViewModel.changeMoney.value = 2

                }
                , color = BuyColor
                , fontColor = Color.Black
                , modifier = Modifier
                    .height(60.dp)
                    .width(120.dp)
                , fontSize = 25)

            Spacer(modifier = Modifier.width(60.dp))


            Buttons( "닫기", onClicked = {
                yenViewModel.selectedBoxId.value = 1
                routeAction.navTo(InvestRoute.MAIN)
                yenViewModel.moneyInputFlow.value = ""
                yenViewModel.rateInputFlow.value = ""
                allViewModel.changeMoney.value = 2
            }
                , color = BuyColor
                , fontColor = Color.Black
                , modifier = Modifier
                    .height(60.dp)
                    .width(120.dp)
                , fontSize = 25)


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


@Composable
fun YenMyDatePickerDialog(
    onDateSelected: ((LocalDate, Int) -> Unit)?,
    onDismissRequest: () -> Unit,
    id: Int,
    yenViewModel: YenViewModel) {

    val selectedDate = remember { mutableStateOf(LocalDate.now()) }

    var currentCardId : Int = id

//    var color = if (selectedId == currentCardId) Color.Gray else Color.LightGray

    Dialog(onDismissRequest = {
        onDismissRequest()
    }, properties = DialogProperties()) {
        Column(
            modifier = Modifier
                .wrapContentSize()
                .background(
                    color = Color.White,
                    shape = RoundedCornerShape(16.dp)
                )
        ) {
            Text(
                text = selectedDate.value.format(DateTimeFormatter.ofPattern("yyyy, MM, dd")),
                modifier = Modifier.padding(10.dp)
            )

            YenCustomCalendarView(onDateSelected = {
                selectedDate.value = it
            })

            Spacer(modifier = Modifier.size(8.dp))

            Row(
                modifier = Modifier
                    .align(Alignment.End)
                    .padding(bottom = 16.dp, end = 16.dp)
            ) {
                TextButton(onClick = onDismissRequest
                ) {
                    Text(text = "닫기"
                    )
                }

                TextButton(
                    onClick = {
                        yenViewModel.dateFlow.value = selectedDate.value.toString()
                        onDateSelected?.invoke(selectedDate.value, currentCardId)
                        onDismissRequest()
                    }
                ) {
                    Text(text = "선택")
                }
            }

        }

    }

}


@Composable
fun YenCustomCalendarView(onDateSelected: (LocalDate) -> Unit) {
    AndroidView(
        modifier = Modifier.wrapContentSize(),
        factory = {context ->
            CalendarView(context)
        },
        update = { view ->
            view.setOnDateChangeListener{ _, year, month, dayOfMonth ->
                Log.d("TAG", "CustomCalendarView: onDateSelected : ")
                onDateSelected(
                    LocalDate
                        .now()
                        .withMonth(month + 1)
                        .withYear(year)
                        .withDayOfMonth(dayOfMonth)
                )
            }

        })
}

















