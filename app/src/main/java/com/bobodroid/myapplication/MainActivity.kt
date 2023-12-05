package com.bobodroid.myapplication

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.Surface
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.bobodroid.myapplication.components.BannerAd
import com.bobodroid.myapplication.components.IconButton
import com.bobodroid.myapplication.components.MainTopBar
import com.bobodroid.myapplication.components.TopTitleButton
import com.bobodroid.myapplication.models.viewmodels.AllViewModel
import com.bobodroid.myapplication.models.viewmodels.DollarViewModel
import com.bobodroid.myapplication.models.viewmodels.SharedViewModel
import com.bobodroid.myapplication.models.viewmodels.WonViewModel
import com.bobodroid.myapplication.models.viewmodels.YenViewModel
import com.bobodroid.myapplication.routes.*
import com.bobodroid.myapplication.screens.*
import com.bobodroid.myapplication.ui.theme.InverstCalculatorTheme
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

    private val sharedViewModel: SharedViewModel by viewModels()

    private val wonViewModel: WonViewModel by viewModels()

    private val allViewModel: AllViewModel by viewModels()



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MobileAds.initialize(this)
        setContent {
            AppScreen(
                    dollarViewModel,
                    yenViewModel,
                    wonViewModel,
                    sharedViewModel,
                    allViewModel)

        }
    }
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppScreen(
    dollarViewModel: DollarViewModel,
    yenViewModel: YenViewModel,
    wonViewModel: WonViewModel,
    sharedViewModel: SharedViewModel,
    allViewModel: AllViewModel
) {
    Column(
        modifier = Modifier
            .fillMaxSize()) {
        MainTopBar()
        Spacer(modifier = Modifier.height(10.dp))

        Column(modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Bottom) {

            InvestAppScreen(dollarViewModel, yenViewModel, wonViewModel ,sharedViewModel, allViewModel)
        }
        Column(
            modifier = Modifier
                .fillMaxWidth(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally) {
            BannerAd()
        }

    }


}


@Composable
fun InvestAppScreen(
    dollarViewModel: DollarViewModel,
    yenViewModel: YenViewModel,
    wonViewModel: WonViewModel,
    sharedViewModel: SharedViewModel,
    allViewModel: AllViewModel) {


    val changeMoney: State<Int> = sharedViewModel.changeMoney.collectAsState()


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
        sharedViewModel = sharedViewModel,
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
    sharedViewModel: SharedViewModel,
    allViewModel: AllViewModel
) {
   NavHost(navController = investNavController, startDestination = startRouter.routeName) {
       composable(InvestRoute.MAIN.routeName) {
           MainScreen(
               dollarViewModel = dollarViewModel,
               yenViewModel = yenViewModel,
               wonViewModel = wonViewModel,
               routeAction = routeAction,
               sharedViewModel = sharedViewModel,
               allViewModel = allViewModel
           )
       }
       composable(InvestRoute.DOLLAR_BUY.routeName) {
           DollarInvestScreen(dollarViewModel = dollarViewModel, routeAction = routeAction, sharedViewModel)
       }

       composable(InvestRoute.YEN_BUY.routeName) {
           YenInvestScreen(yenViewModel = yenViewModel, routeAction = routeAction, sharedViewModel)
       }

       composable(InvestRoute.WON_BUY.routeName) {
           WonInvestScreen(wonViewModel = wonViewModel, routeAction = routeAction, sharedViewModel)
       }
   }
}



