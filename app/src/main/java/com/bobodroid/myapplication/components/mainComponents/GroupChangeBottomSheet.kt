// 📁 새 파일: app/src/main/java/com/bobodroid/myapplication/components/mainComponents/GroupChangeBottomSheet.kt

package com.bobodroid.myapplication.components.mainComponents

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bobodroid.myapplication.models.datamodels.roomDb.ForeignCurrencyRecord
import com.bobodroid.myapplication.screens.RecordListEvent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupChangeBottomSheet(
    sheetState: SheetState,
    record: ForeignCurrencyRecord,
    groupList: List<String>,
    onEvent: (RecordListEvent) -> Unit,
    onDismiss: () -> Unit
) {
    var showNewGroupDialog by remember { mutableStateOf(false) }

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

            // 새 그룹 추가 버튼
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        showNewGroupDialog = true
                    }
                    .padding(horizontal = 24.dp, vertical = 16.dp),
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
                        imageVector = Icons.Rounded.Add,
                        contentDescription = null,
                        tint = Color(0xFF6366F1),
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = "새 그룹 만들기",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF6366F1)
                    )
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

    // 새 그룹 추가 다이얼로그
    if (showNewGroupDialog) {
        var newGroupName by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showNewGroupDialog = false },
            title = {
                Text(
                    text = "새 그룹 만들기",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                OutlinedTextField(
                    value = newGroupName,
                    onValueChange = { newGroupName = it },
                    placeholder = { Text("그룹명을 입력하세요") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (newGroupName.isNotBlank()) {
                            onEvent(RecordListEvent.AddGroup(record, newGroupName))
                            showNewGroupDialog = false
                            onDismiss()
                        }
                    },
                    enabled = newGroupName.isNotBlank()
                ) {
                    Text("추가")
                }
            },
            dismissButton = {
                TextButton(onClick = { showNewGroupDialog = false }) {
                    Text("취소")
                }
            }
        )
    }
}