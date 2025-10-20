package com.bobodroid.myapplication.models.datamodels.roomDb

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.bobodroid.myapplication.models.datamodels.service.UserApi.Rate
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.QuerySnapshot
import java.util.*
import kotlin.collections.HashMap


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

    fun toType(): CurrencyType = when (this) {
        is DrBuyRecord -> CurrencyType.USD
        is YenBuyRecord -> CurrencyType.JPY
        else -> {CurrencyType.USD}
    }

    fun copyWithProfitAndExpectProfit(profit: String?): ForeignCurrencyRecord
}

@Entity(tableName = "buyDollar_table")
data class DrBuyRecord(
    @PrimaryKey
    @ColumnInfo(name = "id")
    override var id: UUID = UUID.randomUUID(),

    @ColumnInfo(name = "date", defaultValue = "")
    override var date: String? = null,

    @ColumnInfo(name = "sell_Date", defaultValue = "")
    override var sellDate: String? = null,

    @ColumnInfo(name = "money", defaultValue = "")
    override var money: String? = null,

    @ColumnInfo(name = "rate", defaultValue = "")
    override var rate: String? = null,

    @ColumnInfo(name = "buy_rate", defaultValue = "")
    override var buyRate: String? = null,

    @ColumnInfo(name = "sell_rate", defaultValue = "")
    override var sellRate: String? = null,

    @ColumnInfo(name = "profit", defaultValue = "")
    override var profit: String? = null,

    @ColumnInfo(name = "sell_profit", defaultValue = "")
    override var sellProfit: String? = null,

    @ColumnInfo(name = "expect_profit", defaultValue = "")
    override var expectProfit: String? = null,

    @ColumnInfo(name = "exchangeMoney", defaultValue = "")
    override var exchangeMoney: String? = null,

    @ColumnInfo(name = "usingRecord", defaultValue = "")
    override var recordColor: Boolean? = null,

    @ColumnInfo(name = "buyDrMemo", defaultValue = "")
    override var memo: String? = null,

    @ColumnInfo(name = "buyDrCategoryName", defaultValue = "")
    override var categoryName: String? = null,
) : ForeignCurrencyRecord {
    override fun copyWithMemo(memo: String) = copy(memo = memo)

    override fun copyWithSell(
        sellDate: String,
        sellRate: String,
        sellProfit: String
    ): ForeignCurrencyRecord = copy(sellDate = sellDate, sellRate =  sellRate, sellProfit = sellProfit)

    override fun copyWithProfitAndExpectProfit(profit: String?): ForeignCurrencyRecord = copy(profit = profit, expectProfit = profit)
}


@Entity(tableName = "buyYen_table")
data class YenBuyRecord(
    @PrimaryKey
    @ColumnInfo(name = "id")
    override var id: UUID = UUID.randomUUID(),

    @ColumnInfo(name = "date", defaultValue = "")
    override var date: String? = null,

    @ColumnInfo(name = "sell_Date", defaultValue = "")
    override var sellDate: String? = null,

    @ColumnInfo(name = "money", defaultValue = "")
    override var money: String? = null,

    @ColumnInfo(name = "rate", defaultValue = "")
    override var rate: String? = null,

    @ColumnInfo(name = "buy_rate", defaultValue = "")
    override var buyRate: String? = null,

    @ColumnInfo(name = "sell_rate", defaultValue = "")
    override var sellRate: String? = null,

    @ColumnInfo(name = "profit", defaultValue = "")
    override var profit: String? = null,

    @ColumnInfo(name = "sell_profit", defaultValue = "")
    override var sellProfit: String? = null,

    @ColumnInfo(name = "expect_profit", defaultValue = "")
    override var expectProfit: String? = null,

    @ColumnInfo(name = "exchangeMoney", defaultValue = "")
    override var exchangeMoney: String? = null,

    @ColumnInfo(name = "usingRecord", defaultValue = "")
    override var recordColor: Boolean? = null,

    @ColumnInfo(name = "buyYenMemo", defaultValue = "")
    override var memo: String? = null,

    @ColumnInfo(name = "buyYenCategoryName", defaultValue = "")
    override var categoryName: String? = null,
) : ForeignCurrencyRecord {
    override fun copyWithMemo(memo: String) = copy(memo = memo)

    override fun copyWithSell(
        sellDate: String,
        sellRate: String,
        sellProfit: String
    ): ForeignCurrencyRecord = copy(sellDate = sellDate, sellRate =  sellRate, sellProfit = sellProfit)

    override fun copyWithProfitAndExpectProfit(profit: String?): ForeignCurrencyRecord = copy(profit = profit, expectProfit = profit)
}



data class CloudUserData(
    var id: String? = null,
    var customId: String? = null,
    var drBuyRecord: List<DrBuyRecord>? = emptyList(),
    var yenBuyRecord: List<YenBuyRecord>? = emptyList(),
    var createAt: String? = null,
) {
}


data class TargetRates(
    var dollarHighRates : List<Rate>? = emptyList(),
    var dollarLowRates: List<Rate>? = emptyList(),
    var yenHighRates : List<Rate>? = emptyList(),
    var yenLowRates : List<Rate>? = emptyList()
)


enum class CurrencyType(val code: String, val koreanName: String) {
    USD("USD", "달러"),
    JPY("JPY", "엔화"),
    EUR("EUR", "유로"),
    GBP("GBP", "파운드"),
    CHF("CHF", "프랑"),
    CAD("CAD", "캐나다 달러"),
    AUD("AUD", "호주 달러"),
    NZD("NZD", "뉴질랜드 달러"),
    CNY("CNY", "위안"),
    HKD("HKD", "홍콩 달러"),
    TWD("TWD", "대만 달러"),
    SGD("SGD", "싱가포르 달러")
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

    companion object {
        fun from(currency: CurrencyType, direction: RateDirection): RateType {
            return when (currency to direction) {  // 모든 케이스 처리
                CurrencyType.USD to RateDirection.HIGH -> USD_HIGH
                CurrencyType.USD to RateDirection.LOW -> USD_LOW
                CurrencyType.JPY to RateDirection.HIGH -> JPY_HIGH
                CurrencyType.JPY to RateDirection.LOW -> JPY_LOW
                else -> USD_HIGH
            }
        }
    }
}