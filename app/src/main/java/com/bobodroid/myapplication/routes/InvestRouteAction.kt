package com.bobodroid.myapplication.routes

import androidx.navigation.NavHostController
import com.bobodroid.myapplication.R


enum class InvestRoute(val route: String, val routName: String) {

    MAIN(route = "MAIN", routName = "메인"),
    DOLLAR_BUY("DOLLAR_BUY", routName = "달러"),
    YEN_BUY("YEN_BUY", "엔화"),
    WON_BUY("WON_BUY", "원화")
}



class InvestRouteAction(navHostController: NavHostController) {


    //특정 라우트로 이동
    val navTo: (InvestRoute) -> Unit = { investRoute ->
        navHostController.navigate(investRoute.route!!) {
            popUpTo(investRoute.route!!){ inclusive = true }

        }
    }

    // 뒤로가기 이동
    val goBack: () -> Unit = {
        navHostController.navigateUp()
    }

}


sealed class MainRoute(
    open val routeName: String? = null,
    open val title: String? = null,
    open val selectValue: Int? = null,
    open val iconResId: Int? = null,
    open val subRoutes: List<String> = emptyList()
) {
    object Main: MainRoute("Main", "홈", 1, iconResId = R.drawable.edit_06)
    object Alert: MainRoute("Alert", "알람", 2, iconResId = R.drawable.alarm)
    object AnalysisScreen: MainRoute("Analysis", "분석", 3, iconResId = R.drawable.baseline_bar_chart_24)
    object MyPage: MainRoute("MyPage", "마이페이지", 4, iconResId = R.drawable.user, subRoutes = listOf("CreateUser", "CustomerServiceCenter", "CloudService"))

    object CreateUser: MainRoute("CreateUser", "유저생성")
    object CustomerServiceCenter: MainRoute("CustomerServiceCenter", "고객센터")
    object CloudService: MainRoute("CloudService", "클라우드")
    object Chart: MainRoute("Chart", "차트")



//    object Notice: MainRoute("Notice", "게시판")
//    object Write: MainRoute("Write", "글쓰기")
//    object Post: MainRoute("Post", "게시글")
//    object Payment: MainRoute("Payment", "구독")


}



// 메인 관련 화면 라우트 액션
class MainRouteAction(navHostController: NavHostController) {

    //특정 라우트로 이동
    val navTo: (MainRoute) -> Unit = { Route ->
        navHostController.navigate(Route.routeName!!) {
            popUpTo(Route.routeName!!){ inclusive = true }
        }
    }

    // 뒤로 가기 이동
    val goBack: () -> Unit = {
        navHostController.navigateUp()
    }

}