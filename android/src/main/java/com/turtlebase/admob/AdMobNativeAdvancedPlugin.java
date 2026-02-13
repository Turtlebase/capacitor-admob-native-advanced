package com.turtlebase.admob;

import android.util.Log;

import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;

/**
 * Capacitor plugin entry point for Turtlebase AdMob Native Advanced.
 *
 * @author Umesh Dafda <umesh@turtlebase.com>
 * @company Turtlebase
 * @version 1.0.0
 */
@CapacitorPlugin(name = "AdMobNativeAdvanced")
public class AdMobNativeAdvancedPlugin extends Plugin {

    private static final String TAG = "TurtlebaseAdMob";

    private AdMobNativeAdvancedManager adManager;

    @Override
    public void load() {
        super.load();
        adManager = new AdMobNativeAdvancedManager(this);
        Log.d(TAG, "AdMobNativeAdvanced plugin loaded - Turtlebase");
    }

    /**
     * Initialize the AdMob SDK.
     */
    @PluginMethod
    public void initialize(PluginCall call) {
        String appId = call.getString("appId");
        if (appId == null || appId.isEmpty()) {
            call.reject("appId is required for AdMob initialization.");
            return;
        }

        Boolean testMode = call.getBoolean("testMode", false);
        Boolean tagForChildDirected = call.getBoolean("tagForChildDirectedTreatment", false);
        Boolean tagForUnderAge = call.getBoolean("tagForUnderAgeOfConsent", false);
        String maxAdContentRating = call.getString("maxAdContentRating", "G");

        adManager.initialize(
            call,
            appId,
            Boolean.TRUE.equals(testMode),
            Boolean.TRUE.equals(tagForChildDirected),
            Boolean.TRUE.equals(tagForUnderAge),
            maxAdContentRating
        );
    }

    /**
     * Load a Native Advanced ad.
     */
    @PluginMethod
    public void loadNativeAd(PluginCall call) {
        String adId = call.getString("adId");
        String adUnitId = call.getString("adUnitId");

        if (adId == null || adId.isEmpty()) {
            call.reject("adId is required.");
            return;
        }
        if (adUnitId == null || adUnitId.isEmpty()) {
            call.reject("adUnitId is required.");
            return;
        }

        adManager.loadNativeAd(call, adId, adUnitId);
    }

    /**
     * Show a loaded Native Ad.
     */
    @PluginMethod
    public void showNativeAd(PluginCall call) {
        String adId = call.getString("adId");
        if (adId == null || adId.isEmpty()) {
            call.reject("adId is required.");
            return;
        }
        adManager.showNativeAd(call, adId);
    }

    /**
     * Hide a visible Native Ad.
     */
    @PluginMethod
    public void hideNativeAd(PluginCall call) {
        String adId = call.getString("adId");
        if (adId == null || adId.isEmpty()) {
            call.reject("adId is required.");
            return;
        }
        adManager.hideNativeAd(call, adId);
    }

    /**
     * Destroy and remove a Native Ad from memory.
     */
    @PluginMethod
    public void destroyNativeAd(PluginCall call) {
        String adId = call.getString("adId");
        if (adId == null || adId.isEmpty()) {
            call.reject("adId is required.");
            return;
        }
        adManager.destroyNativeAd(call, adId);
    }

    /**
     * Update position/size of a Native Ad.
     */
    @PluginMethod
    public void updateNativeAdLayout(PluginCall call) {
        String adId = call.getString("adId");
        if (adId == null || adId.isEmpty()) {
            call.reject("adId is required.");
            return;
        }
        adManager.updateNativeAdLayout(call, adId);
    }

    /**
     * Called when the plugin is destroyed — clean up all ads.
     */
    @Override
    protected void handleOnDestroy() {
        if (adManager != null) {
            adManager.destroyAll();
        }
        super.handleOnDestroy();
    }
}
