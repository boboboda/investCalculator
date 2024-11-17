package com.bobodroid.myapplication.models.datamodels.roomDb

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.bobodroid.myapplication.models.datamodels.service.UserApi.Rate
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.QuerySnapshot
import java.util.*
import kotlin.collections.HashMap


@Entity(tableName = "LocalUserData_table")
data class LocalUserData(

    @PrimaryKey
    @ColumnInfo(name = "id")
    var id: UUID = UUID.randomUUID(),

    @ColumnInfo(name = "customId", defaultValue = "")
    var customId: String? = null,

    @ColumnInfo(name = "pin", defaultValue = "")
    var pin: String? = null,

    // 목표환율 설정 //FCM

    @ColumnInfo(name = "fcm_Token", defaultValue = "")
    var fcmToken: String? = null,

    // 광고 리셋
    // 무료 광고 횟수
    @ColumnInfo(name = "rate_Reset_Count", defaultValue = "")
    var rateResetCount: Int? = null,

    // 환율 새로고침 날짜
    @ColumnInfo(name = "reFresh_CreateAt", defaultValue = "")
    var reFreshCreateAt: String? = null,

    // 광고 시청 후 리셋 기회 제공
    @ColumnInfo(name = "rate_Ad_Count", defaultValue = "")
    var rateAdCount: Int? = null,

    // 하루 무료 기회 제공 여부 (날짜로 저장)
    @ColumnInfo(name = "reward_ad_Showing_date", defaultValue = "")
    var rewardAdShowingDate: String? = null,

    // 무료기회 초기화
    @ColumnInfo(name = "user_Reset_Date", defaultValue = "")
    var userResetDate: String? = null,

    // 공지사항 날짜
    @ColumnInfo(name = "user_Show_Notice_Date", defaultValue = "")
    var userShowNoticeDate: String? = null,

    // 스프레드 설정
    @ColumnInfo(name = "dr_Buy_Spread", defaultValue = "")
    var drBuySpread: Int? = null,

    @ColumnInfo(name = "dr_Sell_Spread", defaultValue = "")
    var drSellSpread: Int? = null,

    @ColumnInfo(name = "yen_Buy_Spread", defaultValue = "")
    var yenBuySpread: Int? = null,

    @ColumnInfo(name = "yen_Sell_Spread", defaultValue = "")
    var yenSellSpread: Int? = null
) {
    constructor(data: DocumentSnapshot) : this() {
        this.customId = data["customId"] as String? ?: ""
        this.pin = data["pin"] as String? ?: ""
        this.fcmToken = data["fcmToken"] as String? ?: "" }

    fun asHasMap(): HashMap<String, Any?> {
        return hashMapOf(
            "customId" to this.customId,
            "pin" to this.pin,
            "fcmToken" to this.fcmToken,
        )
    }
}

