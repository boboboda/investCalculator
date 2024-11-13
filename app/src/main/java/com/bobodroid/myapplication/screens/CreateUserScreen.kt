package com.bobodroid.myapplication.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.SnackbarDuration
import androidx.compose.material.SnackbarHost
import androidx.compose.material.SnackbarHostState
import androidx.compose.material.TextButton
import androidx.compose.material3.Card
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bobodroid.myapplication.R
import com.bobodroid.myapplication.components.Buttons
import com.bobodroid.myapplication.components.shadowCustom
import com.bobodroid.myapplication.routes.MainRoute
import com.bobodroid.myapplication.routes.MainRouteAction
import androidx.compose.material3.Text
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import com.bobodroid.myapplication.components.Dialogs.CustomIdDialog
import com.bobodroid.myapplication.models.viewmodels.AllViewModel
import kotlinx.coroutines.launch

@Composable
fun CreateUSerScreen(routeAction: MainRouteAction, allViewModel: AllViewModel) {


    var customDialog by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()

    val mainScreenSnackBarHostState = remember { SnackbarHostState() }

    var logInDialog by remember { mutableStateOf(false) }

    var localUser = allViewModel.localUserFlow.collectAsState()


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
                    customDialog = true
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
                    text = "아이디 만들기",
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
                    logInDialog = true
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
                    ),
                enabled = localUser.value.customId == null || localUser.value.customId == ""
            ) {
                Text(
                    text = "로그인",
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
                    allViewModel.logout(result = {
                        coroutineScope.launch {
                            mainScreenSnackBarHostState.showSnackbar(
                                it,
                                actionLabel = "닫기", SnackbarDuration.Short
                            )}
                    })
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
                    ),
                enabled = localUser.value.customId != ""
            ) {
                Text(
                    text = "로그아웃",
                    fontSize = 15.sp,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(0.dp))
            }


        }

//        Row(
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(horizontal = 15.dp)
//        ) {
//
//            Buttons(
//                onClicked = {
//                    routeAction.navTo(MainRoute.CreateUser)
//                },
//                color = Color.White,
//                fontColor = Color.Black,
//                modifier = Modifier
//                    .height(55.dp)
//                    .fillMaxWidth()
//                    .padding(top = 10.dp)
//                    .shadowCustom(
//                        color = Color.LightGray,
//                        offsetX = 5.dp,
//                        offsetY = 5.dp,
//                        blurRadius = 10.dp
//                    )
//            ) {
//                Text(
//                    text = "아이디 삭제",
//                    fontSize = 15.sp,
//                    textAlign = TextAlign.Center,
//                    fontWeight = FontWeight.Bold,
//                    modifier = Modifier.padding(0.dp))
//            }
//        }



        if (customDialog) {
            CustomIdDialog(
                onDismissRequest = {
                    customDialog = it
                },
                placeholder = "커스텀 아이디를 입력해주세요",
                buttonLabel = "확인",
                onClicked = { customId, pin ->
                    allViewModel.createUser(customId, pin) { resultMessage ->

                        coroutineScope.launch {
                            customDialog = false
                            mainScreenSnackBarHostState.showSnackbar(
                                resultMessage,
                                actionLabel = "닫기", SnackbarDuration.Short
                            )
                        }

                    }

                },
            )
        }

        if (logInDialog) {
            CustomIdDialog(
                onDismissRequest = {
                    logInDialog = it
                },
                placeholder = "아이디를 입력해주세요",
                buttonLabel = "확인",
                onClicked = { cloudId, pin ->
                    allViewModel.logIn(cloudId, pin) { resultMessage ->
                        coroutineScope.launch {
                            logInDialog = false
                            mainScreenSnackBarHostState.showSnackbar(
                                resultMessage,
                                actionLabel = "닫기", SnackbarDuration.Short
                            )
                        }

                    }
                    logInDialog = false
                },
            )
        }

        Spacer(modifier = Modifier.weight(1f))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
        ) {
            SnackbarHost(
                hostState = mainScreenSnackBarHostState, modifier = Modifier,
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
                                        mainScreenSnackBarHostState.currentSnackbarData?.dismiss()
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