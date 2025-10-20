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
import com.bobodroid.myapplication.components.MainBottomBar
import com.bobodroid.myapplication.components.MainTopBar
import com.bobodroid.myapplication.models.datamodels.social.SocialLoginManager
import com.bobodroid.myapplication.models.viewmodels.AnalysisViewModel
import com.bobodroid.myapplication.models.viewmodels.MainViewModel
import com.bobodroid.myapplication.routes.*
import com.bobodroid.myapplication.screens.*
import com.bobodroid.myapplication.ui.theme.InverstCalculatorTheme
import com.bobodroid.myapplication.widget.WidgetAlarmManager
import com.bobodroid.myapplication.widget.WidgetUpdateHelper
import com.bobodroid.myapplication.widget.WidgetUpdateService
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
    private val analysisViewModel: AnalysisViewModel by viewModels()

    @Inject
    lateinit var socialLoginManager: SocialLoginManager

    private lateinit var splashScreen: SplashScreen

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        Log.w(TAG("메인","onCreate"), "━━━━━━━━━━━━━━━━━━━━━━━━━━")
        Log.w(TAG("메인","onCreate"), "📱 onCreate 실행")
        Log.w(TAG("메인","onCreate"), "savedInstanceState: ${if (savedInstanceState == null) "NULL (새로 생성)" else "존재 (복원)"}")
        Log.w(TAG("메인","onCreate"), "Intent: ${intent?.extras}")
        Log.w(TAG("메인","onCreate"), "━━━━━━━━━━━━━━━━━━━━━━━━━━")

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

        setContent {
            InverstCalculatorTheme {
                AppScreen(
                    mainViewModel,
                    analysisViewModel,
                    activity = this
                )
            }
        }

        // ✅ 위젯 자동 업데이트 시작
        Log.d(TAG("메인","onCreate"), "🔧 setupWidgetAutoUpdate() 호출 시도...")
        try {
            setupWidgetAutoUpdate()
            Log.d(TAG("메인","onCreate"), "✅ setupWidgetAutoUpdate() 완료")
        } catch (e: Exception) {
            Log.e(TAG("메인","onCreate"), "❌ setupWidgetAutoUpdate() 실패", e)
        }
    }

    // ✅ 새로운 메서드 추가
    private fun setupWidgetAutoUpdate() {
        lifecycleScope.launch {
            // User DB에서 프리미엄 상태 확인
            val isPremium = mainViewModel.checkPremiumStatus()

            Log.d(TAG("메인", "setupWidgetAutoUpdate"), "━━━━━━━━━━━━━━━━━━━━━━━━━━")
            Log.d(TAG("메인", "setupWidgetAutoUpdate"), "프리미엄 상태: $isPremium")

            if (isPremium) {
                // 프리미엄: WorkManager 중지, Foreground Service는 설정 화면에서 제어
                WidgetAlarmManager.stopPeriodicUpdate(this@MainActivity)
                Log.d(TAG("메인", "setupWidgetAutoUpdate"), "✅ 프리미엄 사용자 - WorkManager 중지")
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

        // ✅ 위젯 즉시 업데이트하여 눌림 상태 해제
        WidgetUpdateHelper.updateAllWidgets(this)
    }

    // ✅ onStop()에도 위젯 업데이트 추가
    override fun onStop() {
        super.onStop()
        Log.w(TAG("메인", "onStop"), "━━━━━━━━━━━━━━━━━━━━━━━━━━")
        Log.w(TAG("메인", "onStop"), "⏹️ onStop 실행 - 앱이 백그라운드로 이동")
        Log.w(TAG("메인", "onStop"), "━━━━━━━━━━━━━━━━━━━━━━━━━━")

        // ✅ 앱 종료 시 위젯 정상화
        WidgetUpdateHelper.updateAllWidgets(this)
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

    // ✅ 위젯에서 클릭 시 호출됨 (앱이 이미 실행 중일 때)
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

    // ✅ Intent 처리 (네비게이션)
    private fun handleIntent(intent: Intent?) {
        val navigateTo = intent?.getStringExtra("NAVIGATE_TO")

        Log.d(TAG("메인", "handleIntent"), "━━━━━━━━━━━━━━━━━━━━━━━━━━")
        Log.d(TAG("메인", "handleIntent"), "Intent 처리 시작")
        Log.d(TAG("메인", "handleIntent"), "navigateTo: $navigateTo")
        Log.d(TAG("메인", "handleIntent"), "━━━━━━━━━━━━━━━━━━━━━━━━━━")

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
    analysisViewModel: AnalysisViewModel,
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
                analysisViewModel,
                activity
            )
        }
    }
}


@Composable
fun InvestAppScreen(
    mainViewModel: MainViewModel,
    analysisViewModel: AnalysisViewModel,
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
                analysisViewModel = analysisViewModel,
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
    analysisViewModel: AnalysisViewModel,
    mainViewModel: MainViewModel,
    activity: Activity
) {
    NavHost(navController = investNavController, startDestination = startRouter.routeName!!) {
        composable(MainRoute.Main.routeName!!) {
            MainScreen(
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