package com.bobodroid.myapplication

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewTreeObserver
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material.DrawerState
import androidx.compose.material.DrawerValue
import androidx.compose.material.rememberDrawerState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.bobodroid.myapplication.components.Dialogs.GuideDialog
import com.bobodroid.myapplication.components.Dialogs.RewardShowAskDialog
import com.bobodroid.myapplication.components.Dialogs.ThanksDialog
import com.bobodroid.myapplication.components.MainBottomBar
import com.bobodroid.myapplication.components.MainTopBar
import com.bobodroid.myapplication.components.admobs.loadInterstitial
import com.bobodroid.myapplication.components.admobs.loadRewardedAdvertisement
import com.bobodroid.myapplication.components.admobs.loadTargetRewardedAdvertisement
import com.bobodroid.myapplication.components.admobs.showTargetRewardedAdvertisement
import com.bobodroid.myapplication.models.viewmodels.AllViewModel
import com.bobodroid.myapplication.models.viewmodels.AnalysisViewModel
import com.bobodroid.myapplication.models.viewmodels.DollarViewModel
import com.bobodroid.myapplication.models.viewmodels.WonViewModel
import com.bobodroid.myapplication.models.viewmodels.YenViewModel
import com.bobodroid.myapplication.routes.*
import com.bobodroid.myapplication.screens.*
import com.bobodroid.myapplication.ui.theme.InverstCalculatorTheme
import com.google.android.gms.ads.MobileAds
//import com.google.android.gms.ads.MobileAds
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class MainActivity : ComponentActivity() {


    companion object {
        fun TAG(location: String? = "메인", function: String): String {
            return "로그 $location $function"
        }
    }

    private val dollarViewModel: DollarViewModel by viewModels()

    private val yenViewModel: YenViewModel by viewModels()

    private val wonViewModel: WonViewModel by viewModels()

    private val allViewModel: AllViewModel by viewModels()

    private val analysisViewModel: AnalysisViewModel by viewModels()

    private lateinit var splashScreen: SplashScreen
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.w(TAG("메인",""), "onCreate 실행")

        splashScreen = installSplashScreen()

//        startSplash()

        val content: View = findViewById(android.R.id.content)
        content.viewTreeObserver.addOnPreDrawListener(
            object : ViewTreeObserver.OnPreDrawListener {
                override fun onPreDraw(): Boolean {

                    MobileAds.initialize(this@MainActivity)

//            allViewModel.deleteLocalUser()

//                    allViewModel.dateReset()

                    loadInterstitial(this@MainActivity)

                    loadRewardedAdvertisement(this@MainActivity, allViewModel)

                    loadTargetRewardedAdvertisement(this@MainActivity)

//                    lifecycleScope.launchWhenStarted {
//                        allViewModel.recentExchangeRateFlow.collect { recentRate ->
//                            recentRate?.let { rate ->
//                                dollarViewModel.requestRate(rate)
//                                yenViewModel.requestRate(rate)
//                                wonViewModel.requestRate(rate)
//                            }
//                        }
//                    }

                    Thread.sleep(1500)
                    // The content is ready. Start drawing.
                    content.viewTreeObserver.removeOnPreDrawListener(this)
                    return true
                }
            }
        )

        setContent {
            InverstCalculatorTheme {
                AppScreen(
                    dollarViewModel,
                    yenViewModel,
                    wonViewModel,
                    allViewModel,
                    analysisViewModel,
                    activity = this
                )
            }

        }
    }

    override fun onStart() {
        super.onStart()
        // 앱 초기 실행 및 백그라운드에서 포그라운드로 전환될 때 실행

        Log.w(TAG("메인", ""), "onStart 실행")

        checkAppPushNotification()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 1 && resultCode == RESULT_OK) {
            Log.w(TAG("메인", ""), "보상형 액티비티에서 넘어옴")
        }
    }

    // 스플래쉬 애니메이션
//    private fun startSplash() {
//        splashScreen.setOnExitAnimationListener { splashScreenView ->
//
//            Log.w(TAG("메인", ""), "${splashScreenView.iconView}")
//
//            val translateY =
//                PropertyValuesHolder.ofFloat(View.TRANSLATION_Y, 0f, -50f, 0f) // 위아래로 이동
//
//            ObjectAnimator.ofPropertyValuesHolder(splashScreenView.iconView, translateY).run {
//                duration = 1500L
//                interpolator = LinearInterpolator()
//                repeatCount = 2
//                repeatMode = ObjectAnimator.REVERSE
//                doOnEnd {
//                    splashScreenView.remove()
//
//                }
//                start()
//            }
//        }
//    }



    // 알람 권한 스테이트 값으로 저장하여 유저가 확인할 수 있도록 안내
    private fun checkAppPushNotification() {
        //Android 13 이상 && 푸시권한 없음
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
            && PackageManager.PERMISSION_DENIED == ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.POST_NOTIFICATIONS
            )
        ) {
            allViewModel.alarmPermissionState.value = false

            permissionPostNotification.launch(android.Manifest.permission.POST_NOTIFICATIONS)
            return
        }
        allViewModel.alarmPermissionState.value = true
        //권한이 있을때
    }

    /** 권한 요청 */
    private val permissionPostNotification =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                //권한 허용
            } else {
                //권한 비허용
            }
        }

    override fun onDestroy() {
        super.onDestroy()
    }




}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun AppScreen(
    dollarViewModel: DollarViewModel,
    yenViewModel: YenViewModel,
    wonViewModel: WonViewModel,
    allViewModel: AllViewModel,
    analysisViewModel: AnalysisViewModel,
    activity: Activity
) {


    val scope = rememberCoroutineScope()

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {
        MainTopBar()

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Bottom
        ) {

            InvestAppScreen(
                dollarViewModel,
                yenViewModel,
                wonViewModel,
                allViewModel,
                analysisViewModel,
                drawerState = drawerState,
                activity
            )
        }

    }






}


