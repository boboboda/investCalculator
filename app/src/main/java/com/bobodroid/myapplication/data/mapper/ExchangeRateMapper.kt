package com.bobodroid.myapplication.data.mapper

import android.util.Log
import com.bobodroid.myapplication.MainActivity.Companion.TAG
import com.bobodroid.myapplication.data.local.entity.ExchangeRateDto
import com.bobodroid.myapplication.domain.entity.ExchangeRateEntity
import com.bobodroid.myapplication.models.datamodels.roomDb.Currencies
import org.json.JSONObject
import java.math.BigDecimal
import java.math.RoundingMode

/**
 * ExchangeRate Mapper
 *
 * DTO ↔ Entity 변환
 * - ExchangeRateDto (Room, JSON String) → ExchangeRateEntity (Domain, Map)
 * - ExchangeRateEntity (Domain, Map) → ExchangeRateDto (Room, JSON String)
 * - 서버 JSON → ExchangeRateEntity (WebSocket용)
 */
object ExchangeRateMapper {

    /**
     * DTO → Entity
     * Room 데이터 (JSON String)를 Domain 모델 (Map)로 변환
     */
    fun ExchangeRateDto.toEntity(): ExchangeRateEntity {
        val ratesMap = parseJsonToMap(this.rates)
        return ExchangeRateEntity(
            id = this.id,
            createAt = this.createAt,
            rates = ratesMap
        )
    }

    /**
     * Entity → DTO
     * Domain 모델 (Map)을 Room 데이터 (JSON String)로 변환
     */
    fun ExchangeRateEntity.toDto(): ExchangeRateDto {
        val ratesJson = mapToJson(this.rates)
        return ExchangeRateDto(
            id = this.id ?: "",
            createAt = this.createAt ?: "",
            rates = ratesJson
        )
    }

    /**
     * List<DTO> → List<Entity>
     */
    fun List<ExchangeRateDto>.toEntityList(): List<ExchangeRateEntity> {
        return this.map { it.toEntity() }
    }

    /**
     * List<Entity> → List<DTO>
     */
    fun List<ExchangeRateEntity>.toDtoList(): List<ExchangeRateDto> {
        return this.map { it.toDto() }
    }

    // ===== 헬퍼 함수 =====

    /**
     * JSON String → Map
     */
    private fun parseJsonToMap(json: String): Map<String, String> {
        return try {
            if (json.isBlank() || json == "{}") {
                return emptyMap()
            }

            val jsonObject = JSONObject(json)
            val map = mutableMapOf<String, String>()
            val keys = jsonObject.keys()

            while (keys.hasNext()) {
                val key = keys.next() as String
                val value = jsonObject.optString(key, "0")
                map[key] = value
            }

            map
        } catch (e: Exception) {
            Log.e(TAG("ExchangeRateMapper", "parseJsonToMap"), "파싱 실패", e)
            emptyMap()
        }
    }

    /**
     * Map → JSON String
     */
    private fun mapToJson(map: Map<String, String>): String {
        return try {
            if (map.isEmpty()) {
                return "{}"
            }

            val jsonObject = JSONObject()
            map.forEach { (key, value) ->
                jsonObject.put(key, value)
            }

            jsonObject.toString()
        } catch (e: Exception) {
            Log.e(TAG("ExchangeRateMapper", "mapToJson"), "변환 실패", e)
            "{}"
        }
    }

