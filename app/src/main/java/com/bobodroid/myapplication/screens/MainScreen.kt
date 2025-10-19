package com.bobodroid.myapplication.screens

import android.app.Activity
import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.Card
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.*
import com.bobodroid.myapplication.components.*
import com.bobodroid.myapplication.components.Caldenders.RangeDateDialog
import kotlinx.coroutines.launch
import java.util.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import com.bobodroid.myapplication.components.Dialogs.NoticeDialog
import kotlinx.coroutines.delay
import androidx.compose.material3.Text
import androidx.compose.ui.platform.LocalFocusManager
import com.bobodroid.myapplication.components.Dialogs.RewardShowAskDialog
import com.bobodroid.myapplication.components.Dialogs.SellResultDialog
import com.bobodroid.myapplication.components.Dialogs.TextFieldDialog
import com.bobodroid.myapplication.components.Dialogs.ThanksDialog
import com.bobodroid.myapplication.components.admobs.showTargetRewardedAdvertisement
import com.bobodroid.myapplication.components.mainComponents.AddBottomSheet
import com.bobodroid.myapplication.components.mainComponents.EditBottomSheet
import com.bobodroid.myapplication.components.mainComponents.MainHeader
import com.bobodroid.myapplication.components.mainComponents.RateBottomSheet
import com.bobodroid.myapplication.extensions.toLocalDate
import com.bobodroid.myapplication.lists.foreignCurrencyList.RecordListView
import com.bobodroid.myapplication.models.datamodels.roomDb.CurrencyType
import com.bobodroid.myapplication.models.datamodels.roomDb.ForeignCurrencyRecord
import com.bobodroid.myapplication.models.viewmodels.CurrencyRecordState
import com.bobodroid.myapplication.models.viewmodels.MainViewModel
import com.bobodroid.myapplication.screens.MainEvent.BottomSheetEvent
import androidx.compose.material3.SnackbarHostState
import com.bobodroid.myapplication.components.Dialogs.OnboardingTooltipDialog
import com.bobodroid.myapplication.components.mainComponents.GroupChangeBottomSheet
import com.bobodroid.myapplication.util.PreferenceUtil
import kotlinx.coroutines.Job


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

    val mainSnackBarHostState = remember { SnackbarHostState() }
    val sheetSnackBarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    val bottomRefreshPadding = remember { mutableStateOf(5) }
    var isVisible by remember { mutableStateOf(true) }

    val groupChangeSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val rateSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val editSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var thankShowingDialog by remember { mutableStateOf(false) }
    val listScrollState = rememberLazyListState()

    val context = LocalContext.current
    val preferenceUtil = remember { PreferenceUtil(context) }
    var showOnboarding by remember {
        mutableStateOf(preferenceUtil.getData("onboarding_completed", "false") == "false")
    }

    // ✅ 수정: 헤더 확대/축소 상태 관리 (초기값 false = 확대 상태)
    var isCollapsedState by remember { mutableStateOf(false) }


    var isInitialLaunch by remember { mutableStateOf(true) }

