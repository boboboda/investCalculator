package com.bobodroid.myapplication.routes

import androidx.navigation.NavHostController


enum class DollarRoute(val routeName: String) {

    BUY("BUY"),
    BUYRECORD("BUYRECORD")
}

class DollarRouteAction(navHostController: NavHostController) {


    //특정 라우트로 이동
    val navTo: (DollarRoute) -> Unit = { dollarRoute ->
        navHostController.navigate(dollarRoute.routeName) {
            popUpTo(dollarRoute.routeName){ inclusive = true }

        }
    }

    // 뒤로가기 이동
    val goBack: () -> Unit = {
        navHostController.navigateUp()
    }

}