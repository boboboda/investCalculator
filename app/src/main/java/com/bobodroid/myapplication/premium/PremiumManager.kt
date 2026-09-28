// app/src/main/java/com/bobodroid/myapplication/premium/PremiumManager.kt
package com.bobodroid.myapplication.premium

import android.content.Context
import android.util.Log
import com.android.billingclient.api.Purchase
import com.bobodroid.myapplication.MainActivity.Companion.TAG
import com.bobodroid.myapplication.billing.BillingClientLifecycle
import com.bobodroid.myapplication.models.datamodels.repository.UserRepository
import com.bobodroid.myapplication.models.datamodels.roomDb.LocalUserData
import com.bobodroid.myapplication.models.datamodels.roomDb.PremiumType
import com.bobodroid.myapplication.models.datamodels.service.subscriptionApi.RestoreSubscriptionRequest
import com.bobodroid.myapplication.models.datamodels.service.subscriptionApi.SubscriptionApi
import com.bobodroid.myapplication.models.datamodels.service.subscriptionApi.VerifyPurchaseRequest
import com.bobodroid.myapplication.models.datamodels.useCases.UserUseCases
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.temporal.ChronoUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 프리미엄 상태 관리자
 * - 앱 시작 시 구독 상태 확인 및 서버 동기화
 * - BillingClient 구매 이벤트 감시
 * - 서버 검증 자동 처리
 * - ✅ 통합 프리미엄 상태(구독 + 리워드 광고)는 refreshUnifiedPremiumStatus()가 담당
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
        @Deprecated("Use BillingClientLifecycle constants instead")
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

        // ✅ 앱 시작 시 동기화 (구독 + 통합 상태)
        scope.launch {
            delay(1000) // BillingClient 초기화 대기
            syncPremiumStatus()
            refreshUnifiedPremiumStatus() // ✅ 신규: 리워드 프리미엄도 서버 기준으로 확인
            startPeriodicSync()
        }
    }

    /**
     * ✅ 구매 완료 처리 - 서버 검증 및 DB 업데이트
     */
    private suspend fun handlePurchase(purchase: Purchase) {
        val user = userRepository.userData.value?.localUserData ?: run {
            Log.e(TAG("PremiumManager", "handlePurchase"), "사용자 데이터 없음")
            return
        }

        val productId = purchase.products.firstOrNull() ?: run {
            Log.e(TAG("PremiumManager", "handlePurchase"), "제품 ID 없음")
            return
        }

        try {
            val request = VerifyPurchaseRequest(
                deviceId = user.id.toString(),
                productId = productId,
                basePlanId = extractBasePlanId(purchase),
                purchaseToken = purchase.purchaseToken,
                packageName = purchase.packageName,
                socialId = user.socialId,
                socialType = user.socialType
            )

            val response = SubscriptionApi.service.verifyPurchase(request)

            if (response.success && response.data != null) {
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
     * ✅ 구독 상태 동기화 (앱 시작 시 / 주기적) - SUBSCRIPTION만 처리
     * (Play Billing 클라이언트를 직접 조회하는 경로 — 리워드는 refreshUnifiedPremiumStatus()가 담당)
     */
    suspend fun syncPremiumStatus() {
        val user = userRepository.userData.value?.localUserData ?: run {
            Log.e(TAG("PremiumManager", "syncPremiumStatus"), "사용자 데이터 없음")
            return
        }

        if (user.premiumType != "SUBSCRIPTION" && user.premiumType != "NONE") {
            Log.d(TAG("PremiumManager", "syncPremiumStatus"),
                "SUBSCRIPTION이 아님 (${user.premiumType}) - 동기화 건너뜀")
            return
        }

        try {
            withContext(Dispatchers.IO) {
                if (!billingClient.isClientReady()) {
                    Log.w(TAG("PremiumManager", "syncPremiumStatus"), "BillingClient 준비 안 됨")
                    return@withContext
                }

                billingClient.getLatestActiveSubscription { purchase ->
                    scope.launch {
                        if (purchase != null) {
                            val productId = purchase.products.firstOrNull() ?: ""
                            Log.d(TAG("PremiumManager", "syncPremiumStatus"), "활성 구독 발견: $productId")

                            try {
                                val response =
                                    SubscriptionApi.service.getSubscriptionStatus(user.id.toString())

                                if (response.success && response.data.isPremium) {
                                    if (shouldUpdateUser(user, "SUBSCRIPTION", response.data.expiryTime)) {
                                        val updatedUser = user.copy(
                                            premiumType = "SUBSCRIPTION",
                                            premiumExpiryDate = response.data.expiryTime,
                                            isPremium = true
                                        )
                                        userUseCases.localUserUpdate(updatedUser)
                                        Log.d(TAG("PremiumManager", "syncPremiumStatus"), "✅ DB 업데이트 완료")
                                    }
                                } else {
                                    SubscriptionApi.service.reverifySubscription(user.id.toString())
                                }
                            } catch (e: Exception) {
                                Log.e(TAG("PremiumManager", "syncPremiumStatus"), "서버 확인 실패", e)
                            }
                        } else {
                            if (user.premiumType == "SUBSCRIPTION") {
                                expireSubscription(user)
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG("PremiumManager", "syncPremiumStatus"), "동기화 실패", e)
        }
    }

    /**
     * ✅ 통합 프리미엄 상태 동기화 (구독 + 리워드 광고)
     * - 구독/리워드 상관없이 서버가 최종 판정한 결과로 로컬 DB를 갱신
     * - 리워드 광고 시청 직후, 그리고 주기적 동기화에서 호출
     */
    suspend fun refreshUnifiedPremiumStatus() {
        Log.d(TAG("PremiumManager", "refreshUnifiedPremiumStatus"), "통합 프리미엄 상태 동기화 시작")

        val user = userRepository.userData.value?.localUserData ?: run {
            Log.e(TAG("PremiumManager", "refreshUnifiedPremiumStatus"), "사용자 데이터 없음")
            return
        }

        try {
            val response = SubscriptionApi.service.getPremiumStatus(user.id.toString())

            if (!response.success) {
                Log.w(TAG("PremiumManager", "refreshUnifiedPremiumStatus"), "서버 응답 실패")
                return
            }

            val data = response.data

            if (data.isPremium && data.premiumType != "NONE") {
                if (shouldUpdateUser(user, data.premiumType, data.expiryTime)) {
                    val updatedUser = user.copy(
                        premiumType = data.premiumType,
                        premiumExpiryDate = data.expiryTime,
                        premiumGrantedBy = if (data.premiumType == "REWARD_AD") "reward" else "subscription",
                        isPremium = true
                    )
                    userUseCases.localUserUpdate(updatedUser)
                    Log.d(
                        TAG("PremiumManager", "refreshUnifiedPremiumStatus"),
                        "✅ DB 업데이트 완료: ${data.premiumType}, 만료일: ${data.expiryTime}"
                    )
                } else {
                    Log.d(TAG("PremiumManager", "refreshUnifiedPremiumStatus"), "DB 이미 최신 상태")
                }
            } else {
                if (user.premiumType != "NONE") {
                    val clearedUser = user.copy(
                        premiumType = "NONE",
                        premiumExpiryDate = null,
                        isPremium = false
                    )
                    userUseCases.localUserUpdate(clearedUser)
                    Log.d(TAG("PremiumManager", "refreshUnifiedPremiumStatus"), "🔴 서버 기준 프리미엄 없음 - 로컬 초기화")
                }
            }

            data.rewardAd?.let { rewardInfo ->
                Log.d(
                    TAG("PremiumManager", "refreshUnifiedPremiumStatus"),
                    "오늘 리워드 시청: ${rewardInfo.todayRewardCount}/${rewardInfo.dailyRewardCap}"
                )
            }
        } catch (e: Exception) {
            Log.e(TAG("PremiumManager", "refreshUnifiedPremiumStatus"), "동기화 실패", e)
        }
    }


    /**
     * ✅ DB 업데이트 필요 여부 체크 (중복 방지)
     */
    private fun shouldUpdateUser(
        user: LocalUserData,
        newPremiumType: String,
        newExpiryDate: String?
    ): Boolean {
        if (user.premiumType != newPremiumType) return true
        if (user.premiumExpiryDate != newExpiryDate) return true
        val shouldBePremium = newPremiumType != "NONE"
        if (user.isPremium != shouldBePremium) return true
        return false
    }

    /**
     * ✅ 주기적 동기화 (1시간마다) — 구독 + 통합 상태 둘 다 확인
     */
    private fun startPeriodicSync() {
        scope.launch {
            while (true) {
                delay(SYNC_INTERVAL_MS)
                Log.d(TAG("PremiumManager", "periodicSync"), "주기적 동기화 실행")
                syncPremiumStatus()
                refreshUnifiedPremiumStatus()
            }
        }
    }

    /**
     * ✅ 구독 만료 처리 (SUBSCRIPTION 전용)
     */
    private suspend fun expireSubscription(user: LocalUserData) {
        if (user.premiumType == "NONE" && user.premiumExpiryDate == null) return
        if (user.premiumType != "SUBSCRIPTION") return

        val updatedUser = user.copy(
            premiumType = "NONE",
            premiumExpiryDate = null,
            isPremium = false
        )

        userUseCases.localUserUpdate(updatedUser)
        Log.d(TAG("PremiumManager", "expireSubscription"), "✅ 구독 만료 처리 완료")
    }

    /**
     * 프리미엄 상태 확인 (우선순위: LIFETIME > SUBSCRIPTION > EVENT > REWARD_AD > NONE)
     */
    fun checkPremiumStatus(user: LocalUserData): PremiumType {
        if (user.premiumType == "LIFETIME") {
            return PremiumType.LIFETIME
        }

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

        if (now.isAfter(expiry)) {
            return PremiumType.NONE
        }

        return when (user.premiumType) {
            "SUBSCRIPTION" -> PremiumType.SUBSCRIPTION
            "EVENT" -> PremiumType.EVENT
            "REWARD_AD" -> PremiumType.REWARD_AD
            else -> PremiumType.NONE
        }
    }

    /**
     * ✅ 프리미엄 상태 새로고침 (외부 호출용 - 구독 전용 경로)
     */
    suspend fun refreshPremiumStatus() {
        syncPremiumStatus()
    }

    /**
     * ✅ isPremium 상태만 DB 업데이트
     */
    suspend fun updateUserPremiumStatus(isPremium: Boolean) {
        try {
            val currentUser = userRepository.userData.value?.localUserData ?: return

            if (currentUser.isPremium != isPremium) {
                val updatedUser = currentUser.copy(isPremium = isPremium)
                userRepository.localUserUpdate(updatedUser)
            }
        } catch (e: Exception) {
            Log.e(TAG("PremiumManager", "updateUserPremiumStatus"), "DB 업데이트 실패", e)
        }
    }

    /**
     * ✅ 테스트용: 프리미엄 상태 강제 설정 (디버그 빌드에서만)
     */
    suspend fun setTestPremiumStatus(isPremium: Boolean) {
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
        return true
    }

    /**
     * ✅ BasePlanId 추출 (Purchase 객체에서)
     */
    private fun extractBasePlanId(purchase: Purchase): String {
        return try {
            val json = org.json.JSONObject(purchase.originalJson)
            val basePlanId = json.optString("basePlanId", "")
            if (basePlanId.isEmpty()) "monthly-basic" else basePlanId
        } catch (e: Exception) {
            "monthly-basic"
        }
    }


    /**
     * ⚠️ 레거시: 리워드 광고로 24시간 프리미엄 로컬 즉시 지급
     * ✅ 새 흐름(SSV)에서는 더 이상 호출되지 않아야 함 — AdUseCase.showRewardAdAndGrantPremium()은
     *    이제 이 함수 대신 refreshUnifiedPremiumStatus()로 서버 상태를 반영함
     * 완전히 안 쓰이는 게 확인되면 삭제해도 무방
     */
    @Deprecated("서버 SSV 기반 흐름과 어긋남 - refreshUnifiedPremiumStatus() 사용 권장")
    suspend fun grantRewardPremium(user: LocalUserData): Boolean {
        val today = Instant.now().truncatedTo(ChronoUnit.DAYS).toString()

        if (user.lastRewardDate == today) {
            return false
        }

        val expiryDate = Instant.now().plus(24, ChronoUnit.HOURS).toString()

        val updatedUser = user.copy(
            premiumType = "REWARD_AD",
            premiumExpiryDate = expiryDate,
            premiumGrantedBy = "reward",
            premiumGrantedAt = Instant.now().toString(),
            lastRewardDate = today,
            dailyRewardUsed = true,
            isPremium = true
        )

        userUseCases.localUserUpdate(updatedUser)
        return true
    }

    /**
     * 오늘 리워드 광고 사용 가능 여부 (로컬 1차 체크용 — 최종 판단은 서버)
     */
    fun canUseRewardAdToday(user: LocalUserData): Boolean {
        val today = Instant.now().truncatedTo(ChronoUnit.DAYS).toString()
        return user.lastRewardDate != today
    }


    /**
     * ✅ 프리미엄 남은 초 계산
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
        return remaining in 1..3600
    }
}