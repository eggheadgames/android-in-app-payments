package com.billing.google

import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import com.android.billingclient.api.*
import com.billing.BillingService
import java.util.*

class GoogleBillingService2(context: Context?, iapkeys: List<String?>?)
    : BillingService(), PurchasesUpdatedListener, BillingClientStateListener, AcknowledgePurchaseResponseListener {

    private val mBillingClient: BillingClient

    override fun init(key: String) {
        val decodedKey = decodeKey(key)
        mBillingClient.startConnection(this)
    }

    override fun buy(activity: Activity, sku: String, id: Int) {
        launchBillingFlow(activity, sku, BillingClient.SkuType.INAPP)
    }

    override fun subscribe(activity: Activity, sku: String, id: Int) {
        launchBillingFlow(activity, sku, BillingClient.SkuType.SUBS)
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

    override fun enableDebugLogging(enable: Boolean) {}
    private fun launchBillingFlow(activity: Activity, sku: String, type: String) {
        val skuList: MutableList<String> = ArrayList()
        skuList.add(sku)
        val params = SkuDetailsParams.newBuilder()
        params.setSkusList(skuList).setType(type)
        mBillingClient.querySkuDetailsAsync(params.build()) { billingResult, skuDetailsList ->
            if (skuDetailsList.size == 1) {
                val skuDetails = skuDetailsList[0]
                val purchaseParams = BillingFlowParams.newBuilder()
                        .setSkuDetails(skuDetails).build()
                mBillingClient.launchBillingFlow(activity, purchaseParams)
            } else {
                // todo we need to filter list and find needed sku
            }
        }
    }

    /**
     * Called by the Billing Library when new purchases are detected.
     */
    override fun onPurchasesUpdated(billingResult: BillingResult, purchases: List<Purchase>?) {
        if (billingResult == null) {
            Log.wtf(ContentValues.TAG, "onPurchasesUpdated: null BillingResult")
            return
        }
        val responseCode = billingResult.responseCode
        val debugMessage = billingResult.debugMessage
        Log.d(ContentValues.TAG, "onPurchasesUpdated: responseCode:$responseCode debugMessage: $debugMessage")
        when (responseCode) {
            BillingClient.BillingResponseCode.OK -> if (purchases == null) {
                Log.d(ContentValues.TAG, "onPurchasesUpdated: null purchase list")
                processPurchases(null)
            } else {
                processPurchases(purchases)
            }
            BillingClient.BillingResponseCode.USER_CANCELED -> Log.i(ContentValues.TAG, "onPurchasesUpdated: User canceled the purchase")
            BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED -> Log.i(ContentValues.TAG, "onPurchasesUpdated: The user already owns this item")
            BillingClient.BillingResponseCode.DEVELOPER_ERROR -> Log.e(ContentValues.TAG, "onPurchasesUpdated: Developer error means that Google Play " +
                    "does not recognize the configuration. If you are just getting started, " +
                    "make sure you have configured the application correctly in the " +
                    "Google Play Console. The SKU product ID must match and the APK you " +
                    "are using must be signed with release keys."
            )
        }
    }

    private fun processPurchases(purchasesList: List<Purchase>?) {
        if (purchasesList != null) {
            Log.d(ContentValues.TAG, "processPurchases: " + purchasesList.size + " purchase(s)")
            for (purchase in purchasesList) {
                if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
                    // Grant entitlement to the user.
                    subscriptionOwned(purchase.sku, false)

                    // Acknowledge the purchase if it hasn't already been acknowledged.
                    if (!purchase.isAcknowledged) {
                        val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
                                .setPurchaseToken(purchase.purchaseToken).build()
                        mBillingClient.acknowledgePurchase(acknowledgePurchaseParams, this)
                    }
                }
            }
        } else {
            Log.d(ContentValues.TAG, "processPurchases: with no purchases")
        }
    }

    override fun onBillingSetupFinished(billingResult: BillingResult) {
        Log.d(ContentValues.TAG, "onBillingSetupFinished: billingResult: $billingResult")
    }

    override fun onBillingServiceDisconnected() {
        Log.d(ContentValues.TAG, "onBillingServiceDisconnected")
    }

    override fun onAcknowledgePurchaseResponse(billingResult: BillingResult) {
        Log.d(ContentValues.TAG, "onAcknowledgePurchaseResponse: billingResult: $billingResult")
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

    init {
        mBillingClient = BillingClient.newBuilder(context!!).setListener(this).build()
    }
}