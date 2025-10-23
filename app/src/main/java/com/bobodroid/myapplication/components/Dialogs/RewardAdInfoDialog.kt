package com.bobodroid.myapplication.components.Dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog

/**
 * 리워드 광고 안내 다이얼로그
 * - 광고 시청 전 혜택 설명
 * - 24시간 프리미엄 안내
 */
@Composable
fun RewardAdInfoDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // ━━━━━━━━━━━━━━━━━━━━━━━━━━
                // 헤더
                // ━━━━━━━━━━━━━━━━━━━━━━━━━━
                Text(
                    text = "💎",
                    fontSize = 48.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Text(
                    text = "24시간 무료 프리미엄",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // ━━━━━━━━━━━━━━━━━━━━━━━━━━
                // 설명 텍스트
                // ━━━━━━━━━━━━━━━━━━━━━━━━━━
                Text(
                    text = "광고 시청으로\n24시간 동안 프리미엄 기능을\n무료로 이용하세요!",
                    fontSize = 15.sp,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 22.sp,
                    modifier = Modifier.padding(bottom = 20.dp)
                )

                // ━━━━━━━━━━━━━━━━━━━━━━━━━━
                // 혜택 박스
                // ━━━━━━━━━━━━━━━━━━━━━━━━━━
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    InfoItem(text = "✓ 하루 1회 가능")
                    InfoItem(text = "✓ 모든 광고 제거")
                    InfoItem(text = "✓ 프리미엄 기능 사용")
                }

                Spacer(modifier = Modifier.height(24.dp))

                // ━━━━━━━━━━━━━━━━━━━━━━━━━━
                // 버튼 영역
                // ━━━━━━━━━━━━━━━━━━━━━━━━━━

                // 메인 버튼 (광고 보고 받기)
                Button(
                    onClick = onConfirm,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "📺 광고 보고 받기",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // 서브 버튼 (취소)
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "취소",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

/**
 * 정보 아이템 (체크마크 + 텍스트)
 */
@Composable
private fun InfoItem(text: String) {
    Text(
        text = text,
        fontSize = 14.sp,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.fillMaxWidth()
    )
}