package com.bobodroid.myapplication.models.datamodels.service.WebTicketApi

import com.bobodroid.myapplication.BuildConfig
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import java.util.concurrent.TimeUnit

data class WebTicketRequest(val deviceId: String)

data class WebTicketResponse(
    val success: Boolean = false,
    val ticket: String? = null,
    val expiresIn: Int? = null,
    val message: String? = null
)

private val moshi = Moshi.Builder()
    .add(KotlinJsonAdapterFactory())
    .build()

// 티켓은 입장용 민감값이라 로깅 인터셉터를 붙이지 않는다.
// 서버가 응답하지 않을 때 사용자를 오래 붙잡지 않도록 전체 10초로 제한한다.
private val client = OkHttpClient.Builder()
    .callTimeout(10, TimeUnit.SECONDS)
    .build()

private val retrofit = Retrofit.Builder()
    .baseUrl(BuildConfig.BASE_URL)
    .addConverterFactory(MoshiConverterFactory.create(moshi))
    .client(client)
    .build()

object WebTicketApi {
    val service: WebTicketApiService by lazy { retrofit.create(WebTicketApiService::class.java) }
}

interface WebTicketApiService {
    @POST("web-ticket")
    suspend fun issue(@Body request: WebTicketRequest): WebTicketResponse
}