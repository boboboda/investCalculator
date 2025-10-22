package com.bobodroid.myapplication

import android.app.Activity
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import com.bobodroid.myapplication.components.AutoSizeText
import com.bobodroid.myapplication.models.viewmodels.WebViewModel
import com.bobodroid.myapplication.components.WebView
import com.bobodroid.myapplication.ui.theme.InverstCalculatorTheme
import com.bobodroid.myapplication.ui.theme.TopBarColor

class WebActivity : AppCompatActivity() {

    private val webViewModel: WebViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ✅ Edge-to-Edge 활성화
        enableEdgeToEdge()

        // ✅ 시스템바 영역 처리
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            val url = intent.getStringExtra("url") ?: ""

            InverstCalculatorTheme {
                WebScreen(webViewModel, url, this)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WebScreen(
    webViewModel: WebViewModel,
    url: String,
    activity: Activity
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
//        topBar = {
//            // ✅ MainTopBar와 동일한 구조 사용 + 닫기 버튼 추가
//            Row(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .height(40.dp)
//                    .statusBarsPadding() // ✅ 시스템바 패딩 추가
//                    .background(Color.White)
//                    .padding(horizontal = 10.dp),
//                verticalAlignment = Alignment.CenterVertically,
//                horizontalArrangement = Arrangement.Center
//            ) {
//                Image(
//                    modifier = Modifier
//                        .clip(CircleShape)
//                        .padding(5.dp),
//                    painter = painterResource(id = R.drawable.ic_icon),
//                    contentDescription = ""
//                )
//
//                Spacer(modifier = Modifier.width(10.dp))
//
//                AutoSizeText(
//                    value = "달러 기록",
//                    fontSize = 18.sp,
//                    fontWeight = FontWeight.Bold,
//                    maxLines = 1,
//                    minFontSize = 10.sp,
//                    color = Color(0xFF1F2937)
//                )
//
//                Spacer(modifier = Modifier.weight(1f))
//
//                // ✅ 닫기 버튼 추가
//                com.bobodroid.myapplication.components.IconButton(
//                    imageVector = Icons.Outlined.Close,
//                    onClicked = {
//                        webViewModel.finishWebAct(activity = activity)
//                    },
//                    modifier = Modifier.padding(end = 5.dp)
//                )
//            }
//        },
        bottomBar = {
            BottomAppBar(
                modifier = Modifier.height(50.dp),
                actions = {
                    Spacer(Modifier.weight(1f))
                    IconButton(onClick = { webViewModel.undo() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "back",
                            tint = Color.DarkGray
                        )
                    }
                    IconButton(onClick = { webViewModel.redo() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowForward,
                            contentDescription = "forward",
                            tint = Color.DarkGray
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        // ✅ WebView에 paddingValues 적용
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            WebView(
                webViewModel = webViewModel,
                url = url,
                activity = activity
            )
        }
    }
}