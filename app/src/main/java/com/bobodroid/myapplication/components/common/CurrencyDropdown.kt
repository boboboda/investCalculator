package com.bobodroid.myapplication.components.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bobodroid.myapplication.models.datamodels.roomDb.Currencies
import com.bobodroid.myapplication.models.datamodels.roomDb.CurrencyType
import com.bobodroid.myapplication.models.datamodels.roomDb.emoji

/**
 * 공통 통화 선택 드롭다운 컴포넌트
 * 기존 MainHeader 디자인 스타일 유지
 *
 * @param selectedCurrency 현재 선택된 통화
 * @param updateCurrentForeignCurrency 통화 변경 함수
 * @param isPremium 프리미엄 사용자 여부
 * @param onPremiumRequired 프리미엄 필요 시 콜백
 * @param modifier Modifier
 * @param backgroundColor 배경 색상 (기본: 반투명 흰색)
 * @param contentColor 텍스트/아이콘 색상 (기본: 흰색)
 */
@Composable
fun CurrencyDropdown(
    selectedCurrency: CurrencyType,
    updateCurrentForeignCurrency: (CurrencyType) -> Boolean,
    isPremium: Boolean,
    onPremiumRequired: () -> Unit,
    modifier: Modifier = Modifier,
    backgroundColor: Color = Color.White.copy(alpha = 0.2f),
    contentColor: Color = Color.White
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        // ✅ 기존 디자인 스타일 - Surface로 감싸기
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = backgroundColor,
            onClick = { expanded = true }
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "${selectedCurrency.emoji} ${selectedCurrency.code}",
                    color = contentColor,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // 드롭다운 메뉴
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            // 무료 통화 (USD, JPY)
            val freeCurrencies = CurrencyType.values().filter { currencyType ->
                val currency = Currencies.fromCurrencyType(currencyType)
                !currency.isPremium
            }

            // 프리미엄 통화 (나머지)
            val premiumCurrencies = CurrencyType.values().filter { currencyType ->
                val currency = Currencies.fromCurrencyType(currencyType)
                currency.isPremium
            }

            // 무료 통화 표시
            freeCurrencies.forEach { currencyType ->
                val currency = Currencies.fromCurrencyType(currencyType)
                DropdownMenuItem(
                    text = {
                        Text("${currencyType.emoji} ${currency.koreanName} (${currencyType.code})")
                    },
                    onClick = {
                        val success = updateCurrentForeignCurrency(currencyType)
                        if (success) {
                            expanded = false
                        }
                    }
                )
            }

            // 구분선 (무료/프리미엄 구분)
            if (freeCurrencies.isNotEmpty() && premiumCurrencies.isNotEmpty()) {
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 4.dp),
                    color = Color.LightGray
                )
            }

            // 프리미엄 통화 표시
            premiumCurrencies.forEach { currencyType ->
                val currency = Currencies.fromCurrencyType(currencyType)
                DropdownMenuItem(
                    text = {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${currencyType.emoji} ${currency.koreanName} (${currencyType.code})",
                                color = if (isPremium) Color.Black else Color.Gray
                            )

                            // PRO 배지
                            if (!isPremium) {
                                Box(
                                    modifier = Modifier
                                        .background(
                                            color = Color(0xFF6366F1),
                                            shape = RoundedCornerShape(4.dp)
                                        )
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "PRO",
                                        fontSize = 10.sp,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    },
                    onClick = {
                        if (isPremium) {
                            // 프리미엄 사용자는 바로 선택
                            val success = updateCurrentForeignCurrency(currencyType)
                            if (success) {
                                expanded = false
                            }
                        } else {
                            // 일반 사용자는 프리미엄 안내
                            expanded = false
                            onPremiumRequired()
                        }
                    },
                    enabled = true  // 항상 클릭 가능하게
                )
            }
        }
    }
}

/**
 * 사용 예시:
 *
 * // MainHeader.kt (흰색 배경에 사용)
 * CurrencyDropdown(
 *     selectedCurrency = mainUiState.selectedCurrencyType,
 *     updateCurrentForeignCurrency = updateCurrentForeignCurrency,
 *     isPremium = isPremium,
 *     onPremiumRequired = onPremiumRequired,
 *     backgroundColor = Color.White.copy(alpha = 0.2f),  // 반투명 흰색
 *     contentColor = Color.White
 * )
 *
 * // 다른 화면 (일반 배경)
 * CurrencyDropdown(
 *     selectedCurrency = selectedCurrency,
 *     updateCurrentForeignCurrency = { viewModel.updateSelectedCurrency(it) },
 *     isPremium = isPremium,
 *     onPremiumRequired = { Toast.makeText(...).show() },
 *     backgroundColor = Color(0xFFF5F5F5),  // 연한 회색
 *     contentColor = Color.Black
 * )
 */