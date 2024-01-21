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