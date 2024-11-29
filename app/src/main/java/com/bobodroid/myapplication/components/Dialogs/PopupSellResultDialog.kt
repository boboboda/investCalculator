package com.bobodroid.myapplication.components.Dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.bobodroid.myapplication.components.Buttons
import com.bobodroid.myapplication.extensions.toDecUs
import com.bobodroid.myapplication.extensions.toPer
import com.bobodroid.myapplication.extensions.toWon
import com.bobodroid.myapplication.extensions.toYen
import com.bobodroid.myapplication.models.datamodels.roomDb.DrBuyRecord
import com.bobodroid.myapplication.models.datamodels.roomDb.ForeignCurrencyRecord
import com.bobodroid.myapplication.models.datamodels.roomDb.YenBuyRecord
import com.bobodroid.myapplication.models.viewmodels.DollarViewModel
import com.bobodroid.myapplication.models.viewmodels.YenViewModel
import com.bobodroid.myapplication.ui.theme.SellButtonColor
import com.bobodroid.myapplication.ui.theme.SellPopColor
import java.util.UUID


@Composable
fun SellResultDialog(
    onDismissRequest: ((Boolean)->Unit)?,
    selectedRecord:()-> Unit,
    percent: String,
    sellProfit: String
) {


    Dialog(
        onDismissRequest = { onDismissRequest?.invoke(false) },
        properties = DialogProperties()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = SellPopColor,
                    shape = RoundedCornerShape(5.dp)
                )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 20.dp, start = 20.dp, end = 20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start
            ) {

                Box(
                    modifier = Modifier
                        .background(
                            color = Color.White,
                            shape = RoundedCornerShape(5.dp)
                        )
                        .wrapContentHeight()
                        .weight(0.2f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "수익",
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(5.dp).padding(vertical = 5.dp))
                }

                Spacer(modifier = Modifier.width(10.dp))

                Box(
                    modifier = Modifier
                        .background(
                            color = Color.White,
                            shape = RoundedCornerShape(5.dp)
                        )
                        .wrapContentHeight()
                        .weight(0.8f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = sellProfit.toFloat().toWon(),
                        color = Color.Red,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(5.dp).padding(vertical = 5.dp)
                    )
                }

            }

            Row(
                modifier = Modifier
                    .wrapContentSize()
                    .padding(top = 20.dp, start = 20.dp, end = 20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start
            ) {

                Box(
                    modifier = Modifier
                        .background(
                            color = Color.White,
                            shape = RoundedCornerShape(5.dp)
                        )
                        .wrapContentHeight()
                        .weight(0.2f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "수익률",
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(5.dp).padding(vertical = 5.dp))
                }


                Spacer(modifier = Modifier.width(10.dp))

                Box(
                    modifier = Modifier
                        .background(
                            color = Color.White,
                            shape = RoundedCornerShape(5.dp)
                        )
                        .wrapContentHeight()
                        .weight(0.8f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${percent.toFloat().toPer()} %",
                        color = Color.Red,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(5.dp).padding(vertical = 5.dp)
                    )
                }

            }



            Spacer(modifier = Modifier.height(25.dp))
            Row(
                modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp)
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                Buttons(
                    onClicked = {
                        selectedRecord()
                    },
                    color = SellButtonColor,
                    fontColor = Color.White,
                    modifier = Modifier
                        .height(40.dp)
                        .width(80.dp)) {
                    Text(text = "기록", fontSize = 15.sp)
                }

                Spacer(modifier = Modifier.width(40.dp))

                Buttons(
                    onClicked = {
                        onDismissRequest?.invoke(false)
                    },
                    color = SellButtonColor,
                    fontColor = Color.White,
                    modifier = Modifier
                        .height(40.dp)
                        .width(80.dp)) {
                    Text(text = "닫기", fontSize = 15.sp)
                }
            }
        }
    }
}

@Composable
fun YenSellResultDialog(
    onDismissRequest: ((Boolean) -> Unit)?,
    onClicked: ((Boolean) -> Unit)?,
    sellAction: () -> Unit,
    yenViewModel: YenViewModel,
    buyRecord: YenBuyRecord
) {



    val percent: State<Float> = yenViewModel.getPercentFlow.collectAsState()

    val inputMoney: State<String> = yenViewModel.sellYenFlow.collectAsState()


    Dialog(
        onDismissRequest = { onDismissRequest?.invoke(false) },
        properties = DialogProperties()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = SellPopColor,
                    shape = RoundedCornerShape(5.dp)
                )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 20.dp, start = 20.dp, end = 20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start
            ) {

                Box(
                    modifier = Modifier
                        .background(
                            color = Color.White,
                            shape = RoundedCornerShape(5.dp)
                        )
                        .wrapContentHeight()
                        .weight(0.2f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "수익",
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(5.dp).padding(vertical = 5.dp))
                }

                Spacer(modifier = Modifier.width(10.dp))

                Box(
                    modifier = Modifier
                        .background(
                            color = Color.White,
                            shape = RoundedCornerShape(5.dp)
                        )
                        .wrapContentHeight()
                        .weight(0.8f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${inputMoney.value.toFloat().toWon()}",
                        color = Color.Red,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(5.dp).padding(vertical = 5.dp)
                    )
                }

            }

            Row(
                modifier = Modifier
                    .wrapContentSize()
                    .padding(top = 20.dp, start = 20.dp, end = 20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start
            ) {

                Box(
                    modifier = Modifier
                        .background(
                            color = Color.White,
                            shape = RoundedCornerShape(5.dp)
                        )
                        .wrapContentHeight()
                        .weight(0.2f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "수익률",
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(5.dp).padding(vertical = 5.dp))
                }


                Spacer(modifier = Modifier.width(10.dp))

                Box(
                    modifier = Modifier
                        .background(
                            color = Color.White,
                            shape = RoundedCornerShape(5.dp)
                        )
                        .wrapContentHeight()
                        .weight(0.8f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${percent.value.toPer()} %",
                        color = Color.Red,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(5.dp).padding(vertical = 5.dp)
                    )
                }

            }



            Spacer(modifier = Modifier.height(25.dp))
            Row(
                modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp)
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                Buttons(
                    onClicked = {
                        sellAction()
                        yenViewModel.sellRecordValue(buyRecord)
                        onClicked?.invoke(false)
                    },
                    color = SellButtonColor,
                    fontColor = Color.White,
                    modifier = Modifier
                        .height(40.dp)
                        .width(80.dp),) {
                    Text(text = "", fontSize = 15.sp)
                }

                Spacer(modifier = Modifier.width(40.dp))

                Buttons(
                    onClicked = {
                        onDismissRequest?.invoke(false)
                        onClicked?.invoke(false)
                    },
                    color = SellButtonColor,
                    fontColor = Color.White,
                    modifier = Modifier
                        .height(40.dp)
                        .width(80.dp),) {
                    Text(text = "닫기", fontSize = 15.sp)
                }
            }
        }
    }
}