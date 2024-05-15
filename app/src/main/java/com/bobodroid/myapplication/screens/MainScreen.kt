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
import com.bobodroid.myapplication.routes.InvestRoute
import com.bobodroid.myapplication.routes.InvestRouteAction
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
    routeAction: InvestRouteAction,
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

    val drBoxState = dollarViewModel.selectedBoxId.collectAsState()

    val yenCheckBoxState = yenViewModel.selectedBoxId.collectAsState()

    val wonCheckBoxState = wonViewModel.selectedBoxId.collectAsState()

    val coroutineScope = rememberCoroutineScope()

    val noticeShowDialog = allViewModel.noticeShowDialog.collectAsState()

    val rewardShowDialog = allViewModel.rewardShowDialog.collectAsState()

    val noticeContent = allViewModel.noticeContent.collectAsState()

    val localUser = allViewModel.localUserData.collectAsState()

    val bottomRefreshPadding = remember { mutableStateOf(5) }

    var isVisible by remember { mutableStateOf(true) }

    val rateRefreshDialog = remember { mutableStateOf(false) }

    val context = LocalContext.current

    val recentRate = allViewModel.recentExChangeRateFlow.collectAsState()

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

    var thankShowingDialog by remember { mutableStateOf(false) }

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

        ModalDrawer(
            drawerState = drawerState,
            gesturesEnabled = false,
            drawerShape = customShape(),
            modifier = Modifier.weight(1f),
            drawerContent = {
                DrawerCustom(
                    allViewModel,
                    dollarViewModel,
                    yenViewModel,
                    wonViewModel,
                    drawerState,
                    mainScreenSnackBarHostState,
                    activity
                )
            },
        ) {

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

                    when (rowViewController.value) {
                        1 -> {
                            RateView(
                                title = "USD",
                                recentRate = "${recentRate.value.exchangeRates?.usd}",
                                createAt = "${recentRate.value.createAt}"
                            )
                        }

                        2 -> {
                            RateView(
                                title = "JPY",
                                recentRate = "${
                                    BigDecimal(recentRate.value.exchangeRates?.jpy).times(
                                        BigDecimal("100").setScale(-2),
                                    )
                                }",
                                createAt = "${recentRate.value.createAt}"
                            )
                        }

                        3 -> {
                            RateView(
                                title = "USD",
                                recentRate = "${recentRate.value.exchangeRates?.usd}",
                                subTitle = "JPY",
                                subRecentRate = "${
                                    BigDecimal(recentRate.value.exchangeRates?.jpy).times(
                                        BigDecimal("100").setScale(-2)
                                    )
                                }",
                                createAt = "${recentRate.value.createAt}"
                            )
                        }
                    }

                    Spacer(modifier = Modifier.weight(1f))



                    Column() {
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
                                    .padding(horizontal = 20.dp)
                                    .padding(vertical = 5.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = dropdownMenuName, fontSize = 18.sp)

                                Spacer(modifier = Modifier.width(10.dp))
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
                                    label = "기록",
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
                                    modifier = Modifier,
                                    fontSize = 15
                                )

                                Buttons(
                                    label = "닫기",
                                    onClicked = { showBottomSheet = false },
                                    color = BuyColor,
                                    fontColor = Color.Black,
                                    modifier = Modifier,
                                    fontSize = 15
                                )
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


                ContentIcon(
                    allViewModel,
                    dollarViewModel,
                    yenViewModel,
                    wonViewModel,
                    rowViewController.value,
                    drCheckboxController = drBoxState.value,
                    yenCheckboxController = yenCheckBoxState.value,
                    wonCheckboxController = wonCheckBoxState.value,
                    buyButtonClicked = {
                        showBottomSheet = true
                    },
                    totalMoneyCheckClicked = {
                          showOpenDialog.value = true
                    },
                    refreshClicked = {
                        rateRefreshDialog.value = true
                    })

                if (rateRefreshDialog.value) {
                    RateRefreshDialog(
                        allViewModel,
                        onDismissRequest = {
                            rateRefreshDialog.value = it
                        }
                    ) {

                        allViewModel.useItem(
                            useChance = { haveChance, recentRate ->

                                if (!haveChance) {
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

                if(rewardShowDialog.value) {
                    RewardShowAskDialog(
                        onDismissRequest = {
                            allViewModel.rewardDelayDate(localUser.value)
                            allViewModel.rewardShowDialog.value = it
                    },
                        onClicked = {
                            showTargetRewardedAdvertisement(context, onAdDismissed = {
                                allViewModel.rewardDelayDate(localUser.value)
                                allViewModel.rewardShowDialog.value = false
                                thankShowingDialog = true
                            })
                        })
                }

                if(thankShowingDialog)
                ThanksDialog(onDismissRequest = {
                    thankShowingDialog = it
                })





            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            BannerAd()

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
fun RateView(
    title: String,
    subTitle: String? = "",
    recentRate: String,
    subRecentRate: String? = "",
    createAt: String
) {

    val useTitle = if (subTitle == "") {
        "${title}: ${recentRate}"
    } else {
        "${title}: ${recentRate} | ${subTitle}: ${subRecentRate}"
    }

    Column(
        modifier = Modifier
            .wrapContentHeight(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Row(
            modifier = Modifier
                .wrapContentSize(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {


            Text(text = useTitle, fontSize = 20.sp)
        }
        Text(text = "업데이트된 환율: ${createAt}")
    }

}

@Composable
fun MainBottomView(
    showOpenDialog: (Boolean) -> Unit,
    rateRefreshDialog: (Boolean) -> Unit,
    showBottomSheet: () -> Unit,
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
                onClick = showBottomSheet,
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
                    verticalAlignment = Alignment.CenterVertically
                ) {
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
                    verticalAlignment = Alignment.CenterVertically
                ) {
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

                    when (rowViewController) {
                        1 -> {
                            routeAction.navTo(InvestRoute.DOLLAR_BUY)
                        }

                        2 -> {
                            routeAction.navTo(InvestRoute.YEN_BUY)
                        }

                        3 -> {
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
                    verticalAlignment = Alignment.CenterVertically
                ) {
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
                    verticalAlignment = Alignment.CenterVertically
                ) {
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
                    verticalAlignment = Alignment.CenterVertically
                ) {
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
    buyButtonClicked: () -> Unit,
    totalMoneyCheckClicked: () -> Unit,
    refreshClicked: () -> Unit
) {

    val scope = rememberCoroutineScope()

    val totalDrSellProfit = dollarViewModel.totalSellProfit.collectAsState()

    val totalYenSellProfit = yenViewModel.totalSellProfit.collectAsState()

    var bottomRefreshPadding by remember { mutableStateOf(5) }

    var dropdownExpanded by remember { mutableStateOf(false) }

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

        Row(
            modifier = Modifier.weight(1f)
        ) {

            when (rowViewController) {
                1 -> {
                    GetMoneyView(
                        getMoney = "${totalDrSellProfit.value}",
                        onClicked = { Log.d(TAG, "") },
                        allViewModel
                    )
                }

                2 -> {
                    GetMoneyView(
                        getMoney = "${totalYenSellProfit.value}",
                        onClicked = { Log.d(TAG, "") },
                        allViewModel
                    )
                }

                3 -> {
                    when (wonCheckboxController) {
                        1 -> {}
                        2 -> {}
                    }
                }
            }
        }


        Box(
            modifier = Modifier
                .wrapContentSize(Alignment.TopEnd)
        ) {
            FloatingActionButton(
                onClick = {
                    dropdownExpanded = true
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
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Menu,
                        contentDescription = "메뉴",
                        tint = Color.White
                    )
                    AnimatedVisibility(visible = isVisible) {
                        Text(text = "메뉴", color = Color.White, modifier = Modifier)
                    }

                }

            }

            DropdownMenu(
                scrollState = rememberScrollState(),
                modifier = Modifier
                    .wrapContentSize(),
                offset = DpOffset(x = 23.dp, y = 5.dp),
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
                                text = "매수 기록",
                                color = Color.Black,
                                fontSize = 13.sp
                            )
                        }
                    }, onClick = {
                        dropdownExpanded = false
                        buyButtonClicked.invoke()
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
                                text = "총 수익 조회",
                                color = Color.Black,
                                fontSize = 13.sp
                            )
                        }
                    }, onClick = {
                        dropdownExpanded = false
                        totalMoneyCheckClicked.invoke()
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
                                text = "새로고침",
                                color = Color.Black,
                                fontSize = 13.sp
                            )
                        }
                    }, onClick = {
                        dropdownExpanded = false
                        refreshClicked.invoke()
                    })

            }

        }


    }


}


@Composable
fun GetMoneyView(
    getMoney: String,
    onClicked: () -> Unit,
    allViewModel: AllViewModel
) {

    val mathContext = MathContext(0, RoundingMode.HALF_UP)

    val startDate = allViewModel.startDateFlow.collectAsState()

    val stringGetMoney = if (getMoney == "") "" else {
        getMoney
    }

    var endDate = allViewModel.endDateFlow.collectAsState()

    val date = if (startDate.value == "" && endDate.value == "")
        "조회기간: 달력에서 조회 해주세요" else "조회기간: ${startDate.value}~${endDate.value}"

    val profitColor = if (getMoney == "") {
        Color.Black
    } else {
        if (BigDecimal(getMoney.replace(",", ""), mathContext).signum() == -1) {
            Color.Blue
        } else {
            Color.Red
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 15.dp, end = 15.dp),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ) {

        Row {
            Text(text = "매도 총 수익: ", fontSize = 15.sp)

            Text(text = "${stringGetMoney}", fontSize = 15.sp, color = profitColor)
        }


        Text(text = date, fontSize = 15.sp)
    }


}