@Entity(tableName = "buyDollar_table")
data class DrBuyRecord(

    @PrimaryKey
    @ColumnInfo(name = "id")
    var id: UUID = UUID.randomUUID(),

    @ColumnInfo(name = "date", defaultValue = "")
    var date: String? = null,

    @ColumnInfo(name = "sell_Date", defaultValue = "")
    var sellDate: String? = null,

    @ColumnInfo(name = "money", defaultValue = "")
    var money: String? = null,

    @ColumnInfo(name = "rate", defaultValue = "")
    var rate: String? = null,

    @ColumnInfo(name = "buy_rate", defaultValue = "")
    var buyRate: String? = null,

    @ColumnInfo(name = "sell_rate", defaultValue = "")
    var sellRate: String? = null,

    @ColumnInfo(name = "profit", defaultValue = "")
    var profit: String? = null,

    @ColumnInfo(name = "sell_profit", defaultValue = "")
    var sellProfit: String? = null,

    @ColumnInfo(name = "expect_profit", defaultValue = "")
    var expectProfit: String? = null,

    @ColumnInfo(name = "exchangeMoney", defaultValue = "")
    var exchangeMoney: String? = null,

    @ColumnInfo(name = "usingRecord", defaultValue = "")
    var recordColor: Boolean? = null,

    @ColumnInfo(name = "buyDrMemo", defaultValue = "")
    var buyDrMemo: String? = null,

    @ColumnInfo(name = "buyDrCategoryName", defaultValue = "")
    var buyDrCategoryName: String? = null,

    ) {
    constructor(data: Map<String, Any>) : this(
        id = UUID.fromString(data["id"] as String?) ?: UUID.randomUUID(),
        date = data["date"] as String?,
        sellDate = data["sellDate"] as String?,
        money = data["money"] as String?,
        rate = data["rate"] as String?,
        buyRate = data["buyRate"] as String?,
        sellRate = data["sellRate"] as String?,
        profit = data["profit"] as String?,
        sellProfit = data["sellProfit"] as String?,
        expectProfit = data["expectProfit"] as String?,
        exchangeMoney = data["exchangeMoney"] as String,
        recordColor = data["recordColor"] as Boolean,
        buyDrMemo = data["buyDrMemo"] as String,
        buyDrCategoryName = data["buyDrCategoryName"] as String
    )


    fun asHasMap(): HashMap<String, Any?> {
        return hashMapOf(
            "id" to this.id.toString(),
            "date" to this.date,
            "sellDate" to this.sellDate,
            "money" to this.money,
            "rate" to this.rate,
            "profit" to this.profit,
            "buyRate" to this.buyRate,
            "sellRate" to this.sellRate,
            "sellProfit" to this.sellProfit,
            "expectProfit" to this.expectProfit,
            "exchangeMoney" to this.exchangeMoney,
            "recordColor" to this.recordColor,
            "buyDrMemo" to this.buyDrMemo,
            "buyDrCategoryName" to this.buyDrCategoryName
        )
    }

}


@Entity(tableName = "sellDollar_table")
data class DrSellRecord(

    @PrimaryKey
    @ColumnInfo(name = "id")
    var id: UUID = UUID.randomUUID(),

    @ColumnInfo(name = "date", defaultValue = "")
    var date: String? = null,

    @ColumnInfo(name = "money", defaultValue = "")
    var money: String? = null,

    @ColumnInfo(name = "rate", defaultValue = "")
    var rate: String? = null,

    @ColumnInfo(name = "exchangeMoney", defaultValue = "")
    var exchangeMoney: String? = null,

    @ColumnInfo(name = "sellDrMemo", defaultValue = "")
    var sellDrMemo: String? = null,

    @ColumnInfo(name = "sellDrCategoryName", defaultValue = "")
    var sellDrCategoryName: String? = null,
) {
    constructor(data: Map<String, Any>) : this(
        id = UUID.fromString(data["id"] as String?) ?: UUID.randomUUID(),
        date = data["date"] as String?,
        money = data["money"] as String?,
        rate = data["rate"] as String?,
        exchangeMoney = data["exchangeMoney"] as String,
        sellDrMemo = data["sellDrMemo"] as String,
        sellDrCategoryName = data["sellDrCategoryName"] as String
    )

    fun asHasMap(): HashMap<String, Any?> {
        return hashMapOf(
            "id" to this.id.toString(),
            "date" to this.date,
            "money" to this.money,
            "rate" to this.rate,
            "exchangeMoney" to this.exchangeMoney,
            "sellDrMemo" to this.sellDrMemo,
            "sellDrCategoryName" to this.sellDrCategoryName
        )
    }
}

