import { WebPlugin } from "@capacitor/core";

import type {
  AdMobNativeAdvancedPlugin,
  InitializeOptions,
  NativeAdOptions,
  NativeAdLoadResult,
  ShowNativeAdOptions,
  HideNativeAdOptions,
  DestroyNativeAdOptions,
  UpdateNativeAdLayoutOptions,
  NativeAdEventName,
  NativeAdEvent,
  PluginListenerHandle,
} from "./definitions";

/**
 * Web stub for AdMobNativeAdvanced.
 * Native Ads are only supported on Android (and iOS) native platforms.
 * This stub exists to prevent build errors in web/Next.js projects.
 *
 * @author Umesh Dafda <umesh@turtlebase.com>
 * @company Turtlebase
 */
export class AdMobNativeAdvancedWeb
  extends WebPlugin
  implements AdMobNativeAdvancedPlugin
{
  async initialize(_options: InitializeOptions): Promise<void> {
    console.warn(
      "[Turtlebase AdMob] Native Advanced Ads are not supported on web."
    );
  }

  async loadNativeAd(options: NativeAdOptions): Promise<NativeAdLoadResult> {
    console.warn(
      "[Turtlebase AdMob] loadNativeAd is not supported on web.",
      options
    );
    return { adId: options.adId, loaded: false };
  }

  async showNativeAd(_options: ShowNativeAdOptions): Promise<void> {
    console.warn("[Turtlebase AdMob] showNativeAd is not supported on web.");
  }

  async hideNativeAd(_options: HideNativeAdOptions): Promise<void> {
    console.warn("[Turtlebase AdMob] hideNativeAd is not supported on web.");
  }

  async destroyNativeAd(_options: DestroyNativeAdOptions): Promise<void> {
    console.warn("[Turtlebase AdMob] destroyNativeAd is not supported on web.");
  }

  async updateNativeAdLayout(
    _options: UpdateNativeAdLayoutOptions
  ): Promise<void> {
    console.warn(
      "[Turtlebase AdMob] updateNativeAdLayout is not supported on web."
    );
  }

  addListener(
    eventName: NativeAdEventName,
    _listenerFunc: (data: NativeAdEvent) => void
  ): Promise<PluginListenerHandle> & PluginListenerHandle {
    console.warn(
      `[Turtlebase AdMob] addListener(${eventName}) is not supported on web.`
    );
    const handle: PluginListenerHandle = {
      remove: async () => {
        // noop
      },
    };
    return Object.assign(Promise.resolve(handle), handle);
  }

  async removeAllListeners(): Promise<void> {
    // noop on web
  }
}