// ✅ 앱 처음 실행 시에만 자동 축소
    LaunchedEffect(key1 = Unit) {
        if (isInitialLaunch) {
            delay(2500)
            isCollapsedState = true
            isInitialLaunch = false  // 최초 실행 플래그 해제
        }
    }

    // 리스트
    var hideSellRecordState by remember { mutableStateOf(false) }
    val records by mainViewModel.getCurrentRecordsFlow().collectAsState(CurrencyRecordState())
    val focusManager = LocalFocusManager.current

    LaunchedEffect(key1 = Unit, block = {
        coroutineScope.launch {
            delay(1000)
            isVisible = false
            bottomRefreshPadding.value = 0
        }
    })

    LaunchedEffect(key1 = true) {
        mainViewModel.mainSnackBarState.collect { message ->
            if (mainSnackBarHostState.currentSnackbarData == null) {
                mainSnackBarHostState.showSnackbar(
                    message = message,
                    actionLabel = "닫기",
                    duration = SnackbarDuration.Short
                )
            }
        }
    }

    LaunchedEffect(key1 = true) {
        mainViewModel.sheetSnackBarState.collect { message ->
            if (sheetSnackBarHostState.currentSnackbarData == null) {
                sheetSnackBarHostState.showSnackbar(
                    message = message,
                    actionLabel = "닫기",
                    duration = SnackbarDuration.Short
                )
            }
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.BottomCenter,
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            MainHeader(
                mainUiState = mainUiState,
                adUiState = adUiState,
                updateCurrentForeignCurrency = {
                    mainViewModel.updateCurrentForeignCurrency(it)
                },
                recordUiState = recordListUiState,
                hideSellRecordState = hideSellRecordState,
                onHide = {
                    hideSellRecordState = it
                },
                isCollapsed = isCollapsedState,  // ✅ 토글 상태 전달
                onToggleClick = {  // ✅ 토글 버튼 클릭 핸들러 추가
                    isCollapsedState = !isCollapsedState
                }
            )

            Column(
                Modifier
                    .weight(1f)
                    .addFocusCleaner(focusManager)
            ) {
                RecordListView(
                    mainUiState.selectedCurrencyType,
                    records,
                    hideSellRecordState = hideSellRecordState,
                    scrollState = listScrollState,
                    onEvent = { event ->
                        when (event) {
                            is RecordListEvent.SellRecord -> {
                                val record = event.data
                                mainViewModel.handleMainEvent(MainEvent.ShowRateBottomSheet(record))
                            }
                            is RecordListEvent.ShowEditBottomSheet -> {
                                mainViewModel.handleMainEvent(MainEvent.ShowEditBottomSheet(event.data))
                            }
                            is RecordListEvent.ShowAddBottomSheet -> {
                                mainViewModel.handleMainEvent(MainEvent.ShowAddBottomSheet)
                            }
                            is RecordListEvent.ShowGroupChangeBottomSheet -> {
                                mainViewModel.handleRecordEvent(event)
                            }
                            else -> mainViewModel.handleRecordEvent(event)
                        }
                    }
                )
            }

            // BottomSheets
            if (mainUiState.showAddBottomSheet) {
                AddBottomSheet(
                    sheetState,
                    recordListUiState,
                    sheetSnackBarHostState,
                    mainUiState,
                    onEvent = { bottomSheetEvent ->
                        when (bottomSheetEvent) {
                            is BottomSheetEvent.DismissSheet -> {
                                coroutineScope.launch {
                                    sheetState.hide()
                                    mainViewModel.handleMainEvent(bottomSheetEvent)
                                }
                            }
                            else -> mainViewModel.handleMainEvent(bottomSheetEvent)
                        }
                    }
                )
            }

            if (mainUiState.showRateBottomSheet) {
                RateBottomSheet(
                    rateSheetState,
                    sellDate = mainUiState.selectedDate,
                    snackBarHostState = sheetSnackBarHostState,
                    onEvent = { event ->
                        when (event) {
                            MainEvent.RateBottomSheetEvent.DismissRequest -> {
                                coroutineScope.launch {
                                    rateSheetState.hide()
                                    mainViewModel.handleMainEvent(event)
                                }
                            }
                            else -> mainViewModel.handleMainEvent(event)
                        }
                    }
                )
            }

            if (mainUiState.showGroupChangeBottomSheet) {
                val record = recordListUiState.selectedRecord ?: return
                GroupChangeBottomSheet(
                    sheetState = groupChangeSheetState,
                    record = record,
                    groupList = records.groups,
                    onEvent = { event ->
                        mainViewModel.handleRecordEvent(event)
                    },
                    onDismiss = {
                        coroutineScope.launch {
                            groupChangeSheetState.hide()
                            mainViewModel.handleMainEvent(MainEvent.HideGroupChangeBottomSheet)
                        }
                    }
                )
            }

            if (mainUiState.showEditBottomSheet) {
                val record = recordListUiState.selectedRecord ?: return
                EditBottomSheet(
                    record,
                    mainUiState,
                    editSheetState,
                    onEvent = { event ->
                        when (event) {
                            MainEvent.EditBottomSheetEvent.DismissRequest -> {
                                coroutineScope.launch {
                                    editSheetState.hide()
                                    mainViewModel.handleMainEvent(event)
                                }
                            }
                            else -> mainViewModel.handleMainEvent(event)
                        }
                    }
                )
            }

            // Dialogs
            if (mainUiState.showDatePickerDialog) {
                MyDatePickerDialog(
                    selectedDate = mainUiState.selectedDate.toLocalDate(),
                    onDateSelected = { localDate ->
                        mainViewModel.handleMainEvent(MainEvent.SelectedDate(localDate.toString()))
                    },
                    onDismissRequest = {
                        mainViewModel.handleMainEvent(MainEvent.HideDatePickerDialog)
                    }
                )
            }

            if (mainUiState.showGroupAddDialog) {
                TextFieldDialog(
                    onDismissRequest = {
                        mainViewModel.handleMainEvent(MainEvent.HideGroupAddDialog)
                    },
                    placeholder = "새 그룹명을 작성해주세요",
                    onClickedLabel = "추가",
                    closeButtonLabel = "닫기",
                    onClicked = { name ->
                        mainViewModel.handleMainEvent(MainEvent.GroupAdd(name))
                    }
                )
            }

            if (mainUiState.showDateRangeDialog) {
                RangeDateDialog(
                    onDismissRequest = {
                        mainViewModel.handleMainEvent(MainEvent.HideDateRangeDialog)
                    },
                    onClicked = { selectedStartDate, selectedEndDate ->
                        coroutineScope.launch {
                            mainViewModel.handleRecordEvent(
                                RecordListEvent.TotalSumProfit(selectedStartDate, selectedEndDate)
                            )
                        }
                    },
                )
            }

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
                    }
                )
            }

            if (adUiState.rewardShowDialog) {
                RewardShowAskDialog(
                    onDismissRequest = {
                        mainViewModel.rewardDelayDate()
                        mainViewModel.closeRewardDialog()
                    },
                    onClicked = {
                        showTargetRewardedAdvertisement(activity, onAdDismissed = {
                            mainViewModel.rewardDelayDate()
                            mainViewModel.closeRewardDialog()
                            thankShowingDialog = true
                        })
                    }
                )
            }

            if (mainUiState.showSellResultDialog) {
                SellResultDialog(
                    selectedRecord = {
                        mainViewModel.handleMainEvent(MainEvent.SellRecord)
                    },
                    onDismissRequest = {
                        mainViewModel.handleMainEvent(MainEvent.HideSellResultDialog)
                    },
                    percent = recordListUiState.sellPercent,
                    sellProfit = recordListUiState.sellProfit
                )
            }

            if (thankShowingDialog) {
                ThanksDialog(onDismissRequest = { value ->
                    thankShowingDialog = value
                })
            }
        }

        // 플로팅 버튼
        ContentIcon(
            addRecordClicked = {
                mainViewModel.handleMainEvent(MainEvent.ShowAddBottomSheet)
            }

        )

        Box {
            SnackbarHost(
                hostState = mainSnackBarHostState, modifier = Modifier,
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
                                text = snackBarData.visuals.message,
                                fontSize = 15.sp,
                                lineHeight = 20.sp,
                                fontWeight = FontWeight.Medium
                            )

                            Spacer(modifier = Modifier.weight(1f))

                            Text(
                                modifier = Modifier
                                    .padding(8.dp)
                                    .clickable {
                                        mainSnackBarHostState.currentSnackbarData?.dismiss()
                                    },
                                text = "닫기",
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            )
        }

        if (showOnboarding) {
            OnboardingTooltipDialog(
                onDismiss = {
                    preferenceUtil.setData("onboarding_completed", "true")
                    showOnboarding = false
                }
            )
        }
    }
}

