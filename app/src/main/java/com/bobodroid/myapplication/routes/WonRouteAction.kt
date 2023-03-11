package com.bobodroid.myapplication.routes

import androidx.navigation.NavHostController


enum class WonRoute(val routeName: String) {

    BUY("BUY"),
    BUYRECORD("BUYRECORD")
}

class WonRouteAction(navHostController: NavHostController) {


    //특정 라우트로 이동
    val navTo: (WonRoute) -> Unit = { wonRoute ->
        navHostController.navigate(wonRoute.routeName) {
            popUpTo(wonRoute.routeName){ inclusive = true }

        }
    }

    // 뒤로가기 이동
    val goBack: () -> Unit = {
        navHostController.navigateUp()
    }

}