package com.bobodroid.myapplication.models.datamodels.service.UserApi

import com.bobodroid.myapplication.BuildConfig
import com.bobodroid.myapplication.models.datamodels.service.noticeApi.NoticeResponse
import com.squareup.moshi.FromJson
import com.squareup.moshi.Moshi
import com.squareup.moshi.ToJson
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone


private val moshi = Moshi.Builder()
    .add(KotlinJsonAdapterFactory())
    .build()

private val loggingInterceptor = HttpLoggingInterceptor().apply {
    level = HttpLoggingInterceptor.Level.BODY // BODY로 설정하면 URL과 요청/응답 본문까지 모두 로그에 찍힙니다.
}

private val client = OkHttpClient.Builder()
    .addInterceptor(loggingInterceptor)
    .build()

private val UserRetrofit = Retrofit.Builder()
    .baseUrl(BuildConfig.BASE_URL)
    .addConverterFactory(MoshiConverterFactory.create(moshi))
    .client(client)
    .build()



object UserApi {
    val userService : UserApiService by lazy { UserRetrofit.create(UserApiService::class.java) }
}

interface UserApiService {
    @POST("user")
    suspend fun userAddRequest(@Body userRequest: UserRequest): UserResponse


    @POST("user/{deviceId}")
    suspend fun updateUserRates(
        @Path("deviceId") deviceId: String,
        @Body userRatesUpdateRequest: UserRatesUpdateRequest
    ): UserResponse


    @GET("user/{id}")
    suspend fun getUserRequest(
        @Path("id") id: String
    ): UserResponse

}
