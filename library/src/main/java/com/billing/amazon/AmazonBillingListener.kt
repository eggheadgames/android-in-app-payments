package com.billing.amazon

import android.util.Log
import com.amazon.device.iap.PurchasingListener
import com.amazon.device.iap.PurchasingService
import com.amazon.device.iap.model.*
import java.util.*

internal class AmazonBillingListener(private val amazonBillingService: AmazonBillingService) : PurchasingListener {

    var mDebugLog = false

    override fun onUserDataResponse(userDataResponse: UserDataResponse) {
        logDebug("onUserDataResponse " + userDataResponse.requestStatus)
    }

    fun enableDebugLogging(enable: Boolean) {
        mDebugLog = enable
    }

    override fun onProductDataResponse(response: ProductDataResponse) {
        val status = response.requestStatus
        logDebug("onProductDataResponse: RequestStatus ($status)")
        when (status) {
            ProductDataResponse.RequestStatus.SUCCESSFUL -> {
                logDebug("onProductDataResponse: successful.  The item data map in this response includes the valid SKUs")
                val unavailableSkus = response.unavailableSkus
                logDebug("onProductDataResponse: " + unavailableSkus.size + " unavailable skus")
                val productData = response.productData
                logDebug("onProductDataResponse productData : " + productData.size)
                val iapkeyPrices: MutableMap<String, String> = HashMap()
                for ((key) in productData) {
                    val product = productData[key]
                    iapkeyPrices[product!!.sku] = product.price
                }
                amazonBillingService.updatePrices(iapkeyPrices)
            }
            ProductDataResponse.RequestStatus.FAILED, ProductDataResponse.RequestStatus.NOT_SUPPORTED -> logDebug("onProductDataResponse: failed, should retry request")
        }
    }

    override fun onPurchaseResponse(response: PurchaseResponse) {
        logDebug("onPurchaseResponse " + response.requestStatus)
        val requestId = response.requestId.toString()
        val userId = response.userData.userId
        val status = response.requestStatus
        val receipt: Receipt?
        logDebug("onPurchaseResponse: requestId ($requestId) userId ($userId) purchaseRequestStatus ($status)")
        when (status) {
            PurchaseResponse.RequestStatus.SUCCESSFUL -> {
                receipt = response.receipt
                if (receipt != null) {
                    logDebug("onPurchaseResponse: receipt json:" + receipt.toJSON())
                    logDebug("onPurchaseResponse: getUserId:" + response.userData.userId)
                    logDebug("onPurchaseResponse: getMarketplace:" + response.userData.marketplace)
                    if (receipt.productType == ProductType.SUBSCRIPTION) {
                        amazonBillingService.subscriptionOwned(receipt.sku, false)
                    } else {
                        amazonBillingService.productOwned(receipt.sku, false)
                    }
                    PurchasingService.notifyFulfillment(receipt.receiptId, FulfillmentResult.FULFILLED)
                }
            }
            PurchaseResponse.RequestStatus.ALREADY_PURCHASED -> {
                logDebug("onPurchaseResponse: already purchased, you should verify the entitlement purchase on your side and make sure the purchase was granted to customer")
                receipt = response.receipt
                if (receipt != null && !receipt.isCanceled) {
                    if (receipt.productType == ProductType.SUBSCRIPTION) {
                        amazonBillingService.subscriptionOwned(receipt.sku, true)
                    } else {
                        amazonBillingService.productOwned(receipt.sku, true)
                    }
                }
            }
            PurchaseResponse.RequestStatus.INVALID_SKU -> logDebug("onPurchaseResponse: invalid SKU!  onProductDataResponse should have disabled buy button already.")
            PurchaseResponse.RequestStatus.FAILED, PurchaseResponse.RequestStatus.NOT_SUPPORTED -> logDebug("onPurchaseResponse: failed so remove purchase request from local storage")
        }
    }

    override fun onPurchaseUpdatesResponse(response: PurchaseUpdatesResponse?) {
        logDebug("onPurchaseUpdatesResponse " + if (response == null) "null" else response.requestStatus)
        if (response == null) return
        if (response.requestStatus == PurchaseUpdatesResponse.RequestStatus.SUCCESSFUL) {
            val receipts = response.receipts.toTypedArray()
            for (receipt in receipts) {
                if (receipt != null && !receipt.isCanceled) {
                    if (receipt.productType == ProductType.ENTITLED) {
                        amazonBillingService.productOwned(receipt.sku, true)
                        logDebug("onPurchaseUpdatesResponse productOwned: " + receipt.sku)
                    } else if (receipt.productType == ProductType.SUBSCRIPTION) {
                        amazonBillingService.subscriptionOwned(receipt.sku, true)
                        logDebug("onPurchaseUpdatesResponse subscriptionOwned: " + receipt.sku)
                    }
                }
            }
        }
    }

    private fun logDebug(msg: String) {
        if (mDebugLog) Log.d(TAG, msg)
    }

    companion object {
        private val TAG = AmazonBillingListener::class.java.simpleName
    }

    init {
        logDebug("IS_SANDBOX_MODE:" + PurchasingService.IS_SANDBOX_MODE)
    }
}