package com.bobodroid.myapplication.components.mainComponents

import android.provider.Telephony.Mms.Rate
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
import androidx.compose.ui.input.pointer.motionEventSpy
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bobodroid.myapplication.components.Buttons
import com.bobodroid.myapplication.components.CustomCard
import com.bobodroid.myapplication.components.Dialogs.FloatPopupNumberView
import com.bobodroid.myapplication.components.Dialogs.PopupNumberView
import com.bobodroid.myapplication.screens.MainEvent
import com.bobodroid.myapplication.screens.PopupEvent
import com.bobodroid.myapplication.ui.theme.BottomSheetTitleColor
import com.bobodroid.myapplication.ui.theme.BuyColor
import com.bobodroid.myapplication.ui.theme.WelcomeScreenBackgroundColor
import kotlinx.coroutines.delay
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
                    enabled = rateInput.isNotEmpty(),
                    onClicked = {
                        onEvent(MainEvent.RateBottomSheetEvent.SellClicked(rateInput))
                    },
                    color = BuyColor,
                    fontColor = Color.Black,
                    modifier = Modifier
                ) {
                    Text(text = "매도", fontSize = 15.sp)
                }

                Buttons(
                    onClicked = {
                        onEvent(MainEvent.RateBottomSheetEvent.DismissRequest)
                    },
                    color = BuyColor,
                    fontColor = Color.Black,
                    modifier = Modifier,

                    ) {
                    Text(text = "닫기", fontSize = 15.sp)
                }
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth().padding(10.dp)
                    .height(45.dp),
                border = BorderStroke(1.dp, Color.Black),
                colors = CardDefaults.cardColors(contentColor = Color.Black, containerColor = Color.White),
                onClick = {
                    onEvent(MainEvent.RateBottomSheetEvent.ShowDatePickerDialog)
                }) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                ) {
                    Text(text = sellDate,
                        color = Color.Black,
                        fontSize = 18.sp ,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(),
                        textAlign = TextAlign.Center
                    )
                }

            }

            BottomSheetRateNumberField(
                title = rateInput,
                placeholder = "매도환율을 입력해주세요",
                selectedState = ratePadPopViewVisible,
                modifier = Modifier.padding(10.dp)
            ) {
                coroutineScope.launch {
                    ratePadPopViewVisible = true
                }

            }


            Box() {
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

                            })
                    }
                }
            }


        }
    }
}

