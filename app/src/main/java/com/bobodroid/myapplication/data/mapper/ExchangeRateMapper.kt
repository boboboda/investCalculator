package com.bobodroid.myapplication.data.mapper

import com.bobodroid.myapplication.data.local.entity.ExchangeRateDto
import com.bobodroid.myapplication.domain.entity.ExchangeRateEntity
import com.bobodroid.myapplication.models.datamodels.roomDb.ExchangeRate
import org.json.JSONObject

/**
 * ExchangeRate Mapper
 *
 * DTO ↔ Entity 변환
 * - ExchangeRateDto (Room, JSON String) → ExchangeRateEntity (Domain, Map)
 * - ExchangeRateEntity (Domain, Map) → ExchangeRateDto (Room, JSON String)
 * - 하위 호환: ExchangeRate → ExchangeRateEntity
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
            id = this.id,
            createAt = this.createAt,
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

    // ===== 하위 호환성 =====

    /**
     * 기존 ExchangeRate → ExchangeRateEntity
     * 점진적 마이그레이션을 위한 변환
     */
    fun ExchangeRate.toEntity(): ExchangeRateEntity {
        val ratesMap = parseJsonToMap(this.rates)
        return ExchangeRateEntity(
            id = this.id,
            createAt = this.createAt,
            rates = ratesMap
        )
    }

    /**
     * ExchangeRateEntity → 기존 ExchangeRate
     * 점진적 마이그레이션을 위한 역변환
     */
    fun ExchangeRateEntity.toLegacyRate(): ExchangeRate {
        val ratesJson = mapToJson(this.rates)
        return ExchangeRate(
            id = this.id,
            createAt = this.createAt,
            rates = ratesJson
        )
    }

    // ===== 헬퍼 함수 =====

    /**
     * JSON String → Map
     *
     * @param json JSON 문자열 (예: {"USD":"1300.50","JPY":"944.00"})
     * @return Map<String, String>
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
                val key = keys.next()
                val value = jsonObject.optString(key, "0")
                map[key] = value
            }

            map
        } catch (e: Exception) {
            emptyMap()
        }
    }

    /**
     * Map → JSON String
     *
     * @param map Map<String, String>
     * @return JSON 문자열 (예: {"USD":"1300.50","JPY":"944.00"})
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
            "{}"
        }
    }

    /**
     * 서버 JSON → Entity 직접 변환 (WebSocket용)
     *
     * @param jsonString 서버 JSON (예: {"id":"123","createAt":"2025-10-20","exchangeRates":{"USD":"1300"}})
     * @return ExchangeRateEntity 또는 null
     */
    fun fromServerJson(jsonString: String): ExchangeRateEntity? {
        return try {
            if (jsonString.isBlank()) {
                return null
            }

            val jsonObject = JSONObject(jsonString)
            val id = jsonObject.optString("id", "")
            val createAt = jsonObject.optString("createAt", "N/A")
            val exchangeRates = jsonObject.optJSONObject("exchangeRates")

            if (exchangeRates == null) {
                return null
            }

            val ratesMap = mutableMapOf<String, String>()
            val keys = exchangeRates.keys()

            while (keys.hasNext()) {
                val key = keys.next()
                val value = exchangeRates.optString(key, "0")
                ratesMap[key] = value
            }

            ExchangeRateEntity(
                id = id,
                createAt = createAt,
                rates = ratesMap
            )
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Entity → 서버 JSON 형식
     *
     * @return 서버 JSON 문자열
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
            "{}"
        }
    }
}