package com.bobodroid.myapplication.models.datamodels.repository

import android.util.Log
import com.bobodroid.myapplication.MainActivity.Companion.TAG
import com.bobodroid.myapplication.models.datamodels.response.NewsItem
import com.bobodroid.myapplication.models.datamodels.service.NewsApi.NewsApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NewsRepository @Inject constructor() {

    private val newsApiService = NewsApi.service

    /**
     * 최신 뉴스 조회
     */
    fun getLatestNews(limit: Int = 5): Flow<Result<List<NewsItem>>> = flow {
        try {
            Log.d(TAG("NewsRepository", "getLatestNews"), "최신 뉴스 $limit 개 요청")

            val response = newsApiService.getLatestNews(limit)

            if (response.success) {
                Log.d(TAG("NewsRepository", "getLatestNews"), "✅ ${response.data.size}개 뉴스 조회 성공")
                emit(Result.success(response.data))
            } else {
                Log.e(TAG("NewsRepository", "getLatestNews"), "❌ API 실패: ${response.message}")
                emit(Result.failure(Exception(response.message)))
            }
        } catch (e: Exception) {
            Log.e(TAG("NewsRepository", "getLatestNews"), "❌ 네트워크 오류", e)
            emit(Result.failure(e))
        }
    }.flowOn(Dispatchers.IO)
}