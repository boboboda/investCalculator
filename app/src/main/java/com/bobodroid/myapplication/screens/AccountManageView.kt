package com.bobodroid.myapplication.screens

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bobodroid.myapplication.domain.entity.PremiumType
import com.bobodroid.myapplication.domain.entity.SocialType
import com.bobodroid.myapplication.domain.entity.UserEntity
import com.bobodroid.myapplication.routes.MyPageRoute
import com.bobodroid.myapplication.routes.RouteAction

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountManageView(
    routeAction: RouteAction<MyPageRoute>,
    localUser: UserEntity,
    onGoogleLogin: (Activity) -> Unit,
    onKakaoLogin: (Activity) -> Unit,
    onLogout: () -> Unit,
    onDeleteAccount: () -> Unit,  // ✅ 추가
    onUnlinkSocial: () -> Unit
) {
    val context = LocalContext.current
    val activity = context as? Activity
    var showUnlinkDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showFinalConfirmDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("계정 관리") },
                navigationIcon = {
                    IconButton(onClick = { routeAction.goBack() }) {
                        Icon(Icons.Default.ArrowBack, "뒤로가기")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFFF8F9FA))
                .verticalScroll(rememberScrollState())
        ) {

            Spacer(modifier = Modifier.height(16.dp))

            // ✅ 상태 카드
            AccountStatusCard(localUser = localUser)

            Spacer(modifier = Modifier.height(24.dp))

            // ✅ 로그인 버튼들
            if (localUser.socialType == SocialType.NONE) {
                // 미로그인 상태 - 로그인 버튼 표시
                SocialLoginSection(
                    onGoogleLogin = {
                        activity?.let { onGoogleLogin(it) }
                    },
                    onKakaoLogin = {
                        activity?.let { onKakaoLogin(it) }
                    }
                )
            } else {
                // 로그인된 상태 - 로그아웃 & 연동 해제 & 회원 탈퇴 버튼 표시
                LoggedInSection(
                    localUser = localUser,
                    onLogout = onLogout,
                    onUnlinkSocial = { showUnlinkDialog = true },
                    onDeleteAccount = { showDeleteDialog = true }  // ✅ 추가
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ✅ 혜택 안내
            BenefitsSection()
        }
    }

    // ✅ 연동 해제 확인 다이얼로그
    if (showUnlinkDialog) {
        AlertDialog(
            onDismissRequest = { showUnlinkDialog = false },
            title = { Text("소셜 연동 해제") },
            text = {
                Column {
                    Text("정말 소셜 연동을 해제하시겠습니까?")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "⚠️ 서버에 저장된 데이터도 모두 삭제됩니다.\n로컬 데이터(목표환율 등)는 유지됩니다.",
                        fontSize = 13.sp,
                        color = Color(0xFFE53935)
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showUnlinkDialog = false
                        onUnlinkSocial()
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = Color(0xFFE53935)
                    )
                ) {
                    Text("연동 해제")
                }
            },
            dismissButton = {
                TextButton(onClick = { showUnlinkDialog = false }) {
                    Text("취소")
                }
            }
        )
    }

    // 1차 확인 다이얼로그
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Rounded.Warning,
                    contentDescription = null,
                    tint = Color(0xFFEF4444),
                    modifier = Modifier.size(48.dp)
                )
            },
            title = {
                Text(
                    text = "회원 탈퇴",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text("정말 탈퇴하시겠습니까?\n모든 데이터가 삭제되며 복구할 수 없습니다.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        showFinalConfirmDialog = true
                    }
                ) {
                    Text("다음", color = Color(0xFFEF4444))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("취소")
                }
            }
        )
    }

    // 2차 최종 확인 다이얼로그
    if (showFinalConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showFinalConfirmDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Rounded.Warning,
                    contentDescription = null,
                    tint = Color(0xFFEF4444),
                    modifier = Modifier.size(48.dp)
                )
            },
            title = {
                Text(
                    text = "최종 확인",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    Text(
                        "다음 내용이 영구적으로 삭제됩니다:",
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("• 모든 환율 기록")
                    Text("• 알람 설정")
                    Text("• 소셜 로그인 연동")
                    Text("• 프리미엄 구독 정보")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "정말 진행하시겠습니까?",
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFEF4444)
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showFinalConfirmDialog = false
                        onDeleteAccount()
                    }
                ) {
                    Text("탈퇴하기", color = Color(0xFFEF4444), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showFinalConfirmDialog = false }) {
                    Text("취소")
                }
            }
        )
    }
}

