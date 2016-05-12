package com.billing;

import android.app.Activity;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

public abstract class BillingService {

    private ArrayList<BillingServiceListener> billingServiceListeners;

    @SuppressWarnings("WeakerAccess")
    public BillingService() {
        billingServiceListeners = new ArrayList<>();
    }

    public void addListener(BillingServiceListener billingServiceListener) {
        billingServiceListeners.add(billingServiceListener);
    }

    public void removeListener(BillingServiceListener billingServiceListener) {
        billingServiceListeners.remove(billingServiceListener);
    }

    /**
     * @param sku       - product specificator
     * @param isRestore - a flag indicating whether it's a fresh purchase or restored product
     */
    public void productOwned(String sku, boolean isRestore) {
        Iterator<BillingServiceListener> iterator = billingServiceListeners.iterator();
        while (iterator.hasNext()) {
            BillingServiceListener billingServiceListener = iterator.next();
            if (isRestore) {
                billingServiceListener.onProductRestored(sku);
            } else {
                billingServiceListener.onProductPurchased(sku);
            }
        }
    }

    /**
     * @param sku       - subscription specificator
     * @param isRestore - a flag indicating whether it's a fresh purchase or restored subscription
     */
    public void subscriptionOwned(String sku, boolean isRestore) {
        Iterator<BillingServiceListener> iterator = billingServiceListeners.iterator();
        while (iterator.hasNext()) {
            BillingServiceListener billingServiceListener = iterator.next();
            if (isRestore) {
                billingServiceListener.onSubscriptionRestored(sku);
            } else {
                billingServiceListener.onSubscriptionPurchased(sku);
            }
        }
    }

    public void updatePrices(Map<String, String> iapkeyPrices) {
        Iterator<BillingServiceListener> iterator = billingServiceListeners.iterator();
        while (iterator.hasNext()) {
            BillingServiceListener billingServiceListener = iterator.next();
            billingServiceListener.onPricesUpdated(iapkeyPrices);
        }
    }

    public abstract void init(String key);

    public abstract void buy(Activity activity, String sku, int id);

    public abstract void subscribe(Activity activity, String sku, int id);

    public abstract void unsubscribe(Activity activity, String sku, int id);

    public abstract void close();
}
