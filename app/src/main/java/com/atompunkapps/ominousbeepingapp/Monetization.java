package com.atompunkapps.ominousbeepingapp;

import android.app.Activity;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;

public class Monetization {
    static void initAds(Activity context, AdView adView) {
        MobileAds.initialize(context, initializationStatus -> {
//            Map<String, AdapterStatus> statusMap = initializationStatus.getAdapterStatusMap();
//            for (String adapterClass : statusMap.keySet()) {
//                AdapterStatus status = statusMap.get(adapterClass);
//                Log.d("MyApp", String.format(
//                        "Adapter name: %s, Description: %s, Latency: %d",
//                        adapterClass, status.getDescription(), status.getLatency()));
//            }

            // For testing purposes only
//            new RequestConfiguration.Builder().setTestDeviceIds(Arrays.asList("BD62403485F5FEBFDCB000D103922A0C"));

            // Start loading ads here...
            AdRequest adRequest = new AdRequest.Builder().build();
            adView.loadAd(adRequest);
        });
    }
}
