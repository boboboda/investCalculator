package com.bobodroid.myapplication.screens

import android.annotation.SuppressLint
import android.util.Log
import androidx.activity.viewModels
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.DrawerValue
import androidx.compose.material.MaterialTheme
import androidx.compose.material.SnackbarHostState
import androidx.compose.material.TextFieldColors
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.DateRange
import androidx.compose.material3.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.*
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bobodroid.myapplication.MainActivity
import com.bobodroid.myapplication.R
import com.bobodroid.myapplication.components.*
import com.bobodroid.myapplication.components.Caldenders.RangeDateDialog
import com.bobodroid.myapplication.extensions.toWon
import com.bobodroid.myapplication.models.viewmodels.DollarViewModel
import com.bobodroid.myapplication.models.viewmodels.SharedViewModel
import com.bobodroid.myapplication.ui.theme.InverstCalculatorTheme
import com.bobodroid.myapplication.ui.theme.TopButtonColor
import com.bobodroid.myapplication.ui.theme.TopButtonInColor
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.absoluteValue
import com.bobodroid.myapplication.lists.BuyRecordBox
import com.bobodroid.myapplication.lists.SellRecordBox
import com.bobodroid.myapplication.models.viewmodels.AllViewModel
import com.bobodroid.myapplication.models.viewmodels.WonViewModel
import com.bobodroid.myapplication.models.viewmodels.YenViewModel
import com.bobodroid.myapplication.routes.InvestRoute
import com.bobodroid.myapplication.routes.InvestRouteAction
import com.google.firebase.ktx.Firebase
import org.checkerframework.checker.units.qual.A

const val TAG = "메인"



