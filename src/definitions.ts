/**
 * @packageDocumentation
 * @module @turtlebase/capacitor-admob-native-advanced
 * @author Umesh Dafda <umesh@turtlebase.com>
 * @company Turtlebase
 */

export interface AdMobNativeAdvancedPlugin {
  /**
   * Initialize the AdMob SDK. Call this once at app startup.
   * @since 1.0.0
   */
  initialize(options: InitializeOptions): Promise<void>;

  /**
   * Load a native advanced ad into the specified container.
   * @since 1.0.0
   */
  loadNativeAd(options: NativeAdOptions): Promise<NativeAdLoadResult>;

  /**
   * Show a previously loaded native ad.
   * @since 1.0.0
   */
  showNativeAd(options: ShowNativeAdOptions): Promise<void>;

  /**
   * Hide the currently visible native ad.
   * @since 1.0.0
   */
  hideNativeAd(options: HideNativeAdOptions): Promise<void>;

  /**
   * Destroy and remove the native ad from memory.
   * @since 1.0.0
   */
  destroyNativeAd(options: DestroyNativeAdOptions): Promise<void>;

  /**
   * Update the position or size of a visible native ad.
   * @since 1.0.0
   */
  updateNativeAdLayout(options: UpdateNativeAdLayoutOptions): Promise<void>;

  /**
   * Add a listener for native ad events.
   * @since 1.0.0
   */
  addListener(
    eventName: NativeAdEventName,
    listenerFunc: (data: NativeAdEvent) => void
  ): Promise<PluginListenerHandle> & PluginListenerHandle;

  /**
   * Remove all listeners for this plugin.
   * @since 1.0.0
   */
  removeAllListeners(): Promise<void>;
}

export interface InitializeOptions {
  /**
   * Your AdMob App ID (ca-app-pub-XXXXXXXXXXXXXXXX~XXXXXXXXXX).
   * For testing use: ca-app-pub-3940256099942544~3347511713
   */
  appId: string;

  /**
   * Enable test mode. Always true in development, false in production.
   * @default false
   */
  testMode?: boolean;

  /**
   * Tags for child-directed treatment.
   * @default false
   */
  tagForChildDirectedTreatment?: boolean;

  /**
   * Tags for users under age of consent in Europe.
   * @default false
   */
  tagForUnderAgeOfConsent?: boolean;

  /**
   * Maximum ad content rating.
   * @default 'G'
   */
  maxAdContentRating?: "G" | "PG" | "T" | "MA";
}

export interface NativeAdOptions {
  /**
   * Unique identifier for this ad instance.
   * Use this ID in subsequent calls to show/hide/destroy.
   */
  adId: string;

  /**
   * AdMob Native Advanced Ad Unit ID.
   * Test ID: ca-app-pub-3940256099942544/2247696110
   */
  adUnitId: string;

  /**
   * Position of the ad on the screen.
   */
  position: AdPosition;

  /**
   * Width of the native ad view in density-independent pixels (dp).
   * @default 320
   */
  width?: number;

  /**
   * Height of the native ad view in density-independent pixels (dp).
   * @default 250
   */
  height?: number;

  /**
   * Margin from the edge of the screen in dp.
   * @default 0
   */
  marginTop?: number;

  /**
   * Margin from the bottom of the screen in dp.
   * @default 0
   */
  marginBottom?: number;

  /**
   * Custom x offset in dp. Only used when position is CUSTOM.
   */
  x?: number;

  /**
   * Custom y offset in dp. Only used when position is CUSTOM.
   */
  y?: number;

  /**
   * Layout template to use for the native ad.
   * @default 'MEDIUM'
   */
  template?: NativeAdTemplate;

  /**
   * Optional custom layout name (your own XML layout resource name).
   * If provided, template is ignored.
   */
  customLayoutName?: string;

  /**
   * Ad network extras passed to the mediation network.
   */
  networkExtras?: Record<string, string>;
}

export interface ShowNativeAdOptions {
  /** The adId used in loadNativeAd */
  adId: string;
}

export interface HideNativeAdOptions {
  /** The adId used in loadNativeAd */
  adId: string;
}

export interface DestroyNativeAdOptions {
  /** The adId used in loadNativeAd */
  adId: string;
}

export interface UpdateNativeAdLayoutOptions {
  /** The adId used in loadNativeAd */
  adId: string;
  position?: AdPosition;
  x?: number;
  y?: number;
  width?: number;
  height?: number;
  marginTop?: number;
  marginBottom?: number;
}

export interface NativeAdLoadResult {
  /** The adId for this loaded ad */
  adId: string;
  /** Whether the ad loaded successfully */
  loaded: boolean;
}

export type AdPosition =
  | "TOP_LEFT"
  | "TOP_CENTER"
  | "TOP_RIGHT"
  | "BOTTOM_LEFT"
  | "BOTTOM_CENTER"
  | "BOTTOM_RIGHT"
  | "CENTER"
  | "CUSTOM";

export type NativeAdTemplate = "SMALL" | "MEDIUM" | "LARGE";

export type NativeAdEventName =
  | "onAdLoaded"
  | "onAdFailedToLoad"
  | "onAdImpression"
  | "onAdClicked"
  | "onAdOpened"
  | "onAdClosed";

export interface NativeAdEvent {
  /** The adId that triggered the event */
  adId: string;
  /** Event name */
  event: NativeAdEventName;
  /** Error message if event is onAdFailedToLoad */
  errorMessage?: string;
  /** Error code if event is onAdFailedToLoad */
  errorCode?: number;
}

export interface PluginListenerHandle {
  remove: () => Promise<void>;
}
