package com.bobodroid.myapplication.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.SnackbarHostState
import androidx.compose.material3.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import com.bobodroid.myapplication.components.*
import com.bobodroid.myapplication.models.viewmodels.DollarViewModel
import com.bobodroid.myapplication.ui.theme.TopButtonColor
import com.bobodroid.myapplication.lists.BuyRecordBox
import com.bobodroid.myapplication.lists.SellRecordBox
import com.bobodroid.myapplication.models.viewmodels.AllViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalUnitApi::class, ExperimentalMaterialApi::class)
@Composable
fun DollarMainScreen
            (dollarViewModel: DollarViewModel,
             allViewModel: AllViewModel) {


    var selectedCheckBoxId = dollarViewModel.selectedCheckBoxId.collectAsState()

    val resentExchangeRate = allViewModel.recentExChangeRateFlow.collectAsState()

    val reFreshDate = dollarViewModel.refreshDateFlow.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    val scope = rememberCoroutineScope()


    Column(modifier = Modifier
        .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally)
    {

        Column(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .height(100.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Row(modifier = Modifier
                .weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center){
                //업데이트 날짜 값
                Column(modifier = Modifier
                    .wrapContentHeight()
                    .weight(0.6f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center) {
                    Row(modifier = Modifier
                        .wrapContentSize(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically) {

                        Text(text = "USD: ${resentExchangeRate.value.exchangeRates?.usd}", fontSize = 20.sp)
                        Spacer(modifier = Modifier.width(10.dp))
                    }
                    Spacer(modifier = Modifier.height(15.dp))
                    Text(text = "업데이트된 환율: ${resentExchangeRate.value.createAt}")
                }

            }

        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 25.dp)
                .padding(start = 10.dp),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically,
        ) {

            Text(
                modifier = Modifier.padding(start = 10.dp),
                text = "새로고침 시간: ${reFreshDate.value}",
                textAlign = TextAlign.Center)

            Spacer(modifier = Modifier.weight(1f))

            InvestCheckBox(title = "매수",
                1, selectedCheckId = selectedCheckBoxId.value,
                selectCheckBoxAction = {
                    dollarViewModel.selectedCheckBoxId.value = it
                })

            InvestCheckBox(title = "매도",
                2, selectedCheckId = selectedCheckBoxId.value,
                selectCheckBoxAction = {
                    dollarViewModel.selectedCheckBoxId.value = it
                }
            )
        }

        Column(
            horizontalAlignment = Alignment.End
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
            ) {
                if (selectedCheckBoxId.value == 1)
                {
                    BuyRecordBox(dollarViewModel = dollarViewModel, snackBarHostState = snackbarHostState)
                } else {
                    SellRecordBox(dollarViewModel = dollarViewModel)}
            }

        }

    }
}







//@OptIn(ExperimentalMaterialApi::class, ExperimentalMaterial3Api::class)
//@Composable
//fun GetMoneyView(title: String,
//                 getMoney: String,
//                 onClicked: () -> Unit,
//                 dollarViewModel: DollarViewModel
//) {
//
//    val isFirstDialogOpen = remember { mutableStateOf(false) }
//
//    val isSecondDialogOpen = remember { mutableStateOf(false) }
//
//
//    val time = Calendar.getInstance().time
//
//    val formatter = SimpleDateFormat("yyyy-MM-dd")
//
//    val today = formatter.format(time)
//
//    val callFirstDate = dollarViewModel.sellStartDateFlow.collectAsState()
//
//    var callsecondDate = dollarViewModel.sellEndDateFlow.collectAsState()
//
//    var firstDate = if(today == "") "$today" else {callFirstDate.value}
//
//    var secondDate = if(today == "") "$today" else {callsecondDate.value}
//
//
//
//    androidx.compose.material
//        .Card(
//            modifier = Modifier
//                .fillMaxWidth()
//                .height(85.dp),
//            onClick = onClicked
//        ) {
//
//
////            androidx.compose.material3
////            DatePicker
//            Row {
//                Column(
//                    modifier = Modifier
//                        .padding(top = 8.dp)) {
//
//                    Card(
//                        modifier = Modifier
//                            .width(150.dp)
//                            .height(30.dp)
//                            .background(Color.White),
//                        border = BorderStroke(1.dp, Color.Black),
//                        colors = CardDefaults.cardColors(
//                            contentColor = Color.Black,
//                            containerColor = Color.White),
//                        onClick = { isFirstDialogOpen.value = !isFirstDialogOpen.value
//
//                        }) {
//                        Text(text = "시작: $firstDate", color = Color.Black, fontSize = 14.sp , textAlign = TextAlign.Center, modifier = Modifier
//                            .width(160.dp)
//                            .height(30.dp)
//                            .padding(start = 0.dp, top = 4.dp))
//
//                        if(isFirstDialogOpen.value) {
//                            SellFirstDatePickerDialog(onDateSelected = null,
//                                onDismissRequest = {
//                                    isFirstDialogOpen.value = false
//                                }, id = 1,
//                                dollarViewModel
//                            )
//                        }
//                    }
//                    Spacer(modifier = Modifier.height(10.dp))
//
//                    Card(
//                        modifier = Modifier
//                            .width(150.dp)
//                            .height(30.dp)
//                            .background(Color.White),
//                        border = BorderStroke(1.dp, Color.Black),
//                        colors = CardDefaults.cardColors(contentColor = Color.Black, containerColor = Color.White),
//                        onClick = { isSecondDialogOpen.value = !isSecondDialogOpen.value
//
//                        }) {
//                        Text(
//                            text = "종료: $secondDate",
//                            color = Color.Black,
//                            fontSize = 14.sp ,
//                            textAlign = TextAlign.Center,
//                            modifier = Modifier
//                                .width(160.dp)
//                                .height(30.dp)
//                                .padding(start = 0.dp, top = 4.dp))
//
//                        if(isSecondDialogOpen.value) {
//                            SellEndDatePickerDialog(onDateSelected = null,
//                                onDismissRequest = {
//                                    isSecondDialogOpen.value = false
//                                }, id = 1,
//                                dollarViewModel
//                            )
//                        }
//                    }
//
//                }
//                Column(modifier = Modifier
//                    .fillMaxSize()
//                    .padding(top = 10.dp),
//                    horizontalAlignment = Alignment.CenterHorizontally) {
//                    Text(text = title)
//
//                    Spacer(modifier = Modifier.height(15.dp))
//                    Text(text = getMoney, color = Color.Red)
//                }
//            }
//
//        }
//}




