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

### Coming soon: 
 * subscription support
 
### Not supported:
  * receipt validation (either local or server)
  * consumable items

## Similar Libraries

This library is in the same category as [OpenIAB](https://github.com/onepf/OpenIAB) (supports many more stores), 
[OPFLab](https://github.com/onepf/OPFIab) (replacement for OpenIAB). 
If you do not need Amazon support, there are several libraries that support just the Play store (see the [Android Arsenal list](https://android-arsenal.com/tag/79)).
