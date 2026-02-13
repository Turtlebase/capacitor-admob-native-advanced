import { WebPlugin } from "@capacitor/core";
/**
 * Web stub for AdMobNativeAdvanced.
 * Native Ads are only supported on Android (and iOS) native platforms.
 * This stub exists to prevent build errors in web/Next.js projects.
 *
 * @author Umesh Dafda <umesh@turtlebase.com>
 * @company Turtlebase
 */
export class AdMobNativeAdvancedWeb extends WebPlugin {
    async initialize(_options) {
        console.warn("[Turtlebase AdMob] Native Advanced Ads are not supported on web.");
    }
    async loadNativeAd(options) {
        console.warn("[Turtlebase AdMob] loadNativeAd is not supported on web.", options);
        return { adId: options.adId, loaded: false };
    }
    async showNativeAd(_options) {
        console.warn("[Turtlebase AdMob] showNativeAd is not supported on web.");
    }
    async hideNativeAd(_options) {
        console.warn("[Turtlebase AdMob] hideNativeAd is not supported on web.");
    }
    async destroyNativeAd(_options) {
        console.warn("[Turtlebase AdMob] destroyNativeAd is not supported on web.");
    }
    async updateNativeAdLayout(_options) {
        console.warn("[Turtlebase AdMob] updateNativeAdLayout is not supported on web.");
    }
    addListener(eventName, _listenerFunc) {
        console.warn(`[Turtlebase AdMob] addListener(${eventName}) is not supported on web.`);
        const handle = {
            remove: async () => {
                // noop
            },
        };
        return Object.assign(Promise.resolve(handle), handle);
    }
    async removeAllListeners() {
        // noop on web
    }
}
//# sourceMappingURL=web.js.map