package com.bobodroid.myapplication.models.datamodels.service.UserApi

import com.bobodroid.myapplication.BuildConfig
import com.bobodroid.myapplication.models.datamodels.service.noticeApi.NoticeResponse
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path


private val moshi = Moshi.Builder()
    .add(KotlinJsonAdapterFactory())
    .build()

private val UserRetrofit = Retrofit.Builder()
    .baseUrl(BuildConfig.BASE_URL)
    .addConverterFactory(MoshiConverterFactory.create(moshi))
    .build()


object UserApi {
    val userService : UserApiService by lazy { UserRetrofit.create(UserApiService::class.java) }
}

interface UserApiService {
    @POST("user")
    suspend fun userAddRequest(@Body userRequest: UserRequest): UserResponse


    @POST("user/{customId}")
    suspend fun updateUserRates(
        @Path("customId") customId: String,
        @Body userRatesUpdateRequest: UserRatesUpdateRequest
    ): NoticeResponse


    @GET("user/{id}")
    suspend fun getUserRequest(
        @Path("id") id: String
    ): UserResponse

}