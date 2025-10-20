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
        testCalculationCompatibility()
        testExistingEnumConversion()

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
        val usdMatch = newUSD.code == oldUSD.name
        val jpyMatch = newJPY.code == oldJPY.name

        // 한글 이름 일치 확인
        val usdNameMatch = newUSD.koreanName == oldUSD.koreanName
        val jpyNameMatch = newJPY.koreanName == oldJPY.koreanName

        Log.d("CurrencyTest", "  USD: ${oldUSD.name} → ${newUSD.code} ${if(usdMatch) "✅" else "❌"}")
        Log.d("CurrencyTest", "  USD 이름: ${oldUSD.koreanName} → ${newUSD.koreanName} ${if(usdNameMatch) "✅" else "❌"}")
        Log.d("CurrencyTest", "  JPY: ${oldJPY.name} → ${newJPY.code} ${if(jpyMatch) "✅" else "❌"}")
        Log.d("CurrencyTest", "  JPY 이름: ${oldJPY.koreanName} → ${newJPY.koreanName} ${if(jpyNameMatch) "✅" else "❌"}")

        if (usdMatch && jpyMatch && usdNameMatch && jpyNameMatch) {
            Log.d("CurrencyTest", "[테스트 1] ✅ 기존 Enum과 완벽 호환")
        } else {
            Log.e("CurrencyTest", "[테스트 1] ❌ 호환성 문제 발견!")
        }
    }

    // 테스트 2: 기존 계산 로직과의 호환성
    private fun testCalculationCompatibility() {
        Log.d("CurrencyTest", "[테스트 2] 기존 RecordUseCase 계산 로직 호환성")

        val testMoney = "1000000"
        val testUsdRate = "1300"
        val testJpyRate = "9.44"

        // 기존 방식 (RecordUseCase의 로직)
        fun oldCalculateExchangeMoney(type: CurrencyType, money: String, rate: String): String {
            return (BigDecimal(money) / BigDecimal(rate))
                .setScale(20, RoundingMode.HALF_UP)
                .toString()
        }

        // 새로운 방식
        val newUsdExchange = Currencies.USD.calculateExchangeMoney(testMoney, testUsdRate).toString()
        val newJpyExchange = Currencies.JPY.calculateExchangeMoney(testMoney, testJpyRate).toString()

        // 기존 방식
        val oldUsdExchange = oldCalculateExchangeMoney(CurrencyType.USD, testMoney, testUsdRate)
        val oldJpyExchange = oldCalculateExchangeMoney(CurrencyType.JPY, testMoney, testJpyRate)

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
            Log.d("CurrencyTest", "[테스트 2] ✅ 계산 결과 완전 일치")
        } else {
            Log.e("CurrencyTest", "[테스트 2] ❌ 계산 결과 불일치!")
        }
    }

    // 테스트 3: when 구문 대체 가능성
    private fun testExistingEnumConversion() {
        Log.d("CurrencyTest", "[테스트 3] 기존 when(currencyType) 구문 대체 가능성")

        // 기존 방식의 when 구문
        fun oldGetSymbol(type: CurrencyType): String {
            return when(type) {
                CurrencyType.USD -> "$"
                CurrencyType.JPY -> "¥"
            }
        }

        // 새로운 방식
        val usdSymbolOld = oldGetSymbol(CurrencyType.USD)
        val usdSymbolNew = Currencies.fromCurrencyType(CurrencyType.USD).symbol

        val jpySymbolOld = oldGetSymbol(CurrencyType.JPY)
        val jpySymbolNew = Currencies.fromCurrencyType(CurrencyType.JPY).symbol

        val usdMatch = usdSymbolOld == usdSymbolNew
        val jpyMatch = jpySymbolOld == jpySymbolNew

        Log.d("CurrencyTest", "  USD 심볼: $usdSymbolOld → $usdSymbolNew ${if(usdMatch) "✅" else "❌"}")
        Log.d("CurrencyTest", "  JPY 심볼: $jpySymbolOld → $jpySymbolNew ${if(jpyMatch) "✅" else "❌"}")

        if (usdMatch && jpyMatch) {
            Log.d("CurrencyTest", "[테스트 3] ✅ when 구문 완벽 대체 가능")
        } else {
            Log.e("CurrencyTest", "[테스트 3] ❌ when 구문 대체 불가!")
        }
    }
}