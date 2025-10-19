package com.bobodroid.myapplication.premium

import android.content.Context
import android.util.Log
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.QueryPurchasesParams
import com.bobodroid.myapplication.MainActivity.Companion.TAG
import com.bobodroid.myapplication.billing.BillingClientLifecycle
import com.bobodroid.myapplication.models.datamodels.repository.UserRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
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
    private val userRepository: UserRepository
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
    fun setTestPremiumStatus(isPremium: Boolean) {
        Log.d(TAG("PremiumManager", "setTestPremiumStatus"), "테스트 프리미엄 상태: $isPremium")
        scope.launch {
            updateUserPremiumStatus(isPremium)
        }
    }
}