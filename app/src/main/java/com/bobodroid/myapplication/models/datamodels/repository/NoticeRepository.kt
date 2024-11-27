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

    private val _noticeData = MutableStateFlow<Notice?>(null)

    val noticeData = _noticeData.asStateFlow()

    private val _isReady = MutableStateFlow(false)

    val isReady = _isReady.asStateFlow()

    suspend fun loadNotice() {
        try {
            val noticeResponse = NoticeApi.noticeService.noticeRequest().data.first()

            if(noticeResponse == null) {
                Log.e(TAG("NoticeRepository",""), "공지사항 데이터 없음")
                return
            }

            _noticeData.value = Notice(
                content = noticeResponse.content,
                date = noticeResponse.createdAt
            )

            _isReady.value = true

        } catch (e: Exception) {
            Log.e(TAG("NoticeRepository",""), "공지사항 업데이트 실패", e)
        }
    }

    suspend fun waitForNoticeData(): Notice {
        return _noticeData.filterNotNull().first()
    }
}

data class Notice(
    val content: String? = null,
    val date: String? = null
        )