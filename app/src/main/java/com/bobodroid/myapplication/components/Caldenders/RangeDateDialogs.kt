package com.bobodroid.myapplication.components.Caldenders

import android.icu.util.Calendar
import android.icu.util.TimeZone
import android.util.Log
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
import androidx.compose.material3.Text
import androidx.compose.material.TextButton
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.rememberDatePickerState
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
import com.bobodroid.myapplication.MainActivity.Companion.TAG
import com.bobodroid.myapplication.components.CardButton
import com.bobodroid.myapplication.ui.theme.DialogBackgroundColor
import com.bobodroid.myapplication.ui.theme.TopButtonColor
import com.bobodroid.myapplication.ui.theme.WelcomeScreenBackgroundColor
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoField
import java.util.GregorianCalendar

@Composable
fun RangeDateDialog(
    onDismissRequest: (Boolean) -> Unit,
    callStartDate: String,
    callEndDate: String,
    startDateSelected: ((String) -> Unit)? = null,
    endDateSelected: ((String) -> Unit)? = null,
    onClicked: ((selectedStartDate: String, selectedEndDate: String) -> Unit)? = null,
) {

    val datePickerEnableState = remember { mutableStateOf(false) }

    val time = java.util.Calendar.getInstance().time

    val formatter = SimpleDateFormat("yyyy-MM-dd")

    val today = formatter.format(time)

    val selectedStartDate = remember { mutableStateOf("${LocalDate.now()}") }

    val selectedEndDate = remember { mutableStateOf("${LocalDate.now()}") }

    val isStartDateOpen = remember { mutableStateOf(false) }

    val isEndDateOpen = remember { mutableStateOf(false) }

    val dateCardLabel = remember { mutableStateOf("오늘") }

    fun dateWeek(week: Int): String? {
        val c: java.util.Calendar = GregorianCalendar()
        c.add(java.util.Calendar.DAY_OF_WEEK, -week)
        val sdfr = SimpleDateFormat("yyyy-MM-dd")
        return sdfr.format(c.time).toString()
    }

    fun dateMonth(month: Int): String? {
        val c: java.util.Calendar = GregorianCalendar()
        c.add(java.util.Calendar.MONTH, -month)
        val sdfr = SimpleDateFormat("yyyy-MM-dd")
        return sdfr.format(c.time).toString()
    }


    val oneWeek = dateWeek(7)

    val oneMonth = dateMonth(1)

    val scope = rememberCoroutineScope()





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

                Text(text = "매수내역 조회 설정", fontSize = 25.sp)
            }
            Divider(Modifier.fillMaxWidth())

            Row(
                modifier = Modifier
                    .wrapContentHeight()
                    .fillMaxWidth()
                    .padding(start = 10.dp),
                horizontalArrangement = Arrangement.Start
            ) {

                Text(text = "조회기간", fontSize = 15.sp)
            }

            Row(
                modifier = Modifier
                    .height(50.dp)
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp)
                    .padding(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {

                CardButton(
                    label = "오늘",
                    selectedLabel = dateCardLabel.value,
                    onClicked = {

                        selectedStartDate.value = "${LocalDate.now()}"

                        selectedEndDate.value = "${LocalDate.now()}"

                        datePickerEnableState.value = false
                        dateCardLabel.value = it
                        scope.launch {
//                            allViewModel.dateStringFlow.emit(it)
                        }
                    },
                    fontSize = 15,
                    modifier = Modifier.weight(1f),
                    fontColor = Color.Black,
                    buttonColor = Color.White,
                    disableColor = Color.LightGray
                )

                CardButton(
                    label = "한달",
                    selectedLabel = dateCardLabel.value,
                    onClicked = {

                        selectedStartDate.value = "${oneMonth}"

                        selectedEndDate.value = "${LocalDate.now()}"

                        datePickerEnableState.value = false
                        dateCardLabel.value = it
                        scope.launch {
//                            allViewModel.dateStringFlow.emit(it)
                        }
                    },
                    fontSize = 15,
                    modifier = Modifier.weight(1f),
                    fontColor = Color.Black,
                    buttonColor = Color.White,
                    disableColor = Color.LightGray
                )

                CardButton(
                    label = "모두",
                    selectedLabel = dateCardLabel.value,
                    onClicked = {

                        selectedStartDate.value = "1970-01-01"

                        selectedEndDate.value = "${LocalDate.now()}"

                        datePickerEnableState.value = false
                        dateCardLabel.value = it

                        scope.launch {
//                            allViewModel.dateStringFlow.emit(it)
                        }
                    },
                    fontSize = 15,
                    modifier = Modifier.weight(1f),
                    fontColor = Color.Black,
                    buttonColor = Color.White,
                    disableColor = Color.LightGray
                )


                CardButton(
                    label = "직접 설정",
                    selectedLabel = dateCardLabel.value,
                    onClicked = {
                        datePickerEnableState.value = true
                        dateCardLabel.value = it
                        scope.launch {
//                            allViewModel.dateStringFlow.emit(it)
                        }
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

                OutlinedButton(
                    modifier = Modifier
                        .weight(1f),
                    border = BorderStroke(1.dp, color = Color.Black),
                    shape = RoundedCornerShape(5.dp),
                    enabled = datePickerEnableState.value,
                    colors = ButtonDefaults.buttonColors(
                        contentColor = Color.Black,
                        disabledContainerColor = Color.LightGray,
                        containerColor = Color.White
                    ),
                    onClick = {
                        isStartDateOpen.value = true
                    }) {
                    Text(
                        text = "${selectedStartDate.value}",
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center,
                    )
                }

                Text(text = "~")

                OutlinedButton(
                    modifier = Modifier.weight(1f),
                    border = BorderStroke(1.dp, color = Color.Black),
                    enabled = datePickerEnableState.value,
                    colors = ButtonDefaults.buttonColors(
                        contentColor = Color.Black,
                        disabledContainerColor = Color.LightGray,
                        containerColor = Color.White
                    ),
                    shape = RoundedCornerShape(5.dp),
                    onClick = {
                        isEndDateOpen.value = true
                    }) {
                    Text(
                        text = "${selectedEndDate.value}",
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center,
                    )


                }

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
                        onClicked?.invoke(selectedStartDate.value, selectedEndDate.value)
                        Log.d(TAG("RangeDateDialog",""), "조회날짜 ${selectedStartDate.value} ${selectedEndDate.value}")
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




        if (isStartDateOpen.value) {
            startDatePickerDialog(
                selectedStartDate = isStartDateOpen.value,
                onDateSelected = { startDate ->

                    selectedStartDate.value = Instant.ofEpochMilli(startDate).atZone(
                        ZoneId.systemDefault()
                    ).toLocalDate().toString()

                    startDateSelected?.invoke(selectedStartDate.value)

                    isStartDateOpen.value = false
                },
                onDismissRequest = {
                    isStartDateOpen.value = false
                },
                startDate = if (callStartDate == "") "${LocalDate.now()}" else {
                    callStartDate
                },
                endDate = if (callEndDate == "") "${LocalDate.now()}" else {
                    callEndDate
                }
            )
        }





        if (isEndDateOpen.value) {
            endDatePickerDialog(
                selectedEndDate = isEndDateOpen.value,
                onDateSelected = { endDate ->

                    selectedEndDate.value = Instant.ofEpochMilli(endDate).atZone(
                        ZoneId.systemDefault()
                    ).toLocalDate().toString()

                    endDateSelected?.invoke(
                        Instant.ofEpochMilli(endDate).atZone(
                            ZoneId.systemDefault()
                        ).toLocalDate().toString()
                    )

                    isEndDateOpen.value = false
                },
                onDismissRequest = {
                    isEndDateOpen.value = false
                },
                startDate = if (callStartDate == "") "${LocalDate.now()}" else {
                    callStartDate
                },
                endDate = if (callEndDate == "") "${LocalDate.now()}" else {
                    callEndDate
                }
            )
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
    startDate: String
) {


    val Cgdate: LocalDate = LocalDate.parse(endDate, DateTimeFormatter.ISO_DATE)

    val year: Int = Cgdate.get(ChronoField.YEAR)
    val month: Int = Cgdate.get(ChronoField.MONTH_OF_YEAR)
    val day: Int = Cgdate.get(ChronoField.DAY_OF_MONTH)

    val calendar = Calendar.getInstance()

    val today = LocalDate.now()
//    val today = calendar.get(Calendar.DATE)
    calendar.set(today.year, today.monthValue, today.dayOfMonth) // add year, month (Jan), date

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



