package com.bobodroid.myapplication.components

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun ContentIcon(
    buyButtonClicked: () -> Unit,
    totalMoneyCheckClicked: () -> Unit,
    refreshClicked: () -> Unit
) {

    val scope = rememberCoroutineScope()

    var bottomRefreshPadding by remember { mutableStateOf(5) }

    var dropdownExpanded by remember { mutableStateOf(false) }

    var isVisible by remember {
        mutableStateOf(true)
    }

    LaunchedEffect(key1 = Unit, block = {
        scope.launch {
            delay(3000)
            isVisible = false
            bottomRefreshPadding = 0
        }
    })

    Row(
        modifier = Modifier
            .height(120.dp)
            .fillMaxWidth()
            .padding(start = 20.dp, bottom = 10.dp),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically

    ) {

        Spacer(modifier = Modifier.weight(1f))


        Box(
            modifier = Modifier
                .wrapContentSize(Alignment.TopEnd)
        ) {
            FloatingActionButton(
                onClick = {
                    dropdownExpanded = true
                },
                containerColor = MaterialTheme.colors.secondary,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .padding(bottom = 10.dp, end = 20.dp)
                    .height(60.dp)
                    .wrapContentWidth(),
            ) {
                Row(
                    Modifier
                        .wrapContentSize()
                        .padding(start = 17.dp, end = 17.dp),
                    horizontalArrangement = Arrangement.spacedBy(bottomRefreshPadding.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Menu,
                        contentDescription = "메뉴",
                        tint = Color.White
                    )
                    AnimatedVisibility(visible = isVisible) {
                        Text(text = "메뉴", color = Color.White, modifier = Modifier)
                    }

                }

            }

            DropdownMenu(
                scrollState = rememberScrollState(),
                modifier = Modifier

                    .wrapContentSize(),
                offset = DpOffset(x = 23.dp, y = 5.dp),
                expanded = dropdownExpanded,
                onDismissRequest = {
                    dropdownExpanded = false
                }
            ) {
                DropdownMenuItem(
                    modifier = Modifier
                        .fillMaxWidth(),
                    text = {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(),
                            contentAlignment = Alignment.TopStart
                        ) {
                            Text(
                                text = "매수 기록",
                                fontSize = 13.sp
                            )
                        }
                    }, onClick = {
                        dropdownExpanded = false
                        buyButtonClicked.invoke()
                    })


                DropdownMenuItem(
                    modifier = Modifier
                        .fillMaxWidth(),
                    text = {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(),
                            contentAlignment = Alignment.TopStart
                        ) {
                            Text(
                                text = "총 수익 조회",
                                fontSize = 13.sp
                            )
                        }
                    }, onClick = {
                        dropdownExpanded = false
                        totalMoneyCheckClicked.invoke()
                    })

                DropdownMenuItem(
                    modifier = Modifier
                        .fillMaxWidth(),
                    text = {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(),
                            contentAlignment = Alignment.TopStart
                        ) {
                            Text(
                                text = "새로고침",
                                fontSize = 13.sp
                            )
                        }
                    }, onClick = {
                        dropdownExpanded = false
                        refreshClicked.invoke()
                    })

            }

        }


    }


}