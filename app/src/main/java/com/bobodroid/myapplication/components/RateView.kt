package com.bobodroid.myapplication.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun RateView(
    title: String,
    subTitle: String? = "",
    recentRate: String,
    subRecentRate: String? = "",
    createAt: String
) {

    val useTitle = if (subTitle == "") {
        "${title}: ${recentRate}"
    } else {
        "${title}: ${recentRate} | ${subTitle}: ${subRecentRate}"
    }

    Column(
        modifier = Modifier
            .wrapContentHeight()
            .padding(horizontal = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Row(
            modifier = Modifier
                .wrapContentSize(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {



            AutoSizeText(value = useTitle, minFontSize = 10.sp, color = Color.Black, fontSize = 20.sp, maxLines = 1, fontWeight = FontWeight.Bold)
        }

        AutoSizeText(value = "업데이트된 환율: $createAt", minFontSize = 10.sp, color = Color.Black, fontSize = 15.sp, maxLines = 1)
    }

}