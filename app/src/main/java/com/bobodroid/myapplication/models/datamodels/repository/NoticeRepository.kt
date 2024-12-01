package com.bobodroid.myapplication.models.datamodels.repository

import android.util.Log
import com.bobodroid.myapplication.MainActivity.Companion.TAG
import com.bobodroid.myapplication.models.datamodels.roomDb.LocalUserData
import com.bobodroid.myapplication.models.datamodels.service.noticeApi.NoticeApi
import com.bobodroid.myapplication.models.datamodels.useCases.UserDataType
import com.bobodroid.myapplication.models.datamodels.websocket.WebSocketClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NoticeRepository @Inject constructor() {
    private val _noticeData = MutableStateFlow<NoticeResult>(NoticeResult.Loading)
    val noticeData = _noticeData.asStateFlow()

    sealed class NoticeResult {
        object Loading : NoticeResult()
        data class Success(val notice: Notice?) : NoticeResult()
        data class Error(val error: Exception) : NoticeResult()
    }

    suspend fun loadNotice() {
        try {
            val noticeResponse = NoticeApi.noticeService.noticeRequest().data.firstOrNull()
            _noticeData.value = NoticeResult.Success(
                if(noticeResponse == null) null
                else Notice(content = noticeResponse.content, date = noticeResponse.createdAt)
            )
        } catch (e: Exception) {
            _noticeData.value = NoticeResult.Error(e)
        }
    }

    suspend fun waitForNoticeData(): Notice? {
        return when(val result = _noticeData.first { it !is NoticeResult.Loading }) {
            is NoticeResult.Success -> result.notice
            is NoticeResult.Error -> null
            NoticeResult.Loading -> null
        }
    }
}

data class Notice(
    val content: String? = null,
    val date: String? = null
        )