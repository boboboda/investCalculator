package com.bobodroid.myapplication.models.datamodels.service.NewsApi

import com.bobodroid.myapplication.BuildConfig
import com.bobodroid.myapplication.models.datamodels.response.LatestNewsResponse
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

private val moshi = Moshi.Builder()
    .add(KotlinJsonAdapterFactory())
    .build()

private val loggingInterceptor = HttpLoggingInterceptor().apply {
    level = HttpLoggingInterceptor.Level.BODY
}

private val client = OkHttpClient.Builder()
    .connectTimeout(30, TimeUnit.SECONDS)
    .readTimeout(30, TimeUnit.SECONDS)
    .writeTimeout(30, TimeUnit.SECONDS)
    .addInterceptor(loggingInterceptor)
    .build()

private val NewsRetrofit = Retrofit.Builder()
    .baseUrl(BuildConfig.BASE_URL)
    .addConverterFactory(MoshiConverterFactory.create(moshi))
    .client(client)
    .build()

object NewsApi {
    val service: NewsApiService by lazy {
        NewsRetrofit.create(NewsApiService::class.java)
    }
}

interface NewsApiService {

    /**
     * 최신 뉴스 조회
     * GET /news/latest?limit=10
     */
    @GET("news/latest")
    suspend fun getLatestNews(
        @Query("limit") limit: Int = 5
    ): LatestNewsResponse
}