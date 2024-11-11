package com.bobodroid.myapplication.models.datamodels.service.exchangeRateApi

import com.bobodroid.myapplication.models.datamodels.service.noticeApi.NoticeResponse
import com.bobodroid.myapplication.models.datamodels.service.noticeApi.NoticeApiService
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET

private const val BASE_URL = "buyoungsil.ddns.net:3000/exchange-rate"

private val moshi = Moshi.Builder()
    .add(KotlinJsonAdapterFactory())
    .build()

private val NoticeRetrofit = Retrofit.Builder()
    .baseUrl(BASE_URL)
    .addConverterFactory(MoshiConverterFactory.create(moshi))
    .build()


object NoticeApi {
    val noticeService : NoticeApiService by lazy { NoticeRetrofit.create(NoticeApiService::class.java) }
}

interface NoticeApiService {
    @GET("dollarRecord")
    suspend fun noticeRequest() : NoticeResponse

}