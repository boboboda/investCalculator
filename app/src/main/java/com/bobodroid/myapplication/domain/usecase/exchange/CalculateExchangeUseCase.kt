// 파일 생성: domain/usecase/exchange/CalculateExchangeUseCase.kt

package com.bobodroid.myapplication.domain.usecase.exchange

import com.bobodroid.myapplication.models.datamodels.roomDb.Currency
import java.math.BigDecimal
import java.math.RoundingMode
import javax.inject.Inject

/**
 * 환율 계산 UseCase
 *
 * [기존 위치]
 * Currency.calculateExchangeMoney()
 * Currency.calculateSellProfit()
 * Currency.calculateExpectedProfit()
 *
 * [변경 사항]
 * - Currency 클래스에서 분리
 * - Currency 객체를 파라미터로 받아서 needsMultiply 속성 활용
 */
class CalculateExchangeUseCase @Inject constructor() {

    /**
     * 환전 금액 계산
     *
     * 원화를 외화로 환전할 때 받을 수 있는 외화 금액 계산
     *
     * @param currency 통화 객체 (needsMultiply 정보 포함)
     * @param money 원화 금액 (예: "1000000")
     * @param rate 환율 (예: USD "1300", JPY "944")
     * @return 외화 금액 (예: 769.23 USD, 105,932.20 JPY)
     *
     * 공식:
     * - USD 등: 외화금액 = 원화금액 / 환율
     *   예: 1,000,000원 / 1,300원 = 769.23 USD
     *
     * - JPY, THB 등 (needsMultiply=true): 외화금액 = 원화금액 / (환율/100)
     *   예: 1,000,000원 / (944/100) = 1,000,000 / 9.44 = 105,932.20 JPY
     */
    fun calculateExchangeMoney(
        currency: Currency,
        money: String,
        rate: String
    ): BigDecimal {
        val moneyBD = BigDecimal(money)
        val rateBD = BigDecimal(rate)

        return if (currency.needsMultiply) {
            // JPY, THB: 환율이 100단위당 가격이므로 100으로 나눔
            // 944원/100엔 → 9.44원/1엔
            val actualRate = rateBD.divide(BigDecimal(100), 20, RoundingMode.HALF_UP)
            moneyBD.divide(actualRate, 20, RoundingMode.HALF_UP)
        } else {
            // USD, EUR 등: 그대로 계산
            moneyBD.divide(rateBD, 20, RoundingMode.HALF_UP)
        }
    }

    /**
     * 매도 수익 계산
     *
     * 보유 외화를 매도했을 때의 실제 수익금 계산
     *
     * @param currency 통화 객체
     * @param exchangeMoney 보유 외화량 (예: "769.23" USD, "105932.20" JPY)
     * @param sellRate 매도 환율 (예: USD "1350", JPY "950")
     * @param krMoney 투자 원화 (예: "1000000")
     * @return 수익금 (예: +38,461원)
     *
     * 공식:
     * - USD 등: 수익 = (외화량 × 매도환율) - 투자원화
     *   예: (769.23 × 1,350) - 1,000,000 = +38,461원
     *
     * - JPY, THB 등: 수익 = (외화량 × 매도환율/100) - 투자원화
     *   예: (105,932.20 × 950/100) - 1,000,000 = +6,355원
     */
    fun calculateSellProfit(
        currency: Currency,
        exchangeMoney: String,
        sellRate: String,
        krMoney: String
    ): BigDecimal {
        val exchangeMoneyBD = BigDecimal(exchangeMoney)
        val sellRateBD = BigDecimal(sellRate)
        val krMoneyBD = BigDecimal(krMoney)

        val currentValue = if (currency.needsMultiply) {
            // JPY, THB: 환율을 100으로 나눔
            val actualRate = sellRateBD.divide(BigDecimal(100), 20, RoundingMode.HALF_UP)
            exchangeMoneyBD.multiply(actualRate)
        } else {
            // USD, EUR 등: 그대로 곱함
            exchangeMoneyBD.multiply(sellRateBD)
        }

        return currentValue.setScale(20, RoundingMode.HALF_UP) - krMoneyBD
    }

    /**
     * 예상 수익 계산
     *
     * 현재 환율 기준으로 보유 외화의 예상 수익 계산
     *
     * @param currency 통화 객체
     * @param exchangeMoney 보유 외화량
     * @param money 투자 원화
     * @param latestRate 현재 환율
     * @return 예상 수익 (문자열)
     *
     * 공식:
     * - USD 등: 예상수익 = (외화량 × 현재환율) - 투자원화
     * - JPY, THB 등: 예상수익 = (외화량 × 현재환율/100) - 투자원화
     */
    fun calculateExpectedProfit(
        currency: Currency,
        exchangeMoney: String,
        money: String,
        latestRate: String
    ): String {
        val exchangeMoneyBD = BigDecimal(exchangeMoney)
        val latestRateBD = BigDecimal(latestRate)
        val moneyBD = BigDecimal(money)

        val currentValue = if (currency.needsMultiply) {
            // JPY, THB: 환율을 100으로 나눔
            val actualRate = latestRateBD.divide(BigDecimal(100), 20, RoundingMode.HALF_UP)
            exchangeMoneyBD.multiply(actualRate)
        } else {
            // USD, EUR 등: 그대로 곱함
            exchangeMoneyBD.multiply(latestRateBD)
        }

        val profit = currentValue - moneyBD
        return profit.setScale(0, RoundingMode.DOWN).toString()
    }
}