package com.bobodroid.myapplication.data.mapper

import com.bobodroid.myapplication.data.local.entity.LocalUserDto
import com.bobodroid.myapplication.domain.entity.UserEntity
import com.bobodroid.myapplication.domain.entity.SocialType
import com.bobodroid.myapplication.domain.entity.PremiumType

/**
 * User Mapper
 *
 * DTO ↔ Entity 변환
 * - LocalUserDto (Room) → UserEntity (Domain)
 * - UserEntity (Domain) → LocalUserDto (Room)
 * - 하위 호환: LocalUserData → UserEntity
 */
object UserMapper {

    /**
     * DTO → Entity
     * Room 데이터를 Domain 모델로 변환
     */
    fun LocalUserDto.toEntity(): UserEntity {
        return UserEntity(
            id = this.id,
            socialId = this.socialId,
            socialType = parseSocialType(this.socialType),
            email = this.email,
            nickname = this.nickname,
            profileUrl = this.profileUrl,
            isSynced = this.isSynced,
            fcmToken = this.fcmToken,
            isPremium = this.isPremium,
            premiumType = parsePremiumType(this.premiumType),
            premiumExpiryDate = this.premiumExpiryDate,
            premiumGrantedBy = this.premiumGrantedBy,
            premiumGrantedAt = this.premiumGrantedAt,
            rateResetCount = this.rateResetCount,
            rateAdCount = this.rateAdCount,
            userResetDate = this.userResetDate,
            rewardAdShowingDate = this.rewardAdShowingDate,
            dailyRewardUsed = this.dailyRewardUsed,
            lastRewardDate = this.lastRewardDate,
            totalRewardCount = this.totalRewardCount,
            interstitialAdCount = this.interstitialAdCount,
            userShowNoticeDate = this.userShowNoticeDate,
            drBuySpread = this.drBuySpread,
            drSellSpread = this.drSellSpread,
            yenBuySpread = this.yenBuySpread,
            yenSellSpread = this.yenSellSpread,
            monthlyProfitGoal = this.monthlyProfitGoal,
            goalSetMonth = this.goalSetMonth,
            lastSyncAt = this.lastSyncAt
        )
    }

    /**
     * Entity → DTO
     * Domain 모델을 Room 데이터로 변환
     */
    fun UserEntity.toDto(): LocalUserDto {
        return LocalUserDto(
            id = this.id,
            socialId = this.socialId,
            socialType = this.socialType?.name ?: SocialType.NONE.name,
            email = this.email,
            nickname = this.nickname,
            profileUrl = this.profileUrl,
            isSynced = this.isSynced ?: false,
            fcmToken = this.fcmToken,
            isPremium = this.isPremium ?: false,
            premiumType = this.premiumType.name,
            premiumExpiryDate = this.premiumExpiryDate,
            premiumGrantedBy = this.premiumGrantedBy,
            premiumGrantedAt = this.premiumGrantedAt,
            rateResetCount = this.rateResetCount,
            rateAdCount = this.rateAdCount,
            userResetDate = this.userResetDate,
            rewardAdShowingDate = this.rewardAdShowingDate,
            dailyRewardUsed = this.dailyRewardUsed ?: false,
            lastRewardDate = this.lastRewardDate,
            totalRewardCount = this.totalRewardCount,
            interstitialAdCount = this.interstitialAdCount,
            userShowNoticeDate = this.userShowNoticeDate,
            drBuySpread = this.drBuySpread,
            drSellSpread = this.drSellSpread,
            yenBuySpread = this.yenBuySpread,
            yenSellSpread = this.yenSellSpread,
            monthlyProfitGoal = this.monthlyProfitGoal ?: 0L,
            goalSetMonth = this.goalSetMonth,
            lastSyncAt = this.lastSyncAt
        )
    }

    // ===== 헬퍼 함수 =====

    /**
     * String → SocialType 변환
     */
    private fun parseSocialType(value: String): SocialType {
        return when (value.uppercase()) {
            "GOOGLE" -> SocialType.GOOGLE
            "KAKAO" -> SocialType.KAKAO
            else -> SocialType.NONE
        }
    }

    /**
     * String → PremiumType 변환
     */
    private fun parsePremiumType(value: String): PremiumType {
        return when (value.uppercase()) {
            "SUBSCRIPTION" -> PremiumType.SUBSCRIPTION
            "REWARD_AD" -> PremiumType.REWARD_AD
            "EVENT" -> PremiumType.EVENT
            "LIFETIME" -> PremiumType.LIFETIME
            else -> PremiumType.NONE
        }
    }
}