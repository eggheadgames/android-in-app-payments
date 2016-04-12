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

    public void productOwned(String sku) {
        Iterator<BillingServiceListener> iterator = billingServiceListeners.iterator();
        while (iterator.hasNext()) {
            BillingServiceListener billingServiceListener = iterator.next();
            billingServiceListener.onProductOwned(sku);
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

    public abstract void close();
}
