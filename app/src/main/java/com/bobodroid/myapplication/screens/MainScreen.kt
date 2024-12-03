package com.bobodroid.myapplication.screens

import android.app.Activity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.SnackbarDuration
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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import com.bobodroid.myapplication.MainActivity
import com.bobodroid.myapplication.R
import com.bobodroid.myapplication.components.Dialogs.BottomSheetNumberField
import com.bobodroid.myapplication.components.Dialogs.BottomSheetRateNumberField
import com.bobodroid.myapplication.components.Dialogs.FloatPopupNumberView
import com.bobodroid.myapplication.components.Dialogs.PopupNumberView
import com.bobodroid.myapplication.components.Dialogs.RewardShowAskDialog
import com.bobodroid.myapplication.components.Dialogs.SellDialogEvent
import com.bobodroid.myapplication.components.Dialogs.SellResultDialog
import com.bobodroid.myapplication.components.Dialogs.TextFieldDialog
import com.bobodroid.myapplication.components.Dialogs.ThanksDialog
import com.bobodroid.myapplication.components.admobs.BannerAd
import com.bobodroid.myapplication.components.admobs.showTargetRewardedAdvertisement
import com.bobodroid.myapplication.components.mainComponents.BottomSheetEvent
import com.bobodroid.myapplication.components.mainComponents.EditBottomSheet
import com.bobodroid.myapplication.components.mainComponents.EditBottomSheetEvent
import com.bobodroid.myapplication.components.mainComponents.MainBottomSheet
import com.bobodroid.myapplication.components.mainComponents.MainHeader
import com.bobodroid.myapplication.components.mainComponents.RateBottomSheet
import com.bobodroid.myapplication.components.mainComponents.RateBottomSheetEvent
import com.bobodroid.myapplication.lists.dollorList.RecordListEvent
import com.bobodroid.myapplication.lists.dollorList.RecordListView
import com.bobodroid.myapplication.models.datamodels.roomDb.CurrencyType
import com.bobodroid.myapplication.models.datamodels.roomDb.ForeignCurrencyRecord
import com.bobodroid.myapplication.models.viewmodels.MainViewModel
import com.bobodroid.myapplication.ui.theme.BottomSheetTitleColor
import com.bobodroid.myapplication.ui.theme.BuyColor
import com.bobodroid.myapplication.ui.theme.primaryColor


