package com.bobodroid.myapplication.components.mainComponents

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bobodroid.myapplication.models.datamodels.roomDb.ForeignCurrencyRecord
import com.bobodroid.myapplication.screens.RecordListEvent
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupChangeBottomSheet(
    sheetState: SheetState,
    record: ForeignCurrencyRecord,
    groupList: List<String>,
    onEvent: (RecordListEvent) -> Unit,
    onDismiss: () -> Unit
) {
    var showNewGroupInput by remember { mutableStateOf(false) }
    var newGroupName by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }

    // 포커스 자동 설정
    LaunchedEffect(showNewGroupInput) {
        if (showNewGroupInput) {
            delay(100)
            focusRequester.requestFocus()
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.White,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp)
        ) {
            // 헤더
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "그룹 변경",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1F2937)
                    )

                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = "닫기",
                            tint = Color(0xFF6B7280)
                        )
                    }
                }

                Text(
                    text = "이 기록을 이동할 그룹을 선택하세요",
                    fontSize = 14.sp,
                    color = Color(0xFF6B7280)
                )
            }

            HorizontalDivider(color = Color(0xFFE5E7EB))

            // 새 그룹 추가 영역
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp)
            ) {
                // 새 그룹 만들기 버튼
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            showNewGroupInput = !showNewGroupInput
                            if (!showNewGroupInput) {
                                newGroupName = ""
                            }
                        },
                    color = Color(0xFFF9FAFB),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = if (showNewGroupInput) Icons.Rounded.KeyboardArrowUp else Icons.Rounded.Add,
                            contentDescription = null,
                            tint = Color(0xFF6366F1),
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = if (showNewGroupInput) "입력 취소" else "새 그룹 만들기",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF6366F1)
                        )
                    }
                }

                // 인라인 입력 필드 (확장/축소)
                AnimatedVisibility(
                    visible = showNewGroupInput
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = newGroupName,
                            onValueChange = { newGroupName = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .focusRequester(focusRequester),
                            placeholder = {
                                Text(
                                    "그룹명을 입력하세요",
                                    color = Color(0xFF9CA3AF)
                                )
                            },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF6366F1),
                                unfocusedBorderColor = Color(0xFFE5E7EB),
                                focusedTextColor = Color(0xFF1F2937),
                                unfocusedTextColor = Color(0xFF1F2937)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            keyboardOptions = KeyboardOptions(
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    if (newGroupName.isNotBlank()) {
                                        onEvent(RecordListEvent.AddGroup(record, newGroupName))
                                        showNewGroupInput = false
                                        newGroupName = ""
                                        // onDismiss() 제거 - 바텀시트 유지
                                    }
                                }
                            )
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilledTonalButton(
                                onClick = {
                                    if (newGroupName.isNotBlank()) {
                                        onEvent(RecordListEvent.AddGroup(record, newGroupName))
                                        showNewGroupInput = false
                                        newGroupName = ""
                                        // onDismiss() 제거 - 바텀시트 유지
                                    }
                                },
                                enabled = newGroupName.isNotBlank(),
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.filledTonalButtonColors(
                                    containerColor = Color(0xFF6366F1),
                                    contentColor = Color.White,
                                    disabledContainerColor = Color(0xFFE5E7EB),
                                    disabledContentColor = Color(0xFF9CA3AF)
                                ),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("추가", fontSize = 14.sp)
                            }

                            OutlinedButton(
                                onClick = {
                                    showNewGroupInput = false
                                    newGroupName = ""
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = Color(0xFF374151)
                                ),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("취소", fontSize = 14.sp)
                            }
                        }
                    }
                }
            }

            // 그룹 리스트
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = false)
                    .heightIn(max = 400.dp),
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(groupList) { groupName ->
                    val isCurrentGroup = record.categoryName == groupName

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(enabled = !isCurrentGroup) {
                                onEvent(RecordListEvent.UpdateRecordCategory(record, groupName))
                                onDismiss()
                            },
                        color = if (isCurrentGroup) Color(0xFFEEF2FF) else Color.White,
                        shape = RoundedCornerShape(8.dp)
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
                                Icon(
                                    imageVector = Icons.Rounded.Folder,
                                    contentDescription = null,
                                    tint = if (isCurrentGroup) Color(0xFF6366F1) else Color(0xFF9CA3AF),
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = groupName,
                                    fontSize = 15.sp,
                                    fontWeight = if (isCurrentGroup) FontWeight.SemiBold else FontWeight.Normal,
                                    color = if (isCurrentGroup) Color(0xFF6366F1) else Color(0xFF1F2937)
                                )
                            }

                            if (isCurrentGroup) {
                                Icon(
                                    imageVector = Icons.Rounded.Check,
                                    contentDescription = "현재 그룹",
                                    tint = Color(0xFF6366F1),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}