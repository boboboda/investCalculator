package com.bobodroid.myapplication.components.Dialogs

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
import com.bobodroid.myapplication.components.CustomCard
import com.bobodroid.myapplication.components.CustomOutLinedTextField
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
import java.math.BigDecimal

@Composable
fun SpreadDialog(
    onDismissRequest: (Boolean) -> Unit,
    receiveRate: String,
    currency: String,
    onClicked:(String) -> Unit,
) {


    val scope = rememberCoroutineScope()

    val focusManager = LocalFocusManager.current

    val focusRequester by remember { mutableStateOf(FocusRequester()) }

    var userInput by remember {
        mutableStateOf("")
    }

    var changeRate by remember { mutableStateOf(receiveRate) }


    Dialog(onDismissRequest = {
        onDismissRequest(false)
    }
    ) {

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

                Text(text = "스프레드 설정", fontSize = 18.sp)
            }
            Divider(Modifier.fillMaxWidth())

            Column {
                Text(text = "현재 ${currency}: ${receiveRate}")

                Text(text = "변경 ${currency}: ${changeRate}")
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
                        value = userInput,
                        placeholder = "목표환율을 입력해주세요",
                        onValueChange = {
                            userInput = it.filter { it != ',' }

                            var intChangeRate = receiveRate.toBigDecimal()

                            val haveDotUserInput = userInput.startsWith(".")

                            var filterUserInput = if(haveDotUserInput) userInput.drop(1) else userInput

                            val plusAndMinus = filterUserInput.contains("-")

                            val signFilterUserInput = filterUserInput.dropWhile {it == '-'}

                            Log.d(TAG, "userInput ${userInput} filterUserInput${filterUserInput} signFilterUserInput ${signFilterUserInput}")

                            if(signFilterUserInput == "") {
                                changeRate = receiveRate
                            } else {

                                if(plusAndMinus) {
                                    intChangeRate -= signFilterUserInput.toBigDecimal()

                                    changeRate = intChangeRate.toString()
                                } else {
                                    intChangeRate += signFilterUserInput.toBigDecimal()

                                    changeRate = intChangeRate.toString()
                                }


                            }
                        }
                    )
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
                              onClicked.invoke(userInput)
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
                            text = "설정",
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