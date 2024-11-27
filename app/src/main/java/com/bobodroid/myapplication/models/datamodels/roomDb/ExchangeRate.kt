package com.bobodroid.myapplication.models.datamodels.roomDb

import android.util.Log
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.bobodroid.myapplication.MainActivity.Companion.TAG
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.google.firebase.firestore.QuerySnapshot
import org.json.JSONException
import org.json.JSONObject
import java.util.UUID
import javax.annotation.Nonnull

@Entity(tableName = "exchangeRate_table")
data class ExchangeRate(
    @PrimaryKey
    @ColumnInfo(name = "id")
    @Nonnull
    var id: String = UUID.randomUUID().toString(),

    @ColumnInfo(name = "createAt", defaultValue = "N/A")  // 기본값으로 "N/A" 설정
    var createAt: String = "N/A",

    @ColumnInfo(name = "usd")
    var usd: String? = null,

    @ColumnInfo(name = "jpy")
    var jpy: String? = null
) {
    companion object {
        fun fromQuerySnapshot(data: QuerySnapshot): ExchangeRate? {
            val document = data.documents.firstOrNull() ?: return null
            return ExchangeRate(
                id = document.id,
                createAt = document.getString("createAt") ?: "",
                usd = (document["exchangeRates"] as? Map<String, String>)?.get("USD"),
                jpy = (document["exchangeRates"] as? Map<String, String>)?.get("JPY")
            )
        }

        fun fromDocumentSnapshot(data: DocumentSnapshot): ExchangeRate {
            return ExchangeRate(
                id = data.id,
                createAt = data.getString("createAt") ?: "",
                usd = (data["exchangeRates"] as? Map<String, String>)?.get("USD"),
                jpy = (data["exchangeRates"] as? Map<String, String>)?.get("JPY")
            )
        }

        fun fromQueryDocumentSnapshot(data: QueryDocumentSnapshot): ExchangeRate {
            return ExchangeRate(
                id = data.id,
                createAt = data.getString("createAt") ?: "",
                usd = (data["exchangeRates"] as? Map<String, String>)?.get("USD"),
                jpy = (data["exchangeRates"] as? Map<String, String>)?.get("JPY")
            )
        }

        fun fromCustomJson(jsonString: String): ExchangeRate? {
            return try {
                if (jsonString.isBlank()) {
                    Log.e(TAG("ExchangeRate","Type"), "fromCustomJson: 빈 문자열을 수신했습니다.")
                    return null
                }

                // JSON 객체로 변환 시도
                val jsonObject = JSONObject(jsonString)

                // 'exchangeRates' 필드가 있는지 확인
                val exchangeRates = jsonObject.optJSONObject("exchangeRates")
                if (exchangeRates == null) {
                    Log.e(TAG("ExchangeRate","Type"), "fromCustomJson: 'exchangeRates' 필드가 없습니다.")
                    return null
                }

                // 'USD' 및 'JPY' 필드가 있는지 확인 후 가져오기
                val usdRate = if (exchangeRates.has("USD")) exchangeRates.optDouble("USD").toString() else null
                val jpyRate = if (exchangeRates.has("JPY")) exchangeRates.optDouble("JPY").toString() else null

                // ExchangeRate 객체 생성
                ExchangeRate(
                    id = jsonObject.optString("id"),
                    createAt = jsonObject.optString("createAt", "N/A"),
                    usd = usdRate,
                    jpy = jpyRate
                )
            } catch (e: JSONException) {
                Log.e(TAG("ExchangeRate","Type"), "fromCustomJson: JSON 파싱 오류 발생", e)
                null
            }
        }

    }

    fun asHashMap(): HashMap<String, Any?> {
        return hashMapOf(
            "id" to id,
            "createAt" to createAt,
            "usd" to usd,
            "jpy" to jpy
        )
    }
}