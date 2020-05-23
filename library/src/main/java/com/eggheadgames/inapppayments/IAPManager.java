package com.eggheadgames.inapppayments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;

import com.billing.BillingService;
import com.billing.PurchaseServiceListener;
import com.billing.SubscriptionServiceListener;
import com.billing.amazon.AmazonBillingService;
import com.billing.google.GoogleBillingService2;

import java.util.ArrayList;
import java.util.List;

//Public front-end for IAP functionality. 

public class IAPManager {

    public static final int BUILD_TARGET_GOOGLE = 0;
    public static final int BUILD_TARGET_AMAZON = 1;

    @SuppressLint("StaticFieldLeak")
    private static BillingService billingService;

    /**
     * @param context          - application context
     * @param buildTarget      - IAPManager.BUILD_TARGET_GOOGLE or IAPManager.BUILD_TARGET_AMAZON
     * @param iapKeys          - list of sku for purchases
     * @param subscriptionKeys - list of sku for subscriptions
     */
    public static void build(Context context, int buildTarget, List<String> iapKeys, List<String> subscriptionKeys) {
        Context applicationContext = context.getApplicationContext();
        Context contextLocal = applicationContext == null ? context : applicationContext;

        //Build-specific initializations
        if (buildTarget == BUILD_TARGET_GOOGLE) {
            billingService = new GoogleBillingService2(contextLocal, iapKeys, subscriptionKeys);
        } else if (buildTarget == BUILD_TARGET_AMAZON) {
            List<String> keys = new ArrayList<>();
            keys.addAll(iapKeys);
            keys.addAll(subscriptionKeys);
            billingService = new AmazonBillingService(contextLocal, keys);
        }
    }

    public static void init(String key, boolean enableLogging) {
        billingService.init(key);
        billingService.enableDebugLogging(enableLogging);
    }

    public static void addPurchaseListener(PurchaseServiceListener purchaseServiceListener) {
        billingService.addPurchaseListener(purchaseServiceListener);
    }

    public static void removePurchaseListener(PurchaseServiceListener purchaseServiceListener) {
        billingService.removePurchaseListener(purchaseServiceListener);
    }

    public static void addSubscriptionListener(SubscriptionServiceListener subscriptionServiceListener) {
        billingService.addSubscriptionListener(subscriptionServiceListener);
    }

    public static void removeSubscriptionListener(SubscriptionServiceListener subscriptionServiceListener) {
        billingService.removeSubscriptionListener(subscriptionServiceListener);
    }

    public static void buy(Activity activity, String sku, int id) {
        billingService.buy(activity, sku, id);
    }

    public static void subscribe(Activity activity, String sku, int id) {
        billingService.subscribe(activity, sku, id);
    }

    public static void unsubscribe(Activity activity, String sku, int id) {
        billingService.unsubscribe(activity, sku, id);
    }

    public static void destroy() {
        billingService.close();
    }

    public static BillingService getBillingService() {
        return billingService;
    }
}
