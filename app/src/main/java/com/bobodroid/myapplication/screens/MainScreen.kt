package com.bobodroid.myapplication.screens

import android.app.Activity
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideIn
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.DrawerState
import androidx.compose.material.MaterialTheme
import androidx.compose.material.SnackbarHostState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DateRange
import androidx.compose.material.icons.rounded.KeyboardArrowDown
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import com.bobodroid.myapplication.components.Dialogs.NoticeDialog
import com.bobodroid.myapplication.components.Dialogs.RateRefreshDialog
import com.bobodroid.myapplication.components.admobs.BannerAd
import com.bobodroid.myapplication.components.admobs.showInterstitial
import kotlinx.coroutines.delay
import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode
import androidx.compose.material3.Text
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import com.bobodroid.myapplication.ui.theme.WelcomeScreenBackgroundColor
import androidx.compose.material3.Icon
import androidx.compose.ui.text.style.TextAlign
import com.bobodroid.myapplication.components.Dialogs.BottomSheetNumberField
import com.bobodroid.myapplication.components.Dialogs.BottomSheetRateNumberField
import com.bobodroid.myapplication.components.Dialogs.FloatPopupNumberView
import com.bobodroid.myapplication.components.Dialogs.NumberField
import com.bobodroid.myapplication.components.Dialogs.PopupNumberView
import com.bobodroid.myapplication.components.Dialogs.RateNumberField
import com.bobodroid.myapplication.components.Dialogs.RewardShowAskDialog
import com.bobodroid.myapplication.components.Dialogs.TextFieldDialog
import com.bobodroid.myapplication.components.Dialogs.ThanksDialog
import com.bobodroid.myapplication.components.admobs.showTargetRewardedAdvertisement
import com.bobodroid.myapplication.routes.InvestRoute
import com.bobodroid.myapplication.routes.MainRouteAction
import com.bobodroid.myapplication.ui.theme.BottomSheetTitleColor
import com.bobodroid.myapplication.ui.theme.BuyColor
import com.bobodroid.myapplication.ui.theme.MainTopButtonColor

