package com.bobodroid.myapplication.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.SnackbarHostState
import androidx.compose.material3.*
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import com.bobodroid.myapplication.components.*
import com.bobodroid.myapplication.models.viewmodels.DollarViewModel
import com.bobodroid.myapplication.ui.theme.TopButtonColor
import com.bobodroid.myapplication.lists.BuyRecordBox
import com.bobodroid.myapplication.lists.SellRecordBox
import com.bobodroid.myapplication.lists.addFocusCleaner
import com.bobodroid.myapplication.models.viewmodels.AllViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalUnitApi::class, ExperimentalMaterialApi::class)
@Composable
fun DollarMainScreen
            (dollarViewModel: DollarViewModel,
             allViewModel: AllViewModel) {


    var selectedCheckBoxId = dollarViewModel.selectedCheckBoxId.collectAsState()

    val recentExchangeRate = allViewModel.recentExChangeRateFlow.collectAsState()

    val reFreshDate = allViewModel.refreshDateFlow.collectAsState()

    val snackBarHostState = remember { SnackbarHostState() }

    val scope = rememberCoroutineScope()

    val focusManager = LocalFocusManager.current




    Column(modifier = Modifier
        .fillMaxSize()
        .addFocusCleaner(focusManager),
        horizontalAlignment = Alignment.CenterHorizontally)
    {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 25.dp),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically,
        ) {
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

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 25.dp),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                modifier = Modifier.padding(start = 10.dp),
                text = "예상수익 새로고침 시간: ${reFreshDate.value}",
                textAlign = TextAlign.Center)
        }

        Box(modifier = Modifier
            .fillMaxSize(),
            contentAlignment = Alignment.BottomCenter) {
            // Record item
            Column(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                if (selectedCheckBoxId.value == 1)
                {
                    BuyRecordBox(dollarViewModel, snackBarHostState)
                } else {
                    SellRecordBox(dollarViewModel, snackBarHostState)}
            }

            //snackBar
            Row(modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
            ) {
                SnackbarHost(
                    hostState = snackBarHostState, modifier = Modifier,
                    snackbar = { snackBarData ->


                        Card(
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.5.dp, Color.Black),
                            modifier = Modifier
                                .padding(10.dp)
                                .fillMaxWidth(),
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp)
                                    .padding(start = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Start
                            ) {

                                Text(
                                    text = snackBarData.message,
                                    fontSize = 15.sp,
                                    lineHeight = 20.sp,
                                    fontWeight = FontWeight.Medium
                                )

                                Spacer(modifier = Modifier.weight(1f))

                                Text(
                                    modifier = Modifier
                                        .padding(8.dp)
                                        .clickable {
                                            snackBarHostState.currentSnackbarData?.dismiss()
                                        },
                                    text = "닫기",
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    })
            }

        }

    }
}












