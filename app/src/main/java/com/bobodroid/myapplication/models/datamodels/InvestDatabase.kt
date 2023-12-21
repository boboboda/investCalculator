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
        )
                                         ],
    version = 10, exportSchema = true)
abstract class InvestDatabase: RoomDatabase() {

    @RenameColumn(
        tableName = "LocalUserData_table",
        fromColumnName = "recent_rate_CreateAt",
        toColumnName = "reFresh_CreateAt"
    )

    @DeleteColumn(
    tableName = "LocalUserData_table",
    columnName = "recent_rate_CreateAt"
    )

    class MyAutoMigration : AutoMigrationSpec

    abstract fun DrBuyDao() : DollarBuyDatabaseDao

    abstract fun DrSellDao() : DollarSellDatabaseDao

    abstract fun YenBuyDao() : YenBuyDatabaseDao

    abstract fun YenSellDao() : YenSellDatabaseDao

    abstract fun WonBuyDao() : WonBuyDatabaseDao

    abstract fun WonSellDao() : WonSellDatabaseDao

    abstract fun LocalUserDao() : LocalUserDatabaseDao
}