package com.bobodroid.myapplication.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import com.bobodroid.myapplication.models.viewmodels.AllViewModel
import com.bobodroid.myapplication.ui.theme.TopButtonColor

@Composable
fun TopTitleButton(allViewModel: AllViewModel) {

    val changeMoney = allViewModel.changeMoney.collectAsState()

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
                            allViewModel.changeMoney.value = changeMoney.value - 1
                        } else {
                            allViewModel.changeMoney.value = 3
                        }
            },
            modifier = Modifier,
            buttonColor = TopButtonColor)

        Text(text = mainTitle, fontSize = 25.sp)

        CardIconButton(imageVector = Icons.Filled.KeyboardArrowRight,
            onClicked = {
                if(changeMoney.value < 3) {
                    allViewModel.changeMoney.value = changeMoney.value + 1
                } else {
                    allViewModel.changeMoney.value = 1
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




