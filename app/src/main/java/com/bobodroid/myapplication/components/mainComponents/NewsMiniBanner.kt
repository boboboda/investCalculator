package com.bobodroid.myapplication.components.mainComponents

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bobodroid.myapplication.models.datamodels.response.NewsItem
import kotlinx.coroutines.delay

/**
 * 뉴스 미니 배너
 */
@Composable
fun NewsMiniBanner(
    newsList: List<NewsItem>,
    onNewsClick: (NewsItem) -> Unit,
    modifier: Modifier = Modifier
) {
    if (newsList.isEmpty()) {
        return
    }

    var currentIndex by remember { mutableStateOf(0) }

    // 자동 슬라이드 (5초 간격)
    LaunchedEffect(newsList) {
        while (true) {
            delay(5000)
            currentIndex = (currentIndex + 1) % newsList.size
        }
    }

    val currentNews = newsList[currentIndex]

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable { onNewsClick(currentNews) },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // 헤더
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Article,
                        contentDescription = null,
                        tint = Color(0xFF6366F1),
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "환율 뉴스",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1F2937)
                    )
                }

                Text(
                    text = "${currentIndex + 1}/${newsList.size}",
                    fontSize = 12.sp,
                    color = Color(0xFF9CA3AF)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 뉴스 콘텐츠
            AnimatedContent(
                targetState = currentNews,
                transitionSpec = {
                    fadeIn(animationSpec = tween(300)) togetherWith
                            fadeOut(animationSpec = tween(300))
                },
                label = "news_content"
            ) { news ->
                Column {
                    // 제목
                    Text(
                        text = news.title,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF1F2937),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // 요약
                    Text(
                        text = news.summary,
                        fontSize = 13.sp,
                        color = Color(0xFF6B7280),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // 하단 정보
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = news.source,
                            fontSize = 11.sp,
                            color = Color(0xFF9CA3AF)
                        )

                        // 감정 점수
                        val (icon, color, text) = when {
                            news.sentiment > 0.1 -> Triple(Icons.Rounded.TrendingUp, Color(0xFF10B981), "긍정")
                            news.sentiment < -0.1 -> Triple(Icons.Rounded.TrendingDown, Color(0xFFEF4444), "부정")
                            else -> Triple(null, Color(0xFF6B7280), "중립")
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(color.copy(alpha = 0.1f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            icon?.let {
                                Icon(
                                    imageVector = it,
                                    contentDescription = null,
                                    tint = color,
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                            Text(
                                text = text,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = color
                            )
                        }
                    }
                }
            }

            // 페이지 인디케이터
            if (newsList.size > 1) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(newsList.size) { index ->
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 3.dp)
                                .size(if (index == currentIndex) 8.dp else 6.dp)
                                .clip(RoundedCornerShape(50))
                                .background(
                                    if (index == currentIndex) Color(0xFF6366F1)
                                    else Color(0xFFE5E7EB)
                                )
                        )
                    }
                }
            }
        }
    }
}