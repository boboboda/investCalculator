package com.bobodroid.myapplication.util.result

sealed class Result<out T> {
    data class Success<out T>(
        val data: T,
        val message: String? = null
    ) : Result<T>()

    data class Error(
        val message: String,
        val exception: Exception? = null
    ) : Result<Nothing>()

    object Loading : Result<Nothing>()
}

inline fun <T> Result<T>.onSuccess(action: (T, String?) -> Unit): Result<T> {
    if (this is Result.Success) {
        action(data, message)
    }
    return this
}



inline fun <T> Result<T>.onError(action: (Result.Error) -> Unit): Result<T> {
    if (this is Result.Error) {
        action(this)
    }
    return this
}

sealed class UiState<out T> {
    object Initial : UiState<Nothing>()
    object Loading : UiState<Nothing>()
    data class Success<T>(val data: T) : UiState<T>()
    data class Error(val message: String) : UiState<Nothing>()
}