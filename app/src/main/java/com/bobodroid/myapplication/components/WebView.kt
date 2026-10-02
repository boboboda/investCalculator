package com.bobodroid.myapplication.components

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.bobodroid.myapplication.BuildConfig

/**
 * 앱 전용 웹 화면.
 * - 앞으로/뒤로 버튼은 없고, 시스템 뒤로가기가 웹 히스토리를 한 단계씩 되돌린다.
 *   더 갈 곳이 없으면 onCloseRequested 를 호출한다.
 * - 같은 사이트 밖 주소는 외부 브라우저로 연다.
 * - url 이 바뀔 때만 새로 로드한다. (예전에는 리컴포지션마다 첫 주소로 되돌아갔다.)
 */
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun AppWebView(
    activity: Activity,
    url: String,
    onProgress: (Int) -> Unit,
    onCloseRequested: () -> Unit
) {
    val allowedHost = remember { Uri.parse(BuildConfig.BASE_URL).host }

    val webView = remember(url) {
        WebView(activity).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )

            settings.apply {
                javaScriptEnabled = true
                domStorageEnabled = true
                useWideViewPort = true          // 페이지의 viewport 설정을 따른다
                loadWithOverviewMode = false    // 화면을 축소해서 맞추지 않는다
                setSupportZoom(false)
                builtInZoomControls = false
                displayZoomControls = false
                setSupportMultipleWindows(false)
                javaScriptCanOpenWindowsAutomatically = false
            }

            if (BuildConfig.DEBUG) {
                WebView.setWebContentsDebuggingEnabled(true)
            }

            CookieManager.getInstance().setAcceptCookie(true)

            webViewClient = object : WebViewClient() {
                override fun shouldOverrideUrlLoading(
                    view: WebView?,
                    request: WebResourceRequest?
                ): Boolean {
                    val target = request?.url ?: return false

                    if (target.host == allowedHost) return false

                    try {
                        activity.startActivity(Intent(Intent.ACTION_VIEW, target))
                    } catch (e: ActivityNotFoundException) {
                        // 열 수 있는 앱이 없으면 무시한다
                    }
                    return true
                }
            }

            webChromeClient = object : WebChromeClient() {
                override fun onProgressChanged(view: WebView?, newProgress: Int) {
                    onProgress(newProgress)
                }
            }

            loadUrl(url)
        }
    }

    BackHandler {
        if (webView.canGoBack()) {
            webView.goBack()
        } else {
            onCloseRequested()
        }
    }

    DisposableEffect(webView) {
        onDispose {
            (webView.parent as? ViewGroup)?.removeView(webView)
            webView.stopLoading()
            webView.destroy()
        }
    }

    AndroidView(
        factory = { webView },
        modifier = Modifier.fillMaxSize()
    )
}