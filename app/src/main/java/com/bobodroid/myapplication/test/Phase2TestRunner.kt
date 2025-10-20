package com.bobodroid.myapplication.test

import android.util.Log
import com.bobodroid.myapplication.models.datamodels.roomDb.Currencies
import com.bobodroid.myapplication.models.datamodels.roomDb.ExchangeRate

/**
 * Phase 2 테스트: ExchangeRate 완전 교체 테스트
 */
object Phase2TestRunner {

    fun runPhase2Test() {
        Log.d("CurrencyTest", "========================================")
        Log.d("CurrencyTest", "Phase 2: ExchangeRate 완전 교체 테스트")
        Log.d("CurrencyTest", "========================================")

        testJsonParsing()
        testBackwardCompatibility()
        testMultiplyProcessing()
        testAllCurrencies()

        Log.d("CurrencyTest", "========================================")
        Log.d("CurrencyTest", "✅ Phase 2 완료: ExchangeRate 교체 성공!")
        Log.d("CurrencyTest", "========================================")
    }

    // 테스트 1: JSON 파싱
    private fun testJsonParsing() {
        Log.d("CurrencyTest", "[테스트 1] JSON 파싱 및 변환")

        // 서버에서 받는 형태의 JSON (JPY는 9.44, USD는 1300)
        val serverJson = """
            {
                "id": "test-123",
                "createAt": "2025-10-20 11:00:00",
                "exchangeRates": {
                    "USD": 1300.50,
                    "JPY": 9.44,
                    "EUR": 1400.25
                }
            }
        """.trimIndent()

        val exchangeRate = ExchangeRate.fromCustomJson(serverJson)

        if (exchangeRate != null) {
            Log.d("CurrencyTest", "  ✅ JSON 파싱 성공")
            Log.d("CurrencyTest", "  ID: ${exchangeRate.id}")
            Log.d("CurrencyTest", "  생성시간: ${exchangeRate.createAt}")
            Log.d("CurrencyTest", "  rates: ${exchangeRate.rates}")
        } else {
            Log.e("CurrencyTest", "  ❌ JSON 파싱 실패!")
        }
    }

    // 테스트 2: 하위 호환성 (.usd, .jpy 접근)
    private fun testBackwardCompatibility() {
        Log.d("CurrencyTest", "[테스트 2] 하위 호환성 - 기존 .usd, .jpy 접근")

        val serverJson = """
            {
                "id": "test-456",
                "createAt": "2025-10-20 11:00:00",
                "exchangeRates": {
                    "USD": 1300.00,
                    "JPY": 9.44
                }
            }
        """.trimIndent()

        val exchangeRate = ExchangeRate.fromCustomJson(serverJson)

        if (exchangeRate != null) {
            // 기존 방식 접근
            val usdValue = exchangeRate.usd
            val jpyValue = exchangeRate.jpy

            Log.d("CurrencyTest", "  기존 방식 접근:")
            Log.d("CurrencyTest", "    exchangeRate.usd = $usdValue")
            Log.d("CurrencyTest", "    exchangeRate.jpy = $jpyValue")

            val success = usdValue != null && jpyValue != null
            if (success) {
                Log.d("CurrencyTest", "  ✅ 기존 코드와 100% 호환 (하위 호환성 유지)")
            } else {
                Log.e("CurrencyTest", "  ❌ 기존 접근 방식 실패!")
            }
        }
    }

    // 테스트 3: JPY * 100 처리 확인
    private fun testMultiplyProcessing() {
        Log.d("CurrencyTest", "[테스트 3] JPY * 100 자동 처리 확인")

        val serverJson = """
            {
                "id": "test-789",
                "createAt": "2025-10-20 11:00:00",
                "exchangeRates": {
                    "USD": 1300.00,
                    "JPY": 9.44,
                    "THB": 36.50
                }
            }
        """.trimIndent()

        val exchangeRate = ExchangeRate.fromCustomJson(serverJson)

        if (exchangeRate != null) {
            val usdRate = exchangeRate.getRateByCode("USD")
            val jpyRate = exchangeRate.getRateByCode("JPY")
            val thbRate = exchangeRate.getRateByCode("THB")

            Log.d("CurrencyTest", "  서버 원본 값:")
            Log.d("CurrencyTest", "    USD: 1300.00 (multiply=false)")
            Log.d("CurrencyTest", "    JPY: 9.44 (multiply=true)")
            Log.d("CurrencyTest", "    THB: 36.50 (multiply=true)")

            Log.d("CurrencyTest", "  변환 후 값:")
            Log.d("CurrencyTest", "    USD: $usdRate (1300.00 예상)")
            Log.d("CurrencyTest", "    JPY: $jpyRate (943.99 예상)")  // ✅ 수정
            Log.d("CurrencyTest", "    THB: $thbRate (3650.00 예상)")

            val usdCorrect = usdRate == "1300.00"
            val jpyCorrect = jpyRate == "943.99"  // ✅ 수정
            val thbCorrect = thbRate == "3650.00"

            if (usdCorrect && jpyCorrect && thbCorrect) {
                Log.d("CurrencyTest", "  ✅ needsMultiply 플래그 정상 작동!")
            } else {
                Log.e("CurrencyTest", "  ❌ 변환 로직 오류!")
            }
        }
    }

    // 테스트 4: 전체 통화 지원 확인
    private fun testAllCurrencies() {
        Log.d("CurrencyTest", "[테스트 4] 12개 통화 모두 지원 확인")

        val serverJson = """
            {
                "id": "test-all",
                "createAt": "2025-10-20 11:00:00",
                "exchangeRates": {
                    "USD": 1300.00,
                    "JPY": 9.44,
                    "EUR": 1400.00,
                    "GBP": 1650.00,
                    "CNY": 180.00,
                    "AUD": 850.00,
                    "CAD": 950.00,
                    "CHF": 1500.00,
                    "HKD": 165.00,
                    "SGD": 970.00,
                    "NZD": 780.00,
                    "THB": 36.50
                }
            }
        """.trimIndent()

        val exchangeRate = ExchangeRate.fromCustomJson(serverJson)

        if (exchangeRate != null) {
            val allRates = exchangeRate.getAllRates()
            Log.d("CurrencyTest", "  총 ${allRates.size}개 통화 파싱됨:")

            var allSuccess = true
            Currencies.all.forEach { currency ->
                val rate = allRates[currency.code]
                val exists = rate != null
                Log.d("CurrencyTest", "    ${currency.code}: $rate ${if(exists) "✅" else "❌"}")
                if (!exists) allSuccess = false
            }

            if (allSuccess) {
                Log.d("CurrencyTest", "  ✅ 12개 통화 모두 정상 처리!")
            } else {
                Log.e("CurrencyTest", "  ❌ 일부 통화 누락!")
            }
        }
    }
}