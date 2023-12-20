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
import com.bobodroid.myapplication.models.viewmodels.DollarViewModel
import com.bobodroid.myapplication.models.viewmodels.WonViewModel
import com.bobodroid.myapplication.models.viewmodels.YenViewModel
import com.bobodroid.myapplication.ui.theme.TopButtonColor

@Composable
fun TopTitleButton(allViewModel: AllViewModel) {

    val changeMoney = allViewModel.changeMoney.collectAsState()




    Row(modifier = Modifier,
        verticalAlignment = Alignment.CenterVertically)
    {
        val mainTitle = when(changeMoney.value) {
            1-> {"달러"}
            2-> {"엔화"}
            3-> {"원화"}
            else -> {"달러"}
        }

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
}




