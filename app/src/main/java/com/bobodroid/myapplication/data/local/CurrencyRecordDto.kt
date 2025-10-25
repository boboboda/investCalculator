package com.bobodroid.myapplication.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID
import javax.annotation.Nonnull

/**
 * Currency Record DTO (Data Layer)
 *
 * Room Database 전용 모델
 * - @Entity, @ColumnInfo 등 Room 어노테이션 포함
 * - Database 구조에 최적화
 * - RecordEntity와 Mapper로 변환
 *
 * [변경 사항]
 * 기존: CurrencyRecord (Entity + Business Logic)
 * 신규: CurrencyRecordDto (Database Only)
 */
@Entity(tableName = "currency_records")
data class CurrencyRecordDto(
    @PrimaryKey
    @ColumnInfo(name = "id")
    @Nonnull
    val id: UUID = UUID.randomUUID(),

    @ColumnInfo(name = "currency_code")
    val currencyCode: String,

    @ColumnInfo(name = "date")
    val date: String?,

    @ColumnInfo(name = "sell_date")
    val sellDate: String?,

    @ColumnInfo(name = "money")
    val money: String?,

    @ColumnInfo(name = "rate")
    val rate: String?,

    @ColumnInfo(name = "buy_rate")
    val buyRate: String?,

    @ColumnInfo(name = "sell_rate")
    val sellRate: String?,

    @ColumnInfo(name = "profit")
    val profit: String?,

    @ColumnInfo(name = "sell_profit")
    val sellProfit: String?,

    @ColumnInfo(name = "expect_profit")
    val expectProfit: String?,

    @ColumnInfo(name = "exchange_money")
    val exchangeMoney: String?,

    @ColumnInfo(name = "record_color")
    val recordColor: Boolean? = false,  // isSold와 매핑

    @ColumnInfo(name = "category_name")
    val categoryName: String?,

    @ColumnInfo(name = "memo")
    val memo: String?
)