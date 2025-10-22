// app/src/main/java/com/bobodroid/myapplication/components/Dialogs/ImprovedTargetRateDialog.kt

package com.bobodroid.myapplication.components.Dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.bobodroid.myapplication.models.datamodels.roomDb.CurrencyType
import com.bobodroid.myapplication.models.datamodels.roomDb.RateDirection
import com.bobodroid.myapplication.models.datamodels.roomDb.emoji
import com.bobodroid.myapplication.models.datamodels.service.UserApi.Rate

@Composable
fun ImprovedTargetRateDialog(
    currency: CurrencyType,
    direction: RateDirection,
    currentRate: String,
    lastRate: Rate?,
    onDismiss: () -> Unit,
    onConfirm: (Rate) -> Unit
) {
    var userInput by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val directionText = if (direction == RateDirection.HIGH) "고점" else "저점"
    val directionColor = if (direction == RateDirection.HIGH)
        Color(0xFFEF4444) else Color(0xFF3B82F6)
    val directionIcon = if (direction == RateDirection.HIGH)
        Icons.Default.ArrowUpward else Icons.Default.ArrowDownward

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // ✅ 헤더
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "목표환율 설정",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1F2937)
                    )

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "닫기",
                            tint = Color(0xFF6B7280)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // ✅ 통화 정보 카드
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFF9FAFB)
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // 통화 정보
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = currency.emoji,
                                fontSize = 32.sp
                            )
                            Column {
                                Text(
                                    text = currency.koreanName,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1F2937)
                                )
                                Text(
                                    text = currency.name,
                                    fontSize = 14.sp,
                                    color = Color(0xFF6B7280)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // 현재 환율
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "현재 환율: ",
                                fontSize = 14.sp,
                                color = Color(0xFF6B7280)
                            )
                            Text(
                                text = "$currentRate 원",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1F2937)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // 고점/저점 표시
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = directionColor.copy(alpha = 0.1f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = directionIcon,
                                    contentDescription = null,
                                    tint = directionColor,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "$directionText 알림",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = directionColor
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // ✅ 마지막 설정된 목표환율 (있을 경우)
                if (lastRate != null) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFFEF3C7)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "마지막 설정 환율",
                                fontSize = 13.sp,
                                color = Color(0xFF92400E)
                            )
                            Text(
                                text = "${lastRate.rate}원",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF92400E)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // ✅ 안내 메시지
                Text(
                    text = if (direction == RateDirection.HIGH) {
                        "설정한 환율 이상이 되면 알림을 받습니다"
                    } else {
                        "설정한 환율 이하가 되면 알림을 받습니다"
                    },
                    fontSize = 13.sp,
                    color = Color(0xFF6B7280),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                // ✅ 입력 필드
                OutlinedTextField(
                    value = userInput,
                    onValueChange = {
                        userInput = it
                        errorMessage = null
                    },
                    label = { Text("목표 환율") },
                    placeholder = { Text("예: 1350") },
                    suffix = { Text("원", color = Color(0xFF6B7280)) },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number
                    ),
                    singleLine = true,
                    isError = errorMessage != null,
                    supportingText = {
                        errorMessage?.let {
                            Text(
                                text = it,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = directionColor,
                        focusedLabelColor = directionColor
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(24.dp))

                // ✅ 버튼
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // 취소 버튼
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color(0xFF6B7280)
                        )
                    ) {
                        Text(
                            text = "취소",
                            fontSize = 15.sp,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }

                    // 추가 버튼
                    Button(
                        onClick = {
                            val inputValue = userInput.toIntOrNull()

                            when {
                                userInput.isEmpty() -> {
                                    errorMessage = "목표환율을 입력해주세요"
                                }
                                inputValue == null -> {
                                    errorMessage = "올바른 숫자를 입력해주세요"
                                }
                                inputValue <= 0 -> {
                                    errorMessage = "0보다 큰 값을 입력해주세요"
                                }
                                else -> {
                                    val countUpRateNumber = (lastRate?.number ?: 0) + 1
                                    val newRate = Rate(
                                        number = countUpRateNumber,
                                        rate = inputValue
                                    )
                                    onConfirm(newRate)
                                }
                            }
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Transparent
                        ),
                        contentPadding = PaddingValues(0.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    brush = Brush.horizontalGradient(
                                        colors = listOf(
                                            Color(0xFF6366F1),
                                            Color(0xFF8B5CF6)
                                        )
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "추가",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}