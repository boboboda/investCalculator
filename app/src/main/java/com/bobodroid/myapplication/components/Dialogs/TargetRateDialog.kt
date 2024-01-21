package com.bobodroid.myapplication.components.Dialogs

import android.app.Activity
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.SnackbarHostState
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.bobodroid.myapplication.MainActivity.Companion.TAG
import com.bobodroid.myapplication.components.CardButton
import com.bobodroid.myapplication.components.CustomCard
import com.bobodroid.myapplication.components.CustomOutLinedTextField
import com.bobodroid.myapplication.components.admobs.showTargetRewardedAdvertisement
import com.bobodroid.myapplication.lists.dollorList.addFocusCleaner
import com.bobodroid.myapplication.models.datamodels.DollarTargetHighRate
import com.bobodroid.myapplication.models.datamodels.DollarTargetLowRate
import com.bobodroid.myapplication.models.datamodels.TargetRate
import com.bobodroid.myapplication.models.datamodels.YenTargetHighRate
import com.bobodroid.myapplication.models.datamodels.YenTargetLowRate
import com.bobodroid.myapplication.models.viewmodels.AllViewModel
import com.bobodroid.myapplication.ui.theme.DialogBackgroundColor
import com.bobodroid.myapplication.ui.theme.TitleCardColor
import com.bobodroid.myapplication.ui.theme.WelcomeScreenBackgroundColor
import io.grpc.Context

