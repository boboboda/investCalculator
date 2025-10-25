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
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


@Composable
@Suppress("UnusedBoxWithConstraintsScope")
fun AutoSizeText(
    value: String,
    modifier: Modifier = Modifier,
    fontSize: TextUnit = 18.sp,
    maxLines: Int = Int.MAX_VALUE,
    minFontSize: TextUnit,
    scaleFactor: Float = 0.9f,
    color: Color,
    textAlign: TextAlign = TextAlign.Center,
    lineHeight: Int = 20,
    fontWeight: FontWeight? = null
) {
    BoxWithConstraints(
        modifier = modifier
    ) {
        var nFontSize = fontSize

        // BoxWithConstraints의 scope를 명시적으로 사용
        val maxWidthPx = with(LocalDensity.current) { maxWidth.toPx() }
        val maxHeightDp = maxHeight

        val calculateParagraph = @Composable {
            Paragraph(
                text = value,
                style = TextStyle(fontSize = nFontSize),
                density = LocalDensity.current,
                resourceLoader = LocalFontLoader.current,
                maxLines = maxLines,
                width = maxWidthPx  // 미리 계산한 값 사용
            )
        }

        var intrinsics = calculateParagraph()
        with(LocalDensity.current) {
            while ((intrinsics.height.toDp() > maxHeightDp || intrinsics.didExceedMaxLines) && nFontSize >= minFontSize) {
                nFontSize *= scaleFactor
                intrinsics = calculateParagraph()
            }
        }

        Text(
            lineHeight = lineHeight.sp,
            text = value,
            color = color,
            maxLines = maxLines,
            fontSize = nFontSize,
            textAlign = textAlign,
            fontWeight = fontWeight
        )
    }
}