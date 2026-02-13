package com.turtlebase.admob;

import android.app.Activity;
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

public class NativeAdViewHolder {

    private static final String TAG = "TurtlebaseAdMob";

    private final AdMobNativeAdvancedPlugin plugin;
    private final String adId;
    private NativeAd nativeAd;
    private NativeAdView nativeAdView;
    private boolean isAttached = false;

    public NativeAdViewHolder(
            AdMobNativeAdvancedPlugin plugin,
            String adId,
            NativeAd nativeAd,
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

        bindAssets();
    }

    private void bindAssets() {

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

    public void show(Activity activity) {

        if (nativeAdView == null) return;

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
        Log.d(TAG, "Inline native ad attached");
    }

    public void hide() {
        if (nativeAdView != null) {
            nativeAdView.setVisibility(View.GONE);
        }
    }

    public void destroy() {
        if (nativeAdView != null) {
            ViewGroup parent = (ViewGroup) nativeAdView.getParent();
            if (parent != null) parent.removeView(nativeAdView);
            nativeAdView = null;
        }

        if (nativeAd != null) {
            nativeAd.destroy();
            nativeAd = null;
        }
    }

    private int resolveLayoutResId(Activity activity, String template, String customLayoutName) {

        if (customLayoutName != null) {
            int resId = activity.getResources()
                    .getIdentifier(customLayoutName, "layout", activity.getPackageName());
            if (resId != 0) return resId;
        }

        if ("SMALL".equalsIgnoreCase(template)) return R.layout.native_ad_small;
        if ("LARGE".equalsIgnoreCase(template)) return R.layout.native_ad_large;
        return R.layout.native_ad_medium;
    }
}
