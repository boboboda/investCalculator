package com.bobodroid.myapplication.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp


@Composable
fun RecordTextView(recordText: String,
                   TextHeight: Dp,
                   recordFontSize: Int,
                   lineAlignment: Float, bottonPpaing: Dp, color: Color) {
    Box(
        modifier = Modifier
            .width(86.dp)
            .height(TextHeight)
            .border(BorderStroke(1.dp, color = Color.Black))
            .padding(0.dp),
        contentAlignment = Alignment.Center
            ) {
        AutoSizeText(
            value = "$recordText",
            fontSize = recordFontSize.sp,
            modifier = Modifier
                .padding(bottom = bottonPpaing),
            color = color,
            maxLines = 2,
            minFontSize = 10.sp)
    }
}



