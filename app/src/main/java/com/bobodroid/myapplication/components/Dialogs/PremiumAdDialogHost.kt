package com.bobodroid.myapplication.components.Dialogs

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import com.bobodroid.myapplication.models.viewmodels.SharedViewModel

/**
 * 프리미엄 유도 / 리워드 안내 다이얼로그를 앱 전체에서 한 곳에서만 표시
 * - InvestAppScreen 의 Box 안에 한 번만 배치
 * - 각 화면의 PremiumPromptDialog / RewardAdInfoDialog 는 모두 삭제
 */
@Composable
fun PremiumAdDialogHost(sharedViewModel: SharedViewModel) {
    val context = LocalContext.current
    val showPremiumPrompt by sharedViewModel.showPremiumPrompt.collectAsState()
    val showRewardAdInfo by sharedViewModel.showRewardAdInfo.collectAsState()

    if (showPremiumPrompt) {
        PremiumPromptDialog(
            onWatchAd = { sharedViewModel.closePremiumPromptAndShowRewardDialog() },
            onDismiss = { sharedViewModel.closePremiumPrompt() }
        )
    }

    if (showRewardAdInfo) {
        RewardAdInfoDialog(
            onConfirm = { sharedViewModel.showRewardAdAndGrantPremium(context) },
            onDismiss = { sharedViewModel.closeRewardAdDialog() }
        )
    }
}
