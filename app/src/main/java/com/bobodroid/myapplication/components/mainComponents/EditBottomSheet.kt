package com.bobodroid.myapplication.components.mainComponents

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bobodroid.myapplication.components.Buttons
import com.bobodroid.myapplication.components.Dialogs.FloatPopupNumberView
import com.bobodroid.myapplication.components.Dialogs.PopupNumberView
import com.bobodroid.myapplication.models.datamodels.roomDb.ForeignCurrencyRecord
import com.bobodroid.myapplication.models.viewmodels.MainUiState
import com.bobodroid.myapplication.screens.MainEvent
import com.bobodroid.myapplication.screens.PopupEvent
import com.bobodroid.myapplication.ui.theme.BuyColor
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditBottomSheet(
    editRecord:ForeignCurrencyRecord,
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

    ModalBottomSheet(
        onDismissRequest = {
            onEvent(MainEvent.EditBottomSheetEvent.DismissRequest)
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

                Spacer(Modifier.weight(1f))

                Buttons(
                    enabled = isBtnActive,
                    onClicked = {
                        onEvent(MainEvent.EditBottomSheetEvent.EditSelected(editRecord, numberInput, rateInput))
                    },
                    color = BuyColor,
                    fontColor = Color.Black,
                    modifier = Modifier
                ) {
                    Text(text = "수정", fontSize = 15.sp)
                }

                Buttons(
                    onClicked = {
                        onEvent(MainEvent.EditBottomSheetEvent.DismissRequest)
                    },
                    color = BuyColor,
                    fontColor = Color.Black,
                    modifier = Modifier,

                    ) {
                    Text(text = "닫기", fontSize = 15.sp)
                }
            }


            Row(modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp)
                .padding(start = 5.dp),
                verticalAlignment = Alignment.CenterVertically) {

                Column(
                    modifier = Modifier.weight(0.2f)
                ) {
                    Text(text = "매수날짜:", fontSize = 18.sp)
                }

                Card(
                    modifier = Modifier
                        .weight(0.8f)
                        .padding(10.dp),
                    border = BorderStroke(1.dp, Color.Black),
                    colors = CardDefaults.cardColors(
                        contentColor = Color.Black,
                        containerColor = Color.White
                    ),
                    onClick = {
                        onEvent(MainEvent.EditBottomSheetEvent.ShowDatePickerDialog(recordDate))
                    }
                ) {


                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(45.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = recordDate,
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
                    .padding(horizontal = 10.dp)
                    .padding(start = 5.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {

                Column(
                    modifier = Modifier.weight(0.2f)
                ) {
                    Text(text = "매 수 금:", fontSize = 18.sp)
                }

                BottomSheetNumberField(
                    title = numberInput,
                    selectedState = numberPadPopViewIsVible,
                    modifier = Modifier.weight(0.8f)
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
            }


            Spacer(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp)
                    .padding(start = 5.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(0.2f)
                ) {
                    Text(text = "환      율:", fontSize = 18.sp)
                }

                BottomSheetRateNumberField(
                    title = rateInput,
                    selectedState = ratePadPopViewIsVible,
                    placeholder = "환율을 입력해주세요",
                    modifier = Modifier
                        .weight(0.8f)
                        .padding(horizontal = 10.dp)
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
            }





            Box() {
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

                            }},
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

                            })
                    }
                }
            }


        }
    }

}

