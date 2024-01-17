@file:OptIn(ExperimentalMaterialApi::class)

package com.bobodroid.myapplication.components

import android.graphics.drawable.Icon
import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.IconCompat.IconType
import com.bobodroid.myapplication.MainActivity
import com.bobodroid.myapplication.ui.theme.TopButtonColor
import com.bobodroid.myapplication.ui.theme.TopButtonInColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Buttons(
    label: String,
    onClicked: (() -> Unit)?,
    color: Color, fontColor: Color,
    enabled: Boolean = true, modifier: Modifier,
    fontSize: Int
) {
    Button(
        colors = ButtonDefaults.buttonColors(
            containerColor = color,
            contentColor = fontColor,
            disabledContentColor = Color.White,
            disabledContainerColor = Color.Gray
        ),
        modifier = modifier,
        shape = RoundedCornerShape(5.dp),
        enabled = enabled,
        onClick = { onClicked?.invoke() })
    {
        Text(
            text = label,
            fontSize = fontSize.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(0.dp)
        )
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateButtonView(
    mainText: String,
    id: Int,
    selectedId: Int,
    selectAction: (Int) -> Unit
) {

    var currentCardId: Int = id

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
                color = Color.Black
            )
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun MoneyChButtonView(
    mainText: String,
    id: Int,
    selectedId: Int,
    selectAction: (Int) -> Unit
) {

    var currentCardId: Int = id

    var color = if (selectedId == currentCardId) TopButtonInColor else TopButtonColor

    androidx.compose.material.Card(
        backgroundColor = color,
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
                color = Color.Black
            )
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CardButton(
    label: String,
    selectedLabel: String? = null,
    onClicked: (String) -> Unit,
    fontSize: Int,
    modifier: Modifier,
    fontColor: Color,
    buttonColor: Color,
    disableColor: Color? = null
) {

    var cardLabel: String = label


    var color = if (disableColor != null) {
        if (cardLabel == selectedLabel) fontColor else disableColor
    } else {
        fontColor
    }

    Card(colors = CardDefaults.cardColors(buttonColor),
        elevation = CardDefaults.cardElevation(8.dp),
        modifier = modifier,
        border = BorderStroke(1.dp, color),
        shape = RoundedCornerShape(2.dp),
        onClick = {
            onClicked(label)
        }) {
        Row(
            Modifier
                .fillMaxSize(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                AutoSizeText(
                    value = "$label",
                    modifier = Modifier,
                    fontSize = fontSize.sp,
                    maxLines = 1,
                    minFontSize = 10.sp,
                    color = color!!
                )
            }

        }

    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CardTextIconButton(
    label: String,
    onClicked: () -> Unit,
    fontSize: Int,
    modifier: Modifier,
    fontColor: Color,
    buttonColor: Color,
) {
    Card(colors = CardDefaults.cardColors(buttonColor),
        elevation = CardDefaults.cardElevation(8.dp),
        shape = RoundedCornerShape(2.dp),
        modifier = modifier,
        onClick = {
            Log.d(MainActivity.TAG, "클릭되었습니다.")
            onClicked.invoke()
        }) {
        Row(
            Modifier.fillMaxSize(), horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {

            Icon(
                imageVector = Icons.Rounded.Refresh,
                contentDescription = "",
                tint = Color.Black
            )
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                AutoSizeText(
                    value = "$label",
                    modifier = Modifier,
                    fontSize = fontSize.sp,
                    maxLines = 1,
                    minFontSize = 10.sp,
                    color = fontColor
                )
            }
        }

    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CardIconButton(
    imageVector: ImageVector,
    onClicked: () -> Unit,
    modifier: Modifier,
    buttonColor: Color,
) {
    Card(colors = CardDefaults.cardColors(buttonColor),
        elevation = CardDefaults.cardElevation(8.dp),
        shape = RoundedCornerShape(2.dp),
        modifier = modifier,
        onClick = {
            Log.d(MainActivity.TAG, "클릭되었습니다.")
            onClicked.invoke()
        }) {
        Row(
            Modifier.wrapContentSize(), horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = imageVector,
                contentDescription = "",
                tint = Color.Black
            )
        }

    }
}

@Composable
fun IconButton(
    imageVector: ImageVector,
    onClicked: () -> Unit,
    modifier: Modifier,
) {
    Row(
        modifier.wrapContentSize(), horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = imageVector,
            modifier = Modifier.clickable {
                onClicked.invoke()
            },
            contentDescription = "",
            tint = Color.Black
        )
    }

}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomCard(
    label: String,
    fontSize: Int,
    modifier: Modifier,
    fontColor: Color,
    cardColor: Color,
) {

    Card(
        colors = CardDefaults.cardColors(cardColor),
        elevation = CardDefaults.cardElevation(8.dp),
        modifier = modifier,
        shape = RoundedCornerShape(2.dp)
    ) {
        Row(
            Modifier
                .fillMaxSize(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                AutoSizeText(
                    value = "$label",
                    modifier = Modifier,
                    fontSize = fontSize.sp,
                    maxLines = 1,
                    minFontSize = 10.sp,
                    color = fontColor
                )
            }

        }

    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LabelAndIconButton(
    onClicked: () -> Unit,
    label: String,
    icon: ImageVector) {
    Card(
        colors = CardDefaults.cardColors(TopButtonColor),
        elevation = CardDefaults.cardElevation(8.dp),
        shape = RoundedCornerShape(1.dp),
        border = BorderStroke(1.dp, color = Color.Black),
        modifier = Modifier
            .height(20.dp)
            .width(70.dp),
        onClick = onClicked) {
        Row(
            modifier = Modifier
                .fillMaxHeight()
                .wrapContentWidth()
                .padding(horizontal = 10.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            AutoSizeText(value = label, minFontSize = 8.sp, color = Color.Black)

            Image(
                imageVector = icon,
                contentScale = ContentScale.Crop,
                contentDescription = "사용자 정의"
            )
        }
    }
}