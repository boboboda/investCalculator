package com.bobodroid.myapplication.models.datamodels.service.exchangeRateApi

import com.bobodroid.myapplication.BuildConfig
import com.bobodroid.myapplication.models.datamodels.service.noticeApi.NoticeResponse
import com.bobodroid.myapplication.models.datamodels.service.noticeApi.NoticeApiService
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

private val moshi = Moshi.Builder()
    .add(KotlinJsonAdapterFactory())
    .build()

private val RateStatsRetrofit = Retrofit.Builder()
    .baseUrl("${BuildConfig.BASE_URL}/exchange-rate/")
    .addConverterFactory(MoshiConverterFactory.create(moshi))
    .build()


object RateStatsApi {
    val rateStatsService : RateStatsApiService by lazy { RateStatsRetrofit.create(RateStatsApiService::class.java) }
}

interface RateStatsApiService {
    @GET("minute")
    suspend fun getMinuteRates(
        @Query("startDate") startDate: String
    ): List<ExchangeRateResponse>

    @GET("day")
    suspend fun getDailyStats(
        @Query("date") date: String
    ): DailyStatsResponse

    @GET("month")
    suspend fun getMonthlyStats(
        @Query("year") year: String,
        @Query("month") month: String
    ): MonthlyStatsResponse
}