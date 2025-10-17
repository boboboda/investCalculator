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
import java.util.concurrent.TimeUnit
import okhttp3.*
import android.util.Log
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.Proxy
import java.io.IOException

private val moshi = Moshi.Builder()
    .add(KotlinJsonAdapterFactory())
    .build()

private val loggingInterceptor = HttpLoggingInterceptor().apply {
    level = HttpLoggingInterceptor.Level.BODY // BODY로 설정하면 URL과 요청/응답 본문까지 모두 로그에 찍힙니다.
}



// UserApiService.kt 파일 열기



// 기존 client 코드 위에 이거 추가
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

// 기존 client에 eventListener 추가
private val client = OkHttpClient.Builder()
    .connectTimeout(30, TimeUnit.SECONDS)
    .readTimeout(30, TimeUnit.SECONDS)
    .writeTimeout(30, TimeUnit.SECONDS)
    .connectionPool(ConnectionPool(5, 5, TimeUnit.MINUTES))
    .eventListener(eventListener)  // ✅ 이 한 줄만 추가!
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

    @POST("user/{deviceId}")
    suspend fun userUpdate(
        @Path("deviceId") deviceId: String,
        @Body userRequest: UserRequest
    ): UserResponse


    @GET("user/findUser/{customId}")
    suspend fun getUserRequest(
        @Path("customId") customId: String
    ): UserResponse

    @POST("user/login")
    suspend fun login(
        @Body authUser: AuthUser
    ): UserResponse


}
