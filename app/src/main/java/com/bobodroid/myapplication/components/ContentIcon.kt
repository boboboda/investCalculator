package com.bobodroid.myapplication.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun ContentIcon(
    addRecordClicked: () -> Unit  // buyButtonClicked → addRecordClicked로 이름 변경
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(end = 20.dp, bottom = 20.dp),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.Bottom
    ) {
        FloatingActionButton(
            onClick = addRecordClicked,
            containerColor = Color(0xFF6366F1),  // GroupChangeBottomSheet 색상 통일
            contentColor = Color.White,
            elevation = FloatingActionButtonDefaults.elevation(
                defaultElevation = 4.dp,
                pressedElevation = 8.dp
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.Add,
                contentDescription = "기록 추가",
                modifier = Modifier.size(28.dp)
            )
        }
    }
}