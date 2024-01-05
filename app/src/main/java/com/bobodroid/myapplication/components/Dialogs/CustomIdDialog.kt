package com.bobodroid.myapplication.components.Dialogs

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.SnackbarDuration
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
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
import com.bobodroid.myapplication.components.DrSellDatePickerDialog
import com.bobodroid.myapplication.components.RateNumberField
import com.bobodroid.myapplication.components.SellResultDialog
import com.bobodroid.myapplication.models.datamodels.DrBuyRecord
import com.bobodroid.myapplication.models.viewmodels.AllViewModel
import com.bobodroid.myapplication.models.viewmodels.DollarViewModel
import com.bobodroid.myapplication.ui.theme.SellButtonColor
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomIdDialog(
    onDismissRequest: (Boolean) -> Unit,
    allViewModel: AllViewModel
) {

    var userInput by remember { mutableStateOf("") }

    var isBtnActive = if(userInput.isNullOrEmpty()) false else true

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
                .padding(top = 30.dp, bottom = 30.dp)
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {


            OutlinedTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .padding(horizontal = 10.dp)
                ,
                placeholder = {
                    Text(text = "커스텀 아이디를 입력해주세요")
                },
                value = userInput,
                onValueChange = {
                    userInput = it
                },
                textStyle = TextStyle(
                    baselineShift = BaselineShift.None,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Start),
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
                Buttons(label = "수정",
                    onClicked = {
                                allViewModel.localIdCustom(userInput)
                    },

                    color = SellButtonColor,
                    fontColor = Color.White,
                    modifier = Modifier
                        .height(40.dp)
                        .width(80.dp),
                    enabled = isBtnActive,
                    fontSize = 15)
                Spacer(modifier = Modifier.width(25.dp))

                Buttons(label = "닫기",
                    onClicked = {onDismissRequest(false)},
                    color = SellButtonColor,
                    fontColor = Color.White,
                    modifier = Modifier
                        .height(40.dp)
                        .width(80.dp)
                    , fontSize = 15)
            }
        }
    }
}