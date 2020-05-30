package com.eggheadgames.inapppayments

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import com.billing.BillingService
import com.billing.PurchaseServiceListener
import com.billing.SubscriptionServiceListener
import com.billing.amazon.AmazonBillingService
import com.billing.google.GoogleBillingService2
import java.util.*

//Public front-end for IAP functionality.
object IAPManager {
    const val BUILD_TARGET_GOOGLE = 0
    const val BUILD_TARGET_AMAZON = 1

    @SuppressLint("StaticFieldLeak")
    private var mBillingService: BillingService? = null

    /**
     * @param context          - application context
     * @param buildTarget      - IAPManager.BUILD_TARGET_GOOGLE or IAPManager.BUILD_TARGET_AMAZON
     * @param iapKeys          - list of sku for purchases
     * @param subscriptionKeys - list of sku for subscriptions
     */
    @JvmStatic
    fun build(context: Context, buildTarget: Int, iapKeys: List<String>, subscriptionKeys: List<String> = emptyList()) {
        val contextLocal = context.applicationContext ?: context

        //Build-specific initializations
        if (buildTarget == BUILD_TARGET_GOOGLE) {
            mBillingService = GoogleBillingService2(contextLocal, iapKeys, subscriptionKeys)

        } else if (buildTarget == BUILD_TARGET_AMAZON) {
            val keys: MutableList<String> = ArrayList()
            keys.addAll(iapKeys)
            keys.addAll(subscriptionKeys)
            mBillingService = AmazonBillingService(contextLocal, keys)
        }
    }

    fun init(key: String? = null, enableLogging: Boolean) {
        getBillingService().init(key)
        getBillingService().enableDebugLogging(enableLogging)
    }

    @JvmStatic
    fun addPurchaseListener(purchaseServiceListener: PurchaseServiceListener) {
        getBillingService().addPurchaseListener(purchaseServiceListener)
    }

    @JvmStatic
    fun removePurchaseListener(purchaseServiceListener: PurchaseServiceListener) {
        getBillingService().removePurchaseListener(purchaseServiceListener)
    }

    @JvmStatic
    fun addSubscriptionListener(subscriptionServiceListener: SubscriptionServiceListener) {
        getBillingService().addSubscriptionListener(subscriptionServiceListener)
    }

    @JvmStatic
    fun removeSubscriptionListener(subscriptionServiceListener: SubscriptionServiceListener) {
        getBillingService().removeSubscriptionListener(subscriptionServiceListener)
    }

    @JvmStatic
    fun buy(activity: Activity, sku: String, id: Int) {
        getBillingService().buy(activity, sku, id)
    }

    @JvmStatic
    fun subscribe(activity: Activity, sku: String, id: Int) {
        getBillingService().subscribe(activity, sku, id)
    }

    fun unsubscribe(activity: Activity, sku: String, id: Int) {
        getBillingService().unsubscribe(activity, sku, id)
    }

    @JvmStatic
    fun destroy() {
        getBillingService().close()
    }

    @JvmStatic
    fun getBillingService(): BillingService {
        return mBillingService ?: let {
            throw RuntimeException("Call IAPManager.build to initialize billing service")
        }
    }
}