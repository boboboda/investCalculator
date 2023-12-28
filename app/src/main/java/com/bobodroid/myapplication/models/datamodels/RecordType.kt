package com.bobodroid.myapplication.models.datamodels

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*


@Entity(tableName = "LocalUserData_table")
data class LocalUserData(

    @PrimaryKey
    @ColumnInfo(name = "id")
    var id: UUID = UUID.randomUUID(),

    @ColumnInfo(name = "rate_Reset_Count")
    val rateResetCount: Int? = null,

    @ColumnInfo(name = "recent_Us_Rate", defaultValue = "")
    val recentUsRate: String? = null,

    @ColumnInfo(name = "recent_Yen_Rate", defaultValue = "")
    val recentYenRate: String? = null,

    @ColumnInfo(name = "reFresh_CreateAt", defaultValue = "")
    val reFreshCreateAt: String? = null,

    @ColumnInfo(name = "rate_Ad_Count")
    val rateAdCount: Int? = null,

    @ColumnInfo(name = "user_Reset_State")
    var userResetState: String? = null,

    @ColumnInfo(name = "user_Reset_Date")
    var userResetDate: String? = null,

    @ColumnInfo(name = "user_Show_Notice_Date")
    var userShowNoticeDate: String? = null,

    @ColumnInfo(name = "dr_Buy_Spread")
    var drBuySpread: Int? = null,

    @ColumnInfo(name = "dr_Sell_Spread")
    var drSellSpread: Int? = null,

    @ColumnInfo(name = "yen_Buy_Spread")
    var yenBuySpread: Int? = null,

    @ColumnInfo(name = "yen_Sell_Spread")
    var yenSellSpread: Int? = null
)

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

    @ColumnInfo(name = "profit", defaultValue = "")
    val profit: String? = null,

    @ColumnInfo(name = "exchangeMoney")
    val exchangeMoney: String,

    @ColumnInfo(name = "usingRecord")
    val recordColor: Boolean,

    @ColumnInfo(name = "buyDrMemo", defaultValue = "")
    val buyDrMemo: String,

    @ColumnInfo(name = "buyDrCategoryName", defaultValue = "")
    val buyDrCategoryName : String,

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
    val exchangeMoney: String,

    @ColumnInfo(name = "sellDrMemo", defaultValue = "")
    val sellDrMemo: String,

    @ColumnInfo(name = "sellDrCategoryName", defaultValue = "")
    val sellDrCategoryName : String,
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

    @ColumnInfo(name = "profit", defaultValue = "")
    val profit: String? = null,

    @ColumnInfo(name = "exchangeMoney")
    val exchangeMoney: String,

    @ColumnInfo(name = "usingRecord")
    val recordColor: Boolean,

    @ColumnInfo(name = "buyYenMemo", defaultValue = "")
    val buyYenMemo: String,

    @ColumnInfo(name = "buyYenCategoryName", defaultValue = "")
    val buyYenCategoryName : String,
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
    val exchangeMoney: String,

    @ColumnInfo(name = "sellYenMemo", defaultValue = "")
    val sellYenMemo: String,

    @ColumnInfo(name = "sellYenCategoryName", defaultValue = "")
    val sellYenCategoryName : String,
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

    @ColumnInfo(name = "profit", defaultValue = "")
    val profit: String? = null,

    @ColumnInfo(name = "exchangeMoney")
    val exchangeMoney: String,

    @ColumnInfo(name = "usingRecord")
    val recordColor: Boolean,

    @ColumnInfo(name = "moneyType")
    val moneyType: Int,

    @ColumnInfo(name = "buyWonMemo", defaultValue = "")
    val buyWonMemo: String,

    @ColumnInfo(name = "buyWonCategoryName", defaultValue = "")
    val buyWonCategoryName : String,

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
    val exchangeMoney: String,

    @ColumnInfo(name = "moneyType")
    val moneyType: Int,

    @ColumnInfo(name = "sellWonMemo", defaultValue = "")
    val sellWonMemo: String,

    @ColumnInfo(name = "sellWonCategoryName", defaultValue = "")
    val sellWonCategoryName : String,
)

