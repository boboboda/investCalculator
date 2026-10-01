package com.bobodroid.myapplication.screens.backtest

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.RestartAlt
import androidx.compose.material.icons.rounded.TrendingUp
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bobodroid.myapplication.models.datamodels.backtest.BacktestCurrencyType
import com.bobodroid.myapplication.models.datamodels.backtest.BacktestInvestStrategy
import com.bobodroid.myapplication.models.datamodels.backtest.BacktestRequest
import com.bobodroid.myapplication.models.viewmodels.BacktestViewModel

// ── 목업(환테크 백테스트 시뮬레이터 · Main.dc.html) 색상 토큰 ──────────────────────────
private val BgColor = Color(0xFFF6F5F2)
private val HeaderColor = Color(0xFF16233A)
private val HeaderSubColor = Color(0xFF9FB3CC)
private val BodyTextColor = Color(0xFF1B2430)
private val LabelColor = Color(0xFF5B6472)
private val HintColor = Color(0xFF8A93A3)
private val FieldBorderColor = Color(0xFFDEDBD3)
private val CardBorderColor = Color(0xFFE7E4DD)
private val HighColor = Color(0xFFD92D20)
private val LowColor = Color(0xFF2563EB)
private val SeparatorColor = Color(0xFFC7CCD3)
private val SegmentBgColor = Color(0xFFECE9E2)
private val IconBoxColor = Color(0xFFF0EDE6)
private val ErrorBgColor = Color(0xFFFDECEA)

// ⚠️ 변경점: 예전엔 여기 숫자들이 "40000000", "1400" 같은 샘플 기본값이었는데,
// 전부 빈 값(= 숫자패드에서 "0"으로 표시)으로 바꿨습니다. 통화/기간/투자전략처럼
// "선택지"인 것들만 기본 선택값을 둡니다.
private const val DEFAULT_CURRENCY_TYPE_INDEX = 0 // USD
private const val DEFAULT_PERIOD_MONTHS = 3
private val DEFAULT_INVEST_STRATEGY = BacktestInvestStrategy.EQUAL

