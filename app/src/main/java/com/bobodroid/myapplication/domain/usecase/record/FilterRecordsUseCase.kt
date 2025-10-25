// 파일 생성: domain/usecase/record/FilterRecordsUseCase.kt

package com.bobodroid.myapplication.domain.usecase.record

import com.bobodroid.myapplication.domain.entity.RecordFilterCriteria
import com.bobodroid.myapplication.models.datamodels.roomDb.CurrencyRecord

import javax.inject.Inject

/**
 * 기록 필터링 UseCase
 *
 * 기능:
 * - 매도 여부 필터링
 * - 카테고리 필터링
 * - 날짜 범위 필터링
 */
class FilterRecordsUseCase @Inject constructor() {

    /**
     * 필터 조건에 따라 기록 필터링
     *
     * @param records 원본 기록 리스트
     * @param criteria 필터 조건
     * @return 필터링된 기록 리스트
     */
    fun execute(
        records: List<CurrencyRecord>,
        criteria: RecordFilterCriteria
    ): List<CurrencyRecord> {

        var filteredRecords = records

        // ━━━━━━━━━━━━━━━━━━━━━━━━━━
        // 1. 매도 여부 필터링
        // ━━━━━━━━━━━━━━━━━━━━━━━━━━
        if (criteria.hideSold) {
            // recordColor == false: 보유중 (매도 안함)
            // recordColor == true: 매도됨
            filteredRecords = filteredRecords.filter { it.recordColor == false }
        }

        // ━━━━━━━━━━━━━━━━━━━━━━━━━━
        // 2. 카테고리 필터링
        // ━━━━━━━━━━━━━━━━━━━━━━━━━━
        criteria.categoryName?.let { category ->
            filteredRecords = filteredRecords.filter { it.categoryName == category }
        }

        // ━━━━━━━━━━━━━━━━━━━━━━━━━━
        // 3. 날짜 범위 필터링
        // ━━━━━━━━━━━━━━━━━━━━━━━━━━
        criteria.dateRange?.let { range ->
            filteredRecords = filteredRecords.filter { record ->
                val recordDate = record.date ?: return@filter false
                recordDate >= range.startDate && recordDate <= range.endDate
            }
        }

        return filteredRecords
    }
}