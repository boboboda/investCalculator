package com.bobodroid.myapplication.domain.entity

import com.bobodroid.myapplication.models.datamodels.roomDb.Currencies
import com.bobodroid.myapplication.models.datamodels.roomDb.Currency
import com.bobodroid.myapplication.models.datamodels.roomDb.CurrencyType
import com.bobodroid.myapplication.models.datamodels.roomDb.ForeignCurrencyRecord
import java.util.UUID

/**
 * Record Entity (Domain Layer)
 *
 * 플랫폼 독립적인 투자 기록 모델
 * - Room, Firebase 등 구체적 기술과 무관
 * - 비즈니스 로직에만 집중
 * - iOS 이식 가능
 * - ForeignCurrencyRecord 인터페이스 구현 (하위 호환성)
 *
 * [변경 사항]
 * 기존: CurrencyRecord (Room @Entity)
 * 신규: RecordEntity (Pure Kotlin)
 */
data class RecordEntity(
    /**
     * 고유 ID
     */
    override var id: UUID = UUID.randomUUID(),

    /**
     * 통화 코드 (USD, JPY, EUR 등)
     */
    val currencyCode: String,

    /**
     * 매수 날짜 (yyyy.MM.dd)
     */
    override var date: String?,

    /**
     * 매도 날짜 (yyyy.MM.dd)
     */
    override var sellDate: String?,

    /**
     * 원화 금액
     */
    override var money: String?,

    /**
     * 환율
     */
    override var rate: String?,

    /**
     * 매수 환율
     */
    override var buyRate: String?,

    /**
     * 매도 환율
     */
    override var sellRate: String?,

    /**
     * 수익
     */
    override var profit: String?,

    /**
     * 매도 수익
     */
    override var sellProfit: String?,

    /**
     * 예상 수익
     */
    override var expectProfit: String?,

    /**
     * 외화 금액
     */
    override var exchangeMoney: String?,

    /**
     * 매도 여부 (recordColor로 매핑)
     * true: 매도됨
     * false: 보유 중
     */
    override var recordColor: Boolean? = false,

    /**
     * 카테고리명
     */
    override var categoryName: String?,

    /**
     * 메모
     */
    override var memo: String?,

) : ForeignCurrencyRecord {


    /**
     * Currency 객체 가져오기
     */
    fun getCurrency(): Currency? = Currencies.findByCode(currencyCode)

    // ===== ForeignCurrencyRecord 인터페이스 구현 =====

    /**
     * 수익과 예상 수익 업데이트
     */
    override fun copyWithProfitAndExpectProfit(profit: String?): ForeignCurrencyRecord {
        return this.copy(
            profit = profit,
            expectProfit = profit
        )
    }

    /**
     * 메모 업데이트
     */
    override fun copyWithMemo(memo: String): ForeignCurrencyRecord {
        return this.copy(memo = memo)
    }

    /**
     * 매도 처리
     */
    override fun copyWithSell(
        sellDate: String,
        sellRate: String,
        sellProfit: String
    ): ForeignCurrencyRecord {
        return this.copy(
            sellDate = sellDate,
            sellRate = sellRate,
            sellProfit = sellProfit,
            expectProfit = sellProfit,
            recordColor = true
        )
    }

    // ===== 추가 비즈니스 로직 메서드 =====

    /**
     * 수익 업데이트 (불변 방식)
     */
    fun updateProfit(profit: String?): RecordEntity {
        return this.copy(
            profit = profit,
            expectProfit = profit
        )
    }

    /**
     * 메모 업데이트 (불변 방식)
     */
    fun updateMemo(newMemo: String): RecordEntity {
        return this.copy(memo = newMemo)
    }

    /**
     * 매도 처리 (불변 방식)
     */
    fun sell(
        sellDate: String,
        sellRate: String,
        sellProfit: String
    ): RecordEntity {
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
    fun cancelSell(): RecordEntity {
        return this.copy(
            sellDate = null,
            sellRate = null,
            sellProfit = null,
            recordColor = false
        )
    }

    /**
     * 카테고리 변경
     */
    fun updateCategory(categoryName: String): RecordEntity {
        return this.copy(categoryName = categoryName)
    }

    companion object {
        /**
         * 빈 RecordEntity 생성
         */
        fun empty(currencyCode: String = "USD"): RecordEntity {
            return RecordEntity(
                currencyCode = currencyCode,
                date = null,
                sellDate = null,
                money = null,
                rate = null,
                buyRate = null,
                sellRate = null,
                profit = null,
                sellProfit = null,
                expectProfit = null,
                exchangeMoney = null,
                recordColor = false,
                categoryName = null,
                memo = null
            )
        }
    }
}