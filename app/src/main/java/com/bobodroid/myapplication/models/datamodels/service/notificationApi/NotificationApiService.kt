// app/src/main/java/com/bobodroid/myapplication/models/datamodels/service/notificationApi/NotificationApiService.kt

package com.bobodroid.myapplication.models.datamodels.service.notificationApi

import com.bobodroid.myapplication.BuildConfig
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.*
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

private val NotificationRetrofit = Retrofit.Builder()
    .baseUrl(BuildConfig.BASE_URL)
    .addConverterFactory(MoshiConverterFactory.create(moshi))
    .client(client)
    .build()

object NotificationApi {
    val service: NotificationApiService by lazy {
        NotificationRetrofit.create(NotificationApiService::class.java)
    }
}

interface NotificationApiService {

    // ==================== 알림 설정 ====================

    /**
     * 알림 설정 조회
     */
    @GET("fcm/settings/{deviceId}")
    suspend fun getNotificationSettings(
        @Path("deviceId") deviceId: String
    ): NotificationSettingsResponse

    /**
     * 알림 설정 업데이트
     */
    @PUT("fcm/settings/{deviceId}")
    suspend fun updateNotificationSettings(
        @Path("deviceId") deviceId: String,
        @Body settings: UpdateNotificationSettingsRequest
    ): NotificationSettingsResponse

    /**
     * 수익률 알림 배치 업데이트
     */
    @PUT("fcm/settings/{deviceId}/record-alerts")
    suspend fun batchUpdateRecordAlerts(
        @Path("deviceId") deviceId: String,
        @Body request: BatchUpdateRecordAlertsRequest
    ): BatchUpdateRecordAlertsResponse

    // ==================== 알림 히스토리 ====================

    /**
     * 알림 히스토리 조회
     */
    @GET("fcm/history/{deviceId}")
    suspend fun getNotificationHistory(
        @Path("deviceId") deviceId: String,
        @Query("limit") limit: Int = 50
    ): NotificationHistoryResponse

    /**
     * 알림 읽음 처리
     */
    @PUT("fcm/history/{notificationId}/read")
    suspend fun markAsRead(
        @Path("notificationId") notificationId: String
    ): BaseResponse

    /**
     * 알림 클릭 처리 (신규 추가)
     */
    @PUT("fcm/history/{notificationId}/clicked")
    suspend fun markAsClicked(
        @Path("notificationId") notificationId: String
    ): BaseResponse


    // ==================== 알림 통계 ====================

    /**
     * 알림 통계 조회
     */
    @GET("fcm/stats/{deviceId}")
    suspend fun getNotificationStats(
        @Path("deviceId") deviceId: String
    ): NotificationStatsResponse

    // ==================== 테스트 ====================

    /**
     * 테스트 알림 전송
     */
    @POST("fcm/test/{deviceId}")
    suspend fun sendTestNotification(
        @Path("deviceId") deviceId: String
    ): BaseResponse


    /**
     * 개별 알림 삭제
     */
    @DELETE("fcm/history/{notificationId}")
    suspend fun deleteNotification(
        @Path("notificationId") notificationId: String
    ): BaseResponse

    /**
     * 모든 알림 삭제
     */
    @DELETE("fcm/history/all/{deviceId}")
    suspend fun deleteAllNotifications(
        @Path("deviceId") deviceId: String
    ): DeleteNotificationsResponse

    /**
     * 읽은 알림만 삭제
     */
    @DELETE("fcm/history/read/{deviceId}")
    suspend fun deleteReadNotifications(
        @Path("deviceId") deviceId: String
    ): DeleteNotificationsResponse

    /**
     * 오래된 알림 삭제
     */
    @DELETE("fcm/history/old/{deviceId}")
    suspend fun deleteOldNotifications(
        @Path("deviceId") deviceId: String,
        @Query("days") days: Int = 30
    ): DeleteNotificationsResponse
}