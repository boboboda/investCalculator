package com.bobodroid.myapplication

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.database.sqlite.SQLiteDatabase
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
import androidx.hilt.work.HiltWorkerFactory
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.work.Configuration
import com.bobodroid.myapplication.components.Dialogs.GuideDialog
import com.bobodroid.myapplication.components.MainBottomBar
import com.bobodroid.myapplication.components.MainTopBar
import com.bobodroid.myapplication.models.datamodels.social.SocialLoginManager
import com.bobodroid.myapplication.models.datamodels.useCases.FcmUseCases
import com.bobodroid.myapplication.models.viewmodels.AnalysisViewModel
import com.bobodroid.myapplication.models.viewmodels.MainViewModel
import com.bobodroid.myapplication.models.viewmodels.SharedViewModel
import com.bobodroid.myapplication.routes.*
import com.bobodroid.myapplication.screens.*
import com.bobodroid.myapplication.test.Phase2TestRunner
import com.bobodroid.myapplication.ui.theme.InvestCalculatorTheme
import com.bobodroid.myapplication.util.AdMob.AdManager
import com.bobodroid.myapplication.util.PreferenceUtil
import com.bobodroid.myapplication.widget.WidgetAlarmManager
import com.bobodroid.myapplication.widget.WidgetUpdateHelper
import com.bobodroid.myapplication.widget.WidgetUpdateService
import com.bobodroid.myapplication.util.result.Result
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.util.UUID
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

        adManager.preloadAllAds(this)

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
            InvestCalculatorTheme {
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

                // ✅ 앱 실행 중에는 서비스 시작하지 않음 (배터리 절약)
                // 백그라운드로 갈 때만 서비스 시작
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
        Log.w(TAG("메인", "onStart"), "━━━━━━━━━━━━━━━━━━━━━━━━━━")
        Log.w(TAG("메인", "onStart"), "🟢 onStart 실행 - 앱이 보이기 시작")
        Log.w(TAG("메인", "onStart"), "━━━━━━━━━━━━━━━━━━━━━━━━━━")
        checkAppPushNotification()
    }

    override fun onResume() {
        super.onResume()
        Log.w(TAG("메인", "onResume"), "━━━━━━━━━━━━━━━━━━━━━━━━━━")
        Log.w(TAG("메인", "onResume"), "▶️ onResume 실행 - 사용자와 상호작용 가능")
        Log.w(TAG("메인", "onResume"), "━━━━━━━━━━━━━━━━━━━━━━━━━━")

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
        Log.w(TAG("메인", "onStop"), "━━━━━━━━━━━━━━━━━━━━━━━━━━")
        Log.w(TAG("메인", "onStop"), "⏹️ onStop 실행 - 앱이 백그라운드로 이동")
        Log.w(TAG("메인", "onStop"), "━━━━━━━━━━━━━━━━━━━━━━━━━━")

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
            // 서비스 실행 중인지 확인
            if (isWidgetUpdateServiceRunning()) {
                Log.d(TAG("메인", "stopServiceIfRunning"), "━━━━━━━━━━━━━━━━━━━━━━━━━━")
                Log.d(TAG("메인", "stopServiceIfRunning"), "🛑 앱이 포그라운드로 복귀 - 서비스 종료")
                Log.d(TAG("메인", "stopServiceIfRunning"), "💡 이유: 앱 실행 중에는 서비스 불필요 (배터리 절약)")

                // 서비스 종료
                WidgetUpdateService.stopService(this)

                // 서비스 상태는 유지 (백그라운드 진입 시 다시 시작하기 위해)
                // preferenceUtil.setData("widget_service_running", "true") 는 그대로 유지

                Log.d(TAG("메인", "stopServiceIfRunning"), "✅ 서비스 종료 완료")
                Log.d(TAG("메인", "stopServiceIfRunning"), "━━━━━━━━━━━━━━━━━━━━━━━━━━")
            } else {
                Log.d(TAG("메인", "stopServiceIfRunning"), "서비스가 실행 중이지 않음")
            }
        } catch (e: Exception) {
            Log.e(TAG("메인", "stopServiceIfRunning"), "서비스 종료 중 오류", e)
        }
    }

    // ✅ 새로 추가: 백그라운드 전환 시 서비스 체크 및 시작
    private suspend fun checkAndStartServiceForBackground() {
        try {
            Log.d(TAG("메인", "checkAndStartService"), "━━━━━━━━━━━━━━━━━━━━━━━━━━")
            Log.d(TAG("메인", "checkAndStartService"), "🔍 백그라운드 서비스 체크 시작")

            // 1. 프리미엄 상태 확인
            val isPremium = mainViewModel.checkPremiumStatus()
            Log.d(TAG("메인", "checkAndStartService"), "프리미엄 상태: $isPremium")

            if (!isPremium) {
                Log.d(TAG("메인", "checkAndStartService"), "일반 사용자 - 서비스 시작 안 함")
                return
            }

            // 2. 실시간 업데이트 설정 확인
            val isRealtimeEnabled = preferenceUtil.getData("widget_service_running", "false") == "true"
            Log.d(TAG("메인", "checkAndStartService"), "실시간 업데이트 설정: $isRealtimeEnabled")

            if (!isRealtimeEnabled) {
                Log.d(TAG("메인", "checkAndStartService"), "실시간 업데이트 비활성화 - 서비스 시작 안 함")
                return
            }

            // 3. 서비스 실행 중인지 확인
            val isServiceRunning = isWidgetUpdateServiceRunning()
            Log.d(TAG("메인", "checkAndStartService"), "서비스 실행 상태: $isServiceRunning")

            if (isServiceRunning) {
                Log.d(TAG("메인", "checkAndStartService"), "이미 서비스 실행 중")
                return
            }

            // 4. 모든 조건 충족 시 서비스 시작
            Log.d(TAG("메인", "checkAndStartService"), "✅ 모든 조건 충족 - 백그라운드 서비스 시작!")
            Log.d(TAG("메인", "checkAndStartService"), "💡 백그라운드에서만 서비스 실행 (배터리 최적화)")
            WidgetUpdateService.startService(this)

            // 수동으로 끈 상태 해제
            preferenceUtil.setData("service_manually_disabled", "false")

            Log.d(TAG("메인", "checkAndStartService"), "━━━━━━━━━━━━━━━━━━━━━━━━━━")

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
        Log.w(TAG("메인", "onPause"), "━━━━━━━━━━━━━━━━━━━━━━━━━━")
        Log.w(TAG("메인", "onPause"), "⏸️ onPause 실행 - 앱이 일시정지")
        Log.w(TAG("메인", "onPause"), "━━━━━━━━━━━━━━━━━━━━━━━━━━")
    }

    override fun onRestart() {
        super.onRestart()
        Log.w(TAG("메인", "onRestart"), "━━━━━━━━━━━━━━━━━━━━━━━━━━")
        Log.w(TAG("메인", "onRestart"), "🔄 onRestart 실행 - 백그라운드에서 복귀")
        Log.w(TAG("메인", "onRestart"), "━━━━━━━━━━━━━━━━━━━━━━━━━━")
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

        Log.e(TAG("메인", "onNewIntent"), "━━━━━━━━━━━━━━━━━━━━━━━━━━")
        Log.e(TAG("메인", "onNewIntent"), "🔔 onNewIntent 호출됨!")
        Log.e(TAG("메인", "onNewIntent"), "Intent Extras: ${intent.extras}")
        Log.e(TAG("메인", "onNewIntent"), "NAVIGATE_TO: ${intent.getStringExtra("NAVIGATE_TO")}")
        Log.e(TAG("메인", "onNewIntent"), "━━━━━━━━━━━━━━━━━━━━━━━━━━")

        handleIntent(intent)
    }

    // Intent 처리 (네비게이션)
    private fun handleIntent(intent: Intent?) {
        val navigateTo = intent?.getStringExtra("NAVIGATE_TO")

        // ✅ 알림 클릭으로 앱이 열렸는지 확인
        val fromNotification = intent?.getBooleanExtra("FROM_NOTIFICATION", false) ?: false
        val notificationId = intent?.getStringExtra("NOTIFICATION_ID")
        val notificationType = intent?.getStringExtra("NOTIFICATION_TYPE")
        val recordId = intent?.getStringExtra("RECORD_ID")
        val currency = intent?.getStringExtra("CURRENCY")

        Log.d(TAG("메인", "handleIntent"), "━━━━━━━━━━━━━━━━━━━━━━━━━━")
        Log.d(TAG("메인", "handleIntent"), "Intent 처리 시작")
        Log.d(TAG("메인", "handleIntent"), "navigateTo: $navigateTo")
        Log.d(TAG("메인", "handleIntent"), "fromNotification: $fromNotification")
        Log.d(TAG("메인", "handleIntent"), "notificationId: $notificationId")
        Log.d(TAG("메인", "handleIntent"), "notificationType: $notificationType")
        Log.d(TAG("메인", "handleIntent"), "━━━━━━━━━━━━━━━━━━━━━━━━━━")

        // ✅ 알림 클릭 처리
        if (fromNotification && notificationId != null) {
            Log.d(TAG("메인", "handleIntent"), "알림 클릭 감지 - markAsClicked 호출")

            // ✅ lifecycleScope에서 호출하고 when으로 결과 처리
            lifecycleScope.launch {
                when (val result = fcmUseCases.markAsClickedUseCase(notificationId)) {
                    is Result.Success -> {
                        Log.d(TAG("메인", "handleIntent"), "✅ 알림 클릭 처리 완료")
                    }
                    is Result.Error -> {
                        Log.e(TAG("메인", "handleIntent"), "❌ 알림 클릭 처리 실패: ${result.message}")
                    }
                    is Result.Loading -> {
                        // 로딩 상태
                    }
                }
            }

            // ✅ 알림 타입에 따라 화면 이동
            when (notificationType) {
                "RATE_ALERT" -> {
                    Log.d(TAG("메인", "handleIntent"), "환율 알림 - 알림 화면으로 이동")
                    // TODO: 알림 화면으로 네비게이션
                    // navController.navigate("fcm_alarm_screen")
                }
                "PROFIT_ALERT" -> {
                    Log.d(TAG("메인", "handleIntent"), "수익률 알림 - 기록 상세로 이동 (recordId: $recordId)")
                    // TODO: 해당 기록 상세 화면으로 네비게이션
                    // navController.navigate("record_detail/$recordId")
                }
                "RECORD_AGE" -> {
                    Log.d(TAG("메인", "handleIntent"), "매수 경과 알림 - 기록 화면으로 이동")
                    // TODO: 기록 화면으로 네비게이션
                    // navController.navigate("record_screen")
                }
                "DAILY_SUMMARY" -> {
                    Log.d(TAG("메인", "handleIntent"), "일일 요약 - 마이페이지로 이동")
                    // TODO: 마이페이지로 네비게이션
                    // navController.navigate("my_page")
                }
            }
        }

        // 기존 네비게이션 처리
        if (navigateTo != null) {
            Log.d(TAG("메인", "handleIntent"), "네비게이션 요청: $navigateTo")
            // TODO: Compose Navigation으로 전달 필요
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
        Log.e(TAG("메인", "onDestroy"), "━━━━━━━━━━━━━━━━━━━━━━━━━━")
        Log.e(TAG("메인", "onDestroy"), "💀 onDestroy 실행 - Activity 완전 소멸!")
        Log.e(TAG("메인", "onDestroy"), "isFinishing: $isFinishing")
        Log.e(TAG("메인", "onDestroy"), "━━━━━━━━━━━━━━━━━━━━━━━━━━")
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
        topBar = {
            MainTopBar()
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            InvestNavHost(
                investNavController = investNavController,
                mainViewModel = mainViewModel,
                activity = activity,
                sharedViewModel = sharedViewModel
            )

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
                    investNavController.navigate(MainRoute.MyPage.routeName!!)
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
            MyPageScreen(sharedViewModel = sharedViewModel)
        }

        composable(MainRoute.AnalysisScreen.routeName!!) {
            AnalysisScreen(
                sharedViewModel = sharedViewModel
            )
        }

        composable(MainRoute.NotificationSettings.routeName!!) {
            NotificationSettingsScreen(
                onBackClick = {
                    investNavController.navigateUp()
                },
                onPremiumClick = {
                    investNavController.navigate(MyPageRoute.Premium.routeName!!)
                }
            )
        }
    }
}