package com.bobodroid.myapplication.components.Dialogs

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.rounded.ArrowForward
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.bobodroid.myapplication.R
import com.bobodroid.myapplication.components.CardIconButton
import com.bobodroid.myapplication.components.IconButton

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PermissionGuideDialog(
    closeClicked: (Boolean) -> Unit,
    dateDelaySelected: () -> Unit,
) {

    val pagerState = rememberPagerState(pageCount = { 6 })

    Dialog(
        onDismissRequest = { },
        properties = DialogProperties()
    ) {
        Column(
            modifier = Modifier
                .clip(shape = RoundedCornerShape(20.dp))
                .background(Color.White)
                .fillMaxHeight(0.8f)
                .fillMaxWidth(1f),
            horizontalAlignment = Alignment.Start
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
                horizontalArrangement = Arrangement.End
            ) {
                CardIconButton(
                    imageVector = Icons.Filled.Close,
                    onClicked = { closeClicked.invoke(false) },
                    modifier = Modifier,
                    buttonColor = Color.White
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.Start
            ) {
                HorizontalPager(state = pagerState) { page ->

                    when (page) {
                        0 -> {

                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                            ) {
                                Image(
                                    modifier = Modifier
                                        .weight(0.7f),
                                    painter = painterResource(id = R.drawable.permission_1),
                                    contentDescription = "",
                                )
                                Column(
                                    Modifier
                                        .fillMaxHeight()
                                        .weight(0.3f)
                                        .padding(top = 30.dp, end = 5.dp),
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = "검색 창에서 설정을 검색하여 실행시켜주세요",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Medium,
                                        lineHeight = 25.sp
                                    )
                                }
                            }

                        }

                        1 -> {
                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                            ) {
                                Image(
                                    modifier = Modifier
                                        .weight(0.7f),
                                    painter = painterResource(id = R.drawable.permission_2),
                                    contentDescription = "",
                                )
                                Column(
                                    Modifier
                                        .fillMaxHeight()
                                        .weight(0.3f)
                                        .padding(top = 30.dp, end = 5.dp),
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = "설정 창에 들어온 후 애플리케이션을 실행시켜주세요",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Medium,
                                        lineHeight = 25.sp
                                    )
                                }
                            }

                        }

                        2 -> {
                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                            ) {
                                Image(
                                    modifier = Modifier
                                        .weight(0.7f),
                                    painter = painterResource(id = R.drawable.permission_3),
                                    contentDescription = "",
                                )
                                Column(
                                    Modifier
                                        .fillMaxHeight()
                                        .weight(0.3f)
                                        .padding(top = 30.dp, end = 5.dp),
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = "애플리케이션 창에서 다시 검색창을 클릭해주세요",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Medium,
                                        lineHeight = 25.sp
                                    )
                                }
                            }
                        }

                        3 -> {
                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                            ) {
                                Image(
                                    modifier = Modifier
                                        .weight(0.7f),
                                    painter = painterResource(id = R.drawable.permission_4),
                                    contentDescription = "",
                                )
                                Column(
                                    Modifier
                                        .fillMaxHeight()
                                        .weight(0.3f)
                                        .padding(top = 30.dp, end = 5.dp),
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = "달러기록을 검색 후 클릭해주세요",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Medium,
                                        lineHeight = 25.sp
                                    )
                                }
                            }
                        }

                        4 -> {
                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                            ) {
                                Image(
                                    modifier = Modifier
                                        .weight(0.7f),
                                    painter = painterResource(id = R.drawable.permission_5),
                                    contentDescription = "",
                                )
                                Column(
                                    Modifier
                                        .fillMaxHeight()
                                        .weight(0.3f)
                                        .padding(top = 30.dp, end = 5.dp),
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = "달러 기록 알림을 클릭해주세요",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Medium,
                                        lineHeight = 25.sp
                                    )
                                }
                            }
                        }

                        5 -> {
                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                            ) {
                                Image(
                                    modifier = Modifier
                                        .weight(0.7f),
                                    painter = painterResource(id = R.drawable.permission_6),
                                    contentDescription = "",
                                )
                                Column(
                                    Modifier
                                        .fillMaxHeight()
                                        .weight(0.3f)
                                        .padding(top = 30.dp, end = 5.dp),
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = "알림 허용 탭바를 허용으로 바꿔주세요\n" +
                                                "바꾼 후 앱을 다시 실행시켜주세요",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Medium,
                                        lineHeight = 25.sp
                                    )
                                }
                            }
                        }
                    }
                }

            }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 60.dp)
                .wrapContentHeight(),
            horizontalArrangement = Arrangement.spacedBy(5.dp, Alignment.End),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if(pagerState.currentPage == 5) {
                Text(text = "끝", Modifier.padding(end = 15.dp), fontWeight = FontWeight.Bold)
            } else {
                Text(text = "이미지를 오른쪽으로 슬라이드해주세요")

                IconButton(
                    imageVector = Icons.Rounded.ArrowForward,
                    onClicked = {

                    }, modifier = Modifier
                )
            }

        }
    }
}
}