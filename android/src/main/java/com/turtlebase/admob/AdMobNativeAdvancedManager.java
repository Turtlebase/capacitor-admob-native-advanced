package com.turtlebase.admob;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;

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

/**
 * Core manager for all Native Advanced Ad operations.
 *
 * Handles:
 *  - AdMob SDK initialization
 *  - Loading native ads via AdLoader
 *  - Rendering custom or template-based NativeAdViews
 *  - Positioning on screen
 *  - Lifecycle management (show/hide/destroy)
 *
 * @author Umesh Dafda <umesh@turtlebase.com>
 * @company Turtlebase
 */
public class AdMobNativeAdvancedManager {

    private static final String TAG = "TurtlebaseAdMob";

    private final AdMobNativeAdvancedPlugin plugin;

    /** Map of adId -> NativeAdViewHolder */
    private final Map<String, NativeAdViewHolder> adHolders = new HashMap<>();

    private boolean sdkInitialized = false;

    public AdMobNativeAdvancedManager(AdMobNativeAdvancedPlugin plugin) {
        this.plugin = plugin;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // INITIALIZE
    // ─────────────────────────────────────────────────────────────────────────

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
                // Build request configuration
                RequestConfiguration.Builder configBuilder =
                    new RequestConfiguration.Builder();

                if (testMode) {
                    List<String> testDeviceIds = new ArrayList<>();
                    testDeviceIds.add(AdRequest.DEVICE_ID_EMULATOR);
                    configBuilder.setTestDeviceIds(testDeviceIds);
                    Log.d(TAG, "AdMob test mode enabled.");
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

                // Set max content rating
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

                MobileAds.initialize(activity, initializationStatus -> {
                    sdkInitialized = true;
                    Log.d(TAG, "AdMob SDK initialized - Turtlebase");
                    call.resolve();
                });

            } catch (Exception e) {
                Log.e(TAG, "AdMob initialization error: " + e.getMessage(), e);
                call.reject("AdMob initialization failed: " + e.getMessage());
            }
        });
    }

    // ─────────────────────────────────────────────────────────────────────────
    // LOAD NATIVE AD
    // ─────────────────────────────────────────────────────────────────────────

    public void loadNativeAd(PluginCall call, String adId, String adUnitId) {
        Activity activity = plugin.getActivity();
        if (activity == null) {
            call.reject("Activity is not available.");
            return;
        }

        // Extract options
        String position   = call.getString("position", "BOTTOM_CENTER");
        int width         = call.getInt("width", 320);
        int height        = call.getInt("height", 250);
        int marginTop     = call.getInt("marginTop", 0);
        int marginBottom  = call.getInt("marginBottom", 0);
        int x             = call.getInt("x", 0);
        int y             = call.getInt("y", 0);
        String template   = call.getString("template", "MEDIUM");
        String customLayout = call.getString("customLayoutName", null);

        activity.runOnUiThread(() -> {
            try {
                // Destroy existing ad with same ID if any
                if (adHolders.containsKey(adId)) {
                    destroyAdById(adId);
                }

                Context context = activity.getApplicationContext();

                AdLoader.Builder adLoaderBuilder = new AdLoader.Builder(context, adUnitId)
                    .forNativeAd(nativeAd -> {
                        Log.d(TAG, "Native ad loaded for adId: " + adId);

                        activity.runOnUiThread(() -> {
                            try {
                                // Create the NativeAdView
                                NativeAdViewHolder holder = new NativeAdViewHolder(
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

                                // Fire JS event
                                JSObject eventData = new JSObject();
                                eventData.put("adId", adId);
                                eventData.put("event", "onAdLoaded");
                                plugin.notifyListeners("onAdLoaded", eventData);

                                // Resolve the call
                                JSObject result = new JSObject();
                                result.put("adId", adId);
                                result.put("loaded", true);
                                call.resolve(result);

                            } catch (Exception e) {
                                Log.e(TAG, "Error building NativeAdView: " + e.getMessage(), e);
                                call.reject("Error building NativeAdView: " + e.getMessage());
                            }
                        });
                    })
                    .withAdListener(new com.google.android.gms.ads.AdListener() {
                        @Override
                        public void onAdFailedToLoad(LoadAdError loadAdError) {
                            Log.e(TAG, "Native ad failed to load: " + loadAdError.getMessage());

                            JSObject eventData = new JSObject();
                            eventData.put("adId", adId);
                            eventData.put("event", "onAdFailedToLoad");
                            eventData.put("errorMessage", loadAdError.getMessage());
                            eventData.put("errorCode", loadAdError.getCode());
                            plugin.notifyListeners("onAdFailedToLoad", eventData);

                            JSObject result = new JSObject();
                            result.put("adId", adId);
                            result.put("loaded", false);
                            call.resolve(result);
                        }

                        @Override
                        public void onAdImpression() {
                            JSObject eventData = new JSObject();
                            eventData.put("adId", adId);
                            eventData.put("event", "onAdImpression");
                            plugin.notifyListeners("onAdImpression", eventData);
                        }

                        @Override
                        public void onAdClicked() {
                            JSObject eventData = new JSObject();
                            eventData.put("adId", adId);
                            eventData.put("event", "onAdClicked");
                            plugin.notifyListeners("onAdClicked", eventData);
                        }

                        @Override
                        public void onAdOpened() {
                            JSObject eventData = new JSObject();
                            eventData.put("adId", adId);
                            eventData.put("event", "onAdOpened");
                            plugin.notifyListeners("onAdOpened", eventData);
                        }

                        @Override
                        public void onAdClosed() {
                            JSObject eventData = new JSObject();
                            eventData.put("adId", adId);
                            eventData.put("event", "onAdClosed");
                            plugin.notifyListeners("onAdClosed", eventData);
                        }
                    })
                    .withNativeAdOptions(
                        new NativeAdOptions.Builder()
                            .setAdChoicesPlacement(NativeAdOptions.ADCHOICES_TOP_RIGHT)
                            .build()
                    );

                AdLoader adLoader = adLoaderBuilder.build();
                adLoader.loadAd(new AdRequest.Builder().build());

            } catch (Exception e) {
                Log.e(TAG, "loadNativeAd error: " + e.getMessage(), e);
                call.reject("loadNativeAd failed: " + e.getMessage());
            }
        });
    }

    // ─────────────────────────────────────────────────────────────────────────
    // SHOW / HIDE / DESTROY / UPDATE
    // ─────────────────────────────────────────────────────────────────────────

    public void showNativeAd(PluginCall call, String adId) {
        Activity activity = plugin.getActivity();
        if (activity == null) { call.reject("Activity not available."); return; }

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
        if (activity == null) { call.reject("Activity not available."); return; }

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
        if (activity == null) { call.reject("Activity not available."); return; }

        activity.runOnUiThread(() -> {
            destroyAdById(adId);
            call.resolve();
        });
    }

    public void updateNativeAdLayout(PluginCall call, String adId) {
        Activity activity = plugin.getActivity();
        if (activity == null) { call.reject("Activity not available."); return; }

        activity.runOnUiThread(() -> {
            NativeAdViewHolder holder = adHolders.get(adId);
            if (holder == null) {
                call.reject("No ad loaded with adId: " + adId);
                return;
            }

            String newPosition = call.getString("position", null);
            Integer newX       = call.getInt("x", null);
            Integer newY       = call.getInt("y", null);
            Integer newWidth   = call.getInt("width", null);
            Integer newHeight  = call.getInt("height", null);
            Integer newMarginTop    = call.getInt("marginTop", null);
            Integer newMarginBottom = call.getInt("marginBottom", null);

            holder.updateLayout(activity, newPosition, newX, newY,
                newWidth, newHeight, newMarginTop, newMarginBottom);
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
