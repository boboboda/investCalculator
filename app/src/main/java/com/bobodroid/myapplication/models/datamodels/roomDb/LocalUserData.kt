package com.bobodroid.myapplication.models.datamodels.roomDb

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.firestore.DocumentSnapshot
import java.util.*

@Entity(tableName = "LocalUserData_table")
data class LocalUserData(

    @PrimaryKey
    @ColumnInfo(name = "id")
    var id: UUID = UUID.randomUUID(),

    // ✅ 소셜 로그인 필드 추가 (customId, pin 대체)
    @ColumnInfo(name = "social_id", defaultValue = "")
    var socialId: String? = null,

    @ColumnInfo(name = "social_type", defaultValue = "NONE")
    var socialType: String = "NONE",  // "GOOGLE", "KAKAO", "NONE"

    @ColumnInfo(name = "email", defaultValue = "")
    var email: String? = null,

    @ColumnInfo(name = "nickname", defaultValue = "")
    var nickname: String? = null,

    @ColumnInfo(name = "profile_url", defaultValue = "")
    var profileUrl: String? = null,

    @ColumnInfo(name = "is_synced", defaultValue = "0")
    var isSynced: Boolean = false,

    // 기존 필드들 유지
    @ColumnInfo(name = "fcm_Token", defaultValue = "")
    var fcmToken: String? = null,

    @ColumnInfo(name = "rate_Reset_Count", defaultValue = "")
    var rateResetCount: Int? = null,

    @ColumnInfo(name = "reFresh_CreateAt", defaultValue = "")
    var reFreshCreateAt: String? = null,

    @ColumnInfo(name = "rate_Ad_Count", defaultValue = "")
    var rateAdCount: Int? = null,

    @ColumnInfo(name = "reward_ad_Showing_date", defaultValue = "")
    var rewardAdShowingDate: String? = null,

    @ColumnInfo(name = "user_Reset_Date", defaultValue = "")
    var userResetDate: String? = null,

    @ColumnInfo(name = "user_Show_Notice_Date", defaultValue = "")
    var userShowNoticeDate: String? = null,

    @ColumnInfo(name = "dr_Buy_Spread", defaultValue = "")
    var drBuySpread: Int? = null,

    @ColumnInfo(name = "dr_Sell_Spread", defaultValue = "")
    var drSellSpread: Int? = null,

    @ColumnInfo(name = "yen_Buy_Spread", defaultValue = "")
    var yenBuySpread: Int? = null,

    @ColumnInfo(name = "yen_Sell_Spread", defaultValue = "")
    var yenSellSpread: Int? = null,

    @ColumnInfo(name = "monthly_profit_goal", defaultValue = "0")
    var monthlyProfitGoal: Long = 0L,

    @ColumnInfo(name = "goal_set_month", defaultValue = "")
    var goalSetMonth: String? = null

) {
    constructor(data: DocumentSnapshot) : this() {
        this.socialId = data["socialId"] as String? ?: ""
        this.socialType = data["socialType"] as String? ?: "NONE"
        this.email = data["email"] as String? ?: ""
        this.nickname = data["nickname"] as String? ?: ""
        this.profileUrl = data["profileUrl"] as String? ?: ""
        this.fcmToken = data["fcmToken"] as String? ?: ""
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