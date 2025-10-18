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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bobodroid.myapplication.components.Buttons
import com.bobodroid.myapplication.components.Dialogs.FloatPopupNumberView
import com.bobodroid.myapplication.screens.MainEvent
import com.bobodroid.myapplication.screens.PopupEvent
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RateBottomSheet(
    sheetState: SheetState,
    snackBarHostState: SnackbarHostState,
    sellDate: String,
    onEvent: (MainEvent.RateBottomSheetEvent) -> Unit
) {

    var ratePadPopViewVisible by remember { mutableStateOf(false) }

    var rateInput by remember { mutableStateOf("") }

    val coroutineScope = rememberCoroutineScope()


    BottomSheet(
        sheetState,
        snackBarHostState,
        onDismissRequest = {
            onEvent(MainEvent.RateBottomSheetEvent.DismissRequest)
        }
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
                visible = !ratePadPopViewVisible
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
                                text = "매도 환율 입력",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1F2937)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "매도할 환율을 입력하세요",
                                fontSize = 14.sp,
                                color = Color(0xFF6B7280)
                            )
                        }

                        IconButton(onClick = {
                            onEvent(MainEvent.RateBottomSheetEvent.DismissRequest)
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
                visible = !ratePadPopViewVisible
            ) {
                HorizontalDivider(color = Color(0xFFE5E7EB))
            }

            AnimatedVisibility(
                visible = !ratePadPopViewVisible
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
                // 날짜 표시 - 팝업 열릴 때 숨김
                AnimatedVisibility(
                    visible = !ratePadPopViewVisible
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
                            onEvent(MainEvent.RateBottomSheetEvent.ShowDatePickerDialog)
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
                                text = sellDate,
                                color = Color(0xFF1F2937),
                                fontSize = 15.sp
                            )
                        }
                    }
                }

                // 환율 입력
                BottomSheetRateNumberField(
                    title = rateInput,
                    placeholder = "매도환율을 입력해주세요",
                    selectedState = ratePadPopViewVisible,
                    modifier = Modifier
                ) {
                    coroutineScope.launch {
                        ratePadPopViewVisible = true
                    }
                }
            }

            AnimatedVisibility(
                visible = !ratePadPopViewVisible
            ) {
                Spacer(modifier = Modifier.height(24.dp))
            }

            // 액션 버튼 - 팝업 열릴 때 숨김
            AnimatedVisibility(
                visible = !ratePadPopViewVisible
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Buttons(
                        enabled = rateInput.isNotEmpty(),
                        onClicked = {
                            onEvent(MainEvent.RateBottomSheetEvent.SellClicked(rateInput))
                        },
                        color = Color(0xFF6366F1),
                        fontColor = Color.White,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                    ) {
                        Text(
                            text = "매도",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Buttons(
                        onClicked = {
                            onEvent(MainEvent.RateBottomSheetEvent.DismissRequest)
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
                    AnimatedVisibility(visible = ratePadPopViewVisible) {
                        FloatPopupNumberView(
                            event = { event ->
                                when(event) {
                                    is PopupEvent.OnClicked -> {
                                        rateInput = event.moneyOrRate
                                        ratePadPopViewVisible = false
                                    }
                                    is PopupEvent.SnackBarEvent ->
                                        onEvent(MainEvent.RateBottomSheetEvent.Popup(PopupEvent.SnackBarEvent(event.message)))
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}