package com.turtlebase.admob;

import android.app.Activity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.google.android.gms.ads.nativead.MediaView;
import com.google.android.gms.ads.nativead.NativeAd;
import com.google.android.gms.ads.nativead.NativeAdView;

/**
 * Inline NativeAdViewHolder (Scrollable Embedded Version)
 */
public class NativeAdViewHolder {

    private static final String TAG = "TurtlebaseAdMob";

    private final AdMobNativeAdvancedPlugin plugin;
    private final String adId;
    private NativeAd nativeAd;
    private NativeAdView nativeAdView;

    private boolean isAttached = false;
    private boolean isVisible = false;

    public NativeAdViewHolder(
        AdMobNativeAdvancedPlugin plugin,
        String adId,
        NativeAd nativeAd,
        String position,
        int widthDp,
        int heightDp,
        int marginTopDp,
        int marginBottomDp,
        int xDp,
        int yDp,
        String template,
        String customLayoutName
    ) {
        this.plugin = plugin;
        this.adId = adId;
        this.nativeAd = nativeAd;

        buildView(plugin.getActivity(), template, customLayoutName);
    }

    private void buildView(Activity activity, String template, String customLayoutName) {

        int layoutResId = resolveLayoutResId(activity, template, customLayoutName);

        LayoutInflater inflater = LayoutInflater.from(activity);
        nativeAdView = (NativeAdView) inflater.inflate(layoutResId, null, false);

        bindAdAssets();

        Log.d(TAG, "NativeAdView built inline for adId: " + adId);
    }

    private void bindAdAssets() {

        if (nativeAdView == null || nativeAd == null) return;

        TextView headlineView = nativeAdView.findViewById(R.id.ad_headline);
        if (headlineView != null) {
            headlineView.setText(nativeAd.getHeadline());
            nativeAdView.setHeadlineView(headlineView);
        }

        TextView bodyView = nativeAdView.findViewById(R.id.ad_body);
        if (bodyView != null) {
            if (nativeAd.getBody() != null) {
                bodyView.setVisibility(View.VISIBLE);
                bodyView.setText(nativeAd.getBody());
            } else {
                bodyView.setVisibility(View.INVISIBLE);
            }
            nativeAdView.setBodyView(bodyView);
        }

        TextView cta = nativeAdView.findViewById(R.id.ad_call_to_action);
        if (cta != null) {
            if (nativeAd.getCallToAction() != null) {
                cta.setVisibility(View.VISIBLE);
                cta.setText(nativeAd.getCallToAction());
            } else {
                cta.setVisibility(View.INVISIBLE);
            }
            nativeAdView.setCallToActionView(cta);
        }

        ImageView icon = nativeAdView.findViewById(R.id.ad_app_icon);
        if (icon != null) {
            if (nativeAd.getIcon() != null) {
                icon.setVisibility(View.VISIBLE);
                icon.setImageDrawable(nativeAd.getIcon().getDrawable());
            } else {
                icon.setVisibility(View.GONE);
            }
            nativeAdView.setIconView(icon);
        }

        TextView advertiser = nativeAdView.findViewById(R.id.ad_advertiser);
        if (advertiser != null) {
            if (nativeAd.getAdvertiser() != null) {
                advertiser.setVisibility(View.VISIBLE);
                advertiser.setText(nativeAd.getAdvertiser());
            } else {
                advertiser.setVisibility(View.INVISIBLE);
            }
            nativeAdView.setAdvertiserView(advertiser);
        }

        RatingBar ratingBar = nativeAdView.findViewById(R.id.ad_stars);
        if (ratingBar != null) {
            if (nativeAd.getStarRating() != null) {
                ratingBar.setVisibility(View.VISIBLE);
                ratingBar.setRating(nativeAd.getStarRating().floatValue());
            } else {
                ratingBar.setVisibility(View.INVISIBLE);
            }
            nativeAdView.setStarRatingView(ratingBar);
        }

        MediaView mediaView = nativeAdView.findViewById(R.id.ad_media);
        if (mediaView != null) {
            nativeAdView.setMediaView(mediaView);
        }

        nativeAdView.setNativeAd(nativeAd);
    }

    /**
     * ✅ FIXED — INLINE ATTACH TO WEBVIEW PARENT (SCROLLABLE)
     */
    public void show(Activity activity) {

        if (nativeAdView == null) return;

        try {

            View webView = plugin.getBridge().getWebView();
            ViewGroup parent = (ViewGroup) webView.getParent();

            if (!isAttached) {

                FrameLayout.LayoutParams params =
                        new FrameLayout.LayoutParams(
                                FrameLayout.LayoutParams.MATCH_PARENT,
                                FrameLayout.LayoutParams.WRAP_CONTENT
                        );

                params.gravity = Gravity.TOP;

                parent.addView(nativeAdView, params);

                isAttached = true;
            }

            nativeAdView.setVisibility(View.VISIBLE);
            isVisible = true;

            Log.d(TAG, "NativeAd inline attached successfully: " + adId);

        } catch (Exception e) {
            Log.e(TAG, "Inline attach failed: " + e.getMessage(), e);
        }
    }

    public void hide() {
        if (nativeAdView != null) {
            nativeAdView.setVisibility(View.GONE);
            isVisible = false;
        }
    }

    public void destroy() {
        try {
            if (nativeAdView != null) {
                ViewGroup parent = (ViewGroup) nativeAdView.getParent();
                if (parent != null) {
                    parent.removeView(nativeAdView);
                }
                nativeAdView = null;
            }

            if (nativeAd != null) {
                nativeAd.destroy();
                nativeAd = null;
            }

            isAttached = false;
            isVisible = false;

            Log.d(TAG, "NativeAd destroyed inline: " + adId);

        } catch (Exception e) {
            Log.e(TAG, "Error destroying ad: " + e.getMessage(), e);
        }
    }

    private int resolveLayoutResId(Activity activity, String template, String customLayoutName) {

        if (customLayoutName != null && !customLayoutName.isEmpty()) {
            int resId = activity.getResources()
                    .getIdentifier(customLayoutName, "layout", activity.getPackageName());
            if (resId != 0) return resId;
        }

        if ("SMALL".equalsIgnoreCase(template)) {
            return R.layout.native_ad_small;
        } else if ("LARGE".equalsIgnoreCase(template)) {
            return R.layout.native_ad_large;
        } else {
            return R.layout.native_ad_medium;
        }
    }
}
