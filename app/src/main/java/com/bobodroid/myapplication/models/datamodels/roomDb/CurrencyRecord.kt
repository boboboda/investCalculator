package com.bobodroid.myapplication.models.datamodels.roomDb

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID
import javax.annotation.Nonnull

/**
 * 통합 외화 매수 기록 엔티티
 * 모든 통화(USD, JPY, EUR 등)를 하나의 테이블에서 관리
 */
@Entity(tableName = "currency_records")
data class CurrencyRecord(
    @PrimaryKey
    @ColumnInfo(name = "id")
    @Nonnull
    override var id: UUID = UUID.randomUUID(),

    @ColumnInfo(name = "currency_code")
    var currencyCode: String,  // "USD", "JPY", "EUR" 등

    @ColumnInfo(name = "date")
    override var date: String? = null,

    @ColumnInfo(name = "sell_date")
    override var sellDate: String? = null,

    @ColumnInfo(name = "money")
    override var money: String? = null,  // 원화 금액

    @ColumnInfo(name = "rate")
    override var rate: String? = null,  // 환율

    @ColumnInfo(name = "buy_rate")
    override var buyRate: String? = null,  // 매수 환율

    @ColumnInfo(name = "sell_rate")
    override var sellRate: String? = null,  // 매도 환율

    @ColumnInfo(name = "profit")
    override var profit: String? = null,  // 수익

    @ColumnInfo(name = "sell_profit")
    override var sellProfit: String? = null,  // 매도 수익

    @ColumnInfo(name = "expect_profit")
    override var expectProfit: String? = null,  // 예상 수익

    @ColumnInfo(name = "exchange_money")
    override var exchangeMoney: String? = null,  // 외화 금액

    @ColumnInfo(name = "record_color")
    override var recordColor: Boolean? = false,  // 매도 여부 (Boolean? 타입으로 변경)

    @ColumnInfo(name = "category_name")
    override var categoryName: String? = null,  // 카테고리명

    @ColumnInfo(name = "memo")
    override var memo: String? = null  // 메모
) : ForeignCurrencyRecord {

    /**
     * Currency 객체 가져오기
     */
    fun getCurrency(): Currency? = Currencies.findByCode(currencyCode)

    /**
     * ForeignCurrencyRecord 인터페이스 구현 - 수익과 예상 수익 업데이트
     */
    override fun copyWithProfitAndExpectProfit(profit: String?): ForeignCurrencyRecord {
        return this.copy(
            profit = profit,
            expectProfit = profit
        )
    }

    /**
     * ForeignCurrencyRecord 인터페이스 구현 - 메모 업데이트
     */
    override fun copyWithMemo(memo: String): ForeignCurrencyRecord {
        return this.copy(memo = memo)
    }

    /**
     * ForeignCurrencyRecord 인터페이스 구현 - 매도 처리
     */
    override fun copyWithSell(sellDate: String, sellRate: String, sellProfit: String): ForeignCurrencyRecord {
        return this.copy(
            sellDate = sellDate,
            sellRate = sellRate,
            sellProfit = sellProfit,
            expectProfit = sellProfit,
            recordColor = true
        )
    }

    /**
     * 매도 취소
     */
    fun copyForCancelSell(): CurrencyRecord {
        return this.copy(
            sellDate = null,
            sellRate = null,
            sellProfit = null,
            recordColor = false
        )
    }

    /**
     * ForeignCurrencyRecord 인터페이스 구현 - CurrencyType 변환 (하위 호환성)
     */
    override fun toType(): CurrencyType {
        return when (currencyCode) {
            "USD" -> CurrencyType.USD
            "JPY" -> CurrencyType.JPY
            else -> CurrencyType.USD  // 기본값 - 새로운 통화는 USD로 처리 (하위 호환성)
        }
    }
}

/**
 * 마이그레이션용 변환 함수들
 * 기존 테이블의 데이터를 새 테이블로 변환
 */
fun DrBuyRecord.toCurrencyRecord(): CurrencyRecord {
    return CurrencyRecord(
        id = this.id,
        currencyCode = "USD",
        date = this.date,
        sellDate = this.sellDate,
        money = this.money,
        rate = this.rate,
        buyRate = this.buyRate,
        sellRate = this.sellRate,
        profit = this.profit,
        sellProfit = this.sellProfit,
        expectProfit = this.expectProfit,
        exchangeMoney = this.exchangeMoney,
        recordColor = this.recordColor,
        categoryName = this.categoryName,
        memo = this.memo
    )
}

fun YenBuyRecord.toCurrencyRecord(): CurrencyRecord {
    return CurrencyRecord(
        id = this.id,
        currencyCode = "JPY",
        date = this.date,
        sellDate = this.sellDate,
        money = this.money,
        rate = this.rate,
        buyRate = this.buyRate,
        sellRate = this.sellRate,
        profit = this.profit,
        sellProfit = this.sellProfit,
        expectProfit = this.expectProfit,
        exchangeMoney = this.exchangeMoney,
        recordColor = this.recordColor,
        categoryName = this.categoryName,
        memo = this.memo
    )
}

/**
 * CurrencyRecord를 기존 타입으로 역변환 (하위 호환성)
 * 기존 코드가 DrBuyRecord, YenBuyRecord를 사용하는 경우를 위함
 */
fun CurrencyRecord.toLegacyDollarRecord(): DrBuyRecord {
    return DrBuyRecord(
        id = this.id,
        date = this.date,
        sellDate = this.sellDate,
        money = this.money,
        rate = this.rate,
        buyRate = this.buyRate,
        sellRate = this.sellRate,
        profit = this.profit,
        sellProfit = this.sellProfit,
        expectProfit = this.expectProfit,
        exchangeMoney = this.exchangeMoney,
        recordColor = this.recordColor,
        categoryName = this.categoryName,
        memo = this.memo
    )
}

fun CurrencyRecord.toLegacyYenRecord(): YenBuyRecord {
    return YenBuyRecord(
        id = this.id,
        date = this.date,
        sellDate = this.sellDate,
        money = this.money,
        rate = this.rate,
        buyRate = this.buyRate,
        sellRate = this.sellRate,
        profit = this.profit,
        sellProfit = this.sellProfit,
        expectProfit = this.expectProfit,
        exchangeMoney = this.exchangeMoney,
        recordColor = this.recordColor,
        categoryName = this.categoryName,
        memo = this.memo
    )
}