package com.bobodroid.myapplication.components.Dialogs

import androidx.compose.foundation.background
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.bobodroid.myapplication.components.CustomCard
import com.bobodroid.myapplication.components.addFocusCleaner
import com.bobodroid.myapplication.models.datamodels.service.UserApi.Rate
import com.bobodroid.myapplication.ui.theme.DialogBackgroundColor
import com.bobodroid.myapplication.ui.theme.TitleCardColor
import com.bobodroid.myapplication.ui.theme.WelcomeScreenBackgroundColor

@Composable
fun TargetRateDialog(
    onDismissRequest: (Boolean) -> Unit,
    lastRate: Rate? = null,
    selected: (Rate) -> Unit
    ) {

    val focusManager = LocalFocusManager.current



    val focusRequester by remember { mutableStateOf(FocusRequester()) }

    var userInput by remember {
        mutableStateOf("")
    }


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

            Row(
                modifier = Modifier
                    .wrapContentHeight()
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp)
                    .padding(),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(10.dp, alignment = Alignment.Start),
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 5.dp)
                        .padding(vertical = 5.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                    horizontalAlignment = Alignment.Start
                ) {
                    if (lastRate != null) {
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
                                label = "${lastRate.number}",
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
                                    text = "목표환율: ${lastRate.rate}"
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

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
//                CustomOutLinedTextField(
//                    modifier = Modifier,
//                    value = userInput,
//                    placeholder = "목표환율을 입력해주세요",
//                    onValueChange = {
//                        userInput = it
//                    }
//                )

                OutlinedTextField(
                    value = userInput,
                    placeholder = {
                        Text("목표환율을 입력해주세요")
                    },
                    onValueChange = {
                        userInput = it
                    },
                    maxLines = 1,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentSize()
                    .padding(top = 10.dp)
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {

                Button(
                    shape = RoundedCornerShape(5.dp),
                    onClick = {


                        val countUpRateNumber = (lastRate?.number ?: 0) + 1

                        val addRate = Rate(
                            number = countUpRateNumber,
                            rate = userInput.toInt()
                        )

                        selected(addRate)

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
    }
}