/**
 * 상태 카드 컴포넌트
 */
@Composable
fun AccountStatusCard(localUser: UserEntity) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (localUser.socialType != SocialType.NONE) Color(0xFFE3F2FD) else Color.White
        ),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (localUser.socialType != SocialType.NONE) Icons.Default.CheckCircle else Icons.Default.CloudOff,
                contentDescription = null,
                tint = if (localUser.socialType != SocialType.NONE) Color(0xFF1976D2) else Color.Gray,
                modifier = Modifier.size(40.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(
                    text = if (localUser.socialType != SocialType.NONE) "소셜 계정 연동됨" else "미연동",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (localUser.socialType != SocialType.NONE)
                        Color.Black
                    else
                        Color.Gray
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = when (localUser.socialType) {
                        SocialType.GOOGLE -> "Google 계정으로 로그인됨"
                        SocialType.KAKAO  -> "Kakao 계정으로 로그인됨"
                        else -> "소셜 계정을 연동해주세요"
                    },
                    fontSize = 13.sp,
                    color = Color.Gray
                )

                localUser.email?.let { email ->
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = email,
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }
        }
    }
}

/**
 * 소셜 로그인 버튼 섹션
 */
@Composable
fun SocialLoginSection(
    onGoogleLogin: () -> Unit,
    onKakaoLogin: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Google 로그인 버튼
        Button(
            onClick = onGoogleLogin,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.White
            ),
            elevation = ButtonDefaults.buttonElevation(2.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "G",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF4285F4)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Google로 계속하기",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.Black
                )
            }
        }

        // Kakao 로그인 버튼
        Button(
            onClick = onKakaoLogin,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFFEE500)
            )
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "K",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Kakao로 계속하기",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.Black
                )
            }
        }
    }
}

/**
 * 로그인된 상태 섹션 (로그아웃 + 연동 해제 + 회원 탈퇴)
 */
@Composable
fun LoggedInSection(
    localUser: UserEntity,
    onLogout: () -> Unit,
    onUnlinkSocial: () -> Unit,
    onDeleteAccount: () -> Unit  // ✅ 추가
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        // 사용자 정보 카드
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            ),
            elevation = CardDefaults.cardElevation(2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Text(
                    text = "로그인 정보",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Gray
                )

                Spacer(modifier = Modifier.height(12.dp))

                localUser.nickname?.let {
                    InfoRow(label = "닉네임", value = it)
                }

                localUser.email?.let {
                    Spacer(modifier = Modifier.height(8.dp))
                    InfoRow(label = "이메일", value = it)
                }

                Spacer(modifier = Modifier.height(8.dp))
                InfoRow(
                    label = "연동 계정",
                    value = when (localUser.socialType) {
                        SocialType.GOOGLE -> "Google"
                        SocialType.KAKAO ->  "Kakao"
                        else -> "없음"
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ✅ 로그아웃 버튼
        OutlinedButton(
            onClick = onLogout,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = Color(0xFF1976D2)
            )
        ) {
            Text(
                text = "로그아웃",
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // ✅ 연동 해제 버튼
        OutlinedButton(
            onClick = onUnlinkSocial,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = Color(0xFFE53935)
            )
        ) {
            Text(
                text = "소셜 연동 해제",
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // ✅ 회원 탈퇴 버튼
        OutlinedButton(
            onClick = onDeleteAccount,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = Color(0xFFEF4444)
            )
        ) {
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Rounded.Warning,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = Color(0xFFEF4444)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "회원 탈퇴",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

/**
 * 정보 행 컴포넌트
 */
@Composable
fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            color = Color.Gray
        )
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = Color.Black
        )
    }
}

/**
 * 혜택 안내 섹션
 */
@Composable
fun BenefitsSection() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF1F8FF)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = "💡 소셜 로그인 혜택",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1976D2)
            )

            Spacer(modifier = Modifier.height(12.dp))

            BenefitItem("여러 기기에서 동일한 데이터 사용")
            BenefitItem("앱 삭제 후에도 데이터 복구 가능")
            BenefitItem("안전한 클라우드 백업")
        }
    }
}

/**
 * 혜택 항목 컴포넌트
 */
@Composable
fun BenefitItem(text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "✓",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF4CAF50),
            modifier = Modifier.padding(end = 8.dp)
        )
        Text(
            text = text,
            fontSize = 14.sp,
            color = Color(0xFF424242)
        )
    }
}