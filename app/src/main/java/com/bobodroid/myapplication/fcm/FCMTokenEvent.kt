package com.bobodroid.myapplication.fcm

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

object FCMTokenEvent {
    private val _tokenFlow = MutableStateFlow<String?>(null)
    val tokenFlow = _tokenFlow.asStateFlow()

    fun updateToken(token: String) {
        _tokenFlow.value = token
    }
}