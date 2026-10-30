package com.bobodroid.myapplication.components.travelComponents

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Flight
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bobodroid.myapplication.ui.theme.primaryColor

/**
 * 마이페이지 - 여행모드 메뉴 활성/비활성 토글
 * 켜면 헤더의 투자/여행 알약 토글, 기록탭의 여행 카드 리스트, 분석탭의 여행 요약이 노출됨
 * (알약 토글/FAB 배선 자체는 5단계에서 완성 - 여긴 토글 UI와 저장만 담당)
 */
@Composable
fun TravelModeMenuToggle(
    enabled: Boolean,
    onToggle: (Boolean) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFFECEAF5), RoundedCornerShape(14.dp))
            .padding(16.dp)
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .background(Color(0xFFF1EEFE), RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.Flight,
                contentDescription = null,
                tint = primaryColor
            )
        }

        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text("여행모드", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF1F2430))
            Text(
                "여행 중 지출을 내 평균 환전환율로 기록해요",
                fontSize = 11.sp,
                color = Color(0xFF8B93A3)
            )
        }

        Switch(
            checked = enabled,
            onCheckedChange = onToggle,
            colors = SwitchDefaults.colors(checkedTrackColor = primaryColor)
        )
    }
}
