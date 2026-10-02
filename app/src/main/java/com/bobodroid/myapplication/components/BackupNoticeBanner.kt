// app/src/main/java/com/bobodroid/myapplication/components/BackupNoticeBanner.kt
package com.bobodroid.myapplication.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.CloudOff
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * 기록 저장 직후 표시하는 백업 안내 배너 (소셜 미연동 사용자 대상)
 * - 배너를 누르면 onConnectClick, X를 누르면 onDismiss
 */
@Composable
fun BackupNoticeBanner(
    onConnectClick: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .background(Color(0xFF1F2937), RoundedCornerShape(12.dp))
            .clickable(onClick = onConnectClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector = Icons.Rounded.CloudOff,
            contentDescription = null,
            tint = Color(0xFFFBBF24),
            modifier = Modifier.size(22.dp)
        )
        Column(modifier = Modifier.weight(1f)) {
            // 화면 폭에 맞춰 글자 크기가 줄어드는 AutoSizeText 사용 (한 줄 고정)
            AutoSizeText(
                value = "기록이 아직 백업되지 않고 있어요",
                modifier = Modifier.fillMaxWidth(),
                fontSize = 13.sp,
                minFontSize = 9.sp,
                maxLines = 1,
                color = Color.White,
                textAlign = TextAlign.Start,
                fontWeight = FontWeight.Bold
            )
            AutoSizeText(
                value = "소셜 로그인을 연결하면 자동으로 백업돼요",
                modifier = Modifier.fillMaxWidth(),
                fontSize = 12.sp,
                minFontSize = 9.sp,
                maxLines = 1,
                color = Color(0xFFD1D5DB),
                textAlign = TextAlign.Start
            )
        }
        Icon(
            imageVector = Icons.Rounded.Close,
            contentDescription = "닫기",
            tint = Color(0xFF9CA3AF),
            modifier = Modifier
                .size(20.dp)
                .clickable(onClick = onDismiss)
        )
    }
}
