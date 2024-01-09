package com.bobodroid.myapplication.models.datamodels

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.DeleteColumn
import androidx.room.RenameColumn
import androidx.room.RenameTable
import androidx.room.RoomDatabase
import androidx.room.migration.AutoMigrationSpec
import com.bobodroid.myapplication.models.datamodels.DrBuyRecord


@Database(entities = [
    DrBuyRecord::class,
    DrSellRecord::class,
    YenBuyRecord::class,
    YenSellRecord::class,
    WonBuyRecord::class,
    WonSellRecord::class,
    LocalUserData::class],
    autoMigrations = [
        AutoMigration(from = 5, to = 6),
        AutoMigration(from = 6, to = 7),
        AutoMigration(from = 7, to = 8),
        AutoMigration(from = 8, to = 9),
        AutoMigration(
            from = 9,
            to = 10,
            spec = InvestDatabase.MyAutoMigration::class
        ),
        AutoMigration(from = 10, to = 11),
        AutoMigration(
            from = 11,
            to = 12,
            spec = InvestDatabase.MyAutoMigration::class
        ),
        AutoMigration(from = 12, to = 13),
        AutoMigration(from = 13, to = 14),
        AutoMigration(from = 14, to = 15),
        AutoMigration(from = 15, to = 16, spec = InvestDatabase.MyAutoMigration::class),
                     ],

    version = 16, exportSchema = true)
abstract class InvestDatabase: RoomDatabase() {
    abstract fun DrBuyDao() : DollarBuyDatabaseDao

    abstract fun DrSellDao() : DollarSellDatabaseDao

    abstract fun YenBuyDao() : YenBuyDatabaseDao

    abstract fun YenSellDao() : YenSellDatabaseDao

    abstract fun WonBuyDao() : WonBuyDatabaseDao

    abstract fun WonSellDao() : WonSellDatabaseDao

    abstract fun LocalUserDao() : LocalUserDatabaseDao

    @RenameColumn(
        tableName = "LocalUserData_table",
        fromColumnName = "recent_rate_CreateAt",
        toColumnName = "reFresh_CreateAt"
    )

    @DeleteColumn(
        tableName = "LocalUserData_table",
        columnName = "recent_rate_CreateAt"
    )

    @RenameColumn(
        tableName = "LocalUserData_table",
        fromColumnName = "recent_Us_Rate",
        toColumnName = "max_target_Us_Rate"
    )

    @DeleteColumn(
        tableName = "LocalUserData_table",
        columnName = "recent_Us_Rate"
    )

    @RenameColumn(
        tableName = "LocalUserData_table",
        fromColumnName = "recent_Yen_Rate",
        toColumnName = "max_target_Yen_Rate"
    )

    @DeleteColumn(
        tableName = "LocalUserData_table",
        columnName = "recent_Yen_Rate"
    )

    @DeleteColumn(
        tableName = "LocalUserData_table",
        columnName = "max_target_Us_Rate"
    )

    @DeleteColumn(
        tableName = "LocalUserData_table",
        columnName = "min_target_Us_Rate"
    )

    @DeleteColumn(
        tableName = "LocalUserData_table",
        columnName = "max_target_Yen_Rate"
    )

    @DeleteColumn(
        tableName = "LocalUserData_table",
        columnName = "min_target_Yen_Rate"
    )

    @DeleteColumn(
        tableName = "LocalUserData_table",
        columnName = "rateAlarmUseState"
    )


    class MyAutoMigration : AutoMigrationSpec
}