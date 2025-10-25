package com.bobodroid.myapplication.domain.usecase.record

import com.bobodroid.myapplication.domain.entity.RecordEntity
import com.bobodroid.myapplication.models.datamodels.roomDb.ForeignCurrencyRecord
import javax.inject.Inject

/**
 * 기록을 카테고리별로 그룹화하는 UseCase
 *
 * [기존 위치]
 * RecordUseCase.setGroup()
 *
 * [변경 사항]
 * - private 함수 → public UseCase 클래스
 * - RecordUseCase에서 분리
 * - RecordEntity 직접 지원 추가
 */
class GroupRecordsByCategoryUseCase @Inject constructor() {

    /**
     * 기록을 카테고리별로 그룹화 (제네릭 버전)
     *
     * @param records 그룹화할 기록 리스트
     * @param selector 그룹 키를 추출하는 함수 (보통 { it.categoryName })
     * @return 그룹화된 Map (Key: 카테고리명, Value: 해당 카테고리 기록 리스트)
     */
    fun <T : ForeignCurrencyRecord> execute(
        records: List<T>,
        selector: (T) -> String?
    ): Map<String, List<T>> {

        // ━━━━━━━━━━━━━━━━━━━━━━━━━━
        // 1. 카테고리별로 그룹화
        // ━━━━━━━━━━━━━━━━━━━━━━━━━━
        val grouped = records.groupBy { record ->
            selector(record)?.takeIf { it.isNotEmpty() } ?: "미지정"
        }

        // ━━━━━━━━━━━━━━━━━━━━━━━━━━
        // 2. 정렬: "미지정"을 맨 마지막으로
        // ━━━━━━━━━━━━━━━━━━━━━━━━━━
        val sortedGrouped = grouped.toSortedMap(
            compareBy<String> {
                if (it == "미지정") 1 else 0
            }.thenBy { it }
        )

        // ━━━━━━━━━━━━━━━━━━━━━━━━━━
        // 3. 각 그룹 내에서 날짜 역순 정렬
        // ━━━━━━━━━━━━━━━━━━━━━━━━━━
        return sortedGrouped.mapValues { (_, recordList) ->
            recordList.sortedByDescending { it.date }
        }
    }

    /**
     * RecordEntity 전용 오버로드 (더 명확한 사용)
     *
     * @param records RecordEntity 리스트
     * @return 그룹화된 Map (Key: 카테고리명, Value: RecordEntity 리스트)
     */
    fun executeForEntity(
        records: List<RecordEntity>
    ): Map<String, List<RecordEntity>> {
        return execute(records) { it.categoryName }
    }

    /**
     * RecordEntity를 카테고리명으로 그룹화 (편의 함수)
     *
     * @param records RecordEntity 리스트
     * @return 그룹화된 Map
     */
    operator fun invoke(records: List<RecordEntity>): Map<String, List<RecordEntity>> {
        return executeForEntity(records)
    }
}