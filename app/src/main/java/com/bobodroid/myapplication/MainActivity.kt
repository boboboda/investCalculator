package com.bobodroid.myapplication

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.bobodroid.myapplication.components.MainTopBar
import com.bobodroid.myapplication.components.admobs.loadInterstitial
import com.bobodroid.myapplication.models.viewmodels.AllViewModel
import com.bobodroid.myapplication.models.viewmodels.DollarViewModel
import com.bobodroid.myapplication.models.viewmodels.WonViewModel
import com.bobodroid.myapplication.models.viewmodels.YenViewModel
import com.bobodroid.myapplication.routes.*
import com.bobodroid.myapplication.screens.*
import com.google.android.gms.ads.MobileAds
//import com.google.android.gms.ads.MobileAds
import dagger.hilt.android.AndroidEntryPoint
import java.util.*


@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    companion object {
        const val TAG = "메인"
    }

    private val dollarViewModel: DollarViewModel by viewModels()

    private val yenViewModel: YenViewModel by viewModels()

    private val wonViewModel: WonViewModel by viewModels()

    private val allViewModel: AllViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MobileAds.initialize(this)
        setContent {

//            allViewModel.deleteLocalUser()

            loadInterstitial(this)

            allViewModel.recentRateHotListener { recentRate, localData->
                Log.d(TAG, "실시간 데이터 수신 ${recentRate}")

                dollarViewModel.requestRate(recentRate, localData)

                yenViewModel.requestRate(recentRate, localData)

                wonViewModel.requestRate(recentRate, localData)



            }

            AppScreen(
                    dollarViewModel,
                    yenViewModel,
                    wonViewModel,
                    allViewModel)

        }
    }
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun AppScreen(
    dollarViewModel: DollarViewModel,
    yenViewModel: YenViewModel,
    wonViewModel: WonViewModel,
    allViewModel: AllViewModel
) {
    Column(
        modifier = Modifier
            .fillMaxSize()) {
        MainTopBar()

        Column(modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Bottom) {

            InvestAppScreen(dollarViewModel, yenViewModel, wonViewModel , allViewModel)
        }

    }


}


@Composable
fun InvestAppScreen(
    dollarViewModel: DollarViewModel,
    yenViewModel: YenViewModel,
    wonViewModel: WonViewModel,
    allViewModel: AllViewModel) {


    val investNavController = rememberNavController()
    val investRouteAction = remember(investNavController) {
        InvestRouteAction(investNavController)
    }

    InvestNavHost(
        investNavController = investNavController,
        dollarViewModel = dollarViewModel,
        yenViewModel = yenViewModel,
        wonViewModel = wonViewModel,
        routeAction = investRouteAction,
        allViewModel = allViewModel
    )




}


@Composable
fun InvestNavHost(
    investNavController: NavHostController,
    startRouter: InvestRoute = InvestRoute.MAIN,
    dollarViewModel: DollarViewModel,
    yenViewModel: YenViewModel,
    wonViewModel: WonViewModel,
    routeAction: InvestRouteAction,
    allViewModel: AllViewModel
) {
   NavHost(navController = investNavController, startDestination = startRouter.routeName!!) {
       composable(InvestRoute.MAIN.routeName!!) {
           MainScreen(
               dollarViewModel = dollarViewModel,
               yenViewModel = yenViewModel,
               wonViewModel = wonViewModel,
               routeAction = routeAction,
               allViewModel = allViewModel
           )
       }
       composable(InvestRoute.DOLLAR_BUY.routeName!!) {
           DollarInvestScreen(dollarViewModel = dollarViewModel, routeAction = routeAction, allViewModel)
       }

       composable(InvestRoute.YEN_BUY.routeName!!) {
           YenInvestScreen(yenViewModel = yenViewModel, routeAction = routeAction, allViewModel)
       }

       composable(InvestRoute.WON_BUY.routeName!!) {
           WonInvestScreen(wonViewModel = wonViewModel, routeAction = routeAction, allViewModel)
       }
   }
}



