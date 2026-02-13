package com.turtlebase.admob;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import com.getcapacitor.JSObject;
import com.getcapacitor.PluginCall;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.RequestConfiguration;
import com.google.android.gms.ads.nativead.NativeAd;
import com.google.android.gms.ads.nativead.NativeAdOptions;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.LoadAdError;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdMobNativeAdvancedManager {

    private static final String TAG = "TurtlebaseAdMob";

    private final AdMobNativeAdvancedPlugin plugin;

    private final Map<String, NativeAdViewHolder> adHolders = new HashMap<>();

    private boolean sdkInitialized = false;

    public AdMobNativeAdvancedManager(AdMobNativeAdvancedPlugin plugin) {
        this.plugin = plugin;
    }

    // ─────────────────────────────────────────────────────────────
    // INITIALIZE
    // ─────────────────────────────────────────────────────────────

    public void initialize(
            PluginCall call,
            String appId,
            boolean testMode,
            boolean tagForChildDirected,
            boolean tagForUnderAge,
            String maxAdContentRating
    ) {

        Activity activity = plugin.getActivity();
        if (activity == null) {
            call.reject("Activity is not available.");
            return;
        }

        activity.runOnUiThread(() -> {
            try {

                RequestConfiguration.Builder configBuilder =
                        new RequestConfiguration.Builder();

                if (testMode) {
                    List<String> testDeviceIds = new ArrayList<>();
                    testDeviceIds.add(AdRequest.DEVICE_ID_EMULATOR);
                    configBuilder.setTestDeviceIds(testDeviceIds);
                }

                if (tagForChildDirected) {
                    configBuilder.setTagForChildDirectedTreatment(
                            RequestConfiguration.TAG_FOR_CHILD_DIRECTED_TREATMENT_TRUE
                    );
                }

                if (tagForUnderAge) {
                    configBuilder.setTagForUnderAgeOfConsent(
                            RequestConfiguration.TAG_FOR_UNDER_AGE_OF_CONSENT_TRUE
                    );
                }

                switch (maxAdContentRating) {
                    case "PG":
                        configBuilder.setMaxAdContentRating(
                                RequestConfiguration.MAX_AD_CONTENT_RATING_PG);
                        break;
                    case "T":
                        configBuilder.setMaxAdContentRating(
                                RequestConfiguration.MAX_AD_CONTENT_RATING_T);
                        break;
                    case "MA":
                        configBuilder.setMaxAdContentRating(
                                RequestConfiguration.MAX_AD_CONTENT_RATING_MA);
                        break;
                    default:
                        configBuilder.setMaxAdContentRating(
                                RequestConfiguration.MAX_AD_CONTENT_RATING_G);
                        break;
                }

                MobileAds.setRequestConfiguration(configBuilder.build());

                MobileAds.initialize(activity, status -> {
                    sdkInitialized = true;
                    Log.d(TAG, "AdMob SDK initialized");
                    call.resolve();
                });

            } catch (Exception e) {
                call.reject("Initialization failed: " + e.getMessage());
            }
        });
    }

    // ─────────────────────────────────────────────────────────────
    // LOAD NATIVE AD
    // ─────────────────────────────────────────────────────────────

    public void loadNativeAd(PluginCall call, String adId, String adUnitId) {

        Activity activity = plugin.getActivity();
        if (activity == null) {
            call.reject("Activity is not available.");
            return;
        }

        String position = call.getString("position", "BOTTOM_CENTER");
        int width = call.getInt("width", 320);
        int height = call.getInt("height", 250);
        int marginTop = call.getInt("marginTop", 0);
        int marginBottom = call.getInt("marginBottom", 0);
        int x = call.getInt("x", 0);
        int y = call.getInt("y", 0);
        String template = call.getString("template", "MEDIUM");
        String customLayout = call.getString("customLayoutName", null);

        activity.runOnUiThread(() -> {

            try {

                if (adHolders.containsKey(adId)) {
                    destroyAdById(adId);
                }

                Context context = activity.getApplicationContext();

                AdLoader.Builder builder = new AdLoader.Builder(context, adUnitId)
                        .forNativeAd(nativeAd -> {

                            NativeAdViewHolder holder =
                                    new NativeAdViewHolder(
                                            plugin,
                                            adId,
                                            nativeAd,
                                            position,
                                            width,
                                            height,
                                            marginTop,
                                            marginBottom,
                                            x,
                                            y,
                                            template,
                                            customLayout
                                    );

                            adHolders.put(adId, holder);

                            JSObject event = new JSObject();
                            event.put("adId", adId);
                            plugin.sendEvent("onAdLoaded", event);

                            JSObject result = new JSObject();
                            result.put("adId", adId);
                            result.put("loaded", true);
                            call.resolve(result);
                        })
                        .withAdListener(new com.google.android.gms.ads.AdListener() {

                            @Override
                            public void onAdFailedToLoad(LoadAdError error) {

                                JSObject event = new JSObject();
                                event.put("adId", adId);
                                event.put("errorMessage", error.getMessage());
                                event.put("errorCode", error.getCode());

                                plugin.sendEvent("onAdFailedToLoad", event);

                                JSObject result = new JSObject();
                                result.put("adId", adId);
                                result.put("loaded", false);
                                call.resolve(result);
                            }

                            @Override
                            public void onAdImpression() {
                                JSObject event = new JSObject();
                                event.put("adId", adId);
                                plugin.sendEvent("onAdImpression", event);
                            }

                            @Override
                            public void onAdClicked() {
                                JSObject event = new JSObject();
                                event.put("adId", adId);
                                plugin.sendEvent("onAdClicked", event);
                            }

                            @Override
                            public void onAdOpened() {
                                JSObject event = new JSObject();
                                event.put("adId", adId);
                                plugin.sendEvent("onAdOpened", event);
                            }

                            @Override
                            public void onAdClosed() {
                                JSObject event = new JSObject();
                                event.put("adId", adId);
                                plugin.sendEvent("onAdClosed", event);
                            }
                        })
                        .withNativeAdOptions(
                                new NativeAdOptions.Builder()
                                        .setAdChoicesPlacement(
                                                NativeAdOptions.ADCHOICES_TOP_RIGHT)
                                        .build()
                        );

                builder.build().loadAd(new AdRequest.Builder().build());

            } catch (Exception e) {
                call.reject("loadNativeAd failed: " + e.getMessage());
            }
        });
    }

    // ─────────────────────────────────────────────────────────────
    // SHOW / HIDE / DESTROY / UPDATE
    // ─────────────────────────────────────────────────────────────

    public void showNativeAd(PluginCall call, String adId) {

        Activity activity = plugin.getActivity();
        if (activity == null) {
            call.reject("Activity not available.");
            return;
        }

        activity.runOnUiThread(() -> {
            NativeAdViewHolder holder = adHolders.get(adId);
            if (holder == null) {
                call.reject("No ad loaded with adId: " + adId);
                return;
            }
            holder.show(activity);
            call.resolve();
        });
    }

    public void hideNativeAd(PluginCall call, String adId) {

        Activity activity = plugin.getActivity();
        if (activity == null) {
            call.reject("Activity not available.");
            return;
        }

        activity.runOnUiThread(() -> {
            NativeAdViewHolder holder = adHolders.get(adId);
            if (holder == null) {
                call.reject("No ad loaded with adId: " + adId);
                return;
            }
            holder.hide();
            call.resolve();
        });
    }

    public void destroyNativeAd(PluginCall call, String adId) {

        Activity activity = plugin.getActivity();
        if (activity == null) {
            call.reject("Activity not available.");
            return;
        }

        activity.runOnUiThread(() -> {
            destroyAdById(adId);
            call.resolve();
        });
    }

    public void updateNativeAdLayout(PluginCall call, String adId) {

        Activity activity = plugin.getActivity();
        if (activity == null) {
            call.reject("Activity not available.");
            return;
        }

        activity.runOnUiThread(() -> {

            NativeAdViewHolder holder = adHolders.get(adId);
            if (holder == null) {
                call.reject("No ad loaded with adId: " + adId);
                return;
            }

            String newPosition = call.getString("position", null);
            Integer newX = call.getInt("x", null);
            Integer newY = call.getInt("y", null);
            Integer newWidth = call.getInt("width", null);
            Integer newHeight = call.getInt("height", null);
            Integer newMarginTop = call.getInt("marginTop", null);
            Integer newMarginBottom = call.getInt("marginBottom", null);

            holder.updateLayout(
                    activity,
                    newPosition,
                    newX,
                    newY,
                    newWidth,
                    newHeight,
                    newMarginTop,
                    newMarginBottom
            );

            call.resolve();
        });
    }

    public void destroyAll() {
        for (String adId : new ArrayList<>(adHolders.keySet())) {
            destroyAdById(adId);
        }
        adHolders.clear();
    }

    private void destroyAdById(String adId) {
        NativeAdViewHolder holder = adHolders.remove(adId);
        if (holder != null) {
            holder.destroy();
        }
    }
}
