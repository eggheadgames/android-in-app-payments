package com.eggheadgames.inapppayments;

import android.content.Context;

import com.billing.PurchaseServiceListener;
import com.billing.SubscriptionServiceListener;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@RunWith(MockitoJUnitRunner.class)
public class BillingTest {

    @Mock
    Context context;

    @Test
    public void onProductOwnedEvent_eachRegisteredListenerShouldBeTriggered() throws Exception {
        IAPManager.build(context, IAPManager.BUILD_TARGET_GOOGLE, new ArrayList<String>(), new ArrayList<String>());

        PurchaseServiceListener firstListener = Mockito.spy(PurchaseServiceListener.class);
        IAPManager.addPurchaseListener(firstListener);

        PurchaseServiceListener secondListener = Mockito.spy(PurchaseServiceListener.class);
        IAPManager.addPurchaseListener(secondListener);

        IAPManager.getBillingService().productOwned(TestConstants.TEST_SKU, false);

        Mockito.verify(firstListener, Mockito.times(1)).onProductPurchased(TestConstants.TEST_SKU);
        Mockito.verify(secondListener, Mockito.times(1)).onProductPurchased(TestConstants.TEST_SKU);
    }

    @Test
    public void onProductDetailsFetched_eachRegisteredListenerShouldBeTriggered() {
        IAPManager.build(context, IAPManager.BUILD_TARGET_GOOGLE, new ArrayList<String>(), new ArrayList<String>());

        PurchaseServiceListener firstListener = Mockito.spy(PurchaseServiceListener.class);
        IAPManager.addPurchaseListener(firstListener);

        PurchaseServiceListener secondListener = Mockito.spy(PurchaseServiceListener.class);
        IAPManager.addPurchaseListener(secondListener);

        Map<String, String> products = new HashMap<>();
        products.put(TestConstants.TEST_SKU, TestConstants.TEST_PRODUCT);
        products.put(TestConstants.TEST_SKU_1, TestConstants.TEST_PRODUCT);

        IAPManager.getBillingService().updatePrices(products);

        Mockito.verify(firstListener, Mockito.times(1)).onPricesUpdated(products);
        Mockito.verify(secondListener, Mockito.times(1)).onPricesUpdated(products);
    }

    @Test
    public void onIapManagerInteraction_onlyRegisteredListenersShouldBeTriggered() {
        IAPManager.build(context, IAPManager.BUILD_TARGET_GOOGLE, new ArrayList<String>(), new ArrayList<String>());

        PurchaseServiceListener firstListener = Mockito.spy(PurchaseServiceListener.class);
        IAPManager.addPurchaseListener(firstListener);

        PurchaseServiceListener secondListener = Mockito.spy(PurchaseServiceListener.class);
        IAPManager.addPurchaseListener(secondListener);

        IAPManager.removePurchaseListener(firstListener);

        IAPManager.getBillingService().productOwned(TestConstants.TEST_SKU, false);

        Mockito.verify(firstListener, Mockito.never()).onProductPurchased(Mockito.anyString());
        Mockito.verify(secondListener, Mockito.times(1)).onProductPurchased(TestConstants.TEST_SKU);

    }

    @Test
    public void onProductPurchase_purchaseCallbackShouldBeTriggered() {
        IAPManager.build(context, IAPManager.BUILD_TARGET_GOOGLE, new ArrayList<String>(), new ArrayList<String>());

        PurchaseServiceListener listener = Mockito.spy(PurchaseServiceListener.class);
        IAPManager.addPurchaseListener(listener);

        IAPManager.getBillingService().productOwned(TestConstants.TEST_SKU, false);

        Mockito.verify(listener, Mockito.times(1)).onProductPurchased(TestConstants.TEST_SKU);
    }

    @Test
    public void onProductRestore_restoreCallbackShouldBeTriggered() {
        IAPManager.build(context, IAPManager.BUILD_TARGET_GOOGLE, new ArrayList<String>(), new ArrayList<String>());

        PurchaseServiceListener listener = Mockito.spy(PurchaseServiceListener.class);
        IAPManager.addPurchaseListener(listener);

        IAPManager.getBillingService().productOwned(TestConstants.TEST_SKU, true);

        Mockito.verify(listener, Mockito.times(1)).onProductRestored(TestConstants.TEST_SKU);
    }

    @Test
    public void onSubscriptionPurchase_purchaseCallbackShouldBeTriggered() {
        IAPManager.build(context, IAPManager.BUILD_TARGET_GOOGLE, new ArrayList<String>(), new ArrayList<String>());

        SubscriptionServiceListener listener = Mockito.spy(SubscriptionServiceListener.class);
        IAPManager.addSubscriptionListener(listener);

        IAPManager.getBillingService().subscriptionOwned(TestConstants.TEST_SKU, false);

        Mockito.verify(listener, Mockito.times(1)).onSubscriptionPurchased(TestConstants.TEST_SKU);
    }

    @Test
    public void onSubscriptionRestore_restoreCallbackShouldBeTriggered() {
        IAPManager.build(context, IAPManager.BUILD_TARGET_GOOGLE, new ArrayList<String>(), new ArrayList<String>());

        SubscriptionServiceListener listener = Mockito.spy(SubscriptionServiceListener.class);
        IAPManager.addSubscriptionListener(listener);

        IAPManager.getBillingService().subscriptionOwned(TestConstants.TEST_SKU, true);

        Mockito.verify(listener, Mockito.times(1)).onSubscriptionRestored(TestConstants.TEST_SKU);
    }

}