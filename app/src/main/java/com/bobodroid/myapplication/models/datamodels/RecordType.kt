package com.bobodroid.myapplication.models.datamodels

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*


@Entity(tableName = "buyDollar_table")
data class DrBuyRecord (

    @PrimaryKey
    @ColumnInfo(name = "id")
    var id: UUID = UUID.randomUUID(),

    @ColumnInfo(name = "date")
    val date: String,

    @ColumnInfo(name = "money")
    val money: String,

    @ColumnInfo(name = "rate")
    val rate: String,

//    @ColumnInfo(name = "profit", defaultValue = "")
//    val profit: String? = null,

    @ColumnInfo(name = "exchangeMoney")
    val exchangeMoney: Float,

    @ColumnInfo(name = "usingRecord")
    val recordColor: Boolean

)


@Entity(tableName = "sellDollar_table")
data class DrSellRecord (

    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: UUID = UUID.randomUUID(),

    @ColumnInfo(name = "date")
    val date: String,

    @ColumnInfo(name = "money")
    val money: String,

    @ColumnInfo(name = "rate")
    val rate: String,

    @ColumnInfo(name = "exchangeMoney")
    val exchangeMoney: Float,
)

@Entity(tableName = "buyYen_table")
data class YenBuyRecord (

    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: UUID = UUID.randomUUID(),

    @ColumnInfo(name = "date")
    val date: String,

    @ColumnInfo(name = "money")
    val money: String,

    @ColumnInfo(name = "rate")
    val rate: String,

    @ColumnInfo(name = "exchangeMoney")
    val exchangeMoney: Float,

    @ColumnInfo(name = "usingRecord")
    val recordColor: Boolean
)

@Entity(tableName = "sellYen_table")
data class YenSellRecord (

    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: UUID = UUID.randomUUID(),

    @ColumnInfo(name = "date")
    val date: String,

    @ColumnInfo(name = "money")
    val money: String,

    @ColumnInfo(name = "rate")
    val rate: String,

    @ColumnInfo(name = "exchangeMoney")
    val exchangeMoney: Float,
)


@Entity(tableName = "buyWon_table")
data class WonBuyRecord (

    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: UUID = UUID.randomUUID(),

    @ColumnInfo(name = "date")
    val date: String,

    @ColumnInfo(name = "money")
    val money: String,

    @ColumnInfo(name = "rate")
    val rate: String,

    @ColumnInfo(name = "exchangeMoney")
    val exchangeMoney: Float,

    @ColumnInfo(name = "usingRecord")
    val recordColor: Boolean,

    @ColumnInfo(name = "moneyType")
    val moneyType: Int

)

@Entity(tableName = "sellWon_table")
data class WonSellRecord (

    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: UUID = UUID.randomUUID(),

    @ColumnInfo(name = "date")
    val date: String,

    @ColumnInfo(name = "money")
    val money: String,

    @ColumnInfo(name = "rate")
    val rate: String,

    @ColumnInfo(name = "exchangeMoney")
    val exchangeMoney: Float,

    @ColumnInfo(name = "moneyType")
    val moneyType: Int
)

