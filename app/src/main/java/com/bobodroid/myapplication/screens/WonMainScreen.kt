package com.bobodroid.myapplication.screens

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.bobodroid.myapplication.components.Caldenders.*
import com.bobodroid.myapplication.extensions.toDecUs
import com.bobodroid.myapplication.extensions.toYen
import com.bobodroid.myapplication.lists.BuyRecordBox
import com.bobodroid.myapplication.lists.SellRecordBox
import com.bobodroid.myapplication.models.viewmodels.*
import com.bobodroid.myapplication.routes.*
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
fun WonMainScreen(wonViewModel: WonViewModel, routeAction: InvestRouteAction, allViewModel: AllViewModel) {


    var selectedCheckBoxId = wonViewModel.selectedCheckBoxId.collectAsState()

    val isDialogOpen = remember { mutableStateOf(false) }

    val resentExchangeRate = allViewModel.exchangeRateFlow.collectAsState()

    val time = Calendar.getInstance().time

    val formatter = SimpleDateFormat("yyyy-MM-dd")

    val today = formatter.format(time)

    val dateRecord = wonViewModel.dateFlow.collectAsState()

    var date = if(today == "") "$today" else {dateRecord.value}

    val snackbarHostState = remember { SnackbarHostState() }

    Column(modifier = Modifier
        .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally) {
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

                        Text(text = "USD: ${resentExchangeRate.value.exchangeRates?.usd}", fontSize = 20.sp)
                        Spacer(modifier = Modifier.width(10.dp))
                    }

                    Spacer(modifier = Modifier.height(5.dp))

                    Row(modifier = Modifier
                        .wrapContentSize(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically) {

                        Text(text = "JPY: ${resentExchangeRate.value.exchangeRates?.jpy}", fontSize = 20.sp)
                        Spacer(modifier = Modifier.width(10.dp))
                    }
                    Spacer(modifier = Modifier.height(15.dp))
                    Text(text = "업데이트된 환율: ${resentExchangeRate.value.createAt}")
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
                    wonViewModel.selectedCheckBoxId.value = it
                })

            InvestCheckBox(title = "매도",
                2, selectedCheckId = selectedCheckBoxId.value,
                selectCheckBoxAction = {
                    wonViewModel.selectedCheckBoxId.value = it
                }
            )
        }

        Column(
            horizontalAlignment = Alignment.End
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)) {
                if (selectedCheckBoxId.value == 1)
                    BuyRecordBox(wonViewModel, snackbarHostState = snackbarHostState)
                else

                    SellRecordBox(wonViewModel)
            }

//            Row(modifier = Modifier
//                .height(100.dp)
//                .fillMaxWidth()
//                .padding(start = 20.dp, bottom = 10.dp),
//                horizontalArrangement = Arrangement.End,
//                verticalAlignment = Alignment.CenterVertically
//
//            ) {
//
//                Row(modifier = Modifier.weight(1f)
//                ) {
//                    if(selectedCheckBoxId.value == 2) GetMoneyView(
//                        title = "총 수익",
//                        getDollar = "${dollartotal.value}",
//                        getYen = "${yentotal.value}",
//                        onClicked = { Log.d(TAG, "") },
//                        wonViewModel)
//                    else
//                        null
//                }
//
//                Spacer(modifier = Modifier.width(30.dp))
//
//                LargeFloatingActionButton(
//                    onClick = { routeAction.navTo(InvestRoute.WON_BUY)},
//                    containerColor = androidx.compose.material.MaterialTheme.colors.secondary,
//                    shape = RoundedCornerShape(16.dp),
//                    modifier = Modifier
//                        .padding(bottom = 10.dp, end = 20.dp)
//                        .size(60.dp),
//                ) {
//                    androidx.compose.material3.Icon(
//                        imageVector = Icons.Rounded.Add,
//                        contentDescription = "매수화면 가기",
//                        tint = Color.White
//                    )
//                }
//            }

        }

    }

}





//@OptIn(ExperimentalMaterialApi::class, ExperimentalMaterial3Api::class)
//@Composable
//fun GetMoneyView(title: String,
//                 getDollar: String,
//                 getYen: String,
//                 onClicked: () -> Unit,
//                 wonViewModel: WonViewModel
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
//    val callFirstDate = wonViewModel.sellStartDateFlow.collectAsState()
//
//    var callsecondDate = wonViewModel.sellEndDateFlow.collectAsState()
//
//    var firstDate = if (today == "") "$today" else {
//        callFirstDate.value
//    }
//
//    var secondDate = if (today == "") "$today" else {
//        callsecondDate.value
//    }
//
//
//
//    androidx.compose.material
//        .Card(
//            modifier = Modifier
//                .fillMaxWidth()
//                .height(85.dp),
//            onClick = onClicked
//        ) {
//            Row {
//                Column(
//                    modifier = Modifier
//                        .padding(top = 8.dp)
//                ) {
//
//                    Card(
//                        modifier = Modifier
//                            .width(150.dp)
//                            .height(30.dp)
//                            .background(Color.White),
//                        border = BorderStroke(1.dp, Color.Black),
//                        colors = CardDefaults.cardColors(
//                            contentColor = Color.Black,
//                            containerColor = Color.White
//                        ),
//                        onClick = {
//                            isFirstDialogOpen.value = !isFirstDialogOpen.value
//
//                        }) {
//                        Text(
//                            text = "시작: $firstDate",
//                            color = Color.Black,
//                            fontSize = 14.sp,
//                            textAlign = TextAlign.Center,
//                            modifier = Modifier
//                                .width(160.dp)
//                                .height(30.dp)
//                                .padding(start = 0.dp, top = 4.dp)
//                        )
//
//                        if (isFirstDialogOpen.value) {
//                            WonSellFirstDatePickerDialog(
//                                onDateSelected = null,
//                                onDismissRequest = {
//                                    isFirstDialogOpen.value = false
//                                }, id = 1,
//                                wonViewModel
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
//                        colors = CardDefaults.cardColors(
//                            contentColor = Color.Black,
//                            containerColor = Color.White
//                        ),
//                        onClick = {
//                            isSecondDialogOpen.value = !isSecondDialogOpen.value
//
//                        }) {
//                        Text(
//                            text = "종료: $secondDate",
//                            color = Color.Black,
//                            fontSize = 14.sp,
//                            textAlign = TextAlign.Center,
//                            modifier = Modifier
//                                .width(160.dp)
//                                .height(30.dp)
//                                .padding(start = 0.dp, top = 4.dp)
//                        )
//
//                        if (isSecondDialogOpen.value) {
//                            WonSellEndDatePickerDialog(
//                                onDateSelected = null,
//                                onDismissRequest = {
//                                    isSecondDialogOpen.value = false
//                                }, id = 1,
//                                wonViewModel
//                            )
//                        }
//                    }
//
//                }
//                Column(
//                    modifier = Modifier
//                        .fillMaxSize()
//                        .padding(top = 10.dp),
//                    horizontalAlignment = Alignment.CenterHorizontally
//                ) {
//                    Text(text = title)
//
//                    Spacer(modifier = Modifier.height(5.dp))
//                    Text(text = getDollar, color = Color.Red)
//
//                    Spacer(modifier = Modifier.height(2.dp))
//
//                    Text(text = getYen, color = Color.Red)
//                }
//            }
//        }
//}