    /**
     * 서버 JSON → Entity 직접 변환 (WebSocket용)
     * ⭐ 소수점 반올림 및 포맷팅 적용
     */
    fun fromServerJson(jsonString: String): ExchangeRateEntity? {
        return try {
            Log.d(TAG("ExchangeRateMapper", "fromServerJson"), "━━━━━━━━━━━━━━━━━━━━━━━━━━")
            Log.d(TAG("ExchangeRateMapper", "fromServerJson"), "📥 서버 JSON 파싱 시작")
            Log.d(TAG("ExchangeRateMapper", "fromServerJson"), "원본 JSON: $jsonString")

            if (jsonString.isBlank()) {
                Log.w(TAG("ExchangeRateMapper", "fromServerJson"), "⚠️ 빈 JSON 문자열")
                return null
            }

            val jsonObject = JSONObject(jsonString)
            val id = jsonObject.optString("id", "")
            val createAt = jsonObject.optString("createAt", "N/A")
            val exchangeRates = jsonObject.optJSONObject("exchangeRates")

            Log.d(TAG("ExchangeRateMapper", "fromServerJson"), "ID: $id")
            Log.d(TAG("ExchangeRateMapper", "fromServerJson"), "생성시간: $createAt")

            if (exchangeRates == null) {
                Log.e(TAG("ExchangeRateMapper", "fromServerJson"), "❌ exchangeRates 객체 없음")
                return null
            }

            val ratesMap = mutableMapOf<String, String>()
            val keys = exchangeRates.keys()

            while (keys.hasNext()) {
                val currencyCode = keys.next() as String
                val rawValue = exchangeRates.optString(currencyCode, "0")

                // ⭐ Currency 정보 가져오기
                val currency = Currencies.findByCode(currencyCode)

                if (currency != null) {
                    // ⭐ 소수점 포맷팅
                    val formattedValue = formatRate(rawValue, currency.scale)
                    ratesMap[currencyCode] = formattedValue

                    Log.d(TAG("ExchangeRateMapper", "fromServerJson"),
                        "✅ $currencyCode: $rawValue → $formattedValue (scale=${currency.scale})")
                } else {
                    // 알 수 없는 통화는 그대로 저장
                    ratesMap[currencyCode] = rawValue
                    Log.w(TAG("ExchangeRateMapper", "fromServerJson"),
                        "⚠️ 알 수 없는 통화: $currencyCode = $rawValue")
                }
            }

            val entity = ExchangeRateEntity(
                id = id,
                createAt = createAt,
                rates = ratesMap
            )

            Log.d(TAG("ExchangeRateMapper", "fromServerJson"), "━━━━━━━━━━━━━━━━━━━━━━━━━━")
            Log.d(TAG("ExchangeRateMapper", "fromServerJson"), "✨ 파싱 완료!")
            Log.d(TAG("ExchangeRateMapper", "fromServerJson"), "최종 Entity: $entity")
            Log.d(TAG("ExchangeRateMapper", "fromServerJson"), "━━━━━━━━━━━━━━━━━━━━━━━━━━")

            entity
        } catch (e: Exception) {
            Log.e(TAG("ExchangeRateMapper", "fromServerJson"),
                "❌ JSON 파싱 실패: $jsonString", e)
            null
        }
    }

    /**
     * 환율 값 포맷팅
     * ⭐ 소수점 자리수에 맞게 반올림
     */
    private fun formatRate(value: String, scale: Int): String {
        return try {
            val bd = BigDecimal(value)
            bd.setScale(scale, RoundingMode.HALF_UP).toPlainString()
        } catch (e: Exception) {
            Log.e(TAG("ExchangeRateMapper", "formatRate"), "포맷팅 실패: $value", e)
            "0"
        }
    }

    /**
     * Entity → 서버 JSON 형식
     */
    fun ExchangeRateEntity.toServerJson(): String {
        return try {
            val jsonObject = JSONObject()
            jsonObject.put("id", this.id)
            jsonObject.put("createAt", this.createAt)

            val exchangeRatesJson = JSONObject()
            this.rates.forEach { (key, value) ->
                exchangeRatesJson.put(key, value)
            }
            jsonObject.put("exchangeRates", exchangeRatesJson)

            jsonObject.toString()
        } catch (e: Exception) {
            Log.e(TAG("ExchangeRateMapper", "toServerJson"), "JSON 변환 실패", e)
            "{}"
        }
    }
}