@Composable
fun InvestAppScreen(
    dollarViewModel: DollarViewModel,
    yenViewModel: YenViewModel,
    wonViewModel: WonViewModel,
    allViewModel: AllViewModel,
    analysisViewModel: AnalysisViewModel,
    drawerState: DrawerState,
    activity: Activity
) {


    val investNavController = rememberNavController()
    val investRouteAction = remember(investNavController) {
        MainRouteAction(investNavController)
    }

    val mainBackStack = investNavController.currentBackStackEntryAsState()

    var guideDialog by remember {
        mutableStateOf(false)
    }

    var returnGuideDialog by remember {
        mutableStateOf(false)
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {

        Column(
            modifier = Modifier.weight(1f)
        ) {
            InvestNavHost(
                investNavController = investNavController,
                dollarViewModel = dollarViewModel,
                yenViewModel = yenViewModel,
                wonViewModel = wonViewModel,
                routeAction = investRouteAction,
                allViewModel = allViewModel,
                drawerState = drawerState,
                activity = activity,
                analysisViewModel = analysisViewModel)
        }



        Column(
            modifier = Modifier.wrapContentSize()
        ) {
            MainBottomBar(
                mainRouteAction = investRouteAction,
                mainRouteBackStack = mainBackStack.value,
                allViewModel = allViewModel)
        }

        if(guideDialog) {
            GuideDialog(onDismissRequest = {
                                           guideDialog = it
            }, title = "안내", message = "아이디 생성 후 다시 시도해주세요", buttonLabel = "확인")
        }

        if(returnGuideDialog) {
            GuideDialog(onDismissRequest = {
                returnGuideDialog = it
            }, title = "안내", message = "추후 출시될 예정입니다.", buttonLabel = "확인")
        }

    }




}


@Composable
fun InvestNavHost(
    investNavController: NavHostController,
    startRouter: MainRoute = MainRoute.Main,
    dollarViewModel: DollarViewModel,
    yenViewModel: YenViewModel,
    wonViewModel: WonViewModel,
    analysisViewModel: AnalysisViewModel,
    routeAction: MainRouteAction,
    allViewModel: AllViewModel,
    drawerState: DrawerState,
    activity: Activity
) {

    val rewardShowDialog = allViewModel.rewardShowDialog.collectAsState()

    var thankShowingDialog by remember { mutableStateOf(false) }

    NavHost(navController = investNavController, startDestination = startRouter.routeName!!) {
        composable(MainRoute.Main.routeName!!) {
            MainScreen(
                dollarViewModel = dollarViewModel,
                yenViewModel = yenViewModel,
                wonViewModel = wonViewModel,
                routeAction = routeAction,
                allViewModel = allViewModel,
                drawerState = drawerState,
                activity = activity
            )
        }

        composable(MainRoute.Alert.routeName!!) {
            AlarmScreen(allViewModel = allViewModel)
        }

        composable(MainRoute.MyPage.routeName!!) {
            MyPageScreen(routeAction = routeAction, allViewModel = allViewModel)
        }

        composable(MainRoute.CreateUser.routeName!!) {
            CreateUSerScreen(routeAction = routeAction, allViewModel = allViewModel)
        }

        composable(MainRoute.CustomerServiceCenter.routeName!!) {
            CustomerScreen(activity = activity, routeAction = routeAction)
        }

        composable(MainRoute.CloudService.routeName!!) {
            CloudScreen(routeAction, allViewModel, dollarViewModel, yenViewModel, wonViewModel)
        }

        composable(MainRoute.AnalysisScreen.routeName!!) {
            AnalysisScreen(analysisViewModel)
        }


    }

    if(rewardShowDialog.value) {
        RewardShowAskDialog(
            onDismissRequest = {
                allViewModel.rewardDelayDate()
                allViewModel.rewardShowDialog.value = it
            },
            onClicked = {
                showTargetRewardedAdvertisement(activity, onAdDismissed = {
                    allViewModel.rewardDelayDate()
                    allViewModel.deleteBannerDelayDate()
                    allViewModel.rewardShowDialog.value = false
                    thankShowingDialog = true
                })
            })
    }

    if(thankShowingDialog)
        ThanksDialog(onDismissRequest = { value ->
            thankShowingDialog = value
        })
}





