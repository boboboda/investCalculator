package com.bobodroid.myapplication.domain.entity

import java.util.UUID

/**
 * User Entity (Domain Layer)
 *
 * 플랫폼 독립적인 사용자 모델
 * - Room, Firebase 등 구체적 기술과 무관
 * - 비즈니스 로직에만 집중
 * - iOS 이식 가능
 *
 * [변경 사항]
 * 기존: LocalUserData (Room @Entity)
 * 신규: UserEntity (Pure Kotlin)
 */
data class UserEntity(
    /**
     * 고유 ID
     */
    val id: UUID = UUID.randomUUID(),

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━
    // 기본 사용자 정보
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━

    /**
     * 소셜 로그인 ID
     */
    val socialId: String? = null,

    /**
     * 소셜 로그인 타입 (GOOGLE, KAKAO, NONE)
     */
    val socialType: SocialType? = SocialType.NONE,

    /**
     * 이메일
     */
    val email: String? = null,

    /**
     * 닉네임
     */
    val nickname: String? = null,

    /**
     * 프로필 이미지 URL
     */
    val profileUrl: String? = null,

    /**
     * 동기화 여부
     */
    val isSynced: Boolean? = false,

    /**
     * FCM 토큰
     */
    val fcmToken: String?  = null,

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━
    // 프리미엄 관리
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━

    /**
     * 프리미엄 여부
     */
    val isPremium: Boolean? = false,

    /**
     * 프리미엄 타입
     */
    val premiumType: PremiumType = PremiumType.NONE,

    /**
     * 프리미엄 만료 시간 (ISO 8601)
     */
    val premiumExpiryDate: String? = null,

    /**
     * 프리미엄 지급자 (reward, subscription, admin, event)
     */
    val premiumGrantedBy: String? = null,

    /**
     * 프리미엄 지급 시간
     */
    val premiumGrantedAt: String? = null,

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━
    // 광고 관리
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━

    /**
     * 배너 광고 - 리셋 횟수
     */
    val rateResetCount: Int? = null,

    /**
     * 배너 광고 - 광고 횟수
     */
    val rateAdCount: Int?  = null,

    /**
     * 배너 광고 - 리셋 날짜
     */
    val userResetDate: String? = null,

    /**
     * 리워드 광고 - 표시 날짜
     */
    val rewardAdShowingDate: String? = null,

    /**
     * 오늘 리워드 광고 사용 여부
     */
    val dailyRewardUsed: Boolean? = false,

    /**
     * 마지막 리워드 사용 날짜
     */
    val lastRewardDate: String? = null,

    /**
     * 총 리워드 광고 시청 횟수
     */
    val totalRewardCount: Int = 0,

    /**
     * 전면 광고 시청 횟수
     */
    val interstitialAdCount: Int = 0,

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━
    // 알림 및 UI 설정
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━

    /**
     * 공지사항 표시 날짜
     */
    val userShowNoticeDate: String? = null,

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━
    // 스프레드 설정
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━

    /**
     * 달러 매수 스프레드
     */
    val drBuySpread: Int? = null,

    /**
     * 달러 매도 스프레드
     */
    val drSellSpread: Int? = null,

    /**
     * 엔화 매수 스프레드
     */
    val yenBuySpread: Int? = null,

    /**
     * 엔화 매도 스프레드
     */
    val yenSellSpread: Int? = null,

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━
    // 목표 설정
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━

    /**
     * 월간 수익 목표
     */
    val monthlyProfitGoal: Long? = null,

    /**
     * 목표 설정 월 (yyyy-MM)
     */
    val goalSetMonth: String? = null,

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━
    // 동기화
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━

    /**
     * 마지막 동기화 시간
     */
    val lastSyncAt: String? = null
) {

    /**
     * 프리미엄 여부 확인
     */
    fun isActivePremium(): Boolean? = isPremium

    /**
     * 소셜 로그인 연동 여부
     */
    fun hasSocialLogin(): Boolean = socialType != SocialType.NONE && !socialId.isNullOrEmpty()

    /**
     * 프리미엄 업데이트
     */
    fun updatePremium(
        isPremium: Boolean,
        type: PremiumType,
        expiryDate: String?,
        grantedBy: String?,
        grantedAt: String?
    ): UserEntity {
        return this.copy(
            isPremium = isPremium,
            premiumType = type,
            premiumExpiryDate = expiryDate,
            premiumGrantedBy = grantedBy,
            premiumGrantedAt = grantedAt
        )
    }

    /**
     * 소셜 로그인 정보 업데이트
     */
    fun updateSocialLogin(
        socialId: String,
        socialType: SocialType,
        email: String?,
        nickname: String?,
        profileUrl: String?
    ): UserEntity {
        return this.copy(
            socialId = socialId,
            socialType = socialType,
            email = email,
            nickname = nickname,
            profileUrl = profileUrl
        )
    }

    /**
     * FCM 토큰 업데이트
     */
    fun updateFcmToken(token: String): UserEntity {
        return this.copy(fcmToken = token)
    }

    /**
     * 동기화 완료 처리
     */
    fun markAsSynced(timestamp: String): UserEntity {
        return this.copy(
            isSynced = true,
            lastSyncAt = timestamp
        )
    }

    /**
     * 리워드 광고 사용 처리
     */
    fun useRewardAd(date: String): UserEntity {
        return this.copy(
            dailyRewardUsed = true,
            lastRewardDate = date,
            totalRewardCount = totalRewardCount + 1
        )
    }

    companion object {
        /**
         * 빈 UserEntity 생성 (기본값)
         */
        fun empty(): UserEntity {
            return UserEntity(
                socialId = null,
                socialType = SocialType.NONE,
                email = null,
                nickname = null,
                profileUrl = null,
                isSynced = false,
                fcmToken = null,
                isPremium = false,
                premiumType = PremiumType.NONE,
                premiumExpiryDate = null,
                premiumGrantedBy = null,
                premiumGrantedAt = null,
                rateResetCount = null,
                rateAdCount = null,
                userResetDate = null,
                rewardAdShowingDate = null,
                dailyRewardUsed = false,
                lastRewardDate = null,
                totalRewardCount = 0,
                interstitialAdCount = 0,
                userShowNoticeDate = null,
                drBuySpread = null,
                drSellSpread = null,
                yenBuySpread = null,
                yenSellSpread = null,
                monthlyProfitGoal = 0L,
                goalSetMonth = null,
                lastSyncAt = null
            )
        }
    }
}

/**
 * 소셜 로그인 타입
 */
enum class SocialType {
    GOOGLE,
    KAKAO,
    NONE
}

/**
 * 프리미엄 타입
 */
enum class PremiumType {
    NONE,           // 무료 사용자
    SUBSCRIPTION,   // 정기 구독 (Google Play)
    REWARD_AD,      // 리워드 광고 (24시간)
    EVENT,          // 이벤트 (관리자 지급)
    LIFETIME        // 평생 이용권
}