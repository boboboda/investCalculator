package com.bobodroid.myapplication.domain.entity

import com.bobodroid.myapplication.models.datamodels.roomDb.CurrencyRecord


data class RecordFilterCriteria(
    val hideSold: Boolean = false,
    val categoryName: String? = null,
    val dateRange: DateRange? = null
)

data class DateRange(
    val startDate: String,  // "2024-01-01"
    val endDate: String     // "2024-12-31"
)

data class GroupedRecordsEntity(
    val groupedRecords: Map<String, List<CurrencyRecord>> = emptyMap()
)
