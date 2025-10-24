package com.bobodroid.myapplication.premium

import android.content.Context
import android.util.Log
import com.android.billingclient.api.Purchase
import com.bobodroid.myapplication.MainActivity.Companion.TAG
import com.bobodroid.myapplication.billing.BillingClientLifecycle
import com.bobodroid.myapplication.models.datamodels.repository.UserRepository
import com.bobodroid.myapplication.models.datamodels.roomDb.LocalUserData
import com.bobodroid.myapplication.models.datamodels.roomDb.PremiumType
import com.bobodroid.myapplication.models.datamodels.service.subscriptionApi.SubscriptionApi
import com.bobodroid.myapplication.models.datamodels.service.subscriptionApi.VerifyPurchaseRequest
import com.bobodroid.myapplication.models.datamodels.useCases.UserUseCases
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.temporal.ChronoUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 프리미엄 상태 관리자 - 완전판
 * - 앱 시작 시 구독 상태 확인 및 서버 동기화
 * - BillingClient 구매 이벤트 감시
 * - 서버 검증 자동 처리
 * - UserRepository(DB) 자동 업데이트
 * - 주기적 만료 확인
 */
@Singleton
class PremiumManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val userRepository: UserRepository,
    private val userUseCases: UserUseCases
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val billingClient: BillingClientLifecycle by lazy {
        BillingClientLifecycle.getInstance(context)
    }

    companion object {
        private const val PRODUCT_ID = "recordadvertisementremove"
        private const val SYNC_INTERVAL_MS = 3_600_000L // 1시간
    }

    init {
        Log.d(TAG("PremiumManager", "init"), "━━━━━━━━━━━━━━━━━━━━━━━━━━")
        Log.d(TAG("PremiumManager", "init"), "프리미엄 관리자 시작")

        // ✅ 구매 완료 콜백 등록
        billingClient.setOnPurchaseCallback { purchase ->
            scope.launch {
                handlePurchase(purchase)
            }
        }

        // ✅ 앱 시작 시 구독 상태 동기화
        scope.launch {
            delay(1000) // BillingClient 초기화 대기
            syncPremiumStatus()
            startPeriodicSync()
        }
    }

    /**
     * ✅ 구매 완료 처리 - 서버 검증 및 DB 업데이트
     */
    private suspend fun handlePurchase(purchase: Purchase) {
        Log.d(TAG("PremiumManager", "handlePurchase"), "구매 처리 시작: ${purchase.products}")

        val user = userRepository.userData.value?.localUserData ?: run {
            Log.e(TAG("PremiumManager", "handlePurchase"), "사용자 데이터 없음")
            return
        }

        try {
            // 서버에 영수증 검증 요청
            val request = VerifyPurchaseRequest(
                deviceId = user.id.toString(),
                productId = PRODUCT_ID,
                basePlanId = extractBasePlanId(purchase),
                purchaseToken = purchase.purchaseToken,
                packageName = purchase.packageName
            )

            Log.d(TAG("PremiumManager", "handlePurchase"), "서버 검증 요청: $request")

            val response = SubscriptionApi.service.verifyPurchase(request)

            if (response.success && response.data != null) {
                Log.d(TAG("PremiumManager", "handlePurchase"), "서버 검증 성공: ${response.data}")

                // DB 업데이트
                val updatedUser = user.copy(
                    premiumType = "SUBSCRIPTION",
                    premiumExpiryDate = response.data.expiryTime,
                    premiumGrantedBy = "subscription",
                    premiumGrantedAt = Instant.now().toString(),
                    isPremium = true
                )

                userUseCases.localUserUpdate(updatedUser)
                Log.d(TAG("PremiumManager", "handlePurchase"), "✅ 프리미엄 활성화 완료")
            } else {
                Log.e(TAG("PremiumManager", "handlePurchase"), "서버 검증 실패: ${response.message}")
            }
        } catch (e: Exception) {
            Log.e(TAG("PremiumManager", "handlePurchase"), "구매 처리 실패", e)
        }
    }

    /**
     * ✅ 구독 상태 동기화 (앱 시작 시 / 주기적)
     */
    suspend fun syncPremiumStatus() {
        Log.d(TAG("PremiumManager", "syncPremiumStatus"), "구독 상태 동기화 시작")

        val user = userRepository.userData.value?.localUserData ?: run {
            Log.w(TAG("PremiumManager", "syncPremiumStatus"), "사용자 데이터 없음")
            return
        }

        try {
            // 1. BillingClient로 로컬 구독 확인
            billingClient.getLatestPurchase(PRODUCT_ID) { purchase ->
                scope.launch {
                    if (purchase != null && purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
                        Log.d(TAG("PremiumManager", "syncPremiumStatus"), "활성 구독 발견")

                        // 서버 상태 확인
                        try {
                            val response = SubscriptionApi.service.getSubscriptionStatus(user.id.toString())

                            if (response.success && response.data.isPremium) {
                                Log.d(TAG("PremiumManager", "syncPremiumStatus"), "서버 구독 확인: ${response.data}")

                                // DB 업데이트
                                val updatedUser = user.copy(
                                    premiumType = "SUBSCRIPTION",
                                    premiumExpiryDate = response.data.expiryTime,
                                    isPremium = true
                                )
                                userUseCases.localUserUpdate(updatedUser)
                            } else {
                                // 서버에는 없는데 로컬에 있음 → 서버 재검증 요청
                                Log.d(TAG("PremiumManager", "syncPremiumStatus"), "서버 재검증 요청")
                                SubscriptionApi.service.reverifySubscription(user.id.toString())
                            }
                        } catch (e: Exception) {
                            Log.e(TAG("PremiumManager", "syncPremiumStatus"), "서버 확인 실패", e)
                        }
                    } else {
                        Log.d(TAG("PremiumManager", "syncPremiumStatus"), "활성 구독 없음")

                        // DB에 구독이 있으면 만료 처리
                        if (user.premiumType == "SUBSCRIPTION") {
                            expirePremium(user)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG("PremiumManager", "syncPremiumStatus"), "동기화 실패", e)
        }
    }

    /**
     * ✅ 주기적 동기화 (1시간마다)
     */
    private fun startPeriodicSync() {
        scope.launch {
            while (true) {
                delay(SYNC_INTERVAL_MS)
                Log.d(TAG("PremiumManager", "periodicSync"), "주기적 동기화 실행")
                syncPremiumStatus()
            }
        }
    }

    /**
     * 프리미엄 상태 확인 (우선순위: LIFETIME > SUBSCRIPTION > EVENT > REWARD_AD > NONE)
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

        // 만료 확인
        if (now.isAfter(expiry)) {
            Log.d(TAG("PremiumManager", "checkPremiumStatus"), "프리미엄 만료: ${user.premiumType}")
            return PremiumType.NONE
        }

        // 타입 반환
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
        val today = Instant.now().truncatedTo(ChronoUnit.DAYS).toString()

        if (user.lastRewardDate == today) {
            Log.d(TAG("PremiumManager", "grantRewardPremium"), "오늘 이미 리워드 사용")
            return false
        }

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
        Log.d(TAG("PremiumManager", "grantRewardPremium"), "✅ 24시간 프리미엄 지급: $expiryDate")

        return true
    }

    /**
     * 오늘 리워드 광고 사용 가능 여부
     */
    fun canUseRewardAdToday(user: LocalUserData): Boolean {
        val today = Instant.now().truncatedTo(ChronoUnit.DAYS).toString()
        return user.lastRewardDate != today
    }

    /**
     * 정기 구독 활성화
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
        Log.d(TAG("PremiumManager", "activateSubscription"), "✅ 정기 구독 활성화: $expiryDate")

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
        Log.d(TAG("PremiumManager", "expirePremium"), "✅ 프리미엄 만료 처리")

        return true
    }

    /**
     * 프리미엄 남은 시간 (초)
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
     * 프리미엄 만료 1시간 전인지 확인 (푸시 알림용)
     */
    fun isExpiringWithinHour(user: LocalUserData): Boolean {
        val remaining = getRemainingSeconds(user)
        return remaining in 1..3600  // 1초~1시간 사이
    }

    /**
     * ✅ 수동으로 프리미엄 상태 새로고침
     */
    fun refreshPremiumStatus() {
        Log.d(TAG("PremiumManager", "refreshPremiumStatus"), "수동 새로고침 요청")
        scope.launch {
            syncPremiumStatus()
        }
    }

    /**
     * ✅ User DB에 프리미엄 상태 업데이트
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
            }
        } catch (e: Exception) {
            Log.e(TAG("PremiumManager", "updateUserPremiumStatus"), "DB 업데이트 실패", e)
        }
    }

    /**
     * ✅ 테스트용: 프리미엄 상태 강제 설정 (디버그 빌드에서만)
     */
    suspend fun setTestPremiumStatus(isPremium: Boolean) {
        Log.d(TAG("PremiumManager", "setTestPremiumStatus"), "테스트 프리미엄 상태: $isPremium")
        updateUserPremiumStatus(isPremium)
    }

    /**
     * ✅ 테스트용: N분 후 만료되는 프리미엄 지급 (디버그)
     */
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
        Log.d(TAG("PremiumManager", "grantTestPremium"), "✅ 테스트 프리미엄 지급: ${minutes}분 후 만료")

        return true
    }

    /**
     * BasePlanId 추출 (Purchase 객체에서)
     */
    private fun extractBasePlanId(purchase: Purchase): String {
        return try {
            val json = org.json.JSONObject(purchase.originalJson)
            json.optString("basePlanId", "monthly-basic")
        } catch (e: Exception) {
            "monthly-basic" // 기본값
        }
    }
}