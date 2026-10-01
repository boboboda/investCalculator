// screens/SpreadSettingsScreen.kt
package com.bobodroid.myapplication.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.bobodroid.myapplication.models.datamodels.roomDb.Currencies
import com.bobodroid.myapplication.models.datamodels.roomDb.CurrencyType
import com.bobodroid.myapplication.models.viewmodels.SpreadSettingsViewModel
import kotlin.math.roundToInt
import kotlin.math.roundToLong

/**
 * 환율 스프레드 설정 화면
 * - 통화별 매수/매도 스프레드(원) 설정
 * - 저장 시 로컬 + 서버 동기화
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpreadSettingsScreen(
    onBackClick: () -> Unit,
    isPremium: Boolean,
    onPremiumRequired: () -> Unit,
    viewModel: SpreadSettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val selectedCurrencyObj = Currencies.fromCurrencyType(uiState.selectedCurrency)

    LaunchedEffect(uiState.saveResultMessage) {
        uiState.saveResultMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.clearMessage()
        }
    }

    Scaffold(
        // ✅ 상태바 여백은 AppScreen에서 이미 적용 → 화면 쪽 중복 인셋 제거
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                windowInsets = WindowInsets(0, 0, 0, 0),
                title = { Text("환율 스프레드 설정") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "뒤로가기"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF6152D9),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFFF9FAFB))
                .verticalScroll(rememberScrollState())
        ) {
            CurrencyTabRow(
                selected = uiState.selectedCurrency,
                isPremium = isPremium,
                onSelect = { currency ->
                    val currencyObj = Currencies.fromCurrencyType(currency)
                    if (currencyObj.isPremium && !isPremium) {
                        onPremiumRequired()
                    } else {
                        viewModel.loadCurrency(currency)
                    }
                }
            )

            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                SpreadExplainCard(
                    currencyCode = selectedCurrencyObj.code,
                    needsMultiply = selectedCurrencyObj.needsMultiply
                )

                SpreadInputCard(
                    label = "매수(살 때) 스프레드",
                    description = "실시간가보다 몇 원 높게 '살 때' 가격으로 계산할지",
                    value = uiState.buySpreadWon,
                    onValueChange = viewModel::updateBuySpread
                )

                SpreadInputCard(
                    label = "매도(팔 때) 스프레드",
                    description = "실시간가보다 몇 원 낮게 '팔 때' 가격으로 계산할지",
                    value = uiState.sellSpreadWon,
                    onValueChange = viewModel::updateSellSpread
                )

                if (uiState.hasCustomSpread) {
                    Text(
                        text = "✓ 이 통화는 커스텀 스프레드가 적용 중입니다",
                        fontSize = 12.sp,
                        color = Color(0xFF6152D9),
                        fontWeight = FontWeight.Medium
                    )
                }

                Button(
                    onClick = { viewModel.saveSpread() },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !uiState.isSaving,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6152D9)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = if (uiState.isSaving) "저장 중..." else "저장하기",
                        modifier = Modifier.padding(vertical = 6.dp),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun CurrencyTabRow(
    selected: CurrencyType,
    isPremium: Boolean,
    onSelect: (CurrencyType) -> Unit
) {
    // CurrencyType enum에 실제로 존재하는 통화만 순회
    val availableCurrencies = remember {
        Currencies.all.mapNotNull { currencyObj ->
            CurrencyType.entries.find { it.name == currencyObj.code }?.let { currencyType ->
                currencyObj to currencyType
            }
        }
    }

    val selectedIndex = availableCurrencies
        .indexOfFirst { it.second.code == selected.code }
        .coerceAtLeast(0)

    ScrollableTabRow(
        selectedTabIndex = selectedIndex,
        containerColor = Color.White,
        edgePadding = 12.dp
    ) {
        availableCurrencies.forEach { (currencyObj, currencyType) ->
            val locked = currencyObj.isPremium && !isPremium

            Tab(
                selected = selected.code == currencyObj.code,
                onClick = { onSelect(currencyType) },
                text = {
                    Text(
                        text = "${currencyObj.symbol} ${currencyObj.code}" + if (locked) " 🔒" else "",
                        fontSize = 12.sp
                    )
                }
            )
        }
    }
}

@Composable
private fun SpreadExplainCard(
    currencyCode: String,
    needsMultiply: Boolean
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFEDEAFB))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = "실시간 환율은 은행 기준환율(mid)입니다. 실제 살 때/팔 때 가격은 스프레드만큼 차이가 나므로, 주로 이용하는 은행·플랫폼의 스프레드(원)에 맞춰 설정하면 알림과 화면 표시가 더 정확해집니다.",
                fontSize = 12.sp,
                color = Color(0xFF4B3FA8)
            )
            if (needsMultiply) {
                Text(
                    text = "$currencyCode 는 화면에 표시되는 100단위 기준 환율에 이 금액이 더해지고 빠집니다.",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF4B3FA8)
                )
            }
        }
    }
}

// ✅ 슬라이더 조정 범위 / 간격 (원 단위)
private const val SPREAD_MIN = 0.0
private const val SPREAD_MAX = 30.0
private const val SPREAD_STEP = 0.1 // 슬라이더 한 칸 = 0.1원
private val SPREAD_STEPS = ((SPREAD_MAX - SPREAD_MIN) / SPREAD_STEP).roundToInt() - 1

private fun Double.roundTo2(): Double = (this * 100).roundToLong() / 100.0

private fun Double.toSpreadDisplay(): String = "%.2f".format(this)

@Composable
private fun SpreadInputCard(
    label: String,
    description: String,
    value: Double,
    onValueChange: (Double) -> Unit
) {
    // 직접입력 텍스트필드용 로컬 상태
    //   입력 중인 값(예: "0.")이 같은 숫자로 해석되면 덮어쓰지 않아서 소수 입력이 끊기지 않음
    var text by remember { mutableStateOf(value.toSpreadDisplay()) }

    LaunchedEffect(value) {
        if (text.toDoubleOrNull() != value) {
            text = value.toSpreadDisplay()
        }
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = label, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1F2937))
                    Text(text = description, fontSize = 12.sp, color = Color.Gray)
                }
                Text(
                    text = "${value.toSpreadDisplay()}원",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF6152D9)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // ✅ 드래그로 값 조정하는 슬라이더
            Slider(
                value = value.toFloat(),
                onValueChange = { newValue ->
                    val rounded = ((newValue / SPREAD_STEP).roundToInt() * SPREAD_STEP).roundTo2()
                    onValueChange(rounded)
                },
                valueRange = SPREAD_MIN.toFloat()..SPREAD_MAX.toFloat(),
                steps = SPREAD_STEPS,
                colors = SliderDefaults.colors(
                    thumbColor = Color(0xFF6152D9),
                    activeTrackColor = Color(0xFF6152D9),
                    inactiveTrackColor = Color(0xFFE5E7EB)
                )
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "${SPREAD_MIN.toSpreadDisplay()}원", fontSize = 11.sp, color = Color.Gray)
                Text(text = "${SPREAD_MAX.toSpreadDisplay()}원", fontSize = 11.sp, color = Color.Gray)
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "직접 입력 (예: 0.5)",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF6B7280)
            )

            Spacer(modifier = Modifier.height(4.dp))

            // ✅ 슬라이더 범위(0~30원)를 벗어나는 값도 직접 입력할 수 있도록 유지
            OutlinedTextField(
                value = text,
                onValueChange = { input ->
                    if (input.isEmpty() || input.toDoubleOrNull() != null) {
                        text = input
                        input.toDoubleOrNull()
                            ?.takeIf { it >= 0 }
                            ?.let { onValueChange(it.roundTo2()) }
                    }
                },
                suffix = { Text("원") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF6152D9),
                    unfocusedBorderColor = Color(0xFFE5E7EB)
                )
            )
        }
    }
}
