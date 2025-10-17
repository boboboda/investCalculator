package com.bobodroid.myapplication.lists.foreignCurrencyList

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import com.bobodroid.myapplication.extensions.toBigDecimalUs
import com.bobodroid.myapplication.extensions.toBigDecimalWon
import com.bobodroid.myapplication.extensions.toBigDecimalYen
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
    data: ForeignCurrencyRecord,
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
    val foreignCurrencyMoney = when(currencyType) {
        CurrencyType.USD -> {
            BigDecimal(data.exchangeMoney, mathContext)
                .setScale(0, RoundingMode.DOWN)
                .toBigDecimalUs()
        }
        CurrencyType.JPY -> {
            BigDecimal(data.exchangeMoney, mathContext)
                .setScale(0, RoundingMode.DOWN)
                .toBigDecimalYen()
        }
    }

    // 수익 계산
    val profit = if (!data.recordColor!!) {
        if (data.profit.isNullOrEmpty()) "0" else data.profit
    } else {
        if (data.sellProfit.isNullOrEmpty()) "0" else data.sellProfit
    }

    val profitValue = BigDecimal(profit, mathContext)
    val isProfit = profitValue.signum() >= 0

    val profitColor = if (isProfit) Color(0xFF10B981) else Color(0xFFEF4444)

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
            // 🎨 모던한 카드 디자인
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                elevation = CardDefaults.cardElevation(4.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (sellState) Color(0xFFF3F4F6) else Color.White
                ),
                onClick = {
                    if (!itemRowVisible) {
                        coroutineScope.launch {
                            itemRowVisible = true
                            scrollEvent()
                        }
                    } else {
                        focusManager.clearFocus()
                    }
                }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    // 🎨 헤더: 날짜 + 메뉴
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.CalendarToday,
                                contentDescription = null,
                                tint = Color(0xFF6B7280),
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = displayDate ?: "",
                                fontSize = 13.sp,
                                color = Color(0xFF6B7280),
                                fontWeight = FontWeight.Medium
                            )

                            // 매도 상태 뱃지
                            if (sellState) {
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = Color(0xFF10B981).copy(alpha = 0.1f)
                                ) {
                                    Text(
                                        text = "매도완료",
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        fontSize = 10.sp,
                                        color = Color(0xFF10B981),
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        // 메뉴 버튼
                        Box {
                            IconButton(onClick = { dropdownExpanded = true }) {
                                Icon(
                                    imageVector = Icons.Rounded.MoreVert,
                                    contentDescription = "메뉴",
                                    tint = Color(0xFF6B7280)
                                )
                            }

                            DropdownMenu(
                                expanded = dropdownExpanded,
                                onDismissRequest = { dropdownExpanded = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("매도", fontSize = 13.sp) },
                                    onClick = {
                                        dropdownExpanded = false
                                        if (!data.recordColor!!) {
                                            onEvent(RecordListEvent.SellRecord(data))
                                        } else {
                                            onEvent(RecordListEvent.SnackBarEvent("매도한 기록입니다."))
                                        }
                                    },
                                    leadingIcon = {
                                        Icon(Icons.Rounded.TrendingUp, null, modifier = Modifier.size(20.dp))
                                    }
                                )

                                DropdownMenuItem(
                                    text = { Text("수정", fontSize = 13.sp) },
                                    onClick = {
                                        dropdownExpanded = false
                                        onEvent(RecordListEvent.ShowEditBottomSheet(data))
                                    },
                                    leadingIcon = {
                                        Icon(Icons.Rounded.Edit, null, modifier = Modifier.size(20.dp))
                                    }
                                )

                                DropdownMenuItem(
                                    text = { Text("매도 취소", fontSize = 13.sp) },
                                    onClick = {
                                        coroutineScope.launch {
                                            dropdownExpanded = false
                                            if (data.recordColor == false) {
                                                onEvent(RecordListEvent.SnackBarEvent("매도한 기록이 없습니다."))
                                            } else {
                                                onEvent(RecordListEvent.CancelSellRecord(data.id))
                                            }
                                        }
                                    },
                                    leadingIcon = {
                                        Icon(Icons.Rounded.Close, null, modifier = Modifier.size(20.dp))
                                    }
                                )

                                DropdownMenuItem(
                                    text = { Text("그룹 변경", fontSize = 13.sp) },
                                    onClick = {
                                        groupDropdownExpanded = true
                                    },
                                    leadingIcon = {
                                        Icon(Icons.Rounded.Folder, null, modifier = Modifier.size(20.dp))
                                    }
                                )

                                Divider()

                                DropdownMenuItem(
                                    text = { Text("삭제", fontSize = 13.sp, color = Color(0xFFEF4444)) },
                                    onClick = {
                                        dropdownExpanded = false
                                        deleteAskDialog.value = true
                                    },
                                    leadingIcon = {
                                        Icon(Icons.Rounded.Delete, null, tint = Color(0xFFEF4444), modifier = Modifier.size(20.dp))
                                    }
                                )
                            }

                            // 그룹 변경 드롭다운
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
                                            onEvent(RecordListEvent.UpdateRecordCategory(data, groupName))
                                            dropdownExpanded = false
                                            groupDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // 🎨 금액 정보 그리드
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // 외화 금액
                        Column {
                            Text(
                                text = "보유량",
                                fontSize = 11.sp,
                                color = Color(0xFF9CA3AF),
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = foreignCurrencyMoney,
                                fontSize = 15.sp,
                                color = Color(0xFF1F2937),
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "₩${BigDecimal(data.money, mathContext).toBigDecimalWon()}",
                                fontSize = 11.sp,
                                color = Color(0xFF6B7280)
                            )
                        }

                        // 환율
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "환율",
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

                        // 수익
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = if (sellState) "실현수익" else "예상수익",
                                fontSize = 11.sp,
                                color = Color(0xFF9CA3AF),
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = if (isProfit) Icons.Rounded.TrendingUp else Icons.Rounded.TrendingDown,
                                    contentDescription = null,
                                    tint = profitColor,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = BigDecimal(profit, mathContext).toBigDecimalWon(),
                                    fontSize = 15.sp,
                                    color = profitColor,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    // 🎨 확장 영역 (메모)
                    AnimatedVisibility(visible = itemRowVisible) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 16.dp)
                        ) {
                            HorizontalDivider(color = Color(0xFFE5E7EB))

                            Spacer(modifier = Modifier.height(12.dp))

                            // 메모 레이블
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

                            // 메모 입력 필드
                            OutlinedTextField(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .focusRequester(focusRequester = focusRequester),
                                placeholder = {
                                    Text(text = "메모를 입력해주세요", fontSize = 14.sp)
                                },
                                value = memoTextInput,
                                onValueChange = {
                                    if (it.length <= 100) {
                                        memoTextInput = it
                                    } else {
                                        focusManager.clearFocus()
                                        onEvent(RecordListEvent.SnackBarEvent("100자 이하로 작성해주세요"))
                                    }
                                },
                                textStyle = TextStyle(
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Normal
                                ),
                                colors = TextFieldDefaults.outlinedTextFieldColors(
                                    focusedBorderColor = Color(0xFF6366F1),
                                    unfocusedBorderColor = Color(0xFFD1D5DB)
                                ),
                                shape = RoundedCornerShape(8.dp)
                            )

                            // 버튼 행
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${memoTextInput.length}/100",
                                    fontSize = 12.sp,
                                    color = Color(0xFF9CA3AF)
                                )

                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    TextButton(
                                        onClick = {
                                            itemRowVisible = false
                                            coroutineScope.launch {
                                                delay(300)
                                                memoTextInput = data.memo ?: ""
                                            }
                                        }
                                    ) {
                                        Text("닫기", fontSize = 13.sp)
                                    }

                                    Button(
                                        onClick = {
                                            onEvent(RecordListEvent.MemoUpdate(data, memoTextInput))
                                            focusManager.clearFocus()
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Color(0xFF6366F1)
                                        ),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text("저장", fontSize = 13.sp)
                                    }
                                }
                            }
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
                        dropdownExpanded = false
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