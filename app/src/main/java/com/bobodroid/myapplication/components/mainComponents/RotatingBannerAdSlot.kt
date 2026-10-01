package com.bobodroid.myapplication.components.mainComponents

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Autorenew
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.bobodroid.myapplication.components.BacktestBannerCard
import com.bobodroid.myapplication.util.AdMob.BannerAd
import kotlinx.coroutines.delay

private const val PROMO_DURATION_MS = 5_000L
private const val AD_DURATION_MS = 25_000L
private const val TRANSITION_MS = 350
private val SLIDE_DISTANCE = 48.dp

/**
 * 기록 화면 상단의 "환테크 시뮬레이터 배너"와 "실제 광고(AdMob)"를
 * 같은 자리에서 교대로 보여주는 슬롯.
 *
 * - 화면 진입 시 시뮬레이터 배너가 먼저 노출(기본 5초) → 광고로 슬라이드 전환(기본 25초) → 이후 같은 비율로 반복
 * - 오른쪽 아래 작은 전환 버튼으로 사용자가 언제든 수동으로 바꿀 수 있고, 수동 전환 시 자동 타이머도 그 시점부터 다시 시작됨
 * - 중요: BannerAd()는 항상 컴포지션에 유지됩니다 (alpha/위치만 바꿔서 숨김) — AdMob은 짧은 주기로
 *   광고 뷰를 새로 만들고 다시 loadAd() 하는 것을 금지하므로, 이 슬롯은 AdView를 절대 재생성/재로드하지 않습니다.
 *
 * @param adVisible 기존 adUiState.bannerAdState 값. false면 광고 자체를 아예 안 보여주는 설정이므로
 *                  이 경우엔 교체 로직 없이 시뮬레이터 배너만 고정 노출합니다.
 */
@Composable
fun RotatingBannerAdSlot(
    onNavigateToBacktest: () -> Unit,
    adVisible: Boolean,
) {
    if (!adVisible) {
        BacktestBannerCard(
            onClick = onNavigateToBacktest,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp)
        )
        return
    }

    // true = 시뮬레이터 배너가 보이는 상태, false = 광고가 보이는 상태
    var showingPromo by remember { mutableStateOf(true) }
    // 값이 바뀔 때마다 아래 LaunchedEffect가 취소되고 새로 시작됨 → 수동 전환 시 자동 타이머 리셋 용도
    var cycleToken by remember { mutableIntStateOf(0) }

    LaunchedEffect(showingPromo, cycleToken) {
        delay(if (showingPromo) PROMO_DURATION_MS else AD_DURATION_MS)
        showingPromo = !showingPromo
    }

    fun manualSwitch() {
        showingPromo = !showingPromo
        cycleToken++
    }

    val density = LocalDensity.current
    val slideDistancePx = with(density) { SLIDE_DISTANCE.toPx() }

    val promoAlpha by animateFloatAsState(
        targetValue = if (showingPromo) 1f else 0f,
        animationSpec = tween(TRANSITION_MS),
        label = "promoAlpha"
    )
    val promoOffsetY by animateFloatAsState(
        targetValue = if (showingPromo) 0f else -slideDistancePx,
        animationSpec = tween(TRANSITION_MS),
        label = "promoOffsetY"
    )
    val adAlpha by animateFloatAsState(
        targetValue = if (showingPromo) 0f else 1f,
        animationSpec = tween(TRANSITION_MS),
        label = "adAlpha"
    )
    val adOffsetY by animateFloatAsState(
        targetValue = if (showingPromo) slideDistancePx else 0f,
        animationSpec = tween(TRANSITION_MS),
        label = "adOffsetY"
    )

    Box(modifier = Modifier.fillMaxWidth()) {
        // 시뮬레이터 배너 레이어
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .zIndex(if (showingPromo) 1f else 0f)
                .graphicsLayer {
                    alpha = promoAlpha
                    translationY = promoOffsetY
                }
        ) {
            BacktestBannerCard(
                // 숨겨진 상태일 때는 클릭이 안 먹도록 (수동 전환 버튼과 터치 영역이 겹치지 않게)
                onClick = if (showingPromo) onNavigateToBacktest else ({ }),
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp)
            )
        }

        // 광고 레이어 — BannerAd()는 항상 컴포지션에 남아 있음 (재생성/재로드 방지)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .zIndex(if (showingPromo) 0f else 1f)
                .graphicsLayer {
                    alpha = adAlpha
                    translationY = adOffsetY
                }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(vertical = 8.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                BannerAd()
            }
        }

        // 수동 전환 버튼 (항상 최상단 zIndex, 두 레이어 중 어느 쪽이 보이든 눌릴 수 있게)
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .zIndex(2f)
                .padding(end = 14.dp, bottom = 6.dp)
                .size(26.dp)
                .clip(CircleShape)
                .background(Color(0xFF16233A).copy(alpha = 0.82f))
                .clickable(onClick = ::manualSwitch),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.Autorenew,
                contentDescription = "배너/광고 전환",
                tint = Color.White,
                modifier = Modifier.size(15.dp)
            )
        }
    }
}
