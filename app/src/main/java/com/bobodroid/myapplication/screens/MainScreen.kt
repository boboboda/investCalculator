package com.bobodroid.myapplication.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.SnackbarHostState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material3.Card
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.*
import com.bobodroid.myapplication.components.*
import com.bobodroid.myapplication.components.Caldenders.RangeDateDialog
import com.bobodroid.myapplication.models.viewmodels.DollarViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import com.bobodroid.myapplication.models.viewmodels.AllViewModel
import com.bobodroid.myapplication.models.viewmodels.YenViewModel
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import com.bobodroid.myapplication.components.Dialogs.NoticeDialog
import kotlinx.coroutines.delay
import java.math.BigDecimal
import androidx.compose.material3.Text
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import com.bobodroid.myapplication.ui.theme.WelcomeScreenBackgroundColor
import androidx.compose.material3.Icon
import androidx.compose.ui.text.style.TextAlign
import com.bobodroid.myapplication.MainActivity
import com.bobodroid.myapplication.components.Dialogs.BottomSheetNumberField
import com.bobodroid.myapplication.components.Dialogs.BottomSheetRateNumberField
import com.bobodroid.myapplication.components.Dialogs.FloatPopupNumberView
import com.bobodroid.myapplication.components.Dialogs.PopupNumberView
import com.bobodroid.myapplication.components.Dialogs.RewardShowAskDialog
import com.bobodroid.myapplication.components.Dialogs.TextFieldDialog
import com.bobodroid.myapplication.components.Dialogs.ThanksDialog
import com.bobodroid.myapplication.components.admobs.showTargetRewardedAdvertisement
import com.bobodroid.myapplication.models.datamodels.roomDb.CurrencyType
import com.bobodroid.myapplication.models.viewmodels.MainViewModel
import com.bobodroid.myapplication.screens.record.DollarMainScreen
import com.bobodroid.myapplication.ui.theme.BottomSheetTitleColor
import com.bobodroid.myapplication.ui.theme.BuyColor
import com.bobodroid.myapplication.ui.theme.primaryColor
import com.bobodroid.myapplication.screens.record.YenMainScreen


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    dollarViewModel: DollarViewModel,
    yenViewModel: YenViewModel,
    mainViewModel: MainViewModel,
    activity: MainActivity
) {
    val mainUiState by mainViewModel.allUiState.collectAsState()

    val coroutineScope = rememberCoroutineScope()

    val showOpenDialog = remember { mutableStateOf(false) }

    val time = Calendar.getInstance().time

    val formatter = SimpleDateFormat("yyyy-MM-dd")

    val today = formatter.format(time)

    val dollarDateRecord = dollarViewModel.dateFlow.collectAsState()

    val yenDateRecord = yenViewModel.dateFlow.collectAsState()



    val bottomRefreshPadding = remember { mutableStateOf(5) }

    var isVisible by remember { mutableStateOf(true) }

    val context = LocalContext.current


    var dropdownExpanded by remember { mutableStateOf(false) }

    val mainScreenSnackBarHostState = remember { SnackbarHostState() }

    var showBottomSheet by remember { mutableStateOf(false) }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var exchangeRateMoneyType by remember {
        mutableStateOf(CurrencyType.USD)
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

    val groupList = when(exchangeRateMoneyType) {
        CurrencyType.USD-> { dollarViewModel.groupList.collectAsState() }
        CurrencyType.JPY-> { yenViewModel.groupList.collectAsState() }
    }

    val date = if (today == "") today else {
        when (exchangeRateMoneyType) {
            CurrencyType.USD -> {
                dollarDateRecord.value
            }

            CurrencyType.JPY -> {
                yenDateRecord.value
            }
        }
    }

    var groupAddDialog by remember { mutableStateOf(false) }

    var floatingButtonVisible by remember { mutableStateOf(true) }

    val rewardShowDialog = mainViewModel.rewardShowDialog.collectAsState()

    var thankShowingDialog by remember { mutableStateOf(false) }


    LaunchedEffect(key1 = Unit, block = {

        coroutineScope.launch {
            delay(1000)
            isVisible = false
            bottomRefreshPadding.value = 0
        }
    })

    LaunchedEffect(Unit) {
        while (true) {
            delay(5000) // 2초 대기
            floatingButtonVisible = false
        }
    }

    LaunchedEffect(key1 = showBottomSheet) {
        if (!showBottomSheet) {
            numberUserInput = ""
            rateNumberUserInput = ""
            numberPadPopViewIsVible = false
            ratePadPopViewIsVible = false
        }
    }
    Box(
        modifier = Modifier
            .fillMaxSize(),

        contentAlignment = Alignment.BottomCenter,
    )
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
                    when(exchangeRateMoneyType) {
                        CurrencyType.USD -> {
                            RateView(
                                title = "USD",
                                recentRate = "${mainUiState.recentRate.usd}",
                                createAt = mainUiState.recentRate.createAt
                            )
                        }
                        CurrencyType.JPY -> {
                            RateView(
                                title = "JPY",
                                recentRate = "${
                                    BigDecimal(mainUiState.recentRate.jpy).times(
                                        BigDecimal("100").setScale(-2),
                                    )
                                }",
                                createAt = "${mainUiState.recentRate.createAt}"
                            )
                        }
                    }
                }

                Column(modifier = Modifier.weight(0.3f)) {

                    Box {
                        TextButton(
                            onClick = { dropdownExpanded = true },
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = primaryColor
                            )
                        ) {
                            Text(
                                when(exchangeRateMoneyType) {
                                    CurrencyType.USD -> "달러(USD)"
                                    CurrencyType.JPY -> "엔화(JPY)"
                                }
                            )
                            Icon(Icons.Filled.ArrowDropDown, null)
                        }

                        DropdownMenu(
                            expanded = dropdownExpanded,
                            onDismissRequest = { dropdownExpanded = false }
                        ) {
                            DropdownMenuItem(
                                onClick = {
                                    exchangeRateMoneyType = CurrencyType.USD
                                    dropdownExpanded = false
                                },
                                text = { Text("달러(USD)") }
                            )
                            DropdownMenuItem(
                                onClick = {
                                    exchangeRateMoneyType = CurrencyType.JPY
                                    dropdownExpanded = false
                                },
                                text = { Text("엔화(JPY)") }
                            )
                        }
                    }

                }

