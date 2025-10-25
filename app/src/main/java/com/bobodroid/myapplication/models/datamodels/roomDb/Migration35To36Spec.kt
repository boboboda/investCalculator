package com.bobodroid.myapplication.models.datamodels.roomDb

import androidx.room.DeleteColumn
import androidx.room.migration.AutoMigrationSpec

/**
 * Version 35 → 36 마이그레이션 스펙
 * reFresh_CreateAt 컬럼 삭제
 */
@DeleteColumn.Entries(
    DeleteColumn(
        tableName = "LocalUserData_table",
        columnName = "reFresh_CreateAt"
    )
)
class Migration35To36Spec : AutoMigrationSpec