@OptIn(ExperimentalMaterial3Api::class, ExperimentalUnitApi::class, ExperimentalMaterialApi::class)
@Composable
fun MainScreen(dollarViewModel: DollarViewModel,
               yenViewModel: YenViewModel,
               wonViewModel: WonViewModel,
               routeAction: InvestRouteAction,
               sharedViewModel: SharedViewModel,
               allViewModel: AllViewModel) {


    val showOpenDialog = remember { mutableStateOf(false) }

    val time = Calendar.getInstance().time

    val formatter = SimpleDateFormat("yyyy-MM-dd")

    val today = formatter.format(time)

    val selectedDate: MutableState<LocalDate?> = remember { mutableStateOf(LocalDate.now()) }

    val dateRecord = dollarViewModel.dateFlow.collectAsState()

    var date = if (today == "") "$today" else {
        dateRecord.value
    }

    val rowViewController = sharedViewModel.changeMoney.collectAsState()

    val drawerState = rememberDrawerState(DrawerValue.Closed)

    val scope = rememberCoroutineScope()

    val callStartDate = allViewModel.startDateFlow.collectAsState()

    var callEndDate = allViewModel.endDateFlow.collectAsState()

    val checkBoxState = dollarViewModel.selectedCheckBoxId.collectAsState()

    ModalDrawer(
        drawerState = drawerState,
        drawerShape = NavShape(widthOffset = 0.dp, scale = 0.6f),
        drawerContent = {
            DrawerCustom(allViewModel = allViewModel)
        },
    ) {
        Column(modifier = Modifier
            .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally)
        {

            Row(modifier = Modifier
                .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically) {
                TopTitleButton(sharedViewModel)

                Spacer(modifier = Modifier.weight(1f))

                IconButton(
                    imageVector = Icons.Outlined.Settings,
                    onClicked = {
                        scope.launch {
                            drawerState.open()
                        }

                    }, modifier = Modifier.padding(end = 10.dp))
            }

            Column(Modifier
                .weight(1f)) {
                when(rowViewController.value) {
                    1-> {
                        DollarMainScreen(
                            dollarViewModel = dollarViewModel,
                            allViewModel = allViewModel
                        )
                    }

                    2-> {
                        YenMainScreen(
                            yenViewModel = yenViewModel,
                            routeAction = routeAction,
                            allViewModel = allViewModel,
                        )
                    }

                    3-> {
                        WonMainScreen(wonViewModel = wonViewModel,
                            routeAction = routeAction)
                    }
                }
            }




            Row(
                modifier = Modifier
                    .height(120.dp)
                    .fillMaxWidth()
                    .padding(start = 20.dp, bottom = 10.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically

            ) {

//                Row(modifier = Modifier.weight(1f)
//                ) {
//                    if (selectedCheckBoxId.value == 2) GetMoneyView(
//                        title = "총 수익",
//                        getMoney = "${total.value}",
//                        onClicked = { Log.d(TAG, "") },
//                        dollarViewModel
//                    )
//                    else
//                        null
//                }


                FloatingActionButton(
                    onClick = {
                        showOpenDialog.value = true
                    },
                    containerColor = MaterialTheme.colors.secondary,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .padding(bottom = 10.dp, end = 20.dp)
                        .size(60.dp),
                ) {
                    androidx.compose.material3.Icon(
                        imageVector = Icons.Rounded.DateRange,
                        contentDescription = "날짜 범위 지정",
                        tint = Color.White
                    )
                }

                Spacer(modifier = Modifier.width(15.dp))

                FloatingActionButton(
                    onClick = {

                        when(rowViewController.value) {
                            1-> {
                                routeAction.navTo(InvestRoute.DOLLAR_BUY)
                            }
                            2-> {
                                routeAction.navTo(InvestRoute.YEN_BUY)
                            }
                            3-> {
                                routeAction.navTo(InvestRoute.WON_BUY)
                            }
                        }

                         },
                    containerColor = MaterialTheme.colors.secondary,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .padding(bottom = 10.dp, end = 20.dp)
                        .size(60.dp),
                ) {
                    androidx.compose.material3.Icon(
                        imageVector = Icons.Rounded.Add,
                        contentDescription = "매수화면 가기",
                        tint = Color.White
                    )
                }

                if(showOpenDialog.value) {
                    RangeDateDialog(
                        onDismissRequest = {
                            showOpenDialog.value = it
                        },
                        callStartDate.value,
                        callEndDate.value,
                        onClicked = { selectedStartDate, selectedEndDate ->

                            scope.launch {
                                allViewModel.startDateFlow.emit(selectedStartDate)
                                allViewModel.endDateFlow.emit(selectedEndDate)

                                when(checkBoxState.value) {
                                    1-> {
                                        dollarViewModel.buyDateRangeInvoke(
                                            DollarViewModel.Action.Buy,
                                            selectedStartDate,
                                            selectedEndDate
                                        )
                                    }

                                    2-> {
                                        dollarViewModel.buyDateRangeInvoke(
                                            DollarViewModel.Action.Sell,
                                            selectedStartDate,
                                            selectedEndDate
                                        )
                                    }
                                }

                            }

                        },
                        allViewModel
                    )
                }
            }



        }
    }
}




@Composable
fun DrawerCustom(
    allViewModel: AllViewModel) {

    val scope = rememberCoroutineScope()

    val resentRateDate = allViewModel.exchangeRateFlow.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxHeight()
            .requiredWidth(200.dp)
            .padding(start = 10.dp)
    ) {

        Spacer(
            modifier = Modifier
                .height(10.dp)
        )


        Text(text = "디바이스 ID: {}")

        Divider(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp)
        )

        Spacer(
            modifier = Modifier
                .height(20.dp)
        )

        Text(text = "현재 최신 환율: ${resentRateDate.value.createAt}",
            textAlign = TextAlign.Center)

        Spacer(
            modifier = Modifier
                .height(15.dp)
        )

        Row(modifier = Modifier
            .fillMaxWidth()
            .padding(start = 10.dp),
            horizontalArrangement = Arrangement.Start) {
            Text(text = "환율업데이트 횟수: 3(0)회")

            Spacer(modifier = Modifier.width(10.dp))
            CardButton(
                label = "충전",
                onClicked = {

                },
                buttonColor = TopButtonColor,
                fontColor = Color.Black,
                modifier = Modifier
                    .height(20.dp)
                    .width(40.dp),
                fontSize = 15
            )
        }

        Spacer(
            modifier = Modifier
                .height(10.dp)
        )

        Row(modifier = Modifier
            .fillMaxWidth()
            .padding(start = 10.dp),
            horizontalArrangement = Arrangement.Start) {
            Text(text = "스프레드: {}")

            Spacer(modifier = Modifier.width(10.dp))
            CardButton(
                label = "설정",
                onClicked = {

                },
                buttonColor = TopButtonColor,
                fontColor = Color.Black,
                modifier = Modifier
                    .height(20.dp)
                    .width(40.dp),
                fontSize = 15
            )
        }

        androidx.compose.material.Divider(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp)
        )




        Spacer(modifier = Modifier.weight(1f))

        androidx.compose.material.Text(text = "고객센터")
        androidx.compose.material.Text("개발자 이메일: kju9038@gmail.com")
        androidx.compose.material.Text("개발자 유튜브: ")
        androidx.compose.material.Text("문의: 000-0000-0000")
    }
}


