package com.bobodroid.myapplication.screens

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
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.*
import androidx.compose.material3.ButtonDefaults
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
import com.bobodroid.myapplication.MainActivity
import com.bobodroid.myapplication.components.Dialogs.ChargeDialog
import com.bobodroid.myapplication.components.Dialogs.NoticeDialog
import com.bobodroid.myapplication.components.Dialogs.RateRefreshDialog
import com.bobodroid.myapplication.components.admobs.BannerAd
import com.bobodroid.myapplication.components.admobs.showInterstitial
import com.bobodroid.myapplication.models.datamodels.ExchangeRate
import com.bobodroid.myapplication.models.datamodels.Rate
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

    val checkBoxState = dollarViewModel.selectedCheckBoxId.collectAsState()

    val coroutineScope = rememberCoroutineScope()

    val noticeState = allViewModel.noticeState.collectAsState()

    val noticeShowDialog = allViewModel.noticeShowDialog.collectAsState()

    val nowDate = allViewModel.dateFlow.collectAsState()

    val userSelectDate = allViewModel.localUserData.collectAsState()

    val localUserDate = userSelectDate.value.userShowNoticeDate

    val bottomRefreshPadding = remember { mutableStateOf(5) }

    var isVisible by remember { mutableStateOf(true) }

    val rateRefreshDialog = remember { mutableStateOf(false) }

    val context = LocalContext.current

    val recentRate = allViewModel.recentExChangeRateFlow.collectAsState()

    var bottomMenuButton by remember { mutableStateOf(false) }

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
                            checkboxController = checkBoxState.value
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
fun ContentIcon(allViewModel: AllViewModel,
                dollarViewModel: DollarViewModel,
                yenViewModel: YenViewModel,
                wonViewModel: WonViewModel,
                rowViewController: Int,
                checkboxController: Int,
                onClicked: ()-> Unit ) {

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
                    when(checkboxController){
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
                    when(checkboxController){
                        1-> {
                            GetMoneyView(
                                getMoney = "${totalYenExpectProfit.value}",
                                onClicked = { Log.d(TAG, "") },
                                allViewModel
                            )
                        }
                        2-> {
                            GetMoneyView(
                                getMoney = "${totalYenExpectProfit.value}",
                                onClicked = { Log.d(TAG, "") },
                                allViewModel
                            )
                        }
                    }
                }
                3 ->{
                    when(checkboxController){
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

    val stringGetMoney = if(getMoney == "") { "달력에서 조회를 해주세요"} else { getMoney}

    var endDate = allViewModel.endDateFlow.collectAsState()

    val date = if(startDate.value == "" && endDate.value == "")
        "조회기간: 모두" else "조회기간: ${startDate.value}~${endDate.value}"

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
            Text(text = "총수익: ")

            Text(text = "${stringGetMoney}", color =  profitColor)
        }


        Text(text = date, fontSize = 15.sp)
    }


}

