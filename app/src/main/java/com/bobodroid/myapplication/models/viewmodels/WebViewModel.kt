package com.bobodroid.myapplication.models.viewmodels

import android.app.Activity
import android.net.Uri
import android.util.Log
import android.webkit.CookieManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bobodroid.myapplication.BuildConfig
import com.bobodroid.myapplication.models.datamodels.service.WebTicketApi.WebTicketApi
import com.bobodroid.myapplication.models.datamodels.service.WebTicketApi.WebTicketRequest
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface WebLoadState {
    object Loading : WebLoadState
    data class Ready(val url: String) : WebLoadState
}

class WebViewModel : ViewModel() {

    private val _state = MutableStateFlow<WebLoadState>(WebLoadState.Loading)
    val state: StateFlow<WebLoadState> = _state.asStateFlow()

    private var started = false

    /**
     * 서버에서 입장 티켓을 받아 웹 주소를 만든다.
     * 티켓을 받지 못해도 읽기 전용으로 게시판은 열어준다.
     * 화면 회전 등으로 다시 호출돼도 한 번만 실행한다.
     */
    fun start(deviceId: String, path: String) {
        if (started) return
        started = true

        viewModelScope.launch {
            val ticket = fetchTicket(deviceId)

            // 티켓을 못 받았으면 이전에 남은 앱 세션이 다른 신원으로 쓰이지 않게 지운다.
            if (ticket == null) clearAppSession()

            _state.value = WebLoadState.Ready(buildUrl(path, ticket))
        }
    }

    private suspend fun fetchTicket(deviceId: String): String? {
        if (deviceId.isBlank()) return null

        return try {
            val response = WebTicketApi.service.issue(WebTicketRequest(deviceId))
            if (response.success) response.ticket else null
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.w(LOG_TAG, "웹 티켓 발급 실패: ${e.message}")
            null
        }
    }

    private fun buildUrl(path: String, ticket: String?): String {
        val base = BuildConfig.BASE_URL.trimEnd('/')

        return if (ticket == null) {
            base + path
        } else {
            "$base/app/enter?ticket=${Uri.encode(ticket)}&to=${Uri.encode(path)}"
        }
    }

    private fun clearAppSession() {
        val cookieManager = CookieManager.getInstance()
        cookieManager.setCookie(BuildConfig.BASE_URL, "app_session=; Max-Age=0; Path=/")
        cookieManager.flush()
    }

    fun finishWebAct(activity: Activity) {
        activity.finish()
    }

    private companion object {
        const val LOG_TAG = "WebViewModel"
    }
}