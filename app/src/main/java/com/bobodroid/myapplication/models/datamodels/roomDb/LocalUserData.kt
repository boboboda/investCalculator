package com.bobodroid.myapplication.models.datamodels.roomDb

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.firestore.DocumentSnapshot
import java.util.*


// LocalUserData.kt
@Entity(tableName = "LocalUserData_table")
data class LocalUserData(
    @PrimaryKey
    @ColumnInfo(name = "id")
    var id: UUID = UUID.randomUUID(),

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━
    // 기본 사용자 정보
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━
    @ColumnInfo(name = "social_id", defaultValue = "")
    var socialId: String? = null,

    @ColumnInfo(name = "social_type", defaultValue = "NONE")
    var socialType: String = "NONE",

    @ColumnInfo(name = "email", defaultValue = "")
    var email: String? = null,

    @ColumnInfo(name = "nickname", defaultValue = "")
    var nickname: String? = null,

    @ColumnInfo(name = "profile_url", defaultValue = "")
    var profileUrl: String? = null,

    @ColumnInfo(name = "is_synced", defaultValue = "0")
    var isSynced: Boolean = false,

    @ColumnInfo(name = "fcm_Token", defaultValue = "")
    var fcmToken: String? = null,

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━
    // 프리미엄 관리
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━

    // 기존 필드 (하위 호환성 유지)
    @ColumnInfo(name = "is_premium", defaultValue = "0")
    var isPremium: Boolean = false,

    // 🆕 프리미엄 타입 (NONE, SUBSCRIPTION, REWARD_AD, EVENT, LIFETIME)
    @ColumnInfo(name = "premium_type", defaultValue = "NONE")
    var premiumType: String = "NONE",

    // 🆕 프리미엄 만료 시간 (ISO 8601: "2025-01-15T14:30:00Z")
    @ColumnInfo(name = "premium_expiry_date", defaultValue = "")
    var premiumExpiryDate: String? = null,

    // 🆕 프리미엄 지급자 (reward, subscription, admin, event)
    @ColumnInfo(name = "premium_granted_by", defaultValue = "")
    var premiumGrantedBy: String? = null,

    // 🆕 프리미엄 지급 시간
    @ColumnInfo(name = "premium_granted_at", defaultValue = "")
    var premiumGrantedAt: String? = null,

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━
    // 광고 관리
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━

    // 배너 광고
    @ColumnInfo(name = "rate_Reset_Count")
    var rateResetCount: Int? = null,

    @ColumnInfo(name = "rate_Ad_Count")
    var rateAdCount: Int? = null,

    @ColumnInfo(name = "user_Reset_Date", defaultValue = "")
    var userResetDate: String? = null,

    // 리워드 광고
    @ColumnInfo(name = "reward_ad_Showing_date", defaultValue = "")
    var rewardAdShowingDate: String? = null,

    // 🆕 오늘 리워드 광고 사용 여부
    @ColumnInfo(name = "daily_reward_used", defaultValue = "0")
    var dailyRewardUsed: Boolean = false,

    // 🆕 마지막 리워드 사용 날짜
    @ColumnInfo(name = "last_reward_date", defaultValue = "")
    var lastRewardDate: String? = null,

    // 🆕 총 리워드 광고 시청 횟수
    @ColumnInfo(name = "total_reward_count", defaultValue = "0")
    var totalRewardCount: Int = 0,

    // 전면 광고
    // 🆕 전면 광고 카운트
    @ColumnInfo(name = "interstitial_ad_count", defaultValue = "0")
    var interstitialAdCount: Int = 0,

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━
    // 기타
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━
    @ColumnInfo(name = "reFresh_CreateAt", defaultValue = "")
    var reFreshCreateAt: String? = null,

    @ColumnInfo(name = "user_Show_Notice_Date", defaultValue = "")
    var userShowNoticeDate: String? = null,

    @ColumnInfo(name = "dr_Buy_Spread")
    var drBuySpread: Int? = null,

    @ColumnInfo(name = "dr_Sell_Spread")
    var drSellSpread: Int? = null,

    @ColumnInfo(name = "yen_Buy_Spread")
    var yenBuySpread: Int? = null,

    @ColumnInfo(name = "yen_Sell_Spread")
    var yenSellSpread: Int? = null,

    @ColumnInfo(name = "monthly_profit_goal", defaultValue = "0")
    var monthlyProfitGoal: Long = 0L,

    @ColumnInfo(name = "goal_set_month", defaultValue = "")
    var goalSetMonth: String? = null,

    @ColumnInfo(name = "last_sync_at", defaultValue = "")
    var lastSyncAt: String? = null
) {
    constructor(data: DocumentSnapshot) : this() {
        this.socialId = data["socialId"] as String? ?: ""
        this.socialType = data["socialType"] as String? ?: "NONE"
        this.email = data["email"] as String? ?: ""
        this.nickname = data["nickname"] as String? ?: ""
        this.profileUrl = data["profileUrl"] as String? ?: ""
        this.fcmToken = data["fcmToken"] as String? ?: ""
        this.rateResetCount = (data["rateResetCount"] as? Long)?.toInt()
        this.reFreshCreateAt = data["reFreshCreateAt"] as String? ?: ""
        this.rateAdCount = (data["rateAdCount"] as? Long)?.toInt()
        this.rewardAdShowingDate = data["rewardAdShowingDate"] as String? ?: ""
        this.userResetDate = data["userResetDate"] as String? ?: ""
        this.userShowNoticeDate = data["userShowNoticeDate"] as String? ?: ""
        this.drBuySpread = (data["drBuySpread"] as? Long)?.toInt()
        this.drSellSpread = (data["drSellSpread"] as? Long)?.toInt()
        this.yenBuySpread = (data["yenBuySpread"] as? Long)?.toInt()
        this.yenSellSpread = (data["yenSellSpread"] as? Long)?.toInt()
        this.monthlyProfitGoal = (data["monthlyProfitGoal"] as? Long) ?: 0L
        this.goalSetMonth = data["goalSetMonth"] as String? ?: ""
        this.lastSyncAt = data["lastSyncAt"] as String? ?: ""

        // 프리미엄 관련
        this.isPremium = data["isPremium"] as? Boolean ?: false
        this.premiumType = data["premiumType"] as? String ?: "NONE"
        this.premiumExpiryDate = data["premiumExpiryDate"] as String?
        this.premiumGrantedBy = data["premiumGrantedBy"] as String?
        this.premiumGrantedAt = data["premiumGrantedAt"] as String?

        // 광고 관련
        this.interstitialAdCount = ((data["interstitialAdCount"] as? Long) ?: 0L).toInt()
        this.dailyRewardUsed = data["dailyRewardUsed"] as? Boolean ?: false
        this.lastRewardDate = data["lastRewardDate"] as String?
        this.totalRewardCount = ((data["totalRewardCount"] as? Long) ?: 0L).toInt()
    }

    fun asHashMap(): HashMap<String, Any?> {
        return hashMapOf(
            "socialId" to this.socialId,
            "socialType" to this.socialType,
            "email" to this.email,
            "nickname" to this.nickname,
            "profileUrl" to this.profileUrl,
            "fcmToken" to this.fcmToken,
            "isSynced" to this.isSynced
        )
    }

    // ✅ SocialType enum 값을 가져오는 헬퍼 함수
    fun getSocialTypeEnum(): SocialType {
        return when (socialType) {
            "GOOGLE" -> SocialType.GOOGLE
            "KAKAO" -> SocialType.KAKAO
            else -> SocialType.NONE
        }
    }

    // ✅ SocialType enum을 설정하는 헬퍼 함수
    fun setSocialType(type: SocialType) {
        socialType = type.name
    }
}

// 🎯 프리미엄 타입 Enum
enum class PremiumType {
    NONE,           // 무료 사용자
    SUBSCRIPTION,   // 정기 구독 (Google Play)
    REWARD_AD,      // 리워드 광고 (24시간)
    EVENT,          // 이벤트 (관리자 지급)
    LIFETIME        // 평생 이용권
}


