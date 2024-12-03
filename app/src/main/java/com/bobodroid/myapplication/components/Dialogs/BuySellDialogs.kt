package com.bobodroid.myapplication.components.Dialogs

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.bobodroid.myapplication.models.viewmodels.DollarViewModel
import com.bobodroid.myapplication.ui.theme.SellButtonColor
import java.time.LocalDate
import androidx.compose.material.SnackbarHostState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.bobodroid.myapplication.components.Buttons
import com.bobodroid.myapplication.models.datamodels.roomDb.CurrencyType
import com.bobodroid.myapplication.models.datamodels.roomDb.DrBuyRecord
import com.bobodroid.myapplication.models.datamodels.roomDb.ForeignCurrencyRecord
import com.bobodroid.myapplication.models.datamodels.roomDb.YenBuyRecord
import com.bobodroid.myapplication.models.viewmodels.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SellDialog(
    onDismissRequest: (Boolean) -> Unit,
//    mainViewModel: MainViewModel,
//    currencyType: CurrencyType
    sellPercent: String,
    sellProfit: String,
    onEvent: (SellDialogEvent) -> Unit
) {

    var sellRate by remember {
        mutableStateOf("")
    }

    var sellDate by remember {
        mutableStateOf<String?>(null)
    }

//    var sellProfit by remember {
//        mutableStateOf("")
//    }
//
//    var sellPercent by remember {
//        mutableStateOf("")
//    }

    val isDialogOpen = remember { mutableStateOf(false) }

    var openDialog = remember { mutableStateOf(false) }

    val isBtnActive = if(sellRate != "") true else false



    Dialog(
        onDismissRequest = { onDismissRequest(false) },
        properties = DialogProperties()) {
        Column(
            modifier = Modifier
                .wrapContentSize()
                .background(
                    color = Color.White,
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(top = 20.dp, bottom = 20.dp)
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {


                    Spacer(modifier = Modifier.height(20.dp))

                    RateNumberField(
                        title = "매도환율을 입력해주세요",
                        modifier = Modifier.fillMaxWidth(),
                        onClicked = {

                    })

                    Spacer(modifier = Modifier.height(20.dp))
                    Row(
                        modifier =
                        Modifier
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Buttons(
                            onClicked = {

                                onEvent(SellDialogEvent.SellCalculate(sellRate = sellRate))

                                if(openDialog.value == false) openDialog.value = !openDialog.value else null

                            },

                            color = SellButtonColor,
                            fontColor = Color.White,
                            modifier = Modifier
                                .height(40.dp)
                                .width(80.dp),
                            enabled = isBtnActive) {
                            Text("매도", fontSize = 15.sp)
                        }


                        Spacer(modifier = Modifier.width(25.dp))

                        Buttons(
                            onClicked = {onDismissRequest(false)},
                            color = SellButtonColor,
                            fontColor = Color.White,
                            modifier = Modifier
                                .height(40.dp)
                                .width(80.dp)) {
                            Text("닫기", fontSize = 15.sp)
                        }
                    }



        }
    }
}




sealed class SellDialogEvent {
    data class SellCalculate(val sellRate: String) : SellDialogEvent()
    data class SellRecord(val sellRate: String, val sellDate: String): SellDialogEvent()
}
