package com.billing.google

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import com.android.billingclient.api.*
import com.android.vending.billing.Security
import com.billing.BillingService

class GoogleBillingService2(context: Context, private val inAppSkuKeys: List<String>, private val subscriptionSkuKeys: List<String>)
    : BillingService(context), PurchasesUpdatedListener, BillingClientStateListener, AcknowledgePurchaseResponseListener {

    private lateinit var mBillingClient: BillingClient
    private lateinit var decodedKey: String

    private val skusDetails = mutableMapOf<String, SkuDetails?>()

    override fun init(key: String) {
        decodedKey = decodeKey(key)

        mBillingClient = BillingClient.newBuilder(context).setListener(this).build()
        mBillingClient.startConnection(this)
    }

    override fun onBillingSetupFinished(billingResult: BillingResult) {
        Log.d(TAG, "onBillingSetupFinished: billingResult: $billingResult")
        if (billingResult.isOk()) {
            querySkuDetails()
            queryPurchases()
        }
    }

    private fun querySkuDetails() {
        inAppSkuKeys.forEach {
            it.toSkuDetails(BillingClient.SkuType.INAPP)
        }
        subscriptionSkuKeys.forEach {
            it.toSkuDetails(BillingClient.SkuType.SUBS)
        }
    }

    /**
     * Query Google Play Billing for existing purchases.
     * New purchases will be provided to the PurchasesUpdatedListener.
     */
    private fun queryPurchases() {
        Log.d(TAG, "queryPurchases: SUBS")
        val result: Purchase.PurchasesResult? = mBillingClient.queryPurchases(BillingClient.SkuType.SUBS)
        if (result != null && result.purchasesList != null) {
            processPurchases(result.purchasesList, isRestore = true)
        }
    }

    override fun buy(activity: Activity, sku: String, id: Int) {
        if (!sku.isSkuReady()) {
            Log.w(TAG, "buy. Google billing service is not ready yet.")
            return
        }

        launchBillingFlow(activity, sku, BillingClient.SkuType.INAPP)
    }

    override fun subscribe(activity: Activity, sku: String, id: Int) {
        if (!sku.isSkuReady()) {
            Log.w(TAG, "buy. Google billing service is not ready yet.")
            return
        }

        launchBillingFlow(activity, sku, BillingClient.SkuType.SUBS)
    }

    private fun launchBillingFlow(activity: Activity, sku: String, type: String) {
        sku.toSkuDetails(type) { skuDetails ->
            val purchaseParams = BillingFlowParams.newBuilder()
                    .setSkuDetails(skuDetails).build()
            mBillingClient.launchBillingFlow(activity, purchaseParams)
        }
    }

    override fun unsubscribe(activity: Activity, sku: String, id: Int) {
        try {
            val intent = Intent()
            intent.action = Intent.ACTION_VIEW
            val subscriptionUrl = ("http://play.google.com/store/account/subscriptions"
                    + "?package=" + activity.packageName
                    + "&sku=" + sku)
            intent.data = Uri.parse(subscriptionUrl)
            activity.startActivity(intent)
            activity.finish()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun enableDebugLogging(enable: Boolean) {
        // todo. New Google billing does not have debug logs.
    }

    /**
     * Called by the Billing Library when new purchases are detected.
     */
    override fun onPurchasesUpdated(billingResult: BillingResult?, purchases: List<Purchase>?) {
        if (billingResult == null) {
            Log.wtf(TAG, "onSkuDetailsResponse: null BillingResult")
            return
        }

        val responseCode = billingResult.responseCode
        val debugMessage = billingResult.debugMessage
        Log.d(TAG, "onPurchasesUpdated: responseCode:$responseCode debugMessage: $debugMessage")
        when (responseCode) {
            BillingClient.BillingResponseCode.OK -> {
                Log.d(TAG, "onPurchasesUpdated. purchase: $purchases")
                processPurchases(purchases)
            }
            BillingClient.BillingResponseCode.USER_CANCELED -> Log.i(TAG, "onPurchasesUpdated: User canceled the purchase")
            BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED -> Log.i(TAG, "onPurchasesUpdated: The user already owns this item")
            BillingClient.BillingResponseCode.DEVELOPER_ERROR -> Log.e(TAG, "onPurchasesUpdated: Developer error means that Google Play " +
                    "does not recognize the configuration. If you are just getting started, " +
                    "make sure you have configured the application correctly in the " +
                    "Google Play Console. The SKU product ID must match and the APK you " +
                    "are using must be signed with release keys."
            )
        }
    }

    private fun processPurchases(purchasesList: List<Purchase>?, isRestore: Boolean = false) {
        if (purchasesList != null) {
            Log.d(TAG, "processPurchases: " + purchasesList.size + " purchase(s)")
            purchases@ for (purchase in purchasesList) {
                if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED && purchase.sku.isSkuReady()) {
                    if (!isSignatureValid(purchase)) {
                        Log.d(TAG, "processPurchases. Signature is not valid for: $purchase")
                        continue@purchases
                    }

                    // Grant entitlement to the user.
                    val skuDetails = skusDetails[purchase.sku]
                    when (skuDetails?.type) {
                        BillingClient.SkuType.INAPP -> {
                            productOwned(purchase.sku, isRestore)
                        }
                        BillingClient.SkuType.SUBS -> {
                            subscriptionOwned(purchase.sku, isRestore)
                        }
                    }

                    // Acknowledge the purchase if it hasn't already been acknowledged.
                    if (!purchase.isAcknowledged) {
                        val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
                                .setPurchaseToken(purchase.purchaseToken).build()
                        mBillingClient.acknowledgePurchase(acknowledgePurchaseParams, this)
                    }
                } else {
                    Log.e(TAG, "processPurchases failed. purchase: $purchase " +
                            "purchaseState: ${purchase.purchaseState} isSkuReady: ${purchase.sku.isSkuReady()}")
                }
            }
        } else {
            Log.d(TAG, "processPurchases: with no purchases")
        }
    }

    private fun isSignatureValid(purchase: Purchase): Boolean {
        return Security.verifyPurchase(decodedKey, purchase.originalJson, purchase.signature)
    }

    /**
     * Get Sku details by sku and type.
     * This method has cache functionality.
     */
    private fun String.toSkuDetails(type: String, done: (skuDetails: SkuDetails?) -> Unit = {}) {
        if (::mBillingClient.isInitialized.not() || !mBillingClient.isReady) {
            Log.w(TAG, "buy. Google billing service is not ready yet.")
            done(null)
            return
        }

        val skuDetailsCached = skusDetails[this]
        if (skuDetailsCached != null) {
            done(skuDetailsCached)
            return
        }

        val params = SkuDetailsParams.newBuilder()
        params.setSkusList(listOf(this)).setType(type)

        mBillingClient.querySkuDetailsAsync(params.build()) { billingResult, skuDetailsList ->
            if (billingResult.isOk()) {
                val skuDetails: SkuDetails? = skuDetailsList?.find { it.sku == this }
                skusDetails[this] = skuDetails
                done(skuDetails)
            } else {
                Log.w(TAG, "launchBillingFlow. Failed to get details for sku: $this")
                done(null)
            }
        }
    }

    private fun String.isSkuReady(): Boolean {
        return skusDetails.containsKey(this) && skusDetails[this] != null
    }

    override fun onBillingServiceDisconnected() {
        Log.d(TAG, "onBillingServiceDisconnected")
    }

    override fun onAcknowledgePurchaseResponse(billingResult: BillingResult) {
        Log.d(TAG, "onAcknowledgePurchaseResponse: billingResult: $billingResult")
    }

    private fun decodeKey(key: String): String {
        val stringBuilder = StringBuilder()
        for (element in key) {
            var c = element
            if (c in 'A'..'M' || c in 'a'..'m') {
                c += 13
            } else if (c in 'N'..'Z' || c in 'n'..'z') {
                c -= 13
            }
            stringBuilder.append(c)
        }
        return stringBuilder.toString()
    }

    private fun BillingResult.isOk(): Boolean {
        return this.responseCode == BillingClient.BillingResponseCode.OK
    }

    companion object {
        const val TAG = "GoogleBillingService2"
    }
}