package com.billing;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.CallSuper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public abstract class BillingService {
    protected Context context;

    private List<PurchaseServiceListener> purchaseServiceListeners;
    private List<SubscriptionServiceListener> subscriptionServiceListeners;

    @SuppressWarnings("WeakerAccess")
    public BillingService() {
        purchaseServiceListeners = new ArrayList<>();
        subscriptionServiceListeners = new ArrayList<>();
    }

    public void addPurchaseListener(PurchaseServiceListener purchaseServiceListener) {
        purchaseServiceListeners.add(purchaseServiceListener);
    }

    public void removePurchaseListener(PurchaseServiceListener purchaseServiceListener) {
        purchaseServiceListeners.remove(purchaseServiceListener);
    }

    public void addSubscriptionListener(SubscriptionServiceListener subscriptionServiceListener) {
        subscriptionServiceListeners.add(subscriptionServiceListener);
    }

    public void removeSubscriptionListener(SubscriptionServiceListener subscriptionServiceListener) {
        subscriptionServiceListeners.remove(subscriptionServiceListener);
    }

    /**
     * @param sku       - product specificator
     * @param isRestore - a flag indicating whether it's a fresh purchase or restored product
     */
    public void productOwned(String sku, boolean isRestore) {
        for (PurchaseServiceListener purchaseServiceListener : purchaseServiceListeners) {
            if (isRestore) {
                purchaseServiceListener.onProductRestored(sku);
            } else {
                purchaseServiceListener.onProductPurchased(sku);
            }
        }
    }

    /**
     * @param sku       - subscription specificator
     * @param isRestore - a flag indicating whether it's a fresh purchase or restored subscription
     */
    public void subscriptionOwned(String sku, boolean isRestore) {
        for (SubscriptionServiceListener subscriptionServiceListener : subscriptionServiceListeners) {
            if (isRestore) {
                subscriptionServiceListener.onSubscriptionRestored(sku);
            } else {
                subscriptionServiceListener.onSubscriptionPurchased(sku);
            }
        }
    }

    public void updatePrices(Map<String, String> iapkeyPrices) {
        for (BillingServiceListener billingServiceListener : purchaseServiceListeners) {
            billingServiceListener.onPricesUpdated(iapkeyPrices);
        }

        for (BillingServiceListener billingServiceListener : subscriptionServiceListeners) {
            billingServiceListener.onPricesUpdated(iapkeyPrices);
        }
    }

    public abstract void init(String key);

    public abstract void buy(Activity activity, String sku, int id);

    public abstract void subscribe(Activity activity, String sku, int id);

    public abstract void unsubscribe(Activity activity, String sku, int id);

    @CallSuper
    public void close() {
        context = null;
        subscriptionServiceListeners.clear();
        purchaseServiceListeners.clear();
    }
}
