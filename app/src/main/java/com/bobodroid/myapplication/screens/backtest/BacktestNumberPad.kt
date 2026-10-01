package com.bobodroid.myapplication.screens.backtest

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bobodroid.myapplication.components.Dialogs.ActionButton
import com.bobodroid.myapplication.components.Dialogs.FloatActionButton
import com.bobodroid.myapplication.components.Dialogs.FloatNumberButton
import com.bobodroid.myapplication.components.Dialogs.NumberButton
import com.bobodroid.myapplication.components.Dialogs.NumberButtonBottom
import com.bobodroid.myapplication.extensions.toLongWon
import com.bobodroid.myapplication.util.CalculateAction

/**
 * 백테스트 입력 화면이 다루는 숫자 입력란 한 개의 정의.
 *
 * @param isDecimal true면 소수점(.) 버튼이 있는 환율형 패드, false면 정수 전용
 *                  (00 / 000 빠른입력이 있는) 금액형 패드가 뜹니다.
 * @param formatAsWon true면 화면에 보여줄 때 천단위 콤마를 넣습니다(= "원" 단위 금액).
 *                     최소분할매수횟수(횟수)나 회차별 증가율(%)처럼 금액이 아닌 필드는
 *                     false로 넘겨 콤마 없이 그대로 보여줍니다. 입력 중인 실제 값(buffer/
 *                     onValueChange로 저장되는 값)은 이 설정과 무관하게 콤마 없는 순수
 *                     숫자 문자열 그대로 유지됩니다 — 이 플래그는 "표시 방식"만 바꿉니다.
 */
data class BacktestPadField(
    val key: String,
    val label: String,
    val isDecimal: Boolean,
    val maxLength: Int,
    val value: String,
    val onValueChange: (String) -> Unit,
    val formatAsWon: Boolean = true
)

// ⚠️ 신규 추가: 입력 중인 순수 숫자 문자열("4000000", "1350.25" 등)을 화면 표시용으로만
// 천단위 콤마가 들어간 문자열("4,000,000", "1,350.25")로 바꿔줍니다.
// - 정수부만 콤마를 넣고, 소수부(있다면)는 입력한 그대로 뒤에 붙입니다.
// - "1350." 처럼 소수점만 찍고 아직 소수부를 안 적은 중간 상태도 "1,350."으로 자연스럽게
//   보여줍니다.
// - 저장되는 실제 값(buffer, MockValueBox의 onValueChange로 넘어가는 값)에는 전혀
//   영향을 주지 않는, 순수 "표시 전용" 변환입니다.
internal fun formatBacktestAmount(raw: String): String {
    if (raw.isEmpty()) return ""
    val dotIndex = raw.indexOf('.')
    return if (dotIndex == -1) {
        raw.toLongOrNull()?.toLongWon() ?: raw
    } else {
        val intPart = raw.substring(0, dotIndex)
        val fracPart = raw.substring(dotIndex + 1)
        val formattedInt = intPart.toLongOrNull()?.toLongWon() ?: intPart.ifEmpty { "0" }
        "$formattedInt.$fracPart"
    }
}

/**
 * 기록 화면(AddBottomSheet)에서 쓰는 숫자 키패드 버튼들(NumberButton, ActionButton 등)을
 * 그대로 재사용한 백테스트 전용 키패드.
 *
 * - 필드를 하나 탭하면 그 필드부터 패드가 열리고, 패드 안의 "다음"으로 패드를 닫지 않은 채
 *   다음 입력란으로 바로 넘어갈 수 있습니다. "이전"으로 뒤로도 갈 수 있습니다.
 * - 상단 X 버튼: 지금 보고 있는 값까지 반영하고 바로 닫힙니다.
 * - 뒤로가기(시스템 백버튼/제스처): 화면을 나가지 않고 패드만 먼저 닫히도록 가로챕니다.
 * - 스크림(바깥 영역) 탭으로 닫는 기능은 의도적으로 넣지 않았습니다 — 숫자를 입력하는 도중
 *   실수로 바깥을 눌러 닫히는 걸 막기 위해 X 또는 "완료"로만 닫히게 했습니다.
 */