sealed class PopupEvent {
    data class SnackBarEvent(val message: String) : PopupEvent()
    data class OnClicked(val moneyOrRate: String) : PopupEvent()
}

sealed class MainEvent {
    data class SnackBarEvent(val message: String) : MainEvent()
    data class GroupAdd(val groupName: String) : MainEvent()
    data class ShowRateBottomSheet(val record: ForeignCurrencyRecord) : MainEvent()
    data object ShowAddBottomSheet : MainEvent()
    data object ShowDatePickerDialog : MainEvent()
    data object HideGroupAddDialog : MainEvent()
    data object HideDatePickerDialog : MainEvent()
    data class SelectedDate(val date: String) : MainEvent()
    data object SellRecord : MainEvent()
    data object HideSellResultDialog : MainEvent()
    data object HideDateRangeDialog : MainEvent()
    data object ShowDateRangeDialog : MainEvent()
    data object HideGroupChangeBottomSheet : MainEvent()

    sealed class BottomSheetEvent : MainEvent() {
        data class OnRecordAdd(val money: String, val rate: String, val group: String) : BottomSheetEvent()
        data class OnCurrencyTypeChange(val currencyType: CurrencyType) : BottomSheetEvent()
        data object OnGroupSelect : BottomSheetEvent()
        data object OnDateSelect : BottomSheetEvent()
        data object DismissSheet : BottomSheetEvent()
        data class Popup(val popupEvent: PopupEvent) : BottomSheetEvent()
    }

