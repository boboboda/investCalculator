package com.bobodroid.myapplication.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * 소셜 로그인 연동 안내 배너
 * - 소셜 로그인이 안 되어있을 때 경고 표시
 * - 프리미엄 구매 전, 클라우드 백업 화면에서 사용
 */
@Composable
fun SocialLoginWarningBanner(
    isSocialLinked: Boolean,
    onLinkClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (!isSocialLinked) {
        Card(
            modifier = modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFFEF3C7) // 노란색 경고
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 경고 아이콘 + 제목
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Warning,
                        contentDescription = "경고",
                        tint = Color(0xFFF59E0B),
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = "소셜 로그인을 연결하세요",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF92400E)
                    )
                }

                // 설명
                Text(
                    text = "기기 변경 또는 앱 재설치 시 클라우드 백업을 복구할 수 없습니다.\n소셜 로그인을 연결하면 언제 어디서나 데이터를 안전하게 복구할 수 있습니다.",
                    fontSize = 13.sp,
                    color = Color(0xFFB45309),
                    lineHeight = 18.sp
                )

                // 연결하기 버튼
                Button(
                    onClick = onLinkClick,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFF59E0B),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(vertical = 4.dp)
                    ) {
                        Text(
                            text = "지금 연결하기",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

/**
 * 소셜 로그인 상태 표시 카드 (간단 버전)
 * - 연결됨/안됨 상태만 표시
 */
@Composable
fun SocialLoginStatusCard(
    isSocialLinked: Boolean,
    email: String?,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSocialLinked) Color(0xFFECFDF5) else Color(0xFFFEF2F2)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = if (isSocialLinked) Icons.Rounded.CheckCircle else Icons.Rounded.Warning,
                contentDescription = null,
                tint = if (isSocialLinked) Color(0xFF10B981) else Color(0xFFEF4444),
                modifier = Modifier.size(24.dp)
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (isSocialLinked) "소셜 로그인 연결됨" else "소셜 로그인 미연결",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isSocialLinked) Color(0xFF065F46) else Color(0xFF991B1B)
                )

                if (isSocialLinked && !email.isNullOrEmpty()) {
                    Text(
                        text = email,
                        fontSize = 12.sp,
                        color = Color(0xFF6B7280)
                    )
                } else if (!isSocialLinked) {
                    Text(
                        text = "백업 복구가 불가능합니다",
                        fontSize = 12.sp,
                        color = Color(0xFFDC2626)
                    )
                }
            }
        }
    }
}