package com.bobodroid.myapplication.models.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bobodroid.myapplication.models.datamodels.repository.LatestRateRepository
import com.bobodroid.myapplication.models.datamodels.repository.UserRepository
import com.bobodroid.myapplication.models.datamodels.roomDb.ExchangeRate
import com.bobodroid.myapplication.models.datamodels.roomDb.LocalUserData
import com.bobodroid.myapplication.models.datamodels.useCases.UserUseCases
import com.bobodroid.myapplication.util.result.onError
import com.bobodroid.myapplication.util.result.onSuccess
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

class MyPageViewModel @Inject constructor(
    private val userUseCases: UserUseCases,
    private val userRepository: UserRepository,
    private val latestRateRepository: LatestRateRepository
) : ViewModel() {

    val _myPageUiState = MutableStateFlow(MyPageUiState())
    val myPageUiState = _myPageUiState.asStateFlow()



    // -> 완료
    fun createUser(customId: String, pin: String, resultMessage: (String) -> Unit) {
        viewModelScope.launch {
            userUseCases.customIdCreateUser(
                localUserData = _myPageUiState.value.localUser,
                customId = customId,
                pin = pin)
                .onSuccess { updateData, _ ->
                    val uiState = _myPageUiState.value.copy(localUser = updateData)

                    _myPageUiState.emit(uiState)
                    resultMessage("성공적으로 아이디가 생성되었습니다.")
                }
                .onError { _ ->
                    resultMessage("아이디 생성이 실패되었습니다.")
                }
        }
    }

    // -> 완료
    fun logIn(id: String, pin: String, successFind: (message: String) -> Unit) {
        viewModelScope.launch {
            userUseCases.logIn(_myPageUiState.value.localUser, id, pin)
                .onSuccess { localData, msg ->
                    val uiState = _myPageUiState.value.copy(localUser = localData)

                    _myPageUiState.emit(uiState)

                    successFind(msg ?: "")
                }
        }
    }

    // -> 완료
    fun logout(result: (message: String) -> Unit) {

        viewModelScope.launch {
            userUseCases.logout(_myPageUiState.value.localUser)
                .onSuccess { localData, msg ->

                    val uiState = _myPageUiState.value.copy(localUser = localData)

                    _myPageUiState.emit(uiState)

                    result(msg ?: "")
                }
        }
    }

    // 로컬 아이디 삭제
    fun deleteLocalUser() {

    }
}


data class MyPageUiState(
    val localUser: LocalUserData = LocalUserData(),
)