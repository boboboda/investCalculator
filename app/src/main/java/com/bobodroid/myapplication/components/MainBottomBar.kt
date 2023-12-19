package com.bobodroid.myapplication.components

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.Icon
import androidx.compose.material.SnackbarHostState
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.navigation.NavBackStackEntry
import com.bobodroid.myapplication.models.viewmodels.AllViewModel
import com.bobodroid.myapplication.routes.InvestRoute
import com.bobodroid.myapplication.routes.InvestRouteAction

@SuppressLint("SuspiciousIndentation")
@Composable
fun MainBottomBar(
    investRouteAction: InvestRouteAction,
    mainRouteBackStack: NavBackStackEntry?,
    allViewModel: AllViewModel
) {

    val nowBottomValue = allViewModel.changeMoney.collectAsState()

    val snackBarHostState = remember { SnackbarHostState() }

    val coroutineScope = rememberCoroutineScope()



    BottomNavigation(
        modifier = Modifier.fillMaxWidth()
    ) {
        InvestRoute.MAIN.let {
            BottomNavigationItem(
                modifier = Modifier.background(Color.White),
                label = { Text(text = it.title!!) },
                icon = {
                    it.iconResId?.let { iconId ->
                        Icon(painter = painterResource(iconId), contentDescription = it.title)
                    }
                },
                selectedContentColor = Color.Black,
                unselectedContentColor = Color.LightGray,
                selected = (mainRouteBackStack?.destination?.route) == it.routeName,
                onClick = {
                    investRouteAction.navTo(it)
                    allViewModel.changeMoney.value = it.selectValue!!
                },
            )
        }

        InvestRoute.YEN_BUY.let {
            BottomNavigationItem(modifier = Modifier.background(Color.White),
                label = { Text(text = it.title!!) },
                icon = {
                    it.iconResId?.let { iconId ->
                        Icon(painter = painterResource(iconId), contentDescription = it.title)
                    }
                },
                selectedContentColor = Color.Black,
                unselectedContentColor = Color.LightGray,
                selected = (mainRouteBackStack?.destination?.route) == it.routeName,
                onClick = {
                    investRouteAction.navTo(it)
                    allViewModel.changeMoney.value = it.selectValue!!
                }

            )
        }

        InvestRoute.WON_BUY.let {
            it.selectValue
            BottomNavigationItem(modifier = Modifier.background(Color.White),
                label = { Text(text = it.title!!) },
                icon = {
                    it.iconResId?.let { iconId ->
                        Icon(painter = painterResource(iconId), contentDescription = it.title)
                    }
                },
                selectedContentColor = Color.Black,
                unselectedContentColor = Color.LightGray,
                selected = (mainRouteBackStack?.destination?.route) == it.routeName,
                onClick = {
                    investRouteAction.navTo(it)
                    allViewModel.changeMoney.value = it.selectValue!!
                }
            )
        }
    }
}