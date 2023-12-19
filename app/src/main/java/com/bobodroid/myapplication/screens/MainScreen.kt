package com.bobodroid.myapplication.screens

import android.util.Log
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.DrawerState
import androidx.compose.material.DrawerValue
import androidx.compose.material.MaterialTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.*
import com.bobodroid.myapplication.components.*
import com.bobodroid.myapplication.components.Caldenders.RangeDateDialog
import com.bobodroid.myapplication.models.viewmodels.DollarViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.*
import com.bobodroid.myapplication.models.viewmodels.AllViewModel
import com.bobodroid.myapplication.models.viewmodels.WonViewModel
import com.bobodroid.myapplication.models.viewmodels.YenViewModel
import com.bobodroid.myapplication.routes.InvestRoute
import com.bobodroid.myapplication.routes.InvestRouteAction
import com.bobodroid.myapplication.ui.theme.TopButtonColor
import androidx.compose.material3.Text
import androidx.compose.ui.text.style.TextAlign
import com.bobodroid.myapplication.components.Dialogs.NoticeDialog
import kotlinx.coroutines.delay

const val TAG = "메인"

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MainScreen(dollarViewModel: DollarViewModel,
               yenViewModel: YenViewModel,
               wonViewModel: WonViewModel,
               routeAction: InvestRouteAction,
               allViewModel: AllViewModel) {


    val showOpenDialog = remember { mutableStateOf(false) }

    val time = Calendar.getInstance().time

    val formatter = SimpleDateFormat("yyyy-MM-dd")

    val today = formatter.format(time)

    val dateRecord = dollarViewModel.dateFlow.collectAsState()

    var date = if (today == "") "$today" else {
        dateRecord.value
    }

    val rowViewController = allViewModel.changeMoney.collectAsState()

    val drawerState = rememberDrawerState(DrawerValue.Closed)

    val scope = rememberCoroutineScope()

    val callStartDate = allViewModel.startDateFlow.collectAsState()

    var callEndDate = allViewModel.endDateFlow.collectAsState()

    val checkBoxState = dollarViewModel.selectedCheckBoxId.collectAsState()

    val openResetShowDialog = remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()

    val noticeState = allViewModel.noticeState.collectAsState()

    val noticeShowDialog = remember { mutableStateOf(false) }

    val nowDate = allViewModel.dateFlow.collectAsState()

    val userSelectDate = allViewModel.localUserData.collectAsState()

    val localUserDate = userSelectDate.value.userShowNoticeDate

    LaunchedEffect(key1 = Unit, block = {
        coroutineScope.launch {
            // 저장한 날짜와 같으면 실행
            Log.d(TAG, "튜터리얼 날짜\n 오늘날짜: ${nowDate.value},  연기날짜: ${userSelectDate.value.userShowNoticeDate}")

            if(noticeState.value) {
                if(!localUserDate.isNullOrEmpty()) {
                    if(nowDate.value >= userSelectDate.value.userShowNoticeDate!!) {
                        Log.d(TAG,"오늘 날짜가 더 큽니다.")
                        noticeShowDialog.value = true
                    } else return@launch
                } else {
                    Log.d(TAG,"날짜 값이 없습니다.")
                }
            } else return@launch

        }
    })

    ModalDrawer(
        drawerState = drawerState,
        gesturesEnabled = false,
        drawerShape = customShape(),
        drawerContent = {
            DrawerCustom(allViewModel = allViewModel, drawerState = drawerState)
        },
    ) {

        Column(modifier = Modifier
            .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally)
        {

            Row(modifier = Modifier
                .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically) {

                Spacer(modifier = Modifier.weight(1f))

                CardTextIconButton(
                    label = "새로고침",
                    onClicked = {
                                allViewModel.resetRate { resentRate->

                                    dollarViewModel.requestRate(resentRate)

                                }
                    },
                    buttonColor = TopButtonColor,
                    fontColor = Color.Black,
                    modifier = Modifier
                        .width(80.dp)
                        .height(30.dp),
                    fontSize = 15
                )

                Spacer(modifier = Modifier.width(30.dp))

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
                                routeAction = routeAction,
                                allViewModel = allViewModel)
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

                                when(rowViewController.value) {
                                    1-> {
                                        when(checkBoxState.value) {
                                            1-> {
                                                dollarViewModel.dateRangeInvoke(
                                                    DollarViewModel.DrAction.Buy,
                                                    selectedStartDate,
                                                    selectedEndDate
                                                )
                                            }

                                            2-> {
                                                dollarViewModel.dateRangeInvoke(
                                                    DollarViewModel.DrAction.Sell,
                                                    selectedStartDate,
                                                    selectedEndDate
                                                )
                                            }
                                        }
                                    }
                                    2-> {
                                        when(checkBoxState.value) {
                                            1-> {
                                                yenViewModel.dateRangeInvoke(
                                                    YenViewModel.YenAction.Buy,
                                                    selectedStartDate,
                                                    selectedEndDate
                                                )
                                            }

                                            2-> {
                                                yenViewModel.dateRangeInvoke(
                                                    YenViewModel.YenAction.Sell,
                                                    selectedStartDate,
                                                    selectedEndDate
                                                )
                                            }
                                        }
                                    }
                                    3-> {
                                        when(checkBoxState.value) {
                                            1-> {
                                                wonViewModel.dateRangeInvoke(
                                                    WonViewModel.WonAction.Buy,
                                                    selectedStartDate,
                                                    selectedEndDate
                                                )
                                            }

                                            2-> {
                                                wonViewModel.dateRangeInvoke(
                                                    WonViewModel.WonAction.Sell,
                                                    selectedStartDate,
                                                    selectedEndDate
                                                )
                                            }
                                        }
                                    }
                                }



                            }

                        },
                        allViewModel
                    )
                }

                if (noticeShowDialog.value)
                    NoticeDialog(
                        onDismissRequest = { close ->
                            allViewModel.noticeState.value = close
                            noticeShowDialog.value = close
                                           },
                        dateDelaySelected = {
                            coroutineScope.launch {
                                allViewModel.selectDelayDate()
                                delay(1000)
                                Log.d(TAG, "일주일 연기날짜 ${userSelectDate.value}")
                            }
                        },
                        allViewModel)
            }

        }
    }
}




