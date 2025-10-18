package com.bobodroid.myapplication.components.Dialogs

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.delay

@Composable
fun OnboardingTooltipDialog(
    onDismiss: () -> Unit
) {
    // 좌우 스와이프 애니메이션
    val infiniteTransition = rememberInfiniteTransition(label = "swipe")

    val leftSwipeOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -30f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "leftSwipe"
    )

    val rightSwipeOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 30f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "rightSwipe"
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.7f))
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    // 헤더
                    Icon(
                        imageVector = Icons.Rounded.TouchApp,
                        contentDescription = null,
                        tint = Color(0xFF6366F1),
                        modifier = Modifier.size(48.dp)
                    )

                    Text(
                        text = "빠른 사용법",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1F2937)
                    )

                    // 카드 샘플 + 스와이프 안내
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                    ) {
                        // 왼쪽 스와이프 안내
                        Column(
                            modifier = Modifier
                                .align(Alignment.CenterStart)
                                .offset(x = leftSwipeOffset.dp),
                            horizontalAlignment = Alignment.Start,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.ArrowBack,
                                contentDescription = null,
                                tint = Color(0xFF10B981),
                                modifier = Modifier.size(32.dp)
                            )
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFF10B981)
                            ) {
                                Text(
                                    text = "매도",
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    fontSize = 14.sp,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // 중앙 카드
                        Card(
                            modifier = Modifier
                                .align(Alignment.Center)
                                .width(180.dp)
                                .height(120.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF9FAFB)),
                            elevation = CardDefaults.cardElevation(4.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "기록 카드",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF6B7280)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "← 밀기 →",
                                    fontSize = 13.sp,
                                    color = Color(0xFF9CA3AF)
                                )
                            }
                        }

                        // 오른쪽 스와이프 안내
                        Column(
                            modifier = Modifier
                                .align(Alignment.CenterEnd)
                                .offset(x = rightSwipeOffset.dp),
                            horizontalAlignment = Alignment.End,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.ArrowForward,
                                contentDescription = null,
                                tint = Color(0xFFEF4444),
                                modifier = Modifier.size(32.dp)
                            )
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFFEF4444)
                            ) {
                                Text(
                                    text = "삭제",
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    fontSize = 14.sp,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    HorizontalDivider(color = Color(0xFFE5E7EB))

                    // 추가 안내
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        FeatureItem(
                            icon = Icons.Rounded.TouchApp,
                            text = "카드를 탭하면 메모를 추가할 수 있어요",
                            color = Color(0xFF6366F1)
                        )
                        FeatureItem(
                            icon = Icons.Rounded.MoreVert,
                            text = "⋮ 메뉴에서 수정, 그룹 변경이 가능해요",
                            color = Color(0xFF8B5CF6)
                        )
                    }

                    // 시작 버튼
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF6366F1)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "시작하기",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    TextButton(onClick = onDismiss) {
                        Text(
                            text = "다시 보지 않기",
                            fontSize = 13.sp,
                            color = Color(0xFF6B7280)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FeatureItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    color: Color
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(20.dp)
        )
        Text(
            text = text,
            fontSize = 13.sp,
            color = Color(0xFF4B5563),
            lineHeight = 18.sp
        )
    }
}