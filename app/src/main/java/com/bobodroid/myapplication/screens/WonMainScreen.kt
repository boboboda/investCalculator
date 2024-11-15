package com.bobodroid.myapplication.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material.Card
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bobodroid.myapplication.components.*
import com.bobodroid.myapplication.models.viewmodels.DollarViewModel
import com.bobodroid.myapplication.ui.theme.*
import java.util.*
import androidx.compose.material.SnackbarHostState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.ExperimentalUnitApi
import com.bobodroid.myapplication.R
import com.bobodroid.myapplication.lists.dollorList.SellRecordBox
import com.bobodroid.myapplication.lists.dollorList.TotalDrRecordBox
import com.bobodroid.myapplication.lists.yenList.SellYenRecordBox
import com.bobodroid.myapplication.models.viewmodels.AllViewModel
import com.bobodroid.myapplication.models.viewmodels.YenViewModel
import com.bobodroid.myapplication.routes.InvestRouteAction
import java.text.SimpleDateFormat
import androidx.compose.material.SnackbarHost
import com.bobodroid.myapplication.components.admobs.BannerAd
import com.bobodroid.myapplication.lists.wonList.SellWonRecordBox
import com.bobodroid.myapplication.lists.wonList.TotalWonRecordBox
import com.bobodroid.myapplication.lists.yenList.TotalYenRecordBox
import com.bobodroid.myapplication.models.viewmodels.WonViewModel


@OptIn(ExperimentalMaterial3Api::class, ExperimentalUnitApi::class, ExperimentalMaterialApi::class)
@Composable
fun WonMainScreen(wonViewModel: WonViewModel,
                  allViewModel: AllViewModel) {


    var selectedBoxId = wonViewModel.selectedBoxId.collectAsState()

    var hideSellRecordState by remember { mutableStateOf(false) }

    val visibleIcon = if (hideSellRecordState)  R.drawable.ic_visible  else  R.drawable.ic_invisible

    var dropdownExpanded by remember { mutableStateOf(false) }

    val reFreshDate = allViewModel.refreshDateFlow.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    val bannerState = allViewModel.deleteBannerStateFlow.collectAsState()

    Column(modifier = Modifier
        .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 30.dp)
                .padding(bottom = 5.dp),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically,
        ) {


            Spacer(modifier = Modifier.weight(1f))

            Card(
                colors = CardDefaults.cardColors(WelcomeScreenBackgroundColor),
                elevation = CardDefaults.cardElevation(8.dp),
                shape = RoundedCornerShape(1.dp),
                modifier = Modifier
                    .height(40.dp)
                    .wrapContentWidth(),
                onClick = {
                    if (!hideSellRecordState) hideSellRecordState = true else hideSellRecordState = false
                }) {
                Row(
                    modifier = Modifier
                        .fillMaxHeight()
                        .wrapContentWidth()
                        .padding(horizontal = 10.dp)
                        .padding(end = 10.dp)
                        .padding(bottom = 5.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "매도기록")

                    Image(
                        painter = painterResource(id = visibleIcon),
                        contentDescription = "매도기록 노출 여부"
                    )
                }
            }

        }

        if(!bannerState.value) {
            Column(
                modifier = Modifier
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                BannerAd()

            }
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


        Box(
            modifier = Modifier
                .fillMaxSize(),
            contentAlignment = Alignment.BottomCenter) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()) {

                    when (selectedBoxId.value) {
                        1 -> {
                            TotalWonRecordBox(
                                wonViewModel,
                                snackbarHostState,
                                hideSellRecordState)
                        }

                        2 -> {
                            SellWonRecordBox(
                                wonViewModel,
                                snackbarHostState)
                        }
                    }
                }
            }

            //snackBar
            Row(modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
            ) {
                SnackbarHost(
                    hostState = snackbarHostState, modifier = Modifier,
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
                                            snackbarHostState.currentSnackbarData?.dismiss()
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