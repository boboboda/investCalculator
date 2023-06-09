package com.bobodroid.myapplication.screens

import android.annotation.SuppressLint
import android.util.Log
import androidx.activity.viewModels
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.TextFieldColors
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.*
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bobodroid.myapplication.MainActivity
import com.bobodroid.myapplication.R
import com.bobodroid.myapplication.components.*
import com.bobodroid.myapplication.extensions.toWon
import com.bobodroid.myapplication.models.viewmodels.DollarViewModel
import com.bobodroid.myapplication.models.viewmodels.SharedViewModel
import com.bobodroid.myapplication.routes.DollarRoute
import com.bobodroid.myapplication.routes.DollarRouteAction
import com.bobodroid.myapplication.ui.theme.InverstCalculatorTheme
import com.bobodroid.myapplication.ui.theme.TopButtonColor
import com.bobodroid.myapplication.ui.theme.TopButtonInColor
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.absoluteValue

const val TAG = "메인"



@OptIn(ExperimentalMaterial3Api::class, ExperimentalUnitApi::class, ExperimentalMaterialApi::class)
@Composable
fun DollarMainScreen
            (dollarViewModel: DollarViewModel,
             routeAction: DollarRouteAction,
             sharedViewModel: SharedViewModel) {


    var selectedCheckBoxId = dollarViewModel.selectedCheckBoxId.collectAsState()

    val isDialogOpen = remember { mutableStateOf(false) }

    val time = Calendar.getInstance().time

    val formatter = SimpleDateFormat("yyyy-MM-dd")

    val today = formatter.format(time)


    val selectedDate: MutableState<LocalDate?> = remember { mutableStateOf(LocalDate.now()) }

    val dateRecord = dollarViewModel.dateFlow.collectAsState()

    var date = if (today == "") "$today" else {
        dateRecord.value
    }

    val dateSelected = dollarViewModel.changeDateAction.collectAsState()

    val total = dollarViewModel.total.collectAsState("")


    Column(modifier = Modifier.fillMaxSize())
    {

        Row(modifier = Modifier.fillMaxWidth()) {
            TopTitleButton(sharedViewModel)
        }

        Row(
            modifier = Modifier
                .background(Color.White)
                .fillMaxWidth()
                .height(120.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {

            Column(
                modifier = Modifier
                    .width(250.dp)
                    .height(110.dp)
                    .background(Color.White),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {


                Card(
                    modifier = Modifier
                        .width(200.dp)
                        .height(40.dp)
                        .background(Color.White),
                    border = BorderStroke(1.dp, Color.Black),
                    colors = CardDefaults.cardColors(
                        contentColor = Color.Black,
                        containerColor = Color.White
                    ),
                    onClick = {
                        isDialogOpen.value = !isDialogOpen.value

                    }) {

                    Text(
                        text = "$date",
                        color = Color.Black,
                        fontSize = 18.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .width(160.dp)
                            .height(40.dp)
                            .padding(start = 35.dp, top = 8.dp))

                    if (isDialogOpen.value) {
                        MyDatePickerDialog(onDateSelected = { date, seleceted ->
                            selectedDate.value = date
                            dollarViewModel.dateFlow.value = date.toString()
                            dollarViewModel.changeDateAction.value = seleceted!!
                        }, onDismissRequest = {
                            isDialogOpen.value = false
                        }, id = 1,
                            dollarViewModel
                        )
                    }
                }
                Spacer(modifier = Modifier.height(5.dp))

                Row {
                    DateButtonView(
                        mainText = "모두",
                        id = 2,
                        selectedId = dateSelected.value,
                        selectAction = {
                            dollarViewModel.changeDateAction.value = it
                        })

                    Spacer(modifier = Modifier.width(18.dp))

                    DateButtonView(
                        mainText = "한달",
                        id = 3,
                        selectedId = dateSelected.value,
                        selectAction = {
                            dollarViewModel.changeDateAction.value = it
                        })

                    Spacer(modifier = Modifier.width(18.dp))

                    DateButtonView(
                        mainText = "일년",
                        id = 4,
                        selectedId = dateSelected.value,
                        selectAction = {
                            dollarViewModel.changeDateAction.value = it
                        })
                }


            }

            Column(
                modifier = Modifier
                    .width(110.dp)
                    .height(110.dp)
                    .background(Color.White)
            ) {

                Spacer(modifier = Modifier.height(8.dp))
                InverstCheckBox(title = "매수",
                    1, selectedCheckId = selectedCheckBoxId.value,
                    selectCheckBoxAction = {
                        dollarViewModel.selectedCheckBoxId.value = it
                    })

                InverstCheckBox(title = "매도",
                    2, selectedCheckId = selectedCheckBoxId.value,
                    selectCheckBoxAction = {
                        dollarViewModel.selectedCheckBoxId.value = it
                    }
                )
            }
        }
        Column(
            horizontalAlignment = Alignment.End
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
            ) {
                if (selectedCheckBoxId.value == 1)
                    com.bobodroid.myapplication.lists.BuyRecordBox(dollarViewModel = dollarViewModel)
                else com.bobodroid.myapplication.lists.SellRecordBox(dollarViewModel = dollarViewModel)
            }

            Row(
                modifier = Modifier
                    .height(100.dp)
                    .fillMaxWidth()
                    .padding(start = 20.dp, bottom = 10.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically

            ) {

                Row(modifier = Modifier.weight(1f)
                ) {
                    if (selectedCheckBoxId.value == 2) GetMoneyView(
                        title = "총 수익",
                        getMoney = "${total.value}",
                        onClicked = { Log.d(TAG, "") },
                        dollarViewModel
                    )
                    else
                        null
                }

                Spacer(modifier = Modifier.width(30.dp))

                FloatingActionButton(
                    onClick = { routeAction.navTo(DollarRoute.BUY) },
                    containerColor = MaterialTheme.colors.secondary,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .padding(bottom = 10.dp, end = 20.dp)
                        .size(60.dp),
                ) {
                    androidx.compose.material3.Icon(
                        imageVector = Icons.Rounded.Add,
                        contentDescription = "매수화면 가기",
                        tint = Color.White
                    )
                }
            }

        }

    }
}







@OptIn(ExperimentalMaterialApi::class, ExperimentalMaterial3Api::class)
@Composable
fun GetMoneyView(title: String,
                 getMoney: String,
                 onClicked: () -> Unit,
                 dollarViewModel: DollarViewModel
) {

    val isFirstDialogOpen = remember { mutableStateOf(false) }

    val isSecondDialogOpen = remember { mutableStateOf(false) }


    val time = Calendar.getInstance().time

    val formatter = SimpleDateFormat("yyyy-MM-dd")

    val today = formatter.format(time)

    val callFirstDate = dollarViewModel.sellStartDateFlow.collectAsState()

    var callsecondDate = dollarViewModel.sellEndDateFlow.collectAsState()

    var firstDate = if(today == "") "$today" else {callFirstDate.value}

    var secondDate = if(today == "") "$today" else {callsecondDate.value}



    androidx.compose.material
        .Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(85.dp),
        onClick = onClicked
        ) {
        Row {
            Column(
                modifier = Modifier
                    .padding(top = 8.dp)) {

                Card(
                    modifier = Modifier
                        .width(150.dp)
                        .height(30.dp)
                        .background(Color.White),
                    border = BorderStroke(1.dp, Color.Black),
                    colors = CardDefaults.cardColors(
                        contentColor = Color.Black,
                        containerColor = Color.White),
                    onClick = { isFirstDialogOpen.value = !isFirstDialogOpen.value

                    }) {
                    Text(text = "시작: $firstDate", color = Color.Black, fontSize = 14.sp , textAlign = TextAlign.Center, modifier = Modifier
                        .width(160.dp)
                        .height(30.dp)
                        .padding(start = 0.dp, top = 4.dp))

                    if(isFirstDialogOpen.value) {
                        SellFirstDatePickerDialog(onDateSelected = null,
                            onDismissRequest = {
                                isFirstDialogOpen.value = false
                        }, id = 1,
                            dollarViewModel
                        )
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))

                Card(
                    modifier = Modifier
                        .width(150.dp)
                        .height(30.dp)
                        .background(Color.White),
                    border = BorderStroke(1.dp, Color.Black),
                    colors = CardDefaults.cardColors(contentColor = Color.Black, containerColor = Color.White),
                    onClick = { isSecondDialogOpen.value = !isSecondDialogOpen.value

                    }) {
                    Text(
                        text = "종료: $secondDate",
                        color = Color.Black,
                        fontSize = 14.sp ,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .width(160.dp)
                            .height(30.dp)
                            .padding(start = 0.dp, top = 4.dp))

                    if(isSecondDialogOpen.value) {
                        SellEndDatePickerDialog(onDateSelected = null,
                            onDismissRequest = {
                                isSecondDialogOpen.value = false
                        }, id = 1,
                            dollarViewModel
                        )
                    }
                }

            }
            Column(modifier = Modifier
                .fillMaxSize()
                .padding(top = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = title)
                
                Spacer(modifier = Modifier.height(15.dp))
                Text(text = getMoney, color = Color.Red)
            }
        }

    }
}




