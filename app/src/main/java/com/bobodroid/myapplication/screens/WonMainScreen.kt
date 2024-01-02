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
import androidx.compose.material3.Card
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
import androidx.compose.ui.text.font.FontWeight
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
import com.bobodroid.myapplication.lists.BuyWonRecordBox
import com.bobodroid.myapplication.lists.SellRecordBox
import com.bobodroid.myapplication.lists.SellWonRecordBox
import com.bobodroid.myapplication.models.viewmodels.*
import com.bobodroid.myapplication.routes.*
import com.bobodroid.myapplication.ui.theme.InverstCalculatorTheme
import com.bobodroid.myapplication.ui.theme.TopButtonColor
import com.bobodroid.myapplication.ui.theme.TopButtonInColor
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.*
import kotlin.collections.ArrayList


@OptIn(ExperimentalMaterial3Api::class, ExperimentalUnitApi::class, ExperimentalMaterialApi::class)
@Composable
fun WonMainScreen(wonViewModel: WonViewModel, routeAction: InvestRouteAction, allViewModel: AllViewModel) {


    var selectedCheckBoxId = wonViewModel.selectedCheckBoxId.collectAsState()

    val isDialogOpen = remember { mutableStateOf(false) }

    val recentExchangeRate = allViewModel.recentExChangeRateFlow.collectAsState()

    val time = Calendar.getInstance().time

    val formatter = SimpleDateFormat("yyyy-MM-dd")

    val today = formatter.format(time)

    val dateRecord = wonViewModel.dateFlow.collectAsState()

    var date = if(today == "") "$today" else {dateRecord.value}

    val reFreshDate = allViewModel.refreshDateFlow.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    Column(modifier = Modifier
        .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally) {

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
                    wonViewModel.selectedCheckBoxId.value = it
                })

            InvestCheckBox(title = "매도",
                2, selectedCheckId = selectedCheckBoxId.value,
                selectCheckBoxAction = {
                    wonViewModel.selectedCheckBoxId.value = it
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

        Column(
            horizontalAlignment = Alignment.End
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)) {
                if (selectedCheckBoxId.value == 1)
                    BuyWonRecordBox(wonViewModel, snackbarHostState = snackbarHostState)
                else

                    SellWonRecordBox(wonViewModel, snackbarHostState = snackbarHostState)
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