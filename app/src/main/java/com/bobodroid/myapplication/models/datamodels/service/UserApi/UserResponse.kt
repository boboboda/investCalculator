package com.bobodroid.myapplication.models.datamodels.service.UserApi

import android.util.Log
import com.bobodroid.myapplication.MainActivity.Companion.TAG
import com.bobodroid.myapplication.models.datamodels.roomDb.ExchangeRate
import com.bobodroid.myapplication.models.datamodels.roomDb.TargetRates
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

@JsonClass(generateAdapter = true)
data class Rate(
    val number: Int,
    val rate: Int
)

// 전체 응답 클래스 (메시지 포함)
@JsonClass(generateAdapter = true)
data class UserResponse(
    val message: String, // 메시지 필드 추가
    val data: UserData? = null// 데이터를 별도로 포함하는 구조로 변경
)

@JsonClass(generateAdapter = true)
data class UserData(
    val customId: String? = null,
    val deviceId: String? = null,
    val createAt: String,
    val fcmToken: String,
    val usdHighRates: List<Rate>? = emptyList(),
    val usdLowRates: List<Rate>? = emptyList(),
    val jpyHighRates: List<Rate>? = emptyList(),
    val jpyLowRates: List<Rate>? = emptyList(),
) {

    companion object {
        fun fromTargetRateJson(jsonString: String): TargetRates? {
            return try {
                if (jsonString.isBlank()) {
                    Log.e(TAG("TargetRates",""), "fromCustomJson: 빈 문자열을 수신했습니다.")
                    return null
                }

                // JSON 객체로 변환 시도
                val jsonObject = JSONObject(jsonString)

                // JSON 배열을 Rate 리스트로 변환하는 함수
                fun JSONArray.toRateList(): List<Rate> {
                    val rateList = mutableListOf<Rate>()
                    for (i in 0 until length()) {
                        val rateObj = getJSONObject(i)
                        rateList.add(
                            Rate(
                                number = rateObj.getInt("number"),
                                rate = rateObj.getInt("rate")
                            )
                        )
                    }
                    return rateList
                }

                // 'exchangeRates' 필드가 있는지 확인
                val usdHighRates = jsonObject.optJSONArray("usdHighRates")?.toRateList() ?: emptyList()
                val usdLowRates = jsonObject.optJSONArray("usdLowRates")?.toRateList() ?: emptyList()
                val jpyHighRates = jsonObject.optJSONArray("jpyHighRates")?.toRateList() ?: emptyList()
                val jpyLowRates = jsonObject.optJSONArray("jpyLowRates")?.toRateList() ?: emptyList()

                TargetRates(
                    dollarHighRates = usdHighRates,
                    dollarLowRates = usdLowRates,
                    yenHighRates = jpyHighRates,
                    yenLowRates = jpyLowRates
                )
            } catch (e: JSONException) {
                Log.e(TAG("ExchangeRate","Type"), "fromCustomJson: JSON 파싱 오류 발생", e)
                null
            }
        }
    }
}

// 업데이트 요청 데이터 클래스
@JsonClass(generateAdapter = true)
data class UserRatesUpdateRequest(
    val usdHighRates: List<Rate>? = null,
    val usdLowRates: List<Rate>? = null,
    val jpyHighRates: List<Rate>? = null,
    val jpyLowRates: List<Rate>? = null
)

