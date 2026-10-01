package com.bobodroid.myapplication.models.datamodels.service.backtestApi

import com.bobodroid.myapplication.BuildConfig
import com.bobodroid.myapplication.models.datamodels.backtest.BacktestAvailability
import com.bobodroid.myapplication.models.datamodels.backtest.BacktestRequest
import com.bobodroid.myapplication.models.datamodels.backtest.BacktestResponse
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

private val moshi = Moshi.Builder()
    .add(KotlinJsonAdapterFactory())
    .build()

private val loggingInterceptor = HttpLoggingInterceptor().apply {
    level = if (BuildConfig.DEBUG) {
        HttpLoggingInterceptor.Level.BODY
    } else {
        HttpLoggingInterceptor.Level.NONE
    }
}

// 백테스트는 3~12개월치 환율 데이터를 서버에서 순회 계산하므로 타임아웃을 넉넉히 설정
private val backtestClient = OkHttpClient.Builder()
    .connectTimeout(30, TimeUnit.SECONDS)
    .readTimeout(60, TimeUnit.SECONDS)
    .writeTimeout(60, TimeUnit.SECONDS)
    .addInterceptor(loggingInterceptor)
    .build()

private val BacktestRetrofit = Retrofit.Builder()
    .baseUrl(BuildConfig.BASE_URL)
    .addConverterFactory(MoshiConverterFactory.create(moshi))
    .client(backtestClient)
    .build()

object BacktestApi {
    val service: BacktestApiService by lazy {
        BacktestRetrofit.create(BacktestApiService::class.java)
    }
}

interface BacktestApiService {
    @POST("/exchange-rate/backtest")
    suspend fun runBacktest(@Body request: BacktestRequest): BacktestResponse

    // 통화별 백테스트 가능 기간/데이터 시작일 조회 + (periodMonths 전달 시) 해당 기간 고점/저점
    // 서버 컨트롤러가 periodMonths를 문자열 쿼리 파라미터로 받아 내부에서 parseInt 하므로
    // 여기서도 String?으로 넘긴다 (Int로 넘기면 null 처리가 안 됨)
    @GET("/exchange-rate/backtest/availability")
    suspend fun getBacktestAvailability(
        @Query("currencyType") currencyType: String,
        @Query("periodMonths") periodMonths: String? = null
    ): BacktestAvailability
}
