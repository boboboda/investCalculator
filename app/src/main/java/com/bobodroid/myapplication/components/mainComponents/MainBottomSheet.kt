package com.bobodroid.myapplication.components.mainComponents

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bobodroid.myapplication.components.Buttons
import com.bobodroid.myapplication.components.CustomCard
import com.bobodroid.myapplication.components.Dialogs.BottomSheetNumberField
import com.bobodroid.myapplication.components.Dialogs.BottomSheetRateNumberField
import com.bobodroid.myapplication.components.Dialogs.FloatPopupNumberView
import com.bobodroid.myapplication.components.Dialogs.PopupNumberView
import com.bobodroid.myapplication.components.MoneyChButtonView
import com.bobodroid.myapplication.models.datamodels.roomDb.CurrencyType
import com.bobodroid.myapplication.models.viewmodels.MainUiState
import com.bobodroid.myapplication.models.viewmodels.RecordListUiState
import com.bobodroid.myapplication.ui.theme.BottomSheetTitleColor
import com.bobodroid.myapplication.ui.theme.BuyColor
import com.bobodroid.myapplication.ui.theme.WelcomeScreenBackgroundColor
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainBottomSheet(
    sheetState: SheetState,
    recordListUiState: RecordListUiState,
    mainUiState: MainUiState,
    onEvent: (BottomSheetEvent) -> Unit
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

    ModalBottomSheet(
        onDismissRequest = {
            onEvent(BottomSheetEvent.DismissSheet)
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
                    label = mainUiState.selectedCurrencyType.koreanName,
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
                        onEvent(BottomSheetEvent.OnRecordAdd(
                            numberInput,
                            rateInput,
                            group
                        ))
                        group = "미지정"
                        onEvent(BottomSheetEvent.DismissSheet)
                    },
                    color = BuyColor,
                    fontColor = Color.Black,
                    modifier = Modifier
                ) {
                    Text(text = "기록", fontSize = 15.sp)
                }

                Buttons(
                    onClicked = {
                        onEvent(BottomSheetEvent.DismissSheet)
                    },
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
                                onEvent(BottomSheetEvent.OnGroupSelect)
                                groupDropdownExpanded = false
                            })

                        Divider(
                            Modifier
                                .fillMaxWidth(),
                            color = Color.Gray.copy(alpha = 0.2f),
                            thickness = 2.dp
                        )

                        groupList.forEach { groupValue ->
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
                                            text = groupValue,
                                            fontSize = 13.sp
                                        )
                                    }
                                }, onClick = {
                                    group = groupValue
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
                        onEvent(BottomSheetEvent.OnDateSelect)
                    }
                ) {

                    Row(
                        modifier = Modifier
                            .fillMaxSize(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = mainUiState.selectedDate,
                            color = Color.Black,
                            fontSize = 18.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                        )
                    }

                }
            }

//            Row(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .padding(start = 20.dp, bottom = 20.dp, end = 10.dp),
//                horizontalArrangement = Arrangement.spacedBy(
//                    10.dp,
//                    alignment = Alignment.Start
//                ),
//                verticalAlignment = Alignment.CenterVertically
//            ) {
//
//                MoneyChButtonView(
//                    mainText = "달러",
//                    currencyType = CurrencyType.USD,
//                    selectedCurrencyType = mainUiState.selectedCurrencyType,
//                    selectAction = {
//                        onEvent(BottomSheetEvent.OnCurrencyTypeChange(it))
//                    })
//
//                Spacer(modifier = Modifier.width(20.dp))
//
//                MoneyChButtonView(
//                    mainText = "엔화",
//                    currencyType = CurrencyType.JPY,
//                    selectedCurrencyType = mainUiState.selectedCurrencyType,
//                    selectAction = {
//                        onEvent(BottomSheetEvent.OnCurrencyTypeChange(it))
//                    })
//
//            }



            BottomSheetNumberField(
                title = numberInput,
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
                title = rateInput,
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
                                    numberInput = it
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
                            rateInput = it
                            ratePadPopViewIsVible = false
                        })
                    }
                }
            }


        }
    }

}

// 바텀시트 관련 이벤트 정의
sealed class BottomSheetEvent {
    data class OnRecordAdd(val money: String, val rate: String, val group: String) : BottomSheetEvent()
    data class OnCurrencyTypeChange(val currencyType: CurrencyType) : BottomSheetEvent()
    data object OnGroupSelect : BottomSheetEvent()
    data object OnDateSelect : BottomSheetEvent()
    data object DismissSheet : BottomSheetEvent()
}

