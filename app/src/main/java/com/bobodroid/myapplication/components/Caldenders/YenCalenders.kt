package com.bobodroid.myapplication.components.Caldenders

import android.icu.util.Calendar
import android.util.Log
import android.widget.CalendarView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.bobodroid.myapplication.models.viewmodels.DollarViewModel
import com.bobodroid.myapplication.models.viewmodels.YenViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoField

// 달러 탐색 날짜
@Composable
fun YenTopPickerDialog(
    onDateSelected: ((LocalDate, Int) -> Unit)?,
    onDismissRequest: () -> Unit,
    id: Int,
    yenViewModel: YenViewModel
) {

    val selectedDate = remember { mutableStateOf(LocalDate.now()) }

    var currentCardId : Int = id

//    var color = if (selectedId == currentCardId) Color.Gray else Color.LightGray

    Dialog(onDismissRequest = {
        onDismissRequest()
    }, properties = DialogProperties()) {
        Column(
            modifier = Modifier
                .wrapContentSize()
                .background(
                    color = Color.White,
                    shape = RoundedCornerShape(16.dp)
                )
        ) {
            Text(
                text = selectedDate.value.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                modifier = Modifier.padding(10.dp)
            )

            CustomCalendarView(onDateSelected = {
                selectedDate.value = it
            })

            Spacer(modifier = Modifier.size(8.dp))

            Row(
                modifier = Modifier
                    .align(Alignment.End)
                    .padding(bottom = 16.dp, end = 16.dp)
            ) {
                TextButton(onClick = onDismissRequest
                ) {
                    Text(text = "닫기"
                    )
                }

                TextButton(
                    onClick = {
                        yenViewModel.dateFlow.value = selectedDate.value.toString()
                        onDateSelected?.invoke(selectedDate.value, currentCardId)
                        onDismissRequest()
                    }
                ) {
                    Text(text = "선택")
                }
            }

        }

    }

}


@Composable
fun CustomCalendarView(onDateSelected: (LocalDate) -> Unit) {
    AndroidView(
        modifier = Modifier.wrapContentSize(),
        factory = {context ->
            CalendarView(context)
        },
        update = { view ->
            view.setOnDateChangeListener{ _, year, month, dayOfMonth ->
                Log.d("TAG", "CustomCalendarView: onDateSelected : ")
                onDateSelected(
                    LocalDate
                        .now()
                        .withMonth(month + 1)
                        .withYear(year)
                        .withDayOfMonth(dayOfMonth)
                )
            }

        })
}


//매도 날짜 설정
@Composable
fun YenSellDatePickerDialog(
    onDateSelected: ((LocalDate, Int) -> Unit)?,
    onDismissRequest: () -> Unit,
    id: Int,
    yenViewModel: YenViewModel
) {

    val selectedDate = remember { mutableStateOf(LocalDate.now()) }

    var currentCardId : Int = id

//    var color = if (selectedId == currentCardId) Color.Gray else Color.LightGray

    Dialog(onDismissRequest = {
        onDismissRequest()
    }, properties = DialogProperties()) {
        Column(
            modifier = Modifier
                .wrapContentSize()
                .background(
                    color = Color.White,
                    shape = RoundedCornerShape(16.dp)
                )
        ) {
            Text(
                text = selectedDate.value.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                modifier = Modifier.padding(10.dp)
            )

            YenSellCustomCalendarView(onDateSelected = {
                selectedDate.value = it
            })

            Spacer(modifier = Modifier.size(8.dp))

            Row(
                modifier = Modifier
                    .align(Alignment.End)
                    .padding(bottom = 16.dp, end = 16.dp)
            ) {
                TextButton(onClick = onDismissRequest
                ) {
                    Text(text = "닫기"
                    )
                }

                TextButton(
                    onClick = {
                        yenViewModel.sellDateFlow.value = selectedDate.value.toString()
                        onDateSelected?.invoke(selectedDate.value, currentCardId)
                        onDismissRequest()
                    }
                ) {
                    Text(text = "선택")
                }
            }

        }

    }

}


@Composable
fun YenSellCustomCalendarView(onDateSelected: (LocalDate) -> Unit) {

    AndroidView(
        modifier = Modifier.wrapContentSize(),
        factory = { context ->
            CalendarView(context)
        },
        update = { view ->
            view.setOnDateChangeListener{ _, year, month, dayOfMonth ->
                Log.d("TAG", "CustomCalendarView: onDateSelected : ")
                onDateSelected(
                    LocalDate
                        .now()
                        .withMonth(month + 1)
                        .withYear(year)
                        .withDayOfMonth(dayOfMonth)
                )
            }

        })
}




