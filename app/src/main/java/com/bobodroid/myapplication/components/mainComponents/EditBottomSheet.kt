package com.bobodroid.myapplication.components.mainComponents

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bobodroid.myapplication.components.Buttons
import com.bobodroid.myapplication.components.Dialogs.FloatPopupNumberView
import com.bobodroid.myapplication.components.Dialogs.PopupNumberView
import com.bobodroid.myapplication.models.datamodels.roomDb.ForeignCurrencyRecord
import com.bobodroid.myapplication.models.viewmodels.MainUiState
import com.bobodroid.myapplication.screens.MainEvent
import com.bobodroid.myapplication.screens.PopupEvent
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditBottomSheet(
    editRecord: ForeignCurrencyRecord,
    mainUiState: MainUiState,
    sheetState: SheetState,
    onEvent: (MainEvent.EditBottomSheetEvent) -> Unit
) {
    var numberInput by remember { mutableStateOf(editRecord.money ?: "") }
    var recordDate by remember { mutableStateOf(editRecord.date ?: "") }
    var rateInput by remember { mutableStateOf(editRecord.rate ?: "") }

    val isBtnActive = numberInput.isNotEmpty() && rateInput.isNotEmpty()

    val coroutineScope = rememberCoroutineScope()

    var numberPadPopViewIsVible by remember { mutableStateOf(false) }
    var ratePadPopViewIsVible by remember { mutableStateOf(false) }

    LaunchedEffect(key1 = mainUiState.selectedDate) {
        if(mainUiState.selectedDate != "") {
            recordDate = mainUiState.selectedDate
        }
    }

    BottomSheet(
        sheetState = sheetState,
        snackBarHostState = remember { SnackbarHostState() },
        onDismissRequest = {
            onEvent(MainEvent.EditBottomSheetEvent.DismissRequest)
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(bottom = 32.dp)
        ) {
            // 🎨 헤더 - 팝업 열릴 때 숨김
            AnimatedVisibility(
                visible = !numberPadPopViewIsVible && !ratePadPopViewIsVible
            ) {
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
                        Column {
                            Text(
                                text = "기록 수정",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1F2937)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "매수 정보를 수정하세요",
                                fontSize = 14.sp,
                                color = Color(0xFF6B7280)
                            )
                        }

                        IconButton(onClick = {
                            onEvent(MainEvent.EditBottomSheetEvent.DismissRequest)
                        }) {
                            Icon(
                                imageVector = Icons.Rounded.Close,
                                contentDescription = "닫기",
                                tint = Color(0xFF6B7280)
                            )
                        }
                    }
                }
            }

            AnimatedVisibility(
                visible = !numberPadPopViewIsVible && !ratePadPopViewIsVible
            ) {
                HorizontalDivider(color = Color(0xFFE5E7EB))
            }

            AnimatedVisibility(
                visible = !numberPadPopViewIsVible && !ratePadPopViewIsVible
            ) {
                Spacer(modifier = Modifier.height(20.dp))
            }

            // 입력 필드
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 금액 입력
                BottomSheetNumberField(
                    title = numberInput,
                    selectedState = numberPadPopViewIsVible,
                    modifier = Modifier
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

                // 환율 입력
                BottomSheetRateNumberField(
                    title = rateInput,
                    placeholder = "환율을 입력해주세요",
                    selectedState = ratePadPopViewIsVible,
                    modifier = Modifier
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

                // 날짜 선택 - 팝업 열릴 때 숨김
                AnimatedVisibility(
                    visible = !numberPadPopViewIsVible && !ratePadPopViewIsVible
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        border = BorderStroke(1.dp, Color(0xFFE5E7EB)),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.White
                        ),
                        elevation = CardDefaults.cardElevation(0.dp),
                        shape = RoundedCornerShape(12.dp),
                        onClick = {
                            onEvent(MainEvent.EditBottomSheetEvent.ShowDatePickerDialog(recordDate))
                        }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.CalendarToday,
                                contentDescription = null,
                                tint = Color(0xFF6366F1),
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = recordDate,
                                color = Color(0xFF1F2937),
                                fontSize = 15.sp
                            )
                        }
                    }
                }
            }

            AnimatedVisibility(
                visible = !numberPadPopViewIsVible && !ratePadPopViewIsVible
            ) {
                Spacer(modifier = Modifier.height(24.dp))
            }

            // 액션 버튼 - 팝업 열릴 때 숨김
            AnimatedVisibility(
                visible = !numberPadPopViewIsVible && !ratePadPopViewIsVible
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Buttons(
                        enabled = isBtnActive,
                        onClicked = {
                            onEvent(MainEvent.EditBottomSheetEvent.EditSelected(editRecord, numberInput, rateInput))
                        },
                        color = Color(0xFF6366F1),
                        fontColor = Color.White,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                    ) {
                        Text(
                            text = "수정",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Buttons(
                        onClicked = {
                            onEvent(MainEvent.EditBottomSheetEvent.DismissRequest)
                        },
                        color = Color.White,
                        fontColor = Color(0xFF374151),
                        modifier = Modifier.height(48.dp)
                    ) {
                        Text(text = "닫기", fontSize = 15.sp)
                    }
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
                                            delay(700)
                                            ratePadPopViewIsVible = true
                                        }
                                    }
                                    else -> onEvent(MainEvent.EditBottomSheetEvent.Popup(event))
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
                                        onEvent(MainEvent.EditBottomSheetEvent.Popup(PopupEvent.SnackBarEvent(event.message)))
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}