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
import com.bobodroid.myapplication.components.MainTopBar
import com.bobodroid.myapplication.models.viewmodels.AllViewModel
import com.bobodroid.myapplication.models.viewmodels.DollarViewModel
import com.bobodroid.myapplication.models.viewmodels.SharedViewModel
import com.bobodroid.myapplication.models.viewmodels.WonViewModel
import com.bobodroid.myapplication.models.viewmodels.YenViewModel
import com.bobodroid.myapplication.routes.*
import com.bobodroid.myapplication.screens.*
import com.bobodroid.myapplication.ui.theme.InverstCalculatorTheme
import com.google.android.gms.ads.MobileAds
import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
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
                MainScreen(
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
fun MainScreen(
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
            InverstAppScreen(dollarViewModel, yenViewModel, wonViewModel ,sharedViewModel, allViewModel)
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
fun InverstAppScreen(
    dollarViewModel: DollarViewModel,
    yenViewModel: YenViewModel,
    wonViewModel: WonViewModel,
    sharedViewModel: SharedViewModel,
    allViewModel: AllViewModel) {


    val changeMoney: State<Int> = sharedViewModel.changeMoney.collectAsState()


    val dollarNavController = rememberNavController()
    val dollarRouteAction = remember(dollarNavController) {
        DollarRouteAction(dollarNavController)
    }

    val yenNavController = rememberNavController()
    val yenRouteAction = remember(yenNavController) {
        YenRouteAction(yenNavController)
    }

    val wonNavController = rememberNavController()
    val wonRouteAction = remember(wonNavController) {
        WonRouteAction(wonNavController)
    }






    when(changeMoney.value) {
        1 ->
        { DollarNavHost(
            dollarNavController = dollarNavController,
            dollarViewModel = dollarViewModel,
            routeAction = dollarRouteAction,
            sharedViewModel = sharedViewModel,
            allViewModel = allViewModel)
        }
        2 -> { YenNavHost(
            yenNavController = yenNavController ,
            yenViewModel = yenViewModel,
            routeAction = yenRouteAction,
            sharedViewModel = sharedViewModel,
            allViewModel = allViewModel)
        }
        3 -> { WonNavHost(
            wonNavController = wonNavController,
            wonViewModel = wonViewModel,
            routeAction = wonRouteAction,
            sharedViewModel = sharedViewModel
        )  }
    }



}


@Composable
fun DollarNavHost(
    dollarNavController: NavHostController,
    startRouter: DollarRoute = DollarRoute.BUYRECORD,
    dollarViewModel: DollarViewModel,
    routeAction: DollarRouteAction,
    sharedViewModel: SharedViewModel,
    allViewModel: AllViewModel
) {
   NavHost(navController = dollarNavController, startDestination = startRouter.routeName) {
       composable(DollarRoute.BUYRECORD.routeName) {
           DollarMainScreen(dollarViewModel, routeAction, sharedViewModel, allViewModel)
       }
       composable(DollarRoute.BUY.routeName) {
           DollarInvestScreen(dollarViewModel, routeAction)
       }
   }
}


@Composable
fun YenNavHost(
    yenNavController: NavHostController,
    startRouter: YenRoute = YenRoute.BUYRECORD,
    yenViewModel: YenViewModel,
    routeAction: YenRouteAction,
    sharedViewModel: SharedViewModel,
    allViewModel: AllViewModel
) {
    NavHost(navController = yenNavController, startDestination = startRouter.routeName) {
        composable(YenRoute.BUYRECORD.routeName) {
            YenMainScreen(yenViewModel, routeAction, sharedViewModel, allViewModel)
        }
        composable(YenRoute.BUY.routeName) {
            YenInvestScreen(yenViewModel, routeAction)
        }
    }
}

@Composable
fun WonNavHost(
    wonNavController: NavHostController,
    startRouter: WonRoute = WonRoute.BUYRECORD,
    wonViewModel: WonViewModel,
    routeAction: WonRouteAction,
    sharedViewModel: SharedViewModel
) {
    NavHost(navController = wonNavController, startDestination = startRouter.routeName) {
        composable(WonRoute.BUYRECORD.routeName) {
            WonMainScreen(wonViewModel, routeAction, sharedViewModel)
        }
        composable(YenRoute.BUY.routeName) {
            WonInvestScreen(wonViewModel, routeAction)
        }
    }
}

