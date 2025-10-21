// app/src/main/java/com/bobodroid/myapplication/models/datamodels/service/UserApi/UserResponse.kt

package com.bobodroid.myapplication.models.datamodels.service.UserApi

import android.util.Log
import com.bobodroid.myapplication.MainActivity.Companion.TAG
import com.bobodroid.myapplication.models.datamodels.roomDb.CurrencyTargetRates
import com.bobodroid.myapplication.models.datamodels.roomDb.CurrencyType
import com.bobodroid.myapplication.models.datamodels.roomDb.TargetRates
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import org.json.JSONException
import org.json.JSONObject

@JsonClass(generateAdapter = true)
data class Rate(
    val number: Int,
    val rate: Int
)

// ✅ 전체 응답 클래스
@JsonClass(generateAdapter = true)
data class UserResponse(
    val success: Boolean,
    val message: String,
    val code: String? = null,
    val data: UserResponseData? = null,
    val error: String? = null
)

// ✅ 사용자 데이터 (12개 통화 지원)
@JsonClass(generateAdapter = true)
data class UserResponseData(
    val deviceId: String? = null,
    val createAt: String? = null,
    val fcmToken: String? = null,

    // 소셜 로그인 필드
    val socialId: String? = null,
    val socialType: String? = null,
    val email: String? = null,
    val nickname: String? = null,
    val profileUrl: String? = null,

    // 동기화 관련
    val isSynced: Boolean? = null,
    val updatedAt: String? = null,

    // ✅ 새로운 목표환율 구조 (12개 통화)
    val targetRates: Map<String, CurrencyRatesJson>? = null
) {
    companion object {
        fun fromTargetRateJson(jsonString: String): TargetRates? {
            return try {
                if (jsonString.isBlank()) {
                    Log.e(TAG("TargetRates", ""), "fromJson: 빈 문자열")
                    return null
                }

                val jsonObject = JSONObject(jsonString)
                val targetRatesJson = jsonObject.optJSONObject("targetRates")

                if (targetRatesJson == null) {
                    Log.e(TAG("TargetRates", ""), "targetRates 필드 없음")
                    return null
                }

                val ratesMap = mutableMapOf<CurrencyType, CurrencyTargetRates>()

                // 모든 통화 순회
                CurrencyType.values().forEach { currencyType ->
                    val currencyCode = currencyType.code
                    val currencyJson = targetRatesJson.optJSONObject(currencyCode)

                    if (currencyJson != null) {
                        val highArray = currencyJson.optJSONArray("high")
                        val lowArray = currencyJson.optJSONArray("low")

                        val highRates = mutableListOf<Rate>()
                        val lowRates = mutableListOf<Rate>()

                        // High rates 파싱
                        if (highArray != null) {
                            for (i in 0 until highArray.length()) {
                                val rateObj = highArray.getJSONObject(i)
                                highRates.add(
                                    Rate(
                                        number = rateObj.getInt("number"),
                                        rate = rateObj.getInt("rate")
                                    )
                                )
                            }
                        }

                        // Low rates 파싱
                        if (lowArray != null) {
                            for (i in 0 until lowArray.length()) {
                                val rateObj = lowArray.getJSONObject(i)
                                lowRates.add(
                                    Rate(
                                        number = rateObj.getInt("number"),
                                        rate = rateObj.getInt("rate")
                                    )
                                )
                            }
                        }

                        if (highRates.isNotEmpty() || lowRates.isNotEmpty()) {
                            ratesMap[currencyType] = CurrencyTargetRates(
                                high = highRates,
                                low = lowRates
                            )
                        }
                    }
                }

                TargetRates(rates = ratesMap)

            } catch (e: JSONException) {
                Log.e(TAG("TargetRates", "fromJson"), "JSON 파싱 오류", e)
                null
            }
        }
    }
}

// ✅ JSON 파싱용 데이터 클래스
@JsonClass(generateAdapter = true)
data class CurrencyRatesJson(
    val high: List<Rate>? = null,
    val low: List<Rate>? = null
)

// ✅ 목표환율 업데이트 요청 (12개 통화 지원)
@JsonClass(generateAdapter = true)
data class UserRatesUpdateRequest(
    val targetRates: Map<String, CurrencyRatesJson>? = null
) {
    companion object {
        // ✅ TargetRates → 서버 요청 형식 변환
        fun fromTargetRates(targetRates: TargetRates): UserRatesUpdateRequest {
            val targetRatesMap = mutableMapOf<String, CurrencyRatesJson>()

            targetRates.rates.forEach { (currencyType, currencyRates) ->
                targetRatesMap[currencyType.code] = CurrencyRatesJson(
                    high = currencyRates.high,
                    low = currencyRates.low
                )
            }

            return UserRatesUpdateRequest(targetRates = targetRatesMap)
        }

        // ✅ 특정 통화만 업데이트
        fun forCurrency(
            currency: CurrencyType,
            high: List<Rate>? = null,
            low: List<Rate>? = null
        ): UserRatesUpdateRequest {
            val targetRatesMap = mapOf(
                currency.code to CurrencyRatesJson(high = high, low = low)
            )
            return UserRatesUpdateRequest(targetRates = targetRatesMap)
        }
    }
}