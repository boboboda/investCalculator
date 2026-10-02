// app/src/main/java/com/bobodroid/myapplication/models/datamodels/service/subscriptionApi/SubscriptionModels.kt
package com.bobodroid.myapplication.models.datamodels.service.subscriptionApi

import com.squareup.moshi.JsonClass

// ==================== 요청 모델 ====================

/**
 * 영수증 검증 요청
 */
@JsonClass(generateAdapter = true)
data class VerifyPurchaseRequest(
    val deviceId: String,
    val productId: String,
    val basePlanId: String,
    val purchaseToken: String,
    val packageName: String = "com.bobodroid.myapplication",
    val socialId: String? = null,
    val socialType: String? = null
)

/**
 * ✅ 구독 복원 요청 (소셜 로그인 기반)
 */
@JsonClass(generateAdapter = true)
data class RestoreSubscriptionRequest(
    val socialId: String,
    val socialType: String
)

// ==================== 응답 모델 ====================

/**
 * 기본 응답
 */
@JsonClass(generateAdapter = true)
data class BaseSubscriptionResponse(
    val success: Boolean,
    val message: String
)

/**
 * 구독 검증 응답
 */
@JsonClass(generateAdapter = true)
data class SubscriptionResponse(
    val success: Boolean,
    val message: String,
    val data: SubscriptionData?
)

@JsonClass(generateAdapter = true)
data class SubscriptionData(
    val deviceId: String,
    val productId: String,
    val basePlanId: String,
    val expiryTime: String,
    val autoRenewing: Boolean,
    val status: String,
    val socialId: String? = null,
    val socialType: String? = null
)

/**
 * 구독 상태 조회 응답
 */
@JsonClass(generateAdapter = true)
data class SubscriptionStatusResponse(
    val success: Boolean,
    val data: SubscriptionStatusData
)

@JsonClass(generateAdapter = true)
data class SubscriptionStatusData(
    val deviceId: String,
    val isPremium: Boolean,
    val premiumType: String,
    val basePlanId: String? = null,
    val expiryTime: String? = null,
    val autoRenewing: Boolean? = null,
    val status: String? = null,
    val daysRemaining: Int? = null,
    val socialId: String? = null,
    val socialType: String? = null
)

/**
 * ✅ 구독 복원 응답 (소셜 로그인 기반)
 */
@JsonClass(generateAdapter = true)
data class RestoreSubscriptionResponse(
    val success: Boolean,
    val message: String,
    val data: RestoreSubscriptionData?
)

@JsonClass(generateAdapter = true)
data class RestoreSubscriptionData(
    val deviceId: String,
    val isPremium: Boolean,
    val premiumType: String,
    val basePlanId: String? = null,
    val expiryTime: String? = null,
    val autoRenewing: Boolean? = null,
    val status: String? = null,
    val daysRemaining: Int? = null,
    val socialId: String? = null,
    val socialType: String? = null
)

// ==================== ✅ 신규: 통합 프리미엄 상태 ====================

/**
 * 통합 프리미엄 상태 조회 응답 (GET /premium/status/:deviceId)
 * 구독 + 리워드 광고를 서버가 합쳐서 판정한 결과
 */
@JsonClass(generateAdapter = true)
data class PremiumStatusResponse(
    val success: Boolean,
    val data: PremiumStatusData
)

@JsonClass(generateAdapter = true)
data class PremiumStatusData(
    val deviceId: String,
    val isPremium: Boolean,
    val premiumType: String, // "SUBSCRIPTION" | "REWARD_AD" | "NONE"
    val expiryTime: String? = null,
    val daysRemaining: Int? = null,
    val rewardAd: RewardAdInfo? = null
)

@JsonClass(generateAdapter = true)
data class RewardAdInfo(
    val todayRewardCount: Int,
    val dailyRewardCap: Int
)

// ==================== 디버그(테스트 도구) 전용 ====================

/**
 * 테스트 프리미엄 지급 요청 (N분 후 만료)
 */
@JsonClass(generateAdapter = true)
data class DebugGrantRequest(
    val minutes: Int
)

/**
 * 테스트 도구 API 공통 응답
 */
@JsonClass(generateAdapter = true)
data class DebugActionResponse(
    val success: Boolean,
    val message: String? = null
)
