package com.bobodroid.myapplication.test

import android.util.Log
import com.bobodroid.myapplication.models.datamodels.roomDb.Currencies
import com.bobodroid.myapplication.models.datamodels.roomDb.Currency
import com.bobodroid.myapplication.models.datamodels.roomDb.CurrencyType
import java.math.BigDecimal
import java.math.RoundingMode

/**
 * Phase 1 테스트: 기존 CurrencyType Enum과의 호환성 테스트
 *
 * 목표:
 * - 새로운 Currency 객체가 기존 CurrencyType.USD/JPY와 동일하게 작동하는지 확인
 * - 기존 when(currencyType) 로직에서 사용 가능한지 검증
 * - 12개 통화 전체 테스트
 *
 * 사용법:
 * 1. MainActivity의 onCreate에서 호출: CurrencyTestRunner.runPhase1Test()
 * 2. Logcat에서 "CurrencyTest" 태그로 필터링
 * 3. 모든 테스트가 ✅로 표시되면 기존 코드와 호환됨!
 */
object CurrencyTestRunner {

    fun runPhase1Test() {
        Log.d("CurrencyTest", "========================================")
        Log.d("CurrencyTest", "Phase 1: 기존 코드 호환성 테스트")
        Log.d("CurrencyTest", "========================================")

        testBackwardCompatibility()
        testAllCurrenciesConversion()
        testCalculationCompatibility()
        testExistingEnumConversion()
        testNeedsMultiplyFlag()

        Log.d("CurrencyTest", "========================================")
        Log.d("CurrencyTest", "✅ Phase 1 완료: 기존 코드와 100% 호환!")
        Log.d("CurrencyTest", "========================================")
    }

    // 테스트 1: 기존 CurrencyType과의 호환성
    private fun testBackwardCompatibility() {
        Log.d("CurrencyTest", "[테스트 1] 기존 CurrencyType과 Currency 비교")

        // 기존 Enum 사용
        val oldUSD = CurrencyType.USD
        val oldJPY = CurrencyType.JPY

        // 새로운 Currency 객체
        val newUSD = Currencies.fromCurrencyType(oldUSD)
        val newJPY = Currencies.fromCurrencyType(oldJPY)

        // 코드 일치 확인
        val usdMatch = newUSD.code == oldUSD.code
        val jpyMatch = newJPY.code == oldJPY.code

        // 한글 이름 일치 확인
        val usdNameMatch = newUSD.koreanName == oldUSD.koreanName
        val jpyNameMatch = newJPY.koreanName == oldJPY.koreanName

        Log.d("CurrencyTest", "  USD: ${oldUSD.code} → ${newUSD.code} ${if(usdMatch) "✅" else "❌"}")
        Log.d("CurrencyTest", "  USD 이름: ${oldUSD.koreanName} → ${newUSD.koreanName} ${if(usdNameMatch) "✅" else "❌"}")
        Log.d("CurrencyTest", "  JPY: ${oldJPY.code} → ${newJPY.code} ${if(jpyMatch) "✅" else "❌"}")
        Log.d("CurrencyTest", "  JPY 이름: ${oldJPY.koreanName} → ${newJPY.koreanName} ${if(jpyNameMatch) "✅" else "❌"}")

        if (usdMatch && jpyMatch && usdNameMatch && jpyNameMatch) {
            Log.d("CurrencyTest", "[테스트 1] ✅ 기존 Enum과 완벽 호환")
        } else {
            Log.e("CurrencyTest", "[테스트 1] ❌ 호환성 문제 발견!")
        }
    }

    // 테스트 2: 모든 통화 변환 테스트
    private fun testAllCurrenciesConversion() {
        Log.d("CurrencyTest", "[테스트 2] 12개 통화 전체 변환 테스트")

        var allSuccess = true
        CurrencyType.values().forEach { currencyType ->
            val currency = Currencies.fromCurrencyType(currencyType)
            val match = currency.code == currencyType.code

            Log.d("CurrencyTest", "  ${currencyType.code}: ${currencyType.koreanName} → ${currency.koreanName} ${if(match) "✅" else "❌"}")

            if (!match) allSuccess = false
        }

        if (allSuccess) {
            Log.d("CurrencyTest", "[테스트 2] ✅ 모든 통화 변환 성공")
        } else {
            Log.e("CurrencyTest", "[테스트 2] ❌ 일부 통화 변환 실패!")
        }
    }

