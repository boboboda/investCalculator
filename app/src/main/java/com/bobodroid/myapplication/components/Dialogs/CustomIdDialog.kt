package com.bobodroid.myapplication.components.Dialogs

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.IconButton
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.SnackbarDuration
import androidx.compose.material.SnackbarHost
import androidx.compose.material.SnackbarHostState
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.bobodroid.myapplication.R
import com.bobodroid.myapplication.components.Buttons
import com.bobodroid.myapplication.ui.theme.SellButtonColor
import kotlinx.coroutines.launch

@Composable
fun CustomIdDialog(
    onDismissRequest: (Boolean) -> Unit,
    placeholder: String,
    buttonLabel: String,
    onClicked: (id: String, pin:String) -> Unit,
) {

    var userIdInPut by remember { mutableStateOf("") }
    var userPinInput by remember { mutableStateOf("") }

    val snackbarHostState = remember{ SnackbarHostState() }

    val passwordVisible = remember {
        mutableStateOf(false)
    }

    val coroutineScope = rememberCoroutineScope()

    val passwordVisibleIcon = if (passwordVisible.value) R.drawable.ic_visible else R.drawable.ic_invisible

    var isBtnActive = userIdInPut.isNotEmpty() && userPinInput.isNotEmpty()

    Dialog(
        onDismissRequest = { onDismissRequest(false) },
        properties = DialogProperties()
    ) {
        Box(Modifier
            .wrapContentSize(),
            contentAlignment = Alignment.BottomCenter) {
            Column(
                modifier = Modifier
                    .wrapContentSize()
                    .background(
                        color = Color.White,
                        shape = RoundedCornerShape(16.dp)
                    )
                    .padding(top = 30.dp, bottom = 30.dp)
                    .padding(horizontal = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {


                OutlinedTextField(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .padding(horizontal = 10.dp),
                    placeholder = {
                        Text(text = placeholder)
                    },
                    value = userIdInPut,
                    onValueChange = {
                        userIdInPut = it
                    },
                    textStyle = TextStyle(
                        baselineShift = BaselineShift.None,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Start
                    ),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = Color.Black,
                        unfocusedBorderColor = Color.Black
                    )
                )

                Spacer(modifier = Modifier.height(15.dp))

                //pin
                OutlinedTextField(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .padding(horizontal = 10.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    placeholder = {
                        Text(text = "8자리 숫자를 입력해주세요")
                    },
                    value = userPinInput,
                    onValueChange = {

                        if(it.isBlank()) {
                            userPinInput = ""
                        } else {
                            if(it.toIntOrNull() != null){
                                userPinInput = it
                            } else {
                                userPinInput = ""
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar(
                                        "숫자만 입력해주세요",
                                        actionLabel = "닫기", SnackbarDuration.Short
                                    )
                                }
                            }
                        }


                    },
                    textStyle = TextStyle(
                        baselineShift = BaselineShift.None,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Start
                    ),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = Color.Black,
                        unfocusedBorderColor = Color.Black
                    ),
                    visualTransformation = if (passwordVisible.value) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = {
                            passwordVisible.value = !passwordVisible.value
                        }) {
                            Image(
                                painter = painterResource(id = passwordVisibleIcon),
                                contentDescription = "비밀번호 노출여부 버튼"
                            )

                        }
                    },
                )



                Spacer(modifier = Modifier.height(30.dp))

                Row(
                    modifier =
                    Modifier
                        .wrapContentSize(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Buttons(
                        label = buttonLabel,
                        onClicked = {
                            if(userPinInput.length < 8) {
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar(
                                        "pin 8자리 이상 입력해주세요",
                                        actionLabel = "닫기", SnackbarDuration.Short
                                    )
                                }
                            } else {
                                onClicked.invoke(userIdInPut, userPinInput)
                            }


                        },

                        color = SellButtonColor,
                        fontColor = Color.White,
                        modifier = Modifier
                            .height(40.dp)
                            .width(80.dp),
                        enabled = isBtnActive,
                        fontSize = 15
                    )
                    Spacer(modifier = Modifier.width(25.dp))

                    Buttons(
                        label = "닫기",
                        onClicked = { onDismissRequest(false) },
                        color = SellButtonColor,
                        fontColor = Color.White,
                        modifier = Modifier
                            .height(40.dp)
                            .width(80.dp), fontSize = 15
                    )
                }

            }

            Row(modifier = Modifier
                .wrapContentSize()
                .padding(bottom = 20.dp)
            ) {
                SnackbarHost(
                    hostState = snackbarHostState, modifier = Modifier,
                    snackbar = { snackBarData ->


                        Card(
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.5.dp, Color.Black),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(all = 10.dp),
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Start
                            ) {

                                Text(
                                    text = snackBarData.message,
                                    fontSize = 15.sp,
                                    lineHeight = 20.sp,
                                    fontWeight = FontWeight.Medium
                                )

                                Spacer(modifier = Modifier.weight(1f))

                                Text(
                                    modifier = Modifier
                                        .padding(8.dp)
                                        .clickable {
                                            snackbarHostState.currentSnackbarData?.dismiss()
                                        },
                                    text = "닫기",
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    })
            }
        }

    }
}


