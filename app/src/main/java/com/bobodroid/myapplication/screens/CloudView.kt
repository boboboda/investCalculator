package com.bobodroid.myapplication.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.SnackbarDuration
import androidx.compose.material.SnackbarHost
import androidx.compose.material.SnackbarHostState
import androidx.compose.material.TextButton
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bobodroid.myapplication.components.Buttons
import com.bobodroid.myapplication.components.admobs.showRewardedAdvertisement
import com.bobodroid.myapplication.components.shadowCustom
import com.bobodroid.myapplication.models.datamodels.roomDb.LocalUserData
import com.bobodroid.myapplication.routes.MyPageRoute
import com.bobodroid.myapplication.routes.RouteAction
import kotlinx.coroutines.launch

@Composable
fun CloudView(routeAction: RouteAction<MyPageRoute>,
                localUser: LocalUserData) {

    val context = LocalContext.current

    val coroutineScope = rememberCoroutineScope()

    val cloudScreenSnackBarHostState = remember { SnackbarHostState() }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {

            TextButton(onClick = {
                routeAction.goBack()
            }) {
                Text(text = "뒤로", color = Color.Black, fontWeight = FontWeight.Bold)
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 15.dp)
            ) {
                Buttons(
                    onClicked = {

                        if (localUser.email.isNullOrEmpty()) {
                            coroutineScope.launch {

                                cloudScreenSnackBarHostState.showSnackbar(
                                    "연동 후 진행해 주세요",
                                    actionLabel = "닫기", SnackbarDuration.Short
                                )
                            }
                        } else {
                            showRewardedAdvertisement(context, onAdDismissed = {
//                                allViewModel.cloudSave(
//                                    drBuyRecord = dollarViewModel.buyRecordFlow.value,
//                                    drSellRecord = dollarViewModel.sellRecordFlow.value,
//                                    yenBuyRecord = yenViewModel.buyRecordFlow.value,
//                                    yenSellRecord = yenViewModel.sellRecordFlow.value,
//                                    wonBuyRecord = wonViewModel.buyRecordFlow.value,
//                                    wonSellRecord = wonViewModel.sellRecordFlow.value
//                                )
//                                coroutineScope.launch {
//                                    cloudScreenSnackBarHostState.showSnackbar(
//                                        "클라우드에 저장되었습니다.",
//                                        actionLabel = "닫기", SnackbarDuration.Short
//                                    )
//                                }
                            })
                        }

                    },
                    color = Color.White,
                    fontColor = Color.Black,
                    modifier = Modifier
                        .height(55.dp)
                        .fillMaxWidth()
                        .padding(top = 10.dp)
                        .shadowCustom(
                            color = Color.LightGray,
                            offsetX = 5.dp,
                            offsetY = 5.dp,
                            blurRadius = 10.dp
                        )
                ) {
                    Text(
                        text = "클라우드 저장",
                        fontSize = 15.sp,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(0.dp))
                }



            }


            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 15.dp)
            ) {
                Buttons(
                    onClicked = {

                        if (localUser.email.isNullOrEmpty()) {
                            coroutineScope.launch {
                                cloudScreenSnackBarHostState.showSnackbar(
                                    "연동 후 진행해 주세요",
                                    actionLabel = "닫기", SnackbarDuration.Short
                                )
                            }
                        } else {

//                            allViewModel.cloudLoad(localUser.value.customId!!) { cloudData, resultMessage ->
//
//                                dollarViewModel.drCloudLoad(
//                                    cloudData.drBuyRecord ?: emptyList(),
//                                    cloudData.drSellRecord ?: emptyList()
//                                )
//
//                                yenViewModel.yenCloudLoad(
//                                    cloudData.yenBuyRecord ?: emptyList(),
//                                    cloudData.yenSellRecord ?: emptyList()
//                                )
//
//                                wonViewModel.wonCloudLoad(
//                                    cloudData.wonBuyRecord ?: emptyList(),
//                                    cloudData.wonSellRecord ?: emptyList()
//                                )
//
//
//                                coroutineScope.launch {
//                                    cloudScreenSnackBarHostState.showSnackbar(
//                                        resultMessage,
//                                        actionLabel = "닫기", SnackbarDuration.Short
//                                    )
//                                }
//
//                        }
                        }
                                },
                    color = Color.White,
                    fontColor = Color.Black,
                    modifier = Modifier
                        .height(55.dp)
                        .fillMaxWidth()
                        .padding(top = 10.dp)
                        .shadowCustom(
                            color = Color.LightGray,
                            offsetX = 5.dp,
                            offsetY = 5.dp,
                            blurRadius = 10.dp
                        )
                ) {
                    Text(
                        text = "클라우드 불러오기",
                        fontSize = 15.sp,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(0.dp))
                }



            }
        }


        //snackBar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
        ) {
            SnackbarHost(
                hostState = cloudScreenSnackBarHostState, modifier = Modifier,
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
                                        cloudScreenSnackBarHostState.currentSnackbarData?.dismiss()
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