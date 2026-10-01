// models/viewmodels/SpreadSettingsViewModel.kt
package com.bobodroid.myapplication.models.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bobodroid.myapplication.models.datamodels.repository.SettingsRepository
import com.bobodroid.myapplication.models.datamodels.repository.UserRepository
import com.bobodroid.myapplication.models.datamodels.roomDb.CurrencyType
import com.bobodroid.myapplication.models.datamodels.service.UserApi.SpreadSettingRequest
import com.bobodroid.myapplication.models.datamodels.service.UserApi.UserApi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SpreadSettingsUiState(
    val selectedCurrency: CurrencyType = CurrencyType.USD,
    val buySpreadWon: Double = 0.0,
    val sellSpreadWon: Double = 0.0,
    val hasCustomSpread: Boolean = false,
    val isSaving: Boolean = false,
    val saveResultMessage: String? = null
)

@HiltViewModel
class SpreadSettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SpreadSettingsUiState())
    val uiState: StateFlow<SpreadSettingsUiState> = _uiState.asStateFlow()

    init {
        loadCurrency(CurrencyType.USD)
    }

    /**
     * 통화 탭 변경 시 해당 통화의 스프레드 값 로드 (로컬 기준)
     */
    fun loadCurrency(currency: CurrencyType) {
        _uiState.value = _uiState.value.copy(
            selectedCurrency = currency,
            buySpreadWon = settingsRepository.getBuySpreadWon(currency),
            sellSpreadWon = settingsRepository.getSellSpreadWon(currency),
            hasCustomSpread = settingsRepository.hasCustomSpread(currency),
            saveResultMessage = null
        )
    }

    fun updateBuySpread(value: Double) {
        _uiState.value = _uiState.value.copy(buySpreadWon = value)
    }

    fun updateSellSpread(value: Double) {
        _uiState.value = _uiState.value.copy(sellSpreadWon = value)
    }

    /**
     * 저장: 로컬(SharedPreferences) 저장 + 서버(spreadSettings) 동기화
     */
    fun saveSpread() {
        val state = _uiState.value
        val currency = state.selectedCurrency

        // ✅ 로컬 저장
        val savedBuy = settingsRepository.setBuySpreadWon(currency, state.buySpreadWon)
        val savedSell = settingsRepository.setSellSpreadWon(currency, state.sellSpreadWon)

        if (!savedBuy || !savedSell) {
            _uiState.value = state.copy(saveResultMessage = "저장에 실패했습니다.")
            return
        }

        _uiState.value = state.copy(isSaving = true, hasCustomSpread = true)

        // ✅ 서버 동기화 (실패해도 로컬 저장은 이미 완료된 상태로 유지)
        viewModelScope.launch {
            val deviceId = userRepository.userData.value?.localUserData?.id?.toString()

            if (deviceId.isNullOrEmpty()) {
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    saveResultMessage = "기기에 저장되었습니다."
                )
                return@launch
            }

            try {
                UserApi.userService.updateSpreadSetting(
                    deviceId = deviceId,
                    request = SpreadSettingRequest(
                        currency = currency.code,
                        buySpreadWon = settingsRepository.getBuySpreadWon(currency),
                        sellSpreadWon = settingsRepository.getSellSpreadWon(currency)
                    )
                )
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    saveResultMessage = "스프레드 설정이 저장되었습니다."
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    saveResultMessage = "기기에는 저장됐지만 서버 동기화에 실패했습니다."
                )
            }
        }
    }

    fun clearMessage() {
        _uiState.value = _uiState.value.copy(saveResultMessage = null)
    }
}
