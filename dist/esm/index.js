import { registerPlugin } from "@capacitor/core";
/**
 * The main plugin instance.
 * On Android/iOS this bridges to the native implementation.
 * On web this returns the no-op stub.
 *
 * @author Umesh Dafda <umesh@turtlebase.com>
 * @company Turtlebase
 */
const AdMobNativeAdvanced = registerPlugin("AdMobNativeAdvanced", {
    web: () => import("./web").then((m) => new m.AdMobNativeAdvancedWeb()),
});
export * from "./definitions";
export { AdMobNativeAdvanced };
//# sourceMappingURL=index.js.map