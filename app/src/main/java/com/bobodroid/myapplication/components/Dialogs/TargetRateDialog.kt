package com.bobodroid.myapplication.components.Dialogs

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.bobodroid.myapplication.components.Caldenders.endDatePickerDialog
import com.bobodroid.myapplication.components.Caldenders.startDatePickerDialog
import com.bobodroid.myapplication.components.CardButton
import com.bobodroid.myapplication.models.viewmodels.AllViewModel
import com.bobodroid.myapplication.ui.theme.DialogBackgroundColor
import com.bobodroid.myapplication.ui.theme.WelcomeScreenBackgroundColor
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.GregorianCalendar

@Composable
fun TargetDialog(
    onDismissRequest: (Boolean) -> Unit,
    onClicked: (() -> Unit)? = null,
    allViewModel: AllViewModel
) {

    val datePickerEnableState = remember { mutableStateOf(false) }

    val isStartDateOpen = remember { mutableStateOf(false) }

    val isEndDateOpen = remember { mutableStateOf(false) }


    val scope = rememberCoroutineScope()

    val selectCardLabel = remember { mutableStateOf("달러") }





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
                    .height(50.dp)
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp)
                    .padding(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {

                CardButton(
                    label = "달러",
                    selectedLabel = selectCardLabel.value,
                    onClicked = {


                    },
                    fontSize = 15,
                    modifier = Modifier.weight(1f),
                    fontColor = Color.Black,
                    buttonColor = Color.White,
                    disableColor = Color.LightGray
                )

                CardButton(
                    label = "한달",
                    selectedLabel = selectCardLabel.value,
                    onClicked = {


                    },
                    fontSize = 15,
                    modifier = Modifier.weight(1f),
                    fontColor = Color.Black,
                    buttonColor = Color.White,
                    disableColor = Color.LightGray
                )


            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 15.dp, end = 15.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {


            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentSize()
                    .padding(start = 30.dp, end = 10.dp)
            ) {

                Button(
                    shape = RoundedCornerShape(5.dp),
                    onClick = {

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
                            text = "조회",
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