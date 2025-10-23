package com.bobodroid.myapplication.premium

import android.content.Context
import android.util.Log
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.QueryPurchasesParams
import com.bobodroid.myapplication.MainActivity.Companion.TAG
import com.bobodroid.myapplication.billing.BillingClientLifecycle
import com.bobodroid.myapplication.models.datamodels.repository.UserRepository
import com.bobodroid.myapplication.models.datamodels.roomDb.LocalUserData
import com.bobodroid.myapplication.models.datamodels.roomDb.PremiumType
import com.bobodroid.myapplication.models.datamodels.useCases.UserUseCases
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.temporal.ChronoUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 프리미엄 상태 관리자
 * - 앱 시작 시 인스턴스화 (메모리 상주)
 * - BillingClient로 구독 상태 감시
 * - 변경 시 UserRepository(DB)에 자동 업데이트
 * - UI는 User.isPremium만 보면 됨
 */
@Singleton
class PremiumManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val userRepository: UserRepository,
    private val userUseCases: UserUseCases
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    // BillingClient 인스턴스
    private val billingClient: BillingClientLifecycle by lazy {
        BillingClientLifecycle.getInstance(context)
    }

    companion object {
        private const val PRODUCT_ID = "recordadvertisementremove" // 구독 상품 ID
    }

    init {
        Log.d(TAG("PremiumManager", "init"), "━━━━━━━━━━━━━━━━━━━━━━━━━━")
        Log.d(TAG("PremiumManager", "init"), "프리미엄 관리자 시작 (메모리 상주)")

        // ✅ 앱 시작하자마자 구독 상태 확인 및 감시 시작
        scope.launch {
            syncPremiumStatus()
        }
    }
    /**
     * 프리미엄 상태 확인
     * LIFETIME > SUBSCRIPTION > EVENT > REWARD_AD > NONE
     */
    fun checkPremiumStatus(user: LocalUserData): PremiumType {
        // LIFETIME은 만료 없음
        if (user.premiumType == "LIFETIME") {
            return PremiumType.LIFETIME
        }

        // 만료 날짜 확인
        val expiryDate = user.premiumExpiryDate
        if (expiryDate.isNullOrEmpty()) {
            return PremiumType.NONE
        }

        val now = Instant.now()
        val expiry = try {
            Instant.parse(expiryDate)
        } catch (e: Exception) {
            Log.e(TAG("PremiumManager", "checkPremiumStatus"), "만료 날짜 파싱 실패: $expiryDate")
            return PremiumType.NONE
        }

        // 만료되었는지 확인
        if (now.isAfter(expiry)) {
            Log.d(TAG("PremiumManager", "checkPremiumStatus"), "프리미엄 만료됨: ${user.premiumType}")
            return PremiumType.NONE
        }

        // 타입에 따라 반환
        return when (user.premiumType) {
            "SUBSCRIPTION" -> PremiumType.SUBSCRIPTION
            "EVENT" -> PremiumType.EVENT
            "REWARD_AD" -> PremiumType.REWARD_AD
            else -> PremiumType.NONE
        }
    }

    /**
     * 리워드 광고로 24시간 프리미엄 지급
     */
    suspend fun grantRewardPremium(user: LocalUserData): Boolean {
        // 오늘 이미 사용했는지 확인
        val today = Instant.now().truncatedTo(ChronoUnit.DAYS).toString()
        if (user.lastRewardDate == today) {
            Log.d(TAG("PremiumManager", "grantRewardPremium"), "오늘 이미 리워드 사용함")
            return false
        }

        // 24시간 후 만료 시간 계산
        val expiryDate = Instant.now().plus(24, ChronoUnit.HOURS).toString()

        val updatedUser = user.copy(
            premiumType = "REWARD_AD",
            premiumExpiryDate = expiryDate,
            premiumGrantedBy = "reward",
            premiumGrantedAt = Instant.now().toString(),
            dailyRewardUsed = true,
            lastRewardDate = today,
            totalRewardCount = user.totalRewardCount + 1,
            isPremium = true
        )

        userUseCases.localUserUpdate(updatedUser)
        Log.d(TAG("PremiumManager", "grantRewardPremium"), "24시간 프리미엄 지급 완료: $expiryDate")

        return true
    }

    /**
     * 정기 구독 활성화 (Google Play 결제 성공 시)
     */
    suspend fun activateSubscription(user: LocalUserData, expiryDate: String): Boolean {
        val updatedUser = user.copy(
            premiumType = "SUBSCRIPTION",
            premiumExpiryDate = expiryDate,
            premiumGrantedBy = "subscription",
            premiumGrantedAt = Instant.now().toString(),
            isPremium = true
        )

        userUseCases.localUserUpdate(updatedUser)
        Log.d(TAG("PremiumManager", "activateSubscription"), "정기 구독 활성화: $expiryDate")

        return true
    }

    /**
     * 프리미엄 만료 처리
     */
    suspend fun expirePremium(user: LocalUserData): Boolean {
        val updatedUser = user.copy(
            premiumType = "NONE",
            premiumExpiryDate = null,
            isPremium = false
        )

        userUseCases.localUserUpdate(updatedUser)
        Log.d(TAG("PremiumManager", "expirePremium"), "프리미엄 만료 처리 완료")

        return true
    }

    /**
     * 프리미엄 남은 시간 계산 (초 단위)
     */
    fun getRemainingSeconds(user: LocalUserData): Long {
        val expiryDate = user.premiumExpiryDate ?: return 0L

        val now = Instant.now()
        val expiry = try {
            Instant.parse(expiryDate)
        } catch (e: Exception) {
            return 0L
        }

        val remaining = expiry.epochSecond - now.epochSecond
        return if (remaining > 0) remaining else 0L
    }

    /**
     * 오늘 리워드 광고 사용 가능한지 확인
     */
    fun canUseRewardAdToday(user: LocalUserData): Boolean {
        val today = Instant.now().truncatedTo(ChronoUnit.DAYS).toString()
        return user.lastRewardDate != today
    }

    /**
     * 프리미엄 만료 1시간 전인지 확인 (푸시 알림용)
     */
    fun isExpiringWithinHour(user: LocalUserData): Boolean {
        val remaining = getRemainingSeconds(user)
        return remaining in 1..3600  // 1초~1시간 사이
    }

    /**
     * 구독 상태 동기화
     * - BillingClient로 현재 구독 확인
     * - DB에 업데이트
     */
    suspend fun syncPremiumStatus() {
        try {
            Log.d(TAG("PremiumManager", "syncPremiumStatus"), "구독 상태 확인 시작...")

            if (!billingClient.isClientReady()) {
                Log.w(TAG("PremiumManager", "syncPremiumStatus"), "BillingClient 준비 안됨")
                return
            }

            // BillingClient로 구독 확인
            billingClient.queryActivePurchases(PRODUCT_ID) { hasPremium ->
                Log.d(TAG("PremiumManager", "syncPremiumStatus"),
                    "구독 확인 결과: ${if (hasPremium) "프리미엄 ✅" else "일반 사용자"}")

                // ✅ DB 업데이트
                scope.launch {
                    updateUserPremiumStatus(hasPremium)
                }
            }

        } catch (e: Exception) {
            Log.e(TAG("PremiumManager", "syncPremiumStatus"), "구독 상태 확인 실패", e)
        }
    }

    /**
     * User DB에 프리미엄 상태 업데이트
     */
    private suspend fun updateUserPremiumStatus(isPremium: Boolean) {
        try {
            val currentUser = userRepository.userData.value?.localUserData

            if (currentUser == null) {
                Log.w(TAG("PremiumManager", "updateUserPremiumStatus"), "사용자 정보 없음")
                return
            }

            // isPremium 상태가 바뀐 경우만 업데이트
            if (currentUser.isPremium != isPremium) {
                val updatedUser = currentUser.copy(isPremium = isPremium)
                userRepository.localUserUpdate(updatedUser)

                Log.d(TAG("PremiumManager", "updateUserPremiumStatus"),
                    "✅ DB 업데이트 완료: isPremium = $isPremium")
            } else {
                Log.d(TAG("PremiumManager", "updateUserPremiumStatus"),
                    "변경 없음: isPremium = $isPremium")
            }

        } catch (e: Exception) {
            Log.e(TAG("PremiumManager", "updateUserPremiumStatus"), "DB 업데이트 실패", e)
        }
    }

    /**
     * 수동으로 프리미엄 상태 새로고침
     * (설정 화면에서 호출 가능)
     */
    fun refreshPremiumStatus() {
        Log.d(TAG("PremiumManager", "refreshPremiumStatus"), "수동 새로고침 요청")
        scope.launch {
            syncPremiumStatus()
        }
    }

    /**
     * 테스트용: 프리미엄 상태 강제 설정 (디버그 빌드에서만 사용)
     */
    suspend fun setTestPremiumStatus(isPremium: Boolean) {
        Log.d(TAG("PremiumManager", "setTestPremiumStatus"), "테스트 프리미엄 상태: $isPremium")
        updateUserPremiumStatus(isPremium)
    }

    suspend fun grantTestPremium(user: LocalUserData, minutes: Int = 1): Boolean {
        val expiryDate = Instant.now().plus(minutes.toLong(), ChronoUnit.MINUTES).toString()

        val updatedUser = user.copy(
            premiumType = "REWARD_AD",
            premiumExpiryDate = expiryDate,
            premiumGrantedBy = "test",
            premiumGrantedAt = Instant.now().toString(),
            isPremium = true
        )

        userUseCases.localUserUpdate(updatedUser)
        Log.d(TAG("PremiumManager", "grantTestPremium"), "테스트 프리미엄 지급: ${minutes}분 후 만료")

        return true
    }
}


