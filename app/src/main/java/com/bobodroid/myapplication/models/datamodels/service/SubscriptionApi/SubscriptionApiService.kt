// app/src/main/java/com/bobodroid/myapplication/models/datamodels/service/subscriptionApi/SubscriptionApiService.kt
package com.bobodroid.myapplication.models.datamodels.service.subscriptionApi

import com.bobodroid.myapplication.BuildConfig
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.*
import java.util.concurrent.TimeUnit

// ✅ Moshi 설정
private val moshi = Moshi.Builder()
    .add(KotlinJsonAdapterFactory())
    .build()

// ✅ 로깅 인터셉터
private val loggingInterceptor = HttpLoggingInterceptor().apply {
    level = if (BuildConfig.DEBUG) {
        HttpLoggingInterceptor.Level.BODY
    } else {
        HttpLoggingInterceptor.Level.NONE
    }
}

// ✅ OkHttp 클라이언트
private val client = OkHttpClient.Builder()
    .connectTimeout(30, TimeUnit.SECONDS)
    .readTimeout(30, TimeUnit.SECONDS)
    .writeTimeout(30, TimeUnit.SECONDS)
    .addInterceptor(loggingInterceptor)
    .build()

// ✅ Retrofit 인스턴스
private val SubscriptionRetrofit = Retrofit.Builder()
    .baseUrl(BuildConfig.BASE_URL)
    .addConverterFactory(MoshiConverterFactory.create(moshi))
    .client(client)
    .build()

// ✅ Subscription API 싱글톤
object SubscriptionApi {
    val service: SubscriptionApiService by lazy {
        SubscriptionRetrofit.create(SubscriptionApiService::class.java)
    }
}

/**
 * 구독 API 인터페이스
 */
interface SubscriptionApiService {

    /**
     * 구매 영수증 검증
     * POST /subscription/verify
     */
    @POST("subscription/verify")
    suspend fun verifyPurchase(
        @Body request: VerifyPurchaseRequest
    ): SubscriptionResponse

    /**
     * ✅ 구독 복원 (소셜 로그인 기반)
     * POST /subscription/restore
     */
    @POST("subscription/restore")
    suspend fun restoreSubscription(
        @Body request: RestoreSubscriptionRequest
    ): RestoreSubscriptionResponse

    /**
     * 구독 상태 조회
     * GET /subscription/status/:deviceId
     */
    @GET("subscription/status/{deviceId}")
    suspend fun getSubscriptionStatus(
        @Path("deviceId") deviceId: String
    ): SubscriptionStatusResponse

    /**
     * 구독 재검증 (서버에서 Google Play 최신 상태 확인)
     * POST /subscription/reverify/:deviceId
     */
    @POST("subscription/reverify/{deviceId}")
    suspend fun reverifySubscription(
        @Path("deviceId") deviceId: String
    ): BaseSubscriptionResponse
}