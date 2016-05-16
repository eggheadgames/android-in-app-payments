package com.billing;

public interface SubscriptionServiceListener extends BillingServiceListener{

    /**
     * Callback will be triggered upon owned subscription restore
     *
     * @param sku - specificator of owned subscription
     */
    void onSubscriptionRestored(String sku);

    /**
     * Callback will be triggered when a subscription purchased successfully
     *
     * @param sku - specificator of purchased subscription
     */
    void onSubscriptionPurchased(String sku);
}
