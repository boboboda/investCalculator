package com.bobodroid.myapplication.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.SnackbarHostState
import androidx.compose.material.Card
import androidx.compose.material3.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import com.bobodroid.myapplication.components.*
import com.bobodroid.myapplication.lists.dollorList.addFocusCleaner
import com.bobodroid.myapplication.lists.yenList.BuyYenRecordBox
import com.bobodroid.myapplication.lists.yenList.SellYenRecordBox
import com.bobodroid.myapplication.models.viewmodels.*
import com.bobodroid.myapplication.routes.InvestRouteAction
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.*


@OptIn(ExperimentalMaterial3Api::class, ExperimentalUnitApi::class, ExperimentalMaterialApi::class)
@Composable
fun YenMainScreen
            (yenViewModel: YenViewModel,
             routeAction: InvestRouteAction,
             allViewModel: AllViewModel) {


    var selectedCheckBoxId = yenViewModel.selectedCheckBoxId.collectAsState()

    val isDialogOpen = remember { mutableStateOf(false) }

    val time = Calendar.getInstance().time

    val formatter = SimpleDateFormat("yyyy-MM-dd")

    val today = formatter.format(time)


    val selectedDate : MutableState<LocalDate?> = remember { mutableStateOf(LocalDate.now()) }

    val dateRecord = yenViewModel.dateFlow.collectAsState()

    var date = if(today == "") "$today" else {dateRecord.value}

//    val dateSelected = yenViewModel.changeDateAction.collectAsState()
//
//    val total = yenViewModel.total.collectAsState("")

    val recentExchangeRate = allViewModel.recentExChangeRateFlow.collectAsState()

    val reFreshDate = allViewModel.refreshDateFlow.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    val focusManager = LocalFocusManager.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .addFocusCleaner(focusManager),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 25.dp),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically,
        ) {

                InvestCheckBox(title = "매수",
                    1, selectedCheckId = selectedCheckBoxId.value,
                    selectCheckBoxAction = {
                        yenViewModel.selectedCheckBoxId.value = it
                    })

                InvestCheckBox(title = "매도",
                    2, selectedCheckId = selectedCheckBoxId.value,
                    selectCheckBoxAction = {
                        yenViewModel.selectedCheckBoxId.value = it
                    }
                )
            }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 25.dp),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                modifier = Modifier.padding(start = 10.dp),
                text = "예상수익 새로고침 시간: ${reFreshDate.value}",
                textAlign = TextAlign.Center)
        }

        Box(modifier = Modifier
            .fillMaxSize(),
            contentAlignment = Alignment.BottomCenter) {

            // Record item
            Column(
                modifier = Modifier
                    .fillMaxSize()
            ) {

                if (selectedCheckBoxId.value == 1) {
                    BuyYenRecordBox(
                        yenViewModel,
                        snackbarHostState)
                } else {
                    SellYenRecordBox(yenViewModel, snackbarHostState)
                }
            }
            //snackBar
            Row(modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
            ) {
                SnackbarHost(
                    hostState = snackbarHostState, modifier = Modifier,
                    snackbar = { snackBarData ->
                        Card(
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.5.dp, Color.Black),
                            modifier = Modifier
                                .padding(10.dp)
                                .fillMaxWidth(),
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp)
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







