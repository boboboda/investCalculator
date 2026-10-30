package com.bobodroid.myapplication.models.datamodels.useCases

import com.bobodroid.myapplication.models.datamodels.repository.TripRepository
import com.bobodroid.myapplication.models.datamodels.roomDb.CategoryBreakdown
import com.bobodroid.myapplication.models.datamodels.roomDb.Trip
import com.bobodroid.myapplication.models.datamodels.roomDb.TripExpense
import com.bobodroid.myapplication.worker.BackupScheduler
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import javax.inject.Inject
import kotlin.math.max
import kotlin.math.roundToInt

/**
 * 여행모드 - 여행 요약(분석탭) 결과
 */
data class TripSummary(
    val trip: Trip,
    val totalSpentWon: Float,          // 총 지출 (내 평균 환전환율 기준 원화)
    val budgetUsagePercent: Int?,      // 예산 대비 사용률 (예산 미입력 시 null)
    val categoryBreakdown: List<CategoryPercent>,
    val expenseCount: Int,
    val tripDays: Int,                 // 여행 일수 (최소 1일)
    val dailyAverageWon: Float
)

data class CategoryPercent(
    val categoryName: String,
    val totalWon: Float,
    val percent: Int
)

/**
 * 여행모드 - 여행(Trip) & 지출(TripExpense) UseCase
 * RecordUseCase와 동일하게 쓰기 작업 후 backupScheduler.scheduleBackup()으로 자동 백업을 예약한다.
 */
class TripUseCase @Inject constructor(
    private val tripRepository: TripRepository,
    private val backupScheduler: BackupScheduler
) {

    // date 포맷은 CurrencyRecord와 동일하게 "yyyy-MM-dd" 기준으로 가정
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    // ===== 여행(Trip) =====

    fun getAllTrips(): Flow<List<Trip>> = tripRepository.getAllTrips()

    fun getOngoingTrips(): Flow<List<Trip>> = tripRepository.getOngoingTrips()

    fun getCompletedTrips(): Flow<List<Trip>> = tripRepository.getCompletedTrips()

    /**
     * 새 여행 생성 (2단계 바텀시트: 1단계 이름/통화/날짜 + 2단계 예산/평균환율)
     */
    suspend fun createTrip(
        tripName: String,
        currencyCode: String,
        startDate: String,
        endDate: String,
        budgetWon: String?,
        averageExchangeRate: String
    ): Trip {
        val trip = Trip(
            tripName = tripName,
            currencyCode = currencyCode,
            startDate = startDate,
            endDate = endDate,
            budgetWon = budgetWon,
            averageExchangeRate = averageExchangeRate,
            isCompleted = false,
            createdAt = dateFormat.format(Date())
        )
        tripRepository.addTrip(trip)
        backupScheduler.scheduleBackup()
        return trip
    }

    suspend fun updateTrip(trip: Trip) {
        tripRepository.updateTrip(trip)
        backupScheduler.scheduleBackup()
    }

    /**
     * 여행 완료 처리 (기록 탭 카드에 "완료" 배지)
     */
    suspend fun completeTrip(tripId: UUID) {
        tripRepository.updateTripCompleted(tripId, true)
        backupScheduler.scheduleBackup()
    }

    suspend fun reopenTrip(tripId: UUID) {
        tripRepository.updateTripCompleted(tripId, false)
        backupScheduler.scheduleBackup()
    }

    suspend fun deleteTrip(trip: Trip) {
        // trip_expenses는 Room FK(CASCADE)로 함께 삭제됨
        tripRepository.deleteTrip(trip)
        backupScheduler.scheduleBackup()
    }

    // ===== 지출(TripExpense) =====

    fun getExpensesByTrip(tripId: UUID): Flow<List<TripExpense>> =
        tripRepository.getExpensesByTrip(tripId)

    /**
     * 지출 추가 - 저장 시점의 평균환율로 원화 환산액을 고정 저장
     * (이후 평균환율이 바뀌어도 이미 기록된 지출의 원화 가치는 변하지 않음)
     */
    suspend fun addExpense(
        tripId: UUID,
        amount: String,
        categoryName: String,
        memo: String?,
        date: String
    ): TripExpense {
        val trip = tripRepository.getTripById(tripId)
            ?: throw IllegalArgumentException("여행을 찾을 수 없습니다: $tripId")

        val expense = TripExpense(
            tripId = tripId,
            amount = amount,
            krwAmount = trip.convertToWon(amount),
            categoryName = categoryName,
            memo = memo,
            date = date
        )
        tripRepository.addExpense(expense)
        backupScheduler.scheduleBackup()
        return expense
    }

    /**
     * 지출 수정 - 금액이 바뀌면 그 여행의 평균환율로 원화 환산액을 다시 계산
     */
    suspend fun updateExpense(expense: TripExpense) {
        val trip = tripRepository.getTripById(expense.tripId)
        val recalculated = if (trip != null) {
            expense.copy(krwAmount = trip.convertToWon(expense.amount))
        } else {
            expense
        }
        tripRepository.updateExpense(recalculated)
        backupScheduler.scheduleBackup()
    }

    suspend fun deleteExpense(expense: TripExpense) {
        tripRepository.deleteExpense(expense)
        backupScheduler.scheduleBackup()
    }

    // ===== 분석(요약) =====

    /**
     * 분석탭 - 여행 요약 집계
     * 총 지출 / 예산 대비 사용률 / 카테고리별 비중 / 일 평균 지출·지출 건수
     */
    suspend fun getTripSummary(tripId: UUID): TripSummary? {
        val trip = tripRepository.getTripById(tripId) ?: return null

        val totalSpentWon = tripRepository.getTotalSpentWon(tripId) ?: 0f
        val expenseCount = tripRepository.getExpenseCount(tripId)
        val categories = tripRepository.getCategoryBreakdown(tripId)

        val budgetWon = trip.budgetWon?.toFloatOrNull()
        val budgetUsagePercent = budgetWon?.let {
            if (it <= 0f) null else ((totalSpentWon / it) * 100).roundToInt()
        }

        val categoryBreakdown = categories.map { c ->
            CategoryPercent(
                categoryName = c.categoryName,
                totalWon = c.totalWon,
                percent = if (totalSpentWon <= 0f) 0 else ((c.totalWon / totalSpentWon) * 100).roundToInt()
            )
        }

        val tripDays = max(1, daysBetween(trip.startDate, trip.endDate))
        val dailyAverageWon = totalSpentWon / tripDays

        return TripSummary(
            trip = trip,
            totalSpentWon = totalSpentWon,
            budgetUsagePercent = budgetUsagePercent,
            categoryBreakdown = categoryBreakdown,
            expenseCount = expenseCount,
            tripDays = tripDays,
            dailyAverageWon = dailyAverageWon
        )
    }

    private fun daysBetween(startDate: String, endDate: String): Int {
        return try {
            val start = dateFormat.parse(startDate)
            val end = dateFormat.parse(endDate)
            if (start == null || end == null) 1
            else (((end.time - start.time) / (1000 * 60 * 60 * 24)) + 1).toInt()
        } catch (e: Exception) {
            1
        }
    }
}
