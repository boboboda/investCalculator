// app/src/main/java/com/bobodroid/myapplication/models/datamodels/roomDb/RateType.kt

package com.bobodroid.myapplication.models.datamodels.roomDb

// 목표환율 방향 (고점, 저점)
enum class RateDirection {
    HIGH,
    LOW
}

// ✅ sealed class → data class로 변경 (12개 통화 동적 지원)
data class RateType(
    val currency: CurrencyType,
    val direction: RateDirection
) {
    companion object {
        // ✅ 편의 생성자
        fun from(currency: CurrencyType, direction: RateDirection): RateType {
            return RateType(currency, direction)
        }

        // ✅ 문자열로부터 생성
        fun fromStrings(currencyCode: String, directionStr: String): RateType? {
            val currency = CurrencyType.values().find { it.code == currencyCode } ?: return null
            val direction = when (directionStr.uppercase()) {
                "HIGH" -> RateDirection.HIGH
                "LOW" -> RateDirection.LOW
                else -> return null
            }
            return RateType(currency, direction)
        }

        // ✅ 하위 호환성을 위한 레거시 객체들
        @Deprecated("Use RateType(CurrencyType.USD, RateDirection.HIGH) instead")
        val USD_HIGH = RateType(CurrencyType.USD, RateDirection.HIGH)

        @Deprecated("Use RateType(CurrencyType.USD, RateDirection.LOW) instead")
        val USD_LOW = RateType(CurrencyType.USD, RateDirection.LOW)

        @Deprecated("Use RateType(CurrencyType.JPY, RateDirection.HIGH) instead")
        val JPY_HIGH = RateType(CurrencyType.JPY, RateDirection.HIGH)

        @Deprecated("Use RateType(CurrencyType.JPY, RateDirection.LOW) instead")
        val JPY_LOW = RateType(CurrencyType.JPY, RateDirection.LOW)
    }

    // ✅ 필드명 생성 (서버 호환)
    fun toFieldName(): String {
        return "${currency.code.lowercase()}${direction.name.lowercase().capitalize()}Rates"
    }

    // ✅ 표시용 문자열
    fun toDisplayString(): String {
        val directionText = when (direction) {
            RateDirection.HIGH -> "고점"
            RateDirection.LOW -> "저점"
        }
        return "${currency.koreanName} $directionText"
    }
}