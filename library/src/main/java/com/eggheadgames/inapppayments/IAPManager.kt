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

    @JvmStatic
    @SuppressLint("StaticFieldLeak")
    var billingService: BillingService? = null
        private set

    /**
     * @param context          - application context
     * @param buildTarget      - IAPManager.BUILD_TARGET_GOOGLE or IAPManager.BUILD_TARGET_AMAZON
     * @param iapKeys          - list of sku for purchases
     * @param subscriptionKeys - list of sku for subscriptions
     */
    @JvmStatic
    fun build(context: Context, buildTarget: Int, iapKeys: List<String>?, subscriptionKeys: List<String>?) {
        val applicationContext = context.applicationContext
        val contextLocal = applicationContext ?: context

        //Build-specific initializations
        if (buildTarget == BUILD_TARGET_GOOGLE) {
            billingService = GoogleBillingService2(contextLocal, iapKeys!!, subscriptionKeys!!)
        } else if (buildTarget == BUILD_TARGET_AMAZON) {
            val keys: MutableList<String> = ArrayList()
            keys.addAll(iapKeys!!)
            keys.addAll(subscriptionKeys!!)
            billingService = AmazonBillingService(contextLocal, keys)
        }
    }

    fun init(key: String?, enableLogging: Boolean) {
        billingService!!.init(key)
        billingService!!.enableDebugLogging(enableLogging)
    }

    @JvmStatic
    fun addPurchaseListener(purchaseServiceListener: PurchaseServiceListener?) {
        billingService!!.addPurchaseListener(purchaseServiceListener)
    }

    @JvmStatic
    fun removePurchaseListener(purchaseServiceListener: PurchaseServiceListener?) {
        billingService!!.removePurchaseListener(purchaseServiceListener)
    }

    @JvmStatic
    fun addSubscriptionListener(subscriptionServiceListener: SubscriptionServiceListener?) {
        billingService!!.addSubscriptionListener(subscriptionServiceListener)
    }

    fun removeSubscriptionListener(subscriptionServiceListener: SubscriptionServiceListener?) {
        billingService!!.removeSubscriptionListener(subscriptionServiceListener)
    }

    fun buy(activity: Activity?, sku: String?, id: Int) {
        billingService!!.buy(activity, sku, id)
    }

    fun subscribe(activity: Activity?, sku: String?, id: Int) {
        billingService!!.subscribe(activity, sku, id)
    }

    fun unsubscribe(activity: Activity?, sku: String?, id: Int) {
        billingService!!.unsubscribe(activity, sku, id)
    }

    fun destroy() {
        billingService!!.close()
    }

}