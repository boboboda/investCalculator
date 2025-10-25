package com.bobodroid.myapplication.models.datamodels.roomDb

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.bobodroid.myapplication.models.datamodels.service.UserApi.Rate
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.QuerySnapshot
import java.util.*
import kotlin.collections.HashMap


interface ForeignCurrencyRecord {
    var id: UUID
    var date: String?
    var sellDate: String?
    var money: String?
    var rate: String?
    var buyRate: String?
    var sellRate: String?
    var profit: String?
    var sellProfit: String?
    var expectProfit: String?
    var exchangeMoney: String?
    var recordColor: Boolean?
    var categoryName: String?
    var memo: String?

    fun copyWithMemo(memo: String): ForeignCurrencyRecord

    fun copyWithSell(sellDate:String, sellRate:String, sellProfit:String): ForeignCurrencyRecord

    fun copyWithProfitAndExpectProfit(profit: String?): ForeignCurrencyRecord
}




