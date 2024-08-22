package com.bobodroid.myapplication.components.Dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.bobodroid.myapplication.components.Buttons
import com.bobodroid.myapplication.ui.theme.SellButtonColor
import androidx.compose.material.OutlinedTextField
import androidx.compose.ui.text.input.KeyboardType

@Composable
fun TextFieldDialog(
    onDismissRequest: (Boolean) -> Unit,
    onClickedLabel: String,
    placeholder: String,
    onClicked: (String) -> Unit,
    closeButtonLabel: String,
    keyboardType: KeyboardType = KeyboardType.Text,
    textContent: (@Composable () -> Unit)? = null,

) {

    var userInput by remember { mutableStateOf("") }


    Dialog(
        onDismissRequest = { onDismissRequest(false) },
        properties = DialogProperties()
    ) {
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
            verticalArrangement = Arrangement.Center
        ) {

            textContent?.let {
                textContent()
            }


            OutlinedTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .padding(horizontal = 10.dp),
                keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
                placeholder = {
                    Text(text = placeholder)
                },
                value = userInput,
                onValueChange = {
                    userInput = it
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

            Spacer(modifier = Modifier.height(20.dp))
            Row(
                modifier =
                Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Buttons(onClicked = {
                        onClicked.invoke(userInput)
                                userInput = ""
                                },

                    color = SellButtonColor,
                    fontColor = Color.White,
                    modifier = Modifier
                        .height(40.dp)
                        .width(80.dp))
                {
                    Text(text = onClickedLabel, fontSize = 15.sp)
                }

                Spacer(modifier = Modifier.width(25.dp))

                Buttons(
                    onClicked = {onDismissRequest(false)},
                    color = SellButtonColor,
                    fontColor = Color.White,
                    modifier = Modifier
                        .height(40.dp)
                        .wrapContentSize()) {
                    Text(text = closeButtonLabel, fontSize = 15.sp)
                }
            }
        }
    }
}