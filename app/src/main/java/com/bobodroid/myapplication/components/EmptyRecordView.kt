// 📁 app/src/main/java/com/bobodroid/myapplication/components/EmptyRecordView.kt

package com.bobodroid.myapplication.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun EmptyRecordView(
    currencyName: String,
    onAddClick: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 아이콘
            Icon(
                imageVector = Icons.Rounded.Inbox,
                contentDescription = null,
                tint = Color(0xFFBBBBBB),
                modifier = Modifier.size(64.dp)
            )

            // 메인 메시지
            Text(
                text = "아직 ${currencyName} 기록이 없습니다",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF666666),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(4.dp))

            // 햄버거 메뉴 힌트 - 훨씬 작게
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Menu,
                    contentDescription = null,
                    tint = Color(0xFF00BFA5),
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "우측 하단 메뉴에서 추가",
                    fontSize = 13.sp,
                    color = Color(0xFF00BFA5),
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}