package com.bobodroid.myapplication.components

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material3.Card
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.SnackbarDuration
import androidx.compose.material.SnackbarHostState
import androidx.compose.material.Text
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavBackStackEntry
import com.bobodroid.myapplication.MainActivity.Companion.TAG
import com.bobodroid.myapplication.models.viewmodels.AllViewModel
import com.bobodroid.myapplication.routes.MainRoute
import com.bobodroid.myapplication.routes.MainRouteAction
import com.bobodroid.myapplication.ui.theme.TopButtonColor
import com.bobodroid.myapplication.ui.theme.TopButtonInColor
import kotlinx.coroutines.launch



@OptIn(ExperimentalMaterialApi::class, ExperimentalMaterial3Api::class)
@Composable
fun TopButton(mainText: String,
              selectAction: () -> Unit) {


    Card(
        colors = CardDefaults.cardColors(TopButtonColor),
        elevation = CardDefaults.cardElevation(8.dp),
        modifier = Modifier
            .height(70.dp)
            .padding(7.dp)
            .width(100.dp),
        onClick = {
            Log.d(TAG, "클릭되었습니다.")
            selectAction.invoke() }
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            AutoSizeText(
                value = "$mainText",
                modifier = Modifier,
                fontSize = 18.sp,
                maxLines = 2,
                minFontSize = 10.sp,
                color = Color.Black)
        }
    }
}


@Composable
fun TopButtonView(allViewModel: AllViewModel) {

    val changeMoney = allViewModel.changeMoney.collectAsState()

    val mainTitle = when(changeMoney.value) {
        1-> {"달러"}
        2-> {"엔화"}
        3-> {"원화"}
        else -> {"달러"}
    }
    TopButton(
        "${mainTitle}",
        selectAction = {
            when(changeMoney.value) {
                1 -> {
                    allViewModel.changeMoney.value = 2
                }
                2 -> {
                    allViewModel.changeMoney.value = 3
                }
                3 -> {
                    allViewModel.changeMoney.value = 1
                }
            }
        })
}


@Composable
fun MainBottomBar(
    mainRouteAction: MainRouteAction,
    mainRouteBackStack: NavBackStackEntry?,
    allViewModel: AllViewModel
) {

    val snackBarHostState = remember { SnackbarHostState() }

    val coroutineScope = rememberCoroutineScope()



    BottomNavigation(
        modifier = Modifier.fillMaxWidth()
    ) {
        MainRoute.Main.let {
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
                    mainRouteAction.navTo(it)
                    allViewModel.nowBottomCardValue.value = it.selectValue!!
                },
            )
        }

        MainRoute.Alert.let {
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
                    mainRouteAction.navTo(it)
                    allViewModel.nowBottomCardValue.value = it.selectValue!!
                }

            )
        }

        MainRoute.MyPage.let {
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
                    mainRouteAction.navTo(it)
                    allViewModel.nowBottomCardValue.value = it.selectValue!!
                }
            )
        }

//        SnackbarHost(hostState = snackBarHostState, modifier = Modifier)


    }
}
