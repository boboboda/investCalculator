package com.bobodroid.myapplication.routes

import androidx.navigation.NavHostController
import com.bobodroid.myapplication.R


interface Route {
    val routeName: String?
    val title: String?
    val selectValue: Int?
    val iconResId: Int?
    val subRoutes: List<String>
}

sealed class MainRoute(
    override val routeName: String? = null,
    override val title: String? = null,
    override val selectValue: Int? = null,
    override val iconResId: Int? = null,
    override val subRoutes: List<String> = emptyList()
): Route {
    data object Main: MainRoute("Main", "기록", 1, iconResId = R.drawable.edit_06)
    data object Alert: MainRoute("Alert", "알람", 2, iconResId = R.drawable.alarm)
    data object AnalysisScreen: MainRoute("Analysis", "분석", 3, iconResId = R.drawable.baseline_bar_chart_24)
    data object MyPage: MainRoute("MyPage", "마이페이지", 4, iconResId = R.drawable.user, subRoutes = listOf("CreateUser", "CustomerServiceCenter", "CloudService"))

    object CreateUser: MainRoute("CreateUser", "유저생성")
    object CustomerServiceCenter: MainRoute("CustomerServiceCenter", "고객센터")
    object CloudService: MainRoute("CloudService", "클라우드")
}

sealed class MyPageRoute(
    override val routeName: String? = null,
    override val title: String? = null,
    override val selectValue: Int? = null,
    override val iconResId: Int? = null,
    override val subRoutes: List<String> = emptyList()
): Route {
    data object SelectView: MyPageRoute("SelectView", "선택뷰")
    data object CreateUser: MyPageRoute("CreateUser", "유저생성")
    data object CustomerServiceCenter: MyPageRoute("CustomerServiceCenter", "고객센터")
    data object CloudService: MyPageRoute("CloudService", "클라우드")
    data object PremiumSettings: MyPageRoute("PremiumSettings", "프리미엄 설정")
}



// 메인 관련 화면 라우트 액션
class RouteAction<T : Route>(
    private val navHostController: NavHostController,
    private val defaultRoute: String? = null
) {
    // 특정 라우트로 이동
    val navTo: (T) -> Unit = { route ->
        route.routeName?.let { routeName ->
            navHostController.navigate(routeName) {
                // 옵션: defaultRoute가 있는 경우 스택 관리
                defaultRoute?.let { default ->
                    popUpTo(default) { saveState = true }
                }
                launchSingleTop = true
                restoreState = true
            }
        }
    }

    // 특정 라우트로 이동 (팝업 포함)
    val navToWithPopUp: (T) -> Unit = { route ->
        route.routeName?.let { routeName ->
            navHostController.navigate(routeName) {
                popUpTo(routeName) { inclusive = true }
            }
        }
    }

    // 뒤로 가기
    val goBack: () -> Unit = {
        navHostController.navigateUp()
    }

    // 특정 라우트로 이동하며 이전 스택 모두 제거
    val navToClearStack: (T) -> Unit = { route ->
        route.routeName?.let { routeName ->
            navHostController.navigate(routeName) {
                popUpTo(0) { inclusive = true }
            }
        }
    }

    // 중첩 라우트로 이동
    val navToNested: (T, String) -> Unit = { route, nestedRoute ->
        navHostController.navigate("${route.routeName}/$nestedRoute")
    }
}





