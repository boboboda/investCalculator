package com.bobodroid.myapplication

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material.Scaffold
import androidx.compose.material.rememberScaffoldState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.bobodroid.myapplication.components.BannerAd
import com.bobodroid.myapplication.components.MainBottomBar
import com.bobodroid.myapplication.components.MainTopBar
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

            allViewModel.recentRateHotListener { recentRate->
                Log.d(TAG, "실시간 데이터 수신 ${recentRate}")
                dollarViewModel.beforeCalculateProfit(recentRate)
                dollarViewModel.requestRate(recentRate)

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


    val scaffoldState = rememberScaffoldState()

    val investNavController = rememberNavController()
    val investRouteAction = remember(investNavController) {
        InvestRouteAction(investNavController)
    }

    val mainBackStack = investNavController.currentBackStackEntryAsState()


    Scaffold(
        scaffoldState = scaffoldState,
        topBar = { MainTopBar() },
        bottomBar = {
            MainBottomBar(
                investRouteAction,
                mainBackStack.value,
                allViewModel = allViewModel
            )
        }) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    top = it.calculateTopPadding(),
                    bottom = it.calculateBottomPadding()
                )
        ) {

            Column(Modifier.weight(1f)) {


                InvestNavHost(
                    investNavController = investNavController,
                    dollarViewModel = dollarViewModel,
                    yenViewModel = yenViewModel,
                    wonViewModel = wonViewModel,
                    routeAction = investRouteAction,
                    allViewModel = allViewModel
                )
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally) {
                BannerAd()
            }

            Spacer(modifier = Modifier.height(5.dp))
        }


    }


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



