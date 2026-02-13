package com.turtlebase.admob;

import android.app.Activity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.google.android.gms.ads.nativead.MediaView;
import com.google.android.gms.ads.nativead.NativeAd;
import com.google.android.gms.ads.nativead.NativeAdView;

public class NativeAdViewHolder {

    private static final String TAG = "TurtlebaseAdMob";

    private final AdMobNativeAdvancedPlugin plugin;
    private final String adId;

    private NativeAd nativeAd;
    private NativeAdView nativeAdView;

    private boolean isAttached = false;

    // 🔥 Layout values coming from JS (in DP)
    private int xDp = 0;
    private int yDp = 0;
    private int widthDp = 320;
    private int heightDp = 250;

    public NativeAdViewHolder(
            AdMobNativeAdvancedPlugin plugin,
            String adId,
            NativeAd nativeAd,
            int xDp,
            int yDp,
            int widthDp,
            int heightDp,
            String template,
            String customLayoutName
    ) {
        this.plugin = plugin;
        this.adId = adId;
        this.nativeAd = nativeAd;

        this.xDp = xDp;
        this.yDp = yDp;
        this.widthDp = widthDp;
        this.heightDp = heightDp;

        buildView(plugin.getActivity(), template, customLayoutName);
    }

    private void buildView(Activity activity, String template, String customLayoutName) {

        int layoutResId = resolveLayoutResId(activity, template, customLayoutName);

        LayoutInflater inflater = LayoutInflater.from(activity);
        nativeAdView = (NativeAdView) inflater.inflate(layoutResId, null, false);

        bindAssets();
    }

    private void bindAssets() {

        if (nativeAdView == null || nativeAd == null) return;

        TextView headline = nativeAdView.findViewById(R.id.ad_headline);
        if (headline != null) {
            headline.setText(nativeAd.getHeadline());
            nativeAdView.setHeadlineView(headline);
        }

        MediaView media = nativeAdView.findViewById(R.id.ad_media);
        if (media != null) {
            nativeAdView.setMediaView(media);
        }

        nativeAdView.setNativeAd(nativeAd);
    }

    // 🔥 MAIN FIX — POSITION USING MARGINS (NOT GRAVITY)
    public void show(Activity activity) {

        if (nativeAdView == null) return;

        try {

            View webView = plugin.getBridge().getWebView();
            ViewGroup parent = (ViewGroup) webView.getParent();

            int widthPx = dpToPx(activity, widthDp);
            int heightPx = dpToPx(activity, heightDp);
            int xPx = dpToPx(activity, xDp);
            int yPx = dpToPx(activity, yDp);

            if (!isAttached) {

                FrameLayout.LayoutParams params =
                        new FrameLayout.LayoutParams(widthPx, heightPx);

                params.leftMargin = xPx;
                params.topMargin = yPx;

                parent.addView(nativeAdView, params);
                isAttached = true;

            } else {

                FrameLayout.LayoutParams params =
                        (FrameLayout.LayoutParams) nativeAdView.getLayoutParams();

                params.width = widthPx;
                params.height = heightPx;
                params.leftMargin = xPx;
                params.topMargin = yPx;

                nativeAdView.setLayoutParams(params);
            }

            nativeAdView.setVisibility(View.VISIBLE);

            Log.d(TAG, "Native ad positioned at Y: " + yDp);

        } catch (Exception e) {
            Log.e(TAG, "Error showing native ad: " + e.getMessage(), e);
        }
    }

    public void updateLayout(Activity activity, Integer x, Integer y, Integer width, Integer height) {

        if (x != null) this.xDp = x;
        if (y != null) this.yDp = y;
        if (width != null) this.widthDp = width;
        if (height != null) this.heightDp = height;

        if (nativeAdView != null && isAttached) {
            show(activity);
        }
    }

    public void hide() {
        if (nativeAdView != null) {
            nativeAdView.setVisibility(View.GONE);
        }
    }

    public void destroy() {

        try {
            if (nativeAdView != null) {
                ViewGroup parent = (ViewGroup) nativeAdView.getParent();
                if (parent != null) parent.removeView(nativeAdView);
                nativeAdView = null;
            }

            if (nativeAd != null) {
                nativeAd.destroy();
                nativeAd = null;
            }

            isAttached = false;

            Log.d(TAG, "Native ad destroyed");

        } catch (Exception e) {
            Log.e(TAG, "Destroy error: " + e.getMessage(), e);
        }
    }

    private int resolveLayoutResId(Activity activity, String template, String customLayoutName) {

        if (customLayoutName != null && !customLayoutName.isEmpty()) {
            int resId = activity.getResources()
                    .getIdentifier(customLayoutName, "layout", activity.getPackageName());
            if (resId != 0) return resId;
        }

        if ("SMALL".equalsIgnoreCase(template)) return R.layout.native_ad_small;
        if ("LARGE".equalsIgnoreCase(template)) return R.layout.native_ad_large;
        return R.layout.native_ad_medium;
    }

    private int dpToPx(Activity activity, int dp) {
        DisplayMetrics metrics = activity.getResources().getDisplayMetrics();
        return Math.round(dp * metrics.density);
    }
}
