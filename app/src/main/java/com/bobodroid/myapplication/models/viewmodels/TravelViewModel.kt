package com.bobodroid.myapplication.models.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bobodroid.myapplication.models.datamodels.repository.SettingsRepository
import com.bobodroid.myapplication.models.datamodels.roomDb.Trip
import com.bobodroid.myapplication.models.datamodels.roomDb.TripExpense
import com.bobodroid.myapplication.models.datamodels.useCases.TripSummary
import com.bobodroid.myapplication.models.datamodels.useCases.TripUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.util.UUID
import javax.inject.Inject

// ===== UI State =====

data class TravelUiState(
    val isLoading: Boolean = false,
    val snackBarMessage: String? = null,

    // 선택된 여행 (기록탭 카드 펼침 + 분석탭 요약 공용 - 같은 카드를 다시 탭하면 접힘/null)
    val selectedTrip: Trip? = null,
    val expenses: List<TripExpense> = emptyList(),
    val tripSummary: TripSummary? = null,

    // 여행 생성 바텀시트 (0: 닫힘, 1: 1단계, 2: 2단계)
    val createStep: Int = 0,
    val createDraft: TripCreateDraft = TripCreateDraft(),

    // 지출 추가/수정 바텀시트
    val showAddExpenseSheet: Boolean = false,
    val expenseDraft: ExpenseDraft = ExpenseDraft(),
    val editingExpenseId: UUID? = null
)

/**
 * 여행 생성 2단계 바텀시트 - 입력 중인 값 (1단계 + 2단계 공통 보관)
 */
data class TripCreateDraft(
    val tripName: String = "",
    val currencyCode: String = "USD",
    val startDate: String = "",
    val endDate: String = "",
    val budgetWon: String = "",
    val averageExchangeRate: String = ""
) {
    // 1단계 "다음" 버튼 활성화 조건
    val isStep1Valid: Boolean
        get() = tripName.isNotBlank() && startDate.isNotBlank() && endDate.isNotBlank()

    // 2단계 "여행 시작" 버튼 활성화 조건 (예산은 선택 입력)
    val isStep2Valid: Boolean
        get() = averageExchangeRate.toDoubleOrNull()?.let { it > 0 } == true
}

/**
 * 지출 추가/수정 바텀시트 - 입력 중인 값
 */
data class ExpenseDraft(
    val amount: String = "",
    val categoryName: String = "",
    val memo: String = "",
    val date: String = ""
) {
    val isValid: Boolean
        get() = amount.toDoubleOrNull()?.let { it > 0 } == true &&
                categoryName.isNotBlank() &&
                date.isNotBlank()
}

// ===== Event =====

sealed class TravelEvent {

    data class SnackBarEvent(val message: String) : TravelEvent()

    // 여행 생성 바텀시트 열기/닫기
    data object ShowCreateTripSheet : TravelEvent()
    data object DismissCreateTripSheet : TravelEvent()

    sealed class CreateStep1Event : TravelEvent() {
        data class NameChanged(val value: String) : CreateStep1Event()
        data class CurrencyChanged(val value: String) : CreateStep1Event()
        data class StartDateChanged(val value: String) : CreateStep1Event()
        data class EndDateChanged(val value: String) : CreateStep1Event()
        data object NextClicked : CreateStep1Event()
    }

    sealed class CreateStep2Event : TravelEvent() {
        data class BudgetChanged(val value: String) : CreateStep2Event()
        data class AverageRateChanged(val value: String) : CreateStep2Event()
        data object BackClicked : CreateStep2Event()
        data object StartTripClicked : CreateStep2Event()
    }

    // 여행 카드 (기록 탭)
    data class SelectTrip(val tripId: UUID) : TravelEvent()
    data class CompleteTrip(val tripId: UUID) : TravelEvent()
    data class ReopenTrip(val tripId: UUID) : TravelEvent()
    data class DeleteTrip(val trip: Trip) : TravelEvent()

