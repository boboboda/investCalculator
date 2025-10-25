package com.bobodroid.myapplication.lists.foreignCurrencyList

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.DismissDirection
import androidx.compose.material.DismissValue
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.FractionalThreshold
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.SwipeToDismiss
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.rounded.*
import androidx.compose.material.rememberDismissState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bobodroid.myapplication.MainActivity.Companion.TAG
import com.bobodroid.myapplication.components.Dialogs.AskTriggerDialog
import com.bobodroid.myapplication.components.Dialogs.TextFieldDialog
import com.bobodroid.myapplication.domain.entity.RecordEntity
import com.bobodroid.myapplication.extensions.formatWithCurrencyType
import com.bobodroid.myapplication.extensions.toBigDecimalUs
import com.bobodroid.myapplication.extensions.toBigDecimalWon
import com.bobodroid.myapplication.extensions.toBigDecimalYen
import com.bobodroid.myapplication.models.datamodels.roomDb.CurrencyRecord
import com.bobodroid.myapplication.models.datamodels.roomDb.CurrencyType
import com.bobodroid.myapplication.models.datamodels.roomDb.ForeignCurrencyRecord
import com.bobodroid.myapplication.screens.RecordListEvent
import com.bobodroid.myapplication.ui.theme.DeleteColor
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun RecordListRowView(
    currencyType: CurrencyType,
    data: RecordEntity,
    sellState: Boolean = data.recordColor!!,
    groupList: List<String>,
    onEvent: (RecordListEvent) -> Unit,
    scrollEvent: () -> Unit
) {
    val mathContext = MathContext(28, RoundingMode.HALF_UP)

    var itemRowVisible by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current
    val deleteAskDialog = remember { mutableStateOf(false) }
    val dismissState = rememberDismissState()
    var dropdownExpanded by remember { mutableStateOf(false) }
    var groupDropdownExpanded by remember { mutableStateOf(false) }
    var memoTextInput by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()
    var groupAddDialog by remember { mutableStateOf(false) }
    val focusRequester by remember { mutableStateOf(FocusRequester()) }

    // 통화 금액 포맷팅
    val foreignCurrencyMoney = data.exchangeMoney ?: "0"

    // ✅ 원화 금액 포맷팅 추가
    val wonMoney = BigDecimal(data.money, mathContext)
        .setScale(0, RoundingMode.DOWN)
        .toBigDecimalWon()

    // 수익 계산
    val profit = if (!data.recordColor!!) {
        if (data.profit.isNullOrEmpty()) "0" else data.profit
    } else {
        if (data.sellProfit.isNullOrEmpty()) "0" else data.sellProfit
    }

    val profitValue = BigDecimal(profit, mathContext)
    val isProfit = profitValue.signum() >= 0

    val profitColor = if (isProfit) Color(0xFFEF4444) else Color(0xFF2563EB)

    // 날짜 표시
    val displayDate = if (data.recordColor!!) {
        data.sellDate ?: data.date
    } else {
        data.date
    }

    // 환율 표시
    val displayRate = if (data.recordColor!!) {
        data.sellRate ?: data.rate
    } else {
        data.rate
    }

    LaunchedEffect(key1 = data.memo) {
        memoTextInput = data.memo ?: ""
    }

    if (dismissState.isDismissed(DismissDirection.StartToEnd)) {
        LaunchedEffect(key1 = Unit) {
            Log.d(TAG("TotalLineDrRecord",""), "스와이프 이벤트")
            dismissState.reset()
            deleteAskDialog.value = true
        }
    }

    SwipeToDismiss(
        state = dismissState,
        modifier = Modifier,
        directions = setOf(DismissDirection.StartToEnd),
        dismissThresholds = { FractionalThreshold(0.40f) },
        background = {
            val color by animateColorAsState(
                when (dismissState.targetValue) {
                    DismissValue.Default -> Color.White
                    else -> DeleteColor
                }
            )
            val scale by animateFloatAsState(
                if (dismissState.targetValue == DismissValue.Default) 0.75f else 1f
            )

            Box(
                Modifier
                    .fillMaxSize()
                    .background(color)
                    .padding(horizontal = Dp(20f)),
                contentAlignment = Alignment.CenterStart
            ) {
                Image(
                    Icons.Default.Delete,
                    contentDescription = "Delete Icon",
                    modifier = Modifier.scale(scale)
                )
            }
        },
        dismissContent = {
            // 🎨 매도 완료 카드 디자인 개선
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                // 좌측 accent bar (매도 완료 시만 표시)
                if (sellState) {
                    Box(
                        modifier = Modifier
                            .width(4.dp)
                            .fillMaxHeight()
                            .background(
                                color = Color(0xFF10B981),
                                shape = RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp)
                            )
                            .align(Alignment.CenterStart)
                    )
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .then(
                            if (sellState) {
                                Modifier.border(
                                    width = 1.dp,
                                    color = Color(0xFF10B981).copy(alpha = 0.3f),
                                    shape = RoundedCornerShape(16.dp)
                                )
                            } else Modifier
                        ),
                    elevation = CardDefaults.cardElevation(if (sellState) 6.dp else 4.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (sellState) Color(0xFFECFDF5) else Color.White
                    ),
                    onClick = {
                        if (!itemRowVisible) {
                            coroutineScope.launch {
                                itemRowVisible = true  // 열기
                                scrollEvent()
                            }
                        } else {
                            focusManager.clearFocus()
                            itemRowVisible = false  // ✅ 닫기 추가
                        }
                    }
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        // 🎨 헤더: 날짜 + 매도완료 뱃지
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            // 왼쪽: 날짜 + 매도완료 뱃지
                            Column(
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.CalendarToday,
                                        contentDescription = null,
                                        tint = if (sellState) Color(0xFF059669) else Color(0xFF6B7280),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = displayDate ?: "",
                                        fontSize = 13.sp,
                                        color = if (sellState) Color(0xFF059669) else Color(0xFF6B7280),
                                        fontWeight = if (sellState) FontWeight.SemiBold else FontWeight.Medium
                                    )
                                }

                                // 매도 상태 뱃지
                                if (sellState) {
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = Color(0xFF10B981)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Rounded.CheckCircle,
                                                contentDescription = null,
                                                tint = Color.White,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Text(
                                                text = "매도완료",
                                                fontSize = 12.sp,
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // 🎨 금액 및 환율 정보
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "매수금액",
                                    fontSize = 11.sp,
                                    color = Color(0xFF9CA3AF),
                                    fontWeight = FontWeight.Medium
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                // ✅ 원화 금액 (메인)
                                Text(
                                    text = wonMoney,
                                    fontSize = 15.sp,
                                    color = Color(0xFF1F2937),
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                // ✅ 달러/엔화 금액 (보조)
                                Text(
                                    text = foreignCurrencyMoney.formatWithCurrencyType(currencyType),
                                    fontSize = 12.sp,
                                    color = Color(0xFF9CA3AF),
                                    fontWeight = FontWeight.Normal
                                )
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "매수환율",
                                    fontSize = 11.sp,
                                    color = Color(0xFF9CA3AF),
                                    fontWeight = FontWeight.Medium
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = displayRate ?: "",
                                    fontSize = 15.sp,
                                    color = Color(0xFF1F2937),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        HorizontalDivider(color = Color(0xFFE5E7EB))
                        Spacer(modifier = Modifier.height(12.dp))

                        // 🎨 수익 정보
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (sellState) "실현수익" else "예상수익",
                                fontSize = if (sellState) 13.sp else 11.sp,
                                color = if (sellState) Color(0xFF059669) else Color(0xFF9CA3AF),
                                fontWeight = if (sellState) FontWeight.Bold else FontWeight.Medium
                            )

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = if (isProfit) Icons.Rounded.TrendingUp else Icons.Rounded.TrendingDown,
                                    contentDescription = null,
                                    tint = profitColor,
                                    modifier = Modifier.size(if (sellState) 20.dp else 16.dp)
                                )
                                Text(
                                    text = profitValue.toBigDecimalWon(),
                                    fontSize = if (sellState) 18.sp else 15.sp,
                                    color = profitColor,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // 🎨 확장 영역 (액션 버튼 먼저, 메모 나중)
                        AnimatedVisibility(visible = itemRowVisible) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 16.dp)
                            ) {
                                HorizontalDivider(color = Color(0xFFE5E7EB))
                                Spacer(modifier = Modifier.height(16.dp))

                                // 🎯 액션 버튼들 (메모보다 먼저)
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    // 첫 번째 줄: 수정 + 그룹 변경
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        OutlinedButton(
                                            onClick = {
                                                onEvent(RecordListEvent.ShowEditBottomSheet(data))
                                            },
                                            modifier = Modifier.weight(1f),
                                            enabled = !sellState,  // ← 매도 완료면 비활성화
                                            colors = ButtonDefaults.outlinedButtonColors(
                                                contentColor = Color(0xFF6366F1),
                                                disabledContentColor = Color(0xFF9CA3AF)  // ← 비활성화 색상
                                            ),
                                            border = BorderStroke(
                                                1.dp,
                                                if (sellState) Color(0xFFE5E7EB) else Color(0xFF6366F1)  // ← 비활성화 테두리
                                            ),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Rounded.Edit,
                                                contentDescription = null,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("수정", fontSize = 14.sp)
                                        }

                                        OutlinedButton(
                                            onClick = {
                                                // 🎯 바텀시트로 변경
                                                onEvent(RecordListEvent.ShowGroupChangeBottomSheet(data))
                                            },
                                            modifier = Modifier.weight(1f),
                                            colors = ButtonDefaults.outlinedButtonColors(
                                                contentColor = Color(0xFF8B5CF6)
                                            ),
                                            border = BorderStroke(1.dp, Color(0xFF8B5CF6)),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Rounded.Folder,
                                                contentDescription = null,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("그룹", fontSize = 14.sp)
                                        }
                                    }

                                    // 두 번째 줄: 매도/매도취소 + 삭제
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        if (sellState) {
                                            // 매도 완료 상태: 매도 취소 버튼
                                            OutlinedButton(
                                                onClick = {
                                                    onEvent(RecordListEvent.CancelSellRecord(data.id))
                                                },
                                                modifier = Modifier.weight(1f),
                                                colors = ButtonDefaults.outlinedButtonColors(
                                                    contentColor = Color(0xFFF59E0B)
                                                ),
                                                border = BorderStroke(1.dp, Color(0xFFF59E0B)),
                                                shape = RoundedCornerShape(8.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Rounded.Undo,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text("매도취소", fontSize = 14.sp)
                                            }
                                        } else {
                                            // 미매도 상태: 매도 버튼
                                            FilledTonalButton(
                                                onClick = {
                                                    onEvent(RecordListEvent.SellRecord(data))
                                                },
                                                modifier = Modifier.weight(1f),
                                                colors = ButtonDefaults.filledTonalButtonColors(
                                                    containerColor = Color(0xFF10B981).copy(alpha = 0.1f),
                                                    contentColor = Color(0xFF10B981)
                                                ),
                                                shape = RoundedCornerShape(8.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Rounded.TrendingUp,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text("매도", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }

                                        // 삭제 버튼 (항상 표시)
                                        OutlinedButton(
                                            onClick = {
                                                deleteAskDialog.value = true
                                            },
                                            modifier = Modifier.weight(1f),
                                            colors = ButtonDefaults.outlinedButtonColors(
                                                contentColor = Color(0xFFEF4444)
                                            ),
                                            border = BorderStroke(1.dp, Color(0xFFEF4444)),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Rounded.Delete,
                                                contentDescription = null,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("삭제", fontSize = 14.sp)
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))
                                HorizontalDivider(color = Color(0xFFE5E7EB))
                                Spacer(modifier = Modifier.height(16.dp))

                                // 메모 섹션 (액션 버튼 아래로 이동)
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Edit,
                                        contentDescription = null,
                                        tint = Color(0xFF6B7280),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = "메모",
                                        fontSize = 13.sp,
                                        color = Color(0xFF6B7280),
                                        fontWeight = FontWeight.Medium
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                OutlinedTextField(
                                    value = memoTextInput,
                                    onValueChange = {
                                        if (it.length <= 100) {
                                            memoTextInput = it
                                        } else {
                                            focusManager.clearFocus()
                                            onEvent(RecordListEvent.SnackBarEvent("100자 이하로 작성해주세요"))
                                        }
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .focusRequester(focusRequester),
                                    placeholder = {
                                        Text("메모를 입력하세요", fontSize = 13.sp)
                                    },
                                    textStyle = TextStyle(
                                        fontSize = 13.sp,
                                        color = Color(0xFF1F2937)
                                    ),
                                    colors = TextFieldDefaults.outlinedTextFieldColors(
                                        focusedBorderColor = Color(0xFF6366F1),
                                        unfocusedBorderColor = Color(0xFFE5E7EB)
                                    )
                                )

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "${memoTextInput.length}/100",
                                        fontSize = 11.sp,
                                        color = Color(0xFF9CA3AF)
                                    )

                                    Button(
                                        onClick = {
                                            onEvent(RecordListEvent.MemoUpdate(data, memoTextInput))
                                            focusManager.clearFocus()
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Color(0xFF6366F1)
                                        ),
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.Save,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("저장", fontSize = 13.sp)
                                    }
                                }
                            }
                        }
                    }
                }

                // 그룹 변경 드롭다운 (이것만 유지)
                Box {
                    DropdownMenu(
                        scrollState = rememberScrollState(),
                        modifier = Modifier
                            .wrapContentHeight()
                            .heightIn(max = 200.dp),
                        expanded = groupDropdownExpanded,
                        onDismissRequest = { groupDropdownExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = "새그룹",
                                    color = Color(0xFF6366F1),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            },
                            onClick = {
                                groupAddDialog = true
                            }
                        )

                        HorizontalDivider()

                        groupList.forEach { groupName ->
                            DropdownMenuItem(
                                text = { Text(text = groupName, fontSize = 13.sp) },
                                onClick = {
                                    onEvent(RecordListEvent.AddGroup(data, groupName))
                                    groupDropdownExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            // 다이얼로그들
            if (groupAddDialog) {
                TextFieldDialog(
                    onDismissRequest = { groupAddDialog = it },
                    placeholder = "새 그룹명을 작성해주세요",
                    onClickedLabel = "추가",
                    closeButtonLabel = "닫기",
                    onClicked = { name ->
                        onEvent(RecordListEvent.AddGroup(data, name))
                        groupAddDialog = false
                        groupDropdownExpanded = false
                    }
                )
            }

            if (deleteAskDialog.value) {
                AskTriggerDialog(
                    title = "삭제하시겠습니까?",
                    onClickedLabel = "예",
                    onDismissRequest = { deleteAskDialog.value = it }
                ) {
                    onEvent(RecordListEvent.RemoveRecord(data))
                }
            }
        }
    )
}