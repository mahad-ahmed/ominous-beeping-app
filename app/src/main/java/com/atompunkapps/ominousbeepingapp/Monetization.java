package com.atompunkapps.ominousbeepingapp;

import android.app.Activity;
import android.widget.FrameLayout;

import com.facebook.ads.AdSize;
import com.facebook.ads.AdView;
import com.facebook.ads.AudienceNetworkAds;

public class Monetization {
    private final AdView adView;

    Monetization(Activity context, FrameLayout adContainer) {
//        this.adView = adView;
        AudienceNetworkAds.initialize(context);
        adView = new AdView(context, SafeStore.PLACEMENT_ID, AdSize.BANNER_HEIGHT_50);
        adContainer.addView(adView);
        adView.loadAd();
    }

//    void initAds() {
//        MobileAds.initialize(context, initializationStatus -> {
////            Map<String, AdapterStatus> statusMap = initializationStatus.getAdapterStatusMap();
////            for (String adapterClass : statusMap.keySet()) {
////                AdapterStatus status = statusMap.get(adapterClass);
////                Log.d("MyApp", String.format(
////                        "Adapter name: %s, Description: %s, Latency: %d",
////                        adapterClass, status.getDescription(), status.getLatency()));
////            }
//
//            // For testing purposes only
////            new RequestConfiguration.Builder().setTestDeviceIds(Arrays.asList("BD62403485F5FEBFDCB000D103922A0C"));
//
//            // Start loading ads here...
//            AdRequest adRequest = new AdRequest.Builder().build();
//            adView.loadAd(adRequest);
//        });
//    }

//    void pauseAds() {
//        adView.pause();
//    }
//
//    void resumeAds() {
//        adView.resume();
//    }

    void destroyAds() {
        adView.destroy();
    }
}
