package com.bobodroid.myapplication.models.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bobodroid.myapplication.models.datamodels.backtest.BacktestAvailability
import com.bobodroid.myapplication.models.datamodels.backtest.BacktestHistoryItem
import com.bobodroid.myapplication.models.datamodels.backtest.BacktestRequest
import com.bobodroid.myapplication.models.datamodels.backtest.BacktestResponse
import com.bobodroid.myapplication.models.datamodels.repository.BacktestRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class BacktestUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val result: BacktestResponse? = null
)

// 백테스트에서 지원하는 기간 옵션 (서버 BACKTEST_PERIOD_MONTHS와 동일)
private val BACKTEST_PERIOD_MONTHS = listOf(3, 6, 12)

@HiltViewModel
class BacktestViewModel @Inject constructor(
    private val repository: BacktestRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(BacktestUiState())
    val uiState: StateFlow<BacktestUiState> = _uiState.asStateFlow()

    // ⚠️ 수정: "결과 화면으로 넘어가라"는 신호를 uiState.result(값)가 아니라 별도의
    // 1회성 이벤트 스트림(SharedFlow, replay=0)으로 분리했습니다.
    // 예전에는 Input 화면이 LaunchedEffect(uiState.result)로 "result가 null이 아니면 이동"
    // 했는데, Input/Result가 뷰모델을 공유하다 보니 뒤로가기로 Input이 다시 떠도 그 순간
    // uiState.result가 아직 안 지워졌으면 "다시 이동"해버리는 경쟁(race) 버그가 있었습니다.
    // SharedFlow는 과거 값을 다시 들려주지 않으므로(replay 없음), Input이 몇 번을 다시
    // 구독해도 "이미 지나간" 이벤트가 재생될 일이 구조적으로 없습니다.
    private val _navigateToResult = MutableSharedFlow<Unit>()
    val navigateToResult: SharedFlow<Unit> = _navigateToResult.asSharedFlow()

    // ✅ 입력 화면의 "선택 기간 고점·저점" 카드용 상태
    // 통화가 바뀔 때 3/6/12개월치를 한 번에 병렬로 받아서 캐싱해둔다 — 기간 칩을 누를 때는
    // 네트워크 요청 없이 이 맵에서 즉시 꺼내 보여주므로 딜레이가 없다.
    private val _availabilityByPeriod = MutableStateFlow<Map<Int, BacktestAvailability>>(emptyMap())
    val availabilityByPeriod: StateFlow<Map<Int, BacktestAvailability>> = _availabilityByPeriod.asStateFlow()

    private val _availabilityLoading = MutableStateFlow(false)
    val availabilityLoading: StateFlow<Boolean> = _availabilityLoading.asStateFlow()

    // 같은 통화로 중복 호출되는 것을 방지 (예: recomposition으로 LaunchedEffect가 다시 걸리는 경우)
    private var lastLoadedCurrency: String? = null

    val historyList: StateFlow<List<BacktestHistoryItem>> =
        repository.getHistoryList()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // ✅ 통화가 바뀔 때 호출 — 3/6/12개월 고점/저점을 전부 병렬로 미리 받아온다
    fun loadAllAvailability(currencyType: String, force: Boolean = false) {
        if (!force && lastLoadedCurrency == currencyType) return
        lastLoadedCurrency = currencyType

        viewModelScope.launch {
            _availabilityLoading.value = true
            _availabilityByPeriod.value = emptyMap()
            try {
                val results = BACKTEST_PERIOD_MONTHS.map { months ->
                    async {
                        months to runCatching { repository.getAvailability(currencyType, months) }.getOrNull()
                    }
                }.awaitAll()

                _availabilityByPeriod.value = results
                    .mapNotNull { (months, availability) -> availability?.let { months to it } }
                    .toMap()
            } finally {
                _availabilityLoading.value = false
            }
        }
    }

    fun runBacktest(request: BacktestRequest) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            try {
                val response = repository.runBacktest(request)
                repository.saveHistory(response)
                _uiState.value = _uiState.value.copy(isLoading = false, result = response)
                // ✅ 결과 데이터가 준비된 바로 이 시점에 "딱 한 번" 네비게이션 이벤트를 쏩니다.
                _navigateToResult.emit(Unit)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "백테스트 실행 중 오류가 발생했습니다."
                )
            }
        }
    }

    fun loadHistoryDetail(id: UUID) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            val detail = repository.getHistoryDetail(id)
            if (detail != null) {
                _uiState.value = _uiState.value.copy(isLoading = false, result = detail)
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "기록을 불러올 수 없습니다."
                )
            }
        }
    }

    fun clearResult() {
        _uiState.value = _uiState.value.copy(result = null, errorMessage = null)
    }
}