package com.bobodroid.myapplication.components

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.DrawerState
import androidx.compose.material.SnackbarDuration
import androidx.compose.material.SnackbarHostState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Divider
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.bobodroid.myapplication.components.Dialogs.AskTriggerDialog
import com.bobodroid.myapplication.components.Dialogs.ChargeDialog
import com.bobodroid.myapplication.components.Dialogs.CustomIdDialog
import com.bobodroid.myapplication.components.admobs.showInterstitial
import com.bobodroid.myapplication.components.admobs.showRewardedAdvertisement
import com.bobodroid.myapplication.models.viewmodels.AllViewModel
import com.bobodroid.myapplication.models.viewmodels.DollarViewModel
import com.bobodroid.myapplication.models.viewmodels.WonViewModel
import com.bobodroid.myapplication.models.viewmodels.YenViewModel
import com.bobodroid.myapplication.ui.theme.TopButtonColor
import kotlinx.coroutines.launch

@Composable
fun DrawerCustom(
    allViewModel: AllViewModel,
    dollarViewModel: DollarViewModel,
    yenViewModel: YenViewModel,
    wonViewModel: WonViewModel,
    drawerState: DrawerState,
    mainScreenSnackBarHostState: SnackbarHostState
) {

    val coroutineScope = rememberCoroutineScope()

    val userData = allViewModel.localUserData.collectAsState()

    val chargeDialog = remember { mutableStateOf(false) }

    val localUser = allViewModel.localUserData.collectAsState()

    val freeChance = localUser.value.rateResetCount

    val payChance = localUser.value.rateAdCount

    var customDialog by remember { mutableStateOf(false) }

    var cloudLoadDialog by remember { mutableStateOf(false) }

    var findIdDialog by remember { mutableStateOf(false) }

    var cloudSaveAskDialog by remember { mutableStateOf(false) }

    val context = LocalContext.current

    val id = if(localUser.value.customId == null) localUser.value.id else localUser.value.customId

    val webIntent = Intent(Intent.ACTION_VIEW)
    webIntent.data = Uri.parse("https://cobusil.vercel.app")

    val webPostIntent = Intent(Intent.ACTION_VIEW)
    webPostIntent.data = Uri.parse("https://cobusil.vercel.app/release/postBoard/dollarRecord")

    Column(
        modifier = Modifier
            .fillMaxHeight()
            .fillMaxWidth(2.3f / 3)
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            CardIconButton(imageVector = Icons.Rounded.Close, onClicked = {
                coroutineScope.launch {
                    drawerState.close()
                }
            }, modifier = Modifier, buttonColor = Color.White)
        }

        Column(
            Modifier
                .fillMaxWidth()
                .padding(bottom = 20.dp),
            horizontalAlignment = Alignment.Start) {

            Text(
                modifier = Modifier.padding(start = 10.dp, end = 20.dp, bottom = 5.dp),
                text = "ID: ${id}",
                fontSize = 15.sp,
                overflow = TextOverflow.Ellipsis,
                maxLines = 1)
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(top = 5.dp, start = 10.dp, end = 20.dp, bottom = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(space = 5.dp, alignment = Alignment.Start)) {

                CardButton(
                    label = "아이디 만들기",
                    onClicked = {

                        customDialog = true

                    },
                    buttonColor = TopButtonColor,
                    fontColor = Color.Black,
                    modifier = Modifier
                        .height(30.dp)
                        .width(80.dp),
                    fontSize = 15
                )

                CardButton(
                    label = "아이디 찾기",
                    onClicked = {
                        findIdDialog = true
                    },
                    buttonColor = TopButtonColor,
                    fontColor = Color.Black,
                    modifier = Modifier
                        .height(30.dp)
                        .width(80.dp),
                    fontSize = 15
                )

            }

            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(top = 5.dp, start = 10.dp, end = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(5.dp, Alignment.Start)) {

                CardButton(
                    label = "클라우드 저장",
                    onClicked = {

                        if(localUser.value.customId == null) {
                            coroutineScope.launch {
                                drawerState.close()
                                mainScreenSnackBarHostState.showSnackbar(
                                    "아이디를 새로 만든 후 진행해 주세요",
                                    actionLabel = "닫기", SnackbarDuration.Short
                                )
                            }
                        } else {
                            cloudSaveAskDialog = true
                        }


                    },
                    buttonColor = TopButtonColor,
                    fontColor = Color.Black,
                    modifier = Modifier
                        .height(30.dp)
                        .width(80.dp),
                    fontSize = 15
                )

                CardButton(
                    label = "클라우드 불러오기",
                    onClicked = {

                        if(localUser.value.customId == null) {
                            coroutineScope.launch {
                                drawerState.close()
                                mainScreenSnackBarHostState.showSnackbar(
                                    "아이디 찾은 후 진행해 주세요",
                                    actionLabel = "닫기", SnackbarDuration.Short
                                )
                            }
                        } else {
                            cloudLoadDialog = true
                        }
                    },
                    buttonColor = TopButtonColor,
                    fontColor = Color.Black,
                    modifier = Modifier
                        .height(30.dp)
                        .width(100.dp),
                    fontSize = 15
                )

            }
        }

        Divider(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 20.dp)
        )

        Row(modifier = Modifier
            .fillMaxWidth()
            .padding(start = 10.dp),
            horizontalArrangement = Arrangement.Start) {
            Text(text = "새로고침 업데이트 횟수: ${freeChance}(${payChance})회")

            Spacer(modifier = Modifier.width(10.dp))
            CardButton(
                label = "충전",
                onClicked = {
                    chargeDialog.value = true
                },
                buttonColor = TopButtonColor,
                fontColor = Color.Black,
                modifier = Modifier
                    .height(20.dp)
                    .width(40.dp),
                fontSize = 15
            )
        }

        Spacer(
            modifier = Modifier
                .height(10.dp)
        )

        Row(modifier = Modifier
            .fillMaxWidth()
            .padding(start = 10.dp),
            horizontalArrangement = Arrangement.Start) {
            Text(text = "스프레드: {}")

            Spacer(modifier = Modifier.width(10.dp))
            CardButton(
                label = "설정",
                onClicked = {
                    coroutineScope.launch {
                        drawerState.close()
                        mainScreenSnackBarHostState.showSnackbar(
                            "업데이트 예정입니다.",
                            actionLabel = "닫기", SnackbarDuration.Short
                        )
                    }
                },
                buttonColor = TopButtonColor,
                fontColor = Color.Black,
                modifier = Modifier
                    .height(20.dp)
                    .width(40.dp),
                fontSize = 15
            )
        }

        Divider(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp)
        )

        if(chargeDialog.value) {
            ChargeDialog(onDismissRequest = {
                chargeDialog.value = it
            }) {
                showInterstitial(context) {
                    allViewModel.chargeChance()

                    chargeDialog.value = false
                }
            }
        }

        if(customDialog) {
            CustomIdDialog(
                onDismissRequest = {
                    customDialog = it
                },
                placeholder = "커스텀 아이디를 입력해주세요",
                buttonLabel = "확인",
                onClicked = {customId, pin->
                    allViewModel.idCustom(customId, pin) { resultMessage->

                        coroutineScope.launch {
                            customDialog = false
                            drawerState.close()
                            mainScreenSnackBarHostState.showSnackbar(
                                resultMessage,
                                actionLabel = "닫기", SnackbarDuration.Short
                            )
                        }

                    }

                },
            )
        }

        if(cloudLoadDialog) {
            AskTriggerDialog(
                title = "현재 아이디:${localUser.value.customId} \n" +
                        "저장된 클라우드를 불러오시겠습니까?",
                onDismissRequest = {
                    cloudLoadDialog = it
                },
                onClicked = {

                    allViewModel.cloudLoad(localUser.value.customId!!) { cloudData, resultMessage->

                        dollarViewModel.drCloudLoad(
                            cloudData.drBuyRecord ?: emptyList(),
                            cloudData.drSellRecord ?: emptyList()
                        )

                        yenViewModel.yenCloudLoad(
                            cloudData.yenBuyRecord ?: emptyList(),
                            cloudData.yenSellRecord ?: emptyList()
                        )

                        wonViewModel.wonCloudLoad(
                            cloudData.wonBuyRecord ?: emptyList(),
                            cloudData.wonSellRecord ?: emptyList()
                        )


                        coroutineScope.launch {
                            cloudLoadDialog = false
                            drawerState.close()
                            mainScreenSnackBarHostState.showSnackbar(
                                resultMessage,
                                actionLabel = "닫기", SnackbarDuration.Short
                            )
                        }
                    }
                },
            )
        }

        if(cloudSaveAskDialog) {
            AskTriggerDialog(
                title = "현재 아이디:${localUser.value.customId} \n" +
                        "광고를 시청하고 클라우드에 저장하시겠습니까?",
                onDismissRequest = {
                    cloudSaveAskDialog = it
                },
                onClicked = {
                    showRewardedAdvertisement(context, onAdDismissed = {
                        allViewModel.cloudSave(
                            drBuyRecord = dollarViewModel.buyRecordFlow.value,
                            drSellRecord = dollarViewModel.sellRecordFlow.value,
                            yenBuyRecord = yenViewModel.buyRecordFlow.value,
                            yenSellRecord = yenViewModel.sellRecordFlow.value,
                            wonBuyRecord = wonViewModel.buyRecordFlow.value,
                            wonSellRecord = wonViewModel.sellRecordFlow.value
                        )
                        coroutineScope.launch {
                            cloudSaveAskDialog = false
                            drawerState.close()
                            mainScreenSnackBarHostState.showSnackbar(
                                "클라우드에 저장되었습니다.",
                                actionLabel = "닫기", SnackbarDuration.Short
                            )
                        }
                    })
                },
            )
        }

        if(findIdDialog) {
            CustomIdDialog(
                onDismissRequest = {
                    findIdDialog = it
                },
                placeholder = "아이디를 입력해주세요",
                buttonLabel = "확인",
                onClicked = { cloudId, pin->
                    allViewModel.findCustomId(cloudId, pin) { resultMessage->
                        coroutineScope.launch {
                            findIdDialog = false
                            drawerState.close()
                            mainScreenSnackBarHostState.showSnackbar(
                                resultMessage,
                                actionLabel = "닫기", SnackbarDuration.Short
                            )
                        }

                    }
                    findIdDialog = false
                },
            )
        }



        Spacer(modifier = Modifier.height(10.dp))



        Column(modifier = Modifier
            .wrapContentSize()
            .padding(start = 5.dp, bottom = 20.dp)) {
            Column(modifier = Modifier
                .fillMaxWidth()
                .padding(),
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.spacedBy(5.dp)) {
                CardButton(
                    label = "공식사이트 가기",
                    onClicked = {
                        ContextCompat.startActivity(context, webIntent, null)
                    },
                    fontSize = 15,
                    modifier = Modifier
                        .height(30.dp)
                        .width(120.dp),
                    fontColor = Color.Black,
                    buttonColor = TopButtonColor
                )

                CardButton(
                    label = "문의게시판 가기",
                    onClicked = {
                        ContextCompat.startActivity(context, webPostIntent, null)
                    },
                    fontSize = 15,
                    modifier = Modifier
                        .height(30.dp)
                        .width(120.dp),
                    fontColor = Color.Black,
                    buttonColor = TopButtonColor
                )
            }

        }


    }
}