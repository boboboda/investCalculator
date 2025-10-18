package com.bobodroid.myapplication.models.datamodels.service.UserApi

import com.bobodroid.myapplication.BuildConfig
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.*
import android.util.Log
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import java.io.IOException
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.Proxy
import java.util.concurrent.TimeUnit

private val moshi = Moshi.Builder()
    .add(KotlinJsonAdapterFactory())
    .build()

private val loggingInterceptor = HttpLoggingInterceptor().apply {
    level = HttpLoggingInterceptor.Level.BODY
}

private val eventListener = object : EventListener() {
    private var callStartTime = 0L

    override fun callStart(call: Call) {
        callStartTime = System.currentTimeMillis()
        Log.d("OkHttp", "⏱ [0ms] 시작")
    }

    override fun dnsStart(call: Call, domainName: String) {
        val elapsed = System.currentTimeMillis() - callStartTime
        Log.d("OkHttp", "⏱ [${elapsed}ms] DNS 조회 시작")
    }

    override fun dnsEnd(call: Call, domainName: String, inetAddressList: List<InetAddress>) {
        val elapsed = System.currentTimeMillis() - callStartTime
        Log.d("OkHttp", "⏱ [${elapsed}ms] DNS 조회 완료")
    }

    override fun connectStart(call: Call, inetSocketAddress: InetSocketAddress, proxy: Proxy) {
        val elapsed = System.currentTimeMillis() - callStartTime
        Log.d("OkHttp", "⏱ [${elapsed}ms] 연결 시작")
    }

    override fun secureConnectStart(call: Call) {
        val elapsed = System.currentTimeMillis() - callStartTime
        Log.d("OkHttp", "⏱ [${elapsed}ms] SSL 시작")
    }

    override fun secureConnectEnd(call: Call, handshake: Handshake?) {
        val elapsed = System.currentTimeMillis() - callStartTime
        Log.d("OkHttp", "⏱ [${elapsed}ms] SSL 완료")
    }

    override fun callEnd(call: Call) {
        val elapsed = System.currentTimeMillis() - callStartTime
        Log.d("OkHttp", "⏱ [${elapsed}ms] 전체 완료")
    }

    override fun callFailed(call: Call, ioe: IOException) {
        val elapsed = System.currentTimeMillis() - callStartTime
        Log.e("OkHttp", "⏱ [${elapsed}ms] 실패: ${ioe.message}")
    }
}

private val client = OkHttpClient.Builder()
    .connectTimeout(30, TimeUnit.SECONDS)
    .readTimeout(30, TimeUnit.SECONDS)
    .writeTimeout(30, TimeUnit.SECONDS)
    .connectionPool(ConnectionPool(5, 5, TimeUnit.MINUTES))
    .eventListener(eventListener)
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
    // ✅ 사용자 생성
    @POST("user")
    suspend fun userAddRequest(@Body userRequest: UserRequest): UserResponse

    // ✅ 목표환율만 업데이트
    @POST("user/{deviceId}")
    suspend fun updateUserRates(
        @Path("deviceId") deviceId: String,
        @Body userRatesUpdateRequest: UserRatesUpdateRequest
    ): UserResponse

    // ✅ 사용자 전체 정보 업데이트
    @POST("user/{deviceId}")
    suspend fun userUpdate(
        @Path("deviceId") deviceId: String,
        @Body userRequest: UserRequest
    ): UserResponse

    // ✅ deviceId로 사용자 찾기 (서버 동기화용)
    @GET("user/findUser/{deviceId}")
    suspend fun getUserByDeviceId(
        @Path("deviceId") deviceId: String
    ): UserResponse

    // ✅ 소셜 ID로 사용자 찾기
    @GET("user/find-by-social")
    suspend fun findBySocial(
        @Query("socialId") socialId: String,
        @Query("socialType") socialType: String
    ): UserResponse

    // ✅ 소셜 연동 해제
    @POST("user/{deviceId}/unlink-social")
    suspend fun unlinkSocial(
        @Path("deviceId") deviceId: String
    ): UserResponse
}