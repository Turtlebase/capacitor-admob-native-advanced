# Turtlebase AdMob Native Advanced Plugin
# Author: Umesh Dafda

# Keep AdMob classes
-keep class com.google.android.gms.ads.** { *; }
-keep class com.google.ads.** { *; }

# Keep plugin classes
-keep class com.turtlebase.admob.** { *; }

# Keep NativeAd view classes
-keepclassmembers class com.google.android.gms.ads.nativead.** { *; }
