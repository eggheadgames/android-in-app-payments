[![Circle CI](https://circleci.com/gh/eggheadgames/android-in-app-payments.svg?style=svg)](https://circleci.com/gh/eggheadgames/android-in-app-payments)
[![Release](https://jitpack.io/v/eggheadgames/android-in-app-payments.svg)](https://jitpack.io/#eggheadgames/android-in-app-payments)
<a target="_blank" href="https://android-arsenal.com/api?level=15"><img src="https://img.shields.io/badge/API-15%2B-orange.svg"></a>
[![GitHub license](https://img.shields.io/badge/license-MIT-lightgrey.svg)](https://github.com/eggheadgames/android-in-app-payments/blob/master/LICENSE)


# Android Payments

### Support both Google Play and Amazon Kindle Fire in-app purchase payments with a single API

Handy for small apps with in-app purchase (IAP) items that need both Google Play store and Amazon App Store support - i.e. regular Android devices and Amazon Kindle Fire.
We developed this as a convenient way to keep multiple apps updated with the latest IAP code for Play and Amazon. 

## About

A simple wrapper library that provides sample Google and Amazon in-app purchase APIs in a single API.

### Features:
 * non-consumable (Play) / Entitlements (Amazon)
 * [Google In-App Bill API version 3](https://developer.android.com/google/play/billing/billing_overview.html) (includes fixes for > 20 IAP keys)
 * [Amazon IAP v2](https://developer.amazon.com/appsandservices/apis/earn/in-app-purchasing)
 * fetch localised prices
 * actively maintained by [Egghead Games](http://eggheadgames.com) for their cross-platform mobile/tablet apps ([quality brain puzzles with no ads](https://play.google.com/store/apps/dev?id=8905223606155014113)!)
 * subscriptions for Google Play
 * subscriptions for Amazon
 
### Not supported:
  * receipt validation (either local or server)
  * consumable items

## Similar Libraries

This library is in the same category as [OpenIAB](https://github.com/onepf/OpenIAB) (supports many more stores), 
[OPFLab](https://github.com/onepf/OPFIab) (replacement for OpenIAB). 
If you do not need Amazon support, there are several libraries that support just the Play store (see the [Android Arsenal list](https://android-arsenal.com/tag/79)).

## Installation Instructions

Add the JitPack.io repository to your root `build.gradle`:

```gradle
allprojects {
    repositories {
        maven { url "https://jitpack.io" }
    }
}
```

Add a dependency to your application related `build.gradle`

```gradle
dependencies {
    compile 'com.github.eggheadgames:android-in-app-payments:<actual version>'
}
```

## Example
### Setup
The following code snippet initializes the billing module:

```java
    IAPManager.build(context, IAPManager.BUILD_TARGET_GOOGLE, skuList /*can be ignored for Google traget*/);
    IAPManager.addPurchaseListener(new PurchaseServiceListener() {
            @Override
            public void onPricesUpdated(Map<String, String> map) {
                // list of available products will be received here, so you can update UI with prices if needed
            }
            
            @Override
            public void onProductPurchased(String sku) {
                // will be triggered whenever purchase succeeded 
            }

            @Override
            public void onProductRestored(String sku) {
                // will be triggered fetching owned products using IAPManager.init();
            }
    });
    IAPManager.init(googleIapKey /*can be ignored for Amazon target*/);
```

1. Setup billing module.
```
IAPManager.build(Context context, int buildTarget, List<String> skuList)
``` 

`int buildTarget` can be either `IAPManager.BUILD_TARGET_GOOGLE` or `IAPManager.BUILD_TARGET_AMAZON`
`List<String> skuList` - a list of products to fetch information about (relevant only for `IAPManager.BUILD_TARGET_AMAZON`)

2. Request info about available and owned products
```
IAPManager.init(String rot13LicenseKey)
```

`String rot13LicenseKey` is relevant only for `IAPManager.BUILD_TARGET_GOOGLE`, and can be ignored for `IAPManager.BUILD_TARGET_AMAZON`. Note that this is the required Google License Key obtained from the app's Google Play console, after applying the [ROT 13 algorithm](https://en.wikipedia.org/wiki/ROT13). 
You might choose to store the key as ROT-13 in your app to avoid casual decoding of the strings, however, this is not really secure, so you are advised to follow [Google's advice](https://developer.android.com/training/in-app-billing/preparing-iab-app.html) and then ROT-13 the key before passing it to the API:

> Security Recommendation: Google highly recommends that you do not hard-code the exact public license key string value as provided by Google Play. Instead, construct the whole public license key string at runtime from substrings or retrieve it from an encrypted store before passing it to the constructor. This approach makes it more difficult for malicious third parties to modify the public license key string in your APK file.


### Buying a product

To buy a product use the following method:
```
IAPManager.buy(Activity activity, String sku, int requestCode);
```
`PurchaseServiceListener` will notify application about the operation result

### Subscriptions
The following listener can be used to obtain owned subscriptions and to get notification about subscription operation result

```java
    IAPManager.addSubscriptionListener(new SubscriptionServiceListener() {
        @Override
        public void onSubscriptionRestored(String s) {
            // will be triggered upon fetching owned subscription using IAPManager.init();
        }

        @Override
        public void onSubscriptionPurchased(String s) {
            // will be triggered whenever subscription succeeded
        }

        @Override
        public void onPricesUpdated(Map<String, String> map) {
            // list of available products will be received here, so you can update UI with prices if needed            
        }
    });
```
To start a subscription use the following method:

```
IAPManager.subscribe(Activity activity, String sku, int requestCode);
```

`String sku` - a subscription ID
`int requestCode` - a unique request code to be used to deliver result through `onActivityResult`
`SubscriptionServiceListener.onSubscriptionPurchased()` will notify application about successful operation result

Use the following method to remove subscription 

```
IAPManager.unsubscribe(Activity activity, String sku, int requestCode);
```

Please keep in mind that for Google In App Billing it will just lead user to the Google Pay Account Settings page where user may cancel subscription manually.

#### Note

When you are integrating Google In App Billing Subscriptions into your application please pay attention for the subscription cancellation handling.
As long as currently subscription can be cancelled only through Google Play Account settings - application won't be notified about this event.
Thus you have to query for the owned subscriptions each time you open the application (if needed), which is done via `IAPManager.init()` method.
