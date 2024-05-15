package com.bobodroid.myapplication.routes

import androidx.navigation.NavHostController


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
    open val iconResId: Int? = null
) {
    object Main: MainRoute("Main", "홈", 1 )
    object Alert: MainRoute("Data","데이터",2)
    object MyPage: MainRoute("Draw","뽑기", 3)

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