//                    TopButtonView(allViewModel = allViewModel)
            }

            Column(
                Modifier
                    .weight(1f)
            ) {
                when(exchangeRateMoneyType) {
                    CurrencyType.USD -> {
                        DollarMainScreen(
                            dollarViewModel = dollarViewModel,
                            allViewModel = allViewModel
                        )

                    }
                    CurrencyType.JPY -> {
                        YenMainScreen(
                            yenViewModel = yenViewModel,
                            allViewModel = allViewModel,
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
                                label = exchangeRateMoneyType.koreanName,
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
                                    when(exchangeRateMoneyType) {
                                        CurrencyType.USD -> {
                                            dollarViewModel.moneyInputFlow.value = numberUserInput
                                            dollarViewModel.rateInputFlow.value = rateNumberUserInput

                                            dollarViewModel.buyDollarAdd(
                                                groupName = group
                                            )
                                        }
                                        CurrencyType.JPY -> {
                                            yenViewModel.moneyInputFlow.value = numberUserInput
                                            yenViewModel.rateInputFlow.value = rateNumberUserInput
                                            yenViewModel.buyAddRecord(
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
                                currencyType = CurrencyType.USD,
                                selectedCurrencyType = exchangeRateMoneyType,
                                selectAction = {exchangeRateMoneyType = it})

                            Spacer(modifier = Modifier.width(20.dp))

                            MoneyChButtonView(
                                mainText = "엔화",
                                currencyType = CurrencyType.JPY,
                                selectedCurrencyType = exchangeRateMoneyType,
                                selectAction = {exchangeRateMoneyType = it})

                        }



                        BottomSheetNumberField(
                            title = numberUserInput,
                            selectedState = numberPadPopViewIsVible,
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
                        when (exchangeRateMoneyType) {
                            CurrencyType.USD -> {
                                dollarViewModel.dateFlow.value = localDate.toString()
                            }
                            CurrencyType.JPY -> {
                                yenViewModel.dateFlow.value = localDate.toString()
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
                        when(exchangeRateMoneyType) {
                            CurrencyType.USD-> {
                                dollarViewModel.groupAdd(name)
                            }
                            CurrencyType.JPY-> {
                                yenViewModel.groupAdd(name)
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
                    callStartDate = mainUiState.startDate,
                    callEndDate = mainUiState.endDate,
                    startDateSelected = {
                        allViewModel.inputStartDate(it)
                    },
                    endDateSelected = {
                        allViewModel.inputEndDate(it)
                    },
                    onClicked = { selectedStartDate, selectedEndDate ->
                        coroutineScope.launch {
                            // 메인화면 조회기간
                            allViewModel.inputStartDate(selectedStartDate)
                            allViewModel.inputEndDate(selectedEndDate)

                            dollarViewModel.dateRangeInvoke(
                                selectedStartDate,
                                selectedEndDate
                            )

                            yenViewModel.dateRangeInvoke(
                                selectedStartDate,
                                selectedEndDate
                            )
                        }

                    },
                    allViewModel
                )
            }

            if (mainUiState.showNoticeDialog) {
                NoticeDialog(
                    content = mainUiState.notice.content ?: "",
                    onDismissRequest = {
                        allViewModel.closeNotice()
                    },
                    dateDelaySelected = {
                        coroutineScope.launch {
                            allViewModel.selectDelayDate()
                            delay(1000)
                        }
                    })
            }


            if(rewardShowDialog.value) {
                RewardShowAskDialog(
                    onDismissRequest = {
                        mainViewModel.rewardDelayDate()
                        mainViewModel.rewardShowDialog.value = it
                    },
                    onClicked = {
                        showTargetRewardedAdvertisement(activity, onAdDismissed = {
                            mainViewModel.rewardDelayDate()
                            mainViewModel.deleteBannerDelayDate()
                            mainViewModel.rewardShowDialog.value = false
                            thankShowingDialog = true
                        })
                    })
            }

            if(thankShowingDialog)
                ThanksDialog(onDismissRequest = { value ->
                    thankShowingDialog = value
                })
        }

        // 플로팅 버튼
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












