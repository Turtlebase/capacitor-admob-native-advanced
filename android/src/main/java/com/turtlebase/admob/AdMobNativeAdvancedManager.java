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

    // ─────────────────────────────────────────────
    // INITIALIZE
    // ─────────────────────────────────────────────

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
                        configBuilder.setMaxAdContentRating(RequestConfiguration.MAX_AD_CONTENT_RATING_PG);
                        break;
                    case "T":
                        configBuilder.setMaxAdContentRating(RequestConfiguration.MAX_AD_CONTENT_RATING_T);
                        break;
                    case "MA":
                        configBuilder.setMaxAdContentRating(RequestConfiguration.MAX_AD_CONTENT_RATING_MA);
                        break;
                    default:
                        configBuilder.setMaxAdContentRating(RequestConfiguration.MAX_AD_CONTENT_RATING_G);
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

    // ─────────────────────────────────────────────
    // LOAD NATIVE AD
    // ─────────────────────────────────────────────

    public void loadNativeAd(PluginCall call, String adId, String adUnitId) {

        Activity activity = plugin.getActivity();
        if (activity == null) {
            call.reject("Activity is not available.");
            return;
        }

        final String position = call.getString("position", "BOTTOM_CENTER");
        final int width = call.getInt("width", 320);
        final int height = call.getInt("height", 250);
        final int marginTop = call.getInt("marginTop", 0);
        final int marginBottom = call.getInt("marginBottom", 0);
        final int x = call.getInt("x", 0);
        final int y = call.getInt("y", 0);
        final String template = call.getString("template", "MEDIUM");
        final String customLayout = call.getString("customLayoutName", null);

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

                            JSObject result = new JSObject();
                            result.put("adId", adId);
                            result.put("loaded", true);
                            call.resolve(result);
                        })
                        .withAdListener(new com.google.android.gms.ads.AdListener() {

                            @Override
                            public void onAdFailedToLoad(LoadAdError error) {

                                JSObject result = new JSObject();
                                result.put("adId", adId);
                                result.put("loaded", false);
                                result.put("errorMessage", error.getMessage());
                                result.put("errorCode", error.getCode());
                                call.resolve(result);
                            }
                        })
                        .withNativeAdOptions(
                                new NativeAdOptions.Builder()
                                        .setAdChoicesPlacement(NativeAdOptions.ADCHOICES_TOP_RIGHT)
                                        .build()
                        );

                builder.build().loadAd(new AdRequest.Builder().build());

            } catch (Exception e) {
                call.reject("loadNativeAd failed: " + e.getMessage());
            }
        });
    }

    // ─────────────────────────────────────────────
    // SHOW
    // ─────────────────────────────────────────────

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

    // ─────────────────────────────────────────────
    // UPDATE LAYOUT
    // ─────────────────────────────────────────────

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

            holder.updateLayout(
                    activity,
                    call.getString("position", null),
                    call.getInt("x", null),
                    call.getInt("y", null),
                    call.getInt("width", null),
                    call.getInt("height", null),
                    call.getInt("marginTop", null),
                    call.getInt("marginBottom", null)
            );

            call.resolve();
        });
    }

    // ─────────────────────────────────────────────
    // DESTROY
    // ─────────────────────────────────────────────

    public void destroyNativeAd(PluginCall call, String adId) {
        destroyAdById(adId);
        call.resolve();
    }

    private void destroyAdById(String adId) {
        NativeAdViewHolder holder = adHolders.remove(adId);
        if (holder != null) {
            holder.destroy();
        }
    }
}
