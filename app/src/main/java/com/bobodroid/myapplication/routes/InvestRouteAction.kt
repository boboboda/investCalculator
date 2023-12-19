package com.bobodroid.myapplication.routes

import androidx.navigation.NavHostController
import com.bobodroid.myapplication.R


sealed class InvestRoute(open val routeName: String? = null,
                         open val title: String? = null,
                         open val selectValue: Int? = null,
                         open val iconResId: Int? = null) {

    object MAIN: InvestRoute("BUY", "달러", 1 , )
    object YEN_BUY: InvestRoute("YEN_BUY","엔화", 2, )
    object WON_BUY: InvestRoute("WON_BUY","원화", 3, )
    object DOLLAR_BUY: InvestRoute("DOLLAR_BUY","",4, )
}


class InvestRouteAction(navHostController: NavHostController) {





    //특정 라우트로 이동
    val navTo: (InvestRoute) -> Unit = { investRoute ->
        navHostController.navigate(investRoute.routeName!!) {
            popUpTo(investRoute.routeName!!){ inclusive = true }

        }
    }

    // 뒤로가기 이동
    val goBack: () -> Unit = {
        navHostController.navigateUp()
    }

}