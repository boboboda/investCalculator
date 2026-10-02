package com.bobodroid.myapplication

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bobodroid.myapplication.components.AppWebView
import com.bobodroid.myapplication.models.viewmodels.WebLoadState
import com.bobodroid.myapplication.models.viewmodels.WebViewModel
import com.bobodroid.myapplication.ui.theme.InverstCalculatorTheme

class WebActivity : AppCompatActivity() {

    private val webViewModel: WebViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        val deviceId = intent.getStringExtra(EXTRA_DEVICE_ID).orEmpty()
        val path = intent.getStringExtra(EXTRA_PATH).orEmpty()
        val title = intent.getStringExtra(EXTRA_TITLE).orEmpty()

        // 앱 전용 게시판 경로만 연다.
        if (!path.startsWith("/app/board/")) {
            finish()
            return
        }

        webViewModel.start(deviceId, path)

        setContent {
            InverstCalculatorTheme {
                WebScreen(webViewModel = webViewModel, title = title, activity = this)
            }
        }
    }

    companion object {
        private const val EXTRA_DEVICE_ID = "deviceId"
        private const val EXTRA_PATH = "path"
        private const val EXTRA_TITLE = "title"

        fun start(context: Context, deviceId: String, path: String, title: String) {
            val intent = Intent(context, WebActivity::class.java).apply {
                putExtra(EXTRA_DEVICE_ID, deviceId)
                putExtra(EXTRA_PATH, path)
                putExtra(EXTRA_TITLE, title)
                if (context !is Activity) addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        }
    }
}

/** 마이페이지에서 공지사항(postType = "notice") 또는 문의사항(postType = "post")을 연다. */
fun openWebBoard(context: Context, deviceId: String, postType: String, title: String) {
    WebActivity.start(context, deviceId, "/app/board/dollarRecord/$postType", title)
}

@Composable
fun WebScreen(
    webViewModel: WebViewModel,
    title: String,
    activity: Activity
) {
    val state by webViewModel.state.collectAsState()
    var progress by remember { mutableStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()   // 글쓰기 중 키보드가 입력창을 가리지 않게 한다
    ) {
        // 상단 바: 제목 + 닫기. 앞으로/뒤로 버튼은 두지 않는다.
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .padding(start = 16.dp, end = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                modifier = Modifier.weight(1f),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1F2937),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            IconButton(onClick = { webViewModel.finishWebAct(activity) }) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "닫기",
                    tint = Color.DarkGray
                )
            }
        }

        Box(modifier = Modifier.fillMaxWidth().height(2.dp)) {
            val loading = state is WebLoadState.Loading || progress < 100
            if (loading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            } else {
                HorizontalDivider(color = Color(0xFFE5E7EB))
            }
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            when (val current = state) {
                is WebLoadState.Loading -> CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )

                is WebLoadState.Ready -> AppWebView(
                    activity = activity,
                    url = current.url,
                    onProgress = { progress = it },
                    onCloseRequested = { activity.finish() }
                )
            }
        }
    }
}