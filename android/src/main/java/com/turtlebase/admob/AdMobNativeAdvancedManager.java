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

                MobileAds.setRequestConfiguration(configBuilder.build());

                MobileAds.initialize(activity, status -> {
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
            call.reject("Activity not available.");
            return;
        }

        // 🔥 Receive layout values from JS (DP)
        final Integer x = call.getInt("x", 0);
        final Integer y = call.getInt("y", 0);
        final Integer width = call.getInt("width", 320);
        final Integer height = call.getInt("height", 250);

        final String template = call.getString("template", "MEDIUM");
        final String customLayout = call.getString("customLayoutName", null);

        activity.runOnUiThread(() -> {

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
                                        x,
                                        y,
                                        width,
                                        height,
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
                            result.put("loaded", false);
                            result.put("error", error.getMessage());
                            call.resolve(result);
                        }
                    })
                    .withNativeAdOptions(
                            new NativeAdOptions.Builder().build()
                    );

            builder.build().loadAd(new AdRequest.Builder().build());
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
    // UPDATE POSITION (SCROLL FIX)
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
                    call.getInt("x", null),
                    call.getInt("y", null),
                    call.getInt("width", null),
                    call.getInt("height", null)
            );

            call.resolve();
        });
    }

    // ─────────────────────────────────────────────
    // HIDE
    // ─────────────────────────────────────────────

    public void hideNativeAd(PluginCall call, String adId) {

        NativeAdViewHolder holder = adHolders.get(adId);

        if (holder != null) {
            holder.hide();
        }

        call.resolve();
    }

    // ─────────────────────────────────────────────
    // DESTROY
    // ─────────────────────────────────────────────

    public void destroyNativeAd(PluginCall call, String adId) {
        destroyAdById(adId);
        call.resolve();
    }

    public void destroyAll() {
        for (String id : new ArrayList<>(adHolders.keySet())) {
            destroyAdById(id);
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
