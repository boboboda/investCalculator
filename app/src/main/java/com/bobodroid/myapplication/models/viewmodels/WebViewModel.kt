package com.bobodroid.myapplication.models.viewmodels

import android.app.Activity
import android.webkit.WebView
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class WebViewModel(): ViewModel() {

    private val _undoSharedFlow = MutableSharedFlow<Unit>()
    val undoSharedFlow = _undoSharedFlow.asSharedFlow()

    private val _redoSharedFlow = MutableSharedFlow<Unit>()
    val redoSharedFlow = _redoSharedFlow.asSharedFlow()

    // undo 이벤트 발생
    fun undo() {
        viewModelScope.launch {
            _undoSharedFlow.emit(Unit)
        }
    }

    // redo 이벤트 발생
    fun redo() {
        viewModelScope.launch {
            _redoSharedFlow.emit(Unit)
        }
    }

    fun finishWebAct(activity: Activity) {
        activity.finish()
    }
}