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
import com.android.billingclient.api.QueryProductDetailsResult
import com.android.billingclient.api.QueryPurchasesParams
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// DefaultLifecycleObserver 사용은 아직 모르겠음
// billingClient를 위한 context 생성자에 주입
class BillingClientLifecycle private constructor(
    private val applicationContext: Context,
    private val externalScope: CoroutineScope =
        CoroutineScope(SupervisorJob() + Dispatchers.Default)
): DefaultLifecycleObserver {

    private val _fetchedProductList = MutableStateFlow<List<ProductDetails>>(emptyList())

    val fetchedProductList = _fetchedProductList.asStateFlow()

    companion object {
        const val TAG = "BillingClientLifecycle"
        // https://stackoverflow.com/a/11640026
        @Volatile // 멀티쓰레드에서 다른 쓰레드가 해당 인스턴스 변수를 건드리지 못하도록 설정하는 것 - 링크 참조
        private var INSTANCE : BillingClientLifecycle? = null
        // static method - 자기 자신 생성해서 가져오는 팩토리 매소드

        // 블럭내 코드가 호출될때 까지 자기 자신의 인스턴스 락시키기
        // 인스턴스가 없다면 생성
        fun getInstance(applicationContext: Context) : BillingClientLifecycle {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: BillingClientLifecycle(applicationContext).also { INSTANCE = it }
                // 인스턴스 생성하고 INSTANCE 변수에 생성한 녀석 넣어주기 also
            }
        }
    }

    // 구매 업데이트 리스너
    private val purchasesUpdatedListener =
        PurchasesUpdatedListener { billingResult, purchases ->
            // To be implemented in a later section.
            Log.d(TAG, "purchasesUpdatedListener: billingResult: $billingResult")
            purchases?.forEach {
                Log.d(TAG, "purchasesUpdatedListener: purchase: ${it}")
            }
        }

    // 결제 클라 상태 리스너
    // https://developer.android.com/google/play/billing/integrate?hl=ko#kts
    private val billingClientStateListener : BillingClientStateListener = object : BillingClientStateListener {
        //        참고: 자체 연결 재시도 로직을 구현하고 onBillingServiceDisconnected() 메서드를 재정의하는 것이 좋습니다.
//        모든 메서드를 실행할 때는 BillingClient 연결을 유지해야 합니다.
        override fun onBillingServiceDisconnected() {
            Log.d(TAG, "onBillingServiceDisconnected: ")
            Log.d(
                TAG, "Try to restart the connection on the next request to\n" +
                        "Google Play by calling the startConnection() method.")
        }

        override fun onBillingSetupFinished(p0: BillingResult) {
            Log.d(TAG, "onBillingSetupFinished: billingResult: ${p0.debugMessage}")
            if (p0.responseCode ==  BillingClient.BillingResponseCode.OK) {
                // The BillingClient is ready. You can query purchases here.
                Log.d(TAG, "The BillingClient is ready. You can query purchases here.")
                this@BillingClientLifecycle.fetchAvailableProducts()
            }

            // ✅ 구독 상태 확인 추가
            BillingClientLifecycle.getInstance(applicationContext)
                .queryActivePurchases("recordadvertisementremove") { hasPremium ->
                    Log.d(TAG, "Billing setup 후 구독 상태 확인: ${if (hasPremium) "프리미엄" else "일반 사용자"}")
                }
        }
    }

    // object 형태로 리스너를 받아도 됨
    // 리스너 메소드가 하나만 있을 경우에는 람다로 대체 가능
    private val productDetailsResponseListener = object : ProductDetailsResponseListener {

        override fun onProductDetailsResponse(p0: BillingResult, p1: QueryProductDetailsResult) {
            Log.d(TAG, "onProductDetailsResponse: billingResult: $p0, fetchedProductList: ${p1.productDetailsList}")
            externalScope.launch {
                // 이제 productDetailsList는 이미 List<ProductDetails>이므로 toList() 호출 필요 없음
                _fetchedProductList.emit(p1.productDetailsList)
            }
        }
    }

    // 참고: 일부 Android 기기에는 정기 결제와 같은 특정 제품 유형을 지원하지 않는 이전 버전의 Google Play 스토어 앱이 포함되어 있을 수 있습니다. 앱에서 결제 흐름을 시작하기 전에 isFeatureSupported()를 호출하여 판매하려는 제품을 기기에서 지원하는지 확인할 수 있습니다. 지원되는 상품 유형 목록은 BillingClient.FeatureType을 참고하세요.

    // 내부에서 사용하기 위한 결제 클라이언트
    private var billingClient: BillingClient


    init {

        val pendingPurchasesParams = PendingPurchasesParams.newBuilder()
            .enableOneTimeProducts() // <-- 이 줄을 추가합니다.
            .build()

        Log.d(TAG, "init() called")
        billingClient = BillingClient.newBuilder(this.applicationContext)
            .setListener(purchasesUpdatedListener) // 구매 업데이트 리스너 같이 연결
            .enablePendingPurchases(pendingPurchasesParams)
            .build() // builder 패턴으로 생성

        // 결제 클라이언트가 준비되지 않았다면
        if(!billingClient.isReady) {
            // 결제 클라 연결 시키기
            billingClient.startConnection(this.billingClientStateListener)
        }
    }


    // 구매가능 제품 쿼리
    val queryProductDetailsParams =
        QueryProductDetailsParams.newBuilder()
            .setProductList(
                // ImmutableList.of(...) 대신 Kotlin의 listOf(...) 사용
                listOf(
                    QueryProductDetailsParams.Product.newBuilder()
                        .setProductId("recordadvertisementremove")
                        .setProductType(BillingClient.ProductType.SUBS)
                        .build(),
                )
            )
            .build()

    // 구입 가능한 제품 표시
    fun fetchAvailableProducts(){
        Log.d(TAG, "fetchAvailableProducts: ")
        billingClient.queryProductDetailsAsync(queryProductDetailsParams, productDetailsResponseListener)
    }


    fun startBillingFlow(activity: Activity,
                         productDetails: ProductDetails) : Int{

        val offerToken = productDetails.subscriptionOfferDetails?.let { offerDetailsList ->
            offerDetailsList.last().offerToken
        } ?: ""

        val productDetailsParamsList = listOf(
            BillingFlowParams.ProductDetailsParams.newBuilder()
                .setProductDetails(productDetails)
                .setOfferToken(offerToken)
                .build()
        )
        val billingFlowParams = BillingFlowParams
            .newBuilder()
            .setProductDetailsParamsList(productDetailsParamsList)
            .build()

        if (!billingClient.isReady) {
            Log.e(TAG, "launchBillingFlow: BillingClient is not ready")
        }
        val billingResult = billingClient.launchBillingFlow(activity, billingFlowParams)

        val responseCode = billingResult.responseCode
        val debugMessage = billingResult.debugMessage
        Log.d(TAG, "launchBillingFlow: BillingResponse $responseCode $debugMessage")
        return responseCode
    }


    /**
     * 활성 구독 확인 (프리미엄 체크용)
     */
    fun queryActivePurchases(
        productId: String,
        onResult: (Boolean) -> Unit
    ) {
        val params = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.SUBS)
            .build()

        billingClient.queryPurchasesAsync(params) { billingResult, purchases ->
            Log.d(TAG, "queryActivePurchases: 결과=${billingResult.responseCode}, 구매수=${purchases.size}")

            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                val hasPremium = purchases.any { purchase ->
                    purchase.products.contains(productId) &&
                            purchase.purchaseState == Purchase.PurchaseState.PURCHASED
                }

                purchases.forEach { purchase ->
                    Log.d(TAG, "구매 항목: ${purchase.products}, 상태: ${purchase.purchaseState}")
                }

                onResult(hasPremium)
            } else {
                Log.e(TAG, "구매 조회 실패: ${billingResult.debugMessage}")
                onResult(false)
            }
        }
    }

    /**
     * BillingClient 준비 상태 확인
     */
    fun isClientReady(): Boolean {
        return billingClient.isReady
    }

}
