package com.bobodroid.myapplication.components

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Message
import android.util.Log
import android.view.ViewGroup
import android.webkit.JsResult
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebView.WebViewTransport
import android.webkit.WebView.setWebContentsDebuggingEnabled
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.bobodroid.myapplication.models.viewmodels.WebViewModel
import com.bobodroid.myapplication.screens.TAG
import kotlinx.coroutines.flow.collectLatest


@SuppressLint("SetJavaScriptEnabled")
@Composable
fun WebView(webViewModel: WebViewModel, url:String, activity: Activity) {
//    val context = LocalContext.current

    // WebView 인스턴스를 재사용하기 위해 remember 사용
    val webView = remember {
        WebView(activity).apply {

            this.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)

            settings.apply {
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                settings.loadWithOverviewMode = true
                settings.useWideViewPort = true
                settings.setSupportZoom(true)
                settings.setSupportMultipleWindows(true)
                settings.javaScriptCanOpenWindowsAutomatically = true
            }

            setWebContentsDebuggingEnabled(true)

            webViewClient = object : WebViewClient() {
                override fun shouldOverrideUrlLoading(
                    view: WebView?,
                    request: WebResourceRequest?
                ): Boolean {
                    Log.d(TAG, "팝업 호출")
                    return false
                }
            }
            webChromeClient = object : WebChromeClient() {
                override fun onJsAlert(
                    view: WebView?,
                    url: String?,
                    message: String?,
                    result: JsResult?
                ): Boolean {
                    result?.confirm()
                    Log.d(TAG, "팝업 호출")
                    return super.onJsAlert(view, url, message, result)
                }

                override fun onCreateWindow(
                    view: WebView?,
                    isDialog: Boolean,
                    isUserGesture: Boolean,
                    resultMsg: Message?
                ): Boolean {
                    Log.d(TAG, "팝업 호출")
                    val newWebView = view?.context?.let { WebView(it) }
                    if (newWebView != null) {
                        newWebView.webViewClient = WebViewClient()
                    }
                    val transport = resultMsg?.obj as? WebViewTransport
                    transport?.webView = newWebView
                    resultMsg?.sendToTarget()

                    return true
                }
            }

            loadUrl(url)
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
        update = { it.loadUrl(url)

        },
        modifier = Modifier
            .fillMaxSize()
    )
}

