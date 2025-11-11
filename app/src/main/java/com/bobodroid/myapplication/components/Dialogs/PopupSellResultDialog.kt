package com.bobodroid.myapplication.components.Dialogs

import androidx.compose.animation.*
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.bobodroid.myapplication.extensions.toPer
import com.bobodroid.myapplication.extensions.toWon
import kotlinx.coroutines.delay

@Composable
fun SellResultDialog(
    onDismissRequest: ((Boolean) -> Unit)?,
    selectedRecord: () -> Unit,
    percent: String,
    sellProfit: String
) {
    var visible by remember { mutableStateOf(false) }

    // 수익/손실 여부 판단
    val isProfit = sellProfit.toFloatOrNull()?.let { it >= 0 } ?: true
    val percentValue = percent.toFloatOrNull() ?: 0f

    LaunchedEffect(Unit) {
        delay(50)
        visible = true
    }

    Dialog(
        onDismissRequest = { onDismissRequest?.invoke(false) },
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = false
        )
    ) {
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(tween(300)) + scaleIn(
                initialScale = 0.8f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            ),
            exit = fadeOut(tween(200)) + scaleOut(targetScale = 0.8f)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .wrapContentHeight(),
                shape = RoundedCornerShape(24.dp),
                color = Color.White,
                shadowElevation = 24.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // ✨ 상단 그라데이션 헤더
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(
                                brush = if (isProfit) {
                                    Brush.linearGradient(
                                        colors = listOf(
                                            Color(0xFFEF4444),
                                            Color(0xFFDC2626)
                                        )
                                    )
                                } else {
                                    Brush.linearGradient(
                                        colors = listOf(
                                            Color(0xFF3B82F6),
                                            Color(0xFF2563EB)
                                        )
                                    )
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isProfit) Icons.Rounded.TrendingUp else Icons.Rounded.TrendingDown,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(40.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // 타이틀
                    Text(
                        text = if (isProfit) "매도 완료! 🎉" else "매도 완료",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1F2937),
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = if (isProfit) "축하합니다! 수익이 발생했어요" else "매도가 완료되었습니다",
                        fontSize = 14.sp,
                        color = Color(0xFF6B7280),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 4.dp)
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // 수익 카드
                    ResultCard(
                        icon = Icons.Rounded.AccountBalanceWallet,
                        label = "수익",
                        value = sellProfit.toFloat().toWon(),
                        valueColor = if (isProfit) Color(0xFFEF4444) else Color(0xFF3B82F6),
                        backgroundColor = if (isProfit) Color(0xFFFEE2E2) else Color(0xFFDCEBFF)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // 수익률 카드
                    ResultCard(
                        icon = Icons.Rounded.ShowChart,
                        label = "수익률",
                        value = "${percentValue.toPer()} %",
                        valueColor = if (isProfit) Color(0xFFEF4444) else Color(0xFF3B82F6),
                        backgroundColor = if (isProfit) Color(0xFFFEE2E2) else Color(0xFFDCEBFF)
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // 버튼 영역
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // 닫기 버튼
                        OutlinedButton(
                            onClick = { onDismissRequest?.invoke(false) },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = Color(0xFF6B7280)
                            ),
                            border = ButtonDefaults.outlinedButtonBorder.copy(
                                width = 1.5.dp
                            )
                        ) {
                            Text(
                                text = "닫기",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        // 기록 버튼
                        Button(
                            onClick = {
                                selectedRecord()
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isProfit) Color(0xFFEF4444) else Color(0xFF3B82F6)
                            ),
                            elevation = ButtonDefaults.buttonElevation(
                                defaultElevation = 2.dp,
                                pressedElevation = 4.dp
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Check,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "기록",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * 결과 카드 컴포넌트
 */
@Composable
private fun ResultCard(
    icon: ImageVector,
    label: String,
    value: String,
    valueColor: Color,
    backgroundColor: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        ),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color.White.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = valueColor,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Text(
                    text = label,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF374151)
                )
            }

            Text(
                text = value,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = valueColor
            )
        }
    }
}