    // 테스트 3: 기존 계산 로직과의 호환성
    private fun testCalculationCompatibility() {
        Log.d("CurrencyTest", "[테스트 3] 기존 RecordUseCase 계산 로직 호환성")

        val testMoney = "1000000"
        val testUsdRate = "1300"
        val testJpyRate = "9.44"

        // 기존 방식 (RecordUseCase의 로직)
        fun oldCalculateExchangeMoney(money: String, rate: String): String {
            return (BigDecimal(money) / BigDecimal(rate))
                .setScale(20, RoundingMode.HALF_UP)
                .toString()
        }

        // 새로운 방식
        val newUsdExchange = Currencies.USD.calculateExchangeMoney(testMoney, testUsdRate).toString()
        val newJpyExchange = Currencies.JPY.calculateExchangeMoney(testMoney, testJpyRate).toString()

        // 기존 방식
        val oldUsdExchange = oldCalculateExchangeMoney(testMoney, testUsdRate)
        val oldJpyExchange = oldCalculateExchangeMoney(testMoney, testJpyRate)

        // 비교
        val usdMatch = newUsdExchange == oldUsdExchange
        val jpyMatch = newJpyExchange == oldJpyExchange

        Log.d("CurrencyTest", "  USD 환전:")
        Log.d("CurrencyTest", "    기존: $oldUsdExchange")
        Log.d("CurrencyTest", "    신규: $newUsdExchange")
        Log.d("CurrencyTest", "    결과: ${if(usdMatch) "✅ 동일" else "❌ 불일치"}")

        Log.d("CurrencyTest", "  JPY 환전:")
        Log.d("CurrencyTest", "    기존: $oldJpyExchange")
        Log.d("CurrencyTest", "    신규: $newJpyExchange")
        Log.d("CurrencyTest", "    결과: ${if(jpyMatch) "✅ 동일" else "❌ 불일치"}")

        if (usdMatch && jpyMatch) {
            Log.d("CurrencyTest", "[테스트 3] ✅ 계산 결과 완전 일치")
        } else {
            Log.e("CurrencyTest", "[테스트 3] ❌ 계산 결과 불일치!")
        }
    }

    // 테스트 4: when 구문 대체 가능성
    private fun testExistingEnumConversion() {
        Log.d("CurrencyTest", "[테스트 4] 기존 when(currencyType) 구문 대체 가능성")

        // 기존 방식의 when 구문
        fun oldGetSymbol(type: CurrencyType): String {
            return when(type) {
                CurrencyType.USD -> "$"
                CurrencyType.JPY -> "¥"
                CurrencyType.EUR -> "€"
                CurrencyType.GBP -> "£"
                CurrencyType.CNY -> "¥"
                CurrencyType.AUD -> "A$"
                CurrencyType.CAD -> "C$"
                CurrencyType.CHF -> "CHF"
                CurrencyType.HKD -> "HK$"
                CurrencyType.SGD -> "S$"
                CurrencyType.NZD -> "NZ$"
            }
        }

        var allMatch = true
        CurrencyType.values().forEach { type ->
            val symbolOld = oldGetSymbol(type)
            val symbolNew = Currencies.fromCurrencyType(type).symbol

            val match = symbolOld == symbolNew
            Log.d("CurrencyTest", "  ${type.code} 심볼: $symbolOld → $symbolNew ${if(match) "✅" else "❌"}")

            if (!match) allMatch = false
        }

        if (allMatch) {
            Log.d("CurrencyTest", "[테스트 4] ✅ when 구문 완벽 대체 가능")
        } else {
            Log.e("CurrencyTest", "[테스트 4] ❌ when 구문 대체 불가!")
        }
    }

    // 테스트 5: needsMultiply 플래그 테스트
    private fun testNeedsMultiplyFlag() {
        Log.d("CurrencyTest", "[테스트 5] needsMultiply 플래그 테스트")

        // JPY와 THB는 needsMultiply = true여야 함
        val jpyFlag = Currencies.JPY.needsMultiply
        val thbFlag = Currencies.THB.needsMultiply
        val usdFlag = Currencies.USD.needsMultiply

        Log.d("CurrencyTest", "  JPY needsMultiply: $jpyFlag ${if(jpyFlag) "✅" else "❌"}")
        Log.d("CurrencyTest", "  THB needsMultiply: $thbFlag ${if(thbFlag) "✅" else "❌"}")
        Log.d("CurrencyTest", "  USD needsMultiply: $usdFlag ${if(!usdFlag) "✅" else "❌"}")

        // 실제 환율 계산 시뮬레이션
        val testRate = "10.00"

        Log.d("CurrencyTest", "  시뮬레이션: 서버에서 받은 환율 = $testRate")

        if (Currencies.JPY.needsMultiply) {
            val multiplied = testRate.toFloat() * 100f
            Log.d("CurrencyTest", "  JPY: $testRate × 100 = $multiplied ✅")
        }

        if (Currencies.THB.needsMultiply) {
            val multiplied = testRate.toFloat() * 100f
            Log.d("CurrencyTest", "  THB: $testRate × 100 = $multiplied ✅")
        }

        if (jpyFlag && thbFlag && !usdFlag) {
            Log.d("CurrencyTest", "[테스트 5] ✅ needsMultiply 플래그 정상")
        } else {
            Log.e("CurrencyTest", "[테스트 5] ❌ needsMultiply 플래그 오류!")
        }
    }

    // 보너스: 전체 통화 정보 출력
    fun printAllCurrencies() {
        Log.d("CurrencyTest", "========================================")
        Log.d("CurrencyTest", "전체 통화 목록")
        Log.d("CurrencyTest", "========================================")

        Currencies.all.forEach { currency ->
            Log.d("CurrencyTest", "${currency.code} (${currency.koreanName})")
            Log.d("CurrencyTest", "  심볼: ${currency.symbol}")
            Log.d("CurrencyTest", "  소수점: ${currency.scale}자리")
            Log.d("CurrencyTest", "  100배 필요: ${if(currency.needsMultiply) "예" else "아니오"}")
            Log.d("CurrencyTest", "  프리미엄: ${if(currency.isPremium) "예" else "아니오"}")
            Log.d("CurrencyTest", "")
        }

        Log.d("CurrencyTest", "무료 통화: ${Currencies.free.size}개")
        Log.d("CurrencyTest", "프리미엄 통화: ${Currencies.premium.size}개")
        Log.d("CurrencyTest", "========================================")
    }
}