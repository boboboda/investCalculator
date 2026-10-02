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
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.bobodroid.myapplication.components.Dialogs.GuideDialog
import com.bobodroid.myapplication.components.Dialogs.PremiumAdDialogHost
import com.bobodroid.myapplication.components.MainBottomBar
import com.bobodroid.myapplication.models.datamodels.social.SocialLoginManager
import com.bobodroid.myapplication.models.datamodels.useCases.FcmUseCases
import com.bobodroid.myapplication.models.viewmodels.MainViewModel
import com.bobodroid.myapplication.models.viewmodels.SharedViewModel
import com.bobodroid.myapplication.routes.*
import com.bobodroid.myapplication.screens.*
import com.bobodroid.myapplication.screens.backtest.BacktestScreen
import com.bobodroid.myapplication.test.Phase2TestRunner
import com.bobodroid.myapplication.ui.theme.InverstCalculatorTheme
import com.bobodroid.myapplication.util.AdMob.AdManager
import com.bobodroid.myapplication.util.PreferenceUtil
import com.bobodroid.myapplication.widget.WidgetAlarmManager
import com.bobodroid.myapplication.widget.WidgetUpdateHelper
import com.bobodroid.myapplication.widget.WidgetUpdateService
import com.bobodroid.myapplication.util.result.Result
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject


@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    companion object {
        fun TAG(location: String? = "메인", function: String): String {
            return "로그 $location $function"
        }
    }

    private val mainViewModel: MainViewModel by viewModels()

    private val sharedViewModel: SharedViewModel by viewModels()

    @Inject lateinit var fcmUseCases: FcmUseCases

    @Inject
    lateinit var adManager: AdManager

    @Inject
    lateinit var socialLoginManager: SocialLoginManager

    private lateinit var splashScreen: SplashScreen

    // ✅ PreferenceUtil 추가
    private lateinit var preferenceUtil: PreferenceUtil

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()


        super.onCreate(savedInstanceState)

        // ✅ deviceId(=유저 로드)를 기다린 뒤 광고 프리로드 (SSV customData로 필요)
        lifecycleScope.launch {
            val deviceId = sharedViewModel.getDeviceId()
            adManager.preloadAllAds(this@MainActivity, deviceId)
        }

        handleIntent(intent)

        Log.w(TAG("메인","onCreate"), "━━━━━━━━━━━━━━━━━━━━━━━━━━")
        Log.w(TAG("메인","onCreate"), "📱 onCreate 실행")
        Log.w(TAG("메인","onCreate"), "savedInstanceState: ${if (savedInstanceState == null) "NULL (새로 생성)" else "존재 (복원)"}")
        Log.w(TAG("메인","onCreate"), "Intent: ${intent?.extras}")
        Log.w(TAG("메인","onCreate"), "━━━━━━━━━━━━━━━━━━━━━━━━━━")

        // ✅ PreferenceUtil 초기화
        preferenceUtil = PreferenceUtil(this)

        splashScreen = installSplashScreen()

        val content: View = findViewById(android.R.id.content)
        content.viewTreeObserver.addOnPreDrawListener(
            object : ViewTreeObserver.OnPreDrawListener {
                override fun onPreDraw(): Boolean {
                    Thread.sleep(1500)
                    content.viewTreeObserver.removeOnPreDrawListener(this)
                    return true
                }
            }
        )

        handleIntent(intent)


        Phase2TestRunner.runPhase2Test()

        setContent {
            InverstCalculatorTheme {
                AppScreen(
                    mainViewModel,
                    sharedViewModel,
                    activity = this
                )
            }
        }

        // ✅ 위젯 자동 업데이트 시작 (수정)
        Log.d(TAG("메인","onCreate"), "🔧 setupWidgetAutoUpdate() 호출 시도...")
        try {
            setupWidgetAutoUpdate()
            Log.d(TAG("메인","onCreate"), "✅ setupWidgetAutoUpdate() 완료")
        } catch (e: Exception) {
            Log.e(TAG("메인","onCreate"), "❌ setupWidgetAutoUpdate() 실패", e)
        }
    }

    // ✅ 수정된 메서드 - 앱 시작 시에는 서비스 시작하지 않음 (앱 실행 중이므로)
    private fun setupWidgetAutoUpdate() {
        lifecycleScope.launch {
            // User DB에서 프리미엄 상태 확인
            val isPremium = mainViewModel.checkPremiumStatus()

            Log.d(TAG("메인", "setupWidgetAutoUpdate"), "━━━━━━━━━━━━━━━━━━━━━━━━━━")
            Log.d(TAG("메인", "setupWidgetAutoUpdate"), "프리미엄 상태: $isPremium")

            if (isPremium) {
                // 프리미엄: WorkManager 중지
                WidgetAlarmManager.stopPeriodicUpdate(this@MainActivity)
                Log.d(TAG("메인", "setupWidgetAutoUpdate"), "✅ 프리미엄 사용자 - WorkManager 중지")

                Log.d(TAG("메인", "setupWidgetAutoUpdate"), "💡 앱 실행 중 - 서비스 시작 안 함 (배터리 절약)")
            } else {
                // 일반 사용자: WorkManager 시작 (5분 주기)
                WidgetAlarmManager.startPeriodicUpdate(this@MainActivity)
                Log.d(TAG("메인", "setupWidgetAutoUpdate"), "✅ 일반 사용자 - WorkManager 시작 (5분 주기)")
            }

            Log.d(TAG("메인", "setupWidgetAutoUpdate"), "━━━━━━━━━━━━━━━━━━━━━━━━━━")
        }
    }

    override fun onStart() {
        super.onStart()
        checkAppPushNotification()
    }

    override fun onResume() {
        super.onResume()

        // 위젯 즉시 업데이트하여 눌림 상태 해제
        WidgetUpdateHelper.updateAllWidgets(this)

        // ✅ 앱이 포그라운드로 돌아왔을 때 서비스 종료 (배터리 절약)
        lifecycleScope.launch {
            stopServiceIfRunning()
        }
    }

    // ✅ 수정된 onStop - 백그라운드 전환 시 서비스 자동 시작
    override fun onStop() {
        super.onStop()

        // 앱 종료 시 위젯 정상화
        WidgetUpdateHelper.updateAllWidgets(this)

        // ✅ 백그라운드 전환 시 프리미엄 서비스 체크 및 시작
        lifecycleScope.launch {
            checkAndStartServiceForBackground()
        }
    }

    // ✅ 새로 추가: 앱이 포그라운드로 돌아올 때 서비스 종료 (배터리 절약)
    private suspend fun stopServiceIfRunning() {
        try {
            if (isWidgetUpdateServiceRunning()) {
                WidgetUpdateService.stopService(this)
            }
        } catch (e: Exception) {
            Log.e(TAG("메인", "stopServiceIfRunning"), "서비스 종료 중 오류", e)
        }
    }

    // ✅ 새로 추가: 백그라운드 전환 시 서비스 체크 및 시작
    private suspend fun checkAndStartServiceForBackground() {
        try {
            val isPremium = mainViewModel.checkPremiumStatus()

            if (!isPremium) {
                return
            }

            val isRealtimeEnabled = preferenceUtil.getData("widget_service_running", "false") == "true"

            if (!isRealtimeEnabled) {
                return
            }

            val isServiceRunning = isWidgetUpdateServiceRunning()

            if (isServiceRunning) {
                return
            }

            WidgetUpdateService.startService(this)
            preferenceUtil.setData("service_manually_disabled", "false")

        } catch (e: Exception) {
            Log.e(TAG("메인", "checkAndStartService"), "서비스 체크 중 오류", e)
        }
    }

    // ✅ 새로 추가: 서비스 실행 상태 확인
    private fun isWidgetUpdateServiceRunning(): Boolean {
        val manager = getSystemService(ACTIVITY_SERVICE) as android.app.ActivityManager
        for (service in manager.getRunningServices(Integer.MAX_VALUE)) {
            if (WidgetUpdateService::class.java.name == service.service.className) {
                return true
            }
        }
        return false
    }

    override fun onPause() {
        super.onPause()
    }

    override fun onRestart() {
        super.onRestart()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        socialLoginManager.handleGoogleSignInResult(requestCode, resultCode, data)

        if (requestCode == 1 && resultCode == RESULT_OK) {
            Log.w(TAG("메인", ""), "보상형 액티비티에서 넘어옴")
        }
    }

    // 위젯에서 클릭 시 호출됨 (앱이 이미 실행 중일 때)
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntent(intent)
    }

    // Intent 처리 (네비게이션)
    private fun handleIntent(intent: Intent?) {
        val navigateTo = intent?.getStringExtra("NAVIGATE_TO")

        val fromNotification = intent?.getBooleanExtra("FROM_NOTIFICATION", false) ?: false
        val notificationId = intent?.getStringExtra("NOTIFICATION_ID")
        val notificationType = intent?.getStringExtra("NOTIFICATION_TYPE")
        val recordId = intent?.getStringExtra("RECORD_ID")
        val currency = intent?.getStringExtra("CURRENCY")

        if (fromNotification && notificationId != null) {
            lifecycleScope.launch {
                when (val result = fcmUseCases.markAsClickedUseCase(notificationId)) {
                    is Result.Success -> {
                        Log.d(TAG("메인", "handleIntent"), "✅ 알림 클릭 처리 완료")
                    }
                    is Result.Error -> {
                        Log.e(TAG("메인", "handleIntent"), "❌ 알림 클릭 처리 실패: ${result.message}")
                    }
                    is Result.Loading -> {
                    }
                }
            }

            when (notificationType) {
                "RATE_ALERT" -> {}
                "PROFIT_ALERT" -> {}
                "RECORD_AGE" -> {}
                "DAILY_SUMMARY" -> {}
            }
        }

        if (navigateTo != null) {
            Log.d(TAG("메인", "handleIntent"), "네비게이션 요청: $navigateTo")
        }
    }

    private fun checkAppPushNotification() {
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
    }

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
    mainViewModel: MainViewModel,
    sharedViewModel: SharedViewModel,
    activity: Activity
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(WindowInsets.systemBars.asPaddingValues())
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Bottom
        ) {
            InvestAppScreen(
                mainViewModel,
                sharedViewModel,
                activity
            )
        }
    }
}


