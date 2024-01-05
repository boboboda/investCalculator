package com.bobodroid.myapplication.screens

import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.with
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.DrawerState
import androidx.compose.material.MaterialTheme
import androidx.compose.material.SnackbarDuration
import androidx.compose.material.SnackbarHostState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DateRange
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.Card
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.core.content.ContextCompat.startActivity
import com.bobodroid.myapplication.components.Dialogs.ChargeDialog
import com.bobodroid.myapplication.components.Dialogs.CustomIdDialog
import com.bobodroid.myapplication.components.Dialogs.NoticeDialog
import com.bobodroid.myapplication.components.Dialogs.RateRefreshDialog
import com.bobodroid.myapplication.components.admobs.BannerAd
import com.bobodroid.myapplication.components.admobs.showInterstitial
import kotlinx.coroutines.delay
import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode

const val TAG = "메인"

@OptIn(ExperimentalFoundationApi::class, ExperimentalAnimationApi::class,
    ExperimentalMaterialApi::class
)
@Composable
fun MainScreen(dollarViewModel: DollarViewModel,
               yenViewModel: YenViewModel,
               wonViewModel: WonViewModel,
               routeAction: InvestRouteAction,
               allViewModel: AllViewModel,
               drawerState: DrawerState) {


    val showOpenDialog = remember { mutableStateOf(false) }

    val time = Calendar.getInstance().time

    val formatter = SimpleDateFormat("yyyy-MM-dd")

    val today = formatter.format(time)

    val dateRecord = dollarViewModel.dateFlow.collectAsState()

    var date = if (today == "") "$today" else {
        dateRecord.value
    }

    val rowViewController = allViewModel.changeMoney.collectAsState()

    val scope = rememberCoroutineScope()

    val callStartDate = allViewModel.startDateFlow.collectAsState()

    var callEndDate = allViewModel.endDateFlow.collectAsState()

    val drCheckBoxState = dollarViewModel.selectedCheckBoxId.collectAsState()

    val yenCheckBoxState = yenViewModel.selectedCheckBoxId.collectAsState()

    val wonCheckBoxState = wonViewModel.selectedCheckBoxId.collectAsState()

    val coroutineScope = rememberCoroutineScope()

    val noticeShowDialog = allViewModel.noticeShowDialog.collectAsState()

    val noticeContent = allViewModel.noticeContent.collectAsState()

    val nowDate = allViewModel.todayDateFlow.collectAsState()

    val localUser = allViewModel.localUserData.collectAsState()

    val bottomRefreshPadding = remember { mutableStateOf(5) }

    var isVisible by remember { mutableStateOf(true) }

    val rateRefreshDialog = remember { mutableStateOf(false) }

    val context = LocalContext.current

    val recentRate = allViewModel.recentExChangeRateFlow.collectAsState()

    var bottomMenuButton by remember { mutableStateOf(false) }

    val mainScreenSnackBarHostState = remember { SnackbarHostState() }

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
            DrawerCustom(allViewModel = allViewModel, drawerState = drawerState,  mainScreenSnackBarHostState =  mainScreenSnackBarHostState)
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

                when(rowViewController.value) {
                    1-> {
                        RateView(
                            title = "USD",
                            recentRate = "${recentRate.value.exchangeRates?.usd}",
                            createAt = "${recentRate.value.createAt}")
                    }
                    2-> {
                        RateView(
                            title = "JPY",
                            recentRate = "${BigDecimal(recentRate.value.exchangeRates?.jpy).times(
                                BigDecimal("100").setScale(-2),
                            )}",
                            createAt = "${recentRate.value.createAt}")
                    }
                    3-> {
                        RateView(
                            title = "USD",
                            recentRate = "${recentRate.value.exchangeRates?.usd}",
                            subTitle = "JPY",
                            subRecentRate = "${BigDecimal(recentRate.value.exchangeRates?.jpy).times(BigDecimal("100").setScale(-2))}",
                            createAt = "${recentRate.value.createAt}")
                    }
                }

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

            //snackBar
            Row(modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
            ) {
                SnackbarHost(
                    hostState = mainScreenSnackBarHostState, modifier = Modifier,
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
                                            mainScreenSnackBarHostState.currentSnackbarData?.dismiss()
                                        },
                                    text = "닫기",
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    })
            }


                AnimatedContent(
                    targetState = bottomMenuButton,
                    transitionSpec = {
                        fadeIn(animationSpec = tween(150, 150)) with
                                fadeOut(animationSpec = tween(150)) using
                                SizeTransform { initialSize, targetSize ->
                                    if (targetState) {
                                        keyframes {
                                            // Expand horizontally first.
                                            IntSize(targetSize.width, initialSize.height) at 150
                                            durationMillis = 300
                                        }
                                    } else {
                                        keyframes {
                                            // Shrink vertically first.
                                            IntSize(initialSize.width, targetSize.height) at 150
                                            durationMillis = 300
                                        }
                                    }
                                }
                    }, label = ""
                )

                { targetExpanded ->
                    if (targetExpanded) {
                        MainBottomView(
                            showOpenDialog = {
                                showOpenDialog.value = it
                            },
                            rateRefreshDialog = {
                                rateRefreshDialog.value = it
                            },
                            rowViewController = rowViewController.value,
                            routeAction = routeAction,
                            close = {
                                bottomMenuButton = it
                            }
                        )
                    } else {
                        ContentIcon(
                            allViewModel,
                            dollarViewModel,
                            yenViewModel,
                            wonViewModel,
                            rowViewController.value,
                            drCheckboxController = drCheckBoxState.value,
                            yenCheckboxController = yenCheckBoxState.value,
                            wonCheckboxController = wonCheckBoxState.value,
                        ) {
                            bottomMenuButton = true
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
                        useChance = { haveChance, recentRate ->

                            if(!haveChance) {
                                Log.d(TAG, "기회 있는 로직 실행")
                                dollarViewModel.calculateProfit(recentRate)

                                yenViewModel.calculateProfit(recentRate)

                                wonViewModel.calculateProfit(recentRate)

                                rateRefreshDialog.value = false

                            } else {
                                Log.d(TAG, "기회 없어서 광고 실행")
                                showInterstitial(context) {

                                    dollarViewModel.calculateProfit(recentRate)

                                    yenViewModel.calculateProfit(recentRate)

                                    wonViewModel.calculateProfit(recentRate)

                                    rateRefreshDialog.value = false

                                }
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
                                    when(drCheckBoxState.value) {
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
                                    when(yenCheckBoxState.value) {
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
                                    when(wonCheckBoxState.value) {
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
                    content = noticeContent.value,
                    onDismissRequest = { close ->
                        allViewModel.noticeShowDialog.value = close
                    },
                    dateDelaySelected = {
                        coroutineScope.launch {
                            allViewModel.selectDelayDate(localUser.value)
                            delay(1000)
                        }
                    },
                    allViewModel)

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
    drawerState: DrawerState,
    mainScreenSnackBarHostState: SnackbarHostState) {

    val coroutineScope = rememberCoroutineScope()

    val userData = allViewModel.localUserData.collectAsState()

    val chargeDialog = remember { mutableStateOf(false) }

    val localUser = allViewModel.localUserData.collectAsState()

    val freeChance = localUser.value.rateResetCount

    val payChance = localUser.value.rateAdCount

    var customDialog by remember { mutableStateOf(false) }

    val context = LocalContext.current

    val id = if(localUser.value.customId == "") localUser.value.id else localUser.value.customId

    val webIntent = Intent(Intent.ACTION_VIEW)
    webIntent.data = Uri.parse("https://cobusil.vercel.app")

    val webPostIntent = Intent(Intent.ACTION_VIEW)
    webPostIntent.data = Uri.parse("https://cobusil.vercel.app/release/postBoard/dollarRecord")

    Column(
        modifier = Modifier
            .fillMaxHeight()
            .fillMaxWidth(2.3f / 3)
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            CardIconButton(imageVector = Icons.Rounded.Close, onClicked = {
                coroutineScope.launch {
                    drawerState.close()
                }
            }, modifier = Modifier, buttonColor = Color.White)
        }

        Column(
            Modifier
                .fillMaxWidth()
                .padding(bottom = 20.dp),
            horizontalAlignment = Alignment.Start) {
            Text(
                modifier = Modifier.padding(start = 10.dp, end = 20.dp),
                text = "디바이스 ID: ${id}",
                overflow = TextOverflow.Ellipsis,
                maxLines = 1)

            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(top = 5.dp, start = 10.dp, end = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(5.dp, Alignment.End)) {

                CardButton(
                    label = "아이디 수정",
                    onClicked = {

                        customDialog = true

//                        coroutineScope.launch {
//                            drawerState.close()
//                            mainScreenSnackBarHostState.showSnackbar(
//                                "업데이트 예정입니다.",
//                                actionLabel = "닫기", SnackbarDuration.Short
//                            )
//                        }
                    },
                    buttonColor = TopButtonColor,
                    fontColor = Color.Black,
                    modifier = Modifier
                        .height(30.dp)
                        .width(80.dp),
                    fontSize = 15
                )

            }

            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(top = 5.dp, start = 10.dp, end = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(5.dp, Alignment.End)) {

                CardButton(
                    label = "클라우드 저장",
                    onClicked = {

                        if(localUser.value.customId == "") {
                            coroutineScope.launch {
                                drawerState.close()
                                mainScreenSnackBarHostState.showSnackbar(
                                    "아이디 수정 후 진행해 주세요",
                                    actionLabel = "닫기", SnackbarDuration.Short
                                )
                            }
                        } else {

                        }


                    },
                    buttonColor = TopButtonColor,
                    fontColor = Color.Black,
                    modifier = Modifier
                        .height(30.dp)
                        .width(80.dp),
                    fontSize = 15
                )

                CardButton(
                    label = "클라우드 불러오기",
                    onClicked = {
                        coroutineScope.launch {
                            drawerState.close()
                            mainScreenSnackBarHostState.showSnackbar(
                                "업데이트 예정입니다.",
                                actionLabel = "닫기", SnackbarDuration.Short
                            )
                        }
                    },
                    buttonColor = TopButtonColor,
                    fontColor = Color.Black,
                    modifier = Modifier
                        .height(30.dp)
                        .width(100.dp),
                    fontSize = 15
                )

            }
        }

        Divider(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 20.dp)
        )

        Row(modifier = Modifier
            .fillMaxWidth()
            .padding(start = 10.dp),
            horizontalArrangement = Arrangement.Start) {
            Text(text = "새로고침 업데이트 횟수: ${freeChance}(${payChance})회")

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
                    coroutineScope.launch {
                        drawerState.close()
                        mainScreenSnackBarHostState.showSnackbar(
                            "업데이트 예정입니다.",
                            actionLabel = "닫기", SnackbarDuration.Short
                        )
                    }
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

        if(customDialog) {
            CustomIdDialog(
                onDismissRequest = {
                                   customDialog = it
                },
                allViewModel)
        }
        
        Spacer(modifier = Modifier.height(10.dp))



        Column(modifier = Modifier
            .wrapContentSize()
            .padding(start = 5.dp, bottom = 20.dp)) {
            Column(modifier = Modifier
                .fillMaxWidth()
                .padding(),
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.spacedBy(5.dp)) {
                CardButton(
                    label = "공식사이트 가기",
                    onClicked = {
                        startActivity(context, webIntent, null)
                    },
                    fontSize = 15,
                    modifier = Modifier
                        .height(30.dp)
                        .width(120.dp),
                    fontColor = Color.Black,
                    buttonColor = TopButtonColor
                )

                CardButton(
                    label = "문의게시판 가기",
                    onClicked = {
                        startActivity(context, webPostIntent, null)
                    },
                    fontSize = 15,
                    modifier = Modifier
                        .height(30.dp)
                        .width(120.dp),
                    fontColor = Color.Black,
                    buttonColor = TopButtonColor
                )
            }

        }


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

@Composable
fun RateView(title: String,
             subTitle: String? = "",
             recentRate: String,
             subRecentRate: String? = "",
             createAt: String
) {

   val useTitle = if(subTitle == "") {
       "${title}: ${recentRate}"
    } else {
       "${title}: ${recentRate} | ${subTitle}: ${subRecentRate}"
    }

    Column(modifier = Modifier
        .wrapContentHeight(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center) {
        Row(modifier = Modifier
            .wrapContentSize(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically) {



            Text(text =  useTitle, fontSize = 20.sp)
        }
        Text(text = "업데이트된 환율: ${createAt}")
    }

}

@Composable
fun MainBottomView(
    showOpenDialog: (Boolean) -> Unit,
    rateRefreshDialog: (Boolean) -> Unit,
    rowViewController: Int,
    routeAction: InvestRouteAction,
    close: (Boolean) -> Unit
) {

    val scope = rememberCoroutineScope()

    var bottomRefreshPadding by remember { mutableStateOf(5) }

    var isVisible by remember { mutableStateOf(true) }

    LaunchedEffect(key1 = Unit, block = {
        scope.launch {
            delay(3000)
            isVisible = false
            bottomRefreshPadding = 0
        }
    })


    Column(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(top = 10.dp)
    ) {
        Row(
            modifier = Modifier
                .wrapContentHeight()
                .fillMaxWidth()
                .padding(start = 20.dp, bottom = 10.dp),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically

        ) {


            Spacer(modifier = Modifier.width(15.dp))

            FloatingActionButton(
                onClick = {

                    rateRefreshDialog.invoke(true)

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
                    horizontalArrangement = Arrangement.spacedBy(bottomRefreshPadding.dp),
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

            Spacer(modifier = Modifier.width(15.dp))


            FloatingActionButton(
                onClick = {

                    when(rowViewController) {
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
                    horizontalArrangement = Arrangement.spacedBy(bottomRefreshPadding.dp),
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




        }


        Row(
            modifier = Modifier
                .wrapContentHeight()
                .fillMaxWidth()
                .padding(start = 20.dp, bottom = 10.dp),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically

        ) {
            FloatingActionButton(
                onClick = {
                    showOpenDialog.invoke(true)
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
                    horizontalArrangement = Arrangement.spacedBy(bottomRefreshPadding.dp),
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

                    close.invoke(false)

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
                    horizontalArrangement = Arrangement.spacedBy(bottomRefreshPadding.dp),
                    verticalAlignment = Alignment.CenterVertically) {
                    androidx.compose.material3.Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = "닫기",
                        tint = Color.White
                    )
                    AnimatedVisibility(visible = isVisible) {
                        Text(text = "닫기", color = Color.White, modifier = Modifier)
                    }

                }

            }

        }
    }


}

@Composable
fun ContentIcon(
    allViewModel: AllViewModel,
    dollarViewModel: DollarViewModel,
    yenViewModel: YenViewModel,
    wonViewModel: WonViewModel,
    rowViewController: Int,
    drCheckboxController: Int,
    yenCheckboxController: Int,
    wonCheckboxController: Int,
    onClicked: () -> Unit,
) {

    val scope = rememberCoroutineScope()

    val totalDrExpectProfit = dollarViewModel.totalExpectProfit.collectAsState()

    val totalDrSellProfit = dollarViewModel.totalSellProfit.collectAsState()

    val totalYenExpectProfit = yenViewModel.totalExpectProfit.collectAsState()

    val totalYenSellProfit = yenViewModel.totalSellProfit.collectAsState()

    var bottomRefreshPadding by remember { mutableStateOf(5) }

    var isVisible by remember {
        mutableStateOf(true)
    }

    LaunchedEffect(key1 = Unit, block = {
        scope.launch {
            delay(3000)
            isVisible = false
            bottomRefreshPadding = 0
        }
    })

    Row(
        modifier = Modifier
            .height(120.dp)
            .fillMaxWidth()
            .padding(start = 20.dp, bottom = 10.dp),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically

    ) {

        Row(modifier = Modifier.weight(1f)
                ) {

            when(rowViewController) {
                1 ->{
                    when(drCheckboxController){
                        1-> {
                            GetMoneyView(
                                getMoney = "${totalDrExpectProfit.value}",
                                onClicked = { Log.d(TAG, "") },
                                allViewModel
                            )
                        }
                        2-> {
                            GetMoneyView(
                                getMoney = "${totalDrSellProfit.value}",
                                onClicked = { Log.d(TAG, "") },
                                allViewModel
                            )
                        }
                    }
                }
                2 ->{
                    when(yenCheckboxController){
                        1-> {
                            GetMoneyView(
                                getMoney = "${totalYenExpectProfit.value}",
                                onClicked = { Log.d(TAG, "") },
                                allViewModel
                            )
                        }
                        2-> {
                            GetMoneyView(
                                getMoney = "${totalYenSellProfit.value}",
                                onClicked = { Log.d(TAG, "") },
                                allViewModel
                            )
                        }
                    }
                }
                3 ->{
                    when(wonCheckboxController){
                        1-> {}
                        2-> {}
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = onClicked,
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
                horizontalArrangement = Arrangement.spacedBy(bottomRefreshPadding.dp),
                verticalAlignment = Alignment.CenterVertically) {
                androidx.compose.material3.Icon(
                    imageVector = Icons.Rounded.Menu,
                    contentDescription = "메뉴",
                    tint = Color.White
                )
                AnimatedVisibility(visible = isVisible) {
                    Text(text = "메뉴", color = Color.White, modifier = Modifier)
                }

            }

        }

    }



}


@OptIn(ExperimentalMaterialApi::class, ExperimentalMaterial3Api::class)
@Composable
fun GetMoneyView(
                 getMoney: String,
                 onClicked: () -> Unit,
                 allViewModel: AllViewModel
) {

    val mathContext = MathContext(0, RoundingMode.HALF_UP)

    val startDate = allViewModel.startDateFlow.collectAsState()

    val stringGetMoney = if(getMoney == "") "" else { getMoney}

    var endDate = allViewModel.endDateFlow.collectAsState()

    val date = if(startDate.value == "" && endDate.value == "")
        "조회기간: 달력해서 조회 해주세요" else "조회기간: ${startDate.value}~${endDate.value}"

    val profitColor = if(getMoney == ""){
        Color.Black
    } else {
        if(BigDecimal(getMoney.replace(",", ""), mathContext).signum() == -1) { Color.Blue} else {Color.Red}
    }

    Column(modifier = Modifier
        .fillMaxWidth()
        .padding(start = 15.dp, end = 15.dp),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(5.dp)) {

        Row {
            Text(text = "총 수익: ", fontSize = 15.sp)

            Text(text = "${stringGetMoney}",fontSize = 15.sp, color =  profitColor)
        }


        Text(text = date, fontSize = 15.sp)
    }


}

