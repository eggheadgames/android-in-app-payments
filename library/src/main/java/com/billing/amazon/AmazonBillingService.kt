package com.billing.amazon

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import com.amazon.device.iap.PurchasingService
import com.billing.BillingService
import java.util.*

class AmazonBillingService(val context: Context, private val iapkeys: List<String>) : BillingService() {

    private var mAmazonBillingListener: AmazonBillingListener? = null

    override fun init(key: String) {
        mAmazonBillingListener = AmazonBillingListener(this)
        PurchasingService.registerListener(context, mAmazonBillingListener)
        val productSkus: Set<String> = HashSet(iapkeys)
        PurchasingService.getProductData(productSkus)
        PurchasingService.getPurchaseUpdates(true)
    }

    override fun buy(activity: Activity, sku: String, id: Int) {
        PurchasingService.purchase(sku)
    }

    override fun subscribe(activity: Activity, sku: String, id: Int) {
        PurchasingService.purchase(sku)
    }

    override fun unsubscribe(activity: Activity, sku: String, id: Int) {
        try {
            val intent = Intent()
            intent.action = Intent.ACTION_VIEW
            intent.data = Uri.parse("https://www.amazon.com/gp/mas/your-account/myapps/yoursubscriptions/ref=mas_ya_subs")
            activity.startActivity(intent)
            activity.finish()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun enableDebugLogging(enable: Boolean) {
        mAmazonBillingListener?.enableDebugLogging(enable)
    }

}