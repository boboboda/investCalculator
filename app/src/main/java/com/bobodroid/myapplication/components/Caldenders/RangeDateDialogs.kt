package com.bobodroid.myapplication.components.Caldenders

import android.icu.util.Calendar
import android.icu.util.TimeZone
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.material3.Text
import androidx.compose.material.TextButton
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.bobodroid.myapplication.ui.theme.DialogBackgroundColor
import com.bobodroid.myapplication.ui.theme.WelcomeScreenBackgroundColor
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoField

@Composable
fun RangeDateDialog(
    onDismissRequest: (Boolean) -> Unit,
    callStartDate: String,
    callEndDate: String,
    selectedStartDate: (Long) -> Unit,
    selectedEndDate: (Long) -> Unit,
    onClicked: (() -> Unit)? = null,
) {

    val time = java.util.Calendar.getInstance().time

    val formatter = SimpleDateFormat("yyyy-MM-dd")

    val today = formatter.format(time)



    var firstDate = if(callStartDate == "${LocalDate.now()}") "클릭하여 시작 날짜를 선택해주세요" else {"시작: ${callStartDate}"}

    var secondDate = if(callEndDate == "${LocalDate.now()}") "클릭하여 종료 날짜를 선택해주세요" else {"종료: ${callEndDate}"}

    val isStartDateOpen = remember { mutableStateOf(false) }

    val isEndDateOpen = remember { mutableStateOf(false) }


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
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 5.dp)
                .padding(start = 10.dp)) {

                Text(text = "날짜 범위")
            }

            OutlinedButton(
                border = BorderStroke(1.dp, color = Color.Black),
                shape = RoundedCornerShape(5.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White
                ),
                onClick = {
                    isStartDateOpen.value = true
                }) {
                Box(
                    modifier = Modifier
                        .wrapContentSize()
                        .fillMaxWidth(0.8f)
                        .padding(horizontal = 5.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "$firstDate",
                        color = Color.Black,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center,
                    )
                }

                if(isStartDateOpen.value) {
                    startDatePickerDialog(
                        selectedStartDate = isStartDateOpen.value,
                        onDateSelected = {
                            selectedStartDate.invoke(it)
                            isStartDateOpen.value = false
                        },
                        onDismissRequest = {
                            isStartDateOpen.value = false
                        },
                        startDate = callStartDate,
                        endDate = callEndDate
                    )
                }

            }
            Spacer(modifier = Modifier.height(15.dp))

            OutlinedButton(
                border = BorderStroke(1.dp, color = Color.Black),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White
                ),
                shape = RoundedCornerShape(5.dp),
                onClick = {
                    isEndDateOpen.value = true
                }) {
                Box(
                    modifier = Modifier
                        .wrapContentSize()
                        .fillMaxWidth(0.8f)
                        .padding(horizontal = 5.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "$secondDate",
                        color = Color.Black,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center,
                    )
                }

                if(isEndDateOpen.value) {
                    endDatePickerDialog(
                        selectedEndDate = isEndDateOpen.value,
                        onDateSelected = {
                            selectedEndDate.invoke(it)
                            isEndDateOpen.value = false
                        },
                        onDismissRequest = {
                            isEndDateOpen.value = false
                        },
                        startDate = callStartDate,
                        endDate = callEndDate
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(modifier = Modifier
                .fillMaxWidth()
                .wrapContentSize()) {

                Button(
                    onClick = {
                        onClicked?.invoke()
                        onDismissRequest(false)
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
                            text = "확인",
                            color = Color.Black,
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center,
                        )
                    }

                }

                Spacer(modifier = Modifier.width(15.dp))

                Button(
                    onClick = {
                        onDismissRequest(false)
                    },
                    colors = ButtonDefaults.buttonColors(WelcomeScreenBackgroundColor))
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun startDatePickerDialog(
    selectedStartDate: Boolean,
    onDateSelected: ((Long) -> Unit)?,
    onDismissRequest: () -> Unit,
    endDate: String,
    startDate: String) {



    val Cgdate: LocalDate = LocalDate.parse(endDate, DateTimeFormatter.ISO_DATE)

    val year: Int = Cgdate.get(ChronoField.YEAR)
    val month: Int = Cgdate.get(ChronoField.MONTH_OF_YEAR)
    val day: Int = Cgdate.get(ChronoField.DAY_OF_MONTH)

    val calendar = Calendar.getInstance()

    val today = LocalDate.now()
//    val today = calendar.get(Calendar.DATE)
    calendar.set(today.year, today.monthValue , today.dayOfMonth) // add year, month (Jan), date

    val cgDate = startDate.let {
        val data = LocalDate.parse(it)
        calendar.set(data.year, data.monthValue - 1, data.dayOfMonth)
        val cgData = calendar
        cgData
    }

    // set the initial date
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = cgDate.timeInMillis,
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                calendar.set(year, month - 1, day)

                utcTimeMillis <= calendar.timeInMillis

                val calendar1 = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
                calendar1.timeInMillis = utcTimeMillis

                return utcTimeMillis <= calendar.timeInMillis
                        && (calendar1[Calendar.DAY_OF_WEEK] != Calendar.MONDAY)
                        && (calendar1[Calendar.DAY_OF_WEEK] != Calendar.TUESDAY)
                        && (calendar1[Calendar.DAY_OF_WEEK] != Calendar.WEDNESDAY)
                        && (calendar1[Calendar.DAY_OF_WEEK] != Calendar.THURSDAY)
                        && (calendar1[Calendar.DAY_OF_WEEK] != Calendar.FRIDAY)
            }
        }
    )

    if (selectedStartDate) {
        DatePickerDialog(
            onDismissRequest = {},
            confirmButton = {
                TextButton(onClick = {
                    onDateSelected!!.invoke(datePickerState.selectedDateMillis!!)
                }) {
                    Text(text = "선택")
                }
            },
            dismissButton = {
                TextButton(onClick = onDismissRequest) {
                    Text(text = "닫기")
                }
            }
        ) {
            DatePicker(
                state = datePickerState
            )
        }
    }


}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun endDatePickerDialog(
    selectedEndDate: Boolean,
    onDateSelected: ((Long) -> Unit)?,
    onDismissRequest: () -> Unit,
    endDate: String,
    startDate: String
) {
    val Cgdate: LocalDate = LocalDate.parse(startDate, DateTimeFormatter.ISO_DATE)

    val year: Int = Cgdate.get(ChronoField.YEAR)
    val month: Int = Cgdate.get(ChronoField.MONTH_OF_YEAR)
    val day: Int = Cgdate.get(ChronoField.DAY_OF_MONTH)

    val calendar = Calendar.getInstance()

    val today = LocalDate.now()
//    val today = calendar.get(Calendar.DATE)
    calendar.set(today.year, today.monthValue - 1, today.dayOfMonth) // add year, month (Jan), date

//    var selectedDate = remember { mutableStateOf(calendar.timeInMillis) }

    val cgDate = endDate.let {
        val data = LocalDate.parse(it)
        calendar.set(data.year, data.monthValue - 1, data.dayOfMonth)
        val cgData = calendar
        cgData
    }



    // set the initial date
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = cgDate.timeInMillis,
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                calendar.set(year, month - 1, day)

                val calendar1 = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
                calendar1.timeInMillis = utcTimeMillis

                return utcTimeMillis >= calendar.timeInMillis
                        && (calendar1[Calendar.DAY_OF_WEEK] != Calendar.MONDAY)
                        && (calendar1[Calendar.DAY_OF_WEEK] != Calendar.TUESDAY)
                        && (calendar1[Calendar.DAY_OF_WEEK] != Calendar.WEDNESDAY)
                        && (calendar1[Calendar.DAY_OF_WEEK] != Calendar.THURSDAY)
                        && (calendar1[Calendar.DAY_OF_WEEK] != Calendar.FRIDAY)
            }
        }
    )

    if (selectedEndDate) {
        DatePickerDialog(
            onDismissRequest = { },
            confirmButton = {
                TextButton(onClick = {
                    onDateSelected!!.invoke(datePickerState.selectedDateMillis!!)
                }) {
                    Text(text = "선택")
                }
            },
            dismissButton = {
                TextButton(onClick = onDismissRequest) {
                    Text(text = "닫기")
                }
            }
        ) {
            DatePicker(
                state = datePickerState
            )
        }
    }
}



