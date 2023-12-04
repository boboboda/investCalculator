@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3Api::class)

package com.bobodroid.myapplication.screens

import android.util.Log
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bobodroid.myapplication.components.*
import com.bobodroid.myapplication.models.viewmodels.DollarViewModel
import com.bobodroid.myapplication.routes.DollarRoute
import com.bobodroid.myapplication.routes.DollarRouteAction
import com.bobodroid.myapplication.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.material.SnackbarDuration
import androidx.compose.material.SnackbarHost
import androidx.compose.material.SnackbarHostState

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)

@Composable
fun DollarInvestScreen(dollarViewModel: DollarViewModel,
                       routeAction: DollarRouteAction) {



    var rateInput = dollarViewModel.rateInputFlow.collectAsState()
    var moneyInput = dollarViewModel.moneyInputFlow.collectAsState()
    val time = Calendar.getInstance().time
    val formatter = SimpleDateFormat("yyyy-MM-dd")
    val today = formatter.format(time)
    val isBtnActive = rateInput.value.isNotEmpty() && moneyInput.value.isNotEmpty()

    val isDialogOpen = remember { mutableStateOf(false) }

    val dateRecord = dollarViewModel.dateFlow.collectAsState()

    var date = if(today == "") "$today" else {dateRecord.value}

    val snackBarHostState = remember { SnackbarHostState() }




    Box(
        Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
        ) {

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
                    text = "달러",
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
                dollarViewModel.moneyInputFlow.value = it
                Log.d(TAG, "$it")
            },
               snackBarHostState )

            Spacer(modifier = Modifier
                .fillMaxWidth()
                .height(10.dp))

            RateNumberField("매수환율을 입력해주세요", onClicked = {
                dollarViewModel.rateInputFlow.value = it
            })

            if(isDialogOpen.value) {
                MyDatePickerDialog(onDateSelected = null,
                    onDismissRequest = {
                        isDialogOpen.value = false
                    }, id = 1,
                    dollarViewModel)
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
                        dollarViewModel.buyDollarAdd()
                        dollarViewModel.selectedCheckBoxId.value = 1
                        routeAction.navTo(DollarRoute.BUYRECORD)
                    }
                    , color = BuyColor
                    , fontColor = Color.Black
                    , modifier = Modifier
                        .height(60.dp)
                        .width(120.dp)
                    , fontSize = 25)

                Spacer(modifier = Modifier.width(60.dp))


                Buttons( "닫기",
                    onClicked = {
                        dollarViewModel.selectedCheckBoxId.value = 1
                        routeAction.navTo(DollarRoute.BUYRECORD) },
                    color = BuyColor,
                    fontColor = Color.Black,
                    modifier = Modifier
                        .height(60.dp)
                        .width(120.dp),
                    fontSize = 25)


            }

        }
}
}




