const val TAG = "메인"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    dollarViewModel: DollarViewModel,
    yenViewModel: YenViewModel,
    wonViewModel: WonViewModel,
    routeAction: MainRouteAction,
    allViewModel: AllViewModel,
    drawerState: DrawerState,
    activity: Activity
) {


    val showOpenDialog = remember { mutableStateOf(false) }

    val rowViewController = allViewModel.changeMoney.collectAsState()

    val time = Calendar.getInstance().time

    val formatter = SimpleDateFormat("yyyy-MM-dd")

    val today = formatter.format(time)

    val dollarDateRecord = dollarViewModel.dateFlow.collectAsState()

    val yenDateRecord = yenViewModel.dateFlow.collectAsState()

    val wonDateRecord = wonViewModel.dateFlow.collectAsState()

    val date = if (today == "") today else {
        when (rowViewController.value) {
            1 -> {
                dollarDateRecord.value
            }

            2 -> {
                yenDateRecord.value
            }

            3 -> {
                wonDateRecord.value
            }

            else -> {
                today
            }
        }
    }

    val callStartDate = allViewModel.startDateFlow.collectAsState()

    var callEndDate = allViewModel.endDateFlow.collectAsState()

    val coroutineScope = rememberCoroutineScope()

    val noticeShowDialog = allViewModel.noticeShowDialog.collectAsState()

    val noticeContent = allViewModel.noticeContent.collectAsState()

    val localUser = allViewModel.localUserFlow.collectAsState()

    val bottomRefreshPadding = remember { mutableStateOf(5) }

    var isVisible by remember { mutableStateOf(true) }

    val rateRefreshDialog = remember { mutableStateOf(false) }

    val context = LocalContext.current

    val recentRate = allViewModel.recentExchangeRateFlow.collectAsState()

    var dropdownExpanded by remember { mutableStateOf(false) }

    val mainScreenSnackBarHostState = remember { SnackbarHostState() }

    var showBottomSheet by remember { mutableStateOf(false) }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val dropdownMenuName = when (rowViewController.value) {
        1 -> {
            "달러"
        }

        2 -> {
            "엔화"
        }

        3 -> {
            "원화"
        }

        else -> {
            "오류"
        }
    }

    var numberPadPopViewIsVible by remember { mutableStateOf(false) }

    var ratePadPopViewIsVible by remember { mutableStateOf(false) }

    var numberUserInput by remember { mutableStateOf("") }

    var rateNumberUserInput by remember { mutableStateOf("") }

    val isBtnActive = numberUserInput.isNotEmpty() && rateNumberUserInput.isNotEmpty()

    var showDateDialog by remember { mutableStateOf(false) }

    // 그룹
    var group by remember { mutableStateOf("미지정") }

    var groupDropdownExpanded by remember { mutableStateOf(false) }

    val groupList = when(rowViewController.value) {
        1-> { dollarViewModel.groupList.collectAsState() }
        2-> { yenViewModel.groupList.collectAsState() }
        3-> { wonViewModel.groupList.collectAsState() }
        else -> {return}
    }

    var groupAddDialog by remember { mutableStateOf(false) }

    val moneyCgBtnSelected = wonViewModel.moneyCgBtnSelected.collectAsState()



    LaunchedEffect(key1 = Unit, block = {

        coroutineScope.launch {
            delay(3000)
            isVisible = false
            bottomRefreshPadding.value = 0
        }
    })

    LaunchedEffect(key1 = showBottomSheet) {
        if (!showBottomSheet) {
            numberUserInput = ""
            rateNumberUserInput = ""
            numberPadPopViewIsVible = false
            ratePadPopViewIsVible = false
        }
    }
    Column {

        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter)
        {

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            )
            {



                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 20.dp, end = 20.dp, bottom = 5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Column(
                        modifier = Modifier.weight(0.7f)
                    ) {
                        when (rowViewController.value) {
                            1 -> {
                                RateView(
                                    title = "USD",
                                    recentRate = "${recentRate.value.usd}",
                                    createAt = "${recentRate.value.createAt}"
                                )
                            }

                            2 -> {
                                RateView(
                                    title = "JPY",
                                    recentRate = "${
                                        BigDecimal(recentRate.value.jpy).times(
                                            BigDecimal("100").setScale(-2),
                                        )
                                    }",
                                    createAt = "${recentRate.value.createAt}"
                                )
                            }

                            3 -> {
                                RateView(
                                    title = "USD",
                                    recentRate = "${recentRate.value.usd}",
                                    subTitle = "JPY",
                                    subRecentRate = "${
                                        BigDecimal(recentRate.value.jpy).times(
                                            BigDecimal("100").setScale(-2)
                                        )
                                    }",
                                    createAt = "${recentRate.value.createAt}"
                                )
                            }
                        }
                    }

                    Column(modifier = Modifier.weight(0.3f)) {
                        Card(
                            colors = CardDefaults.cardColors(MainTopButtonColor),
                            elevation = CardDefaults.cardElevation(8.dp),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .height(50.dp)
                                .wrapContentWidth(),
                            onClick = {
                                if (!dropdownExpanded) dropdownExpanded = true
                            }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .wrapContentWidth()
                                    .padding(horizontal = 15.dp)
                                    .padding(vertical = 5.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = dropdownMenuName, fontSize = 18.sp)

                                Spacer(modifier = Modifier.width(5.dp))
                                Icon(
                                    imageVector = Icons.Rounded.KeyboardArrowDown,
                                    contentDescription = ""
                                )
                            }
                        }

                        DropdownMenu(
                            scrollState = rememberScrollState(),
                            modifier = Modifier
                                .wrapContentHeight()
                                .background(Color.White)
                                .heightIn(max = 200.dp)
                                .width(100.dp),
                            offset = DpOffset(x = 0.dp, y = 5.dp),
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
                                            text = "달러",
                                            color = Color.Black,
                                            fontSize = 13.sp
                                        )
                                    }
                                }, onClick = {

                                    allViewModel.changeMoney.value = 1
                                    dropdownExpanded = false

                                })


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
                                            text = "엔화",
                                            color = Color.Black,
                                            fontSize = 13.sp
                                        )
                                    }
                                }, onClick = {
                                    allViewModel.changeMoney.value = 2
                                    dropdownExpanded = false
                                })

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
                                            text = "원화",
                                            color = Color.Black,
                                            fontSize = 13.sp
                                        )
                                    }
                                }, onClick = {
                                    allViewModel.changeMoney.value = 3
                                    dropdownExpanded = false
                                })

                        }
                    }

