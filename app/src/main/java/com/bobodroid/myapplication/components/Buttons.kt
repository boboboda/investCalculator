@file:OptIn(ExperimentalMaterialApi::class)

package com.bobodroid.myapplication.components

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.bobodroid.myapplication.MainActivity
import com.bobodroid.myapplication.ui.theme.TopButtonColor
import com.bobodroid.myapplication.ui.theme.TopButtonInColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Buttons(label: String,
            onClicked:(()->Unit)?,
            color: Color, fontColor: Color,
            enabled: Boolean = true, modifier: Modifier,
            fontSize: Int) {
    Button(
        colors = ButtonDefaults.buttonColors(
            containerColor = color,
            contentColor = fontColor,
            disabledContentColor = Color.White,
            disabledContainerColor = Color.Gray),
        modifier = modifier,
        enabled = enabled,
        onClick = {onClicked?.invoke()})
    {
        Text(text = label, fontSize = fontSize.sp, textAlign = TextAlign.Center, modifier = Modifier.padding(0.dp))
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateButtonView(mainText: String,
                   id: Int,
                   selectedId: Int,
                   selectAction: (Int) -> Unit) {

    var currentCardId : Int = id

    var color = if (selectedId == currentCardId) TopButtonInColor else TopButtonColor

    Card(colors = CardDefaults.cardColors(color),
        elevation = CardDefaults.cardElevation(8.dp),
        modifier = Modifier
            .padding(top = 5.dp, bottom = 10.dp)
            .width(60.dp),
        onClick = {
            Log.d(MainActivity.TAG, "클릭되었습니다.")
            selectAction(currentCardId)
        }) {
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


@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun MoneyChButtonView(mainText: String,
                      id: Int,
                      selectedId: Int,
                      selectAction: (Int) -> Unit) {

    var currentCardId : Int = id

    var color = if (selectedId == currentCardId) TopButtonInColor else TopButtonColor

    androidx.compose.material.Card(
        backgroundColor = color ,
        elevation = 8.dp,
        modifier = Modifier
            .padding(top = 5.dp, bottom = 10.dp)
            .width(60.dp)
            .height(50.dp),
        onClick = {
            Log.d(MainActivity.TAG, "클릭되었습니다.")
            selectAction(currentCardId)
        }) {
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