@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun MainScreen(
    mainViewModel: MainViewModel,
    activity: Activity
) {
    val mainUiState by mainViewModel.mainUiState.collectAsState()

    val adUiState by mainViewModel.adUiState.collectAsState()

    val recordListUiState by mainViewModel.recordListUiState.collectAsState()

    val noticeUiState by mainViewModel.noticeUiState.collectAsState()

    val dateRangeUiState by mainViewModel.dateRangeUiState.collectAsState()

    val coroutineScope = rememberCoroutineScope()

    val bottomRefreshPadding = remember { mutableStateOf(5) }

    var isVisible by remember { mutableStateOf(true) }

    val context = LocalContext.current


    val mainScreenSnackBarHostState = remember { SnackbarHostState() }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val rateSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val editSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var showBottomSheet by remember { mutableStateOf(false) }

    var showDateDialog by remember { mutableStateOf(false) }

    var groupAddDialog by remember { mutableStateOf(false) }

    var thankShowingDialog by remember { mutableStateOf(false) }

    // 리스트
    // 매도기록 노출 여부
    var hideSellRecordState by remember { mutableStateOf(false) }

    val records = mainViewModel.getCurrentRecords()

    val focusManager = LocalFocusManager.current

//    val reFreshDate = allViewModel.refreshDateFlow.collectAsState()

//    val totalDrSellProfit = dollarViewModel.totalSellProfit.collectAsState()

//    val bannerState = allViewModel.deleteBannerStateFlow.collectAsState()

    LaunchedEffect(key1 = Unit, block = {

        coroutineScope.launch {
            delay(1000)
            isVisible = false
            bottomRefreshPadding.value = 0
        }
    })

    LaunchedEffect(mainUiState.showRateBottomSheet) {
        if (mainUiState.showRateBottomSheet) {
            rateSheetState.show()
        } else {
            rateSheetState.hide()
        }
    }

    LaunchedEffect(mainUiState.showEditBottomSheet) {
        if (mainUiState.showEditBottomSheet) {
            editSheetState.show()
        } else {
            editSheetState.hide()
        }
    }

    LaunchedEffect(key1 = true) {
        mainViewModel.mainSnackBarState.collect { message ->
            mainScreenSnackBarHostState.showSnackbar(
                message = message,
                actionLabel = "닫기",
                duration = SnackbarDuration.Short
            )
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
            MainHeader(
                mainUiState = mainUiState,
                adUiState = adUiState,
                updateCurrentForeignCurrency = {
                    mainViewModel.updateCurrentForeignCurrency(it)
                },
                hideSellRecordState,
                onHide = {
                    hideSellRecordState = it
                }

            )

            Column(
                Modifier
                    .weight(1f)
                    .addFocusCleaner(focusManager)
            ) {
                RecordListView(
                    mainScreenSnackBarHostState,
                    records,
                    hideSellRecordState = hideSellRecordState,
                    onEvent = {event ->

                        when(event) {
                            is RecordListEvent.SellRecord -> {
                               val record = event.data
                                mainViewModel.handleMainEvent(MainEvent.ShowRateBottomSheet(record))
                            }

                            else -> mainViewModel.handleRecordEvent(event)
                        }


                    })

            }

            // bottomsheet
            if (showBottomSheet) {
                MainBottomSheet(
                    sheetState,
                    recordListUiState,
                    mainUiState,
                    onEvent = { bottomSheetEvent ->
                        when (bottomSheetEvent) {
                            is BottomSheetEvent.DismissSheet -> {
                                coroutineScope.launch {
                                    sheetState.hide()
                                    showBottomSheet = false
                                }
                            }
                            is BottomSheetEvent.OnRecordAdd -> {
                                mainViewModel.addRecord(bottomSheetEvent.money, bottomSheetEvent.rate, bottomSheetEvent.group)
                            }
                            is BottomSheetEvent.OnGroupSelect -> {
                                groupAddDialog = true
                            }
                            is BottomSheetEvent.OnDateSelect -> {
                                showDateDialog = true
                            }
                            is BottomSheetEvent.OnCurrencyTypeChange -> {
                                mainViewModel.updateCurrentForeignCurrency(bottomSheetEvent.currencyType)
                            }
                        }
                    }
                )
            }


            // sellBottomSheet
            if (mainUiState.showRateBottomSheet) {
                RateBottomSheet(
                    rateSheetState,
                    sellDate = mainUiState.selectedDate,
                    onEvent = { event ->
                        when(event) {
                            RateBottomSheetEvent.DismissRequest -> {
                                mainViewModel.handleMainEvent(MainEvent.HideRateBottomSheet)
                            }
                            is RateBottomSheetEvent.SellClicked -> {
                                mainViewModel.handleMainEvent(MainEvent.SellCalculate(event.sellRate))
                            }

                            RateBottomSheetEvent.ShowDatePickerDialog -> {
                                showDateDialog = true
                            }
                        }
                    }
                )
            }


            // EditBottomSheet

            if(mainUiState.showEditBottomSheet) {
                val record = recordListUiState.selectedRecord ?: return
                EditBottomSheet(
                    record,
                    mainUiState,
                    editSheetState,
                    onEvent = { event ->
                        when(event) {
                            EditBottomSheetEvent.DismissRequest -> {
                                mainViewModel.handleMainEvent(MainEvent.HideEditBottomSheet)
                            }
                            is EditBottomSheetEvent.EditSelected -> {
                                mainViewModel.handleMainEvent(MainEvent.EditRecord(
                                    event.record,
                                    event.editMoney,
                                    event.editRate
                                ))
                            }

                            EditBottomSheetEvent.ShowDatePickerDialog -> {
                                showDateDialog = true
                            }
                        }
                    }
                )

            }


            // 단일 날짜 선택
            if (showDateDialog) {
                MyDatePickerDialog(
                    onDateSelected = { localDate ->
                        mainViewModel.handleMainEvent(MainEvent.SelectedDate(localDate.toString()))
                    },
                    onDismissRequest = {
                        showDateDialog = false
                    }
                )
            }

            // 그룹 추가 다이로그
            if (groupAddDialog) {
                TextFieldDialog(
                    onDismissRequest = {
                        groupAddDialog = it },
                    placeholder = "새 그룹명을 작성해주세요",
                    onClickedLabel = "추가",
                    closeButtonLabel = "닫기",
                    onClicked = { name ->
                        mainViewModel.handleMainEvent(MainEvent.GroupAdd(name))
                        groupAddDialog = false
                    })
            }

            // 날짜범위 선택
            if (dateRangeUiState.dateRangeDialog) {
                RangeDateDialog(
                    onDismissRequest = {
                       showDateDialog = it
                    },
                    callStartDate = dateRangeUiState.startDate,
                    callEndDate = dateRangeUiState.endDate,
                    startDateSelected = {
                        mainViewModel.inputStartDate(it)
                    },
                    endDateSelected = {
                        mainViewModel.inputEndDate(it)
                    },
                    onClicked = { selectedStartDate, selectedEndDate ->
                        coroutineScope.launch {
                            // 메인화면 조회기간
                            mainViewModel.inputStartDate(selectedStartDate)
                            mainViewModel.inputEndDate(selectedEndDate)

//                            dollarViewModel.dateRangeInvoke(
//                                selectedStartDate,
//                                selectedEndDate
//                            )
//
//                            yenViewModel.dateRangeInvoke(
//                                selectedStartDate,
//                                selectedEndDate
//                            )
                        }

                    },
                )
            }

            // 공지 다이로그
            if (noticeUiState.showNoticeDialog) {
                NoticeDialog(
                    content = noticeUiState.notice.content ?: "",
                    onDismissRequest = {
                        mainViewModel.closeNotice()
                    },
                    dateDelaySelected = {
                        coroutineScope.launch {
                            mainViewModel.selectDelayDate()
                            delay(1000)
                        }
                    })
            }

            // 광고 시청 다이로그
            if(adUiState.rewardShowDialog) {
                RewardShowAskDialog(
                    onDismissRequest = {
                        mainViewModel.rewardDelayDate()
                        mainViewModel.closeRewardDialog()
                    },
                    onClicked = {
                        showTargetRewardedAdvertisement(activity, onAdDismissed = {
                            mainViewModel.rewardDelayDate()
//                            mainViewModel.deleteBannerDelayDate()
                            mainViewModel.closeRewardDialog()
                            thankShowingDialog = true
                        })
                    })
            }

            if (mainUiState.showSellResultDialog) {
                SellResultDialog(
                    selectedRecord = {
                        mainViewModel.handleMainEvent(MainEvent.SellRecord)
                    },
                    onDismissRequest = {


                    },
                    percent = recordListUiState.sellPercent,
                    sellProfit = recordListUiState.sellProfit)

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
//                showOpenDialog.value = true
            },
            refreshClicked = {
//                allViewModel.reFreshProfit { recentRate ->
//                    dollarViewModel.calculateProfit(recentRate)
//
//                    yenViewModel.calculateProfit(recentRate)
//                }
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

sealed class MainEvent {
    data class GroupAdd(val groupName: String) : MainEvent()
    data class ShowRateBottomSheet(val record: ForeignCurrencyRecord) : MainEvent()
    data object HideRateBottomSheet : MainEvent()
    data object HideEditBottomSheet: MainEvent()
    data class SellCalculate(val sellRate:String) : MainEvent()
    data class SelectedDate(val date:String): MainEvent()
    data object SellRecord: MainEvent()
    data object HideSellResultDialog : MainEvent()
    data class EditRecord(val record: ForeignCurrencyRecord,
                          val money: String,
                          val rate: String): MainEvent()
}