@Composable
fun BacktestNumberPadOverlay(
    fields: List<BacktestPadField>,
    activeIndex: Int?,
    onActiveIndexChange: (Int?) -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        modifier = modifier,
        visible = activeIndex != null,
        enter = slideInVertically(animationSpec = tween(220)) { it },
        exit = slideOutVertically(animationSpec = tween(220)) { it }
    ) {
        val index = activeIndex ?: return@AnimatedVisibility
        val field = fields[index]

        // 패드를 연 시점(= index가 바뀐 시점)의 필드 값으로 시작합니다.
        var buffer by remember(index) { mutableStateOf(field.value) }

        fun commit() {
            field.onValueChange(buffer)
        }

        fun close() {
            commit()
            onActiveIndexChange(null)
        }

        fun moveTo(nextIndex: Int) {
            commit()
            onActiveIndexChange(nextIndex)
        }

        BackHandler(enabled = true) { close() }

        // ⚠️ 변경점: 기존엔 정수형(금액) 필드만 toLongWon()으로 콤마를 넣고, 소수점이 있는
        // 필드(시작 매입가/하락폭/상승폭/스프레드 등)는 콤마 없이 그대로 보여줬습니다.
        // 이제는 소수점이 있어도 formatBacktestAmount()로 정수부에 콤마를 넣고, field의
        // formatAsWon이 false인 필드(최소분할매수횟수, 회차별 증가율)만 예외로 콤마 없이
        // 그대로 보여줍니다. 실제로 저장되는 값(buffer)은 그대로 콤마 없는 숫자입니다.
        val displayText = when {
            buffer.isEmpty() -> ""
            !field.formatAsWon -> buffer
            else -> formatBacktestAmount(buffer)
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                // ⚠️ 420dp였을 때는 그리드 + "이전" 줄까지 합친 실제 높이가 이 한도를 넘어서
                // "이전" 줄이 화면 밖으로 잘려 안 보이는 버그가 있었습니다. 여유를 두고 올림.
                .heightIn(max = 560.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF9FAFB)),
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
            elevation = CardDefaults.cardElevation(6.dp)
        ) {
            Column(modifier = Modifier.padding(bottom = 10.dp)) {
                // 상단 바 — 닫기(X) · 필드명 · 진행 상태(n / 총)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { close() }, modifier = Modifier.size(28.dp)) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = "닫기",
                            tint = Color(0xFF6B7280)
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = field.label,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1F2937),
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "${index + 1} / ${fields.size}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF9CA3AF)
                    )
                }

                BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                    val availableWidth = this.maxWidth
                    val itemSize = (availableWidth - (8.dp * 4)) / 5

                    LazyVerticalGrid(
                        modifier = Modifier.padding(horizontal = 15.dp),
                        columns = GridCells.Fixed(5),
                        userScrollEnabled = false,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        content = {

                            // 입력 중인 값 표시
                            item(span = { GridItemSpan(maxLineSpan) }) {
                                Row(
                                    verticalAlignment = Alignment.Bottom,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = displayText,
                                        fontSize = 34.sp,
                                        fontWeight = FontWeight.Bold,
                                        textAlign = TextAlign.End,
                                        lineHeight = 38.sp,
                                        maxLines = 1,
                                        color = Color(0xFF1F2937)
                                    )
                                }
                            }

                            // 모두삭제
                            item(span = { GridItemSpan(1) }) {
                                if (field.isDecimal) {
                                    FloatActionButton(itemSize, action = CalculateAction.AllClear, onClicked = { buffer = "" })
                                } else {
                                    ActionButton(itemSize, action = CalculateAction.AllClear, onClicked = { buffer = "" })
                                }
                            }

                            // 한 글자 지우기
                            item(span = { GridItemSpan(1) }) {
                                if (field.isDecimal) {
                                    FloatActionButton(itemSize, action = CalculateAction.Del, onClicked = {
                                        buffer = if (buffer.length <= 1) "" else buffer.dropLast(1)
                                    })
                                } else {
                                    ActionButton(itemSize, action = CalculateAction.Del, onClicked = {
                                        buffer = if (buffer.length <= 1) "" else buffer.dropLast(1)
                                    })
                                }
                            }

                            if (field.isDecimal) {
                                // 소수점
                                item(span = { GridItemSpan(1) }) {
                                    FloatActionButton(itemSize, action = CalculateAction.DOT, onClicked = {
                                        if (buffer.isNotEmpty() && !buffer.contains('.')) {
                                            buffer += "."
                                        }
                                    })
                                }
                                item(span = { GridItemSpan(2) }) {
                                    NavButton(
                                        itemSize = itemSize,
                                        label = if (index == fields.lastIndex) "완료" else "다음",
                                        onClicked = {
                                            if (index == fields.lastIndex) close() else moveTo(index + 1)
                                        }
                                    )
                                }
                            } else {
                                item(span = { GridItemSpan(3) }) {
                                    NavButton(
                                        itemSize = itemSize,
                                        label = if (index == fields.lastIndex) "완료" else "다음",
                                        onClicked = {
                                            if (index == fields.lastIndex) close() else moveTo(index + 1)
                                        }
                                    )
                                }
                            }

                            // 숫자 버튼 1~9, 0
                            items(listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "0")) { digit ->
                                if (field.isDecimal) {
                                    FloatNumberButton(itemSize, digit, onClicked = {
                                        if (buffer.length < field.maxLength) buffer += digit
                                    })
                                } else {
                                    NumberButton(itemSize, digit, onClicked = {
                                        if (buffer.length < field.maxLength) buffer += digit
                                    })
                                }
                            }

                            // 정수형(금액) 전용 빠른입력
                            if (!field.isDecimal) {
                                item(span = { GridItemSpan(2) }) {
                                    NumberButtonBottom(itemSize, number = "00", onClicked = {
                                        if (buffer.isNotEmpty() && buffer.length < field.maxLength) buffer += "00"
                                    }, fontSize = 22)
                                }
                                item(span = { GridItemSpan(3) }) {
                                    NumberButtonBottom(itemSize, number = "000", onClicked = {
                                        if (buffer.isNotEmpty() && buffer.length < field.maxLength) buffer += "000"
                                    }, fontSize = 22)
                                }
                            }

                            // 하단 여백
                            item(span = { GridItemSpan(5) }) {
                                Spacer(modifier = Modifier.height(4.dp))
                            }
                        }
                    )
                }

                // 이전 — 그리드 밖, 좌측 하단에 보조 링크 형태로 배치
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 15.dp, end = 15.dp, top = 4.dp, bottom = 0.dp),
                    horizontalArrangement = Arrangement.Start
                ) {
                    Text(
                        text = "◀ 이전",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (index == 0) Color(0xFFD1D5DB) else Color(0xFF4B5563),
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .then(
                                if (index == 0) Modifier
                                else Modifier.clickable { moveTo(index - 1) }
                            )
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun NavButton(itemSize: Dp, label: String, onClicked: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(itemSize),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF16233A)),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(0.dp),
        onClick = onClicked
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(text = label, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }
    }
}