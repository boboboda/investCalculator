package com.bobodroid.myapplication.routes

import androidx.navigation.NavHostController


enum class YenRoute(val routeName: String) {

    BUY("BUY"),
    BUYRECORD("BUYRECORD")
}

class YenRouteAction(navHostController: NavHostController) {


    //특정 라우트로 이동
    val navTo: (YenRoute) -> Unit = { yenRoute ->
        navHostController.navigate(yenRoute.routeName) {
            popUpTo(yenRoute.routeName){ inclusive = true }

        }
    }

    // 뒤로가기 이동
    val goBack: () -> Unit = {
        navHostController.navigateUp()
    }

}