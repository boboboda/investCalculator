package com.bobodroid.myapplication.components.mainComponents

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bobodroid.myapplication.extensions.toLongWon
import com.bobodroid.myapplication.ui.theme.BottomSheetSelectedColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BottomSheetNumberField(
    title: String,
    selectedState: Boolean,
    modifier: Modifier,
    onClicked: () -> Unit
) {

    val formatTitle = if(title == "") {
        "매수금(원)을 입력해주세요"
    } else {
        title.toLong().toLongWon()
    }

    val cardColor = if(selectedState) CardDefaults.cardColors(BottomSheetSelectedColor) else CardDefaults.cardColors(
        Color.White)

    Card(
        modifier = modifier
            .padding(10.dp)
            .height(45.dp),
        border = BorderStroke(1.dp, Color.Black),
        colors = cardColor,

        onClick = onClicked
    ) {
        Text(
            text = formatTitle,
            fontSize = 18.sp,
            modifier = Modifier
                .padding(3.dp, top = 10.dp)
                .fillMaxWidth(),
            textAlign = TextAlign.Center
        )
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BottomSheetRateNumberField(
    title: String,
    modifier: Modifier,
    selectedState: Boolean,
    placeholder: String,
    onClicked: () -> Unit
) {

    val formatTile = if (title == "") placeholder else title

    val cardColor = if(selectedState) CardDefaults.cardColors(BottomSheetSelectedColor) else CardDefaults.cardColors(Color.White)


    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(45.dp),
        border = BorderStroke(1.dp, Color.Black),
        colors = cardColor,

        onClick = onClicked
    ) {
        Text(
            text = formatTile,
            fontSize = 18.sp,
            modifier = Modifier
                .padding(3.dp, top = 10.dp)
                .fillMaxWidth(),
            textAlign = TextAlign.Center
        )
    }
}