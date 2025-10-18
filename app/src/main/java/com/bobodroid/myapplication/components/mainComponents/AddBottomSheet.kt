package com.bobodroid.myapplication.components.mainComponents

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bobodroid.myapplication.components.CustomCard
import com.bobodroid.myapplication.components.Dialogs.FloatPopupNumberView
import com.bobodroid.myapplication.components.Dialogs.PopupNumberView
import com.bobodroid.myapplication.models.datamodels.roomDb.CurrencyType
import com.bobodroid.myapplication.models.viewmodels.MainUiState
import com.bobodroid.myapplication.models.viewmodels.RecordListUiState
import com.bobodroid.myapplication.screens.MainEvent
import com.bobodroid.myapplication.screens.PopupEvent
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddBottomSheet(
    sheetState: SheetState,
    recordListUiState: RecordListUiState,
    snackBarHostState: SnackbarHostState,
    mainUiState: MainUiState,
    onEvent: (MainEvent.BottomSheetEvent) -> Unit
) {
    var numberInput by remember { mutableStateOf("") }
    var rateInput by remember { mutableStateOf("") }
    val isBtnActive = numberInput.isNotEmpty() && rateInput.isNotEmpty()
    val coroutineScope = rememberCoroutineScope()

    // 그룹
    var group by remember { mutableStateOf("미지정") }
    var groupDropdownExpanded by remember { mutableStateOf(false) }

    val groupList = when(mainUiState.selectedCurrencyType) {
        CurrencyType.USD -> { recordListUiState.foreignCurrencyRecord.dollarState.groups }
        CurrencyType.JPY -> { recordListUiState.foreignCurrencyRecord.yenState.groups }
    }

    var numberPadPopViewIsVible by remember { mutableStateOf(false) }
    var ratePadPopViewIsVible by remember { mutableStateOf(false) }

    BottomSheet(
        sheetState = sheetState,
        snackBarHostState = snackBarHostState,
        onDismissRequest = {
            onEvent(MainEvent.BottomSheetEvent.DismissSheet)
        },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp)
        ) {
            // 헤더
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "기록 추가",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1F2937)
                    )

                    IconButton(onClick = {
                        onEvent(MainEvent.BottomSheetEvent.DismissSheet)
                    }) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = "닫기",
                            tint = Color(0xFF6B7280)
                        )
                    }
                }

                Text(
                    text = "선택한 통화: ${mainUiState.selectedCurrencyType.koreanName}",
                    fontSize = 14.sp,
                    color = Color(0xFF6B7280)
                )
            }

            HorizontalDivider(color = Color(0xFFE5E7EB))

            // 입력 섹션
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(top = 24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 금액 입력
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "금액",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF374151)
                    )

                    CustomCard(
                        label = numberInput.ifEmpty { "금액을 입력하세요" },
                        fontSize = 15,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        fontColor = if (numberInput.isEmpty()) Color(0xFF9CA3AF) else Color.Black,
                        cardColor = Color(0xFFF9FAFB),
                        onClick = {
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
                    )
                }

                // 환율 입력
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "환율",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF374151)
                    )

                    CustomCard(
                        label = rateInput.ifEmpty { "환율을 입력하세요" },
                        fontSize = 15,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        fontColor = if (rateInput.isEmpty()) Color(0xFF9CA3AF) else Color.Black,
                        cardColor = Color(0xFFF9FAFB),
                        onClick = {
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
                    )
                }

                // 그룹 선택
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "그룹",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF374151)
                    )

                    Box(modifier = Modifier.fillMaxWidth()) {
                        Surface(
                            onClick = { groupDropdownExpanded = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            color = Color(0xFFF9FAFB),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Folder,
                                        contentDescription = null,
                                        tint = Color(0xFF6366F1),
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Text(
                                        text = group,
                                        fontSize = 15.sp,
                                        color = Color(0xFF1F2937)
                                    )
                                }
                                Icon(
                                    imageVector = Icons.Rounded.KeyboardArrowDown,
                                    contentDescription = null,
                                    tint = Color(0xFF6B7280)
                                )
                            }
                        }

                        DropdownMenu(
                            expanded = groupDropdownExpanded,
                            onDismissRequest = { groupDropdownExpanded = false },
                            modifier = Modifier.fillMaxWidth(0.85f)
                        ) {
                            // 새 그룹 만들기
                            DropdownMenuItem(
                                text = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.Add,
                                            contentDescription = null,
                                            tint = Color(0xFF6366F1),
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Text(
                                            text = "새 그룹 만들기",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Color(0xFF6366F1)
                                        )
                                    }
                                },
                                onClick = {
                                    onEvent(MainEvent.BottomSheetEvent.OnGroupSelect)
                                    groupDropdownExpanded = false
                                }
                            )

                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = 4.dp),
                                color = Color(0xFFE5E7EB)
                            )

                            // 그룹 리스트
                            groupList.forEach { groupValue ->
                                DropdownMenuItem(
                                    text = {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Rounded.Folder,
                                                contentDescription = null,
                                                tint = Color(0xFF9CA3AF),
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Text(
                                                text = groupValue,
                                                fontSize = 14.sp,
                                                color = Color(0xFF1F2937)
                                            )
                                        }
                                    },
                                    onClick = {
                                        group = groupValue
                                        groupDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                // 날짜 선택
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "날짜",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF374151)
                    )

                    Surface(
                        onClick = { onEvent(MainEvent.BottomSheetEvent.OnDateSelect) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        color = Color(0xFFF9FAFB),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.CalendarToday,
                                contentDescription = null,
                                tint = Color(0xFF6366F1),
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = mainUiState.selectedDate,
                                fontSize = 15.sp,
                                color = Color(0xFF1F2937)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 액션 버튼
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                FilledTonalButton(
                    onClick = {
                        onEvent(MainEvent.BottomSheetEvent.OnRecordAdd(
                            numberInput,
                            rateInput,
                            group
                        ))
                        group = "미지정"
                        onEvent(MainEvent.BottomSheetEvent.DismissSheet)
                    },
                    enabled = isBtnActive,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = Color(0xFF6366F1),
                        contentColor = Color.White,
                        disabledContainerColor = Color(0xFFE5E7EB),
                        disabledContentColor = Color(0xFF9CA3AF)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "기록",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                OutlinedButton(
                    onClick = {
                        onEvent(MainEvent.BottomSheetEvent.DismissSheet)
                    },
                    modifier = Modifier.height(48.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color(0xFF374151)
                    ),
                    border = ButtonDefaults.outlinedButtonBorder.copy(
                        width = 1.dp,
                        brush = androidx.compose.ui.graphics.SolidColor(Color(0xFFE5E7EB))
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "닫기",
                        fontSize = 15.sp
                    )
                }
            }

            // 팝업 영역
            Box {
                Spacer(modifier = Modifier.height(50.dp))

                Column {
                    AnimatedVisibility(visible = numberPadPopViewIsVible) {
                        PopupNumberView(
                            event = { event ->
                                when(event) {
                                    is PopupEvent.OnClicked -> {
                                        coroutineScope.launch {
                                            numberInput = event.moneyOrRate
                                            numberPadPopViewIsVible = false
                                            delay(500)
                                            ratePadPopViewIsVible = true
                                        }
                                    }
                                    is PopupEvent.SnackBarEvent ->
                                        onEvent(MainEvent.BottomSheetEvent.Popup(PopupEvent.SnackBarEvent(event.message)))
                                }
                            },
                            limitNumberLength = 10
                        )
                    }

                    AnimatedVisibility(visible = ratePadPopViewIsVible) {
                        FloatPopupNumberView(
                            event = { event ->
                                when (event) {
                                    is PopupEvent.OnClicked -> {
                                        rateInput = event.moneyOrRate
                                        ratePadPopViewIsVible = false
                                    }
                                    is PopupEvent.SnackBarEvent ->
                                        onEvent(MainEvent.BottomSheetEvent.Popup(PopupEvent.SnackBarEvent(event.message)))
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}