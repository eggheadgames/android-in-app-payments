package com.billing.amazon;

import android.util.Log;

import com.amazon.device.iap.PurchasingListener;
import com.amazon.device.iap.PurchasingService;
import com.amazon.device.iap.model.FulfillmentResult;
import com.amazon.device.iap.model.Product;
import com.amazon.device.iap.model.ProductDataResponse;
import com.amazon.device.iap.model.ProductType;
import com.amazon.device.iap.model.PurchaseResponse;
import com.amazon.device.iap.model.PurchaseUpdatesResponse;
import com.amazon.device.iap.model.Receipt;
import com.amazon.device.iap.model.UserDataResponse;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

class AmazonBillingListener implements PurchasingListener {

    private static final String TAG = AmazonBillingListener.class.getSimpleName();
    private AmazonBillingService amazonBillingService;
    boolean mDebugLog = false;

    AmazonBillingListener(AmazonBillingService amazonBillingService) {
        this.amazonBillingService = amazonBillingService;
        logDebug("IS_SANDBOX_MODE:" + PurchasingService.IS_SANDBOX_MODE);
    }

    @Override
    public void onUserDataResponse(UserDataResponse userDataResponse) {
        logDebug("onUserDataResponse " + userDataResponse.getRequestStatus());
    }

    public void enableDebugLogging(boolean enable) {
        mDebugLog = enable;
    }

    @Override
    public void onProductDataResponse(ProductDataResponse response) {
        final ProductDataResponse.RequestStatus status = response.getRequestStatus();
        logDebug("onProductDataResponse: RequestStatus (" + status + ")");

        switch (status) {
            case SUCCESSFUL:
                logDebug("onProductDataResponse: successful.  The item data map in this response includes the valid SKUs");
                final Set<String> unavailableSkus = response.getUnavailableSkus();
                logDebug("onProductDataResponse: " + unavailableSkus.size() + " unavailable skus");
                Map<String, Product> productData = response.getProductData();
                logDebug("onProductDataResponse productData : " + productData.size());
                Map<String, String> iapkeyPrices = new HashMap<>();
                for (Map.Entry<String, Product> entry : productData.entrySet()) {
                    Product product = productData.get(entry.getKey());
                    iapkeyPrices.put(product.getSku(), product.getPrice());
                }
                amazonBillingService.updatePrices(iapkeyPrices);
                break;
            case FAILED:
            case NOT_SUPPORTED:
                logDebug("onProductDataResponse: failed, should retry request");
                break;
        }

    }

    @Override
    public void onPurchaseResponse(PurchaseResponse response) {
        logDebug("onPurchaseResponse " + response.getRequestStatus());
        final String requestId = response.getRequestId().toString();
        final String userId = response.getUserData().getUserId();
        final PurchaseResponse.RequestStatus status = response.getRequestStatus();
        final Receipt receipt;
        logDebug("onPurchaseResponse: requestId (" + requestId + ") userId (" + userId + ") purchaseRequestStatus (" + status + ")");
        switch (status) {
            case SUCCESSFUL:
                receipt = response.getReceipt();
                if (receipt != null) {
                    logDebug("onPurchaseResponse: receipt json:" + receipt.toJSON());
                    logDebug("onPurchaseResponse: getUserId:" + response.getUserData().getUserId());
                    logDebug("onPurchaseResponse: getMarketplace:" + response.getUserData().getMarketplace());

                    if (receipt.getProductType() == ProductType.SUBSCRIPTION) {
                        amazonBillingService.subscriptionOwned(receipt.getSku(), false);
                    } else {
                        amazonBillingService.productOwned(receipt.getSku(), false);
                    }

                    PurchasingService.notifyFulfillment(receipt.getReceiptId(), FulfillmentResult.FULFILLED);
                }
                break;
            case ALREADY_PURCHASED:
                logDebug("onPurchaseResponse: already purchased, you should verify the entitlement purchase on your side and make sure the purchase was granted to customer");
                receipt = response.getReceipt();
                if (receipt != null && !receipt.isCanceled()) {
                    if (receipt.getProductType() == ProductType.SUBSCRIPTION) {
                        amazonBillingService.subscriptionOwned(receipt.getSku(), true);
                    } else {
                        amazonBillingService.productOwned(receipt.getSku(), true);
                    }
                }
                break;
            case INVALID_SKU:
                logDebug("onPurchaseResponse: invalid SKU!  onProductDataResponse should have disabled buy button already.");
                break;
            case FAILED:
            case NOT_SUPPORTED:
                logDebug("onPurchaseResponse: failed so remove purchase request from local storage");
                break;
        }
    }

    @SuppressWarnings({"ConstantConditions", "ToArrayCallWithZeroLengthArrayArgument"})
    @Override
    public void onPurchaseUpdatesResponse(PurchaseUpdatesResponse response) {
        logDebug("onPurchaseUpdatesResponse " + response.getRequestStatus());
        if (response == null)
            return;
        if (response.getRequestStatus() == PurchaseUpdatesResponse.RequestStatus.SUCCESSFUL) {
            Receipt[] receipts = response.getReceipts().toArray(new Receipt[0]);
            for (Receipt receipt : receipts) {
                if (receipt != null && !receipt.isCanceled()) {
                    if (receipt.getProductType() == ProductType.ENTITLED) {
                        amazonBillingService.productOwned(receipt.getSku(), true);
                        logDebug("onPurchaseUpdatesResponse productOwned: " + receipt.getSku());
                    } else if (receipt.getProductType() == ProductType.SUBSCRIPTION) {
                        amazonBillingService.subscriptionOwned(receipt.getSku(), true);
                        logDebug("onPurchaseUpdatesResponse subscriptionOwned: " + receipt.getSku());
                    }
                }
            }
        }
    }

    private void logDebug(String msg) {
        if (mDebugLog) Log.d(TAG, msg);
    }
}
