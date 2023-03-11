@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3Api::class)

package com.bobodroid.myapplication.screens

import android.app.DatePickerDialog
import android.content.Context
import android.util.Log
import android.widget.CalendarView
import android.widget.DatePicker
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.navigation.NavHostController
import com.bobodroid.myapplication.components.*
import com.bobodroid.myapplication.models.viewmodels.DollarViewModel
import com.bobodroid.myapplication.models.viewmodels.YenViewModel
import com.bobodroid.myapplication.routes.DollarRoute
import com.bobodroid.myapplication.routes.DollarRouteAction
import com.bobodroid.myapplication.routes.YenRoute
import com.bobodroid.myapplication.routes.YenRouteAction
import com.bobodroid.myapplication.ui.theme.*
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)

@Composable
fun YenInvestScreen(yenViewModel: YenViewModel, routeAction: YenRouteAction) {



    var rateInput = yenViewModel.rateInputFlow.collectAsState()
    var moneyInput = yenViewModel.moneyInputFlow.collectAsState()
    val time = Calendar.getInstance().time
    val formatter = SimpleDateFormat("yyyy-MM-dd")
    val today = formatter.format(time)
    val isBtnActive = rateInput.value.isNotEmpty() && moneyInput.value.isNotEmpty()

    val isDialogOpen = remember { mutableStateOf(false) }

    val dateRecord = yenViewModel.dateFlow.collectAsState()

    var date = if(today == "") "$today" else {dateRecord.value}





    Column(
        modifier = Modifier
            .fillMaxSize()) {

        Spacer(modifier = Modifier
            .fillMaxWidth()
            .height(8.dp))
        Card(
            modifier = Modifier
                .padding(10.dp)
                .height(50.dp)
                .width(80.dp),
            colors = CardDefaults.cardColors(containerColor = Green)
        ) {
            Text(
                text = "엔화",
                fontSize = 20.sp,
                modifier = Modifier
                    .padding(10.dp)
                    .height(50.dp)
                    .width(80.dp),
                textAlign = TextAlign.Center)
        }

        Spacer(modifier = Modifier
            .fillMaxWidth()
            .height(5.dp))


        Card(
            modifier = Modifier
                .width(170.dp)
                .height(40.dp)
                .background(Color.White),
            border = BorderStroke(1.dp, Color.Black),
            colors = CardDefaults.cardColors(contentColor = Color.Black, containerColor = Color.White),
            onClick = { isDialogOpen.value = !isDialogOpen.value

        }) {
            Text(text = "$date",
                color = Color.Black,
                fontSize = 18.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier
                .width(160.dp)
                .height(40.dp)
                .padding(start = 12.dp, top = 8.dp))
        }




        Spacer(modifier = Modifier
            .fillMaxWidth()
            .height(5.dp))

        NumberField("매수금(원)을 입력해주세요", onClicked = {
             yenViewModel.moneyInputFlow.value = it


        })

        Spacer(modifier = Modifier
            .fillMaxWidth()
            .height(10.dp))

        RateNumberField("매수환율을 입력해주세요", onClicked = {
            yenViewModel.rateInputFlow.value = it
        })

        if(isDialogOpen.value) {
            YenMyDatePickerDialog(onDateSelected = null,
                onDismissRequest = {
                isDialogOpen.value = false
            }, id = 1,
                yenViewModel)
        }


        Spacer(modifier = Modifier
            .fillMaxWidth()
            .height(50.dp))

        Row(modifier = Modifier
            .fillMaxWidth()
            .height(70.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center) {
            Buttons("매수",
                enabled = isBtnActive,
                onClicked = {
                    yenViewModel.buyAddRecord()
                    yenViewModel.selectedCheckBoxId.value = 1
                    routeAction.navTo(YenRoute.BUYRECORD)

                }
                , color = BuyColor
                , fontColor = Color.Black
                , modifier = Modifier
                    .height(60.dp)
                    .width(120.dp)
                , fontSize = 25)

            Spacer(modifier = Modifier.width(60.dp))


            Buttons( "닫기", onClicked = {
                yenViewModel.selectedCheckBoxId.value = 1
                routeAction.navTo(YenRoute.BUYRECORD)
            }
                , color = BuyColor
                , fontColor = Color.Black
                , modifier = Modifier
                    .height(60.dp)
                    .width(120.dp)
                , fontSize = 25)


        }

    }
}


@Composable
fun YenMyDatePickerDialog(
    onDateSelected: ((LocalDate, Int) -> Unit)?,
    onDismissRequest: () -> Unit,
    id: Int,
    yenViewModel: YenViewModel) {

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
                text = selectedDate.value.format(DateTimeFormatter.ofPattern("yyyy, MM, dd")),
                modifier = Modifier.padding(10.dp)
            )

            YenCustomCalendarView(onDateSelected = {
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
fun YenCustomCalendarView(onDateSelected: (LocalDate) -> Unit) {
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

