@Composable
fun TargetRateDialog(
    onDismissRequest: (Boolean) -> Unit,
    targetRate: TargetRate,
    currency: String,
    highAndLowState: String,
    context: android.content.Context,
    onClicked: (() -> Unit)? = null,
    allViewModel: AllViewModel,
) {


    val scope = rememberCoroutineScope()

    val highDollarNumber = if (targetRate.dollarHighRateList.isNullOrEmpty()) {
        "0"
    } else { "${targetRate.dollarHighRateList?.size ?: "" }"}

    val filterDollarHighRate = targetRate.dollarHighRateList?.filter { it.number == highDollarNumber } ?: emptyList()

    val lowDollarNumber = if (targetRate.dollarLowRateList.isNullOrEmpty()) {
        "0"
    } else { "${targetRate.dollarLowRateList?.size ?: "" }"}

    val filterDollarLowRate = targetRate.dollarLowRateList?.filter { it.number == lowDollarNumber } ?: emptyList()


    val highYenNumber = if (targetRate.yenHighRateList.isNullOrEmpty()) {
        "0"
    } else { "${targetRate.yenHighRateList?.size ?: ""}"}

    val filterYenHighRate = targetRate.yenHighRateList?.filter { it.number == highYenNumber } ?: emptyList()

    val lowYenNumber = if (targetRate.yenLowRateList.isNullOrEmpty()) {
        "0"
    } else { "${targetRate.yenLowRateList?.size ?: ""}"}

    val filterYenLowRate = targetRate.yenLowRateList?.filter { it.number == lowYenNumber } ?: emptyList()

    var rate by remember { mutableStateOf("") }

    val focusManager = LocalFocusManager.current

    var adViewAskDialog by remember { mutableStateOf(false) }

    val focusRequester by remember { mutableStateOf(FocusRequester()) }


    Dialog(onDismissRequest = {
        onDismissRequest(false)
    }) {

        Column(
            modifier = Modifier
                .wrapContentSize()
                .background(
                    color = DialogBackgroundColor,
                    shape = RoundedCornerShape(16.dp)
                ),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {

            Row(
                modifier = Modifier
                    .wrapContentHeight()
                    .addFocusCleaner(focusManager)
                    .fillMaxWidth()
                    .padding(vertical = 5.dp)
                    .padding(start = 10.dp),
                horizontalArrangement = Arrangement.Center
            ) {

                Text(text = "목표환율 알람 설정", fontSize = 25.sp)
            }
            Divider(Modifier.fillMaxWidth())

            when(currency) {
                "달러"-> {
                    when(highAndLowState) {
                        "고점" -> {
                            Row(
                                modifier = Modifier
                                    .wrapContentHeight()
                                    .fillMaxWidth()
                                    .padding(horizontal = 10.dp)
                                    .padding(),
                                verticalAlignment = Alignment.Top,
                                horizontalArrangement = Arrangement.spacedBy(10.dp, alignment = Alignment.Start),
                            ) {
                                Column {
                                    CustomCard(
                                        label = currency,
                                        fontSize = 15,
                                        modifier = Modifier
                                            .padding(start = 5.dp, top = 8.dp)
                                            .width(50.dp)
                                            .height(40.dp)
                                            .padding(bottom = 5.dp),
                                        fontColor = Color.Black,
                                        cardColor = Color.White,
                                    )

                                    CustomCard(
                                        label = highAndLowState,
                                        fontSize = 15,
                                        modifier = Modifier
                                            .padding(start = 5.dp, top = 8.dp)
                                            .width(50.dp)
                                            .height(40.dp),
                                        fontColor = Color.Black,
                                        cardColor = Color.White,
                                    )
                                }
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(start = 5.dp)
                                        .padding(vertical = 5.dp)
                                        .border(
                                            width = 1.dp,
                                            color = Color.Black,
                                            shape = RoundedCornerShape(10.dp)
                                        ),
                                    verticalArrangement = Arrangement.spacedBy(2.dp),
                                    horizontalAlignment = Alignment.Start
                                ) {
                                    if (!filterDollarHighRate.isNullOrEmpty()) {
                                        Text(
                                            modifier = Modifier
                                                .padding(all = 5.dp),
                                            text = "현재 설정된 마지막 목표환율"
                                        )

                                        Row(
                                            modifier = Modifier
                                                .wrapContentSize(),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(
                                                5.dp,
                                                alignment = Alignment.Start
                                            )
                                        ) {
                                            CustomCard(
                                                label = "$highDollarNumber",
                                                fontSize = 12,
                                                modifier = Modifier
                                                    .height(20.dp)
                                                    .width(25.dp)
                                                    .padding(horizontal = 5.dp),
                                                fontColor = Color.Black,
                                                cardColor = TitleCardColor
                                            )

                                            Column(
                                                modifier = Modifier
                                                    .wrapContentSize()
                                                    .padding(bottom = 3.dp)
                                            ) {
                                                Text(
                                                    modifier = Modifier
                                                        .padding(start = 5.dp),
                                                    text = "목표환율: ${filterDollarHighRate?.first()?.highRate ?: ""}"
                                                )
                                            }
                                        }
                                    } else {
                                        Text(
                                            modifier = Modifier
                                                .padding(all = 5.dp),
                                            text = "목표 환율 고점은 목표환율보다 이상일 때 알림을 받을 수 있습니다." +
                                                    "저점은 목표환율보다 이하일 때 알림을 받을 수 있습니다."
                                        )
                                    }
                                }

                            }


                        }
                        "저점" -> {
                            Row(
                                modifier = Modifier
                                    .wrapContentHeight()
                                    .fillMaxWidth()
                                    .padding(horizontal = 10.dp)
                                    .padding(),
                                verticalAlignment = Alignment.Top,
                                horizontalArrangement = Arrangement.spacedBy(10.dp, alignment = Alignment.Start),
                            ) {
                                Column {
                                    CustomCard(
                                        label = currency,
                                        fontSize = 15,
                                        modifier = Modifier
                                            .padding(start = 5.dp, top = 8.dp)
                                            .width(50.dp)
                                            .height(40.dp)
                                            .padding(bottom = 5.dp),
                                        fontColor = Color.Black,
                                        cardColor = Color.White,
                                    )

                                    CustomCard(
                                        label = highAndLowState,
                                        fontSize = 15,
                                        modifier = Modifier
                                            .padding(start = 5.dp, top = 8.dp)
                                            .width(50.dp)
                                            .height(40.dp),
                                        fontColor = Color.Black,
                                        cardColor = Color.White,
                                    )
                                }
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(start = 5.dp)
                                        .padding(vertical = 5.dp)
                                        .border(
                                            width = 1.dp,
                                            color = Color.Black,
                                            shape = RoundedCornerShape(10.dp)
                                        ),
                                    verticalArrangement = Arrangement.spacedBy(2.dp),
                                    horizontalAlignment = Alignment.Start
                                ) {
                                    if (!filterDollarLowRate.isNullOrEmpty()) {
                                        Text(
                                            modifier = Modifier
                                                .padding(all = 5.dp),
                                            text = "현재 설정된 마지막 목표환율"
                                        )

                                        Row(
                                            modifier = Modifier
                                                .wrapContentSize(),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(
                                                5.dp,
                                                alignment = Alignment.Start
                                            )
                                        ) {
                                            CustomCard(
                                                label = "$lowDollarNumber",
                                                fontSize = 12,
                                                modifier = Modifier
                                                    .height(20.dp)
                                                    .width(25.dp)
                                                    .padding(horizontal = 5.dp),
                                                fontColor = Color.Black,
                                                cardColor = TitleCardColor
                                            )

                                            Column(
                                                modifier = Modifier
                                                    .wrapContentSize()
                                                    .padding(bottom = 3.dp)
                                            ) {
                                                Text(
                                                    modifier = Modifier
                                                        .padding(start = 5.dp),
                                                    text = "목표환율: ${filterDollarLowRate?.first()?.lowRate ?: ""}"
                                                )
                                            }
                                        }
                                    } else {
                                        Text(
                                            modifier = Modifier
                                                .padding(all = 5.dp),
                                            text = "목표 환율 고점은 목표환율보다 이상일 때 알림을 받을 수 있습니다." +
                                                    "저점은 목표환율보다 이하일 때 알림을 받을 수 있습니다."
                                        )
                                    }
                                }

                            }
                        }
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 15.dp, end = 15.dp)
                    ) {

                        Row(
                            modifier = Modifier
                                .wrapContentSize(),
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            CustomOutLinedTextField(
                                modifier = Modifier
                                    .focusRequester(focusRequester = focusRequester)
                                    .weight(1f),
                                value = rate,
                                placeholder = "목표환율을 입력해주세요",
                                onValueChange = {
                                    rate = it.filter { it != '-' && it != ',' }
                                }
                            )
                        }

                    }
                }

                "엔화"-> {
                    when(highAndLowState) {
                        "고점" -> {
                            Row(
                                modifier = Modifier
                                    .wrapContentHeight()
                                    .fillMaxWidth()
                                    .padding(horizontal = 10.dp)
                                    .padding(),
                                verticalAlignment = Alignment.Top,
                                horizontalArrangement = Arrangement.spacedBy(10.dp, alignment = Alignment.Start),
                            ) {
                                Column {
                                    CustomCard(
                                        label = currency,
                                        fontSize = 15,
                                        modifier = Modifier
                                            .padding(start = 5.dp, top = 8.dp)
                                            .width(50.dp)
                                            .height(40.dp)
                                            .padding(bottom = 5.dp),
                                        fontColor = Color.Black,
                                        cardColor = Color.White,
                                    )

                                    CustomCard(
                                        label = highAndLowState,
                                        fontSize = 15,
                                        modifier = Modifier
                                            .padding(start = 5.dp, top = 8.dp)
                                            .width(50.dp)
                                            .height(40.dp),
                                        fontColor = Color.Black,
                                        cardColor = Color.White,
                                    )
                                }
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(start = 5.dp)
                                        .padding(vertical = 5.dp)
                                        .border(
                                            width = 1.dp,
                                            color = Color.Black,
                                            shape = RoundedCornerShape(10.dp)
                                        ),
                                    verticalArrangement = Arrangement.spacedBy(2.dp),
                                    horizontalAlignment = Alignment.Start
                                ) {
                                    if (!filterYenHighRate.isNullOrEmpty()) {
                                        Text(
                                            modifier = Modifier
                                                .padding(all = 5.dp),
                                            text = "현재 설정된 마지막 목표환율"
                                        )

                                        Row(
                                            modifier = Modifier
                                                .wrapContentSize(),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(
                                                5.dp,
                                                alignment = Alignment.Start
                                            )
                                        ) {
                                            CustomCard(
                                                label = "$highYenNumber",
                                                fontSize = 12,
                                                modifier = Modifier
                                                    .height(20.dp)
                                                    .width(25.dp)
                                                    .padding(horizontal = 5.dp),
                                                fontColor = Color.Black,
                                                cardColor = TitleCardColor
                                            )

                                            Column(
                                                modifier = Modifier
                                                    .wrapContentSize()
                                                    .padding(bottom = 3.dp)
                                            ) {
                                                Text(
                                                    modifier = Modifier
                                                        .padding(start = 5.dp),
                                                    text = "목표환율: ${filterYenHighRate?.first()?.highRate ?: ""}"
                                                )
                                            }
                                        }
                                    } else {
                                        Text(
                                            modifier = Modifier
                                                .padding(all = 5.dp),
                                            text = "목표 환율 고점은 목표환율보다 이상일 때 알림을 받을 수 있습니다." +
                                                    "저점은 목표환율보다 이하일 때 알림을 받을 수 있습니다."
                                        )
                                    }
                                }

                            }


                        }
                        "저점" -> {
                            Row(
                                modifier = Modifier
                                    .wrapContentHeight()
                                    .fillMaxWidth()
                                    .padding(horizontal = 10.dp)
                                    .padding(),
                                verticalAlignment = Alignment.Top,
                                horizontalArrangement = Arrangement.spacedBy(10.dp, alignment = Alignment.Start),
                            ) {
                                Column {
                                    CustomCard(
                                        label = currency,
                                        fontSize = 15,
                                        modifier = Modifier
                                            .padding(start = 5.dp, top = 8.dp)
                                            .width(50.dp)
                                            .height(40.dp)
                                            .padding(bottom = 5.dp),
                                        fontColor = Color.Black,
                                        cardColor = Color.White,
                                    )

                                    CustomCard(
                                        label = highAndLowState,
                                        fontSize = 15,
                                        modifier = Modifier
                                            .padding(start = 5.dp, top = 8.dp)
                                            .width(50.dp)
                                            .height(40.dp),
                                        fontColor = Color.Black,
                                        cardColor = Color.White,
                                    )
                                }
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(start = 5.dp)
                                        .padding(vertical = 5.dp)
                                        .border(
                                            width = 1.dp,
                                            color = Color.Black,
                                            shape = RoundedCornerShape(10.dp)
                                        ),
                                    verticalArrangement = Arrangement.spacedBy(2.dp),
                                    horizontalAlignment = Alignment.Start
                                ) {
                                    if (!filterYenLowRate.isNullOrEmpty()) {
                                        Text(
                                            modifier = Modifier
                                                .padding(all = 5.dp),
                                            text = "현재 설정된 마지막 목표환율"
                                        )

                                        Row(
                                            modifier = Modifier
                                                .wrapContentSize(),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(
                                                5.dp,
                                                alignment = Alignment.Start
                                            )
                                        ) {
                                            CustomCard(
                                                label = "$lowYenNumber",
                                                fontSize = 12,
                                                modifier = Modifier
                                                    .height(20.dp)
                                                    .width(25.dp)
                                                    .padding(horizontal = 5.dp),
                                                fontColor = Color.Black,
                                                cardColor = TitleCardColor
                                            )

                                            Column(
                                                modifier = Modifier
                                                    .wrapContentSize()
                                                    .padding(bottom = 3.dp)
                                            ) {
                                                Text(
                                                    modifier = Modifier
                                                        .padding(start = 5.dp),
                                                    text = "목표환율: ${filterYenLowRate?.first()?.lowRate ?: ""}"
                                                )
                                            }
                                        }
                                    } else {
                                        Text(
                                            modifier = Modifier
                                                .padding(all = 5.dp),
                                            text = "목표 환율 고점은 목표환율보다 이상일 때 알림을 받을 수 있습니다." +
                                                    "저점은 목표환율보다 이하일 때 알림을 받을 수 있습니다."
                                        )
                                    }
                                }

                            }
                        }
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 15.dp, end = 15.dp)
                    ) {

                        Row(
                            modifier = Modifier
                                .wrapContentSize(),
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            CustomOutLinedTextField(
                                modifier = Modifier
                                    .focusRequester(focusRequester = focusRequester)
                                    .weight(1f),
                                value = rate,
                                placeholder = "목표환율 입력해주세요",
                                onValueChange = {
                                    rate = it.filter { it != '-' && it != ',' }
                                }
                            )
                        }

                    }



                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentSize()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {

                Button(
                    shape = RoundedCornerShape(5.dp),
                    onClick = {

                        adViewAskDialog = true

                    },
                    colors = ButtonDefaults.buttonColors(WelcomeScreenBackgroundColor)
                ) {

                    Box(
                        modifier = Modifier
                            .wrapContentSize()
                            .padding(horizontal = 5.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "추가",
                            color = Color.Black,
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center,
                        )
                    }

                }

                Spacer(modifier = Modifier.width(15.dp))

                Button(
                    shape = RoundedCornerShape(5.dp),
                    onClick = {
                        onDismissRequest(false)
                    },
                    colors = ButtonDefaults.buttonColors(WelcomeScreenBackgroundColor)
                )
                {
                    Box(
                        modifier = Modifier
                            .wrapContentSize()
                            .padding(horizontal = 5.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "닫기",
                            color = Color.Black,
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center,
                        )
                    }
                }

            }

            Spacer(modifier = Modifier.height(10.dp))

        }

        if(adViewAskDialog) {
            AskTriggerDialog(
                onDismissRequest = {
                    adViewAskDialog = false
                },
                title = "광고 시청하여 목표환율을 설정하시겠습니까?",
                onClickedLabel = "예") {
                showTargetRewardedAdvertisement(context, onAdDismissed = {
                    when(currency) {
                        "달러"-> {
                            when(highAndLowState) {
                                "고점" -> {
                                    allViewModel.targetRateAdd(
                                        drHighRate = DollarTargetHighRate(
                                            number = highDollarNumber.toInt().plus(1).toString(),
                                            highRate = rate),
                                        drLowRate = null,
                                        yenHighRate = null,
                                        yenLowRate = null
                                    )
                                    onClicked?.invoke()
                                }
                                "저점" -> {
                                    allViewModel.targetRateAdd(
                                        drHighRate = null,
                                        drLowRate = DollarTargetLowRate(
                                            number = lowDollarNumber.toInt().plus(1).toString(),
                                            lowRate = rate),
                                        yenHighRate = null,
                                        yenLowRate = null
                                    )
                                    onClicked?.invoke()
                                }
                            }

                        }
                        "엔화"-> {
                            when(highAndLowState) {
                                "고점" -> {
                                    allViewModel.targetRateAdd(
                                        drHighRate = null,
                                        drLowRate = null,
                                        yenHighRate = YenTargetHighRate(
                                            number = highYenNumber.toInt().plus(1).toString(),
                                            highRate = rate),
                                        yenLowRate = null
                                    )
                                    onClicked?.invoke()
                                }
                                "저점" -> {
                                    allViewModel.targetRateAdd(
                                        drHighRate = null,
                                        drLowRate = null,
                                        yenHighRate = null,
                                        yenLowRate = YenTargetLowRate(
                                            number = lowYenNumber.toInt().plus(1).toString(),
                                            lowRate = rate)
                                    )
                                    onClicked?.invoke()
                                }
                            }

                        }
                    }
                    adViewAskDialog = false
                })
            }
        }




    }
}