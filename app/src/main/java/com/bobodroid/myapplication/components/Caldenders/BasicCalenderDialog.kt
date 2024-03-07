package com.bobodroid.myapplication.components.Caldenders

import android.util.Log
import android.widget.CalendarView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Calendar

@Composable
fun BasicCalenderDialog(
    initialDate: String? = LocalDate.now().toString(),
    onDateSelected: ((LocalDate) -> Unit)?,
    onDismissRequest: () -> Unit,
) {

    val selectedDate = remember { mutableStateOf(LocalDate.now()) }

    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    val localDate = LocalDate.parse(initialDate, formatter)


    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties()) {
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

            BasicCalendarView(
                initialDate = localDate,
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
                        onDateSelected?.invoke(selectedDate.value)
                    }
                ) {
                    Text(text = "선택")
                }
            }

        }

    }

}


@Composable
fun BasicCalendarView(
    initialDate: LocalDate? = LocalDate.now(),
    onDateSelected: (LocalDate) -> Unit) {
    AndroidView(
        modifier = Modifier.wrapContentSize(),
        factory = {context ->
            CalendarView(context).apply {
                // 초기 날짜 설정
                val initial = Calendar.getInstance().apply {
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