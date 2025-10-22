// app/src/main/java/com/bobodroid/myapplication/models/datamodels/service/BackupApi/BackupApiService.kt
package com.bobodroid.myapplication.models.datamodels.service.BackupApi

import com.bobodroid.myapplication.BuildConfig
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.ConnectionPool
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.*
import java.util.concurrent.TimeUnit

// ==================== 🆕 데이터 클래스 추가 ====================



@JsonClass(generateAdapter = true)
data class CreateBackupDto(
    val deviceId: String,
    val socialId: String? = null,
    val socialType: String? = null,
    val currencyRecords: List<CurrencyRecordDto>
)

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
    .connectionPool(ConnectionPool(5, 5, TimeUnit.MINUTES))
    .addInterceptor(loggingInterceptor)
    .build()

// ✅ Retrofit 인스턴스
private val backupRetrofit = Retrofit.Builder()
    .baseUrl(BuildConfig.BASE_URL)
    .addConverterFactory(MoshiConverterFactory.create(moshi))
    .client(client)
    .build()

// ✅ Backup API 싱글톤
object BackupApi {
    val backupService: BackupApiService by lazy {
        backupRetrofit.create(BackupApiService::class.java)
    }
}

/**
 * 백업 API 인터페이스
 */
interface BackupApiService {

    /**
     * 백업 생성/업데이트
     * POST /backup
     */
    @POST("backup")
    suspend fun createBackup(
        @Body backupRequest: BackupRequest
    ): BackupResponse

    /**
     * 🆕 백업 생성/업데이트 (DTO 버전 - 수익률 알림용)
     * POST /backup
     */
    @POST("backup")
    suspend fun createBackupWithDto(
        @Body backupDto: CreateBackupDto
    ): BackupResponse

    /**
     * deviceId로 백업 복구
     * GET /backup/restore?deviceId=xxx
     */
    @GET("backup/restore")
    suspend fun restoreByDeviceId(
        @Query("deviceId") deviceId: String
    ): RestoreResponse

    /**
     * socialId로 백업 복구 (기기 변경 시)
     * GET /backup/restore-social?socialId=xxx&socialType=GOOGLE
     */
    @GET("backup/restore-social")
    suspend fun restoreBySocialId(
        @Query("socialId") socialId: String,
        @Query("socialType") socialType: String
    ): RestoreResponse

    /**
     * 백업 통계 조회
     * GET /backup/stats?deviceId=xxx
     */
    @GET("backup/stats")
    suspend fun getBackupStats(
        @Query("deviceId") deviceId: String
    ): BackupStatsResponse

    /**
     * 백업 삭제
     * DELETE /backup/:deviceId
     */
    @DELETE("backup/{deviceId}")
    suspend fun deleteBackup(
        @Path("deviceId") deviceId: String
    ): BackupResponse
}