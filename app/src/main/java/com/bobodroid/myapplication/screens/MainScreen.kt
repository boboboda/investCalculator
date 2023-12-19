package com.bobodroid.myapplication.screens

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.DrawerState
import androidx.compose.material.DrawerValue
import androidx.compose.material.MaterialTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DateRange
import androidx.compose.material.icons.rounded.Refresh
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
import java.util.*
import com.bobodroid.myapplication.models.viewmodels.AllViewModel
import com.bobodroid.myapplication.models.viewmodels.WonViewModel
import com.bobodroid.myapplication.models.viewmodels.YenViewModel
import com.bobodroid.myapplication.routes.InvestRoute
import com.bobodroid.myapplication.routes.InvestRouteAction
import com.bobodroid.myapplication.ui.theme.TopButtonColor
import androidx.compose.material3.Text
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import com.bobodroid.myapplication.components.Dialogs.ChargeDialog
import com.bobodroid.myapplication.components.Dialogs.NoticeDialog
import com.bobodroid.myapplication.components.Dialogs.RateRefreshDialog
import com.bobodroid.myapplication.components.admobs.BannerAd
import com.bobodroid.myapplication.components.admobs.showInterstitial
import kotlinx.coroutines.delay

const val TAG = "메인"

@OptIn(ExperimentalFoundationApi::class, ExperimentalAnimationApi::class)
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

//    val openResetShowDialog = remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()

    val noticeState = allViewModel.noticeState.collectAsState()

    val noticeShowDialog = allViewModel.noticeShowDialog.collectAsState()

    val nowDate = allViewModel.dateFlow.collectAsState()

    val userSelectDate = allViewModel.localUserData.collectAsState()

    val localUserDate = userSelectDate.value.userShowNoticeDate

    val bottomRefresh = remember { mutableStateOf("새로고침") }

    val bottomRefreshPadding = remember { mutableStateOf(5) }

    var isVisible by remember { mutableStateOf(true) }

    val rateRefreshDialog = remember { mutableStateOf(false) }

    val context = LocalContext.current

    LaunchedEffect(key1 = Unit, block = {

        coroutineScope.launch {
            delay(3000)
//            bottomRefresh.value = ""
            isVisible = false
            bottomRefreshPadding.value = 0
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
            .fillMaxSize()
            .padding(top = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally)
        {



            Row(modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 20.dp),
                verticalAlignment = Alignment.CenterVertically) {

                IconButton(
                    imageVector = Icons.Outlined.Menu,
                    onClicked = {
                        scope.launch {
                            drawerState.open()
                        }

                    }, modifier = Modifier.padding(end = 10.dp))



                Spacer(modifier = Modifier.weight(1f))


                TopButtonView(allViewModel = allViewModel)


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
                        .height(60.dp)
                        .wrapContentWidth(),
                ) {

                    Row(
                        Modifier
                            .wrapContentSize()
                            .padding(start = 17.dp, end = 17.dp),
                        horizontalArrangement = Arrangement.spacedBy(bottomRefreshPadding.value.dp),
                        verticalAlignment = Alignment.CenterVertically) {
                        androidx.compose.material3.Icon(
                            imageVector = Icons.Rounded.DateRange,
                            contentDescription = "날짜 범위 지정",
                            tint = Color.White
                        )
                        AnimatedVisibility(visible = isVisible) {
                            Text(text = "조회", color = Color.White, modifier = Modifier)
                        }

                    }


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
                        .height(60.dp)
                        .wrapContentWidth(),
                    ) {
                    Row(
                        Modifier
                            .wrapContentSize()
                            .padding(start = 17.dp, end = 17.dp),
                        horizontalArrangement = Arrangement.spacedBy(bottomRefreshPadding.value.dp),
                        verticalAlignment = Alignment.CenterVertically) {
                        androidx.compose.material3.Icon(
                            imageVector = Icons.Rounded.Add,
                            contentDescription = "매수화면",
                            tint = Color.White
                        )
                        AnimatedVisibility(visible = isVisible) {
                            Text(text = "매수", color = Color.White, modifier = Modifier)
                        }

                    }
                }

                Spacer(modifier = Modifier.width(15.dp))


                FloatingActionButton(
                    onClick = {

                        rateRefreshDialog.value = true

                    },
                    containerColor = MaterialTheme.colors.secondary,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .padding(bottom = 10.dp, end = 20.dp)
                        .height(60.dp)
                        .wrapContentWidth(),
                ) {
                    Row(
                        Modifier
                            .wrapContentSize()
                            .padding(start = 17.dp, end = 17.dp),
                        horizontalArrangement = Arrangement.spacedBy(bottomRefreshPadding.value.dp),
                        verticalAlignment = Alignment.CenterVertically) {
                        androidx.compose.material3.Icon(
                            imageVector = Icons.Rounded.Refresh,
                            contentDescription = "새로고침",
                            tint = Color.White
                        )
                        AnimatedVisibility(visible = isVisible) {
                            Text(text = "새로고침", color = Color.White, modifier = Modifier)
                        }

                    }

                }

                if(rateRefreshDialog.value) {
                    RateRefreshDialog(
                        allViewModel,
                        onDismissRequest = {
                        rateRefreshDialog.value = it
                    }
                        ) {

                        allViewModel.useItem(
                            useChance = {
                                allViewModel.resetRate { resentRate->

                                    dollarViewModel.requestRate(resentRate)

                                    yenViewModel.requestRate(resentRate)

                                    wonViewModel.requestRate(resentRate)

                                    rateRefreshDialog.value = false
                                }
                            },
                            notExistChance = {
                                showInterstitial(context) {
                                    allViewModel.resetRate { resentRate->

                                        dollarViewModel.requestRate(resentRate)

                                        yenViewModel.requestRate(resentRate)

                                        wonViewModel.requestRate(resentRate)
                                    }

                                    rateRefreshDialog.value = false
                                }
                            }
                        )
                    }



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
                            allViewModel.noticeShowDialog.value = close
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


            Column(
                modifier = Modifier
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally) {
                BannerAd()
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

    val chargeDialog = remember { mutableStateOf(false) }

    val localUser = allViewModel.localUserData.collectAsState()

    val freeChance = localUser.value.rateResetCount

    val payChance = localUser.value.rateAdCount

    val context = LocalContext.current

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
            Text(text = "환율업데이트 횟수: ${freeChance}(${payChance})회")

            Spacer(modifier = Modifier.width(10.dp))
            CardButton(
                label = "충전",
                onClicked = {
                            chargeDialog.value = true
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

        if(chargeDialog.value) {
            ChargeDialog(onDismissRequest = {
                chargeDialog.value = it
            }) {
                showInterstitial(context) {
                    allViewModel.chargeChance()

                    chargeDialog.value = false
                }
            }
        }




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

