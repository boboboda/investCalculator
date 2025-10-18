package com.bobodroid.myapplication.components.mainComponents

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SheetState
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bobodroid.myapplication.components.Buttons
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
        CurrencyType.USD-> { recordListUiState.foreignCurrencyRecord.dollarState.groups }
        CurrencyType.JPY-> { recordListUiState.foreignCurrencyRecord.yenState.groups }
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
        // Sheet content
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
                                text = "기록 추가",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1F2937)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${mainUiState.selectedCurrencyType.koreanName} 매수 기록을 입력하세요",
                                fontSize = 14.sp,
                                color = Color(0xFF6B7280)
                            )
                        }

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

            // 입력 필드들
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

                // 그룹 선택
                Box(modifier = Modifier.fillMaxWidth()) {
                    Card(
                        colors = CardDefaults.cardColors(Color(0xFFF9FAFB)),
                        elevation = CardDefaults.cardElevation(0.dp),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        onClick = {
                            groupDropdownExpanded = !groupDropdownExpanded
                        }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
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
                        scrollState = rememberScrollState(),
                        modifier = Modifier
                            .wrapContentHeight()
                            .heightIn(max = 200.dp)
                            .fillMaxWidth(0.85f),
                        offset = DpOffset(x = 0.dp, y = 8.dp),
                        expanded = groupDropdownExpanded,
                        onDismissRequest = {
                            groupDropdownExpanded = false
                        }
                    ) {
                        // 새 그룹
                        DropdownMenuItem(
                            text = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
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
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
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

                // 날짜 선택
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
                        onEvent(MainEvent.BottomSheetEvent.OnDateSelect)
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
                            text = mainUiState.selectedDate,
                            color = Color(0xFF1F2937),
                            fontSize = 15.sp
                        )
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
                Buttons(
                    enabled = isBtnActive,
                    onClicked = {
                        onEvent(MainEvent.BottomSheetEvent.OnRecordAdd(
                            numberInput,
                            rateInput,
                            group
                        ))
                        group = "미지정"
                        onEvent(MainEvent.BottomSheetEvent.DismissSheet)
                    },
                    color = Color(0xFF6366F1),
                    fontColor = Color.White,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                ) {
                    Text(
                        text = "기록",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Buttons(
                    onClicked = {
                        onEvent(MainEvent.BottomSheetEvent.DismissSheet)
                    },
                    color = Color.White,
                    fontColor = Color(0xFF374151),
                    modifier = Modifier.height(48.dp)
                ) {
                    Text(text = "닫기", fontSize = 15.sp)
                }
            }

            // 팝업 영역
            Box {
                Spacer(modifier = Modifier.height(50.dp))

                Column {
                    AnimatedVisibility(visible = numberPadPopViewIsVible) {
                        PopupNumberView(
                            event = { event->
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

                            })
                    }
                }
            }
        }
    }
}