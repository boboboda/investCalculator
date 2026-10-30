package com.bobodroid.myapplication.models.datamodels.repository

import com.bobodroid.myapplication.models.datamodels.roomDb.*
import kotlinx.coroutines.flow.Flow
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 여행모드 - 여행(Trip) & 지출(TripExpense) Repository
 */
@Singleton
class TripRepository @Inject constructor(
    private val tripDao: TripDao
) {

    // ===== 여행(Trip) 조회 =====

    fun getAllTrips(): Flow<List<Trip>> = tripDao.getAllTrips()

    fun getOngoingTrips(): Flow<List<Trip>> = tripDao.getOngoingTrips()

    fun getCompletedTrips(): Flow<List<Trip>> = tripDao.getCompletedTrips()

    suspend fun getTripById(id: UUID): Trip? = tripDao.getTripById(id)

    // ===== 여행 추가/수정/삭제 =====

    suspend fun addTrip(trip: Trip) = tripDao.insertTrip(trip)

    suspend fun updateTrip(trip: Trip) = tripDao.updateTrip(trip)

    suspend fun updateTripCompleted(id: UUID, isCompleted: Boolean) =
        tripDao.updateTripCompleted(id, isCompleted)

    suspend fun deleteTrip(trip: Trip) = tripDao.deleteTrip(trip)

    suspend fun deleteTripById(id: UUID) = tripDao.deleteTripById(id)

    // ===== 지출(TripExpense) 조회 =====

    fun getExpensesByTrip(tripId: UUID): Flow<List<TripExpense>> =
        tripDao.getExpensesByTrip(tripId)

    suspend fun getExpenseById(id: UUID): TripExpense? = tripDao.getExpenseById(id)

    // ===== 지출 추가/수정/삭제 =====

    suspend fun addExpense(expense: TripExpense) = tripDao.insertExpense(expense)

    suspend fun updateExpense(expense: TripExpense) = tripDao.updateExpense(expense)

    suspend fun deleteExpense(expense: TripExpense) = tripDao.deleteExpense(expense)

    suspend fun deleteExpenseById(id: UUID) = tripDao.deleteExpenseById(id)

    suspend fun deleteExpensesByTrip(tripId: UUID) = tripDao.deleteExpensesByTrip(tripId)

    // ===== 분석(통계) =====

    suspend fun getTotalSpentWon(tripId: UUID): Float? = tripDao.getTotalSpentWon(tripId)

    suspend fun getExpenseCount(tripId: UUID): Int = tripDao.getExpenseCount(tripId)

    suspend fun getCategoryBreakdown(tripId: UUID): List<CategoryBreakdown> =
        tripDao.getCategoryBreakdown(tripId)
}
