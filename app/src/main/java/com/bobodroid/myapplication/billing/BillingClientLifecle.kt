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

/**
 * BillingClient 생명주기 관리 클래스 - 완전판
 * - 구매 흐름 처리
 * - 서버 검증 콜백 제공
 * - 구독 상태 확인
 */
class BillingClientLifecycle private constructor(
    private val applicationContext: Context,
    private val externalScope: CoroutineScope =
        CoroutineScope(SupervisorJob() + Dispatchers.Default)
): DefaultLifecycleObserver {

    private val _fetchedProductList = MutableStateFlow<List<ProductDetails>>(emptyList())
    val fetchedProductList = _fetchedProductList.asStateFlow()

    // ✅ 구매 완료 콜백 (외부에서 설정)
    private var onPurchaseCallback: ((Purchase) -> Unit)? = null

    fun setOnPurchaseCallback(callback: (Purchase) -> Unit) {
        onPurchaseCallback = callback
    }

    companion object {
        const val TAG = "BillingClientLifecycle"
        const val PRODUCT_ID = "recordadvertisementremove"

        @Volatile
        private var INSTANCE: BillingClientLifecycle? = null

        fun getInstance(applicationContext: Context): BillingClientLifecycle {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: BillingClientLifecycle(applicationContext).also { INSTANCE = it }
            }
        }
    }

    // ✅ 구매 업데이트 리스너 (서버 검증 연동)
    private val purchasesUpdatedListener =
        PurchasesUpdatedListener { billingResult, purchases ->
            Log.d(TAG, "purchasesUpdatedListener: responseCode=${billingResult.responseCode}")

            when (billingResult.responseCode) {
                BillingClient.BillingResponseCode.OK -> {
                    purchases?.forEach { purchase ->
                        Log.d(TAG, "구매 성공: products=${purchase.products}, state=${purchase.purchaseState}")

                        // ✅ PURCHASED 상태인 경우 서버 검증 트리거
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
                    // 복원 로직 트리거
                    queryActivePurchases(PRODUCT_ID) { hasPremium ->
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

    // 결제 클라이언트 상태 리스너
    private val billingClientStateListener = object : BillingClientStateListener {
        override fun onBillingServiceDisconnected() {
            Log.d(TAG, "Billing 서비스 연결 끊김 - 재연결 필요")
        }

        override fun onBillingSetupFinished(billingResult: BillingResult) {
            Log.d(TAG, "Billing 설정 완료: ${billingResult.debugMessage}")
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                Log.d(TAG, "BillingClient 준비 완료")
                fetchAvailableProducts()

                // ✅ 앱 시작 시 구독 상태 확인
                queryActivePurchases(PRODUCT_ID) { hasPremium ->
                    Log.d(TAG, "앱 시작 시 구독 상태: ${if (hasPremium) "구독 활성" else "구독 없음"}")
                }
            }
        }
    }

    // 상품 정보 응답 리스너
    private val productDetailsResponseListener = object : ProductDetailsResponseListener {
        override fun onProductDetailsResponse(
            billingResult: BillingResult,
            productDetailsResult: com.android.billingclient.api.QueryProductDetailsResult
        ) {
            val productDetailsList = productDetailsResult.productDetailsList ?: emptyList()
            Log.d(TAG, "상품 정보 조회: count=${productDetailsList.size}")
            productDetailsList.forEach { product ->
                Log.d(TAG, "상품: ${product.productId}, ${product.name}")
            }
            externalScope.launch {
                _fetchedProductList.emit(productDetailsList)
            }
        }
    }

    // BillingClient 인스턴스
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

    // 구매 가능 상품 쿼리 파라미터
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

    /**
     * 구매 가능한 상품 목록 가져오기
     */
    fun fetchAvailableProducts() {
        Log.d(TAG, "상품 정보 조회 시작")
        billingClient.queryProductDetailsAsync(queryProductDetailsParams, productDetailsResponseListener)
    }

    /**
     * 구매 플로우 시작
     */
    fun startBillingFlow(activity: Activity, productDetails: ProductDetails): Int {
        val offerToken = productDetails.subscriptionOfferDetails?.lastOrNull()?.offerToken
            ?: run {
                Log.e(TAG, "offerToken을 찾을 수 없습니다")
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
        Log.d(TAG, "구매 플로우 시작: ${billingResult.responseCode}, ${billingResult.debugMessage}")

        return billingResult.responseCode
    }

    /**
     * 활성 구독 확인
     */
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

    /**
     * ✅ 최신 구매 정보 가져오기 (서버 동기화용)
     */
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

    /**
     * BillingClient 준비 상태 확인
     */
    fun isClientReady(): Boolean = billingClient.isReady
}