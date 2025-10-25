package com.bobodroid.myapplication.data.mapper

import com.bobodroid.myapplication.data.local.entity.CurrencyRecordDto
import com.bobodroid.myapplication.domain.entity.RecordEntity
import com.bobodroid.myapplication.models.datamodels.roomDb.CurrencyRecord

/**
 * Record Mapper
 *
 * DTO ↔ Entity 변환
 * - CurrencyRecordDto (Room) → RecordEntity (Domain)
 * - RecordEntity (Domain) → CurrencyRecordDto (Room)
 * - 하위 호환: CurrencyRecord → RecordEntity
 */
object RecordMapper {

    /**
     * DTO → Entity
     * Room 데이터를 Domain 모델로 변환
     */
    fun CurrencyRecordDto.toEntity(): RecordEntity {
        return RecordEntity(
            id = this.id,
            currencyCode = this.currencyCode,
            date = this.date,
            sellDate = this.sellDate,
            money = this.money,
            rate = this.rate,
            buyRate = this.buyRate,
            sellRate = this.sellRate,
            profit = this.profit,
            sellProfit = this.sellProfit,
            expectProfit = this.expectProfit,
            exchangeMoney = this.exchangeMoney,
            recordColor = this.recordColor,  // ⭐ recordColor 그대로 매핑
            categoryName = this.categoryName,
            memo = this.memo
        )
    }

    /**
     * Entity → DTO
     * Domain 모델을 Room 데이터로 변환
     */
    fun RecordEntity.toDto(): CurrencyRecordDto {
        return CurrencyRecordDto(
            id = this.id,
            currencyCode = this.currencyCode,
            date = this.date,
            sellDate = this.sellDate,
            money = this.money,
            rate = this.rate,
            buyRate = this.buyRate,
            sellRate = this.sellRate,
            profit = this.profit,
            sellProfit = this.sellProfit,
            expectProfit = this.expectProfit,
            exchangeMoney = this.exchangeMoney,
            recordColor = this.recordColor,  // ⭐ recordColor 그대로 매핑
            categoryName = this.categoryName,
            memo = this.memo
        )
    }

    /**
     * List<DTO> → List<Entity>
     */
    fun List<CurrencyRecordDto>.toEntityList(): List<RecordEntity> {
        return this.map { it.toEntity() }
    }

    /**
     * List<Entity> → List<DTO>
     */
    fun List<RecordEntity>.toDtoList(): List<CurrencyRecordDto> {
        return this.map { it.toDto() }
    }

    // ===== 하위 호환성 =====

    /**
     * 기존 CurrencyRecord → RecordEntity
     * 점진적 마이그레이션을 위한 변환
     */
    fun CurrencyRecord.toEntity(): RecordEntity {
        return RecordEntity(
            id = this.id,
            currencyCode = this.currencyCode,
            date = this.date,
            sellDate = this.sellDate,
            money = this.money,
            rate = this.rate,
            buyRate = this.buyRate,
            sellRate = this.sellRate,
            profit = this.profit,
            sellProfit = this.sellProfit,
            expectProfit = this.expectProfit,
            exchangeMoney = this.exchangeMoney,
            recordColor = this.recordColor,  // ⭐ recordColor 그대로
            categoryName = this.categoryName,
            memo = this.memo
        )
    }

    /**
     * RecordEntity → 기존 CurrencyRecord
     * 점진적 마이그레이션을 위한 역변환
     */
    fun RecordEntity.toLegacyRecord(): CurrencyRecord {
        return CurrencyRecord(
            id = this.id,
            currencyCode = this.currencyCode,
            date = this.date,
            sellDate = this.sellDate,
            money = this.money,
            rate = this.rate,
            buyRate = this.buyRate,
            sellRate = this.sellRate,
            profit = this.profit,
            sellProfit = this.sellProfit,
            expectProfit = this.expectProfit,
            exchangeMoney = this.exchangeMoney,
            recordColor = this.recordColor,  // ⭐ recordColor 그대로
            categoryName = this.categoryName,
            memo = this.memo
        )
    }

    /**
     * List<CurrencyRecord> → List<RecordEntity>
     */
    fun List<CurrencyRecord>.toLegacyEntityList(): List<RecordEntity> {
        return this.map { it.toEntity() }
    }

    /**
     * List<RecordEntity> → List<CurrencyRecord>
     */
    fun List<RecordEntity>.toLegacyRecordList(): List<CurrencyRecord> {
        return this.map { it.toLegacyRecord() }
    }
}