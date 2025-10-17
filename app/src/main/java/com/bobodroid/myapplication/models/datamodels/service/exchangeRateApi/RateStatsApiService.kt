package com.bobodroid.myapplication.models.datamodels.service.exchangeRateApi

import com.bobodroid.myapplication.BuildConfig
import com.bobodroid.myapplication.models.datamodels.service.noticeApi.NoticeResponse
import com.bobodroid.myapplication.models.datamodels.service.noticeApi.NoticeApiService
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

private val moshi = Moshi.Builder()
    .add(KotlinJsonAdapterFactory())
    .build()

private val RateRetrofit = Retrofit.Builder()
    .baseUrl(BuildConfig.BASE_URL)
    .addConverterFactory(MoshiConverterFactory.create(moshi))
    .build()


object RateApi {
    val rateService : RateApiService by lazy { RateRetrofit.create(RateApiService::class.java) }
}

interface RateApiService {
//    @GET("exchange-rate/daily")
//    suspend fun getDailyRange(
//        @Query("date") date: String
//    ): List<ExchangeRateResponse>
//
//    @GET("exchange-rate/weekly")
//    suspend fun getWeeklyRange(
//        @Query("startDate") startDate: String,
//        @Query("endDate") endDate: String
//    ): List<ExchangeRateResponse>
//
//    @GET("exchange-rate/monthly")
//    suspend fun getMonthlyRange(
//        @Query("startDate") startDate: String,
//        @Query("endDate") endDate: String
//    ): List<ExchangeRateResponse>
//
//    @GET("exchange-rate/yearly")
//    suspend fun getYearlyRange(
//        @Query("startDate") startDate: String,
//        @Query("endDate") endDate: String
//    ): List<ExchangeRateResponse>

    // 또는 통합 엔드포인트 사용
    @GET("/exchange-rate/range")
    suspend fun getRatesByPeriod(
        @Query("period") period: String,
        @Query("startDate") startDate: String,
        @Query("endDate") endDate: String? = null
    ): List<ExchangeRateResponse>

    @GET("/exchange-rate/daily-change")
    suspend fun getDailyChange(): ExchangeRateDailyChange


    @GET("/exchange-rate/latest")
    suspend fun getLatestRate(): Response<ExchangeRateResponse>
}