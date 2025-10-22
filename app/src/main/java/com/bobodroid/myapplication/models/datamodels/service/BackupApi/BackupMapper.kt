// app/src/main/java/com/bobodroid/myapplication/models/datamodels/service/BackupApi/BackupMapper.kt
package com.bobodroid.myapplication.models.datamodels.service.BackupApi

import com.bobodroid.myapplication.models.datamodels.roomDb.CurrencyRecord

/**
 * CurrencyRecord <-> CurrencyRecordDto 변환
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
            id = java.util.UUID.fromString(dto.id),
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
}