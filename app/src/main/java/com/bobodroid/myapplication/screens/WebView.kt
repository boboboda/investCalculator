package com.bobodroid.myapplication.screens

import android.app.Activity
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.bobodroid.myapplication.models.viewmodels.WebViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@Composable
fun WebView(webViewModel: WebViewModel, url:String) {
    val context = LocalContext.current

    // WebView 인스턴스를 재사용하기 위해 remember 사용
    val webView = remember {
        WebView(context).apply {
            settings.javaScriptEnabled = true
            webViewClient = WebViewClient()
            loadUrl("${url}")
        }
    }

    // undo 이벤트를 관찰하고 처리
    LaunchedEffect(webViewModel.undoSharedFlow) {
        webViewModel.undoSharedFlow.collectLatest {
            if (webView.canGoBack()) {
                webView.goBack()
            }
        }
    }

    // redo 이벤트를 관찰하고 처리
    LaunchedEffect(webViewModel.redoSharedFlow) {
        webViewModel.redoSharedFlow.collectLatest {
            if (webView.canGoForward()) {
                webView.goForward()
            }
        }
    }

    AndroidView(
        factory = { webView },
        Modifier
            .fillMaxSize())
}


