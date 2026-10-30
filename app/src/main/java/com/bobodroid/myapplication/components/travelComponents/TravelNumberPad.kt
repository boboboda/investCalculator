package com.bobodroid.myapplication.components.travelComponents

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * 여행모드 전용 숫자패드.
 * 기존 PopupNumberView(= MainEvent 전용, 정수/원화만 지원)는 재사용하지 않고
 * 디자인 톤만 맞춘 독립 컴포넌트로 새로 만듦 — 소수점(.) 입력 지원이 필요해서.
 *
 * @param decimalKey true면 가운데 아래 키가 "."(외화 소수점 입력용), false면 "000"(원화 큰 단위 입력용)
 * @param onKeyPress "0".."9" 또는 "." 또는 "000"이 눌렸을 때 호출 (append 로직은 String.appendNumberKey가 처리)
 */
@Composable
fun TravelNumberPad(
    modifier: Modifier = Modifier,
    decimalKey: Boolean = true,
    onKeyPress: (String) -> Unit,
    onBackspace: () -> Unit
) {
    val keys = listOf(
        "1", "2", "3",
        "4", "5", "6",
        "7", "8", "9",
        if (decimalKey) "." else "000", "0", "BACKSPACE"
    )

    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        userScrollEnabled = false
    ) {
        items(keys) { key ->
            TravelNumberPadKey(
                label = if (key == "BACKSPACE") "⌫" else key,
                onClick = {
                    if (key == "BACKSPACE") onBackspace() else onKeyPress(key)
                }
            )
        }
    }
}

@Composable
private fun TravelNumberPadKey(label: String, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFFF7F6FE),
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
            Text(
                text = label,
                fontSize = 17.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF1F2430)
            )
        }
    }
}

/**
 * 숫자패드 입력을 문자열 상태에 안전하게 반영하는 append 로직
 * - 소수점은 1개까지만, 소수점 이하 2자리까지만 허용
 * - 맨 앞의 불필요한 0은 제거 (단, "0." 형태는 허용)
 */
fun String.appendNumberKey(key: String): String {
    if (key == ".") {
        return if (this.contains(".")) this else if (this.isEmpty()) "0." else this + "."
    }

    // "000"은 소수점 입력이 없는(원화) 키패드 전용 — 이미 소수점 뒤라면 무시
    if (key == "000" && this.contains(".")) return this

    val afterDot = this.substringAfter(".", missingDelimiterValue = "")
    if (this.contains(".") && afterDot.length >= 2) return this

    val appended = this + key
    return if (appended.startsWith("0") && !appended.startsWith("0.") && appended.length > 1) {
        appended.trimStart('0').ifEmpty { "0" }
    } else {
        appended
    }
}

fun String.removeLastDigit(): String = if (this.isEmpty()) this else this.dropLast(1)
