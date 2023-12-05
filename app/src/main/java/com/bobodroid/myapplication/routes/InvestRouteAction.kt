package com.bobodroid.myapplication.routes

import androidx.navigation.NavHostController


enum class InvestRoute(val routeName: String) {

    MAIN("BUY"),
    DOLLAR_BUY("DOLLAR_BUY"),
    YEN_BUY("YEN_BUY"),
    WON_BUY("WON_BUY")
}

class InvestRouteAction(navHostController: NavHostController) {


    //특정 라우트로 이동
    val navTo: (InvestRoute) -> Unit = { investRoute ->
        navHostController.navigate(investRoute.routeName) {
            popUpTo(investRoute.routeName){ inclusive = true }

        }
    }

    // 뒤로가기 이동
    val goBack: () -> Unit = {
        navHostController.navigateUp()
    }

}