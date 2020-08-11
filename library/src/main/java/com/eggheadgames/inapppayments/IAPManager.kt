package com.eggheadgames.inapppayments

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import com.billing.BillingService
import com.billing.IBillingService
import com.billing.PurchaseServiceListener
import com.billing.SubscriptionServiceListener

//Public front-end for IAP functionality.
object IAPManager {

    @SuppressLint("StaticFieldLeak")
    private var mBillingService: IBillingService? = null

    /**
     * @param context          - application context
     * @param iapKeys          - list of sku for purchases
     * @param subscriptionKeys - list of sku for subscriptions
     */
    @JvmStatic
    fun build(context: Context, iapKeys: List<String>, subscriptionKeys: List<String> = emptyList()) {
        val contextLocal = context.applicationContext ?: context
        mBillingService = BillingService(contextLocal, iapKeys, subscriptionKeys)
    }

    /**
     * Initialize billing service.
     *
     * @param key - key to verify purchase messages. Currently valid only for Google Billing. Leave empty if you want to skip verification
     */
    @JvmStatic
    fun init(key: String? = null, enableLogging: Boolean = false) {
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
    fun buy(activity: Activity, sku: String) {
        getBillingService().buy(activity, sku)
    }

    @JvmStatic
    fun subscribe(activity: Activity, sku: String) {
        getBillingService().subscribe(activity, sku)
    }

    @JvmStatic
    fun unsubscribe(activity: Activity, sku: String) {
        getBillingService().unsubscribe(activity, sku)
    }

    @JvmStatic
    fun destroy() {
        getBillingService().close()
    }

    @JvmStatic
    fun getBillingService(): IBillingService {
        return mBillingService ?: let {
            throw RuntimeException("Call IAPManager.build to initialize billing service")
        }
    }
}