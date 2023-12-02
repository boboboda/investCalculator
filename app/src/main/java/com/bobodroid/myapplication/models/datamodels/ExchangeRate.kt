package com.bobodroid.myapplication.models.datamodels

import com.google.firebase.firestore.QuerySnapshot

data class ExchangeRate(
    var id: String? = null,
    var createAt: String? = null,
    var exchangeRates: Rate? = null
)
{
    constructor(data: QuerySnapshot): this() {
        for (document in data.documents) {
            // document는 QueryDocumentSnapshot 객체입니다.
            this.id = document.id
            this.createAt = document["createAt"] as String? ?: ""
            this.exchangeRates = Rate(document["exchangeRates"]!!) as Rate? ?: Rate()
        }
    }



    fun asHasMap(): HashMap<String, Any?> {
    return hashMapOf(
        "id" to this.id,
        "createAt" to this.createAt,
        "exchangeRates" to this.exchangeRates
    )
}
}

data class Rate(
    var usd: String? = null,
    var jpy: String? = null
) {
    constructor(data: Any): this() {
            // document는 QueryDocumentSnapshot 객체입니다.
           this.jpy = (data as? Map<String, String>)?.get("JPY")
        this.usd = (data as? Map<String, String>)?.get("USD")
    }

    fun asHasMap(): HashMap<String, Any?> {
        return hashMapOf(
            "USD" to this.usd,
            "JPY" to this.jpy,
        )
    }
}

//private operator fun Any.get(s: String): Any {
//    return s
//}