//                    TopButtonView(allViewModel = allViewModel)
                }

                Column(
                    Modifier
                        .weight(1f)
                ) {
                    when (rowViewController.value) {
                        1 -> {
                            DollarMainScreen(
                                dollarViewModel = dollarViewModel,
                                allViewModel = allViewModel
                            )

                        }

                        2 -> {
                            YenMainScreen(
                                yenViewModel = yenViewModel,
                                allViewModel = allViewModel,
                            )
                        }

                        3 -> {
                            WonMainScreen(
                                wonViewModel = wonViewModel,
                                allViewModel = allViewModel
                            )
                        }
                    }
                }





                // bottomsheet
                if (showBottomSheet) {
                    ModalBottomSheet(
                        onDismissRequest = {
                            showBottomSheet = false
                        },
                        sheetState = sheetState
                    ) {
                        // Sheet content
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .wrapContentHeight()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = 20.dp, bottom = 20.dp, end = 20.dp),
                                horizontalArrangement = Arrangement.spacedBy(
                                    10.dp,
                                    alignment = Alignment.End
                                ),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CustomCard(
                                    label = dropdownMenuName,
                                    fontSize = 15,
                                    modifier = Modifier
                                        .width(60.dp)
                                        .height(50.dp)
                                        .padding(bottom = 5.dp),
                                    fontColor = Color.Black,
                                    cardColor = BottomSheetTitleColor
                                )

                                Spacer(Modifier.weight(1f))

                                Buttons(
                                    enabled = isBtnActive,
                                    onClicked = {
                                        when(rowViewController.value) {
                                            1-> {
                                                dollarViewModel.moneyInputFlow.value = numberUserInput
                                                dollarViewModel.rateInputFlow.value = rateNumberUserInput

                                                dollarViewModel.buyDollarAdd(
                                                    groupName = group
                                                )
                                            }
                                            2-> {
                                                yenViewModel.moneyInputFlow.value = numberUserInput
                                                yenViewModel.rateInputFlow.value = rateNumberUserInput
                                                yenViewModel.buyAddRecord(
                                                    groupName = group
                                                )
                                            }
                                            3-> {
                                                wonViewModel.moneyInputFlow.value = numberUserInput
                                                wonViewModel.rateInputFlow.value = rateNumberUserInput
                                                wonViewModel.buyAddRecord(
                                                    groupName = group
                                                )
                                            }
                                        }

                                        group = "미지정"
                                        showBottomSheet = false
                                    },
                                    color = BuyColor,
                                    fontColor = Color.Black,
                                    modifier = Modifier
                                ) {
                                    Text(text = "기록", fontSize = 15.sp)
                                }

                                Buttons(
                                    onClicked = { showBottomSheet = false },
                                    color = BuyColor,
                                    fontColor = Color.Black,
                                    modifier = Modifier,

                                    ) {
                                    Text(text = "닫기", fontSize = 15.sp)
                                }
                            }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = 20.dp, bottom = 20.dp, end = 10.dp),
                                horizontalArrangement = Arrangement.spacedBy(
                                    10.dp,
                                    alignment = Alignment.End
                                ),
                                verticalAlignment = Alignment.CenterVertically
                            ) {

                                Card(
                                    colors = CardDefaults.cardColors(WelcomeScreenBackgroundColor),
                                    elevation = CardDefaults.cardElevation(3.dp),
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
                                        Text(text = group)
                                    }
                                }


                                Box(
                                    modifier = Modifier
                                        .wrapContentSize(Alignment.TopEnd)
                                ) {
                                    Card(
                                        colors = CardDefaults.cardColors(WelcomeScreenBackgroundColor),
                                        elevation = CardDefaults.cardElevation(3.dp),
                                        shape = RoundedCornerShape(1.dp),
                                        modifier = Modifier
                                            .height(40.dp)
                                            .wrapContentWidth(),
                                        onClick = {
                                            groupDropdownExpanded = !groupDropdownExpanded
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
                                        expanded = groupDropdownExpanded,
                                        onDismissRequest = {
                                            groupDropdownExpanded = false
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
                                            color = Color.Gray.copy(alpha = 0.2f),
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
                                                    group = groupName
                                                    groupDropdownExpanded = false
                                                })
                                        }


                                    }
                                }

                                Spacer(modifier = Modifier.weight(1f))

                                Card(
                                    modifier = Modifier
                                        .width(160.dp)
                                        .padding(end = 10.dp)
                                        .height(40.dp),
                                    border = BorderStroke(1.dp, Color.Black),
                                    colors = CardDefaults.cardColors(
                                        contentColor = Color.Black,
                                        containerColor = Color.White
                                    ),
                                    onClick = {
                                        showDateDialog = true
                                    }) {

                                    Row(
                                        modifier = Modifier
                                            .fillMaxSize(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        Text(
                                            text = date,
                                            color = Color.Black,
                                            fontSize = 18.sp,
                                            textAlign = TextAlign.Center,
                                            modifier = Modifier
                                        )
                                    }

                                }
                            }

                            AnimatedVisibility(visible = rowViewController.value == 3) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(start = 20.dp, bottom = 20.dp, end = 10.dp),
                                    horizontalArrangement = Arrangement.spacedBy(
                                        10.dp,
                                        alignment = Alignment.Start
                                    ),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {

                                    MoneyChButtonView(
                                        mainText = "달러",
                                        id = 1,
                                        selectedId = moneyCgBtnSelected.value ,
                                        selectAction = {wonViewModel.moneyCgBtnSelected.value = it})

                                    Spacer(modifier = Modifier.width(20.dp))

                                    MoneyChButtonView(
                                        mainText = "엔화",
                                        id = 2,
                                        selectedId = moneyCgBtnSelected.value ,
                                        selectAction = {wonViewModel.moneyCgBtnSelected.value = it})

                                }
                            }



                            BottomSheetNumberField(
                                title = numberUserInput,
                                selectedState = numberPadPopViewIsVible,
                                selectedMoneyType = rowViewController.value,
                                buyMoneyType = moneyCgBtnSelected.value
                            ) {
                                coroutineScope.launch {
                                    if (ratePadPopViewIsVible) {
                                        ratePadPopViewIsVible = false
                                        delay(500)
                                        numberPadPopViewIsVible = true
                                    } else {
                                        numberPadPopViewIsVible = true
                                    }
                                }

                            }

                            Spacer(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(10.dp)
                            )

                            BottomSheetRateNumberField(
                                title = rateNumberUserInput,
                                selectedState = ratePadPopViewIsVible,
                                modifier = Modifier.padding(10.dp)
                            ) {
                                coroutineScope.launch {

                                    if (numberPadPopViewIsVible) {
                                        numberPadPopViewIsVible = false
                                        delay(500)
                                        ratePadPopViewIsVible = true
                                    } else {
                                        ratePadPopViewIsVible = true
                                    }


                                }

                            }


                            Box() {
                                Spacer(modifier = Modifier.height(50.dp))

                                Column {
                                    AnimatedVisibility(visible = numberPadPopViewIsVible) {
                                        PopupNumberView(
                                            onClicked = {
                                                coroutineScope.launch {
                                                    numberUserInput = it
                                                    numberPadPopViewIsVible = false
                                                    delay(700)
                                                    ratePadPopViewIsVible = true
                                                }

                                            },
                                            limitNumberLength = 10
                                        )
                                    }

                                    AnimatedVisibility(visible = ratePadPopViewIsVible) {
                                        FloatPopupNumberView(onClicked = {
                                            rateNumberUserInput = it
                                            ratePadPopViewIsVible = false
                                        })
                                    }
                                }
                            }


                        }
                    }
                }

                if (showDateDialog) {
                    MyDatePickerDialog(
                        onDateSelected = { localDate ->
                            when (rowViewController.value) {
                                1 -> {
                                    dollarViewModel.dateFlow.value = localDate.toString()
                                }
                                2 -> {
                                    yenViewModel.dateFlow.value = localDate.toString()
                                }
                                3 -> {
                                    wonViewModel.dateFlow.value = localDate.toString()
                                }
                            }
                        },
                        onDismissRequest = {
                            showDateDialog = false
                        }
                    )
                }

                if (groupAddDialog) {
                    TextFieldDialog(
                        onDismissRequest = {
                            groupAddDialog = it },
                        placeholder = "새 그룹명을 작성해주세요",
                        onClickedLabel = "추가",
                        closeButtonLabel = "닫기",
                        onClicked = { name ->
                            when(rowViewController.value) {
                                1-> {
                                    dollarViewModel.groupAdd(name)
                                }
                                2-> {
                                    yenViewModel.groupAdd(name)
                                }
                                3-> {
                                    wonViewModel.groupAdd(name)
                                }
                            }

                            groupAddDialog = false
                        })
                }

                if (showOpenDialog.value) {
                    RangeDateDialog(
                        onDismissRequest = {
                            showOpenDialog.value = it
                        },
                        callStartDate.value,
                        callEndDate.value,
                        startDateSelected = {
                            coroutineScope.launch {
                                allViewModel.startDateFlow.emit(it)
                            }
                        },
                        endDateSelected = {
                            coroutineScope.launch {
                                allViewModel.endDateFlow.emit(it)
                            }
                        },
                        onClicked = { selectedStartDate, selectedEndDate ->
                            coroutineScope.launch {
                                // 메인화면 조회기간
                                allViewModel.startDateFlow.emit(selectedStartDate)
                                allViewModel.endDateFlow.emit(selectedEndDate)

                                dollarViewModel.dateRangeInvoke(
                                    selectedStartDate,
                                    selectedEndDate
                                )

                                yenViewModel.dateRangeInvoke(
                                    selectedStartDate,
                                    selectedEndDate
                                )

                                wonViewModel.dateRangeInvoke(
                                    selectedStartDate,
                                    selectedEndDate
                                )

                            }

                        },
                        allViewModel
                    )
                }

                if (noticeShowDialog.value) {
                    NoticeDialog(
                        content = noticeContent.value,
                        onDismissRequest = { close ->
                            allViewModel.noticeShowDialog.value = close
                            allViewModel.openAppNoticeDateState.value = close
                        },
                        dateDelaySelected = {
                            coroutineScope.launch {
                                allViewModel.selectDelayDate(localUser.value)
                                delay(1000)
                            }
                        })
                }









            }

            ContentIcon(
                buyButtonClicked = {
                    showBottomSheet = true
                },
                totalMoneyCheckClicked = {
                    showOpenDialog.value = true
                },
                refreshClicked = {

                    allViewModel.reFreshProfit { recentRate ->
                        dollarViewModel.calculateProfit(recentRate)

                        yenViewModel.calculateProfit(recentRate)

                        wonViewModel.calculateProfit(recentRate)

                    }
                })

            //snackBar
            Row(
                modifier = Modifier
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
        }


    }
}












