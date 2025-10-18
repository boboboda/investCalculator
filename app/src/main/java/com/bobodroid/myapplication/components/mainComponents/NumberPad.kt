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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bobodroid.myapplication.extensions.toLongWon

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

    // 🎨 GroupChangeBottomSheet 스타일 적용
    val cardColor = if(selectedState) {
        CardDefaults.cardColors(Color(0xFFEEF2FF)) // 연한 보라 (선택 시)
    } else {
        CardDefaults.cardColors(Color(0xFFF9FAFB)) // 연한 회색 (기본)
    }

    val textColor = if(title == "") Color(0xFF9CA3AF) else Color(0xFF1F2937)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 0.dp)
            .height(56.dp),
        border = BorderStroke(1.dp, if(selectedState) Color(0xFF6366F1) else Color(0xFFE5E7EB)),
        colors = cardColor,
        elevation = CardDefaults.cardElevation(0.dp),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
        onClick = onClicked
    ) {
        Text(
            text = formatTitle,
            fontSize = 15.sp,
            fontWeight = if(title == "") FontWeight.Normal else FontWeight.Medium,
            color = textColor,
            modifier = Modifier
                .padding(vertical = 18.dp)
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

    // 🎨 GroupChangeBottomSheet 스타일 적용
    val cardColor = if(selectedState) {
        CardDefaults.cardColors(Color(0xFFEEF2FF)) // 연한 보라 (선택 시)
    } else {
        CardDefaults.cardColors(Color(0xFFF9FAFB)) // 연한 회색 (기본)
    }

    val textColor = if(title == "") Color(0xFF9CA3AF) else Color(0xFF1F2937)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 0.dp)
            .height(56.dp),
        border = BorderStroke(1.dp, if(selectedState) Color(0xFF6366F1) else Color(0xFFE5E7EB)),
        colors = cardColor,
        elevation = CardDefaults.cardElevation(0.dp),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
        onClick = onClicked
    ) {
        Text(
            text = formatTile,
            fontSize = 15.sp,
            fontWeight = if(title == "") FontWeight.Normal else FontWeight.Medium,
            color = textColor,
            modifier = Modifier
                .padding(vertical = 18.dp)
                .fillMaxWidth(),
            textAlign = TextAlign.Center
        )
    }
}