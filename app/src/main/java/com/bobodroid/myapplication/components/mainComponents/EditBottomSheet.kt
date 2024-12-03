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
import com.bobodroid.myapplication.models.datamodels.roomDb.CurrencyType
import com.bobodroid.myapplication.models.datamodels.roomDb.ForeignCurrencyRecord
import com.bobodroid.myapplication.models.viewmodels.MainUiState
import com.bobodroid.myapplication.ui.theme.BottomSheetTitleColor
import com.bobodroid.myapplication.ui.theme.BuyColor
import com.bobodroid.myapplication.ui.theme.WelcomeScreenBackgroundColor
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditBottomSheet(
    editRecord:ForeignCurrencyRecord,
    mainUiState: MainUiState,
    sheetState: SheetState,
    onEvent: (EditBottomSheetEvent) -> Unit
) {
    var numberInput by remember { mutableStateOf(editRecord.money ?: "") }

    var rateInput by remember { mutableStateOf(editRecord.rate ?: "") }

    val isBtnActive = numberInput.isNotEmpty() && rateInput.isNotEmpty()

    val coroutineScope = rememberCoroutineScope()



    var numberPadPopViewIsVible by remember { mutableStateOf(false) }

    var ratePadPopViewIsVible by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = {
            onEvent(EditBottomSheetEvent.DismissRequest)
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
                        onEvent(EditBottomSheetEvent.EditSelected(editRecord, numberInput, rateInput))
                    },
                    color = BuyColor,
                    fontColor = Color.Black,
                    modifier = Modifier
                ) {
                    Text(text = "수정", fontSize = 15.sp)
                }

                Buttons(
                    onClicked = {
                        onEvent(EditBottomSheetEvent.DismissRequest)
                    },
                    color = BuyColor,
                    fontColor = Color.Black,
                    modifier = Modifier,

                    ) {
                    Text(text = "닫기", fontSize = 15.sp)
                }
            }


            Row {
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
                        onEvent(EditBottomSheetEvent.ShowDatePickerDialog)
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

sealed class EditBottomSheetEvent {
    data class EditSelected(
        val record: ForeignCurrencyRecord,
        val editMoney: String,
        val editRate: String): EditBottomSheetEvent()
    data object DismissRequest : EditBottomSheetEvent()
    data object ShowDatePickerDialog: EditBottomSheetEvent()
}