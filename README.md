# @turtlebase/capacitor-admob-native-advanced

> Capacitor 7 plugin for **Google AdMob Native Advanced Ads** on Android.
>
> - Java 21 · Android SDK 35
> - Full ad lifecycle: load, show, hide, update, destroy
> - 3 built-in templates (SMALL, MEDIUM, LARGE) + custom XML layout support
> - Event listeners: loaded, failed, impression, click, opened, closed
>
> **Company:** [Turtlebase](https://turtlebase.com)
> **Author:** Umesh Dafda \<umesh@turtlebase.com\>

---

## Install

```bash
npm install @turtlebase/capacitor-admob-native-advanced
npx cap sync
```

### Or link locally (development):

```bash
# In the plugin repo:
npm run build && npm link

# In your app:
npm link @turtlebase/capacitor-admob-native-advanced
npx cap sync android
```

---

## Android Setup

### 1. Add your AdMob App ID to `AndroidManifest.xml`

In your **app's** `android/app/src/main/AndroidManifest.xml`:

```xml
<manifest>
  <application>
    <!-- Add this inside <application> -->
    <meta-data
      android:name="com.google.android.gms.ads.APPLICATION_ID"
      android:value="ca-app-pub-XXXXXXXXXXXXXXXX~XXXXXXXXXX"/>
  </application>
</manifest>
```

> For testing use App ID: `ca-app-pub-3940256099942544~3347511713`

### 2. That's it — no `MainActivity.java` changes needed

Capacitor 7 uses **annotation-based auto-discovery**. The `@CapacitorPlugin` annotation on `AdMobNativeAdvancedPlugin` is picked up automatically at build time via the annotation processor. No manual `registerPlugin()` call is required.

Just run `npx cap sync android` and the plugin is ready.

---

## Usage (Next.js / Web + Capacitor)

```typescript
import { AdMobNativeAdvanced } from '@turtlebase/capacitor-admob-native-advanced';
import { Capacitor } from '@capacitor/core';

// Only run on native
if (Capacitor.isNativePlatform()) {

  // 1. Initialize once at app startup
  await AdMobNativeAdvanced.initialize({
    appId: 'ca-app-pub-XXXXXXXXXXXXXXXX~XXXXXXXXXX',
    testMode: true, // set false in production
  });

  // 2. Listen for events
  await AdMobNativeAdvanced.addListener('onAdLoaded', (data) => {
    console.log('Ad loaded!', data.adId);
  });

  await AdMobNativeAdvanced.addListener('onAdFailedToLoad', (data) => {
    console.error('Ad failed:', data.errorMessage);
  });

  await AdMobNativeAdvanced.addListener('onAdClicked', (data) => {
    console.log('Ad clicked:', data.adId);
  });

  // 3. Load the ad
  const result = await AdMobNativeAdvanced.loadNativeAd({
    adId: 'my-home-ad',
    adUnitId: 'ca-app-pub-3940256099942544/2247696110', // test ID
    position: 'BOTTOM_CENTER',
    width: 360,
    height: 280,
    template: 'MEDIUM',
    marginBottom: 60,
  });

  if (result.loaded) {
    // 4. Show the ad
    await AdMobNativeAdvanced.showNativeAd({ adId: 'my-home-ad' });
  }

  // 5. Hide it
  await AdMobNativeAdvanced.hideNativeAd({ adId: 'my-home-ad' });

  // 6. Update position/size dynamically
  await AdMobNativeAdvanced.updateNativeAdLayout({
    adId: 'my-home-ad',
    position: 'TOP_CENTER',
    marginTop: 80,
  });

  // 7. Destroy when done
  await AdMobNativeAdvanced.destroyNativeAd({ adId: 'my-home-ad' });
}
```

---

## API

### `initialize(options)`

Initialize the AdMob SDK. Call once at app startup.

| Option | Type | Default | Description |
|---|---|---|---|
| `appId` | `string` | required | Your AdMob App ID |
| `testMode` | `boolean` | `false` | Enable test ads |
| `tagForChildDirectedTreatment` | `boolean` | `false` | COPPA compliance |
| `tagForUnderAgeOfConsent` | `boolean` | `false` | GDPR compliance |
| `maxAdContentRating` | `'G'|'PG'|'T'|'MA'` | `'G'` | Max content rating |

---

### `loadNativeAd(options)`

Load a Native Advanced ad.

| Option | Type | Default | Description |
|---|---|---|---|
| `adId` | `string` | required | Your unique ID for this ad |
| `adUnitId` | `string` | required | AdMob Native Ad Unit ID |
| `position` | `AdPosition` | `'BOTTOM_CENTER'` | Screen position |
| `width` | `number` | `320` | Width in dp |
| `height` | `number` | `250` | Height in dp |
| `marginTop` | `number` | `0` | Top margin in dp |
| `marginBottom` | `number` | `0` | Bottom margin in dp |
| `x` | `number` | `0` | X offset (CUSTOM position only) |
| `y` | `number` | `0` | Y offset (CUSTOM position only) |
| `template` | `NativeAdTemplate` | `'MEDIUM'` | Built-in template |
| `customLayoutName` | `string` | — | Your custom XML layout name |

**Returns:** `{ adId: string, loaded: boolean }`

---

### `showNativeAd({ adId })`
### `hideNativeAd({ adId })`
### `destroyNativeAd({ adId })`
### `updateNativeAdLayout({ adId, ...options })`

---

### `AdPosition` values

`TOP_LEFT` · `TOP_CENTER` · `TOP_RIGHT` · `BOTTOM_LEFT` · `BOTTOM_CENTER` · `BOTTOM_RIGHT` · `CENTER` · `CUSTOM`

---

### `NativeAdTemplate` values

`SMALL` · `MEDIUM` · `LARGE`

---

### Events

| Event | Fired when |
|---|---|
| `onAdLoaded` | Ad loaded successfully |
| `onAdFailedToLoad` | Ad failed to load |
| `onAdImpression` | Ad shown to user |
| `onAdClicked` | User clicked the ad |
| `onAdOpened` | Ad opened full screen |
| `onAdClosed` | Full screen ad closed |

---

## Custom Layout

1. Create `android/app/src/main/res/layout/my_custom_native_ad.xml` in your **app** (not the plugin).
2. Use `com.google.android.gms.ads.nativead.NativeAdView` as root.
3. Use these view IDs (same as built-in templates): `ad_headline`, `ad_body`, `ad_app_icon`, `ad_media`, `ad_call_to_action`, `ad_advertiser`, `ad_stars`.
4. Pass `customLayoutName: 'my_custom_native_ad'` in `loadNativeAd`.

---

## Test Ad Unit IDs

| Format | Test Ad Unit ID |
|---|---|
| Native Advanced | `ca-app-pub-3940256099942544/2247696110` |

---

## License

MIT © [Turtlebase](https://turtlebase.com) · Author: Umesh Dafda