@Entity(tableName = "buyYen_table")
data class YenBuyRecord(

    @PrimaryKey
    @ColumnInfo(name = "id")
    var id: UUID = UUID.randomUUID(),

    @ColumnInfo(name = "date", defaultValue = "")
    var date: String? = null,

    @ColumnInfo(name = "sell_Date", defaultValue = "")
    var sellDate: String? = null,

    @ColumnInfo(name = "money", defaultValue = "")
    var money: String? = null,

    @ColumnInfo(name = "rate", defaultValue = "")
    var rate: String? = null,

    @ColumnInfo(name = "buy_rate", defaultValue = "")
    var buyRate: String? = null,

    @ColumnInfo(name = "sell_rate", defaultValue = "")
    var sellRate: String? = null,

    @ColumnInfo(name = "profit", defaultValue = "")
    var profit: String? = null,

    @ColumnInfo(name = "sell_profit", defaultValue = "")
    var sellProfit: String? = null,

    @ColumnInfo(name = "expect_profit", defaultValue = "")
    var expectProfit: String? = null,

    @ColumnInfo(name = "exchangeMoney", defaultValue = "")
    var exchangeMoney: String? = null,

    @ColumnInfo(name = "usingRecord", defaultValue = "")
    var recordColor: Boolean? = null,

    @ColumnInfo(name = "buyYenMemo", defaultValue = "")
    var buyYenMemo: String? = null,

    @ColumnInfo(name = "buyYenCategoryName", defaultValue = "")
    var buyYenCategoryName: String? = null,
) {
    constructor(data: Map<String, Any>) : this(
        id = UUID.fromString(data["id"] as String?) ?: UUID.randomUUID(),
        date = data["date"] as String?,
        sellDate = data["sellDate"] as String?,
        money = data["money"] as String?,
        rate = data["rate"] as String?,
        buyRate = data["buyRate"] as String?,
        sellRate = data["sellRate"] as String?,
        profit = data["profit"] as String?,
        sellProfit = data["sellProfit"] as String?,
        expectProfit = data["expectProfit"] as String?,
        exchangeMoney = data["exchangeMoney"] as String,
        recordColor = data["recordColor"] as Boolean,
        buyYenMemo = data["buyYenMemo"] as String,
        buyYenCategoryName = data["buyYenCategoryName"] as String
    )


    fun asHasMap(): HashMap<String, Any?> {
        return hashMapOf(
            "id" to this.id.toString(),
            "date" to this.date,
            "sellDate" to this.sellDate,
            "money" to this.money,
            "rate" to this.rate,
            "buyRate" to this.buyRate,
            "sellRate" to this.sellRate,
            "profit" to this.profit,
            "sellProfit" to this.sellProfit,
            "expectProfit" to this.expectProfit,
            "exchangeMoney" to this.exchangeMoney,
            "recordColor" to this.recordColor,
            "buyYenMemo" to this.buyYenMemo,
            "buyYenCategoryName" to this.buyYenCategoryName
        )
    }
}

@Entity(tableName = "sellYen_table")
data class YenSellRecord(

    @PrimaryKey
    @ColumnInfo(name = "id")
    var id: UUID = UUID.randomUUID(),

    @ColumnInfo(name = "date", defaultValue = "")
    var date: String? = null,

    @ColumnInfo(name = "money", defaultValue = "")
    var money: String? = null,

    @ColumnInfo(name = "rate", defaultValue = "")
    var rate: String? = null,

    @ColumnInfo(name = "exchangeMoney", defaultValue = "")
    var exchangeMoney: String? = null,

    @ColumnInfo(name = "sellYenMemo", defaultValue = "")
    var sellYenMemo: String? = null,

    @ColumnInfo(name = "sellYenCategoryName", defaultValue = "")
    var sellYenCategoryName: String? = null,
) {
    constructor(data: Map<String, Any>) : this(
        id = UUID.fromString(data["id"] as String?) ?: UUID.randomUUID(),
        date = data["date"] as String?,
        money = data["money"] as String?,
        rate = data["rate"] as String?,
        exchangeMoney = data["exchangeMoney"] as String,
        sellYenMemo = data["sellYenMemo"] as String,
        sellYenCategoryName = data["sellYenCategoryName"] as String
    )
    fun asHasMap(): HashMap<String, Any?> {
        return hashMapOf(
            "id" to this.id.toString(),
            "date" to this.date,
            "money" to this.money,
            "rate" to this.rate,
            "exchangeMoney" to this.exchangeMoney,
            "sellYenMemo" to this.sellYenMemo,
            "sellYenCategoryName" to this.sellYenCategoryName
        )
    }
}


