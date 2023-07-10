package com.bobodroid.myapplication.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.contentValuesOf
import com.bobodroid.myapplication.R
import com.bobodroid.myapplication.ui.theme.BackgroundColor
import com.bobodroid.myapplication.ui.theme.TopBarColor

@Composable
fun MainTopBar() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(40.dp)
            .background(TopBarColor),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center) {
        Image(
            modifier = Modifier
                .clip(CircleShape)
                .padding(5.dp),
            painter = painterResource(id = R.drawable.ic_icon),
            contentDescription = "")

        Spacer(modifier = Modifier.width(10.dp))
        AutoSizeText(value = "달러 기록 (엔화 , 원화)",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            minFontSize = 10.sp,
            color = Color.Black)
    }
}