package com.billing;

import java.util.Map;

interface BillingServiceListener {

    /**
     * Callback will be triggered upon obtaining information about product prices
     *
     * @param iapKeyPrices - a map with available products
     */
    void onPricesUpdated(Map<String, String> iapKeyPrices);
}
