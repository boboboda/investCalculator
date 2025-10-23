package com.bobodroid.myapplication.components.Dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog

/**
 * 프리미엄 유도 팝업
 * - 전면 광고 3회마다 표시
 * - 24시간 무료 프리미엄 유도
 */
@Composable
fun PremiumPromptDialog(
    onWatchAd: () -> Unit,
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
                    text = "✨",
                    fontSize = 48.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Text(
                    text = "프리미엄으로\n업그레이드하세요!",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // ━━━━━━━━━━━━━━━━━━━━━━━━━━
                // 혜택 리스트
                // ━━━━━━━━━━━━━━━━━━━━━━━━━━
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    BenefitItem(icon = "🚫", text = "모든 광고 제거")
                    BenefitItem(icon = "⚡", text = "빠른 분석 속도")
                    BenefitItem(icon = "📊", text = "고급 차트 기능")
                    BenefitItem(icon = "🔔", text = "무제한 알림")
                }

                Spacer(modifier = Modifier.height(24.dp))

                // ━━━━━━━━━━━━━━━━━━━━━━━━━━
                // 버튼 영역
                // ━━━━━━━━━━━━━━━━━━━━━━━━━━

                // 메인 버튼 (24시간 무료 체험)
                Button(
                    onClick = onWatchAd,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text(
                        text = "💎 24시간 무료 체험",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // 서브 버튼 (나중에)
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "나중에",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

/**
 * 혜택 아이템 (아이콘 + 텍스트)
 */
@Composable
private fun BenefitItem(icon: String, text: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        Text(
            text = icon,
            fontSize = 20.sp,
            modifier = Modifier.padding(end = 12.dp)
        )
        Text(
            text = text,
            fontSize = 15.sp,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}