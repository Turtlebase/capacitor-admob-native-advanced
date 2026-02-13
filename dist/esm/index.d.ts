import type { AdMobNativeAdvancedPlugin } from "./definitions";
/**
 * The main plugin instance.
 * On Android/iOS this bridges to the native implementation.
 * On web this returns the no-op stub.
 *
 * @author Umesh Dafda <umesh@turtlebase.com>
 * @company Turtlebase
 */
declare const AdMobNativeAdvanced: AdMobNativeAdvancedPlugin;
export * from "./definitions";
export { AdMobNativeAdvanced };