// 달러 수익 범위 설정
@Composable
fun YenSellFirstDatePickerDialog(
    onDateSelected: ((LocalDate) -> Unit)?,
    onDismissRequest: () -> Unit,
    id: Int,
    yenViewModel: YenViewModel
) {

    val selectedDate = remember { mutableStateOf(LocalDate.now()) }

    val selectedMaxDate = remember { mutableStateOf(java.util.Calendar.getInstance()) }

    var currentCardId : Int = id

//    var color = if (selectedId == currentCardId) Color.Gray else Color.LightGray

    Dialog(
        onDismissRequest = {
            onDismissRequest()
        }, properties = DialogProperties()
    ) {
        Column(
            modifier = Modifier
                .wrapContentSize()
                .background(
                    color = Color.White,
                    shape = RoundedCornerShape(16.dp)
                )
        ) {
            Text(
                text = selectedDate.value.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                modifier = Modifier.padding(10.dp)
            )

            FirstCustomCalendarView(
                onDateSelected = {
                    selectedDate.value = it }
                ,yenViewModel)

            Spacer(modifier = Modifier.size(8.dp))

            Row(
                modifier = Modifier
                    .align(Alignment.End)
                    .padding(bottom = 16.dp, end = 16.dp)
            ) {
                TextButton(onClick = onDismissRequest
                ) {
                    Text(text = "닫기"
                    )
                }

                TextButton(
                    onClick = {
                        onDateSelected?.invoke(selectedDate.value)
//                        dollarViewModel.maxDate.value = selectedMaxDate.value.toDate().toString()
//                        Log.d(com.bobodroid.myapplication.screens.TAG, "${selectedMaxDate.value.}")
//                        Log.d(TAG, "${selectedDate.value}")
                        yenViewModel.sellStartDateFlow.value = selectedDate.value.toString()

                        onDismissRequest()
                    }
                ) {
                    Text(text = "선택")
                }
            }

        }

    }

}


@Composable
fun YenSellEndDatePickerDialog(
    onDateSelected: ((LocalDate) -> Unit)?,
    onDismissRequest: () -> Unit,
    id: Int,
    yenViewModel: YenViewModel
) {

    val selectedDate = remember { mutableStateOf(LocalDate.now()) }

    var currentCardId : Int = id

//    var color = if (selectedId == currentCardId) Color.Gray else Color.LightGray

    Dialog(onDismissRequest = {
        onDismissRequest()
    }, properties = DialogProperties()) {
        Column(
            modifier = Modifier
                .wrapContentSize()
                .background(
                    color = Color.White,
                    shape = RoundedCornerShape(16.dp)
                )
        ) {
            Text(
                text = selectedDate.value.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                modifier = Modifier.padding(10.dp)
            )

            SecondCustomCalendarView(onDateSelected = {
                selectedDate.value = it
            }, yenViewModel)

            Spacer(modifier = Modifier.size(8.dp))

            Row(
                modifier = Modifier
                    .align(Alignment.End)
                    .padding(bottom = 16.dp, end = 16.dp)
            ) {
                TextButton(onClick = onDismissRequest
                ) {
                    Text(text = "닫기")
                }

                TextButton(
                    onClick = {
                        onDateSelected?.invoke(selectedDate.value)
                        yenViewModel.sellEndDateFlow.value = selectedDate.value.toString()
                        onDismissRequest()
                    }
                ) {
                    Text(text = "선택")
                }
            }

        }

    }

}


@Composable
fun FirstCustomCalendarView(
    onDateSelected: (LocalDate) -> Unit,
    yenViewModel: YenViewModel
) {

    val date: State<String> = yenViewModel.sellEndDateFlow.collectAsState()

    val Cgdate: LocalDate = LocalDate.parse(date.value, DateTimeFormatter.ISO_DATE)

    val year: Int = Cgdate.get(ChronoField.YEAR)
    val month: Int = Cgdate.get(ChronoField.MONTH_OF_YEAR)
    val day: Int = Cgdate.get(ChronoField.DAY_OF_MONTH)




    AndroidView(
        modifier = Modifier.wrapContentSize(),
        factory = {context ->
            CalendarView(context)
        },
        update = { view ->
            val mCalendar: Calendar = Calendar.getInstance()
            mCalendar.set(year, month-1, day)
            view.maxDate = mCalendar.timeInMillis

            view.setOnDateChangeListener{ _, year, month, dayOfMonth ->
                onDateSelected(
                    LocalDate
                        .now()
                        .withMonth(month + 1)
                        .withYear(year)
                        .withDayOfMonth(dayOfMonth)
                )
            }


        })
}


@Composable
fun SecondCustomCalendarView(
    onDateSelected: (LocalDate) -> Unit,
    yenViewModel: YenViewModel
) {
    val date: State<String> = yenViewModel.sellStartDateFlow.collectAsState()

    val Cgdate: LocalDate = LocalDate.parse(date.value, DateTimeFormatter.ISO_DATE)

    val year: Int = Cgdate.get(ChronoField.YEAR)
    val month: Int = Cgdate.get(ChronoField.MONTH_OF_YEAR)
    val day: Int = Cgdate.get(ChronoField.DAY_OF_MONTH)

    Log.d(com.bobodroid.myapplication.screens.TAG,"${Cgdate}, $year, $month, $day")

    AndroidView(
        modifier = Modifier.wrapContentSize(),
        factory = {context ->
            CalendarView(context)
        },
        update = { view ->
            val mCalendar: Calendar = Calendar.getInstance()
            mCalendar.set(year, month -1, day)
            view.minDate = mCalendar.timeInMillis
            view.setOnDateChangeListener{ _, year, month, dayOfMonth ->
                onDateSelected(
                    LocalDate
                        .now()
                        .withMonth(month + 1)
                        .withYear(year)
                        .withDayOfMonth(dayOfMonth)
                )
            }

        })
}