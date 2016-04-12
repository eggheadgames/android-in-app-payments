package com.billing.google;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import com.android.vending.billing.IabHelper;
import com.billing.BillingService;

import java.util.List;

public class GoogleBillingService extends BillingService {

    private IabHelper iap;
    public List<String> iapkeys;
    private Context context;
    private GoogleBillingListener googleBillingListener;

    public GoogleBillingService(Context context, List<String> iapkeys) {
        super();
        this.context = context;
        this.iapkeys = iapkeys;
    }

    public void purchaseCallback(int requestCode, int resultCode, Intent data) {
        try {
            iap.handleActivityResult(requestCode, resultCode, data);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public void init(String key) {
        //We rebuild the public key at run-time.
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < key.length(); i++) {
            char c = key.charAt(i);
            if ((c >= 'A' && c <= 'M') || (c >= 'a' && c <= 'm'))
                c += 13;
            else if ((c >= 'N' && c <= 'Z') || (c >= 'n' && c <= 'z'))
                c -= 13;
            stringBuilder.append(c);
        }
        try {
            iap = new IabHelper(context, stringBuilder.toString());
            if (iap != null) {
                googleBillingListener = new GoogleBillingListener(iap, this);
                iap.startSetup(googleBillingListener);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void buy(Activity activity, String sku, int id) {
        try {
            if (iap != null)
                iap.launchPurchaseFlow(activity, sku, id, googleBillingListener);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void close() {
    }

}
