package com.eggheadgames.inapppayments;

import android.content.Intent;
import androidx.appcompat.app.AppCompatActivity;

import com.billing.BillingService;
import com.billing.google.GoogleBillingService;

public abstract class IAPActivity extends AppCompatActivity {

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        BillingService billingService = IAPManager.getBillingService();
        if (billingService instanceof GoogleBillingService) {
            ((GoogleBillingService) billingService).purchaseCallback(requestCode, resultCode, data);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

}
