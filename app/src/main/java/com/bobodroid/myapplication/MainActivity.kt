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
import com.bobodroid.myapplication.models.viewmodels.AnalysisViewModel
import com.bobodroid.myapplication.models.viewmodels.DollarViewModel
import com.bobodroid.myapplication.models.viewmodels.MainViewModel
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

    private val mainViewModel: MainViewModel by viewModels()

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


                    //            allViewModel.deleteLocalUser()

//                    allViewModel.dateReset()

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
                    mainViewModel,
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
            mainViewModel.alarmPermissionState.value = false

            permissionPostNotification.launch(android.Manifest.permission.POST_NOTIFICATIONS)
            return
        }
        mainViewModel.alarmPermissionState.value = true
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
    mainViewModel: MainViewModel,
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
                mainViewModel,
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
    mainViewModel: MainViewModel,
    analysisViewModel: AnalysisViewModel,
    drawerState: DrawerState,
    activity: Activity
) {


    val investNavController = rememberNavController()
    val mainRouteAction = remember {
        RouteAction<MainRoute>(investNavController, MainRoute.Main.routeName)
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
                routeAction = mainRouteAction,
                mainViewModel = mainViewModel,
                drawerState = drawerState,
                activity = activity,
                analysisViewModel = analysisViewModel)
        }



        Column(
            modifier = Modifier.wrapContentSize()
        ) {
            MainBottomBar(
                mainRouteAction = mainRouteAction,
                mainRouteBackStack = mainBackStack.value,
                mainViewModel = mainViewModel)
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
    analysisViewModel: AnalysisViewModel,
    mainViewModel: MainViewModel,
    activity: Activity
) {

    NavHost(navController = investNavController, startDestination = startRouter.routeName!!) {
        composable(MainRoute.Main.routeName!!) {
            MainScreen(
                dollarViewModel = dollarViewModel,
                yenViewModel = yenViewModel,
                mainViewModel = mainViewModel,
                activity = activity
            )
        }

        composable(MainRoute.Alert.routeName!!) {
            FcmAlarmScreen()
        }

        composable(MainRoute.MyPage.routeName!!) {
            MyPageScreen()
        }

        composable(MainRoute.AnalysisScreen.routeName!!) {
            AnalysisScreen(analysisViewModel)
        }


    }


}