@Composable
fun BacktestInputScreen(
    viewModel: BacktestViewModel,
    onBackClick: () -> Unit,
    onSubmit: () -> Unit,
    onNavigateToHistory: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    // ✅ 통화 바뀔 때 3/6/12개월 전체를 한 번에 받아서 캐싱한 맵 — 기간 칩 클릭 시엔
    // 네트워크 요청 없이 여기서 즉시 꺼내 쓰므로 딜레이가 없습니다.
    val availabilityByPeriod by viewModel.availabilityByPeriod.collectAsState()
    val availabilityLoading by viewModel.availabilityLoading.collectAsState()

    // ⚠️ 변경점: remember → rememberSaveable로 바꿨습니다.
    // 예전엔 plain remember라서, 결과 화면으로 넘어갔다가 뒤로 돌아오면 이 Input 컴포저블이
    // 다시 생성되면서 입력값이 전부 초기화돼버렸습니다. rememberSaveable은 네비게이션을
    // 오가도(= 이 화면이 백스택에 남아있는 한) 값을 그대로 들고 있습니다.
    var currencyType by rememberSaveable { mutableStateOf(BacktestCurrencyType.USD) }
    var budget by rememberSaveable { mutableStateOf("") }
    var startRate by rememberSaveable { mutableStateOf("") }
    var minInvestPerRound by rememberSaveable { mutableStateOf("") }
    var maxInvestPerRound by rememberSaveable { mutableStateOf("") }
    var minSplitCount by rememberSaveable { mutableStateOf("") }
    var dropGapWon by rememberSaveable { mutableStateOf("") }
    var riseGapWon by rememberSaveable { mutableStateOf("") }
    var periodMonths by rememberSaveable { mutableStateOf(DEFAULT_PERIOD_MONTHS) }
    var buySpreadWon by rememberSaveable { mutableStateOf("") }
    var sellSpreadWon by rememberSaveable { mutableStateOf("") }
    var investStrategy by rememberSaveable { mutableStateOf(DEFAULT_INVEST_STRATEGY) }
    var progressiveIncreasePercent by rememberSaveable { mutableStateOf("") }

    // ✅ 기록 화면에서 쓰는 숫자 키패드를 재사용하는 백테스트 전용 패드가 지금 어떤 필드를
    // 보여주고 있는지(null = 닫힘). 필드를 탭하면 그 필드의 인덱스가 들어갑니다.
    var activePadIndex by remember { mutableStateOf<Int?>(null) }

    // ✅ 헤더의 초기화 버튼 — 통화/기간/투자전략을 포함해 전부 기본 상태로 되돌립니다.
    fun resetAll() {
        currencyType = BacktestCurrencyType.USD
        budget = ""
        startRate = ""
        minInvestPerRound = ""
        maxInvestPerRound = ""
        minSplitCount = ""
        dropGapWon = ""
        riseGapWon = ""
        periodMonths = DEFAULT_PERIOD_MONTHS
        buySpreadWon = ""
        sellSpreadWon = ""
        investStrategy = DEFAULT_INVEST_STRATEGY
        progressiveIncreasePercent = ""
        activePadIndex = null
    }

    // 패드가 순서대로 넘겨줄 입력란 목록 — 투자 전략이 "회차별 증가"일 때만
    // 회차별 증가율 필드가 끝에 추가됩니다.
    // ⚠️ 변경점: 금액(원) 필드들은 formatAsWon 기본값(true)을 그대로 두어 패드 안에서도
    // 천단위 콤마가 보이게 하고, 금액이 아닌 최소분할매수횟수(횟수)·회차별 증가율(%)만
    // formatAsWon = false로 명시해서 콤마 없이 그대로 보여줍니다.
    val padFields = buildList {
        add(BacktestPadField("budget", "예산 (원)", isDecimal = false, maxLength = 12, value = budget, onValueChange = { budget = it }))
        add(BacktestPadField("startRate", "시작 매입가 (원)", isDecimal = true, maxLength = 10, value = startRate, onValueChange = { startRate = it }))
        add(BacktestPadField("minInvestPerRound", "최소 투자금", isDecimal = false, maxLength = 12, value = minInvestPerRound, onValueChange = { minInvestPerRound = it }))
        add(BacktestPadField("maxInvestPerRound", "최대 투자금", isDecimal = false, maxLength = 12, value = maxInvestPerRound, onValueChange = { maxInvestPerRound = it }))
        add(BacktestPadField("minSplitCount", "최소분할매수횟수", isDecimal = false, maxLength = 3, value = minSplitCount, onValueChange = { minSplitCount = it }, formatAsWon = false))
        add(BacktestPadField("dropGapWon", "추가매수 하락폭(원)", isDecimal = true, maxLength = 8, value = dropGapWon, onValueChange = { dropGapWon = it }))
        add(BacktestPadField("riseGapWon", "매도 상승폭(원)", isDecimal = true, maxLength = 8, value = riseGapWon, onValueChange = { riseGapWon = it }))
        add(BacktestPadField("buySpreadWon", "매수 스프레드(원)", isDecimal = true, maxLength = 8, value = buySpreadWon, onValueChange = { buySpreadWon = it }))
        add(BacktestPadField("sellSpreadWon", "매도 스프레드(원)", isDecimal = true, maxLength = 8, value = sellSpreadWon, onValueChange = { sellSpreadWon = it }))
        if (investStrategy == BacktestInvestStrategy.PROGRESSIVE) {
            add(BacktestPadField("progressiveIncreasePercent", "회차별 증가율(%)", isDecimal = true, maxLength = 5, value = progressiveIncreasePercent, onValueChange = { progressiveIncreasePercent = it }, formatAsWon = false))
        }
    }

    // 필드 key로 패드를 열 때 쓸 인덱스를 찾습니다 (투자 전략에 따라 목록 길이가 바뀌므로
    // 고정 인덱스를 외우지 않고 매번 찾습니다).
    fun openPad(key: String) {
        val idx = padFields.indexOfFirst { it.key == key }
        if (idx >= 0) activePadIndex = idx
    }

    // "결과 데이터(uiState.result)"와 "지금 막 끝나서 이동하라는 1회성 신호"를 분리한
    // SharedFlow(navigateToResult)를 구독만 합니다 — 과거 이벤트가 재생될 일이 없으므로,
    // Input이 몇 번을 다시 떠도 이걸로 인해 저절로 다시 넘어가는 일은 없습니다.
    LaunchedEffect(Unit) {
        viewModel.navigateToResult.collect {
            onSubmit()
        }
    }

    // ✅ 통화가 바뀔 때만 호출 — 3/6/12개월 고점·저점을 전부 병렬로 미리 받아 둡니다.
    // 기간 칩(3/6/12개월)을 누르는 것은 이제 네트워크 요청이 아니라 캐시 조회이므로
    // periodMonths는 더 이상 이 LaunchedEffect의 키가 아닙니다.
    LaunchedEffect(currencyType) {
        viewModel.loadAllAvailability(currencyType.name)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(BgColor)
        ) {
            // 헤더
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(HeaderColor)
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                IconButton(onClick = onBackClick, modifier = Modifier.size(32.dp)) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = "뒤로가기",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "환테크 백테스트", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = HeaderSubColor)
                    Text(text = "시뮬레이션 설정", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
                // ⚠️ 신규 추가: 입력값 전체 초기화 버튼 (통화/기간/투자전략 포함 전부 기본 상태로)
                IconButton(onClick = { resetAll() }, modifier = Modifier.size(32.dp)) {
                    Icon(
                        imageVector = Icons.Rounded.RestartAlt,
                        contentDescription = "입력값 초기화",
                        tint = HeaderSubColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
                // ⚠️ 의도적 변경점: 목업에는 없는 버튼이지만, 지난 기록 화면(History)으로 바로 들어갈
                // 다른 진입점이 이 화면에는 없어 실용성을 위해 유지합니다 (마이페이지 하단 링크와는 별도).
                IconButton(onClick = onNavigateToHistory, modifier = Modifier.size(32.dp)) {
                    Icon(
                        imageVector = Icons.Rounded.History,
                        contentDescription = "지난 기록",
                        tint = HeaderSubColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(22.dp)
            ) {
                // 통화
                FieldGroup(label = "통화") {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        MockChip(
                            label = "달러 USD",
                            selected = currencyType == BacktestCurrencyType.USD,
                            onClick = { currencyType = BacktestCurrencyType.USD },
                            modifier = Modifier.weight(1f)
                        )
                        MockChip(
                            label = "엔화 JPY",
                            selected = currencyType == BacktestCurrencyType.JPY,
                            onClick = { currencyType = BacktestCurrencyType.JPY },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // 시뮬레이션 기간 + 선택 기간 고점·저점 카드
                FieldGroup(label = "시뮬레이션 기간") {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf(3, 6, 12).forEach { m ->
                                MockChip(
                                    label = "${m}개월",
                                    selected = periodMonths == m,
                                    onClick = { periodMonths = m },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                        // 캐시에서 현재 선택된 기간의 고점/저점을 즉시 꺼내 보여줍니다 (요청 없음).
                        val currentAvailability = availabilityByPeriod[periodMonths]
                        PeriodHighLowCard(
                            highRate = currentAvailability?.highRate,
                            lowRate = currentAvailability?.lowRate,
                            // 통화를 막 바꿔서 3개 기간을 병렬로 받아오는 "최초 로딩" 구간에만 표시되고,
                            // 이후 기간 칩을 누를 때는 캐시 조회라 로딩 표시가 뜨지 않습니다.
                            isLoading = availabilityLoading
                        )
                    }
                }

                // 시작 매입가 — 탭하면 전용 숫자 키패드가 열립니다
                // ⚠️ 변경점: 원단위 콤마 표시(예: 1,350.25)를 적용했습니다. 실제 저장되는
                // startRate 값(서버로 보내는 값)은 콤마 없는 순수 숫자 그대로입니다.
                FieldGroup(label = "시작 매입가 (원)", gap = 6.dp) {
                    MockValueBox(
                        value = formatBacktestAmount(startRate),
                        isEmpty = startRate.isEmpty(),
                        height = 48.dp,
                        fontSize = 17.sp,
                        onClick = { openPad("startRate") }
                    )
                    Text(
                        text = "이 가격에 도달하는 첫 시점에 1회차 매수를 실행합니다",
                        fontSize = 11.sp,
                        color = HintColor
                    )
                }

                // 예산
                FieldGroup(label = "예산 (원)", gap = 6.dp) {
                    MockValueBox(
                        value = formatBacktestAmount(budget),
                        isEmpty = budget.isEmpty(),
                        height = 48.dp,
                        fontSize = 17.sp,
                        onClick = { openPad("budget") }
                    )
                }

                // 최소 / 최대 투자금
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    FieldGroup(label = "최소 투자금", gap = 6.dp, modifier = Modifier.weight(1f)) {
                        MockValueBox(
                            value = formatBacktestAmount(minInvestPerRound),
                            isEmpty = minInvestPerRound.isEmpty(),
                            height = 46.dp,
                            fontSize = 15.sp,
                            onClick = { openPad("minInvestPerRound") }
                        )
                    }
                    FieldGroup(label = "최대 투자금", gap = 6.dp, modifier = Modifier.weight(1f)) {
                        MockValueBox(
                            value = formatBacktestAmount(maxInvestPerRound),
                            isEmpty = maxInvestPerRound.isEmpty(),
                            height = 46.dp,
                            fontSize = 15.sp,
                            onClick = { openPad("maxInvestPerRound") }
                        )
                    }
                }

                // 최소분할매수횟수 — 금액이 아니라 "횟수"라서 콤마를 넣지 않습니다.
                FieldGroup(label = "최소분할매수횟수", gap = 6.dp) {
                    MockValueBox(
                        value = minSplitCount,
                        isEmpty = minSplitCount.isEmpty(),
                        height = 46.dp,
                        fontSize = 15.sp,
                        onClick = { openPad("minSplitCount") }
                    )
                }

                // 추가매수 하락폭 / 매도 상승폭
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    FieldGroup(label = "추가매수 하락폭(원)", gap = 6.dp, modifier = Modifier.weight(1f)) {
                        MockValueBox(
                            value = formatBacktestAmount(dropGapWon),
                            isEmpty = dropGapWon.isEmpty(),
                            height = 46.dp,
                            fontSize = 15.sp,
                            onClick = { openPad("dropGapWon") }
                        )
                    }
                    FieldGroup(label = "매도 상승폭(원)", gap = 6.dp, modifier = Modifier.weight(1f)) {
                        MockValueBox(
                            value = formatBacktestAmount(riseGapWon),
                            isEmpty = riseGapWon.isEmpty(),
                            height = 46.dp,
                            fontSize = 15.sp,
                            onClick = { openPad("riseGapWon") }
                        )
                    }
                }

                // 매수 / 매도 스프레드 (목업과 동일하게 원단위)
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    FieldGroup(label = "매수 스프레드(원)", gap = 6.dp, modifier = Modifier.weight(1f)) {
                        MockValueBox(
                            value = formatBacktestAmount(buySpreadWon),
                            isEmpty = buySpreadWon.isEmpty(),
                            height = 46.dp,
                            fontSize = 15.sp,
                            onClick = { openPad("buySpreadWon") }
                        )
                    }
                    FieldGroup(label = "매도 스프레드(원)", gap = 6.dp, modifier = Modifier.weight(1f)) {
                        MockValueBox(
                            value = formatBacktestAmount(sellSpreadWon),
                            isEmpty = sellSpreadWon.isEmpty(),
                            height = 46.dp,
                            fontSize = 15.sp,
                            onClick = { openPad("sellSpreadWon") }
                        )
                    }
                }

                // 투자 전략
                FieldGroup(label = "투자 전략") {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(SegmentBgColor, RoundedCornerShape(12.dp))
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        MockSegment(
                            label = "균등분할",
                            selected = investStrategy == BacktestInvestStrategy.EQUAL,
                            onClick = { investStrategy = BacktestInvestStrategy.EQUAL },
                            modifier = Modifier.weight(1f)
                        )
                        MockSegment(
                            label = "회차별 증가",
                            selected = investStrategy == BacktestInvestStrategy.PROGRESSIVE,
                            onClick = { investStrategy = BacktestInvestStrategy.PROGRESSIVE },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    if (investStrategy == BacktestInvestStrategy.PROGRESSIVE) {
                        Column(
                            modifier = Modifier.padding(top = 4.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(text = "회차별 증가율(%)", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = LabelColor)
                            // 금액이 아니라 "퍼센트"라서 콤마를 넣지 않습니다.
                            MockValueBox(
                                value = progressiveIncreasePercent,
                                isEmpty = progressiveIncreasePercent.isEmpty(),
                                height = 46.dp,
                                fontSize = 15.sp,
                                onClick = { openPad("progressiveIncreasePercent") }
                            )
                            Text(
                                text = "직전 회차 투입액 대비 몇 % 더 투입할지 · 이 모드에서는 최대투자금 캡이 비활성화됩니다",
                                fontSize = 11.sp,
                                color = HintColor
                            )
                        }
                    }
                }

                uiState.errorMessage?.let { message ->
                    Text(
                        text = message,
                        color = HighColor,
                        fontSize = 13.sp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(ErrorBgColor, RoundedCornerShape(12.dp))
                            .padding(14.dp)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))
            }

            // 스티키 하단 제출 버튼 (목업: padding 16/20/24, border-top 1px, height 54, radius 14)
            Column(modifier = Modifier.fillMaxWidth()) {
                HorizontalDivider(thickness = 1.dp, color = CardBorderColor)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(BgColor)
                        .padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 24.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(HeaderColor)
                            .clickable(enabled = !uiState.isLoading) {
                                val request = BacktestRequest(
                                    currencyType = currencyType,
                                    budget = budget.toDoubleOrNull() ?: 0.0,
                                    startRate = startRate.toDoubleOrNull() ?: 0.0,
                                    minInvestPerRound = minInvestPerRound.toDoubleOrNull() ?: 0.0,
                                    maxInvestPerRound = maxInvestPerRound.toDoubleOrNull() ?: 0.0,
                                    minSplitCount = minSplitCount.toIntOrNull() ?: 1,
                                    dropGapWon = dropGapWon.toDoubleOrNull() ?: 0.0,
                                    riseGapWon = riseGapWon.toDoubleOrNull() ?: 0.0,
                                    periodMonths = periodMonths,
                                    buySpreadWon = buySpreadWon.toDoubleOrNull() ?: 0.0,
                                    sellSpreadWon = sellSpreadWon.toDoubleOrNull() ?: 0.0,
                                    investStrategy = investStrategy,
                                    progressiveIncreasePercent = if (investStrategy == BacktestInvestStrategy.PROGRESSIVE)
                                        progressiveIncreasePercent.toDoubleOrNull() else null
                                )
                                viewModel.runBacktest(request)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(text = "시뮬레이션 실행", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }
        }

        // ✅ 기록 화면의 숫자 키패드를 재사용한 백테스트 전용 패드 — 화면 맨 아래에 떠서
        // 필드를 순서대로 넘겨줍니다. activePadIndex가 null이면 아무것도 표시되지 않습니다.
        BacktestNumberPadOverlay(
            fields = padFields,
            activeIndex = activePadIndex,
            onActiveIndexChange = { activePadIndex = it },
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
private fun FieldGroup(
    label: String,
    modifier: Modifier = Modifier,
    gap: Dp = 8.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(gap)) {
        Text(text = label, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = LabelColor)
        content()
    }
}

@Composable
private fun MockChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(44.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(if (selected) HeaderColor else Color.White)
            .then(
                if (selected) Modifier
                else Modifier.border(1.dp, FieldBorderColor, RoundedCornerShape(10.dp))
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.SemiBold,
            color = if (selected) Color.White else LabelColor
        )
    }
}

@Composable
private fun MockSegment(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(40.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(if (selected) Color.White else Color.Transparent)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.SemiBold,
            color = if (selected) HeaderColor else HintColor
        )
    }
}

// ⚠️ 변경점: 기존 BasicTextField 직접입력(MockInput)을 제거하고, 탭하면 전용 숫자 키패드
// (BacktestNumberPadOverlay)가 열리는 "읽기 전용 표시 + 탭" 방식(MockValueBox)으로 바꿨습니다.
// ⚠️ 추가 변경점: 값이 이미 콤마 등으로 "화면 표시용"으로 포맷된 채로 넘어오므로,
// 빈 값 여부는 더 이상 value.isEmpty()로 판단하지 않고 호출부에서 isEmpty를 직접 넘겨받습니다
// (예: formatBacktestAmount("")는 ""를 그대로 반환하므로 사실 기존 判定로도 맞지만, 포맷 결과가
// 빈 문자열이 아닌데 실제로는 "비어있음"인 경우가 생기지 않도록 명시적으로 분리했습니다).
@Composable
private fun MockValueBox(
    value: String,
    isEmpty: Boolean,
    height: Dp,
    fontSize: TextUnit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .background(Color.White, RoundedCornerShape(12.dp))
            .border(1.dp, FieldBorderColor, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Text(
            text = if (isEmpty) "0" else value,
            fontSize = fontSize,
            fontWeight = FontWeight.SemiBold,
            color = if (isEmpty) HintColor else BodyTextColor
        )
    }
}

@Composable
private fun PeriodHighLowCard(
    highRate: Double?,
    lowRate: Double?,
    isLoading: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(12.dp))
            .border(1.dp, CardBorderColor, RoundedCornerShape(12.dp))
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(text = "선택 기간 고점 · 저점 (참고용)", fontSize = 11.sp, color = HintColor)
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(14.dp),
                        color = LabelColor,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = highRate?.let { "%,.2f".format(it) } ?: "-",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = HighColor
                    )
                    Text(text = " / ", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = SeparatorColor)
                    Text(
                        text = lowRate?.let { "%,.2f".format(it) } ?: "-",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = LowColor
                    )
                }
            }
        }
        Box(
            modifier = Modifier
                .size(34.dp)
                .background(IconBoxColor, RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.TrendingUp,
                contentDescription = null,
                tint = LabelColor,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}