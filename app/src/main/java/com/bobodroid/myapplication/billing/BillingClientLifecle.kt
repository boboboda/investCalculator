package com.bobodroid.myapplication.billing

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.lifecycle.DefaultLifecycleObserver
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.ProductDetailsResponseListener
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Singleton

/**
 * BillingClient 생명주기 관리 클래스
 * - 하나의 상품에서 월간/연간 요금제 모두 가져오기
 */

class BillingClientLifecycle private constructor(
    private val applicationContext: Context,
    private val externalScope: CoroutineScope =
        CoroutineScope(SupervisorJob() + Dispatchers.Default)
): DefaultLifecycleObserver {

    private val _fetchedProductList = MutableStateFlow<List<ProductDetails>>(emptyList())
    val fetchedProductList = _fetchedProductList.asStateFlow()

    private var onPurchaseCallback: ((Purchase) -> Unit)? = null

    fun setOnPurchaseCallback(callback: (Purchase) -> Unit) {
        onPurchaseCallback = callback
    }

    companion object {
        const val TAG = "BillingClientLifecycle"

        // ✅ 하나의 상품 ID (여기에 월간/연간 요금제가 모두 포함됨)
        const val PRODUCT_ID = "recordadvertisementremove"

        // ✅ 기본 요금제 ID들
        const val BASE_PLAN_MONTHLY = "recordadvertisementremove"  // 월간
        const val BASE_PLAN_YEARLY = "premium-subscription-yearly"  // 연간

        @Volatile
        private var INSTANCE: BillingClientLifecycle? = null

        fun getInstance(applicationContext: Context): BillingClientLifecycle {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: BillingClientLifecycle(applicationContext).also { INSTANCE = it }
            }
        }
    }

    private val purchasesUpdatedListener =
        PurchasesUpdatedListener { billingResult, purchases ->
            Log.d(TAG, "purchasesUpdatedListener: responseCode=${billingResult.responseCode}")

            when (billingResult.responseCode) {
                BillingClient.BillingResponseCode.OK -> {
                    purchases?.forEach { purchase ->
                        Log.d(TAG, "구매 성공: products=${purchase.products}, state=${purchase.purchaseState}")

                        if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
                            Log.d(TAG, "서버 검증 콜백 호출: token=${purchase.purchaseToken}")
                            onPurchaseCallback?.invoke(purchase)
                        } else if (purchase.purchaseState == Purchase.PurchaseState.PENDING) {
                            Log.d(TAG, "구매 대기 중 (PENDING)")
                        }
                    }
                }
                BillingClient.BillingResponseCode.USER_CANCELED -> {
                    Log.d(TAG, "사용자가 구매를 취소했습니다")
                }
                BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED -> {
                    Log.d(TAG, "이미 구매한 상품입니다 - 복원 필요")
                    queryAllActiveSubscriptions { hasPremium ->
                        if (hasPremium) {
                            Log.d(TAG, "구독 복원 완료")
                        }
                    }
                }
                else -> {
                    Log.e(TAG, "구매 실패: ${billingResult.debugMessage}")
                }
            }
        }

    private val billingClientStateListener = object : BillingClientStateListener {
        override fun onBillingServiceDisconnected() {
            Log.d(TAG, "Billing 서비스 연결 끊김 - 재연결 필요")
        }

        override fun onBillingSetupFinished(billingResult: BillingResult) {
            Log.d(TAG, "Billing 설정 완료: ${billingResult.debugMessage}")
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                Log.d(TAG, "BillingClient 준비 완료")
                fetchAvailableProducts()

                queryAllActiveSubscriptions { hasPremium ->
                    Log.d(TAG, "앱 시작 시 구독 상태: ${if (hasPremium) "구독 활성" else "구독 없음"}")
                }
            }
        }
    }

    private val productDetailsResponseListener = object : ProductDetailsResponseListener {
        override fun onProductDetailsResponse(
            billingResult: BillingResult,
            productDetailsResult: com.android.billingclient.api.QueryProductDetailsResult
        ) {
            Log.d(TAG, "━━━━━━━━━━━━━━━━━━━━━━━━━━")
            Log.d(TAG, "상품 조회 응답 코드: ${billingResult.responseCode}")
            Log.d(TAG, "상품 조회 디버그 메시지: ${billingResult.debugMessage}")

            val productDetailsList = productDetailsResult.productDetailsList ?: emptyList()

            Log.d(TAG, "상품 정보 조회 완료: ${productDetailsList.size}개")

            if (productDetailsList.isEmpty()) {
                Log.w(TAG, "⚠️ 조회된 상품이 없습니다!")
            }

            productDetailsList.forEach { product ->
                Log.d(TAG, "  └─ 상품: ${product.productId} | ${product.name}")
                Log.d(TAG, "     타입: ${product.productType}")

                // ✅ 기본 요금제 목록 로그
                val offers = product.subscriptionOfferDetails
                Log.d(TAG, "     기본 요금제 개수: ${offers?.size ?: 0}개")

                offers?.forEach { offer ->
                    val basePlanId = offer.basePlanId
                    val pricingPhase = offer.pricingPhases.pricingPhaseList.firstOrNull()
                    val price = pricingPhase?.formattedPrice ?: "N/A"
                    val billingPeriod = pricingPhase?.billingPeriod ?: "N/A"

                    Log.d(TAG, "       ├─ 요금제: $basePlanId")
                    Log.d(TAG, "       │  가격: $price")
                    Log.d(TAG, "       │  주기: $billingPeriod")
                }
            }
            Log.d(TAG, "━━━━━━━━━━━━━━━━━━━━━━━━━━")

            externalScope.launch {
                _fetchedProductList.emit(productDetailsList)
            }
        }
    }

    private var billingClient: BillingClient

    init {
        val pendingPurchasesParams = PendingPurchasesParams.newBuilder()
            .enableOneTimeProducts()
            .build()

        Log.d(TAG, "BillingClient 초기화")
        billingClient = BillingClient.newBuilder(applicationContext)
            .setListener(purchasesUpdatedListener)
            .enablePendingPurchases(pendingPurchasesParams)
            .build()

        if (!billingClient.isReady) {
            billingClient.startConnection(billingClientStateListener)
        }
    }

    // ✅ 1개 상품만 조회 (월간/연간 요금제가 모두 포함됨)
    private val queryProductDetailsParams =
        QueryProductDetailsParams.newBuilder()
            .setProductList(
                listOf(
                    QueryProductDetailsParams.Product.newBuilder()
                        .setProductId(PRODUCT_ID)
                        .setProductType(BillingClient.ProductType.SUBS)
                        .build()
                )
            )
            .build()

    fun fetchAvailableProducts() {
        Log.d(TAG, "━━━━━━━━━━━━━━━━━━━━━━━━━━")
        Log.d(TAG, "상품 정보 조회 시작")
        Log.d(TAG, "요청 제품: $PRODUCT_ID (월간/연간 요금제 포함)")
        Log.d(TAG, "━━━━━━━━━━━━━━━━━━━━━━━━━━")
        billingClient.queryProductDetailsAsync(queryProductDetailsParams, productDetailsResponseListener)
    }

    /**
     * ✅ 구매 플로우 시작 (특정 요금제 선택)
     */
    fun startBillingFlow(
        activity: Activity,
        productDetails: ProductDetails,
        basePlanId: String  // 월간 or 연간 요금제 ID
    ): Int {
        val offer = productDetails.subscriptionOfferDetails?.find {
            it.basePlanId == basePlanId
        }

        val offerToken = offer?.offerToken
            ?: run {
                Log.e(TAG, "offerToken을 찾을 수 없습니다: basePlanId=$basePlanId")
                return BillingClient.BillingResponseCode.ERROR
            }

        val productDetailsParamsList = listOf(
            BillingFlowParams.ProductDetailsParams.newBuilder()
                .setProductDetails(productDetails)
                .setOfferToken(offerToken)
                .build()
        )

        val billingFlowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(productDetailsParamsList)
            .build()

        if (!billingClient.isReady) {
            Log.e(TAG, "BillingClient가 준비되지 않았습니다")
            return BillingClient.BillingResponseCode.SERVICE_UNAVAILABLE
        }

        val billingResult = billingClient.launchBillingFlow(activity, billingFlowParams)
        Log.d(TAG, "구매 플로우 시작: basePlanId=$basePlanId, responseCode=${billingResult.responseCode}")

        return billingResult.responseCode
    }

    fun queryActivePurchases(productId: String, onResult: (Boolean) -> Unit) {
        val params = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.SUBS)
            .build()

        billingClient.queryPurchasesAsync(params) { billingResult, purchases ->
            Log.d(TAG, "구독 조회: responseCode=${billingResult.responseCode}, count=${purchases.size}")

            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                val hasPremium = purchases.any { purchase ->
                    purchase.products.contains(productId) &&
                            purchase.purchaseState == Purchase.PurchaseState.PURCHASED
                }

                purchases.forEach { purchase ->
                    Log.d(TAG, "구매 항목: ${purchase.products}, state=${purchase.purchaseState}")
                }

                onResult(hasPremium)
            } else {
                Log.e(TAG, "구매 조회 실패: ${billingResult.debugMessage}")
                onResult(false)
            }
        }
    }

    fun queryAllActiveSubscriptions(onResult: (Boolean) -> Unit) {
        val params = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.SUBS)
            .build()

        billingClient.queryPurchasesAsync(params) { billingResult, purchases ->
            Log.d(TAG, "전체 구독 조회: responseCode=${billingResult.responseCode}, count=${purchases.size}")

            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                val hasPremium = purchases.any { purchase ->
                    purchase.products.contains(PRODUCT_ID) &&
                            purchase.purchaseState == Purchase.PurchaseState.PURCHASED
                }

                purchases.forEach { purchase ->
                    Log.d(TAG, "구매 항목: ${purchase.products}, state=${purchase.purchaseState}")
                }

                Log.d(TAG, "프리미엄 상태: ${if (hasPremium) "활성" else "비활성"}")
                onResult(hasPremium)
            } else {
                Log.e(TAG, "구매 조회 실패: ${billingResult.debugMessage}")
                onResult(false)
            }
        }
    }

    fun getLatestPurchase(productId: String, onResult: (Purchase?) -> Unit) {
        val params = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.SUBS)
            .build()

        billingClient.queryPurchasesAsync(params) { billingResult, purchases ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                val purchase = purchases.find {
                    it.products.contains(productId) &&
                            it.purchaseState == Purchase.PurchaseState.PURCHASED
                }
                onResult(purchase)
            } else {
                onResult(null)
            }
        }
    }

    fun getLatestActiveSubscription(onResult: (Purchase?) -> Unit) {
        val params = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.SUBS)
            .build()

        billingClient.queryPurchasesAsync(params) { billingResult, purchases ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                val purchase = purchases.find {
                    it.products.contains(PRODUCT_ID) &&
                            it.purchaseState == Purchase.PurchaseState.PURCHASED
                }
                onResult(purchase)
            } else {
                onResult(null)
            }
        }
    }

    fun isClientReady(): Boolean = billingClient.isReady
}