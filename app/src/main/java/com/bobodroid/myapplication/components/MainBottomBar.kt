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
import com.bobodroid.myapplication.routes.InvestRoute
import com.bobodroid.myapplication.routes.InvestRouteAction
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

    val titles = listOf<String>("달러", "엔화", "원화")

    val title = remember { mutableStateOf(titles[0]) }

    TopButton(
        "${title.value}",
        selectAction = {
            when(title.value) {
                "달러" -> {
                    title.value = titles[1]
                    allViewModel.changeMoney.value = 2
                }
                "엔화" -> {
                    title.value = titles[2]
                    allViewModel.changeMoney.value = 3
                }
                "원화" -> {
                    title.value = titles[0]
                    allViewModel.changeMoney.value = 1
                }
                else -> {
                    title.value = titles[0]
                    allViewModel.changeMoney.value = 1
                }
            }
        })
}