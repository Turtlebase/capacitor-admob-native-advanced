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
 * Wraps a loaded NativeAd and its NativeAdView.
 * Handles adding the view to the activity's root layout,
 * positioning, showing, hiding, updating, and destroying.
 *
 * @author Umesh Dafda <umesh@turtlebase.com>
 * @company Turtlebase
 */
public class NativeAdViewHolder {

    private static final String TAG = "TurtlebaseAdMob";

    private final AdMobNativeAdvancedPlugin plugin;
    private final String adId;
    private NativeAd nativeAd;
    private NativeAdView nativeAdView;

    // Current layout params
    private String position;
    private int widthDp;
    private int heightDp;
    private int marginTopDp;
    private int marginBottomDp;
    private int xDp;
    private int yDp;

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
        this.position = position;
        this.widthDp = widthDp;
        this.heightDp = heightDp;
        this.marginTopDp = marginTopDp;
        this.marginBottomDp = marginBottomDp;
        this.xDp = xDp;
        this.yDp = yDp;

        buildView(plugin.getActivity(), template, customLayoutName);
    }

    /**
     * Inflate the NativeAdView layout and bind NativeAd data to it.
     */
    private void buildView(Activity activity, String template, String customLayoutName) {
        int layoutResId = resolveLayoutResId(activity, template, customLayoutName);

        LayoutInflater inflater = LayoutInflater.from(activity);
        nativeAdView = (NativeAdView) inflater.inflate(layoutResId, null, false);

        // Bind ad assets
        bindAdAssets();

        Log.d(TAG, "NativeAdView built for adId: " + adId);
    }

    /**
     * Bind NativeAd fields to the NativeAdView's child views.
     */
    private void bindAdAssets() {
        if (nativeAdView == null || nativeAd == null) return;

        // Headline
        TextView headlineView = nativeAdView.findViewById(R.id.ad_headline);
        if (headlineView != null) {
            headlineView.setText(nativeAd.getHeadline());
            nativeAdView.setHeadlineView(headlineView);
        }

        // Body
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

        // Call to action
        TextView callToActionView = nativeAdView.findViewById(R.id.ad_call_to_action);
        if (callToActionView != null) {
            if (nativeAd.getCallToAction() != null) {
                callToActionView.setVisibility(View.VISIBLE);
                callToActionView.setText(nativeAd.getCallToAction());
            } else {
                callToActionView.setVisibility(View.INVISIBLE);
            }
            nativeAdView.setCallToActionView(callToActionView);
        }

        // Icon
        ImageView iconView = nativeAdView.findViewById(R.id.ad_app_icon);
        if (iconView != null) {
            if (nativeAd.getIcon() != null) {
                iconView.setVisibility(View.VISIBLE);
                iconView.setImageDrawable(nativeAd.getIcon().getDrawable());
            } else {
                iconView.setVisibility(View.GONE);
            }
            nativeAdView.setIconView(iconView);
        }

        // Advertiser
        TextView advertiserView = nativeAdView.findViewById(R.id.ad_advertiser);
        if (advertiserView != null) {
            if (nativeAd.getAdvertiser() != null) {
                advertiserView.setVisibility(View.VISIBLE);
                advertiserView.setText(nativeAd.getAdvertiser());
            } else {
                advertiserView.setVisibility(View.INVISIBLE);
            }
            nativeAdView.setAdvertiserView(advertiserView);
        }

        // Star rating
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

        // MediaView
        MediaView mediaView = nativeAdView.findViewById(R.id.ad_media);
        if (mediaView != null) {
            nativeAdView.setMediaView(mediaView);
        }

        // Register the NativeAdView — must be called LAST
        nativeAdView.setNativeAd(nativeAd);
    }

    /**
     * Attach the NativeAdView to the activity's root layout and position it.
     */
    public void show(Activity activity) {
        if (nativeAdView == null) {
            Log.w(TAG, "show() called but nativeAdView is null for adId: " + adId);
            return;
        }

        ViewGroup rootView = activity.getWindow().getDecorView()
            .findViewById(android.R.id.content);

        if (!isAttached) {
            FrameLayout.LayoutParams params = buildLayoutParams(activity);
            rootView.addView(nativeAdView, params);
            isAttached = true;
        } else {
            nativeAdView.setVisibility(View.VISIBLE);
        }

        isVisible = true;
        Log.d(TAG, "NativeAd shown for adId: " + adId);
    }

    /**
     * Hide the view (keeps it in layout, just invisible).
     */
    public void hide() {
        if (nativeAdView != null) {
            nativeAdView.setVisibility(View.INVISIBLE);
            isVisible = false;
        }
    }

    /**
     * Destroy the NativeAd and remove from parent.
     */
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
            Log.d(TAG, "NativeAd destroyed for adId: " + adId);
        } catch (Exception e) {
            Log.e(TAG, "Error destroying ad: " + e.getMessage(), e);
        }
    }

    /**
     * Update position/size of the ad view.
     */
    public void updateLayout(
        Activity activity,
        String newPosition,
        Integer newX,
        Integer newY,
        Integer newWidth,
        Integer newHeight,
        Integer newMarginTop,
        Integer newMarginBottom
    ) {
        if (newPosition != null) this.position = newPosition;
        if (newX != null) this.xDp = newX;
        if (newY != null) this.yDp = newY;
        if (newWidth != null) this.widthDp = newWidth;
        if (newHeight != null) this.heightDp = newHeight;
        if (newMarginTop != null) this.marginTopDp = newMarginTop;
        if (newMarginBottom != null) this.marginBottomDp = newMarginBottom;

        if (nativeAdView != null && isAttached) {
            FrameLayout.LayoutParams params = buildLayoutParams(activity);
            nativeAdView.setLayoutParams(params);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // HELPERS
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Resolve layout resource ID from template name or custom layout name.
     */
    private int resolveLayoutResId(Activity activity, String template, String customLayoutName) {
        if (customLayoutName != null && !customLayoutName.isEmpty()) {
            int resId = activity.getResources().getIdentifier(
                customLayoutName, "layout", activity.getPackageName());
            if (resId != 0) {
                Log.d(TAG, "Using custom layout: " + customLayoutName);
                return resId;
            } else {
                Log.w(TAG, "Custom layout not found: " + customLayoutName + ", falling back to template.");
            }
        }

        // Use built-in templates
        if ("SMALL".equalsIgnoreCase(template)) {
            return R.layout.native_ad_small;
        } else if ("LARGE".equalsIgnoreCase(template)) {
            return R.layout.native_ad_large;
        } else {
            return R.layout.native_ad_medium; // default MEDIUM
        }
    }

    /**
     * Convert dp to pixels.
     */
    private int dpToPx(Activity activity, int dp) {
        DisplayMetrics metrics = activity.getResources().getDisplayMetrics();
        return Math.round(dp * metrics.density);
    }

    /**
     * Build FrameLayout.LayoutParams based on position and dimensions.
     */
    private FrameLayout.LayoutParams buildLayoutParams(Activity activity) {
        int widthPx  = dpToPx(activity, widthDp);
        int heightPx = dpToPx(activity, heightDp);

        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(widthPx, heightPx);

        int gravity = resolveGravity(position);
        params.gravity = gravity;

        // Set margins based on position
        int topPx    = dpToPx(activity, marginTopDp);
        int bottomPx = dpToPx(activity, marginBottomDp);

        if ("CUSTOM".equalsIgnoreCase(position)) {
            params.leftMargin = dpToPx(activity, xDp);
            params.topMargin  = dpToPx(activity, yDp);
        } else {
            params.topMargin    = topPx;
            params.bottomMargin = bottomPx;
        }

        return params;
    }

    /**
     * Map position string to Android Gravity constant.
     */
    private int resolveGravity(String position) {
        if (position == null) return Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;

        switch (position.toUpperCase()) {
            case "TOP_LEFT":      return Gravity.TOP | Gravity.START;
            case "TOP_CENTER":    return Gravity.TOP | Gravity.CENTER_HORIZONTAL;
            case "TOP_RIGHT":     return Gravity.TOP | Gravity.END;
            case "BOTTOM_LEFT":   return Gravity.BOTTOM | Gravity.START;
            case "BOTTOM_RIGHT":  return Gravity.BOTTOM | Gravity.END;
            case "CENTER":        return Gravity.CENTER;
            case "CUSTOM":        return Gravity.TOP | Gravity.START;
            default:              return Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL; // BOTTOM_CENTER
        }
    }
}
