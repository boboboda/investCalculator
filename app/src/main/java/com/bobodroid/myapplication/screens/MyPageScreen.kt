package com.bobodroid.myapplication.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.bobodroid.myapplication.R
import com.bobodroid.myapplication.components.Buttons
import com.bobodroid.myapplication.components.CardIconButton
import com.bobodroid.myapplication.components.Dialogs.GuideDialog
import com.bobodroid.myapplication.components.shadowCustom
import com.bobodroid.myapplication.models.datamodels.roomDb.LocalUserData
import com.bobodroid.myapplication.models.viewmodels.AllViewModel
import com.bobodroid.myapplication.routes.MainRoute
import com.bobodroid.myapplication.routes.MyPageRoute
import com.bobodroid.myapplication.routes.RouteAction
import com.bobodroid.myapplication.ui.theme.MyPageButtonColor

@Composable
fun MyPageScreen(allViewModel: AllViewModel) {

    val uiState by allViewModel.allUiState.collectAsState()

    val navController = rememberNavController()

    val myPageRouteAction = remember {
        RouteAction<MyPageRoute>(navController, MyPageRoute.SelectView.routeName)
    }

    val rewardAdIsReadyState = allViewModel.rewardIsReadyStateFlow.collectAsState()

    NavHost(
        navController = navController,
        startDestination = MyPageRoute.SelectView.routeName ?: "",
    ) {

        composable(MyPageRoute.SelectView.routeName!!) {
            MyPageSelectView(myPageRouteAction, uiState.localUser)
        }


        composable(MyPageRoute.CreateUser.routeName!!) {
            CreateUserScreen(routeAction = myPageRouteAction, allViewModel = allViewModel)
        }

        composable(MyPageRoute.CustomerServiceCenter.routeName!!) {
            CustomerScreen(routeAction = myPageRouteAction)
        }

        composable(MyPageRoute.CloudService.routeName!!) {
            CloudScreen(myPageRouteAction, localUser =  uiState.localUser)
        }

    }
}

@Composable
fun MyPageSelectView(
    routeAction: RouteAction<MyPageRoute>,
    localUser: LocalUserData
) {



    var showGuidDialog by remember { mutableStateOf(false) }

    val id = if (localUser.customId != null && localUser.customId != "") { localUser.customId } else { "아이디를 만들어주세요" }


    var showRewardGuideDialog by remember {
        mutableStateOf(false)
    }



    Column(
        modifier = Modifier.fillMaxSize()
    ) {

        Row(modifier = Modifier
            .height(35.dp)
            .padding(top = 5.dp), verticalAlignment = Alignment.CenterVertically){

            Image(painter = painterResource(id = R.drawable.user), contentDescription = "", modifier = Modifier
                .padding(end = 5.dp)
                .padding(start = 15.dp))

            Text(text = "마이페이지", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 15.dp)
        ) {
            Buttons(
                onClicked = {
                    routeAction.navTo(MyPageRoute.CreateUser)
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
            ) {
                Text(
                    text = "id: $id",
                    fontSize = 15.sp,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(0.dp))

                Spacer(modifier = Modifier.weight(1f))

                Image(painter = painterResource(id = R.drawable.baseline_arrow_forward_ios_24), contentDescription = "", modifier = Modifier.size(15.dp))
            }


        }

        Row(modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 15.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)) {

            Buttons(onClicked = {
                localUser.customId?.let { id ->

                    if (id.isNotEmpty()) {
                        routeAction.navTo(MyPageRoute.CloudService)
                    } else {
                        showGuidDialog = true
                    }
                } ?: run {
                    showGuidDialog = true
                }


            },  color = MyPageButtonColor,
                fontColor = Color.Black,
                modifier = Modifier
                    .height(120.dp)
                    .weight(0.5f)
                    .padding(top = 10.dp)
                    .shadowCustom(
                        color = Color.LightGray,
                        offsetX = 10.dp,
                        offsetY = 10.dp,
                        blurRadius = 10.dp
                    )) {

                Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {

                    Image(painter = painterResource(id = R.drawable.baseline_cloud_queue_24), contentDescription = "", modifier = Modifier.size(30.dp))

                    Text("클라우드", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                }


            }

            Buttons(onClicked = {
                routeAction.navTo(MyPageRoute.CustomerServiceCenter)
            },  color = MyPageButtonColor,
                fontColor = Color.Black,
                modifier = Modifier
                    .height(120.dp)
                    .weight(0.5f)
                    .padding(top = 10.dp)
                    .shadowCustom(
                        color = Color.LightGray,
                        offsetX = 10.dp,
                        offsetY = 10.dp,
                        blurRadius = 10.dp
                    )) {

                Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {

                    Image(painter = painterResource(id = R.drawable.baseline_help_center_24), contentDescription = "", modifier = Modifier.size(30.dp))

                    Text("고객센터", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                }

            }

        }


        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 15.dp)
        ) {
            Buttons(
                onClicked = {

                    if(rewardAdIsReadyState.value) {
                        allViewModel.rewardShowDialog.value = true
                    } else {
                        showRewardGuideDialog = true
                    }


                },
                color = Color.White,
                fontColor = Color.Black,
                modifier = Modifier
                    .height(70.dp)
                    .fillMaxWidth()
                    .padding(top = 10.dp)
                    .shadowCustom(
                        color = Color.LightGray,
                        offsetX = 5.dp,
                        offsetY = 5.dp,
                        blurRadius = 10.dp
                    ),
            ) {
                Text(
                    text = "광고 배너 제거",
                    fontSize = 15.sp,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(0.dp))
            }


        }


        if(showRewardGuideDialog) {
            GuideDialog(onDismissRequest = {
                showRewardGuideDialog = it
            }, title = "안내", message = "광고가 준비되어있지 않습니다. 잠시 후 다시 시도해주세요", buttonLabel = "확인")
        }

        if(showGuidDialog) {
            GuideDialog(onDismissRequest = {
                showGuidDialog = it
            }, title = "안내", message = "아이디 생성 또는 로그인 후 시도해주세요", buttonLabel = "확인")
        }

    }
}