@Entity(tableName = "buyWon_table")
data class WonBuyRecord(

    @PrimaryKey
    @ColumnInfo(name = "id")
    var id: UUID = UUID.randomUUID(),

    @ColumnInfo(name = "date", defaultValue = "")
    var date: String? = null,

    @ColumnInfo(name = "sell_Date", defaultValue = "")
    var sellDate: String? = null,

    @ColumnInfo(name = "money", defaultValue = "")
    var money: String? = null,

    @ColumnInfo(name = "rate", defaultValue = "")
    var rate: String? = null,

    @ColumnInfo(name = "buy_rate", defaultValue = "")
    var buyRate: String? = null,

    @ColumnInfo(name = "sell_rate", defaultValue = "")
    var sellRate: String? = null,

    @ColumnInfo(name = "profit", defaultValue = "")
    var profit: String? = null,

    @ColumnInfo(name = "sell_profit", defaultValue = "")
    var sellProfit: String? = null,

    @ColumnInfo(name = "expect_profit", defaultValue = "")
    var expectProfit: String? = null,

    @ColumnInfo(name = "exchangeMoney", defaultValue = "")
    var exchangeMoney: String? = null,

    @ColumnInfo(name = "usingRecord", defaultValue = "")
    var recordColor: Boolean? = null,

    @ColumnInfo(name = "moneyType", defaultValue = "")
    var moneyType: Int? = null,

    @ColumnInfo(name = "buyWonMemo", defaultValue = "")
    var buyWonMemo: String? = null,

    @ColumnInfo(name = "buyWonCategoryName", defaultValue = "")
    var buyWonCategoryName: String? = null,

    ) {

    constructor(data: Map<String, Any>) : this(
        id = UUID.fromString(data["id"] as String?) ?: UUID.randomUUID(),
        date = data["date"] as String?,
        sellDate = data["sellDate"] as String?,
        money = data["money"] as String?,
        moneyType = data["moneyType"].let { it.toString().toInt() } as Int?,
        rate = data["rate"] as String?,
        buyRate = data["buyRate"] as String?,
        sellRate = data["sellRate"] as String?,
        profit = data["profit"] as String?,
        sellProfit = data["sellProfit"] as String?,
        expectProfit = data["expectProfit"] as String?,
        exchangeMoney = data["exchangeMoney"] as String,
        recordColor = data["recordColor"] as Boolean,
        buyWonMemo = data["buyWonMemo"] as String,
        buyWonCategoryName = data["buyWonCategoryName"] as String
    )


    fun asHasMap(): HashMap<String, Any?> {
        return hashMapOf(
            "id" to this.id.toString(),
            "date" to this.date,
            "sellDate" to this.sellDate,
            "money" to this.money,
            "moneyType" to this.moneyType,
            "rate" to this.rate,
            "buyRate" to this.buyRate,
            "sellRate" to this.sellRate,
            "profit" to this.profit,
            "sellProfit" to this.sellProfit,
            "expectProfit" to this.expectProfit,
            "exchangeMoney" to this.exchangeMoney,
            "recordColor" to this.recordColor,
            "buyWonMemo" to this.buyWonMemo,
            "buyWonCategoryName" to this.buyWonCategoryName
        )
    }

}

