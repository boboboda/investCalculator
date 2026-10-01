package com.bobodroid.myapplication.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.TrendingUp
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * 환테크 백테스트 시뮬레이터 진입 배너 — 목업(Records.dc.html) 기준
 * 기록 화면(MainHeader) 전용. 보라색 그라디언트 스타일.
 *
 * 외부 여백(margin)은 caller가 modifier로 직접 지정합니다 (내부에 기본 padding을 두지 않음 —
 * 기존 구현에서 배너 간격이 이상했던 원인이 내부 padding과 호출부 padding이 중복 적용된 것이었습니다).
 * MainHeader.kt 호출부: modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp)
 */
@Composable
fun BacktestBannerCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(Color(0xFF6C5CE7), Color(0xFF8B7CF0))
                ),
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(onClick = onClick)
            .padding(18.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .background(Color.White.copy(alpha = 0.18f), RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.TrendingUp,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(22.dp)
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "그때 투자했으면 얼마 벌었을까?",
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                text = "과거 환율로 내 전략 무료로 검증해보기",
                color = Color(0xFFEAE6FC),
                fontSize = 12.sp
            )
        }

        Icon(
            imageVector = Icons.Rounded.ChevronRight,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(18.dp)
        )
    }
}

/**
 * 환테크 백테스트 시뮬레이터 진입 배너 — 목업(MyPage.dc.html) 기준
 * 마이페이지 화면(ImprovedMyPageView) 전용. 네이비 단색 스타일, 문구/색상이 위 컴포넌트와 다릅니다.
 *
 * MyPageScreen.kt 호출부: modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 7.dp)
 */
@Composable
fun BacktestBannerCardCompact(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(Color(0xFF16233A), RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(Color.White.copy(alpha = 0.12f), RoundedCornerShape(11.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.TrendingUp,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "그때 투자했으면 얼마 벌었을까?",
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                text = "환테크 백테스트 시뮬레이터 · 무료",
                color = Color(0xFF9FB3CC),
                fontSize = 11.sp
            )
        }

        Icon(
            imageVector = Icons.Rounded.ChevronRight,
            contentDescription = null,
            tint = Color(0xFF9FB3CC),
            modifier = Modifier.size(16.dp)
        )
    }
}
