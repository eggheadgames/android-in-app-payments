package com.billing.google;

import com.android.vending.billing.IabHelper;
import com.android.vending.billing.IabResult;
import com.android.vending.billing.Inventory;
import com.android.vending.billing.Purchase;
import com.android.vending.billing.SkuDetails;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class GoogleBillingListener implements IabHelper.OnIabSetupFinishedListener,
        IabHelper.QueryInventoryFinishedListener, IabHelper.OnIabPurchaseFinishedListener {

    private IabHelper iap;
    private GoogleBillingService googleBillingService;

    public GoogleBillingListener(IabHelper iap, GoogleBillingService googleBillingService) {
        this.iap = iap;
        this.googleBillingService = googleBillingService;
    }

    @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
    @Override
    public void onIabPurchaseFinished(IabResult result, Purchase info) {
        try {
            if (result != null && info != null &&
                    (result.isSuccess() || result.getResponse() == IabHelper.BILLING_RESPONSE_RESULT_ITEM_ALREADY_OWNED)) {
                //We assume that any problems will have become obvious through the Play store, so no need to do anything
                //if the purchase has failed.
                if (info.getItemType().equals(IabHelper.ITEM_TYPE_INAPP)) {
                    googleBillingService.productOwned(info.getSku(), false);
                } else {
                    googleBillingService.subscriptionOwned(info.getSku(), false);
                }
            } else if (result != null && result.getResponse() == IabHelper.BILLING_RESPONSE_RESULT_ITEM_ALREADY_OWNED) {
                if (googleBillingService.isProductPurchaseRequested()) {
                    googleBillingService.productOwned(googleBillingService.getLastRequestedSku(), false);
                } else {
                    googleBillingService.subscriptionOwned(googleBillingService.getLastRequestedSku(), false);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void onIabSetupFinished(IabResult result) {
        try {
            if (result != null && result.isSuccess()) {
                //If we're successfully talking to Google, next step is to get a list of already purchased items.
                iap.queryInventoryAsync(true, googleBillingService.iapkeys, this);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    @Override
    public void onQueryInventoryFinished(IabResult result, final Inventory inv) {
        try {
            if (result != null && inv != null && result.isSuccess()) {
                ArrayList<String> owned = (ArrayList<String>) inv.getAllOwnedSkus();
                for (int i = 0; i < owned.size(); i++) {
                    //The customer owns this product. Update the local data to reflect that if necessary.
                    String sku = owned.get(i);
                    SkuDetails skuDetails = inv.getSkuDetails(sku);
                    if (skuDetails != null && skuDetails.getType().equals(IabHelper.ITEM_TYPE_SUBS)) {
                        googleBillingService.subscriptionOwned(sku, true);
                    } else {
                        googleBillingService.productOwned(sku, true);
                    }
                }

                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        List<String> iapkeys = googleBillingService.iapkeys;
                        Map<String, String> iapkeyPrices = new HashMap<>(iapkeys.size());
                        for (int i = 0, n = iapkeys.size(); i < n; i++) {
                            String sku = iapkeys.get(i);
                            if (inv.hasDetails(sku)) {
                                iapkeyPrices.put(sku, inv.getSkuDetails(sku).getPrice());
                            }
                        }
                        googleBillingService.updatePrices(iapkeyPrices);
                    }
                });
                thread.start();

            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
