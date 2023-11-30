package com.bobodroid.myapplication.components

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bobodroid.myapplication.MainActivity
import com.bobodroid.myapplication.models.viewmodels.SharedViewModel
import com.bobodroid.myapplication.ui.theme.TopButtonColor
import com.bobodroid.myapplication.ui.theme.TopButtonInColor





@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopTitleView(mainText: String,
                 id: Int,
                 selectedId: Int,
                 selectAction: (Int) -> Unit) {

    var currentCardId : Int = id

    var color = if (selectedId == currentCardId) TopButtonInColor else TopButtonColor

    Card(
        colors = CardDefaults.cardColors(color),
        elevation = CardDefaults.cardElevation(8.dp),
        modifier = Modifier
            .height(70.dp)
            .padding(7.dp)
            .fillMaxWidth(),
        onClick = {
            Log.d(MainActivity.TAG, "클릭되었습니다.")
            selectAction(currentCardId) }
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
fun TopTitleButton(sharedViewModel: SharedViewModel) {

    val changeMoney = sharedViewModel.changeMoney.collectAsState()
    Column(
        modifier = Modifier
            .background(Color.White)
    ) {
        LazyVerticalGrid(
            modifier = Modifier.padding(start = 20.dp, end = 20.dp),
            columns = GridCells.Fixed(3),
            horizontalArrangement = Arrangement.spacedBy(20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            content = {

                item(span = {
                    GridItemSpan(1)
                }
                ) {
                    TopTitleView(
                        "달러투자",
                        1,
                        selectedId = changeMoney.value,
                        selectAction = {
                            sharedViewModel.changeMoney.value = it
                        })
                }

                item(span = {
                    GridItemSpan(1)
                }
                ) {
                    TopTitleView(
                        "엔화투자",
                        2,
                        selectedId = changeMoney.value,
                        selectAction = {
                            sharedViewModel.changeMoney.value = it
                        })
                }

                item(span = {
                    GridItemSpan(1)
                }
                ) {
                    TopTitleView(
                        "원화투자",
                        3,
                        selectedId = changeMoney.value,
                        selectAction = {
                            sharedViewModel.changeMoney.value = it
                        })
                }
            })
    }
}




