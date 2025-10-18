package com.bobodroid.myapplication.models.datamodels.roomDb

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.DeleteColumn
import androidx.room.DeleteTable
import androidx.room.RenameColumn
import androidx.room.RoomDatabase
import androidx.room.migration.AutoMigrationSpec


@Database(entities = [
    DrBuyRecord::class,
    DrSellRecord::class,
    YenBuyRecord::class,
    YenSellRecord::class,
    WonBuyRecord::class,
    WonSellRecord::class,
    LocalUserData::class,
    ExchangeRate::class],
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
        AutoMigration(
            from = 15,
            to = 16,
            spec = InvestDatabase.MyAutoMigration::class),
        AutoMigration(
            from = 16,
            to = 17,
            spec = InvestDatabase.MyAutoMigration::class),
        AutoMigration(from = 17, to = 18),
        AutoMigration(from = 18, to = 19, spec = InvestDatabase.MyAutoMigration::class),
        AutoMigration(from = 19, to = 20, spec = InvestDatabase.MyAutoMigration::class),
        AutoMigration(from = 20, to = 21, spec = InvestDatabase.MyAutoMigration::class),
        AutoMigration(from = 21, to = 22, spec = InvestDatabase.MyAutoMigration::class)
                     ],
    version = 22, exportSchema = true)

@DeleteTable.Entries(
    DeleteTable(tableName = "sellWon_table"),
    DeleteTable(tableName = "buyWon_table"),
    DeleteTable(tableName = "sellYen_table"),
    DeleteTable(tableName = "sellDollar_table")
)

abstract class InvestDatabase: RoomDatabase() {
    abstract fun DrBuyDao() : DollarBuyDatabaseDao
    abstract fun YenBuyDao() : YenBuyDatabaseDao
    abstract fun LocalUserDao() : LocalUserDatabaseDao
    abstract fun exchangeRateDao() : ExchangeRateDataBaseDao

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

    // 17ver
    @RenameColumn(
        tableName = "LocalUserData_table",
        fromColumnName = "user_Reset_State",
        toColumnName = "reward_ad_Showing_date"
    )

    @DeleteColumn(
        tableName = "LocalUserData_table",
        columnName = "user_Reset_State"
    )

    @DeleteColumn(
        tableName = "LocalUserData_table",
        columnName = "customId"
    )
    @DeleteColumn(
        tableName = "LocalUserData_table",
        columnName = "pin"
    )



    class MyAutoMigration : AutoMigrationSpec
}