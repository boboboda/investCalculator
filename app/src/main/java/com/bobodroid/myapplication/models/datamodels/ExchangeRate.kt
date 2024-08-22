package com.bobodroid.myapplication.models.datamodels

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.google.firebase.firestore.QuerySnapshot
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