    // 지출 추가/수정 바텀시트
    data class ShowAddExpenseSheet(val tripId: UUID) : TravelEvent()
    data class ShowEditExpenseSheet(val expense: TripExpense) : TravelEvent()
    data object DismissExpenseSheet : TravelEvent()

    sealed class ExpenseDraftEvent : TravelEvent() {
        data class AmountChanged(val value: String) : ExpenseDraftEvent()
        data class CategorySelected(val value: String) : ExpenseDraftEvent()
        data class MemoChanged(val value: String) : ExpenseDraftEvent()
        data class DateSelected(val value: String) : ExpenseDraftEvent()
        data object ConfirmClicked : ExpenseDraftEvent()
    }

    data class DeleteExpense(val expense: TripExpense) : TravelEvent()

    // 마이페이지 - 여행모드 메뉴 활성/비활성 토글
    data class ToggleTravelMode(val enabled: Boolean) : TravelEvent()
}

/**
 * 여행모드 ViewModel
 * - 기록탭: 여행 카드 리스트, 2단계 생성 바텀시트, 카드 펼침/접힘
 * - 지출: 지출 추가/수정/삭제 바텀시트
 * - 분석탭: 선택된 여행의 TripSummary
 * - 마이페이지: 여행모드 메뉴 노출 토글
 */
