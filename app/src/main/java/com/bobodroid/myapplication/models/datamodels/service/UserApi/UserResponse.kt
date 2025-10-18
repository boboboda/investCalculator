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

// ✅ 전체 응답 클래스 (소셜 로그인 지원)
@JsonClass(generateAdapter = true)
data class UserResponse(
    val success: Boolean,           // ✅ 추가: 성공 여부
    val message: String,
    val code: String? = null,       // ✅ 추가: 에러 코드 (ALREADY_LINKED 등)
    val data: UserResponseData? = null,
    val error: String? = null       // ✅ 추가: 에러 메시지
)

// ✅ 사용자 데이터 (소셜 로그인 필드 추가)
@JsonClass(generateAdapter = true)
data class UserResponseData(
    // 기본 필드
    val deviceId: String? = null,
    val createAt: String? = null,
    val fcmToken: String? = null,

    // ✅ 소셜 로그인 필드
    val socialId: String? = null,
    val socialType: String? = null,  // "GOOGLE", "KAKAO", "NONE"
    val email: String? = null,
    val nickname: String? = null,
    val profileUrl: String? = null,

    // ✅ 동기화 관련
    val isSynced: Boolean? = null,
    val updatedAt: String? = null,

    // 목표 환율
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



// ✅ 목표환율만 업데이트 (기존 호환성 유지)
@JsonClass(generateAdapter = true)
data class UserRatesUpdateRequest(
    val usdHighRates: List<Rate>? = null,
    val usdLowRates: List<Rate>? = null,
    val jpyHighRates: List<Rate>? = null,
    val jpyLowRates: List<Rate>? = null
)