    sealed class RateBottomSheetEvent: MainEvent() {
        data object DismissRequest : RateBottomSheetEvent()
        data object ShowDatePickerDialog : RateBottomSheetEvent()
        data class SellClicked(val sellRate: String) : RateBottomSheetEvent()
        data class Popup(val event: PopupEvent) : RateBottomSheetEvent()
    }

    data class ShowEditBottomSheet(val record: ForeignCurrencyRecord) : MainEvent()

    sealed class EditBottomSheetEvent: MainEvent() {
        data class EditSelected(
            val record: ForeignCurrencyRecord,
            val editMoney: String,
            val editRate: String): EditBottomSheetEvent()
        data object DismissRequest : EditBottomSheetEvent()
        data class ShowDatePickerDialog(val date: String): EditBottomSheetEvent()
        data class Popup(val event: PopupEvent) : EditBottomSheetEvent()
    }

    sealed class GroupChangeBottomSheetEvent: MainEvent() {
        data object DismissRequest : GroupChangeBottomSheetEvent()
        data class GroupChanged(val record: ForeignCurrencyRecord, val groupName: String) : GroupChangeBottomSheetEvent()
        data object OnGroupSelect : GroupChangeBottomSheetEvent()
    }
}

sealed class RecordListEvent {
    data class ShowEditBottomSheet(val data: ForeignCurrencyRecord) : RecordListEvent()
    data class SnackBarEvent(val message: String): RecordListEvent()
    data class AddGroup(val data:ForeignCurrencyRecord, val groupName: String): RecordListEvent()
    data class CancelSellRecord(val id: UUID): RecordListEvent()
    data class UpdateRecordCategory(val record: ForeignCurrencyRecord, val groupName: String): RecordListEvent()
    data class MemoUpdate(val record: ForeignCurrencyRecord, val updateMemo: String): RecordListEvent()
    data class SellRecord(val data: ForeignCurrencyRecord): RecordListEvent()
    data class RemoveRecord(val data: ForeignCurrencyRecord): RecordListEvent()
    data class TotalSumProfit(val startDate: String, val endDate: String): RecordListEvent()
    data object ShowAddBottomSheet : RecordListEvent()
    data class ShowGroupChangeBottomSheet(val data: ForeignCurrencyRecord) : RecordListEvent()
}