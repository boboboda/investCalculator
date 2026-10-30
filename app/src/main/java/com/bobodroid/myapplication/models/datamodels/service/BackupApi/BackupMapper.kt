// app/src/main/java/com/bobodroid/myapplication/models/datamodels/service/BackupApi/BackupMapper.kt
package com.bobodroid.myapplication.models.datamodels.service.BackupApi

import com.bobodroid.myapplication.models.datamodels.roomDb.CurrencyRecord
import com.bobodroid.myapplication.models.datamodels.roomDb.Trip
import com.bobodroid.myapplication.models.datamodels.roomDb.TripExpense
import java.util.UUID

/**
 * CurrencyRecord / Trip / TripExpense <-> Dto 변환
 */
object BackupMapper {

    /**
     * CurrencyRecord → CurrencyRecordDto (백업용)
     */
    fun toDto(record: CurrencyRecord): CurrencyRecordDto {
        return CurrencyRecordDto(
            id = record.id.toString(),
            currencyCode = record.currencyCode,
            date = record.date ?: "",
            money = record.money ?: "",
            rate = record.rate ?: "",
            buyRate = record.buyRate ?: "",
            exchangeMoney = record.exchangeMoney ?: "",
            profit = record.profit ?: "",
            expectProfit = record.expectProfit ?: "",
            categoryName = record.categoryName ?: "",
            memo = record.memo ?: "",
            sellRate = record.sellRate,
            sellProfit = record.sellProfit,
            sellDate = record.sellDate,
            recordColor = record.recordColor ?: false
        )
    }

    /**
     * CurrencyRecordDto → CurrencyRecord (복구용)
     */
    fun fromDto(dto: CurrencyRecordDto): CurrencyRecord {
        return CurrencyRecord(
            id = UUID.fromString(dto.id),
            currencyCode = dto.currencyCode,
            date = dto.date,
            money = dto.money,
            rate = dto.rate,
            buyRate = dto.buyRate,
            exchangeMoney = dto.exchangeMoney,
            profit = dto.profit,
            expectProfit = dto.expectProfit,
            categoryName = dto.categoryName,
            memo = dto.memo,
            sellRate = dto.sellRate,
            sellProfit = dto.sellProfit,
            sellDate = dto.sellDate,
            recordColor = dto.recordColor
        )
    }

    /**
     * List<CurrencyRecord> → List<CurrencyRecordDto>
     */
    fun toDtoList(records: List<CurrencyRecord>): List<CurrencyRecordDto> {
        return records.map { toDto(it) }
    }

    /**
     * List<CurrencyRecordDto> → List<CurrencyRecord>
     */
    fun fromDtoList(dtos: List<CurrencyRecordDto>): List<CurrencyRecord> {
        return dtos.map { fromDto(it) }
    }

    // ===== 여행모드 =====

    /**
     * TripExpense → TripExpenseDto (백업용)
     */
    fun toDto(expense: TripExpense): TripExpenseDto {
        return TripExpenseDto(
            id = expense.id.toString(),
            tripId = expense.tripId.toString(),
            amount = expense.amount,
            krwAmount = expense.krwAmount,
            categoryName = expense.categoryName,
            memo = expense.memo ?: "",
            date = expense.date
        )
    }

    /**
     * TripExpenseDto → TripExpense (복구용)
     */
    fun fromDto(dto: TripExpenseDto): TripExpense {
        return TripExpense(
            id = UUID.fromString(dto.id),
            tripId = UUID.fromString(dto.tripId),
            amount = dto.amount,
            krwAmount = dto.krwAmount,
            categoryName = dto.categoryName,
            memo = dto.memo,
            date = dto.date
        )
    }

    /**
     * Trip + 해당 지출 목록 → TripDto (백업용)
     * (Trip과 TripExpense는 별도 테이블이라 지출 목록을 함께 전달받아야 함)
     */
    fun toDto(trip: Trip, expenses: List<TripExpense>): TripDto {
        return TripDto(
            id = trip.id.toString(),
            tripName = trip.tripName,
            currencyCode = trip.currencyCode,
            startDate = trip.startDate,
            endDate = trip.endDate,
            budgetWon = trip.budgetWon,
            averageExchangeRate = trip.averageExchangeRate,
            isCompleted = trip.isCompleted,
            createdAt = trip.createdAt,
            expenses = expenses.map { toDto(it) }
        )
    }

    /**
     * TripDto → Trip (복구용, 지출 목록은 fromDto(TripExpenseDto)로 별도 저장)
     */
    fun fromDto(dto: TripDto): Trip {
        return Trip(
            id = UUID.fromString(dto.id),
            tripName = dto.tripName,
            currencyCode = dto.currencyCode,
            startDate = dto.startDate,
            endDate = dto.endDate,
            budgetWon = dto.budgetWon,
            averageExchangeRate = dto.averageExchangeRate,
            isCompleted = dto.isCompleted,
            createdAt = dto.createdAt
        )
    }
}
