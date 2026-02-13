package com.turtlebase.admob;

import android.util.Log;

import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;

@CapacitorPlugin(name = "AdMobNativeAdvanced")
public class AdMobNativeAdvancedPlugin extends Plugin {

    private static final String TAG = "TurtlebaseAdMob";
    private AdMobNativeAdvancedManager adManager;

    @Override
    public void load() {
        super.load();
        adManager = new AdMobNativeAdvancedManager(this);
        Log.d(TAG, "AdMobNativeAdvanced plugin loaded");
    }

    public void sendEvent(String eventName, JSObject data) {
        notifyListeners(eventName, data);
    }

    @PluginMethod
    public void initialize(PluginCall call) {
        String appId = call.getString("appId");
        if (appId == null) {
            call.reject("appId required");
            return;
        }

        adManager.initialize(call, appId, false, false, false, "G");
    }

    @PluginMethod
    public void loadNativeAd(PluginCall call) {
        adManager.loadNativeAd(call,
                call.getString("adId"),
                call.getString("adUnitId"));
    }

    @PluginMethod
    public void showNativeAd(PluginCall call) {
        adManager.showNativeAd(call, call.getString("adId"));
    }

    @PluginMethod
    public void hideNativeAd(PluginCall call) {
        adManager.hideNativeAd(call, call.getString("adId"));
    }

    @PluginMethod
    public void destroyNativeAd(PluginCall call) {
        adManager.destroyNativeAd(call, call.getString("adId"));
    }

    @Override
    protected void handleOnDestroy() {
        if (adManager != null) {
            adManager.destroyAll();
        }
        super.handleOnDestroy();
    }
}
