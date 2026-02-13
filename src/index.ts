import { registerPlugin } from "@capacitor/core";

import type { AdMobNativeAdvancedPlugin } from "./definitions";

/**
 * The main plugin instance.
 * On Android/iOS this bridges to the native implementation.
 * On web this returns the no-op stub.
 *
 * @author Umesh Dafda <umesh@turtlebase.com>
 * @company Turtlebase
 */
const AdMobNativeAdvanced = registerPlugin<AdMobNativeAdvancedPlugin>(
  "AdMobNativeAdvanced",
  {
    web: () =>
      import("./web").then((m) => new m.AdMobNativeAdvancedWeb()),
  }
);

export * from "./definitions";
export { AdMobNativeAdvanced };