@Composable
fun DrawerCustom(
    allViewModel: AllViewModel,
    drawerState: DrawerState) {

    val scope = rememberCoroutineScope()

    val resentRateDate = allViewModel.recentExChangeRateFlow.collectAsState()

    val userData = allViewModel.localUserData.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxHeight()
            .fillMaxWidth(2.3f / 3)
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            CardIconButton(imageVector = Icons.Rounded.Close, onClicked = {
                scope.launch {
                    drawerState.close()
                }
            }, modifier = Modifier, buttonColor = Color.White)
        }

        Text(
            modifier = Modifier.padding(start = 10.dp),
            text = "디바이스 ID: ${userData.value.id}")

        Spacer(
            modifier = Modifier
                .height(20.dp)
        )


        Divider(
            modifier = Modifier
                .fillMaxWidth()
        )

        Spacer(
            modifier = Modifier
                .height(20.dp)
        )

        Text(
            modifier = Modifier.padding(start = 10.dp),
            text = "현재 최신 환율: ${resentRateDate.value.createAt}",
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

        Divider(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp)
        )




        Spacer(modifier = Modifier.weight(1f))

        Text(text = "고객센터")
        Text("개발자 이메일: kju9038@gmail.com")
//        androidx.compose.material.Text("개발자 유튜브: ")
        Text("문의: 000-0000-0000")
    }
}


@Composable
fun customShape() = object : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        return Outline.Rectangle(
            Rect(
                left = 0f,
                top = 0f,
                right = size.width * 2.3f / 3,
                bottom = size.height
            )
        )
    }
}

