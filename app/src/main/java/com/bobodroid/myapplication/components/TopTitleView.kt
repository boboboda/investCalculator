package com.bobodroid.myapplication.components

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.sharp.KeyboardArrowLeft
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
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

@Composable
fun TopTitleButton(sharedViewModel: SharedViewModel) {

    val changeMoney = sharedViewModel.changeMoney.collectAsState()

    val mainTitle = when(changeMoney.value) {
        1-> {"달러"}
        2-> {"엔화"}
        3-> {"원화"}
        else -> {"원화"}
    }


    Row(modifier = Modifier,
        verticalAlignment = Alignment.CenterVertically)
    {

        CardIconButton(imageVector = Icons.Filled.KeyboardArrowLeft,
            onClicked = {
                        if(changeMoney.value > 1) {
                            sharedViewModel.changeMoney.value = changeMoney.value - 1
                        } else {
                            sharedViewModel.changeMoney.value = 3
                        }
            },
            modifier = Modifier,
            buttonColor = TopButtonColor)

        Text(text = mainTitle, fontSize = 25.sp)

        CardIconButton(imageVector = Icons.Filled.KeyboardArrowRight,
            onClicked = {
                if(changeMoney.value < 3) {
                    sharedViewModel.changeMoney.value = changeMoney.value + 1
                } else {
                    sharedViewModel.changeMoney.value = 1
                }
            },
            modifier = Modifier,
            buttonColor = TopButtonColor)

    }



//    Column(
//        modifier = Modifier
//            .background(Color.White)
//    ) {
//
//
//        LazyVerticalGrid(
//            modifier = Modifier.padding(start = 20.dp, end = 20.dp),
//            columns = GridCells.Fixed(3),
//            horizontalArrangement = Arrangement.spacedBy(20.dp),
//            verticalArrangement = Arrangement.spacedBy(8.dp),
//            content = {
//
//                item(span = {
//                    GridItemSpan(1)
//                }
//                ) {
//                    TopTitleView(
//                        "달러",
//                        1,
//                        selectedId = changeMoney.value,
//                        selectAction = {
//                            sharedViewModel.changeMoney.value = it
//                        })
//                }
//
//                item(span = {
//                    GridItemSpan(1)
//                }
//                ) {
//                    TopTitleView(
//                        "엔화",
//                        2,
//                        selectedId = changeMoney.value,
//                        selectAction = {
//                            sharedViewModel.changeMoney.value = it
//                        })
//                }
//
//                item(span = {
//                    GridItemSpan(1)
//                }
//                ) {
//                    TopTitleView(
//                        "원화",
//                        3,
//                        selectedId = changeMoney.value,
//                        selectAction = {
//                            sharedViewModel.changeMoney.value = it
//                        })
//                }
//            })
//    }
}




