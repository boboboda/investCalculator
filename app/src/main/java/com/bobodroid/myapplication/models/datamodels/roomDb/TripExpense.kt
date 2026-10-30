package com.bobodroid.myapplication.models.datamodels.roomDb

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID
import javax.annotation.Nonnull

/**
 * 여행모드 - 여행 중 지출 엔티티
 * 외화 금액과 함께, 저장 시점의 평균환율로 계산한 원화 환산액을 같이 고정 저장한다.
 * (이후 평균환율 설정이 바뀌어도 이미 기록된 지출의 원화 가치는 변하지 않도록)
 */
@Entity(
    tableName = "trip_expenses",
    foreignKeys = [
        ForeignKey(
            entity = Trip::class,
            parentColumns = ["id"],
            childColumns = ["trip_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["trip_id"])]
)
data class TripExpense(
    @PrimaryKey
    @ColumnInfo(name = "id")
    @Nonnull
    var id: UUID = UUID.randomUUID(),

    @ColumnInfo(name = "trip_id")
    var tripId: UUID,

    @ColumnInfo(name = "amount")
    var amount: String,

    @ColumnInfo(name = "krw_amount")
    var krwAmount: String,

    @ColumnInfo(name = "category_name")
    var categoryName: String,

    @ColumnInfo(name = "memo")
    var memo: String? = null,

    @ColumnInfo(name = "date")
    var date: String
)
