@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3Api::class)

package com.bobodroid.myapplication.screens

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.content.Context
import android.util.Log
import android.widget.CalendarView
import android.widget.DatePicker
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
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
import com.bobodroid.myapplication.components.Caldenders.WonMyDatePickerDialog
import com.bobodroid.myapplication.models.viewmodels.DollarViewModel
import com.bobodroid.myapplication.models.viewmodels.WonViewModel
import com.bobodroid.myapplication.models.viewmodels.YenViewModel
import com.bobodroid.myapplication.routes.*
import com.bobodroid.myapplication.ui.theme.*
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

@SuppressLint("RememberReturnType")
@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)

@Composable
fun WonInvestScreen(wonViewModel: WonViewModel, routeAction: WonRouteAction) {



    var rateInput = wonViewModel.rateInputFlow.collectAsState()
    var moneyInput = wonViewModel.moneyInputFlow.collectAsState()
    val time = Calendar.getInstance().time
    val formatter = SimpleDateFormat("yyyy-MM-dd")
    val today = formatter.format(time)
    val isBtnActive = rateInput.value.isNotEmpty() && moneyInput.value.isNotEmpty()

    val isDialogOpen = remember { mutableStateOf(false) }

    val dateRecord = wonViewModel.dateFlow.collectAsState()

    var date = if(today == "") "$today" else {dateRecord.value}

    val moneyCgBtnSelected = wonViewModel.moneyCgBtnSelected.collectAsState()


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
                text = "원화",
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

        Row(
            modifier = Modifier.width(180.dp),
            horizontalArrangement = Arrangement.Center) {
            MoneyChButtonView(
                mainText = "달러",
                id = 1,
                selectedId = moneyCgBtnSelected.value ,
                selectAction = {wonViewModel.moneyCgBtnSelected.value = it})

            Spacer(modifier = Modifier.width(20.dp))

            MoneyChButtonView(
                mainText = "엔화",
                id = 2,
                selectedId = moneyCgBtnSelected.value ,
                selectAction = {wonViewModel.moneyCgBtnSelected.value = it})
        }

        Spacer(modifier = Modifier
            .fillMaxWidth()
            .height(5.dp))


        WonNumberField("매수금(엔화,달러)을 입력해주세요", onClicked = {

             wonViewModel.moneyInputFlow.value = it


        },wonViewModel)

        Spacer(modifier = Modifier
            .fillMaxWidth()
            .height(10.dp))

        RateNumberField("매수환율을 입력해주세요", onClicked = {
            wonViewModel.rateInputFlow.value = it
        })

        if(isDialogOpen.value) {
            WonMyDatePickerDialog(onDateSelected = null,
                onDismissRequest = {
                isDialogOpen.value = false
            }, id = 1,
                wonViewModel)
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
                    wonViewModel.buyAddRecord()
                    wonViewModel.selectedCheckBoxId.value = 1
                    routeAction.navTo(WonRoute.BUYRECORD)
                    Log.d(TAG, "${wonViewModel.exchangeMoney.value}")

                }
                , color = BuyColor
                , fontColor = Color.Black
                , modifier = Modifier
                    .height(60.dp)
                    .width(120.dp)
                , fontSize = 25)

            Spacer(modifier = Modifier.width(60.dp))


            Buttons( "닫기", onClicked = {
                wonViewModel.selectedCheckBoxId.value = 1
                routeAction.navTo(WonRoute.BUYRECORD)
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




















