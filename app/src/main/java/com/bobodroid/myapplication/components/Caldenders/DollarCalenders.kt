package com.bobodroid.myapplication.components

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
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoField

// 달러 탐색 날짜
@Composable
fun MyDatePickerDialog(
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    onDismissRequest: () -> Unit) {

    val selectedDate = remember { mutableStateOf(selectedDate) }
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

            CustomCalendarView(
                selectedDate.value,
                onDateSelected = {
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
                        onDateSelected.invoke(selectedDate.value)
//                        dollarViewModel.dateFlow.value = selectedDate.value.toString()
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
fun CustomCalendarView(
    initialDate: LocalDate? = LocalDate.now(),
    onDateSelected: (LocalDate) -> Unit) {
    AndroidView(
        modifier = Modifier.wrapContentSize(),
        factory = {context ->
            CalendarView(context).apply {
                // 초기 날짜 설정
                val initial = java.util.Calendar.getInstance().apply {
                    initialDate?.let { set(initialDate.year, initialDate.monthValue - 1, initialDate.dayOfMonth) }
                }
                date = initial.timeInMillis
            }

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
