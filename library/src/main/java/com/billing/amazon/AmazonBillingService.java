package com.billing.amazon;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import com.amazon.device.iap.PurchasingService;
import com.billing.BillingService;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AmazonBillingService extends BillingService {

    private List<String> iapkeys;
    private Context context;

    public AmazonBillingService(Context context, List<String> iapkeys) {
        this.context = context;
        this.iapkeys = iapkeys;
    }

    @Override
    public void init(String key) {
        AmazonBillingListener amazonBillingListener = new AmazonBillingListener(this);
        PurchasingService.registerListener(context, amazonBillingListener);

        final Set<String> productSkus = new HashSet<>(iapkeys);
        PurchasingService.getProductData(productSkus);

        PurchasingService.getPurchaseUpdates(false);
    }

    @Override
    public void buy(Activity activity, String sku, int id) {
        PurchasingService.purchase(sku);
    }

    @Override
    public void subscribe(Activity activity, String sku, int id) {
        PurchasingService.purchase(sku);
    }

    @Override
    public void unsubscribe(Activity activity, String sku, int id) {
        try {
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("https://www.amazon.com/gp/mas/your-account/myapps/yoursubscriptions/ref=mas_ya_subs"));
            activity.startActivity(intent);
            activity.finish();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void close() {
    }
}
