package com.bobodroid.myapplication.models.datamodels

import androidx.room.Database
import androidx.room.RoomDatabase
import com.bobodroid.myapplication.models.datamodels.DrBuyRecord


@Database(entities = [
    DrBuyRecord::class,
    DrSellRecord::class,
    YenBuyRecord::class,
    YenSellRecord::class,
    WonBuyRecord::class,
    WonSellRecord::class
                     ], version = 5, exportSchema = true)
abstract class InvestDatabase: RoomDatabase() {

    abstract fun DrBuyDao() : DollarBuyDatabaseDao

    abstract fun DrSellDao() : DollarSellDatabaseDao

    abstract fun YenBuyDao() : YenBuyDatabaseDao

    abstract fun YenSellDao() : YenSellDatabaseDao

    abstract fun WonBuyDao() : WonBuyDatabaseDao

    abstract fun WonSellDao() : WonSellDatabaseDao
}