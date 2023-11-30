package com.bobodroid.myapplication.components

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFontLoader
import androidx.compose.ui.text.Paragraph
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp

@Composable
fun AutoSizeText(
    value: String,
    modifier: Modifier = Modifier,
    fontSize: TextUnit = 18.sp,
    maxLines: Int = Int.MAX_VALUE,
    minFontSize: TextUnit,
    scaleFactor: Float = 0.9f,
    color: Color,
    fontWeight: FontWeight? = null) {
    BoxWithConstraints(
        modifier = modifier
    ) {
        var nFontSize = fontSize

        val calculateParagraph = @Composable {
            Paragraph(
                text = value,
                style = TextStyle(fontSize = nFontSize),
                density = LocalDensity.current,
                resourceLoader = LocalFontLoader.current,
                maxLines = maxLines,
                width = with(LocalDensity.current) { maxWidth.toPx() }
            )
        }

        var intrinsics = calculateParagraph()
        with(LocalDensity.current) {
            while ((intrinsics.height.toDp() > maxHeight || intrinsics.didExceedMaxLines) && nFontSize >= minFontSize) {
                nFontSize *= scaleFactor
                intrinsics = calculateParagraph()
            }
        }

        Text(
            text = value,
            color = color,
            maxLines = maxLines,
            fontSize = nFontSize,
            textAlign = TextAlign.Center,
            fontWeight = fontWeight)

    }
}