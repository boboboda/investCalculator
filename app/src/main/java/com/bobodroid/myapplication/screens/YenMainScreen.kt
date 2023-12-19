package com.bobodroid.myapplication.screens

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.SnackbarHostState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bobodroid.myapplication.MainActivity
import com.bobodroid.myapplication.R
import com.bobodroid.myapplication.components.*
import com.bobodroid.myapplication.components.Caldenders.YenTopPickerDialog
import com.bobodroid.myapplication.lists.BuyRecordBox
import com.bobodroid.myapplication.lists.BuyYenRecordBox
import com.bobodroid.myapplication.lists.SellRecordBox
import com.bobodroid.myapplication.lists.SellYenRecordBox
import com.bobodroid.myapplication.models.viewmodels.*
import com.bobodroid.myapplication.routes.InvestRoute
import com.bobodroid.myapplication.routes.InvestRouteAction
import com.bobodroid.myapplication.routes.YenRoute
import com.bobodroid.myapplication.routes.YenRouteAction
import com.bobodroid.myapplication.ui.theme.InverstCalculatorTheme
import com.bobodroid.myapplication.ui.theme.TopButtonColor
import com.bobodroid.myapplication.ui.theme.TopButtonInColor
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.*
import kotlin.collections.ArrayList


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

    val resentExchangeRate = allViewModel.exChangeRateFlow.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    Column(
        modifier = Modifier
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Column(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .height(100.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Row(modifier = Modifier
                .weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center){
                //업데이트 날짜 값
                Column(modifier = Modifier
                    .wrapContentHeight()
                    .weight(0.6f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center) {
                    Row(modifier = Modifier
                        .wrapContentSize(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically) {

                        Text(text = "JPY: ${resentExchangeRate.value.exchangeRates?.jpy}", fontSize = 20.sp)
                        Spacer(modifier = Modifier.width(10.dp))
                    }
                    Spacer(modifier = Modifier.height(15.dp))
                    Text(text = "업데이트된 환율: ${resentExchangeRate.value.createAt}")
                    // 최신환율 업데이트 환율 같을 시 업데이트 통제
                }

            }

        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 25.dp)
                .padding(start = 10.dp),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically,
        ) {


            Spacer(modifier = Modifier.weight(1f))
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
        Column(
            horizontalAlignment = Alignment.End
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
            ) {

                if (selectedCheckBoxId.value == 1) {
                    BuyYenRecordBox(yenViewModel, snackbarHostState = snackbarHostState)
                } else {
                    SellYenRecordBox(yenViewModel)
                }

            }
        }

    }
            }






//@OptIn(ExperimentalMaterialApi::class, ExperimentalMaterial3Api::class)
//@Composable
//fun GetMoneyView(title: String,
//                 getMoney: String,
//                 onClicked: () -> Unit,
//                 yenViewModel: YenViewModel
//) {
//
//    val isFirstDialogOpen = remember { mutableStateOf(false) }
//
//    val isSecondDialogOpen = remember { mutableStateOf(false) }
//
//
//    val time = Calendar.getInstance().time
//
//    val formatter = SimpleDateFormat("yyyy-MM-dd")
//
//    val today = formatter.format(time)
//
//
//    androidx.compose.material
//        .Card(
//            modifier = Modifier
//                .fillMaxWidth()
//                .height(85.dp),
//            onClick = onClicked) {
//            Row {
//                Column(
//                    modifier = Modifier
//                        .padding(top = 8.dp)) {
//
//                    Card(
//                        modifier = Modifier
//                            .width(150.dp)
//                            .height(30.dp)
//                            .background(Color.White),
//                        border = BorderStroke(1.dp, Color.Black),
//                        colors = CardDefaults.cardColors(
//                            contentColor = Color.Black,
//                            containerColor = Color.White),
//                        onClick = { isFirstDialogOpen.value = !isFirstDialogOpen.value
//
//                        }) {
//                        Text(text = "시작: $firstDate", color = Color.Black, fontSize = 14.sp , textAlign = TextAlign.Center, modifier = Modifier
//                            .width(160.dp)
//                            .height(30.dp)
//                            .padding(start = 0.dp, top = 4.dp))
//
//                        if(isFirstDialogOpen.value) {
//                            YenSellFirstDatePickerDialog(onDateSelected = null,
//                                onDismissRequest = {
//                                    isFirstDialogOpen.value = false
//                                }, id = 1,
//                                yenViewModel
//                            )
//                        }
//                    }
//                    Spacer(modifier = Modifier.height(10.dp))
//
//                    Card(
//                        modifier = Modifier
//                            .width(150.dp)
//                            .height(30.dp)
//                            .background(Color.White),
//                        border = BorderStroke(1.dp, Color.Black),
//                        colors = CardDefaults.cardColors(contentColor = Color.Black, containerColor = Color.White),
//                        onClick = { isSecondDialogOpen.value = !isSecondDialogOpen.value
//
//                        }) {
//                        Text(
//                            text = "종료: $secondDate",
//                            color = Color.Black,
//                            fontSize = 14.sp ,
//                            textAlign = TextAlign.Center,
//                            modifier = Modifier
//                                .width(160.dp)
//                                .height(30.dp)
//                                .padding(start = 0.dp, top = 4.dp))
//
//                        if(isSecondDialogOpen.value) {
//                            YenSellEndDatePickerDialog(onDateSelected = null,
//                                onDismissRequest = {
//                                    isSecondDialogOpen.value = false
//                                }, id = 1,
//                                yenViewModel
//                            )
//                        }
//                    }
//
//                }
//                Column(modifier = Modifier
//                    .fillMaxSize()
//                    .padding(top = 10.dp),
//                    horizontalAlignment = Alignment.CenterHorizontally) {
//                    Text(text = title)
//
//                    Spacer(modifier = Modifier.height(15.dp))
//                    Text(text = getMoney, color = Color.Red)
//                }
//            }
//
//        }
//}







