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
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.UUID
import javax.annotation.Nonnull

@Entity(tableName = "exchangeRate_table")
data class ExchangeRate(
    @PrimaryKey
    @ColumnInfo(name = "id")
    @Nonnull
    var id: String = UUID.randomUUID().toString(),

    @ColumnInfo(name = "createAt", defaultValue = "N/A")
    var createAt: String = "N/A",

    @ColumnInfo(name = "rates")
    var rates: String = "{}"
) {
    // ✅ 하위 호환성: 기존 코드에서 .usd, .jpy 접근 가능
    val usd: String?
        get() = getRateByCode("USD")

    val jpy: String?
        get() = getRateByCode("JPY")

    fun getRateByCode(code: String): String? {
        return try {
            val json = JSONObject(rates)
            json.optString(code).takeIf { it.isNotEmpty() }
        } catch (e: Exception) {
            null
        }
    }

    fun getRate(currency: Currency): String {
        return getRateByCode(currency.code) ?: "0"
    }

    fun getAllRates(): Map<String, String> {
        return try {
            val json = JSONObject(rates)
            val map = mutableMapOf<String, String>()
            val keys = json.keys()
            while (keys.hasNext()) {
                val key = keys.next() as String
                map[key] = json.getString(key)
            }
            map
        } catch (e: Exception) {
            emptyMap()
        }
    }

    companion object {
        fun fromCustomJson(jsonString: String): ExchangeRate? {
            return try {
                if (jsonString.isBlank()) {
                    Log.e(TAG("ExchangeRate", "Type"), "fromCustomJson: 빈 문자열을 수신했습니다.")
                    return null
                }

                val jsonObject = JSONObject(jsonString)
                val exchangeRates = jsonObject.optJSONObject("exchangeRates")

                if (exchangeRates == null) {
                    Log.e(TAG("ExchangeRate", "Type"), "fromCustomJson: 'exchangeRates' 필드가 없습니다.")
                    return null
                }

                val ratesMap = mutableMapOf<String, String>()

                Currencies.all.forEach { currency ->
                    if (exchangeRates.has(currency.code)) {
                        val rawValue = exchangeRates.optDouble(currency.code)

                        val processedValue = if (currency.needsMultiply) {
                            BigDecimal(rawValue)
                                .multiply(BigDecimal("100"))
                                .setScale(2, RoundingMode.DOWN)
                        } else {
                            BigDecimal(rawValue)
                                .setScale(2, RoundingMode.DOWN)
                        }

                        ratesMap[currency.code] = processedValue.toString()
                    }
                }

                val ratesJson = JSONObject().apply {
                    ratesMap.forEach { (key, value) ->
                        put(key, value)
                    }
                }.toString()

                ExchangeRate(
                    id = jsonObject.optString("id"),
                    createAt = jsonObject.optString("createAt", "N/A"),
                    rates = ratesJson
                )
            } catch (e: JSONException) {
                Log.e(TAG("ExchangeRate", "Type"), "fromCustomJson: JSON 파싱 오류 발생", e)
                null
            }
        }

        fun fromQuerySnapshot(data: QuerySnapshot): ExchangeRate? {
            val document = data.documents.firstOrNull() ?: return null
            return fromDocumentSnapshot(document)
        }

        fun fromDocumentSnapshot(data: DocumentSnapshot): ExchangeRate {
            val ratesMap = data["exchangeRates"] as? Map<*, *> ?: emptyMap<String, Any>()

            val processedRates = mutableMapOf<String, String>()
            Currencies.all.forEach { currency ->
                val rawValue = when (val value = ratesMap[currency.code]) {
                    is Number -> value.toDouble()
                    is String -> value.toDoubleOrNull() ?: 0.0
                    else -> 0.0
                }

                val processedValue = if (currency.needsMultiply) {
                    BigDecimal(rawValue).multiply(BigDecimal("100"))
                } else {
                    BigDecimal(rawValue)
                }.setScale(2, RoundingMode.HALF_UP)  // ✅ DOWN → HALF_UP

                processedRates[currency.code] = processedValue.toString()
            }

            val ratesJson = JSONObject().apply {
                processedRates.forEach { (key, value) ->
                    put(key, value)
                }
            }.toString()

            return ExchangeRate(
                id = data.id,
                createAt = data.getString("createAt") ?: "",
                rates = ratesJson
            )
        }

        fun fromQueryDocumentSnapshot(data: QueryDocumentSnapshot): ExchangeRate {
            return fromDocumentSnapshot(data)
        }
    }

    fun asHashMap(): HashMap<String, Any?> {
        return hashMapOf(
            "id" to id,
            "createAt" to createAt,
            "exchangeRates" to getAllRates()
        )
    }
}