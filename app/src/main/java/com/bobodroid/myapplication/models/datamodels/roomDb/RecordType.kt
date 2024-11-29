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


enum class CurrencyType(val koreanName: String) {
    USD("달러"),
    JPY("엔화")
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