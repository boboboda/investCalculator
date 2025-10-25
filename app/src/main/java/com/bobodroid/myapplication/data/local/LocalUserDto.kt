package com.bobodroid.myapplication.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID
import javax.annotation.Nonnull

/**
 * Local User DTO (Data Layer)
 *
 * Room Database 전용 모델
 * - @Entity, @ColumnInfo 등 Room 어노테이션 포함
 * - Database 구조에 최적화
 * - UserEntity와 Mapper로 변환
 *
 * [변경 사항]
 * 기존: LocalUserData (Entity + Business Logic)
 * 신규: LocalUserDto (Database Only)
 */
@Entity(tableName = "LocalUserData_table")
data class LocalUserDto(
    @PrimaryKey
    @ColumnInfo(name = "id")
    @Nonnull
    val id: UUID = UUID.randomUUID(),

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━
    // 기본 사용자 정보
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━

    @ColumnInfo(name = "social_id", defaultValue = "")
    val socialId: String?,

    @ColumnInfo(name = "social_type", defaultValue = "NONE")
    val socialType: String,

    @ColumnInfo(name = "email", defaultValue = "")
    val email: String?,

    @ColumnInfo(name = "nickname", defaultValue = "")
    val nickname: String?,

    @ColumnInfo(name = "profile_url", defaultValue = "")
    val profileUrl: String?,

    @ColumnInfo(name = "is_synced", defaultValue = "0")
    val isSynced: Boolean,

    @ColumnInfo(name = "fcm_Token", defaultValue = "")
    val fcmToken: String?,

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━
    // 프리미엄 관리
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━

    @ColumnInfo(name = "is_premium", defaultValue = "0")
    val isPremium: Boolean,

    @ColumnInfo(name = "premium_type", defaultValue = "NONE")
    val premiumType: String,

    @ColumnInfo(name = "premium_expiry_date", defaultValue = "")
    val premiumExpiryDate: String?,

    @ColumnInfo(name = "premium_granted_by", defaultValue = "")
    val premiumGrantedBy: String?,

    @ColumnInfo(name = "premium_granted_at", defaultValue = "")
    val premiumGrantedAt: String?,

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━
    // 광고 관리
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━

    @ColumnInfo(name = "rate_Reset_Count")
    val rateResetCount: Int?,

    @ColumnInfo(name = "rate_Ad_Count")
    val rateAdCount: Int?,

    @ColumnInfo(name = "user_Reset_Date", defaultValue = "")
    val userResetDate: String?,

    @ColumnInfo(name = "reward_ad_Showing_date", defaultValue = "")
    val rewardAdShowingDate: String?,

    @ColumnInfo(name = "daily_reward_used", defaultValue = "0")
    val dailyRewardUsed: Boolean,

    @ColumnInfo(name = "last_reward_date", defaultValue = "")
    val lastRewardDate: String?,

    @ColumnInfo(name = "total_reward_count", defaultValue = "0")
    val totalRewardCount: Int,

    @ColumnInfo(name = "interstitial_ad_count", defaultValue = "0")
    val interstitialAdCount: Int,

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━
    // 알림 및 UI 설정
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━

    @ColumnInfo(name = "user_Show_Notice_Date", defaultValue = "")
    val userShowNoticeDate: String?,

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━
    // 스프레드 설정
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━

    @ColumnInfo(name = "dr_buy_spread")
    val drBuySpread: Int?,

    @ColumnInfo(name = "dr_sell_spread")
    val drSellSpread: Int?,

    @ColumnInfo(name = "yen_buy_spread")
    val yenBuySpread: Int?,

    @ColumnInfo(name = "yen_sell_spread")
    val yenSellSpread: Int?,

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━
    // 목표 설정
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━

    @ColumnInfo(name = "monthly_profit_goal", defaultValue = "0")
    val monthlyProfitGoal: Long,

    @ColumnInfo(name = "goal_set_month", defaultValue = "")
    val goalSetMonth: String?,

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━
    // 동기화
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━

    @ColumnInfo(name = "last_sync_at", defaultValue = "")
    val lastSyncAt: String?
)