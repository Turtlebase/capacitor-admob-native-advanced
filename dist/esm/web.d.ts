import { WebPlugin } from "@capacitor/core";
import type { AdMobNativeAdvancedPlugin, InitializeOptions, NativeAdOptions, NativeAdLoadResult, ShowNativeAdOptions, HideNativeAdOptions, DestroyNativeAdOptions, UpdateNativeAdLayoutOptions, NativeAdEventName, NativeAdEvent, PluginListenerHandle } from "./definitions";
/**
 * Web stub for AdMobNativeAdvanced.
 * Native Ads are only supported on Android (and iOS) native platforms.
 * This stub exists to prevent build errors in web/Next.js projects.
 *
 * @author Umesh Dafda <umesh@turtlebase.com>
 * @company Turtlebase
 */
export declare class AdMobNativeAdvancedWeb extends WebPlugin implements AdMobNativeAdvancedPlugin {
    initialize(_options: InitializeOptions): Promise<void>;
    loadNativeAd(options: NativeAdOptions): Promise<NativeAdLoadResult>;
    showNativeAd(_options: ShowNativeAdOptions): Promise<void>;
    hideNativeAd(_options: HideNativeAdOptions): Promise<void>;
    destroyNativeAd(_options: DestroyNativeAdOptions): Promise<void>;
    updateNativeAdLayout(_options: UpdateNativeAdLayoutOptions): Promise<void>;
    addListener(eventName: NativeAdEventName, _listenerFunc: (data: NativeAdEvent) => void): Promise<PluginListenerHandle> & PluginListenerHandle;
    removeAllListeners(): Promise<void>;
}
