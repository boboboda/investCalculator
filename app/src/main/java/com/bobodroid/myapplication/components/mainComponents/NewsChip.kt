package com.bobodroid.myapplication.components.mainComponents

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Article
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

/**
 * 작은 칩 형태의 뉴스 컴포넌트
 */
@Composable
fun NewsChip(
    latestNewsTitle: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (latestNewsTitle.isNullOrBlank()) {
        return
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF8F9FF) // 연한 파란색 배경
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // 아이콘 + 제목
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // 뉴스 아이콘
                Icon(
                    imageVector = Icons.Rounded.Article,
                    contentDescription = null,
                    tint = Color(0xFF6366F1),
                    modifier = Modifier.size(20.dp)
                )

                // 제목
                Text(
                    text = latestNewsTitle,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF1F2937),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // 화살표 아이콘
            Icon(
                imageVector = Icons.Rounded.ChevronRight,
                contentDescription = "더보기",
                tint = Color(0xFF9CA3AF),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

/**
 * 애니메이션이 있는 뉴스 배너 (여러 뉴스 순환)
 *
 * - 섹션 제목(sectionTitle)을 배너 위에 함께 표시 (null/빈 문자열이면 제목 숨김)
 * - 뉴스가 없으면 제목과 배너 모두 표시하지 않음
 * - 제목은 최대 2줄, 2줄 높이를 항상 확보해서 뉴스가 바뀌어도 아래 화면이 흔들리지 않음
 * - 하단 점 표시로 현재 몇 번째 뉴스인지 표시
 */
@Composable
fun AnimatedNewsChip(
    newsTitles: List<String>,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    sectionTitle: String? = "📰 환율 뉴스"
) {
    if (newsTitles.isEmpty()) {
        return
    }

    var currentIndex by remember { mutableStateOf(0) }

    // 뉴스 목록이 바뀌면 처음부터 다시 시작, 2개 이상일 때만 5초마다 순환
    LaunchedEffect(newsTitles) {
        currentIndex = 0
        if (newsTitles.size < 2) return@LaunchedEffect

        while (true) {
            delay(5000)
            currentIndex = (currentIndex + 1) % newsTitles.size
        }
    }

    // 뉴스 개수가 줄어든 직후 한 프레임 동안 범위를 벗어나는 것을 방지
    val safeIndex = currentIndex.coerceIn(0, newsTitles.lastIndex)

    // 제목 2줄 높이 (글자 크기 설정이 바뀌어도 맞도록 sp 기준으로 계산)
    val titleLineHeight = 22.sp
    val twoLineHeight = with(LocalDensity.current) { (titleLineHeight * 2).toDp() }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(top = 16.dp)
    ) {
        // 섹션 제목 (다른 섹션 제목과 같은 스타일)
        if (!sectionTitle.isNullOrBlank()) {
            Text(
                text = sectionTitle,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1F2937),
                modifier = Modifier.padding(bottom = 12.dp)
            )
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFF8F9FF)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onClick) // Card 안쪽에 두어 물결 효과가 둥근 모서리 안에서만 표시됨
                    .padding(horizontal = 16.dp, vertical = 18.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 뉴스 아이콘 (원형 배경)
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF6366F1).copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Article,
                        contentDescription = null,
                        tint = Color(0xFF6366F1),
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                // 제목 + 점 표시
                Column(modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = twoLineHeight),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        AnimatedContent(
                            targetState = newsTitles[safeIndex],
                            transitionSpec = {
                                fadeIn(animationSpec = tween(300)) togetherWith
                                        fadeOut(animationSpec = tween(300))
                            },
                            label = "news_title"
                        ) { title ->
                            Text(
                                text = title,
                                fontSize = 16.sp,
                                lineHeight = titleLineHeight,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF1F2937),
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    if (newsTitles.size > 1) {
                        Spacer(modifier = Modifier.height(10.dp))

                        Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                            newsTitles.indices.forEach { index ->
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (index == safeIndex) Color(0xFF6366F1)
                                            else Color(0xFFD1D5DB)
                                        )
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // 화살표 아이콘
                Icon(
                    imageVector = Icons.Rounded.ChevronRight,
                    contentDescription = "뉴스 더보기",
                    tint = Color(0xFF9CA3AF),
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}
