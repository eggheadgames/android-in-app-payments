package com.billing;

import java.util.Map;

public interface BillingServiceListener {

    /**
     * Callback will be triggered upon obtaining information about product prices
     *
     * @param iapKeyPrices - a map with available products
     */
    void onPricesUpdated(Map<String, String> iapKeyPrices);

    /**
     * Callback will be triggered when a product purchased successfully
     *
     * @param sku - specificator of owned product
     */
    void onProductPurchased(String sku);

    /**
     * Callback will be triggered upon owned products restore
     *
     * @param sku - specificator of owned product
     */
    void onProductRestored(String sku);

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
