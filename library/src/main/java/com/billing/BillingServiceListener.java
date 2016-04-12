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
     * Callback will be triggered when a product purchased successfully,
     * OR upon owned products restore
     *
     * @param sku - specificator of owned product
     */
    void onProductOwned(String sku);
}
