package com.bobodroid.myapplication.models.datamodels.roomDb

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID
import javax.annotation.Nonnull

/**
 * 여행모드 - 여행(트립) 엔티티
 * 여행 하나당 통화 하나를 고정으로 가짐 (여행 중 통화 변경 미지원)
 */
@Entity(tableName = "trips")
data class Trip(
    @PrimaryKey
    @ColumnInfo(name = "id")
    @Nonnull
    var id: UUID = UUID.randomUUID(),

    @ColumnInfo(name = "trip_name")
    var tripName: String,

    @ColumnInfo(name = "currency_code")
    var currencyCode: String,

    @ColumnInfo(name = "start_date")
    var startDate: String,

    @ColumnInfo(name = "end_date")
    var endDate: String,

    @ColumnInfo(name = "budget_won")
    var budgetWon: String? = null,

    @ColumnInfo(name = "average_exchange_rate")
    var averageExchangeRate: String,

    @ColumnInfo(name = "is_completed")
    var isCompleted: Boolean = false,

    @ColumnInfo(name = "created_at")
    var createdAt: String
) {
    /**
     * 내 평균 환전환율(100통화 단위 기준)로 외화 금액을 원화로 환산
     */
    fun convertToWon(foreignAmount: String): String {
        val amount = foreignAmount.toDoubleOrNull() ?: return "0"
        val rate = averageExchangeRate.toDoubleOrNull() ?: return "0"
        return (amount * rate / 100).toString()
    }
}