@Entity(tableName = "sellWon_table")
data class WonSellRecord(

    @PrimaryKey
    @ColumnInfo(name = "id")
    var id: UUID = UUID.randomUUID(),

    @ColumnInfo(name = "date", defaultValue = "")
    var date: String? = null,

    @ColumnInfo(name = "money", defaultValue = "")
    var money: String? = null,

    @ColumnInfo(name = "rate", defaultValue = "")
    var rate: String? = null,

    @ColumnInfo(name = "exchangeMoney", defaultValue = "")
    var exchangeMoney: String? = null,

    @ColumnInfo(name = "moneyType", defaultValue = "")
    var moneyType: Int? = null,

    @ColumnInfo(name = "sellWonMemo", defaultValue = "")
    var sellWonMemo: String? = null,

    @ColumnInfo(name = "sellWonCategoryName", defaultValue = "")
    var sellWonCategoryName: String? = null,
) {
    constructor(data: Map<String, Any>) : this(
        id = UUID.fromString(data["id"] as String?) ?: UUID.randomUUID(),
        date = data["date"] as String?,
        money = data["money"] as String?,
        rate = data["rate"] as String?,
        exchangeMoney = data["exchangeMoney"] as String,
        sellWonMemo = data["sellWonMemo"] as String,
        sellWonCategoryName = data["sellWonCategoryName"] as String
    )
    fun asHasMap(): HashMap<String, Any?> {
        return hashMapOf(
            "id" to this.id.toString(),
            "date" to this.date,
            "money" to this.money,
            "rate" to this.rate,
            "exchangeMoney" to this.exchangeMoney,
            "sellWonMemo" to this.sellWonMemo,
            "sellWonCategoryName" to this.sellWonCategoryName
        )
    }
}
data class CloudUserData(
    var id: String? = null,
    var customId: String? = null,
    var drBuyRecord: List<DrBuyRecord>? = emptyList(),
    var drSellRecord: List<DrSellRecord>? = emptyList(),
    var yenBuyRecord: List<YenBuyRecord>? = emptyList(),
    var yenSellRecord: List<YenSellRecord>? = emptyList(),
    var wonBuyRecord: List<WonBuyRecord>? = emptyList(),
    var wonSellRecord: List<WonSellRecord>? = emptyList(),
    var createAt: String? = null,
) {
    // list 일때
    constructor(data: QuerySnapshot) : this() {
        for (document in data.documents) {
            this.id = document["id"] as String? ?: ""
            this.customId = document["customId"] as String? ?: ""
            this.drBuyRecord = createDrBuyRecordList(document["drBuyRecord"]!!) as List<DrBuyRecord>? ?: emptyList()
            this.drSellRecord = createDrSellRecordList(document["drSellRecord"]!!) as List<DrSellRecord>? ?: emptyList()
            this.yenBuyRecord = createYenBuyRecordList(document["yenBuyRecord"]!!) as List<YenBuyRecord>? ?: emptyList()
            this.yenSellRecord = createYenSellRecordList(document["yenSellRecord"]!!) as List<YenSellRecord>? ?: emptyList()
            this.wonBuyRecord = createWonBuyRecordList(document["wonBuyRecord"]!!) as List<WonBuyRecord>? ?: emptyList()
            this.wonSellRecord = createWonSellRecordList(document["wonSellRecord"]!!) as List<WonSellRecord>? ?: emptyList()
            this.createAt = document["createAt"] as String? ?: ""
        }
    }

    private fun createDrBuyRecordList(data: Any): List<DrBuyRecord> {
       return (data as? List<Map<String, Any>>)?.map { DrBuyRecord(it) }!!
    }
    private fun createDrSellRecordList(data: Any): List<DrSellRecord> {
        return (data as? List<Map<String, Any>>)?.map { DrSellRecord(it) }!!
    }

    private fun createYenBuyRecordList(data: Any): List<YenBuyRecord> {
        return (data as? List<Map<String, Any>>)?.map { YenBuyRecord(it) }!!
    }
    private fun createYenSellRecordList(data: Any): List<YenSellRecord> {
        return (data as? List<Map<String, Any>>)?.map { YenSellRecord(it) }!!
    }

    private fun createWonBuyRecordList(data: Any): List<WonBuyRecord> {
        return (data as? List<Map<String, Any>>)?.map { WonBuyRecord(it) }!!
    }
    private fun createWonSellRecordList(data: Any): List<WonSellRecord> {
        return (data as? List<Map<String, Any>>)?.map { WonSellRecord(it) }!!
    }




    fun asHasMap(): HashMap<String, Any?> {
        return hashMapOf(
            "drBuyRecord" to this.drBuyRecord?.map { it.asHasMap() },
            "drSellRecord" to this.drSellRecord?.map { it.asHasMap() },
            "yenBuyRecord" to this.yenBuyRecord?.map { it.asHasMap() },
            "yenSellRecord" to this.yenSellRecord?.map { it.asHasMap() },
            "wonBuyRecord" to this.wonBuyRecord?.map { it.asHasMap() },
            "wonSellRecord" to this.wonSellRecord?.map { it.asHasMap() }
        )
    }


}


data class TargetRates(
    var dollarHighRates : List<Rate>? = emptyList(),
    var dollarLowRates: List<Rate>? = emptyList(),
    var yenHighRates : List<Rate>? = emptyList(),
    var yenLowRates : List<Rate>? = emptyList()
)

enum class CurrencyType {
    USD,
    JPY
}

// 목표환율 방향 (고점, 저점)
enum class RateDirection {
    HIGH,
    LOW
}


sealed class RateType(
    val currency: CurrencyType,
    val direction: RateDirection
) {
    object USD_HIGH : RateType(CurrencyType.USD, RateDirection.HIGH)
    object USD_LOW : RateType(CurrencyType.USD, RateDirection.LOW)
    object JPY_HIGH : RateType(CurrencyType.JPY, RateDirection.HIGH)
    object JPY_LOW : RateType(CurrencyType.JPY, RateDirection.LOW)
}