@HiltViewModel
class TravelViewModel @Inject constructor(
    private val tripUseCase: TripUseCase,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TravelUiState())
    val uiState: StateFlow<TravelUiState> = _uiState.asStateFlow()

    val ongoingTrips: StateFlow<List<Trip>> =
        tripUseCase.getOngoingTrips()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val completedTrips: StateFlow<List<Trip>> =
        tripUseCase.getCompletedTrips()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // 마이페이지 토글 상태 (SettingsRepository에 영속 저장)
    val isTravelModeEnabled: StateFlow<Boolean> = settingsRepository.travelModeEnabled

    // selectTrip으로 지출 목록을 구독 중인 Job (여행을 바꿔 선택할 때 이전 구독을 끊기 위함)
    private var expensesJob: Job? = null

    fun handleTravelEvent(event: TravelEvent) {
        when (event) {
            is TravelEvent.SnackBarEvent -> showSnackBar(event.message)

            TravelEvent.ShowCreateTripSheet ->
                _uiState.value = _uiState.value.copy(createStep = 1, createDraft = TripCreateDraft())

            TravelEvent.DismissCreateTripSheet ->
                _uiState.value = _uiState.value.copy(createStep = 0, createDraft = TripCreateDraft())

            is TravelEvent.CreateStep1Event -> handleCreateStep1(event)
            is TravelEvent.CreateStep2Event -> handleCreateStep2(event)

            is TravelEvent.SelectTrip -> selectTrip(event.tripId)
            is TravelEvent.CompleteTrip -> completeTrip(event.tripId)
            is TravelEvent.ReopenTrip -> reopenTrip(event.tripId)
            is TravelEvent.DeleteTrip -> deleteTrip(event.trip)

            is TravelEvent.ShowAddExpenseSheet ->
                _uiState.value = _uiState.value.copy(
                    showAddExpenseSheet = true,
                    expenseDraft = ExpenseDraft(date = LocalDate.now().toString()),
                    editingExpenseId = null
                )

            is TravelEvent.ShowEditExpenseSheet -> {
                val expense = event.expense
                _uiState.value = _uiState.value.copy(
                    showAddExpenseSheet = true,
                    expenseDraft = ExpenseDraft(
                        amount = expense.amount,
                        categoryName = expense.categoryName,
                        memo = expense.memo ?: "",
                        date = expense.date
                    ),
                    editingExpenseId = expense.id
                )
            }

            TravelEvent.DismissExpenseSheet ->
                _uiState.value = _uiState.value.copy(
                    showAddExpenseSheet = false,
                    expenseDraft = ExpenseDraft(),
                    editingExpenseId = null
                )

            is TravelEvent.ExpenseDraftEvent -> handleExpenseDraft(event)

            is TravelEvent.DeleteExpense -> deleteExpense(event.expense)

            is TravelEvent.ToggleTravelMode -> settingsRepository.setTravelModeEnabled(event.enabled)
        }
    }

    // ===== 여행 생성 (2단계 바텀시트) =====

    private fun handleCreateStep1(event: TravelEvent.CreateStep1Event) {
        val draft = _uiState.value.createDraft
        when (event) {
            is TravelEvent.CreateStep1Event.NameChanged ->
                _uiState.value = _uiState.value.copy(createDraft = draft.copy(tripName = event.value))

            is TravelEvent.CreateStep1Event.CurrencyChanged ->
                _uiState.value = _uiState.value.copy(createDraft = draft.copy(currencyCode = event.value))

            is TravelEvent.CreateStep1Event.StartDateChanged ->
                _uiState.value = _uiState.value.copy(createDraft = draft.copy(startDate = event.value))

            is TravelEvent.CreateStep1Event.EndDateChanged ->
                _uiState.value = _uiState.value.copy(createDraft = draft.copy(endDate = event.value))

            TravelEvent.CreateStep1Event.NextClicked -> {
                if (draft.isStep1Valid) {
                    _uiState.value = _uiState.value.copy(createStep = 2)
                } else {
                    showSnackBar("여행 이름과 기간을 입력해 주세요")
                }
            }
        }
    }

    private fun handleCreateStep2(event: TravelEvent.CreateStep2Event) {
        val draft = _uiState.value.createDraft
        when (event) {
            is TravelEvent.CreateStep2Event.BudgetChanged ->
                _uiState.value = _uiState.value.copy(createDraft = draft.copy(budgetWon = event.value))

            is TravelEvent.CreateStep2Event.AverageRateChanged ->
                _uiState.value = _uiState.value.copy(createDraft = draft.copy(averageExchangeRate = event.value))

            TravelEvent.CreateStep2Event.BackClicked ->
                _uiState.value = _uiState.value.copy(createStep = 1)

            TravelEvent.CreateStep2Event.StartTripClicked -> startTrip(draft)
        }
    }

    private fun startTrip(draft: TripCreateDraft) {
        if (!draft.isStep2Valid) {
            showSnackBar("평균 환율을 입력해 주세요")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                tripUseCase.createTrip(
                    tripName = draft.tripName,
                    currencyCode = draft.currencyCode,
                    startDate = draft.startDate,
                    endDate = draft.endDate,
                    budgetWon = draft.budgetWon.ifBlank { null },
                    averageExchangeRate = draft.averageExchangeRate
                )
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    createStep = 0,
                    createDraft = TripCreateDraft(),
                    snackBarMessage = "여행이 시작되었습니다"
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    snackBarMessage = e.message ?: "여행 생성에 실패했습니다"
                )
            }
        }
    }

    // ===== 여행 카드 (기록탭) =====

    /**
     * 같은 여행 카드를 다시 탭하면 접고(null), 다른 카드를 탭하면 그 카드를 펼친다.
     */
    private fun selectTrip(tripId: UUID) {
        if (_uiState.value.selectedTrip?.id == tripId) {
            expensesJob?.cancel()
            _uiState.value = _uiState.value.copy(selectedTrip = null, expenses = emptyList(), tripSummary = null)
            return
        }

        val trip = (ongoingTrips.value + completedTrips.value).find { it.id == tripId }
        _uiState.value = _uiState.value.copy(selectedTrip = trip)
        loadTripDetail(tripId)
    }

    private fun loadTripDetail(tripId: UUID) {
        expensesJob?.cancel()
        expensesJob = viewModelScope.launch {
            tripUseCase.getExpensesByTrip(tripId).collect { expenses ->
                _uiState.value = _uiState.value.copy(expenses = expenses)
            }
        }
        viewModelScope.launch {
            val summary = tripUseCase.getTripSummary(tripId)
            _uiState.value = _uiState.value.copy(tripSummary = summary)
        }
    }

    private fun completeTrip(tripId: UUID) {
        viewModelScope.launch {
            tripUseCase.completeTrip(tripId)
            showSnackBar("여행을 완료 처리했습니다")
        }
    }

    private fun reopenTrip(tripId: UUID) {
        viewModelScope.launch {
            tripUseCase.reopenTrip(tripId)
        }
    }

    private fun deleteTrip(trip: Trip) {
        viewModelScope.launch {
            tripUseCase.deleteTrip(trip)
            if (_uiState.value.selectedTrip?.id == trip.id) {
                expensesJob?.cancel()
                _uiState.value = _uiState.value.copy(
                    selectedTrip = null,
                    expenses = emptyList(),
                    tripSummary = null
                )
            }
            showSnackBar("여행을 삭제했습니다")
        }
    }

    // ===== 지출 추가/수정/삭제 =====

    private fun handleExpenseDraft(event: TravelEvent.ExpenseDraftEvent) {
        val draft = _uiState.value.expenseDraft
        when (event) {
            is TravelEvent.ExpenseDraftEvent.AmountChanged ->
                _uiState.value = _uiState.value.copy(expenseDraft = draft.copy(amount = event.value))

            is TravelEvent.ExpenseDraftEvent.CategorySelected ->
                _uiState.value = _uiState.value.copy(expenseDraft = draft.copy(categoryName = event.value))

            is TravelEvent.ExpenseDraftEvent.MemoChanged ->
                _uiState.value = _uiState.value.copy(expenseDraft = draft.copy(memo = event.value))

            is TravelEvent.ExpenseDraftEvent.DateSelected ->
                _uiState.value = _uiState.value.copy(expenseDraft = draft.copy(date = event.value))

            TravelEvent.ExpenseDraftEvent.ConfirmClicked -> confirmExpense()
        }
    }

    private fun confirmExpense() {
        val tripId = _uiState.value.selectedTrip?.id
        if (tripId == null) {
            showSnackBar("여행을 먼저 선택해 주세요")
            return
        }

        val draft = _uiState.value.expenseDraft
        if (!draft.isValid) {
            showSnackBar("금액, 카테고리, 날짜를 확인해 주세요")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val editingId = _uiState.value.editingExpenseId
                val existing = editingId?.let { id -> _uiState.value.expenses.find { it.id == id } }

                if (existing != null) {
                    tripUseCase.updateExpense(
                        existing.copy(
                            amount = draft.amount,
                            categoryName = draft.categoryName,
                            memo = draft.memo.ifBlank { null },
                            date = draft.date
                        )
                    )
                } else {
                    tripUseCase.addExpense(
                        tripId = tripId,
                        amount = draft.amount,
                        categoryName = draft.categoryName,
                        memo = draft.memo.ifBlank { null },
                        date = draft.date
                    )
                }

                loadTripDetail(tripId)

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    showAddExpenseSheet = false,
                    expenseDraft = ExpenseDraft(),
                    editingExpenseId = null,
                    snackBarMessage = "지출을 저장했습니다"
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    snackBarMessage = e.message ?: "지출 저장에 실패했습니다"
                )
            }
        }
    }

    private fun deleteExpense(expense: TripExpense) {
        viewModelScope.launch {
            tripUseCase.deleteExpense(expense)
            loadTripDetail(expense.tripId)
            showSnackBar("지출을 삭제했습니다")
        }
    }

    // ===== 공통 =====

    private fun showSnackBar(message: String) {
        _uiState.value = _uiState.value.copy(snackBarMessage = message)
    }

    /**
     * 스낵바를 보여준 뒤 Compose 쪽에서 호출해 메시지를 비운다 (중복 노출 방지)
     */
    fun consumeSnackBar() {
        _uiState.value = _uiState.value.copy(snackBarMessage = null)
    }
}
