package com.bobodroid.myapplication.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import javax.annotation.Nonnull

/**
 * ExchangeRate DTO (Data Layer)
 *
 * Room Database 전용 모델
 * - @Entity, @ColumnInfo 등 Room 어노테이션 포함
 * - JSON String으로 환율 저장 (Database 효율성)
 * - ExchangeRateEntity와 Mapper로 변환
 *
 * [변경 사항]
 * 기존: ExchangeRate (Entity + Business Logic)
 * 신규: ExchangeRateDto (Database Only)
 */
@Entity(tableName = "exchangeRate_table")
data class ExchangeRateDto(
    @PrimaryKey
    @ColumnInfo(name = "id")
    @Nonnull
    val id: String,

    @ColumnInfo(name = "createAt", defaultValue = "N/A")
    val createAt: String,

    /**
     * JSON String으로 저장
     *
     * 예시: {"USD":"1300.50","JPY":"944.00","EUR":"1400.25"}
     */
    @ColumnInfo(name = "rates")
    val rates: String
)