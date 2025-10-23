package com.bobodroid.myapplication.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.bobodroid.myapplication.components.SocialLoginStatusCard
import com.bobodroid.myapplication.components.SocialLoginWarningBanner
import com.bobodroid.myapplication.models.datamodels.roomDb.LocalUserData
import com.bobodroid.myapplication.models.viewmodels.MyPageViewModel
import com.bobodroid.myapplication.routes.MyPageRoute
import com.bobodroid.myapplication.routes.RouteAction
import kotlinx.coroutines.launch

/**
 * 클라우드 백업 화면 (완전 개선 버전)
 * - 소셜 로그인 상태 확인
 * - 마지막 백업 시간 표시
 * - 수동 백업/복구 버튼
 * - 프리미엄 사용자는 자동 백업 안내
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CloudView(
    routeAction: RouteAction<MyPageRoute>,
    localUser: LocalUserData,
    myPageViewModel: MyPageViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // ✅ 소셜 로그인 상태 확인
    val isSocialLinked = localUser.socialType != "NONE" && !localUser.socialId.isNullOrEmpty()

    // ✅ 프리미엄 상태 확인
    val myPageUiState by myPageViewModel.myPageUiState.collectAsState()
    val isPremium = myPageUiState.localUser.isPremium

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("클라우드 백업") },
                navigationIcon = {
                    IconButton(onClick = { routeAction.goBack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "뒤로가기"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF6366F1),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFFF9FAFB))
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ✅ 소셜 로그인 경고 배너 (미연결 시)
            SocialLoginWarningBanner(
                isSocialLinked = isSocialLinked,
                onLinkClick = {
                    routeAction.navTo(MyPageRoute.AccountManage)
                }
            )

            // ✅ 소셜 로그인 상태 카드
            SocialLoginStatusCard(
                isSocialLinked = isSocialLinked,
                email = localUser.email
            )

            // ✅ 백업 상태 카드
            BackupStatusCard(
                isSocialLinked = isSocialLinked,
                isPremium = isPremium,
                lastSyncAt = localUser.lastSyncAt
            )

            // ✅ 백업 기능 카드 (수정됨)
            if (isSocialLinked) {
                BackupActionsCard(
                    onBackupClick = {
                        coroutineScope.launch {
                            myPageViewModel.syncToServer { message ->
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar(
                                        message = message,
                                        duration = SnackbarDuration.Short
                                    )
                                }
                            }
                        }
                    },
                    onRestoreClick = {
                        coroutineScope.launch {
                            myPageViewModel.restoreFromServer { message ->
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar(
                                        message = message,
                                        duration = SnackbarDuration.Short
                                    )
                                }
                            }
                        }
                    },
                    isPremium = isPremium
                )
            }

            // ✅ 백업 안내 카드
            BackupInfoCard()

            // ✅ 프리미엄 안내 (일반 사용자만)
            if (!isPremium && isSocialLinked) {
                PremiumBackupPromotionCard(
                    onUpgradeClick = {
                        routeAction.navTo(MyPageRoute.Premium)
                    }
                )
            }
        }
    }
}

/**
 * 백업 상태 카드
 */
@Composable
fun BackupStatusCard(
    isSocialLinked: Boolean,
    isPremium: Boolean,
    lastSyncAt: String?
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.CloudDone,
                    contentDescription = null,
                    tint = Color(0xFF6366F1),
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = "백업 상태",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1F2937)
                )
            }

            // 백업 타입
            StatusRow(
                label = "백업 방식",
                value = if (isPremium) "자동 백업 (실시간)" else "수동 백업",
                valueColor = if (isPremium) Color(0xFF10B981) else Color(0xFF6B7280)
            )

            // 마지막 백업 시간
            StatusRow(
                label = "마지막 백업",
                value = when {
                    !isSocialLinked -> "백업 기록 없음"
                    lastSyncAt.isNullOrEmpty() -> "아직 백업하지 않음"
                    else -> formatSyncTime(lastSyncAt)
                },
                valueColor = Color(0xFF6B7280)
            )

            // 백업 위치
            if (isSocialLinked) {
                StatusRow(
                    label = "백업 위치",
                    value = "서버 (안전하게 보관됨)",
                    valueColor = Color(0xFF6366F1)
                )
            }
        }
    }
}

@Composable
fun StatusRow(
    label: String,
    value: String,
    valueColor: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            color = Color(0xFF6B7280)
        )
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = valueColor
        )
    }
}

/**
 * 백업 기능 버튼 카드
 */
@Composable
fun BackupActionsCard(
    onBackupClick: () -> Unit,
    onRestoreClick: () -> Unit,
    isPremium: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "백업 관리",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1F2937)
            )

            // 수동 백업 버튼
            Button(
                onClick = onBackupClick,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF6366F1)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(vertical = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.CloudUpload,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isPremium) "지금 백업하기" else "수동 백업",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // 복구 버튼
            OutlinedButton(
                onClick = onRestoreClick,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Color(0xFF6366F1)
                )
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(vertical = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.CloudDownload,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "백업 복구",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

/**
 * 백업 안내 카드
 */
@Composable
fun BackupInfoCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFEEF2FF)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Info,
                    contentDescription = null,
                    tint = Color(0xFF6366F1),
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "백업 안내",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1F2937)
                )
            }

            BackupInfoItem("소셜 로그인 연동 시 서버에 안전하게 백업됩니다")
            BackupInfoItem("기기 변경, 앱 재설치 시에도 데이터 복구 가능")
            BackupInfoItem("프리미엄 사용자는 실시간 자동 백업 지원")
            BackupInfoItem("일반 사용자는 수동으로 백업해야 합니다")
        }
    }
}

@Composable
fun BackupInfoItem(text: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = "•",
            fontSize = 14.sp,
            color = Color(0xFF6366F1),
            modifier = Modifier.padding(end = 8.dp)
        )
        Text(
            text = text,
            fontSize = 13.sp,
            color = Color(0xFF4B5563),
            lineHeight = 18.sp,
            modifier = Modifier.weight(1f)
        )
    }
}

/**
 * 프리미엄 백업 홍보 카드
 */
@Composable
fun PremiumBackupPromotionCard(
    onUpgradeClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF6366F1)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Rounded.Star,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = Color(0xFFFBBF24)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "프리미엄으로\n자동 백업 사용하기",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "수동 백업의 번거로움 없이\n실시간으로 자동 백업됩니다",
                fontSize = 13.sp,
                color = Color.White.copy(alpha = 0.9f),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = onUpgradeClick,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = Color(0xFF6366F1)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Star,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "프리미엄 알아보기",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

private fun formatSyncTime(lastSyncAt: String): String {
    return try {
        val formatter = java.text.SimpleDateFormat(
            "yyyy-MM-dd'T'HH:mm:ss",
            java.util.Locale.getDefault()
        )
        val syncDate = formatter.parse(lastSyncAt) ?: return "알 수 없음"
        val now = java.util.Date()

        val diffMillis = now.time - syncDate.time
        val diffSeconds = diffMillis / 1000
        val diffMinutes = diffSeconds / 60
        val diffHours = diffMinutes / 60
        val diffDays = diffHours / 24

        when {
            diffSeconds < 60 -> "방금 전"
            diffMinutes < 60 -> "${diffMinutes}분 전"
            diffHours < 24 -> "${diffHours}시간 전"
            diffDays < 7 -> "${diffDays}일 전"
            diffDays < 30 -> "${diffDays / 7}주 전"
            diffDays < 365 -> "${diffDays / 30}개월 전"
            else -> "${diffDays / 365}년 전"
        }
    } catch (e: Exception) {
        "알 수 없음"
    }
}