@Composable
fun InvestAppScreen(
    mainViewModel: MainViewModel,
    sharedViewModel: SharedViewModel,
    activity: Activity
) {
    val investNavController = rememberNavController()
    val mainRouteAction = remember {
        RouteAction<MainRoute>(investNavController, MainRoute.Main.routeName)
    }

    val mainBackStack = investNavController.currentBackStackEntryAsState()

    var guideDialog by remember { mutableStateOf(false) }
    var returnGuideDialog by remember { mutableStateOf(false) }

    Scaffold(
        bottomBar = {
            MainBottomBar(
                mainRouteAction = mainRouteAction,
                mainRouteBackStack = mainBackStack.value,
                mainViewModel = mainViewModel)
        },
        // ✅ MainTopBar 제거: 하단 탭에서 이미 현재 화면이 드러나므로 상단 타이틀바가 불필요하다고 판단

        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            InvestNavHost(
                investNavController = investNavController,
                mainRouteAction = mainRouteAction, // ✅ 추가
                mainViewModel = mainViewModel,
                activity = activity,
                sharedViewModel = sharedViewModel
            )

            // ✅ 프리미엄 유도 / 리워드 안내 다이얼로그는 여기서만 표시
            PremiumAdDialogHost(sharedViewModel = sharedViewModel)

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
}


@Composable
fun InvestNavHost(
    investNavController: NavHostController,
    mainRouteAction: RouteAction<MainRoute>, // ✅ 추가
    startRouter: MainRoute = MainRoute.Main,
    sharedViewModel: SharedViewModel,
    mainViewModel: MainViewModel,
    activity: Activity
) {
    NavHost(navController = investNavController, startDestination = startRouter.routeName!!) {
        composable(MainRoute.Main.routeName!!) {
            MainScreen(
                mainViewModel = mainViewModel,
                activity = activity,
                onNavigateToPremium = {
                    sharedViewModel.requestPremiumUnlock()
                },
                onNavigateToNews = {
                    investNavController.navigate(MainRoute.News.routeName!!)
                },
                onNavigateToMyPage = {
                    // ✅ 수정: 스프레드 설정으로 직행하도록 요청 후 마이페이지 탭으로 이동
                    sharedViewModel.requestMyPageRoute(MyPageRoute.SpreadSettings.routeName!!)
                    mainRouteAction.navTo(MainRoute.MyPage)
                },
                onNavigateToBacktest = {
                    investNavController.navigate(MainRoute.Backtest.routeName!!)
                },
                sharedViewModel = sharedViewModel
            )
        }

        composable(MainRoute.Alert.routeName!!) {
            FcmAlarmScreen(
                onNavigateToSettings = {
                    investNavController.navigate(MainRoute.NotificationSettings.routeName!!)
                },
                sharedViewModel = sharedViewModel
            )
        }

        composable(MainRoute.MyPage.routeName!!) {
            MyPageScreen(
                sharedViewModel = sharedViewModel,
                onNavigateToBacktest = {
                    investNavController.navigate(MainRoute.Backtest.routeName!!)
                }
            )
        }

        composable(MainRoute.AnalysisScreen.routeName!!) {
            AnalysisScreen(
                sharedViewModel = sharedViewModel,
                onNavigateToPremium = {
                    sharedViewModel.requestPremiumUnlock()
                },
                onNavigateToNews = { investNavController.navigate(MainRoute.News.routeName!!) } // 추가

            )
        }

        composable(MainRoute.NotificationSettings.routeName!!) {
            NotificationSettingsScreen(
                onBackClick = {
                    investNavController.navigateUp()
                },
                onPremiumClick = {
                    sharedViewModel.requestPremiumUnlock()
                }
            )
        }

        composable(MainRoute.News.routeName!!) {
            NewsScreen(
                onBackClick = { investNavController.popBackStack() }
            )
        }

        // ✅ 신규: 환테크 백테스트 시뮬레이터 (자체 내부 NavHost — MyPageScreen과 동일 패턴)
        composable(MainRoute.Backtest.routeName!!) {
            BacktestScreen(
                onBackClick = {
                    investNavController.navigateUp()
                }
            )
        }
    }
}
